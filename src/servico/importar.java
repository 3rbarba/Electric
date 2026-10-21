package servico;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import servico.QuadroCargasService.ParametrosCircuito;
import servico.QuadroCargasService.SistemaFases;

public final class Importar {
    private Importar() {
    }

    public static List<ComodoImportado> lerComodosCsv(Path arquivo, List<String> tiposValidos)
            throws IOException {
        List<String> linhas = Files.readAllLines(arquivo, StandardCharsets.UTF_8);
        List<ComodoImportado> importados = new ArrayList<>();
        Character separador = null;
        int indiceTipo = 0;
        int indiceNome = 1;
        int indiceArea = 2;
        int indicePerimetro = 3;
        boolean cabecalhoLido = false;

        for (int i = 0; i < linhas.size(); i++) {
            String linha = linhas.get(i);
            if (i == 0 && !linha.isEmpty() && linha.charAt(0) == '\uFEFF') {
                linha = linha.substring(1);
            }
            if (linha.trim().isEmpty() || linha.trim().startsWith("#")) {
                continue;
            }
            if (separador == null) {
                separador = detectarSeparador(linha);
            }
            List<String> colunas;
            try {
                colunas = separarCsv(linha, separador);
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("Linha " + (i + 1) + ": " + ex.getMessage());
            }

            if (!cabecalhoLido && ehCabecalhoCsv(colunas)) {
                int[] indices = indicesCabecalho(colunas);
                indiceTipo = indices[0];
                indiceNome = indices[1];
                indiceArea = indices[2];
                indicePerimetro = indices[3];
                cabecalhoLido = true;
                continue;
            }
            cabecalhoLido = true;

            int maxIndice = Math.max(indiceTipo, Math.max(indiceArea, indicePerimetro));
            if (indiceNome >= 0) {
                maxIndice = Math.max(maxIndice, indiceNome);
            }
            if (colunas.size() <= maxIndice) {
                throw new IllegalArgumentException("Linha " + (i + 1)
                        + ": colunas insuficientes. Use Tipo;Nome;Área;Perímetro.");
            }

            String tipo = encontrarTipo(colunas.get(indiceTipo), tiposValidos);
            String nome = indiceNome < 0 ? "" : colunas.get(indiceNome).trim();
            double area = lerMedida(colunas.get(indiceArea), "área", i + 1);
            double perimetro = lerMedida(colunas.get(indicePerimetro), "perímetro", i + 1);
            importados.add(new ComodoImportado(tipo, nome, area, perimetro));
        }
        return importados;
    }

    public static ProjetoImportado lerProjeto(Path arquivo, List<String> tiposValidos) throws IOException {
        List<String> linhas = Files.readAllLines(arquivo, StandardCharsets.UTF_8);
        if (!linhas.isEmpty() && !linhas.get(0).isEmpty() && linhas.get(0).charAt(0) == '\uFEFF') {
            linhas.set(0, linhas.get(0).substring(1));
        }
        for (int i = 0; i < linhas.size(); i++) {
            if ("# DADOS_ELETRIC;1".equals(linhas.get(i).trim())) {
                return lerProjetoEstruturado(linhas, tiposValidos, i);
            }
        }
        if (!linhas.isEmpty() && linhas.get(0).startsWith("MEMORIAL - QUADRO DE CARGAS")) {
            return lerMemorialAntigo(linhas, tiposValidos);
        }
        return new ProjetoImportado(lerComodosCsv(arquivo, tiposValidos),
                Collections.<EquipamentoImportado>emptyList(),
                Collections.<String, ParametrosCircuito>emptyMap(),
                SistemaFases.MONOFASICO, 127, false, false);
    }

    private static ProjetoImportado lerProjetoEstruturado(List<String> linhas, List<String> tiposValidos,
                                                           int marcador) {
        List<ComodoImportado> comodos = new ArrayList<>();
        List<EquipamentoImportado> equipamentos = new ArrayList<>();
        Map<String, ParametrosCircuito> parametros = new LinkedHashMap<>();
        SistemaFases sistema = SistemaFases.MONOFASICO;
        int tensao = 127;
        boolean encontrouSistema = false;
        boolean encontrouFim = false;

        for (int i = marcador + 1; i < linhas.size(); i++) {
            String linha = linhas.get(i);
            if ("# FIM_DADOS_ELETRIC".equals(linha.trim())) {
                encontrouFim = true;
                break;
            }
            if (linha.trim().isEmpty()) {
                continue;
            }
            String[] campos = linha.split(";", -1);
            try {
                switch (campos[0]) {
                    case "SISTEMA":
                        exigirCampos(campos, 3);
                        if (encontrouSistema) {
                            throw new IllegalArgumentException("Registro SISTEMA duplicado.");
                        }
                        sistema = SistemaFases.valueOf(campos[1]);
                        tensao = Integer.parseInt(campos[2]);
                        if (tensao != 127 && tensao != 220) {
                            throw new IllegalArgumentException("Tensão não suportada: " + tensao);
                        }
                        encontrouSistema = true;
                        break;
                    case "COMODO":
                        exigirCampos(campos, 5);
                        String nome = decodificar(campos[1]);
                        String tipo = encontrarTipo(decodificar(campos[2]), tiposValidos);
                        comodos.add(new ComodoImportado(tipo, nome,
                                Double.parseDouble(campos[3]), Double.parseDouble(campos[4])));
                        break;
                    case "EQUIPAMENTO":
                        exigirCampos(campos, 4);
                        equipamentos.add(new EquipamentoImportado(
                                Integer.parseInt(campos[1]), decodificar(campos[2]),
                                Long.parseLong(campos[3])));
                        break;
                    case "CIRCUITO":
                        exigirCampos(campos, 6);
                        parametros.put(decodificar(campos[1]),
                                new ParametrosCircuito(campos[2], Integer.parseInt(campos[3]),
                                        Double.parseDouble(campos[4]), Double.parseDouble(campos[5])));
                        break;
                    default:
                        throw new IllegalArgumentException("Registro desconhecido: " + campos[0]);
                }
            } catch (RuntimeException ex) {
                throw new IllegalArgumentException("Dados de importação inválidos na linha " + (i + 1)
                        + ": " + ex.getMessage(), ex);
            }
        }
        if (!encontrouFim || comodos.isEmpty() || !encontrouSistema) {
            throw new IllegalArgumentException("O memorial não contém um bloco de importação completo.");
        }
        return new ProjetoImportado(comodos, equipamentos, parametros, sistema, tensao, true, false);
    }

    private static ProjetoImportado lerMemorialAntigo(List<String> linhas, List<String> tiposValidos) {
        List<ComodoImportado> comodos = new ArrayList<>();
        List<EquipamentoImportado> equipamentos = new ArrayList<>();
        SistemaFases sistema = SistemaFases.MONOFASICO;
        int tensao = 127;
        boolean lendoComodos = false;
        boolean lendoEquipamentos = false;

        for (String linha : linhas) {
            if (linha.startsWith("Sistema: ")) {
                String configuracao = linha.substring("Sistema: ".length());
                if (configuracao.startsWith("Bifásico")) sistema = SistemaFases.BIFASICO;
                else if (configuracao.startsWith("Trifásico")) sistema = SistemaFases.TRIFASICO;
                String tensaoMarcador = "Tensão nominal: ";
                int inicioTensao = configuracao.indexOf(tensaoMarcador);
                if (inicioTensao >= 0) {
                    int inicioNumero = inicioTensao + tensaoMarcador.length();
                    int fimNumero = configuracao.indexOf(' ', inicioNumero);
                    if (fimNumero > inicioNumero) {
                        tensao = Integer.parseInt(configuracao.substring(inicioNumero, fimNumero));
                    }
                }
            }
            if (linha.equals("CÔMODOS, ÁREAS E CARGAS")) {
                lendoComodos = true;
                lendoEquipamentos = false;
                continue;
            }
            if (linha.equals("EQUIPAMENTOS ESPECIAIS (TUE)")) {
                lendoComodos = false;
                lendoEquipamentos = true;
                continue;
            }
            if (linha.startsWith("TOTAL ")) {
                lendoComodos = false;
            }
            if (linha.trim().isEmpty()) {
                if (lendoEquipamentos) lendoEquipamentos = false;
                continue;
            }

            String[] campos = linha.split("\\t", -1);
            if (lendoComodos && campos.length >= 6 && !campos[0].equals("Cômodo")) {
                try {
                    comodos.add(new ComodoImportado(encontrarTipo(campos[1], tiposValidos),
                            campos[0], lerNumeroMemorial(campos[2]), lerNumeroMemorial(campos[3])));
                } catch (RuntimeException ex) {
                    throw new IllegalArgumentException("Não foi possível ler um cômodo do memorial antigo: "
                            + linha, ex);
                }
            } else if (lendoEquipamentos && campos.length >= 2
                    && !campos[0].equals("Equipamento")) {
                try {
                    int tabela = inferirTabelaEquipamento(campos[0]);
                    equipamentos.add(new EquipamentoImportado(
                            tabela, campos[0], Long.parseLong(campos[1])));
                } catch (RuntimeException ex) {
                    throw new IllegalArgumentException("Não foi possível identificar o equipamento no memorial: "
                            + linha, ex);
                }
            }
        }
        if (comodos.isEmpty()) {
            throw new IllegalArgumentException("Não encontrei cômodos no memorial TXT.");
        }
        if (tensao != 127 && tensao != 220) {
            throw new IllegalArgumentException("Tensão não suportada no memorial: " + tensao);
        }
        return new ProjetoImportado(comodos, equipamentos,
                Collections.<String, ParametrosCircuito>emptyMap(), sistema, tensao, true, true);
    }

    private static int inferirTabelaEquipamento(String nome) {
        String normalizado = normalizar(nome);
        if (normalizado.startsWith("chuveiro") || normalizado.startsWith("aquecedoresdeagua")
                || normalizado.startsWith("ferroeletrico") || normalizado.startsWith("torneiraeletrica")) return 7;
        if (normalizado.startsWith("aquecedorcentral")) return 8;
        if (normalizado.startsWith("secadoraderoupa") || normalizado.startsWith("fornoeletrico")
                || normalizado.startsWith("lavaroupas") || normalizado.startsWith("lavaloucas")
                || normalizado.startsWith("microondas")) return 9;
        if (normalizado.startsWith("fogoeseletricos")) return 10;
        if (normalizado.startsWith("arcondicionado")) return 11;
        if (normalizado.startsWith("bombas") || normalizado.startsWith("hidromassagem")) return 16;
        throw new IllegalArgumentException("Tipo de equipamento não reconhecido: " + nome);
    }

    private static double lerNumeroMemorial(String valor) {
        return Double.parseDouble(valor.trim().replace(',', '.'));
    }

    private static String decodificar(String valor) {
        return new String(Base64.getDecoder().decode(valor), StandardCharsets.UTF_8);
    }

    private static void exigirCampos(String[] campos, int quantidade) {
        if (campos.length != quantidade) {
            throw new IllegalArgumentException("Quantidade de campos inválida para " + campos[0] + ".");
        }
    }

    public static final class ProjetoImportado {
        private final List<ComodoImportado> comodos;
        private final List<EquipamentoImportado> equipamentos;
        private final Map<String, ParametrosCircuito> parametrosPorCircuito;
        private final SistemaFases sistema;
        private final int tensao;
        private final boolean memorial;
        private final boolean formatoAntigo;

        private ProjetoImportado(List<ComodoImportado> comodos, List<EquipamentoImportado> equipamentos,
                                 Map<String, ParametrosCircuito> parametrosPorCircuito,
                                 SistemaFases sistema, int tensao, boolean memorial, boolean formatoAntigo) {
            this.comodos = Collections.unmodifiableList(new ArrayList<>(comodos));
            this.equipamentos = Collections.unmodifiableList(new ArrayList<>(equipamentos));
            this.parametrosPorCircuito = Collections.unmodifiableMap(
                    new LinkedHashMap<>(parametrosPorCircuito));
            this.sistema = sistema;
            this.tensao = tensao;
            this.memorial = memorial;
            this.formatoAntigo = formatoAntigo;
        }

        public List<ComodoImportado> getComodos() {
            return comodos;
        }

        public List<EquipamentoImportado> getEquipamentos() {
            return equipamentos;
        }

        public Map<String, ParametrosCircuito> getParametrosPorCircuito() {
            return parametrosPorCircuito;
        }

        public SistemaFases getSistema() {
            return sistema;
        }

        public int getTensao() {
            return tensao;
        }

        public boolean isMemorial() {
            return memorial;
        }

        public boolean isFormatoAntigo() {
            return formatoAntigo;
        }
    }

    public static final class EquipamentoImportado {
        private final int tabela;
        private final String nome;
        private final long potenciaInstalada;

        private EquipamentoImportado(int tabela, String nome, long potenciaInstalada) {
            if (tabela != 7 && tabela != 8 && tabela != 9 && tabela != 10 && tabela != 11 && tabela != 16) {
                throw new IllegalArgumentException("Tabela de equipamento inválida: " + tabela);
            }
            if (potenciaInstalada <= 0) {
                throw new IllegalArgumentException("Potência do equipamento deve ser maior que zero.");
            }
            if (nome == null || nome.trim().isEmpty()) {
                throw new IllegalArgumentException("O nome do equipamento não pode ficar vazio.");
            }
            this.tabela = tabela;
            this.nome = nome;
            this.potenciaInstalada = potenciaInstalada;
        }

        public int getTabela() {
            return tabela;
        }

        public String getNome() {
            return nome;
        }

        public long getPotenciaInstalada() {
            return potenciaInstalada;
        }
    }

    private static Character detectarSeparador(String linha) {
        int pontoEVirgula = contarSeparadores(linha, ';');
        int virgulas = contarSeparadores(linha, ',');
        if (pontoEVirgula == 0 && virgulas == 0) {
            throw new IllegalArgumentException("Não encontrei separador CSV. Use ponto e vírgula ou vírgula.");
        }
        return pontoEVirgula >= virgulas ? ';' : ',';
    }

    private static int contarSeparadores(String linha, char separador) {
        boolean entreAspas = false;
        int quantidade = 0;
        for (int i = 0; i < linha.length(); i++) {
            char atual = linha.charAt(i);
            if (atual == '"') {
                if (entreAspas && i + 1 < linha.length() && linha.charAt(i + 1) == '"') {
                    i++;
                } else {
                    entreAspas = !entreAspas;
                }
            } else if (atual == separador && !entreAspas) {
                quantidade++;
            }
        }
        return quantidade;
    }

    private static List<String> separarCsv(String linha, char separador) {
        List<String> colunas = new ArrayList<>();
        StringBuilder valor = new StringBuilder();
        boolean entreAspas = false;
        for (int i = 0; i < linha.length(); i++) {
            char atual = linha.charAt(i);
            if (atual == '"') {
                if (entreAspas && i + 1 < linha.length() && linha.charAt(i + 1) == '"') {
                    valor.append('"');
                    i++;
                } else {
                    entreAspas = !entreAspas;
                }
            } else if (atual == separador && !entreAspas) {
                colunas.add(valor.toString().trim());
                valor.setLength(0);
            } else {
                valor.append(atual);
            }
        }
        if (entreAspas) {
            throw new IllegalArgumentException("aspas não foram fechadas.");
        }
        colunas.add(valor.toString().trim());
        return colunas;
    }

    private static boolean ehCabecalhoCsv(List<String> colunas) {
        boolean temTipo = false;
        boolean temArea = false;
        boolean temPerimetro = false;
        for (String coluna : colunas) {
            String nome = normalizar(coluna);
            temTipo |= nome.equals("tipo") || nome.equals("tipocomodo") || nome.equals("comodo");
            temArea |= nome.startsWith("area");
            temPerimetro |= nome.startsWith("perimetro");
        }
        return temTipo && temArea && temPerimetro;
    }

    private static int[] indicesCabecalho(List<String> colunas) {
        int tipo = -1;
        int nome = -1;
        int area = -1;
        int perimetro = -1;
        for (int i = 0; i < colunas.size(); i++) {
            String cabecalho = normalizar(colunas.get(i));
            if (cabecalho.equals("tipo") || cabecalho.equals("tipocomodo") || cabecalho.equals("comodo")) {
                tipo = i;
            } else if (cabecalho.equals("nome") || cabecalho.equals("descricao")) {
                nome = i;
            } else if (cabecalho.startsWith("area")) {
                area = i;
            } else if (cabecalho.startsWith("perimetro")) {
                perimetro = i;
            }
        }
        if (tipo < 0 || area < 0 || perimetro < 0) {
            throw new IllegalArgumentException("Cabeçalho inválido. Colunas necessárias: Tipo, Área e Perímetro.");
        }
        return new int[]{tipo, nome, area, perimetro};
    }

    private static String encontrarTipo(String valor, List<String> tiposValidos) {
        String tipoNormalizado = normalizar(valor);
        for (String tipo : tiposValidos) {
            if (normalizar(tipo).equals(tipoNormalizado)) {
                return tipo;
            }
        }
        throw new IllegalArgumentException("Tipo de cômodo inválido: " + valor
                + ". Tipos aceitos: Sala, Quarto, Cozinha, Banheiro, Lavanderia, "
                + "Área de Serviço, Suíte, Corredor, Varanda, Hall e Outros.");
    }

    private static double lerMedida(String valor, String campo, int linha) {
        try {
            double medida = Double.parseDouble(valor.trim().replace(',', '.'));
            if (!Double.isFinite(medida) || medida < 0) {
                throw new NumberFormatException();
            }
            return medida;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Linha " + linha + ": " + campo
                    + " inválido. Informe um número maior ou igual a zero.");
        }
    }

    private static String normalizar(String texto) {
        return Normalizer.normalize(texto.trim().toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^a-z0-9]", "");
    }

    public static final class ComodoImportado {
        private final String tipo;
        private final String nome;
        private final double area;
        private final double perimetro;

        private ComodoImportado(String tipo, String nome, double area, double perimetro) {
            if (tipo == null || nome == null || !Double.isFinite(area) || area < 0
                    || !Double.isFinite(perimetro) || perimetro < 0) {
                throw new IllegalArgumentException("Dados inválidos para o cômodo.");
            }
            this.tipo = tipo;
            this.nome = nome;
            this.area = area;
            this.perimetro = perimetro;
        }

        public String getTipo() {
            return tipo;
        }

        public String getNome() {
            return nome;
        }

        public double getArea() {
            return area;
        }

        public double getPerimetro() {
            return perimetro;
        }
    }
}
