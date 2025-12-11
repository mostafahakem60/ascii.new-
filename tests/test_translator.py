import pathlib
import sys
import unittest

PROJECT_ROOT = pathlib.Path(__file__).resolve().parents[1]
sys.path.insert(0, str(PROJECT_ROOT / "smali-translator" / "src"))

from smali_translator import SmaliTranslator  # noqa: E402


class TestSmaliTranslator(unittest.TestCase):
    def test_translate_const_and_return(self) -> None:
        code = """
.method public foo()I
    .locals 1
    const/4 v0, 0x1
    return v0
.end method
"""
        res = SmaliTranslator().translate(code)
        self.assertEqual(res.errors, [])
        self.assertIsNotNone(res.pseudocode)
        self.assertIn("int v0 = 1;", res.pseudocode)
        self.assertIn("return v0;", res.pseudocode)

    def test_translate_invoke_with_move_result(self) -> None:
        code = """
.method public bar(Ljava/lang/String;)I
    .locals 2
    invoke-virtual {p1}, Ljava/lang/String;->length()I
    move-result v0
    return v0
.end method
"""
        res = SmaliTranslator().translate(code)
        self.assertEqual(res.errors, [])
        self.assertIsNotNone(res.pseudocode)
        self.assertIn("int v0 = p1.length();", res.pseudocode)

    def test_translate_control_flow(self) -> None:
        code = """
.method public cf(II)I
    .locals 2
    const/4 v0, 0x0
    :cond_0
    if-eq p1, p2, :done
    add-int/2addr v0, p1
    goto :cond_0
    :done
    return v0
.end method
"""
        res = SmaliTranslator().translate(code)
        self.assertEqual(res.errors, [])
        self.assertIsNotNone(res.pseudocode)
        self.assertIn("cond_0:", res.pseudocode)
        self.assertIn("if (p1 == p2) goto done;", res.pseudocode)
        self.assertIn("goto cond_0;", res.pseudocode)

    def test_malformed_smali_reports_errors(self) -> None:
        code = """
.method public bad()V
    .locals 0
    const/4 v0
    return-void v0
.end method
"""
        res = SmaliTranslator().translate(code)
        self.assertIsNone(res.pseudocode)
        self.assertGreaterEqual(len(res.errors), 1)
        for e in res.errors:
            self.assertIn(e.kind, {"lex", "parse", "emit"})
            # Error objects must be serializable for callers.
            self.assertIsInstance(e.to_dict(), dict)

    def test_translation_cache(self) -> None:
        code = """
.method public foo()I
    .locals 1
    const/4 v0, 0x2
    return v0
.end method
"""
        tr = SmaliTranslator()
        first = tr.translate(code)
        second = tr.translate(code)
        self.assertFalse(first.cached)
        self.assertTrue(second.cached)
        self.assertEqual(second.pseudocode, first.pseudocode)


if __name__ == "__main__":
    unittest.main()
