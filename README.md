# Assignment 4 — Java 21 solution

Implementation of all eight required commands: `lcs`, `merge`, `tree`, `naive`, `kmp`, `rk`, `suffix`, `aho`.

- [Source](Java/source/Algorithms.java)
- [English analysis, correctness arguments, complexity and measurements](ANALYSIS.md)
- [Student tests and evidence](tests/student/README.md)
- [Full task](docs/Assignment_4_EN.md) / [Russian task](docs/Assignment_4_RU.md)
- [Contracts](docs/guide/CONTRACTS.md) / [Rubric](docs/guide/RUBRIC.md)

## Requirements and launch

Install JDK **21** and Python **3.12+**. Python runs the course launcher and tests; the solution itself is Java. `submission.json` selects `Java`. Run from the repository root:

```sh
python tools/run.py --setup
python tools/run.py --check
python tools/run.py --input examples/lcs.jsonl
python tests/student/test_solution.py
python tests/student/benchmark.py
```

The program accepts UTF-8 JSON Lines, with one answer per request. Example:

```json
{"task":"lcs","a":"tea_time","b":"team"}
```

```json
{"length":4,"sequence":"team"}
```

For this machine, the working PowerShell setup is:

```powershell
$env:Path = 'C:/Program Files/Java/jdk-21.0.11/bin;' + $env:Path
$a4Python = 'C:/Users/rusla/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/python.exe'
& $a4Python tools/run.py --setup
& $a4Python tools/run.py --check
& $a4Python tests/student/test_solution.py
& $a4Python tests/student/benchmark.py
```

These machine-specific paths are optional; normal `python`, `java` and `javac` on PATH work elsewhere. No third-party Python or Java packages are needed. For an IDE, select JDK 21, mark `Java/source` as the source directory, and run `Main`.

The launcher compiles with `--release 21` into ignored `Java/build`. GitHub Actions uses Java 21 and runs the public checks and student tests. Benchmarks are a separate reproducible local command, not a grading-time test.

## Results and submission

Local checks passed: 44 public cases, the repeated 88-request public batch, and 8,160 student-test responses. Actual measurements and test logs are included under `tests/student`. These results do not assign an official grade.

Create your own GitHub repository using the course template, transfer/commit this solution there, and push. Confirm **Public checks** for that exact commit. In Moodle submit its URL containing the full 40-character SHA and language **Java 21**; grant the teacher access if the repository is private. A ZIP is not the required Moodle submission. The deadline stated by the assignment is 8 November 2026, 23:59, subject to any approved Moodle extension.

The clone's current `origin` still refers to the instructor's template: use your own repository as the publication destination. No GitHub or Moodle submission has been made by this local preparation. AI assistance and verification are disclosed in `ANALYSIS.md`. Review the code and report before the independent defence.
