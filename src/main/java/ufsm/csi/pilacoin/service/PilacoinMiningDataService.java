package ufsm.csi.pilacoin.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ufsm.csi.pilacoin.model.PilacoinMiningData;
import ufsm.csi.pilacoin.repository.PilaCoinMiningDataRepository;

@Service
public class PilacoinMiningDataService {
    private final PilaCoinMiningDataRepository pilaCoinMiningDataRepository;

    public PilacoinMiningDataService(PilaCoinMiningDataRepository pilaCoinMiningDataRepository) {
        this.pilaCoinMiningDataRepository = pilaCoinMiningDataRepository;
    }

    public void save(PilacoinMiningData pilacoinMiningData) {
       this.pilaCoinMiningDataRepository.save(pilacoinMiningData) ;
    }

    public Page<PilacoinMiningData> findPilaCoinMiningDataWithPaginationAndSorting(int offset, int pageSize, String field) {
        return pilaCoinMiningDataRepository.findAll(PageRequest.of(offset, pageSize).withSort(Sort.by(field).descending()));
    }
}
