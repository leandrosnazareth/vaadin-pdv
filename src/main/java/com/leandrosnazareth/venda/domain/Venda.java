package com.leandrosnazareth.venda.domain;

import com.leandrosnazareth.base.domain.AbstractEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidade que representa uma venda no sistema PDV.
 * <p>
 * Esta entidade contém todas as informações de uma venda,
 * incluindo itens, valores totais e forma de pagamento.
 * </p>
 */
@Entity
@Table(name = "venda", indexes = {
    @Index(name = "idx_venda_data", columnList = "data_venda"),
    @Index(name = "idx_venda_status", columnList = "status")
})
public class Venda extends AbstractEntity<Long> {

    public static final int OBSERVACOES_MAX_LENGTH = 500;

    public enum StatusVenda {
        PENDENTE("Pendente"),
        FINALIZADA("Finalizada"),
        CANCELADA("Cancelada");

        private final String descricao;

        StatusVenda(String descricao) {
            this.descricao = descricao;
        }

        public String getDescricao() {
            return descricao;
        }
    }

    public enum FormaPagamento {
        DINHEIRO("Dinheiro"),
        CARTAO_CREDITO("Cartão de Crédito"),
        CARTAO_DEBITO("Cartão de Débito"),
        PIX("PIX"),
        MISTO("Misto");

        private final String descricao;

        FormaPagamento(String descricao) {
            this.descricao = descricao;
        }

        public String getDescricao() {
            return descricao;
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "venda_seq")
    @SequenceGenerator(name = "venda_seq", sequenceName = "venda_seq", allocationSize = 1)
    @Column(name = "venda_id")
    private Long id;

    @Column(name = "data_venda", nullable = false)
    @NotNull(message = "Data da venda é obrigatória")
    private LocalDateTime dataVenda;

    @Column(name = "valor_total", precision = 10, scale = 2, nullable = false)
    @NotNull(message = "Valor total é obrigatório")
    @DecimalMin(value = "0.00", message = "Valor total deve ser maior ou igual a zero")
    private BigDecimal valorTotal;

    @Column(name = "desconto", precision = 10, scale = 2)
    @DecimalMin(value = "0.00", message = "Desconto deve ser maior ou igual a zero")
    private BigDecimal desconto;

    @Column(name = "valor_recebido", precision = 10, scale = 2)
    @DecimalMin(value = "0.00", message = "Valor recebido deve ser maior ou igual a zero")
    private BigDecimal valorRecebido;

    @Column(name = "troco", precision = 10, scale = 2)
    @DecimalMin(value = "0.00", message = "Troco deve ser maior ou igual a zero")
    private BigDecimal troco;

    @Enumerated(EnumType.STRING)
    @Column(name = "forma_pagamento", nullable = false)
    @NotNull(message = "Forma de pagamento é obrigatória")
    private FormaPagamento formaPagamento;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @NotNull(message = "Status da venda é obrigatório")
    private StatusVenda status;

    @Column(name = "observacoes", length = OBSERVACOES_MAX_LENGTH)
    @Size(max = OBSERVACOES_MAX_LENGTH, message = "Observações deve ter no máximo {max} caracteres")
    private String observacoes;

    @OneToMany(mappedBy = "venda", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<ItemVenda> itens = new ArrayList<>();

    /**
     * Construtor padrão para JPA.
     */
    public Venda() {
        this.dataVenda = LocalDateTime.now();
        this.status = StatusVenda.PENDENTE;
        this.valorTotal = BigDecimal.ZERO;
        this.desconto = BigDecimal.ZERO;
        this.valorRecebido = BigDecimal.ZERO;
        this.troco = BigDecimal.ZERO;
        this.formaPagamento = FormaPagamento.DINHEIRO; // Valor padrão
    }

    /**
     * Construtor para criar uma venda básica.
     */
    public Venda(FormaPagamento formaPagamento) {
        this();
        this.formaPagamento = formaPagamento;
    }

    @Override
    public @Nullable Long getId() {
        return id;
    }

    public LocalDateTime getDataVenda() {
        return dataVenda;
    }

    public void setDataVenda(LocalDateTime dataVenda) {
        this.dataVenda = dataVenda;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public @Nullable BigDecimal getDesconto() {
        return desconto;
    }

    public void setDesconto(@Nullable BigDecimal desconto) {
        this.desconto = desconto;
    }

    public @Nullable BigDecimal getValorRecebido() {
        return valorRecebido;
    }

    public void setValorRecebido(@Nullable BigDecimal valorRecebido) {
        this.valorRecebido = valorRecebido;
    }

    public @Nullable BigDecimal getTroco() {
        return troco;
    }

    public void setTroco(@Nullable BigDecimal troco) {
        this.troco = troco;
    }

    public FormaPagamento getFormaPagamento() {
        return formaPagamento;
    }

    public void setFormaPagamento(FormaPagamento formaPagamento) {
        this.formaPagamento = formaPagamento;
    }

    public StatusVenda getStatus() {
        return status;
    }

    public void setStatus(StatusVenda status) {
        this.status = status;
    }

    public @Nullable String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(@Nullable String observacoes) {
        this.observacoes = observacoes;
    }

    public List<ItemVenda> getItens() {
        return itens;
    }

    public void setItens(List<ItemVenda> itens) {
        this.itens = itens;
    }

    /**
     * Adiciona um item à venda.
     * @param item item a ser adicionado
     */
    public void adicionarItem(ItemVenda item) {
        item.setVenda(this);
        this.itens.add(item);
        recalcularTotal();
    }

    /**
     * Remove um item da venda.
     * @param item item a ser removido
     */
    public void removerItem(ItemVenda item) {
        this.itens.remove(item);
        recalcularTotal();
    }

    /**
     * Recalcula o valor total da venda baseado nos itens.
     */
    public void recalcularTotal() {
        BigDecimal total = itens.stream()
            .map(ItemVenda::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        if (desconto != null && desconto.compareTo(BigDecimal.ZERO) > 0) {
            total = total.subtract(desconto);
        }
        
        this.valorTotal = total.max(BigDecimal.ZERO);
    }

    /**
     * Calcula o troco baseado no valor recebido.
     */
    public void calcularTroco() {
        if (valorRecebido != null && valorRecebido.compareTo(valorTotal) >= 0) {
            this.troco = valorRecebido.subtract(valorTotal);
        } else {
            this.troco = BigDecimal.ZERO;
        }
    }

    /**
     * Finaliza a venda.
     */
    public void finalizar() {
        this.status = StatusVenda.FINALIZADA;
        calcularTroco();
    }

    /**
     * Cancela a venda.
     */
    public void cancelar() {
        this.status = StatusVenda.CANCELADA;
    }

    /**
     * Verifica se a venda pode ser finalizada.
     * @return true se pode ser finalizada
     */
    public boolean podeSerFinalizada() {
        return !itens.isEmpty() && 
               status == StatusVenda.PENDENTE && 
               valorTotal.compareTo(BigDecimal.ZERO) > 0 &&
               valorRecebido != null &&
               valorRecebido.compareTo(valorTotal) >= 0;
    }

    /**
     * Retorna o número de itens na venda.
     * @return quantidade de itens
     */
    public int getQuantidadeItens() {
        return itens.size();
    }

    /**
     * Retorna a quantidade total de produtos na venda.
     * @return quantidade total de produtos
     */
    public int getQuantidadeTotalProdutos() {
        return itens.stream()
            .mapToInt(ItemVenda::getQuantidade)
            .sum();
    }
}
