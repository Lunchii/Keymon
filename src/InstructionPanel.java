import java.awt.*;
import javax.swing.*;

/**
 * Multi-page instructions screen with keyboard and mouse navigation.
 */
public class InstructionPanel extends JPanel {
    private static final String[] PAGE_TITLES = {
        "WELCOME TO KEYMON",
        "HOW BATTLES WORK",
        "POWERUPS, STAGES, AND SAVING"
    };

    private static final String[] PAGE_TEXT = {
        "Keymon is a typing adventure for young trainers. Walk around each map with WASD, avoid obstacles, and reach the end tile to finish the stage. Tall grass and danger tiles can trigger typing battles, so stay ready.\n\nUse the mouse or keyboard to move through menus. Press ESC during a stage to pause, save, and return to your trainer hub.",
        "When a battle starts, press ATTACK and type the words that appear before the timer runs out. Correct letters are highlighted while you type. Faster and cleaner typing earns more points, builds combos, and hurts the enemy more.\n\nMistakes reduce your focus health, so accuracy matters. If your party faints or your focus health reaches zero, the stage ends and you can retry.",
        "Each stage becomes harder with longer words and less time. Winning battles and building streaks can earn useful powerups like Potions, Attack Boost, and Slow Time. Your score, highest unlocked stage, and typing statistics are saved locally.\n\nTeachers and parents can use the parental controls screen to create accounts, reset passwords, reset statistics, and manage the high score table."
    };

    private final CardLayout cardLayout;
    private final JPanel container;
    private final Main mainApp;
    private int currentPage = 0;
    private final JLabel titleLabel;
    private final JTextArea bodyArea;
    private final JLabel pageLabel;
    private final JButton previousButton;
    private final JButton nextButton;

    public InstructionPanel(CardLayout cardLayout, JPanel container, Main mainApp) {
        this.cardLayout = cardLayout;
        this.container = container;
        this.mainApp = mainApp;

        setLayout(new BorderLayout(18, 18));
        setBackground(new Color(20, 45, 30));
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        titleLabel = new JLabel("", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Monospaced", Font.BOLD, 30));
        titleLabel.setForeground(Color.WHITE);
        add(titleLabel, BorderLayout.NORTH);

        bodyArea = new JTextArea();
        bodyArea.setEditable(false);
        bodyArea.setLineWrap(true);
        bodyArea.setWrapStyleWord(true);
        bodyArea.setFont(new Font("SansSerif", Font.PLAIN, 20));
        bodyArea.setBackground(new Color(245, 245, 245));
        bodyArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.BLACK, 4),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)
        ));
        add(new JScrollPane(bodyArea), BorderLayout.CENTER);

        pageLabel = new JLabel("", SwingConstants.CENTER);
        pageLabel.setFont(new Font("Monospaced", Font.BOLD, 16));
        pageLabel.setForeground(new Color(220, 235, 220));

        previousButton = createButton("PREVIOUS");
        previousButton.addActionListener(e -> changePage(-1));

        nextButton = createButton("NEXT");
        nextButton.addActionListener(e -> changePage(1));

        JButton tutorialButton = createButton("START GUIDED TUTORIAL");
        tutorialButton.addActionListener(e -> mainApp.startTutorial());

        JButton backButton = createButton("BACK TO MENU");
        backButton.addActionListener(e -> cardLayout.show(container, "MENU"));

        JPanel controls = new JPanel(new BorderLayout());
        controls.setOpaque(false);
        controls.add(pageLabel, BorderLayout.NORTH);

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 12));
        buttonRow.setOpaque(false);
        buttonRow.add(previousButton);
        buttonRow.add(nextButton);
        buttonRow.add(tutorialButton);
        buttonRow.add(backButton);
        controls.add(buttonRow, BorderLayout.CENTER);
        add(controls, BorderLayout.SOUTH);

        setupKeyBindings();
        refreshPage();
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Monospaced", Font.BOLD, 15));
        button.setFocusPainted(false);
        return button;
    }

    private void changePage(int delta) {
        currentPage = Math.max(0, Math.min(PAGE_TITLES.length - 1, currentPage + delta));
        refreshPage();
    }

    private void refreshPage() {
        titleLabel.setText(PAGE_TITLES[currentPage]);
        bodyArea.setText(PAGE_TEXT[currentPage]);
        bodyArea.setCaretPosition(0);
        pageLabel.setText("Page " + (currentPage + 1) + " of " + PAGE_TITLES.length);
        previousButton.setEnabled(currentPage > 0);
        nextButton.setEnabled(currentPage < PAGE_TITLES.length - 1);
    }

    private void setupKeyBindings() {
        InputMap inputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getActionMap();

        inputMap.put(KeyStroke.getKeyStroke("LEFT"), "previous_page");
        inputMap.put(KeyStroke.getKeyStroke("A"), "previous_page");
        actionMap.put("previous_page", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                changePage(-1);
            }
        });

        inputMap.put(KeyStroke.getKeyStroke("RIGHT"), "next_page");
        inputMap.put(KeyStroke.getKeyStroke("D"), "next_page");
        actionMap.put("next_page", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                changePage(1);
            }
        });

        inputMap.put(KeyStroke.getKeyStroke("ESCAPE"), "back_menu");
        actionMap.put("back_menu", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                cardLayout.show(container, "MENU");
            }
        });
    }
}
