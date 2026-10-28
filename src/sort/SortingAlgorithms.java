package sort;

/**
 * MEMBER 2 - Sorting algorithms.
 *
 * All five algorithms are implemented by hand (Arrays.sort is NOT used).
 * Every method sorts the given array in ascending order, in place.
 */
public class SortingAlgorithms {

    private SortingAlgorithms() { }

    // ------------------------------------------------------------------
    // 1. INSERTION SORT   Time: best O(n), average/worst O(n^2)   Space: O(1)
    //    Takes each element and inserts it into the already-sorted left part.
    // ------------------------------------------------------------------
    public static void insertionSort(double[] a) {
        if (a == null) return;
        for (int i = 1; i < a.length; i++) {
            double key = a[i];
            int j = i - 1;
            // shift larger elements one step to the right
            while (j >= 0 && a[j] > key) {
                a[j + 1] = a[j];
                j--;
            }
            a[j + 1] = key;
        }
    }

    // ------------------------------------------------------------------
    // 2. SHELL SORT   Time: about O(n log^2 n) to O(n^2) depending on gaps   Space: O(1)
    //    Insertion sort on elements that are 'gap' apart; the gap shrinks to 1.
    // ------------------------------------------------------------------
    public static void shellSort(double[] a) {
        if (a == null) return;
        int n = a.length;
        for (int gap = n / 2; gap > 0; gap /= 2) {
            for (int i = gap; i < n; i++) {
                double temp = a[i];
                int j = i;
                while (j >= gap && a[j - gap] > temp) {
                    a[j] = a[j - gap];
                    j -= gap;
                }
                a[j] = temp;
            }
        }
    }

    // ------------------------------------------------------------------
    // 3. MERGE SORT   Time: O(n log n) always   Space: O(n)
    //    Divide the array in half, sort each half, then merge them.
    // ------------------------------------------------------------------
    public static void mergeSort(double[] a) {
        if (a == null || a.length < 2) return;
        double[] temp = new double[a.length];
        mergeSort(a, temp, 0, a.length - 1);
    }

    private static void mergeSort(double[] a, double[] temp, int low, int high) {
        if (low >= high) return;
        int mid = low + (high - low) / 2;
        mergeSort(a, temp, low, mid);
        mergeSort(a, temp, mid + 1, high);
        merge(a, temp, low, mid, high);
    }

    /** Merges the two sorted parts a[low..mid] and a[mid+1..high]. */
    private static void merge(double[] a, double[] temp, int low, int mid, int high) {
        System.arraycopy(a, low, temp, low, high - low + 1);
        int i = low;       // pointer in left part
        int j = mid + 1;   // pointer in right part
        for (int k = low; k <= high; k++) {
            if (i > mid) {
                a[k] = temp[j++];
            } else if (j > high) {
                a[k] = temp[i++];
            } else if (temp[j] < temp[i]) {
                a[k] = temp[j++];
            } else {
                a[k] = temp[i++];
            }
        }
    }

    // ------------------------------------------------------------------
    // 4. QUICK SORT   Time: average O(n log n), worst O(n^2)   Space: O(log n)
    //    Partition around a pivot (middle element, so sorted input is not the
    //    worst case). Recurse on the smaller part and loop on the larger part
    //    to keep recursion depth small (avoids StackOverflowError).
    // ------------------------------------------------------------------
    public static void quickSort(double[] a) {
        if (a == null || a.length < 2) return;
        quickSort(a, 0, a.length - 1);
    }

    private static void quickSort(double[] a, int low, int high) {
        while (low < high) {
            int p = partition(a, low, high);
            if (p - low < high - p) {
                quickSort(a, low, p);
                low = p + 1;
            } else {
                quickSort(a, p + 1, high);
                high = p;
            }
        }
    }

    /** Hoare partition scheme: returns index p so that [low..p] <= [p+1..high]. */
    private static int partition(double[] a, int low, int high) {
        double pivot = a[low + (high - low) / 2];
        int i = low - 1;
        int j = high + 1;
        while (true) {
            do { i++; } while (a[i] < pivot);
            do { j--; } while (a[j] > pivot);
            if (i >= j) return j;
            swap(a, i, j);
        }
    }

    // ------------------------------------------------------------------
    // 5. HEAP SORT   Time: O(n log n) always   Space: O(1)
    //    Build a max-heap, then repeatedly move the largest element to the end.
    // ------------------------------------------------------------------
    public static void heapSort(double[] a) {
        if (a == null || a.length < 2) return;
        int n = a.length;

        // Build max-heap (start from the last parent node)
        for (int i = n / 2 - 1; i >= 0; i--) {
            heapify(a, n, i);
        }
        // Extract the maximum one by one
        for (int end = n - 1; end > 0; end--) {
            swap(a, 0, end);      // move current max to its final place
            heapify(a, end, 0);   // restore heap on the remaining part
        }
    }

    /** Sinks the element at index i down until the max-heap property holds. */
    private static void heapify(double[] a, int heapSize, int i) {
        while (true) {
            int largest = i;
            int left = 2 * i + 1;
            int right = left + 1;
            if (left < heapSize && a[left] > a[largest]) largest = left;
            if (right < heapSize && a[right] > a[largest]) largest = right;
            if (largest == i) return;
            swap(a, i, largest);
            i = largest;
        }
    }

    private static void swap(double[] a, int i, int j) {
        double t = a[i];
        a[i] = a[j];
        a[j] = t;
    }
}
