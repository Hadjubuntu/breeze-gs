package imageprocessing;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;


public class HomographyGUI extends JPanel implements ProjectionListener {
	private BufferedImage img;
	private BufferedImage imgTransformed;
	private int polygonIndex;
	private Point[] points;
	private BufferedImage source;
	public static int sizePixels = 800;

	public HomographyGUI() {
		super();
		setSize(sizePixels, sizePixels);
	

		File fileImage = new File("/home/adrien/imageIO/source.png");
		try {
			source = ImageIO.read(fileImage);
			img = source;			
		} catch (IOException e1) {
			e1.printStackTrace();
		}


		imgTransformed = null;
		polygonIndex = 0;
		points = new Point[5];



		addMouseListener(new MouseListener() {

			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mousePressed(MouseEvent e) {

			}

			@Override
			public void mouseExited(MouseEvent e) {


			}

			@Override
			public void mouseEntered(MouseEvent e) {

			}

			@Override
			public void mouseClicked(MouseEvent e) {

				
				if (polygonIndex < 4) {
					points[polygonIndex] = new Point(e.getX(), e.getY());
					polygonIndex ++;
				}


				if (polygonIndex == 4) {
					makeProjection();
				}
			}
		});
		
		repaint();
	}


	protected void makeProjection() {
		// Update polygon chosen and image transformation
		AlgoProjection algo = new AlgoProjection();
		algo.addListener(this);
		algo.setImage(source);
		algo.updatePoints(points);
		algo.start();
		polygonIndex = 0;
	}


	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		setOpaque(true);
		Graphics2D g2d = (Graphics2D) g;
		AffineTransform at = new AffineTransform();

		g2d.drawImage(img, at, null);
		if (imgTransformed != null) {
			g2d.drawImage(imgTransformed, sizePixels, 0, null);
		}
	}

	@Override
	public void updateProjection(BufferedImage pImg) {
		imgTransformed = pImg;
		repaint();
	}
}
