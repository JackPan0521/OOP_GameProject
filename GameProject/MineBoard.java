import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.io.File;
import java.util.Random;
import javax.imageio.ImageIO;

public class MineBoard {
    public static final int ZONE_SIZE = 8; 

    private final int tileSize;      
    private final int zoneRows;      
    private final int zoneCols;      
    private final int totalRows;     
    private final int totalCols;     
    private final int boardLeft;     
    private final int boardTop;      
    private final int minePerZone;   // 每個區塊最多的地雷數
    private int totalMineCount = 0;  // 實際放置的總地雷數（生成後才知道）

    private boolean generated = false;
    private Cell[][] cells;
    private boolean[][] zoneCleared; 

    // 攝影機系統
    private int cameraX = 0;
    private int cameraY = 0;

    private boolean gameOver = false;
    private boolean mineHitPending = false; // 本幀踩到地雷，等外部來處理
    private int elapsedTime = 0;
    private long startTime = -1;

    private Image unopenTile;
    private Image flagTile;
    private Image boomTile;
    private Image[] numberTiles;

    public MineBoard(Builder builder) {
        this.tileSize = builder.tileSize;
        this.zoneRows = builder.zoneRows;
        this.zoneCols = builder.zoneCols;
        this.totalRows = builder.zoneRows * ZONE_SIZE;
        this.totalCols = builder.zoneCols * ZONE_SIZE;
        this.boardLeft = builder.boardLeft;
        this.boardTop = builder.boardTop;
        this.minePerZone = builder.minePerZone;

        cells = new Cell[totalRows][totalCols];
        for (int r = 0; r < totalRows; r++) {
            for (int c = 0; c < totalCols; c++) {
                cells[r][c] = new Cell(r, c);
            }
        }

        zoneCleared = new boolean[zoneRows][zoneCols];
        numberTiles = new Image[9];

        try {
            unopenTile = ImageIO.read(new File("src/minesweeper icon/unopen.png"));
            flagTile = ImageIO.read(new File("src/minesweeper icon/flag.png"));
            boomTile = ImageIO.read(new File("src/minesweeper icon/boom.png"));

            for (int i = 0; i <= 8; i++) {
                File f = new File("src/minesweeper icon/" + i + ".png");
                if (f.exists()) {
                    numberTiles[i] = ImageIO.read(f);
                }
            }
        } catch (Exception e) {
            System.out.println("地圖圖片載入失敗，請檢查路徑：" + e.getMessage());
        }
    }

    public void updateCamera(int playerX, int playerY, int playerW, int playerH, int screenW, int screenH) {
        // 定義玩家距離視窗邊緣多少像素時，開始推動攝影機（例如設為 128 像素，約 2 格的距離）
        int paddingX = 128;
        int paddingY = 128;

        // 計算玩家目前在「螢幕上」的相對位置
        int screenPlayerX = playerX - cameraX;
        int screenPlayerY = playerY - cameraY;

        // --- 水平方向推動 ---
        // 玩家太靠右邊，把攝影機往右推
        if (screenPlayerX > screenW - playerW - paddingX) {
            cameraX = playerX - (screenW - playerW - paddingX);
        }
        // 玩家太靠左邊，把攝影機往左推
        else if (screenPlayerX < paddingX) {
            cameraX = playerX - paddingX;
        }

        // --- 垂直方向推動 ---
        // 玩家太靠下邊，把攝影機往下推
        if (screenPlayerY > screenH - playerH - paddingY) {
            cameraY = playerY - (screenH - playerH - paddingY);
        }
        // 玩家太靠上邊（注意避開上方 boardTop 計分板），把攝影機往上推
        else if (screenPlayerY < boardTop + paddingY) {
            cameraY = playerY - (boardTop + paddingY);
        }

        // --- 最終保險：限制攝影機絕對不能露出大地圖外 ---
        int maxCameraX = (totalCols * tileSize) - screenW;
        int maxCameraY = (totalRows * tileSize) - (screenH - boardTop);

        if (cameraX < 0) cameraX = 0;
        if (cameraY < 0) cameraY = 0;
        if (maxCameraX > 0 && cameraX > maxCameraX) cameraX = maxCameraX;
        if (maxCameraY > 0 && cameraY > maxCameraY) cameraY = maxCameraY;
    }

    public int getCameraX() { return this.cameraX; }
    public int getCameraY() { return this.cameraY; }
    public int getTotalRows() { return this.totalRows; }
    public int getTotalCols() { return this.totalCols; }
    public int getTileSize() { return this.tileSize; }
    public int getBoardTop() { return this.boardTop; }
    public boolean isGameOver() { return this.gameOver; }
    public void setGameOver(boolean val) {
        this.gameOver = val;
        if (val) revealAllMines();
    }
    /** 每幀呼叫一次：若本幀踩到地雷回傳 true，並自動清除旗標 */
    public boolean pollMineHit() {
        if (mineHitPending) { mineHitPending = false; return true; }
        return false;
    }
    public int getElapsedTime() { return this.elapsedTime; }

    private void updateTime() {
        if (startTime == -1 || gameOver || isBigLevelCleared()) return;
        elapsedTime = (int) ((System.currentTimeMillis() - startTime) / 1000);
    }

    public void openAtPixel(int pixelX, int pixelY, PlayerData playerData, int currentWave) {
        if (gameOver || isBigLevelCleared()) return;
        int c = (pixelX - boardLeft) / tileSize;
        int r = (pixelY - boardTop) / tileSize;

        if (inBounds(r, c)) {
            if (!generated) {
                generateMines(r, c);
                generated = true;
                startTime = System.currentTimeMillis();
            }

            Cell cell = cells[r][c];

            // Chord 操作：對已翻開的數字格按 F，若周圍旗子數 >= 相鄰地雷數，自動翻開所有未插旗鄰居
            if (cell.isRevealed() && cell.getAdjacentMines() > 0) {
                int flagCount = 0;
                for (int dr = -1; dr <= 1; dr++) {
                    for (int dc = -1; dc <= 1; dc++) {
                        if (inBounds(r + dr, c + dc) && cells[r + dr][c + dc].isFlagged()) {
                            flagCount++;
                        }
                    }
                }
                if (flagCount >= cell.getAdjacentMines()) {
                    for (int dr = -1; dr <= 1; dr++) {
                        for (int dc = -1; dc <= 1; dc++) {
                            if (inBounds(r + dr, c + dc)) {
                                openCell(r + dr, c + dc, playerData, currentWave);
                            }
                        }
                    }
                }
            } else {
                openCell(r, c, playerData, currentWave);
            }
        }
    }

    public void toggleFlagAtPixel(int pixelX, int pixelY) {
        if (gameOver || isBigLevelCleared()) return;
        int c = (pixelX - boardLeft) / tileSize;
        int r = (pixelY - boardTop) / tileSize;

        if (inBounds(r, c)) {
            Cell cell = cells[r][c];
            if (!cell.isRevealed()) {
                cell.setFlagged(!cell.isFlagged());
            }
        }
    }

    private void generateMines(int startRow, int startCol) {
        Random rand = new Random();
        totalMineCount = 0;

        // 逐區塊放置地雷，每個區塊最多放 minePerZone 顆
        for (int zr = 0; zr < zoneRows; zr++) {
            for (int zc = 0; zc < zoneCols; zc++) {
                int placed = 0;
                int attempts = 0;
                int maxAttempts = ZONE_SIZE * ZONE_SIZE * 10; // 避免無限迴圈

                while (placed < minePerZone && attempts < maxAttempts) {
                    attempts++;
                    // 在這個區塊的範圍內隨機選格
                    int r = zr * ZONE_SIZE + rand.nextInt(ZONE_SIZE);
                    int c = zc * ZONE_SIZE + rand.nextInt(ZONE_SIZE);

                    // 跳過起始點周圍 3x3 的安全區
                    if (Math.abs(r - startRow) <= 1 && Math.abs(c - startCol) <= 1) {
                        continue;
                    }

                    if (!cells[r][c].isMine()) {
                        cells[r][c].setMine(true);
                        placed++;
                        totalMineCount++;
                    }
                }
            }
        }

        // 計算每格的相鄰地雷數
        for (int r = 0; r < totalRows; r++) {
            for (int c = 0; c < totalCols; c++) {
                if (!cells[r][c].isMine()) {
                    int count = 0;
                    for (int dr = -1; dr <= 1; dr++) {
                        for (int dc = -1; dc <= 1; dc++) {
                            if (inBounds(r + dr, c + dc) && cells[r + dr][c + dc].isMine()) {
                                count++;
                            }
                        }
                    }
                    cells[r][c].setAdjacentMines(count);
                }
            }
        }
    }

    private void openCell(int r, int c, PlayerData playerData, int currentWave) {
        Cell cell = cells[r][c];
        if (cell.isRevealed() || cell.isFlagged()) return;

        cell.setRevealed(true);

        if (cell.isMine()) {
            mineHitPending = true; // 通知外部處理扣血，不在這裡決定 gameOver
            cell.setRevealed(true);
            return;
        }

        if (cell.getAdjacentMines() == 0) {
            for (int dr = -1; dr <= 1; dr++) {
                for (int dc = -1; dc <= 1; dc++) {
                    if (inBounds(r + dr, c + dc)) {
                        openCell(r + dr, c + dc, playerData, currentWave);
                    }
                }
            }
        }
        checkZoneCleared(r, c);
    }

    private void checkZoneCleared(int r, int c) {
        int zr = r / ZONE_SIZE;
        int zc = c / ZONE_SIZE;

        if (zoneCleared[zr][zc]) return;

        boolean allClear = true;
        for (int row = zr * ZONE_SIZE; row < (zr + 1) * ZONE_SIZE; row++) {
            for (int col = zc * ZONE_SIZE; col < (zc + 1) * ZONE_SIZE; col++) {
                Cell cell = cells[row][col];
                if (!cell.isMine() && !cell.isRevealed()) {
                    allClear = false;
                    break;
                }
            }
            if (!allClear) break;
        }

        if (allClear) {
            zoneCleared[zr][zc] = true;
        }
    }

    public boolean isBigLevelCleared() {
        for (int zr = 0; zr < zoneRows; zr++) {
            for (int zc = 0; zc < zoneCols; zc++) {
                if (!zoneCleared[zr][zc]) {
                    return false;
                }
            }
        }
        return true;
    }

    private void revealAllMines() {
        for (int r = 0; r < totalRows; r++) {
            for (int c = 0; c < totalCols; c++) {
                if (cells[r][c].isMine()) {
                    cells[r][c].setRevealed(true);
                }
            }
        }
    }

    private int getFlagCount() {
        int count = 0;
        for (int r = 0; r < totalRows; r++) {
            for (int c = 0; c < totalCols; c++) {
                if (cells[r][c].isFlagged()) count++;
            }
        }
        return count;
    }

    public void draw(Graphics g, PlayerData playerData) {
        if (unopenTile == null) return;

        // 💡 所有的格子繪製都要手動扣除攝影機偏移量 (cameraX, cameraY)
        for (int r = 0; r < totalRows; r++) {
            for (int c = 0; c < totalCols; c++) {
                Cell cell = cells[r][c];
                int x = boardLeft + c * tileSize - cameraX;
                int y = boardTop + r * tileSize - cameraY;

                // 效能優化：超出螢幕範圍的不畫
                if (x + tileSize < 0 || x > 1080 || y + tileSize < boardTop || y > 720) {
                    continue;
                }

                if (!cell.isRevealed()) {
                    if (cell.isFlagged() && flagTile != null) {
                        g.drawImage(flagTile, x, y, tileSize, tileSize, null);
                    } else {
                        g.drawImage(unopenTile, x, y, tileSize, tileSize, null);
                    }
                } else {
                    if (cell.isMine()) {
                        if (boomTile != null) {
                            g.drawImage(boomTile, x, y, tileSize, tileSize, null);
                        }
                    } else {
                        int n = cell.getAdjacentMines();
                        if (n >= 0 && n <= 8 && numberTiles[n] != null) {
                            g.drawImage(numberTiles[n], x, y, tileSize, tileSize, null);
                        } else {
                            g.drawImage(unopenTile, x, y, tileSize, tileSize, null);
                        }
                    }
                }
            }
        }

        // 粗黑區域線（同樣扣除攝影機）
        g.setColor(Color.BLACK);
        for (int r = 0; r <= zoneRows; r++) {
            int y = boardTop + r * ZONE_SIZE * tileSize - cameraY;
            if (y >= boardTop && y <= 720) {
                g.fillRect(boardLeft, y - 2, totalCols * tileSize, 4); 
            }
        }
        for (int c = 0; c <= zoneCols; c++) {
            int x = boardLeft + c * ZONE_SIZE * tileSize - cameraX;
            if (x >= 0 && x <= 1080) {
                g.fillRect(x - 2, boardTop, 4, totalRows * tileSize); 
            }
        }

        // UI 狀態面板永遠固定在畫面上方，不隨攝影機移動
        updateTime();
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(0, 0, 1080, boardTop);
        g.setColor(Color.DARK_GRAY);
        g.drawRect(0, 0, 1080 - 1, boardTop - 1);

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // --- 左側：地雷數 ---
        g2.setFont(new Font("Monospaced", Font.BOLD, 24));
        int remainingMines = totalMineCount - getFlagCount();
        g2.setColor(new Color(200, 40, 40));
        g2.drawString(String.format("💣 %03d", remainingMines), 20, 46);

        // --- 中間偏左：HP Bar ---
        if (playerData != null) {
            int hp    = playerData.getCurrentHp();
            int maxHp = playerData.getMaxHp();

            // HP 文字
            g2.setFont(new Font("Monospaced", Font.BOLD, 18));
            g2.setColor(Color.DARK_GRAY);
            g2.drawString(String.format("HP %d/%d", hp, maxHp), 330, 24);

            // 進度條背景
            int barX = 330, barY = 30, barW = 260, barH = 20;
            g2.setColor(new Color(60, 20, 20));
            g2.fillRoundRect(barX, barY, barW, barH, 8, 8);

            // 進度條填色（綠→黃→紅 依血量比例）
            float ratio = (float) hp / maxHp;
            int filled  = (int)(ratio * barW);
            Color barColor;
            if      (ratio > 0.6f) barColor = new Color(60, 210, 80);
            else if (ratio > 0.3f) barColor = new Color(230, 190, 30);
            else                   barColor = new Color(220, 50, 50);

            if (filled > 0) {
                g2.setColor(barColor);
                g2.fillRoundRect(barX, barY, filled, barH, 8, 8);
            }

            // 進度條外框
            g2.setColor(new Color(180, 180, 180));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(barX, barY, barW, barH, 8, 8);

            // --- 分數 ---
            g2.setFont(new Font("Monospaced", Font.BOLD, 20));
            g2.setColor(new Color(50, 50, 50));
            g2.drawString(String.format("PT: %d", playerData.getScore()), 620, 46);
        }

        // --- 右側：時間 ---
        g2.setFont(new Font("Monospaced", Font.BOLD, 24));
        g2.setColor(Color.DARK_GRAY);
        g2.drawString(String.format("⏱ %03d", elapsedTime), 870, 46);

        // --- 中央提示（通關 / Game Over）---
        if (isBigLevelCleared()) {
            g2.setFont(new Font("Arial", Font.BOLD, 26));
            g2.setColor(new Color(30, 160, 30));
            g2.drawString("STAGE CLEAR!  Press 'R' for Next Wave!", 220, 46);
        } else if (gameOver) {
            g2.setFont(new Font("Arial", Font.BOLD, 26));
            g2.setColor(new Color(200, 30, 30));
            g2.drawString("GAME OVER!  Press 'R' to Retry!", 260, 46);
        }
    }

    private boolean inBounds(int row, int col) {
        return row >= 0 && row < totalRows && col >= 0 && col < totalCols;
    }

    public static class Builder {
        private int tileSize = 64;
        private int zoneRows = 2;
        private int zoneCols = 2;
        private int boardLeft = 0;
        private int boardTop = 72;
        private int minePerZone = 10; // 每個區塊最多的地雷數

        public Builder setZoneDimensions(int zoneRows, int zoneCols, int tileSize) {
            this.zoneRows = zoneRows;
            this.zoneCols = zoneCols;
            this.tileSize = tileSize;
            return this;
        }

        public Builder setMapByPixels(int totalPixelW, int totalPixelH, int tileSize) {
            this.tileSize = tileSize;
            int totalGridW = totalPixelW / tileSize;
            int totalGridH = (totalPixelH - boardTop) / tileSize;
            this.zoneCols = Math.max(1, totalGridW / ZONE_SIZE);
            this.zoneRows = Math.max(1, totalGridH / ZONE_SIZE);
            return this;
        }

        public Builder setBoardOffset(int left, int top) {
            this.boardLeft = left;
            this.boardTop = top;
            return this;
        }

        public Builder setMinesPerZone(int count) {
            this.minePerZone = count;
            return this;
        }

        public MineBoard build() {
            return new MineBoard(this);
        }
    }
}