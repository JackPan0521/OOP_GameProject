public class PlayerData {
    private int score = 0;

    // === HP ===
    private int maxHp = 100;
    private int currentHp = 100;

    // === 防禦 ===
    private int defense = 0;

    // === 移動速度 ===
    private int speed = 10;

    // -------------------------------------------------------
    // 技能系統
    // skill[0] = 抵禦爆炸   (解鎖後按 Z 啟動，用完或過關消耗)
    // skill[1] = 自動標示   (解鎖後按 X 啟動，標示最近3顆炸彈)
    // skill[2] = 經驗加倍   (解鎖後按 C 啟動，本關獎勵×2)
    // -------------------------------------------------------
    private boolean[] skillUnlocked = new boolean[3]; // 是否已購買解鎖
    private boolean   shieldActive  = false;          // 技能0：防爆護盾是否啟動中
    private boolean   expBoostActive = false;         // 技能2：經驗加倍是否啟動中

    // -------------------------------------------------------
    // 升級費用
    // -------------------------------------------------------
    public int getHpUpgradeCost()      { return maxHp * 2; }
    public int getDefUpgradeCost()     { return (defense + 1) * 100; }
    public int getSpeedUpgradeCost()   { return (speed - 9) * 150; }
    public int getSkillUnlockCost(int i) {
        switch (i) {
            case 0: return 300;  // 防爆護盾
            case 1: return 200;  // 自動標示
            case 2: return 250;  // 經驗加倍
            default: return 9999;
        }
    }

    // -------------------------------------------------------
    // 屬性升級
    // -------------------------------------------------------
    public boolean upgradeHp() {
        if (score < getHpUpgradeCost()) return false;
        score -= getHpUpgradeCost();
        maxHp += 20;
        currentHp = maxHp;
        return true;
    }

    public boolean upgradeDefense() {
        if (score < getDefUpgradeCost()) return false;
        score -= getDefUpgradeCost();
        defense += 1;
        return true;
    }

    public boolean upgradeSpeed() {
        if (speed >= 20) return false;
        if (score < getSpeedUpgradeCost()) return false;
        score -= getSpeedUpgradeCost();
        speed += 1;
        return true;
    }

    // -------------------------------------------------------
    // 技能解鎖（商店購買）
    // -------------------------------------------------------
    public boolean unlockSkill(int i) {
        if (i < 0 || i >= 3) return false;
        if (skillUnlocked[i]) return false;           // 已解鎖不能再買
        if (score < getSkillUnlockCost(i)) return false;
        score -= getSkillUnlockCost(i);
        skillUnlocked[i] = true;
        return true;
    }

    public boolean isSkillUnlocked(int i) {
        return (i >= 0 && i < 3) && skillUnlocked[i];
    }

    // -------------------------------------------------------
    // 技能啟動（玩家按鍵觸發）
    // -------------------------------------------------------

    /** 技能0：啟動防爆護盾（解鎖後才能用）*/
    public boolean activateShield() {
        if (!skillUnlocked[0] || shieldActive) return false;
        shieldActive = true;
        return true;
    }

    /** 技能0：護盾是否啟動中 */
    public boolean isShieldActive() { return shieldActive; }

    /** 技能0：消耗護盾（被炸到或過關時呼叫）*/
    public void consumeShield() { shieldActive = false; }

    /** 技能2：啟動經驗加倍 */
    public boolean activateExpBoost() {
        if (!skillUnlocked[2] || expBoostActive) return false;
        expBoostActive = true;
        return true;
    }

    /** 技能2：是否加倍中 */
    public boolean isExpBoostActive() { return expBoostActive; }

    /** 技能2：關卡結束後重置（每關只能用一次）*/
    public void resetExpBoost() { expBoostActive = false; }

    // -------------------------------------------------------
    // 扣血（護盾攔截）
    // -------------------------------------------------------
    public boolean takeDamage(int wave) {
        // 護盾啟動時攔截一次爆炸，不扣血並消耗護盾
        if (shieldActive) {
            consumeShield();
            return false; // false = 沒有真正受傷
        }
        int baseDmg  = (int)(maxHp * 0.25);
        int waveBonus = wave * 5;
        int finalDmg  = Math.max(10, (baseDmg + waveBonus) - defense);
        currentHp -= finalDmg;
        if (currentHp < 0) currentHp = 0;
        return true; // true = 真的扣了血
    }

    public void heal(int amount) {
        currentHp = Math.min(currentHp + amount, maxHp);
    }

    public void resetHp() { currentHp = maxHp; }

    // -------------------------------------------------------
    // Getters / Setters
    // -------------------------------------------------------
    public int  getScore()       { return score; }
    public void addScore(int n)  { score += n; }
    public int  getCurrentHp()  { return currentHp; }
    public int  getMaxHp()      { return maxHp; }
    public int  getDefense()    { return defense; }
    public int  getSpeed()      { return speed; }
    public boolean isDead()     { return currentHp <= 0; }
}