import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;
import javax.swing.*;

/**
 * Panel for listing all accounts with options to view details.
 *
 * @author Group 72 (Isaac, Miguel, Ryan, Nathaniel, Dehou)
 * @version 1.0
 */
public class AccountListPanel extends JPanel {
    private Image backgroundGif;
    private Font customFont;
    private CardLayout cardLayout;
    private JPanel container;
    private Main mainApp;

    public AccountListPanel(CardLayout layout, JPanel container, Main mainApp) {
        this.cardLayout = layout;
        this.container = container;
        this.mainApp = mainApp;

        backgroundGif = new ImageIcon("images/menuBackground.gif").getImage();
        loadCustomFont();

        initializeComponents();
    }

    private void initializeComponents() {
        this.setLayout(new BorderLayout());
        this.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Title
        OutlinedLabel titleLabel = new OutlinedLabel("ACCOUNT LIST");
        titleLabel.setFont(customFont.deriveFont(Font.BOLD, 48f));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        this.add(titleLabel, BorderLayout.NORTH);

        // Center panel
        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));

        // Get all usernames
        List<String> usernames = ProfileStore.getAllUsernames();
        String[] accountList = usernames.toArray(new String[0]);

        JList<String> accountJList = new JList<>(accountList);
        accountJList.setFont(customFont.deriveFont(Font.PLAIN, 20f));
        accountJList.setBackground(new Color(240, 240, 240));
        accountJList.setForeground(Color.BLACK);
        accountJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        accountJList.setVisibleRowCount(Math.min(10, usernames.size()));

        JScrollPane scrollPane = new JScrollPane(accountJList);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.WHITE, 2),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        scrollPane.setMaximumSize(new Dimension(400, 300));
        scrollPane.setAlignmentX(Component.CENTER_ALIGNMENT);

        OutlinedLabel countLabel = new OutlinedLabel("Total Accounts: " + usernames.size());
        countLabel.setFont(customFont.deriveFont(Font.PLAIN, 24f));
        countLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        OutlinedButton viewDetailsBtn = new OutlinedButton("VIEW DETAILS");
        viewDetailsBtn.setFont(customFont.deriveFont(Font.PLAIN, 24f));
        viewDetailsBtn.setBackground(new Color(100, 150, 200));
        viewDetailsBtn.setForeground(Color.BLACK);
        viewDetailsBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        viewDetailsBtn.addActionListener(e -> {
            String selectedUsername = accountJList.getSelectedValue();
            if (selectedUsername != null) {
                showAccountDetailsPanel(selectedUsername);
            } else {
                JOptionPane.showMessageDialog(this, "Please select an account first.", "No Selection",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });

        accountJList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    String selectedUsername = accountJList.getSelectedValue();
                    if (selectedUsername != null) {
                        if (ProfileStore.loadProfile(selectedUsername) != null) {
                            mainApp.showStatsPage(selectedUsername);
                        } else {
                            JOptionPane.showMessageDialog(AccountListPanel.this,
                                    "No statistics are available for " + selectedUsername + ".",
                                    "No Stats", JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                }
            }
        });

        centerPanel.add(countLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        centerPanel.add(scrollPane);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        centerPanel.add(viewDetailsBtn);

        this.add(centerPanel, BorderLayout.CENTER);

        // Back button
        OutlinedButton backBtn = new OutlinedButton("BACK");
        backBtn.setFont(customFont.deriveFont(Font.PLAIN, 24f));
        backBtn.setBackground(new Color(150, 150, 150));
        backBtn.setForeground(Color.BLACK);
        backBtn.addActionListener(e -> cardLayout.show(container, "ACCOUNT_MANAGEMENT"));

        JPanel backPanel = new JPanel();
        backPanel.setOpaque(false);
        backPanel.add(backBtn);
        this.add(backPanel, BorderLayout.SOUTH);
    }

    private void showAccountDetailsPanel(String username) {
        AccountDetailsPanel detailsPanel = new AccountDetailsPanel(cardLayout, container, mainApp, username);
        container.add(detailsPanel, "ACCOUNT_DETAILS");
        cardLayout.show(container, "ACCOUNT_DETAILS");
    }

    private void loadCustomFont() {
        try {
            File fontFile = new File("fonts/ka1.ttf");
            customFont = Font.createFont(Font.TRUETYPE_FONT, fontFile);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(customFont);
        } catch (Exception e) {
            customFont = new Font("Arial", Font.BOLD, 24);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundGif != null) {
            g.drawImage(backgroundGif, 0, 0, getWidth(), getHeight(), this);
        }
    }
}