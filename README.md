# Dimensionamento Elétrico Residencial

Aplicativo desktop desenvolvido em Java Swing para registrar cômodos e equipamentos especiais de uma residência e gerar uma estimativa do quadro de cargas e da divisão de circuitos.

## Funcionalidades

- Cadastro de cômodos com nome, área e perímetro.
- Importação de memoriais TXT exportados pelo programa, incluindo cômodos, equipamentos e configurações elétricas.
- Importação de cômodos a partir de arquivos CSV, com separador vírgula ou ponto e vírgula.
- Estimativa de cargas de tomadas de uso geral (TUG) e iluminação.
- Cadastro de equipamentos especiais (TUE), com cálculo de demanda conforme as tabelas implementadas no projeto.
- Geração do quadro de cargas com circuito, descrição, tipo, tensão, potência instalada, correntes de projeto e corrigida, ampacidade, disjuntor, seção do condutor, indicação de DR e fase.
- Configuração do sistema, tensão, método de instalação, FCA e FCT, individualmente por circuitos selecionados ou em lote.
- Exportação e impressão do memorial, incluindo cômodos, equipamentos, divisão dos circuitos, resumo e legenda.

## Tecnologias

- Java 8 ou superior
- Java Swing
- Maven
- IntelliJ IDEA GUI Designer (`.form`)

## Requisitos

- JDK instalado e configurado.
- Maven instalado e disponível no `PATH`, caso a compilação seja feita pelo terminal.

## Compilar e executar

Na raiz do projeto, execute:

```bash
mvn clean package
```

O JAR será gerado em `target/electric-1.0.0.jar`. Para iniciar:

```bash
java -jar target/electric-1.0.0.jar
```

Também é possível abrir o projeto no IntelliJ IDEA e executar a classe `Main`.

## Estrutura do projeto

```text
src/
├── Main.java
├── screen/
│   ├── mainScreen.java
│   ├── mainScreen.form
│   ├── equipamentosScreen.java
│   ├── equipamentosScreen.form
│   ├── circuitosScreen.java
│   ├── circuitosScreen.form
│   └── DarkScrollBarUI.java
└── servico/
    ├── comodos.java
    ├── demandaEquip.java
    ├── Importar.java
    ├── potenciaCorrigida.java
    └── QuadroCargasService.java
```

## Importar um projeto ou cômodos por CSV

Na primeira tela, clique em **Importar TXT/CSV**. Um memorial TXT exportado pelo programa restaura cômodos, equipamentos especiais, sistema, tensão e configurações FCA/FCT/método dos circuitos. Os arquivos TXT antigos também são aceitos; como eles não guardavam as configurações por circuito, esses valores serão reiniciados para o padrão.

Para importar apenas cômodos, selecione um CSV UTF-8. O formato recomendado usa cabeçalho e separador ponto e vírgula:

```csv
Tipo;Nome;Área (m²);Perímetro (m)
Sala;Estar;20,5;18
Quarto;Suíte principal;14;15,2
Cozinha;Principal;12;14
```

Os nomes das colunas podem ser omitidos se os dados estiverem na ordem `Tipo;Nome;Área;Perímetro`. Tipos aceitos: Sala, Quarto, Cozinha, Banheiro, Lavanderia, Área de Serviço, Suíte, Corredor, Varanda, Hall e Outros. Área e perímetro devem ser números não negativos. Os cômodos CSV são adicionados à lista atual; nomes repetidos recebem um sufixo numérico. Importar um memorial substitui os dados atuais do projeto.

## Observações sobre os cálculos

O dimensionamento é uma estimativa de apoio ao projeto. A ampacidade de referência implementada usa valores da Tabela 36 da NBR 5410:2004 para condutores de cobre com isolação PVC 70 °C. O método de instalação e os fatores FCA/FCT devem corresponder às condições reais; confirme as tabelas e a edição vigente da norma antes de utilizar os resultados.

O padrão de entrada da Coelba permanece indicado como pendente de confirmação dos limites oficiais aplicáveis. Os resultados não substituem projeto, verificação ou responsabilidade de profissional habilitado, nem inspeção das condições reais da instalação.
