package ufsm.csi.pilacoin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ufsm.csi.pilacoin.model.PilacoinMiningData;

@Repository
public interface PilaCoinMiningDataRepository extends JpaRepository<PilacoinMiningData, Long> {
}
