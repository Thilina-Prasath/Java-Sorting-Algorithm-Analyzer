package util;

import sort.SortingAlgorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * MEMBER 3 - Performance evaluation.
 *
 * Runs every sorting algorithm on its OWN COPY of the data, measures the
 * execution time with System.nanoTime(), checks that the result is sorted,
 * and finds the fastest algorithm.
 */
public class PerformanceEvaluator {

    /** Algorithm name -> sorting method (insertion order is kept for display). */
    private static final Map<String, Consumer<double[]>> ALGORITHMS = new LinkedHashMap<>();

    static {
        ALGORITHMS.put("Insertion Sort", SortingAlgorithms::insertionSort);
        ALGORITHMS.put("Shell Sort", SortingAlgorithms::shellSort);
        ALGORITHMS.put("Merge Sort", SortingAlgorithms::mergeSort);
        ALGORITHMS.put("Quick Sort", SortingAlgorithms::quickSort);
        ALGORITHMS.put("Heap Sort", SortingAlgorithms::heapSort);
    }

    private PerformanceEvaluator() { }

    /**
     * Runs all algorithms on the data and returns one SortResult per algorithm.
     * The original array is never modified.
     */
    public static List<SortResult> evaluate(double[] data) {
        warmUp(data);

        List<SortResult> results = new ArrayList<>();
        for (Map.Entry<String, Consumer<double[]>> entry : ALGORITHMS.entrySet()) {
            // Every algorithm gets a fresh copy, otherwise later algorithms
            // would receive an already-sorted array.
            double[] copy = data.clone();

            long start = System.nanoTime();
            entry.getValue().accept(copy);
            long end = System.nanoTime();

            double timeMs = (end - start) / 1_000_000.0;
            results.add(new SortResult(entry.getKey(), timeMs, isSorted(copy)));
        }
        return results;
    }

    /**
     * Returns the result with the shortest time among the correctly sorted
     * runs, or null if the list is empty / nothing sorted correctly.
     */
    public static SortResult findBest(List<SortResult> results) {
        SortResult best = null;
        for (SortResult r : results) {
            if (r.correct() && (best == null || r.timeMs() < best.timeMs())) {
                best = r;
            }
        }
        return best;
    }

    /** Checks that the array is in ascending order. */
    public static boolean isSorted(double[] a) {
        for (int i = 1; i < a.length; i++) {
            if (a[i - 1] > a[i]) return false;
        }
        return true;
    }

    /**
     * Small warm-up so the JVM's JIT compiler has already optimised the
     * algorithms; otherwise the first algorithm would look unfairly slow.
     */
    private static void warmUp(double[] data) {
        double[] sample = Arrays.copyOf(data, Math.min(data.length, 2000));
        for (Consumer<double[]> algorithm : ALGORITHMS.values()) {
            algorithm.accept(sample.clone());
        }
    }
}