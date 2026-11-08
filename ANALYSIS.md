# Assignment 4 — algorithm analysis

## Environment and commands

The selected language is **Java 21**, recorded as `Java` in `submission.json`. The launcher compiles with `javac --release 21`. GitHub Actions also selects JDK 21. No algorithm library or built-in substring search is used in the submitted algorithms. The provided JSON parser and JSON Lines entry point are retained.

Local measurements were taken on 7 November 2026:

- Oracle JDK 21.0.11+9-LTS-211, 64-bit HotSpot; Python 3.12.14 for the supplied launcher and student test driver.
- Windows 11, build 26200, AMD64; AMD Ryzen 7 5800H with Radeon Graphics.
- Physical memory: 16,473,034,752 bytes (15.34 GiB available as installed physical RAM reported by Windows).
- Student test and benchmark JVMs use `-XX:ActiveProcessorCount=1 -Xmx384m`. This sets the JVM's processor count; it does not reproduce the official container's CPU scheduling or total memory limit.
- Hardware/runtime evidence: [environment.txt](tests/student/environment.txt).

Run these commands from the repository root with JDK 21 and Python 3.12+ on PATH:

```sh
python tools/run.py --setup
python tools/run.py --check
python tools/run.py --input examples/lcs.jsonl
python tests/student/test_solution.py
python tests/student/benchmark.py
python tests/student/environment.py
```

On this Windows machine, the system Python shortcut is not usable. The actual executable used was `C:/Users/rusla/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/python.exe`; JDK binaries were selected from `C:/Program Files/Java/jdk-21.0.11/bin`. See [README.md](README.md) for the exact PowerShell setup. Python is a test/launch tool, not the solution language.

An alternative Java-only build and execution is:

```sh
javac --release 21 -encoding UTF-8 -d Java/build Java/source/Algorithms.java Java/source/Json.java Java/source/Main.java
java -Xmx384m -cp Java/build Main
```

The running program reads one UTF-8 JSON request per line and emits one JSON object per line. Diagnostics do not go to standard output. All algorithm arrays and collections are request-local. Character comparisons use ASCII code order, and positions are zero-based. Costs, tree sums and dictionary counts use `long`.

## Algorithm explanations

### 1. LCS: prefix dynamic programming

Let `dp[i][j]` be the maximum length of a common subsequence of `a[0:i]` and `b[0:j]`. An empty prefix has value zero. If the last characters agree, `dp[i][j] = dp[i-1][j-1] + 1`; otherwise it is `max(dp[i-1][j], dp[i][j-1])`. Rows and columns are processed in increasing order, so all referenced prefixes are ready.

For matching last characters, an optimum can use the common last character after an optimum of the shorter prefixes. For different last characters, a common subsequence cannot use both final positions, so it must omit at least one of them. These alternatives prove the recurrence by induction on the prefix lengths.

Reconstruction starts at `(n,m)`. Equal characters are appended while moving diagonally; otherwise a predecessor with the same optimum is chosen. An upper cell wins ties. Reversing the collected characters produces a subsequence of both strings of length `dp[n][m]`. The optimum need not be unique.

Time and auxiliary memory are `O((n+1)(m+1))`; reconstruction adds `O(n+m)` time. Input storage is `O(n+m)`, output storage `O(length+1)`. The empty-side case still allocates a zero-filled table, within the bound. `tea_time` / `team` tests subsequence rather than substring semantics; `ab` / `ba` tests an optimal tie.

### 2. Ordered merging: interval dynamic programming

`dp[l][r]` is the cheapest way to merge the closed original interval `[l,r]` into one block. Singletons cost zero. Prefix sums give `sum(l,r)` in constant time. Every last merge separates the interval at some `k`:

`dp[l][r] = min over l <= k < r of dp[l][k] + dp[k+1][r] + sum(l,r)`.

Intervals are processed by increasing length. Any legal last merge has two adjacent interval children, and replacing either child's plan by a cheaper one improves the total. Conversely, any two optimal child plans followed by their merge are legal. Thus considering every last split finds an optimum. The first minimum split is retained, so equal costs choose the smallest `k`.

An explicit stack traverses saved splits in postorder. A merge `[l,k,r]` is output only after both child plans, so the chronological output always refers to existing adjacent blocks. There are exactly `n-1` operations for `n>1`; zero or one block returns an empty plan and cost zero. All cost arithmetic is `long`, including prefix sums.

Time is `O(n^3)` and auxiliary memory `O(n^2)` for nonempty input; empty input costs `O(1)`. Input and output each require `O(n+1)` space. `[1,100,1]` has optimum 203: combining the two nonadjacent small blocks would illegally obtain 104. Three blocks of size one billion give 5,000,000,000, detecting 32-bit overflow.

### 3. Tree maximum-weight independent set

The tree is rooted at vertex 0 with an iterative parent-before-child order. Primitive adjacency arrays avoid boxed edge storage inside the algorithm. Reversing this order processes descendants before parents without recursive calls, even on a chain of 100,000 vertices.

`take[u]` is the optimum in the subtree when `u` is selected; `skip[u]` is the optimum when it is omitted:

- `take[u] = weight[u] + sum(skip[v])` over children `v`.
- `skip[u] = sum(max(take[v], skip[v]))` over children `v`.

Subtrees have no cross edges. Selecting a parent excludes every child, while omitting the parent allows each child to choose its better state independently. Induction from leaves proves both formulas. The global optimum is `max(take[0],skip[0])`; skip states permit the empty set and therefore handle all-negative weights.

Reconstruction follows parent-before-child order. A vertex is selected only if its parent was omitted and its take value strictly exceeds its skip value. Ties omit the vertex. Every vertex is visited once, no adjacent vertices are selected, and the choices attain the stored value. An empty tree returns zero and an empty list. Root choice affects a possible witness but not the optimum.

Time and auxiliary space are `O(n+1)`. Input adjacency/weights and output occupy `O(n+1)` as well. The tests enumerate independent subsets for small randomly relabelled trees and validate the returned witness. Large chain/star totals exceed 32-bit range; negative, zero and singleton cases check empty-set and tie behavior.

### 4a. Naive exact search

For each start from `0` through `n-m`, compare pattern characters left to right until a mismatch or a full match. The loop invariant is that the first `j` characters of this window equal the first `j` pattern characters. A start is output exactly when `j=m`. Incrementing the start by one preserves overlaps and produces increasing positions.

When `m=0`, every start from 0 through `n` succeeds; when `m>n`, no start is tested. `a b` with an empty pattern returns `[0,1,2,3]`, including the final boundary.

Time is `O(n*m+n+1)` including output, auxiliary space is `O(1)`, and the `ArrayList<Integer>` output uses `O(z+1)` memory for `z` starts. Input strings occupy `O(n+m)`.

### 4b. KMP

`pi[i]` is the length of the longest proper prefix of `pattern[0:i+1]` that is also its suffix. During prefix construction, `j` is a candidate border of the preceding prefix. A mismatch replaces `j` by `pi[j-1]`; a match extends it. The failure chain lists all possible shorter borders, so no valid border is skipped.

During the text scan, `j` is the length of the currently matched pattern prefix ending at the previous text position. The same fallback rule preserves exactly the viable suffix-prefix matches. Reaching `m` reports `i-m+1`, then falls back to `pi[m-1]` instead of zero, retaining overlaps. The empty pattern is handled before indexing it. `abababa` / `aba` must return `[0,2,4]`.

Each forward step increases `j` by at most one; every fallback strictly decreases it. Hence total fallback work is linear, giving `O(n+m+z+1)` time, `O(m+1)` auxiliary memory and `O(z+1)` output memory. Input storage is `O(n+m)`.

### 4c. Exact Rabin–Karp

The polynomial hash uses ASCII codes, base 31 and modulus 101. The leftmost character has power `31^(m-1)`. Both the target and the first window are built from left to right. If `P = 31^(m-1) mod 101`, the next window is

`floorMod((window - outgoingCode * P) * 31 + incomingCode, 101)`.

Removing the outgoing highest-power term, shifting the remaining powers and adding the incoming character preserves the window hash. Java's `Math.floorMod` normalizes negative intermediate values. Hash inequality proves a mismatch; equality is only a candidate, which is verified character by character before reporting. Thus collisions never create false positives. `ab` and `dj` both hash to 75, but `abdj` / `ab` returns only `[0]`. In `zab` / `ab`, the first rolling intermediate is negative; the true match at 1 checks normalization.

Empty patterns return all boundaries; patterns longer than the text return no starts before any initial-window access. For `1<=m<=n`, time is `O(n+m+c*m)` for `c` equal-hash windows; worst-case time can be quadratic. A bound covering every input is `O(n*m+n+m+1)`. Only scalar rolling state is kept, so auxiliary space is `O(1)` and output space `O(z+1)`, besides the `O(n+m)` input. The intentionally small modulus increases accidental candidates; it is not a model for every production hash.

### 5. Suffix array and Kasai LCP

Initially a rank represents one character. At doubling width `w`, each suffix is represented by `(rank[i], rank[i+w])`, with `-1` for a missing second block. Sorting these pairs orders prefixes of length `2w`. Equal pairs receive the same new rank and distinct pairs consecutive ranks. Induction on the width proves the prefix-order invariant. Doubling until the width covers the text, or until all ranks are distinct, produces the full suffix order. A shorter suffix sorts first when it ends inside an otherwise equal prefix.

The implementation uses `Integer[]` with Java's comparison-based `Arrays.sort` and a constant-time pair comparator. It creates no array of suffix strings and emits exactly indices `0..n-1`, with no sentinel suffix. Sorting costs `O(n log^2(n+1))` time over all rounds, and `O(n+1)` auxiliary memory, including boxed indices and sorting workspace.

Kasai uses inverse suffix positions. For original start `i`, it compares with the preceding suffix in the array, stores the length at `lcp[inverse[i]]`, and reuses at least `common-1` characters for the next start. Removing a shared first character leaves that many common characters; the lexicographic predecessor of the next suffix cannot have a shorter shared prefix than that guaranteed candidate. If the current suffix has rank zero, there is no predecessor and the reused length is reset. Increases of the common length total `O(n)` because it decreases at most once per step. Kasai therefore takes `O(n)` time and space, with `lcp[0]=0`.

Suffixes sharing a prefix form a contiguous interval in sorted order. A substring repeated at distinct starts is therefore shared by an adjacent pair somewhere in that interval. The largest adjacent LCP is exactly the longest repeated-substring length. The first strictly longer repeat encountered in original-start order is retained; ties may return any optimum. `banana` returns `ana`, whose occurrences overlap. Empty input yields empty arrays and an empty repeat; a single character yields `[0]`, `[0]`, and an empty repeat.

Input storage is `O(n)`; output storage including the repeated string is `O(n+1)`. Overall time is `O(n log^2(n+1))` for nonempty input and `O(1)` for empty input; auxiliary memory is `O(n+1)`.

### 6. Aho–Corasick

Insert every pattern into a trie and record the terminal state for each original dictionary position. Duplicate strings share a state but retain separate terminal entries. Each actual state has a 95-element primitive transition row; missing trie edges initially have value zero, which also represents a return to the root.

Breadth-first construction gives each state a failure link to the longest proper suffix represented in the trie. Root children have failure zero. For an existing edge `(u,c)->v`, set `failure[v]=go[failure[u]][c]`; for a missing edge set `go[u][c]=go[failure[u]][c]`. Failure targets are shallower, so their transition rows are complete when needed. No inherited lists of pattern matches are copied.

The text scan maintains the longest trie prefix that is a suffix of the text seen so far, using one transition per character. Increment visits to that state. After the scan, propagate visits to failure parents in reverse breadth-first order. Every pattern ending at a text position lies on the current state's failure chain, so propagation counts all suffix matches and overlaps exactly once without traversing every individual match. Read terminal visits in original dictionary order.

Every text starts from state zero with a fresh visits array; every request rebuilds its own automaton. An empty dictionary returns one empty row per text; no texts returns no rows; empty texts give zero counts. The `he, she, he` fixture checks suffix matches, duplicates and reset across equal independent texts.

With total pattern length `L`, `p` dictionary entries and `S` actual states including root, construction costs `O(L+95*S+p)` time and `O(95*S+p)` auxiliary memory. Actual trie states are allocated individually, so duplicate patterns do not allocate unused transition rows proportional to their total duplicate length. Each text costs `O(|text|+S+p)` time for scanning, initializing, propagation and output extraction. Input storage is `O(L+totalTextLength+p+q)`; output storage is `O(q*(p+1)+1)`. Counts use `long`. Dense matches do not create a per-occurrence output list.

## Own tests and results

[test_solution.py](tests/student/test_solution.py) uses seed **20261107**, checks the complete JSON output schema and validates witnesses with the supplied validator. Its expected optimum values are independently obtained: all subsequences for LCS, all legal adjacent-merge histories for merging, all subsets for small trees, Python `startswith` for search/counting, and explicit suffix-string sorting with direct character comparisons for small suffix inputs. These slow oracles are only test code.

The test suite checks 4,064 generated small cases twice in the same JVM (8,128 responses), 12 named fixtures, and 20 large cases grouped by command: **8,160 checked responses**. The repeated batch mixes all commands, catching leaked state. Tree labels and edge order are randomized. All binary texts of length at most 5 are paired with all binary patterns of length at most 3 for every search implementation. Named fixture input, expected result, actual result and pass status are saved in [fixture-results.json](tests/student/fixture-results.json). Witnesses are validated by meaning, not by exact equality to one tie-breaking choice.

|Command|Fixture / generated case|Expected result|Actual result / evidence|Defect checked|
|---|---|---|---|---|
|lcs|`tea_time`, `team`|length 4, `team`|PASS; fixture-results.json|Substring/subsequence confusion|
|lcs|`ab`, `ba`; either/both empty|length 1; empty optimum 0|PASS; fixtures and test-results.txt|Ties, zero boundaries|
|merge|`[1,100,1]`|cost 203; legal chronological plan|PASS; fixture-results.json|Illegal nonadjacent merge|
|merge|three blocks of 1,000,000,000|cost 5,000,000,000|PASS; fixture-results.json|32-bit overflow|
|merge|200 equal large blocks; 200 zeros|1,544,000,000,000; 0; each 199 valid merges|PASS; test-results.txt|Full cubic domain and plan validity|
|tree|weights `[4,9,6]`, chain|weight 10, vertices `[0,2]`|PASS; fixture-results.json|Reconstruction conflicts|
|tree|negative chain; empty/singleton/zero|empty set allowed; optimum 0 where appropriate|PASS; fixtures and generated cases|Forced selection of negative weights|
|tree|100,000-vertex positive chain/star|50,000,000,000,000 / 99,999,000,000,000|PASS; test-results.txt|Deep recursion and large sums|
|naive|`a b`, empty pattern|`[0,1,2,3]`|PASS; fixture-results.json|Final boundary missing|
|kmp|`abababa`, `aba`|`[0,2,4]`|PASS; fixture-results.json|Overlap lost after full match|
|rk|`abdj`, `ab`; `zab`, `ab`|`[0]`; `[1]`|PASS; fixture-results.json|Hash collision; negative remainder|
|all searches|empty text, longer pattern, ASCII case/punctuation; maximum dense inputs|Independent startsWith or formula|PASS; test-results.txt|Shared domain boundaries and full output|
|suffix|`banana`|SA `[5,3,1,0,4,2]`; LCP `[0,1,3,0,0,2]`; `ana`|PASS; fixture-results.json|Order, sentinel omission, overlapping repeat|
|suffix|empty/single/distinct/tied; 50,000 equal or alternating chars|Exact SA/LCP; repeat lengths 49,999 / 49,998|PASS; test-results.txt|Kasai reuse and full input size|
|aho|`he,she,he`; `sheshe,sheshe,empty`|`[[2,2,2],[2,2,2],[0,0,0]]`|PASS; fixture-results.json|Suffix patterns, duplicate columns, reset|
|aho|10,000 copies of `a`, 200,000-character text|200,000 in each column|PASS; test-results.txt|Dense duplicate aggregation|
|aho|patterns `a^1` through `a^446`|count `200001-length` per column|PASS; test-results.txt|Dense suffix matches|
|aho|100,000-character pattern, 100 short texts|100 zero rows|PASS; test-results.txt|Maximum trie depth and text count|
|aho|10,000 patterns, 100 texts of 2,000 `a` characters|1,000,000 counts equal to 2,000|PASS; test-results.txt|Maximum result dimensions|

For 200 equal merge sizes, a full binary tree of minimum external path length has leaf depths 7 and 8, giving `200*9-256=1544` units of cost. This gives a separate closed-form stress oracle. Large suffix-array expectations use descending lengths within each equal-character prefix group; they do not run the submitted suffix algorithm as an oracle.

All 44 supplied public cases and the repeated 88-request public batch passed. Logs are kept in [public-results.txt](tests/student/public-results.txt) and [test-results.txt](tests/student/test-results.txt). Each student domain-limit group completed below the supplied 20-second timeout with a 384 MiB Java heap. These are local results, not a claim about unseen grading cases or a hosted Actions run.

## Search comparison

[SearchBenchmark.java](tests/student/SearchBenchmark.java) compares all three methods on exactly the same inputs in one JVM. `benchmark.py` compiles first, outside the timing scope, and saves the measurements. Three increasing sizes are used: `(n,m)=(750,187),(1500,375),(3000,750)`, all in the common domain.

Random text is generated by `java.util.Random(20261107+n)` over `abcd`. Its pattern is the substring starting at `n/3`, guaranteeing a real match. Repetitive text and pattern are `a^n` and `a^m`, giving many overlapping matches. An additional near-miss case uses `a^n` and `a^(m-1)b`, stressing naive comparison without successful matches.

Before any warm-up or timing for a pair, every method's positions are checked against a separate `String.startsWith` oracle. Each method/pair receives 200 untimed warm-up calls. There are then three measured batches of 50 calls each; the method order is rotated across rounds. Timing uses `System.nanoTime`; reported units are **microseconds per invocation**, obtained by dividing elapsed batch time by 50. The boundary includes method dispatch, pattern preprocessing, comparisons, result allocation and assignment to a volatile sink. It excludes generation, correctness checks, compilation, process startup, JSON transport and printing. JIT is enabled; no explicit garbage collection is requested. This is a small classroom benchmark, not a statistically controlled JMH study.

The complete raw measurements and medians follow. They are also retained unchanged in [benchmark-results.md](tests/student/benchmark-results.md).

|Case|n|m|Seed|Algorithm|Run 1 (us/call)|Run 2|Run 3|Median|Correct outputs checked|
|---|---:|---:|---:|---|---:|---:|---:|---:|---|
|random|750|187|20261857|naive|25.668|28.718|26.438|26.438|yes (1 starts)|
|random|750|187|20261857|kmp|35.810|40.416|38.470|38.470|yes (1 starts)|
|random|750|187|20261857|rk|139.826|116.622|119.564|119.564|yes (1 starts)|
|random|1500|375|20262607|naive|60.928|5.434|4.558|5.434|yes (1 starts)|
|random|1500|375|20262607|kmp|9.188|7.980|8.878|8.878|yes (1 starts)|
|random|1500|375|20262607|rk|164.438|165.454|161.936|164.438|yes (1 starts)|
|random|3000|750|20264107|naive|9.676|9.076|9.000|9.076|yes (1 starts)|
|random|3000|750|20264107|kmp|17.068|16.072|16.100|16.100|yes (1 starts)|
|random|3000|750|20264107|rk|41.062|40.892|40.946|40.946|yes (1 starts)|
|repetitive|750|187|n/a|naive|209.164|210.836|217.774|210.836|yes (564 starts)|
|repetitive|750|187|n/a|kmp|14.014|13.676|13.602|13.676|yes (564 starts)|
|repetitive|750|187|n/a|rk|476.882|479.068|497.730|479.068|yes (564 starts)|
|repetitive|1500|375|n/a|naive|816.136|811.800|846.222|816.136|yes (1126 starts)|
|repetitive|1500|375|n/a|kmp|25.230|25.980|26.638|25.980|yes (1126 starts)|
|repetitive|1500|375|n/a|rk|1876.972|1881.380|1896.174|1881.380|yes (1126 starts)|
|repetitive|3000|750|n/a|naive|5045.612|4928.498|4836.442|4928.498|yes (2251 starts)|
|repetitive|3000|750|n/a|kmp|58.326|76.122|80.818|76.122|yes (2251 starts)|
|repetitive|3000|750|n/a|rk|9628.366|9749.698|9847.134|9749.698|yes (2251 starts)|
|near-miss|750|187|n/a|naive|315.252|309.316|300.098|309.316|yes (0 starts)|
|near-miss|750|187|n/a|kmp|11.446|12.004|7.030|11.446|yes (0 starts)|
|near-miss|750|187|n/a|rk|10.522|10.078|9.344|10.078|yes (0 starts)|
|near-miss|1500|375|n/a|naive|1284.096|1233.120|1322.898|1284.096|yes (0 starts)|
|near-miss|1500|375|n/a|kmp|14.450|17.754|23.250|17.754|yes (0 starts)|
|near-miss|1500|375|n/a|rk|12.930|13.838|17.148|13.838|yes (0 starts)|
|near-miss|3000|750|n/a|naive|4925.890|4931.588|4705.400|4925.890|yes (0 starts)|
|near-miss|3000|750|n/a|kmp|45.244|44.450|45.984|45.244|yes (0 starts)|
|near-miss|3000|750|n/a|rk|33.020|33.954|35.232|33.954|yes (0 starts)|

On dense repetitive inputs, KMP is much faster in this run because each text character is processed with bounded amortized work. Naive and exact RK repeatedly verify a long match; RK's hash cannot reject a true match. At n=3000 the measured medians were 76.122 us for KMP, 4928.498 us for naive and 9749.698 us for RK. On near misses at n=3000, RK rejects windows by hash and measured 33.954 us, while naive measured 4925.890 us.

Random data causes many early naive mismatches; prefix preprocessing and hash arithmetic can cost more than this saved work at small sizes. The random rows are not monotonic: for example, naive's n=1500 runs change from 60.928 to 5.434 to 4.558 us. JIT activity and scheduling can remain visible after the fixed warm-up. These observations and GC/allocation variability limit conclusions from just three batches. The data are reported as measured, not smoothed or used to prove asymptotic bounds.

Modulus 101 deliberately admits many collisions. Candidate verification preserves exactness but adds work; a larger production modulus would change the collision rate and constants. Neither these measurements nor a low empirical collision rate establish deterministic linear worst-case time for exact RK.

## Sources

- Course starter repository: [RZhaniya/daa-assignment-4-archive](https://github.com/RZhaniya/daa-assignment-4-archive). The provided JSON transport, launcher, public validator, fixtures and assignment documentation were reused. The launcher Java release and Actions JDK were changed to 21.
- Requirements and definitions: [Assignment_4_EN.md](docs/Assignment_4_EN.md), [CONTRACTS.md](docs/guide/CONTRACTS.md), [RUBRIC.md](docs/guide/RUBRIC.md), [TESTING.md](docs/guide/TESTING.md). The implementations follow the algorithms required there.

## AI Usage

ChatGPT helped me understand the tasks, check the solutions, and provided small hints throughout the work. It also wrote the report.

## Personal Verification

I personally ran the provided verification commands, including the public checks (`tools/run.py --check`) and the student tests (`tests/student/test_solution.py`). I checked the command output and did not observe any reported errors. This records my own execution of the checks, in addition to the automated runs documented earlier in this report.

## Submission status

The local implementation, tests and report are prepared. A personal GitHub repository, its pushed submission commit, successful hosted Actions run and Moodle submission must be completed separately. No final commit URL or submission status is fabricated here. The assessed defence is individual and must be taken without AI assistance.
