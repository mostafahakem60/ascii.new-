from __future__ import annotations

import re


_PRIMITIVE_MAP: dict[str, str] = {
    "V": "void",
    "Z": "boolean",
    "B": "byte",
    "S": "short",
    "C": "char",
    "I": "int",
    "J": "long",
    "F": "float",
    "D": "double",
}


def descriptor_to_java(descriptor: str, *, short: bool = True) -> str:
    desc = descriptor.strip()
    if not desc:
        return "Object"

    dims = 0
    while desc.startswith("["):
        dims += 1
        desc = desc[1:]

    base = _PRIMITIVE_MAP.get(desc)
    if base is None:
        m = re.fullmatch(r"L(?P<name>[^;]+);", desc)
        if m:
            dotted = m.group("name").replace("/", ".")
            base = dotted.split(".")[-1] if short else dotted
        else:
            base = "Object"

    return base + "[]" * dims


def parse_type_list(descriptors: str) -> list[str]:
    out: list[str] = []
    i = 0
    while i < len(descriptors):
        ch = descriptors[i]
        if ch == "[":
            start = i
            i += 1
            while i < len(descriptors) and descriptors[i] == "[":
                i += 1
            if i >= len(descriptors):
                raise ValueError("unterminated array descriptor")
            if descriptors[i] == "L":
                semi = descriptors.find(";", i)
                if semi == -1:
                    raise ValueError("unterminated object descriptor")
                i = semi + 1
                out.append(descriptors[start:i])
            else:
                i += 1
                out.append(descriptors[start:i])
            continue

        if ch == "L":
            semi = descriptors.find(";", i)
            if semi == -1:
                raise ValueError("unterminated object descriptor")
            out.append(descriptors[i : semi + 1])
            i = semi + 1
            continue

        if ch in _PRIMITIVE_MAP:
            out.append(ch)
            i += 1
            continue

        raise ValueError(f"invalid type descriptor char: {ch!r}")

    return out


_METHOD_REF_RE = re.compile(
    r"^(?P<owner>L[^;]+;)->(?P<name>[^\(]+)\((?P<params>[^\)]*)\)(?P<ret>.+)$"
)


def parse_method_ref(text: str) -> tuple[str, str, tuple[str, ...], str]:
    m = _METHOD_REF_RE.match(text.strip())
    if not m:
        raise ValueError("invalid method reference")

    owner = m.group("owner")
    name = m.group("name")
    params = m.group("params")
    ret = m.group("ret")

    param_list = tuple(parse_type_list(params))
    return owner, name, param_list, ret
