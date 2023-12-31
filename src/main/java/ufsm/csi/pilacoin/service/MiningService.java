package ufsm.csi.pilacoin.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import ufsm.csi.pilacoin.constants.AppInfo;
import ufsm.csi.pilacoin.constants.Colors;
import ufsm.csi.pilacoin.model.DifficultyObserver;
import ufsm.csi.pilacoin.model.PilaCoin;
import ufsm.csi.pilacoin.shared.SharedResources;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Date;
import java.util.Random;

@Service
public class MiningService implements Runnable, DifficultyObserver {
    private final RabbitService rabbitService;
    private BigInteger difficulty;
    private final PilaCoinService pilaCoinService;
    private final SharedResources sharedResources;
    private volatile boolean stopMining = false;


    public MiningService(RabbitService rabbitService, PilaCoinService pilaCoinService, SharedResources sharedResources) {
        this.rabbitService = rabbitService;
        this.pilaCoinService = pilaCoinService;
        this.sharedResources = sharedResources;
    }

    public void stopMining() {
       this.stopMining = true;
    }

    @Override
    @SneakyThrows
    public void run() {
        BigInteger hash;
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        String json = "";
        PilaCoin pilaCoin = PilaCoin.builder()
                .chaveCriador(this.sharedResources.getPublicKey().getEncoded())
                .nomeCriador(AppInfo.DEFAULT_NAME)
                .build();
        int count = 0;
        Random random = new Random();
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        byte[] byteArray = new byte[256 / 8];

        while(!stopMining) {
            random.nextBytes(byteArray);
            pilaCoin.setNonce(new BigInteger(md.digest(byteArray)).abs().toString());
            pilaCoin.setDataCriacao(Date.from(Instant.now()));
            json = ow.writeValueAsString(pilaCoin);
            hash = new BigInteger(md.digest(json.getBytes(StandardCharsets.UTF_8))).abs();
            count++;
            if(hash.compareTo(this.difficulty) < 0) {
                this.rabbitService.send("pila-minerado", json);
                this.sharedResources.updatePilaCoinsFoundPerDifficulty(this.difficulty);
                this.sharedResources.updatePilaCoinsFoundPerThread(Thread.currentThread().getName());
                System.out.printf( MessageFormatterService.threadIdentifierMessage(Thread.currentThread()) + Colors.BLACK_BACKGROUND + "Pilacoin found in " + Colors.WHITE_BOLD_BRIGHT + "%,d" + " tries" + Colors.ANSI_RESET + "\n", count);
                System.out.println(json);
                this.pilaCoinService.save(pilaCoin);
                count = 0;
            }
        }
        Thread.currentThread().interrupt();
    }

    @Override
    public void update(BigInteger difficulty) {
       this.difficulty = difficulty;
    }
}
