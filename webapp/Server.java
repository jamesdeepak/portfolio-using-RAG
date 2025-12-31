
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.File;
import java.nio.file.Files;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Server {

    private static final Map<String, String> KNOWLEDGE_BASE = new HashMap<>();

    static {
        // Initialize simple RAG Knowledge Base
        KNOWLEDGE_BASE.put("services",
                "I specialize in **Machine Learning**, **NLP/LLMs**, **Computer Vision**, and **Data Science**. I can build custom AI models.");
        KNOWLEDGE_BASE.put("rag",
                "Yes! I am an expert in building **Retrieval-Augmented Generation (RAG)** systems connecting your data to LLMs.");
        KNOWLEDGE_BASE.put("tech", "I work with **Java**, **Python**, **TensorFlow**, **LangChain**, and **React**.");
        KNOWLEDGE_BASE.put("contact", "You can reach me via email at **deepak@example.com** or on LinkedIn.");
    }

    public static void main(String[] args) throws IOException {
        int port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // Serve Static Files (Index, CSS, JS, Images)
        server.createContext("/", new StaticHandler());

        // API Endpoint for Chat
        server.createContext("/api/ask", new ChatHandler());

        server.setExecutor(null); // creates a default executor
        System.out.println("Server started on http://localhost:" + port);
        server.start();
    }

    static class StaticHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String uri = t.getRequestURI().getPath();
            if (uri.equals("/")) {
                uri = "/index.html";
            }

            File file = new File("." + uri).getCanonicalFile(); // Simple path traversal protection would be good in
                                                                // prod

            if (file.exists() && file.isFile()) {
                String mimeType = "text/plain";
                if (uri.endsWith(".html"))
                    mimeType = "text/html";
                else if (uri.endsWith(".css"))
                    mimeType = "text/css";
                else if (uri.endsWith(".js"))
                    mimeType = "application/javascript";
                else if (uri.endsWith(".jpg"))
                    mimeType = "image/jpeg";
                else if (uri.endsWith(".png"))
                    mimeType = "image/png";

                t.getResponseHeaders().set("Content-Type", mimeType);
                t.sendResponseHeaders(200, file.length());
                try (OutputStream os = t.getResponseBody()) {
                    Files.copy(file.toPath(), os);
                }
            } else {
                String response = "404 (Not Found)\n";
                t.sendResponseHeaders(404, response.length());
                try (OutputStream os = t.getResponseBody()) {
                    os.write(response.getBytes());
                }
            }
        }
    }

    static class ChatHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            if ("POST".equals(t.getRequestMethod())) {
                InputStream is = t.getRequestBody();
                String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);

                // Very simple JSON parsing
                // Expecting: {"question": "some text"}
                String question = "";
                if (body.contains("\"question\":")) {
                    int start = body.indexOf(":", body.indexOf("\"question\"")) + 1;
                    question = body.substring(start).replace("\"", "").replace("}", "").trim().toLowerCase();
                }

                // Strict RAG Logic
                String answer = "I am an AI assistant focused on Deepak's professional portfolio. I cannot answer unrelated questions.";

                // Normalize for matching
                String q = question.toLowerCase();

                if (q.contains("services") || q.contains("offer") || q.contains("do")) {
                    answer = KNOWLEDGE_BASE.get("services");
                } else if (q.contains("rag") || q.contains("retrieval")) {
                    answer = KNOWLEDGE_BASE.get("rag");
                } else if (q.contains("tech") || q.contains("stack") || q.contains("tools")) {
                    answer = KNOWLEDGE_BASE.get("tech");
                } else if (q.contains("contact") || q.contains("hire") || q.contains("email")) {
                    answer = KNOWLEDGE_BASE.get("contact");
                } else if (q.contains("hello") || q.contains("hi")) {
                    answer = "Hello! Please select a question below to learn more about Deepak.";
                }

                String jsonResponse = "{\"answer\": \"" + answer + "\"}";

                t.getResponseHeaders().set("Content-Type", "application/json");
                t.sendResponseHeaders(200, jsonResponse.length());
                try (OutputStream os = t.getResponseBody()) {
                    os.write(jsonResponse.getBytes());
                }
            } else {
                t.sendResponseHeaders(405, -1); // Method Not Allowed
            }
        }
    }
}
