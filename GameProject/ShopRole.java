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

    // 選項索引：0=HP, 1=防禦, 2=速度, 3=技能0, 4=技能1, 5=技能2
    private int selected = 0;
    private static final int ITEM_COUNT = 6;

    private static final String[] LABELS = {
        "HP UP         (+20 HP, 回滿血)",
        "DEFENSE UP    (+1 防禦)",
        "SPEED UP      (+1 移動速度)",
        "[Z] 防爆護盾  (抵禦一次爆炸)",
        "[X] 自動標示  (標示最近3顆炸彈)",
        "[C] 經驗加倍  (本關獎勵×2)"
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

        // 技能狀態
        g2.setFont(new Font("Monospaced", Font.BOLD, 15));
        g2.setColor(playerData.isSkillUnlocked(0)
            ? (playerData.isShieldActive() ? new Color(100, 255, 120) : new Color(180, 220, 255))
            : new Color(90, 90, 100));
        g2.drawString("[Z] 護盾 " + (playerData.isShieldActive() ? "●" : (playerData.isSkillUnlocked(0) ? "○" : "🔒")), leftX, infoY + 190);

        g2.setColor(playerData.isSkillUnlocked(1) ? new Color(180, 220, 255) : new Color(90, 90, 100));
        g2.drawString("[X] 標示 " + (playerData.isSkillUnlocked(1) ? "○" : "🔒"), leftX + 170, infoY + 190);

        g2.setColor(playerData.isSkillUnlocked(2)
            ? (playerData.isExpBoostActive() ? new Color(255, 220, 60) : new Color(180, 220, 255))
            : new Color(90, 90, 100));
        g2.drawString("[C] 加倍 " + (playerData.isExpBoostActive() ? "●" : (playerData.isSkillUnlocked(2) ? "○" : "🔒")), leftX, infoY + 215);

        // 分數（大字）
        g2.setFont(new Font("Monospaced", Font.BOLD, 26));
        g2.setColor(Color.WHITE);
        g2.drawString(String.format("SCORE: %d pt", playerData.getScore()), leftX, infoY + 255);

        // === 分隔線 ===
        g2.setColor(new Color(100, 140, 255, 80));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawLine(panelX + 390, panelY + 80, panelX + 390, panelY + panelH - 50);

        // === 右欄：商品列表 ===
        int[] costs = {
            playerData.getHpUpgradeCost(),
            playerData.getDefUpgradeCost(),
            playerData.getSpeedUpgradeCost(),
            playerData.getSkillUnlockCost(0),
            playerData.getSkillUnlockCost(1),
            playerData.getSkillUnlockCost(2)
        };
        boolean[] canBuy = {
            playerData.getScore() >= costs[0],
            playerData.getScore() >= costs[1],
            playerData.getScore() >= costs[2] && playerData.getSpeed() < 20,
            playerData.getScore() >= costs[3] && !playerData.isSkillUnlocked(0),
            playerData.getScore() >= costs[4] && !playerData.isSkillUnlocked(1),
            playerData.getScore() >= costs[5] && !playerData.isSkillUnlocked(2)
        };
        // 技能已解鎖時顯示狀態標籤
        String[] statusTag = {
            "",
            "",
            playerData.getSpeed() >= 20 ? " [MAX]" : "",
            playerData.isSkillUnlocked(0) ? (playerData.isShieldActive() ? " [啟動中]" : " [已解鎖]") : "",
            playerData.isSkillUnlocked(1) ? " [已解鎖]" : "",
            playerData.isSkillUnlocked(2) ? (playerData.isExpBoostActive() ? " [啟動中]" : " [已解鎖]") : ""
        };

        int itemStartY = panelY + 100;
        int itemH      = 78;   // 6個項目壓縮一點
        int rightX     = panelX + 410;
        int rightW     = panelW - 420;

        for (int i = 0; i < ITEM_COUNT; i++) {
            boolean isSelected = (i == selected);
            int iy = itemStartY + i * itemH;

            // 選中背景
            if (isSelected) {
                g2.setColor(new Color(70, 95, 200, 150));
                g2.fillRoundRect(rightX - 8, iy - 22, rightW, itemH - 8, 14, 14);
                g2.setColor(new Color(140, 180, 255));
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(rightX - 8, iy - 22, rightW, itemH - 8, 14, 14);
            }

            // 技能分隔線（在第3個項目前畫一條線）
            if (i == 3) {
                g2.setColor(new Color(100, 140, 255, 60));
                g2.setStroke(new BasicStroke(1));
                g2.drawLine(rightX - 8, iy - 26, rightX + rightW - 8, iy - 26);
                g2.setFont(new Font("Monospaced", Font.BOLD, 13));
                g2.setColor(new Color(160, 160, 180));
                g2.drawString("── 技能（一次性解鎖）──", rightX, iy - 14);
            }

            // 箭頭
            g2.setFont(new Font("Monospaced", Font.BOLD, 18));
            g2.setColor(isSelected ? new Color(255, 215, 50) : new Color(80, 80, 100));
            g2.drawString(isSelected ? "▶" : "  ", rightX, iy);

            // 名稱 + 狀態標籤
            g2.setFont(new Font("Monospaced", Font.BOLD, 16));
            boolean unlocked = (i >= 3 && playerData.isSkillUnlocked(i - 3));
            g2.setColor(unlocked ? new Color(120, 220, 120)
                       : canBuy[i] ? Color.WHITE
                       : new Color(110, 110, 115));
            g2.drawString(LABELS[i] + statusTag[i], rightX + 26, iy);

            // 費用 pill（已解鎖技能不顯示費用）
            if (!unlocked) {
                String costStr = costs[i] + " pt";
                boolean affordable = canBuy[i];
                g2.setFont(new Font("Monospaced", Font.BOLD, 13));
                FontMetrics fm = g2.getFontMetrics();
                int pillW = fm.stringWidth(costStr) + 16;
                g2.setColor(affordable ? new Color(80, 65, 10, 180) : new Color(80, 20, 20, 180));
                g2.fillRoundRect(rightX + 26, iy + 8, pillW, 20, 8, 8);
                g2.setColor(affordable ? new Color(255, 200, 40) : new Color(160, 60, 60));
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(rightX + 26, iy + 8, pillW, 20, 8, 8);
                g2.drawString(costStr, rightX + 34, iy + 23);
            }
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
        g2.drawString("↑ ↓ 選擇    ENTER 購買    S 關閉   |   Z/X/C 啟動技能", panelX + 200, panelY + panelH - 22);
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
            case 3: success = playerData.unlockSkill(0); break;
            case 4: success = playerData.unlockSkill(1); break;
            case 5: success = playerData.unlockSkill(2); break;
        }

        if (success) {
            feedbackMsg  = "✔  " + itemName + (index >= 3 ? " 解鎖！" : " 升級成功！");
            feedbackGood = true;
        } else {
            boolean alreadyUnlocked = index >= 3 && playerData.isSkillUnlocked(index - 3);
            boolean maxed = (index == 2 && playerData.getSpeed() >= 20);
            if (alreadyUnlocked)   feedbackMsg = "✘  已解鎖！";
            else if (maxed)        feedbackMsg = "✘  已達上限！";
            else                   feedbackMsg = "✘  點數不足！";
            feedbackGood = false;
        }
        feedbackTick = FEEDBACK_DURATION;
    }
}