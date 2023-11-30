package ufsm.csi.pilacoin.shared;

import lombok.Data;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;
import ufsm.csi.pilacoin.constants.Colors;
import ufsm.csi.pilacoin.service.MessageFormatterService;

import javax.crypto.Cipher;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@Controller
@Data
public class SharedResources {
    private Map<String, Integer> pilaCoinsFoundPerDifficulty = new HashMap<String, Integer>();
    private Map<String, Integer> pilaCoinsFoundPerThread = new HashMap<String, Integer>();
    private Map<BigInteger, Integer> blocksFoundPerDifficulty = new HashMap<BigInteger, Integer>();
    private Map<String, Integer> blocksFoundPerThread = new HashMap<String, Integer>();
    private int blocksFound = 0;
    private final Object lock = new Object();
    private static volatile SharedResources  instance;
    private final AtomicBoolean alreadyExecutedShutdown = new AtomicBoolean(false);
    @Autowired
    public SimpMessagingTemplate template;
    public int pilacoinsFound = 0;
    private PublicKey publicKey;
    private PrivateKey privateKey;
    private boolean firstPilacoinsTotalSent = false;
    private boolean firstPilacoinsFoundPerDifficultySent = false;
    private boolean firstPilacoinsFoundPerThreadSent = false;
    private final CountDownLatch difficultyLatch = new CountDownLatch(1);
    private final Thread shutdownThread = new Thread(() -> {
        synchronized (lock) {
            if (this.alreadyExecutedShutdown.compareAndSet(false, true)) {
                this.printMiningData();
            }
        }
    });
    @SneakyThrows
    private SharedResources (){
        this.getKeys();
    }

    public static SharedResources getInstance() {
        SharedResources result = instance;
        if(result == null) {
            synchronized (SharedResources.class) {
                result = instance;
                if (instance == null) {
                    instance = result = new SharedResources();
                }
            }
        }
        return result;
    }

    {
        Runtime.getRuntime().addShutdownHook(shutdownThread);
    }

    @SneakyThrows
    public void generateKeys() {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        this.publicKey = keyPair.getPublic();
        this.privateKey = keyPair.getPrivate();
        FileOutputStream fosPublic = new FileOutputStream("public-key");
        fosPublic.write(this.publicKey.getEncoded());
        fosPublic.close();
        FileOutputStream fosPrivate = new FileOutputStream("private-key");
        fosPrivate.write(this.privateKey.getEncoded());
        fosPrivate.close();
    }

    @SneakyThrows
    public void getKeys() {
        try {
            //FileInputStream fosPublic = new FileInputStream("/usr/local/lib/public-key");
            //FileInputStream fosPrivate = new FileInputStream("/usr/local/lib/private-key");
            FileInputStream fosPublic = new FileInputStream("public-key");
            FileInputStream fosPrivate = new FileInputStream("private-key");
            byte[] encodedPublic = new byte[fosPublic.available()];
            byte[] encodedPrivate = new byte[fosPrivate.available()];
            fosPublic.read(encodedPublic);
            fosPrivate.read(encodedPrivate);
            fosPublic.close();
            fosPrivate.close();

            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(encodedPublic);
            PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(encodedPrivate);
            this.publicKey = keyFactory.generatePublic(publicKeySpec);
            this.privateKey = keyFactory.generatePrivate(privateKeySpec);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SneakyThrows
    public byte[] generateSignature(String str) {
        Cipher encryptCipher = Cipher.getInstance("RSA");
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        encryptCipher.init(Cipher.ENCRYPT_MODE, this.getPrivateKey());
        byte[] hash = md.digest(str.getBytes(StandardCharsets.UTF_8));
        return encryptCipher.doFinal(hash);
    }

    public synchronized Map<String, Integer> getPilaCoinsFoundPerDifficulty() {
        return pilaCoinsFoundPerDifficulty;
    }

    public synchronized void setPilaCoinsFoundPerDifficulty(Map<String, Integer> pilaCoinsFoundPerDifficulty) {
        this.pilaCoinsFoundPerDifficulty = pilaCoinsFoundPerDifficulty;
    }

    public synchronized Map<String, Integer> getPilaCoinsFoundPerThread() {
        return pilaCoinsFoundPerThread;
    }

    public synchronized void setPilaCoinsFoundPerThread(Map<String, Integer> pilaCoinsFoundPerThread) {
        this.pilaCoinsFoundPerThread = pilaCoinsFoundPerThread;
    }

    //@Scheduled(fixedRate = 4000)
    public void sendPilacoinsFoundPerDifficulty() {
        if(!this.pilaCoinsFoundPerDifficulty.isEmpty()) {
            this.template.convertAndSend("/topic/pilacoins_found_per_difficulty", this.pilaCoinsFoundPerDifficulty);
        }
    }

    //@Scheduled(fixedRate = 4000)
    public void sendPilacoinsFoundPerThread() {
        if(!this.pilaCoinsFoundPerThread.isEmpty()) {
            this.template.convertAndSend("/topic/pilacoins_found_per_thread", this.pilaCoinsFoundPerThread);
        }
    }

    public void sendBlocksFoundPerThread() {
        if(!this.blocksFoundPerThread.isEmpty()) {
            this.template.convertAndSend("/topic/blocks_found_per_thread", this.blocksFoundPerThread);
        }
    }

    //@Scheduled(fixedRate = 4000)
    public void sendPilacoinsTotal() {
        this.template.convertAndSend("/topic/total_pilacoins", this.pilacoinsFound);
    }

    public void sendBlocksTotal() {
        this.template.convertAndSend("/topic/total_blocks", this.blocksFound);
    }

    public synchronized void updatePilaCoinsFoundPerDifficulty(BigInteger difficulty) {
        this.pilacoinsFound++;
        this.sendPilacoinsTotal();
        pilaCoinsFoundPerDifficulty.merge(difficulty.toString(), 1, Integer::sum);
        this.sendPilacoinsFoundPerDifficulty();
    }

    public void sendBlocksFoundPerDifficulty() {
        if(!this.blocksFoundPerDifficulty.isEmpty()) {
            this.template.convertAndSend("/topic/blocks_found_per_difficulty", this.blocksFoundPerDifficulty);
        }
    }

    public synchronized void updateBlocksFoundPerDifficulty(BigInteger difficulty) {
        this.blocksFound++;
        this.sendBlocksTotal();
        blocksFoundPerDifficulty.merge(difficulty, 1, Integer::sum);
        this.sendBlocksFoundPerDifficulty();
    }

    public synchronized void updateBlocksFoundPerThread(String threadName) {
        blocksFoundPerThread.merge(threadName, 1, Integer::sum);
        this.sendBlocksFoundPerThread();
    }

    public synchronized void updatePilaCoinsFoundPerThread(String threadName) {
        pilaCoinsFoundPerThread.merge(threadName, 1, Integer::sum);
        this.sendPilacoinsFoundPerThread();
    }

    private void printMiningData() {
        System.out.println("\n");
        System.out.println(Colors.YELLOW_BOLD_BRIGHT + "Mining Data" + Colors.ANSI_RESET);
        System.out.println(Colors.ANSI_CYAN + MessageFormatterService.surroundMessage("-","Pilacoins found per difficulty") + Colors.ANSI_RESET);
        pilaCoinsFoundPerDifficulty.entrySet().stream()
                .sorted((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()))
                .forEach(entry -> {
                    String k = entry.getKey();
                    Integer v = entry.getValue();
                    System.out.println("| " + Colors.WHITE_BOLD_BRIGHT + k + ": " + Colors.ANSI_GREEN + v + Colors.ANSI_RESET + " |");
                });
        System.out.println(Colors.ANSI_CYAN + MessageFormatterService.surroundMessage("-","Pilacoins found per Thread") + Colors.ANSI_RESET);
        pilaCoinsFoundPerThread.entrySet().stream()
                .sorted((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()))
                .forEach(entry -> {
                    String k = entry.getKey();
                    Integer v = entry.getValue();
                    System.out.println("| " + Colors.WHITE_BOLD_BRIGHT + k + ": " + Colors.ANSI_GREEN + v + Colors.ANSI_RESET + " |");
                });
        System.out.println("\n");
    }

}
