import java.awt.event.ActionEvent;
import javax.swing.*;

public class InputHandler {
    private GamePanel gp; // Reference to your game panel

    public InputHandler(GamePanel gp) {
        this.gp = gp;
        setupKeyBindings();
    }

    private void setupKeyBindings() {
        // We use the gp's InputMap and ActionMap
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

        // UP (W)
        im.put(KeyStroke.getKeyStroke("W"), "move_up");
        am.put("move_up", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (gp.isPaused() && !gp.isStat()) {
                    gp.navigateMenuUp();
                } else if (!gp.isPaused() && !gp.getPlayer().isMoving) {
                    gp.getPlayer().moveUp();
                }
            }
        });

        im.put(KeyStroke.getKeyStroke("S"), "move_down");
        am.put("move_down", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (gp.isPaused() && !gp.isStat()) {
                    gp.navigateMenuDown();
                } else if (!gp.isPaused() && !gp.getPlayer().isMoving) {
                    gp.getPlayer().moveDown();
                }
            }
        });

        im.put(KeyStroke.getKeyStroke("A"), "move_left");
        am.put("move_left", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (!gp.isPaused() && !gp.getPlayer().isMoving) {
                    gp.getPlayer().moveLeft();
                }
            }
        });

        im.put(KeyStroke.getKeyStroke("D"), "move_right");
        am.put("move_right", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (!gp.isPaused() && !gp.getPlayer().isMoving) {
                    gp.getPlayer().moveRight();
                }
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
    }
}
