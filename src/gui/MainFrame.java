package gui;

import data.CsvLoader;
import util.PerformanceEvaluator;
import util.SortResult;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * MEMBER 3 - Main window (GUI).
 *
 * Layout:
 *   top    : [Upload CSV] file name | Column [dropdown] [Sort & Evaluate]
 *   center : ResultsPanel (Member 1)
 *   bottom : status label
 *
 * Sorting runs in a SwingWorker (background thread) so the window does not
 * freeze with large datasets.
 */
public class MainFrame extends JFrame {

    private final CsvLoader loader = new CsvLoader();

    private final JButton uploadButton = new JButton("Upload CSV");
    private final JLabel fileLabel = new JLabel("No file selected");
    private final JComboBox<String> columnBox = new JComboBox<>();
    private final JButton sortButton = new JButton("Sort & Evaluate");
    private final JLabel statusLabel = new JLabel("Ready. Please upload a CSV file.");
    private final ResultsPanel resultsPanel = new ResultsPanel();

    public MainFrame() {
        super("Sorting Algorithm Performance Evaluator");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(750, 480);
        setMinimumSize(new Dimension(650, 400));
        setLocationRelativeTo(null);

        buildLayout();
        addListeners();
    }

    /** Creates and arranges all components. */
    private void buildLayout() {
        columnBox.setPreferredSize(new Dimension(180, 28));
        columnBox.setEnabled(false);
        sortButton.setEnabled(false);

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        topPanel.add(uploadButton);
        topPanel.add(fileLabel);
        topPanel.add(new JLabel("   Column:"));
        topPanel.add(columnBox);
        topPanel.add(sortButton);

        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        setLayout(new BorderLayout(5, 5));
        add(topPanel, BorderLayout.NORTH);
        add(resultsPanel, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);
    }

    private void addListeners() {
        uploadButton.addActionListener(e -> onUpload());
        sortButton.addActionListener(e -> onSort());
    }

    // ------------------------------------------------------------------
    // Upload button
    // ------------------------------------------------------------------
    private void onUpload() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select a CSV file");
        chooser.setFileFilter(new FileNameExtensionFilter("CSV files (*.csv)", "csv"));

        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return; // user cancelled
        }
        File file = chooser.getSelectedFile();

        try {
            loader.load(file);                       // Member 1's code
        } catch (IllegalArgumentException | IOException ex) {
            resetAfterError();
            showError(ex.getMessage());
            return;
        }

        // Show the columns in the dropdown
        columnBox.removeAllItems();
        for (String name : loader.getColumnNames()) {
            columnBox.addItem(name);
        }
        columnBox.setEnabled(true);
        sortButton.setEnabled(true);
        resultsPanel.clear();
        fileLabel.setText(loader.getFileName());
        statusLabel.setText("Loaded " + loader.getRowCount() + " rows and "
                + loader.getColumnNames().size() + " columns. Select a numeric column.");
    }

    // ------------------------------------------------------------------
    // Sort & Evaluate button
    // ------------------------------------------------------------------
    private void onSort() {
        String column = (String) columnBox.getSelectedItem();
        if (column == null) {
            showError("Please select a column first.");
            return;
        }

        final double[] data;
        try {
            data = loader.getNumericColumn(column);  // Member 1's code
        } catch (IllegalArgumentException ex) {
            resultsPanel.clear();                    // remove the old results
            statusLabel.setText("Sorting not started: invalid column.");
            showError(ex.getMessage());              // e.g. non-numeric / empty column
            return;
        }

        setBusy(true);
        statusLabel.setText("Sorting " + data.length + " values with 5 algorithms... please wait.");

        // Run the sorting off the GUI thread
        new SwingWorker<List<SortResult>, Void>() {
            @Override
            protected List<SortResult> doInBackground() {
                return PerformanceEvaluator.evaluate(data);   // Member 3's code
            }

            @Override
            protected void done() {
                try {
                    resultsPanel.showResults(get());
                    statusLabel.setText("Finished. Sorted " + data.length
                            + " values from column '" + column + "'.");
                } catch (InterruptedException | ExecutionException ex) {
                    showError("Sorting failed: " + ex.getCause());
                    statusLabel.setText("Sorting failed.");
                } finally {
                    setBusy(false);
                }
            }
        }.execute();
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------
    /** Enables/disables the buttons while sorting is running. */
    private void setBusy(boolean busy) {
        uploadButton.setEnabled(!busy);
        sortButton.setEnabled(!busy);
        columnBox.setEnabled(!busy);
    }

    /** Clears the UI after a failed upload. */
    private void resetAfterError() {
        columnBox.removeAllItems();
        columnBox.setEnabled(false);
        sortButton.setEnabled(false);
        resultsPanel.clear();
        fileLabel.setText("No file selected");
        statusLabel.setText("Upload failed. Please select a valid CSV file.");
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}