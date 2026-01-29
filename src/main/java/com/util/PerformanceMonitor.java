package com.util;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Performance monitoring utility for tracking query execution times
 * Supports pre/post optimization comparisons
 */
public class PerformanceMonitor {
    private static final PerformanceMonitor instance = new PerformanceMonitor();
    private final Map<String, List<Long>> preOptimizationTimes = new ConcurrentHashMap<>();
    private final Map<String, List<Long>> postOptimizationTimes = new ConcurrentHashMap<>();
    private boolean optimizationEnabled = false;

    private PerformanceMonitor() {}

    public static PerformanceMonitor getInstance() {
        return instance;
    }

    public void enableOptimization() {
        this.optimizationEnabled = true;
    }

    public long startTimer() {
        return System.nanoTime();
    }

    public void recordQueryTime(String queryName, long startTime) {
        long duration = System.nanoTime() - startTime;
        Map<String, List<Long>> targetMap = optimizationEnabled ? postOptimizationTimes : preOptimizationTimes;
        targetMap.computeIfAbsent(queryName, k -> new ArrayList<>()).add(duration);
    }

    public PerformanceReport generateReport() {
        return new PerformanceReport(preOptimizationTimes, postOptimizationTimes);
    }

    public static class PerformanceReport {
        private final Map<String, QueryStats> comparisons = new HashMap<>();

        public PerformanceReport(Map<String, List<Long>> preOpt, Map<String, List<Long>> postOpt) {
            Set<String> allQueries = new HashSet<>(preOpt.keySet());
            allQueries.addAll(postOpt.keySet());

            for (String query : allQueries) {
                List<Long> preTimes = preOpt.getOrDefault(query, Collections.emptyList());
                List<Long> postTimes = postOpt.getOrDefault(query, Collections.emptyList());
                comparisons.put(query, new QueryStats(preTimes, postTimes));
            }
        }

        public String generateTextReport() {
            StringBuilder report = new StringBuilder();
            report.append("=== PERFORMANCE OPTIMIZATION REPORT ===\n\n");
            
            for (Map.Entry<String, QueryStats> entry : comparisons.entrySet()) {
                QueryStats stats = entry.getValue();
                report.append(String.format("Query: %s\n", entry.getKey()));
                report.append(String.format("  Pre-optimization:  %.2f ms (avg), %d samples\n", 
                    stats.preAvgMs, stats.preSamples));
                report.append(String.format("  Post-optimization: %.2f ms (avg), %d samples\n", 
                    stats.postAvgMs, stats.postSamples));
                report.append(String.format("  Improvement: %.1f%% faster\n\n", stats.improvementPercent));
            }
            
            return report.toString();
        }

        public Map<String, QueryStats> getComparisons() {
            return comparisons;
        }
    }

    public static class QueryStats {
        public final double preAvgMs;
        public final double postAvgMs;
        public final int preSamples;
        public final int postSamples;
        public final double improvementPercent;

        public QueryStats(List<Long> preTimes, List<Long> postTimes) {
            this.preSamples = preTimes.size();
            this.postSamples = postTimes.size();
            this.preAvgMs = preTimes.isEmpty() ? 0 : preTimes.stream().mapToLong(Long::longValue).average().orElse(0) / 1_000_000.0;
            this.postAvgMs = postTimes.isEmpty() ? 0 : postTimes.stream().mapToLong(Long::longValue).average().orElse(0) / 1_000_000.0;
            this.improvementPercent = preAvgMs > 0 ? ((preAvgMs - postAvgMs) / preAvgMs) * 100 : 0;
        }
    }
}