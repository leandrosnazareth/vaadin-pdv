package com.leandrosnazareth.base.ui.view;

import com.leandrosnazareth.produto.service.ProdutoService;
import com.leandrosnazareth.venda.service.VendaService;
import com.leandrosnazareth.venda.domain.Venda;
import com.leandrosnazareth.produto.domain.Produto;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@PageTitle("Dashboard")
@Route(value = "", layout = com.leandrosnazareth.base.ui.view.MainLayout.class)
@Menu(order = -1, icon = "vaadin:dashboard", title = "Dashboard")
@PermitAll
@CssImport("./styles/dashboard.css")
public final class MainView extends Main {

    private final VendaService vendaService;
    private final ProdutoService produtoService;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    // Componentes de estatísticas
    private Span totalVendasHojeLabel;
    private Span totalVendasMesLabel;
    private Span quantidadeVendasHojeLabel;
    private Span quantidadeVendasMesLabel;
    private Span totalProdutosLabel;
    private Span ticketMedioLabel;

    // Estatísticas adicionais
    private VerticalLayout estatisticasVendas30Dias;
    private VerticalLayout estatisticasFormasPagamento;

    // Tabelas
    private Grid<Object[]> produtosMaisVendidosGrid;
    private Grid<Produto> produtosEstoqueBaixoGrid;
    private Grid<Venda> ultimasVendasGrid;

    public MainView(VendaService vendaService, ProdutoService produtoService) {
        this.vendaService = vendaService;
        this.produtoService = produtoService;

        addClassName(LumoUtility.Background.BASE);
        addClassName(LumoUtility.Padding.NONE);
        addClassName("main-view");
        setSizeFull();
        setMinHeight("100vh");

        criarInterface();
        carregarDados();
    }

    public static void showMainView() {
        UI.getCurrent().navigate(MainView.class);
    }

    private void criarInterface() {
        // Container principal com gradiente sutil
        VerticalLayout mainContainer = new VerticalLayout();
        mainContainer.setSizeFull();
        mainContainer.setPadding(false);
        mainContainer.setSpacing(false);
        mainContainer.setMinHeight("100vh");
        mainContainer.addClassName("main-container");
        mainContainer.getStyle().set("background",
                "linear-gradient(135deg, var(--lumo-contrast-5pct) 0%, var(--lumo-primary-color-10pct) 100%)");

        // Header com título e informações
        VerticalLayout header = criarHeader();

        // Container para o conteúdo principal
        VerticalLayout contentContainer = new VerticalLayout();
        contentContainer.addClassName(LumoUtility.Padding.LARGE);
        contentContainer.setSpacing(true);
        contentContainer.setFlexGrow(1);
        contentContainer.setSizeFull();

        // Criar seções do dashboard
        HorizontalLayout primeiraLinha = criarPrimeiraLinha();
        HorizontalLayout segundaLinha = criarSegundaLinha();
        HorizontalLayout terceiraLinha = criarTerceiraLinha();

        contentContainer.add(primeiraLinha, segundaLinha, terceiraLinha);
        mainContainer.add(header, contentContainer);

        add(mainContainer);
    }

    private VerticalLayout criarHeader() {
        VerticalLayout header = new VerticalLayout();
        header.addClassName(LumoUtility.Background.PRIMARY);
        header.addClassName(LumoUtility.Padding.LARGE);
        header.setSpacing(false);
        header.getStyle().set("background",
                "linear-gradient(135deg, var(--lumo-primary-color) 0%, var(--lumo-primary-color-50pct) 100%)");
        header.getStyle().set("box-shadow", "0 4px 20px rgba(0,0,0,0.1)");

        // Título principal
        H1 titulo = new H1("📊 Dashboard - Sistema PDV");
        titulo.addClassName(LumoUtility.Margin.NONE);
        titulo.addClassName(LumoUtility.TextColor.PRIMARY_CONTRAST);
        titulo.addClassName(LumoUtility.FontSize.XXLARGE);
        titulo.getStyle().set("text-shadow", "0 2px 4px rgba(0,0,0,0.1)");

        // Subtítulo com data atual
        Span subtitulo = new Span("Visão geral das operações em tempo real • " +
                java.time.LocalDate.now()
                        .format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", new Locale("pt", "BR"))));
        subtitulo.addClassName(LumoUtility.TextColor.PRIMARY_CONTRAST);
        subtitulo.addClassName(LumoUtility.FontSize.MEDIUM);
        subtitulo.getStyle().set("opacity", "0.8");
        subtitulo.addClassName(LumoUtility.Margin.Top.SMALL);

        header.add(titulo, subtitulo);
        return header;
    }

    private HorizontalLayout criarPrimeiraLinha() {
        HorizontalLayout linha = new HorizontalLayout();
        linha.setWidthFull();
        linha.setSpacing(true);
        linha.addClassName(LumoUtility.Gap.LARGE);

        // Cards de estatísticas principais com animações
        VerticalLayout cardVendasHoje = criarCardEstatistica(
                "💰 Vendas Hoje",
                VaadinIcon.DOLLAR,
                "R$ 0,00",
                "0 vendas realizadas",
                "success",
                "linear-gradient(135deg, #4CAF50 0%, #45a049 100%)");

        VerticalLayout cardVendasMes = criarCardEstatistica(
                "📅 Vendas do Mês",
                VaadinIcon.CALENDAR_CLOCK,
                "R$ 0,00",
                "0 vendas no período",
                "primary",
                "linear-gradient(135deg, #2196F3 0%, #1976D2 100%)");

        VerticalLayout cardProdutos = criarCardEstatistica(
                "📦 Produtos Ativos",
                VaadinIcon.PACKAGE,
                "0",
                "produtos no catálogo",
                "contrast",
                "linear-gradient(135deg, #9C27B0 0%, #7B1FA2 100%)");

        VerticalLayout cardTicketMedio = criarCardEstatistica(
                "📈 Ticket Médio",
                VaadinIcon.CHART_LINE,
                "R$ 0,00",
                "média do mês atual",
                "warning",
                "linear-gradient(135deg, #FF9800 0%, #F57C00 100%)");

        // Armazenar referências para os labels
        totalVendasHojeLabel = (Span) ((VerticalLayout) cardVendasHoje.getComponentAt(1)).getComponentAt(0);
        quantidadeVendasHojeLabel = (Span) ((VerticalLayout) cardVendasHoje.getComponentAt(1)).getComponentAt(1);

        totalVendasMesLabel = (Span) ((VerticalLayout) cardVendasMes.getComponentAt(1)).getComponentAt(0);
        quantidadeVendasMesLabel = (Span) ((VerticalLayout) cardVendasMes.getComponentAt(1)).getComponentAt(1);

        totalProdutosLabel = (Span) ((VerticalLayout) cardProdutos.getComponentAt(1)).getComponentAt(0);
        ticketMedioLabel = (Span) ((VerticalLayout) cardTicketMedio.getComponentAt(1)).getComponentAt(0);

        linha.add(cardVendasHoje, cardVendasMes, cardProdutos, cardTicketMedio);
        return linha;
    }

    private HorizontalLayout criarSegundaLinha() {
        HorizontalLayout linha = new HorizontalLayout();
        linha.setWidthFull();
        linha.setSpacing(true);
        linha.addClassName(LumoUtility.Gap.LARGE);

        // Estatísticas de vendas dos últimos 30 dias com fundo branco
        VerticalLayout cardEstatisticasVendas = criarCardEstatisticasTextuais(
                "📊 Vendas dos Últimos 30 Dias",
                criarEstatisticasVendas30Dias(),
                "linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%)");

        // Estatísticas de vendas por forma de pagamento com fundo branco
        VerticalLayout cardEstatisticasFormasPagamento = criarCardEstatisticasTextuais(
                "💳 Vendas por Forma de Pagamento",
                criarEstatisticasFormasPagamento(),
                "linear-gradient(135deg, #ffffff 0%, #f1f3f4 100%)");

        linha.add(cardEstatisticasVendas, cardEstatisticasFormasPagamento);
        return linha;
    }

    private HorizontalLayout criarTerceiraLinha() {
        HorizontalLayout linha = new HorizontalLayout();
        linha.setWidthFull();
        linha.setSpacing(true);
        linha.addClassName(LumoUtility.Gap.LARGE);

        // Produtos mais vendidos com design discreto
        VerticalLayout cardProdutosMaisVendidos = criarCardTabela(
                "🏆 Top Produtos Vendidos",
                criarTabelaProdutosMaisVendidos(),
                "linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%)");

        // Produtos com estoque baixo com design discreto
        VerticalLayout cardEstoqueBaixo = criarCardTabela(
                "⚠️ Alertas de Estoque",
                criarTabelaEstoqueBaixo(),
                "linear-gradient(135deg, #ffffff 0%, #f1f3f4 100%)");

        // Últimas vendas com design discreto
        VerticalLayout cardUltimasVendas = criarCardTabela(
                "🕐 Vendas Recentes",
                criarTabelaUltimasVendas(),
                "linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%)");

        linha.add(cardProdutosMaisVendidos, cardEstoqueBaixo, cardUltimasVendas);
        return linha;
    }

    private VerticalLayout criarCardEstatistica(String titulo, VaadinIcon icone, String valor, String descricao,
            String tema, String gradiente) {
        VerticalLayout card = new VerticalLayout();
        card.addClassName(LumoUtility.BorderRadius.LARGE);
        card.addClassName(LumoUtility.Padding.LARGE);
        card.setSpacing(false);
        card.setWidthFull();
        card.setHeight("160px");

        // Aplicar gradiente e efeitos visuais
        card.getStyle().set("background", gradiente);
        card.getStyle().set("box-shadow", "0 8px 32px rgba(0,0,0,0.12)");
        card.getStyle().set("transition", "transform 0.3s ease, box-shadow 0.3s ease");
        card.getStyle().set("cursor", "pointer");
        card.addClassName("dashboard-card");
        card.addClassName("gradient-bg");
        card.addClassName("fade-in");

        // Efeito hover
        card.getElement().addEventListener("mouseenter", e -> {
            card.getStyle().set("transform", "translateY(-4px)");
            card.getStyle().set("box-shadow", "0 12px 48px rgba(0,0,0,0.15)");
        });

        card.getElement().addEventListener("mouseleave", e -> {
            card.getStyle().set("transform", "translateY(0)");
            card.getStyle().set("box-shadow", "0 8px 32px rgba(0,0,0,0.12)");
        });

        // Cabeçalho com ícone e título
        HorizontalLayout header = new HorizontalLayout();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setSpacing(true);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setWidthFull();

        // Container para ícone e título
        HorizontalLayout iconeTitulo = new HorizontalLayout();
        iconeTitulo.setAlignItems(FlexComponent.Alignment.CENTER);
        iconeTitulo.setSpacing(true);

        Icon icon = new Icon(icone);
        icon.setSize("32px");
        icon.getStyle().set("color", "white");
        icon.getStyle().set("filter", "drop-shadow(0 2px 4px rgba(0,0,0,0.2))");

        H3 tituloH3 = new H3(titulo);
        tituloH3.addClassName(LumoUtility.Margin.NONE);
        tituloH3.addClassName(LumoUtility.FontSize.MEDIUM);
        tituloH3.getStyle().set("color", "white");
        tituloH3.getStyle().set("font-weight", "600");
        tituloH3.getStyle().set("text-shadow", "0 1px 2px rgba(0,0,0,0.1)");

        iconeTitulo.add(icon, tituloH3);
        header.add(iconeTitulo);

        // Valores
        VerticalLayout valores = new VerticalLayout();
        valores.setSpacing(false);
        valores.setPadding(false);
        valores.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        valores.setFlexGrow(1);

        Span valorLabel = new Span(valor);
        valorLabel.addClassName(LumoUtility.FontSize.XXLARGE);
        valorLabel.addClassName(LumoUtility.FontWeight.BOLD);
        valorLabel.getStyle().set("color", "white");
        valorLabel.getStyle().set("text-shadow", "0 2px 4px rgba(0,0,0,0.2)");
        valorLabel.getStyle().set("line-height", "1.2");

        Span descricaoLabel = new Span(descricao);
        descricaoLabel.addClassName(LumoUtility.FontSize.SMALL);
        descricaoLabel.getStyle().set("color", "rgba(255,255,255,0.9)");
        descricaoLabel.getStyle().set("margin-top", "4px");

        valores.add(valorLabel, descricaoLabel);

        card.add(header, valores);
        return card;
    }

    private VerticalLayout criarCardEstatisticasTextuais(String titulo, VerticalLayout conteudo, String gradiente) {
        VerticalLayout card = new VerticalLayout();
        card.addClassName(LumoUtility.BorderRadius.LARGE);
        card.addClassName(LumoUtility.Padding.LARGE);
        card.setWidthFull();
        card.setHeight("450px");

        // Aplicar gradiente e efeitos visuais
        card.getStyle().set("background", gradiente);
        card.getStyle().set("box-shadow", "0 8px 32px rgba(0,0,0,0.12)");
        card.getStyle().set("transition", "transform 0.3s ease, box-shadow 0.3s ease");
        card.addClassName("dashboard-card");
        card.addClassName("glass-effect");
        card.addClassName("fade-in");

        // Efeito hover
        card.getElement().addEventListener("mouseenter", e -> {
            card.getStyle().set("transform", "translateY(-2px)");
            card.getStyle().set("box-shadow", "0 12px 48px rgba(0,0,0,0.15)");
        });

        card.getElement().addEventListener("mouseleave", e -> {
            card.getStyle().set("transform", "translateY(0)");
            card.getStyle().set("box-shadow", "0 8px 32px rgba(0,0,0,0.12)");
        });

        H3 tituloH3 = new H3(titulo);
        tituloH3.addClassName(LumoUtility.Margin.NONE);
        tituloH3.addClassName(LumoUtility.Margin.Bottom.MEDIUM);
        tituloH3.addClassName(LumoUtility.FontSize.LARGE);
        tituloH3.addClassName(LumoUtility.TextColor.HEADER);
        tituloH3.getStyle().set("font-weight", "700");
        tituloH3.getStyle().set("color", "var(--lumo-contrast-90pct)");

        // Container para o conteúdo com fundo semi-transparente
        VerticalLayout conteudoContainer = new VerticalLayout();
        conteudoContainer.setWidthFull();
        conteudoContainer.setFlexGrow(1);
        conteudoContainer.addClassName(LumoUtility.Padding.MEDIUM);
        conteudoContainer.addClassName(LumoUtility.BorderRadius.MEDIUM);
        conteudoContainer.getStyle().set("background", "rgba(255,255,255,0.8)");
        conteudoContainer.getStyle().set("backdrop-filter", "blur(10px)");
        conteudoContainer.getStyle().set("border", "1px solid rgba(255,255,255,0.4)");
        conteudoContainer.getStyle().set("box-shadow", "0 2px 8px rgba(0,0,0,0.05)");

        conteudoContainer.add(conteudo);
        card.add(tituloH3, conteudoContainer);
        return card;
    }

    private VerticalLayout criarEstatisticasVendas30Dias() {
        estatisticasVendas30Dias = new VerticalLayout();
        estatisticasVendas30Dias.setSpacing(true);
        estatisticasVendas30Dias.setPadding(false);
        estatisticasVendas30Dias.setSizeFull();

        // Placeholder estilizado que será preenchido com dados reais
        VerticalLayout placeholder = new VerticalLayout();
        placeholder.setAlignItems(FlexComponent.Alignment.CENTER);
        placeholder.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        placeholder.setHeight("200px");

        Icon loadingIcon = new Icon(VaadinIcon.SPINNER);
        loadingIcon.setSize("32px");
        loadingIcon.getStyle().set("color", "var(--lumo-contrast-60pct)");
        loadingIcon.getStyle().set("animation", "spin 2s linear infinite");

        Span loadingText = new Span("Carregando dados de vendas...");
        loadingText.addClassName(LumoUtility.FontSize.MEDIUM);
        loadingText.getStyle().set("color", "var(--lumo-contrast-70pct)");
        loadingText.addClassName(LumoUtility.Margin.Top.MEDIUM);

        placeholder.add(loadingIcon, loadingText);
        estatisticasVendas30Dias.add(placeholder);

        return estatisticasVendas30Dias;
    }

    private VerticalLayout criarEstatisticasFormasPagamento() {
        estatisticasFormasPagamento = new VerticalLayout();
        estatisticasFormasPagamento.setSpacing(true);
        estatisticasFormasPagamento.setPadding(false);
        estatisticasFormasPagamento.setSizeFull();

        // Placeholder estilizado que será preenchido com dados reais
        VerticalLayout placeholder = new VerticalLayout();
        placeholder.setAlignItems(FlexComponent.Alignment.CENTER);
        placeholder.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        placeholder.setHeight("200px");

        Icon loadingIcon = new Icon(VaadinIcon.CREDIT_CARD);
        loadingIcon.setSize("32px");
        loadingIcon.getStyle().set("color", "var(--lumo-contrast-60pct)");

        Span loadingText = new Span("Carregando dados de pagamento...");
        loadingText.addClassName(LumoUtility.FontSize.MEDIUM);
        loadingText.getStyle().set("color", "var(--lumo-contrast-70pct)");
        loadingText.addClassName(LumoUtility.Margin.Top.MEDIUM);

        placeholder.add(loadingIcon, loadingText);
        estatisticasFormasPagamento.add(placeholder);

        return estatisticasFormasPagamento;
    }

    private VerticalLayout criarCardTabela(String titulo, com.vaadin.flow.component.Component tabela,
            String gradiente) {
        VerticalLayout card = new VerticalLayout();
        card.addClassName(LumoUtility.BorderRadius.LARGE);
        card.addClassName(LumoUtility.Padding.LARGE);
        card.setWidthFull();
        card.setHeight("500px");

        // Aplicar gradiente e efeitos visuais
        card.getStyle().set("background", gradiente);
        card.getStyle().set("box-shadow", "0 8px 32px rgba(0,0,0,0.12)");
        card.getStyle().set("transition", "transform 0.3s ease, box-shadow 0.3s ease");
        card.addClassName("dashboard-card");
        card.addClassName("glass-effect");
        card.addClassName("fade-in");

        // Efeito hover
        card.getElement().addEventListener("mouseenter", e -> {
            card.getStyle().set("transform", "translateY(-2px)");
            card.getStyle().set("box-shadow", "0 12px 48px rgba(0,0,0,0.15)");
        });

        card.getElement().addEventListener("mouseleave", e -> {
            card.getStyle().set("transform", "translateY(0)");
            card.getStyle().set("box-shadow", "0 8px 32px rgba(0,0,0,0.12)");
        });

        H3 tituloH3 = new H3(titulo);
        tituloH3.addClassName(LumoUtility.Margin.NONE);
        tituloH3.addClassName(LumoUtility.Margin.Bottom.MEDIUM);
        tituloH3.addClassName(LumoUtility.FontSize.LARGE);
        tituloH3.addClassName(LumoUtility.TextColor.HEADER);
        tituloH3.getStyle().set("font-weight", "700");
        tituloH3.getStyle().set("color", "var(--lumo-contrast-90pct)");

        // Container para a tabela com fundo
        VerticalLayout tabelaContainer = new VerticalLayout();
        tabelaContainer.setWidthFull();
        tabelaContainer.setFlexGrow(1);
        tabelaContainer.setPadding(false);
        tabelaContainer.addClassName(LumoUtility.BorderRadius.MEDIUM);
        tabelaContainer.getStyle().set("background", "rgba(255,255,255,0.9)");
        tabelaContainer.getStyle().set("backdrop-filter", "blur(10px)");
        tabelaContainer.getStyle().set("border", "1px solid rgba(255,255,255,0.5)");
        tabelaContainer.getStyle().set("overflow", "hidden");
        tabelaContainer.getStyle().set("box-shadow", "0 2px 8px rgba(0,0,0,0.05)");

        tabelaContainer.add(tabela);
        card.add(tituloH3, tabelaContainer);
        return card;
    }

    private void carregarDados() {
        try {
            carregarEstatisticasVendas();
            carregarEstatisticasProdutos();
            carregarEstatisticasTextuais();
            carregarTabelas();
        } catch (Exception e) {
            // Em caso de erro, exibir dados padrão
            System.err.println("Erro ao carregar dados do dashboard: " + e.getMessage());
        }
    }

    private Grid<Object[]> criarTabelaProdutosMaisVendidos() {
        produtosMaisVendidosGrid = new Grid<>();
        produtosMaisVendidosGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
        produtosMaisVendidosGrid.setWidthFull();
        produtosMaisVendidosGrid.setHeight("350px");
        produtosMaisVendidosGrid.addClassName(LumoUtility.BorderRadius.MEDIUM);
        produtosMaisVendidosGrid.addClassName("dashboard-grid");
        produtosMaisVendidosGrid.addClassName("fade-in");

        // Estilo personalizado para headers
        produtosMaisVendidosGrid.getStyle().set("--lumo-header-color", "var(--lumo-primary-text-color)");
        produtosMaisVendidosGrid.getStyle().set("--lumo-header-font-weight", "600");

        produtosMaisVendidosGrid.addColumn(item -> (String) item[1])
                .setHeader("🏷️ Produto")
                .setFlexGrow(1);

        produtosMaisVendidosGrid.addColumn(item -> {
            Long quantidade = (Long) item[2];
            return "🛒 " + quantidade.toString() + " un";
        })
                .setHeader("📊 Quantidade")
                .setFlexGrow(0)
                .setWidth("120px");

        return produtosMaisVendidosGrid;
    }

    private Grid<Produto> criarTabelaEstoqueBaixo() {
        produtosEstoqueBaixoGrid = new Grid<>(Produto.class, false);
        produtosEstoqueBaixoGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
        produtosEstoqueBaixoGrid.setWidthFull();
        produtosEstoqueBaixoGrid.setHeight("350px");
        produtosEstoqueBaixoGrid.addClassName(LumoUtility.BorderRadius.MEDIUM);
        produtosEstoqueBaixoGrid.addClassName("dashboard-grid");
        produtosEstoqueBaixoGrid.addClassName("fade-in");

        // Estilo personalizado para headers
        produtosEstoqueBaixoGrid.getStyle().set("--lumo-header-color", "var(--lumo-primary-text-color)");
        produtosEstoqueBaixoGrid.getStyle().set("--lumo-header-font-weight", "600");

        produtosEstoqueBaixoGrid.addColumn(produto -> "📦 " + produto.getNome())
                .setHeader("🏷️ Produto")
                .setFlexGrow(1);

        produtosEstoqueBaixoGrid.addColumn(new ComponentRenderer<>(this::criarIndicadorEstoque))
                .setHeader("📊 Estoque")
                .setFlexGrow(0)
                .setWidth("120px");

        return produtosEstoqueBaixoGrid;
    }

    private Grid<Venda> criarTabelaUltimasVendas() {
        ultimasVendasGrid = new Grid<>(Venda.class, false);
        ultimasVendasGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
        ultimasVendasGrid.setWidthFull();
        ultimasVendasGrid.setHeight("350px");
        ultimasVendasGrid.addClassName(LumoUtility.BorderRadius.MEDIUM);
        ultimasVendasGrid.addClassName("dashboard-grid");
        ultimasVendasGrid.addClassName("fade-in");

        // Estilo personalizado para headers
        ultimasVendasGrid.getStyle().set("--lumo-header-color", "var(--lumo-primary-text-color)");
        ultimasVendasGrid.getStyle().set("--lumo-header-font-weight", "600");

        ultimasVendasGrid.addColumn(venda -> "#" + venda.getId())
                .setHeader("🔢 Nº")
                .setFlexGrow(0)
                .setWidth("70px");

        ultimasVendasGrid.addColumn(venda -> "🕐 " + venda.getDataVenda().format(DateTimeFormatter.ofPattern("HH:mm")))
                .setHeader("⏰ Hora")
                .setFlexGrow(0)
                .setWidth("90px");

        ultimasVendasGrid.addColumn(venda -> "💰 " + currencyFormat.format(venda.getValorTotal()))
                .setHeader("💵 Total")
                .setFlexGrow(0)
                .setWidth("110px");

        ultimasVendasGrid.addColumn(new ComponentRenderer<>(this::criarStatusBadge))
                .setHeader("📋 Status")
                .setFlexGrow(0)
                .setWidth("100px");

        return ultimasVendasGrid;
    }

    private com.vaadin.flow.component.Component criarIndicadorEstoque(Produto produto) {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setSpacing(false);
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        layout.setPadding(false);

        Span estoqueSpan = new Span(produto.getEstoqueAtual().toString() + " un");
        estoqueSpan.addClassName(LumoUtility.FontWeight.MEDIUM);
        estoqueSpan.addClassName(LumoUtility.FontSize.SMALL);

        Icon icon;
        if (produto.getEstoqueAtual() == 0) {
            estoqueSpan.getElement().getThemeList().add("badge error");
            icon = new Icon(VaadinIcon.CLOSE_CIRCLE);
            icon.getStyle().set("color", "var(--lumo-error-color)");
            estoqueSpan.getStyle().set("color", "var(--lumo-error-color)");
            estoqueSpan.getStyle().set("font-weight", "bold");
        } else if (produto.isEstoqueBaixo()) {
            estoqueSpan.getElement().getThemeList().add("badge contrast");
            icon = new Icon(VaadinIcon.WARNING);
            icon.getStyle().set("color", "var(--lumo-warning-color)");
            estoqueSpan.getStyle().set("color", "var(--lumo-warning-color)");
        } else {
            estoqueSpan.getElement().getThemeList().add("badge success");
            icon = new Icon(VaadinIcon.CHECK_CIRCLE);
            icon.getStyle().set("color", "var(--lumo-success-color)");
            estoqueSpan.getStyle().set("color", "var(--lumo-success-color)");
        }

        icon.setSize("16px");
        icon.addClassName(LumoUtility.Margin.Right.XSMALL);

        layout.add(icon, estoqueSpan);
        return layout;
    }

    private Span criarStatusBadge(Venda venda) {
        Span badge = new Span();
        badge.getElement().getThemeList().add("badge");
        badge.addClassName(LumoUtility.FontWeight.MEDIUM);
        badge.addClassName(LumoUtility.FontSize.SMALL);
        badge.addClassName("custom-badge");
        badge.getStyle().set("border-radius", "12px");
        badge.getStyle().set("padding", "4px 8px");

        switch (venda.getStatus()) {
            case FINALIZADA:
                badge.setText("✅ " + venda.getStatus().getDescricao());
                badge.getElement().getThemeList().add("success");
                badge.getStyle().set("background", "var(--lumo-success-color-10pct)");
                badge.getStyle().set("color", "var(--lumo-success-color)");
                break;
            case CANCELADA:
                badge.setText("❌ " + venda.getStatus().getDescricao());
                badge.getElement().getThemeList().add("error");
                badge.getStyle().set("background", "var(--lumo-error-color-10pct)");
                badge.getStyle().set("color", "var(--lumo-error-color)");
                break;
            case PENDENTE:
                badge.setText("⏳ " + venda.getStatus().getDescricao());
                badge.getElement().getThemeList().add("contrast");
                badge.getStyle().set("background", "var(--lumo-contrast-10pct)");
                badge.getStyle().set("color", "var(--lumo-contrast-70pct)");
                break;
        }

        return badge;
    }

    private void carregarEstatisticasTextuais() {
        carregarEstatisticasVendas30Dias();
        carregarEstatisticasFormasPagamento();
    }

    private void carregarEstatisticasVendas() {
        try {
            // Vendas de hoje
            BigDecimal totalHoje = vendaService.calcularTotalVendasHoje();
            long quantidadeHoje = vendaService.contarVendasHoje();

            totalVendasHojeLabel.setText(currencyFormat.format(totalHoje));
            quantidadeVendasHojeLabel.setText(quantidadeHoje + " vendas realizadas");

            // Vendas do mês
            BigDecimal totalMes = vendaService.calcularTotalVendasMes();
            long quantidadeMes = vendaService.contarVendasPorStatus(Venda.StatusVenda.FINALIZADA);

            totalVendasMesLabel.setText(currencyFormat.format(totalMes));
            quantidadeVendasMesLabel.setText(quantidadeMes + " vendas no período");

            // Ticket médio
            BigDecimal ticketMedio = quantidadeMes > 0
                    ? totalMes.divide(BigDecimal.valueOf(quantidadeMes), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            ticketMedioLabel.setText(currencyFormat.format(ticketMedio));

        } catch (Exception e) {
            totalVendasHojeLabel.setText("R$ 0,00");
            quantidadeVendasHojeLabel.setText("0 vendas realizadas");
            totalVendasMesLabel.setText("R$ 0,00");
            quantidadeVendasMesLabel.setText("0 vendas no período");
            ticketMedioLabel.setText("R$ 0,00");
        }
    }

    private void carregarEstatisticasProdutos() {
        try {
            long totalProdutos = produtoService.contarProdutosAtivos();
            totalProdutosLabel.setText(String.valueOf(totalProdutos));

        } catch (Exception e) {
            totalProdutosLabel.setText("0");
        }
    }

    private void carregarEstatisticasVendas30Dias() {
        try {
            LocalDateTime dataInicio = LocalDate.now().minusDays(30).atStartOfDay();
            LocalDateTime dataFim = LocalDate.now().atTime(23, 59, 59);

            List<Object[]> dadosVendas = vendaService.obterEstatisticasPorDia(dataInicio, dataFim);

            estatisticasVendas30Dias.removeAll();

            if (dadosVendas == null || dadosVendas.isEmpty()) {
                VerticalLayout semDados = new VerticalLayout();
                semDados.setAlignItems(FlexComponent.Alignment.CENTER);
                semDados.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
                semDados.setHeight("200px");

                Icon icon = new Icon(VaadinIcon.INFO_CIRCLE);
                icon.setSize("48px");
                icon.getStyle().set("color", "var(--lumo-contrast-50pct)");

                Span texto = new Span("Nenhuma venda encontrada no período");
                texto.addClassName(LumoUtility.FontSize.MEDIUM);
                texto.getStyle().set("color", "var(--lumo-contrast-70pct)");
                texto.addClassName(LumoUtility.Margin.Top.MEDIUM);

                semDados.add(icon, texto);
                estatisticasVendas30Dias.removeAll();
                estatisticasVendas30Dias.add(semDados);
                return;
            }

            BigDecimal totalPeriodo = BigDecimal.ZERO;
            long totalVendas = 0;

            for (Object[] dados : dadosVendas) {
                if (dados != null && dados.length >= 3) {
                    try {
                        // dados[0] = data (LocalDate ou Date)
                        // dados[1] = count (Long)
                        // dados[2] = sum (BigDecimal)

                        Object countObj = dados[1];
                        Object sumObj = dados[2];

                        if (countObj instanceof Number) {
                            totalVendas += ((Number) countObj).longValue();
                        }

                        if (sumObj instanceof BigDecimal) {
                            totalPeriodo = totalPeriodo.add((BigDecimal) sumObj);
                        } else if (sumObj instanceof Number) {
                            totalPeriodo = totalPeriodo.add(BigDecimal.valueOf(((Number) sumObj).doubleValue()));
                        }
                    } catch (Exception e) {
                        System.err.println("Erro ao processar dados de venda: " + e.getMessage());
                        // Continua com o próximo registro
                    }
                }
            }

            // Estatísticas resumidas com design melhorado
            VerticalLayout resumoContainer = new VerticalLayout();
            resumoContainer.setPadding(false);
            resumoContainer.addClassName(LumoUtility.BorderRadius.MEDIUM);
            resumoContainer.addClassName(LumoUtility.Padding.MEDIUM);
            resumoContainer.addClassName(LumoUtility.Margin.Bottom.MEDIUM);
            resumoContainer.getStyle().set("background", "rgba(0,0,0,0.05)");
            resumoContainer.getStyle().set("border", "1px solid rgba(0,0,0,0.1)");

            HorizontalLayout resumo = new HorizontalLayout();
            resumo.setWidthFull();
            resumo.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

            VerticalLayout totalLayout = new VerticalLayout();
            totalLayout.setPadding(false);
            totalLayout.setSpacing(false);
            totalLayout.setAlignItems(FlexComponent.Alignment.CENTER);

            Span totalLabel = new Span("💰 Total do Período");
            totalLabel.addClassName(LumoUtility.FontSize.SMALL);
            totalLabel.getStyle().set("color", "var(--lumo-contrast-70pct)");

            Span totalValor = new Span(currencyFormat.format(totalPeriodo));
            totalValor.addClassName(LumoUtility.FontSize.XLARGE);
            totalValor.addClassName(LumoUtility.FontWeight.BOLD);
            totalValor.getStyle().set("color", "var(--lumo-contrast-90pct)");

            totalLayout.add(totalLabel, totalValor);

            VerticalLayout vendasLayout = new VerticalLayout();
            vendasLayout.setPadding(false);
            vendasLayout.setSpacing(false);
            vendasLayout.setAlignItems(FlexComponent.Alignment.CENTER);

            Span vendasLabel = new Span("🛒 Total de Vendas");
            vendasLabel.addClassName(LumoUtility.FontSize.SMALL);
            vendasLabel.getStyle().set("color", "var(--lumo-contrast-70pct)");

            Span vendasValor = new Span(String.valueOf(totalVendas));
            vendasValor.addClassName(LumoUtility.FontSize.XLARGE);
            vendasValor.addClassName(LumoUtility.FontWeight.BOLD);
            vendasValor.getStyle().set("color", "var(--lumo-contrast-90pct)");

            vendasLayout.add(vendasLabel, vendasValor);

            resumo.add(totalLayout, vendasLayout);
            resumoContainer.add(resumo);
            estatisticasVendas30Dias.add(resumoContainer);

            // Últimos 7 dias detalhados com design melhorado
            H3 titulo = new H3("📅 Últimos 7 dias");
            titulo.addClassName(LumoUtility.FontSize.MEDIUM);
            titulo.addClassName(LumoUtility.Margin.Top.MEDIUM);
            titulo.addClassName(LumoUtility.Margin.Bottom.SMALL);
            titulo.getStyle().set("color", "var(--lumo-contrast-90pct)");
            estatisticasVendas30Dias.add(titulo);

            int limite = Math.min(7, dadosVendas.size());
            for (int i = Math.max(0, dadosVendas.size() - limite); i < dadosVendas.size(); i++) {
                Object[] dados = dadosVendas.get(i);
                if (dados != null && dados.length >= 3) {
                    try {
                        Object dataObj = dados[0];
                        Object quantidadeObj = dados[1];
                        Object valorObj = dados[2];

                        LocalDate data;
                        if (dataObj instanceof LocalDate) {
                            data = (LocalDate) dataObj;
                        } else if (dataObj instanceof java.sql.Date) {
                            data = ((java.sql.Date) dataObj).toLocalDate();
                        } else {
                            continue; // Pula se não conseguir converter a data
                        }

                        long quantidade = quantidadeObj instanceof Number ? ((Number) quantidadeObj).longValue() : 0;

                        BigDecimal valor;
                        if (valorObj instanceof BigDecimal) {
                            valor = (BigDecimal) valorObj;
                        } else if (valorObj instanceof Number) {
                            valor = BigDecimal.valueOf(((Number) valorObj).doubleValue());
                        } else {
                            valor = BigDecimal.ZERO;
                        }

                        HorizontalLayout linha = new HorizontalLayout();
                        linha.setWidthFull();
                        linha.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
                        linha.setAlignItems(FlexComponent.Alignment.CENTER);
                        linha.addClassName(LumoUtility.Padding.SMALL);
                        linha.addClassName(LumoUtility.BorderRadius.SMALL);
                        linha.getStyle().set("background", "rgba(0,0,0,0.02)");
                        linha.getStyle().set("border", "1px solid rgba(0,0,0,0.05)");
                        linha.addClassName(LumoUtility.Margin.Bottom.XSMALL);

                        HorizontalLayout dataContainer = new HorizontalLayout();
                        dataContainer.setSpacing(true);
                        dataContainer.setAlignItems(FlexComponent.Alignment.CENTER);

                        Icon calendario = new Icon(VaadinIcon.CALENDAR);
                        calendario.setSize("14px");
                        calendario.getStyle().set("color", "var(--lumo-contrast-60pct)");

                        Span dataSpan = new Span(data.format(DateTimeFormatter.ofPattern("dd/MM")));
                        dataSpan.addClassName(LumoUtility.FontSize.SMALL);
                        dataSpan.addClassName(LumoUtility.FontWeight.MEDIUM);
                        dataSpan.getStyle().set("color", "var(--lumo-contrast-90pct)");

                        dataContainer.add(calendario, dataSpan);

                        Span infoSpan = new Span(quantidade + " vendas • " + currencyFormat.format(valor));
                        infoSpan.addClassName(LumoUtility.FontSize.SMALL);
                        infoSpan.getStyle().set("color", "var(--lumo-contrast-70pct)");

                        linha.add(dataContainer, infoSpan);
                        estatisticasVendas30Dias.add(linha);
                    } catch (Exception e) {
                        System.err.println("Erro ao processar dados detalhados de venda: " + e.getMessage());
                        // Continua com o próximo registro
                    }
                }
            }

        } catch (Exception e) {
            estatisticasVendas30Dias.removeAll();
            Span erro = new Span("Erro ao carregar dados de vendas: " + e.getMessage());
            erro.addClassName(LumoUtility.TextColor.ERROR);
            estatisticasVendas30Dias.add(erro);
            System.err.println("Erro ao carregar estatísticas de vendas dos últimos 30 dias: " + e.getMessage());
            e.printStackTrace(); // Adicionado para debug
        }
    }

    private void carregarEstatisticasFormasPagamento() {
        try {
            LocalDateTime dataInicio = LocalDate.now().withDayOfMonth(1).atStartOfDay();
            LocalDateTime dataFim = LocalDate.now().atTime(23, 59, 59);

            List<Object[]> dadosFormasPagamento = vendaService.obterEstatisticasPorFormaPagamento(dataInicio, dataFim);

            estatisticasFormasPagamento.removeAll();

            if (dadosFormasPagamento == null || dadosFormasPagamento.isEmpty()) {
                VerticalLayout semDados = new VerticalLayout();
                semDados.setAlignItems(FlexComponent.Alignment.CENTER);
                semDados.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
                semDados.setHeight("200px");

                Icon icon = new Icon(VaadinIcon.CREDIT_CARD);
                icon.setSize("48px");
                icon.getStyle().set("color", "var(--lumo-contrast-50pct)");

                Span texto = new Span("Nenhuma venda encontrada no mês");
                texto.addClassName(LumoUtility.FontSize.MEDIUM);
                texto.getStyle().set("color", "var(--lumo-contrast-70pct)");
                texto.addClassName(LumoUtility.Margin.Top.MEDIUM);

                semDados.add(icon, texto);
                estatisticasFormasPagamento.removeAll();
                estatisticasFormasPagamento.add(semDados);
                return;
            }

            // Calcula total geral
            BigDecimal totalGeral = BigDecimal.ZERO;
            for (Object[] dados : dadosFormasPagamento) {
                if (dados != null && dados.length >= 3 && dados[2] != null) {
                    try {
                        Object valorObj = dados[2];
                        if (valorObj instanceof BigDecimal) {
                            totalGeral = totalGeral.add((BigDecimal) valorObj);
                        } else if (valorObj instanceof Number) {
                            totalGeral = totalGeral.add(BigDecimal.valueOf(((Number) valorObj).doubleValue()));
                        }
                    } catch (Exception e) {
                        System.err.println("Erro ao calcular total geral: " + e.getMessage());
                    }
                }
            }

            for (Object[] dados : dadosFormasPagamento) {
                if (dados != null && dados.length >= 3) {
                    try {
                        // dados[0] = FormaPagamento
                        // dados[1] = count (Long)
                        // dados[2] = sum (BigDecimal)

                        Object formaObj = dados[0];
                        Object quantidadeObj = dados[1];
                        Object valorObj = dados[2];

                        if (!(formaObj instanceof Venda.FormaPagamento)) {
                            continue; // Pula se não for uma forma de pagamento válida
                        }

                        Venda.FormaPagamento forma = (Venda.FormaPagamento) formaObj;

                        long quantidade = quantidadeObj instanceof Number ? ((Number) quantidadeObj).longValue() : 0;

                        BigDecimal valor;
                        if (valorObj instanceof BigDecimal) {
                            valor = (BigDecimal) valorObj;
                        } else if (valorObj instanceof Number) {
                            valor = BigDecimal.valueOf(((Number) valorObj).doubleValue());
                        } else {
                            valor = BigDecimal.ZERO;
                        }

                        BigDecimal percentual = totalGeral.compareTo(BigDecimal.ZERO) > 0
                                ? valor.divide(totalGeral, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                                : BigDecimal.ZERO;

                        VerticalLayout formaLayout = new VerticalLayout();
                        formaLayout.setPadding(false);
                        formaLayout.setSpacing(false);
                        formaLayout.addClassName(LumoUtility.Margin.Bottom.MEDIUM);
                        formaLayout.addClassName(LumoUtility.Padding.MEDIUM);
                        formaLayout.addClassName(LumoUtility.BorderRadius.MEDIUM);
                        formaLayout.getStyle().set("background", "rgba(0,0,0,0.03)");
                        formaLayout.getStyle().set("border", "1px solid rgba(0,0,0,0.08)");
                        formaLayout.getStyle().set("transition", "background 0.3s ease");

                        // Efeito hover
                        formaLayout.getElement().addEventListener("mouseenter", e -> {
                            formaLayout.getStyle().set("background", "rgba(0,0,0,0.06)");
                        });

                        formaLayout.getElement().addEventListener("mouseleave", e -> {
                            formaLayout.getStyle().set("background", "rgba(0,0,0,0.03)");
                        });

                        HorizontalLayout header = new HorizontalLayout();
                        header.setWidthFull();
                        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
                        header.setAlignItems(FlexComponent.Alignment.CENTER);

                        HorizontalLayout nomeContainer = new HorizontalLayout();
                        nomeContainer.setSpacing(true);
                        nomeContainer.setAlignItems(FlexComponent.Alignment.CENTER);

                        // Ícone para cada forma de pagamento
                        Icon iconeForma;
                        switch (forma) {
                            case DINHEIRO:
                                iconeForma = new Icon(VaadinIcon.MONEY);
                                break;
                            case CARTAO_CREDITO:
                                iconeForma = new Icon(VaadinIcon.CREDIT_CARD);
                                break;
                            case CARTAO_DEBITO:
                                iconeForma = new Icon(VaadinIcon.CREDIT_CARD);
                                break;
                            case PIX:
                                iconeForma = new Icon(VaadinIcon.MOBILE);
                                break;
                            default:
                                iconeForma = new Icon(VaadinIcon.WALLET);
                        }
                        iconeForma.setSize("16px");
                        iconeForma.getStyle().set("color", "var(--lumo-contrast-70pct)");

                        Span nomeSpan = new Span(forma.getDescricao());
                        nomeSpan.addClassName(LumoUtility.FontWeight.BOLD);
                        nomeSpan.getStyle().set("color", "var(--lumo-contrast-90pct)");

                        nomeContainer.add(iconeForma, nomeSpan);

                        Span percentualSpan = new Span(String.format("%.1f%%", percentual));
                        percentualSpan.addClassName(LumoUtility.FontSize.SMALL);
                        percentualSpan.addClassName(LumoUtility.FontWeight.MEDIUM);
                        percentualSpan.getStyle().set("color", "var(--lumo-contrast-80pct)");
                        percentualSpan.addClassName(LumoUtility.Padding.Horizontal.SMALL);
                        percentualSpan.addClassName(LumoUtility.BorderRadius.SMALL);
                        percentualSpan.getStyle().set("background", "rgba(0,0,0,0.08)");

                        header.add(nomeContainer, percentualSpan);

                        HorizontalLayout info = new HorizontalLayout();
                        info.setSpacing(true);
                        info.setAlignItems(FlexComponent.Alignment.CENTER);

                        Span valorSpan = new Span("💰 " + currencyFormat.format(valor));
                        valorSpan.addClassName(LumoUtility.FontSize.MEDIUM);
                        valorSpan.addClassName(LumoUtility.FontWeight.MEDIUM);
                        valorSpan.getStyle().set("color", "var(--lumo-contrast-90pct)");

                        Span quantidadeSpan = new Span("🛒 " + quantidade + " vendas");
                        quantidadeSpan.addClassName(LumoUtility.FontSize.SMALL);
                        quantidadeSpan.getStyle().set("color", "var(--lumo-contrast-70pct)");

                        info.add(valorSpan, quantidadeSpan);

                        formaLayout.add(header, info);
                        estatisticasFormasPagamento.add(formaLayout);
                    } catch (Exception e) {
                        System.err.println("Erro ao processar dados de forma de pagamento: " + e.getMessage());
                        // Continua com o próximo registro
                    }
                }
            }

        } catch (Exception e) {
            estatisticasFormasPagamento.removeAll();
            Span erro = new Span("Erro ao carregar dados de formas de pagamento: " + e.getMessage());
            erro.addClassName(LumoUtility.TextColor.ERROR);
            estatisticasFormasPagamento.add(erro);
            System.err.println("Erro ao carregar estatísticas de formas de pagamento: " + e.getMessage());
            e.printStackTrace(); // Adicionado para debug
        }
    }

    private void carregarTabelas() {
        carregarTabelaProdutosMaisVendidos();
        carregarTabelaEstoqueBaixo();
        carregarTabelaUltimasVendas();
    }

    private void carregarTabelaProdutosMaisVendidos() {
        try {
            List<Object[]> produtosMaisVendidos = vendaService.buscarProdutosMaisVendidos(10);
            produtosMaisVendidosGrid.setItems(produtosMaisVendidos);
        } catch (Exception e) {
            System.err.println("Erro ao carregar produtos mais vendidos: " + e.getMessage());
        }
    }

    private void carregarTabelaEstoqueBaixo() {
        try {
            List<Produto> produtosEstoqueBaixo = produtoService.listarProdutosComEstoqueBaixo();
            produtosEstoqueBaixoGrid.setItems(produtosEstoqueBaixo.stream().limit(10).toList());
        } catch (Exception e) {
            System.err.println("Erro ao carregar produtos com estoque baixo: " + e.getMessage());
        }
    }

    private void carregarTabelaUltimasVendas() {
        try {
            org.springframework.data.domain.Slice<Venda> ultimasVendas = vendaService.listarVendasPorStatus(
                    Venda.StatusVenda.FINALIZADA,
                    org.springframework.data.domain.PageRequest.of(0, 10));
            ultimasVendasGrid.setItems(ultimasVendas.getContent());
        } catch (Exception e) {
            System.err.println("Erro ao carregar últimas vendas: " + e.getMessage());
        }
    }
}
