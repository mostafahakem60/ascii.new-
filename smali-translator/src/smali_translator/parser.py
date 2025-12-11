from __future__ import annotations

import re

from .errors import ParseError, TranslationError
from .ir import (
    BinOp,
    Const,
    Goto,
    If,
    Invoke,
    Label,
    MethodRef,
    Move,
    Return,
    SmaliFile,
    SmaliMethod,
    UnknownInstruction,
)
from .lexer import LexedLine, SmaliLexer
from .smali_types import parse_method_ref, parse_type_list


class SmaliParser:
    def __init__(self) -> None:
        self._lexer = SmaliLexer()

    def parse(self, code: str) -> tuple[SmaliFile | None, list[TranslationError]]:
        lexed = self._lexer.lex(code)
        errors: list[TranslationError] = list(lexed.errors)

        methods: list[SmaliMethod] = []
        cur: SmaliMethod | None = None
        pending_invoke: Invoke | None = None

        def ensure_method(line: int) -> SmaliMethod:
            nonlocal cur
            if cur is None:
                cur = SmaliMethod(
                    name="<anonymous>",
                    param_descriptors=(),
                    return_descriptor="V",
                )
                methods.append(cur)
            return cur

        for line in lexed.lines:
            if line.kind == "directive":
                op = line.opcode or ""
                if op == ".method":
                    m, err = _parse_method_header(line)
                    if err:
                        errors.append(err)
                        cur = None
                    else:
                        cur = m
                        methods.append(m)
                    pending_invoke = None
                    continue

                if op == ".end":
                    if (line.operands or "").strip() == "method":
                        cur = None
                        pending_invoke = None
                    continue

                if op in (".locals", ".registers"):
                    method = ensure_method(line.line)
                    count, err = _parse_int((line.operands or "").strip())
                    if err:
                        errors.append(
                            ParseError(
                                f"invalid {op} count: {err}",
                                line=line.line,
                                snippet=line.text,
                                code="INVALID_DIRECTIVE",
                            )
                        )
                    else:
                        if op == ".locals":
                            method.locals_count = count
                        else:
                            method.registers_count = count
                    continue

                continue

            if line.kind == "label":
                method = ensure_method(line.line)
                method.instructions.append(
                    Label(line=line.line, raw=line.text, name=line.opcode or "")
                )
                pending_invoke = None
                continue

            if line.kind != "instruction":
                continue

            method = ensure_method(line.line)
            opcode = line.opcode or ""
            operands = (line.operands or "").strip()

            if opcode.startswith("const"):
                ins, err = _parse_const(opcode, operands, line)
                if err:
                    errors.append(err)
                else:
                    method.instructions.append(ins)
                pending_invoke = None
                continue

            if opcode.startswith("move-result"):
                if pending_invoke is None:
                    errors.append(
                        ParseError(
                            "move-result without preceding invoke",
                            line=line.line,
                            snippet=line.text,
                            code="MOVE_RESULT_WITHOUT_INVOKE",
                        )
                    )
                    continue

                dest = operands.strip()
                if not _is_register(dest):
                    errors.append(
                        ParseError(
                            "invalid move-result destination register",
                            line=line.line,
                            snippet=line.text,
                            code="INVALID_REGISTER",
                        )
                    )
                    continue

                pending_invoke.result = dest
                pending_invoke = None
                continue

            if opcode.startswith("move"):
                ins, err = _parse_move(opcode, operands, line)
                if err:
                    errors.append(err)
                else:
                    method.instructions.append(ins)
                pending_invoke = None
                continue

            if opcode.startswith("return"):
                ins, err = _parse_return(opcode, operands, line)
                if err:
                    errors.append(err)
                else:
                    method.instructions.append(ins)
                pending_invoke = None
                continue

            if opcode.startswith("goto"):
                target = operands.strip()
                if not target.startswith(":"):
                    errors.append(
                        ParseError(
                            "goto target must be a label",
                            line=line.line,
                            snippet=line.text,
                            code="INVALID_GOTO",
                        )
                    )
                else:
                    method.instructions.append(
                        Goto(
                            line=line.line,
                            raw=line.text,
                            target=target[1:],
                        )
                    )
                pending_invoke = None
                continue

            if opcode.startswith("if-"):
                ins, err = _parse_if(opcode, operands, line)
                if err:
                    errors.append(err)
                else:
                    method.instructions.append(ins)
                pending_invoke = None
                continue

            if opcode.startswith("invoke-"):
                ins, err = _parse_invoke(opcode, operands, line)
                if err:
                    errors.append(err)
                    pending_invoke = None
                else:
                    method.instructions.append(ins)
                    pending_invoke = ins
                continue

            if _is_binop(opcode):
                ins, err = _parse_binop(opcode, operands, line)
                if err:
                    errors.append(err)
                else:
                    method.instructions.append(ins)
                pending_invoke = None
                continue

            method.instructions.append(
                UnknownInstruction(
                    line=line.line,
                    raw=line.text,
                    opcode=opcode,
                    operands=operands,
                )
            )
            pending_invoke = None

        if errors:
            # Return IR if we have at least something parseable, but callers can decide whether to emit.
            return SmaliFile(methods=methods) if methods else None, errors

        return SmaliFile(methods=methods), errors


_METHOD_SIG_RE = re.compile(r"(?P<name>[^\(]+)\((?P<params>[^\)]*)\)(?P<ret>.+)")


def _parse_method_header(line: LexedLine) -> tuple[SmaliMethod | None, ParseError | None]:
    text = (line.operands or "").strip()
    if not text:
        return None, ParseError(
            "missing .method signature",
            line=line.line,
            snippet=line.text,
            code="MISSING_METHOD_SIGNATURE",
        )

    parts = text.split()
    sig_token = None
    access_flags: list[str] = []
    for p in parts:
        if "(" in p and ")" in p:
            sig_token = p
            break
        access_flags.append(p)

    if sig_token is None:
        return None, ParseError(
            "could not find method signature token",
            line=line.line,
            snippet=line.text,
            code="INVALID_METHOD_SIGNATURE",
        )

    m = _METHOD_SIG_RE.fullmatch(sig_token)
    if not m:
        return None, ParseError(
            "invalid method signature",
            line=line.line,
            snippet=line.text,
            code="INVALID_METHOD_SIGNATURE",
        )

    name = m.group("name")
    params = m.group("params")
    ret = m.group("ret")

    try:
        param_descriptors = tuple(parse_type_list(params))
    except ValueError as e:
        return None, ParseError(
            f"invalid method parameter descriptors: {e}",
            line=line.line,
            snippet=line.text,
            code="INVALID_METHOD_SIGNATURE",
        )

    return (
        SmaliMethod(
            name=name,
            param_descriptors=param_descriptors,
            return_descriptor=ret,
            access_flags=tuple(access_flags),
        ),
        None,
    )


def _parse_const(opcode: str, operands: str, line: LexedLine) -> tuple[Const | None, ParseError | None]:
    parts = [p.strip() for p in operands.split(",")]
    if len(parts) != 2 or not parts[0] or not parts[1]:
        return None, ParseError(
            f"invalid {opcode} operands",
            line=line.line,
            snippet=line.text,
            code="INVALID_CONST",
        )

    dest = parts[0]
    if not _is_register(dest):
        return None, ParseError(
            "invalid destination register",
            line=line.line,
            snippet=line.text,
            code="INVALID_REGISTER",
        )

    val_text = parts[1]
    if opcode == "const-string":
        s, err = _parse_string(val_text)
        if err:
            return None, ParseError(
                f"invalid string literal: {err}",
                line=line.line,
                snippet=line.text,
                code="INVALID_STRING",
            )
        value: int | str = s
    else:
        n, err = _parse_int(val_text)
        if err:
            return None, ParseError(
                f"invalid literal: {err}",
                line=line.line,
                snippet=line.text,
                code="INVALID_LITERAL",
            )
        value = n

    return (
        Const(line=line.line, raw=line.text, dest=dest, value=value, const_kind=opcode),
        None,
    )


def _parse_move(opcode: str, operands: str, line: LexedLine):
    parts = [p.strip() for p in operands.split(",")]
    if len(parts) != 2:
        return None, ParseError(
            f"invalid {opcode} operands",
            line=line.line,
            snippet=line.text,
            code="INVALID_MOVE",
        )

    dest, src = parts
    if not _is_register(dest) or not _is_register(src):
        return None, ParseError(
            "invalid move register",
            line=line.line,
            snippet=line.text,
            code="INVALID_REGISTER",
        )

    return Move(line=line.line, raw=line.text, dest=dest, src=src, move_kind=opcode), None


def _parse_return(opcode: str, operands: str, line: LexedLine):
    if opcode == "return-void":
        if operands.strip():
            return None, ParseError(
                "return-void takes no operands",
                line=line.line,
                snippet=line.text,
                code="INVALID_RETURN",
            )
        return Return(line=line.line, raw=line.text, value=None, return_kind=opcode), None

    reg = operands.strip()
    if not _is_register(reg):
        return None, ParseError(
            "invalid return register",
            line=line.line,
            snippet=line.text,
            code="INVALID_REGISTER",
        )

    return Return(line=line.line, raw=line.text, value=reg, return_kind=opcode), None


def _parse_if(opcode: str, operands: str, line: LexedLine):
    parts = [p.strip() for p in operands.split(",")]

    if opcode.endswith("z"):
        if len(parts) != 2:
            return None, ParseError(
                "invalid if-?z operands",
                line=line.line,
                snippet=line.text,
                code="INVALID_IF",
            )
        left = parts[0]
        if not _is_register(left):
            return None, ParseError(
                "invalid if register",
                line=line.line,
                snippet=line.text,
                code="INVALID_REGISTER",
            )
        target = parts[1]
        if not target.startswith(":"):
            return None, ParseError(
                "if target must be a label",
                line=line.line,
                snippet=line.text,
                code="INVALID_LABEL",
            )
        return (
            If(
                line=line.line,
                raw=line.text,
                cond=opcode,
                left=left,
                right=None,
                target=target[1:],
            ),
            None,
        )

    if len(parts) != 3:
        return None, ParseError(
            "invalid if operands",
            line=line.line,
            snippet=line.text,
            code="INVALID_IF",
        )

    left, right, target = parts
    if not _is_register(left) or not _is_register(right):
        return None, ParseError(
            "invalid if register",
            line=line.line,
            snippet=line.text,
            code="INVALID_REGISTER",
        )

    if not target.startswith(":"):
        return None, ParseError(
            "if target must be a label",
            line=line.line,
            snippet=line.text,
            code="INVALID_LABEL",
        )

    return (
        If(
            line=line.line,
            raw=line.text,
            cond=opcode,
            left=left,
            right=right,
            target=target[1:],
        ),
        None,
    )


def _parse_invoke(opcode: str, operands: str, line: LexedLine):
    # invoke-virtual {v0, v1}, Lfoo;->bar(I)I
    # invoke-virtual/range {v0 .. v3}, Lfoo;->bar(III)I
    m = re.match(r"^\{(?P<regs>[^}]*)\}\s*,\s*(?P<method>.+)$", operands)
    if not m:
        return None, ParseError(
            "invalid invoke operands",
            line=line.line,
            snippet=line.text,
            code="INVALID_INVOKE",
        )

    regs_text = m.group("regs").strip()
    method_text = m.group("method").strip()

    args: list[str] = []
    if ".." in regs_text:
        rm = re.match(r"^(?P<start>[vp]\d+)\s*\.\.\s*(?P<end>[vp]\d+)$", regs_text)
        if not rm:
            return None, ParseError(
                "invalid invoke/range register list",
                line=line.line,
                snippet=line.text,
                code="INVALID_REGISTER_LIST",
            )
        start = rm.group("start")
        end = rm.group("end")
        if start[0] != end[0]:
            return None, ParseError(
                "invoke/range registers must share prefix (v or p)",
                line=line.line,
                snippet=line.text,
                code="INVALID_REGISTER_LIST",
            )
        start_i = int(start[1:])
        end_i = int(end[1:])
        if end_i < start_i:
            return None, ParseError(
                "invoke/range end before start",
                line=line.line,
                snippet=line.text,
                code="INVALID_REGISTER_LIST",
            )
        args = [f"{start[0]}{i}" for i in range(start_i, end_i + 1)]
    else:
        if regs_text:
            args = [r.strip() for r in regs_text.split(",") if r.strip()]

    for r in args:
        if not _is_register(r):
            return None, ParseError(
                "invalid register in invoke list",
                line=line.line,
                snippet=line.text,
                code="INVALID_REGISTER",
            )

    try:
        owner, name, params, ret = parse_method_ref(method_text)
    except ValueError as e:
        return None, ParseError(
            f"invalid method reference: {e}",
            line=line.line,
            snippet=line.text,
            code="INVALID_METHOD_REF",
        )

    return (
        Invoke(
            line=line.line,
            raw=line.text,
            invoke_kind=opcode,
            args=tuple(args),
            method=MethodRef(owner, name, params, ret),
        ),
        None,
    )


def _parse_binop(opcode: str, operands: str, line: LexedLine):
    parts = [p.strip() for p in operands.split(",")]

    if opcode.endswith("/2addr"):
        if len(parts) != 2:
            return None, ParseError(
                "invalid /2addr operands",
                line=line.line,
                snippet=line.text,
                code="INVALID_BINOP",
            )
        dest, src = parts
        if not _is_register(dest) or not _is_register(src):
            return None, ParseError(
                "invalid register in binop",
                line=line.line,
                snippet=line.text,
                code="INVALID_REGISTER",
            )
        op = opcode.split("-", 1)[0]
        return (
            BinOp(
                line=line.line,
                raw=line.text,
                op=op,
                dest=dest,
                left=dest,
                right=src,
            ),
            None,
        )

    if len(parts) != 3:
        return None, ParseError(
            "invalid binop operands",
            line=line.line,
            snippet=line.text,
            code="INVALID_BINOP",
        )

    dest, left, right = parts
    if not _is_register(dest) or not _is_register(left) or not _is_register(right):
        return None, ParseError(
            "invalid register in binop",
            line=line.line,
            snippet=line.text,
            code="INVALID_REGISTER",
        )

    op = opcode.split("-", 1)[0]
    return BinOp(line=line.line, raw=line.text, op=op, dest=dest, left=left, right=right), None


def _parse_int(text: str) -> tuple[int | None, str | None]:
    t = text.strip().lower()
    if not t:
        return None, "empty"
    try:
        if t.startswith("0x") or t.startswith("-0x"):
            return int(t, 16), None
        return int(t, 10), None
    except ValueError as e:
        return None, str(e)


_STRING_RE = re.compile(r'^"(?P<body>(?:\\.|[^"\\])*)"$')


def _parse_string(text: str) -> tuple[str | None, str | None]:
    m = _STRING_RE.match(text.strip())
    if not m:
        return None, "not a quoted string"

    body = m.group("body")
    try:
        return bytes(body, "utf-8").decode("unicode_escape"), None
    except Exception as e:  # noqa: BLE001
        return None, str(e)


_REG_RE = re.compile(r"^[vp]\d+$")


def _is_register(text: str) -> bool:
    return bool(_REG_RE.fullmatch(text.strip()))


def _is_binop(opcode: str) -> bool:
    base = opcode
    if base.endswith("/2addr"):
        base = base[: -len("/2addr")]
    return base.startswith(
        (
            "add-",
            "sub-",
            "mul-",
            "div-",
            "rem-",
            "and-",
            "or-",
            "xor-",
            "shl-",
            "shr-",
            "ushr-",
        )
    )
