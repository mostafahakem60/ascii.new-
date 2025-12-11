from __future__ import annotations

from dataclasses import dataclass

from .errors import LexError


@dataclass(frozen=True)
class LexedLine:
    line: int
    text: str
    kind: str
    opcode: str | None = None
    operands: str | None = None


@dataclass
class LexResult:
    lines: list[LexedLine]
    errors: list[LexError]


class SmaliLexer:
    def lex(self, code: str) -> LexResult:
        lines: list[LexedLine] = []
        errors: list[LexError] = []

        for idx, raw in enumerate(code.splitlines(), start=1):
            text = raw.rstrip("\n")
            stripped = _strip_comment(text).strip()
            if not stripped:
                continue

            if stripped.startswith(":"):
                name = stripped[1:].strip()
                if not name:
                    errors.append(
                        LexError(
                            "empty label",
                            line=idx,
                            column=1,
                            snippet=text,
                            code="EMPTY_LABEL",
                        )
                    )
                    continue
                lines.append(LexedLine(line=idx, text=text, kind="label", opcode=name))
                continue

            if stripped.startswith("."):
                parts = stripped.split(None, 1)
                directive = parts[0]
                args = parts[1] if len(parts) > 1 else ""
                lines.append(
                    LexedLine(
                        line=idx,
                        text=text,
                        kind="directive",
                        opcode=directive,
                        operands=args,
                    )
                )
                continue

            parts = stripped.split(None, 1)
            opcode = parts[0]
            operands = parts[1] if len(parts) > 1 else ""
            lines.append(
                LexedLine(
                    line=idx,
                    text=text,
                    kind="instruction",
                    opcode=opcode,
                    operands=operands,
                )
            )

        return LexResult(lines=lines, errors=errors)


def _strip_comment(line: str) -> str:
    # Smali comments start with # and run to end-of-line.
    # Keep # inside quoted strings.
    in_str = False
    escaped = False
    for i, ch in enumerate(line):
        if in_str:
            if escaped:
                escaped = False
                continue
            if ch == "\\":
                escaped = True
                continue
            if ch == '"':
                in_str = False
                continue
            continue

        if ch == '"':
            in_str = True
            continue

        if ch == "#":
            return line[:i]

    return line
