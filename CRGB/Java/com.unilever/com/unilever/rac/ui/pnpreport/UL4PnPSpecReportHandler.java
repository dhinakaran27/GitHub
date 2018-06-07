/* Source File Name:   UL4PnPSpecReportHandler
 *
 * Description:  This file contains code to generate PAM Specification PDF Report
 * 
 * Revision History:
 * -------------------------------------------------------------------------
 * Revision    Author                Date Created           Reason
 * -------------------------------------------------------------------------
 *  1.0.0       Dhinakaran.V          30/09/2014            Initial Creation
 *
 */

package com.unilever.rac.ui.pnpreport;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.aifrcp.AifrcpPlugin;
import com.teamcenter.rac.aifrcp.SelectionHelper;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentProject;
import com.teamcenter.rac.kernel.TCComponentProjectType;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.services.ILocalSelectionService;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.OSGIUtil;
import com.teamcenter.rac.util.Registry;
import com.unilever.rac.ui.common.UL4Common;


/**
 * The Class UL4PnPSpecReportHandler.
 */


public class UL4PnPSpecReportHandler extends AbstractHandler  implements IHandler
{
	
	/** TCSession */
    private TCSession session                 = null ;  

    /** Shell  */
    private Shell shell                       = null ;   	

    /** PACK Revision or PAM Revision   */
    private TCComponent selection             = null ;

    /** The Registry  */
    private Registry reg                      = null ;

    /** Logged User Name  */
    private String sessionUser 				  = null;

    /** Logged Role Name  */
    private String sessonRole				  = null ;

    /** Logged Group Name  */
    private String sessionGroup 			  = null ;
   
   
    public Object execute(ExecutionEvent event) throws ExecutionException
    {        
    	AIFDesktop desktop 					= AIFUtility.getActiveDesktop();
    	shell                         	    = desktop.getShell();
    	reg 								= Registry.getRegistry( this );    	
    	TCComponentItemRevision  packrevision    = null ;
    	TCComponentItemRevision  pamrevision    = null ;
    	
    	try 
    	{
    		ILocalSelectionService localILocalSelectionService = (ILocalSelectionService)OSGIUtil.getService(AifrcpPlugin.getDefault(), ILocalSelectionService.class);
    		ISelection localISelection = localILocalSelectionService.getSelection( "com.teamcenter.rac.leftHandNavigator.selection" );
		
			session = (TCSession) AIFUtility.getSessionManager().getSession( "com.teamcenter.rac.kernel.TCSession" );
			if ((localISelection == null) || (localISelection.isEmpty())) 		   
		       localISelection = HandlerUtil.getCurrentSelection(event);		    		     
		        
		    InterfaceAIFComponent[] arrayOfInterfaceAIFComponent = SelectionHelper.getTargetComponents(localISelection);
			selection = (TCComponent) arrayOfInterfaceAIFComponent[0];

			String parentType = selection.getTypeComponent().toString();
			
			if(parentType.equals(UL4Common.PNPREVISION))
			{
				
				//AIFComponentContext[] aifcomponent = selection.whereReferenced() ;

				AIFComponentContext[] aifcomponent = selection.getPrimary() ;
				
				for( int inx=0 ; inx < aifcomponent.length ; inx++)
				{
					TCComponent component = (TCComponent) aifcomponent[inx].getComponent() ;
					
					if( component instanceof TCComponentItemRevision && ((TCComponentItemRevision)component).getTypeComponent().getType().equals(UL4Common.DDEREVISION) )
					{
						packrevision = (TCComponentItemRevision) component ;
						pamrevision = (TCComponentItemRevision) selection;
						break;
					}
				}
				
				if(packrevision == null)
				{
					MessageBox.post(shell ,reg.getString("parentmissing"),reg.getString("error"),MessageBox.ERROR);	
				}
			}
			else if(parentType.equals(UL4Common.DDEREVISION))
			{
				AIFComponentContext[] pamcomponent  = selection.getRelated(UL4Common.PNPSPECIFICATION);
				
				if(pamcomponent.length == 0)
				{
					MessageBox.post(shell , reg.getString("pammissing"),reg.getString("error"),MessageBox.ERROR);	
				}
				else
				{		
					packrevision = (TCComponentItemRevision) selection ;
					pamrevision = (TCComponentItemRevision) pamcomponent[0].getComponent();
				}
			}
			else
			{
				MessageBox.post(shell , reg.getString("invalidselection"),reg.getString("error"),MessageBox.ERROR);		
			}

			if(packrevision != null && pamrevision != null)
			{	
				boolean imgRequired = false ;
				
				if(imageDatasetAvialable(packrevision))
				{
					Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
	    			   org.eclipse.swt.widgets.MessageBox  messageBox = new org.eclipse.swt.widgets.MessageBox(shell, SWT.ICON_QUESTION
	    			            | SWT.YES | SWT.NO);
	    			        messageBox.setMessage("Generate the Report with drawings ? ");
	    			        messageBox.setText("Image Conversion.");
	    			        int response = messageBox.open();
	    			        if (response == SWT.YES)
	    			        	imgRequired = true ;
				}
				
				
				UL4PnPSpecReportOperation pdf = new UL4PnPSpecReportOperation(packrevision,pamrevision,session,imgRequired);
				//pdf.executeOperation();
				session.queueOperation(pdf);
			} 
    	} 
    	catch (Exception e)
    	{
    		MessageBox.post(shell ,e.getMessage(),reg.getString("error"),MessageBox.ERROR);	
		}
  	      	 
        return null;
    }
    
    /**
     * 
     * Function to check logged user is privileged team member in assigned TC Project
     * 
     * @return true / false
     */
    
    private boolean isPrivilegedUsers() 
	{	
		TCComponentProject project = null ;     
        boolean validUser = false ;
        String pid  = null ;
		
		if(selection != null)
		{
			try
			{				
				sessionUser  = session.getUserName();	
				sessionGroup = session.getGroup().getStringProperty(UL4Common.NAME);
				sessonRole   = session.getRole().getStringProperty(UL4Common.ROLENAME);
				
				if(sessonRole.equals(UL4Common.DBA))
					return true ;

				pid   = selection.getStringProperty(UL4Common.TCPROJECTIDS);

				if( pid == null || pid.length() <= 1)
				{
					selection.refresh();
					pid   = selection.getStringProperty(UL4Common.TCPROJECTIDS);
				}
				
				if(pid != null && pid.length() > 1)
				{					
					String[] projectsId =  pid.split(",");
					
					TCComponentProjectType projectType = (TCComponentProjectType)(session.getTypeComponent(UL4Common.TCPROJECT));
					
					for(int nProj = 0; nProj < projectsId.length; nProj++)
					{
						project = projectType.find(projectsId[nProj].trim());
						
						if(projectType.isPrivilegedMember(project, session.getUser()))
							return true ;
					}					
		
				}
				else
					project = null ;
			} 
			catch (TCException e) 
			{
				MessageBox.post(shell, e.getMessage() ,reg.getString("error"), MessageBox.ERROR);
			}
		}
		
		if(!validUser)
		{
			if(project==null)
				MessageBox.post(shell, reg.getString("tcprojectnotfound") ,reg.getString("error"), MessageBox.ERROR);
			else
				MessageBox.post(shell, "TC Project < " + pid.trim() + " > " + reg.getString("invaliduser") ,reg.getString("error"), MessageBox.ERROR);
		}
		
		
		return validUser ;
	}
    
    public boolean imageDatasetAvialable(TCComponentItemRevision  revision)
 	{
 		boolean image = false ;
 		
 		try
 		{
 			AIFComponentContext[] component = revision.getRelated(UL4Common.IMANSPECIFICATION);			
 				
 			for( int inx=0 ; inx < component.length ; inx++)
 			{
 				if( component[inx].getComponent()  instanceof TCComponentDataset)
 				{
 					String type = component[inx].getComponent().getType() ;
 					
 					if(type.equals("U4_StructuredPDF") || type.equals("U4_PrintPDF")  || type.equals("PDF"))
 						return true ;
 				}				
 			}
 		}
 		catch (TCException e)
 		{
 			image = false ;	
 		}
 		
 		return image ;	
 		
 	}
}