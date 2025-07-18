package com.leandrosnazareth.produto.ui.component;

import java.math.BigDecimal;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.leandrosnazareth.produto.domain.Produto;
import com.leandrosnazareth.produto.service.ProdutoService;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.validator.BigDecimalRangeValidator;
import com.vaadin.flow.data.validator.IntegerRangeValidator;
import com.vaadin.flow.data.validator.StringLengthValidator;

/**
 * Componente de formulário para criar e editar produtos.
 * <p>
 * Este componente fornece uma interface de usuário completa para
 * gerenciar produtos, incluindo validação de dados e integração
 * com o serviço de produtos.
 * </p>
 */
public class ProdutoForm extends Dialog {
    
    private static final Logger logger = LoggerFactory.getLogger(ProdutoForm.class);
    
    private final ProdutoService produtoService;
    private final Binder<Produto> binder;
    
    // Campos do formulário
    private final TextField codigoField;
    private final TextField nomeField;
    private final TextArea descricaoField;
    private final ComboBox<String> categoriaField;
    private final TextField marcaField;
    private final TextField fornecedorField;
    private final BigDecimalField precoCompraField;
    private final BigDecimalField precoVendaField;
    private final IntegerField estoqueAtualField;
    private final IntegerField estoqueMinimo;
    private final IntegerField estoqueMaximo;
    private final TextField unidadeField;
    private final BigDecimalField pesoField;
    private final Checkbox ativoField;
    private final TextArea observacoesField;
    
    // Campos de foto
    private final FotoUploadComponent fotoUpload;
    private byte[] fotoBytes;
    
    // Botões
    private final Button salvarButton;
    private final Button cancelarButton;
    private final Button excluirButton;
    
    private Produto produto;
    private final Consumer<Produto> onSave;
    private final Consumer<Produto> onDelete;
    
    /**
     * Construtor do formulário de produto.
     * 
     * @param produtoService serviço para operações de produto
     * @param onSave callback chamado após salvar com sucesso
     * @param onDelete callback chamado após excluir com sucesso
     */
    public ProdutoForm(ProdutoService produtoService, Consumer<Produto> onSave, Consumer<Produto> onDelete) {
        this.produtoService = produtoService;
        this.onSave = onSave;
        this.onDelete = onDelete;
        this.binder = new Binder<>(Produto.class);
        
        // Configurar dialog
        setModal(true);
        setDraggable(true);
        setResizable(true);
        setWidth("800px");
        setHeight("90vh");
        
        // Inicializar campos
        codigoField = new TextField("Código");
        nomeField = new TextField("Nome");
        descricaoField = new TextArea("Descrição");
        categoriaField = new ComboBox<>("Categoria");
        marcaField = new TextField("Marca");
        fornecedorField = new TextField("Fornecedor");
        precoCompraField = new BigDecimalField("Preço de Compra");
        precoVendaField = new BigDecimalField("Preço de Venda");
        estoqueAtualField = new IntegerField("Estoque Atual");
        estoqueMinimo = new IntegerField("Estoque Mínimo");
        estoqueMaximo = new IntegerField("Estoque Máximo");
        unidadeField = new TextField("Unidade");
        pesoField = new BigDecimalField("Peso (kg)");
        ativoField = new Checkbox("Ativo");
        observacoesField = new TextArea("Observações");
        
        // Campos de foto
        fotoUpload = new FotoUploadComponent();
        fotoUpload.setOnFotoChange(bytes -> {
            // Apenas armazenar os bytes, não disparar atualizações desnecessárias
            this.fotoBytes = bytes;
            // Log para debug
            logger.debug("Foto alterada no formulário: {} bytes", 
                        bytes != null ? bytes.length : 0);
        });
        
        // Botões
        salvarButton = new Button("Salvar");
        cancelarButton = new Button("Cancelar");
        excluirButton = new Button("Excluir");
        
        configurarCampos();
        configurarBinder();
        configurarBotoes();
        criarLayout();
        
        // Carregar dados para campos de seleção
        carregarDadosComboBox();
    }
    
    /**
     * Configura as propriedades dos campos do formulário.
     */
    private void configurarCampos() {
        // Código
        codigoField.setRequired(true);
        codigoField.setMaxLength(Produto.CODIGO_MAX_LENGTH);
        codigoField.setClearButtonVisible(true);
        codigoField.setHelperText("Código único do produto");
        
        // Nome
        nomeField.setRequired(true);
        nomeField.setMaxLength(Produto.NOME_MAX_LENGTH);
        nomeField.setClearButtonVisible(true);
        
        // Descrição
        descricaoField.setMaxLength(Produto.DESCRICAO_MAX_LENGTH);
        descricaoField.setClearButtonVisible(true);
        descricaoField.setHeight("100px");
        
        // Categoria
        categoriaField.setAllowCustomValue(true);
        categoriaField.setClearButtonVisible(true);
        categoriaField.addCustomValueSetListener(e -> {
            String customValue = e.getDetail();
            categoriaField.setValue(customValue);
        });
        
        // Marca
        marcaField.setMaxLength(Produto.MARCA_MAX_LENGTH);
        marcaField.setClearButtonVisible(true);
        
        // Fornecedor
        fornecedorField.setMaxLength(Produto.FORNECEDOR_MAX_LENGTH);
        fornecedorField.setClearButtonVisible(true);
        
        // Preços
        precoCompraField.setPrefixComponent(new com.vaadin.flow.component.html.Span("R$"));
        
        precoVendaField.setPrefixComponent(new com.vaadin.flow.component.html.Span("R$"));
        precoVendaField.setRequired(true);
        
        // Estoque
        estoqueAtualField.setMin(0);
        estoqueAtualField.setRequired(true);
        estoqueAtualField.setValue(0);
        
        estoqueMinimo.setMin(0);
        estoqueMinimo.setValue(0);
        estoqueMinimo.setHelperText("Alertar quando estoque for menor ou igual");
        
        estoqueMaximo.setMin(0);
        estoqueMaximo.setValue(0);
        estoqueMaximo.setHelperText("Capacidade máxima de estoque");
        
        // Unidade
        unidadeField.setMaxLength(Produto.UNIDADE_MAX_LENGTH);
        unidadeField.setClearButtonVisible(true);
        unidadeField.setHelperText("Ex: UN, KG, L, M");
        
        // Peso
        pesoField.setSuffixComponent(new com.vaadin.flow.component.html.Span("kg"));
        
        // Ativo
        ativoField.setValue(true);
        ativoField.setHelperText("Produto disponível para venda");
        
        // Observações
        observacoesField.setMaxLength(Produto.DESCRICAO_MAX_LENGTH);
        observacoesField.setClearButtonVisible(true);
        observacoesField.setHeight("80px");
        
        // Foto
        // Configuração específica já feita no componente
    }
    
    /**
     * Configura o binder para validação e vinculação dos dados.
     */
    private void configurarBinder() {
        // Código
        binder.forField(codigoField)
            .withValidator(new StringLengthValidator("Código deve ter entre 1 e " + Produto.CODIGO_MAX_LENGTH + " caracteres", 1, Produto.CODIGO_MAX_LENGTH))
            .bind(Produto::getCodigo, Produto::setCodigo);
        
        // Nome
        binder.forField(nomeField)
            .withValidator(new StringLengthValidator("Nome deve ter entre 1 e " + Produto.NOME_MAX_LENGTH + " caracteres", 1, Produto.NOME_MAX_LENGTH))
            .bind(Produto::getNome, Produto::setNome);
        
        // Descrição
        binder.forField(descricaoField)
            .withValidator(new StringLengthValidator("Descrição deve ter no máximo " + Produto.DESCRICAO_MAX_LENGTH + " caracteres", 0, Produto.DESCRICAO_MAX_LENGTH))
            .bind(Produto::getDescricao, Produto::setDescricao);
        
        // Categoria
        binder.forField(categoriaField)
            .withValidator(new StringLengthValidator("Categoria deve ter no máximo " + Produto.CATEGORIA_MAX_LENGTH + " caracteres", 0, Produto.CATEGORIA_MAX_LENGTH))
            .bind(Produto::getCategoria, Produto::setCategoria);
        
        // Marca
        binder.forField(marcaField)
            .withValidator(new StringLengthValidator("Marca deve ter no máximo " + Produto.MARCA_MAX_LENGTH + " caracteres", 0, Produto.MARCA_MAX_LENGTH))
            .bind(Produto::getMarca, Produto::setMarca);
        
        // Fornecedor
        binder.forField(fornecedorField)
            .withValidator(new StringLengthValidator("Fornecedor deve ter no máximo " + Produto.FORNECEDOR_MAX_LENGTH + " caracteres", 0, Produto.FORNECEDOR_MAX_LENGTH))
            .bind(Produto::getFornecedor, Produto::setFornecedor);
        
        // Preço de compra
        binder.forField(precoCompraField)
            .withValidator(new BigDecimalRangeValidator("Preço de compra deve ser maior ou igual a zero", BigDecimal.ZERO, null))
            .bind(Produto::getPrecoCompra, Produto::setPrecoCompra);
        
        // Preço de venda
        binder.forField(precoVendaField)
            .withValidator(new BigDecimalRangeValidator("Preço de venda deve ser maior que zero", BigDecimal.valueOf(0.01), null))
            .bind(Produto::getPrecoVenda, Produto::setPrecoVenda);
        
        // Estoque atual
        binder.forField(estoqueAtualField)
            .withValidator(new IntegerRangeValidator("Estoque atual deve ser maior ou igual a zero", 0, null))
            .bind(Produto::getEstoqueAtual, Produto::setEstoqueAtual);
        
        // Estoque mínimo
        binder.forField(estoqueMinimo)
            .withValidator(new IntegerRangeValidator("Estoque mínimo deve ser maior ou igual a zero", 0, null))
            .bind(Produto::getEstoqueMinimo, Produto::setEstoqueMinimo);
        
        // Estoque máximo
        binder.forField(estoqueMaximo)
            .withValidator(new IntegerRangeValidator("Estoque máximo deve ser maior ou igual a zero", 0, null))
            .bind(Produto::getEstoqueMaximo, Produto::setEstoqueMaximo);
        
        // Unidade
        binder.forField(unidadeField)
            .withValidator(new StringLengthValidator("Unidade deve ter no máximo " + Produto.UNIDADE_MAX_LENGTH + " caracteres", 0, Produto.UNIDADE_MAX_LENGTH))
            .bind(Produto::getUnidade, Produto::setUnidade);
        
        // Peso
        binder.forField(pesoField)
            .withValidator(new BigDecimalRangeValidator("Peso deve ser maior ou igual a zero", BigDecimal.ZERO, null))
            .bind(Produto::getPeso, Produto::setPeso);
        
        // Ativo
        binder.forField(ativoField)
            .bind(Produto::getAtivo, Produto::setAtivo);
        
        // Observações
        binder.forField(observacoesField)
            .withValidator(new StringLengthValidator("Observações deve ter no máximo " + Produto.DESCRICAO_MAX_LENGTH + " caracteres", 0, Produto.DESCRICAO_MAX_LENGTH))
            .bind(Produto::getObservacoes, Produto::setObservacoes);
        
        // Foto - não precisa de binding específico pois o componente já gerencia
        // os dados internamente através do callback
    }
    
    /**
     * Configura os botões do formulário.
     */
    private void configurarBotoes() {
        // Salvar
        salvarButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        salvarButton.addClickShortcut(Key.ENTER);
        salvarButton.addClickListener(e -> salvar());
        
        // Cancelar
        cancelarButton.addClickListener(e -> cancelar());
        cancelarButton.addClickShortcut(Key.ESCAPE);
        
        // Excluir
        excluirButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        excluirButton.addClickListener(e -> excluir());
    }
    
    /**
     * Cria o layout do formulário.
     */
    private void criarLayout() {
        H3 title = new H3("Produto");
        
        // Formulário principal
        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0", 1),
            new FormLayout.ResponsiveStep("600px", 2),
            new FormLayout.ResponsiveStep("900px", 3)
        );
        
        // Adicionar campos ao formulário
        formLayout.add(codigoField, nomeField, categoriaField);
        formLayout.add(marcaField, fornecedorField, unidadeField);
        formLayout.add(precoCompraField, precoVendaField, pesoField);
        formLayout.add(estoqueAtualField, estoqueMinimo, estoqueMaximo);
        
        // Campos que ocupam toda a largura
        formLayout.setColspan(descricaoField, 3);
        formLayout.add(descricaoField);
        
        formLayout.add(ativoField);
        
        formLayout.setColspan(fotoUpload, 3);
        formLayout.add(fotoUpload);
        
        formLayout.setColspan(observacoesField, 3);
        formLayout.add(observacoesField);
        
        // Layout dos botões
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        buttonLayout.add(excluirButton, cancelarButton, salvarButton);
        
        // Layout principal
        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.add(title, formLayout, buttonLayout);
        mainLayout.setSpacing(true);
        mainLayout.setPadding(true);
        mainLayout.setAlignItems(FlexComponent.Alignment.STRETCH);
        
        add(mainLayout);
    }
    
    /**
     * Carrega dados para os campos de seleção.
     */
    private void carregarDadosComboBox() {
        try {
            categoriaField.setItems(produtoService.listarCategorias());
        } catch (Exception e) {
            logger.error("Erro ao carregar categorias", e);
            mostrarNotificacaoErro("Erro ao carregar categorias");
        }
    }
    
    /**
     * Abre o formulário para criar um novo produto.
     */
    public void abrirParaNovo() {
        this.produto = new Produto();
        binder.setBean(produto);
        
        // Limpar foto
        fotoBytes = null;
        fotoUpload.setFoto(null);
        
        excluirButton.setVisible(false);
        open();
        codigoField.focus();
    }
    
    /**
     * Abre o formulário para editar um produto existente.
     * 
     * @param produto o produto a ser editado
     */
    public void abrirParaEdicao(Produto produto) {
        this.produto = produto;
        binder.setBean(produto);
        
        // Carregar foto se existir
        if (produto.getFoto() != null) {
            fotoBytes = produto.getFoto();
            fotoUpload.setFoto(fotoBytes);
        } else {
            fotoBytes = null;
            fotoUpload.setFoto(null);
        }
        
        excluirButton.setVisible(true);
        open();
        nomeField.focus();
    }
    
    /**
     * Salva o produto.
     */
    private void salvar() {
        try {
            if (binder.validate().isOk()) {
                binder.writeBean(produto);
                
                // Salvar foto se houver
                if (fotoBytes != null) {
                    produto.setFoto(fotoBytes);
                }
                
                // Validação adicional
                if (produto.getEstoqueMinimo() != null && produto.getEstoqueMaximo() != null &&
                    produto.getEstoqueMinimo() > produto.getEstoqueMaximo()) {
                    mostrarNotificacaoErro("Estoque mínimo não pode ser maior que o estoque máximo");
                    return;
                }
                
                Produto produtoSalvo;
                if (produto.getId() == null) {
                    produtoSalvo = produtoService.criarProduto(produto);
                    mostrarNotificacaoSucesso("Produto criado com sucesso");
                } else {
                    produtoSalvo = produtoService.atualizarProduto(produto);
                    mostrarNotificacaoSucesso("Produto atualizado com sucesso");
                }
                
                if (onSave != null) {
                    onSave.accept(produtoSalvo);
                }
                
                close();
            }
        } catch (ValidationException e) {
            mostrarNotificacaoErro("Verifique os dados informados");
        } catch (IllegalArgumentException e) {
            mostrarNotificacaoErro(e.getMessage());
        } catch (Exception e) {
            logger.error("Erro ao salvar produto", e);
            mostrarNotificacaoErro("Erro inesperado ao salvar produto");
        }
    }
    
    /**
     * Exclui o produto.
     */
    private void excluir() {
        if (produto != null && produto.getId() != null) {
            try {
                produtoService.excluirProduto(produto.getId());
                mostrarNotificacaoSucesso("Produto excluído com sucesso");
                
                if (onDelete != null) {
                    onDelete.accept(produto);
                }
                
                close();
            } catch (Exception e) {
                logger.error("Erro ao excluir produto", e);
                mostrarNotificacaoErro("Erro ao excluir produto: " + e.getMessage());
            }
        }
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
    
    /**
     * Cancela a edição/criação do produto.
     */
    private void cancelar() {
        try {
            // Limpar estado da foto sem disparar callbacks
            fotoBytes = null;
            fotoUpload.limparSilenciosamente();
            
            // Resetar formulário
            binder.readBean(null);
            
            // Fechar dialog
            close();
            
            logger.debug("Formulário cancelado com sucesso");
        } catch (Exception e) {
            logger.error("Erro ao cancelar formulário: {}", e.getMessage(), e);
            // Forçar fechamento mesmo com erro
            close();
        }
    }
}
