package ch25;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class ChatServer2 {
	
	Vector<ClientThread2> vc;
	
	public ChatServer2() {
		vc = new Vector<ClientThread2>();
		ServerSocket server = null;
		int port = 8888;
		try {
			server = new ServerSocket(port);
		} catch (Exception e) {
			System.err.println("Error in ChatServer");
			e.printStackTrace();
			System.exit(1);
		}
		System.out.println("*********************************");
		System.out.println("클라이언트의 접속을 기다리고 있습니다.");
		System.out.println("*********************************");
		try {
			while(true){
				Socket sock = server.accept();
				ClientThread2 ct = 
					new ClientThread2(this,sock);
				ct.start();
				vc.add(ct);
			}
		} catch (Exception e) {
			System.err.println("Error in Socket");
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	//접속된 모든 Client에게 메세지를 보낸다.
	public void  sendAllClient(String msg) {
		for (int i = 0; i < vc.size(); i++) {
			ClientThread2 ct = vc.get(i);
			ct.sendMessage(msg);
		}
	}
	//Vector에서 ClientThread 제거한다.
	public void  removeClientThread(ClientThread2 ct) {
		vc.remove(ct);
	}
	
	class ClientThread2 extends Thread{
		
		ChatServer2 cs;
		Socket sock;
		BufferedReader in;
		PrintWriter out;
		String id = "익명";
		
		public ClientThread2(ChatServer2 cs, Socket sock) {
			this.cs = cs;
			this.sock = sock;
		}
		
		public void run(){
			try {
				System.out.println(sock + " 접속됨");
				in = new BufferedReader(
						new InputStreamReader(sock.getInputStream()));
				out = 
					new PrintWriter(sock.getOutputStream(),true);
				//처음 클라이언트에 보내는 메세지
				out.println("사용하실 아이디를 입력하세요.");
				String data = null;
				boolean done = false;
				while(!done){
					String line = in.readLine();
					if(line==null) done = true;
					else routine(line);
				}
			} catch (Exception e) {
				cs.removeClientThread(this);
				System.err.println(sock+"["+id+"] 끊어짐");
				e.printStackTrace();
			}
		}
		
		public void routine(String line) {
			//CHATALL:오늘은 불타는 금요일
			int idx = line.indexOf(':');
			//int len;
			String cmd = line.substring(0,idx);//CHATALL
			String data = line.substring(idx+1);//오늘은 불타는 금요일
			if(cmd.equals(ChatProtocol2.ID)){
				//ID:aaa
				if(data!=null&&data.length()>0)id=data;
				cs.sendAllClient(ChatProtocol2.CHATLIST+":"+
						getIds());//CHATLIST:aaa,bbb,ccc
				cs.sendAllClient(ChatProtocol2.CHATALL+":"+
						"["+id+"]님 입장하였습니다.");
			}else if(cmd.equals(ChatProtocol2.CHAT)){
				//CHAT:bbb;배고프나?
				idx = data.indexOf(';');
				cmd = data.substring(0,idx);//bbb
				data = data.substring(idx+1);//배고프나?
				ClientThread2 ct = findThread(cmd/*bbb*/);
				if(ct!=null){
					ct.sendMessage(ChatProtocol2.CHAT+":"+
							"["+id/*aaa*/+"]"+data);
				}else{
					sendMessage(ChatProtocol2.CHAT+":"+
						"["+cmd+"] 사용자가 아닙니다.");
				}
			}else if(cmd.equals(ChatProtocol2.CHATALL)){
				cs.sendAllClient(ChatProtocol2.CHATALL+":"+
						"["+id+"]"+data);
			}else if(cmd.equals(ChatProtocol2.MESSAGE)){
				//(C->S)MESSAGE:bbb;잠시만.... (aaa->bbb 쪽지)
				//(S->C)MESSAGE:aaa;잠시만....
				idx = data.indexOf(';');
				cmd = data.substring(0,idx);//bbb
				data = data.substring(idx+1);//잠시만....
				ClientThread2 ct = findThread(cmd/*bbb*/);
				if(ct!=null){
					ct.sendMessage(ChatProtocol2.MESSAGE+":"+
							id/*aaa*/+";"+data);
				}else{
					sendMessage(ChatProtocol2.CHAT+":"+
							"["+cmd+"] 사용자가 아닙니다.");
				}
			}
		}
		
		//매개변수로 id로 ClientThread 찾아서 리턴
		public ClientThread2 findThread(String id2){
			ClientThread2 ct = null;
			for (int i = 0; i < vc.size(); i++) {
				ct = vc.get(i);
				if(ct.id.equals(id2)) break;
			}
			return ct;
		}
		
		//접속된 모든 id를 리턴(; 구분) ex)aaa,bbb,홍길동
		public String getIds(){
			String ids = "";
			for (int i = 0; i < vc.size(); i++) {
				ClientThread2 ct = vc.get(i);
				ids+=ct.id+",";//aaa,bbb,ccc,ddd,
			}
			return ids;
		}
		
		public void sendMessage(String msg){
			out.println(msg);
		}
	}
	
	public static void main(String[] args) {
		new ChatServer2();
	}
}
