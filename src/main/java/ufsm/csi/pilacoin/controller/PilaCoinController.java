package ufsm.csi.pilacoin.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ufsm.csi.pilacoin.model.PilaCoin;
import ufsm.csi.pilacoin.service.DifficultyService;
import ufsm.csi.pilacoin.service.PilaCoinService;

import java.util.Optional;

@RestController
@CrossOrigin("*")
@RequestMapping("/pilacoin")
public class PilaCoinController {
    private final DifficultyService difficultyService;
    private final PilaCoinService pilaCoinService;

    public PilaCoinController(DifficultyService difficultyService, PilaCoinService pilaCoinService) {
        this.difficultyService = difficultyService;
        this.pilaCoinService = pilaCoinService;
    }

    @GetMapping("/getAll")
    public ResponseEntity getPilas() {
        return ResponseEntity.of(Optional.ofNullable(this.pilaCoinService.getPilaCoins()));
    }

    @GetMapping("/startMining")
    public ResponseEntity startMining() {
        this.difficultyService.startMining();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/stopMining")
    public ResponseEntity stopMining() {
        this.difficultyService.stopMining();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/paginationAndSort/{offset}/{pageSize}/{field}")
    public ResponseEntity<Page<PilaCoin>> getPilaCoinsWithPaginationAndSort(@PathVariable int offset, @PathVariable int pageSize, @PathVariable String field) {
        Page<PilaCoin> pilaCoins = this.pilaCoinService.findPilaCoinsWithPaginationAndSorting(offset, pageSize, field);
        return ResponseEntity.ok(pilaCoins);
    }

}
