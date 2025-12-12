# arabic-explainer

مكتبة Kotlin لتوليد شروحات عربية تقنية، خطوة بخطوة، لتعليميات Smali.

## الهدف

- استهلاك ناتج المترجم/الـ IR (Instruction/Method IR) أو نص Smali مباشر.
- إنتاج سرد عربي تفصيلي:
  - شرح لكل تعليمة (per-instruction).
  - شرح كامل لطريقة (full-method narrative) مع ترقيم خطوات.
- دعم تنسيق RTL ومزج رموز LTR مثل المسجلات (`v0`, `p0`) ومراجع الدوال.
- التعامل مع الحالات غير الصحيحة/غير المكتملة بإرجاع رسائل خطأ عربية قابلة للعرض.

## الاستخدام

```kotlin
import com.example.arabic_explainer.api.ArabicSmaliExplainer

val explainer = ArabicSmaliExplainer()

val instruction = explainer.explainInstructionText("const/4 v0, 0x1")

val method = explainer.explainMethodText(
    """
    .method public test()V
        const/4 v0, 0x1
        return-void
    .end method
    """.trimIndent()
)
```

## خيارات اللغة/التنسيق

- `wrapRtl`: يلف النص بعلامات Unicode RTL isolate لضمان عرض صحيح داخل واجهات مختلطة.
- `enableDiacritics`: يفعّل مصطلحات عربية مع التشكيل (مثل: `المُسَجِّل`).
- `numberSteps` و `useArabicIndicDigits`: للتحكم بترقيم الخطوات.

## التوسعة (Extensibility)

التغطية الحالية تشمل مجموعة أساسية من التعليمات (مثل: `const*`, `move*`, `add-int*`, `invoke-*`, `if-*`, `goto*`, `return*`).

لإضافة تغطية لتعليمات جديدة:

1. أنشئ صنفًا جديدًا يطبق `InstructionExplainer` داخل الحزمة:
   `com.example.arabic_explainer.templates`.
2. عرّف `supports(opcode: String)` و `explain(...)` باستخدام مصطلحات/مساعدات:
   - `ArabicTerms` للمصطلحات.
   - `ArabicFormat` و `Bidi` لضبط اتجاه LTR/RTL.
3. أضف الـ explainer إلى `InstructionExplainerRegistry.default()`.

> ملاحظة: يوجد `DefaultExplainer` لتقديم وصف عام للتعليمات غير المدعومة مؤقتًا.
