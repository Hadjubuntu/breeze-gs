package breeze.groundstation.main;


import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

	private static BundleContext context;

	static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		System.setProperty("java.library.path", "/home/adrien/UAV/BreezeWorkspace/breeze.groundstation/lib");
//		System.load("D:/EclispeWorkspace/BreezeWorkspace/breeze.groundstation/lib/jinput-dx8.dll");
//		System.load("D:/EclispeWorkspace/BreezeWorkspace/breeze.groundstation/lib/jinput-raw.dll");
//		System.load("D:/EclispeWorkspace/BreezeWorkspace/breeze.groundstation/lib/jinput-wintab.dll");
		/*System.load("/home/adrien/UAV/BreezeWorkspace/breeze.groundstation/lib/librxtxParallel.so");
		System.load("/home/adrien/UAV/BreezeWorkspace/breeze.groundstation/lib/librxtxSerial.so");
		System.load("/home/adrien/UAV/BreezeWorkspace/breeze.groundstation/lib/libjinput-linux64.so");
		*/
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
	}

}

