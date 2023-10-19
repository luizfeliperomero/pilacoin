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
import ufsm.csi.pilacoin.model.Block;
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
    private BigInteger difficulty;
    private BigInteger prevDifficulty;
    private boolean firstPilaSended = false;

    @SneakyThrows
    public void mine() {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(1024);
            BigInteger hash;
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            String json = "";
            PilaCoin pilaCoin = PilaCoin.builder()
                    .dataCriacao(new Date(System.currentTimeMillis()))
                    .chaveCriador(Constants.PUBLICKEY.getBytes(StandardCharsets.UTF_8))
                    .nomeCriador("Luiz Felipe")
                    .build();
            int count = 0;
            while(true) {
                Random random = new Random();
                byte[] byteArray = new byte[256 / 8];
                random.nextBytes(byteArray);
                ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
                pilaCoin.setNonce(new BigInteger(md.digest(byteArray)).abs().toString());
                pilaCoin.setDataCriacao(new Date(System.currentTimeMillis()));
                json = ow.writeValueAsString(pilaCoin);
                hash = new BigInteger(md.digest(json.getBytes(StandardCharsets.UTF_8))).abs();
                if(!difficulty.equals(prevDifficulty)) {
                    System.out.println(Constants.ANSI_CYAN + surroundMessage("-", "Difficulty Changed: " + difficulty) + Constants.ANSI_RESET);
                }
                prevDifficulty = difficulty;
                if(!firstPilaSended) {
                    this.rabbitTemplate.convertAndSend("pila-minerado", json);
                    firstPilaSended = true;
                }
                count++;
                if(hash.compareTo(this.difficulty) < 0) {
                    this.rabbitTemplate.convertAndSend("pila-minerado", json);
                    System.out.println(json);
                    System.out.println(Constants.ANSI_PURPLE + "Found 1 Pilacoin in " + count + " tries" + Constants.ANSI_RESET);
                    count = 0;
                }
            }
    }

    @RabbitListener(queues = {"luiz_felipe"})
    public void rabbitResponse(@Payload Message message) {
        String responseMessage = new String(message.getBody());
        String outputColor = responseMessage.contains("erro") ? Constants.ANSI_RED : Constants.ANSI_GREEN;
        System.out.println(outputColor + responseMessage + Constants.ANSI_RESET);
    }


    @SneakyThrows
    @RabbitListener(queues = {"${queue.dificuldade}"})
    public void getDifficulty(@Payload String difJson) {
        ObjectMapper objectMapper = new ObjectMapper();
        Difficulty dif = objectMapper.readValue(difJson, Difficulty.class);
        System.out.println(Constants.ANSI_YELLOW + surroundMessage("-", "Difficulty Received: " + new BigInteger(dif.getDificuldade(), 16)) + Constants.ANSI_RESET);
        this.difficulty = new BigInteger(dif.getDificuldade(), 16);
        this.mine();
    }

    private String surroundMessage(String surround, String message) {
        String surrounds = surround.repeat(message.length());
        return surrounds + "\n" + message + "\n" + surrounds;
    }
}
