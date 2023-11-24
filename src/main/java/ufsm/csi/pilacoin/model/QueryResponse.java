package ufsm.csi.pilacoin.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QueryResponse {
    Long idQuery;
    String usuario;
    List<QueryResponsePila> pilasResult;
    List<Block> blocosResult;
    List<Usuario> usuariosResult;
}
