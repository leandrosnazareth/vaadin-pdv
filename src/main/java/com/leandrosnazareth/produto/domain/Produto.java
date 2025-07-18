package com.leandrosnazareth.produto.domain;

import com.leandrosnazareth.base.domain.AbstractEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

@Entity
@Table(name = "produto", indexes = {
    @Index(name = "idx_produto_codigo", columnList = "codigo"),
    @Index(name = "idx_produto_nome", columnList = "nome"),
    @Index(name = "idx_produto_categoria", columnList = "categoria")
})
public class Produto extends AbstractEntity<Long> {

    public static final int CODIGO_MAX_LENGTH = 50;
    public static final int NOME_MAX_LENGTH = 200;
    public static final int DESCRICAO_MAX_LENGTH = 500;
    public static final int CATEGORIA_MAX_LENGTH = 100;
    public static final int UNIDADE_MAX_LENGTH = 10;
    public static final int MARCA_MAX_LENGTH = 100;
    public static final int FORNECEDOR_MAX_LENGTH = 150;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "produto_seq")
    @SequenceGenerator(name = "produto_seq", sequenceName = "produto_seq", allocationSize = 1)
    @Column(name = "produto_id")
    private Long id;

    @Column(name = "codigo", unique = true, nullable = false, length = CODIGO_MAX_LENGTH)
    @NotBlank(message = "Código é obrigatório")
    @Size(max = CODIGO_MAX_LENGTH, message = "Código deve ter no máximo {max} caracteres")
    private String codigo;

    @Column(name = "nome", nullable = false, length = NOME_MAX_LENGTH)
    @NotBlank(message = "Nome é obrigatório")
    @Size(max = NOME_MAX_LENGTH, message = "Nome deve ter no máximo {max} caracteres")
    private String nome;

    @Column(name = "descricao", length = DESCRICAO_MAX_LENGTH)
    @Size(max = DESCRICAO_MAX_LENGTH, message = "Descrição deve ter no máximo {max} caracteres")
    private String descricao;

    @Column(name = "categoria", length = CATEGORIA_MAX_LENGTH)
    @Size(max = CATEGORIA_MAX_LENGTH, message = "Categoria deve ter no máximo {max} caracteres")
    private String categoria;

    @Column(name = "marca", length = MARCA_MAX_LENGTH)
    @Size(max = MARCA_MAX_LENGTH, message = "Marca deve ter no máximo {max} caracteres")
    private String marca;

    @Column(name = "fornecedor", length = FORNECEDOR_MAX_LENGTH)
    @Size(max = FORNECEDOR_MAX_LENGTH, message = "Fornecedor deve ter no máximo {max} caracteres")
    private String fornecedor;

    @Column(name = "preco_compra", precision = 10, scale = 2)
    @DecimalMin(value = "0.00", message = "Preço de compra deve ser maior ou igual a zero")
    private BigDecimal precoCompra;

    @Column(name = "preco_venda", precision = 10, scale = 2, nullable = false)
    @NotNull(message = "Preço de venda é obrigatório")
    @DecimalMin(value = "0.01", message = "Preço de venda deve ser maior que zero")
    private BigDecimal precoVenda;

    @Column(name = "estoque_atual", nullable = false)
    @NotNull(message = "Estoque atual é obrigatório")
    @Min(value = 0, message = "Estoque atual deve ser maior ou igual a zero")
    private Integer estoqueAtual;

    @Column(name = "estoque_minimo")
    @Min(value = 0, message = "Estoque mínimo deve ser maior ou igual a zero")
    private Integer estoqueMinimo;

    @Column(name = "estoque_maximo")
    @Min(value = 0, message = "Estoque máximo deve ser maior ou igual a zero")
    private Integer estoqueMaximo;

    @Column(name = "unidade", length = UNIDADE_MAX_LENGTH)
    @Size(max = UNIDADE_MAX_LENGTH, message = "Unidade deve ter no máximo {max} caracteres")
    private String unidade;

    @Column(name = "peso", precision = 8, scale = 3)
    @DecimalMin(value = "0.000", message = "Peso deve ser maior ou igual a zero")
    private BigDecimal peso;

    @Column(name = "ativo", nullable = false)
    @NotNull
    private Boolean ativo;

    @Column(name = "data_criacao", nullable = false)
    @NotNull
    private Instant dataCriacao;

    @Column(name = "data_atualizacao")
    private Instant dataAtualizacao;

    @Column(name = "observacoes", length = DESCRICAO_MAX_LENGTH)
    @Size(max = DESCRICAO_MAX_LENGTH, message = "Observações deve ter no máximo {max} caracteres")
    private String observacoes;

    @Lob
    @Column(name = "foto", columnDefinition = "BLOB")
    private byte[] foto;

    public Produto() {
        this.ativo = true;
        this.estoqueAtual = 0;
        this.estoqueMinimo = 0;
        this.estoqueMaximo = 0;
        this.precoCompra = BigDecimal.ZERO;
        this.precoVenda = BigDecimal.ZERO;
    }

    public Produto(String codigo, String nome, BigDecimal precoVenda) {
        this();
        this.codigo = codigo;
        this.nome = nome;
        this.precoVenda = precoVenda;
    }

    @Override
    public @Nullable Long getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public @Nullable String getDescricao() {
        return descricao;
    }

    public void setDescricao(@Nullable String descricao) {
        this.descricao = descricao;
    }

    public @Nullable String getCategoria() {
        return categoria;
    }

    public void setCategoria(@Nullable String categoria) {
        this.categoria = categoria;
    }

    public @Nullable String getMarca() {
        return marca;
    }

    public void setMarca(@Nullable String marca) {
        this.marca = marca;
    }

    public @Nullable String getFornecedor() {
        return fornecedor;
    }

    public void setFornecedor(@Nullable String fornecedor) {
        this.fornecedor = fornecedor;
    }

    public @Nullable BigDecimal getPrecoCompra() {
        return precoCompra;
    }

    public void setPrecoCompra(@Nullable BigDecimal precoCompra) {
        this.precoCompra = precoCompra;
    }

    public BigDecimal getPrecoVenda() {
        return precoVenda;
    }

    public void setPrecoVenda(BigDecimal precoVenda) {
        this.precoVenda = precoVenda;
    }

    public Integer getEstoqueAtual() {
        return estoqueAtual;
    }

    public void setEstoqueAtual(Integer estoqueAtual) {
        this.estoqueAtual = estoqueAtual;
    }

    public @Nullable Integer getEstoqueMinimo() {
        return estoqueMinimo;
    }

    public void setEstoqueMinimo(@Nullable Integer estoqueMinimo) {
        this.estoqueMinimo = estoqueMinimo;
    }

    public @Nullable Integer getEstoqueMaximo() {
        return estoqueMaximo;
    }

    public void setEstoqueMaximo(@Nullable Integer estoqueMaximo) {
        this.estoqueMaximo = estoqueMaximo;
    }

    public @Nullable String getUnidade() {
        return unidade;
    }

    public void setUnidade(@Nullable String unidade) {
        this.unidade = unidade;
    }

    public @Nullable BigDecimal getPeso() {
        return peso;
    }

    public void setPeso(@Nullable BigDecimal peso) {
        this.peso = peso;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public Instant getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(Instant dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public @Nullable Instant getDataAtualizacao() {
        return dataAtualizacao;
    }

    public void setDataAtualizacao(@Nullable Instant dataAtualizacao) {
        this.dataAtualizacao = dataAtualizacao;
    }

    public @Nullable String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(@Nullable String observacoes) {
        this.observacoes = observacoes;
    }

    public boolean isEstoqueBaixo() {
        return estoqueMinimo != null && estoqueAtual != null && estoqueAtual <= estoqueMinimo;
    }

    public @Nullable BigDecimal calcularMargemLucro() {
        if (precoCompra == null || precoCompra.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        
        BigDecimal lucro = precoVenda.subtract(precoCompra);
        return lucro.divide(precoCompra, 4, RoundingMode.HALF_UP)
                   .multiply(BigDecimal.valueOf(100));
    }

    public boolean atualizarEstoque(int quantidade) {
        int novoEstoque = estoqueAtual + quantidade;
        if (novoEstoque < 0) {
            return false;
        }
        estoqueAtual = novoEstoque;
        return true;
    }

    public byte @Nullable [] getFoto() {
        return foto;
    }

    public void setFoto(byte @Nullable [] foto) {
        this.foto = foto;
    }

    @PrePersist
    protected void onCreate() {
        dataCriacao = Instant.now();
        dataAtualizacao = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        dataAtualizacao = Instant.now();
    }
}
