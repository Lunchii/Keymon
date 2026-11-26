import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import javax.swing.*;

/**
 * Main menu screen with an animated background and keyboard/mouse navigable
 * buttons.
 *
 * <p>
 * Buttons: LOGIN, HIGH SCORES, TUTORIAL, PARENTAL CONTROLS, EXIT.
 * Keyboard navigation uses W/S to change selection and ENTER to activate.
 * The custom KA1 font is loaded at startup; Arial is used as the fallback.
 * </p>
 * 
 * @author Group 72 (Isaac, Miguel, Ryan, Nathaniel, Dehou)
 * @version 1.0
 */
public class MenuPanel extends JPanel {
    private Image backgroundGif;
    private ArrayList<JButton> buttons = new ArrayList<>();
    private int focusedIndex = 0;
    private Font customFont;
    private CardLayout cardLayout;
    private JPanel container;

    public MenuPanel(CardLayout layout, JPanel container) {
        this.cardLayout = layout;
        this.container = container;

        backgroundGif = new ImageIcon("images/menuBackground.gif").getImage();
        loadCustomFont();
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                MusicPlayer.play("menu");
            }
        });

        this.setLayout(new BorderLayout());
        this.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 150));

        // --- RIGHT SIDE: MENU BUTTONS ---
        JPanel menuContent = new JPanel();
        menuContent.setOpaque(false);
        menuContent.setLayout(new BoxLayout(menuContent, BoxLayout.Y_AXIS));

        OutlinedLabel title = createTitle("Keymon");
        OutlinedLabel title2 = createTitle("Journey");

        menuContent.add(Box.createVerticalGlue());
        menuContent.add(title);
        menuContent.add(title2);
        menuContent.add(Box.createRigidArea(new Dimension(0, 50)));

        String[] labels = { "LOGIN", "HIGHSCORE", "TUTORIAL", "PARENTAL", "EXIT" };
        for (int i = 0; i < labels.length; i++) {
            final int index = i;
            final String label = labels[i];

            OutlinedButton btn = new OutlinedButton(label);
            btn.setFont(customFont.deriveFont(Font.PLAIN, 28f));
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            btn.setMaximumSize(new Dimension(400, 60));
            btn.setFocusable(false);

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

        JPanel footerInfo = new JPanel();
        footerInfo.setOpaque(false);
        footerInfo.setLayout(new BoxLayout(footerInfo, BoxLayout.Y_AXIS));
        footerInfo.setBorder(BorderFactory.createEmptyBorder(0, 50, 50, 0));

        float infoSize = 18f;

        OutlinedLabel teamLabel = new OutlinedLabel("TEAM 72");
        teamLabel.setFont(customFont.deriveFont(Font.BOLD, 24f));

        OutlinedLabel devLabel = new OutlinedLabel("Isaac, Miguel, Ryan, Nathaniel, Dehou");
        devLabel.setFont(customFont.deriveFont(Font.PLAIN, infoSize));

        OutlinedLabel courseLabel = new OutlinedLabel("CS2212 \u2022 Winter 2026 \u2022 Western University");
        courseLabel.setFont(customFont.deriveFont(Font.PLAIN, infoSize));

        footerInfo.add(Box.createVerticalGlue());
        footerInfo.add(teamLabel);
        footerInfo.add(devLabel);
        footerInfo.add(courseLabel);

        this.add(footerInfo, BorderLayout.WEST);

        updateButtonStyles();
        setupKeyboardNavigation();
    }

    private void handleSelection(String label) {
        if (label.equals("EXIT")) {
            System.exit(0);
        } else if (label.equals("PARENTAL")) {
            // --- SECURE PASSCODE CHECK ---
            JPasswordField pf = new JPasswordField();
            int okCxl = JOptionPane.showConfirmDialog(this, pf, "Enter Parental Passcode", JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE);

            if (okCxl == JOptionPane.OK_OPTION) {
                String password = new String(pf.getPassword());
                // You can change "1234" to any code your team chooses
                if (password.equals("1234")) {
                    cardLayout.show(container, "PARENTAL");
                } else {
                    JOptionPane.showMessageDialog(this, "Incorrect Passcode!", "Access Denied",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        } else if (label.equals("HIGHSCORE")) {
            for (Component comp : container.getComponents()) {
                if (comp instanceof HighscorePanel) {
                    HighscorePanel hp = (HighscorePanel) comp;
                    hp.setPreviousCard("MENU"); // Tell it to return here
                    hp.refreshData();
                    break;
                }
            }
            cardLayout.show(container, "HIGHSCORE");
        } else if (label.equals("PARENTAL CONTROLS")) {
            // --- SECURE PASSCODE CHECK ---
            JPasswordField pf = new JPasswordField();
            int passcode = JOptionPane.showConfirmDialog(this, pf, "Enter Parental PIN",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (passcode == JOptionPane.OK_OPTION) {
                String password = new String(pf.getPassword());
                // Using the hardcoded PIN from ProfileStore logic
                if (password.equals("1234")) {
                    cardLayout.show(container, "PARENTAL");
                } else {
                    JOptionPane.showMessageDialog(this, "Incorrect PIN!", "Access Denied",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        } else if (label.equals("LOGIN")) {
            cardLayout.show(container, "LOGIN");
        } else {
            cardLayout.show(container, label);
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

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundGif != null) {
            g.drawImage(backgroundGif, 0, 0, getWidth(), getHeight(), this);
        }
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
}
