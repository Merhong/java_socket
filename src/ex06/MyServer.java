package ex06;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

/* 양방향 통신(서버) */
public class MyServer {

    ServerSocket serverSocket; // 연결 받는 서버소켓
    Socket socket;             // 리스너 대기중(통신하는 소켓)
    BufferedReader reader;
    BufferedWriter writer;
    Scanner keyboard;

    public MyServer() throws Exception {
        // 1. 메인쓰레드 = 리스너 담당, 서버 소켓 생성
        serverSocket = new ServerSocket(10000);
        System.out.println("서버 소켓이 클라이언트 대기중...");
        socket = serverSocket.accept(); // 리스너 대기중
        System.out.println("클라이언트가 연결되었습니다.");

        connectLine();
        read();
        write();
    }

    public static void main(String[] args) {
        try {
            new MyServer();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void connectLine() throws Exception {
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "utf-8"));
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "utf-8"));
        keyboard = new Scanner(System.in, "utf-8");
    }

    private void write() {
        new Thread(() -> {
            try {
                while (true) {
                    String msg = keyboard.nextLine();
                    writer.write(msg);
                    writer.write("\n");
                    writer.flush();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void read() {
        new Thread(() -> {
            try {
                while (true) {
                    try {
                        String msg = reader.readLine(); // 버퍼 비우기
                        String username = msg.split(":")[0];
                        String body = msg.split(":")[1];
                        System.out.println(username + " : " + body);
                    } catch (Exception e) {
                        System.out.println("괜찮아!!");
                    }

                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
