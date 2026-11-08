import java.util.*;

/** Required algorithms; all working state belongs to one request. */
public final class Algorithms {
    private Algorithms() {}

    private static String string(Map<String, Object> input, String key) {
        return (String) input.get(key);
    }

    private static List<?> list(Map<String, Object> input, String key) {
        return (List<?>) input.get(key);
    }

    private static long number(Object value) {
        return ((Number) value).longValue();
    }

    static Map<String, Object> lcs(Map<String, Object> input) {
        String a = string(input, "a"), b = string(input, "b");
        int n = a.length(), m = b.length();
        int[][] dp = new int[n + 1][m + 1];
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                dp[i][j] = a.charAt(i - 1) == b.charAt(j - 1)
                        ? dp[i - 1][j - 1] + 1 : Math.max(dp[i - 1][j], dp[i][j - 1]);
            }
        }
        StringBuilder sequence = new StringBuilder();
        int i = n, j = m;
        while (i > 0 && j > 0) {
            if (a.charAt(i - 1) == b.charAt(j - 1)) {
                sequence.append(a.charAt(--i));
                j--;
            } else if (dp[i - 1][j] >= dp[i][j - 1]) {
                i--; // Prefer the upper cell on a tie.
            } else {
                j--;
            }
        }
        return Map.of("length", dp[n][m], "sequence", sequence.reverse().toString());
    }

    static Map<String, Object> merge(Map<String, Object> input) {
        List<?> sizes = list(input, "sizes");
        int n = sizes.size();
        List<int[]> plan = new ArrayList<>();
        if (n < 2) return Map.of("cost", 0L, "plan", plan);
        long[] prefix = new long[n + 1];
        for (int i = 0; i < n; i++) prefix[i + 1] = prefix[i] + number(sizes.get(i));
        long[][] dp = new long[n][n];
        int[][] split = new int[n][n];
        for (int length = 2; length <= n; length++) {
            for (int left = 0; left + length <= n; left++) {
                int right = left + length - 1;
                dp[left][right] = Long.MAX_VALUE;
                long sum = prefix[right + 1] - prefix[left];
                for (int k = left; k < right; k++) {
                    long candidate = dp[left][k] + dp[k + 1][right] + sum;
                    if (candidate < dp[left][right]) {
                        dp[left][right] = candidate;
                        split[left][right] = k;
                    }
                }
            }
        }
        // Child merges precede the parent merge in this explicit postorder.
        Deque<int[]> stack = new ArrayDeque<>();
        stack.push(new int[]{0, n - 1, 0});
        while (!stack.isEmpty()) {
            int[] frame = stack.pop();
            int left = frame[0], right = frame[1];
            if (left == right) continue;
            int k = split[left][right];
            if (frame[2] == 1) {
                plan.add(new int[]{left, k, right});
            } else {
                stack.push(new int[]{left, right, 1});
                stack.push(new int[]{k + 1, right, 0});
                stack.push(new int[]{left, k, 0});
            }
        }
        return Map.of("cost", dp[0][n - 1], "plan", plan);
    }

    static Map<String, Object> tree(Map<String, Object> input) {
        int n = (int) number(input.get("n"));
        List<Integer> selected = new ArrayList<>();
        if (n == 0) return Map.of("weight", 0L, "selected", selected);
        List<?> weights = list(input, "weights"), edges = list(input, "edges");
        int[] degree = new int[n];
        for (Object edge : edges) {
            List<?> e = (List<?>) edge;
            degree[(int) number(e.get(0))]++;
            degree[(int) number(e.get(1))]++;
        }
        int[][] adjacent = new int[n][];
        for (int u = 0; u < n; u++) adjacent[u] = new int[degree[u]];
        Arrays.fill(degree, 0);
        for (Object edge : edges) {
            List<?> e = (List<?>) edge;
            int u = (int) number(e.get(0)), v = (int) number(e.get(1));
            adjacent[u][degree[u]++] = v;
            adjacent[v][degree[v]++] = u;
        }
        int[] parent = new int[n], order = new int[n];
        parent[0] = -1;
        int count = 1;
        for (int i = 0; i < n; i++) {
            int u = order[i];
            for (int v : adjacent[u]) {
                if (v != parent[u]) {
                    parent[v] = u;
                    order[count++] = v;
                }
            }
        }
        long[] take = new long[n], skip = new long[n];
        for (int u = 0; u < n; u++) take[u] = number(weights.get(u));
        for (int i = n - 1; i > 0; i--) {
            int u = order[i], p = parent[u];
            take[p] += skip[u];
            skip[p] += Math.max(take[u], skip[u]);
        }
        boolean[] chosen = new boolean[n];
        for (int u : order) {
            chosen[u] = (parent[u] == -1 || !chosen[parent[u]]) && take[u] > skip[u];
            if (chosen[u]) selected.add(u); // Skip on a tie, including zero weights.
        }
        return Map.of("weight", Math.max(take[0], skip[0]), "selected", selected);
    }

    static Map<String, Object> naive(Map<String, Object> input) {
        String text = string(input, "text"), pattern = string(input, "pattern");
        int n = text.length(), m = pattern.length();
        List<Integer> positions = new ArrayList<>();
        for (int start = 0; start <= n - m; start++) {
            int j = 0;
            while (j < m && text.charAt(start + j) == pattern.charAt(j)) j++;
            if (j == m) positions.add(start);
        }
        return Map.of("positions", positions);
    }

    private static Map<String, Object> boundaries(int n) {
        List<Integer> positions = new ArrayList<>(n + 1);
        for (int i = 0; i <= n; i++) positions.add(i);
        return Map.of("positions", positions);
    }

    static Map<String, Object> kmp(Map<String, Object> input) {
        String text = string(input, "text"), pattern = string(input, "pattern");
        int n = text.length(), m = pattern.length();
        if (m == 0) return boundaries(n);
        int[] pi = new int[m];
        for (int i = 1, j = 0; i < m; i++) {
            while (j > 0 && pattern.charAt(i) != pattern.charAt(j)) j = pi[j - 1];
            if (pattern.charAt(i) == pattern.charAt(j)) j++;
            pi[i] = j;
        }
        List<Integer> positions = new ArrayList<>();
        for (int i = 0, j = 0; i < n; i++) {
            while (j > 0 && text.charAt(i) != pattern.charAt(j)) j = pi[j - 1];
            if (text.charAt(i) == pattern.charAt(j)) j++;
            if (j == m) {
                positions.add(i - m + 1);
                j = pi[j - 1]; // Preserve overlapping matches.
            }
        }
        return Map.of("positions", positions);
    }

    static Map<String, Object> rk(Map<String, Object> input) {
        String text = string(input, "text"), pattern = string(input, "pattern");
        int n = text.length(), m = pattern.length();
        if (m == 0) return boundaries(n);
        List<Integer> positions = new ArrayList<>();
        if (m > n) return Map.of("positions", positions);
        long power = 1, target = 0, window = 0;
        for (int i = 0; i < m; i++) {
            target = (target * 31 + pattern.charAt(i)) % 101;
            window = (window * 31 + text.charAt(i)) % 101;
            if (i > 0) power = power * 31 % 101;
        }
        for (int start = 0; start <= n - m; start++) {
            if (target == window) {
                int j = 0;
                while (j < m && text.charAt(start + j) == pattern.charAt(j)) j++;
                if (j == m) positions.add(start);
            }
            if (start < n - m) {
                window = Math.floorMod((window - text.charAt(start) * power) * 31
                        + text.charAt(start + m), 101L);
            }
        }
        return Map.of("positions", positions);
    }

    static Map<String, Object> suffix(Map<String, Object> input) {
        String text = string(input, "text");
        int n = text.length();
        Integer[] sa = new Integer[n];
        int[] rank = new int[n];
        for (int i = 0; i < n; i++) {
            sa[i] = i;
            rank[i] = text.charAt(i);
        }
        for (int width = 1; width < n; width *= 2) {
            final int step = width;
            final int[] previous = rank;
            Comparator<Integer> pairOrder = (a, b) -> {
                int first = Integer.compare(previous[a], previous[b]);
                if (first != 0) return first;
                return Integer.compare(a + step < n ? previous[a + step] : -1,
                        b + step < n ? previous[b + step] : -1);
            };
            Arrays.sort(sa, pairOrder);
            rank = new int[n];
            for (int i = 1; i < n; i++) {
                rank[sa[i]] = rank[sa[i - 1]] + (pairOrder.compare(sa[i - 1], sa[i]) == 0 ? 0 : 1);
            }
            if (rank[sa[n - 1]] == n - 1) break;
        }
        int[] inverse = new int[n], lcp = new int[n];
        for (int i = 0; i < n; i++) inverse[sa[i]] = i;
        int bestLength = 0, bestStart = 0;
        for (int i = 0, common = 0; i < n; i++) {
            int place = inverse[i];
            if (place == 0) {
                common = 0;
                continue;
            }
            int other = sa[place - 1];
            while (i + common < n && other + common < n
                    && text.charAt(i + common) == text.charAt(other + common)) common++;
            lcp[place] = common;
            if (common > bestLength) {
                bestLength = common;
                bestStart = i;
            }
            if (common > 0) common--;
        }
        return Map.of("sa", sa, "lcp", lcp,
                "repeated", text.substring(bestStart, bestStart + bestLength));
    }

    static Map<String, Object> aho(Map<String, Object> input) {
        List<?> patterns = list(input, "patterns"), texts = list(input, "texts");
        // Only actual trie states are allocated; duplicates share terminal states.
        List<int[]> transitions = new ArrayList<>();
        transitions.add(new int[95]);
        int[] terminal = new int[patterns.size()];
        for (int i = 0; i < patterns.size(); i++) {
            String pattern = (String) patterns.get(i);
            int state = 0;
            for (int j = 0; j < pattern.length(); j++) {
                int c = pattern.charAt(j) - 32;
                int[] row = transitions.get(state);
                if (row[c] == 0) {
                    row[c] = transitions.size();
                    transitions.add(new int[95]);
                }
                state = row[c];
            }
            terminal[i] = state;
        }
        int states = transitions.size();
        int[] failure = new int[states], order = new int[states];
        int head = 0, tail = 0;
        for (int child : transitions.get(0)) if (child != 0) order[tail++] = child;
        while (head < tail) {
            int state = order[head++];
            int[] row = transitions.get(state), fallback = transitions.get(failure[state]);
            for (int c = 0; c < 95; c++) {
                if (row[c] == 0) {
                    row[c] = fallback[c];
                } else {
                    failure[row[c]] = fallback[c];
                    order[tail++] = row[c];
                }
            }
        }
        List<long[]> counts = new ArrayList<>();
        for (Object value : texts) {
            String text = (String) value;
            long[] visits = new long[states];
            int state = 0;
            for (int i = 0; i < text.length(); i++) {
                state = transitions.get(state)[text.charAt(i) - 32];
                visits[state]++;
            }
            for (int i = tail - 1; i >= 0; i--) {
                int v = order[i];
                visits[failure[v]] += visits[v];
            }
            long[] row = new long[terminal.length];
            for (int i = 0; i < terminal.length; i++) row[i] = visits[terminal[i]];
            counts.add(row);
        }
        return Map.of("counts", counts);
    }
}
