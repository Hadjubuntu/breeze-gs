package imageprocessing;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

import javax.imageio.ImageIO;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.FeatureDetector;
import org.opencv.imgcodecs.Imgcodecs;

public class FilterGaussian {

	private BufferedImage source;
	private int W, H;
	private double omega;

	private BufferedImage output;

	public FilterGaussian(BufferedImage pSource, double pOmega) {
		source = pSource;
		W = source.getWidth();
		H = source.getHeight();
		omega = pOmega;





		System.loadLibrary(Core.NATIVE_LIBRARY_NAME );
	}

	public void run() {
		try {
			ImageIO.write(source, "png", new File("/home/adrien/imageIO/sourceTmp.png"));
			Mat source = Imgcodecs.imread("/home/adrien/imageIO/sourceTmp.png");
//			Mat destination = new Mat(source.rows(),source.cols(),source.type());
//			destination = source;
//			//Imgproc.GaussianBlur(source, destination,new Size(45,45), 0);
//			Imgcodecs.imwrite("/home/adrien/output.png", destination);
			

			MatOfKeyPoint matOfKeyPoints = new MatOfKeyPoint();

			FeatureDetector blobDetector = FeatureDetector.create(FeatureDetector.DYNAMIC_HARRIS);
			blobDetector.detect(source, matOfKeyPoints);

			System.out.println("Detected " + matOfKeyPoints.size()+ " blobs in the image");

			List<KeyPoint> keyPoints = matOfKeyPoints.toList();
					

			output = ImageIO.read(new File("/home/adrien/imageIO/sourceTmp.png"));

			for (KeyPoint key : keyPoints) {
				for (int i = -10; i < 10; i ++) {
					output.setRGB((int)key.pt.x + i, (int)key.pt.y, (new Color(250, 0, 0)).getRGB());
				}
			}
			
			
			ImageIO.write(output, "png", new File("/home/adrien/imageIO/output.png"));
			
//			Calib3d.findHomography(source, destination); // CV_RANSAC 

		} catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
		}
	}

	public BufferedImage getOutput() {
		return output;
	}
}
