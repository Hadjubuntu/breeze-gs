package breeze.groundstation.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import breeze.groundstation.algo.GSMath;
import breeze.groundstation.main.GSController;
import breeze.groundstation.main.Utils;
import breeze.groundstation.model.GeoPosition;
import breeze.groundstation.model.Mission;
import breeze.groundstation.model.UAVState;
import breeze.groundstation.parts.MapVectorizedPart;

public class Map extends Rect {

	private static int MAX_POSITIONS_ON_MAP = 150;
	private int sizeX, sizeY, borderMargin;
	private UAVState _uav;
	private int zoomFactor;
	private double[] corners;
	private Mission _mission;

	public Map(Color pBg) {
		super(pBg);
		sizeX = 900;
		sizeY = 800;
		setSize(sizeX, sizeY);
		borderMargin = 50;
		_uav = GSController.getInstance().getUav();
		_mission = null;
		corners = new double[4]; // most left, most top, most right, most bottom
		zoomFactor = 1;

		initFocusListener();
	}


	private void initCorners() {
		for (int i = 0; i < corners.length; i++) {
			corners[i] = -1000.0;
		}
	}

	private void updateCorners(GeoPosition cPos) {
		if (cPos.getLon() < corners[0] || corners[0] == -1000.0) {
			corners[0] = cPos.getLon();
		}
		if (cPos.getLat() > corners[1] || corners[1] == -1000.0) {
			corners[1] = cPos.getLat();
		}
		if (cPos.getLon() > corners[2] || corners[2] == -1000.0) {
			corners[2] = cPos.getLon();
		}
		if (cPos.getLat() < corners[3] || corners[3] == -1000.0) {
			corners[3] = cPos.getLat();
		}
	}

	/**
	 * Determine map corners to see the waypoints and the uav on the map
	 */
	public void evalCorners() {
		initCorners();
		int n = _uav.getPosition().size();
		int iStart = Math.max(0, n-MAX_POSITIONS_ON_MAP);

		// With UAV positions
		for (int i = iStart; i < n; i ++) {
			GeoPosition cPos = _uav.getPosition().get(i);
			updateCorners(cPos);
		}

		// With UAV Mission if it exists
		if (_mission != null) {
			for (GeoPosition wp : _mission.getWaypoints()) {
				updateCorners(wp);
			}
		}		


		double dlat = Math.abs(corners[1] - corners[3]);
		double dlon = Math.abs(corners[2] - corners[0]);
		int minSizePx = (Math.min(sizeX, sizeY)-50-borderMargin*2);
		zoomFactor = (int)(minSizePx / (Math.max(dlat, dlon)));
	}


	/**
	 * Draw UAV and mission
	 */
	public void paintComponent(Graphics g) {
		super.paintComponent(g);  
		Graphics2D g2d = (Graphics2D) g;
		int relPosX, relPosY;
		Color refColor;


		// Evaluate dimensions
		//-----------------------------------------------------------
		evalCorners();


		// Draw map background
		//-----------------------------------------------------------
		double widthBgSpace = _sizeX / 10;
		double heightBgSpace = _sizeY / 10;
		refColor = new Color(80, 80, 200, 100);

		g2d.setStroke(new BasicStroke(3));
		g2d.setColor(refColor);

		for (int i = 0; i < 10; i ++) {
			g2d.draw(new Line2D.Double(i*widthBgSpace, 0, i*widthBgSpace, _sizeY));
		}
		for (int j = 0; j < 10; j ++) {
			g2d.draw(new Line2D.Double(0, j*heightBgSpace, _sizeX, j*heightBgSpace));
		}


		// Draw UAV positions
		//-----------------------------------------------------------
		int n = _uav.getPosition().size();
		int iStart = Math.max(0, n-MAX_POSITIONS_ON_MAP);

		int polyLength = Math.min(n, MAX_POSITIONS_ON_MAP);
		int polyPosX[] = new int[polyLength];
		int polyPosY[] = new int[polyLength];

		for (int i = iStart; i < n; i ++) {
			GeoPosition cPos = _uav.getPosition().get(i);

			int colorMvt = (n-(i+1)) * 20;
			if (colorMvt > 230) {
				colorMvt = 230;
			}

			refColor = new Color(100, (230-colorMvt), 0, (250 - colorMvt));

			// Draw circle
			g2d.setColor(refColor);
			BasicStroke bs3 = new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
			g2d.setStroke(bs3);

			relPosX = (int)((cPos.getLon() - corners[0]) * zoomFactor) + borderMargin;
			relPosY = -(int)((cPos.getLat() - corners[1]) * zoomFactor) + borderMargin;
			g2d.drawArc(relPosX-2, relPosY-2, 4, 4, 0, 360);

			//------------------------------------------------------------------
			// For the current position, render speed, distance, icon ..
			if (i == n-1 && i > 0) {
				String speedInfo = "";
				double geoDistanceMeters = (GSMath.geoDistance(cPos, _uav.getPosition().get(i-1)));
				// Draw last airspeed (pitot) or groundspeed m/s (gps)
				if (_uav.getAirspeedVms() > 0) {
					speedInfo = " | " + _uav.getAirspeedVms() + "m/s | " + _uav.getAngleDiffToTarget() + "°";
				}				

				g2d.drawString(Utils.distHumanText(geoDistanceMeters) + speedInfo, relPosX+20, relPosY+5);


				try {
					String imagePath = "/home/adrien/UAV/BreezeWorkspace/breeze.groundstation/img/uav_green.png";
					if (_uav.isAutoMode()) {
						imagePath = "/home/adrien/UAV/BreezeWorkspace/breeze.groundstation/img/uav_blue.png";
					}
					BufferedImage iconUAV = ImageIO.read(new File(imagePath));
					AffineTransform at = new AffineTransform();

					at.translate(relPosX,relPosY);
					at.scale(0.25, 0.25);
					at.rotate(_uav.getCap() * Math.PI / 180.0);
					at.translate(-175/2,-134/2);

					g2d.drawImage(iconUAV, at, null);
					g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				} catch(Exception e) {System.out.println(e.getMessage());}


				// Render cap heading (gps) and angle diff to target
				g2d.setColor(new Color(230, 10, 10, 200));
				BasicStroke bs4 = new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
				g2d.setStroke(bs4);
				double xCapTarget = relPosX+500*Math.cos(_uav.getCap()*Math.PI/180.0 - Math.PI/2.0);
				double yCapTarget = relPosY+500*Math.sin(_uav.getCap()*Math.PI/180.0 - Math.PI/2.0);
				g2d.draw(new Line2D.Double(relPosX,relPosY,xCapTarget,yCapTarget));

				if (_uav.isAutoMode()) {
					double angleDiffRad = _uav.getAngleDiffToTarget() * Math.PI/180.0;
					//double posXTargeted = xCapTarget*Math.cos(angleDiffRad) - Math.sin(angleDiffRad) * yCapTarget;
					//double posYTargeted = xCapTarget * Math.sin(angleDiffRad) + Math.cos(angleDiffRad) * yCapTarget;
					double posXTargeted = relPosX+500*Math.cos(_uav.getCap()*Math.PI/180.0 + angleDiffRad - Math.PI/2.0);
					double posYTargeted = relPosX+500*Math.sin(_uav.getCap()*Math.PI/180.0 + angleDiffRad - Math.PI/2.0);

					g2d.setColor(new Color(10, 230, 250, 200));
					BasicStroke bs5 = new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
					g2d.setStroke(bs5);
					g2d.draw(new Line2D.Double(relPosX,relPosY,posXTargeted,posYTargeted));
				}
			}

			// Polyline data
			polyPosX[i-iStart] = relPosX;
			polyPosY[i-iStart] = relPosY;
		}

		BasicStroke bsPoly = new BasicStroke(1);
		g2d.setColor(new Color(10, 200, 0, 120));
		g2d.setStroke(bsPoly);
		g2d.drawPolyline(polyPosX, polyPosY, polyLength);


		// Draw waypoints of the mission
		//-----------------------------------------------------------
		_mission = MapVectorizedPart.INSTANCE.getCurrentMission();
		if (_mission != null) {
			int i = 1;
			for (GeoPosition wp : _mission.getWaypoints()) {
				String textInfo = "";
				refColor = new Color(0, 230, 230);

				if (_uav.getCurrentWP() == i-1 && _uav.getPosition().size() > 0) {
					refColor = new Color(0, 230, 50, 230);
					double geoDistanceMeters = (GSMath.geoDistance(_uav.getPosition().get(n-1), wp));
					textInfo =  " / " + Utils.distHumanText(geoDistanceMeters);
				}

				g2d.setColor(refColor);
				BasicStroke bs3 = new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
				g2d.setStroke(bs3);

				relPosX = (int)((wp.getLon() - corners[0]) * zoomFactor) + borderMargin;
				relPosY = -(int)((wp.getLat() - corners[1]) * zoomFactor) + borderMargin;
				g2d.drawArc(relPosX-2, relPosY-2, 4, 4, 0, 360);


				g2d.drawString("wp_" + i + textInfo, relPosX+20, relPosY+5);

				i++;
			}
		}
	}
	
	
	
	private void initFocusListener() {
		addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mousePressed(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseExited(MouseEvent arg0) {
				
			}
			
			@Override
			public void mouseEntered(MouseEvent arg0) {
				// Mouse on map => user can zoom into
			}
			
			@Override
			public void mouseClicked(MouseEvent arg0) {
			}

			
		});
	}
}
