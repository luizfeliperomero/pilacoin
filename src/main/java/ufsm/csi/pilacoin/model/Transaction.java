package ufsm.csi.pilacoin.model;

import java.util.Date;

public class Transaction {
    private String chaveUsuarioOrigem;
    private String chaveUsuarioDestino;
    private String assinatura;
    private String noncePila;
    private Date dataTransacao;
    private Long id;
    private String status;
}
