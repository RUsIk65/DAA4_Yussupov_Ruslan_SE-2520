"""Compile outside the timing scope; preserve raw benchmark output as Markdown."""
import pathlib
import subprocess
import sys

ROOT = pathlib.Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / 'tools'))
from run import build

build('Java')
subprocess.run(['javac', '--release', '21', '-encoding', 'UTF-8', '-cp', str(ROOT / 'Java/build'),
                '-d', str(ROOT / 'Java/build'), str(ROOT / 'tests/student/SearchBenchmark.java')],
               check=True, timeout=60)
result = subprocess.run(['java', '-XX:ActiveProcessorCount=1', '-Xmx384m', '-cp', str(ROOT / 'Java/build'),
                         'SearchBenchmark'], capture_output=True, text=True, encoding='utf-8', check=True, timeout=60)
(ROOT / 'tests/student/benchmark-results.md').write_text(result.stdout, encoding='utf-8')
print(result.stdout, end='')
