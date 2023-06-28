package ch25;

public class ChatProtocol2 {
	
	public final static String ID = "ID";
	//(C->S)ID:아이디
	//(S->C)CHATLIST:아이디;아이디;아이디
	
	public final static String CHAT = "CHAT";
	//(C->S)CHAT:받는아이디;메세지
	//(S->C)CHAT:보낸아이디;메세지
	public final static String CHATALL = "CHATALL";
	//(C->S)CHATALL:메세지
	//(S->C)CHATALL:[보낸아이디]메세지
	
	public final static String MESSAGE = "MESSAGE";
	//(C->S)MESSAGE;받는아이디;메세지
	//(S->C)MESSAGE;보낸아이디;메세지
	public final static String CHATLIST = "CHATLIST";
	

}

