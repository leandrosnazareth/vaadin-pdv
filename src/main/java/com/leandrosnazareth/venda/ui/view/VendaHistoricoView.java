package com.leandrosnazareth.venda.ui.view;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.springframework.data.domain.PageRequest;

import com.leandrosnazareth.base.ui.component.ViewToolbar;
import com.leandrosnazareth.venda.domain.Venda;
import com.leandrosnazareth.venda.service.VendaService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.NumberRenderer;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;

import com.vaadin.flow.component.dependency.CssImport;

import jakarta.annotation.security.PermitAll;

@PageTitle("Histórico de Vendas")
@Route(value = "vendas", layout = com.leandrosnazareth.base.ui.view.MainLayout.class)
@Menu(order = 2, icon = "vaadin:invoice", title = "Vendas")
@CssImport("./styles/vendas.css")
@PermitAll
public class VendaHistoricoView extends Main {

    private final VendaService vendaService;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // Componentes
    private Grid<Venda> grid;
    private ComboBox<Venda.StatusVenda> statusCombo;
    private DatePicker dataInicio;
    private DatePicker dataFim;
    private Button buscarButton;
    private Button limparButton;
    private Span totalLabel;

    public VendaHistoricoView(VendaService vendaService) {
        this.vendaService = vendaService;
        
        configurarComponentes();
        criarLayout();
        configurarEventos();
        carregarDados();
    }

    private void configurarComponentes() {
        // Status
        statusCombo = new ComboBox<>("Status");
        statusCombo.setItems(Venda.StatusVenda.values());
        statusCombo.setItemLabelGenerator(Venda.StatusVenda::getDescricao);
        statusCombo.setClearButtonVisible(true);
        statusCombo.setPlaceholder("Todos");
        statusCombo.setWidth("150px");

        // Data início
        dataInicio = new DatePicker("Data Início");
        dataInicio.setPlaceholder("dd/MM/yyyy");
        dataInicio.setWidth("150px");

        // Data fim
        dataFim = new DatePicker("Data Fim");
        dataFim.setPlaceholder("dd/MM/yyyy");
        dataFim.setWidth("150px");

        // Botões
        buscarButton = new Button("Buscar", new Icon(VaadinIcon.SEARCH));
        buscarButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        limparButton = new Button("Limpar", new Icon(VaadinIcon.REFRESH));
        limparButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        // Grid
        grid = criarGrid();
        
        // Total
        totalLabel = new Span("Total: R$ 0,00");
        totalLabel.addClassName(LumoUtility.FontWeight.BOLD);
        totalLabel.addClassName(LumoUtility.TextColor.PRIMARY);
    }

    private void criarLayout() {
        // Layout de filtros
        HorizontalLayout filtrosLayout = new HorizontalLayout();
        filtrosLayout.addClassName("vendas-filtros");
        filtrosLayout.setWidthFull();
        filtrosLayout.setAlignItems(FlexComponent.Alignment.END);
        filtrosLayout.add(statusCombo, dataInicio, dataFim, buscarButton, limparButton);
        filtrosLayout.setFlexGrow(1, statusCombo);

        // Toolbar com filtros
        ViewToolbar toolbar = new ViewToolbar("Histórico de Vendas", 
            ViewToolbar.group(filtrosLayout));
        toolbar.addClassName("vendas-toolbar");

        // Rodapé com total
        HorizontalLayout rodape = new HorizontalLayout(totalLabel);
        rodape.addClassName("total-vendas");
        rodape.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        rodape.addClassName(LumoUtility.Padding.Top.MEDIUM);

        // Layout principal
        addClassNames(LumoUtility.BoxSizing.BORDER, LumoUtility.Display.FLEX, 
                     LumoUtility.FlexDirection.COLUMN, LumoUtility.Padding.MEDIUM, 
                     LumoUtility.Gap.SMALL);
        
        add(toolbar, grid, rodape);
        setSizeFull();
    }

    private Grid<Venda> criarGrid() {
        Grid<Venda> grid = new Grid<>(Venda.class, false);
        grid.addClassName("vendas-grid");
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
        grid.setHeightFull();
        grid.setMinHeight("500px");
        grid.setPageSize(20);

        // Colunas
        grid.addColumn(venda -> "#" + venda.getId())
            .setHeader("Número")
            .setWidth("100px")
            .setFlexGrow(0)
            .setSortable(true);

        grid.addColumn(venda -> venda.getDataVenda().format(dateFormatter))
            .setHeader("Data/Hora")
            .setWidth("150px")
            .setFlexGrow(0)
            .setSortable(true);

        grid.addColumn(new ComponentRenderer<>(this::criarStatusBadge))
            .setHeader("Status")
            .setWidth("120px")
            .setFlexGrow(0)
            .setSortable(true);

        grid.addColumn(venda -> venda.getFormaPagamento().getDescricao())
            .setHeader("Pagamento")
            .setWidth("120px")
            .setFlexGrow(0)
            .setSortable(true);

        grid.addColumn(Venda::getQuantidadeItens)
            .setHeader("Itens")
            .setWidth("80px")
            .setFlexGrow(0)
            .setSortable(true);

        grid.addColumn(new NumberRenderer<>(Venda::getValorTotal, currencyFormat))
            .setHeader("Total")
            .setWidth("120px")
            .setFlexGrow(0)
            .setSortable(true);

        grid.addColumn(venda -> venda.getObservacoes() != null ? venda.getObservacoes() : "")
            .setHeader("Observações")
            .setFlexGrow(1);

        return grid;
    }

    private Span criarStatusBadge(Venda venda) {
        Span badge = new Span(venda.getStatus().getDescricao());
        badge.addClassName("status-badge");
        
        switch (venda.getStatus()) {
            case FINALIZADA:
                badge.addClassName("finalizada");
                break;
            case CANCELADA:
                badge.addClassName("cancelada");
                break;
            case PENDENTE:
                badge.addClassName("pendente");
                break;
        }
        
        return badge;
    }

    private void configurarEventos() {
        buscarButton.addClickListener(e -> carregarDados());
        limparButton.addClickListener(e -> limparFiltros());
    }

    private void carregarDados() {
        try {
            var vendas = vendaService.listarVendas(PageRequest.of(0, 100));
            grid.setItems(vendas.getContent());
            
            // Calcula total
            BigDecimal total = vendas.getContent().stream()
                .filter(v -> v.getStatus() == Venda.StatusVenda.FINALIZADA)
                .map(Venda::getValorTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            totalLabel.setText("Total: " + currencyFormat.format(total));
        } catch (Exception e) {
            grid.setItems();
            totalLabel.setText("Total: R$ 0,00");
        }
    }

    private void limparFiltros() {
        statusCombo.clear();
        dataInicio.clear();
        dataFim.clear();
        carregarDados();
    }
}
