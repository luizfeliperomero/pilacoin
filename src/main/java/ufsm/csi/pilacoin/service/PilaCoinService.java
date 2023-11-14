package ufsm.csi.pilacoin.service;

import org.springframework.stereotype.Service;
import ufsm.csi.pilacoin.model.PilaCoin;
import ufsm.csi.pilacoin.repository.PilaCoinRepository;

@Service
public class PilaCoinService {
    private final PilaCoinRepository pilaCoinRepository;

    public PilaCoinService(PilaCoinRepository pilaCoinRepository) {
        this.pilaCoinRepository = pilaCoinRepository;
    }

    public void save(PilaCoin pilaCoin) {
        this.pilaCoinRepository.save(pilaCoin);
    }
}
