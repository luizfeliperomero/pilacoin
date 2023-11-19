package ufsm.csi.pilacoin.service;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.SneakyThrows;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Service;
import ufsm.csi.pilacoin.constants.AppInfo;
import ufsm.csi.pilacoin.constants.Colors;
import ufsm.csi.pilacoin.model.Difficulty;
import ufsm.csi.pilacoin.model.DifficultyObservable;
import ufsm.csi.pilacoin.model.DifficultyObserver;
import ufsm.csi.pilacoin.model.PilacoinMiningData;
import ufsm.csi.pilacoin.shared.SharedResources;

import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@Service
@Data
public class DifficultyService implements DifficultyObservable {
    private RabbitService rabbitService;
    private boolean firstPilaSent = false;
    private boolean threadsAlreadyStarted = false;
    private BigInteger currentDifficulty;
    private final SharedResources sharedResources;
    private final PilaCoinService pilaCoinService;
    private Long miningStartTime;
    private Long miningEndTime;
    private Long miningPeriod;
    private Long miningPeriodHours;
    private Long miningPeriodMinutes;
    private Long miningPeriodSeconds;
    private BigInteger prevDifficulty;
    private boolean isFirstDifficulty = true;
    private Date miningDate;
    private List<MiningService> pilacoinMiningServices = new ArrayList<>();
    private List<DifficultyObserver> observers = new ArrayList<>();
    private final PilacoinMiningDataService pilacoinMiningDataService;
    DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    private final Thread shutdownThread = new Thread(() -> {
        this.miningPeriod = this.miningEndTime - this.miningStartTime;
        this.miningPeriodSeconds = this.miningPeriod / 1000;
        this.miningPeriodMinutes = this.miningPeriodSeconds / 60;
        this.miningPeriodHours = this.miningPeriodMinutes / 60;
        if(this.miningPeriodSeconds < 0 || this.miningPeriodSeconds >= 60) {
            if(this.miningPeriodSeconds > 60) {
               this.miningPeriodSeconds = this.miningPeriodSeconds % 60;
            } else this.miningPeriodSeconds = 0L;
        }
        if(this.miningPeriodMinutes < 0 || this.miningPeriodMinutes >= 60) {
            if(this.miningPeriodMinutes > 60) {
                this.miningPeriodMinutes = this.miningPeriodMinutes % 60;
            } else this.miningPeriodMinutes = 0L;
        }
        if(this.miningPeriodHours < 0) {
            this.miningPeriodSeconds = 0L;
        }
        System.out.println(Colors.ANSI_PURPLE + "Mining Time: " + Colors.ANSI_RESET + Colors.WHITE_BOLD_BRIGHT + MessageFormatterService.formattedTimeMessage(this.miningPeriodHours, this.miningPeriodMinutes, this.miningPeriodSeconds) + Colors.ANSI_RESET);
    });
    {
        Runtime.getRuntime().addShutdownHook(shutdownThread);
    }

    public DifficultyService(RabbitService rabbitService, SharedResources sharedResources, PilaCoinService pilaCoinService, PilacoinMiningDataService pilacoinMiningDataService) {
        this.rabbitService = rabbitService;
        this.sharedResources = sharedResources;
        this.pilaCoinService = pilaCoinService;
        this.pilacoinMiningDataService = pilacoinMiningDataService;
        this.observers.add(this.rabbitService);
    }
    @SneakyThrows
    @RabbitListener(queues = {"${queue.dificuldade}"})
    public void getDifficulty(@Payload String difJson) {
        ObjectMapper objectMapper = new ObjectMapper();
        Difficulty dif = objectMapper.readValue(difJson, Difficulty.class);
        setCurrentDifficulty(new BigInteger(dif.getDificuldade(), 16));
        if(!currentDifficulty.equals(this.prevDifficulty) && !isFirstDifficulty) {
            System.out.println(Colors.ANSI_CYAN + MessageFormatterService.surroundMessage("-", "Difficulty Changed: " + currentDifficulty) + Colors.ANSI_RESET);
        }
        if(isFirstDifficulty) {
            System.out.println(Colors.ANSI_YELLOW + MessageFormatterService.surroundMessage("-", "Difficulty Received: " + currentDifficulty) + Colors.ANSI_RESET);
            isFirstDifficulty = false;
        }
        this.sharedResources.getDifficultyLatch().countDown();
        prevDifficulty = currentDifficulty;
    }

    @SneakyThrows
    public void startMining() {
        if(this.currentDifficulty != null && !threadsAlreadyStarted) {
            this.sharedResources.getDifficultyLatch().await();
            this.startMiningThreads(AppInfo.MINING_THREADS_NUMBER);
            this.threadsAlreadyStarted = true;
        }
    }

    @SendTo("/topic/difficulty")
    public BigInteger setCurrentDifficulty(BigInteger difficulty) {
       this.currentDifficulty = difficulty;
       if(observers.size() != 0) {
           observers.forEach(observer -> observer.update(this.currentDifficulty));
       }
       return this.currentDifficulty;
    }

    @Override
    public void subscribe(DifficultyObserver difficultyObserver) {
        observers.add(difficultyObserver);
    }

    @Override
    public void unsubscribe(DifficultyObserver difficultyObserver) {
        observers.remove(difficultyObserver);
    }

    public void stopMining() {
        this.miningEndTime = System.currentTimeMillis();
        long duration = Math.abs(this.miningStartTime - this.miningEndTime);
        String timeElapsed = String.format("%02d h, %02d min, %02d sec",
                TimeUnit.MILLISECONDS.toHours(duration),
                TimeUnit.MILLISECONDS.toMinutes(duration),
                TimeUnit.MILLISECONDS.toSeconds(duration)
                ) ;
        PilacoinMiningData pilacoinMiningData = PilacoinMiningData.builder()
                .pilacoins_mined(this.sharedResources.pilacoinsFound)
                .pilacoinsFoundPerDifficulty(this.sharedResources.getPilaCoinsFoundPerDifficulty())
                .pilacoinsFoundPerThread(this.sharedResources.getPilaCoinsFoundPerThread())
                .timeElapsed(timeElapsed)
                .date(this.miningDate)
                .build();
       this.pilacoinMiningDataService.save(pilacoinMiningData);
       this.pilacoinMiningServices.forEach(MiningService::stopMining);
       this.pilacoinMiningServices.clear();
       this.sharedResources.getPilaCoinsFoundPerDifficulty().clear();
       this.sharedResources.getPilaCoinsFoundPerThread().clear();
       this.sharedResources.setPilacoinsFound(0);
       this.sharedResources.setFirstPilacoinsTotalSent(false);
       this.sharedResources.setFirstPilacoinsFoundPerThreadSent(false);
       this.sharedResources.setFirstPilacoinsFoundPerDifficultySent(false);
       this.threadsAlreadyStarted = false;
    }

    public void startMiningThreads(int threads) {
        this.miningDate = new Date(System.currentTimeMillis());
        if(!firstPilaSent) {
            this.rabbitService.send("pila-minerado", "");
            this.firstPilaSent = true;
        }
        this.miningStartTime = System.currentTimeMillis();
        IntStream.range(0, threads)
                .mapToObj(i -> {
                            MiningService ms = new MiningService(this.rabbitService, pilaCoinService, sharedResources);
                            this.pilacoinMiningServices.add(ms);
                            return ms;
                        }
                )
                .peek(miningService -> {
                    this.subscribe(miningService);
                    miningService.update(this.currentDifficulty);
                })
                .forEach(miningService -> new Thread(miningService).start());
    }
}
