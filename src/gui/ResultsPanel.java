package gui;

import util.PerformanceEvaluator;
import util.SortResult;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.List;

/**
 * MEMBER 1 - Results table of the GUI.
 *
 * Shows one row per algorithm (name, time, correctness), highlights the
 * best algorithm in green and shows its name under the table.
 *
 * Used by MainFrame:
 *   resultsPanel.showResults(results);
 *   resultsPanel.clear();
 */
public class ResultsPanel extends JPanel {

    private static final Color BEST_COLOR = new Color(198, 239, 206);

    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"Algorithm", "Execution Time (ms)", "Sorted Correctly"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false; // read-only table
        }
    };
    private final JTable table = new JTable(model);
    private final JLabel bestLabel = new JLabel("Best algorithm: -", SwingConstants.CENTER);

    /** Name of the best algorithm of the current results (null if none). */
    private String bestAlgorithm = null;

    public ResultsPanel() {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createTitledBorder("Performance Results"));

        table.setRowHeight(26);
        table.getTableHeader().setReorderingAllowed(false);
        table.setDefaultRenderer(Object.class, new BestRowRenderer());

        bestLabel.setFont(bestLabel.getFont().deriveFont(Font.BOLD, 15f));
        bestLabel.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));

        add(new JScrollPane(table), BorderLayout.CENTER);
        add(bestLabel, BorderLayout.SOUTH);
    }

    /** Fills the table with the given results and highlights the best one. */
    public void showResults(List<SortResult> results) {
        model.setRowCount(0);
        SortResult best = PerformanceEvaluator.findBest(results);
        bestAlgorithm = (best == null) ? null : best.algorithm();

        for (SortResult r : results) {
            model.addRow(new Object[]{
                    r.algorithm(),
                    String.format("%.3f", r.timeMs()),
                    r.correct() ? "Yes" : "NO"
            });
        }

        if (best == null) {
            bestLabel.setText("Best algorithm: none (no algorithm sorted correctly)");
        } else {
            bestLabel.setText(String.format("Best algorithm: %s (%.3f ms)",
                    best.algorithm(), best.timeMs()));
        }
        table.repaint();
    }

    /** Empties the table. */
    public void clear() {
        model.setRowCount(0);
        bestAlgorithm = null;
        bestLabel.setText("Best algorithm: -");
    }

    /** Paints the row of the best algorithm green and bold. */
    private class BestRowRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(column == 0 ? SwingConstants.LEFT : SwingConstants.CENTER);

            boolean isBest = bestAlgorithm != null && bestAlgorithm.equals(model.getValueAt(row, 0));
            if (!isSelected) {
                c.setBackground(isBest ? BEST_COLOR : Color.WHITE);
            }
            c.setFont(c.getFont().deriveFont(isBest ? Font.BOLD : Font.PLAIN));
            return c;
        }
    }
}
