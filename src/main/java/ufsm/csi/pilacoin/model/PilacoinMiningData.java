package ufsm.csi.pilacoin.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.util.Date;
import java.util.Map;

@Data
@Entity
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class PilacoinMiningData {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long pilacoinMiningDataId;
    @ElementCollection
    private Map<String, Integer> pilacoinsFoundPerThread;
    @ElementCollection
    private Map<BigInteger, Integer> pilacoinsFoundPerDifficulty;
    private String timeElapsed;
    private int pilacoins_mined;
    private Date date;
}
