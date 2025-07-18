package com.leandrosnazareth.produto.domain;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;


@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long>, JpaSpecificationExecutor<Produto> {

    Optional<Produto> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    boolean existsByCodigoAndIdNot(String codigo, Long id);

    Slice<Produto> findByNomeContainingIgnoreCase(String nome, Pageable pageable);

    Slice<Produto> findByCategoria(String categoria, Pageable pageable);

    Slice<Produto> findByAtivoTrue(Pageable pageable);

    @Query("SELECT p FROM Produto p WHERE p.estoqueAtual <= p.estoqueMinimo AND p.ativo = true")
    List<Produto> findProdutosComEstoqueBaixo();

    Slice<Produto> findByPrecoVendaBetween(BigDecimal precoMin, BigDecimal precoMax, Pageable pageable);

    Slice<Produto> findByMarca(String marca, Pageable pageable);

    Slice<Produto> findByFornecedor(String fornecedor, Pageable pageable);

    @Query("SELECT p FROM Produto p WHERE " +
           "LOWER(p.nome) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.codigo) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.descricao) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.categoria) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.marca) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Slice<Produto> findBySearchTerm(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("SELECT DISTINCT p.categoria FROM Produto p WHERE p.categoria IS NOT NULL ORDER BY p.categoria")
    List<String> findDistinctCategorias();

    @Query("SELECT DISTINCT p.marca FROM Produto p WHERE p.marca IS NOT NULL ORDER BY p.marca")
    List<String> findDistinctMarcas();

    @Query("SELECT DISTINCT p.fornecedor FROM Produto p WHERE p.fornecedor IS NOT NULL ORDER BY p.fornecedor")
    List<String> findDistinctFornecedores();

    long countByCategoria(String categoria);

    long countByAtivoTrue();

    @Query("SELECT COUNT(p) FROM Produto p WHERE p.estoqueAtual <= p.estoqueMinimo AND p.ativo = true")
    long countProdutosComEstoqueBaixo();

    Slice<Produto> findAllByOrderByNome(Pageable pageable);

    Slice<Produto> findAllByOrderByDataCriacaoDesc(Pageable pageable);

    @Query("SELECT p FROM Produto p WHERE p.estoqueAtual = 0 AND p.ativo = true")
    List<Produto> findProdutosSemEstoque();

    Slice<Produto> findByEstoqueAtualBetween(Integer estoqueMin, Integer estoqueMax, Pageable pageable);

    Slice<Produto> findAllBy(Pageable pageable);
}
