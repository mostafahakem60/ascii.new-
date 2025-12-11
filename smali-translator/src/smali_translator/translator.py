from __future__ import annotations

from dataclasses import dataclass

from .cache import TranslationCache, code_hash
from .emitter import PseudocodeEmitter
from .errors import TranslationError
from .ir import SmaliFile
from .parser import SmaliParser


@dataclass
class TranslationResult:
    pseudocode: str | None
    ir: SmaliFile | None
    errors: list[TranslationError]
    cached: bool


class SmaliTranslator:
    def __init__(self, *, cache: TranslationCache[TranslationResult] | None = None) -> None:
        self._parser = SmaliParser()
        self._emitter = PseudocodeEmitter()
        self._cache = cache or TranslationCache()

    def translate(self, code: str, *, use_cache: bool = True) -> TranslationResult:
        key = code_hash(code)
        if use_cache:
            cached = self._cache.get(key)
            if cached is not None:
                return TranslationResult(
                    pseudocode=cached.pseudocode,
                    ir=cached.ir,
                    errors=list(cached.errors),
                    cached=True,
                )

        file, parse_errors = self._parser.parse(code)

        if file is None:
            res = TranslationResult(pseudocode=None, ir=None, errors=parse_errors, cached=False)
            if use_cache:
                self._cache.set(key, res)
            return res

        if parse_errors:
            res = TranslationResult(pseudocode=None, ir=file, errors=parse_errors, cached=False)
            if use_cache:
                self._cache.set(key, res)
            return res

        emit = self._emitter.emit(file)
        errors: list[TranslationError] = []
        errors.extend(parse_errors)
        errors.extend(emit.errors)

        res = TranslationResult(
            pseudocode=emit.pseudocode,
            ir=file,
            errors=errors,
            cached=False,
        )
        if use_cache:
            self._cache.set(key, res)
        return res
