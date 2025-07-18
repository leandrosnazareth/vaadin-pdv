package com.leandrosnazareth.venda.ui.component;

import com.leandrosnazareth.produto.domain.Produto;
import com.leandrosnazareth.produto.service.ProdutoService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.NumberRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.function.BiConsumer;

/**
 * Componente para buscar e selecionar produtos no PDV.
 * <p>
 * Permite buscar produtos por nome, código ou categoria,
 * e adicionar ao carrinho com quantidade personalizada.
 * </p>
 */
public class BuscarProdutoComponent extends VerticalLayout {

    private final ProdutoService produtoService;
    private TextField buscaField;
    private ComboBox<String> categoriaComboBox;
    private final Grid<Produto> gridProdutos;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
    
    private BiConsumer<Produto, Integer> onAdicionarProduto;

    public BuscarProdutoComponent(ProdutoService produtoService) {
        this.produtoService = produtoService;
        
        setWidth("100%");
        setHeight("100%");
        setPadding(false);
        setSpacing(false);
        
        // Cabeçalho
        VerticalLayout header = criarCabecalho();
        
        // Grid de produtos
        gridProdutos = criarGridProdutos();
        gridProdutos.addClassName(LumoUtility.Flex.GROW);
        
        add(header, gridProdutos);
        
        // Carrega dados iniciais
        atualizarGridProdutos();
        carregarCategorias();
    }

    private VerticalLayout criarCabecalho() {
        H4 titulo = new H4("Buscar Produtos");
        titulo.addClassName(LumoUtility.Margin.NONE);
        
        // Campo de busca
        buscaField = new TextField();
        buscaField.setPlaceholder("Buscar por nome, código ou descrição...");
        buscaField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        buscaField.setValueChangeMode(ValueChangeMode.LAZY);
        buscaField.addValueChangeListener(e -> atualizarGridProdutos());
        buscaField.setWidthFull();
        
        // ComboBox de categoria
        categoriaComboBox = new ComboBox<>("Categoria");
        categoriaComboBox.setPlaceholder("Todas as categorias");
        categoriaComboBox.setClearButtonVisible(true);
        categoriaComboBox.addValueChangeListener(e -> atualizarGridProdutos());
        categoriaComboBox.setWidth("200px");
        
        HorizontalLayout filtros = new HorizontalLayout(buscaField, categoriaComboBox);
        filtros.setWidthFull();
        filtros.setFlexGrow(1, buscaField);
        filtros.setAlignItems(Alignment.END);
        
        VerticalLayout header = new VerticalLayout(titulo, filtros);
        header.setPadding(true);
        header.setSpacing(true);
        header.addClassName(LumoUtility.Background.CONTRAST_5);
        
        return header;
    }

    private Grid<Produto> criarGridProdutos() {
        Grid<Produto> grid = new Grid<>(Produto.class, false);
        grid.setWidthFull();
        grid.setAllRowsVisible(true);
        
        // Coluna da foto
        grid.addColumn(new ComponentRenderer<>(this::criarFotoProduto))
            .setHeader("Foto")
            .setWidth("80px")
            .setSortable(false);
        
        // Coluna do código
        grid.addColumn(Produto::getCodigo)
            .setHeader("Código")
            .setWidth("120px")
            .setSortable(true);
        
        // Coluna do nome
        grid.addColumn(Produto::getNome)
            .setHeader("Produto")
            .setFlexGrow(1)
            .setSortable(true);
        
        // Coluna da categoria
        grid.addColumn(produto -> produto.getCategoria() != null ? produto.getCategoria() : "")
            .setHeader("Categoria")
            .setWidth("120px")
            .setSortable(true);
        
        // Coluna do preço
        grid.addColumn(new NumberRenderer<>(Produto::getPrecoVenda, currencyFormat))
            .setHeader("Preço")
            .setWidth("100px")
            .setSortable(true);
        
        // Coluna do estoque
        grid.addColumn(new ComponentRenderer<>(this::criarIndicadorEstoque))
            .setHeader("Estoque")
            .setWidth("80px")
            .setSortable(true);
        
        // Coluna de ações
        grid.addColumn(new ComponentRenderer<>(this::criarBotaoAdicionar))
            .setHeader("Adicionar")
            .setWidth("120px")
            .setSortable(false);
        
        return grid;
    }

    private Image criarFotoProduto(Produto produto) {
        Image foto = new Image();
        foto.setWidth("60px");
        foto.setHeight("60px");
        
        if (produto.getFoto() != null && produto.getFoto().length > 0) {
            // Usando data URL para evitar API depreciada
            String dataUrl = "data:image/jpeg;base64," + java.util.Base64.getEncoder().encodeToString(produto.getFoto());
            foto.setSrc(dataUrl);
        } else {
            foto.setSrc("images/no-image.png");
        }
        
        foto.getStyle().set("object-fit", "cover");
        foto.getStyle().set("border-radius", "4px");
        
        return foto;
    }

    private Span criarIndicadorEstoque(Produto produto) {
        Span estoque = new Span(produto.getEstoqueAtual().toString());
        
        if (produto.getEstoqueAtual() == 0) {
            estoque.addClassName(LumoUtility.TextColor.ERROR);
            estoque.addClassName(LumoUtility.FontWeight.BOLD);
        } else if (produto.isEstoqueBaixo()) {
            estoque.addClassName(LumoUtility.TextColor.WARNING);
            estoque.addClassName(LumoUtility.FontWeight.BOLD);
        }
        
        return estoque;
    }

    private HorizontalLayout criarBotaoAdicionar(Produto produto) {
        IntegerField quantidadeField = new IntegerField();
        quantidadeField.setValue(1);
        quantidadeField.setMin(1);
        quantidadeField.setMax(produto.getEstoqueAtual());
        quantidadeField.setWidth("60px");
        quantidadeField.setStepButtonsVisible(true);
        
        Button adicionarBtn = new Button(new Icon(VaadinIcon.PLUS));
        adicionarBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        adicionarBtn.setEnabled(produto.getEstoqueAtual() > 0);
        adicionarBtn.addClickListener(e -> {
            if (onAdicionarProduto != null) {
                Integer quantidade = quantidadeField.getValue();
                if (quantidade != null && quantidade > 0) {
                    onAdicionarProduto.accept(produto, quantidade);
                    quantidadeField.setValue(1); // Reset para 1 após adicionar
                }
            }
        });
        
        HorizontalLayout layout = new HorizontalLayout(quantidadeField, adicionarBtn);
        layout.setAlignItems(Alignment.CENTER);
        layout.setSpacing(false);
        
        return layout;
    }

    private void atualizarGridProdutos() {
        Pageable pageable = PageRequest.of(0, 50);
        
        String busca = buscaField.getValue();
        String categoria = categoriaComboBox.getValue();
        
        if (busca != null && !busca.trim().isEmpty()) {
            gridProdutos.setItems(produtoService.buscarPorTermo(busca.trim(), pageable).getContent());
        } else if (categoria != null && !categoria.trim().isEmpty()) {
            gridProdutos.setItems(produtoService.buscarPorCategoria(categoria, pageable).getContent());
        } else {
            gridProdutos.setItems(produtoService.listarProdutosAtivos(pageable).getContent());
        }
        
        gridProdutos.getDataProvider().refreshAll();
    }

    private void carregarCategorias() {
        try {
            categoriaComboBox.setItems(produtoService.listarCategorias());
        } catch (Exception e) {
            // Se houver erro ao carregar categorias, deixa vazio
            categoriaComboBox.setItems();
        }
    }

    public void refresh() {
        atualizarGridProdutos();
    }

    public void focarBusca() {
        buscaField.focus();
    }

    public void limparBusca() {
        buscaField.clear();
        categoriaComboBox.clear();
        atualizarGridProdutos();
    }

    public void setOnAdicionarProduto(BiConsumer<Produto, Integer> onAdicionarProduto) {
        this.onAdicionarProduto = onAdicionarProduto;
    }
}
