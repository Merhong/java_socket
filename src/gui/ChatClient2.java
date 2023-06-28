package ch25;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.List;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.StringTokenizer;

public class ChatClient2 extends MyFrame 
implements ActionListener, Runnable {

	Button bt1, bt2, bt3, bt4;
	TextField tf1, tf2, tf3;
	TextArea area;
	List list;
	BufferedReader in;
	PrintWriter out;
	String listTitle = "*******대화자명단*******";
	boolean flag = false;

	public ChatClient2() {
		super(450, 500);
		setTitle("MyChat 2.0");
		Panel p1 = new Panel();
		p1.add(new Label("Host", Label.RIGHT));
		p1.add(tf1 = new TextField("127.0.0.1"));
		p1.add(new Label("Port", Label.RIGHT));
		p1.add(tf2 = new TextField("8888"));
		bt1 = new Button("connect");
		bt1.addActionListener(this);
		p1.add(bt1);
		add(BorderLayout.NORTH, p1);
		// //////////////////////////////////////////////////////////////////////////////////////////
		area = new TextArea("MyChat 2.0");
		area.setBackground(Color.DARK_GRAY);
		area.setForeground(Color.YELLOW);
		area.setEditable(false);
		add(BorderLayout.CENTER, area);
		// /////////////////////////////////////////////////////////////////////////////////////////
		Panel p2 = new Panel();
		p2.setLayout(new BorderLayout());
		list = new List();
		list.add(listTitle);
		p2.add(BorderLayout.CENTER, list);
		Panel p3 = new Panel();
		p3.setLayout(new GridLayout(1, 2));
		bt2 = new Button("Save");
		bt2.addActionListener(this);
		bt3 = new Button("Message");
		bt3.addActionListener(this);
		p3.add(bt2);
		p3.add(bt3);
		p2.add(BorderLayout.SOUTH, p3);
		add(BorderLayout.EAST, p2);
		// ///////////////////////////////////////////////////////////////////////////////////////////
		Panel p4 = new Panel();
		tf3 = new TextField("", 50);
		tf3.addActionListener(this);
		bt4 = new Button("send");
		bt4.addActionListener(this);
		p4.add(tf3);
		p4.add(bt4);
		add(BorderLayout.SOUTH, p4);
	}

	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		if(bt1/*connect*/==o){
			new Thread(this).start();
			bt1.setEnabled(false);
			area.setText("");
		}else if(bt2/*save*/==o){
			String saveStr = area.getText();
			long millis = System.currentTimeMillis();
			//1907.1.1 ~ 현재까지 1/1000초 단위로 계산
			try {
				FileWriter fw = new FileWriter(millis+".txt");
				fw.write(saveStr);
				fw.close();
				new SaveMsg(this);
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}else if(bt3/*message*/==o){
			new Message(this,"TO:");
		}else if(bt4/*send*/==o||tf3==o){
			String msg = tf3.getText();
			if(filterMgr(msg)){
				new Waring(this);
				return;
			}
			if(!flag/*아이디입력일때*/){
				sendMessage(ChatProtocol2.ID+":"+msg);//ID:aaa
				setTitle(getTitle()+"-"+msg+"님 반갑니다.");
				area.setText("");
				flag = true;
			}else/*채팅일때*/{
				int i = list.getSelectedIndex();
				if(i==-1||i==0/*전체채팅*/){
					sendMessage(ChatProtocol2.CHATALL+":"+msg);
				}else{
					String id = list.getSelectedItem();
					sendMessage(ChatProtocol2.CHAT+":"
					+id+";"+msg);
				}
			}
			tf3.setText("");
			tf3.requestFocus();
		}
	}
	
	public boolean filterMgr(String msg){//오늘 밥 머 먹을까?
		boolean flag = false;//false이면 금지어 아님
		String str[] = {"바보","개새끼","새끼","자바","java"};
		//하하 호호 히히
		StringTokenizer st = new StringTokenizer(msg);
		String msgs[] = new String[st.countTokens()];
		for (int i = 0; i < msgs.length; i++) {
			msgs[i] = st.nextToken();
		}
		for (int i = 0; i < str.length; i++) {
			if(flag) break;
			for (int j = 0; j < msgs.length; j++) {
				if(str[i].equals(msgs[j])){
					flag = true;
					break;
				}
			}
		}
		return flag;
	}

	public void sendMessage(String msg) {
		out.println(msg);
	}

	public void run() {
		try {
			Socket sock = new Socket(tf1.getText(), Integer.parseInt(tf2
					.getText()));
			in = new BufferedReader(
					new InputStreamReader(sock.getInputStream()));
			out = new PrintWriter(sock.getOutputStream(), true);
			area.append(in.readLine() + "\n");
			boolean done = false;
			while (!done) {
				String line = in.readLine();
				if (line == null)
					done = true;
				else
					routine(line);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void routine(String line) {
		int idx = line.indexOf(':');
		String cmd = line.substring(0,idx);
		String data = line.substring(idx+1);
		if(cmd.equals(ChatProtocol2.CHAT)||
				cmd.equals(ChatProtocol2.CHATALL)){
			//CHAT or CHATALL:[bbb]파이팅!!!
			area.append(data+"\n");
		}else if(cmd.equals(ChatProtocol2.CHATLIST)){
			//CHATLIST:aaa,bbb,ccc
			list.removeAll();
			list.add(listTitle);
			StringTokenizer st = 
					new StringTokenizer(data,",");
			while(st.hasMoreTokens()){
				String id = st.nextToken();
				if(id!=null) list.add(id);
			}
		}else if(cmd.equals(ChatProtocol2.MESSAGE)){
			//MESSAGE:aaa;잠시만....
			idx = data.indexOf(';');
			cmd = data.substring(0,idx);//aaa
			data = data.substring(idx+1);//잠시만....
			new Message("FROM", cmd, data);
		}
	}

	class SaveMsg extends Dialog implements ActionListener {

		Button ok;
		ChatClient2 ct2;

		public SaveMsg(ChatClient2 ct2) {
			super(ct2, "Save", true);
			this.ct2 = ct2;
			 addWindowListener(new WindowAdapter() {
				   public void windowClosing(WindowEvent e) {
				    dispose();
				   }
			});
			setLayout(new GridLayout(2, 1));
			Label label = new Label("대화내용을 저장하였습니다.", Label.CENTER);
			add(label);
			add(ok = new Button("확인"));
			ok.addActionListener(this);
			layset();
			setVisible(true);
		}

		public void layset() {
			int x = ct2.getX();
			int y = ct2.getY();
			int w = ct2.getWidth();
			int h = ct2.getHeight();
			int w1 = 150;
			int h1 = 100;
			setBounds(x + w / 2 - w1 / 2, y + h / 2 - h1 / 2, 200, 100);
		}

		public void actionPerformed(ActionEvent e) {
			ct2.area.setText("");
			setVisible(false);
			dispose();
		}
	}

	class Message extends Frame implements ActionListener {

		ChatClient2 ct2;
		Button send, close;
		TextField name;
		TextArea msg;
		String mode;// to/from
		String id;

		public Message(ChatClient2 ct2, String mode) {
			setTitle("쪽지보내기");
			this.ct2 = ct2;
			this.mode = mode;
			id = ct2.list.getSelectedItem();
			layset();
		}
		public Message(String mode, String id, String msg) {
			setTitle("쪽지읽기");
			this.mode = mode;
			this.id = id;
			layset();
			this.msg.setText(msg);
		}
		public void layset() {
			 addWindowListener(new WindowAdapter() {
				   public void windowClosing(WindowEvent e) {
				    dispose();
				   }
			});
			Panel p1 = new Panel();
			p1.add(new Label(mode, Label.CENTER));
			name = new TextField(id, 20);
			p1.add(name);
			add(BorderLayout.NORTH, p1);
			
			msg = new TextArea("");
			add(BorderLayout.CENTER, msg);
			
			Panel p2 = new Panel();
			if (mode.equals("TO:")) {
				p2.add(send = new Button("send"));
				send.addActionListener(this);
			}
			p2.add(close = new Button("close"));
			close.addActionListener(this);
			add(BorderLayout.SOUTH, p2);
			
			setBounds(200, 200, 250, 250);
			setVisible(true);
		}

		public void actionPerformed(ActionEvent e) {
			if(e.getSource()==send){
				ct2.sendMessage(ChatProtocol2.MESSAGE+
						":"+id+";"+ msg.getText());
			}
			setVisible(false);
			dispose();
		}
	}

	
	class Waring extends Dialog 
	implements ActionListener{
		
		Button ok;
		ChatClient2 ct2;
		
		public Waring(ChatClient2 ct2) {
			super(ct2,"경고",true);
			this.ct2 = ct2;
			 //////////////////////////////////////////////////////////////////////////////////////////
			   addWindowListener(new WindowAdapter() {
			    public void windowClosing(WindowEvent e) {
			     dispose();
			    }
			   });
			   /////////////////////////////////////////////////////////////////////////////////////////
			   setLayout(new GridLayout(2,1));
			   Label label = new Label("입력하신 글짜는 금지어입니다."
					   ,Label.CENTER);
			   add(label);
			   add(ok = new Button("확인"));
			   ok.addActionListener(this);
			   layset();
			   setVisible(true);
		}
		
		public void layset() {
			int x = ct2.getX();
			int y = ct2.getY();
			int w = ct2.getWidth();
			int h = ct2.getHeight();
			int w1 = 150;
			int h1 = 100;
			setBounds(x + w / 2 - w1 / 2, 
					y + h / 2 - h1 / 2, 200, 100);
		}

		public void actionPerformed(ActionEvent e) {
			ct2.tf3.setText("");
			dispose();
		}
	}
	
	public static void main(String[] args) {
		new ChatClient2();
	}
}
