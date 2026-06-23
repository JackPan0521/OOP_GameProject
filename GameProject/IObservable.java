/**
 * Observable（Subject）介面
 * 任何可以被觀察的類別都要實作這個介面
 */
public interface IObservable {
    void addObserver(IObserver o);
    void removeObserver(IObserver o);
    void notifyObservers(GameEvent event);
}
