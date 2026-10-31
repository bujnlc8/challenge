"""Run with: python3 -m unittest discover -s scripts -p 'test_*.py'."""
import tempfile
import unittest
from pathlib import Path

try:
    import restore_database as recovery
except ImportError:
    recovery = None


class RecoveryTests(unittest.TestCase):
    def setUp(self):
        self.assertIsNotNone(recovery, "The Python 3 database recovery tool is missing")

    def parse(self, text, name="question.txt"):
        return recovery.parse_text(text, name)

    def test_letter_answer_is_not_taken_from_question_text(self):
        q = self.parse('维生素A有什么作用？（B）\n错误\n正确', 'question-09224.txt')[0]
        self.assertEqual((q.content, q.answer, q.options), ('维生素A有什么作用？', 2, ('错误', '正确')))

    def test_exact_text_answer_does_not_use_substring_matching(self):
        q = self.parse('应当怎样处理？\n可以\n不可以\n答案为：不可以', 'question-xxqg.txt')[0]
        self.assertEqual(q.answer, 2)

    def test_middle_and_trailing_letter_answers(self):
        for text in ['哪一项？\nB\n一\n二', '哪一项？\n一\n二\n答案: B', '哪一项？\n一\n二\nB']:
            self.assertEqual(self.parse(text)[0].answer, 2)

    def test_missing_or_contradictory_answers_are_rejected(self):
        for text in ['题目\n一\n二', '题目\n一\n二\n答案C', '题目\n一\n二\n答案A.二']:
            with self.assertRaises(ValueError):
                self.parse(text)

    def test_boolean_explanation_is_preserved(self):
        q = self.parse('皇帝是曹丕吗（不是）司马炎', 'question-092207.txt')[0]
        self.assertEqual((q.content, q.options, q.answer, q.analysis), ('皇帝是曹丕吗', ('是', '不是'), 2, '司马炎'))

    def test_numbered_wrapped_questions(self):
        text = '1.\n山城是\n哪里？(B)\n北京\n重庆\n2.\n另一题(A)\n甲\n乙'
        qs = self.parse(text, 'question-092205.txt')
        self.assertEqual([(q.content, q.answer) for q in qs], [('山城是哪里？', 2), ('另一题', 1)])

    def test_inline_option_labels(self):
        q = self.parse('1、画法是？D\nA.甲 B.乙 C.丙 D.丁', 'question-092206.txt')[0]
        self.assertEqual((q.options, q.answer), (('甲', '乙', '丙', '丁'), 4))

    def test_numeric_options_are_not_question_boundaries(self):
        text = '1.\n多少人？(B)\n50-100\n100-120\n120-150\n150-200\n2.\n题目(A)\n甲\n乙'
        qs = self.parse(text, 'question-092205.txt')
        self.assertEqual(len(qs), 2)
        self.assertEqual(qs[0].options, ('50-100', '100-120', '120-150', '150-200'))

    def test_standalone_numbers_and_wrapped_bare_numbered_headers(self):
        text = '1.\n数字？(D)\n4\n7\n8\n13\n2第二题\n?(B)\n甲\n乙\n3\n第三题？(A)\n丙\n丁'
        qs = self.parse(text, 'question-092205.txt')
        self.assertEqual([q.options for q in qs], [('4', '7', '8', '13'), ('甲', '乙'), ('丙', '丁')])

    def test_inline_labels_do_not_consume_vitamin_letters(self):
        q = self.parse('1、维生素是？（A）\nA维生素A   B维生素B  C维生素C  D维生素D', 'question-092206.txt')[0]
        self.assertEqual(q.options, ('维生素A', '维生素B', '维生素C', '维生素D'))

    def test_bare_trailing_answer_in_question(self):
        q = self.parse('食物是哪一种C。\n苹果\n土豆\n花生\n猪肉', 'question-09224.txt')[0]
        self.assertEqual((q.content, q.answer), ('食物是哪一种。', 3))

    def test_letter_and_text_answer_without_punctuation(self):
        q = self.parse('哪个省\n湖南\n湖北\n答案A湖南')[0]
        self.assertEqual(q.answer, 1)

    def test_spaces_between_numeric_parts_are_significant(self):
        q = recovery.question('哪一天', ['1 19', '11 9', '11 1'], 2)
        self.assertEqual(q.answer, 2)

    def test_html_table_keeps_rows_and_columns(self):
        q = recovery.question('资料<table><tr><td>公司</td><td>资产</td></tr><tr><td>甲</td><td>50</td></tr></table>问题', ['甲', '乙'], 1)
        self.assertIn('公司；资产', q.content)
        self.assertIn('甲；50', q.content)

    def test_analysis_image_is_preserved_as_reference(self):
        q = recovery.question('问题', ['甲', '乙'], 1, analysis='说明<img src="https://example.com/formula.png">')
        self.assertIn('https://example.com/formula.png', q.analysis)
        self.assertEqual(q.status, 1)

    def test_poetry_uses_current_category(self):
        self.assertEqual(self.parse('诗人？(A)\n李白\n杜甫', 'shicidahui.txt')[0].category, 5)

    def test_conflicting_answers_are_quarantined(self):
        first = self.parse('问题\n甲\n乙\n答案A')[0]
        same = self.parse('问题\n乙\n甲\n答案B')[0]
        conflict = self.parse('问题\n甲\n乙\n答案B')[0]
        qs, duplicates, conflicts = recovery.deduplicate([first, same, conflict])
        self.assertEqual((len(qs), duplicates, len(conflicts)), (0, 1, 1))

    def test_sql_encodes_quotes_backslashes_and_unicode(self):
        self.assertEqual(recovery.sql_value("a'\\中\n"), "CONVERT(X'61275ce4b8ad0a' USING utf8mb4)")

    def test_real_corpus_is_valid_and_deterministic(self):
        qs, report = recovery.load_questions(Path(__file__).parent)
        again, _ = recovery.load_questions(Path(__file__).parent)
        self.assertGreater(len(qs), 10000)
        self.assertEqual(qs, again)
        self.assertFalse(report['unhandled_files'])
        self.assertFalse(report['unused_semantic_corrections'])
        self.assertGreaterEqual(len(report['semantic_corrections']), 40)
        self.assertTrue(set(range(1, 10)).issubset({q.category for q in qs}))
        for q in qs:
            self.assertTrue(1 <= q.answer <= len(q.options) <= 4)
            self.assertNotIn('|', ''.join(q.options))
            self.assertTrue(q.content and len(q.content) <= 1000)
            self.assertLessEqual(len('|'.join(q.options)), 2000)
            if '###' in q.content:
                self.assertEqual(q.status, 0)
        with tempfile.TemporaryDirectory() as tmp:
            path = Path(tmp) / 'questions.sql'
            recovery.export_sql(qs, path)
            self.assertIn('START TRANSACTION', path.read_text())


if __name__ == '__main__':
    unittest.main()
