package com.util;

import java.util.function.Supplier;

/**
 * Performance Timer Utility
 * Provides methods for timing operations and logging performance metrics
 * 
 * Used for performance monitoring and optimization analysis
 */
public class PerformanceTimer {
    
    private static boolean enabled = true;
    
    /**
     * Enable or disable performance timing
     */
    public static void setEnabled(boolean enable) {
        enabled = enable;
    }
    
    /**
     * Time a query or operation and return the result
     * Logs the execution time to console
     * 
     * @param label Description of the operation being timed
     * @param operation The operation to execute and time
     * @return The result of the operation
     */
    public static <T> T timeOperation(String label, Supplier<T> operation) {
        if (!enabled) {
            return operation.get();
        }
        
        long startTime = System.nanoTime();
        T result = operation.get();
        long endTime = System.nanoTime();
        
        double durationMs = (endTime - startTime) / 1_000_000.0;
        System.out.printf("[PERF] %s: %.2f ms%n", label, durationMs);
        
        return result;
    }
    
    /**
     * Time a void operation (no return value)
     * 
     * @param label Description of the operation being timed
     * @param operation The operation to execute and time
     */
    public static void timeVoidOperation(String label, Runnable operation) {
        if (!enabled) {
            operation.run();
            return;
        }
        
        long startTime = System.nanoTime();
        operation.run();
        long endTime = System.nanoTime();
        
        double durationMs = (endTime - startTime) / 1_000_000.0;
        System.out.printf("[PERF] %s: %.2f ms%n", label, durationMs);
    }
    
    /**
     * Time an operation and return both the result and timing information
     * 
     * @param label Description of the operation
     * @param operation The operation to execute
     * @return TimedResult containing the result and execution time
     */
    public static <T> TimedResult<T> timeWithResult(String label, Supplier<T> operation) {
        long startTime = System.nanoTime();
        T result = operation.get();
        long endTime = System.nanoTime();
        
        double durationMs = (endTime - startTime) / 1_000_000.0;
        
        if (enabled) {
            System.out.printf("[PERF] %s: %.2f ms%n", label, durationMs);
        }
        
        return new TimedResult<>(result, durationMs);
    }
    
    /**
     * Container for operation result and timing information
     */
    public static class TimedResult<T> {
        private final T result;
        private final double durationMs;
        
        public TimedResult(T result, double durationMs) {
            this.result = result;
            this.durationMs = durationMs;
        }
        
        public T getResult() {
            return result;
        }
        
        public double getDurationMs() {
            return durationMs;
        }
        
        public String getFormattedDuration() {
            return String.format("%.2f ms", durationMs);
        }
    }
    
    /**
     * Log a performance warning if operation exceeds threshold
     * 
     * @param label Operation description
     * @param thresholdMs Threshold in milliseconds
     * @param operation The operation to execute
     * @return The result of the operation
     */
    public static <T> T timeWithThreshold(String label, double thresholdMs, Supplier<T> operation) {
        long startTime = System.nanoTime();
        T result = operation.get();
        long endTime = System.nanoTime();
        
        double durationMs = (endTime - startTime) / 1_000_000.0;
        
        if (durationMs > thresholdMs) {
            System.out.printf("[PERF WARNING] %s: %.2f ms (threshold: %.2f ms)%n", 
                            label, durationMs, thresholdMs);
        } else if (enabled) {
            System.out.printf("[PERF] %s: %.2f ms%n", label, durationMs);
        }
        
        return result;
    }
    
    /**
     * Compare performance of two implementations
     * 
     * @param label Test description
     * @param impl1Name Name of first implementation
     * @param impl1 First implementation
     * @param impl2Name Name of second implementation  
     * @param impl2 Second implementation
     */
    public static <T> void comparePerformance(String label, 
                                               String impl1Name, Supplier<T> impl1,
                                               String impl2Name, Supplier<T> impl2) {
        System.out.println("[PERF COMPARISON] " + label);
        
        // Run first implementation
        long start1 = System.nanoTime();
        impl1.get();
        long end1 = System.nanoTime();
        double time1 = (end1 - start1) / 1_000_000.0;
        
        // Run second implementation
        long start2 = System.nanoTime();
        impl2.get();
        long end2 = System.nanoTime();
        double time2 = (end2 - start2) / 1_000_000.0;
        
        // Calculate improvement
        double improvement = ((time1 - time2) / time1) * 100;
        
        System.out.printf("  %s: %.2f ms%n", impl1Name, time1);
        System.out.printf("  %s: %.2f ms%n", impl2Name, time2);
        
        if (improvement > 0) {
            System.out.printf("  %s is %.1f%% faster%n", impl2Name, improvement);
        } else {
            System.out.printf("  %s is %.1f%% faster%n", impl1Name, -improvement);
        }
    }
}
