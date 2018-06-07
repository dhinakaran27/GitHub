package com.unilever.cad.createcad;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.SWT;

import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.common.Activator;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCSession;

public class CreateDatasetHandler extends AbstractHandler {

	@Override
	public Object execute( ExecutionEvent event ) throws ExecutionException {
		
		TCComponentItemRevision mSelected = (TCComponentItemRevision) Activator.getDefault().getSelectionMediatorService().getTargetComponents()[0];
		TCSession mSession = null;
		
		try {
			mSession = (TCSession) Activator.getDefault().getSessionService().getSession( "com.teamcenter.rac.kernel.TCSession" );
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		new CreateDatasetDialog( AIFDesktop.getActiveDesktop().getShell(), 
								SWT.APPLICATION_MODAL | SWT.CLOSE, mSelected, mSession ).open( );
		
		return null;
	}

}
