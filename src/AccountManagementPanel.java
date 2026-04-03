import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import javax.swing.*;

/**
 * Account Management Panel for parental controls.
 * Provides comprehensive account management functionality.
 *
 * @author Group 72 (Isaac, Miguel, Ryan, Nathaniel, Dehou)
 * @version 1.0
 */
public class AccountManagementPanel extends JPanel {
    private Image backgroundGif;
    private ArrayList<JButton> buttons = new ArrayList<>();
    private int focusedIndex = 0;
    private Font customFont;
    private CardLayout cardLayout;
    private JPanel container;
    private Main mainApp;

    public AccountManagementPanel(CardLayout layout, JPanel container, Main mainApp) {
        this.cardLayout = layout;
        this.container = container;
        this.mainApp = mainApp;

        backgroundGif = new ImageIcon("images/menuBackground.gif").getImage();
        loadCustomFont();

        this.setLayout(new BorderLayout());
        this.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 150));

        // --- RIGHT SIDE: ACCOUNT MANAGEMENT BUTTONS ---
        JPanel menuContent = new JPanel();
        menuContent.setOpaque(false);
        menuContent.setLayout(new BoxLayout(menuContent, BoxLayout.Y_AXIS));

        OutlinedLabel title = createTitle("Account");
        OutlinedLabel title2 = createTitle("Manager");

        menuContent.add(Box.createVerticalGlue());
        menuContent.add(title);
        menuContent.add(title2);
        menuContent.add(Box.createRigidArea(new Dimension(0, 50)));

        String[] labels = { "LIST ACCOUNTS", "CREATE ACCOUNT", "RESET PASSWORD", "RESET STATS", "DELETE ACCOUNT", "BACK" };
        for (int i = 0; i < labels.length; i++) {
            final int index = i;
            final String label = labels[i];

            OutlinedButton btn = new OutlinedButton(label);
            btn.setFont(customFont.deriveFont(Font.PLAIN, 28f));
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            btn.setMaximumSize(new Dimension(400, 60));
            btn.setFocusable(false);
            btn.setForeground(Color.BLACK);

            btn.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    focusedIndex = index;
                    updateButtonStyles();
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    handleSelection(label);
                }
            });

            buttons.add(btn);
            menuContent.add(btn);
            menuContent.add(Box.createRigidArea(new Dimension(0, 20)));
        }

        menuContent.add(Box.createVerticalGlue());
        this.add(menuContent, BorderLayout.EAST);

        // --- LEFT SIDE: ACCOUNT INFO ---
        JPanel footerInfo = new JPanel();
        footerInfo.setOpaque(false);
        footerInfo.setLayout(new BoxLayout(footerInfo, BoxLayout.Y_AXIS));
        footerInfo.setBorder(BorderFactory.createEmptyBorder(0, 50, 50, 0));

        OutlinedLabel adminLabel = new OutlinedLabel("ACCOUNT ADMINISTRATION");
        adminLabel.setFont(customFont.deriveFont(Font.BOLD, 24f));
        OutlinedLabel warningLabel = new OutlinedLabel("USE CAUTION WITH DESTRUCTIVE ACTIONS");
        warningLabel.setFont(customFont.deriveFont(Font.PLAIN, 18f));

        footerInfo.add(Box.createVerticalGlue());
        footerInfo.add(adminLabel);
        footerInfo.add(Box.createRigidArea(new Dimension(0, 20)));
        footerInfo.add(warningLabel);
        footerInfo.add(Box.createVerticalGlue());

        this.add(footerInfo, BorderLayout.WEST);

        setupKeyboardNavigation();
    }

    private void handleSelection(String label) {
        if (label.equals("BACK")) {
            cardLayout.show(container, "PARENTAL");
        } else if (label.equals("LIST ACCOUNTS")) {
            showAccountListPanel();
        } else if (label.equals("CREATE ACCOUNT")) {
            showCreateAccountPanel();
        } else if (label.equals("RESET PASSWORD")) {
            showResetPasswordPanel();
        } else if (label.equals("RESET STATS")) {
            showResetStatsPanel();
        } else if (label.equals("DELETE ACCOUNT")) {
            showDeleteAccountPanel();
        }
    }

    private void showAccountListPanel() {
        AccountListPanel listPanel = new AccountListPanel(cardLayout, container, mainApp);
        container.add(listPanel, "ACCOUNT_LIST");
        cardLayout.show(container, "ACCOUNT_LIST");
    }

    private void showCreateAccountPanel() {
        CreateAccountPanel createPanel = new CreateAccountPanel(cardLayout, container, mainApp);
        container.add(createPanel, "CREATE_ACCOUNT");
        cardLayout.show(container, "CREATE_ACCOUNT");
    }

    private void showResetPasswordPanel() {
        ResetPasswordPanel resetPanel = new ResetPasswordPanel(cardLayout, container, mainApp);
        container.add(resetPanel, "RESET_PASSWORD");
        cardLayout.show(container, "RESET_PASSWORD");
    }

    private void showResetStatsPanel() {
        ResetStatsPanel resetPanel = new ResetStatsPanel(cardLayout, container, mainApp);
        container.add(resetPanel, "RESET_STATS");
        cardLayout.show(container, "RESET_STATS");
    }

    private void showDeleteAccountPanel() {
        DeleteAccountPanel deletePanel = new DeleteAccountPanel(cardLayout, container, mainApp);
        container.add(deletePanel, "DELETE_ACCOUNT");
        cardLayout.show(container, "DELETE_ACCOUNT");
    }

    private void updateButtonStyles() {
        for (int i = 0; i < buttons.size(); i++) {
            JButton btn = buttons.get(i);
            btn.setBackground(new Color(0, 0, 0, 0));

            if (i == focusedIndex) {
                btn.setForeground(Color.BLACK);
                btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            } else {
                btn.setForeground(Color.LIGHT_GRAY);
                btn.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        }
        repaint();
    }

    private OutlinedLabel createTitle(String text) {
        OutlinedLabel lbl = new OutlinedLabel(text);
        lbl.setFont(customFont.deriveFont(Font.BOLD, 100f));
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        lbl.setPreferredSize(new Dimension(800, 150));
        lbl.setMaximumSize(new Dimension(800, 150));
        return lbl;
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

    private void setupKeyboardNavigation() {
        InputMap im = this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = this.getActionMap();
        im.put(KeyStroke.getKeyStroke("W"), "moveUp");
        am.put("moveUp", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                focusedIndex = (focusedIndex > 0) ? focusedIndex - 1 : buttons.size() - 1;
                updateButtonStyles();
            }
        });
        im.put(KeyStroke.getKeyStroke("S"), "moveDown");
        am.put("moveDown", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                focusedIndex = (focusedIndex + 1) % buttons.size();
                updateButtonStyles();
            }
        });
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "select");
        am.put("select", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (focusedIndex >= 0 && focusedIndex < buttons.size()) {
                    handleSelection(buttons.get(focusedIndex).getText());
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundGif != null) {
            g.drawImage(backgroundGif, 0, 0, getWidth(), getHeight(), this);
        }
    }

    private OutlinedLabel createTitle(String text) {
        OutlinedLabel lbl = new OutlinedLabel(text);
        lbl.setFont(customFont.deriveFont(Font.BOLD, 100f));
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        lbl.setPreferredSize(new Dimension(800, 150));
        lbl.setMaximumSize(new Dimension(800, 150));
        return lbl;
    }
}