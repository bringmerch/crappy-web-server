
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
/**
 *
 * Package Name:
 * File Name: CrappyWebServer
 * Description:
 * author: munke
 *
 * @version 1.0
 * @since 2026-06-15
 * <p>
 * Modification Information
 * 수정일          수정자                    수정내용
 * --------- ------------------- -------------------------------
 * 2026-06-15        munke                   최초개정
 */
public class CrappyWebServer {
    // todo: nio 기반으로 변경 (이벤트루프 기반)
    // todo: request headedr status WAS에서 준 걸로 세팅
    // todo: path에 따라 WAS acceptor(?) 호출 - 서블릿컨테이너가 아니라 nginx가 도메인에 따라 넘기는거
    // todo: fileReader로 파일넘기기
    // todo: client socket 시간지나면 죽이기 (timeout) & Execution Service pool에서 꺼내는 걸로 바꾸기
    // todo: backend에서 8888로부터 오는 것만 받기 (inbound 제한)
    // todo: connection keep-alive에 따라 클라이언트 소켓 close() 안 하고 듣고 있기 & 타임아웃
    // todo: chunked encoding 받기
    // todo: client close 안하고 connection: keep-alive 고려, 5초간 더 연결 지속 (현재 handle 끝나면 clientSocket close하고 있음)

    private static final int BACKEND_PORT = 9999;
    private static final String BACKEND_HOST = "127.0.0.1";
    private static final String NEW_LINE = System.lineSeparator();

    public static void main(String[] args) {
        System.out.println("program started...");
        // 8888포트에 소켓 생성
        try (ServerSocket serverSocket = new ServerSocket(8888)) { // serverSocket = 8888 포트에서 연결 요청을 기다림
            while(!serverSocket.isClosed()) {
                System.out.println("listening started on port 8888...");
                // accept() : 대기하다가 요청 수신 시 backlog queue에서 연결 꺼내서 새로운 Socket 객체 반환
                Socket clientSocket = serverSocket.accept();
                    clientSocket.setSoTimeout(10000); // 10초동안 입력 없으면 SocketTimeoutException 발생
                    // 요청별로 쓰레드 & 클라이언트소켓 생성
                    new Thread(() -> {
                        try {
                            handleRequest(clientSocket);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }).start();
            }
        } catch(SocketTimeoutException e) {
            System.out.println("SocketTimeoutException occurred...");
        } catch (IOException e) {
            System.out.println("ServerSocket creation failed: " + e.getMessage());
        }
    }

    public static void handleRequest(Socket clientSocket) throws IOException {
        try (
            Socket backendSocket = new Socket(BACKEND_HOST, BACKEND_PORT);
            BufferedReader clientBufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
            BufferedWriter clientBufferedWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream(), StandardCharsets.UTF_8));
            BufferedWriter backendBufferedWriter = new BufferedWriter(new OutputStreamWriter(backendSocket.getOutputStream(), StandardCharsets.UTF_8)); // backend랑 연결된 buffered output stream
            BufferedReader backendBufferedReader = new BufferedReader(new InputStreamReader(backendSocket.getInputStream(), StandardCharsets.UTF_8)) // backend랑 연결된 buffered input stream
        ) {
            // backend로 요청 포워딩
            backendBufferedWriter.write(readRequest(clientBufferedReader));
            backendBufferedWriter.flush();
            // client한테 backend로부터 온 응답 전달
            clientBufferedWriter.write(readResponse(backendBufferedReader));
            clientBufferedWriter.flush();
        } catch (IOException e) {
            System.out.println("handleRequest.getMessage() = " + e.getMessage());
        } finally {
            clientSocket.close();
        }
    }

    private static String readRequest(BufferedReader clientBufferedReader) throws IOException {
        // 클라이언트 http request message 파싱
        StringBuilder requestMessage = new StringBuilder();
        String requestLine;
        boolean transferEncodingChunked = false;
        int contentLength = 0;
        // 1. request startLine
        String requestStartLine = clientBufferedReader.readLine();
        if (requestStartLine == null || requestStartLine.isBlank()) {
            throw new IOException("Empty http request startLine.");
        }
        String[] requestStartLineParts = requestStartLine.split(" ", 3);
        if (requestStartLineParts.length != 3) {
            throw new IllegalArgumentException("Invalid HTTP Request Start Line.");
        }
        requestMessage
            .append(requestStartLineParts[0])
            .append(" ")
            .append("/backend/work")
            .append(" ")
            .append(requestStartLineParts[2])
            .append(NEW_LINE);
        // 2. 헤더
        while (true) {
            requestLine = clientBufferedReader.readLine();
            // 빈 줄 만나기 전에 EOF 나오면 정상적으로 온 메시지가 아님
            if (requestLine == null) {
                throw new EOFException("Unexpected EOF while reading http request headers.");
            }
            // 빈 줄 만나면 헤더 끝
            if (requestLine.isBlank())
                break;
            // Host 헤더는 Backend Host로 변경할 것이므로 pass
            if (requestLine.toLowerCase().startsWith("host"))
                continue;
            // body framing을 위해 content-length 저장
            if (!transferEncodingChunked && requestLine.toLowerCase().startsWith("content-length"))
                contentLength = Integer.parseInt(requestLine.split(":")[1].trim());
            // transfer-encoding: chunked 여부 저장
            if (requestLine.toLowerCase().startsWith("transfer-encoding"))
                transferEncodingChunked = requestLine.split(":")[1].trim().equalsIgnoreCase("chunked");
            requestMessage
                .append(requestLine)
                .append(NEW_LINE);
        }
        // Host 헤더는 Backend Host로 변경
        requestMessage
            .append("Host: ")
            .append(BACKEND_HOST)
            .append(NEW_LINE);
        // 헤더 끝을 의미하는 개행 (헤더 없어도, 마지막에 개행 필요)
        requestMessage.append(NEW_LINE);
        // 3. 바디
        // 바디있는지 판단 = chunked false, content-length 0 이상
        if (!transferEncodingChunked && contentLength > 0) {
            char[] body = new char[contentLength];
            clientBufferedReader.read(body, 0, contentLength);
            requestMessage
                .append(new String(body))
                .append(NEW_LINE);
        }
        System.out.println("requestMessage = " + requestMessage);
        return requestMessage.toString();
    }

    private static String readResponse(BufferedReader backendBufferedReader) throws IOException {
        // backend http response message 파싱
        StringBuilder responseMessage = new StringBuilder();
        String responseLine;
        boolean transferEncodingChunked = false;
        int contentLength = 0;
        // 1. response startLine
        String responseStartLine = backendBufferedReader.readLine();
        if (responseStartLine == null || responseStartLine.isBlank()) {
            throw new IOException("Empty http response startLine.");
        }
        String[] responseStartLineParts = responseStartLine.split(" ", 3);
        if (responseStartLineParts.length != 3 && responseStartLineParts.length != 2) {
            throw new IllegalArgumentException("Invalid HTTP Response Start Line.");
        }
        responseMessage
            .append(responseStartLineParts[0])
            .append(" ")
            .append(responseStartLineParts[1])
            .append(NEW_LINE); // reason phrase(status code 설명)는 생략
        // 2. 헤더
        while (true) {
            responseLine = backendBufferedReader.readLine();
            // 빈 줄 만나기 전에 EOF 나오면 정상적으로 온 메시지가 아님
            if (responseLine == null) {
                throw new EOFException("Unexpected EOF while reading http response headers.");
            }
            // 빈 줄 만나면 헤더 끝
            if (responseLine.isBlank())
                break;
            // body framing을 위해 content-length 저장
            if (!transferEncodingChunked && responseLine.toLowerCase().startsWith("content-length"))
                contentLength = Integer.parseInt(responseLine.split(":")[1].trim());
            // transfer-encoding: chunked 여부 저장
            if (responseLine.toLowerCase().startsWith("transfer-encoding"))
                transferEncodingChunked = responseLine.split(":")[1].trim().equalsIgnoreCase("chunked");
            responseMessage
                .append(responseLine)
                .append(NEW_LINE);
        }
        // 헤더 끝을 의미하는 개행 (헤더 없어도, 마지막에 개행 필요)
        responseMessage.append(NEW_LINE);
        // 3. 바디
        // 바디 있는지 판단 = chunked false, content-length 0 이상
        if (!transferEncodingChunked && contentLength > 0) {
            char[] body = new char[contentLength];
            backendBufferedReader.read(body, 0, contentLength);
            responseMessage
                .append(new String(body))
                .append(NEW_LINE);
        }
        System.out.println("responseMessage = " + responseMessage);
        return responseMessage.toString();
    }
}
