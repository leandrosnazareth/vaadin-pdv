package com.leandrosnazareth.venda.domain;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositório para operações de persistência da entidade Venda.
 * <p>
 * Este repositório fornece métodos para realizar operações CRUD e consultas
 * específicas relacionadas às vendas no sistema PDV.
 * </p>
 */
@Repository
public interface VendaRepository extends JpaRepository<Venda, Long> {

    /**
     * Busca vendas por status com paginação.
     * @param status status da venda
     * @param pageable configuração de paginação
     * @return slice de vendas com o status especificado
     */
    Slice<Venda> findByStatus(Venda.StatusVenda status, Pageable pageable);

    /**
     * Busca vendas por forma de pagamento.
     * @param formaPagamento forma de pagamento
     * @param pageable configuração de paginação
     * @return slice de vendas com a forma de pagamento especificada
     */
    Slice<Venda> findByFormaPagamento(Venda.FormaPagamento formaPagamento, Pageable pageable);

    /**
     * Busca vendas por período.
     * @param dataInicio data de início
     * @param dataFim data de fim
     * @param pageable configuração de paginação
     * @return slice de vendas no período especificado
     */
    Slice<Venda> findByDataVendaBetween(LocalDateTime dataInicio, LocalDateTime dataFim, Pageable pageable);

    /**
     * Busca vendas de uma data específica.
     * @param data data da venda
     * @param pageable configuração de paginação
     * @return slice de vendas da data especificada
     */
    @Query("SELECT v FROM Venda v WHERE CAST(v.dataVenda AS DATE) = :data ORDER BY v.dataVenda DESC")
    Slice<Venda> findByDataVenda(@Param("data") LocalDate data, Pageable pageable);

    /**
     * Busca vendas finalizadas.
     * @param pageable configuração de paginação
     * @return slice de vendas finalizadas
     */
    Slice<Venda> findByStatusOrderByDataVendaDesc(Venda.StatusVenda status, Pageable pageable);

    /**
     * Busca vendas por faixa de valor.
     * @param valorMin valor mínimo
     * @param valorMax valor máximo
     * @param pageable configuração de paginação
     * @return slice de vendas na faixa de valor especificada
     */
    Slice<Venda> findByValorTotalBetween(BigDecimal valorMin, BigDecimal valorMax, Pageable pageable);

    /**
     * Conta vendas por status.
     * @param status status da venda
     * @return número de vendas com o status especificado
     */
    long countByStatus(Venda.StatusVenda status);

    /**
     * Conta vendas de hoje.
     * @param dataInicio início do dia
     * @param dataFim fim do dia
     * @return número de vendas de hoje
     */
    long countByDataVendaBetween(LocalDateTime dataInicio, LocalDateTime dataFim);

    /**
     * Calcula o total de vendas por período.
     * @param dataInicio data de início
     * @param dataFim data de fim
     * @return total de vendas no período
     */
    @Query("SELECT COALESCE(SUM(v.valorTotal), 0) FROM Venda v WHERE v.dataVenda BETWEEN :dataInicio AND :dataFim AND v.status = 'FINALIZADA'")
    BigDecimal calcularTotalVendasPeriodo(@Param("dataInicio") LocalDateTime dataInicio, @Param("dataFim") LocalDateTime dataFim);

    /**
     * Calcula o total de vendas de hoje.
     * @param dataInicio início do dia
     * @param dataFim fim do dia
     * @return total de vendas de hoje
     */
    @Query("SELECT COALESCE(SUM(v.valorTotal), 0) FROM Venda v WHERE v.dataVenda BETWEEN :dataInicio AND :dataFim AND v.status = 'FINALIZADA'")
    BigDecimal calcularTotalVendasHoje(@Param("dataInicio") LocalDateTime dataInicio, @Param("dataFim") LocalDateTime dataFim);

    /**
     * Busca vendas mais recentes.
     * @param pageable configuração de paginação
     * @return slice de vendas ordenadas por data decrescente
     */
    Slice<Venda> findAllByOrderByDataVendaDesc(Pageable pageable);

    /**
     * Busca vendas por forma de pagamento e período.
     * @param formaPagamento forma de pagamento
     * @param dataInicio data de início
     * @param dataFim data de fim
     * @param pageable configuração de paginação
     * @return slice de vendas com a forma de pagamento no período especificado
     */
    Slice<Venda> findByFormaPagamentoAndDataVendaBetween(
        Venda.FormaPagamento formaPagamento, 
        LocalDateTime dataInicio, 
        LocalDateTime dataFim, 
        Pageable pageable
    );

    /**
     * Busca as últimas vendas finalizadas.
     * @param pageable configuração de paginação
     * @return slice das últimas vendas finalizadas
     */
    @Query("SELECT v FROM Venda v WHERE v.status = 'FINALIZADA' ORDER BY v.dataVenda DESC")
    Slice<Venda> findUltimasVendasFinalizadas(Pageable pageable);

    /**
     * Busca vendas com valor total acima de um valor específico.
     * @param valorMinimo valor mínimo
     * @param pageable configuração de paginação
     * @return slice de vendas com valor acima do especificado
     */
    Slice<Venda> findByValorTotalGreaterThanEqual(BigDecimal valorMinimo, Pageable pageable);

    /**
     * Busca estatísticas de vendas por forma de pagamento.
     * @param dataInicio data de início
     * @param dataFim data de fim
     * @return lista com estatísticas por forma de pagamento
     */
    @Query("SELECT v.formaPagamento, COUNT(v), SUM(v.valorTotal) FROM Venda v " +
           "WHERE v.dataVenda BETWEEN :dataInicio AND :dataFim AND v.status = 'FINALIZADA' " +
           "GROUP BY v.formaPagamento")
    List<Object[]> obterEstatisticasPorFormaPagamento(@Param("dataInicio") LocalDateTime dataInicio, @Param("dataFim") LocalDateTime dataFim);

    /**
     * Busca estatísticas de vendas por dia.
     * @param dataInicio data de início
     * @param dataFim data de fim
     * @return lista com estatísticas por dia
     */
    @Query("SELECT CAST(v.dataVenda AS DATE), COUNT(v), SUM(v.valorTotal) FROM Venda v " +
           "WHERE v.dataVenda BETWEEN :dataInicio AND :dataFim AND v.status = 'FINALIZADA' " +
           "GROUP BY CAST(v.dataVenda AS DATE) ORDER BY CAST(v.dataVenda AS DATE)")
    List<Object[]> obterEstatisticasPorDia(@Param("dataInicio") LocalDateTime dataInicio, @Param("dataFim") LocalDateTime dataFim);

    /**
     * Busca a primeira venda pendente (se houver).
     * @return Optional da venda pendente
     */
    Optional<Venda> findFirstByStatusOrderByDataVendaDesc(Venda.StatusVenda status);

    /**
     * Busca todas as vendas pendentes.
     * @return lista de vendas pendentes
     */
    List<Venda> findByStatus(Venda.StatusVenda status);

    /**
     * Exclui todas as vendas pendentes.
     */
    @Modifying
    @Query("DELETE FROM Venda v WHERE v.status = :status")
    void deleteByStatus(@Param("status") Venda.StatusVenda status);
}
