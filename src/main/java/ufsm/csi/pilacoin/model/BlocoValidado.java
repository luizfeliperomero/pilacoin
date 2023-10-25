package ufsm.csi.pilacoin.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BlocoValidado {
    String nomeValidador;
    byte[] chavePublicaValidador;
    byte[] assinaturaBloco;
    Block bloco;
}
