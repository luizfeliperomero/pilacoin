package ufsm.csi.pilacoin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ufsm.csi.pilacoin.model.Block;

@Repository
public interface BlockRepository extends JpaRepository<Block, Long> {
}
