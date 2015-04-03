package breeze.groundstation.gui;


import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;

public class HUD extends Rect {

	private double pitch;
	private double roll;
	private double rollDesired;
	private double pitchDesired;

	public HUD(Color pBg) {
		super(pBg);
		pitch = 0.0;
		roll = 0.0;
	}

	public void setAttitude(double pRoll, double pPitch, double pDesiredRoll, double pDesiredPitch) {
		roll = pRoll;
		pitch = pPitch;
		rollDesired = pDesiredRoll;
		pitchDesired = pDesiredPitch;
	}

	public int pitchToYCenter(double pPitch) {
		return (int) (_sizeY/2.0 - pPitch * (_sizeY/2/90.0));
	}

	public int rollToYLeft(double pPitch, double pRoll, int pSizeX) {
		int y_pitch_center = pitchToYCenter(pPitch);
		return y_pitch_center - (int)(Math.tan(Math.PI/180.0*pRoll)*(pSizeX/2.0));
	}

	public int rollToYRight(double pPitch, double pRoll, int pSizeX) {
		int y_pitch_center = pitchToYCenter(pPitch);
		return y_pitch_center + (int)(Math.tan(Math.PI/180.0*pRoll)*(pSizeX/2.0));
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);  
		Graphics2D g2d = (Graphics2D) g;
		
		// Colors
		Color refColor = new Color(0, 30, 0, 200);
		if (pitch < 85) {
			refColor = new Color(0, 230, 0, 250);
		}
		Color targetColor = new Color(0, 50, 230, 250);

		// Draw some pitch with the specified roll
		//---------------------------------------------------
		g2d.setColor(refColor);
		BasicStroke bs2 = new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		g2d.setStroke(bs2);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		double pitchAxis = 0.0;
		for (int i = -3; i <= 3; i ++) {
			pitchAxis = i * 20;
			g2d.draw(new Line2D.Double(_sizeX/2.0-75, rollToYLeft(pitchAxis, roll, 150), _sizeX/2.0+75, rollToYRight(pitchAxis, roll, 150)));

			g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g2d.drawString(" " + (int)pitchAxis,  (int)(_sizeX/2.0+85), rollToYRight(pitchAxis, roll, 150));
		}

		
		
		
		
		// Draw Polygon as HUD
		//---------------------------------------------------
		Polygon p = new Polygon();


		Point pointHUDRight = new Point(_sizeX, rollToYRight(pitch, roll, _sizeX));
		Point pointHUDLeft = new Point(0, rollToYLeft(pitch, roll, _sizeX));

		// Add points to make HUD
		p.addPoint(pointHUDLeft.x, pointHUDLeft.y);
		p.addPoint(pointHUDRight.x, pointHUDRight.y);
		p.addPoint(_sizeX, _sizeY);
		p.addPoint(0, _sizeY);


		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setColor(new Color(0, 200, 0, 200));  
		g2d.fillPolygon(p);


		//-----------------------------------------------
		// Then draw reference / text
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2d.drawString("pitch= " + pitch + " , roll= " + roll, 15,  15);

		// Draw targeted roll and pitch
		//--------------------------------------------
		g2d.setColor(targetColor);
		BasicStroke bs3 = new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		g2d.setStroke(bs3);
	//	g2d.drawArc((int)(_sizeX/2.0-15), pitchToYCenter(pitchDesired)-15, 30, 30, 20, 200);
		
		g2d.draw(new Line2D.Double((double)((_sizeX/2.0)-20*Math.cos(Math.PI/180.0*rollDesired)), (double)rollToYLeft(pitchDesired, rollDesired, 40), (double)((_sizeX/2.0)+20*Math.cos(Math.PI/180.0*rollDesired)), (double)rollToYRight(pitchDesired, rollDesired, 40)));

	}

}
