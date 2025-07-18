package com.leandrosnazareth.produto.ui.component;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Input;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

/**
 * Componente personalizado para upload de fotos.
 * 
 * Permite upload de arquivos locais ou inserção via URL.
 * Exibe preview da imagem selecionada.
 */
public class FotoUploadComponent extends VerticalLayout {
    
    private static final Logger logger = LoggerFactory.getLogger(FotoUploadComponent.class);
    
    private final Image preview;
    private final TextField urlField;
    private final Button previewButton;
    private final Button removeButton;
    private final Button selectFileButton;
    private final Input fileInput;
    private final Div uploadInfo;
    
    private byte[] fotoBytes;
    private String fileName;
    private Consumer<byte[]> onFotoChange;
    private boolean silenciarCallbacks = false;
    
    /**
     * Construtor do componente de upload de foto.
     */
    public FotoUploadComponent() {
        setSpacing(true);
        setPadding(false);
        
        // Criar preview da imagem
        preview = new Image();
        preview.setWidth("200px");
        preview.setHeight("150px");
        preview.getStyle().set("border", "1px solid #ccc");
        preview.getStyle().set("border-radius", "8px");
        preview.getStyle().set("object-fit", "cover");
        preview.setVisible(false);
        
        // Criar input de arquivo (HTML5)
        fileInput = new Input();
        fileInput.getElement().setAttribute("type", "file");
        fileInput.getElement().setAttribute("accept", "image/*");
        fileInput.getElement().setAttribute("style", "display: none");
        
        // Criar botão para selecionar arquivo
        selectFileButton = new Button("Selecionar Arquivo", VaadinIcon.UPLOAD.create());
        selectFileButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        selectFileButton.addClickListener(e -> {
            fileInput.getElement().callJsFunction("click");
        });
        
        // Info sobre upload
        uploadInfo = new Div();
        uploadInfo.setText("Arquivos aceitos: JPEG, PNG, GIF, WEBP (máx. 5MB)");
        uploadInfo.getStyle().set("font-size", "0.8em");
        uploadInfo.getStyle().set("color", "var(--lumo-secondary-text-color)");
        
        // Criar campo de URL
        urlField = new TextField("Ou insira uma URL");
        urlField.setPlaceholder("https://exemplo.com/imagem.jpg");
        urlField.setClearButtonVisible(true);
        urlField.setWidthFull();
        
        // Criar botão de preview
        previewButton = new Button("Visualizar URL", VaadinIcon.EYE.create());
        previewButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        previewButton.addClickListener(e -> mostrarPreviewUrl());
        
        // Criar botão de remover
        removeButton = new Button("Remover", VaadinIcon.TRASH.create());
        removeButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
        removeButton.setVisible(false);
        removeButton.addClickListener(e -> removerFoto());
        
        // Layout dos botões de arquivo
        HorizontalLayout fileButtonLayout = new HorizontalLayout(selectFileButton);
        fileButtonLayout.setSpacing(true);
        
        // Layout dos botões de URL
        HorizontalLayout urlButtonLayout = new HorizontalLayout(previewButton, removeButton);
        urlButtonLayout.setSpacing(true);
        
        // Separador visual
        Div separator = new Div();
        separator.setText("OU");
        separator.getStyle().set("text-align", "center");
        separator.getStyle().set("color", "var(--lumo-secondary-text-color)");
        separator.getStyle().set("font-size", "0.9em");
        separator.getStyle().set("margin", "10px 0");
        
        // Adicionar componentes
        add(fileInput, fileButtonLayout, uploadInfo, separator, urlField, urlButtonLayout, preview);
        
        // Configurar listeners
        configurarFileInput();
        
        // Adicionar listener para Enter no campo URL
        urlField.addKeyPressListener(event -> {
            if (event.getKey().getKeys().contains("Enter")) {
                mostrarPreviewUrl();
            }
        });
    }
    
    /**
     * Configura o input de arquivo usando JavaScript.
     */
    private void configurarFileInput() {
        // Adicionar listener via JavaScript para processar o arquivo
        fileInput.getElement().addEventListener("change", e -> {
            // Usar JavaScript para ler o arquivo
            getElement().executeJs(
                """
                const fileInput = this.querySelector('input[type=file]');
                const file = fileInput.files[0];
                if (file) {
                    if (file.size > 5 * 1024 * 1024) {
                        $0.$server.onFileError('Arquivo muito grande. Máximo: 5MB');
                        return;
                    }
                    
                    const allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'];
                    if (!allowedTypes.includes(file.type)) {
                        $0.$server.onFileError('Tipo de arquivo não permitido. Use: JPEG, PNG, GIF ou WEBP');
                        return;
                    }
                    
                    const reader = new FileReader();
                    reader.onload = function(e) {
                        const base64 = e.target.result;
                        $0.$server.onFileSelected(file.name, file.type, base64);
                    };
                    reader.readAsDataURL(file);
                }
                """,
                getElement()
            );
        });
    }
    
    /**
     * Callback chamado quando um arquivo é selecionado com sucesso.
     */
    @com.vaadin.flow.component.ClientCallable
    public void onFileSelected(String fileName, String mimeType, String base64Data) {
        try {
            this.fileName = fileName;
            
            // Converter base64 para bytes (remover o prefixo data:image/...;base64,)
            String base64 = base64Data.substring(base64Data.indexOf(',') + 1);
            this.fotoBytes = java.util.Base64.getDecoder().decode(base64);
            
            // Exibir preview
            preview.setSrc(base64Data);
            preview.setVisible(true);
            removeButton.setVisible(true);
            
            // Limpar campo URL
            urlField.clear();
            
            // Notificar mudança
            notificarMudancaFoto(fotoBytes);
            
            logger.info("Arquivo carregado: {} ({} bytes)", fileName, fotoBytes.length);
            
            Notification.show("Imagem carregada com sucesso!", 
                3000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                
        } catch (Exception e) {
            logger.error("Erro ao processar arquivo: {}", e.getMessage(), e);
            Notification.show("Erro ao processar arquivo: " + e.getMessage(), 
                5000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
    
    /**
     * Callback chamado quando há erro no arquivo.
     */
    @com.vaadin.flow.component.ClientCallable
    public void onFileError(String message) {
        logger.warn("Erro no arquivo: {}", message);
        Notification.show(message, 5000, Notification.Position.MIDDLE)
            .addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
    
    /**
     * Mostra o preview da imagem a partir da URL.
     */
    private void mostrarPreviewUrl() {
        String url = urlField.getValue();
        if (url != null && !url.trim().isEmpty()) {
            try {
                // Simular conversão para bytes (em uma implementação real, 
                // você baixaria a imagem e converteria para bytes)
                fotoBytes = url.getBytes();
                
                preview.setSrc(url);
                preview.setVisible(true);
                removeButton.setVisible(true);
                
                // Notificar mudança
                notificarMudancaFoto(fotoBytes);
                
                logger.info("Preview da foto carregado: {}", url);
                
            } catch (Exception e) {
                logger.error("Erro ao carregar preview: {}", e.getMessage(), e);
                Notification.show("Erro ao carregar imagem: " + e.getMessage(), 
                    5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        } else {
            Notification.show("Por favor, insira uma URL válida", 
                3000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_WARNING);
        }
    }
    
    /**
     * Remove a foto selecionada.
     */
    private void removerFoto() {
        fotoBytes = null;
        fileName = null;
        urlField.clear();
        preview.setVisible(false);
        removeButton.setVisible(false);
        
        // Limpar o input de arquivo
        fileInput.getElement().setProperty("value", "");
        
        // Notificar mudança
        notificarMudancaFoto(null);
    }
    
    /**
     * Limpa o estado do componente sem disparar callbacks.
     */
    public void limparSilenciosamente() {
        silenciarCallbacks = true;
        try {
            fotoBytes = null;
            fileName = null;
            urlField.clear();
            preview.setVisible(false);
            removeButton.setVisible(false);
            
            // Limpar o input de arquivo
            fileInput.getElement().setProperty("value", "");
            
            logger.debug("Componente de foto limpo silenciosamente");
        } catch (Exception e) {
            logger.error("Erro ao limpar componente de foto: {}", e.getMessage(), e);
        } finally {
            silenciarCallbacks = false;
        }
    }

    /**
     * Define o callback para mudanças na foto.
     */
    public void setOnFotoChange(Consumer<byte[]> onFotoChange) {
        this.onFotoChange = onFotoChange;
    }
    
    /**
     * Define a foto atual.
     */
    public void setFoto(byte[] fotoBytes) {
        this.fotoBytes = fotoBytes;
        if (fotoBytes != null && fotoBytes.length > 0) {
            // Converter bytes para base64 para exibir
            String base64 = java.util.Base64.getEncoder().encodeToString(fotoBytes);
            String dataUrl = "data:image/jpeg;base64," + base64;
            
            preview.setSrc(dataUrl);
            preview.setVisible(true);
            removeButton.setVisible(true);
            
            // Limpar campo URL
            urlField.clear();
        } else {
            urlField.clear();
            preview.setVisible(false);
            removeButton.setVisible(false);
        }
    }
    
    /**
     * Define a URL da foto.
     */
    public void setFotoUrl(String url) {
        urlField.setValue(url != null ? url : "");
        if (url != null && !url.trim().isEmpty()) {
            mostrarPreviewUrl();
        }
    }
    
    /**
     * Retorna os bytes da foto atual.
     */
    public byte[] getFoto() {
        return fotoBytes;
    }
    
    /**
     * Retorna a URL da foto.
     */
    public String getFotoUrl() {
        return urlField.getValue();
    }
    
    /**
     * Retorna o nome do arquivo.
     */
    public String getFileName() {
        return fileName;
    }
    
    /**
     * Verifica se há uma foto selecionada.
     */
    public boolean hasFoto() {
        return fotoBytes != null && fotoBytes.length > 0;
    }
    
    /**
     * Notifica mudança de foto de forma segura.
     */
    private void notificarMudancaFoto(byte[] bytes) {
        if (silenciarCallbacks) {
            return;
        }
        
        if (silenciarCallbacks) {
            return;
        }
        
        try {
            if (onFotoChange != null) {
                onFotoChange.accept(bytes);
            }
        } catch (Exception e) {
            logger.error("Erro ao notificar mudança de foto: {}", e.getMessage(), e);
        }
    }
}
