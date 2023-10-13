package ufsm.csi.pilacoin.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.SneakyThrows;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import ufsm.csi.pilacoin.Constants;
import ufsm.csi.pilacoin.model.Difficulty;
import ufsm.csi.pilacoin.model.PilaCoin;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.util.Date;
import java.util.Random;

@Service
public class MiningService {
    @Value("clients-errors")
    private String queue_errors;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @SneakyThrows
    public void mine(BigInteger difficulty) {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(1024);
            BigInteger hash;
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            int count = 0;
            String json = "";
            PilaCoin pilaCoin = PilaCoin.builder()
                    .chaveCriador(Constants.PUBLICKEY.getBytes(StandardCharsets.UTF_8))
                    .nomeCriador("Luiz Felipe")
                    .build();
            do {
                Random random = new Random();
                byte[] byteArray = new byte[256 / 8];
                random.nextBytes(byteArray);
                ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
                pilaCoin.setNonce(new BigInteger(md.digest(byteArray)).abs().toString());
                pilaCoin.setDataCriacao(new Date(System.currentTimeMillis()));
                json = ow.writeValueAsString(pilaCoin);
                hash = new BigInteger(md.digest(json.getBytes(StandardCharsets.UTF_8))).abs();
                count++;
            } while (hash.compareTo(difficulty) > 0);
            System.out.println(json);
            System.out.println("Found 1 Pilacoin in " + count + " tries");
            this.rabbitTemplate.convertAndSend("pila-minerado", json);
    }

    @RabbitListener(queues = {"${queue.msgs}"})
    public void successMessages(@Payload Object message) {
        System.out.println("Success: " + message.toString());
    }

    @RabbitListener(queues = {"${queue.errors}"})
    public void errorMessages(@Payload Message message) {
        System.out.println(new String(message.getBody(), StandardCharsets.UTF_8) );
    }

    @SneakyThrows
    @RabbitListener(queues = {"${queue.dificuldade}"})
    public void getDifficulty(@Payload String difJson) {
        ObjectMapper objectMapper = new ObjectMapper();
        Difficulty dif = objectMapper.readValue(difJson, Difficulty.class);
        BigInteger difficulty = new BigInteger(dif.getDificuldade(), 16);
        this.mine(difficulty);
    }

}
