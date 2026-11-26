import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import javax.swing.*;

public class ParentalPanel extends JPanel {
    private Image backgroundGif;
    private ArrayList<JButton> buttons = new ArrayList<>();
    private int focusedIndex = 0;
    private Font customFont;
    private CardLayout cardLayout;
    private JPanel container;
    private Main mainApp;

    public ParentalPanel(CardLayout layout, JPanel container, Main mainApp) {
        this.cardLayout = layout;
        this.container = container;
        this.mainApp = mainApp;

        backgroundGif = new ImageIcon("menuBackground.gif").getImage();
        loadCustomFont();

        this.setLayout(new BorderLayout());
        this.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 150));

        // --- RIGHT SIDE: PARENTAL BUTTONS (Mirroring MenuPanel) ---
        JPanel menuContent = new JPanel();
        menuContent.setOpaque(false);
        menuContent.setLayout(new BoxLayout(menuContent, BoxLayout.Y_AXIS));

        OutlinedLabel title = createTitle("Parental");
        OutlinedLabel title2 = createTitle("Controls");

        menuContent.add(Box.createVerticalGlue());
        menuContent.add(title);
        menuContent.add(title2);
        menuContent.add(Box.createRigidArea(new Dimension(0, 50)));

        String[] labels = { "PLAYER STATS", "HIGH SCORES", "RESET TABLE", "BACK" };
        for (int i = 0; i < labels.length; i++) {
            final int index = i;
            final String label = labels[i];

            OutlinedButton btn = new OutlinedButton(label);
            btn.setFont(customFont.deriveFont(Font.PLAIN, 28f));
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            btn.setMaximumSize(new Dimension(400, 60));
            btn.setFocusable(false);

            // Set font to black as default
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

        // --- LEFT SIDE: ADMIN FOOTER (Mirroring Team Info) ---
        JPanel footerInfo = new JPanel();
        footerInfo.setOpaque(false);
        footerInfo.setLayout(new BoxLayout(footerInfo, BoxLayout.Y_AXIS));
        footerInfo.setBorder(BorderFactory.createEmptyBorder(0, 50, 50, 0));

        OutlinedLabel adminLabel = new OutlinedLabel("ADMINISTRATOR MODE");
        adminLabel.setFont(customFont.deriveFont(Font.BOLD, 24f));

        OutlinedLabel warningLabel = new OutlinedLabel("SYSTEM RESET AVAILABLE");
        warningLabel.setFont(customFont.deriveFont(Font.PLAIN, 18f));

        footerInfo.add(Box.createVerticalGlue());
        footerInfo.add(adminLabel);
        footerInfo.add(warningLabel);
        this.add(footerInfo, BorderLayout.WEST);

        updateButtonStyles();
        setupKeyboardNavigation();
    }

    private void handleSelection(String label) {
        if (label.equals("BACK")) {
            cardLayout.show(container, "MENU");
        } else if (label.equals("PLAYER STATS")) {
            cardLayout.show(container, "STATS_PAGE");
        } else if (label.equals("HIGH SCORES")) {
            cardLayout.show(container, "HIGHSCORE");
        } else if (label.equals("RESET TABLE")) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure? This will delete ALL high scores and save files.",
                    "CRITICAL WARNING", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                // Uses SaveManager to wipe all accounts
                java.util.List<SaveManager.PlayerRecord> records = SaveManager.getAllHighScores();
                for (SaveManager.PlayerRecord r : records) {
                    SaveManager.deleteAccount(r.name);
                }
                JOptionPane.showMessageDialog(this, "High Score Table Reset.");
            }
        }
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
        // Refresh the panel
        this.revalidate();
        this.repaint();
    }

    private OutlinedLabel createTitle(String text) {
        OutlinedLabel lbl = new OutlinedLabel(text);
        lbl.setFont(customFont.deriveFont(Font.BOLD, 100f));
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        lbl.setPreferredSize(new Dimension(800, 150));
        lbl.setMaximumSize(new Dimension(800, 150));
        return lbl;
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
                focusedIndex = (focusedIndex < buttons.size() - 1) ? focusedIndex + 1 : 0;
                updateButtonStyles();
            }
        });
        im.put(KeyStroke.getKeyStroke("ENTER"), "select");
        am.put("select", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                handleSelection(buttons.get(focusedIndex).getText());
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundGif != null)
            g.drawImage(backgroundGif, 0, 0, getWidth(), getHeight(), this);
    }

    private void loadCustomFont() {
        try {
            customFont = Font.createFont(Font.TRUETYPE_FONT, new File("ka1.ttf"));
        } catch (Exception e) {
            customFont = new Font("Arial", Font.BOLD, 24);
        }
    }
}