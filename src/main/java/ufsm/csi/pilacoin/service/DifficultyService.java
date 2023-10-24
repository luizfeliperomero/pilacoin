package ufsm.csi.pilacoin.service;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import ufsm.csi.pilacoin.Constants;
import ufsm.csi.pilacoin.model.Difficulty;
import ufsm.csi.pilacoin.model.DifficultyObservable;
import ufsm.csi.pilacoin.model.DifficultyObserver;
import ufsm.csi.pilacoin.shared.SharedResources;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.IntStream;

@Service
public class DifficultyService implements DifficultyObservable {
    private RabbitService rabbitService;
    private boolean firstPilaSent = false;
    private boolean threadsAlreadyStarted = false;
    private BigInteger currentDifficulty;
    private final SharedResources sharedResources;
    private BigInteger prevDifficulty;
    private boolean isFirstDifficulty = true;
    private List<DifficultyObserver> observers = new ArrayList<>();

    public DifficultyService(RabbitService rabbitService, SharedResources sharedResources) {
        this.rabbitService = rabbitService;
        this.sharedResources = sharedResources;
    }
    @SneakyThrows
    @RabbitListener(queues = {"${queue.dificuldade}"})
    public void getDifficulty(@Payload String difJson) {
        ObjectMapper objectMapper = new ObjectMapper();
        Difficulty dif = objectMapper.readValue(difJson, Difficulty.class);
        setCurrentDifficulty(new BigInteger(dif.getDificuldade(), 16));
        if(!currentDifficulty.equals(this.prevDifficulty) && !isFirstDifficulty) {
            System.out.println(Constants.ANSI_CYAN + MessageFormatterService.surroundMessage("-", "Difficulty Changed: " + currentDifficulty) + Constants.ANSI_RESET);
        }
        if(isFirstDifficulty) {
            System.out.println(Constants.ANSI_YELLOW + MessageFormatterService.surroundMessage("-", "Difficulty Received: " + currentDifficulty) + Constants.ANSI_RESET);
            isFirstDifficulty = false;
        }
        prevDifficulty = currentDifficulty;
        if(!threadsAlreadyStarted) {
            this.startMiningThreads(Constants.MINING_THREADS_NUMBER);
            this.threadsAlreadyStarted = true;
        }
    }

    public void setCurrentDifficulty(BigInteger difficulty) {
       this.currentDifficulty = difficulty;
       if(observers.size() != 0) {
           observers.forEach(observer -> observer.update(this.currentDifficulty));
       }
    }

    @Override
    public void subscribe(DifficultyObserver difficultyObserver) {
        observers.add(difficultyObserver);
    }

    @Override
    public void unsubscribe(DifficultyObserver difficultyObserver) {
        observers.remove(difficultyObserver);
    }

    public void startMiningThreads(int threads) {
        if(!firstPilaSent) {
            this.rabbitService.send("pila-minerado", "");
            this.firstPilaSent = true;
        }
        IntStream.range(0, threads)
                .mapToObj(i -> new MiningService(this.rabbitService, sharedResources))
                .peek(miningService -> {
                    this.subscribe(miningService);
                    miningService.update(this.currentDifficulty);
                })
                .forEach(miningService -> new Thread(miningService).start());
    }
}
