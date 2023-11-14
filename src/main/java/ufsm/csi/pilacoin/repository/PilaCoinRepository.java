package ufsm.csi.pilacoin.repository;

import org.springframework.data.repository.Repository;
import ufsm.csi.pilacoin.model.PilaCoin;

public interface PilaCoinRepository extends Repository<PilaCoin, Long> {
    void save(PilaCoin pilaCoin);
}
