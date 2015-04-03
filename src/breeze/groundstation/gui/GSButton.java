package breeze.groundstation.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JPanel;

public class GSButton extends JPanel {

	private Color _bg;
	private int _sizeX, _sizeY;

	public GSButton(Color pBg, int sizeX, int sizeY) {
		_sizeX = sizeX;
		_sizeY = sizeY;
		_bg = pBg;

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

		g2d.draw(new RoundRectangle2D.Double(0, 0,
                _sizeX,
                _sizeY,
                10, 10));  
		g2d.setColor(_bg);  
		g2d.fillRect(0,0,_sizeX,_sizeY);  
	}
}
