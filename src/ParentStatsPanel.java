import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Administrative screen using ProfileStore to manage player statistics.
 * Includes a search bar and a targeted reset function.
 */
public class ParentStatsPanel extends JPanel {
    private CardLayout cardLayout;
    private JPanel container;
    private Main mainApp;

    private JTextField searchField;
    private DefaultListModel<String> listModel;
    private JList<String> playerList;
    private List<String> allPlayers;

    public ParentStatsPanel(CardLayout layout, JPanel container, Main mainApp) {
        this.cardLayout = layout;
        this.container = container;
        this.mainApp = mainApp;

        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(30, 35, 45));
        setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));

        // --- Header Section ---
        JPanel headerPanel = new JPanel(new BorderLayout(10, 10));
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("TRAINER ADMINISTRATION", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Monospaced", Font.BOLD, 32));
        titleLabel.setForeground(Color.CYAN);
        headerPanel.add(titleLabel, BorderLayout.NORTH);

        searchField = new JTextField();
        searchField.setFont(new Font("SansSerif", Font.PLAIN, 18));
        searchField.setBorder(BorderFactory.createTitledBorder("Search Trainer Name"));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                filterList();
            }

            public void removeUpdate(DocumentEvent e) {
                filterList();
            }

            public void changedUpdate(DocumentEvent e) {
                filterList();
            }
        });
        headerPanel.add(searchField, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

        // --- Center: Player List ---
        listModel = new DefaultListModel<>();
        playerList = new JList<>(listModel);
        playerList.setFont(new Font("Monospaced", Font.BOLD, 20));
        playerList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(playerList), BorderLayout.CENTER);

        // --- Bottom: Control Buttons ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setOpaque(false);

        JButton resetBtn = new JButton("RESET SELECTED PLAYER STATS");
        resetBtn.setFont(new Font("Monospaced", Font.BOLD, 16));
        resetBtn.setBackground(new Color(180, 70, 60));
        resetBtn.setForeground(Color.BLACK);
        resetBtn.addActionListener(e -> resetSelectedPlayer());
        buttonPanel.add(resetBtn);

        JButton backBtn = new JButton("BACK TO ADMIN MENU");
        backBtn.setFont(new Font("Monospaced", Font.BOLD, 16));
        backBtn.addActionListener(e -> cardLayout.show(container, "PARENTAL"));
        buttonPanel.add(backBtn);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    /** Loads all users using ProfileStore. */
    public void refreshData() {
        allPlayers = ProfileStore.getAllUsernames(); // Fetches all saved profile names
        filterList();
    }

    private void filterList() {
        String filter = searchField.getText().toLowerCase();
        listModel.clear();
        if (allPlayers != null) {
            for (String name : allPlayers) {
                if (name.toLowerCase().contains(filter)) {
                    listModel.addElement(name);
                }
            }
        }
    }

    private void resetSelectedPlayer() {
        String selected = playerList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a trainer first.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to wipe all progress and stats for " + selected + "?",
                "Confirm Reset", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            // CALLING YOUR EXISTING METHOD
            boolean success = ProfileStore.resetStatistics(selected);

            if (success) {
                JOptionPane.showMessageDialog(this, "Stats for " + selected + " have been reset.");
                refreshData(); // Updates the list view
            } else {
                JOptionPane.showMessageDialog(this, "Error: Could not reset stats.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}