package com.unilever.rac.reftextsearch;

import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
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
import com.teamcenter.rac.util.OSGIUtil;

public class RefTextSearchHandler extends AbstractHandler {

	@Override
	public Object execute( ExecutionEvent event ) throws ExecutionException {
		
		TCSession session = null;
		Shell shell = AIFDesktop.getActiveDesktop().getShell();
		
		 ILocalSelectionService localILocalSelectionService = (ILocalSelectionService)OSGIUtil.getService(AifrcpPlugin.getDefault(), ILocalSelectionService.class);
		 ISelection localISelection = localILocalSelectionService.getSelection( "com.teamcenter.rac.leftHandNavigator.selection" );
		 try {
			session = (TCSession) AIFUtility.getSessionManager().getSession( "com.teamcenter.rac.kernel.TCSession" );
		} catch (Exception e) {
			e.printStackTrace();
		}
		 
		 Map mParamMap = event.getParameters();
		 

	     if ((localISelection == null) || (localISelection.isEmpty())) 		   
	        localISelection = HandlerUtil.getCurrentSelection(event);		    		     
    
	    InterfaceAIFComponent[] arrayOfInterfaceAIFComponent = SelectionHelper.getTargetComponents(localISelection);
		TCComponent selection = (TCComponent) arrayOfInterfaceAIFComponent[0];
		
		new RefTextSearchDialog( shell, SWT.APPLICATION_MODAL | SWT.CLOSE, selection, session, mParamMap ).open();
		
		return null;
	}

}
