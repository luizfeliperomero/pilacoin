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

import java.math.BigInteger;

@Service
public class DifficultyService {
    private MiningService miningService;
    private boolean threadsAlreadyStarted = false;
    public static BigInteger currentDifficulty;
    private BigInteger prevDifficulty;
    private boolean isFirstDifficulty = true;

    public DifficultyService(MiningService miningService) {
       this.miningService = miningService;
    }
    @SneakyThrows
    @RabbitListener(queues = {"${queue.dificuldade}"})
    public void getDifficulty(@Payload String difJson) {
        ObjectMapper objectMapper = new ObjectMapper();
        Difficulty dif = objectMapper.readValue(difJson, Difficulty.class);
        currentDifficulty = new BigInteger(dif.getDificuldade(), 16);
        if(!currentDifficulty.equals(this.prevDifficulty) && !isFirstDifficulty) {
            System.out.println(Constants.ANSI_CYAN + MessageFormatterService.surroundMessage("-", "Difficulty Changed: " + currentDifficulty) + Constants.ANSI_RESET);
        }
        if(isFirstDifficulty) {
            System.out.println(Constants.ANSI_YELLOW + MessageFormatterService.surroundMessage("-", "Difficulty Received: " + currentDifficulty) + Constants.ANSI_RESET);
            isFirstDifficulty = false;
        }
        prevDifficulty = currentDifficulty;
        if(!threadsAlreadyStarted) {
            this.miningService.startMiningThreads(Constants.MINING_THREADS_NUMBER);
            this.threadsAlreadyStarted = true;
        }
    }

}
