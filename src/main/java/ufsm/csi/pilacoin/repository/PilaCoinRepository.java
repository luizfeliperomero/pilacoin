package ufsm.csi.pilacoin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import ufsm.csi.pilacoin.model.PilaCoin;

import java.util.List;

@org.springframework.stereotype.Repository
public interface PilaCoinRepository extends JpaRepository<PilaCoin, Long> {
}
