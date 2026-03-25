import java.awt.*;
import java.awt.event.ActionEvent;
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

        // Title
        JLabel title = new JLabel("Keymon");
        title.setFont(new Font("Arial", Font.BOLD, 150));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        menuContent.add(Box.createVerticalGlue());
        menuContent.add(title);
        menuContent.add(Box.createRigidArea(new Dimension(0, 50)));

        // Buttons
        String[] labels = { "PLAY GAME", "LOGIN", "HIGHSCORE", "TUTORIAL", "PARENTAL", "EXIT" };
        for (String label : labels) {
            OutlinedButton btn = new OutlinedButton(label);

            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            btn.setMaximumSize(new Dimension(400, 60));
            btn.setFont(new Font("Arial", Font.BOLD, 24)); // Bold makes the outline look better
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
}