package ufsm.csi.pilacoin.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ufsm.csi.pilacoin.Constants;
import ufsm.csi.pilacoin.model.Difficulty;
import ufsm.csi.pilacoin.model.PilaCoin;
import ufsm.csi.pilacoin.model.PilaCoinJson;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Random;

@Service
public class MiningService {
    @Value("clients-errors")
    private String queue_errors;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    private static BigInteger difficulty;

    @SneakyThrows
    @Scheduled(fixedDelay = 5000, initialDelay = 5000)
    public void mine() {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(1024);

            PilaCoin pilaCoin = PilaCoin.builder()
                    .chavePublica(Constants.PUBLICKEY.getBytes(StandardCharsets.UTF_8))
                    .nomeMinerador("Luiz Felipe")
                    .dataHoraCriacao(new Date())
                    .build();
            BigInteger hash;
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            int count = 0;
            String json = "";
            PilaCoinJson pilaCoinJson = new PilaCoinJson();
            do {
                Random random = new Random();
                byte[] byteArray = new byte[256 / 8];
                random.nextBytes(byteArray);
                pilaCoin.setMagicNumber(byteArray);
                ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
                pilaCoinJson.setNonce(new BigInteger(md.digest(pilaCoin.getMagicNumber())).abs().toString());
                pilaCoinJson.setDataCriacao(new Date(System.currentTimeMillis()));
                pilaCoinJson.setNomeCriador("Luiz Felipe");
                pilaCoinJson.setChaveCriador(pilaCoin.getChavePublica());
                hash = new BigInteger(md.digest(json.getBytes(StandardCharsets.UTF_8))).abs();
                json = ow.writeValueAsString(pilaCoinJson);
                count++;
            } while (difficulty != null && hash.compareTo(difficulty) > 0);
            System.out.println(hash);
            System.out.println(json);
            System.out.println("Found 1 Pilacoin in " + count + " tries");
            rabbitTemplate.convertAndSend("pila-minerado", json);
    }

    @RabbitListener(queues = {"${queue.msgs}"})
    public void successMessages(@Payload String message) {
        System.out.println("Success: " + message);
    }

    @RabbitListener(queues = {"${queue.errors}"})
    public void errorMessages(@Payload String message) {
        System.out.println("Error: " + message);
    }

    @SneakyThrows
    @RabbitListener(queues = {"${queue.dificuldade}"})
    public void getDifficulty(@Payload String difJson) {
        System.out.println("Difficulty: " + difJson);
        ObjectMapper objectMapper = new ObjectMapper();
        Difficulty dif = objectMapper.readValue(difJson, Difficulty.class);
        difficulty = new BigInteger(dif.getDificuldade(), 16);
    }
}
