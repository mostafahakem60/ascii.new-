from __future__ import annotations

import hashlib
import threading
from dataclasses import dataclass
from typing import Generic, TypeVar


def code_hash(code: str) -> str:
    normalized = "\n".join(
        line.rstrip() for line in code.replace("\r\n", "\n").split("\n")
    )
    return hashlib.sha256(normalized.encode("utf-8")).hexdigest()


T = TypeVar("T")


@dataclass(frozen=True)
class CacheEntry(Generic[T]):
    key: str
    value: T


class TranslationCache(Generic[T]):
    def __init__(self) -> None:
        self._lock = threading.Lock()
        self._by_key: dict[str, T] = {}

    def get(self, key: str) -> T | None:
        with self._lock:
            return self._by_key.get(key)

    def set(self, key: str, value: T) -> None:
        with self._lock:
            self._by_key[key] = value

    def clear(self) -> None:
        with self._lock:
            self._by_key.clear()
