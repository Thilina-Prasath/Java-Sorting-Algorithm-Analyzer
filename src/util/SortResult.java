package util;

/**
 * MEMBER 3 - Holds the result of one algorithm run.
 *
 * @param algorithm name of the algorithm, e.g. "Merge Sort"
 * @param timeMs    execution time in milliseconds
 * @param correct   true if the output array was verified as sorted
 */
public record SortResult(String algorithm, double timeMs, boolean correct) { }