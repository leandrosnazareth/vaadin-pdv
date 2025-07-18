package com.leandrosnazareth.venda.domain;

import com.leandrosnazareth.base.domain.AbstractEntity;
import com.leandrosnazareth.produto.domain.Produto;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;

@Entity
@Table(name = "item_venda", indexes = {
    @Index(name = "idx_item_venda_venda", columnList = "venda_id"),
    @Index(name = "idx_item_venda_produto", columnList = "produto_id")
})
public class ItemVenda extends AbstractEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "item_venda_seq")
    @SequenceGenerator(name = "item_venda_seq", sequenceName = "item_venda_seq", allocationSize = 1)
    @Column(name = "item_venda_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venda_id", nullable = false)
    @NotNull(message = "Venda é obrigatória")
    private Venda venda;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "produto_id", nullable = false)
    @NotNull(message = "Produto é obrigatório")
    private Produto produto;

    @Column(name = "quantidade", nullable = false)
    @NotNull(message = "Quantidade é obrigatória")
    @Min(value = 1, message = "Quantidade deve ser maior que zero")
    private Integer quantidade;

    @Column(name = "preco_unitario", precision = 10, scale = 2, nullable = false)
    @NotNull(message = "Preço unitário é obrigatório")
    @DecimalMin(value = "0.00", message = "Preço unitário deve ser maior ou igual a zero")
    private BigDecimal precoUnitario;

    @Column(name = "subtotal", precision = 10, scale = 2, nullable = false)
    @NotNull(message = "Subtotal é obrigatório")
    @DecimalMin(value = "0.00", message = "Subtotal deve ser maior ou igual a zero")
    private BigDecimal subtotal;

    public ItemVenda() {
        this.quantidade = 1;
        this.precoUnitario = BigDecimal.ZERO;
        this.subtotal = BigDecimal.ZERO;
    }

    public ItemVenda(Produto produto, Integer quantidade, BigDecimal precoUnitario) {
        this();
        this.produto = produto;
        this.quantidade = quantidade;
        this.precoUnitario = precoUnitario;
        calcularSubtotal();
    }

    @Override
    public @Nullable Long getId() {
        return id;
    }

    public Venda getVenda() {
        return venda;
    }

    public void setVenda(Venda venda) {
        this.venda = venda;
    }

    public Produto getProduto() {
        return produto;
    }

    public void setProduto(Produto produto) {
        this.produto = produto;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
        calcularSubtotal();
    }

    public BigDecimal getPrecoUnitario() {
        return precoUnitario;
    }

    public void setPrecoUnitario(BigDecimal precoUnitario) {
        this.precoUnitario = precoUnitario;
        calcularSubtotal();
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public void calcularSubtotal() {
        if (quantidade != null && precoUnitario != null) {
            this.subtotal = precoUnitario.multiply(BigDecimal.valueOf(quantidade));
        } else {
            this.subtotal = BigDecimal.ZERO;
        }
    }

    public void incrementarQuantidade() {
        this.quantidade++;
        calcularSubtotal();
    }

    public void decrementarQuantidade() {
        if (quantidade > 1) {
            this.quantidade--;
            calcularSubtotal();
        }
    }

    public boolean podeDecrementar() {
        return quantidade > 1;
    }

    public String getDescricaoItem() {
        return String.format("%s - %dx R$ %.2f", 
            produto.getNome(), quantidade, precoUnitario);
    }

    @PrePersist
    @PreUpdate
    protected void onSave() {
        calcularSubtotal();
    }
}
