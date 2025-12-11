from __future__ import annotations

from dataclasses import dataclass, field


@dataclass(frozen=True)
class SmaliType:
    descriptor: str

    def to_java(self, *, short: bool = True) -> str:
        from .smali_types import descriptor_to_java

        return descriptor_to_java(self.descriptor, short=short)


@dataclass(frozen=True)
class MethodRef:
    owner_descriptor: str
    name: str
    param_descriptors: tuple[str, ...]
    return_descriptor: str

    @property
    def owner_type(self) -> SmaliType:
        return SmaliType(self.owner_descriptor)

    @property
    def return_type(self) -> SmaliType:
        return SmaliType(self.return_descriptor)

    @property
    def param_types(self) -> tuple[SmaliType, ...]:
        return tuple(SmaliType(p) for p in self.param_descriptors)


@dataclass
class SmaliFile:
    methods: list[SmaliMethod] = field(default_factory=list)


@dataclass
class SmaliMethod:
    name: str
    param_descriptors: tuple[str, ...]
    return_descriptor: str
    access_flags: tuple[str, ...] = ()
    locals_count: int | None = None
    registers_count: int | None = None
    instructions: list[Instruction] = field(default_factory=list)

    @property
    def is_static(self) -> bool:
        return "static" in self.access_flags


@dataclass
class Instruction:
    line: int
    raw: str


@dataclass
class Label(Instruction):
    name: str


@dataclass
class Const(Instruction):
    dest: str
    value: int | str
    const_kind: str


@dataclass
class Move(Instruction):
    dest: str
    src: str
    move_kind: str


@dataclass
class BinOp(Instruction):
    op: str
    dest: str
    left: str
    right: str


@dataclass
class Invoke(Instruction):
    invoke_kind: str
    args: tuple[str, ...]
    method: MethodRef
    result: str | None = None


@dataclass
class Return(Instruction):
    value: str | None
    return_kind: str


@dataclass
class Goto(Instruction):
    target: str


@dataclass
class If(Instruction):
    cond: str
    left: str
    right: str | None
    target: str


@dataclass
class UnknownInstruction(Instruction):
    opcode: str
    operands: str
