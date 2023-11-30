package ufsm.csi.pilacoin.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ufsm.csi.pilacoin.model.*;
import ufsm.csi.pilacoin.repository.PilaCoinRepository;
import ufsm.csi.pilacoin.repository.QueryResponsePilaRepository;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Service
public class PilaCoinService {
    private final PilaCoinRepository pilaCoinRepository;
    private final QueryResponsePilaRepository queryResponsePilaRepository;

    public PilaCoinService(PilaCoinRepository pilaCoinRepository, QueryResponsePilaRepository queryResponsePilaRepository) {
        this.pilaCoinRepository = pilaCoinRepository;
        this.queryResponsePilaRepository = queryResponsePilaRepository;
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
        pilaCoin.setId(0L);
        this.pilaCoinRepository.save(pilaCoin);
    }

    public Page<PilaCoin> findPilaCoinsWithPaginationAndSorting(int offset, int pageSize, String field) {
        return pilaCoinRepository.findAll(PageRequest.of(offset, pageSize).withSort(Sort.by(field).descending()));
    }

    public void saveAllQueryResponsePilas(List<QueryResponsePila> pilacoins) {
        this.queryResponsePilaRepository.saveAll(pilacoins);
    }

    public void deleteAllQueryResponsePilas() {
       this.queryResponsePilaRepository.deleteAll();
    }


    public Page<QueryResponsePila> getQueryResponsePilas(int offset, int pageSize, String field) {
        return this.queryResponsePilaRepository.findAll(PageRequest.of(offset, pageSize).withSort(Sort.by(field).descending()));
    }

    public List<PilaCoin> getPilaCoins() {
        return this.pilaCoinRepository.findAll();
    }
}
