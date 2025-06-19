package sh.ome.itemex.web;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import sh.ome.itemex.Itemex;
import sh.ome.itemex.RAM.TopOrders;
import sh.ome.itemex.functions.sqliteDb;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Simple HTTP server exposing a very small REST API for Itemex.
 */
public class WebServer {
    private static HttpServer server;

    /**
     * Starts the HTTP server on the given port.
     */
    public static void startServer(int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new RootHandler());
        server.createContext("/health", new HealthHandler());
        server.createContext("/items", new ItemsHandler());
        server.createContext("/item/top", new TopHandler());
        server.createContext("/item/history", new HistoryHandler());
        server.createContext("/item/orders", new OrdersHandler());
        server.createContext("/stats", new StatsHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
    }

    /** Stop the server if it is running. */
    public static void stopServer() {
        if (server != null) {
            server.stop(0);
            server = null;
        }
    }

    private static class RootHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "Itemex Web API";
            exchange.sendResponseHeaders(200, response.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }

    private static class HealthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "{\"status\":\"ok\",\"version\":\"" + sh.ome.itemex.Itemex.version + "\"}";
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }

    /** Returns a list of all known item identifiers. */
    private static class ItemsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            List<String> items = new ArrayList<>(Itemex.getPlugin().mtop.keySet());
            String response = new Gson().toJson(items);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }

    /** Returns top buy/sell orders for a given item. */
    private static class TopHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Map<String, String> params = parseQuery(exchange.getRequestURI().getRawQuery());
            String id = params.get("id");
            if (id == null) {
                sendError(exchange, 400, "missing id parameter");
                return;
            }

            String item;
            try {
                item = new String(Base64.getUrlDecoder().decode(id), StandardCharsets.UTF_8);
            } catch (IllegalArgumentException e) {
                item = URLDecoder.decode(id, StandardCharsets.UTF_8.name());
            }

            TopOrders orders = Itemex.getPlugin().mtop.get(item);
            if (orders == null) {
                sendError(exchange, 404, "unknown item");
                return;
            }

            Map<String, Object> result = new HashMap<>();
            result.put("buy", orders.get_top_buy(Itemex.admin_function));
            result.put("sell", orders.get_top_sell(Itemex.admin_function));
            result.put("last_price", orders.get_last_trade_price());
            result.put("last_timestamp", orders.get_last_timestamp());

            String response = new Gson().toJson(result);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }

    /** Returns recent trade history for an item. */
    private static class HistoryHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Map<String, String> params = parseQuery(exchange.getRequestURI().getRawQuery());
            String id = params.get("id");
            if (id == null) {
                sendError(exchange, 400, "missing id parameter");
                return;
            }

            String item;
            try {
                item = new String(Base64.getUrlDecoder().decode(id), StandardCharsets.UTF_8);
            } catch (IllegalArgumentException e) {
                item = URLDecoder.decode(id, StandardCharsets.UTF_8.name());
            }

            int limit = parseInt(params.get("limit"), 4);
            String[] trades = sqliteDb.get_last_trades(item, Integer.toString(limit));
            String response = new Gson().toJson(trades);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }

    /** Returns open buy and sell orders for an item with pagination. */
    private static class OrdersHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Map<String, String> params = parseQuery(exchange.getRequestURI().getRawQuery());
            String id = params.get("id");
            if (id == null) {
                sendError(exchange, 400, "missing id parameter");
                return;
            }

            String item;
            try {
                item = new String(Base64.getUrlDecoder().decode(id), StandardCharsets.UTF_8);
            } catch (IllegalArgumentException e) {
                item = URLDecoder.decode(id, StandardCharsets.UTF_8.name());
            }

            int page = parseInt(params.get("page"), 0);
            int size = parseInt(params.get("size"), 20);
            if (page < 0) page = 0;
            if (size <= 0) size = 20;
            int offset = page * size;

            List<sqliteDb.OrderBuffer> buy = sqliteDb.getOrders("BUYORDERS", item, size, offset);
            List<sqliteDb.OrderBuffer> sell = sqliteDb.getOrders("SELLORDERS", item, size, offset);

            Map<String, Object> result = new HashMap<>();
            result.put("buy", buy);
            result.put("sell", sell);

            String response = new Gson().toJson(result);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }

    /** Returns basic server statistics. */
    private static class StatsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Map<String, Object> stats = new HashMap<>();
            stats.put("version", Itemex.version);
            stats.put("items_loaded", Itemex.getPlugin().mtop.size());
            stats.put("buy_orders", sqliteDb.countOrders("BUYORDERS"));
            stats.put("sell_orders", sqliteDb.countOrders("SELLORDERS"));

            String response = new Gson().toJson(stats);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }

    private static void sendError(HttpExchange exchange, int code, String message) throws IOException {
        String response = "{\"error\":\"" + message + "\"}";
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(code, response.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }

    private static int parseInt(String value, int defaultVal) {
        if (value == null) return defaultVal;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }

    private static Map<String, String> parseQuery(String query) throws IOException {
        Map<String, String> params = new HashMap<>();
        if (query == null || query.isEmpty()) return params;
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf('=');
            if (idx > 0) {
                String key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8.name());
                String value = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8.name());
                params.put(key, value);
            }
        }
        return params;
    }
}
