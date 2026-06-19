public class PlayerData {
    private int score = 0;
    private int maxHp = 100;
    private int currentHp = 100;
    private int defense = 0;

    // 升級屬性所需的費用
    public int getHpUpgradeCost() { return maxHp * 2; }
    public int getDefUpgradeCost() { return (defense + 1) * 100; }

    public void upgradeHp() {
        if (score >= getHpUpgradeCost()) {
            score -= getHpUpgradeCost();
            maxHp += 20;
            currentHp = maxHp; // 升級時順便回滿血
        }
    }

    public void upgradeDefense() {
        if (score >= getDefUpgradeCost()) {
            score -= getDefUpgradeCost();
            defense += 1;
        }
    }

    // 扣血邏輯：考慮防禦力減傷
    public void takeDamage(int dmg) {
        int finalDmg = Math.max(1, dmg - defense); // 至少扣 1 點血
        currentHp -= finalDmg;
        if (currentHp < 0) currentHp = 0;
    }

    // Getters 和 Setters ...
    public int getScore() { return score; }
    public void addScore(int amount) { this.score += amount; }
    public int getCurrentHp() { return currentHp; }
    public int getMaxHp() { return maxHp; }
    public int getDefense() { return defense; }
}