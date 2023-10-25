package ufsm.csi.pilacoin.model;

import lombok.Data;

import java.math.BigInteger;
import java.util.List;

@Data
public class Block {
    private Long numeroBloco;
    private BigInteger nonce;
    private BigInteger nonceBlocoAnterior;
    private byte[] chaveUsuarioMinerador;
    private List<Transaction> transacoes;
}
