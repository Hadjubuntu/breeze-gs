package imageprocessing;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

public class AlgoProjection extends Thread {
	private BufferedImage source;
	private ArrayList<ProjectionListener> listeners;

	private Point P1, P2, P3, P4;
	private BufferedImage  target ;

	public AlgoProjection() {
		listeners = new ArrayList<ProjectionListener>();

		P1 = new Point(86, 53);
		P2 = new Point(293, 68);
		P3 = new Point(299, 297);
		P4 = new Point(55, 283);

	}

	public void addListener(ProjectionListener pListener) {
		listeners.add(pListener);
	}

	public void fireListeners() {
		for (ProjectionListener list : listeners) {
			list.updateProjection(target);
		}
	}

	public void setImage(BufferedImage pSource) {
		source = pSource;
	}

	public void updatePoints(Point[] pPoints) {
		if (pPoints.length > 2) {
			P1 = pPoints[0];
			P2 = pPoints[1];
			P3 = pPoints[2];
			P4 = pPoints[3];
		}

	}

	public void run() {
		if (source == null) {
			System.out.println("Error : source image not set.");
			return ;
		}


		// résolution du système linéaire
		double[] system = getSystem(P1,P2,P3,P4);

		// création de l'image corrigée
		int W=HomographyGUI.sizePixels,H=HomographyGUI.sizePixels;
		target = new BufferedImage(W,H, BufferedImage.TYPE_INT_ARGB);

		// pour chaque pixel (x,y) de l'image corrigée
		for(int y=0;y<H;y++) {
			for(int x=0;x<W;x++) {

				// conversion dans le repère orthonormé (u,v) [0,1]x[0,1]
				double u = (double)x/W;
				double v = (double)y/H;

				// passage dans le repère perspective
				double[] P = invert(u, v, system);

				// copie du pixel (px,py) correspondant de l'image source 
				// TODO: faire une interpolation pour avoir de meilleurs résultats
				int px=(int)Math.round(P[0]);
				int py=(int)Math.round(P[1]);

				try {

					Color c = new Color(source.getRGB(px, py));
					int red = c.getRed();
					int green = c.getGreen();
					int blue = c.getBlue();

					target.setRGB(x, y, c.getRGB());
				}
				catch (ArrayIndexOutOfBoundsException e) {

				}
			}
		}


		FilterGaussian filter = new FilterGaussian(target, 1);
		filter.run();
		target = filter.getOutput();

		if (target != null) {
			File outputfile = new File("/home/adrien/imageIO/foutput.png");
			try {
				ImageIO.write(target, "png", outputfile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		fireListeners();
	}


	public static double[] getSystem(Point... P) {
		double[] system = new double[8];
		double sx = (P[0].x-P[1].x)+(P[2].x-P[3].x);
		double sy = (P[0].y-P[1].y)+(P[2].y-P[3].y);
		double dx1 = P[1].x-P[2].x;
		double dx2 = P[3].x-P[2].x;
		double dy1 = P[1].y-P[2].y;
		double dy2 = P[3].y-P[2].y;

		double z = (dx1*dy2)-(dy1*dx2);
		double g = ((sx*dy2)-(sy*dx2))/z;
		double h = ((sy*dx1)-(sx*dy1))/z;

		system[0]=P[1].x-P[0].x+g*P[1].x;
		system[1]=P[3].x-P[0].x+h*P[3].x;
		system[2]=P[0].x;
		system[3]=P[1].y-P[0].y+g*P[1].y;
		system[4]=P[3].y-P[0].y+h*P[3].y;
		system[5]=P[0].y;
		system[6]=g;
		system[7]=h;

		return system;
	}

	public static double[] invert(double u, double v, double[] system) {
		double x = (system[0]*u+system[1]*v+system[2])/(system[6]*u+system[7]*v+1); 
		double y = (system[3]*u+system[4]*v+system[5])/(system[6]*u+system[7]*v+1);
		return new double[]{x,y};
	}

}
