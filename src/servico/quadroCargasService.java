package servico;

import java.util.*;
import static servico.comodos.comodoILUVA;
import static servico.comodos.comodoTMDVA;
import static servico.comodos.comodoTipo;
import static servico.demandaEquip.demandaTab6;

public final class quadroCargasService {
    private static final double FATOR_POTENCIA_ASSUMIDO = 1.0;
    private static final double MAX_CORRENTE_ILUMINACAO = 10.0;
    private static final double MAX_CORRENTE_TUG = 10.0;
    private static final double MAX_POTENCIA_ILUMINACAO = 1800.0;
    // Tabela 36 da NBR 5410 / PDF raiz do projeto: cabo de cobre, PVC 70 °C, seções nominais até 1000 mm².
    private static final double[] SECOES_CU_PVC_MM2 = {0.5, 0.75, 1.0, 1.5, 2.5, 4.0, 6.0, 10.0, 16.0, 25.0, 35.0,
            50.0, 70.0, 95.0, 120.0, 150.0, 185.0, 240.0, 300.0, 400.0, 500.0, 630.0, 800.0, 1000.0};
    private static final String[] METODOS_REFERENCIA = {"A1", "A2", "B1", "B2", "C", "D"};
    //2 ou 3 condutores carregados, cobre/PVC 70 °C.
    private static final double[][][] CORRENTES_TABELA_36 = {
            {{7.0, 7.0}, {9.0, 9.0}, {11.0, 10.0}, {14.5, 13.5}, {19.5, 18.0}, {26.0, 24.0}, {34.0, 31.0}, {46.0, 42.0}, {61.0, 56.0}, {80.0, 73.0},
                    {99.0, 89.0}, {119.0, 108.0}, {151.0, 136.0}, {182.0, 164.0}, {210.0, 188.0}, {240.0, 216.0}, {273.0, 245.0}, {321.0, 286.0},
                    {367.0, 328.0}, {438.0, 390.0}, {502.0, 447.0}, {578.0, 514.0}, {669.0, 593.0}, {767.0, 679.0}},
            {{7.0, 7.0}, {9.0, 9.0}, {11.0, 10.0}, {14.0, 13.0}, {18.5, 17.5}, {25.0, 23.0}, {32.0, 29.0}, {43.0, 39.0}, {57.0, 52.0}, {75.0, 68.0},
                    {92.0, 83.0}, {110.0, 99.0}, {139.0, 125.0}, {167.0, 150.0}, {192.0, 172.0}, {219.0, 196.0}, {248.0, 223.0}, {291.0, 261.0},
                    {334.0, 298.0}, {398.0, 355.0}, {456.0, 406.0}, {526.0, 467.0}, {609.0, 540.0}, {698.0, 618.0}},
            {{9.0, 8.0}, {11.0, 10.0}, {14.0, 12.0}, {17.5, 15.5}, {24.0, 21.0}, {32.0, 28.0}, {41.0, 36.0}, {57.0, 50.0}, {76.0, 68.0}, {101.0, 89.0},
                    {125.0, 110.0}, {151.0, 134.0}, {192.0, 171.0}, {232.0, 207.0}, {269.0, 239.0}, {309.0, 275.0}, {353.0, 314.0}, {415.0, 370.0},
                    {477.0, 426.0}, {571.0, 510.0}, {656.0, 587.0}, {758.0, 678.0}, {881.0, 788.0}, {1012.0, 906.0}},
            {{9.0, 8.0}, {11.0, 10.0}, {13.0, 12.0}, {16.5, 15.0}, {23.0, 20.0}, {30.0, 27.0}, {38.0, 34.0}, {52.0, 46.0}, {69.0, 62.0}, {90.0, 80.0},
                    {111.0, 99.0}, {133.0, 118.0}, {168.0, 149.0}, {201.0, 179.0}, {232.0, 206.0}, {265.0, 236.0}, {300.0, 268.0}, {351.0, 313.0},
                    {401.0, 358.0}, {477.0, 425.0}, {545.0, 486.0}, {626.0, 559.0}, {723.0, 645.0}, {827.0, 738.0}},
            {{10.0, 9.0}, {13.0, 11.0}, {15.0, 14.0}, {19.5, 17.5}, {27.0, 24.0}, {36.0, 32.0}, {46.0, 41.0}, {63.0, 57.0}, {85.0, 76.0}, {112.0, 96.0},
                    {138.0, 119.0}, {168.0, 144.0}, {213.0, 184.0}, {258.0, 223.0}, {299.0, 259.0}, {344.0, 299.0}, {392.0, 341.0}, {461.0, 403.0},
                    {530.0, 464.0}, {634.0, 557.0}, {729.0, 642.0}, {843.0, 743.0}, {978.0, 865.0}, {1125.0, 996.0}},
            {{12.0, 10.0}, {15.0, 12.0}, {18.0, 15.0}, {22.0, 18.0}, {29.0, 24.0}, {38.0, 31.0}, {47.0, 39.0}, {63.0, 52.0}, {81.0, 67.0}, {104.0, 86.0},
                    {125.0, 103.0}, {148.0, 122.0}, {183.0, 151.0}, {216.0, 179.0}, {246.0, 203.0}, {278.0, 230.0}, {312.0, 258.0}, {361.0, 297.0},
                    {408.0, 336.0}, {478.0, 394.0}, {540.0, 445.0}, {614.0, 506.0}, {700.0, 577.0}, {792.0, 652.0}}
    };

    private quadroCargasService() {
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
                    correnteProjeto, correnteCorrigida, dimensionamento.corrente,
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

        int numero = 0;
        double restante = potencia;

        while (restante > 0.01) {
            double trecho = Math.min(restante, potenciaPorCircuito);

            circuitos.add(new CircuitoRascunho(
                    "Iluminação" + (numero != 0 ? " " + numero++: ""),
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

        if (especiais) {
            for (String local : locais) {
                double cargaLocal = cargas.get(local);
                if (cargaLocal <= 0) continue;

                double divisor = 1;
                double cargaPorCircuito = cargaLocal / divisor;
                while (cargaPorCircuito > limite) {
                    divisor++;
                    cargaPorCircuito = cargaLocal / divisor;
                }

                int numeroEspecial = 0;
                double restanteLocal = cargaLocal;
                while (restanteLocal > 0.01) {
                    double trecho = Math.min(restanteLocal, cargaPorCircuito);
                    circuitos.add(new CircuitoRascunho(
                            "TUG " + local + " " + (numeroEspecial == 0?" " + numeroEspecial++: ""),
                            "TUG", trecho, trecho, false));
                    restanteLocal -= trecho;
                }
            }
            return;
        }

        double cargaTotalGeral = 0;
        for (String local : locais) {
            cargaTotalGeral += cargas.get(local);
        }

        if (cargaTotalGeral <= 0) {
            return;
        }
        double divisorGeral = 1;
        double cargaPorCircuitoGeral = cargaTotalGeral / divisorGeral;
        while (cargaPorCircuitoGeral > limite) {
            divisorGeral++;
            cargaPorCircuitoGeral = cargaTotalGeral / divisorGeral;
        }

        int numeroGeral = 1;
        double restanteGeral = cargaTotalGeral;
        while (restanteGeral > 0.01) {
            double trecho = Math.min(restanteGeral, cargaPorCircuitoGeral);
            circuitos.add(new CircuitoRascunho(
                    "TUG demais cômodos " + (numeroGeral == 0? "" :" " + numeroGeral++),
                    "TUG", trecho, trecho, false));
            restanteGeral -= trecho;
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
        if (tipo == null) return false;
        String normalizado = tipo.toLowerCase(Locale.ROOT);

        Set<String> comodoEspecial = new HashSet<>(Arrays.asList(
                "cozinha", "copa", "serviço", "servico", "lavanderia", "banheiro", "suíte", "garagem"
        ));
        return comodoEspecial.stream().anyMatch(normalizado::contains);
    }

    private static boolean exigeDr(String nome) {
        if (nome == null) return false;
        String normalizado = nome.toLowerCase(Locale.ROOT);

        Set<String> nomes = new HashSet<>(Arrays.asList(
                "chuveiro", "aquecedor", "torneira elétrica", "hidromassagem", "bomba"
        ));
        return nomes.stream().anyMatch(normalizado::contains);
    }

    private static final class Dimensionamento {
        private final int disjuntor;
        private final double secao;
        private final double corrente;

        private Dimensionamento(int disjuntor, double secao, double corrente) {
            this.disjuntor = disjuntor;
            this.secao = secao;
            this.corrente = corrente;
        }
    }

    private static Dimensionamento dimensionarCondutor(double correnteProjeto, double correnteCorrigida, String tipo, ParametrosCircuito parametros) {

        int[] disjuntores = tipo.contains("Iluminação") ? new int[]{10, 16}
                : tipo.contains("Tug") ? new int[]{16, 20}
                : new int[]{10, 16, 20, 25, 32, 40, 50};

        double secaoMinima = tipo.contains("Iluminação") ? 1.5 : 2.5;
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
