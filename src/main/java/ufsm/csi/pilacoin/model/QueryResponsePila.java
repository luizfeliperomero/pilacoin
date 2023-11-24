package ufsm.csi.pilacoin.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QueryResponsePila {
    @Id
    private Long id;
    private Date dataCriacao;
    @Column(columnDefinition = "LONGTEXT")
    private String chaveCriador;
    private String nomeCriador;
    private String status;
    @Column(columnDefinition = "LONGTEXT")
    private String noncePila;
    @Column(columnDefinition = "LONGTEXT")
    private String nonce;
    @OneToMany(cascade = CascadeType.PERSIST)
    private List<Transaction> transacoes;
}
