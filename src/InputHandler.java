import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.*;

/**
 * Registers all keyboard bindings for the world-map game panel.
 *
 * <p>Key mappings:
 * <ul>
 *   <li>{@code W / S}  – move up/down (or navigate the pause menu)</li>
 *   <li>{@code A / D}  – move left/right</li>
 *   <li>{@code ESC}    – toggle pause</li>
 *   <li>{@code P}      – toggle party overlay</li>
 *   <li>{@code B}      – toggle bag overlay</li>
 *   <li>{@code ENTER}  – confirm pause-menu selection</li>
 * </ul>
 * </p>
 * @author Group 72 (Isaac, Miguel, Ryan, Nathaniel, Dehou)
 * @version 1.0
 */
public class InputHandler {
    private GamePanel gp; // Reference to your game panel

    public InputHandler(GamePanel gp) {
        this.gp = gp;
        setupKeyBindings();
    }

    private void setupKeyBindings() {
        // We use the gp's InputMap and ActionMap for non-movement keys
        InputMap im = gp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = gp.getActionMap();

        // ESCAPE - Toggle Pause
        im.put(KeyStroke.getKeyStroke("ESCAPE"), "toggle_pause");
        am.put("toggle_pause", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gp.togglePause();
            }
        });

        // P — toggle party menu
        im.put(KeyStroke.getKeyStroke("P"), "party_menu");
        am.put("party_menu", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                gp.togglePartyMenu();
            }
        });

        // B — toggle bag menu
        im.put(KeyStroke.getKeyStroke("B"), "bag_menu");
        am.put("bag_menu", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                gp.toggleBagMenu();
            }
        });

        // T — toggle HUD
        im.put(KeyStroke.getKeyStroke("T"), "toggle_hud");
        am.put("toggle_hud", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                gp.toggleHud();
            }
        });

        // ENTER (Select Pause Option)
        im.put(KeyStroke.getKeyStroke("ENTER"), "pause_select");
        am.put("pause_select", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (gp.isPaused()) {
                    gp.handlePauseSelection();
                }
            }
        });

        // Add KeyListener for movement keys
        gp.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (gp.isPaused() && !gp.isStat()) {
                    if (e.getKeyCode() == KeyEvent.VK_W) {
                        gp.navigateMenuUp();
                    } else if (e.getKeyCode() == KeyEvent.VK_S) {
                        gp.navigateMenuDown();
                    }
                } else if (!gp.isPaused() && !gp.isInBattle()) {
                    switch (e.getKeyCode()) {
                        case KeyEvent.VK_W:
                            gp.getPlayer().moveUp = true;
                            break;
                        case KeyEvent.VK_S:
                            gp.getPlayer().moveDown = true;
                            break;
                        case KeyEvent.VK_A:
                            gp.getPlayer().moveLeft = true;
                            break;
                        case KeyEvent.VK_D:
                            gp.getPlayer().moveRight = true;
                            break;
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_W:
                        gp.getPlayer().moveUp = false;
                        break;
                    case KeyEvent.VK_S:
                        gp.getPlayer().moveDown = false;
                        break;
                    case KeyEvent.VK_A:
                        gp.getPlayer().moveLeft = false;
                        break;
                    case KeyEvent.VK_D:
                        gp.getPlayer().moveRight = false;
                        break;
                }
            }
        });
    }
}
