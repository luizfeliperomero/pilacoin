package ufsm.csi.pilacoin.shared;

import lombok.Data;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import ufsm.csi.pilacoin.constants.Colors;
import ufsm.csi.pilacoin.service.MessageFormatterService;

import java.math.BigInteger;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@Data
public class SharedResources {
    private Map<BigInteger, Integer> pilaCoinsFoundPerDifficulty = new HashMap<BigInteger, Integer>();
    private Map<String, Integer> pilaCoinsFoundPerThread = new HashMap<String, Integer>();
    private final Object lock = new Object();
    private static volatile SharedResources  instance;
    private final AtomicBoolean alreadyExecutedShutdown = new AtomicBoolean(false);
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
    }

    public synchronized void updatePilaCoinsFoundPerThread(String threadName) {
        pilaCoinsFoundPerThread.merge(threadName, 1, Integer::sum);
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
