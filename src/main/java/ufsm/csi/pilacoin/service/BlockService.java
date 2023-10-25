package ufsm.csi.pilacoin.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import lombok.SneakyThrows;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import ufsm.csi.pilacoin.Constants;
import ufsm.csi.pilacoin.model.Block;
import ufsm.csi.pilacoin.model.BlockObservable;
import ufsm.csi.pilacoin.model.BlockObserver;
import ufsm.csi.pilacoin.shared.SharedResources;

import java.util.List;
import java.util.stream.IntStream;

@Service
public class BlockService implements BlockObservable {
    private final ObjectReader objectReader = new ObjectMapper().reader();
    private List<BlockObserver> observers;
    private final DifficultyService difficultyService;
    private final RabbitService rabbitService;
    private Block currentBlock;
    private boolean miningThreadsStarted = false;

    public BlockService(DifficultyService difficultyService, RabbitService rabbitService) {
        this.difficultyService = difficultyService;
        this.rabbitService = rabbitService;
    }

    public void startBlockMiningThreads(int threads) {
        IntStream.range(0, threads)
                .mapToObj(i -> new BlockMiningService(SharedResources.getInstance(), this.rabbitService))
                .peek(miningService -> {
                    this.subscribe(miningService);
                    this.difficultyService.subscribe(miningService);
                    miningService.update(this.currentBlock);
                    miningService.update(this.difficultyService.getCurrentDifficulty());
                })
                .forEach(miningService -> new Thread(miningService).start());
    }

    @SneakyThrows
    @RabbitListener(queues = {"descobre-bloco"})
    public void findBlocks(@Payload String blockStr) {
        this.currentBlock = this.objectReader.readValue(blockStr, Block.class);
        if(!this.miningThreadsStarted) {
            this.startBlockMiningThreads(Constants.MINING_THREADS_NUMBER);
            this.miningThreadsStarted = true;
        }
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
