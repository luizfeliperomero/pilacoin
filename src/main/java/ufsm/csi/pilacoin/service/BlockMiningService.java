package ufsm.csi.pilacoin.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import ufsm.csi.pilacoin.constants.Colors;
import ufsm.csi.pilacoin.model.Block;
import ufsm.csi.pilacoin.model.BlockObserver;
import ufsm.csi.pilacoin.model.DifficultyObserver;
import ufsm.csi.pilacoin.shared.SharedResources;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Random;

@Service
public class BlockMiningService implements Runnable, BlockObserver, DifficultyObserver {
    private Block block;
    private final ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
    private final SharedResources sharedResources;
    private BigInteger difficulty;
    private final RabbitService rabbitService;

    public BlockMiningService(SharedResources sharedResources, RabbitService rabbitService) {
        this.sharedResources = sharedResources;
        this.rabbitService = rabbitService;
    }


    @Override
    public void update(Block block) {
        this.block = block;
    }

    @SneakyThrows
    @Override
    public void run() {
        BigInteger hash;
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        String json = "";
        int count = 0;
        Random random = new Random();
        this.block.setChaveUsuarioMinerador(this.sharedResources.getPublicKey().toString().getBytes(StandardCharsets.UTF_8));
        while (true) {
            byte[] byteArray = new byte[256 / 8];
            random.nextBytes(byteArray);
            this.block.setNonce(new BigInteger(md.digest(byteArray)).abs());
            json = objectWriter.writeValueAsString(this.block);
            hash = new BigInteger(md.digest(json.getBytes(StandardCharsets.UTF_8))).abs();
            count++;
            if(this.difficulty != null && hash.compareTo(this.difficulty) < 0) {
                System.out.printf( MessageFormatterService.threadIdentifierMessage(Thread.currentThread()) + Colors.BLACK_BACKGROUND + "Block found in " + Colors.WHITE_BOLD_BRIGHT + "%,d" + " tries" + Colors.ANSI_RESET + "\n", count);
                System.out.println(json);
                this.rabbitService.send("bloco-minerado", json);
            }
        }
    }

    @Override
    public void update(BigInteger difficulty) {
        this.difficulty = difficulty;
    }
}
