package breeze.groundstation.gui;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class ImageOnOff extends JPanel {
	private boolean _state;
	private double _scale;
	private String _pathOn, _pathOff;
	private BufferedImage _imageOn, _imageOff;

	public ImageOnOff(double scale, String pPathOn, String pPathOff) {
		_pathOn = pPathOn;
		_pathOff = pPathOff;
		_state = false;
		_scale = scale;

		try {
			_imageOn = ImageIO.read(new File(_pathOn));
			_imageOff = ImageIO.read(new File(_pathOff));
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void setState(boolean pState) {
		_state = pState;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		setOpaque(true);
		setBackground(GUICommon.panelBG);

		BufferedImage cImage = null;
		if (_state) {
			cImage = _imageOn;
		}
		else {
			cImage = _imageOff;
		}

		// create the transform, note that the transformations happen
		// in reversed order (so check them backwards)
		AffineTransform at = new AffineTransform();
		
       /*   // 4. translate it to the center of the component
          at.translate(getWidth() / 2, getHeight() / 2);

          // 3. do the actual rotation
          at.rotate(Math.PI/4);*/

          // 2. just a scale because this image is big
          at.scale(_scale, _scale);

          // 1. translate the object so that you rotate it around the 
          //    center (easier :))
         // at.translate(-cImage.getWidth()/2, -cImage.getHeight()/2);
		 
		// draw the image
		Graphics2D g2d = (Graphics2D) g;
		g2d.drawImage(cImage, at, null);
		
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

	}
}
