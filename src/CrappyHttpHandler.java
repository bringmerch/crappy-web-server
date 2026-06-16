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
        StringBuffer response = new StringBuffer();
        response.append("exchange.getResponseCode() = ").append(exchange.getResponseCode()).append("\r\n");
        response.append("exchange.getRequestMethod() = ").append(exchange.getRequestMethod()).append("\r\n");
        response.append("exchange.getLocalAddress() = ").append(exchange.getLocalAddress()).append("\r\n");
        response.append("exchange.getRemoteAddress() = ").append(exchange.getRemoteAddress()).append("\r\n");
        response.append("exchange.getRequestURI() = ").append(exchange.getRequestURI()).append("\r\n");
        response.append("exchange.getProtocol() = ").append(exchange.getProtocol()).append("\r\n");
        response.append("exchange.getResponseHeaders() = ").append(exchange.getResponseHeaders()).append("\r\n");
        exchange.sendResponseHeaders(200, response.length()); // set Content-Length header, set status as 200
        OutputStream outputStream = exchange.getResponseBody();
        outputStream.write(String.valueOf(response).getBytes());
        outputStream.close();
        System.out.println("just responded...current time : " + Instant.now());
    }
}
