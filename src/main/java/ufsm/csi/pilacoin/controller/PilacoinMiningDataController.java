package ufsm.csi.pilacoin.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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

    @GetMapping("/list/{offset}/{size}/{field}")
    public ResponseEntity<Page<PilacoinMiningData>> paginationAndSort(@PathVariable int offset, @PathVariable int size, @PathVariable String field) {
        return ResponseEntity.ok(this.pilacoinMiningDataService.findPilaCoinMiningDataWithPaginationAndSorting(offset, size, field));
    }
}
