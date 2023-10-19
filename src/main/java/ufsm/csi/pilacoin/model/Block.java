package ufsm.csi.pilacoin.model;

import lombok.Data;

import java.util.List;

@Data
public class Block {
    private Long numeroBloco;
    private String nonce;
    private byte[] chaveUsuarioMinerador;
    private List<Object> transacoes;
}
