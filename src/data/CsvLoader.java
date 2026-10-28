package data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * MEMBER 1 - File upload and dataset handling.
 *
 * Reads a .csv file, stores the column names and rows, and converts a
 * chosen column into a double[] ready for sorting.
 *
 * Error handling (the GUI shows these messages to the user):
 *   - IOException              -> file missing / cannot be read
 *   - IllegalArgumentException -> invalid content (not .csv, empty file,
 *                                 empty column, non-numeric column)
 */
public class CsvLoader {

    /** Values treated as "missing" and skipped when reading a numeric column. */
    private static final Set<String> MISSING_MARKERS =
            Set.of("na", "n/a", "nan", "null", "none", "-");

    private final List<String> columnNames = new ArrayList<>();
    private final List<String[]> rows = new ArrayList<>();
    private String fileName = "";

    /**
     * Loads and validates a CSV file. The old data is replaced only if
     * the new file loads successfully.
     */
    public void load(File file) throws IOException {
        if (file == null || !file.isFile()) {
            throw new IOException("The selected file does not exist.");
        }
        if (!file.getName().toLowerCase().endsWith(".csv")) {
            throw new IllegalArgumentException("Invalid file type. Please select a .csv file.");
        }

        List<String> newColumns = new ArrayList<>();
        List<String[]> newRows = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {

            String headerLine = reader.readLine();
            if (headerLine == null || headerLine.isBlank()) {
                throw new IllegalArgumentException("The CSV file is empty.");
            }
            // Remove the invisible BOM character that Excel adds to some files
            if (headerLine.charAt(0) == '\uFEFF') {
                headerLine = headerLine.substring(1);
            }
            for (String name : parseLine(headerLine)) {
                newColumns.add(name.trim());
            }

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue; // skip empty lines
                }
                newRows.add(parseLine(line));
            }
        }

        if (newRows.isEmpty()) {
            throw new IllegalArgumentException("The CSV file has a header but no data rows.");
        }

        columnNames.clear();
        columnNames.addAll(newColumns);
        rows.clear();
        rows.addAll(newRows);
        fileName = file.getName();
    }

    /** Returns the column names found in the header row. */
    public List<String> getColumnNames() {
        return new ArrayList<>(columnNames);
    }

    /** Returns the number of data rows (header excluded). */
    public int getRowCount() {
        return rows.size();
    }

    /** Returns the name of the loaded file. */
    public String getFileName() {
        return fileName;
    }

    /**
     * Converts the given column to a double[].
     * Empty cells and markers like "NA" are skipped.
     *
     * @throws IllegalArgumentException if the column does not exist, is empty,
     *                                  or contains non-numeric values
     */
    public double[] getNumericColumn(String columnName) {
        int index = columnNames.indexOf(columnName);
        if (index < 0) {
            throw new IllegalArgumentException("Column not found: " + columnName);
        }

        double[] buffer = new double[rows.size()];
        int count = 0;

        for (String[] row : rows) {
            if (index >= row.length) {
                continue; // short row, treat as missing
            }
            String value = row[index].trim();
            if (value.isEmpty() || MISSING_MARKERS.contains(value.toLowerCase())) {
                continue;
            }
            try {
                // remove thousands separators such as "1,234"
                double number = Double.parseDouble(value.replace(",", ""));
                if (Double.isNaN(number) || Double.isInfinite(number)) {
                    throw new NumberFormatException();
                }
                buffer[count++] = number;
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Column '" + columnName
                        + "' is not numeric (found value: '" + value
                        + "'). Please select a numeric column.");
            }
        }

        if (count == 0) {
            throw new IllegalArgumentException("Column '" + columnName + "' has no data (empty column).");
        }
        return Arrays.copyOf(buffer, count);
    }

    /**
     * Splits one CSV line into fields. Handles quoted values that contain
     * commas, e.g.  1,"Colombo, LK",25  ->  [1] [Colombo, LK] [25]
     * and escaped quotes ("") inside quoted values.
     */
    private static String[] parseLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (inQuotes) {
                if (ch == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        current.append('"'); // escaped quote
                        i++;
                    } else {
                        inQuotes = false;    // closing quote
                    }
                } else {
                    current.append(ch);
                }
            } else {
                if (ch == '"') {
                    inQuotes = true;
                } else if (ch == ',') {
                    fields.add(current.toString());
                    current.setLength(0);
                } else {
                    current.append(ch);
                }
            }
        }
        fields.add(current.toString());
        return fields.toArray(new String[0]);
    }
}
