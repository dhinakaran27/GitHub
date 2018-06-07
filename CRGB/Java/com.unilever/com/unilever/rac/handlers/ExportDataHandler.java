package com.unilever.rac.handlers;

import java.util.Vector;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.common.DataExportCommon;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentBOMWindowType;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentRevisionRule;
import com.teamcenter.rac.kernel.TCComponentRevisionRuleType;
import com.teamcenter.rac.kernel.TCException;
import com.unilever.rac.dialogs.ExportDataDialog;

public class ExportDataHandler  extends AbstractHandler
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
			/*
			 * Check if preferences "U4_PackComponentRevisions" and its' values available
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
				
				try
				{
					Vector<TCComponent> vParentAttachments = new Vector<TCComponent>();
					Vector<TCComponent> vChildrenAttachments = new Vector<TCComponent>();
					
					vParentAttachments.addAll(common.getAttachments(itemRevision, common.RLN_SPEC));
					
					if(itemRevision.getTCProperty(common.PROP_HAS_CHILDREN).toString().isEmpty() == false)
					{
						TCComponentBOMWindow bw = null;
						TCComponentBOMLine blTop = null;

						try 
						{
							bw  = getAssemblyWindow(itemRevision);	
						} 
						catch (Exception e) 
						{
							System.out.println("***DataExport:Error in getAssemblyWindow function "
									+ e.toString());
						}
						
						try 
						{
							blTop  = getWindowTopLine(bw, itemRevision);	
						} 
						catch (Exception e) 
						{
							System.out.println("***DataExport:Error in getWindowTopLine function "
									+ e.toString());
						}
						
						try 
						{
							vChildrenAttachments.addAll(getChildrenAttachments(blTop));
							bw.close();
						} 
						catch (Exception e)
						{
							System.out.println("***DataExport:Error in getChildrenAttachments function "
									+ e.toString());
						}
					}
						
					ExportDataDialog dialog = new ExportDataDialog(shell, itemRevision, vParentAttachments, vChildrenAttachments);
					dialog.open();
				}
				catch (TCException e) 
				{
					System.out.println("***DataExport:Error in isCompStageValidForExchange function "
										+ e.toString());	
				}
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
	
	private TCComponentBOMWindow getAssemblyWindow(TCComponentItemRevision itemRevision) throws TCException 
	{
		TCComponentBOMWindowType bwt = 
				(TCComponentBOMWindowType) itemRevision.getSession().getTypeComponent("BOMWindow");
		TCComponentRevisionRuleType rrt =
				(TCComponentRevisionRuleType) itemRevision.getSession().getTypeComponent("RevisionRule");
		TCComponentRevisionRule rule = rrt.getDefaultRule();
		TCComponentBOMWindow bw = bwt.create(rule);

		return bw;
	}

	private TCComponentBOMLine getWindowTopLine(TCComponentBOMWindow bw, TCComponentItemRevision itemRevision) throws TCException 
	{
		bw.setWindowTopLine(null, itemRevision, null, null);
		TCComponentBOMLine blTop = bw.getTopBOMLine();
		return blTop;
	}

	private Vector<TCComponent> getChildrenAttachments(TCComponentBOMLine bomline) throws Exception 
	{		
		//System.out.println("in getChildrenAttachments");
		//System.out.println("bl_line_name "+ bomline.getTCProperty("bl_line_name").toString());
		
		Vector<TCComponent> vAttachments = new Vector<TCComponent>();
		
		AIFComponentContext[] children = bomline.getChildren();
		
		for (int c = 0; c < children.length; c++)
		{
			//System.out.println("bl_line_name2 " + children[c].getComponent().getProperty("bl_line_name").toString());
			//System.out.println("bl_has_children "+ children[c].getComponent().getProperty("bl_has_children").toString());
			
			if(children[c].getComponent().getProperty(common.PROP_BL_HAS_CHILDREN).toString().equals("True"))
			{
				TCComponent childBOMLine = (TCComponent) children[c].getComponent();
				TCComponent rev = childBOMLine.getReferenceProperty("bl_line_object");
				
				vAttachments.addAll(common.getAttachments((TCComponentItemRevision)rev, common.RLN_SPEC));
				
				getChildrenAttachments((TCComponentBOMLine) children[c].getComponent());
			}
			else
			{
				TCComponent childBOMLine = (TCComponent) children[c].getComponent();
				TCComponent rev = childBOMLine.getReferenceProperty("bl_line_object");
				
				vAttachments.addAll(common.getAttachments((TCComponentItemRevision)rev, common.RLN_SPEC));
			}
		}
		return vAttachments;
	}
}
	

	
