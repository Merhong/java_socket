package ch25;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
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
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class ChatClient2_1 extends MyFrame 
implements ActionListener, Runnable {

	JButton connectBtn, saveBtn, msgBtn, sendBtn;
	JTextField hostTxt, postTxt, msgTxt;
	JTextArea area;
	JList<String> list;
	BufferedReader in;
	PrintWriter out;
	Vector<String> vlist;
	boolean flag = false;
	int port = 8888;
	String listTitle = "*******대화자명단*******";

	public ChatClient2_1() {
		super(450, 500);
		setTitle("MyChat 2.1");
		JPanel p1 = new JPanel();
		p1.add(new JLabel("Host", Label.RIGHT));
		p1.add(hostTxt = new JTextField("127.0.0.1",15));
		p1.add(new JLabel("Port", Label.RIGHT));
		p1.add(postTxt = new JTextField(port+"" ,8));
		connectBtn = new JButton("connect");
		connectBtn.addActionListener(this);
		p1.add(connectBtn);
		add(BorderLayout.NORTH, p1);
		// //////////////////////////////////////////////////////////////////////////////////////////
		area = new JTextArea("MyChat2.0");
		JScrollPane scroll = new JScrollPane(area,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		area.setBackground(Color.DARK_GRAY);
		area.setForeground(Color.YELLOW);
		area.setEditable(false);
		add(BorderLayout.CENTER, scroll);
		// /////////////////////////////////////////////////////////////////////////////////////////
		JPanel p2 = new JPanel();
		p2.setLayout(new BorderLayout());
		vlist = new Vector<String>();
		vlist.add(listTitle);
		list = new JList(vlist);
		p2.add(BorderLayout.CENTER, list);
		JPanel p3 = new JPanel();
		p3.setLayout(new GridLayout(1, 2));
		saveBtn = new JButton("Save");
		saveBtn.addActionListener(this);
		msgBtn = new JButton("Message");
		msgBtn.addActionListener(this);
		p3.add(saveBtn);
		p3.add(msgBtn);
		p2.add(BorderLayout.SOUTH, p3);
		add(BorderLayout.EAST, p2);
		// ///////////////////////////////////////////////////////////////////////////////////////////
		JPanel p4 = new JPanel();
		msgTxt = new JTextField("", 33);
		msgTxt.addActionListener(this);
		sendBtn = new JButton("SEND");
		sendBtn.addActionListener(this);
		p4.add(msgTxt);
		p4.add(sendBtn);
		add(BorderLayout.SOUTH, p4);
	}

	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		if(connectBtn/*connect*/==o){
			connect(hostTxt.getText());
			connectBtn.setEnabled(false);
			hostTxt.setEditable(false);
			postTxt.setEditable(false);
		}else if(saveBtn/*save*/==o){
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
		}else if(msgBtn/*message*/==o){
			new Message(this,"TO:");
		}else if(sendBtn/*send*/==o||msgTxt==o){
			String msg = msgTxt.getText();
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
					//String id = list.getSelectedItem();
					String id =  vlist.get(list.getSelectedIndex());
					sendMessage(ChatProtocol2.CHAT+":"
					+id+";"+msg);
				}
			}
			msgTxt.setText("");
			msgTxt.requestFocus();
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
	
	public void connect(String host){
		try {
			Socket sock = new Socket(host, port);
			in = new BufferedReader(
					new InputStreamReader(
							sock.getInputStream()));
			out = new PrintWriter(
					sock.getOutputStream(),true);
			area.setText(in.readLine() + "\n");
		} catch (Exception e) {
			System.err.println("Error in Connect");
			e.printStackTrace();
			System.exit(1);
		}
		new Thread(this).start();
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
			vlist.removeAllElements();
			vlist.add(listTitle);
			StringTokenizer st = 
					new StringTokenizer(data,",");
			while(st.hasMoreTokens()){
				String id = st.nextToken();
				if(id!=null) vlist.add(id);
			}
			list.setListData(vlist);
		}else if(cmd.equals(ChatProtocol2.MESSAGE)){
			//MESSAGE:aaa;잠시만....
			idx = data.indexOf(';');
			cmd = data.substring(0,idx);//aaa
			data = data.substring(idx+1);//잠시만....
			new Message("FROM", cmd, data);
		}
	}

	class SaveMsg extends Dialog implements ActionListener {

		JButton ok;
		ChatClient2_1 ct2;

		public SaveMsg(ChatClient2_1 ct2) {
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
			add(ok = new JButton("확인"));
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

		ChatClient2_1 ct2;
		JButton send, close;
		JTextField name;
		JTextArea msg;
		String mode;// to/from
		String id;

		public Message(ChatClient2_1 ct2, String mode) {
			setTitle("쪽지보내기");
			this.ct2 = ct2;
			this.mode = mode;
			//id = ct2.list.getSelectedItem();
			id =  vlist.get(ct2.list.getSelectedIndex());
			layset();
		}
		public Message(String mode, String id, String msg) {
			setTitle("쪽지읽기");
			this.mode = mode;
			this.id = id;
			layset();
			this.msg.setText(msg);
			name.setEditable(false);
		}
		public void layset() {
			 addWindowListener(new WindowAdapter() {
				   public void windowClosing(WindowEvent e) {
				    dispose();
				   }
			});
			JPanel p1 = new JPanel();
			p1.add(new Label(mode, Label.CENTER));
			name = new JTextField(id, 20);
			p1.add(name);
			add(BorderLayout.NORTH, p1);
			msg = new JTextArea(10,30);
			JScrollPane scroll = new JScrollPane(msg,
					JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
					JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			add(scroll);
			JPanel p2 = new JPanel();
			if (mode.equals("TO:")) {
				p2.add(send = new JButton("send"));
				send.addActionListener(this);
			}
			p2.add(close = new JButton("close"));
			close.addActionListener(this);
			add(BorderLayout.SOUTH, p2);
			
			setBounds(200, 200, 300, 250);
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
		
		JButton ok;
		ChatClient2_1 ct2;
		
		public Waring(ChatClient2_1 ct2) {
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
			   JLabel label = new JLabel("입력하신 글짜는 금지어입니다."
					   ,Label.CENTER);
			   add(label);
			   add(ok = new JButton("확인"));
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
			ct2.msgTxt.setText("");
			dispose();
		}
	}
	
	public static void main(String[] args) {
		new ChatClient2_1();
	}
}
