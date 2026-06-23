/**
 * 遊戲內所有事件類型
 * MineBoard 發布，任何訂閱者皆可接收
 */
public enum GameEvent {
    MINE_HIT,       // 玩家踩到地雷
    GAME_OVER,      // 血量歸零，遊戲失敗
    STAGE_CLEARED,  // 整張地圖通關
    STAGE_RESET,    // 按 R 重置關卡（過關或重試）
    PURCHASE_OK,    // 商店購買成功
    PURCHASE_FAIL,  // 商店購買失敗
    SKILL_USED,     // 技能成功啟動
}
