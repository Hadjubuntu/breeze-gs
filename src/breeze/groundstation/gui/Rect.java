package breeze.groundstation.gui;


import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

public class Rect extends JPanel {
	protected Color _bg;
	protected int _sizeX, _sizeY;

	public Rect(Color pBg) {
		_bg = pBg;
		_sizeX = 400;
		_sizeY = 400;
		

		setBackground(GUICommon.panelBG);
		setOpaque(true);
	}

	public void setSize(int sizeX, int sizeY) {
		_sizeX = sizeX;
		_sizeY = sizeY;
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);  
		
		Graphics2D g2d = (Graphics2D) g;
		

		g2d.setColor(new Color(20, 200, 20, 200));
		g2d.drawRect(0,0,_sizeX,_sizeY);  
		
		g2d.setColor(_bg);  
		g2d.fillRect(0,0,_sizeX,_sizeY);  
	}
}
