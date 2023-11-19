package ufsm.csi.pilacoin.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ufsm.csi.pilacoin.model.PilacoinMiningData;
import ufsm.csi.pilacoin.service.PilacoinMiningDataService;

import java.util.List;

@RestController
@CrossOrigin("*")
@RequestMapping("/pilacoin_mining_data")
public class PilacoinMiningDataController {
    private final PilacoinMiningDataService pilacoinMiningDataService;

    public PilacoinMiningDataController(PilacoinMiningDataService pilacoinMiningDataService) {
        this.pilacoinMiningDataService = pilacoinMiningDataService;
    }

    @GetMapping("/list")
    public List<PilacoinMiningData> getAll() {
        return this.pilacoinMiningDataService.getData();
    }
}
