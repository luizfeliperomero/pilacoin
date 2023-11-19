package ufsm.csi.pilacoin.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

import java.util.Date;

@JsonPropertyOrder(alphabetic = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
public class Transaction {
    private String chaveUsuarioOrigem;
    private String chaveUsuarioDestino;
    private String assinatura;
    private String noncePila;
    private Date dataTransacao;
    @Id
    private Long id;
    private String status;
}
