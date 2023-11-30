package ufsm.csi.pilacoin.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.*;
import ufsm.csi.pilacoin.constants.AppInfo;
import ufsm.csi.pilacoin.model.QueryRequest;
import ufsm.csi.pilacoin.model.QueryType;
import ufsm.csi.pilacoin.model.Usuario;
import ufsm.csi.pilacoin.service.RabbitService;
import ufsm.csi.pilacoin.service.UserService;

import java.util.List;

@RestController
@CrossOrigin("*")
@RequestMapping("/user")
public class UserController {
    private final UserService userService;
    private final RabbitService rabbitService;

    public UserController(UserService userService, RabbitService rabbitService) {
        this.userService = userService;
        this.rabbitService = rabbitService;
    }


    @GetMapping("/list")
    public ResponseEntity<List<Usuario>> getUsers() {
       return ResponseEntity.ok(this.userService.getAll());
    }

    @PostMapping("/update")
    public void update() {
        QueryRequest query = QueryRequest.builder()
                .idQuery(1)
                .nomeUsuario(AppInfo.DEFAULT_NAME)
                .tipoQuery(QueryType.USUARIOS)
                .build();
        this.rabbitService.sendQuery(query);
    }
}
