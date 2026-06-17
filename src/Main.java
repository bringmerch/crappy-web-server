//import java.io.IOException;
//import java.io.OutputStream;
//import java.io.InputStream;
//import java.net.SocketAddress;
import java.net.InetSocketAddress;
import com.sun.net.httpserver.HttpServer;
//import com.sun.net.httpserver.HttpExchange;
//import com.sun.net.httpserver.HttpHandler; // 요청 오면 부를 콜백들을 가지고 있는 인터페이스


/**
 *
 * Package Name:
 * File Name: Main
 * Description:
 * author: munke
 *
 * @version 1.0
 * @see Main
 * @since 2026-06-15
 * <p>
 * Modification Information
 * 수정일          수정자                    수정내용
 * --------- ------------------- -------------------------------
 * 2026-06-15        munke                   최초개정
 */
public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println("program started...");
        CrappyHttpHandler crappyHttpHandler = new CrappyHttpHandler();
        HttpServer httpServer = HttpServer.create(new InetSocketAddress(8080), 0);
        httpServer.createContext("/api", crappyHttpHandler); // 핸들러랑 path랑 매핑
        httpServer.setExecutor(null);
        httpServer.start();
    }
}
