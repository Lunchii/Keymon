import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

public class TeamSelectPanel extends JPanel {
    private Main mainApp;
    private String username;

    private List<Pokemon> pool;
    private List<Integer> selected = new ArrayList<>();

    private JButton[] slotButtons;
    private JLabel statusLabel;
    private JButton confirmBtn;

    public TeamSelectPanel(JPanel mainContainer, Main mainApp, String username) {
        this.mainApp  = mainApp;
        this.username = username;

        pool = Pokemon.getPool();

        setLayout(new BorderLayout());
        setBackground(new Color(15, 50, 35));

        // Title
        JLabel title = new JLabel("CHOOSE YOUR TEAM OF 3", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 26));
        title.setForeground(Color.WHITE);
        title.setBorder(BorderFactory.createEmptyBorder(30, 0, 10, 0));
        add(title, BorderLayout.NORTH);

        // Grid of monster cards
        JPanel grid = new JPanel(new GridLayout(2, 3, 20, 20));
        grid.setBackground(new Color(15, 50, 35));
        grid.setBorder(BorderFactory.createEmptyBorder(20, 60, 20, 60));

        slotButtons = new JButton[pool.size()];
        for (int i = 0; i < pool.size(); i++) {
            final int idx = i;
            Pokemon p = pool.get(i);
            p.loadSprite();

            JButton btn = new JButton() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    boolean sel = selected.contains(idx);
                    g2.setColor(sel ? new Color(40, 130, 70) : new Color(20, 65, 40));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                    g2.setColor(sel ? new Color(80, 220, 130) : new Color(50, 110, 70));
                    g2.setStroke(new BasicStroke(sel ? 3f : 1.5f));
                    g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 14, 14);

                    // Sprite
                    if (p.getSprite() != null)
                        g2.drawImage(p.getSprite(), getWidth()/2 - 40, 10, 80, 80, null);

                    // Name
                    g2.setFont(new Font("SansSerif", Font.BOLD, 14));
                    g2.setColor(Color.WHITE);
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(p.name, (getWidth() - fm.stringWidth(p.name)) / 2, 108);

                    // Stats
                    g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
                    g2.setColor(new Color(180, 230, 200));
                    String stats = "HP " + p.maxHp + "   ATK " + p.attack;
                    FontMetrics fm2 = g2.getFontMetrics();
                    g2.drawString(stats, (getWidth() - fm2.stringWidth(stats)) / 2, 126);

                    // Checkmark
                    if (sel) {
                        g2.setFont(new Font("SansSerif", Font.BOLD, 20));
                        g2.setColor(new Color(80, 255, 140));
                        g2.drawString("✓", getWidth() - 26, 22);
                    }
                }
            };
            btn.setOpaque(false);
            btn.setContentAreaFilled(false);
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.setPreferredSize(new Dimension(160, 145));
            btn.addActionListener(e -> toggleSelect(idx));
            slotButtons[i] = btn;
            grid.add(btn);
        }
        add(grid, BorderLayout.CENTER);

        // Bottom bar
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 14));
        bottom.setBackground(new Color(15, 50, 35));

        statusLabel = new JLabel("Select 3 Keymon  (0 / 3)");
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        statusLabel.setForeground(new Color(180, 230, 200));
        bottom.add(statusLabel);

        confirmBtn = new JButton("START ADVENTURE");
        confirmBtn.setFont(new Font("SansSerif", Font.BOLD, 16));
        confirmBtn.setBackground(new Color(50, 160, 90));
        confirmBtn.setForeground(Color.WHITE);
        confirmBtn.setFocusPainted(false);
        confirmBtn.setBorderPainted(false);
        confirmBtn.setEnabled(false);
        confirmBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        confirmBtn.setBorder(BorderFactory.createEmptyBorder(10, 28, 10, 28));
        confirmBtn.addActionListener(e -> confirm());
        bottom.add(confirmBtn);

        add(bottom, BorderLayout.SOUTH);
    }

    private void toggleSelect(int idx) {
        if (selected.contains(idx)) {
            selected.remove(Integer.valueOf(idx));
        } else {
            if (selected.size() < 3) selected.add(idx);
        }
        confirmBtn.setEnabled(selected.size() == 3);
        statusLabel.setText("Select 3 Keymon  (" + selected.size() + " / 3)");
        for (JButton b : slotButtons) b.repaint();
    }

    private void confirm() {
        List<Pokemon> team = new ArrayList<>();
        for (int idx : selected) {
            Pokemon template = pool.get(idx);
            Pokemon p = new Pokemon(template.name, template.maxHp, template.attack, template.spritePath);
            p.loadSprite();
            team.add(p);
        }
        mainApp.startGame(username, team);
    }
}
