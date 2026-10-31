#!/usr/bin/env python3
"""Recover the database from repository sources without executing the legacy scripts."""
import argparse
import ast
from collections import Counter
from dataclasses import asdict, dataclass, replace
from datetime import datetime
import csv
import hashlib
import html
import io
import json
import os
from pathlib import Path
import random
import re
import shutil


ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / 'scripts'
ASSET_HOST = 'https://bucket-1256650966.cos.ap-beijing.myqcloud.com'
CATEGORIES = {1: '财经', 2: '百科', 3: '历史', 4: '地理', 5: '诗词',
              6: '驾考科一', 7: '驾考科三理论', 8: '交通规则', 9: '基金从业真题', 10: '世界杯'}
TEXT_FILES = {
    'question.txt': 2, 'question-0921.txt': 3, 'history.txt': 3,
    'question-0922.txt': 2, 'question-092201.txt': 2, 'question-092202.txt': 2,
    'question-092205.txt': 2, 'question-092206.txt': 2, 'question-092207.txt': 2,
    'question-09223.txt': 2, 'question-09224.txt': 2, 'question-09225.txt': 2,
    'question-xxqg.txt': 2, 'shicidahui.txt': 5, 'shicidahui1.txt': 5,
}
NUMBERED = {'question-092205.txt', 'question-092206.txt'}
MARKER = re.compile(r'[（(]\s*([A-D])\s*[)）]')


def clean(value):
    value = html.unescape(str(value or ''))
    def table_text(match):
        rows = []
        for row in re.findall(r'<tr\b[^>]*>(.*?)</tr\s*>', match[0], re.I | re.S):
            cells = re.findall(r'<t[dh]\b[^>]*>(.*?)</t[dh]\s*>', row, re.I | re.S)
            rows.append('；'.join(re.sub(r'\s+', ' ', clean(cell)) for cell in cells))
        return '\n' + '\n'.join(rows) + '\n'
    value = re.sub(r'<table\b.*?</table\s*>', table_text, value, flags=re.I | re.S)
    def image_reference(match):
        url = re.search(r'src\s*=\s*[\"\']([^\"\']+)', match[0], re.I)
        return '[原解析图片：' + url[1] + ']' if url else '[原解析图片缺失]'
    value = re.sub(r'<img\b[^>]*>', image_reference, value, flags=re.I)
    value = re.sub(r'<sup\b[^>]*>', '^', value, flags=re.I)
    value = re.sub(r'<br\s*/?\s*>|</p\s*>', '\n', value, flags=re.I)
    value = re.sub(r'<[^>]+>', '', value)
    return '\n'.join(re.sub(r'[^\S\n]+', ' ', line).strip()
                     for line in value.splitlines()).strip()


def normalized(value):
    return re.sub(r'\s+', ' ', value).strip()


@dataclass(frozen=True)
class Question:
    content: str
    options: tuple
    answer: int
    category: int = 2
    level: int = 1
    analysis: str = ''
    status: int = 1
    source: str = ''

    @property
    def key(self):
        return normalized(self.content), tuple(sorted(normalized(x) for x in self.options))

    @property
    def correct(self):
        return normalized(self.options[self.answer - 1])


def question(content, options, answer, category=2, level=1, analysis='', source='', status=1):
    if re.search(r'<img\b|<math\b', str(content) + ''.join(options), re.I):
        raise ValueError('Question or option requires an external image or formula')
    content, options, analysis = clean(content), tuple(clean(x) for x in options), clean(analysis)
    answer = int(answer)
    if not content or len(content) > 1000:
        raise ValueError('Missing question or content longer than 1000 characters')
    if not 2 <= len(options) <= 4 or any(not x or '|' in x for x in options):
        raise ValueError('Expected 2-4 nonempty options without pipe separators')
    if len(set(normalized(x) for x in options)) != len(options):
        raise ValueError('Duplicate options')
    if not 1 <= answer <= len(options) or len('|'.join(options)) > 2000:
        raise ValueError('Answer outside options or options longer than 2000 characters')
    # Current labels come from CommonController, not obsolete comments in question.sql.
    if category == 2:
        if '世界杯' in content:
            category = 10
        elif re.search(r'机动车|交通信号|交通警察|驾驶证|道路交通安全|交通事故|行车道', content):
            category = 8
        elif re.search(r'基金|股票|证券|债券|期货|外汇|税收|纳税|增值税|货币政策|金融|商业银行|财政政策', content):
            category = 1
    if '###' in content:
        status = 0  # Preserve the record; old remote images are not assumed available.
    return Question(content, options, answer, category, level, analysis, status, source)


def blocks(text, name):
    if name == 'question-092207.txt':
        return [line.strip() for line in text.splitlines() if line.strip()]
    if name in NUMBERED:
        # These two files have numbered questions, not ~ delimiters.
        result, current = [], []
        lines = [line for line in text.splitlines() if line.strip()]
        for i, line in enumerate(lines):
            marker = re.match(r'^\s*(\d{1,3})([.．、]?)(.*)$', line)
            following = lines[i + 1:i + 3]
            wrapped = (following and not re.match(r'^\s*\d', following[0]) and
                       (MARKER.search(following[0]) or
                        (len(following) > 1 and re.fullmatch(r'\s*[?:：？]*\s*[（(]\s*[A-D]\s*[)）][。.]?\s*', following[1]))))
            boundary = marker and (
                MARKER.search(marker[3]) or
                (marker[2] and not re.match(r'\s*\d', marker[3])) or
                wrapped or
                (name == 'question-092206.txt' and len(marker[3].strip()) > 5))
            if boundary:
                if any(x.strip() for x in current):
                    result.append('\n'.join(current).strip())
                current = [marker[3]]
            else:
                current.append(line)
        if any(x.strip() for x in current):
            result.append('\n'.join(current).strip())
        return result
    return [b.strip() for b in re.split(r'~{3,}', text) if b.strip()]


def parse_block(block, name):
    category = TEXT_FILES.get(name, 2)
    if name == 'question-092207.txt':
        m = re.search(r'[（(](不是|不能|不会|不可以|不对|不好|没有|是|能|会|可以|对|好|有)[)）]', block)
        if not m:
            raise ValueError('No explicit boolean answer')
        actual = m[1]
        positive = {'没有': '有', '不是': '是', '不能': '能', '不会': '会',
                    '不可以': '可以', '不对': '对', '不好': '好'}.get(actual, actual)
        negative = '没有' if positive == '有' else '不' + positive
        return question(block[:m.start()], [positive, negative], 1 if actual == positive else 2,
                        category, analysis=block[m.end():], source=name)
    lines = [clean(x) for x in block.splitlines() if clean(x)]
    content, options, answer = '', [], None
    if name == 'question-092206.txt':
        lines = [line.translate(str.maketrans('ＡＢＣＤ', 'ABCD')) for line in lines]
        match = MARKER.search(lines[0])
        if not match:
            match = re.search(r'([A-D])\s*$', lines[0])
        if not match:
            raise ValueError('No inline answer letter')
        answer = ord(match[1]) - 64
        content = lines[0][:match.start()] + lines[0][match.end():]
        raw = '\n'.join(lines[1:])
        markers = list(re.finditer(r'(?<!\S)([A-D])(?:\s*[.．、,，]\s*|\s*）\s*|\s*)', raw))
        if [m[1] for m in markers] != list('ABCD')[:len(markers)]:
            raise ValueError('Ambiguous inline option labels')
        options = [raw[m.end():markers[i + 1].start() if i + 1 < len(markers) else len(raw)].strip()
                   for i, m in enumerate(markers)]
    elif name in NUMBERED:
        joined = '\n'.join(lines)
        match = MARKER.search(joined)
        if not match:
            raise ValueError('No answer marker in numbered question')
        content = ''.join(joined[:match.start()].splitlines())
        answer = ord(match[1]) - 64
        options = [re.sub(r'^[A-D][.．、]\s*', '', x.strip())
                   for x in joined[match.end():].splitlines() if x.strip()]
    elif name == 'question-09225.txt':
        content, actual = lines[0].rsplit('|', 1)
        options = lines[1:]
        matches = [i + 1 for i, x in enumerate(options) if normalized(x) == normalized(actual)]
        if len(matches) != 1:
            raise ValueError('Text answer not found exactly once')
        answer = matches[0]
    else:
        content = lines[0]
        options = lines[1:]
        match = MARKER.search(content)
        if not match and name == 'question-09224.txt':
            match = re.search(r'([A-D])(?=[。．.]?$)', content)
        if match:
            answer = ord(match[1]) - 64
            content = content[:match.start()] + content[match.end():]
        elif options and re.fullmatch(r'[A-D]', options[0]):
            answer = ord(options.pop(0)) - 64
        else:
            if not options:
                raise ValueError('Missing options and answer')
            tail = options.pop()
            letter = re.fullmatch(r'(?:参考)?答案\s*[:：]?\s*([A-D])(?:[.．、]?\s*(.*))?', tail)
            if not letter:
                letter = re.fullmatch(r'([A-D])', tail)
            if letter:
                answer = ord(letter[1]) - 64
                if letter.lastindex == 2 and letter[2] and (answer > len(options) or normalized(clean(letter[2])) != normalized(clean(options[answer - 1]))):
                    raise ValueError('Answer letter contradicts answer text')
            else:
                actual = re.sub(r'^(?:参考)?答案(?:为)?\s*[:：]?\s*', '', tail)
                if actual == tail:
                    raise ValueError('Missing explicit answer')
                matches = [i + 1 for i, x in enumerate(options) if normalized(clean(x)) == normalized(clean(actual))]
                if len(matches) != 1:
                    raise ValueError('Text answer not found exactly once')
                answer = matches[0]
    return question(content, options, answer, category, source=name)


def parse_text(text, name):
    return [parse_block(block, name) for block in blocks(text, name)]


def deduplicate(questions):
    unique, bad, duplicates, conflicts = {}, set(), 0, []
    for q in questions:
        if q.key in bad:
            continue
        previous = unique.get(q.key)
        if previous:
            if previous.correct != q.correct:
                conflicts.append({'content': q.content, 'sources': [previous.source, q.source],
                                  'answers': [previous.correct, q.correct]})
                bad.add(q.key)
                del unique[q.key]
            else:
                duplicates += 1
                # Keep a more specific category/analysis when the same record appears twice.
                unique[q.key] = replace(previous, analysis=previous.analysis or q.analysis,
                                        category=q.category if previous.category == 2 else previous.category)
        else:
            unique[q.key] = q
    return list(unique.values()), duplicates, conflicts


def literal(path, variable):
    """Extract old Python data constants using AST; do not execute/import old scripts."""
    for node in ast.parse(path.read_text(encoding='utf-8')).body:
        if isinstance(node, ast.Assign) and any(isinstance(t, ast.Name) and t.id == variable for t in node.targets):
            return ast.literal_eval(node.value)
    raise ValueError('Missing literal: ' + variable)


def source_digest(raw):
    return hashlib.sha256(json.dumps(raw, ensure_ascii=False, sort_keys=True).encode('utf-8')).hexdigest()


def load_questions(source=SOURCE, image_base_url=None, enable_remote_images=False):
    questions, rejected, counts, handled = [], [], Counter(), set()
    corrections_path = source / 'recovered_questions.json'
    corrections = json.loads(corrections_path.read_text(encoding='utf-8')) if corrections_path.exists() else {'questions': [], 'countries': []}
    overrides = {(row['source'], row['sha256']): row for row in corrections['questions']}
    applied = []

    def add(name, ref, raw, builder):
        counts[name] += 1
        try:
            override = overrides.get((name, source_digest(raw)))
            if override:
                fields = {k: override[k] for k in ('content', 'options', 'answer', 'category', 'analysis') if k in override}
                q = question(**fields)
                applied.append({'source': name, 'record': ref, 'note': override['note']})
            else:
                q = builder()
            questions.append(replace(q, source=f'{name}:{ref}'))
        except (ValueError, TypeError, KeyError, IndexError) as exc:
            rejected.append({'source': name, 'record': ref, 'reason': str(exc), 'raw': raw})

    for name in TEXT_FILES:
        path = source / name
        handled.add(name)
        for i, block in enumerate(blocks(path.read_text(encoding='utf-8-sig'), name), 1):
            add(name, i, block, lambda block=block, name=name: parse_block(block, name))
    name = 'EarthWiki_PureTextQuiz.txt'
    handled.add(name)
    for i, row in enumerate(csv.reader(io.StringIO((source / name).read_text(encoding='utf-8-sig'))), 1):
        def earth(row=row):
            if len(row) != 10:
                raise ValueError('Expected 10 CSV columns')
            content, a, b, c, d, answer, difficulty, category, analysis, _ = row
            opts = [a, b, c, d]
            index = ord(answer.strip().lower()) - ord('a')
            if not 0 <= index < 4 or not opts[index].strip():
                raise ValueError('Invalid CSV answer')
            remapped = sum(bool(x.strip()) for x in opts[:index + 1])
            return question(content, [x for x in opts if x.strip()], remapped,
                            {'历史': 3, '地理': 4}.get(category, 2),
                            1 if int(difficulty) <= 2 else 2 if int(difficulty) <= 4 else 3, analysis)
        add(name, i, row, earth)
    for path in sorted(source.glob('*.json')):
        name = path.name
        data = json.loads(path.read_text(encoding='utf-8-sig'))
        if name != 'jiakao.json' and not name.endswith('_真题_题库.json'):
            continue
        handled.add(name)
        if name == 'jiakao.json':
            for i, row in enumerate(data['data'], 1):
                def driving(row=row):
                    content = row['question']
                    if row['imageurl']:
                        url = row['imageurl']
                        if url.startswith('/v2/'):
                            url = ASSET_HOST + '/jiakao/images/' + url[4:]
                        if not url.startswith(('https://', 'http://')):
                            raise ValueError('Unknown driving image URL format')
                        content += '###' + url
                    opts = [row[f'an{n}'] for n in range(1, 5)]
                    answer = int(row['answertrue'])
                    if not 1 <= answer <= 4 or not opts[answer - 1]:
                        raise ValueError('Invalid driving answer (possibly multiple choice)')
                    if int(row['stype']) not in (1, 4):
                        raise ValueError('Unknown driving category')
                    return question(content, [x for x in opts if x], sum(bool(x) for x in opts[:answer]),
                                    6 if row['stype'] == 1 else 7, analysis=row.get('explain', ''))
                add(name, i, row, driving)
        else:
            for ref, row in data.items():
                def fund(row=row):
                    if row['examType'] != 1 or not re.fullmatch('[A-D]', row['answer']):
                        raise ValueError('Only single-answer questions are supported')
                    return question(row['content'], row['optionList'], ord(row['answer']) - 64,
                                    9, analysis=row.get('analysis', ''))
                add(name, ref, row, fund)
    # Recreate the original capital/flag/map generators deterministically from their constants.
    rng = random.Random(20221031)
    capitals = literal(source / 'capital.py', 'CAPITALS')
    for i, (country, city) in enumerate(capitals, 1):
        for inverse in (False, True):
            correct = country if inverse else city
            pool = sorted({row[0 if inverse else 1] for row in capitals} - {correct})
            opts = [correct] + rng.sample(pool, 3)
            rng.shuffle(opts)
            content = f'下列哪个国家的首都是{city}？' if inverse else f'{country} 的首都是下列的那个城市？'
            add('capital.py', f'{i}:{int(inverse)}', [country, city],
                lambda content=content, opts=opts, correct=correct: question(content, opts, opts.index(correct) + 1, 4, 2))
    countries = literal(source / 'insert_question.py', 'COUNTRY_CODE')
    additions = {row[0]: tuple(row) for row in corrections['countries']}
    countries = [additions.pop(row[0], row) for row in countries] + list(additions.values())
    for directory, label, field in [('flag', '国旗', 0), ('maps', '地图', 2)]:
        lookup = {row[field]: row for row in countries}
        known = [(p, lookup[p.stem]) for p in sorted((source / directory).glob('*.png')) if p.stem in lookup]
        for path, row in known:
            opts = [row[1]] + rng.sample(sorted({r[1] for _, r in known} - {row[1]}), 3)
            rng.shuffle(opts)
            url_path = ('map' if directory == 'maps' else 'flag') + '/' + path.name
            host = (image_base_url or ASSET_HOST).rstrip('/')
            q = question(f'下图是哪个国家的{label}？###{host}/{url_path}', opts, opts.index(row[1]) + 1, 4,
                         source=str(path.relative_to(source)))
            questions.append(replace(q, status=1 if image_base_url else 0))
            counts[directory] += 1
        for p in sorted((source / directory).glob('*.png')):
            if p.stem not in lookup:
                rejected.append({'source': directory, 'record': p.name,
                                 'reason': 'No exact country mapping in original constants', 'raw': p.name})
    if enable_remote_images:
        questions = [replace(q, status=1) if '###' in q.content else q for q in questions]
    unique, duplicates, conflicts = deduplicate(questions)
    unhandled = sorted(p.name for p in source.iterdir() if p.suffix in ('.txt', '.json')
                       and p.name not in handled | {'country_code.txt', 'requirements.txt', 'recovered_questions.json'})
    report = {'source_records': dict(counts), 'parsed': len(questions), 'duplicates': duplicates,
              'conflicts': conflicts, 'rejected': rejected, 'unhandled_files': unhandled,
              'semantic_corrections': applied,
              'unused_semantic_corrections': sorted(set(row['source'] + ':' + str(row['record']) for row in corrections['questions']) -
                                                    set(row['source'] + ':' + str(row['record']) for row in applied)),
              'questions': len(unique), 'active': sum(q.status == 1 for q in unique),
              'inactive_images': sum(q.status == 0 for q in unique),
              'categories': {str(i): {'name': label, 'total': sum(q.category == i for q in unique),
                                    'active': sum(q.category == i and q.status == 1 for q in unique)}
                             for i, label in CATEGORIES.items()}}
    return unique, report


COLUMNS = ('content', 'options', 'answer', 'level', 'category', 'status', 'analysis', 'create_time', 'update_time')


def values(q, now):
    return q.content, '|'.join(q.options), q.answer, q.level, q.category, q.status, q.analysis, now, now


def sql_value(value):
    # Hex literals work with either NO_BACKSLASH_ESCAPES setting and preserve all Unicode.
    if isinstance(value, int):
        return str(value)
    return "CONVERT(X'" + str(value).encode('utf-8').hex() + "' USING utf8mb4)"


def export_sql(questions, path):
    now = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
    columns = ','.join('`' + c + '`' for c in COLUMNS)
    with path.open('w', encoding='utf-8') as f:
        f.write('-- Select the destination database first. Apply sql/schema.sql before this file.\n')
        f.write('SET NAMES utf8mb4;\n')
        f.write('CREATE TEMPORARY TABLE `_restore_question` LIKE `question`;\n')
        f.write('ALTER TABLE `_restore_question` ADD COLUMN `recovery_key` binary(32), ADD KEY (`recovery_key`);\n')
        # Build a small indexed snapshot once; repeat imports do not scan the whole question table per row.
        expr = "UNHEX(SHA2(CONCAT(`content`,CHAR(0),`options`,CHAR(0),CAST(`category` AS CHAR)),256))"
        f.write(f'CREATE TEMPORARY TABLE `_restore_existing` AS SELECT DISTINCT {expr} AS recovery_key FROM `question`;\n')
        f.write('ALTER TABLE `_restore_existing` ADD PRIMARY KEY (`recovery_key`);\nSTART TRANSACTION;\n')
        for offset in range(0, len(questions), 100):
            batch = questions[offset:offset + 100]
            f.write(f'INSERT INTO `_restore_question` ({columns},`recovery_key`) VALUES\n')
            rows = []
            for q in batch:
                key = hashlib.sha256((q.content + '\0' + '|'.join(q.options) + '\0' + str(q.category)).encode('utf-8')).hexdigest()
                rows.append('(' + ','.join(sql_value(v) for v in values(q, now)) + ",X'" + key + "')")
            f.write(',\n'.join(rows) + ';\n')
        selected = ','.join('s.`' + c + '`' for c in COLUMNS)
        f.write(f'INSERT INTO `question` ({columns}) SELECT {selected} FROM `_restore_question` s '
                'LEFT JOIN `_restore_existing` e ON e.recovery_key=s.recovery_key '
                'WHERE e.recovery_key IS NULL ORDER BY s.id;\n')
        f.write("SELECT ROW_COUNT() AS inserted_questions;\nCOMMIT;\n")
        f.write('DROP TEMPORARY TABLE `_restore_question`;\nDROP TEMPORARY TABLE `_restore_existing`;\n')


def apply_database(questions, args):
    import pymysql  # Offline parsing/export uses only the Python standard library.
    if not re.fullmatch(r'[A-Za-z][A-Za-z0-9_]*', args.database):
        raise ValueError('Invalid database name')
    connection = pymysql.connect(host=args.host, port=args.port, user=args.user,
                                 password=os.environ.get('MYSQL_PASSWORD', ''), charset='utf8mb4',
                                 connect_timeout=10, autocommit=False)
    try:
        with connection.cursor() as cursor:
            cursor.execute(f'CREATE DATABASE IF NOT EXISTS `{args.database}` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci')
            cursor.execute(f'USE `{args.database}`')
            schema = '\n'.join(line for line in (ROOT / 'sql/schema.sql').read_text().splitlines()
                               if not line.lstrip().startswith('--'))
            for statement in schema.split(';'):
                if statement.strip():
                    cursor.execute(statement)
            cursor.execute('SELECT content,options,answer FROM question')
            existing = {}
            for content, options, answer in cursor.fetchall():
                opts = tuple(options.split('|'))
                key = (normalized(content), tuple(sorted(normalized(x) for x in opts)))
                existing.setdefault(key, set()).add(normalized(opts[answer - 1]) if 1 <= answer <= len(opts) else '<invalid>')
            pending = [q for q in questions if q.key not in existing]
            conflicts = [q.source for q in questions if q.key in existing and existing[q.key] != {q.correct}]
            if conflicts:
                raise ValueError(f'{len(conflicts)} existing answers conflict; no questions inserted. Examples: {conflicts[:5]}')
            now = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
            columns = ','.join('`' + c + '`' for c in COLUMNS)
            for offset in range(0, len(pending), 500):
                cursor.executemany(f'INSERT INTO question ({columns}) VALUES ({",".join(["%s"] * len(COLUMNS))})',
                                   [values(q, now) for q in pending[offset:offset + 500]])
            connection.commit()
            return {'database': args.database, 'inserted': len(pending), 'already_present': len(questions) - len(pending)}
    except Exception:
        connection.rollback()
        raise
    finally:
        connection.close()


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--output-dir', type=Path, default=ROOT / 'target/database-recovery')
    parser.add_argument('--image-base-url', help='Public base URL for local flag/ and map/ assets; enables these questions')
    parser.add_argument('--enable-remote-images', action='store_true', help='Enable image questions after restoring their remote assets')
    parser.add_argument('--apply', action='store_true', help='Create missing tables and insert questions into MySQL')
    parser.add_argument('--host', default=os.environ.get('MYSQL_HOST', '127.0.0.1'))
    parser.add_argument('--port', type=int, default=int(os.environ.get('MYSQL_PORT', '33060')))
    parser.add_argument('--user', default=os.environ.get('MYSQL_USER', 'root'))
    parser.add_argument('--database', default=os.environ.get('MYSQL_DATABASE', 'challenge'))
    args = parser.parse_args()
    if args.image_base_url and not re.fullmatch(r'https?://[^\s#]+', args.image_base_url):
        parser.error('--image-base-url must be an HTTP(S) URL without a fragment')
    questions, report = load_questions(image_base_url=args.image_base_url,
                                       enable_remote_images=args.enable_remote_images)
    if report['unhandled_files']:
        parser.error('Unrecognized source files: ' + ', '.join(report['unhandled_files']))
    args.output_dir.mkdir(parents=True, exist_ok=True)
    shutil.copyfile(ROOT / 'sql/schema.sql', args.output_dir / 'schema.sql')
    export_sql(questions, args.output_dir / 'questions.sql')
    with (args.output_dir / 'questions.jsonl').open('w', encoding='utf-8') as output:
        for q in questions:
            output.write(json.dumps(asdict(q), ensure_ascii=False) + '\n')
    for directory, destination in [('flag', 'flag'), ('maps', 'map')]:
        shutil.copytree(SOURCE / directory, args.output_dir / 'assets' / destination, dirs_exist_ok=True)
    if args.apply:
        report['database_import'] = apply_database(questions, args)
    (args.output_dir / 'report.json').write_text(json.dumps(report, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    print(json.dumps({k: v for k, v in report.items() if k not in ('rejected', 'conflicts', 'semantic_corrections')}, ensure_ascii=False, indent=2))
    print(f'Model semantic corrections applied: {len(report["semantic_corrections"])}')
    print(f'Rejected: {len(report["rejected"])}; conflicting groups: {len(report["conflicts"])}')
    print(f'Files: {args.output_dir}')


if __name__ == '__main__':
    main()
