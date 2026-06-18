import java.awt.Graphics;
import java.awt.Image;
import java.io.File;
import java.util.Random;
import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;

public class MineBoard {
    private final int BOARD_W = 1080;
    private final int BOARD_H = 648;
    private final int TILE = 108;
    private final int COLS = BOARD_W / TILE;
    private final int ROWS = BOARD_H / TILE;

    private final int boardLeft = 0;
    private final int boardTop = 72;

    private final int mineCount = 10;
    private boolean generated = false;

    private Cell[][] cells;

    private Image unopenTile;
    private Image flagTile;
    private Image boomTile;
    private Image[] numberTiles;

    // 在 MineBoard 類別中新增以下欄位 (約在 cells 宣告的附近)
    private long startTime = 0;
    private int elapsedTime = 0; // 經過的秒數
    private boolean gameOver = false; // 遊戲結束標記（踩雷或獲勝時停止計時）
    private boolean gameWon = false;

    public MineBoard() {
        cells = new Cell[ROWS][COLS];
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                cells[r][c] = new Cell(r, c);
            }
        }

        numberTiles = new Image[8];

        try {
            unopenTile = ImageIO.read(new File("src/minesweeper icon/unopen.png"));
            flagTile = ImageIO.read(new File("src/minesweeper icon/flag.png"));
            boomTile = ImageIO.read(new File("src/minesweeper icon/boom.png"));

            for (int i = 0; i <= 7; i++) {
                numberTiles[i] = ImageIO.read(new File("src/minesweeper icon/" + i + ".png"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 新增一個檢查遊戲狀態的方法
    private void checkGameStatus() {
        boolean hitMine = false;
        boolean allSafeRevealed = true;

        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                Cell cell = cells[r][c];
                if (cell.isMine() && cell.isRevealed()) {
                    hitMine = true; // 踩到雷了
                }
                if (!cell.isMine() && !cell.isRevealed()) {
                    allSafeRevealed = false; // 還有安全格子沒被翻開
                }
            }
        }

        if (hitMine) {
            gameOver = true;
        } else if (allSafeRevealed) {
            gameOver = true;
            gameWon = true;
        }
    }

    // 修改或新增一個更新時間的方法（這個會在每幀被呼叫）
    public void updateTime() {
        if (generated && !gameOver) {
            elapsedTime = (int) ((System.currentTimeMillis() - startTime) / 1000);
            // 傳統踩地雷最多顯示到 999 秒
            if (elapsedTime > 999) {
                elapsedTime = 999;
            }
        }
    }


    public void openAtPixel(int px, int py) {
        int col = (px - boardLeft) / TILE;
        int row = (py - boardTop) / TILE;
        openAtCell(row, col);
    }

    // 修改 openAtCell 方法，在第一次點擊時啟動計時器
    public void openAtCell(int row, int col) {
        if (!inBounds(row, col) || gameOver) {
            return;
        }

        if (!generated) {
            generateMinesWithSafeZone(row, col);
            generated = true;
            startTime = System.currentTimeMillis(); // --- 新增：記錄開始時間 ---
        }

        reveal(row, col);
        checkGameStatus(); // --- 新增：每次翻開後檢查輸贏 ---
    }

    // 新增一個方法來實時計算目前地圖上有幾支旗子
    public int getFlagCount() {
        int count = 0;
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (cells[r][c].isFlagged()) {
                    count++;
                }
            }
        }
        return count;
    }

    public void toggleFlagAtPixel(int px, int py) {
        int col = (px - boardLeft) / TILE;
        int row = (py - boardTop) / TILE;
        toggleFlag(row, col);
    }

    public void toggleFlag(int row, int col) {
        if (!inBounds(row, col)) {
            return;
        }

        Cell cell = cells[row][col];
        if (cell.isRevealed()) {
            return;
        }

        cell.setFlagged(!cell.isFlagged());
    }

    private void generateMinesWithSafeZone(int safeRow, int safeCol) {
        Random random = new Random();
        int placed = 0;

        while (placed < mineCount) {
            int row = random.nextInt(ROWS);
            int col = random.nextInt(COLS);

            if (cells[row][col].isMine()) {
                continue;
            }

            if (isInSafeZone(row, col, safeRow, safeCol)) {
                continue;
            }

            cells[row][col].setMine(true);
            placed++;
        }

        computeAdjacentCounts();
    }

    private boolean isInSafeZone(int row, int col, int safeRow, int safeCol) {
        return Math.abs(row - safeRow) <= 1 && Math.abs(col - safeCol) <= 1;
    }

    private void computeAdjacentCounts() {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (cells[r][c].isMine()) {
                    continue;
                }

                int count = 0;
                for (int dr = -1; dr <= 1; dr++) {
                    for (int dc = -1; dc <= 1; dc++) {
                        if (dr == 0 && dc == 0) {
                            continue;
                        }
                        int nr = r + dr;
                        int nc = c + dc;
                        if (inBounds(nr, nc) && cells[nr][nc].isMine()) {
                            count++;
                        }
                    }
                }
                cells[r][c].setAdjacentMines(count);
            }
        }
    }

    public void reveal(int row, int col) {
        if (!inBounds(row, col)) {
            return;
        }

        Cell start = cells[row][col];
        if (start.isFlagged()) {
            return;
        }

        // --- 新增：如果這一格已經翻開了，觸發雙擊（Chording）邏輯 ---
        if (start.isRevealed()) {
            int adjacentMines = start.getAdjacentMines();
            // 只有大於 0 的數字格才需要處理雙擊
            if (adjacentMines > 0 && !start.isMine()) {
                int flaggedCount = 0;
                
                // 1. 先計算周圍九宮格內被玩家插旗的數量
                for (int dr = -1; dr <= 1; dr++) {
                    for (int dc = -1; dc <= 1; dc++) {
                        if (dr == 0 && dc == 0) continue;
                        int nr = row + dr;
                        int nc = col + dc;
                        if (inBounds(nr, nc) && cells[nr][nc].isFlagged()) {
                            flaggedCount++;
                        }
                    }
                }

                // 2. 如果插旗數量等於該格顯示的數字，就把周圍其他沒插旗的格子通通翻開
                if (flaggedCount == adjacentMines) {
                    for (int dr = -1; dr <= 1; dr++) {
                        for (int dc = -1; dc <= 1; dc++) {
                            if (dr == 0 && dc == 0) continue;
                            int nr = row + dr;
                            int nc = col + dc;
                            if (inBounds(nr, nc)) {
                                Cell next = cells[nr][nc];
                                // 只有沒翻開且沒插旗的格子才進行翻開
                                if (!next.isRevealed() && !next.isFlagged()) {
                                    // 這裡直接呼叫 reveal，如果翻到安全格（數字0）會自動觸發你原本寫好的擴展
                                    if (next.isMine()) {
                                        next.setRevealed(true); // 踩到雷
                                    } else if (next.getAdjacentMines() == 0) {
                                        revealConnectedSafeArea(nr, nc); // 觸發連鎖空白
                                    } else {
                                        next.setRevealed(true); // 普通數字格
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return;
        }

        if (start.isMine()) {
            start.setRevealed(true);
            return;
        }

        revealConnectedSafeArea(row, col);
    }

    private void revealConnectedSafeArea(int row, int col) {
        java.util.ArrayDeque<int[]> queue = new java.util.ArrayDeque<>();
        queue.add(new int[] { row, col });

        while (!queue.isEmpty()) {
            int[] pos = queue.removeFirst();
            int r = pos[0];
            int c = pos[1];

            if (!inBounds(r, c)) {
                continue;
            }

            Cell cell = cells[r][c];
            if (cell.isRevealed() || cell.isFlagged()) {
                continue;
            }

            if (cell.isMine()) {
                continue;
            }

            cell.setRevealed(true);

            if (cell.getAdjacentMines() == 0) {
                for (int dr = -1; dr <= 1; dr++) {
                    for (int dc = -1; dc <= 1; dc++) {
                        if (dr == 0 && dc == 0) {
                            continue;
                        }
                        int nr = r + dr;
                        int nc = c + dc;
                        if (inBounds(nr, nc)) {
                            Cell next = cells[nr][nc];
                            if (!next.isRevealed() && !next.isFlagged() && !next.isMine()) {
                                queue.add(new int[] { nr, nc });
                            }
                        }
                    }
                }
            }
        }
    }

    public void draw(Graphics g) {
        if (unopenTile == null) {
            return;
        }

        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                Cell cell = cells[r][c];
                int x = boardLeft + c * TILE;
                int y = boardTop + r * TILE;

                if (!cell.isRevealed()) {
                    if (cell.isFlagged() && flagTile != null) {
                        g.drawImage(flagTile, x, y, TILE, TILE, null);
                    } else {
                        g.drawImage(unopenTile, x, y, TILE, TILE, null);
                    }
                } else {
                    if (cell.isMine()) {
                        if (boomTile != null) {
                            g.drawImage(boomTile, x, y, TILE, TILE, null);
                        }
                    } else {
                        int n = cell.getAdjacentMines();
                        if (n >= 0 && n <= 7 && numberTiles[n] != null) {
                            g.drawImage(numberTiles[n], x, y, TILE, TILE, null);
                        } else {
                            g.drawImage(unopenTile, x, y, TILE, TILE, null);
                        }
                    }
                }
            }
        }
        updateTime(); // 每次重繪時更新時間數據

        // 1. 繪製計分板背景
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(0, 0, BOARD_W, boardTop);
        g.setColor(Color.DARK_GRAY);
        g.drawRect(0, 0, BOARD_W - 1, boardTop - 1);

        // 2. 設定文字字型
        g.setFont(new Font("Monospaced", Font.BOLD, 30));

        // 3. 繪製剩餘雷數 (左側)
        // 剩餘雷數 = 總雷數 - 已插旗數
        int remainingMines = mineCount - getFlagCount(); 
        g.setColor(Color.RED);
        g.drawString(String.format("MINES: %03d", remainingMines), 50, 45);

        // 4. 繪製時間 (右側)
        g.setColor(Color.BLACK);
        g.drawString(String.format("TIME: %03d", elapsedTime), BOARD_W - 250, 45);

        // 5. 繪製遊戲結束/獲勝狀態提示 (中間)
        if (gameOver) {
            g.setFont(new Font("Arial", Font.BOLD, 35));
            if (gameWon) {
                g.setColor(Color.GREEN);
                g.drawString("YOU WIN!", BOARD_W / 2 - 80, 48);
            } else {
                g.setColor(Color.RED);
                g.drawString("GAME OVER", BOARD_W / 2 - 100, 48);
            }
        }
    }

    public void pressAtPixel(int px, int py) {
        int col = (px - boardLeft) / TILE;
        int row = (py - boardTop) / TILE;

        if (!inBounds(row, col)) {
            return;
        }

        if (!generated) {
            generateMinesWithSafeZone(row, col);
            generated = true;
            reveal(row, col);
            return;
        }

        Cell cell = cells[row][col];

        if (!cell.isRevealed()) {
            reveal(row, col);
            return;
        }

        if (cell.isMine()) {
            return;
        }

        if (cell.getAdjacentMines() > 0) {
            int flagCount = countAdjacentFlags(row, col);
            if (flagCount == cell.getAdjacentMines()) {
                openAround(row, col);
            }
        }
    }

    private int countAdjacentFlags(int row, int col) {
        int count = 0;
    
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) {
                    continue;
                }
    
                int nr = row + dr;
                int nc = col + dc;
    
                if (inBounds(nr, nc) && cells[nr][nc].isFlagged()) {
                    count++;
                }
            }
        }
    
        return count;
    }
    
    private void openAround(int row, int col) {
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) {
                    continue;
                }
    
                int nr = row + dr;
                int nc = col + dc;
    
                if (!inBounds(nr, nc)) {
                    continue;
                }
    
                Cell next = cells[nr][nc];
                if (!next.isRevealed() && !next.isFlagged()) {
                    reveal(nr, nc);
                }
            }
        }
    }

    private boolean inBounds(int row, int col) {
        return row >= 0 && row < ROWS && col >= 0 && col < COLS;
    }
}