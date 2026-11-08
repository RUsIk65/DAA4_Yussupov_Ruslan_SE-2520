import java.util.*;

/** In-process benchmark: generation, oracle, JSON and printing are outside timing. */
public final class SearchBenchmark {
    private static volatile Object sink;
    private static final int WARMUP = 200;
    private static final int REPETITIONS = 50;

    private static Map<String, Object> search(String algorithm, Map<String, Object> input) {
        return switch (algorithm) {
            case "naive" -> Algorithms.naive(input);
            case "kmp" -> Algorithms.kmp(input);
            case "rk" -> Algorithms.rk(input);
            default -> throw new IllegalArgumentException(algorithm);
        };
    }

    private static List<Integer> oracle(String text, String pattern) {
        List<Integer> starts = new ArrayList<>();
        for (int i = 0; i <= text.length(); i++) {
            if (text.startsWith(pattern, i)) starts.add(i);
        }
        return starts;
    }

    public static void main(String[] args) {
        Locale.setDefault(Locale.ROOT);
        System.out.println("# Search benchmark measurements");
        System.out.println();
        System.out.println("Runtime: " + System.getProperty("java.runtime.version") + "; "
                + System.getProperty("java.vm.name") + ". OS: " + System.getProperty("os.name")
                + " " + System.getProperty("os.version") + " " + System.getProperty("os.arch") + ".");
        System.out.println("JVM arguments: " + java.lang.management.ManagementFactory.getRuntimeMXBean().getInputArguments());
        System.out.println("Each cell is microseconds per call: elapsed nanoTime / 1000 / " + REPETITIONS + ".");
        System.out.println("Each method/case has " + WARMUP + " untimed warm-up calls, then 3 measured batches of "
                + REPETITIONS + " calls. JIT remains enabled; no explicit GC. Allocation and preprocessing are included.");
        System.out.println("All three outputs are checked against startsWith before warm-up. Volatile sink retains results.");
        System.out.println("Random: java.util.Random(seed=20261107+n), alphabet abcd; pattern is text[n/3:n/3+m].");
        System.out.println("Repetitive: text=a^n, pattern=a^m. Near-miss: text=a^n, pattern=a^(m-1)b.");
        System.out.println();
        System.out.println("|Case|n|m|Seed|Algorithm|Run 1 (us/call)|Run 2|Run 3|Median|Correct outputs checked|");
        System.out.println("|---|---:|---:|---:|---|---:|---:|---:|---:|---|");
        for (String kind : List.of("random", "repetitive", "near-miss")) {
            for (int n : new int[]{750, 1500, 3000}) {
                int m = n / 4;
                long seed = 20261107L + n;
                Random random = new Random(seed);
                StringBuilder generated = new StringBuilder(n);
                for (int i = 0; i < n; i++) generated.append("abcd".charAt(random.nextInt(4)));
                String text = kind.equals("random") ? generated.toString() : "a".repeat(n);
                String pattern = switch (kind) {
                    case "random" -> text.substring(n / 3, n / 3 + m);
                    case "repetitive" -> "a".repeat(m);
                    default -> "a".repeat(m - 1) + "b";
                };
                Map<String, Object> input = Map.of("text", text, "pattern", pattern);
                List<Integer> expected = oracle(text, pattern);
                String[] methods = {"naive", "kmp", "rk"};
                for (String method : methods) {
                    if (!search(method, input).get("positions").equals(expected)) {
                        throw new AssertionError(kind + " " + n + " " + method);
                    }
                    for (int i = 0; i < WARMUP; i++) sink = search(method, input);
                }
                double[][] times = new double[3][3];
                // Rotate method order across measured rounds to reduce order bias.
                for (int round = 0; round < 3; round++) {
                    for (int offset = 0; offset < 3; offset++) {
                        int index = (round + offset) % 3;
                        long start = System.nanoTime();
                        for (int i = 0; i < REPETITIONS; i++) sink = search(methods[index], input);
                        times[index][round] = (System.nanoTime() - start) / (1000.0 * REPETITIONS);
                    }
                }
                for (int index = 0; index < 3; index++) {
                    double[] ordered = times[index].clone();
                    Arrays.sort(ordered);
                    System.out.printf("|%s|%d|%d|%s|%s|%.3f|%.3f|%.3f|%.3f|yes (%d starts)|%n",
                            kind, n, m, kind.equals("random") ? Long.toString(seed) : "n/a", methods[index],
                            times[index][0], times[index][1], times[index][2], ordered[1], expected.size());
                }
            }
        }
    }
}
