package ufsm.csi.pilacoin.service;

import org.springframework.stereotype.Service;
import ufsm.csi.pilacoin.model.PilaCoin;
import ufsm.csi.pilacoin.model.PilaTransfer;
import ufsm.csi.pilacoin.repository.PilaCoinRepository;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Service
public class PilaCoinService {
    private final PilaCoinRepository pilaCoinRepository;

    public PilaCoinService(PilaCoinRepository pilaCoinRepository) {
        this.pilaCoinRepository = pilaCoinRepository;
    }

    public void transferPila(PilaCoin pilaCoin, String target_username, String target_user_key) {
        PilaTransfer pilaTransfer = PilaTransfer.builder()
                .nomeUsuarioDestino(target_username)
                .noncePila(pilaCoin.getNonce())
                .chaveUsuarioDestino(target_user_key.getBytes(StandardCharsets.UTF_8))
                .dataTransacao(new Date(System.currentTimeMillis()))
                .build();
    }

    public void save(PilaCoin pilaCoin) {
        this.pilaCoinRepository.save(pilaCoin);
    }

    public List<PilaCoin> getPilaCoins() {
        return this.pilaCoinRepository.findAll();
    }
}
