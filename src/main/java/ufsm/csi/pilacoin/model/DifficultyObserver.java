package ufsm.csi.pilacoin.model;

import java.math.BigInteger;

public interface DifficultyObserver {
    void update(BigInteger difficulty);
}
