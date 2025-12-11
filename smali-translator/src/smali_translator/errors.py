from __future__ import annotations

from dataclasses import dataclass
from typing import Any


@dataclass(frozen=True)
class TranslationError:
    kind: str
    message: str
    line: int | None = None
    column: int | None = None
    snippet: str | None = None
    code: str | None = None

    def to_dict(self) -> dict[str, Any]:
        return {
            "kind": self.kind,
            "message": self.message,
            "line": self.line,
            "column": self.column,
            "snippet": self.snippet,
            "code": self.code,
        }


class LexError(TranslationError):
    def __init__(
        self,
        message: str,
        *,
        line: int | None = None,
        column: int | None = None,
        snippet: str | None = None,
        code: str | None = None,
    ) -> None:
        super().__init__(
            kind="lex",
            message=message,
            line=line,
            column=column,
            snippet=snippet,
            code=code,
        )


class ParseError(TranslationError):
    def __init__(
        self,
        message: str,
        *,
        line: int | None = None,
        column: int | None = None,
        snippet: str | None = None,
        code: str | None = None,
    ) -> None:
        super().__init__(
            kind="parse",
            message=message,
            line=line,
            column=column,
            snippet=snippet,
            code=code,
        )


class EmitError(TranslationError):
    def __init__(
        self,
        message: str,
        *,
        line: int | None = None,
        column: int | None = None,
        snippet: str | None = None,
        code: str | None = None,
    ) -> None:
        super().__init__(
            kind="emit",
            message=message,
            line=line,
            column=column,
            snippet=snippet,
            code=code,
        )
