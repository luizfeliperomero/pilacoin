package ufsm.csi.pilacoin.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ufsm.csi.pilacoin.service.BlockService;

@RestController
@CrossOrigin("*")
@RequestMapping("/block")
public class BlockController {
    private final BlockService blockService;

    public BlockController(BlockService blockService) {
        this.blockService = blockService;
    }

    @GetMapping("/startMining")
    public ResponseEntity startMining() {
        this.blockService.startBlockMiningThreads();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/stopMining")
    public ResponseEntity stopMining() {
        this.blockService.stopBlockMiningThreads();
        return ResponseEntity.ok().build();
    }

}
