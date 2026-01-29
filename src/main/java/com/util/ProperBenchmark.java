package com.util;

import java.util.*;

/**
 * Proper performance benchmarking with statistical validity
 */
public class ProperBenchmark {
    private static final int WARMUP_ITERATIONS = 10;
    private static final int MEASUREMENT_ITERATIONS = 50;
    private static final int MIN_SAMPLE_SIZE = 30;
    
    private final Map<String, List<Long>> baselineTimes = new HashMap<>();
    private final Map<String, List<Long>> optimizedTimes = new HashMap<>();
    
    public void runBenchmark() {
        System.out.println("=== PROPER PERFORMANCE BENCHMARK ===\n");
        
        // Phase 1: Baseline measurements (no optimizations)
        System.out.println("Phase 1: Baseline Performance (No Optimizations)");
        runBaselineTests();
        
        // Phase 2: Optimized measurements (with caching/indexing)
        System.out.println("\nPhase 2: Optimized Performance (With Caching/Indexing)");
        runOptimizedTests();
        
        // Phase 3: Statistical analysis and reporting
        System.out.println("\nPhase 3: Statistical Analysis");
        generateStatisticalReport();
    }
    
    private void runBaselineTests() {
        com.dao.ProductDAO dao = com.dao.ProductDAO.getInstance();
        
        // Warm-up phase (silent)
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            dao.getAllProducts();
            dao.getProductById(1);
            dao.searchProducts("laptop");
        }
        
        // Measurement phase (silent)
        List<Long> getAllTimes = new ArrayList<>();
        for (int i = 0; i < MEASUREMENT_ITERATIONS; i++) {
            dao.invalidateCache();
            
            long start = System.nanoTime();
            dao.getAllProducts();
            long duration = System.nanoTime() - start;
            getAllTimes.add(duration);
            
            try { Thread.sleep(10); } catch (InterruptedException e) {}
        }
        baselineTimes.put("getAllProducts", getAllTimes);
        
        // Test getProductById
        List<Long> getByIdTimes = new ArrayList<>();
        for (int i = 0; i < MEASUREMENT_ITERATIONS; i++) {
            dao.invalidateCache();
            
            long start = System.nanoTime();
            dao.getProductById((i % 10) + 1);
            long duration = System.nanoTime() - start;
            getByIdTimes.add(duration);
            
            try { Thread.sleep(10); } catch (InterruptedException e) {}
        }
        baselineTimes.put("getProductById", getByIdTimes);
        
        // Test searchProducts
        String[] searchTerms = {"laptop", "phone", "book", "shirt", "watch"};
        List<Long> searchTimes = new ArrayList<>();
        for (int i = 0; i < MEASUREMENT_ITERATIONS; i++) {
            dao.invalidateCache();
            
            String term = searchTerms[i % searchTerms.length];
            long start = System.nanoTime();
            dao.searchProducts(term);
            long duration = System.nanoTime() - start;
            searchTimes.add(duration);
            
            try { Thread.sleep(10); } catch (InterruptedException e) {}
        }
        baselineTimes.put("searchProducts", searchTimes);
    }
    
    private void runOptimizedTests() {
        com.dao.ProductionOptimizedDAO dao = com.dao.ProductionOptimizedDAO.getInstance();
        
        // Warm-up phase (silent)
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            dao.getAllProducts();
            dao.getProductById(1);
            dao.searchProducts("laptop");
        }
        
        // Measurement phase (silent)
        List<Long> getAllTimes = new ArrayList<>();
        dao.clearCache();
        
        for (int i = 0; i < MEASUREMENT_ITERATIONS; i++) {
            long start = System.nanoTime();
            dao.getAllProducts();
            long duration = System.nanoTime() - start;
            getAllTimes.add(duration);
            
            try { Thread.sleep(10); } catch (InterruptedException e) {}
        }
        optimizedTimes.put("getAllProducts", getAllTimes);
        
        // Test getProductById with caching
        List<Long> getByIdTimes = new ArrayList<>();
        dao.clearCache();
        
        for (int i = 0; i < MEASUREMENT_ITERATIONS; i++) {
            int productId = (i % 10) + 1;
            long start = System.nanoTime();
            dao.getProductById(productId);
            long duration = System.nanoTime() - start;
            getByIdTimes.add(duration);
            
            try { Thread.sleep(10); } catch (InterruptedException e) {}
        }
        optimizedTimes.put("getProductById", getByIdTimes);
        
        // Test searchProducts with caching
        String[] searchTerms = {"laptop", "phone", "book", "shirt", "watch"};
        List<Long> searchTimes = new ArrayList<>();
        dao.clearCache();
        
        for (int i = 0; i < MEASUREMENT_ITERATIONS; i++) {
            String term = searchTerms[i % searchTerms.length];
            long start = System.nanoTime();
            dao.searchProducts(term);
            long duration = System.nanoTime() - start;
            searchTimes.add(duration);
            
            try { Thread.sleep(10); } catch (InterruptedException e) {}
        }
        optimizedTimes.put("searchProducts", searchTimes);
    }
    
    private void generateStatisticalReport() {
        System.out.println("\n=== STATISTICAL PERFORMANCE ANALYSIS ===\n");
        
        for (String operation : baselineTimes.keySet()) {
            List<Long> baseline = baselineTimes.get(operation);
            List<Long> optimized = optimizedTimes.get(operation);
            
            if (baseline.size() < MIN_SAMPLE_SIZE || optimized.size() < MIN_SAMPLE_SIZE) {
                System.out.println("⚠️  " + operation + ": Insufficient sample size for statistical analysis");
                continue;
            }
            
            BenchmarkStats baselineStats = calculateStats(baseline);
            BenchmarkStats optimizedStats = calculateStats(optimized);
            
            double improvementPercent = ((baselineStats.mean - optimizedStats.mean) / baselineStats.mean) * 100;
            
            System.out.println("📊 " + operation + ":");
            System.out.printf("   Baseline:  %.2f ms ± %.2f ms (n=%d)\n", 
                baselineStats.mean / 1_000_000.0, baselineStats.stdDev / 1_000_000.0, baseline.size());
            System.out.printf("   Optimized: %.2f ms ± %.2f ms (n=%d)\n", 
                optimizedStats.mean / 1_000_000.0, optimizedStats.stdDev / 1_000_000.0, optimized.size());
            System.out.printf("   Improvement: %.1f%% %s\n", 
                Math.abs(improvementPercent), improvementPercent > 0 ? "faster" : "slower");
            System.out.printf("   Statistical Significance: %s\n\n", 
                isStatisticallySignificant(baseline, optimized) ? "✅ Significant" : "❌ Not Significant");
        }
    }
    
    private BenchmarkStats calculateStats(List<Long> times) {
        double mean = times.stream().mapToLong(Long::longValue).average().orElse(0);
        double variance = times.stream()
            .mapToDouble(time -> Math.pow(time - mean, 2))
            .average().orElse(0);
        double stdDev = Math.sqrt(variance);
        
        return new BenchmarkStats(mean, stdDev);
    }
    
    private boolean isStatisticallySignificant(List<Long> baseline, List<Long> optimized) {
        BenchmarkStats baselineStats = calculateStats(baseline);
        BenchmarkStats optimizedStats = calculateStats(optimized);
        
        double pooledStdDev = Math.sqrt(
            (Math.pow(baselineStats.stdDev, 2) + Math.pow(optimizedStats.stdDev, 2)) / 2
        );
        
        double tStat = Math.abs(baselineStats.mean - optimizedStats.mean) / 
            (pooledStdDev * Math.sqrt(2.0 / baseline.size()));
        
        return tStat > 2.0;
    }
    
    private static class BenchmarkStats {
        final double mean;
        final double stdDev;
        
        BenchmarkStats(double mean, double stdDev) {
            this.mean = mean;
            this.stdDev = stdDev;
        }
    }
}