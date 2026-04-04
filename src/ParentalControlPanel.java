import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import javax.swing.*;

/**
 * Admin settings panel for parental controls and maintenance operations.
 *
 * <p>
 * Provides navigation for the admin dashboard and exposes controls for:
 * <ul>
 * <li>Viewing high score table</li>
 * <li>Performing a full reset of user and highscore data</li>
 * <li>Returning to the main menu</li>
 * </ul>
 * </p>
 *
 * @author Group 72 (Isaac, Miguel, Ryan, Nathaniel, Dehou)
 * @version 1.0
 */
public class ParentalControlPanel extends JPanel {
    private Image backgroundGif;
    private ArrayList<JButton> buttons = new ArrayList<>();
    private int focusedIndex = 0;
    private Font customFont;
    private CardLayout cardLayout;
    private JPanel container;
    private Main mainApp;

    public ParentalControlPanel(CardLayout layout, JPanel container, Main mainApp) {
        this.cardLayout = layout;
        this.container = container;
        this.mainApp = mainApp;

        backgroundGif = new ImageIcon("images/menuBackground.gif").getImage();
        loadCustomFont();

        this.setLayout(new BorderLayout());
        this.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 150));

        // --- RIGHT SIDE: ADMIN BUTTONS (Mirroring MenuPanel) ---
        JPanel menuContent = new JPanel();
        menuContent.setOpaque(false);
        menuContent.setLayout(new BoxLayout(menuContent, BoxLayout.Y_AXIS));

        OutlinedLabel title = createTitle("Parental");
        OutlinedLabel title2 = createTitle("Controls");

        menuContent.add(Box.createVerticalGlue());
        menuContent.add(title);
        menuContent.add(title2);
        menuContent.add(Box.createRigidArea(new Dimension(0, 50)));

        String[] labels = { "HIGH SCORES", "ACCOUNT MANAGEMENT", "RESET TABLE", "BACK" };
        for (int i = 0; i < labels.length; i++) {
            final int index = i;
            final String label = labels[i];

            OutlinedButton btn = new OutlinedButton(label);
            btn.setFont(customFont.deriveFont(Font.PLAIN, 28f));
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            btn.setMaximumSize(new Dimension(450, 60));
            btn.setFocusable(false);
            btn.setForeground(Color.BLACK); // Black font by default

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

        // --- LEFT SIDE: ADMIN INFO ---
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
        } else if (label.equals("HIGH SCORES")) {
            for (Component comp : container.getComponents()) {
                if (comp instanceof HighscorePanel) {
                    HighscorePanel hp = (HighscorePanel) comp;
                    hp.setPreviousCard("PARENTAL"); // Tell it to return to Admin Dashboard
                    hp.refreshData();
                    break;
                }
            }
            cardLayout.show(container, "HIGHSCORE");
        } else if (label.equals("ACCOUNT MANAGEMENT")) {
            cardLayout.show(container, "ACCOUNT_MANAGEMENT");
        } else if (label.equals("RESET TABLE")) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Reset the high score table to defaults?",
                    "Confirm Reset", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                // Reset Highscore table to defaults only
                ProfileStore.resetHighScoreTable();
                JOptionPane.showMessageDialog(this, "High score table has been reset to defaults.");
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
            customFont = Font.createFont(Font.TRUETYPE_FONT, new File("fonts/ka1.ttf"));
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(customFont);
        } catch (Exception e) {
            customFont = new Font("Arial", Font.BOLD, 24);
        }
    }
}