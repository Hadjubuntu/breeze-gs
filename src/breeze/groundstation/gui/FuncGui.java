package breeze.groundstation.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;

import breeze.groundstation.main.Utils;
import breeze.groundstation.model.UAVState;

public class FuncGui extends Rect {

	private int MAX_POINTS = 20;
	private int ALT_MAX_METERS = 10;
	private int SIZE_PITCH_LINE = 60;

	private UAVState _uav;
	private int spaceX;

	public FuncGui(UAVState pUav, Color pBg) {
		super(pBg);
		_uav = pUav;
		spaceX = (int)(_sizeX / (MAX_POINTS+1));
	}


	public void paintComponent(Graphics g) {
		super.paintComponent(g);  
		Graphics2D g2d = (Graphics2D) g;
		if (_uav != null) {

			int n = _uav.getPosition().size();
			int i_start = Math.max(n-MAX_POINTS, 0);
			int i_max = Math.min(MAX_POINTS+n, n);

			for (int i = i_start; i < i_max; i ++) {

				double altMeters = _uav.getPosition().get(i).getAltCm()/100.0;
				
				if (altMeters > 0.85*ALT_MAX_METERS) {
					ALT_MAX_METERS = (int)(2*altMeters);
				}
				
				int posY = _sizeY - (int)((_sizeY/ALT_MAX_METERS) * altMeters);
				int posX = (i-i_start) * spaceX;
				
				g2d.setColor(new Color(60, 60, 250, 220));
				Ellipse2D.Double circle = new Ellipse2D.Double(posX-2, posY-2, 4, 4);
				g2d.fill(circle);
				
				if (i == i_max-1 && i > 0) {
					int spanX = 0;
					int spanY = -20;
					if (n > MAX_POINTS) {
						spanX = -20;
					}
					

					// Render pitch
					double endX = posX + SIZE_PITCH_LINE * Math.cos(Math.toRadians(_uav.getPitch()));
					double endY = posY - SIZE_PITCH_LINE * Math.sin(Math.toRadians(_uav.getPitch()));
					g2d.setColor(new Color(0, 230, 0, 250));
					g2d.draw(new Line2D.Double(posX, posY, endX, endY));
					g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
					
					// Render text altitude
					g2d.drawString(Utils.distHumanText(altMeters), posX+spanX, posY+spanY);
					
				}
			}
		}
	}
}
