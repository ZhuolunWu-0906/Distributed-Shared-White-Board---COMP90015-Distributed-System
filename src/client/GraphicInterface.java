package client;
 
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
 
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
 
public class GraphicInterface {
 

	Color black = new Color(0, 0, 0);
	Color white = new Color(255, 255, 255);
	Color gray = new Color(128, 128, 128);
	Color silver = new Color(192, 192, 192);
	Color maroon = new Color(128, 0, 0);
	Color red = new Color(255, 0, 0);
	Color purple = new Color(128, 0, 128);
	Color fushsia = new Color(255, 0, 255);
	Color green = new Color(0, 128, 0);
	Color lime = new Color(0, 255, 0);
	Color olive = new Color(128, 128, 0);
	Color yellow = new Color(255, 255, 0);
	Color navy = new Color(0, 0, 128);
	Color blue = new Color(0, 0, 255);
	Color teal = new Color(0, 128, 128);
	Color aqua = new Color(0, 255, 255);
	Color[] colors = {black, white, gray, silver, maroon, red, purple, fushsia, green, lime, olive, yellow, navy, blue, teal, aqua};
	
	JButton clearBtn, blackBtn, whiteBtn, grayBtn, silverBtn, maroonBtn, redBtn, purpleBtn, fushsiaBtn, greenBtn, limeBtn, oliveBtn, yellowBtn, navyBtn, blueBtn, tealBtn, aquaBtn;
	JButton[] btns = {blackBtn, whiteBtn, grayBtn, silverBtn, maroonBtn, redBtn, purpleBtn, fushsiaBtn, greenBtn, limeBtn, oliveBtn, yellowBtn, navyBtn, blueBtn, tealBtn, aquaBtn};
	
	Whiteboard drawArea;
	Dimension dm = new Dimension(30,30);
	
	ActionListener actionListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == clearBtn) {
				drawArea.clear();
			} else {
				drawArea.changeColor(((JButton)e.getSource()).getBackground());
			}
		}
	};

	public static void main(String[] args) {
		new GraphicInterface().show();
	}
	
	public void show() {
		// create main frame
		JFrame frame = new JFrame("Swing Paint");
		Container content = frame.getContentPane();
		content.setLayout(new BorderLayout());
		drawArea = new Whiteboard();
		
		// add to content pane
		content.add(drawArea, BorderLayout.CENTER);
		
		JPanel controls = new JPanel();

		clearBtn = new JButton("Clear");
		clearBtn.addActionListener(actionListener);
		
		for (int i = 0; i < btns.length; i++) {
			btns[i] = new JButton("");
			btns[i].setPreferredSize(dm);
			btns[i].setBackground(colors[i]);
			btns[i].addActionListener(actionListener);
			controls.add(btns[i]);
		}
		
		controls.add(clearBtn);
		
		content.add(controls, BorderLayout.NORTH);
		
		frame.setSize(1200, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
 
}