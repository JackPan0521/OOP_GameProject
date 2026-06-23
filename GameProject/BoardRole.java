import game.framework.*;
import java.awt.Graphics;
import java.awt.Rectangle;

public class BoardRole implements Role {
    private MineBoard board;
    private MyRole player; 

    public BoardRole(MineBoard board) {
        this.board = board;
    }

    public void setBoard(MineBoard board){
        this.board = board;
    }

    public void setPlayer(MyRole player) {
        this.player = player;
    }

    @Override
    public void getReady() {}

    @Override
    public void run() {}

    @Override
    public void end() {}

    @Override
    public Model getModel() { return null; }

    @Override
    public Effect conflict(Role r, Rectangle intersect) { return null; }

    @Override
    public void display(Graphics g) {
        if (board != null && player != null) {
            int screenW = 1080;
            int screenH = 720;

            // ✅ 先根據玩家最新位置更新攝影機
            board.updateCamera(
                player.getX(), player.getY(),
                player.getW(), player.getH(),
                screenW, screenH
            );

            // ✅ 直接繪製地圖（地圖內部自己扣 cameraX/cameraY）
            // ✅ 不做 g.translate，不呼叫 player.display（MyRole 自己換算螢幕座標）
            board.draw(g, player.getPlayerData());

        } else if (board != null) {
            board.draw(g, null);
        }
    }
}