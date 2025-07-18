package com.leandrosnazareth.venda.service;

import com.leandrosnazareth.produto.domain.Produto;
import com.leandrosnazareth.produto.service.ProdutoService;
import com.leandrosnazareth.venda.domain.ItemVenda;
import com.leandrosnazareth.venda.domain.ItemVendaRepository;
import com.leandrosnazareth.venda.domain.Venda;
import com.leandrosnazareth.venda.domain.VendaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Serviço para operações de negócio relacionadas a vendas.
 * <p>
 * Este serviço encapsula a lógica de negócio para gerenciamento de vendas,
 * incluindo criação, finalização, cancelamento e consultas estatísticas.
 * </p>
 */
@Service
@PreAuthorize("isAuthenticated()")
public class VendaService {

    private final VendaRepository vendaRepository;
    private final ItemVendaRepository itemVendaRepository;
    private final ProdutoService produtoService;

    public VendaService(VendaRepository vendaRepository, 
                       ItemVendaRepository itemVendaRepository,
                       ProdutoService produtoService) {
        this.vendaRepository = vendaRepository;
        this.itemVendaRepository = itemVendaRepository;
        this.produtoService = produtoService;
    }

    /**
     * Busca ou cria uma venda pendente.
     * Garante que só exista uma venda pendente por vez.
     * @param formaPagamento forma de pagamento
     * @return venda pendente (nova ou existente)
     */
    @Transactional
    public Venda buscarOuCriarVendaPendente(Venda.FormaPagamento formaPagamento) {
        // Busca venda pendente existente
        Optional<Venda> vendaExistente = vendaRepository.findFirstByStatusOrderByDataVendaDesc(Venda.StatusVenda.PENDENTE);
        
        if (vendaExistente.isPresent()) {
            return vendaExistente.get();
        }
        
        // Se não existe, cria nova venda
        return criarVenda(formaPagamento);
    }

    /**
     * Cria uma nova venda, excluindo todas as vendas pendentes anteriores.
     * @param formaPagamento forma de pagamento
     * @return nova venda criada
     */
    @Transactional
    public Venda criarVenda(Venda.FormaPagamento formaPagamento) {
        // Exclui todas as vendas pendentes anteriores
        vendaRepository.deleteByStatus(Venda.StatusVenda.PENDENTE);
        
        // Cria nova venda
        Venda venda = new Venda(formaPagamento);
        return vendaRepository.save(venda);
    }

    /**
     * Cancela todas as vendas pendentes.
     */
    @Transactional
    public void cancelarVendasPendentes() {
        List<Venda> vendasPendentes = vendaRepository.findByStatus(Venda.StatusVenda.PENDENTE);
        for (Venda venda : vendasPendentes) {
            venda.cancelar();
        }
        vendaRepository.saveAll(vendasPendentes);
    }

    /**
     * Busca uma venda por ID.
     * @param vendaId ID da venda
     * @return venda encontrada
     * @throws IllegalArgumentException se a venda não existir
     */
    @Transactional(readOnly = true)
    public Venda buscarPorId(Long vendaId) {
        return vendaRepository.findById(vendaId)
            .orElseThrow(() -> new IllegalArgumentException("Venda não encontrada com ID: " + vendaId));
    }

    /**
     * Adiciona um item à venda.
     * @param vendaId ID da venda
     * @param produtoId ID do produto
     * @param quantidade quantidade do produto
     * @return venda atualizada
     * @throws IllegalArgumentException se a venda não existir ou não tiver estoque suficiente
     */
    @Transactional
    public Venda adicionarItem(Long vendaId, Long produtoId, Integer quantidade) {
        Venda venda = buscarPorId(vendaId);
        
        if (venda.getStatus() != Venda.StatusVenda.PENDENTE) {
            throw new IllegalArgumentException("Não é possível adicionar itens a uma venda já finalizada ou cancelada");
        }

        Produto produto = produtoService.buscarPorId(produtoId)
            .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado com ID: " + produtoId));
        
        if (produto.getEstoqueAtual() < quantidade) {
            throw new IllegalArgumentException("Estoque insuficiente para o produto: " + produto.getNome());
        }

        // Verifica se o produto já existe na venda
        Optional<ItemVenda> itemExistente = venda.getItens().stream()
            .filter(item -> item.getProduto().getId().equals(produtoId))
            .findFirst();

        if (itemExistente.isPresent()) {
            // Atualiza a quantidade do item existente
            ItemVenda item = itemExistente.get();
            int novaQuantidade = item.getQuantidade() + quantidade;
            
            if (produto.getEstoqueAtual() < novaQuantidade) {
                throw new IllegalArgumentException("Estoque insuficiente para o produto: " + produto.getNome());
            }
            
            item.setQuantidade(novaQuantidade);
            itemVendaRepository.save(item);
        } else {
            // Cria novo item
            ItemVenda novoItem = new ItemVenda(produto, quantidade, produto.getPrecoVenda());
            venda.adicionarItem(novoItem);
            itemVendaRepository.save(novoItem);
        }

        venda.recalcularTotal();
        return vendaRepository.save(venda);
    }

    /**
     * Remove um item da venda.
     * @param vendaId ID da venda
     * @param itemId ID do item
     * @return venda atualizada
     * @throws IllegalArgumentException se a venda ou item não existir
     */
    @Transactional
    public Venda removerItem(Long vendaId, Long itemId) {
        Venda venda = buscarPorId(vendaId);
        
        if (venda.getStatus() != Venda.StatusVenda.PENDENTE) {
            throw new IllegalArgumentException("Não é possível remover itens de uma venda já finalizada ou cancelada");
        }

        ItemVenda item = itemVendaRepository.findById(itemId)
            .orElseThrow(() -> new IllegalArgumentException("Item não encontrado com ID: " + itemId));

        venda.removerItem(item);
        itemVendaRepository.delete(item);
        
        venda.recalcularTotal();
        return vendaRepository.save(venda);
    }

    /**
     * Atualiza a quantidade de um item na venda.
     * @param vendaId ID da venda
     * @param itemId ID do item
     * @param novaQuantidade nova quantidade
     * @return venda atualizada
     * @throws IllegalArgumentException se não houver estoque suficiente
     */
    @Transactional
    public Venda atualizarQuantidadeItem(Long vendaId, Long itemId, Integer novaQuantidade) {
        Venda venda = buscarPorId(vendaId);
        
        if (venda.getStatus() != Venda.StatusVenda.PENDENTE) {
            throw new IllegalArgumentException("Não é possível alterar itens de uma venda já finalizada ou cancelada");
        }

        ItemVenda item = itemVendaRepository.findById(itemId)
            .orElseThrow(() -> new IllegalArgumentException("Item não encontrado com ID: " + itemId));

        Produto produto = item.getProduto();
        
        if (produto.getEstoqueAtual() < novaQuantidade) {
            throw new IllegalArgumentException("Estoque insuficiente para o produto: " + produto.getNome());
        }

        item.setQuantidade(novaQuantidade);
        itemVendaRepository.save(item);
        
        venda.recalcularTotal();
        return vendaRepository.save(venda);
    }

    /**
     * Aplica desconto na venda.
     * @param vendaId ID da venda
     * @param desconto valor do desconto
     * @return venda atualizada
     */
    @Transactional
    public Venda aplicarDesconto(Long vendaId, BigDecimal desconto) {
        Venda venda = buscarPorId(vendaId);
        
        if (venda.getStatus() != Venda.StatusVenda.PENDENTE) {
            throw new IllegalArgumentException("Não é possível aplicar desconto a uma venda já finalizada ou cancelada");
        }

        venda.setDesconto(desconto);
        venda.recalcularTotal();
        return vendaRepository.save(venda);
    }

    /**
     * Finaliza uma venda.
     * @param vendaId ID da venda
     * @param valorRecebido valor recebido
     * @return venda finalizada
     * @throws IllegalArgumentException se a venda não puder ser finalizada
     */
    @Transactional
    public Venda finalizarVenda(Long vendaId, BigDecimal valorRecebido) {
        Venda venda = buscarPorId(vendaId);
        
        if (venda.getStatus() != Venda.StatusVenda.PENDENTE) {
            throw new IllegalArgumentException("Venda já foi finalizada ou cancelada");
        }

        if (venda.getItens().isEmpty()) {
            throw new IllegalArgumentException("Não é possível finalizar uma venda sem itens");
        }

        if (valorRecebido.compareTo(venda.getValorTotal()) < 0) {
            throw new IllegalArgumentException("Valor recebido é menor que o valor total da venda");
        }

        venda.setValorRecebido(valorRecebido);
        venda.finalizar();

        // Atualiza estoque dos produtos
        for (ItemVenda item : venda.getItens()) {
            produtoService.atualizarEstoque(item.getProduto().getId(), -item.getQuantidade());
        }

        return vendaRepository.save(venda);
    }

    /**
     * Cancela uma venda.
     * @param vendaId ID da venda
     * @return venda cancelada
     * @throws IllegalArgumentException se a venda não puder ser cancelada
     */
    @Transactional
    public Venda cancelarVenda(Long vendaId) {
        Venda venda = buscarPorId(vendaId);
        
        if (venda.getStatus() == Venda.StatusVenda.FINALIZADA) {
            throw new IllegalArgumentException("Não é possível cancelar uma venda já finalizada");
        }

        venda.cancelar();
        return vendaRepository.save(venda);
    }

    /**
     * Lista todas as vendas com paginação.
     * @param pageable configuração de paginação
     * @return slice de vendas
     */
    @Transactional(readOnly = true)
    public Slice<Venda> listarVendas(Pageable pageable) {
        return vendaRepository.findAllByOrderByDataVendaDesc(pageable);
    }

    /**
     * Lista vendas por status.
     * @param status status da venda
     * @param pageable configuração de paginação
     * @return slice de vendas com o status especificado
     */
    @Transactional(readOnly = true)
    public Slice<Venda> listarVendasPorStatus(Venda.StatusVenda status, Pageable pageable) {
        return vendaRepository.findByStatusOrderByDataVendaDesc(status, pageable);
    }

    /**
     * Lista vendas do dia.
     * @param data data das vendas
     * @param pageable configuração de paginação
     * @return slice de vendas da data especificada
     */
    @Transactional(readOnly = true)
    public Slice<Venda> listarVendasDoDia(LocalDate data, Pageable pageable) {
        return vendaRepository.findByDataVenda(data, pageable);
    }

    /**
     * Lista vendas de hoje.
     * @param pageable configuração de paginação
     * @return slice de vendas de hoje
     */
    @Transactional(readOnly = true)
    public Slice<Venda> listarVendasDeHoje(Pageable pageable) {
        return listarVendasDoDia(LocalDate.now(), pageable);
    }

    /**
     * Calcula o total de vendas de hoje.
     * @return total de vendas de hoje
     */
    @Transactional(readOnly = true)
    public BigDecimal calcularTotalVendasHoje() {
        LocalDateTime inicioHoje = LocalDate.now().atStartOfDay();
        LocalDateTime fimHoje = LocalDate.now().atTime(LocalTime.MAX);
        return vendaRepository.calcularTotalVendasHoje(inicioHoje, fimHoje);
    }

    /**
     * Calcula o total de vendas do mês.
     * @return total de vendas do mês
     */
    @Transactional(readOnly = true)
    public BigDecimal calcularTotalVendasMes() {
        LocalDate hoje = LocalDate.now();
        LocalDateTime inicioMes = hoje.withDayOfMonth(1).atStartOfDay();
        LocalDateTime fimMes = hoje.withDayOfMonth(hoje.lengthOfMonth()).atTime(LocalTime.MAX);
        return vendaRepository.calcularTotalVendasPeriodo(inicioMes, fimMes);
    }

    /**
     * Conta vendas de hoje.
     * @return número de vendas de hoje
     */
    @Transactional(readOnly = true)
    public long contarVendasHoje() {
        LocalDateTime inicioHoje = LocalDate.now().atStartOfDay();
        LocalDateTime fimHoje = LocalDate.now().atTime(LocalTime.MAX);
        return vendaRepository.countByDataVendaBetween(inicioHoje, fimHoje);
    }

    /**
     * Conta vendas por status.
     * @param status status da venda
     * @return número de vendas com o status especificado
     */
    @Transactional(readOnly = true)
    public long contarVendasPorStatus(Venda.StatusVenda status) {
        return vendaRepository.countByStatus(status);
    }

    /**
     * Busca produtos mais vendidos.
     * @param limite número máximo de produtos
     * @return lista de produtos mais vendidos
     */
    @Transactional(readOnly = true)
    public List<Object[]> buscarProdutosMaisVendidos(int limite) {
        return itemVendaRepository.findProdutosMaisVendidos(limite);
    }

    /**
     * Busca estatísticas de vendas por forma de pagamento.
     * @param dataInicio data de início
     * @param dataFim data de fim
     * @return lista com estatísticas por forma de pagamento
     */
    @Transactional(readOnly = true)
    public List<Object[]> obterEstatisticasPorFormaPagamento(LocalDateTime dataInicio, LocalDateTime dataFim) {
        return vendaRepository.obterEstatisticasPorFormaPagamento(dataInicio, dataFim);
    }

    /**
     * Busca estatísticas de vendas por dia.
     * @param dataInicio data de início
     * @param dataFim data de fim
     * @return lista com estatísticas por dia
     */
    @Transactional(readOnly = true)
    public List<Object[]> obterEstatisticasPorDia(LocalDateTime dataInicio, LocalDateTime dataFim) {
        return vendaRepository.obterEstatisticasPorDia(dataInicio, dataFim);
    }
}
