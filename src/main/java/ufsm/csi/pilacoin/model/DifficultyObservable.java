package ufsm.csi.pilacoin.model;

public interface DifficultyObservable {
    void subscribe(DifficultyObserver difficultyObserver);
    void unsubscribe(DifficultyObserver difficultyObserver);
}
