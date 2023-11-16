package ufsm.csi.pilacoin.shared;

import lombok.Data;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;
import ufsm.csi.pilacoin.constants.Colors;
import ufsm.csi.pilacoin.service.MessageFormatterService;

import java.lang.reflect.Type;
import java.math.BigInteger;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@Controller
@Data
public class SharedResources {
    private Map<BigInteger, Integer> pilaCoinsFoundPerDifficulty = new HashMap<BigInteger, Integer>();
    private Map<String, Integer> pilaCoinsFoundPerThread = new HashMap<String, Integer>();
    private final Object lock = new Object();
    private static volatile SharedResources  instance;
    private final AtomicBoolean alreadyExecutedShutdown = new AtomicBoolean(false);
    @Autowired
    public SimpMessagingTemplate template;
    private PublicKey publicKey;
    private PrivateKey privateKey;
    private final Thread shutdownThread = new Thread(() -> {
        synchronized (lock) {
            if (this.alreadyExecutedShutdown.compareAndSet(false, true)) {
                this.printMiningData();
            }
        }
    });
    @SneakyThrows
    private SharedResources (){
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(1024);
        this.publicKey = keyPairGenerator.generateKeyPair().getPublic();
        this.privateKey = keyPairGenerator.generateKeyPair().getPrivate();
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
    public synchronized Map<BigInteger, Integer> getPilaCoinsFoundPerDifficulty() {
        return pilaCoinsFoundPerDifficulty;
    }

    public synchronized void setPilaCoinsFoundPerDifficulty(Map<BigInteger, Integer> pilaCoinsFoundPerDifficulty) {
        this.pilaCoinsFoundPerDifficulty = pilaCoinsFoundPerDifficulty;
    }

    public synchronized Map<String, Integer> getPilaCoinsFoundPerThread() {
        return pilaCoinsFoundPerThread;
    }

    public synchronized void setPilaCoinsFoundPerThread(Map<String, Integer> pilaCoinsFoundPerThread) {
        this.pilaCoinsFoundPerThread = pilaCoinsFoundPerThread;
    }

    public synchronized void updatePilaCoinsFoundPerDifficulty(BigInteger difficulty) {
        pilaCoinsFoundPerDifficulty.merge(difficulty, 1, Integer::sum);
        this.template.convertAndSend("/topic/pilacoins_found_per_difficulty", this.pilaCoinsFoundPerDifficulty);
    }

    public synchronized void updatePilaCoinsFoundPerThread(String threadName) {
        pilaCoinsFoundPerThread.merge(threadName, 1, Integer::sum);
        this.template.convertAndSend("/topic/pilacoins_found_per_thread", this.pilaCoinsFoundPerThread);
    }

    private void printMiningData() {
        System.out.println("\n");
        System.out.println(Colors.YELLOW_BOLD_BRIGHT + "Mining Data" + Colors.ANSI_RESET);
        System.out.println(Colors.ANSI_CYAN + MessageFormatterService.surroundMessage("-","Pilacoins found per difficulty") + Colors.ANSI_RESET);
        pilaCoinsFoundPerDifficulty.entrySet().stream()
                .sorted((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()))
                .forEach(entry -> {
                    BigInteger k = entry.getKey();
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
