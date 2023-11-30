package ufsm.csi.pilacoin.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.SneakyThrows;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import ufsm.csi.pilacoin.constants.AppInfo;
import ufsm.csi.pilacoin.constants.Colors;
import ufsm.csi.pilacoin.model.*;
import ufsm.csi.pilacoin.shared.SharedResources;

import javax.crypto.Cipher;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class RabbitService implements DifficultyObserver {
    @Autowired
    private RabbitTemplate rabbitTemplate;
    private final ObjectReader objectReader = new ObjectMapper().reader();
    private final ObjectWriter objectWriter = new ObjectMapper().writer();
    private final SharedResources sharedResources;
    private final UserService userService;
    private final PilaCoinService pilaCoinService;
    private BigInteger difficulty;

    public RabbitService(RabbitTemplate rabbitTemplate, SharedResources sharedResources, UserService userService, PilaCoinService pilaCoinService) {
        this.rabbitTemplate = rabbitTemplate;
        this.sharedResources = sharedResources;
        this.userService = userService;
        this.pilaCoinService = pilaCoinService;
    }

    public void send(String topic, String object) {
        this.rabbitTemplate.convertAndSend(topic, object);
    }


    @SneakyThrows
    @RabbitListener(queues = {"pila-minerado"})
    public void validatePila(@Payload String pilaCoinStr) {
        if(!pilaCoinStr.isEmpty()) {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            BigInteger hash = new BigInteger(md.digest(pilaCoinStr.getBytes(StandardCharsets.UTF_8))).abs();
            PilaCoin pilaCoin = null;
            try {
                pilaCoin = this.objectReader.readValue(pilaCoinStr, PilaCoin.class);
            } catch(Exception e) {}
            if (this.difficulty != null &&  pilaCoin != null && !pilaCoin.getNomeCriador().equals(AppInfo.DEFAULT_NAME) && (hash.compareTo(this.difficulty) < 0)) {
                PilaValidado pilaValidado = PilaValidado.builder()
                        .nomeValidador(AppInfo.DEFAULT_NAME)
                        .chavePublicaValidador(this.sharedResources.getPublicKey().getEncoded())
                        .assinaturaPilaCoin(this.sharedResources.generateSignature(pilaCoinStr))
                        .pilaCoinJson(pilaCoin)
                        .build();
                System.out.println(Colors.WHITE_BOLD_BRIGHT + pilaValidado.getPilaCoinJson().getNomeCriador() + "'s " + Colors.ANSI_CYAN + "Pila valid!" + Colors.ANSI_RESET);
                String json = this.objectWriter.writeValueAsString(pilaValidado);
                this.send("pila-validado", json);
            } else this.send("pila-minerado", pilaCoinStr);
        }
    }

    @SneakyThrows
    @RabbitListener(queues = "report")
    public void getReport(@Payload String report) {
        List<Report> reports = List.of(this.objectReader.readValue(report, Report[].class));
        Optional<Report> myReport = reports.stream()
                .filter(r  -> r.getNomeUsuario() != null && r.getNomeUsuario().equals(AppInfo.DEFAULT_NAME))
                .findFirst();
        System.out.println(myReport);
    }

    @SneakyThrows
    @RabbitListener(queues = {"luizf-query"})
    public void query(@Payload String query) {
        QueryResponse queryResponse = this.objectReader.readValue(query, QueryResponse.class);
        if(queryResponse.getIdQuery() == 1) {
            List<Usuario> users = queryResponse.getUsuariosResult();
            this.saveUsers(users);
        } else if(queryResponse.getIdQuery() == 2) {
            List<QueryResponsePila> pilaCoins = queryResponse.getPilasResult();
            this.saveAllQueryResponsePilas(pilaCoins);
        }
    }

    @RabbitListener(queues = "luizf")
    public void user(@Payload String str) {
        System.out.println(str);
    }

    public void saveUsers(List<Usuario> users) {
        this.userService.saveAll(users);
    }

    public void saveAllQueryResponsePilas(List<QueryResponsePila> queryResponsePilas) {
        this.pilaCoinService.saveAllQueryResponsePilas(queryResponsePilas);
    }

    @SneakyThrows
    public void transfer(Usuario user, PilaCoin pilaCoin) {
        Transfer transfer = Transfer.builder()
                .noncePila(pilaCoin.getNonce())
                .chaveUsuarioOrigem(this.sharedResources.getPublicKey().getEncoded())
                .dataTransacao(new Date(System.currentTimeMillis()))
                .nomeUsuarioDestino(user.getNome())
                .chaveUsuarioDestino(user.getChavePublica())
                .nomeUsuarioOrigem(AppInfo.DEFAULT_NAME)
                .build();
        String transferJson = this.objectWriter.writeValueAsString(transfer);
        transfer.setAssinatura(this.sharedResources.generateSignature(transferJson));
        transferJson = this.objectWriter.writeValueAsString(transfer);
        this.send("transferir-pila", transferJson);
    }

    @SneakyThrows
    public void sendQuery(QueryRequest query) {
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String queryJson = ow.writeValueAsString(query);
        this.send("query", queryJson);
    }

    @Override
    public void update(BigInteger difficulty) {
       this.difficulty = difficulty;
    }
}
