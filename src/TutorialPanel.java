import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import javax.swing.*;

/**
 * Tutorial menu screen matching ParentalControlPanel style exactly.
 * Displays a multi-page instruction dialog that can be closed,
 * revealing menu buttons beneath: Info, Start Tutorial, and Back.
 * 
 * @author Group 72 (Isaac, Miguel, Ryan, Nathaniel, Dehou)
 * @version 1.0
 */
public class TutorialPanel extends JPanel {
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

    private Image backgroundGif;
    private ArrayList<JButton> buttons = new ArrayList<>();
    private int focusedIndex = 0;
    private Font customFont;
    private CardLayout cardLayout;
    private JPanel container;
    private Main mainApp;
    private JDialog instructionDialog;
    private boolean instructionDialogOpen = true;
    private int currentPage = 0;

    /**
     * Constructs the tutorial panel with navigation buttons and
     * keyboard support, matching ParentalControlPanel style.
     *
     * @param layout    the shared CardLayout used to switch screens
     * @param container the parent panel that holds all screens
     * @param mainApp   the main application instance for screen coordination
     */
    public TutorialPanel(CardLayout layout, JPanel container, Main mainApp) {
        this.cardLayout = layout;
        this.container = container;
        this.mainApp = mainApp;

        backgroundGif = new ImageIcon("images/menuBackground.gif").getImage();
        loadCustomFont();

        this.setLayout(new BorderLayout());
        this.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 150));

        // --- RIGHT SIDE: TUTORIAL BUTTONS (Mirroring MenuPanel/ParentalControlPanel) ---
        JPanel menuContent = new JPanel();
        menuContent.setOpaque(false);
        menuContent.setLayout(new BoxLayout(menuContent, BoxLayout.Y_AXIS));

        OutlinedLabel title = createTitle("Tutorial");
        OutlinedLabel title2 = createTitle("Menu");

        menuContent.add(Box.createVerticalGlue());
        menuContent.add(title);
        menuContent.add(title2);
        menuContent.add(Box.createRigidArea(new Dimension(0, 50)));

        String[] labels = { "INFO", "START TUTORIAL", "BACK" };
        for (int i = 0; i < labels.length; i++) {
            final int index = i;
            final String label = labels[i];

            OutlinedButton btn = new OutlinedButton(label);
            btn.setFont(customFont.deriveFont(Font.PLAIN, 28f));
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            btn.setMaximumSize(new Dimension(450, 60));
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

        // --- LEFT SIDE: TUTORIAL INFO ---
        JPanel footerInfo = new JPanel();
        footerInfo.setOpaque(false);
        footerInfo.setLayout(new BoxLayout(footerInfo, BoxLayout.Y_AXIS));
        footerInfo.setBorder(BorderFactory.createEmptyBorder(0, 50, 50, 0));

        OutlinedLabel tutorialLabel = new OutlinedLabel("LEARN THE BASICS");
        tutorialLabel.setFont(customFont.deriveFont(Font.BOLD, 24f));
        OutlinedLabel startLabel = new OutlinedLabel("START YOUR JOURNEY");
        startLabel.setFont(customFont.deriveFont(Font.PLAIN, 18f));

        footerInfo.add(Box.createVerticalGlue());
        footerInfo.add(tutorialLabel);
        footerInfo.add(startLabel);
        this.add(footerInfo, BorderLayout.WEST);

        updateButtonStyles();
        setupKeyboardNavigation();

        // Add component listener to show instruction dialog when this panel is shown
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                if (instructionDialogOpen) {
                    showInstructionDialog();
                }
            }

            @Override
            public void componentHidden(ComponentEvent e) {
                if (instructionDialog != null && instructionDialog.isVisible()) {
                    instructionDialog.dispose();
                }
            }
        });
    }

    /**
     * Shows the multi-page instruction dialog box in the center of the screen.
     */
    private void showInstructionDialog() {
        if (instructionDialog != null && instructionDialog.isVisible()) {
            instructionDialog.toFront();
            return;
        }

        instructionDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Tutorial Instructions",
                Dialog.ModalityType.MODELESS);

        // Create instruction content panel
        JPanel instructionContent = new JPanel(new BorderLayout(10, 10));
        instructionContent.setBackground(new Color(20, 45, 30));
        instructionContent.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Title
        JLabel titleLabel = new JLabel("", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Monospaced", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        instructionContent.add(titleLabel, BorderLayout.NORTH);

        // Instructions text area
        JTextArea instructionArea = new JTextArea();
        instructionArea.setEditable(false);
        instructionArea.setLineWrap(true);
        instructionArea.setWrapStyleWord(true);
        instructionArea.setFont(new Font("SansSerif", Font.PLAIN, 16));
        instructionArea.setBackground(new Color(245, 245, 245));
        instructionArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.BLACK, 4),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JScrollPane scrollPane = new JScrollPane(instructionArea);
        scrollPane.setBorder(null);
        instructionContent.add(scrollPane, BorderLayout.CENTER);

        // Page info and navigation buttons
        JLabel pageLabel = new JLabel("", SwingConstants.CENTER);
        pageLabel.setFont(new Font("Monospaced", Font.BOLD, 14));
        pageLabel.setForeground(new Color(220, 235, 220));

        JButton prevButton = new JButton("PREVIOUS");
        prevButton.setFont(new Font("Monospaced", Font.BOLD, 12));
        prevButton.setFocusPainted(false);

        JButton nextButton = new JButton("NEXT");
        nextButton.setFont(new Font("Monospaced", Font.BOLD, 12));
        nextButton.setFocusPainted(false);

        JButton closeButton = new JButton("CLOSE");
        closeButton.setFont(new Font("Monospaced", Font.BOLD, 12));
        closeButton.setFocusPainted(false);
        closeButton.addActionListener(e -> {
            instructionDialogOpen = false;
            instructionDialog.dispose();
        });

        prevButton.addActionListener(e -> {
            currentPage = Math.max(0, currentPage - 1);
            updateDialogContent(titleLabel, instructionArea, pageLabel, prevButton, nextButton);
        });

        nextButton.addActionListener(e -> {
            currentPage = Math.min(PAGE_TITLES.length - 1, currentPage + 1);
            updateDialogContent(titleLabel, instructionArea, pageLabel, prevButton, nextButton);
        });

        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.setOpaque(false);
        buttonPanel.add(pageLabel, BorderLayout.NORTH);

        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        navPanel.setOpaque(false);
        navPanel.add(prevButton);
        navPanel.add(nextButton);
        navPanel.add(closeButton);
        buttonPanel.add(navPanel, BorderLayout.CENTER);

        instructionContent.add(buttonPanel, BorderLayout.SOUTH);

        instructionDialog.add(instructionContent);
        instructionDialog.setSize(600, 500);
        instructionDialog.setLocationRelativeTo(SwingUtilities.getWindowAncestor(this));
        instructionDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        // Initialize dialog content
        updateDialogContent(titleLabel, instructionArea, pageLabel, prevButton, nextButton);

        instructionDialog.setVisible(true);
    }

    private void updateDialogContent(JLabel titleLabel, JTextArea instructionArea, JLabel pageLabel,
                                     JButton prevButton, JButton nextButton) {
        titleLabel.setText(PAGE_TITLES[currentPage]);
        instructionArea.setText(PAGE_TEXT[currentPage]);
        instructionArea.setCaretPosition(0);
        pageLabel.setText("Page " + (currentPage + 1) + " of " + PAGE_TITLES.length);
        prevButton.setEnabled(currentPage > 0);
        nextButton.setEnabled(currentPage < PAGE_TITLES.length - 1);
    }

    /**
     * Handles a button selection by navigating to the appropriate screen
     * or performing the selected action.
     *
     * @param label the text label of the selected button
     */
    private void handleSelection(String label) {
        if (label.equals("BACK")) {
            if (instructionDialog != null && instructionDialog.isVisible()) {
                instructionDialog.dispose();
            }
            cardLayout.show(container, "MENU");
        } else if (label.equals("INFO")) {
            instructionDialogOpen = true;
            currentPage = 0;
            showInstructionDialog();
        } else if (label.equals("START TUTORIAL")) {
            if (instructionDialog != null && instructionDialog.isVisible()) {
                instructionDialog.dispose();
            }
            mainApp.startTutorial();
        }
    }

    /**
     * Updates button foreground colours to highlight the currently focused
     * button and dim the rest.
     */
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

    /**
     * Registers W/S/ENTER key bindings for keyboard navigation of the panel.
     */
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

    /**
     * Creates a large outlined title label styled for the panel heading.
     *
     * @param text the title text to display
     * @return a configured {@link OutlinedLabel}
     */
    private OutlinedLabel createTitle(String text) {
        OutlinedLabel lbl = new OutlinedLabel(text);
        lbl.setFont(customFont.deriveFont(Font.BOLD, 100f));
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        lbl.setPreferredSize(new Dimension(800, 150));
        lbl.setMaximumSize(new Dimension(800, 150));
        return lbl;
    }

    /**
     * Paints the animated GIF background scaled to fill the panel.
     *
     * @param g the graphics context
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundGif != null)
            g.drawImage(backgroundGif, 0, 0, getWidth(), getHeight(), this);
    }

    /**
     * Loads the custom KA1 TrueType font from the fonts directory. Falls back
     * to Arial if the font file cannot be read.
     */
    private void loadCustomFont() {
        try {
            customFont = Font.createFont(Font.TRUETYPE_FONT, new File("fonts/ka1.ttf"));
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(customFont);
        } catch (Exception e) {
            customFont = new Font("Arial", Font.BOLD, 24);
        }
    }
}
