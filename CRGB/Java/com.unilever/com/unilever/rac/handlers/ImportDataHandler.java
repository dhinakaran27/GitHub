package com.unilever.rac.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.swt.widgets.Shell;

import com.unilever.rac.dialogs.ImportDataDialog;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.common.DataExportCommon;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;

public class ImportDataHandler  extends AbstractHandler
{

	private InterfaceAIFComponent selectedComp = null;
    private Shell shell = null ; 
    
	DataExportCommon common = new DataExportCommon();
	private TCComponentItemRevision itemRevision = null;
	
	public Object execute(ExecutionEvent event) throws ExecutionException
    { 
        shell = AIFUtility.getActiveDesktop().getShell();  
        selectedComp = AIFUtility.getActiveDesktop().getCurrentApplication().getTargetComponent();
        
        //Cast to TCComponet as it can be any object from TC
        TCComponent comp= (TCComponent) selectedComp;
        
		if(comp != null)
		{
			
			/*Check if preference
			"U4_PackComponentRevisions" and its' values available
			 */
			if(common.isSupExPrefsAvailable() == false)
			{
				IWorkbenchWindow window;
				try 
				{
					window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
					MessageDialog.openError(window.getShell(),common.PrefMissing, common.PrefMissingMsg);
				}catch (ExecutionException e){ e.printStackTrace();}	
				return null;
			}
			
			if(common.isUnileverComponent(comp))
			{
				itemRevision = (TCComponentItemRevision) comp;
				
				try 
				{
					if(common.isUserPrevilegedOnComponent(itemRevision) == false)
					{
						IWorkbenchWindow window;
						try {
							window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
							MessageDialog.openError(window.getShell(), common.UserNotPrivileged, common.UserNotPrivilegedMsg);	
							}catch (ExecutionException e){ e.printStackTrace();}	
						return null;
					}
				} 
				catch (TCException e) 
				{
					System.out.println("***DataExport:Error in isUserPrevilegedOnComponent function "
							+ e.toString());
				}
	
				ImportDataDialog dialog = new ImportDataDialog(shell, itemRevision);
				dialog.open();

			}
			else
			{
				IWorkbenchWindow window;
				try {
					window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
					MessageDialog.openError(window.getShell(),common.WrongCompStatus, common.WrongCompMsg);
				}catch (ExecutionException e){ e.printStackTrace();}	
				return null;
			}
		}
		return null;
    }
}
	

	
