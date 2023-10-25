package ufsm.csi.pilacoin.model;

public interface BlockObservable {
    void subscribe(BlockObserver blockObserver);
    void unsubscribe(BlockObserver blockObserver);
}
