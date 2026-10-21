package servico;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static servico.comodos.comodoILUVA;
import static servico.comodos.comodoTMDVA;
import static servico.comodos.comodoTipo;
import static servico.demandaEquip.demandaTab6;

public final class QuadroCargasService {
    private static final double FATOR_POTENCIA_ASSUMIDO = 1.0;
    private static final double MAX_CORRENTE_ILUMINACAO = 10.0;
    private static final double MAX_CORRENTE_TUG = 10.0;
    private static final double MAX_POTENCIA_ILUMINACAO = 1800.0;
    private static final double[] SECOES_CU_PVC_MM2 = {1.5, 2.5, 4.0, 6.0, 10.0};
    private static final String[] METODOS_REFERENCIA = {"A1", "A2", "B1", "B2", "C", "D"};
    // Tabela 36 anexada: [seção][2 ou 3 condutores carregados], cobre/PVC 70 °C.
    private static final double[][][] CORRENTES_TABELA_36 = {
            {{14.0, 13.5}, {19.5, 18.0}, {26.0, 24.0}, {34.0, 31.0}, {46.0, 42.0}},
            {{14.0, 13.0}, {18.5, 17.5}, {25.0, 23.0}, {32.0, 29.0}, {43.0, 39.0}},
            {{17.5, 15.5}, {24.0, 21.0}, {32.0, 28.0}, {41.0, 36.0}, {57.0, 50.0}},
            {{16.5, 15.0}, {23.0, 20.0}, {30.0, 27.0}, {38.0, 34.0}, {52.0, 46.0}},
            {{19.5, 17.5}, {27.0, 24.0}, {36.0, 32.0}, {46.0, 41.0}, {63.0, 57.0}},
            {{22.0, 18.0}, {29.0, 24.0}, {38.0, 31.0}, {47.0, 39.0}, {63.0, 52.0}}
    };

    private QuadroCargasService() {
    }

    public enum SistemaFases {
        MONOFASICO("Monofásico"),
        BIFASICO("Bifásico"),
        TRIFASICO("Trifásico");

        private final String descricao;

        SistemaFases(String descricao) {
            this.descricao = descricao;
        }

        @Override
        public String toString() {
            return descricao;
        }
    }

    public static final class ParametrosCircuito {
        private final String metodoInstalacao;
        private final int condutoresCarregados;
        private final double fca;
        private final double fct;

        public ParametrosCircuito(String metodoInstalacao, int condutoresCarregados, double fca, double fct) {
            if (!ehMetodoValido(metodoInstalacao)) {
                throw new IllegalArgumentException("Método de instalação inválido.");
            }
            if (condutoresCarregados != 2 && condutoresCarregados != 3) {
                throw new IllegalArgumentException("Condutores carregados deve ser 2 ou 3.");
            }
            validarFator("FCA", fca);
            validarFator("FCT", fct);
            this.metodoInstalacao = metodoInstalacao;
            this.condutoresCarregados = condutoresCarregados;
            this.fca = fca;
            this.fct = fct;
        }

        public String getMetodoInstalacao() {
            return metodoInstalacao;
        }

        public int getCondutoresCarregados() {
            return condutoresCarregados;
        }

        public double getFca() {
            return fca;
        }

        public double getFct() {
            return fct;
        }

        private static void validarFator(String nome, double valor) {
            if (!Double.isFinite(valor) || valor <= 0 || valor > 1) {
                throw new IllegalArgumentException(nome + " deve ser maior que 0 e no máximo 1,00.");
            }
        }
    }

    public static final class Circuito {
        private final String numero;
        private final String descricao;
        private final String tipo;
        private final int tensao;
        private final double potenciaInstalada;
        private final double potenciaDemanda;
        private final double correnteProjeto;
        private final double correnteCorrigida;
        private final double ampacidadeCondutor;
        private final int disjuntor;
        private final double secaoCondutor;
        private final boolean drObrigatorio;
        private final String fase;
        private final ParametrosCircuito parametros;

        private Circuito(String numero, String descricao, String tipo, int tensao,
                         double potenciaInstalada, double potenciaDemanda, double correnteProjeto,
                         double correnteCorrigida, double ampacidadeCondutor,
                         int disjuntor, double secaoCondutor, boolean drObrigatorio, String fase,
                         ParametrosCircuito parametros) {
            this.numero = numero;
            this.descricao = descricao;
            this.tipo = tipo;
            this.tensao = tensao;
            this.potenciaInstalada = potenciaInstalada;
            this.potenciaDemanda = potenciaDemanda;
            this.correnteProjeto = correnteProjeto;
            this.correnteCorrigida = correnteCorrigida;
            this.ampacidadeCondutor = ampacidadeCondutor;
            this.disjuntor = disjuntor;
            this.secaoCondutor = secaoCondutor;
            this.drObrigatorio = drObrigatorio;
            this.fase = fase;
            this.parametros = parametros;
        }

        public Object[] toTableRow() {
            return new Object[]{
                    numero, descricao, tipo, tensao,
                    Math.round(potenciaInstalada), formatar(correnteProjeto), formatar(correnteCorrigida),
                    ampacidadeCondutor == 0 ? "Insuficiente" : formatar(ampacidadeCondutor),
                    disjuntor == 0 ? "Insuficiente" : disjuntor,
                    secaoCondutor == 0 ? "Insuficiente" : formatar(secaoCondutor),
                    drObrigatorio ? "Sim" : "Não", fase
            };
        }

        private static String formatar(double valor) {
            return String.format(Locale.ROOT, "%.2f", valor);
        }

        public String getNumero() {
            return numero;
        }

        public String getChaveConfiguracao() {
            return tipo + "|" + descricao;
        }

        public double getPotenciaInstalada() {
            return potenciaInstalada;
        }

        public double getPotenciaDemanda() {
            return potenciaDemanda;
        }

        public ParametrosCircuito getParametros() {
            return parametros;
        }
    }

    public static final class Resultado {
        private final List<Circuito> circuitos;
        private final double potenciaInstalada;
        private final double demanda;
        private final String padraoEntrada;
        private final List<String> avisos;

        private Resultado(List<Circuito> circuitos, double potenciaInstalada, double demanda,
                          String padraoEntrada, List<String> avisos) {
            this.circuitos = circuitos;
            this.potenciaInstalada = potenciaInstalada;
            this.demanda = demanda;
            this.padraoEntrada = padraoEntrada;
            this.avisos = avisos;
        }

        public List<Circuito> getCircuitos() {
            return circuitos;
        }

        public double getPotenciaInstalada() {
            return potenciaInstalada;
        }

        public double getDemanda() {
            return demanda;
        }

        public String getPadraoEntrada() {
            return padraoEntrada;
        }

        public List<String> getAvisos() {
            return avisos;
        }
    }

    private static final class CircuitoRascunho {
        private final String descricao;
        private final String tipo;
        private final double potenciaInstalada;
        private final double potenciaDemanda;
        private final boolean drObrigatorio;

        private CircuitoRascunho(String descricao, String tipo, double potenciaInstalada,
                                 double potenciaDemanda, boolean drObrigatorio) {
            this.descricao = descricao;
            this.tipo = tipo;
            this.potenciaInstalada = potenciaInstalada;
            this.potenciaDemanda = potenciaDemanda;
            this.drObrigatorio = drObrigatorio;
        }
    }

    public static Resultado calcular(int tensao, SistemaFases sistema,
                                     List<demandaEquip.CargaEspecial> equipamentos,
                                     Map<String, ParametrosCircuito> parametrosPorCircuito) {
        if (tensao != 127 && tensao != 220) {
            throw new IllegalArgumentException("A tensão dos circuitos deve ser 127 V ou 220 V.");
        }
        if (sistema == null || equipamentos == null || parametrosPorCircuito == null) {
            throw new IllegalArgumentException("Sistema, equipamentos e parâmetros dos circuitos são obrigatórios.");
        }

        List<String> avisos = new ArrayList<>();
        List<CircuitoRascunho> circuitos = new ArrayList<>();
        Map<String, Double> tomadasEspeciais = new LinkedHashMap<>();
        Map<String, Double> tomadasComuns = new LinkedHashMap<>();
        double potenciaIluminacao = 0;

        List<String> comodos = new ArrayList<>(comodoTipo.keySet());
        comodos.sort(Comparator.naturalOrder());
        for (String comodo : comodos) {
            String tipo = comodoTipo.getOrDefault(comodo, "").toLowerCase(Locale.ROOT);
            double iluminacao = comodoILUVA.getOrDefault(comodo, 0);
            double tomada = comodoTMDVA.getOrDefault(comodo, 0);
            potenciaIluminacao += iluminacao;
            if (tomada > 0) {
                Map<String, Double> destino = ehAreaEspecial(tipo) ? tomadasEspeciais : tomadasComuns;
                destino.put(comodo, tomada);
            }
        }

        double limiteIluminacao = Math.min(MAX_POTENCIA_ILUMINACAO, tensao * MAX_CORRENTE_ILUMINACAO);
        dividirIluminacao(potenciaIluminacao, limiteIluminacao, circuitos);
        dividirTomadas(tomadasEspeciais, tensao, true, circuitos);
        dividirTomadas(tomadasComuns, tensao, false, circuitos);

        double potenciaTugIluminacao = circuitos.stream()
                .filter(c -> "Iluminação".equals(c.tipo) || "TUG".equals(c.tipo))
                .mapToDouble(c -> c.potenciaInstalada).sum();
        double demandaTugIluminacao = potenciaTugIluminacao * demandaTab6((int) Math.ceil(potenciaTugIluminacao));
        distribuirDemandaTugIluminacao(circuitos, potenciaTugIluminacao, demandaTugIluminacao);

        for (demandaEquip.CargaEspecial equipamento : equipamentos) {
            double instalada = equipamento.getPotenciaInstalada();
            double demanda = equipamento.getPotenciaCorrigida();
            circuitos.add(new CircuitoRascunho(
                    "TUE - " + equipamento.getNome(), "TUE", instalada, demanda,
                    exigeDr(equipamento.getNome())));
        }

        List<Circuito> resultadoCircuitos = new ArrayList<>();
        double[] potenciaPorFase = new double[3];
        double potenciaInstaladaTotal = 0;
        double demandaTotal = 0;
        int indice = 1;
        for (CircuitoRascunho rascunho : circuitos) {
            String numero = "C" + indice;
            String chave = rascunho.tipo + "|" + rascunho.descricao;
            ParametrosCircuito parametrosConfigurados = parametrosPorCircuito.getOrDefault(
                    chave, new ParametrosCircuito("B1", sistema == SistemaFases.TRIFASICO ? 3 : 2, 1.0, 1.0));
            ParametrosCircuito parametros = new ParametrosCircuito(
                    parametrosConfigurados.getMetodoInstalacao(),
                    sistema == SistemaFases.TRIFASICO ? 3 : 2,
                    parametrosConfigurados.getFca(), parametrosConfigurados.getFct());
            parametrosPorCircuito.put(chave, parametros);
            double correnteProjeto = rascunho.potenciaInstalada / (tensao * FATOR_POTENCIA_ASSUMIDO);
            double correnteCorrigida = correnteProjeto / (parametros.getFca() * parametros.getFct());
            Dimensionamento dimensionamento = dimensionarCondutor(
                    correnteProjeto, correnteCorrigida, rascunho.tipo, parametros);
            int disjuntor = dimensionamento.disjuntor;
            double secao = dimensionamento.secao;
            String fase = alocarFase(potenciaPorFase, rascunho.potenciaInstalada, tensao, sistema);
            if (disjuntor == 0) {
                avisos.add("A corrente calculada para " + rascunho.descricao
                        + " não pode ser protegida com as seções da Tabela 36 após aplicar FCA/FCT.");
            }
            resultadoCircuitos.add(new Circuito(numero, rascunho.descricao, rascunho.tipo,
                    tensao, rascunho.potenciaInstalada, rascunho.potenciaDemanda,
                    correnteProjeto, correnteCorrigida, dimensionamento.ampacidade,
                    disjuntor, secao, rascunho.drObrigatorio, fase, parametros));
            potenciaInstaladaTotal += rascunho.potenciaInstalada;
            demandaTotal += rascunho.potenciaDemanda;
            indice++;
        }

        String padraoEntrada = indicarPadraoEntrada();
        return new Resultado(resultadoCircuitos, potenciaInstaladaTotal, demandaTotal, padraoEntrada, avisos);
    }

    private static void dividirIluminacao(double potencia, double limite, List<CircuitoRascunho> circuitos) {
        if (potencia <= 0) {
            return;
        }
        double divisor = 1;
        double potenciaPorCircuito = potencia / divisor;

        while (potenciaPorCircuito > limite) {
            divisor++;
            potenciaPorCircuito = potencia / divisor;
        }

        int numero = 1;
        double restante = potencia;

        while (restante > 0.01) {
            double trecho = Math.min(restante, potenciaPorCircuito);

            circuitos.add(new CircuitoRascunho(
                    "Iluminação " + numero++,
                    "Iluminação",
                    trecho,
                    trecho,
                    false
            ));

            restante -= trecho;
        }
    }

    private static void dividirTomadas(Map<String, Double> cargas, int tensao, boolean especiais,
                                       List<CircuitoRascunho> circuitos) {
        double limite = tensao * MAX_CORRENTE_TUG;
        List<String> locais = new ArrayList<>(cargas.keySet());
        locais.sort(Comparator.naturalOrder());

        // CASO 1: Tomadas especiais (Trata cada local individualmente dividindo em partes iguais)
        if (especiais) {
            for (String local : locais) {
                double cargaLocal = cargas.get(local);
                if (cargaLocal <= 0) continue;

                // Calcula o divisor ideal para este local
                double divisor = 1;
                double cargaPorCircuito = cargaLocal / divisor;
                while (cargaPorCircuito > limite) {
                    divisor++;
                    cargaPorCircuito = cargaLocal / divisor;
                }

                // Cria os circuitos iguais para o local
                int numeroEspecial = 1;
                double restanteLocal = cargaLocal;
                while (restanteLocal > 0.01) {
                    double trecho = Math.min(restanteLocal, cargaPorCircuito);
                    circuitos.add(new CircuitoRascunho(
                            "TUG " + local + " " + numeroEspecial++,
                            "TUG", trecho, trecho, false));
                    restanteLocal -= trecho;
                }
            }
            return;
        }

        // CASO 2: Tomadas normais (Agrupadas e divididas igualmente)
        double cargaTotalGeral = 0;
        for (String local : locais) {
            cargaTotalGeral += cargas.get(local);
        }

        if (cargaTotalGeral <= 0) {
            return;
        }

        // Calcula o divisor ideal baseado na carga total de todos os cômodos
        double divisorGeral = 1;
        double cargaPorCircuitoGeral = cargaTotalGeral / divisorGeral;
        while (cargaPorCircuitoGeral > limite) {
            divisorGeral++;
            cargaPorCircuitoGeral = cargaTotalGeral / divisorGeral;
        }

        // Cria a lista com os nomes de todos os cômodos envolvidos para colocar no nome do circuito
        String descricaoLocais = String.join(", ", locais);

        int numeroGeral = 1;
        double restanteGeral = cargaTotalGeral;
        while (restanteGeral > 0.01) {
            double trecho = Math.min(restanteGeral, cargaPorCircuitoGeral);
            circuitos.add(new CircuitoRascunho(
                    "TUG demais cômodos " + numeroGeral++ + " (" + descricaoLocais + ")",
                    "TUG", trecho, trecho, false));
            restanteGeral -= trecho;
        }
    }


    private static void dividirCargaEmCircuitos(String descricao, double potencia, double limite,
                                                List<CircuitoRascunho> circuitos) {
        double restante = potencia;
        int numero = 1;
        while (restante > 0) {
            double trecho = Math.min(restante, limite);
            String descricaoCircuito = numero == 1 ? descricao : descricao + " (continuação " + numero + ")";
            circuitos.add(new CircuitoRascunho(descricaoCircuito, "TUG", trecho, trecho, true));
            restante -= trecho;
            numero++;
        }
    }

    private static void distribuirDemandaTugIluminacao(List<CircuitoRascunho> circuitos,
                                                        double potenciaTotal, double demandaTotal) {
        if (potenciaTotal <= 0) {
            return;
        }
        for (int i = 0; i < circuitos.size(); i++) {
            CircuitoRascunho circuito = circuitos.get(i);
            if ("Iluminação".equals(circuito.tipo) || "TUG".equals(circuito.tipo)) {
                double demanda = circuito.potenciaInstalada / potenciaTotal * demandaTotal;
                circuitos.set(i, new CircuitoRascunho(circuito.descricao, circuito.tipo,
                        circuito.potenciaInstalada, demanda, circuito.drObrigatorio));
            }
        }
    }

    private static boolean ehAreaEspecial(String tipo) {
        return tipo.contains("cozinha") || tipo.contains("copa") || tipo.contains("serviço")
                || tipo.contains("servico") || tipo.contains("lavanderia")
                || tipo.contains("banheiro") || tipo.contains("suíte") || tipo.contains("suite")
                || tipo.contains("varanda") || tipo.contains("garagem") || tipo.contains("extern");
    }

    private static boolean exigeDr(String nome) {
        String normalizado = nome.toLowerCase(Locale.ROOT);
        return normalizado.contains("chuveiro") || normalizado.contains("aquecedor")
                || normalizado.contains("torneira elétrica") || normalizado.contains("torneira eletrica")
                || normalizado.contains("hidromassagem") || normalizado.contains("bomba");
    }

    private static final class Dimensionamento {
        private final int disjuntor;
        private final double secao;
        private final double ampacidade;

        private Dimensionamento(int disjuntor, double secao, double ampacidade) {
            this.disjuntor = disjuntor;
            this.secao = secao;
            this.ampacidade = ampacidade;
        }
    }

    private static Dimensionamento dimensionarCondutor(double correnteProjeto, double correnteCorrigida, String tipo,
                                                       ParametrosCircuito parametros) {
        int[] disjuntores = "Iluminação".equals(tipo)
                ? new int[]{10, 16}
                : "TUG".equals(tipo) ? new int[]{16, 20} : new int[]{10, 16, 20, 25, 32, 40, 50};
        double secaoMinima = "Iluminação".equals(tipo) ? 1.5 : 2.5;
        int metodo = indiceMetodo(parametros.getMetodoInstalacao());
        int condutores = parametros.getCondutoresCarregados() - 2;

        for (int i = 0; i < SECOES_CU_PVC_MM2.length; i++) {
            double secao = SECOES_CU_PVC_MM2[i];
            if (secao < secaoMinima) {
                continue;
            }
            double ampacidade = CORRENTES_TABELA_36[metodo][i][condutores];
            double ampacidadeCorrigida = ampacidade * parametros.getFca() * parametros.getFct();
            if (correnteCorrigida > ampacidade) {
                continue;
            }
            for (int disjuntor : disjuntores) {
                if (correnteProjeto <= disjuntor && disjuntor <= ampacidadeCorrigida) {
                    return new Dimensionamento(disjuntor, secao, ampacidade);
                }
            }
        }
        return new Dimensionamento(0, 0, 0);
    }

    private static int indiceMetodo(String metodo) {
        for (int i = 0; i < METODOS_REFERENCIA.length; i++) {
            if (METODOS_REFERENCIA[i].equals(metodo)) {
                return i;
            }
        }
        throw new IllegalArgumentException("Método de instalação não suportado: " + metodo);
    }

    private static boolean ehMetodoValido(String metodo) {
        for (String metodoReferencia : METODOS_REFERENCIA) {
            if (metodoReferencia.equals(metodo)) {
                return true;
            }
        }
        return false;
    }

    private static String alocarFase(double[] potenciaPorFase, double potencia, int tensao,
                                     SistemaFases sistema) {
        String[] fases = {"R", "S", "T"};
        if (sistema == SistemaFases.MONOFASICO) {
            potenciaPorFase[0] += potencia;
            return tensao == 127 ? "R-N" : "R-N (220 V)";
        }
        if (tensao == 127) {
            int limiteFases = sistema == SistemaFases.BIFASICO ? 2 : 3;
            int fase = indiceMenorFase(potenciaPorFase, limiteFases);
            potenciaPorFase[fase] += potencia;
            return fases[fase] + "-N";
        }

        if (sistema == SistemaFases.BIFASICO) {
            potenciaPorFase[0] += potencia / 2.0;
            potenciaPorFase[1] += potencia / 2.0;
            return "R-S";
        }
        int primeira = indiceMenorFase(potenciaPorFase, 3);
        potenciaPorFase[primeira] += potencia / 2.0;
        int segunda = indiceMenorFase(potenciaPorFase, 3, primeira);
        potenciaPorFase[segunda] += potencia / 2.0;
        return fases[Math.min(primeira, segunda)] + "-" + fases[Math.max(primeira, segunda)];
    }

    private static int indiceMenorFase(double[] fases, int limite) {
        int menor = 0;
        for (int i = 1; i < limite; i++) {
            if (fases[i] < fases[menor]) {
                menor = i;
            }
        }
        return menor;
    }

    private static int indiceMenorFase(double[] fases, int limite, int ignorar) {
        int menor = ignorar == 0 ? 1 : 0;
        for (int i = 0; i < limite; i++) {
            if (i != ignorar && fases[i] < fases[menor]) {
                menor = i;
            }
        }
        return menor;
    }

    private static String indicarPadraoEntrada() {
        return "Pendente: confirmar limites DIS-NOR-030";
    }
}
