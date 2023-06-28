package ch25;

import javax.swing.JFrame;

public class MyFrame extends JFrame{
	
	int w = 300;
	int h = 200;
	
	public MyFrame() {
		setSize(w, h);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
	}
	
	public MyFrame(int w, int h) {
		setSize(w, h);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
	}
}







