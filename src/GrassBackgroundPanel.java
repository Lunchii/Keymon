import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GrassBackgroundPanel extends JPanel {
    private Image grassImage;
    private int xOffset = 0;
    private final int speed = 2; // Pixels per frame

    public GrassBackgroundPanel() {
        // Load your image (ensure grass.png is in your project folder)
        grassImage = new ImageIcon("grass.png").getImage();

        // Create a timer to refresh the screen every 20ms (~50 FPS)
        Timer timer = new Timer(20, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                xOffset -= speed; // Move left

                // If the image has scrolled past its width, reset
                if (xOffset <= -grassImage.getWidth(null)) {
                    xOffset = 0;
                }
                repaint(); // Redraw the panel
            }
        });
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw the image twice to fill the gap as it scrolls
        int imgWidth = grassImage.getWidth(this);
        int imgHeight = getHeight(); // Stretch height to fit screen

        // Draw first copy
        g.drawImage(grassImage, xOffset, 0, imgWidth, imgHeight, this);
        // Draw second copy right after the first
        g.drawImage(grassImage, xOffset + imgWidth, 0, imgWidth, imgHeight, this);
    }
}