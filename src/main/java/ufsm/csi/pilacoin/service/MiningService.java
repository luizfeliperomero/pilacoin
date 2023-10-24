package ufsm.csi.pilacoin.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.SneakyThrows;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ufsm.csi.pilacoin.Constants;
import ufsm.csi.pilacoin.model.PilaCoin;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.util.Date;
import java.util.Random;
import java.util.stream.IntStream;

@Service
public class MiningService implements Runnable{
    private boolean firstPilaSent = false;
    private final RabbitService rabbitService;


    public MiningService(RabbitService rabbitService) {
        this.rabbitService = rabbitService;
    }

    public void startMiningThreads(int threads) {
        if(!firstPilaSent) {
            this.rabbitService.send("pila-minerado", "");
            this.firstPilaSent = true;
        }
        IntStream.range(0, threads)
                .mapToObj(i -> new MiningService(this.rabbitService))
                .map(Thread::new)
                .forEach(Thread::start);
    }

    @Override
    @SneakyThrows
    public void run() {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(1024);
        BigInteger hash;
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        String json = "";
        PilaCoin pilaCoin = PilaCoin.builder()
                .chaveCriador(Constants.PUBLICKEY.getBytes(StandardCharsets.UTF_8))
                .nomeCriador("Luiz Felipe")
                .build();
        int count = 0;
        Random random = new Random();
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        while(true) {
            byte[] byteArray = new byte[256 / 8];
            random.nextBytes(byteArray);
            pilaCoin.setNonce(new BigInteger(md.digest(byteArray)).abs().toString());
            pilaCoin.setDataCriacao(new Date(System.currentTimeMillis()));
            json = ow.writeValueAsString(pilaCoin);
            hash = new BigInteger(md.digest(json.getBytes(StandardCharsets.UTF_8))).abs();
            count++;
            if(hash.compareTo(DifficultyService.currentDifficulty) < 0) {
                this.rabbitService.send("pila-minerado", json);
                System.out.printf( MessageFormatterService.threadIdentifierMessage(Thread.currentThread()) + Constants.BLACK_BACKGROUND + "Pilacoin found in " + Constants.WHITE_BOLD_BRIGHT + "%,d" + " tries" + Constants.ANSI_RESET + "\n", count);
                System.out.println(json);
                count = 0;
            }
        }
    }
}
