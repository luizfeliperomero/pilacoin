package ufsm.csi.pilacoin.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ufsm.csi.pilacoin.service.PilaCoinService;

import java.util.Optional;

@RestController
@CrossOrigin("*")
@RequestMapping("/pilacoin")
public class PilaCoinController {
    private final PilaCoinService pilaCoinService;

    public PilaCoinController(PilaCoinService pilaCoinService) {
        this.pilaCoinService = pilaCoinService;
    }

    @GetMapping("/getAll")
    public ResponseEntity getPilas() {
        return ResponseEntity.of(Optional.ofNullable(this.pilaCoinService.getPilaCoins()));
    }

}
