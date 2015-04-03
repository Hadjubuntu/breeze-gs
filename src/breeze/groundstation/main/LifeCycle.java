package breeze.groundstation.main;

import org.eclipse.e4.ui.workbench.lifecycle.PostContextCreate;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.swt.widgets.Display;

public class LifeCycle {
	@PostContextCreate
	void postContextCreate(IApplicationContext appContext, Display display) {
		// Application e4 has started, run the differents controller
		Arduino arduino = new Arduino();
		arduino.run();
	}
}
