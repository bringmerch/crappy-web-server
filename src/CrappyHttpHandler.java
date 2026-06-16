import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;

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
        String response = "umm..it worked.";
        exchange.sendResponseHeaders(200, response.length());
        OutputStream outputStream = exchange.getResponseBody();
        outputStream.write(response.getBytes());
        outputStream.close();
        System.out.println("just responded...");
    }
}
