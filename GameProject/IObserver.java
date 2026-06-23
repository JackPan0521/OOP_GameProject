/**
 * Observer 介面
 * 所有想要接收遊戲事件通知的類別都要實作這個介面
 */
public interface IObserver {
    void update(GameEvent event);
}
