package com.leandrosnazareth.venda.ui.view;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.leandrosnazareth.produto.domain.Produto;
import com.leandrosnazareth.produto.service.ProdutoService;
import com.leandrosnazareth.venda.domain.ItemVenda;
import com.leandrosnazareth.venda.domain.Venda;
import com.leandrosnazareth.venda.service.VendaService;
import com.leandrosnazareth.venda.ui.component.BuscarProdutoComponent;
import com.leandrosnazareth.venda.ui.component.CarrinhoComponent;
import com.leandrosnazareth.venda.ui.component.PagamentoComponent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;

import jakarta.annotation.security.PermitAll;

@PageTitle("PDV - Ponto de Venda")
@Route(value = "pdv", layout = com.leandrosnazareth.base.ui.view.MainLayout.class)
@Menu(order = 0, icon = "vaadin:shop", title = "PDV")
@PermitAll
public class PDVView extends VerticalLayout {

    private final VendaService vendaService;
    private final ProdutoService produtoService;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    // Componentes principais
    private BuscarProdutoComponent buscarProdutoComponent;
    private CarrinhoComponent carrinhoComponent;
    private PagamentoComponent pagamentoComponent;
    private Tabs tabs;
    private Map<Tab, Component> tabsToComponents;

    // Estado da venda
    private Venda vendaAtual;

    // Estatísticas
    private Span totalVendasHojeLabel;
    private Span quantidadeVendasHojeLabel;

    public PDVView(VendaService vendaService, ProdutoService produtoService) {
        this.vendaService = vendaService;
        this.produtoService = produtoService;

        setSizeFull();
        setPadding(false);
        setSpacing(false);

        criarInterface();
        configurarEventos();
        carregarVendaAtual();
        atualizarEstatisticas();
    }

    private void criarInterface() {
        // Cabeçalho
        HorizontalLayout cabecalho = criarCabecalho();

        // Área principal dividida
        SplitLayout splitLayout = criarAreaPrincipal();
        splitLayout.addClassName(LumoUtility.Flex.GROW);

        add(cabecalho, splitLayout);
    }

    private HorizontalLayout criarCabecalho() {
        H1 titulo = new H1("PDV");
        titulo.addClassName(LumoUtility.Margin.NONE);
        titulo.addClassName(LumoUtility.TextColor.PRIMARY);

        // Estatísticas do dia
        totalVendasHojeLabel = new Span("Total: R$ 0,00");
        totalVendasHojeLabel.addClassName(LumoUtility.FontWeight.BOLD);
        totalVendasHojeLabel.addClassName(LumoUtility.TextColor.SUCCESS);

        quantidadeVendasHojeLabel = new Span("Vendas: 0");
        quantidadeVendasHojeLabel.addClassName(LumoUtility.FontWeight.BOLD);

        VerticalLayout estatisticas = new VerticalLayout(totalVendasHojeLabel, quantidadeVendasHojeLabel);
        estatisticas.setSpacing(false);
        estatisticas.setPadding(false);

        // Botões de ação
        Button novaVendaBtn = new Button("Nova Venda", new Icon(VaadinIcon.PLUS));
        novaVendaBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        novaVendaBtn.addClickListener(e -> iniciarNovaVenda());

        Button historicoBtn = new Button("Histórico", new Icon(VaadinIcon.RECORDS));
        historicoBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        historicoBtn.addClickListener(e -> abrirHistorico());

        HorizontalLayout botoes = new HorizontalLayout(novaVendaBtn, historicoBtn);
        botoes.setSpacing(true);

        HorizontalLayout cabecalho = new HorizontalLayout(titulo, estatisticas, botoes);
        cabecalho.setWidthFull();
        cabecalho.setJustifyContentMode(JustifyContentMode.BETWEEN);
        cabecalho.setAlignItems(Alignment.CENTER);
        cabecalho.setPadding(true);
        cabecalho.addClassName(LumoUtility.Background.CONTRAST_5);

        return cabecalho;
    }

    private SplitLayout criarAreaPrincipal() {
        // Lado esquerdo: Busca de produtos e carrinho
        VerticalLayout ladoEsquerdo = criarLadoEsquerdo();

        // Lado direito: Pagamento
        VerticalLayout ladoDireito = criarLadoDireito();

        SplitLayout splitLayout = new SplitLayout(ladoEsquerdo, ladoDireito);
        splitLayout.setSplitterPosition(70);
        splitLayout.setSizeFull();

        return splitLayout;
    }

    private VerticalLayout criarLadoEsquerdo() {
        // Componentes
        buscarProdutoComponent = new BuscarProdutoComponent(produtoService);
        carrinhoComponent = new CarrinhoComponent();

        // Abas
        Tab buscarTab = new Tab("Buscar Produtos");
        Tab carrinhoTab = new Tab("Carrinho");

        tabs = new Tabs(buscarTab, carrinhoTab);
        tabs.setWidthFull();

        // Mapeamento de abas para componentes
        tabsToComponents = new HashMap<>();
        tabsToComponents.put(buscarTab, buscarProdutoComponent);
        tabsToComponents.put(carrinhoTab, carrinhoComponent);

        // Container para o conteúdo das abas
        VerticalLayout conteudoAbas = new VerticalLayout();
        conteudoAbas.setSizeFull();
        conteudoAbas.setPadding(false);
        conteudoAbas.setSpacing(false);
        conteudoAbas.addClassName(LumoUtility.Flex.GROW);

        // Inicialmente mostra a aba de busca
        conteudoAbas.add(buscarProdutoComponent);

        // Listener para mudança de aba
        tabs.addSelectedChangeListener(event -> {
            conteudoAbas.removeAll();
            Component selectedComponent = tabsToComponents.get(event.getSelectedTab());
            if (selectedComponent != null) {
                conteudoAbas.add(selectedComponent);
            }
        });

        VerticalLayout ladoEsquerdo = new VerticalLayout(tabs, conteudoAbas);
        ladoEsquerdo.setSizeFull();
        ladoEsquerdo.setPadding(false);
        ladoEsquerdo.setSpacing(false);

        return ladoEsquerdo;
    }

    private VerticalLayout criarLadoDireito() {
        pagamentoComponent = new PagamentoComponent();

        VerticalLayout ladoDireito = new VerticalLayout(pagamentoComponent);
        ladoDireito.setSizeFull();
        ladoDireito.setPadding(false);
        ladoDireito.setSpacing(false);

        return ladoDireito;
    }

    private void configurarEventos() {
        // Buscar produto - adicionar ao carrinho
        buscarProdutoComponent.setOnAdicionarProduto(this::adicionarProdutoAoCarrinho);

        // Carrinho - remover item
        carrinhoComponent.setOnRemoverItem(this::removerItemDoCarrinho);

        // Carrinho - incrementar quantidade
        carrinhoComponent.setOnIncrementarQuantidade(this::incrementarQuantidadeItem);

        // Carrinho - decrementar quantidade
        carrinhoComponent.setOnDecrementarQuantidade(this::decrementarQuantidadeItem);

        // Carrinho - limpar carrinho
        carrinhoComponent.setOnLimparCarrinho(this::limparCarrinho);

        // Pagamento - finalizar venda
        pagamentoComponent.setOnFinalizarVenda(this::finalizarVenda);

        // Pagamento - cancelar venda
        pagamentoComponent.setOnCancelarVenda(this::cancelarVenda);

        // Pagamento - aplicar desconto
        pagamentoComponent.setOnAplicarDesconto(this::aplicarDesconto);
    }

    private void carregarVendaAtual() {
        try {
            vendaAtual = vendaService.buscarOuCriarVendaPendente(Venda.FormaPagamento.DINHEIRO);
            atualizarInterfaceVenda();

            // Foca na busca de produtos
            buscarProdutoComponent.focarBusca();

            if (vendaAtual.getItens().isEmpty()) {
                Notification.show("Venda carregada - adicione produtos ao carrinho", 2000,
                        Notification.Position.MIDDLE);
            } else {
                Notification.show("Venda em andamento carregada", 2000, Notification.Position.MIDDLE);
            }
        } catch (Exception e) {
            Notification.show("Erro ao carregar venda: " + e.getMessage(), 5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void iniciarNovaVenda() {
        try {
            vendaAtual = vendaService.criarVenda(Venda.FormaPagamento.DINHEIRO);
            atualizarInterfaceVenda();

            // Foca na busca de produtos
            buscarProdutoComponent.focarBusca();

            Notification.show("Nova venda iniciada!", 2000, Notification.Position.MIDDLE);
        } catch (Exception e) {
            Notification.show("Erro ao iniciar nova venda: " + e.getMessage(), 5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void adicionarProdutoAoCarrinho(Produto produto, Integer quantidade) {
        try {
            vendaAtual = vendaService.adicionarItem(vendaAtual.getId(), produto.getId(), quantidade);
            atualizarInterfaceVenda();

            // Alterna para a aba do carrinho
            tabs.setSelectedIndex(1);

            Notification.show(String.format("Produto '%s' adicionado ao carrinho!", produto.getNome()),
                    2000, Notification.Position.MIDDLE);
        } catch (Exception e) {
            Notification.show("Erro ao adicionar produto: " + e.getMessage(), 5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void removerItemDoCarrinho(ItemVenda item) {
        try {
            vendaAtual = vendaService.removerItem(vendaAtual.getId(), item.getId());
            atualizarInterfaceVenda();

            Notification.show(String.format("Produto '%s' removido do carrinho!", item.getProduto().getNome()),
                    2000, Notification.Position.MIDDLE);
        } catch (Exception e) {
            Notification.show("Erro ao remover item: " + e.getMessage(), 5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void incrementarQuantidadeItem(ItemVenda item) {
        try {
            vendaAtual = vendaService.atualizarQuantidadeItem(vendaAtual.getId(), item.getId(),
                    item.getQuantidade() + 1);
            atualizarInterfaceVenda();
        } catch (Exception e) {
            Notification.show("Erro ao incrementar quantidade: " + e.getMessage(), 5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void decrementarQuantidadeItem(ItemVenda item) {
        try {
            vendaAtual = vendaService.atualizarQuantidadeItem(vendaAtual.getId(), item.getId(),
                    item.getQuantidade() - 1);
            atualizarInterfaceVenda();
        } catch (Exception e) {
            Notification.show("Erro ao decrementar quantidade: " + e.getMessage(), 5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void limparCarrinho() {
        try {
            vendaService.cancelarVenda(vendaAtual.getId());
            carregarVendaAtual();

            Notification.show("Carrinho limpo!", 2000, Notification.Position.MIDDLE);
        } catch (Exception e) {
            Notification.show("Erro ao limpar carrinho: " + e.getMessage(), 5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void aplicarDesconto(Venda venda) {
        try {
            vendaAtual = vendaService.aplicarDesconto(venda.getId(), venda.getDesconto());
            atualizarInterfaceVenda();
        } catch (Exception e) {
            Notification.show("Erro ao aplicar desconto: " + e.getMessage(), 5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void finalizarVenda(Venda venda) {
        try {
            vendaAtual = vendaService.finalizarVenda(venda.getId(), venda.getValorRecebido());

            // Mostra cupom fiscal
            mostrarCupomFiscal(vendaAtual);

            // Atualiza estatísticas
            atualizarEstatisticas();

            // Inicia nova venda
            carregarVendaAtual();

            Notification.show("Venda finalizada com sucesso!", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } catch (Exception e) {
            Notification.show("Erro ao finalizar venda: " + e.getMessage(), 5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void cancelarVenda(Venda venda) {
        try {
            vendaService.cancelarVenda(venda.getId());
            carregarVendaAtual();

            Notification.show("Venda cancelada!", 2000, Notification.Position.MIDDLE);
        } catch (Exception e) {
            Notification.show("Erro ao cancelar venda: " + e.getMessage(), 5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void atualizarInterfaceVenda() {
        carrinhoComponent.setVenda(vendaAtual);
        pagamentoComponent.setVenda(vendaAtual);

        // Atualiza dados de busca
        buscarProdutoComponent.refresh();
    }

    private void mostrarCupomFiscal(Venda venda) {
        Dialog dialog = new Dialog();
        dialog.setWidth("400px");
        dialog.setHeight("500px");
        dialog.setDraggable(true);
        dialog.setResizable(true);

        VerticalLayout cupomLayout = criarCupomFiscal(venda);

        Button fecharBtn = new Button("Fechar", e -> dialog.close());
        fecharBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button imprimirBtn = new Button("Imprimir", new Icon(VaadinIcon.PRINT));
        imprimirBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        // TODO: Implementar impressão

        HorizontalLayout botoes = new HorizontalLayout(imprimirBtn, fecharBtn);
        botoes.setJustifyContentMode(JustifyContentMode.END);

        VerticalLayout dialogContent = new VerticalLayout(cupomLayout, botoes);
        dialogContent.setSizeFull();
        dialogContent.setSpacing(true);

        dialog.add(dialogContent);
        dialog.open();
    }

    private VerticalLayout criarCupomFiscal(Venda venda) {
        VerticalLayout cupom = new VerticalLayout();
        cupom.setSpacing(false);
        cupom.setPadding(true);
        cupom.addClassName(LumoUtility.Background.CONTRAST_5);

        // Cabeçalho
        H2 titulo = new H2("CUPOM FISCAL");
        titulo.getStyle().set("text-align", "center");
        titulo.addClassName(LumoUtility.Margin.NONE);

        Span data = new Span("Data: "
                + venda.getDataVenda().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        Span numeroVenda = new Span("Venda: #" + venda.getId());

        cupom.add(titulo, data, numeroVenda);

        // Itens
        cupom.add(new H3("ITENS:"));
        for (ItemVenda item : venda.getItens()) {
            HorizontalLayout itemLayout = new HorizontalLayout();
            itemLayout.setWidthFull();
            itemLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);

            Span produtoInfo = new Span(String.format("%s (%dx)", item.getProduto().getNome(), item.getQuantidade()));
            Span valorInfo = new Span(currencyFormat.format(item.getSubtotal()));

            itemLayout.add(produtoInfo, valorInfo);
            cupom.add(itemLayout);
        }

        // Totais
        if (venda.getDesconto() != null && venda.getDesconto().compareTo(BigDecimal.ZERO) > 0) {
            HorizontalLayout descontoLayout = new HorizontalLayout();
            descontoLayout.setWidthFull();
            descontoLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);
            descontoLayout.add(new Span("Desconto:"), new Span("- " + currencyFormat.format(venda.getDesconto())));
            cupom.add(descontoLayout);
        }

        HorizontalLayout totalLayout = new HorizontalLayout();
        totalLayout.setWidthFull();
        totalLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);

        Span totalLabel = new Span("TOTAL:");
        totalLabel.addClassName(LumoUtility.FontWeight.BOLD);

        Span totalValue = new Span(currencyFormat.format(venda.getValorTotal()));
        totalValue.addClassName(LumoUtility.FontWeight.BOLD);

        totalLayout.add(totalLabel, totalValue);
        cupom.add(totalLayout);

        // Pagamento
        cupom.add(new Span("Forma de pagamento: " + venda.getFormaPagamento().getDescricao()));
        cupom.add(new Span("Valor recebido: " + currencyFormat.format(venda.getValorRecebido())));
        cupom.add(new Span("Troco: " + currencyFormat.format(venda.getTroco())));

        return cupom;
    }

    private void atualizarEstatisticas() {
        try {
            BigDecimal totalHoje = vendaService.calcularTotalVendasHoje();
            long quantidadeHoje = vendaService.contarVendasHoje();

            totalVendasHojeLabel.setText("Total: " + currencyFormat.format(totalHoje));
            quantidadeVendasHojeLabel.setText("Vendas: " + quantidadeHoje);
        } catch (Exception e) {
            totalVendasHojeLabel.setText("Total: R$ 0,00");
            quantidadeVendasHojeLabel.setText("Vendas: 0");
        }
    }

    private void abrirHistorico() {
        // Navegar para a tela de histórico de vendas
        UI.getCurrent().navigate("vendas");
    }
}
