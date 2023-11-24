package ufsm.csi.pilacoin.service;

import org.springframework.stereotype.Service;
import ufsm.csi.pilacoin.model.Usuario;
import ufsm.csi.pilacoin.repository.UserRepository;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void saveAll(List<Usuario> user) {
        this.userRepository.saveAll(user);
    }

    public List<Usuario> getAll() {
       return this.userRepository.findAll();
    }
}
