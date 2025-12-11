# Supported Smali Format Documentation

This document provides detailed information about the Smali format supported by the translator and common patterns you'll encounter.

## Overview

Smali is the human-readable representation of Dalvik bytecode (Android's virtual machine bytecode). It's similar to assembly language for the JVM, but specifically designed for Android.

## File Structure

A typical Smali file has the following structure:

```smali
.class <access-flags> <class-name>
.super <superclass-name>
.implements <interface-name>    # Optional, can be multiple

.source "<source-file-name>"    # Optional

# Fields
.field <access-flags> <field-name>:<field-type>

# Methods
.method <access-flags> <method-name>(<parameter-types>)<return-type>
    # Method body
.end method
```

## Class Declaration

### Format
```smali
.class <access-flags> L<package>/<class-name>;
```

### Examples
```smali
.class public Lcom/example/MyClass;
.class public final Lcom/example/FinalClass;
.class public abstract Lcom/example/AbstractClass;
.class public interface Lcom/example/MyInterface;
```

### Access Flags
- `public` - Publicly accessible
- `private` - Private to the class
- `protected` - Protected access
- `final` - Cannot be subclassed
- `abstract` - Abstract class
- `interface` - Interface definition
- `static` - Static class (inner classes)

## Superclass Declaration

### Format
```smali
.super L<package>/<class-name>;
```

### Examples
```smali
.super Ljava/lang/Object;
.super Landroid/app/Activity;
.super Lcom/example/BaseClass;
```

## Field Declarations

### Format
```smali
.field <access-flags> <field-name>:<type>
```

### Examples
```smali
# Primitive types
.field public value:I                    # int
.field private name:Ljava/lang/String;  # String
.field protected count:J                 # long
.field public static final TAG:Ljava/lang/String; = "MyClass"

# Array types
.field private data:[I                   # int[]
.field public items:[Ljava/lang/String; # String[]
```

## Method Declarations

### Format
```smali
.method <access-flags> <method-name>(<parameter-types>)<return-type>
    .locals <number-of-local-variables>
    # Method body
.end method
```

### Examples

#### Constructor
```smali
.method public constructor <init>()V
    .locals 0
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V
    return-void
.end method
```

#### Simple Method
```smali
.method public getValue()I
    .locals 1
    iget v0, p0, Lcom/example/MyClass;->value:I
    return v0
.end method
```

#### Method with Parameters
```smali
.method public setValue(I)V
    .locals 0
    iput p1, p0, Lcom/example/MyClass;->value:I
    return-void
.end method
```

#### Static Method
```smali
.method public static add(II)I
    .locals 1
    add-int v0, p0, p1
    return v0
.end method
```

## Type Descriptors

Smali uses specific characters to represent types:

### Primitive Types

| Smali | Java Type | Description |
|-------|-----------|-------------|
| `V` | `void` | Void (no return value) |
| `Z` | `boolean` | Boolean |
| `B` | `byte` | Byte |
| `S` | `short` | Short integer |
| `C` | `char` | Character |
| `I` | `int` | Integer |
| `J` | `long` | Long integer (64-bit) |
| `F` | `float` | Floating point |
| `D` | `double` | Double precision float |

### Object Types

Format: `L<package>/<class>;`

Examples:
```smali
Ljava/lang/Object;          # java.lang.Object
Ljava/lang/String;          # java.lang.String
Landroid/content/Context;   # android.content.Context
Lcom/example/MyClass;       # com.example.MyClass
```

### Array Types

Format: `[<type>`

Examples:
```smali
[I                          # int[]
[Ljava/lang/String;        # String[]
[[I                         # int[][] (2D array)
[[[Ljava/lang/Object;      # Object[][][] (3D array)
```

## Method Signatures

### Format
```
(<parameter-types>)<return-type>
```

### Examples

```smali
()V                         # void method()
(I)V                        # void method(int)
(II)I                       # int method(int, int)
(Ljava/lang/String;)V      # void method(String)
(ILjava/lang/String;)Z     # boolean method(int, String)
([I)V                       # void method(int[])
()Ljava/lang/String;       # String method()
```

## Common Instructions

### Variable Operations
```smali
const v0, 0x7f040001        # Load constant
move v1, v0                 # Move register
move-result v2              # Move method result
```

### Field Operations
```smali
iget v0, p0, Lcom/example/MyClass;->value:I       # Get instance field
iput v1, p0, Lcom/example/MyClass;->value:I       # Put instance field
sget v0, Lcom/example/MyClass;->staticField:I     # Get static field
sput v1, Lcom/example/MyClass;->staticField:I     # Put static field
```

### Method Invocation
```smali
# Instance method
invoke-virtual {p0, v1}, Lcom/example/MyClass;->method(I)V

# Static method
invoke-static {v0, v1}, Lcom/example/MyClass;->staticMethod(II)I

# Constructor
invoke-direct {v0}, Ljava/lang/Object;-><init>()V

# Interface method
invoke-interface {p0, v1}, Lcom/example/MyInterface;->method(I)V
```

### Control Flow
```smali
if-eq v0, v1, :label         # If equal, goto label
if-ne v0, v1, :label         # If not equal, goto label
if-lt v0, v1, :label         # If less than, goto label
if-ge v0, v1, :label         # If greater or equal, goto label

goto :label                  # Unconditional jump

:label
    # Code here
```

### Return Statements
```smali
return-void                  # void return
return v0                    # return int/object
return-wide v0               # return long/double
```

## Annotations

```smali
.annotation system Ldalvik/annotation/Signature;
    value = {
        "Ljava/lang/Class<",
        "Ljava/lang/String;",
        ">;"
    }
.end annotation
```

## Common Patterns

### Singleton Pattern
```smali
.class public Lcom/example/Singleton;
.super Ljava/lang/Object;

.field private static instance:Lcom/example/Singleton;

.method private constructor <init>()V
    .locals 0
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V
    return-void
.end method

.method public static getInstance()Lcom/example/Singleton;
    .locals 1
    sget-object v0, Lcom/example/Singleton;->instance:Lcom/example/Singleton;
    if-nez v0, :cond_0
    new-instance v0, Lcom/example/Singleton;
    invoke-direct {v0}, Lcom/example/Singleton;-><init>()V
    sput-object v0, Lcom/example/Singleton;->instance:Lcom/example/Singleton;
    :cond_0
    return-object v0
.end method
```

### Getter/Setter Pattern
```smali
.class public Lcom/example/Person;
.super Ljava/lang/Object;

.field private name:Ljava/lang/String;

.method public getName()Ljava/lang/String;
    .locals 1
    iget-object v0, p0, Lcom/example/Person;->name:Ljava/lang/String;
    return-object v0
.end method

.method public setName(Ljava/lang/String;)V
    .locals 0
    iput-object p1, p0, Lcom/example/Person;->name:Ljava/lang/String;
    return-void
.end method
```

## Validation Requirements

The translator validates files for:

1. **Required Directives**
   - Must contain `.class` directive
   - Should contain `.super` directive

2. **Syntax Requirements**
   - Balanced `.method` and `.end method` pairs
   - Valid type descriptors
   - Proper formatting

3. **File Structure**
   - Non-empty content
   - Valid UTF-8 encoding
   - Within size limits

## Unsupported Features

The current translator has simplified handling for:
- Complex control flow (try-catch blocks)
- Detailed bytecode instruction translation
- Annotation details
- Inner class relationships
- Generic type information

These are recognized but translated in simplified form.

## Translation Notes

When translating Smali to Java, the translator:

1. **Extracts class information** from `.class` and `.super` directives
2. **Converts type descriptors** to Java types
3. **Identifies fields** from `.field` directives
4. **Identifies methods** from `.method` directives
5. **Preserves access modifiers** (public, private, protected, etc.)
6. **Simplifies method bodies** (full decompilation requires additional analysis)

The generated Java code provides a readable representation of the class structure but may not include complete method implementations.

## Best Practices

When working with Smali files:

1. **File Naming**: Use `.smali` extension
2. **Encoding**: Ensure UTF-8 encoding
3. **Size**: Keep files under 10 MB
4. **Structure**: Maintain proper directive order
5. **Formatting**: Use consistent indentation
6. **Comments**: Use `#` for comments

## Resources

For more information about Smali:
- [Smali Project on GitHub](https://github.com/JesusFreke/smali)
- [Android Dalvik Bytecode](https://source.android.com/devices/tech/dalvik/dalvik-bytecode)
- [Smali Syntax Guide](https://github.com/JesusFreke/smali/wiki)
