import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import javax.swing.*;

public class TutorialPanel extends JPanel {
    private Image backgroundGif;
    private ArrayList<JButton> buttons = new ArrayList<>();
    private int focusedIndex = 0;
    private Font customFont;
    private CardLayout cardLayout;
    private JPanel container;
    private Main mainApp;

    public TutorialPanel(CardLayout layout, JPanel container, Main mainApp) {
        this.cardLayout = layout;
        this.container = container;
        this.mainApp = mainApp;

        backgroundGif = new ImageIcon("menuBackground.gif").getImage();
        loadCustomFont();

        this.setLayout(new BorderLayout());
        this.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 150));

        JPanel menuContent = new JPanel();
        menuContent.setOpaque(false);
        menuContent.setLayout(new BoxLayout(menuContent, BoxLayout.Y_AXIS));

        OutlinedLabel title = createTitle("Tutorial");
        OutlinedLabel subtitle = createSubtitle("Learn the basics and meet your first trainer");

        menuContent.add(Box.createVerticalGlue());
        menuContent.add(title);
        menuContent.add(subtitle);
        menuContent.add(Box.createRigidArea(new Dimension(0, 40)));

        String[] labels = { "CONTROLS", "START TUTORIAL", "BACK" };
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

        updateButtonStyles();
        setupKeyboardNavigation();
    }

    private void handleSelection(String label) {
        switch (label) {
            case "CONTROLS":
                JOptionPane.showMessageDialog(this,
                        "Controls:\nWASD - Move\nESC - Pause/Menu\nENTER - Select/Interact",
                        "Tutorial Controls",
                        JOptionPane.INFORMATION_MESSAGE);
                break;
            case "START TUTORIAL":
                mainApp.startTutorial();
                break;
            case "BACK":
                cardLayout.show(container, "MENU");
                break;
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

    private OutlinedLabel createSubtitle(String text) {
        OutlinedLabel lbl = new OutlinedLabel(text);
        lbl.setFont(customFont.deriveFont(Font.PLAIN, 18f));
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        lbl.setPreferredSize(new Dimension(800, 50));
        lbl.setMaximumSize(new Dimension(800, 50));
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
            File fontFile = new File("ka1.ttf");
            customFont = Font.createFont(Font.TRUETYPE_FONT, fontFile);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(customFont);
        } catch (Exception e) {
            customFont = new Font("Arial", Font.BOLD, 24);
        }
    }
}
