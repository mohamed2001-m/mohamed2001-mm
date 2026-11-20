#!/usr/bin/env python3
import json
from pathlib import Path

path = Path(__file__).resolve().parents[1] / "app/src/main/assets/lessons.json"
lessons = json.loads(path.read_text(encoding="utf-8"))
assert len(lessons) >= 6, "Expected at least 6 lessons"
for i, lesson in enumerate(lessons):
    for field in ("title", "topic", "grammar", "reading"):
        assert lesson.get(field), f"Lesson {i}: missing {field}"
    assert len(lesson["reading"]) >= 400, f"Lesson {i}: reading is short"
    assert len(lesson["words"]) >= 10, f"Lesson {i}: not enough vocabulary"
    for word in lesson["words"]:
        assert len(word) == 2 and all(word), f"Lesson {i}: invalid word"
    assert len(lesson["questions"]) == 5, f"Lesson {i}: expected 5 questions"
    for q in lesson["questions"]:
        assert len(q["options"]) == 3 and 0 <= q["answer"] < 3, f"Lesson {i}: invalid options"
print(f"Validated {len(lessons)} lessons, {sum(len(x['words']) for x in lessons)} words, {sum(len(x['questions']) for x in lessons)} questions.")
