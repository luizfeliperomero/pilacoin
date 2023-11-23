package ufsm.csi.pilacoin.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QueryResponse {
    Long idQuery;
    String usuario;
    PilaCoin[] pilasResult;
    Block[] blocosResult;
    String[] usuariosResult;
}
