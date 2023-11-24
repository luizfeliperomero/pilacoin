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
import java.util.List;

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
                Cipher encryptCipher = Cipher.getInstance("RSA");
                encryptCipher.init(Cipher.ENCRYPT_MODE, this.sharedResources.getPrivateKey());
                byte[] hashByteArr = hash.toString().getBytes(StandardCharsets.UTF_8);
                PilaValidado pilaValidado = PilaValidado.builder()
                        .nomeValidador(AppInfo.DEFAULT_NAME)
                        .chavePublicaValidador(this.sharedResources.getPublicKey().toString().getBytes(StandardCharsets.UTF_8))
                        .assinaturaPilaCoin(encryptCipher.doFinal(hashByteArr))
                        .pilaCoin(pilaCoin)
                        .build();
                System.out.println(Colors.WHITE_BOLD_BRIGHT + pilaValidado.getPilaCoin().getNomeCriador() + "'s " + Colors.ANSI_CYAN + "Pila valid!" + Colors.ANSI_RESET);
                String json = this.objectWriter.writeValueAsString(pilaValidado);
                this.send("pila-validado", json);
            } else this.send("pila-minerado", pilaCoinStr);
        }
    }

    @SneakyThrows
    @RabbitListener(queues = {"Luiz Felipe-query"})
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

    public void saveUsers(List<Usuario> users) {
        this.userService.saveAll(users);
    }

    public void saveAllQueryResponsePilas(List<QueryResponsePila> queryResponsePilas) {
        this.pilaCoinService.saveAllQueryResponsePilas(queryResponsePilas);
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
