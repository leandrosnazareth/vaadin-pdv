package com.leandrosnazareth.produto.ui.view;

import com.leandrosnazareth.base.ui.component.ViewToolbar;
import com.leandrosnazareth.produto.domain.Produto;
import com.leandrosnazareth.produto.service.ProdutoService;
import com.leandrosnazareth.produto.ui.component.ProdutoForm;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

import static com.vaadin.flow.spring.data.VaadinSpringDataHelpers.toSpringPageRequest;

/**
 * View principal para gerenciamento de produtos.
 * <p>
 * Esta view fornece uma interface completa para visualizar, criar,
 * editar e excluir produtos, incluindo funcionalidades de busca
 * e filtragem.
 * </p>
 */
@Route("produtos")
@PageTitle("Produtos")
@Menu(order = 1, icon = "vaadin:package", title = "Produtos")
@PermitAll
public class ProdutoListView extends Main {

    private static final Logger logger = LoggerFactory.getLogger(ProdutoListView.class);
    
    private final ProdutoService produtoService;
    private final NumberFormat currencyFormat;
    
    // Componentes de busca e filtros
    private final TextField buscaField;
    private final ComboBox<String> categoriaFilter;
    private final ComboBox<String> statusFilter;
    private final Button novoProdutoButton;
    private final Button atualizarButton;
    
    // Grid de produtos
    private final Grid<Produto> produtoGrid;
    
    // Formulário
    private ProdutoForm produtoForm;

    /**
     * Construtor da view de produtos.
     * 
     * @param produtoService serviço para operações de produto
     */
    public ProdutoListView(ProdutoService produtoService) {
        this.produtoService = produtoService;
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        
        // Inicializar componentes
        buscaField = new TextField();
        categoriaFilter = new ComboBox<>();
        statusFilter = new ComboBox<>();
        novoProdutoButton = new Button("Novo Produto");
        atualizarButton = new Button();
        produtoGrid = new Grid<>(Produto.class, false);
        
        configurarComponentes();
        configurarGrid();
        criarLayout();
        carregarDados();
        
        // Garantir que o grid seja inicializado corretamente
        inicializarGrid();
    }
    
    /**
     * Inicializa o grid com configurações adicionais para evitar problemas.
     */
    private void inicializarGrid() {
        try {
            // Configurar como não editável
            produtoGrid.setDetailsVisibleOnClick(false);
            
            // Configurar refresh automático
            produtoGrid.getDataProvider().refreshAll();
            
            logger.info("Grid inicializado com sucesso");
        } catch (Exception e) {
            logger.error("Erro ao inicializar grid", e);
        }
    }
    
    /**
     * Configura os componentes da view.
     */
    private void configurarComponentes() {
        // Campo de busca
        buscaField.setPlaceholder("Buscar produtos...");
        buscaField.setClearButtonVisible(true);
        buscaField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        buscaField.setValueChangeMode(ValueChangeMode.LAZY);
        buscaField.addValueChangeListener(e -> filtrarProdutos());
        buscaField.setWidthFull();
        
        // Filtro por categoria
        categoriaFilter.setPlaceholder("Todas as categorias");
        categoriaFilter.setClearButtonVisible(true);
        categoriaFilter.addValueChangeListener(e -> filtrarProdutos());
        categoriaFilter.setWidth("200px");
        
        // Filtro por status
        statusFilter.setPlaceholder("Todos os status");
        statusFilter.setItems("Ativo", "Inativo", "Estoque Baixo", "Sem Estoque");
        statusFilter.setClearButtonVisible(true);
        statusFilter.addValueChangeListener(e -> filtrarProdutos());
        statusFilter.setWidth("150px");
        
        // Botão novo produto
        novoProdutoButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        novoProdutoButton.setIcon(new Icon(VaadinIcon.PLUS));
        novoProdutoButton.addClickListener(e -> abrirFormularioNovo());
        
        // Botão atualizar
        atualizarButton.setIcon(new Icon(VaadinIcon.REFRESH));
        atualizarButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        atualizarButton.addClickListener(e -> atualizarDados());
        atualizarButton.setTooltipText("Atualizar lista");
    }
    
    /**
     * Configura o grid de produtos.
     */
    private void configurarGrid() {
        produtoGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        produtoGrid.setMultiSort(true);
        produtoGrid.setPageSize(20);
        
        // Colunas básicas
        produtoGrid.addColumn(Produto::getCodigo)
                .setHeader("Código")
                .setSortable(true)
                .setFlexGrow(0)
                .setWidth("120px");
        
        // Coluna de foto
        produtoGrid.addColumn(new ComponentRenderer<>(this::criarFotoProduto))
                .setHeader("Foto")
                .setFlexGrow(0)
                .setWidth("80px");
        
        produtoGrid.addColumn(Produto::getNome)
                .setHeader("Nome")
                .setSortable(true)
                .setFlexGrow(1);
        
        produtoGrid.addColumn(Produto::getCategoria)
                .setHeader("Categoria")
                .setSortable(true)
                .setFlexGrow(0)
                .setWidth("150px");
        
        // Coluna de preço formatado
        produtoGrid.addColumn(produto -> formatarMoeda(produto.getPrecoVenda()))
                .setHeader("Preço")
                .setSortable(true)
                .setFlexGrow(0)
                .setWidth("120px");
        
        // Coluna de estoque com indicador visual
        produtoGrid.addColumn(new ComponentRenderer<>(this::criarIndicadorEstoque))
                .setHeader("Estoque")
                .setSortable(true)
                .setFlexGrow(0)
                .setWidth("120px");
        
        // Coluna de status
        produtoGrid.addColumn(new ComponentRenderer<>(this::criarIndicadorStatus))
                .setHeader("Status")
                .setFlexGrow(0)
                .setWidth("100px");
        
        // Coluna de ações
        produtoGrid.addColumn(new ComponentRenderer<>(this::criarBotoesAcao))
                .setHeader("Ações")
                .setFlexGrow(0)
                .setWidth("120px");
        
        // Configurar fonte de dados
        produtoGrid.setItems(query -> produtoService.listarProdutos(toSpringPageRequest(query)).stream());
        
        // Configurar seleção
        produtoGrid.addItemDoubleClickListener(e -> editarProduto(e.getItem()));
        
        produtoGrid.setSizeFull();
    }
    
    /**
     * Cria o layout principal da view.
     */
    private void criarLayout() {
        // Layout de filtros
        HorizontalLayout filtrosLayout = new HorizontalLayout();
        filtrosLayout.setWidthFull();
        filtrosLayout.setAlignItems(FlexComponent.Alignment.END);
        filtrosLayout.add(buscaField, categoriaFilter, statusFilter, atualizarButton);
        filtrosLayout.setFlexGrow(1, buscaField);
        
        // Toolbar
        ViewToolbar toolbar = new ViewToolbar("Produtos", ViewToolbar.group(filtrosLayout, novoProdutoButton));
        
        // Layout principal
        addClassNames(LumoUtility.BoxSizing.BORDER, LumoUtility.Display.FLEX, 
                     LumoUtility.FlexDirection.COLUMN, LumoUtility.Padding.MEDIUM, 
                     LumoUtility.Gap.SMALL);
        
        add(toolbar, produtoGrid);
        setSizeFull();
    }
    
    /**
     * Carrega os dados iniciais.
     */
    private void carregarDados() {
        try {
            // Carregar categorias para o filtro
            categoriaFilter.setItems(produtoService.listarCategorias());
            
            // Atualizar estatísticas na toolbar se necessário
            atualizarEstatisticas();
            
        } catch (Exception e) {
            logger.error("Erro ao carregar dados iniciais", e);
            mostrarNotificacaoErro("Erro ao carregar dados");
        }
    }
    
    /**
     * Atualiza os dados da grid.
     */
    private void atualizarDados() {
        try {
            // Usar getUI() para garantir que a atualização seja feita no thread correto
            getUI().ifPresent(ui -> ui.access(() -> {
                // Validar estado do grid antes de atualizar
                validarEstadoGrid();
                
                // Limpar seleção antes de atualizar
                produtoGrid.deselectAll();
                
                // Forçar refresh do data provider
                produtoGrid.getDataProvider().refreshAll();
                
                // Recarregar dados auxiliares
                carregarDados();
                
                // Mostrar notificação apenas se não estiver em modo silencioso
                if (!isSilentUpdate) {
                    mostrarNotificacaoSucesso("Dados atualizados");
                }
            }));
        } catch (Exception e) {
            logger.error("Erro ao atualizar dados", e);
            mostrarNotificacaoErro("Erro ao atualizar dados");
            // Em caso de erro, tentar reconfigurar o grid
            reconfigurarGrid();
        }
    }
    
    // Flag para controlar notificações durante atualizações automáticas
    private boolean isSilentUpdate = false;
    
    /**
     * Atualiza os dados silenciosamente (sem notificação).
     */
    private void atualizarDadosSilencioso() {
        isSilentUpdate = true;
        atualizarDados();
        isSilentUpdate = false;
    }
    
    /**
     * Reconfigura o grid para resolver problemas de estado.
     */
    private void reconfigurarGrid() {
        try {
            getUI().ifPresent(ui -> ui.access(() -> {
                // Limpar seleção
                produtoGrid.deselectAll();
                
                // Reconfigurar data provider
                produtoGrid.setItems(query -> produtoService.listarProdutos(toSpringPageRequest(query)).stream());
                
                // Forçar refresh
                produtoGrid.getDataProvider().refreshAll();
                
                logger.info("Grid reconfigurado com sucesso");
            }));
        } catch (Exception e) {
            logger.error("Erro ao reconfigurar grid", e);
        }
    }
    
    /**
     * Valida e corrige o estado do grid se necessário.
     */
    private void validarEstadoGrid() {
        try {
            if (produtoGrid.getDataProvider() == null) {
                logger.warn("Data provider do grid está nulo, reconfigurando...");
                reconfigurarGrid();
            }
        } catch (Exception e) {
            logger.error("Erro ao validar estado do grid", e);
            reconfigurarGrid();
        }
    }

    /**
     * Aplica filtros aos produtos.
     */
    private void filtrarProdutos() {
        String busca = buscaField.getValue();
        String categoria = categoriaFilter.getValue();
        String status = statusFilter.getValue();
        
        if (busca != null && !busca.trim().isEmpty()) {
            produtoGrid.setItems(query -> 
                produtoService.buscarPorTermo(busca.trim(), toSpringPageRequest(query)).stream());
        } else if (categoria != null && !categoria.trim().isEmpty()) {
            produtoGrid.setItems(query -> 
                produtoService.buscarPorCategoria(categoria, toSpringPageRequest(query)).stream());
        } else if ("Ativo".equals(status)) {
            produtoGrid.setItems(query -> 
                produtoService.listarProdutosAtivos(toSpringPageRequest(query)).stream());
        } else {
            produtoGrid.setItems(query -> 
                produtoService.listarProdutos(toSpringPageRequest(query)).stream());
        }
        
        // Aplicar filtro adicional por status especial
        if ("Estoque Baixo".equals(status)) {
            produtoGrid.setItems(produtoService.listarProdutosComEstoqueBaixo());
        } else if ("Sem Estoque".equals(status)) {
            produtoGrid.setItems(produtoService.listarProdutosSemEstoque());
        }
    }
    
    /**
     * Abre o formulário para criar novo produto.
     */
    private void abrirFormularioNovo() {
        if (produtoForm == null) {
            produtoForm = new ProdutoForm(produtoService, this::onProdutoSalvo, this::onProdutoExcluido);
        }
        produtoForm.abrirParaNovo();
    }
    
    /**
     * Edita um produto existente.
     * 
     * @param produto o produto a ser editado
     */
    private void editarProduto(Produto produto) {
        try {
            if (produtoForm == null) {
                produtoForm = new ProdutoForm(produtoService, this::onProdutoSalvo, this::onProdutoExcluido);
            }
            produtoForm.abrirParaEdicao(produto);
        } catch (Exception e) {
            logger.error("Erro ao abrir formulário de edição", e);
            mostrarNotificacaoErro("Erro ao abrir formulário de edição");
            
            // Tentar reconfigurar o grid em caso de erro
            reconfigurarGrid();
        }
    }
    
    /**
     * Exclui um produto.
     * 
     * @param produto o produto a ser excluído
     */
    private void excluirProduto(Produto produto) {
        ConfirmDialog dialog = new ConfirmDialog(
            "Confirmar exclusão",
            "Tem certeza que deseja excluir o produto \"" + produto.getNome() + "\"?",
            "Excluir",
            e -> {
                try {
                    produtoService.excluirProduto(produto.getId());
                    mostrarNotificacaoSucesso("Produto excluído com sucesso");
                    reconfigurarGrid(); // Usar reconfiguração para evitar problemas
                } catch (Exception ex) {
                    logger.error("Erro ao excluir produto", ex);
                    mostrarNotificacaoErro("Erro ao excluir produto");
                    // Tentar reconfigurar mesmo em caso de erro
                    reconfigurarGrid();
                }
            },
            "Cancelar",
            e -> {
                // Não fazer nada
            }
        );
        dialog.open();
    }
    
    /**
     * Callback chamado quando um produto é salvo.
     * 
     * @param produto o produto salvo
     */
    private void onProdutoSalvo(Produto produto) {
        try {
            // Se o produto tem foto, usar reconfiguração para evitar problemas
            if (produto.getFoto() != null && produto.getFoto().length > 0) {
                reconfigurarGrid();
            } else {
                atualizarDadosSilencioso();
            }
        } catch (Exception e) {
            logger.error("Erro ao processar produto salvo", e);
            // Fallback para reconfiguração completa
            reconfigurarGrid();
        }
    }
    
    /**
     * Callback chamado quando um produto é excluído.
     * 
     * @param produto o produto excluído
     */
    private void onProdutoExcluido(Produto produto) {
        atualizarDadosSilencioso();
    }
    
    /**
     * Cria o componente de foto do produto.
     * 
     * @param produto o produto
     * @return componente com foto ou placeholder
     */
    private Component criarFotoProduto(Produto produto) {
        if (produto == null) {
            return criarIconePadrao();
        }
        
        try {
            if (produto.getFoto() != null && produto.getFoto().length > 0) {
                // Verificar se os dados são uma URL válida
                if (isValidUrl(produto.getFoto())) {
                    return criarImagemUrl(produto.getFoto());
                } else {
                    return criarImagemBytes(produto.getFoto());
                }
            } else {
                return criarIconePadrao();
            }
        } catch (Exception e) {
            logger.error("Erro ao criar componente de foto para produto {}: {}", 
                        produto.getId(), e.getMessage(), e);
            return criarIconePadrao();
        }
    }
    
    /**
     * Cria uma imagem a partir de uma URL.
     */
    private Component criarImagemUrl(byte[] fotoBytes) {
        try {
            String fotoUrl = new String(fotoBytes);
            com.vaadin.flow.component.html.Image img = new com.vaadin.flow.component.html.Image();
            img.setSrc(fotoUrl);
            configurarImagemMiniatura(img);
            return img;
        } catch (Exception e) {
            logger.error("Erro ao criar imagem da URL: {}", e.getMessage());
            return criarIconePadrao();
        }
    }
    
    /**
     * Cria uma imagem a partir de bytes.
     */
    private Component criarImagemBytes(byte[] fotoBytes) {
        try {
            String base64 = java.util.Base64.getEncoder().encodeToString(fotoBytes);
            String dataUrl = "data:image/jpeg;base64," + base64;
            
            com.vaadin.flow.component.html.Image img = new com.vaadin.flow.component.html.Image();
            img.setSrc(dataUrl);
            configurarImagemMiniatura(img);
            return img;
        } catch (Exception e) {
            logger.error("Erro ao criar imagem dos bytes: {}", e.getMessage());
            return criarIconePadrao();
        }
    }
    
    /**
     * Configura uma imagem como miniatura.
     */
    private void configurarImagemMiniatura(com.vaadin.flow.component.html.Image img) {
        img.setWidth("40px");
        img.setHeight("40px");
        img.getStyle().set("object-fit", "cover");
        img.getStyle().set("border-radius", "4px");
        img.getStyle().set("flex-shrink", "0");
    }
    
    /**
     * Cria um ícone padrão para produtos sem foto.
     */
    private Component criarIconePadrao() {
        Icon icon = new Icon(VaadinIcon.PICTURE);
        icon.setSize("40px");
        icon.setColor("var(--lumo-contrast-30pct)");
        return icon;
    }
    
    /**
     * Verifica se os dados em bytes representam uma URL válida.
     * 
     * @param fotoBytes os bytes da foto
     * @return true se for uma URL válida, false caso contrário
     */
    private boolean isValidUrl(byte[] fotoBytes) {
        try {
            String possibleUrl = new String(fotoBytes);
            // Verificar se é uma URL válida e não contém dados binários
            return (possibleUrl.startsWith("http") || possibleUrl.startsWith("https")) 
                   && !possibleUrl.contains("\0") // Não deve conter caracteres nulos (comum em dados binários)
                   && possibleUrl.length() < 2000; // URLs muito longas provavelmente não são URLs válidas
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Cria o indicador visual de estoque.
     * 
     * @param produto o produto
     * @return componente com indicador de estoque
     */
    private HorizontalLayout criarIndicadorEstoque(Produto produto) {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setSpacing(false);
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        
        Span estoqueSpan = new Span(produto.getEstoqueAtual().toString());
        
        if (produto.getEstoqueAtual() == 0) {
            estoqueSpan.getElement().getThemeList().add("badge error");
            layout.add(new Icon(VaadinIcon.WARNING), estoqueSpan);
        } else if (produto.isEstoqueBaixo()) {
            estoqueSpan.getElement().getThemeList().add("badge contrast");
            layout.add(new Icon(VaadinIcon.EXCLAMATION), estoqueSpan);
        } else {
            estoqueSpan.getElement().getThemeList().add("badge success");
            layout.add(estoqueSpan);
        }
        
        return layout;
    }
    
    /**
     * Cria o indicador visual de status.
     * 
     * @param produto o produto
     * @return componente com indicador de status
     */
    private Span criarIndicadorStatus(Produto produto) {
        Span statusSpan = new Span(produto.getAtivo() ? "Ativo" : "Inativo");
        
        if (produto.getAtivo()) {
            statusSpan.getElement().getThemeList().add("badge success");
        } else {
            statusSpan.getElement().getThemeList().add("badge error");
        }
        
        return statusSpan;
    }
    
    /**
     * Cria os botões de ação para cada produto.
     * 
     * @param produto o produto
     * @return layout com botões de ação
     */
    private HorizontalLayout criarBotoesAcao(Produto produto) {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setSpacing(false);
        layout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        
        // Botão editar
        Button editarButton = new Button(new Icon(VaadinIcon.EDIT));
        editarButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        editarButton.setTooltipText("Editar produto");
        editarButton.addClickListener(e -> editarProduto(produto));
        
        // Botão excluir
        Button excluirButton = new Button(new Icon(VaadinIcon.TRASH));
        excluirButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
        excluirButton.setTooltipText("Excluir produto");
        excluirButton.addClickListener(e -> excluirProduto(produto));
        
        layout.add(editarButton, excluirButton);
        return layout;
    }
    
    /**
     * Atualiza as estatísticas exibidas.
     */
    private void atualizarEstatisticas() {
        try {
            long totalProdutos = produtoService.contarProdutosAtivos();
            long produtosEstoqueBaixo = produtoService.contarProdutosComEstoqueBaixo();
            
            logger.info("Estatísticas: {} produtos ativos, {} com estoque baixo", 
                       totalProdutos, produtosEstoqueBaixo);
        } catch (Exception e) {
            logger.error("Erro ao atualizar estatísticas", e);
        }
    }
    
    /**
     * Formata um valor monetário.
     * 
     * @param valor o valor a ser formatado
     * @return valor formatado como moeda
     */
    private String formatarMoeda(BigDecimal valor) {
        return valor != null ? currencyFormat.format(valor) : "-";
    }
    
    /**
     * Mostra uma notificação de sucesso.
     * 
     * @param message mensagem a ser exibida
     */
    private void mostrarNotificacaoSucesso(String message) {
        Notification notification = Notification.show(message, 3000, Notification.Position.BOTTOM_END);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }
    
    /**
     * Mostra uma notificação de erro.
     * 
     * @param message mensagem a ser exibida
     */
    private void mostrarNotificacaoErro(String message) {
        Notification notification = Notification.show(message, 5000, Notification.Position.BOTTOM_END);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
