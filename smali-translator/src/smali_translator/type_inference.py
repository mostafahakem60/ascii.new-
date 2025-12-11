from __future__ import annotations

from dataclasses import dataclass, field

from .ir import Invoke, SmaliMethod
from .smali_types import descriptor_to_java


@dataclass
class RegisterInfo:
    java_type: str = "Object"
    defined: bool = False


@dataclass
class TypeEnv:
    regs: dict[str, RegisterInfo] = field(default_factory=dict)

    def get(self, reg: str) -> RegisterInfo:
        return self.regs.setdefault(reg, RegisterInfo())

    def set_type(self, reg: str, java_type: str) -> None:
        info = self.get(reg)
        info.java_type = java_type
        info.defined = True


def seed_params(method: SmaliMethod) -> TypeEnv:
    env = TypeEnv()

    if method.is_static:
        first_param_reg = 0
    else:
        env.set_type("p0", "Object")
        first_param_reg = 1

    for idx, desc in enumerate(method.param_descriptors):
        reg = f"p{first_param_reg + idx}"
        env.set_type(reg, descriptor_to_java(desc))

    return env


def invoke_return_java_type(invoke: Invoke) -> str:
    return descriptor_to_java(invoke.method.return_descriptor)
