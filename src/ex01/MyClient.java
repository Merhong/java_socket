package ex01;import java.io.BufferedWriter;import java.io.IOException;import java.io.OutputStreamWriter;import java.net.Socket;import java.util.Scanner;/* 단방향 통신(클라이언트) */// 키보드에 버퍼를 연결해서 -> 키보드를 메시지를 서버쪽으로 전송public class MyClient {    public MyClient() throws IOException {        // 1. 서버 연결 (localhost == 127.0.0.1) ==> IP주소 : 192.168.200.115        // 10, 172, 192로 시작하는 IP는 사설 IP임        Socket socket = new Socket("127.0.0.1", 10000); // 루프백 주소(자기자신)        // 2. 서버쪽으로 메시지 전송        // BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out); // System.out을 -> socket으로 바꿈        BufferedWriter writer = new BufferedWriter(            new OutputStreamWriter(socket.getOutputStream()));        // 3. 키보드 버퍼 연결 - 키보드 메시지 받아서!!        // 키보드 연결 : System.in 사용해야하니 -> socket으로 바꾸려면 InputStream 사용? (X)        // Stream은 통신 단계에서 사용되니 클라이언트쪽에서 메시지를 보내는건 단순히 키보드 입력하면 됨.        // 따라서 Scanner로 System.in을 사용        Scanner sc = new Scanner(System.in);        System.out.println("서버에 보낼 메시지를 작성하고 엔터를 누르세요.");        String msg = sc.nextLine(); // 스캐너를 사용해 키보드 입력을 받는다.        writer.write(msg); // 버퍼 소비, 키보드로 입력받은 메시지를 한 줄 보냄. \n을 써줘야 함. 안쓰면 에러.        writer.write("\n"); // 메시지 종료를 알리는 null을 보내준다.        writer.flush(); // flush 하지 않으면 크기가 충분하지 않기에 메시지가 날라가지 않음.    }    public static void main(String[] args) {        try {            new MyClient();        } catch (Exception e) {            e.printStackTrace();        }    }}