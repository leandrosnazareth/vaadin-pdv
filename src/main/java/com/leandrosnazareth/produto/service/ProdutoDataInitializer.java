package com.leandrosnazareth.produto.service;

import com.leandrosnazareth.produto.domain.Produto;
import com.leandrosnazareth.produto.domain.ProdutoRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;

@Component
public class ProdutoDataInitializer {

    private final ProdutoRepository produtoRepository;
    private final Clock clock;

    public ProdutoDataInitializer(ProdutoRepository produtoRepository, Clock clock) {
        this.produtoRepository = produtoRepository;
        this.clock = clock;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void inicializarDados() {
        // Só criar dados se não existirem produtos
        if (produtoRepository.count() == 0) {
            criarProdutosExemplo();
        }
    }

    /**
     * Cria produtos de exemplo para demonstração.
     */
    private void criarProdutosExemplo() {
        // Produtos de informática
        criarProduto("MOUSE001", "Mouse Óptico USB", "Periféricos", "Logitech", 
                    new BigDecimal("25.00"), new BigDecimal("45.00"), 50, 5, 100, "UN",
                    "https://via.placeholder.com/200x200/4A90E2/FFFFFF?text=Mouse");
        
        criarProduto("TECLADO001", "Teclado Mecânico RGB", "Periféricos", "Corsair", 
                    new BigDecimal("180.00"), new BigDecimal("299.99"), 20, 3, 50, "UN",
                    "https://via.placeholder.com/200x200/E94B3C/FFFFFF?text=Teclado");
        
        criarProduto("MONITOR001", "Monitor 24\" Full HD", "Monitores", "LG", 
                    new BigDecimal("450.00"), new BigDecimal("799.99"), 15, 2, 30, "UN",
                    "https://via.placeholder.com/200x200/50C878/FFFFFF?text=Monitor");
        
        criarProduto("NOTEBOOK001", "Notebook Intel i5 8GB", "Computadores", "Lenovo", 
                    new BigDecimal("2200.00"), new BigDecimal("3499.99"), 8, 1, 20, "UN",
                    "https://via.placeholder.com/200x200/9932CC/FFFFFF?text=Notebook");
        
        criarProduto("SSD001", "SSD 480GB SATA", "Armazenamento", "Kingston", 
                    new BigDecimal("120.00"), new BigDecimal("199.99"), 25, 5, 50, "UN",
                    "https://via.placeholder.com/200x200/FF6347/FFFFFF?text=SSD");
        
        // Produtos de escritório
        criarProduto("PAPEL001", "Papel A4 500 folhas", "Papel", "Chamex", 
                    new BigDecimal("12.00"), new BigDecimal("22.99"), 100, 10, 200, "PCT");
        
        criarProduto("CANETA001", "Caneta Esferográfica Azul", "Escrita", "Bic", 
                    new BigDecimal("0.50"), new BigDecimal("1.50"), 500, 50, 1000, "UN");
        
        criarProduto("GRAMPEADOR001", "Grampeador Médio", "Acessórios", "Spiral", 
                    new BigDecimal("15.00"), new BigDecimal("29.99"), 30, 5, 60, "UN");
        
        // Produtos de limpeza
        criarProduto("DETERGENTE001", "Detergente Líquido 500ml", "Limpeza", "Ypê", 
                    new BigDecimal("1.80"), new BigDecimal("3.49"), 80, 10, 150, "UN");
        
        criarProduto("ALCOOL001", "Álcool Gel 70% 500ml", "Higiene", "Asepsia", 
                    new BigDecimal("8.00"), new BigDecimal("14.99"), 60, 8, 120, "UN");
        
        // Produtos alimentícios
        criarProduto("CAFE001", "Café Torrado e Moído 500g", "Bebidas", "Pilão", 
                    new BigDecimal("8.50"), new BigDecimal("16.99"), 40, 5, 80, "UN");
        
        criarProduto("ACUCAR001", "Açúcar Cristal 1kg", "Alimentação", "União", 
                    new BigDecimal("2.20"), new BigDecimal("4.99"), 60, 10, 120, "UN");
        
        // Produtos eletrônicos
        criarProduto("CABO001", "Cabo USB-C 1m", "Cabos", "Multilaser", 
                    new BigDecimal("8.00"), new BigDecimal("19.99"), 35, 5, 70, "UN");
        
        criarProduto("FONE001", "Fone de Ouvido Bluetooth", "Áudio", "JBL", 
                    new BigDecimal("80.00"), new BigDecimal("149.99"), 18, 3, 40, "UN");
        
        criarProduto("CARREGADOR001", "Carregador Portátil 10000mAh", "Energia", "Anker", 
                    new BigDecimal("65.00"), new BigDecimal("119.99"), 22, 3, 45, "UN");
        
        // Produtos com estoque baixo para demonstração
        criarProduto("LOWSTOCK001", "Produto com Estoque Baixo", "Teste", "Exemplo", 
                    new BigDecimal("10.00"), new BigDecimal("20.00"), 2, 10, 50, "UN");
        
        criarProduto("NOSTOCK001", "Produto Sem Estoque", "Teste", "Exemplo", 
                    new BigDecimal("5.00"), new BigDecimal("15.00"), 0, 5, 30, "UN");
    }

    private void criarProduto(String codigo, String nome, String categoria, String marca,
                             BigDecimal precoCompra, BigDecimal precoVenda, int estoque,
                             int estoqueMin, int estoqueMax, String unidade) {
        criarProduto(codigo, nome, categoria, marca, precoCompra, precoVenda, estoque, estoqueMin, estoqueMax, unidade, null);
    }
    
    private void criarProduto(String codigo, String nome, String categoria, String marca,
                             BigDecimal precoCompra, BigDecimal precoVenda, int estoque,
                             int estoqueMin, int estoqueMax, String unidade, String fotoUrl) {
        Produto produto = new Produto();
        produto.setCodigo(codigo);
        produto.setNome(nome);
        produto.setCategoria(categoria);
        produto.setMarca(marca);
        produto.setPrecoCompra(precoCompra);
        produto.setPrecoVenda(precoVenda);
        produto.setEstoqueAtual(estoque);
        produto.setEstoqueMinimo(estoqueMin);
        produto.setEstoqueMaximo(estoqueMax);
        produto.setUnidade(unidade);
        produto.setAtivo(true);
        produto.setDataCriacao(clock.instant());
        produto.setDataAtualizacao(clock.instant());
        
        // Definir foto se fornecida
        if (fotoUrl != null && !fotoUrl.isEmpty()) {
            produto.setFoto(fotoUrl.getBytes());
        }
        
        // Adicionar descrições
        if (codigo.contains("MOUSE")) {
            produto.setDescricao("Mouse óptico com sensor de alta precisão, ideal para uso diário em escritório.");
        } else if (codigo.contains("TECLADO")) {
            produto.setDescricao("Teclado mecânico com iluminação RGB personalizável e switches duráveis.");
        } else if (codigo.contains("MONITOR")) {
            produto.setDescricao("Monitor LED Full HD com excelente qualidade de imagem e baixo consumo de energia.");
        } else if (codigo.contains("NOTEBOOK")) {
            produto.setDescricao("Notebook com processador Intel i5, 8GB RAM e SSD 256GB para alta performance.");
        }
        
        produtoRepository.save(produto);
    }
}
