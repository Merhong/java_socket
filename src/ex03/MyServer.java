package ex03;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/* 양방향 통신(서버) */
// Half Duplex (반이중) - 쓰레드가 필요 없음
public class MyServer {

    public MyServer() throws IOException {
        // 1. 서버 소켓 생성(서버 소켓 = 리스너)
        ServerSocket serverSocket = new ServerSocket(10000); // 연결 받는 서버소켓
        System.out.println("서버 소켓이 클라이언트 대기중...");
        Socket socket = serverSocket.accept();  // 리스너 대기중(통신하는 소켓)
        System.out.println("클라이언트가 연결되었습니다.");

        // 2. 서버 요청
        // BufferedReader reader = new BufferedReader(new InputStreamReader(System.in)); // System.in -> socket으로 바꾸는게 목표
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        String msg = reader.readLine(); // 버퍼 소비, 들어온 스트림을 읽는다.
        System.out.println("클라이언트에게서 요청이 왔습니다 :" + msg); // 읽은 스트림을 모니터에 출력

        // 3. 구문 분석(파싱)
        String responseBody = "";
        if (msg.equals("text/html")) {
            responseBody = "<html><h1>Hello</h1></html>";
        } else if (msg.equals("text/plain")) {
            responseBody = "Hello";
        } else {
            responseBody = "404 not found";
        }

        // 4. 서버 응답
        // Write를 하면 생기는 Output을 Stream으로 통신(전송)한다.
        BufferedWriter writer = new BufferedWriter(
            new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
        writer.write(responseBody);     // Plain-Text, 평문
        writer.write("\n");          // 메시지 종료를 뜻하는 \n
        writer.flush();                 // 버퍼 소비
    }

    public static void main(String[] args) {
        try {
            new MyServer();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
