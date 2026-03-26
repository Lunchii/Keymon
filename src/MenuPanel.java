import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import javax.swing.*;

public class MenuPanel extends JPanel {
    private Image backgroundGif;
    private ArrayList<JButton> buttons = new ArrayList<>();
    private int focusedIndex = 0; // Starts at the first button

    public MenuPanel(CardLayout layout, JPanel container) {
        backgroundGif = new ImageIcon("menuBackground.gif").getImage();
        this.setLayout(new BorderLayout());
        this.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 150));

        JPanel menuContent = new JPanel();
        menuContent.setOpaque(false);
        menuContent.setLayout(new BoxLayout(menuContent, BoxLayout.Y_AXIS));

        loadCustomFont();

        // Title
        OutlinedLabel title = new OutlinedLabel("Keymon");
        title.setFont(customFont.deriveFont(Font.BOLD, 100f));
        title.setForeground(Color.BLACK);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        // Add extra width (e.g., 800) and height (e.g., 150) to prevent cropping
        title.setPreferredSize(new Dimension(800, 150));
        title.setMaximumSize(new Dimension(800, 150)); // BoxLayout respects MaximumSize

        OutlinedLabel title2 = new OutlinedLabel("Journey");
        title2.setFont(customFont.deriveFont(Font.BOLD, 100f));
        title2.setForeground(Color.BLACK);
        title2.setAlignmentX(Component.CENTER_ALIGNMENT);
        title2.setPreferredSize(new Dimension(800, 150));
        title2.setMaximumSize(new Dimension(800, 150));

        menuContent.add(Box.createVerticalGlue());
        menuContent.add(title);
        menuContent.add(title2);
        menuContent.add(Box.createRigidArea(new Dimension(0, 50)));

        // Buttons
        String[] labels = { "PLAY GAME", "LOGIN", "HIGHSCORE", "TUTORIAL", "PARENTAL", "EXIT" };
        for (String label : labels) {
            OutlinedButton btn = new OutlinedButton(label);
            btn.setFont(customFont.deriveFont(Font.PLAIN, 28f)); // Set size to 28

            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            btn.setMaximumSize(new Dimension(400, 60));
            btn.setFocusable(false);
            btn.setForeground(Color.BLACK); // Main text color

            buttons.add(btn);
            menuContent.add(btn);
            menuContent.add(Box.createRigidArea(new Dimension(0, 20)));

            btn.addActionListener(e -> layout.show(container, label));
        }
        menuContent.add(Box.createVerticalGlue());
        this.add(menuContent, BorderLayout.EAST);

        // INITIAL HIGHLIGHT
        updateButtonStyles();

        // KEY BINDINGS
        setupKeyboardNavigation();
    }

    private void setupKeyboardNavigation() {
        InputMap im = this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = this.getActionMap();

        // UP (W)
        im.put(KeyStroke.getKeyStroke("W"), "moveUp");
        am.put("moveUp", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                focusedIndex = (focusedIndex > 0) ? focusedIndex - 1 : buttons.size() - 1;
                updateButtonStyles();
            }
        });

        // DOWN (S)
        im.put(KeyStroke.getKeyStroke("S"), "moveDown");
        am.put("moveDown", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                focusedIndex = (focusedIndex < buttons.size() - 1) ? focusedIndex + 1 : 0;
                updateButtonStyles();
            }
        });

        // SELECT (ENTER)
        im.put(KeyStroke.getKeyStroke("ENTER"), "select");
        am.put("select", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                buttons.get(focusedIndex).doClick(); // Simulates a mouse click
            }
        });
    }

    private void updateButtonStyles() {
        for (int i = 0; i < buttons.size(); i++) {
            if (i == focusedIndex) {
                // Highlighted Style
                buttons.get(i).setForeground(Color.BLACK);
                buttons.get(i).setBorder(BorderFactory.createLineBorder(Color.WHITE, 4));
            } else {
                // Default Style
                buttons.get(i).setBackground(new Color(255, 255, 255, 200));
                buttons.get(i).setForeground(Color.GRAY);
                buttons.get(i).setBorder(BorderFactory.createEmptyBorder());
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundGif != null) {
            g.drawImage(backgroundGif, 0, 0, getWidth(), getHeight(), this);
        }
    }

    private Font customFont;

    private void loadCustomFont() {
        try {
            // 1. Load the file (Make sure the path is correct!)
            File fontFile = new File("ka1.ttf");
            customFont = Font.createFont(Font.TRUETYPE_FONT, fontFile);

            // 2. Register it so the OS can "see" it
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(customFont);

        } catch (Exception e) {
            e.printStackTrace();
            // Fallback to a default font if the file is missing
            customFont = new Font("Arial", Font.BOLD, 24);
        }
    }
}
