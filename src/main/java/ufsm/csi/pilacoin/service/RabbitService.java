package ufsm.csi.pilacoin.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.SneakyThrows;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import ufsm.csi.pilacoin.Constants;
import ufsm.csi.pilacoin.model.Block;
import ufsm.csi.pilacoin.model.DifficultyObserver;
import ufsm.csi.pilacoin.model.PilaCoin;
import ufsm.csi.pilacoin.model.PilaValidado;
import ufsm.csi.pilacoin.shared.SharedResources;

import javax.crypto.Cipher;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Service
public class RabbitService implements DifficultyObserver {
    @Autowired
    private RabbitTemplate rabbitTemplate;
    private final ObjectReader objectReader = new ObjectMapper().reader();
    private final ObjectWriter objectWriter = new ObjectMapper().writer();
    private final SharedResources sharedResources;
    private BigInteger difficulty;

    public RabbitService(SharedResources sharedResources) {
        this.sharedResources = sharedResources;
    }

    public void send(String topic, String object) {
        this.rabbitTemplate.convertAndSend(topic, object);
    }

    @SneakyThrows
    @RabbitListener(queues = {"pila-minerado"})
    public void validatePila(@Payload String pilaCoinStr) {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        BigInteger hash = new BigInteger(md.digest(pilaCoinStr.getBytes(StandardCharsets.UTF_8))).abs();
        PilaCoin pilaCoin = this.objectReader.readValue(pilaCoinStr, PilaCoin.class);
        hash = new BigInteger(md.digest(pilaCoinStr.getBytes(StandardCharsets.UTF_8))).abs();

        if(this.difficulty != null && pilaCoin.getNomeCriador().equals("Luiz Felipe") && (hash.compareTo(this.difficulty) < 0)) {
            Cipher encryptCipher = Cipher.getInstance("RSA");
            encryptCipher.init(Cipher.ENCRYPT_MODE, this.sharedResources.getPrivateKey());
            byte[] hashByteArr = hash.toString().getBytes(StandardCharsets.UTF_8);
            PilaValidado pilaValidado = PilaValidado.builder()
                    .nomeValidador("Luiz Felipe")
                    .chavePublicaValidador(this.sharedResources.getPublicKey().toString().getBytes(StandardCharsets.UTF_8))
                    .assinaturaPilaCoin(encryptCipher.doFinal(hashByteArr))
                    .pilaCoin(pilaCoin)
                    .build();
            String json = this.objectWriter.writeValueAsString(pilaValidado);
            this.send("pila-validado", json);
        } else this.send("pila-minerado", pilaCoinStr);
    }


    @RabbitListener(queues = {"luiz_felipe"})
    public void rabbitResponse(@Payload Message message) {
        String responseMessage = new String(message.getBody());
        String outputColor = responseMessage.contains("erro") ? Constants.ANSI_RED : Constants.ANSI_GREEN;
        System.out.println(outputColor + responseMessage + Constants.ANSI_RESET);
    }


    @Override
    public void update(BigInteger difficulty) {
       this.difficulty = difficulty;
    }
}
