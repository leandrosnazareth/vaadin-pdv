package com.leandrosnazareth.produto.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.leandrosnazareth.produto.domain.Produto;
import com.leandrosnazareth.produto.domain.ProdutoRepository;

@Service
@PreAuthorize("isAuthenticated()")
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final Clock clock;

    public ProdutoService(ProdutoRepository produtoRepository, Clock clock) {
        this.produtoRepository = produtoRepository;
        this.clock = clock;
    }

    @Transactional
    public Produto criarProduto(Produto produto) {
        validarProduto(produto);

        if (produtoRepository.existsByCodigo(produto.getCodigo())) {
            throw new IllegalArgumentException("Já existe um produto com o código: " + produto.getCodigo());
        }

        produto.setDataCriacao(clock.instant());
        produto.setDataAtualizacao(clock.instant());

        return produtoRepository.save(produto);
    }

    @Transactional
    public Produto atualizarProduto(Produto produto) {
        validarProduto(produto);

        if (produto.getId() == null) {
            throw new IllegalArgumentException("ID do produto é obrigatório para atualização");
        }

        if (!produtoRepository.existsById(produto.getId())) {
            throw new IllegalArgumentException("Produto não encontrado com ID: " + produto.getId());
        }

        if (produtoRepository.existsByCodigoAndIdNot(produto.getCodigo(), produto.getId())) {
            throw new IllegalArgumentException("Já existe outro produto com o código: " + produto.getCodigo());
        }

        produto.setDataAtualizacao(clock.instant());

        return produtoRepository.save(produto);
    }

    @Transactional(readOnly = true)
    public Optional<Produto> buscarPorId(Long id) {
        return produtoRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Produto> buscarPorCodigo(String codigo) {
        return produtoRepository.findByCodigo(codigo);
    }

    @Transactional(readOnly = true)
    public Slice<Produto> listarProdutos(Pageable pageable) {
        return produtoRepository.findAllBy(pageable);
    }

    @Transactional(readOnly = true)
    public Slice<Produto> listarProdutosAtivos(Pageable pageable) {
        return produtoRepository.findByAtivoTrue(pageable);
    }

    @Transactional(readOnly = true)
    public Slice<Produto> buscarPorNome(String nome, Pageable pageable) {
        return produtoRepository.findByNomeContainingIgnoreCase(nome, pageable);
    }

    @Transactional(readOnly = true)
    public Slice<Produto> buscarPorCategoria(String categoria, Pageable pageable) {
        return produtoRepository.findByCategoria(categoria, pageable);
    }

    @Transactional(readOnly = true)
    public Slice<Produto> buscarPorTermo(String searchTerm, Pageable pageable) {
        return produtoRepository.findBySearchTerm(searchTerm, pageable);
    }

    @Transactional(readOnly = true)
    public Slice<Produto> buscarPorFaixaPreco(BigDecimal precoMin, BigDecimal precoMax, Pageable pageable) {
        return produtoRepository.findByPrecoVendaBetween(precoMin, precoMax, pageable);
    }

    @Transactional(readOnly = true)
    public List<Produto> listarProdutosComEstoqueBaixo() {
        return produtoRepository.findProdutosComEstoqueBaixo();
    }

    @Transactional(readOnly = true)
    public List<Produto> listarProdutosSemEstoque() {
        return produtoRepository.findProdutosSemEstoque();
    }

    @Transactional
    public Produto atualizarEstoque(Long produtoId, int quantidade) {
        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado com ID: " + produtoId));

        if (!produto.atualizarEstoque(quantidade)) {
            throw new IllegalArgumentException("Operação resultaria em estoque negativo");
        }

        produto.setDataAtualizacao(clock.instant());

        return produtoRepository.save(produto);
    }

    @Transactional
    public Produto alterarStatusAtivo(Long produtoId, boolean ativo) {
        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado com ID: " + produtoId));

        produto.setAtivo(ativo);
        produto.setDataAtualizacao(clock.instant());

        return produtoRepository.save(produto);
    }

    @Transactional
    public void excluirProduto(Long produtoId) {
        alterarStatusAtivo(produtoId, false);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void excluirProdutoFisicamente(Long produtoId) {
        if (!produtoRepository.existsById(produtoId)) {
            throw new IllegalArgumentException("Produto não encontrado com ID: " + produtoId);
        }

        produtoRepository.deleteById(produtoId);
    }

    @Transactional(readOnly = true)
    public List<String> listarCategorias() {
        return produtoRepository.findDistinctCategorias();
    }

    @Transactional(readOnly = true)
    public List<String> listarMarcas() {
        return produtoRepository.findDistinctMarcas();
    }

    @Transactional(readOnly = true)
    public List<String> listarFornecedores() {
        return produtoRepository.findDistinctFornecedores();
    }

    @Transactional(readOnly = true)
    public long contarProdutosPorCategoria(String categoria) {
        return produtoRepository.countByCategoria(categoria);
    }

    @Transactional(readOnly = true)
    public long contarProdutosAtivos() {
        return produtoRepository.countByAtivoTrue();
    }

    @Transactional(readOnly = true)
    public long contarProdutosComEstoqueBaixo() {
        return produtoRepository.countProdutosComEstoqueBaixo();
    }

    private void validarProduto(Produto produto) {
        if (produto == null) {
            throw new IllegalArgumentException("Produto não pode ser nulo");
        }

        if (produto.getCodigo() == null || produto.getCodigo().trim().isEmpty()) {
            throw new IllegalArgumentException("Código do produto é obrigatório");
        }

        if (produto.getNome() == null || produto.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do produto é obrigatório");
        }

        if (produto.getPrecoVenda() == null || produto.getPrecoVenda().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Preço de venda deve ser maior que zero");
        }

        if (produto.getEstoqueAtual() == null || produto.getEstoqueAtual() < 0) {
            throw new IllegalArgumentException("Estoque atual deve ser maior ou igual a zero");
        }

        if (produto.getEstoqueMinimo() != null && produto.getEstoqueMinimo() < 0) {
            throw new IllegalArgumentException("Estoque mínimo deve ser maior ou igual a zero");
        }

        if (produto.getEstoqueMaximo() != null && produto.getEstoqueMaximo() < 0) {
            throw new IllegalArgumentException("Estoque máximo deve ser maior ou igual a zero");
        }

        if (produto.getEstoqueMinimo() != null && produto.getEstoqueMaximo() != null &&
                produto.getEstoqueMinimo() > produto.getEstoqueMaximo()) {
            throw new IllegalArgumentException("Estoque mínimo não pode ser maior que o estoque máximo");
        }

        if (produto.getPrecoCompra() != null && produto.getPrecoCompra().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Preço de compra deve ser maior ou igual a zero");
        }

        if (produto.getPeso() != null && produto.getPeso().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Peso deve ser maior ou igual a zero");
        }
    }
}
