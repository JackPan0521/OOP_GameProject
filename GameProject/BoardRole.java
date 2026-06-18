import game.framework.*;
import java.awt.Graphics;
import java.awt.Rectangle;

public class BoardRole implements Role {
    private final MineBoard board;

    public BoardRole(MineBoard board) {
        this.board = board;
    }

    @Override
    public void getReady() {
    }

    @Override
    public void run() {
    }

    @Override
    public void end() {
    }

    @Override
    public Model getModel() {
        return null; // 不參與碰撞
    }

    @Override
    public Effect conflict(Role r, Rectangle intersect) {
        return null;
    }

    @Override
    public void display(Graphics g) {
        board.draw(g);
    }
}