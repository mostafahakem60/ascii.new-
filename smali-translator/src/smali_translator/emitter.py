from __future__ import annotations

from dataclasses import dataclass

from .errors import EmitError
from .ir import (
    BinOp,
    Const,
    Goto,
    If,
    Invoke,
    Label,
    Move,
    Return,
    SmaliFile,
    SmaliMethod,
    UnknownInstruction,
)
from .smali_types import descriptor_to_java
from .type_inference import TypeEnv, invoke_return_java_type, seed_params


@dataclass
class EmitResult:
    pseudocode: str | None
    errors: list[EmitError]


class PseudocodeEmitter:
    def emit(self, file: SmaliFile) -> EmitResult:
        out: list[str] = []
        errors: list[EmitError] = []

        for m in file.methods:
            method_text, method_errors = self.emit_method(m)
            errors.extend(method_errors)
            if method_text:
                out.append(method_text)

        return EmitResult(pseudocode="\n\n".join(out) if out else None, errors=errors)

    def emit_method(self, method: SmaliMethod) -> tuple[str | None, list[EmitError]]:
        errors: list[EmitError] = []
        out: list[str] = []

        ret_java = descriptor_to_java(method.return_descriptor)
        params = []
        if method.is_static:
            for i, desc in enumerate(method.param_descriptors):
                params.append(f"{descriptor_to_java(desc)} p{i}")
        else:
            params.append("Object p0")
            for i, desc in enumerate(method.param_descriptors, start=1):
                params.append(f"{descriptor_to_java(desc)} p{i}")

        out.append(f"{ret_java} {method.name}({', '.join(params)}) {{")

        env = seed_params(method)
        declared: set[str] = set(env.regs.keys())

        for ins in method.instructions:
            try:
                line_text = self._emit_instruction(ins, env=env, declared=declared)
            except Exception as e:  # noqa: BLE001
                errors.append(
                    EmitError(
                        f"failed to emit instruction: {e}",
                        line=ins.line,
                        snippet=ins.raw,
                        code="EMIT_EXCEPTION",
                    )
                )
                continue

            if line_text is None:
                continue
            out.append(f"    {line_text}")

        out.append("}")
        return "\n".join(out), errors

    def _emit_instruction(
        self,
        ins,
        *,
        env: TypeEnv,
        declared: set[str],
    ) -> str | None:
        if isinstance(ins, Label):
            return f"{ins.name}:"

        if isinstance(ins, Const):
            if isinstance(ins.value, str):
                env.set_type(ins.dest, "String")
                rhs = _java_string(ins.value)
            else:
                env.set_type(ins.dest, "int")
                rhs = str(ins.value)
            return _assign(ins.dest, rhs, env=env, declared=declared)

        if isinstance(ins, BinOp):
            env.set_type(ins.dest, "int")
            op = _binop_symbol(ins.op)
            return _assign(ins.dest, f"{ins.left} {op} {ins.right}", env=env, declared=declared)

        if isinstance(ins, Invoke):
            # For invoke-* we emit either a statement or an assignment if there is a move-result.
            recv, args = _invoke_receiver_and_args(ins)
            call = f"{recv}.{ins.method.name}({', '.join(args)})" if recv else f"{ins.method.name}({', '.join(args)})"

            if ins.result:
                rtype = invoke_return_java_type(ins)
                env.set_type(ins.result, rtype)
                return _assign(ins.result, call, env=env, declared=declared)

            return f"{call};"

        if isinstance(ins, Return):
            if ins.value is None:
                return "return;"
            return f"return {ins.value};"

        if isinstance(ins, Goto):
            return f"goto {ins.target};"

        if isinstance(ins, If):
            cond = _if_to_java(ins)
            return f"if ({cond}) goto {ins.target};"

        if isinstance(ins, UnknownInstruction):
            return f"/* unsupported: {ins.opcode} {ins.operands} */"

        if isinstance(ins, Move):
            src_type = env.get(ins.src).java_type
            env.set_type(ins.dest, src_type)
            return _assign(ins.dest, ins.src, env=env, declared=declared)

        return None


def _invoke_receiver_and_args(ins: Invoke) -> tuple[str | None, list[str]]:
    if ins.invoke_kind.startswith("invoke-static"):
        return None, list(ins.args)

    if not ins.args:
        return "this", []

    recv = ins.args[0]
    return recv, list(ins.args[1:])


def _assign(dest: str, rhs: str, *, env: TypeEnv, declared: set[str]) -> str:
    if dest not in declared:
        declared.add(dest)
        jtype = env.get(dest).java_type
        return f"{jtype} {dest} = {rhs};"
    return f"{dest} = {rhs};"


def _java_string(value: str) -> str:
    escaped = (
        value.replace("\\", "\\\\")
        .replace('"', '\\"')
        .replace("\n", "\\n")
        .replace("\r", "\\r")
        .replace("\t", "\\t")
    )
    return f'"{escaped}"'


def _binop_symbol(op: str) -> str:
    return {
        "add": "+",
        "sub": "-",
        "mul": "*",
        "div": "/",
        "rem": "%",
        "and": "&",
        "or": "|",
        "xor": "^",
        "shl": "<<",
        "shr": ">>",
        "ushr": ">>>",
    }.get(op, op)


def _if_to_java(ins: If) -> str:
    if ins.cond.endswith("z"):
        op = ins.cond[3:-1]
        # eqz, nez, ltz, lez, gtz, gez
        if op == "eq":
            return f"{ins.left} == 0"
        if op == "ne":
            return f"{ins.left} != 0"
        if op == "lt":
            return f"{ins.left} < 0"
        if op == "le":
            return f"{ins.left} <= 0"
        if op == "gt":
            return f"{ins.left} > 0"
        if op == "ge":
            return f"{ins.left} >= 0"
        return f"{ins.left} /* {ins.cond} */ 0"

    op = ins.cond[3:]
    sym = {
        "eq": "==",
        "ne": "!=",
        "lt": "<",
        "le": "<=",
        "gt": ">",
        "ge": ">=",
    }.get(op, op)
    right = ins.right or "0"
    return f"{ins.left} {sym} {right}"
