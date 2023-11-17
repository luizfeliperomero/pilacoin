package ufsm.csi.pilacoin.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.SneakyThrows;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import ufsm.csi.pilacoin.constants.AppInfo;
import ufsm.csi.pilacoin.model.Block;
import ufsm.csi.pilacoin.model.BlockObservable;
import ufsm.csi.pilacoin.model.BlockObserver;
import ufsm.csi.pilacoin.model.BlocoValidado;
import ufsm.csi.pilacoin.shared.SharedResources;

import javax.crypto.Cipher;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@Service
public class BlockService implements BlockObservable {
    private final ObjectReader objectReader = new ObjectMapper().reader();
    private List<BlockObserver> observers = new ArrayList<>();
    private final DifficultyService difficultyService;
    private final RabbitService rabbitService;
    private final SharedResources sharedResources;
    private final ObjectWriter objectWriter = new ObjectMapper().writer();
    private List<BlockMiningService> blockMiningServices = new ArrayList<>();
    private Block currentBlock;
    private boolean miningThreadsStarted = false;

    public BlockService(DifficultyService difficultyService, RabbitService rabbitService, SharedResources sharedResources) {
        this.difficultyService = difficultyService;
        this.rabbitService = rabbitService;
        this.sharedResources = sharedResources;
    }

    public void stopBlockMiningThreads() {
        this.blockMiningServices.forEach(BlockMiningService::stopMining);
        this.miningThreadsStarted = false;
    }

    @SneakyThrows
    public void startBlockMiningThreads() {
        if(!this.miningThreadsStarted) {
            this.sharedResources.getDifficultyLatch().await();
            IntStream.range(0, AppInfo.MINING_THREADS_NUMBER)
                    .mapToObj(i -> {
                        BlockMiningService bms = new BlockMiningService(SharedResources.getInstance(), this.rabbitService);
                        this.blockMiningServices.add(bms);
                        return bms;
                    }
                    )
                    .peek(miningService -> {
                        this.subscribe(miningService);
                        this.difficultyService.subscribe(miningService);
                        miningService.update(this.currentBlock);
                        miningService.update(this.difficultyService.getCurrentDifficulty());
                    })
                    .forEach(miningService -> new Thread(miningService).start());
            this.miningThreadsStarted = true;
        }
    }

    @SneakyThrows
    @RabbitListener(queues = {"descobre-bloco"})
    public void findBlocks(@Payload String blockStr) {
        this.currentBlock = this.objectReader.readValue(blockStr, Block.class);
    }

    @SneakyThrows
    @RabbitListener(queues = {"bloco-minerado"})
    public void validateBlock(@Payload String blockStr) {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        BigInteger hash = new BigInteger(md.digest(blockStr.getBytes(StandardCharsets.UTF_8))).abs();
        Block block = this.objectReader.readValue(blockStr, Block.class);
        if(hash.compareTo(this.difficultyService.getCurrentDifficulty()) < 0) {
            Cipher encryptCipher = Cipher.getInstance("RSA");
            encryptCipher.init(Cipher.ENCRYPT_MODE, this.sharedResources.getPrivateKey());
            byte[] hashByteArr = hash.toString().getBytes(StandardCharsets.UTF_8);
            BlocoValidado blocoValidado = BlocoValidado.builder()
                    .nomeValidador(AppInfo.DEFAULT_NAME)
                    .bloco(block)
                    .assinaturaBloco(encryptCipher.doFinal(hashByteArr))
                    .chavePublicaValidador(this.sharedResources.getPublicKey().toString().getBytes(StandardCharsets.UTF_8))
                    .build();
            String json = this.objectWriter.writeValueAsString(blocoValidado);
            this.rabbitService.send("bloco-validado", json);
        } else this.rabbitService.send("bloco-minerado", blockStr);
    }

    @Override
    public void subscribe(BlockObserver blockObserver) {
        this.observers.add(blockObserver);
    }

    @Override
    public void unsubscribe(BlockObserver blockObserver) {
        this.observers.remove(blockObserver);
    }
}
