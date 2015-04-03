package breeze.groundstation.gui;

import java.awt.Graphics;
import java.awt.GridBagLayout;
import java.awt.GridLayout;

import javax.swing.JPanel;

public class GSPanel extends JPanel {

	public GSPanel(GridLayout gridLayout) {
		super(gridLayout);
	}
	public GSPanel(GridBagLayout gridLayout) {
		super(gridLayout);
	}
	public GSPanel() {
		super();
	}


	@Override 
	public void paintComponent(Graphics g)  
	{  
		super.paintComponent(g);  
		setOpaque(true);
		setBackground(GUICommon.panelBG);
	}  
}
