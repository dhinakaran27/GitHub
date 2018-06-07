package com.unilever.rac.ui.packreport;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.aifrcp.AifrcpPlugin;
import com.teamcenter.rac.aifrcp.SelectionHelper;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.services.ILocalSelectionService;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.OSGIUtil;
import com.teamcenter.rac.util.Registry;
import com.unilever.rac.ui.common.UL4Common;

/**
 * @author s.yarrabothula
 *
 */
public class UL4PackCompReportHandler  extends AbstractHandler  implements IHandler{

	/** TCSession */
    private TCSession session                 = null ;  

    /** Shell  */
    private Shell shell                       = null ;   	

    /** PACK Revision */
    private TCComponent selection             = null ;

    /** The Registry  */
    private Registry reg                      = null ;
    
    private static int lock = 0;
    
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		if (UL4PackCompReportHandler.lock == 0) {
			UL4PackCompReportHandler.lock = 1;
			AIFDesktop desktop 					= AIFUtility.getActiveDesktop();
			this.shell                     	    = desktop.getShell();
			this.reg                     	    = Registry.getRegistry(this);
			System.out.println("<<PCR::DBG>> Invoked execute on UL4PackCompReportHandler ...");
			// get selected component
			ILocalSelectionService localSelectionService = (ILocalSelectionService)OSGIUtil.getService(AifrcpPlugin.getDefault(), ILocalSelectionService.class);
			ISelection localSelection = localSelectionService.getSelection( "com.teamcenter.rac.leftHandNavigator.selection");
			if (localSelection == null || localSelection.isEmpty())
				localSelection = HandlerUtil.getCurrentSelection(event);
			InterfaceAIFComponent[] aifComps = SelectionHelper.getTargetComponents(localSelection);
			this.selection = (TCComponent) aifComps[0];
			String parentType = this.selection.getTypeComponent().getParent().toString();
			
			try {
				this.session = (TCSession) AIFUtility.getSessionManager().getSession( "com.teamcenter.rac.kernel.TCSession");
			
				if(parentType.equals(UL4Common.PACKREVISION)) {
					UL4PackCompReportOperation pdf = new UL4PackCompReportOperation(this.selection, this.session);
					this.session.queueOperation(pdf);
				} else {
					MessageBox.post(shell , reg.getString("invalidselection"),reg.getString("error"),MessageBox.ERROR);
				}
			} catch (Exception exc) {
				System.out.println("<<PCR::DBG>> Error occured in getting session ...");
				exc.printStackTrace();
				MessageBox.post(shell ,exc.getMessage(),reg.getString("error"),MessageBox.ERROR);	
			}
			UL4PackCompReportHandler.lock = 0;
		} else {
			System.out.println("<<PCR::DBG>> Report generation is in progress. please wait...");
		}
		return null;
	}

}
