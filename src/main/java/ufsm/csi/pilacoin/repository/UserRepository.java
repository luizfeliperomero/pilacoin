package ufsm.csi.pilacoin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ufsm.csi.pilacoin.model.Usuario;

public interface UserRepository extends JpaRepository<Usuario, Long> {
}
