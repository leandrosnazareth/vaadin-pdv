package com.leandrosnazareth.venda.ui.component;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.function.Consumer;

import com.leandrosnazareth.venda.domain.ItemVenda;
import com.leandrosnazareth.venda.domain.Venda;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.NumberRenderer;
import com.vaadin.flow.theme.lumo.LumoUtility;

/**
 * Componente do carrinho de compras para o PDV.
 * <p>
 * Exibe os itens da venda atual com opções para alterar quantidades,
 * remover itens e visualizar o total.
 * </p>
 */
public class CarrinhoComponent extends VerticalLayout {

    private final Grid<ItemVenda> gridItens;
    private H3 totalLabel;
    private H4 quantidadeItensLabel;
    private Button limparCarrinhoButton;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
    
    private Venda vendaAtual;
    private Consumer<ItemVenda> onRemoverItem;
    private Consumer<ItemVenda> onIncrementarQuantidade;
    private Consumer<ItemVenda> onDecrementarQuantidade;
    private Runnable onLimparCarrinho;

    public CarrinhoComponent() {
        setWidth("100%");
        setHeight("100%");
        setPadding(false);
        setSpacing(false);
        
        // Cabeçalho
        HorizontalLayout header = criarCabecalho();
        
        // Grid de itens
        gridItens = criarGridItens();
        gridItens.addClassName(LumoUtility.Flex.GROW);
        
        // Rodapé com total
        VerticalLayout footer = criarRodape();
        
        add(header, gridItens, footer);
        
        atualizarInterface();
    }

    private HorizontalLayout criarCabecalho() {
        quantidadeItensLabel = new H4("Nenhum item no carrinho");
        quantidadeItensLabel.addClassName(LumoUtility.Margin.NONE);
        
        limparCarrinhoButton = new Button("Limpar", new Icon(VaadinIcon.TRASH));
        limparCarrinhoButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
        limparCarrinhoButton.addClickListener(e -> {
            if (onLimparCarrinho != null) {
                onLimparCarrinho.run();
            }
        });
        
        HorizontalLayout header = new HorizontalLayout(quantidadeItensLabel, limparCarrinhoButton);
        header.setWidthFull();
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);
        header.setAlignItems(Alignment.CENTER);
        header.setPadding(true);
        header.addClassName(LumoUtility.Background.CONTRAST_5);
        
        return header;
    }

    private Grid<ItemVenda> criarGridItens() {
        Grid<ItemVenda> grid = new Grid<>(ItemVenda.class, false);
        grid.setWidthFull();
        grid.setAllRowsVisible(true);
        
        // Coluna do produto
        grid.addColumn(item -> item.getProduto().getNome())
            .setHeader("Produto")
            .setFlexGrow(1)
            .setSortable(false);
        
        // Coluna do preço unitário
        grid.addColumn(new NumberRenderer<>(ItemVenda::getPrecoUnitario, currencyFormat))
            .setHeader("Preço")
            .setWidth("80px")
            .setSortable(false);
        
        // Coluna da quantidade com botões
        grid.addColumn(new ComponentRenderer<>(this::criarControleQuantidade))
            .setHeader("Qtd.")
            .setWidth("120px")
            .setSortable(false);
        
        // Coluna do subtotal
        grid.addColumn(new NumberRenderer<>(ItemVenda::getSubtotal, currencyFormat))
            .setHeader("Subtotal")
            .setWidth("100px")
            .setSortable(false);
        
        // Coluna de ações
        grid.addColumn(new ComponentRenderer<>(this::criarBotaoRemover))
            .setHeader("")
            .setWidth("50px")
            .setSortable(false);
        
        return grid;
    }

    private HorizontalLayout criarControleQuantidade(ItemVenda item) {
        Button decrementarBtn = new Button(new Icon(VaadinIcon.MINUS));
        decrementarBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        decrementarBtn.setEnabled(item.podeDecrementar());
        decrementarBtn.addClickListener(e -> {
            if (onDecrementarQuantidade != null) {
                onDecrementarQuantidade.accept(item);
            }
        });
        
        Span quantidadeSpan = new Span(item.getQuantidade().toString());
        quantidadeSpan.addClassName(LumoUtility.FontWeight.BOLD);
        quantidadeSpan.getStyle().set("min-width", "30px");
        quantidadeSpan.getStyle().set("text-align", "center");
        
        Button incrementarBtn = new Button(new Icon(VaadinIcon.PLUS));
        incrementarBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        incrementarBtn.addClickListener(e -> {
            if (onIncrementarQuantidade != null) {
                onIncrementarQuantidade.accept(item);
            }
        });
        
        HorizontalLayout layout = new HorizontalLayout(decrementarBtn, quantidadeSpan, incrementarBtn);
        layout.setAlignItems(Alignment.CENTER);
        layout.setJustifyContentMode(JustifyContentMode.CENTER);
        layout.setSpacing(false);
        
        return layout;
    }

    private Button criarBotaoRemover(ItemVenda item) {
        Button removerBtn = new Button(new Icon(VaadinIcon.TRASH));
        removerBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
        removerBtn.addClickListener(e -> {
            if (onRemoverItem != null) {
                onRemoverItem.accept(item);
            }
        });
        
        return removerBtn;
    }

    private VerticalLayout criarRodape() {
        totalLabel = new H3("Total: R$ 0,00");
        totalLabel.addClassName(LumoUtility.Margin.NONE);
        totalLabel.addClassName(LumoUtility.FontWeight.BOLD);
        totalLabel.addClassName(LumoUtility.TextColor.PRIMARY);
        
        VerticalLayout footer = new VerticalLayout(totalLabel);
        footer.setPadding(true);
        footer.setAlignItems(Alignment.END);
        footer.addClassName(LumoUtility.Background.CONTRAST_5);
        
        return footer;
    }

    public void setVenda(Venda venda) {
        this.vendaAtual = venda;
        atualizarInterface();
    }

    private void atualizarInterface() {
        if (vendaAtual == null || vendaAtual.getItens().isEmpty()) {
            gridItens.setItems();
            quantidadeItensLabel.setText("Nenhum item no carrinho");
            totalLabel.setText("Total: R$ 0,00");
            limparCarrinhoButton.setEnabled(false);
        } else {
            gridItens.setItems(vendaAtual.getItens());
            
            int totalItens = vendaAtual.getQuantidadeTotalProdutos();
            int tiposItens = vendaAtual.getQuantidadeItens();
            
            String labelText = tiposItens == 1 ? 
                String.format("%d produto (%d item)", tiposItens, totalItens) :
                String.format("%d produtos (%d itens)", tiposItens, totalItens);
            
            quantidadeItensLabel.setText(labelText);
            totalLabel.setText("Total: " + currencyFormat.format(vendaAtual.getValorTotal()));
            limparCarrinhoButton.setEnabled(true);
        }
        
        gridItens.getDataProvider().refreshAll();
    }

    public void refresh() {
        atualizarInterface();
    }

    // Getters e setters para os callbacks
    public void setOnRemoverItem(Consumer<ItemVenda> onRemoverItem) {
        this.onRemoverItem = onRemoverItem;
    }

    public void setOnIncrementarQuantidade(Consumer<ItemVenda> onIncrementarQuantidade) {
        this.onIncrementarQuantidade = onIncrementarQuantidade;
    }

    public void setOnDecrementarQuantidade(Consumer<ItemVenda> onDecrementarQuantidade) {
        this.onDecrementarQuantidade = onDecrementarQuantidade;
    }

    public void setOnLimparCarrinho(Runnable onLimparCarrinho) {
        this.onLimparCarrinho = onLimparCarrinho;
    }
}
