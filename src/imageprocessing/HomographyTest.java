package imageprocessing;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.swing.JFrame;



public class HomographyTest {

	public static void main(String[] args) throws IOException {
		//test1();

		// test1();

		createFrame();
	}

	public static void createFrame() throws IOException {
		JFrame frame = new JFrame();
		frame.setSize(HomographyGUI.sizePixels * 2, HomographyGUI.sizePixels);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


		HomographyGUI hgui = new HomographyGUI();
		frame.add(hgui);
		hgui.setVisible(true);
	}



	public static void test1() throws IOException {
		File fileImage = new File("/home/adrien/t1.png");
		BufferedImage source = ImageIO.read(fileImage);



		Point P1 = new Point(300,0);
		Point P2 = new Point(0, 0);
		Point P3 = new Point(0,300);
		Point P4 = new Point(300, 300);

		/**
		 * 86, 53
293, 68
299, 297
55, 283

	P1 = new Point(0+50,0);
		P2 = new Point(395-50, 0);
		P3 = new Point(395,395);
		P4 = new Point(0, 395);
		 */
		P1 = new Point(86, 53);
		P2 = new Point(293, 68);
		P3 = new Point(299, 297);
		P4 = new Point(55, 283);


		// résolution du système linéaire
		double[] system = getSystem(P1,P2,P3,P4);
		System.out.println(Arrays.toString(system));

		// création de l'image corrigée
		int W=400,H=400;
		BufferedImage target = new BufferedImage(W,H, BufferedImage.TYPE_INT_ARGB);

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
					target.setRGB(x, y, source.getRGB(px, py) );
				}
				catch (ArrayIndexOutOfBoundsException e) {

				}
			}
		}

		File outputfile = new File("/home/adrien/test.png");
		ImageIO.write(target, "png", outputfile);
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
	
	
	
	
	
	
	
	
	
	
	
	
	

	//	public static void test2() throws IOException {
	//		File fileImage = new File("/home/adrien/t1.png");
	//		BufferedImage source = ImageIO.read(fileImage);
	//
	//
	//
	//		// création de l'image corrigée
	//		int W=400,H=400;
	//		BufferedImage target = new BufferedImage(W,H, BufferedImage.TYPE_INT_ARGB);
	//
	//		int xMiddle = (int) (W / 2) ;
	//		int yMiddle = (int) (H / 2) ;
	//
	//		int xTranslate, yTranslate, xPrim, yPrim, xPrimRef1, yPrimRef1;
	//		double angleRad = 45 * Math.PI / 180.0;
	//
	//		// pour chaque pixel (x,y) de l'image corrigée
	//		for(int y=0;y<H;y++) {
	//			for(int x=0;x<W;x++) {
	//
	//				// Translate
	//				xTranslate = x - xMiddle;
	//				yTranslate = -y + yMiddle;
	//
	//				// Rotation,
	//				double angleRad2 = angleRad * xTranslate/xMiddle;
	//				double angleRad3 = angleRad * yTranslate/yMiddle;
	//				//				xPrim = (int)(Math.sin(angleRad2) * xTranslate - Math.cos(angleRad2) * yTranslate);
	//				//				yPrim = (int)(Math.cos(angleRad3) * xTranslate + Math.sin(angleRad3) * yTranslate);
	//
	//				xPrim = (int)(x+x*Math.sin(angleRad2));
	//				yPrim = (int)(y+y*Math.sin(angleRad3));
	//
	//				// Translate inv
	//				xPrimRef1 = xPrim + xMiddle;
	//				yPrimRef1 = -yPrim + yMiddle;
	//				try {
	//					target.setRGB(xPrimRef1, yPrimRef1, source.getRGB(x, y) );
	//				}
	//				catch (ArrayIndexOutOfBoundsException e) {
	//					System.out.println("error coords out of bounds on " + x + ", " + y);
	//				}
	//			}
	//		}
	//
	//		File outputfile = new File("/home/adrien/test.png");
	//		ImageIO.write(target, "png", outputfile);
	//	}
}
