package servico;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;

public final class AutoCadApiServer implements AutoCloseable {
    public static final int PORT = 47651;
    private static final String PATH = "/api/autocad/measurement";
    private static final int MAX_REQUEST_BYTES = 4096;

    private final BiConsumer<Double, Double> measurementHandler;
    private HttpServer server;
    private ExecutorService executor;

    public AutoCadApiServer(BiConsumer<Double, Double> measurementHandler) {
        if (measurementHandler == null) {
            throw new IllegalArgumentException("O manipulador das medidas é obrigatório.");
        }
        this.measurementHandler = measurementHandler;
    }

    public void start() throws IOException {
        if (server != null) {
            return;
        }
        HttpServer newServer = HttpServer.create(
                new InetSocketAddress(InetAddress.getByName("127.0.0.1"), PORT), 0);
        newServer.createContext(PATH, this::handleMeasurement);
        ExecutorService newExecutor = Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(runnable, "autocad-api");
            thread.setDaemon(true);
            return thread;
        });
        newServer.setExecutor(newExecutor);
        try {
            newServer.start();
        } catch (RuntimeException ex) {
            newServer.stop(0);
            newExecutor.shutdownNow();
            throw ex;
        }
        server = newServer;
        executor = newExecutor;
    }

    private void handleMeasurement(HttpExchange exchange) throws IOException {
        try {
            HttpExchange request = exchange;
            if (!"POST".equals(request.getRequestMethod())) {
                request.getResponseHeaders().set("Allow", "POST");
                responder(request, 405, "Use POST para enviar uma medição.");
                return;
            }
            if (!PATH.equals(request.getRequestURI().getPath())) {
                responder(request, 404, "Endpoint não encontrado.");
                return;
            }

            Map<String, String> parameters;
            try {
                parameters = lerParametros(request);
                double area = lerMedida(parameters, "area");
                double perimeter = lerMedida(parameters, "perimetro");
                measurementHandler.accept(area, perimeter);
            } catch (IllegalArgumentException ex) {
                responder(request, 400, ex.getMessage());
                return;
            }
            responder(request, 200, "Medição recebida pelo ELETRIC.");
        } finally {
            exchange.close();
        }
    }

    private static Map<String, String> lerParametros(HttpExchange request) throws IOException {
        ByteArrayOutputStream body = new ByteArrayOutputStream();
        byte[] buffer = new byte[512];
        int total = 0;
        int read;
        while ((read = request.getRequestBody().read(buffer)) != -1) {
            total += read;
            if (total > MAX_REQUEST_BYTES) {
                throw new IllegalArgumentException("A solicitação excede o tamanho permitido.");
            }
            body.write(buffer, 0, read);
        }

        String encoded = new String(body.toByteArray(), StandardCharsets.UTF_8);
        Map<String, String> parameters = new HashMap<>();
        for (String pair : encoded.split("&")) {
            String[] parts = pair.split("=", 2);
            if (parts.length != 2) {
                throw new IllegalArgumentException("Formato de solicitação inválido.");
            }
            String key = URLDecoder.decode(parts[0], "UTF-8");
            String value = URLDecoder.decode(parts[1], "UTF-8");
            if (parameters.put(key, value) != null) {
                throw new IllegalArgumentException("Parâmetro repetido: " + key + ".");
            }
        }
        if (parameters.size() != 2 || !parameters.containsKey("area")
                || !parameters.containsKey("perimetro")) {
            throw new IllegalArgumentException("Informe os parâmetros area e perimetro.");
        }
        return parameters;
    }

    private static double lerMedida(Map<String, String> parameters, String name) {
        try {
            double value = Double.parseDouble(parameters.get(name));
            if (!Double.isFinite(value) || value < 0) {
                throw new NumberFormatException();
            }
            return value;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Valor inválido para " + name
                    + ": informe um número finito maior ou igual a zero.");
        }
    }

    private static void responder(HttpExchange exchange, int status, String message)
            throws IOException {
        byte[] response = message.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
        exchange.sendResponseHeaders(status, response.length);
        exchange.getResponseBody().write(response);
    }

    @Override
    public void close() {
        if (server != null) {
            server.stop(0);
            server = null;
        }
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }
    }
}
