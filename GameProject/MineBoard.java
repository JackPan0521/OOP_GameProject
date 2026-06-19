import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.io.File;
import java.util.Random;
import javax.imageio.ImageIO;

public class MineBoard {
    // 這些屬性不再寫死，改由 Builder 帶入
    private final int boardW;
    private final int boardH;
    private final int tile;
    private final int cols;
    private final int rows;

    private final int boardLeft;
    private final int boardTop;
    private final int mineCount;

    private boolean generated = false;
    private Cell[][] cells;

    private Image unopenTile;
    private Image flagTile;
    private Image boomTile;
    private Image[] numberTiles;

    // 時間與計分板變數
    private long startTime = 0;
    private int elapsedTime = 0;
    private boolean gameOver = false;
    private boolean gameWon = false;

    // 1. 將建構子設為 private，強制外部必須透過 Builder 來建立物件
    private MineBoard(Builder builder) {
        this.boardW = builder.boardW;
        this.boardH = builder.boardH;
        this.tile = builder.tile;
        this.cols = this.boardW / this.tile;
        this.rows = this.boardH / this.tile;
        this.boardLeft = builder.boardLeft;
        this.boardTop = builder.boardTop;
        this.mineCount = builder.mineCount;

        // 初始化格子陣列
        cells = new Cell[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                cells[r][c] = new Cell(r, c);
            }
        }

        numberTiles = new Image[8];

        // 載入圖片資源
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

    // 2. 建立靜態內部類別 Builder
    public static class Builder {
        // 設定預設值（如果外部沒設定，就用這套預設值）
        private int boardW = 1080;
        private int boardH = 648;
        private int tile = 108;
        private int boardLeft = 0;
        private int boardTop = 72;
        private int mineCount = 10;

        public Builder setBoardSize(int width, int height) {
            this.boardW = width;
            this.boardH = height;
            return this; // 回傳 this 以支援鏈式調用 (Chaining)
        }

        public Builder setTileSize(int tileSize) {
            this.tile = tileSize;
            return this;
        }

        public Builder setBoardOffset(int left, int top) {
            this.boardLeft = left;
            this.boardTop = top;
            return this;
        }

        public Builder setMineCount(int count) {
            this.mineCount = count;
            return this;
        }

        // 最終組裝並產出 MineBoard 物件
        public MineBoard build() {
            return new MineBoard(this);
        }
    }

    // ==========================================
    //  以下為原本的遊戲邏輯與繪製方法，保持不變
    // ==========================================

    public void openAtPixel(int px, int py) {
        int col = (px - boardLeft) / tile;
        int row = (py - boardTop) / tile;
        openAtCell(row, col);
    }

    public void openAtCell(int row, int col) {
        if (!inBounds(row, col) || gameOver) {
            return;
        }

        if (!generated) {
            generateMinesWithSafeZone(row, col);
            generated = true;
            startTime = System.currentTimeMillis();
        }

        reveal(row, col);
        checkGameStatus();
    }

    public void toggleFlagAtPixel(int px, int py) {
        int col = (px - boardLeft) / tile;
        int row = (py - boardTop) / tile;
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
            int row = random.nextInt(rows);
            int col = random.nextInt(cols);

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
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
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

        if (start.isRevealed()) {
            int adjacentMines = start.getAdjacentMines();
            if (adjacentMines > 0 && !start.isMine()) {
                int flaggedCount = 0;
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

                if (flaggedCount == adjacentMines) {
                    for (int dr = -1; dr <= 1; dr++) {
                        for (int dc = -1; dc <= 1; dc++) {
                            if (dr == 0 && dc == 0) continue;
                            int nr = row + dr;
                            int nc = col + dc;
                            if (inBounds(nr, nc)) {
                                Cell next = cells[nr][nc];
                                if (!next.isRevealed() && !next.isFlagged()) {
                                    if (next.isMine()) {
                                        next.setRevealed(true);
                                    } else if (next.getAdjacentMines() == 0) {
                                        revealConnectedSafeArea(nr, nc);
                                    } else {
                                        next.setRevealed(true);
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

    public int getFlagCount() {
        int count = 0;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (cells[r][c].isFlagged()) {
                    count++;
                }
            }
        }
        return count;
    }

    private void checkGameStatus() {
        boolean hitMine = false;
        boolean allSafeRevealed = true;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Cell cell = cells[r][c];
                if (cell.isMine() && cell.isRevealed()) {
                    hitMine = true;
                }
                if (!cell.isMine() && !cell.isRevealed()) {
                    allSafeRevealed = false;
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

    public void updateTime() {
        if (generated && !gameOver) {
            elapsedTime = (int) ((System.currentTimeMillis() - startTime) / 1000);
            if (elapsedTime > 999) {
                elapsedTime = 999;
            }
        }
    }

    public void draw(Graphics g) {
        if (unopenTile == null) {
            return;
        }

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Cell cell = cells[r][c];
                int x = boardLeft + c * tile;
                int y = boardTop + r * tile;

                if (!cell.isRevealed()) {
                    if (cell.isFlagged() && flagTile != null) {
                        g.drawImage(flagTile, x, y, tile, tile, null);
                    } else {
                        g.drawImage(unopenTile, x, y, tile, tile, null);
                    }
                } else {
                    if (cell.isMine()) {
                        if (boomTile != null) {
                            g.drawImage(boomTile, x, y, tile, tile, null);
                        }
                    } else {
                        int n = cell.getAdjacentMines();
                        if (n >= 0 && n <= 7 && numberTiles[n] != null) {
                            g.drawImage(numberTiles[n], x, y, tile, tile, null);
                        } else {
                            g.drawImage(unopenTile, x, y, tile, tile, null);
                        }
                    }
                }
            }
        }

        updateTime();

        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(0, 0, boardW, boardTop);
        g.setColor(Color.DARK_GRAY);
        g.drawRect(0, 0, boardW - 1, boardTop - 1);

        g.setFont(new Font("Monospaced", Font.BOLD, 30));

        int remainingMines = mineCount - getFlagCount(); 
        g.setColor(Color.RED);
        g.drawString(String.format("MINES: %03d", remainingMines), 50, 45);

        g.setColor(Color.BLACK);
        g.drawString(String.format("TIME: %03d", elapsedTime), boardW - 250, 45);

        if (gameOver) {
            g.setFont(new Font("Arial", Font.BOLD, 35));
            if (gameWon) {
                g.setColor(Color.GREEN);
                g.drawString("YOU WIN!", boardW / 2 - 80, 48);
            } else {
                g.setColor(Color.RED);
                g.drawString("GAME OVER", boardW / 2 - 100, 48);
            }
        }
    }

    private boolean inBounds(int row, int col) {
        return row >= 0 && row < rows && col >= 0 && col < cols;
    }
}