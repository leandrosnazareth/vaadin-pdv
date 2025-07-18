# 🛒 Sistema PDV - Vaadin

**Sistema de Ponto de Venda desenvolvido com Vaadin Framework**

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.org/projects/jdk/17/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Vaadin](https://img.shields.io/badge/Vaadin-24.8.3-00B4F0.svg)](https://vaadin.com/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

---

## 📋 Índice

- [Sobre o Projeto](#-sobre-o-projeto)
- [Funcionalidades](#-funcionalidades)
- [Tecnologias](#️-tecnologias)
- [Screenshots](#️-screenshots)
- [Como Executar](#-como-executar)
- [Framework Vaadin](#-framework-vaadin)
- [Autor](#-autor)
- [Como Contribuir](#-como-contribuir)
- [Mostre seu Apoio](#-mostre-seu-apoio)
- [Licença](#-licença)

---

## 🎯 Sobre o Projeto

O **Vaadin PDV** é um sistema base de **Ponto de Venda (PDV)** O sistema oferece uma interface moderna, intuitiva e responsiva, similar aos PDVs utilizados em supermercados e estabelecimentos comerciais.

### Objetivo

Demonstrar as capacidades do **Vaadin Framework** na criação de aplicações empresariais robustas, oferecendo:

- **Interface rica e responsiva** sem JavaScript customizado
- **Arquitetura empresarial** seguindo melhores práticas
- **Desenvolvimento rápido** com componentes prontos
- **Integração perfeita** com Spring Boot
- **Segurança integrada** e controle de acesso

---

## ✨ Funcionalidades

### Módulo de Vendas (PDV)

#### Interface Principal
- **Busca Inteligente**: Pesquisa produtos por nome, código ou categoria
- **Carrinho Dinâmico**: Visualização em tempo real dos itens selecionados
- **Controle de Quantidade**: Incremento/decremento com validação de estoque
- **Cálculo Automático**: Atualização automática de subtotais e total geral

#### Formas de Pagamento
- **Dinheiro**: Cálculo automático de troco
- **Cartão de Crédito**: Integração para pagamentos eletrônicos
- **Cartão de Débito**: Processamento rápido e seguro

#### Cupom não Fiscal
- **Geração Automática**: Cupom não fiscal após cada venda
- **Informações Detalhadas**: Produtos, quantidades, preços e totais
- **Dados da Transação**: Data, hora, número da venda
- **Opção de Impressão**: Interface preparada para impressoras termicas não fiscal

### Módulo de Produtos

#### Gestão Completa
- **Cadastro Completo**: Produtos com validação de dados
- **Edição Dinâmica**: Atualização de produtos existentes
- **Exclusão Lógica**: Soft delete para manter histórico
- **Controle de Estoque**: Alertas de estoque baixo automatizados
- **Categorização**: Por categoria, marca e fornecedor

#### Busca e Filtragem
- **Busca Textual**: Pesquisa em múltiplos campos simultaneamente
- **Filtros Avançados**: Por categoria, status, estoque
- **Ordenação Inteligente**: Por diferentes critérios
- **Paginação**: Performance otimizada para grandes volumes

### Dashboard Executivo

#### Estatísticas em Tempo Real
- **Vendas do Dia**: Valor total e quantidade de vendas
- **Vendas do Mês**: Comparativo mensal
- **Ticket Médio**: Valor médio por venda
- **Produtos Cadastrados**: Contagem total de produtos ativos

### Histórico de Vendas

#### Consultas Avançadas
- **Filtro por Período**: Consulta por data específica
- **Filtro por Status**: Vendas finalizadas, canceladas, pendentes
- **Busca Detalhada**: Por número da venda, valor, etc.
- **Visualização Completa**: Todos os detalhes da transação

### Segurança e Controle

#### Autenticação e Autorização
- **Login Seguro**: Autenticação baseada em Spring Security
- **Controle de Acesso**: Diferentes níveis de permissão
- **Sessões Seguras**: Gerenciamento de sessão automático
- **Auditoria**: Log de todas as operações importantes

#### Validações de Negócio
- **Estoque Suficiente**: Verificação antes de adicionar itens
- **Valores Positivos**: Validação de quantidades e preços
- **Pagamento Completo**: Valor recebido ≥ valor total
- **Status Consistente**: Operações válidas para cada estado

---

## 🏗️ Tecnologias

### Backend

| Tecnologia | Versão | Descrição |
|------------|--------|-----------|
| **Java** | 17 | Linguagem de programação |
| **Spring Boot** | 3.5.0 | Framework principal |
| **Spring Data JPA** | 3.5.0 | Persistência de dados |
| **Spring Security** | 6.2.7 | Autenticação e autorização |
| **H2 Database** | 2.3.232 | Banco de dados embarcado |
| **Maven** | 3.6+ | Gerenciamento de dependências |

### Frontend

| Tecnologia | Versão | Descrição |
|------------|--------|-----------|
| **Vaadin Flow** | 24.8.3 | Framework de UI Java |
| **Lumo Theme** | 24.8.3 | Tema moderno e responsivo |
| **Vaadin Grid** | 24.8.3 | Tabelas avançadas |
| **CSS/HTML5** | - | Estilização e marcação |

### Ferramentas de Desenvolvimento

| Ferramenta | Versão | Descrição |
|------------|--------|-----------|
| **Spring Boot DevTools** | 3.5.0 | Hot reload em desenvolvimento |
| **Spring Boot Actuator** | 3.5.0 | Monitoramento da aplicação |
| **Jakarta Bean Validation** | 3.1.0 | Validação de dados |
| **SLF4J + Logback** | 2.0.16 | Sistema de logging |

---

## 🖼️ Screenshots

### Dashboard Principal
![Dashboard](docs/images/dashboard.png)
*Dashboard executivo com estatísticas em tempo real*

### Tela de PDV
![PDV](docs/images/pdv.png)
*Interface principal do ponto de venda com busca de produtos e carrinho*

### Gestão de Produtos
![Produtos](docs/images/produtos.png)
*Módulo completo de gestão de produtos com busca e filtros avançados*

### Histórico de Vendas
![Vendas](docs/images/vendas.png)
*Consulta detalhada do histórico de vendas com filtros por período*

---

## 🚀 Como Executar

### Pré-requisitos

Antes de começar, certifique-se de ter instalado:

- **Java 17+** ([OpenJDK](https://openjdk.org/projects/jdk/17/) ou [Oracle JDK](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html))
- **Maven 3.6+** ([Apache Maven](https://maven.apache.org/download.cgi))
- **IDE** (recomendado: [IntelliJ IDEA](https://www.jetbrains.com/idea/) ou [VS Code](https://code.visualstudio.com/))

### Clonando o Repositório

```bash
# Clone o repositório
git clone https://github.com/leandrosnazareth/vaadin-pdv.git

# Entre no diretório
cd vaadin-pdv
```

### Execução Rápida

```bash
# Compilar e executar
./mvnw spring-boot:run

# Ou no Windows
mvnw.cmd spring-boot:run
```

### Execução com Perfil de Desenvolvimento

```bash
# Executar com perfil de desenvolvimento (recomendado)
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Com hot reload ativado
./mvnw spring-boot:run -Dspring.devtools.restart.enabled=true
```

### Acessando a Aplicação

Após iniciar a aplicação, acesse:

- **Sistema PDV**: [http://localhost:8080](http://localhost:8080)
- **Console H2**: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
- **Actuator**: [http://localhost:8080/actuator](http://localhost:8080/actuator)

### Configuração do Banco H2

Para acessar o console H2 (desenvolvimento):

| Configuração | Valor |
|-------------|-------|
| **JDBC URL** | `jdbc:h2:file:./data/vaadin-pdv` |
| **User Name** | `sa` |
| **Password** | *(deixar em branco)* |

### Build para Produção

```bash
# Build otimizado para produção
./mvnw clean package -Pproduction

# Executar JAR gerado
java -jar target/vaadin-pdv-1.0-SNAPSHOT.jar
```

---

## 📖 Framework Vaadin

### O que é o Vaadin?

O **Vaadin** é um framework de desenvolvimento web moderno que permite criar aplicações ricas usando **apenas Java**. Fundado em 2006 na Finlândia, o Vaadin revolucionou o desenvolvimento web ao eliminar a necessidade de escrever JavaScript, HTML ou CSS para criar interfaces complexas.

### Principais Características

#### Desenvolvimento 100% Java
- **Sem JavaScript**: Toda a lógica no backend
- **Type Safety**: Detecção de erros em tempo de compilação
- **Refactoring Seguro**: IDE oferece suporte completo
- **Debugging Fácil**: Debug direto no código Java

#### Componentes Ricos
- **Biblioteca Extensa**: +40 componentes profissionais
- **Charts Avançados**: Gráficos interativos prontos
- **Grid Poderoso**: Tabelas com filtros, ordenação, paginação
- **Layouts Responsivos**: Adaptação automática a diferentes telas

#### Performance Otimizada
- **Lazy Loading**: Carregamento sob demanda
- **Bundle Otimizado**: Apenas código necessário no cliente
- **Server Push**: Atualizações em tempo real
- **Caching Inteligente**: Cache automático de recursos

### Versões do Vaadin

#### Versões Regulares (Feature Releases)

| Versão | Lançamento | Principais Novidades |
|--------|------------|---------------------|
| **24.8** | Nov 2024 | Melhorias de segurança e performance |
| **24.7** | Out 2024 | Novos componentes de formulário |
| **24.6** | Set 2024 | Integração aprimorada com Spring |
| **24.5** | Ago 2024 | Novos temas e componentes |
| **24.4** | Jul 2024 | Performance e acessibilidade |
| **24.3** | Jun 2024 | Recursos mobile aprimorados |
| **24.2** | Mai 2024 | Novos componentes de dados |
| **24.1** | Abr 2024 | Primeira versão da série 24 |

### Pontos Positivos do Vaadin

#### Produtividade
- **Desenvolvimento Rápido**: Prototipagem em minutos
- **Menos Código**: Redução de até 70% em linhas de código
- **Foco no Negócio**: Menos tempo com infraestrutura
- **Reaproveitamento**: Componentes reutilizáveis

#### Ideal para Empresas
- **Equipes Java**: Aproveita conhecimento existente
- **Segurança Robusta**: Integração com Spring Security
- **Escalabilidade**: Suporta aplicações complexas
- **Manutenibilidade**: Código limpo e organizado

#### Interface Moderna
- **Responsivo**: Mobile-first design
- **Acessibilidade**: WAI-ARIA compliant
- **Temas**: Light/Dark mode automático
- **Customização**: CSS e temas personalizados


### Limitações do Vaadin

#### Aspectos Comerciais
- **Custo Pro**: Licença comercial para recursos avançados
- **Foco Empresarial**: Menos adequado para sites públicos
- **Charts Pagos**: Gráficos avançados requerem licença

#### Casos de Uso
- **SEO Limitado**: Não ideal para sites com muito conteúdo
- **Apps Móveis**: Melhor para webapps que apps nativos
- **Interatividade Extrema**: Limitado para jogos/animações complexas

#### Aspectos Técnicos
- **Bundle Size**: Aplicações podem ser maiores
- **Conectividade**: Requer conexão estável com servidor
- **Learning Curve**: Paradigma diferente para devs frontend

### Por que Escolher Vaadin?

#### Para Desenvolvedores Java
- **Aproveitar Conhecimento**: Usar skills Java existentes
- **Type Safety**: Detecção de erros em tempo de compilação
- **Tooling**: Suporte completo da IDE
- **Testing**: Testes unitários e integração simples

#### Para Empresas
- **Time to Market**: Desenvolvimento 3x mais rápido
- **Manutenção**: Código mais limpo e organizado
- **Recursos**: Equipe Java pode fazer frontend
- **Segurança**: Integração nativa com frameworks Java

---

## 👨‍💻 Autor

<div align="center">

![Leandro Nazareth](https://github.com/leandrosnazareth.png?size=120)

**Leandro Nazareth**  
*Desenvolvedor Full Stack Java*

[![LinkedIn](https://img.shields.io/badge/LinkedIn-0077B5?style=for-the-badge&logo=linkedin&logoColor=white)](https://linkedin.com/in/leandrosnazareth)
[![GitHub](https://img.shields.io/badge/GitHub-100000?style=for-the-badge&logo=github&logoColor=white)](https://github.com/leandrosnazareth)
[![Email](https://img.shields.io/badge/Email-D14836?style=for-the-badge&logo=gmail&logoColor=white)](mailto:leandro@nazareth.dev)

</div>

---

## 🤝 Como Contribuir

Contribuições são sempre bem-vindas! Este projeto é uma excelente oportunidade para aprender Vaadin e contribuir para um projeto real.

### Fork do Projeto

1. **Fork** o repositório
2. **Clone** sua fork: `git clone https://github.com/seu-usuario/vaadin-pdv.git`
3. **Branch** para sua feature: `git checkout -b feature/nova-funcionalidade`
4. **Commit** suas mudanças: `git commit -m 'Adiciona nova funcionalidade'`
5. **Push** para a branch: `git push origin feature/nova-funcionalidade`
6. **Abra** um Pull Request

### Reportando Bugs

Encontrou um bug? Ajude-nos a melhorar:

1. **Verifique** se o bug já foi reportado
2. **Abra** uma [nova issue](https://github.com/leandrosnazareth/vaadin-pdv/issues)
3. **Descreva** o problema detalhadamente
4. **Inclua** passos para reproduzir
5. **Anexe** screenshots se necessário

### Sugerindo Melhorias

Tem uma ideia incrível? Compartilhe conosco:

1. **Abra** uma [nova issue](https://github.com/leandrosnazareth/vaadin-pdv/issues)
2. **Use** o label "enhancement"
3. **Descreva** sua sugestão
4. **Explique** o benefício para os usuários

### 📋 **Diretrizes de Contribuição**

#### Padrões de Código
- **Java Code Style**: Siga as convenções do projeto
- **Documentação**: Comente código complexo
- **Testes**: Inclua testes para novas funcionalidades
- **Segurança**: Valide entradas e saídas

#### Áreas de Contribuição
- **Novas Funcionalidades**: Relatórios, dashboards, integrações
- **Correção de Bugs**: Melhorias de estabilidade
- **Documentação**: Tutoriais, exemplos, melhorias no README
- **Interface**: Melhorias de UX/UI, temas, responsividade
- **Performance**: Otimizações, cache, consultas SQL
- **Testes**: Cobertura de testes, testes de integração

### Reconhecimento de Contribuidores

Todos os contribuidores serão reconhecidos:

- **Arquivo CONTRIBUTORS.md**: Lista de todos os colaboradores
- **GitHub Contributors**: Reconhecimento automático
- **Redes Sociais**: Divulgação de contribuições importantes
- **Agradecimentos**: Menção em releases e changelog

---

## ⭐ Mostre seu Apoio

Se este projeto te ajudou de alguma forma, mostre seu apoio:

### Dê uma Estrela!

Clique na ⭐ no topo desta página! Isso ajuda:

- **Divulgação**: Mais pessoas descobrem o projeto
- **Reconhecimento**: Valoriza o trabalho do desenvolvedor
- **Motivação**: Encoraja melhorias e atualizações
- **Metrics**: Mostra a utilidade do projeto

### Compartilhe

- **Twitter**: Tweet sobre o projeto
- **LinkedIn**: Compartilhe com sua rede profissional
- **Blog**: Escreva sobre sua experiência
- **Comunidades**: Divulgue em grupos de desenvolvedores

### Outras Formas de Apoio

- **Fork**: Crie sua própria versão
- **Issues**: Reporte bugs ou sugira melhorias
- **Pull Requests**: Contribua com código
- **Documentação**: Ajude a melhorar a documentação

### Por que seu Apoio Importa?

Este projeto foi criado para:

- **Educar**: Ensinar desenvolvimento com Vaadin
- **Inspirar**: Mostrar as capacidades do framework
- **Conectar**: Unir a comunidade de desenvolvedores Java
- **Inovar**: Demonstrar melhores práticas

**Sua estrela ⭐ faz toda a diferença!**

---

## 📄 Licença

Este projeto está licenciado sob a **MIT License** - veja o arquivo [LICENSE](LICENSE) para detalhes.

### Resumo da Licença MIT

**Permitido:**
- Uso comercial
- Modificação
- Distribuição
- Uso privado

**Condições:**
- Incluir licença e copyright
- Documentar mudanças significativas

**Limitações:**
- Sem garantia
- Sem responsabilidade do autor

### Para Uso Comercial

Este projeto pode ser usado livremente em projetos comerciais. Recomendamos:

1. **Ler** a licença completa
2. **Manter** os créditos do autor original
3. **Contribuir** de volta com melhorias (opcional, mas apreciado)

---

<div align="center">

## 🙏 Agradecimentos

Agradecimentos especiais a:

- **Vaadin Team**: Pelo excelente framework
- **Spring Team**: Pela integração perfeita
- **Comunidade Java**: Pelo suporte e feedback
- **GitHub**: Por hospedar este projeto

---

### Contato e Suporte

**Email**: [leandro@nazareth.dev](mailto:leandro@nazareth.dev)  
**LinkedIn**: [leandrosnazareth](https://linkedin.com/in/leandrosnazareth)  
**GitHub**: [leandrosnazareth](https://github.com/leandrosnazareth)

---

**Desenvolvido com ❤️ para a comunidade Java brasileira**

*Ajude a espalhar o conhecimento sobre Vaadin Framework!*

</div>
