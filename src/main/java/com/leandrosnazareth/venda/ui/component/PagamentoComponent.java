package com.leandrosnazareth.venda.ui.component;

import com.leandrosnazareth.venda.domain.Venda;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * Componente para finalizar o pagamento da venda.
 * <p>
 * Permite selecionar a forma de pagamento, aplicar desconto,
 * informar valor recebido e finalizar a venda.
 * </p>
 */
public class PagamentoComponent extends VerticalLayout {

    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
    
    private ComboBox<Venda.FormaPagamento> formaPagamentoComboBox;
    private BigDecimalField descontoField;
    private BigDecimalField valorRecebidoField;
    private H3 valorTotalLabel;
    private H4 trocoLabel;
    private TextArea observacoesField;
    private Button finalizarButton;
    private Button cancelarButton;
    
    private Venda vendaAtual;
    private Consumer<Venda> onFinalizarVenda;
    private Consumer<Venda> onCancelarVenda;
    private Consumer<Venda> onAplicarDesconto;

    public PagamentoComponent() {
        setWidth("100%");
        setPadding(true);
        setSpacing(true);
        
        criarComponentes();
        configurarEventos();
        atualizarInterface();
    }

    private void criarComponentes() {
        H3 titulo = new H3("Finalizar Pagamento");
        titulo.addClassName(LumoUtility.Margin.NONE);
        
        // Forma de pagamento
        formaPagamentoComboBox = new ComboBox<>("Forma de Pagamento");
        formaPagamentoComboBox.setItems(Venda.FormaPagamento.values());
        formaPagamentoComboBox.setItemLabelGenerator(Venda.FormaPagamento::getDescricao);
        formaPagamentoComboBox.setRequired(true);
        formaPagamentoComboBox.setWidthFull();
        
        // Desconto
        descontoField = new BigDecimalField("Desconto (R$)");
        descontoField.setValue(BigDecimal.ZERO);
        descontoField.setPrefixComponent(new Span("R$"));
        descontoField.setWidthFull();
        
        // Valor recebido
        valorRecebidoField = new BigDecimalField("Valor Recebido (R$)");
        valorRecebidoField.setRequired(true);
        valorRecebidoField.setPrefixComponent(new Span("R$"));
        valorRecebidoField.setWidthFull();
        
        // Labels de valores
        valorTotalLabel = new H3("Total: R$ 0,00");
        valorTotalLabel.addClassName(LumoUtility.FontWeight.BOLD);
        valorTotalLabel.addClassName(LumoUtility.TextColor.PRIMARY);
        
        trocoLabel = new H4("Troco: R$ 0,00");
        trocoLabel.addClassName(LumoUtility.FontWeight.BOLD);
        trocoLabel.addClassName(LumoUtility.TextColor.SUCCESS);
        
        // Observações
        observacoesField = new TextArea("Observações");
        observacoesField.setPlaceholder("Observações sobre a venda...");
        observacoesField.setWidthFull();
        observacoesField.setMaxLength(500);
        
        // Botões
        finalizarButton = new Button("Finalizar Venda", new Icon(VaadinIcon.CHECK));
        finalizarButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        finalizarButton.setWidthFull();
        finalizarButton.setEnabled(false);
        
        cancelarButton = new Button("Cancelar Venda", new Icon(VaadinIcon.CLOSE));
        cancelarButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        cancelarButton.setWidthFull();
        cancelarButton.setEnabled(false);
        
        HorizontalLayout botoesLayout = new HorizontalLayout(finalizarButton, cancelarButton);
        botoesLayout.setWidthFull();
        
        // Layout principal
        add(titulo, 
            formaPagamentoComboBox,
            descontoField,
            valorTotalLabel,
            valorRecebidoField,
            trocoLabel,
            observacoesField,
            botoesLayout);
    }

    private void configurarEventos() {
        descontoField.addValueChangeListener(e -> {
            if (vendaAtual != null && onAplicarDesconto != null) {
                BigDecimal desconto = e.getValue() != null ? e.getValue() : BigDecimal.ZERO;
                vendaAtual.setDesconto(desconto);
                if (onAplicarDesconto != null) {
                    onAplicarDesconto.accept(vendaAtual);
                }
                atualizarValores();
            }
        });
        
        valorRecebidoField.addValueChangeListener(e -> {
            calcularTroco();
            atualizarBotaoFinalizar();
        });
        
        formaPagamentoComboBox.addValueChangeListener(e -> atualizarBotaoFinalizar());
        
        finalizarButton.addClickListener(e -> finalizarVenda());
        
        cancelarButton.addClickListener(e -> {
            if (onCancelarVenda != null && vendaAtual != null) {
                onCancelarVenda.accept(vendaAtual);
            }
        });
    }

    private void finalizarVenda() {
        if (!validarCampos()) {
            return;
        }
        
        if (vendaAtual != null && onFinalizarVenda != null) {
            vendaAtual.setFormaPagamento(formaPagamentoComboBox.getValue());
            vendaAtual.setValorRecebido(valorRecebidoField.getValue());
            vendaAtual.setObservacoes(observacoesField.getValue());
            
            onFinalizarVenda.accept(vendaAtual);
        }
    }

    private boolean validarCampos() {
        if (formaPagamentoComboBox.getValue() == null) {
            Notification.show("Selecione a forma de pagamento", 3000, Notification.Position.MIDDLE);
            formaPagamentoComboBox.focus();
            return false;
        }
        
        if (valorRecebidoField.getValue() == null) {
            Notification.show("Informe o valor recebido", 3000, Notification.Position.MIDDLE);
            valorRecebidoField.focus();
            return false;
        }
        
        if (vendaAtual != null && valorRecebidoField.getValue().compareTo(vendaAtual.getValorTotal()) < 0) {
            Notification.show("Valor recebido é menor que o total da venda", 3000, Notification.Position.MIDDLE);
            valorRecebidoField.focus();
            return false;
        }
        
        return true;
    }

    private void calcularTroco() {
        if (vendaAtual != null && valorRecebidoField.getValue() != null) {
            BigDecimal valorRecebido = valorRecebidoField.getValue();
            BigDecimal valorTotal = vendaAtual.getValorTotal();
            
            if (valorRecebido.compareTo(valorTotal) >= 0) {
                BigDecimal troco = valorRecebido.subtract(valorTotal);
                trocoLabel.setText("Troco: " + currencyFormat.format(troco));
                trocoLabel.addClassName(LumoUtility.TextColor.SUCCESS);
                trocoLabel.removeClassName(LumoUtility.TextColor.ERROR);
            } else {
                BigDecimal falta = valorTotal.subtract(valorRecebido);
                trocoLabel.setText("Falta: " + currencyFormat.format(falta));
                trocoLabel.addClassName(LumoUtility.TextColor.ERROR);
                trocoLabel.removeClassName(LumoUtility.TextColor.SUCCESS);
            }
        } else {
            trocoLabel.setText("Troco: R$ 0,00");
            trocoLabel.removeClassName(LumoUtility.TextColor.ERROR);
            trocoLabel.addClassName(LumoUtility.TextColor.SUCCESS);
        }
    }

    private void atualizarBotaoFinalizar() {
        boolean podeAtualizar = vendaAtual != null && 
                               !vendaAtual.getItens().isEmpty() &&
                               formaPagamentoComboBox.getValue() != null &&
                               valorRecebidoField.getValue() != null &&
                               valorRecebidoField.getValue().compareTo(vendaAtual.getValorTotal()) >= 0;
        
        finalizarButton.setEnabled(podeAtualizar);
    }

    private void atualizarValores() {
        if (vendaAtual != null) {
            valorTotalLabel.setText("Total: " + currencyFormat.format(vendaAtual.getValorTotal()));
            calcularTroco();
        } else {
            valorTotalLabel.setText("Total: R$ 0,00");
            trocoLabel.setText("Troco: R$ 0,00");
        }
    }

    public void setVenda(Venda venda) {
        this.vendaAtual = venda;
        atualizarInterface();
    }

    private void atualizarInterface() {
        if (vendaAtual == null || vendaAtual.getItens().isEmpty()) {
            setEnabled(false);
            limparCampos();
        } else {
            setEnabled(true);
            atualizarValores();
            
            if (vendaAtual.getFormaPagamento() != null) {
                formaPagamentoComboBox.setValue(vendaAtual.getFormaPagamento());
            }
            
            if (vendaAtual.getDesconto() != null) {
                descontoField.setValue(vendaAtual.getDesconto());
            }
            
            if (vendaAtual.getValorRecebido() != null) {
                valorRecebidoField.setValue(vendaAtual.getValorRecebido());
            }
            
            if (vendaAtual.getObservacoes() != null) {
                observacoesField.setValue(vendaAtual.getObservacoes());
            }
            
            cancelarButton.setEnabled(true);
        }
        
        atualizarBotaoFinalizar();
    }

    private void limparCampos() {
        formaPagamentoComboBox.clear();
        descontoField.setValue(BigDecimal.ZERO);
        valorRecebidoField.clear();
        observacoesField.clear();
        valorTotalLabel.setText("Total: R$ 0,00");
        trocoLabel.setText("Troco: R$ 0,00");
        finalizarButton.setEnabled(false);
        cancelarButton.setEnabled(false);
    }

    public void focarFormaPagamento() {
        formaPagamentoComboBox.focus();
    }

    public void definirValorRecebidoComoTotal() {
        if (vendaAtual != null) {
            valorRecebidoField.setValue(vendaAtual.getValorTotal());
        }
    }

    // Getters e setters para os callbacks
    public void setOnFinalizarVenda(Consumer<Venda> onFinalizarVenda) {
        this.onFinalizarVenda = onFinalizarVenda;
    }

    public void setOnCancelarVenda(Consumer<Venda> onCancelarVenda) {
        this.onCancelarVenda = onCancelarVenda;
    }

    public void setOnAplicarDesconto(Consumer<Venda> onAplicarDesconto) {
        this.onAplicarDesconto = onAplicarDesconto;
    }
}
