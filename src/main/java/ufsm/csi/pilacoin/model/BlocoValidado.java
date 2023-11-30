package ufsm.csi.pilacoin.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BlocoValidado {
    String nomeValidador;
    byte[] chavePublicaValidador;
    byte[] assinaturaBloco;
    Block bloco;
}
