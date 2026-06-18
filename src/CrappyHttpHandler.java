import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;

/**
 *
 * Package Name:
 * File Name: CrappyHttpHandler
 * Description:
 * author: munke
 *
 * @version 1.0
 * @see CrappyHttpHandler
 * @since 2026-06-16
 * <p>
 * Modification Information
 * 수정일          수정자                    수정내용
 * --------- ------------------- -------------------------------
 * 2026-06-16        munke                   최초개정
 */
public class CrappyHttpHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        StringBuilder response = new StringBuilder();

        switch(method.toUpperCase()) {
            case "GET":
                processGetRequest();
                break;
            case "POST":
                processPostRequest();
                break;
            case "PUT":
                processPutRequest();
                break;
            case "PATCH":
                processPatchRequest();
                break;
            case "DELETE":
                processDeleteRequest();
                break;
            case "OPTION":
                processOptionRequest();
                break;
            case "HEAD":
                processHeadRequest();
                break;
            case "TRACE":
                processTraceRequest();
                break;
            default:
                break;
        }

        response
            .append("""
                <!DOCTYPE html>
                    <html lang="en">
                    <head>
                        <meta charset="UTF-8">
                        <title>Purple Background Example</title>
                        <style>
                            body {
                                background-color: purple;
                                color: white; /* Makes text easier to read on a dark background */
                                font-family: sans-serif;
                                padding: 20px;
                            }
                        </style>
                    </head>
                    <body>
                        <h1>roger that.</h1>
                    </body>
                    </html>
            """);
        exchange.getResponseHeaders().set("Content-Type", "text/html");
        exchange.getResponseHeaders().set("X-Content-Type-Options", "nosniff"); // 브라우저의 MIME 추측 금지
        exchange.sendResponseHeaders(200, response.length()); // set Content-Length header, set status as 200
        OutputStream outputStream = exchange.getResponseBody();
        outputStream.write(String.valueOf(response).getBytes());
        outputStream.close();
        System.out.println("just responded...current time : " + Instant.now());
    }

    public void processGetRequest() {
    }

    public void processPostRequest() {
    }

    public void processPutRequest() {
    }

    public void processPatchRequest() {
    }

    public void processDeleteRequest() {
    }

    public void processOptionRequest() {
    }

    public void processHeadRequest() {
    }

    public void processTraceRequest() {
    }
}
