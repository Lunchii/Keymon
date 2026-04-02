import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;

public class HighscorePanel extends JPanel {
    private CardLayout cardLayout;
    private JPanel container;
    private DefaultTableModel tableModel;

    public HighscorePanel(CardLayout layout, JPanel container) {
        this.cardLayout = layout;
        this.container = container;

        this.setBackground(new Color(30, 30, 30)); // Dark retro background
        this.setLayout(new BorderLayout());

        // 1. Header Section
        JLabel titleLabel = new JLabel("TOP 10 TRAINERS", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Monospaced", Font.BOLD, 48));
        titleLabel.setForeground(Color.YELLOW);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(30, 0, 30, 0));
        add(titleLabel, BorderLayout.NORTH);

        // 2. Table Section
        String[] columnNames = {"RANK", "TRAINER NAME", "SCORE"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Table is read-only
            }
        };

        JTable table = new JTable(tableModel);
        setupTableStyle(table);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 100, 20, 100));
        scrollPane.getViewport().setBackground(new Color(30, 30, 30));
        scrollPane.getVerticalScrollBar().setBackground(new Color(30, 30, 30));
        add(scrollPane, BorderLayout.CENTER);

        // 3. Footer Section (Back Button)
        JPanel footer = new JPanel();
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createEmptyBorder(20, 0, 40, 0));

        JButton backBtn = new JButton("BACK TO MENU");
        backBtn.setFont(new Font("Monospaced", Font.BOLD, 24));
        backBtn.setBackground(Color.BLACK);
        backBtn.setForeground(Color.WHITE);
        backBtn.setFocusPainted(false);
        backBtn.addActionListener(e -> cardLayout.show(container, "MENU"));

        footer.add(backBtn);
        add(footer, BorderLayout.SOUTH);
    }

    /**
     * Fetches the latest data from SaveManager and refreshes the table.
     * Should be called every time this panel is shown.
     */
    public void refreshData() {
        tableModel.setRowCount(0); // Clear current rows
        List<ProfileStore.PlayerRecord> records = ProfileStore.getAllHighScores();

        int count = 0;
        for (ProfileStore.PlayerRecord record : records) {
            if (count >= 10) break; // Only show top 10
            tableModel.addRow(new Object[]{ (count + 1), record.name.toUpperCase(), record.score });
            count++;
        }
    }

    private void setupTableStyle(JTable table) {
        table.setFont(new Font("Monospaced", Font.PLAIN, 22));
        table.setRowHeight(40);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setBackground(new Color(45, 52, 71)); // Using group's dark blue-grey
        table.setForeground(Color.WHITE);
        table.setSelectionBackground(new Color(70, 130, 180));

        // Header Style
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Monospaced", Font.BOLD, 24));
        header.setBackground(Color.BLACK);
        header.setForeground(Color.WHITE);
        header.setReorderingAllowed(false);

        // Center Aligning Columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        centerRenderer.setBackground(new Color(45, 52, 71));
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
    }
}
