package ufsm.csi.pilacoin.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;

import java.math.BigInteger;
import java.util.List;

@Data
@JsonPropertyOrder(alphabetic = true)
@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
public class Block {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "block_seq")
    @JsonIgnore
    @Column(precision = 38, scale = 0)
    private Long blockId;
    private Long numeroBloco;
    @JsonIgnore
    private boolean minerado;
    @Column(precision = 400, scale = 0)
    private BigInteger nonce;
    @Column(precision = 400, scale = 0)
    private BigInteger nonceBlocoAnterior;
    private byte[] chaveUsuarioMinerador;
    private String nomeUsuarioMinerador;
    @OneToMany(cascade = CascadeType.PERSIST)
    private List<Transaction> transacoes;
}
