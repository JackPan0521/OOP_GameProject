import java.awt.event.*;
import java.awt.Graphics;
import game.framework.*;

public class MyRole extends SampleRole5 {
    private int speed = 10;

    private boolean movingUp = false;
    private boolean movingDown = false;
    private boolean movingLeft = false;
    private boolean movingRight = false;

    private MineBoard board;
    private BoardRole boardRole;
    private PlayerData playerData = new PlayerData(); 

    public MyRole(int x, int y, int w, int h, int jvx, int jvy, int bottom, ImageSequence[][] is, MineBoard board) {
        super(x, y, w, h, jvx, jvy, bottom, is);
        this.board = board;
    }

    public MyRole(int x, int y, int w, int h, int jvx, int jvy, int bottom, ImageSequence[][] is) {
        super(x, y, w, h, jvx, jvy, bottom, is);
    }

    public void setBoardRole(BoardRole boardRole){
        this.boardRole = boardRole;
    }

    public PlayerData getPlayerData() {
        return this.playerData;
    }

    public void setBoard(MineBoard board) {
        this.board = board;
    }

    @Override
    public void getReady() {
        super.getReady();
        dx = 0;
        dy = 0;
        dim1 = 0; 
        dim2 = 0;
    }

    @Override
    public void run() {
        dx = 0;
        dy = 0;

        if (movingLeft) dx -= speed;
        if (movingRight) dx += speed;
        if (movingUp) dy -= speed;
        if (movingDown) dy += speed;

        if (dx != 0 || dy != 0) {
            dim1 = 1; 
            if (dx > 0) { dim2 = 0; dir = 0; } 
            else if (dx < 0) { dim2 = 1; dir = 1; } 
            else if (dy < 0) { dim2 = 2; } 
            else { dim2 = 3; }
        } else {
            dim1 = 0; dim2 = 0;
        }

        // 讓引擎先更新玩家的世界座標
        super.run();

        // ✅ 只做「大地圖物理邊界」的硬拉回，螢幕邊界交給攝影機處理
        if (board != null) {
            int boardTop = board.getBoardTop();
            int mapWidth  = board.getTotalCols() * board.getTileSize();
            int mapHeight = board.getTotalRows() * board.getTileSize() + boardTop;

            if (this.x < 0)                  this.x = 0;
            if (this.y < boardTop)            this.y = boardTop;
            if (this.x > mapWidth - this.w)   this.x = mapWidth - this.w;
            if (this.y > mapHeight - this.h)  this.y = mapHeight - this.h;
        }

        if (model != null) {
            model.setState(this.x, this.y);
        }
    }

    // ✅ 自行換算世界座標 → 螢幕座標，確保玩家跟地圖一起被攝影機正確偏移
    @Override
    public void display(Graphics g) {
        if (is == null) return;
        if (dim1 < 0 || dim1 >= is.length) return;
        if (is[dim1] == null) return;
        if (dim2 < 0 || dim2 >= is[dim1].length) return;
        if (is[dim1][dim2] == null) return;

        int drawX = this.x;
        int drawY = this.y;

        if (board != null) {
            drawX = this.x - board.getCameraX();
            drawY = this.y - board.getCameraY();
        }

        is[dim1][dim2].delay(delay);
        g.drawImage(is[dim1][dim2].next(true), drawX, drawY, w, h, null);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:    movingUp = true;    break;
            case KeyEvent.VK_DOWN:  movingDown = true;  break;
            case KeyEvent.VK_LEFT:  movingLeft = true;  break;
            case KeyEvent.VK_RIGHT: movingRight = true; break;
            case KeyEvent.VK_F:
                if (board != null) {
                    int footX = x + w / 2;
                    int footY = y + h - 1;
                    board.openAtPixel(footX, footY);
                }
                break;
            case KeyEvent.VK_G:
                if (board != null) {
                    int footX = x + w / 2;
                    int footY = y + h - 1;
                    board.toggleFlagAtPixel(footX, footY);
                }
                break;
            case KeyEvent.VK_R:
                if (board != null && (board.isBigLevelCleared() || board.isGameOver())) {
                    if (board.isBigLevelCleared()) {
                        int reward = Math.max(10, 300 - board.getElapsedTime() * 2);
                        playerData.addScore(reward);
                    }

                    MineBoard newBoard = new MineBoard.Builder()
                                .setZoneDimensions(4, 4, 64) 
                                .setBoardOffset(0, 72)
                                .setMinesPerZone(25) 
                                .build();
                    
                    this.setBoard(newBoard);
                    if (boardRole != null) {
                        boardRole.setBoard(newBoard); 
                    }

                    this.x = 200;
                    this.y = 200;
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:    movingUp = false;    break;
            case KeyEvent.VK_DOWN:  movingDown = false;  break;
            case KeyEvent.VK_LEFT:  movingLeft = false;  break;
            case KeyEvent.VK_RIGHT: movingRight = false; break;
            default:
                break;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}
}
