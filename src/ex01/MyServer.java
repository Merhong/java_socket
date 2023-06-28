package ex01;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/* 단방향 통신(서버) */
public class MyServer {

    public MyServer() throws IOException {
        // 1. 서버 소켓 생성(서버 소켓 = 리스너)
        ServerSocket serverSocket = new ServerSocket(10000); // 연결 받는 서버소켓
        System.out.println("서버 소켓이 클라이언트 대기중...");
        Socket socket = serverSocket.accept();  // 리스너 대기중(통신하는 소켓)
        System.out.println("클라이언트가 연결되었습니다.");

        // 2. 클라이언트의 메세지를 받기
        // BufferedReader reader = new BufferedReader(new InputStreamReader(System.in)); // System.in -> socket으로 바꾸는게 목표
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String msg = reader.readLine();            // 버퍼 소비, 들어온 스트림을 읽는다.
        System.out.println("받은 메시지 :" + msg);   // 읽은 스트림을 모니터에 출력
    }

    public static void main(String[] args) {
        try {
            new MyServer();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
