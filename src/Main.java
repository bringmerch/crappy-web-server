//import java.io.IOException;
//import java.io.OutputStream;
//import java.io.InputStream;
//import java.net.SocketAddress;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;


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

    private static final int BACKEND_PORT = 9999;
    private static final String BACKEND_HOST = "127.0.0.1";
    private static final String newLine = System.lineSeparator();

    public static void main(String[] args) {
        System.out.println("program started...");
        System.out.println("main thread name is : " + Thread.currentThread().getName());

        /// WAS 인척하고 9999에서 listen
        //todo:  WAS로 포워딩 테스트
        Thread zz_pretend_was = new Thread(() -> {
            try (ServerSocket zz_serverSocket = new ServerSocket(BACKEND_PORT)) { // 얘가 WAS라고 치고 9999에서 listen 한다.
                Socket zz_clientSocket = zz_serverSocket.accept();
                BufferedReader zz_bufferedReader = new BufferedReader(new InputStreamReader(zz_clientSocket.getInputStream(), StandardCharsets.UTF_8));
                List<String> allLines = zz_bufferedReader.lines().toList();
                System.out.println("zz_clientSocket이 다음 데이터를 읽음 : \r\n");
                allLines.forEach(System.out::println);
            } catch (IOException e) {
                System.out.println("serverSocket creation failed");
            }}
        );
        zz_pretend_was.start();

        try (ServerSocket serverSocket = new ServerSocket(8888)) { // serverSocket = 포트에서 연결 요청을 기다림
            while(!serverSocket.isClosed()) {
                System.out.println("listening started on port: " + serverSocket.getLocalPort() + "...");

                try (Socket clientSocket = serverSocket.accept()) { // accept() : 대기하다가 클라이언트 요청 수신 시 새로운 Socket 객체 반환
                    forwardRequest(clientSocket);
                    // 요청별로 new Thread 생성한다.
                    new Thread(() -> {
                        try {
                            forwardRequest(clientSocket);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }).start();
                } catch(IOException e) {
                    System.out.println("accept failed...");

                    if (serverSocket.isClosed()) {
                        System.out.println("serverSocket is closed...");
                        break;
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("ServerSocket creation failed: " + e.getMessage());
        }
    }
    public static void forwardRequest(Socket clientSocket) throws IOException {
        // todo: was 모듈에 요청 날리고 다시 가기
        // todo: ㄴ를 비동기로 하기 (event driven like nginx, io(X) nio(O))
        // todo: was 모듈에서 response 받으면 클라이언트에게 응답 전송
        // todo: clientSocket 닫기 (5초 내 요청없으면 닫히는지 확인/ was로부터 응답없으면 닫기) -> connection 헤더 같이 보삼
        // todo: request headedr status WAS에서 준 걸로 세팅
        // todo: path에 따라 WAS acceptor(?) 호출
        // todo: fileReader로 파일넘기기
        // todo: client socket 시간지나면 죽이기 (timeout) & Execution Service pool에서 꺼내는 걸로 바꾸기
        // todo: thread 여러개 돌고있을 때 디버깅 어케??
        // todo: 톰캣이 받을 수 있는 양식으로 요청 문자열 빌딩해서 web server -> was로 flush하기 !!

        System.out.println("i'm in forwardRequest(). and the main thread name is: " + Thread.currentThread().getName());

        try (
            Socket socket2Backend = new Socket(BACKEND_HOST, BACKEND_PORT);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket2Backend.getOutputStream(), StandardCharsets.UTF_8)) // was로 향한다
        ) {
            // request message 읽기.
            StringBuilder requestMessage = new StringBuilder();
            String requestLine;
            while ((requestLine = bufferedReader.readLine()) != null) {
                requestMessage.append(requestLine).append(newLine);
            }
            // web server(8888) -> was인척(9999) 요청보내기
            bufferedWriter.write(requestMessage.toString());
            System.out.println("http request Message from client : " + requestMessage);
            bufferedWriter.flush(); // web server(8888) -> was(인척)(9999) 송신

//            StringBuilder response = new StringBuilder()
//                .append("""
//                    <!DOCTYPE html>
//                        <html lang="en">
//                        <head>
//                            <meta charset="UTF-8">
//                            <title>Purple Background Example</title>
//                            <style>
//                                body {
//                                    background-color: purple;
//                                    color: white; /* Makes text easier to read on a dark background */
//                                    font-family: sans-serif;
//                                    padding: 20px;
//                                }
//                            </style>
//                        </head>
//                        <body>
//                            <h1>roger that.</h1>
//                        </body>
//                        </html>
//                """);
//
//            bufferedWriter.write("HTTP/1.1 200"); // response startline
//            bufferedWriter.newLine();
//            bufferedWriter.write("X-Content-Type-Options: nosniff");
//            bufferedWriter.newLine();
//            bufferedWriter.write("Content-Type: text/html; charset=UTF-8");
//            bufferedWriter.newLine();
//            bufferedWriter.write("Content-Length: " + response.toString().getBytes(StandardCharsets.UTF_8).length); // response body 길이
//            bufferedWriter.newLine();
//            bufferedWriter.write("Connection: close");
//            bufferedWriter.newLine();
//            bufferedWriter.write("Set-Cookie: name=lee");
//            bufferedWriter.newLine();
//            bufferedWriter.write("Set-Cookie: age=31");
//            bufferedWriter.newLine();
//            bufferedWriter.newLine();
//            bufferedWriter.write(String.valueOf(response)); // response body
//            bufferedWriter.flush(); // was한테 전송
        } catch (IOException e) {
            System.out.println("forwardRequest.getMessage() = " + e.getMessage());
        }
    }
}
