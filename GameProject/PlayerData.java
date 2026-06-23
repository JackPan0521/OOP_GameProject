public class PlayerData {
    private int score = 0;

    // === HP ===
    private int maxHp = 100;
    private int currentHp = 100;

    // === 防禦 ===
    private int defense = 0;

    // === 移動速度（初始值，實際套用在 MyRole） ===
    private int speed = 10;

    // === 特殊技能槽（未來擴充用） ===
    // skill[0] = 提示地雷位置  (level 0 = 未解鎖, 1+ = 已解鎖/升級)
    private int[] skillLevel = new int[4]; // 預留 4 個技能槽

    // -------------------------------------------------------
    // 升級費用
    // -------------------------------------------------------
    public int getHpUpgradeCost()    { return maxHp * 2; }
    public int getDefUpgradeCost()   { return (defense + 1) * 100; }
    public int getSpeedUpgradeCost() { return (speed - 9) * 150; }  // 10→11 cost 150, 11→12 cost 300 ...

    // -------------------------------------------------------
    // 升級動作
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
        if (speed >= 20) return false;          // 速度上限
        if (score < getSpeedUpgradeCost()) return false;
        score -= getSpeedUpgradeCost();
        speed += 1;
        return true;
    }

    // -------------------------------------------------------
    // 特殊技能（預留介面，未來填入邏輯）
    // skillIndex: 0 = 地雷提示, 1~3 = 保留
    // -------------------------------------------------------
    public int getSkillUpgradeCost(int skillIndex) {
        return (skillLevel[skillIndex] + 1) * 200;
    }

    public boolean upgradeSkill(int skillIndex) {
        if (skillIndex < 0 || skillIndex >= skillLevel.length) return false;
        if (score < getSkillUpgradeCost(skillIndex)) return false;
        score -= getSkillUpgradeCost(skillIndex);
        skillLevel[skillIndex]++;
        return true;
    }

    public int getSkillLevel(int skillIndex) {
        if (skillIndex < 0 || skillIndex >= skillLevel.length) return 0;
        return skillLevel[skillIndex];
    }

    // -------------------------------------------------------
    // 扣血（考慮防禦減傷）
    // -------------------------------------------------------
    public void takeDamage(int wave) {
        // 1. 計算基礎比例傷害 (最大血量的 25%)
        int baseDmg = (int)(maxHp * 0.25);
        // 2. 加上關卡難度加成 (每關多 5 點傷害)
        int waveBonus = wave * 5;
        // 3. 扣除防禦力，並設定保底傷害為 10
        int finalDmg = Math.max(10, (baseDmg + waveBonus) - defense);
        
        currentHp -= finalDmg;
        if (currentHp < 0) currentHp = 0;
    }

    public void heal(int amount) {
        currentHp = Math.min(currentHp + amount, maxHp);
    }

    public void resetHp(){
        currentHp = maxHp;
    }

    // -------------------------------------------------------
    // Getters / Setters
    // -------------------------------------------------------
    public int getScore()      { return score; }
    public void addScore(int n){ score += n; }

    public int getCurrentHp()  { return currentHp; }
    public int getMaxHp()      { return maxHp; }
    public int getDefense()    { return defense; }
    public int getSpeed()      { return speed; }
    public boolean isDead()    {return currentHp <=0;}
}