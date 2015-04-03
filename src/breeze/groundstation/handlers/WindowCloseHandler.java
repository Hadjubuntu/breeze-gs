package breeze.groundstation.handlers;

import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.modeling.IWindowCloseHandler;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import breeze.groundstation.main.GSController;

public class WindowCloseHandler implements IWindowCloseHandler{
    @Override
    public boolean close(MWindow window) {
//      System.out.println("in window close handler");
        Object shell = window.getWidget();
        boolean closing = MessageDialog.openConfirm((Shell) shell, "Confirmation",
                "Shutdown of the UAV if confirmed before window closed");
        
        if (closing) {
        	GSController.getInstance().shutdownUavCommand();
        	GSController.getInstance().saveLogger();
        }
        
        return closing;
    }
}