package ufsm.csi.pilacoin.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ufsm.csi.pilacoin.service.DifficultyService;

@RestController
@CrossOrigin("*")
public class GeneralController {
    private final DifficultyService difficultyService;

    public GeneralController(DifficultyService difficultyService) {
        this.difficultyService = difficultyService;
    }

    @GetMapping("/health")
    public ResponseEntity health() {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/startMining")
    public ResponseEntity startMining() {
       this.difficultyService.startMining();
       return ResponseEntity.ok().build();
    }

    @GetMapping("/stop")
    public void closeApplication() {
        System.exit(0);
    }
}
