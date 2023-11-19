package ufsm.csi.pilacoin.service;

import org.springframework.stereotype.Service;
import ufsm.csi.pilacoin.model.PilacoinMiningData;
import ufsm.csi.pilacoin.repository.PilaCoinMiningDataRepository;

import java.util.List;

@Service
public class PilacoinMiningDataService {
    private final PilaCoinMiningDataRepository pilaCoinMiningDataRepository;

    public PilacoinMiningDataService(PilaCoinMiningDataRepository pilaCoinMiningDataRepository) {
        this.pilaCoinMiningDataRepository = pilaCoinMiningDataRepository;
    }

    public List<PilacoinMiningData> getData() {
       return this.pilaCoinMiningDataRepository.findAll();
    }

    public void save(PilacoinMiningData pilacoinMiningData) {
       this.pilaCoinMiningDataRepository.save(pilacoinMiningData) ;
    }
}
