import game.framework.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.Rectangle;

public class ShopRole implements Role, KeyListener {

    private static final int W = 1080;
    private static final int H = 720;

    private boolean open = false;
    private PlayerData playerData;
    private MyRole player;

    // 選項索引：0=HP, 1=防禦, 2=速度, 3=技能(地雷提示)
    private int selected = 0;
    private static final int ITEM_COUNT = 4;

    private static final String[] LABELS = {
        "HP UP         (+20 HP, 回滿血)",
        "DEFENSE UP    (+1 防禦)",
        "SPEED UP      (+1 移動速度)",
        "SKILL: 地雷提示 (升級提示範圍)"
    };

    // --- 購買回饋訊息 ---
    private String feedbackMsg   = "";
    private boolean feedbackGood = true;   // true=成功(綠), false=失敗(紅)
    private int     feedbackTick = 0;      // 剩餘顯示幀數
    private static final int FEEDBACK_DURATION = 90; // 約 1.5 秒（60fps）

    public ShopRole(PlayerData playerData, MyRole player) {
        this.playerData = playerData;
        this.player     = player;
    }

    public boolean isOpen() { return open; }

    // -------------------------------------------------------
    // Role 介面
    // -------------------------------------------------------
    @Override public void getReady() {}
    @Override public void end() {}
    @Override public Model getModel() { return null; }
    @Override public Effect conflict(Role r, Rectangle rec) { return null; }

    @Override
    public void run() {
        if (open && player != null) {
            player.setDX(0);
            player.setDY(0);
        }
        // 回饋訊息倒數
        if (feedbackTick > 0) feedbackTick--;
    }

    @Override
    public void display(Graphics g) {
        if (!open) return;

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // === 半透明遮罩 ===
        g2.setColor(new Color(0, 0, 0, 210));
        g2.fillRect(0, 0, W, H);

        int panelX = 100, panelY = 60, panelW = 880, panelH = 600;

        // === 主面板 ===
        g2.setColor(new Color(20, 24, 54, 245));
        g2.fillRoundRect(panelX, panelY, panelW, panelH, 28, 28);
        g2.setColor(new Color(100, 140, 255));
        g2.setStroke(new BasicStroke(2.5f));
        g2.drawRoundRect(panelX, panelY, panelW, panelH, 28, 28);

        // === 標題列 ===
        g2.setColor(new Color(40, 48, 100));
        g2.fillRoundRect(panelX, panelY, panelW, 64, 28, 28);
        g2.fillRect(panelX, panelY + 36, panelW, 28);

        g2.setFont(new Font("Monospaced", Font.BOLD, 32));
        g2.setColor(new Color(255, 215, 50));
        g2.drawString("★  S H O P  ★", panelX + 300, panelY + 44);

        // === 左欄：玩家數值面板 ===
        int leftX = panelX + 30;
        int infoY = panelY + 90;

        // 關卡
        int wave = (player != null) ? player.getWave() : 1;
        g2.setFont(new Font("Monospaced", Font.BOLD, 20));
        g2.setColor(new Color(255, 215, 50));
        g2.drawString(String.format("WAVE  %d", wave), leftX, infoY);

        // 數值區塊背景
        g2.setColor(new Color(255, 255, 255, 15));
        g2.fillRoundRect(leftX - 10, infoY + 10, 340, 190, 12, 12);
        g2.setColor(new Color(100, 140, 255, 60));
        g2.setStroke(new BasicStroke(1));
        g2.drawRoundRect(leftX - 10, infoY + 10, 340, 190, 12, 12);

        // HP Bar
        drawStatRow(g2, leftX, infoY + 40,  "HP",
            String.format("%d / %d", playerData.getCurrentHp(), playerData.getMaxHp()),
            playerData.getCurrentHp(), playerData.getMaxHp(),
            new Color(80, 220, 100));

        // DEF
        drawStatRow(g2, leftX, infoY + 90,  "DEF",
            String.format("%d", playerData.getDefense()),
            playerData.getDefense(), 10,
            new Color(100, 180, 255));

        // SPD
        drawStatRow(g2, leftX, infoY + 140, "SPD",
            String.format("%d / 20", playerData.getSpeed()),
            playerData.getSpeed() - 10, 10,
            new Color(255, 200, 60));

        // SKILL
        int skillLv = playerData.getSkillLevel(0);
        g2.setFont(new Font("Monospaced", Font.BOLD, 16));
        g2.setColor(new Color(180, 220, 255));
        g2.drawString(String.format("SKILL  Lv.%d", skillLv), leftX, infoY + 190);

        // 分數（大字）
        g2.setFont(new Font("Monospaced", Font.BOLD, 26));
        g2.setColor(Color.WHITE);
        g2.drawString(String.format("SCORE: %d pt", playerData.getScore()), leftX, infoY + 235);

        // === 分隔線 ===
        g2.setColor(new Color(100, 140, 255, 80));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawLine(panelX + 390, panelY + 80, panelX + 390, panelY + panelH - 50);

        // === 右欄：商品列表 ===
        int[] costs = {
            playerData.getHpUpgradeCost(),
            playerData.getDefUpgradeCost(),
            playerData.getSpeedUpgradeCost(),
            playerData.getSkillUpgradeCost(0)
        };
        boolean[] canBuy = {
            playerData.getScore() >= costs[0],
            playerData.getScore() >= costs[1],
            playerData.getScore() >= costs[2] && playerData.getSpeed() < 20,
            playerData.getScore() >= costs[3]
        };

        int itemStartY = panelY + 100;
        int itemH      = 105;
        int rightX     = panelX + 410;
        int rightW     = panelW - 420;

        for (int i = 0; i < ITEM_COUNT; i++) {
            boolean isSelected = (i == selected);
            int iy = itemStartY + i * itemH;

            // 選中背景
            if (isSelected) {
                g2.setColor(new Color(70, 95, 200, 150));
                g2.fillRoundRect(rightX - 8, iy - 24, rightW, itemH - 10, 14, 14);
                g2.setColor(new Color(140, 180, 255));
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(rightX - 8, iy - 24, rightW, itemH - 10, 14, 14);
            }

            // 箭頭
            g2.setFont(new Font("Monospaced", Font.BOLD, 20));
            g2.setColor(isSelected ? new Color(255, 215, 50) : new Color(80, 80, 100));
            g2.drawString(isSelected ? "▶" : "  ", rightX, iy);

            // 名稱
            g2.setFont(new Font("Monospaced", Font.BOLD, 18));
            g2.setColor(canBuy[i] ? Color.WHITE : new Color(110, 110, 115));
            g2.drawString(LABELS[i], rightX + 28, iy);

            // 費用 pill
            String costStr = costs[i] + " pt";
            Color pillColor = canBuy[i] ? new Color(255, 200, 40) : new Color(160, 60, 60);
            g2.setFont(new Font("Monospaced", Font.BOLD, 15));
            FontMetrics fm = g2.getFontMetrics();
            int pillW = fm.stringWidth(costStr) + 20;
            g2.setColor(canBuy[i] ? new Color(80, 65, 10, 180) : new Color(80, 20, 20, 180));
            g2.fillRoundRect(rightX + 28, iy + 10, pillW, 24, 10, 10);
            g2.setColor(pillColor);
            g2.setStroke(new BasicStroke(1));
            g2.drawRoundRect(rightX + 28, iy + 10, pillW, 24, 10, 10);
            g2.drawString(costStr, rightX + 38, iy + 27);
        }

        // === 購買回饋訊息 ===
        if (feedbackTick > 0) {
            float alpha = Math.min(1f, feedbackTick / 20f); // 最後 20 幀淡出
            Color msgColor = feedbackGood
                ? new Color(80, 255, 120, (int)(alpha * 255))
                : new Color(255, 80,  80,  (int)(alpha * 255));

            g2.setFont(new Font("Monospaced", Font.BOLD, 22));
            g2.setColor(msgColor);
            FontMetrics fm = g2.getFontMetrics();
            int msgX = panelX + (panelW - fm.stringWidth(feedbackMsg)) / 2;
            g2.drawString(feedbackMsg, msgX, panelY + panelH - 55);
        }

        // === 操作提示 ===
        g2.setFont(new Font("Monospaced", Font.PLAIN, 14));
        g2.setColor(new Color(130, 130, 140));
        g2.drawString("↑ ↓ 選擇    ENTER 購買    S 關閉", panelX + 330, panelY + panelH - 22);
    }

    // 畫一列數值 + 進度條
    private void drawStatRow(Graphics2D g2, int x, int y,
                              String label, String valueStr,
                              int current, int max, Color barColor) {
        g2.setFont(new Font("Monospaced", Font.BOLD, 16));
        g2.setColor(new Color(160, 180, 220));
        g2.drawString(label, x, y);

        g2.setColor(Color.WHITE);
        g2.drawString(valueStr, x + 60, y);

        // 進度條底
        int barX = x, barY = y + 5, barW = 300, barH = 10;
        g2.setColor(new Color(50, 50, 70));
        g2.fillRoundRect(barX, barY, barW, barH, 6, 6);

        // 進度條填色
        int filled = (max > 0) ? (int)((float) Math.min(current, max) / max * barW) : 0;
        if (filled > 0) {
            g2.setColor(barColor);
            g2.fillRoundRect(barX, barY, filled, barH, 6, 6);
        }
    }

    // -------------------------------------------------------
    // 按鍵處理
    // -------------------------------------------------------
    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyReleased(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();

        if (code == KeyEvent.VK_S) {
            open = !open;
            return;
        }

        if (!open) return;

        switch (code) {
            case KeyEvent.VK_UP:
                selected = (selected - 1 + ITEM_COUNT) % ITEM_COUNT;
                break;
            case KeyEvent.VK_DOWN:
                selected = (selected + 1) % ITEM_COUNT;
                break;
            case KeyEvent.VK_ENTER:
                purchase(selected);
                break;
            default:
                break;
        }
    }

    private void purchase(int index) {
        boolean success = false;
        String  itemName = LABELS[index].split("\\(")[0].trim();

        switch (index) {
            case 0: success = playerData.upgradeHp();      break;
            case 1: success = playerData.upgradeDefense(); break;
            case 2:
                success = playerData.upgradeSpeed();
                if (success && player != null) player.setSpeed(playerData.getSpeed());
                break;
            case 3: success = playerData.upgradeSkill(0);  break;
        }

        if (success) {
            feedbackMsg  = "✔  " + itemName + " 升級成功！";
            feedbackGood = true;
        } else {
            boolean maxed = (index == 2 && playerData.getSpeed() >= 20);
            feedbackMsg  = maxed ? "✘  已達上限！" : "✘  點數不足！";
            feedbackGood = false;
        }
        feedbackTick = FEEDBACK_DURATION;
    }
}