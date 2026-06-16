import java.awt.event.*;
import game.framework.*;

public class MyRole extends SampleRole5 {
    private int speed = 10;
    private int screenW = 1080;
    private int screenH = 720;

    private boolean movingUp = false;
    private boolean movingDown = false;
    private boolean movingLeft = false;
    private boolean movingRight = false;

    public MyRole(int x, int y, int w, int h, int jvx, int jvy, int bottom, ImageSequence[][] is) {
        super(x, y, w, h, jvx, jvy, bottom, is);
    }

    @Override
    public void getReady() {
        super.getReady();
        dx = 0;
        dy = 0;
        dim1 = 0; // stop
        dim2 = 0;
    }

    @Override
    public void run() {
        dx = 0;
        dy = 0;

        if (movingLeft) {
            dx -= speed;
        }
        if (movingRight) {
            dx += speed;
        }
        if (movingUp) {
            dy -= speed;
        }
        if (movingDown) {
            dy += speed;
        }

        if (dx != 0 || dy != 0) {
            dim1 = 1; // walk

            // 動畫方向優先採水平，其次垂直
            if (dx > 0) {
                dim2 = 0; // right
                dir = 0;
            } else if (dx < 0) {
                dim2 = 1; // left
                dir = 1;
            } else if (dy < 0) {
                dim2 = 2; // up
            } else {
                dim2 = 3; // down
            }
        } else {
            dim1 = 0; // stop
            dim2 = 0;
        }

        super.run();

        // 邊界限制
        if (x < 0) {
            x = 0;
        }
        if (y < 0) {
            y = 0;
        }
        if (x > screenW - w) {
            x = screenW - w;
        }
        if (y > screenH - h) {
            y = screenH - h;
        }

        if (model != null) {
            model.setState(x, y);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
                movingUp = true;
                break;
            case KeyEvent.VK_DOWN:
                movingDown = true;
                break;
            case KeyEvent.VK_LEFT:
                movingLeft = true;
                break;
            case KeyEvent.VK_RIGHT:
                movingRight = true;
                break;
            default:
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
                movingUp = false;
                break;
            case KeyEvent.VK_DOWN:
                movingDown = false;
                break;
            case KeyEvent.VK_LEFT:
                movingLeft = false;
                break;
            case KeyEvent.VK_RIGHT:
                movingRight = false;
                break;
            default:
                break;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }
}