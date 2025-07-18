package com.leandrosnazareth.venda.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositório para operações de persistência da entidade ItemVenda.
 * <p>
 * Este repositório fornece métodos para realizar operações CRUD e consultas
 * específicas relacionadas aos itens de venda no sistema PDV.
 * </p>
 */
@Repository
public interface ItemVendaRepository extends JpaRepository<ItemVenda, Long> {

    /**
     * Busca itens de venda por venda.
     * @param venda venda
     * @return lista de itens da venda
     */
    List<ItemVenda> findByVenda(Venda venda);

    /**
     * Busca itens de venda por ID da venda.
     * @param vendaId ID da venda
     * @return lista de itens da venda
     */
    List<ItemVenda> findByVendaId(Long vendaId);

    /**
     * Busca itens de venda por produto.
     * @param produtoId ID do produto
     * @return lista de itens do produto
     */
    @Query("SELECT iv FROM ItemVenda iv WHERE iv.produto.id = :produtoId")
    List<ItemVenda> findByProdutoId(@Param("produtoId") Long produtoId);

    /**
     * Busca itens de venda por produto e período.
     * @param produtoId ID do produto
     * @param dataInicio data de início
     * @param dataFim data de fim
     * @return lista de itens do produto no período
     */
    @Query("SELECT iv FROM ItemVenda iv INNER JOIN iv.venda v " +
           "WHERE iv.produto.id = :produtoId AND v.dataVenda BETWEEN :dataInicio AND :dataFim " +
           "AND v.status = 'FINALIZADA'")
    List<ItemVenda> findByProdutoIdAndPeriodo(
        @Param("produtoId") Long produtoId,
        @Param("dataInicio") LocalDateTime dataInicio,
        @Param("dataFim") LocalDateTime dataFim
    );

    /**
     * Calcula a quantidade total vendida de um produto.
     * @param produtoId ID do produto
     * @return quantidade total vendida
     */
    @Query("SELECT COALESCE(SUM(iv.quantidade), 0) FROM ItemVenda iv INNER JOIN iv.venda v " +
           "WHERE iv.produto.id = :produtoId AND v.status = 'FINALIZADA'")
    Long calcularQuantidadeTotalVendidaProduto(@Param("produtoId") Long produtoId);

    /**
     * Calcula a quantidade total vendida de um produto em um período.
     * @param produtoId ID do produto
     * @param dataInicio data de início
     * @param dataFim data de fim
     * @return quantidade total vendida no período
     */
    @Query("SELECT COALESCE(SUM(iv.quantidade), 0) FROM ItemVenda iv INNER JOIN iv.venda v " +
           "WHERE iv.produto.id = :produtoId AND v.dataVenda BETWEEN :dataInicio AND :dataFim " +
           "AND v.status = 'FINALIZADA'")
    Long calcularQuantidadeTotalVendidaProdutoPeriodo(
        @Param("produtoId") Long produtoId,
        @Param("dataInicio") LocalDateTime dataInicio,
        @Param("dataFim") LocalDateTime dataFim
    );

    /**
     * Busca os produtos mais vendidos.
     * @param limite número máximo de produtos
     * @return lista com produtos mais vendidos
     */
    @Query("SELECT iv.produto.id, iv.produto.nome, SUM(iv.quantidade) as total " +
           "FROM ItemVenda iv INNER JOIN iv.venda v " +
           "WHERE v.status = 'FINALIZADA' " +
           "GROUP BY iv.produto.id, iv.produto.nome " +
           "ORDER BY total DESC")
    List<Object[]> findProdutosMaisVendidos(@Param("limite") int limite);

    /**
     * Busca os produtos mais vendidos em um período.
     * @param dataInicio data de início
     * @param dataFim data de fim
     * @param limite número máximo de produtos
     * @return lista com produtos mais vendidos no período
     */
    @Query("SELECT iv.produto.id, iv.produto.nome, SUM(iv.quantidade) as total " +
           "FROM ItemVenda iv INNER JOIN iv.venda v " +
           "WHERE v.dataVenda BETWEEN :dataInicio AND :dataFim AND v.status = 'FINALIZADA' " +
           "GROUP BY iv.produto.id, iv.produto.nome " +
           "ORDER BY total DESC")
    List<Object[]> findProdutosMaisVendidosPeriodo(
        @Param("dataInicio") LocalDateTime dataInicio,
        @Param("dataFim") LocalDateTime dataFim,
        @Param("limite") int limite
    );

    /**
     * Conta total de itens vendidos.
     * @return número total de itens vendidos
     */
    @Query("SELECT COUNT(iv) FROM ItemVenda iv INNER JOIN iv.venda v WHERE v.status = 'FINALIZADA'")
    long countTotalItensVendidos();

    /**
     * Conta total de itens vendidos em um período.
     * @param dataInicio data de início
     * @param dataFim data de fim
     * @return número total de itens vendidos no período
     */
    @Query("SELECT COUNT(iv) FROM ItemVenda iv INNER JOIN iv.venda v " +
           "WHERE v.dataVenda BETWEEN :dataInicio AND :dataFim AND v.status = 'FINALIZADA'")
    long countTotalItensVendidosPeriodo(
        @Param("dataInicio") LocalDateTime dataInicio,
        @Param("dataFim") LocalDateTime dataFim
    );

    /**
     * Busca itens de vendas finalizadas.
     * @return lista de itens de vendas finalizadas
     */
    @Query("SELECT iv FROM ItemVenda iv INNER JOIN iv.venda v WHERE v.status = 'FINALIZADA'")
    List<ItemVenda> findItensVendasFinalizadas();

    /**
     * Remove itens de uma venda.
     * @param vendaId ID da venda
     */
    @Query("DELETE FROM ItemVenda iv WHERE iv.venda.id = :vendaId")
    void deleteByVendaId(@Param("vendaId") Long vendaId);
}
