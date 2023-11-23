package ufsm.csi.pilacoin.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.*;
import ufsm.csi.pilacoin.model.PilaCoin;
import ufsm.csi.pilacoin.model.QueryRequest;
import ufsm.csi.pilacoin.model.QueryResponse;
import ufsm.csi.pilacoin.service.DifficultyService;
import ufsm.csi.pilacoin.service.PilaCoinService;
import ufsm.csi.pilacoin.service.RabbitService;

import java.util.Optional;

@RestController
@CrossOrigin("*")
@RequestMapping("/pilacoin")
public class PilaCoinController {
    private final DifficultyService difficultyService;
    private final PilaCoinService pilaCoinService;
    private final RabbitService rabbitService;

    public PilaCoinController(DifficultyService difficultyService, PilaCoinService pilaCoinService, RabbitService rabbitService, RabbitService rabbitService1) {
        this.difficultyService = difficultyService;
        this.pilaCoinService = pilaCoinService;
        this.rabbitService = rabbitService1;
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
    @PostMapping("/query")
    public void query(@Payload QueryRequest queryRequest) {
        this.rabbitService.sendQuery(queryRequest);
    }

}
