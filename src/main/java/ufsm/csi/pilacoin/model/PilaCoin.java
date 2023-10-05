package ufsm.csi.pilacoin.model;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Builder
@Data
public class PilaCoin {
    private byte[] chavePublica;
    private String nomeMinerador;
    private Date dataHoraCriacao;
    private byte[] magicNumber;
}
