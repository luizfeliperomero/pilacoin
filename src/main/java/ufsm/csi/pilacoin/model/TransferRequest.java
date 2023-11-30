package ufsm.csi.pilacoin.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferRequest {
    private Usuario user;
    private PilaCoin pilaCoin;
}
