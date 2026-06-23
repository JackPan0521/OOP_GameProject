import java.awt.event.*;
import java.awt.Graphics;
import game.framework.*;

public class MyRole extends SampleRole5 {
    private int speed = 10;

    private boolean movingUp    = false;
    private boolean movingDown  = false;
    private boolean movingLeft  = false;
    private boolean movingRight = false;

    private MineBoard  board;
    private BoardRole  boardRole;
    private ShopRole   shopRole;                        // 商店參考，用來判斷是否暫停
    private PlayerData playerData = new PlayerData();
    private int wave = 1; // 目前關卡數，每過一關 +1

    // -------------------------------------------------------
    // 建構子
    // -------------------------------------------------------
    public MyRole(int x, int y, int w, int h, int jvx, int jvy, int bottom,
                  ImageSequence[][] is, MineBoard board) {
        super(x, y, w, h, jvx, jvy, bottom, is);
        this.board = board;
    }

    public MyRole(int x, int y, int w, int h, int jvx, int jvy, int bottom,
                  ImageSequence[][] is) {
        super(x, y, w, h, jvx, jvy, bottom, is);
    }

    // -------------------------------------------------------
    // Setters
    // -------------------------------------------------------
    public void setBoardRole(BoardRole boardRole) { this.boardRole = boardRole; }
    public void setShopRole(ShopRole shopRole)    { this.shopRole  = shopRole;  }
    public void setBoard(MineBoard board)         { this.board     = board;     }
    public void setSpeed(int speed)               { this.speed     = speed;     }
    public int  getWave()                         { return wave;                }

    public PlayerData getPlayerData() { return this.playerData; }

    // -------------------------------------------------------
    // Role 生命週期
    // -------------------------------------------------------
    @Override
    public void getReady() {
        super.getReady();
        dx = 0; dy = 0;
        dim1 = 0; dim2 = 0;
    }

    @Override
    public void run() {
        // 商店開啟時凍結移動
        if (shopRole != null && shopRole.isOpen()) return;

        dx = 0; dy = 0;

        if (movingLeft)  dx -= speed;
        if (movingRight) dx += speed;
        if (movingUp)    dy -= speed;
        if (movingDown)  dy += speed;

        if (dx != 0 || dy != 0) {
            dim1 = 1;
            if      (dx > 0) { dim2 = 0; dir = 0; }
            else if (dx < 0) { dim2 = 1; dir = 1; }
            else if (dy < 0) { dim2 = 2; }
            else              { dim2 = 3; }
        } else {
            dim1 = 0; dim2 = 0;
        }

        super.run();

        // 大地圖物理邊界硬拉回
        if (board != null) {
            int boardTop  = board.getBoardTop();
            int mapWidth  = board.getTotalCols() * board.getTileSize();
            int mapHeight = board.getTotalRows() * board.getTileSize() + boardTop;

            if (this.x < 0)                  this.x = 0;
            if (this.y < boardTop)            this.y = boardTop;
            if (this.x > mapWidth  - this.w)  this.x = mapWidth  - this.w;
            if (this.y > mapHeight - this.h)  this.y = mapHeight - this.h;
        }

        if (model != null) model.setState(this.x, this.y);

        // 每幀檢查是否踩到地雷
        if (board != null && board.pollMineHit()) {
            boolean reallyHurt = playerData.takeDamage(wave); // 護盾攔截時回傳 false
            if (reallyHurt && playerData.isDead()) {
                board.setGameOver(true);
            }
        }
    }

    // 自行換算世界座標 → 螢幕座標
    @Override
    public void display(Graphics g) {
        if (is == null) return;
        if (dim1 < 0 || dim1 >= is.length)        return;
        if (is[dim1] == null)                      return;
        if (dim2 < 0 || dim2 >= is[dim1].length)  return;
        if (is[dim1][dim2] == null)                return;

        int drawX = (board != null) ? this.x - board.getCameraX() : this.x;
        int drawY = (board != null) ? this.y - board.getCameraY() : this.y;

        is[dim1][dim2].delay(delay);
        g.drawImage(is[dim1][dim2].next(true), drawX, drawY, w, h, null);
    }

    // -------------------------------------------------------
    // 按鍵
    // -------------------------------------------------------
    @Override
    public void keyPressed(KeyEvent e) {
        // 商店開啟時，移動鍵不觸發（方向鍵交給商店處理）
        boolean shopOpen = (shopRole != null && shopRole.isOpen());

        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
                if (!shopOpen) movingUp    = true; break;
            case KeyEvent.VK_DOWN:
                if (!shopOpen) movingDown  = true; break;
            case KeyEvent.VK_LEFT:
                if (!shopOpen) movingLeft  = true; break;
            case KeyEvent.VK_RIGHT:
                if (!shopOpen) movingRight = true; break;

            case KeyEvent.VK_F:
                if (!shopOpen && board != null) {
                    board.openAtPixel(x + w / 2, y + h - 1, playerData, wave);
                }
                break;

            case KeyEvent.VK_G:
                if (!shopOpen && board != null) {
                    board.toggleFlagAtPixel(x + w / 2, y + h - 1);
                }
                break;

            case KeyEvent.VK_Z:
                // 技能0：啟動防爆護盾
                if (!shopOpen) playerData.activateShield();
                break;

            case KeyEvent.VK_X:
                // 技能1：自動標示最近 3 顆炸彈
                if (!shopOpen && board != null && playerData.isSkillUnlocked(1)) {
                    board.autoFlagNearestMines(x + w / 2, y + h / 2, 3);
                }
                break;

            case KeyEvent.VK_C:
                // 技能2：啟動經驗加倍（本關獎勵×2）
                if (!shopOpen) playerData.activateExpBoost();
                break;

            case KeyEvent.VK_R:
                if (!shopOpen && board != null
                        && (board.isBigLevelCleared() || board.isGameOver())) {
                    if (board.isBigLevelCleared()) {
                        int reward = Math.max(10, 300 - board.getElapsedTime() * 2);
                        if (playerData.isExpBoostActive()) reward *= 2; // 技能2：加倍
                        playerData.addScore(reward);
                        playerData.resetExpBoost();   // 經驗加倍用完
                        playerData.consumeShield();   // 護盾過關清除
                        wave++;
                    } else {
                        playerData.resetHp();
                        playerData.resetExpBoost();
                        playerData.consumeShield();
                    }
                    int minesPerZone = Math.min(5 + (wave - 1), 20);
                    int minesZone    = Math.min(wave, 5); // 區塊數：wave1=2, wave2=3 ... 上限5
                    MineBoard newBoard = new MineBoard.Builder()
                            .setZoneDimensions(minesZone, minesZone, 64)
                            .setBoardOffset(0, 72)
                            .setMinesPerZone(minesPerZone)
                            .build();
                    this.setBoard(newBoard);
                    if (boardRole != null) boardRole.setBoard(newBoard);
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
            case KeyEvent.VK_UP:    movingUp    = false; break;
            case KeyEvent.VK_DOWN:  movingDown  = false; break;
            case KeyEvent.VK_LEFT:  movingLeft  = false; break;
            case KeyEvent.VK_RIGHT: movingRight = false; break;
            default: break;
        }
    }

    @Override public void keyTyped(KeyEvent e) {}
}