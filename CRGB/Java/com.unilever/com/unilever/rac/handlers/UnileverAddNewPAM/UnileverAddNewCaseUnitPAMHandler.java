package com.unilever.rac.handlers.UnileverAddNewPAM;

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
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentProject;
import com.teamcenter.rac.kernel.TCComponentProjectType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.services.ILocalSelectionService;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.OSGIUtil;
import com.teamcenter.rac.util.Registry;
import com.unilever.rac.ui.common.UL4Common;

public class UnileverAddNewCaseUnitPAMHandler extends AbstractHandler {

	private Registry reg 					= null;
	private TCSession session 				= null;
	private String sessionUser 				= null;
    private String sessonRole				= null;
    private String sessionGroup 			= null;
	
	@Override
	public Object execute( ExecutionEvent event ) throws ExecutionException {
		
		session = null;
		reg = Registry.getRegistry(this );
		Shell shell = AIFDesktop.getActiveDesktop().getShell();
		
		 ILocalSelectionService localILocalSelectionService = (ILocalSelectionService)OSGIUtil.getService(AifrcpPlugin.getDefault(), ILocalSelectionService.class);
		 ISelection localISelection = localILocalSelectionService.getSelection( "com.teamcenter.rac.leftHandNavigator.selection" );
		 try {
			session = (TCSession) AIFUtility.getSessionManager().getSession( "com.teamcenter.rac.kernel.TCSession" );
		} catch (Exception e) {
			e.printStackTrace();
		}
		 
		 //Map mParamMap = event.getParameters();
		
		if ((localISelection == null) || (localISelection.isEmpty())) 		   
	        localISelection = HandlerUtil.getCurrentSelection(event);		    		     
    
	    InterfaceAIFComponent[] arrayOfInterfaceAIFComponent = SelectionHelper.getTargetComponents(localISelection);
		TCComponentItemRevision selection = (TCComponentItemRevision) arrayOfInterfaceAIFComponent[0];
		
		//Include a check for P&PSpec Revision, if its RELEASED then throw an error stating - You can not perform this action on Released object.
		try
		{
			String releaseStatus = selection.getProperty(UL4Common.RELEASE_STATUS_LIST_ATTR);
			if( UL4Common.RELEASED_STATUS.equals(releaseStatus) )
			{
				org.eclipse.swt.widgets.MessageBox mb = new org.eclipse.swt.widgets.MessageBox(AIFUtility.getActiveDesktop().getShell(), SWT.ICON_ERROR);
				mb.setText(reg.getString("ERROR_TITLE"));
				mb.setMessage(reg.getString("WRITE_ACCESS_ERROR"));
				mb.open();	
				return null;
			}
		}
		catch(TCException ex)
		{
			ex.printStackTrace();
		}
		
		//Invoke below dialog if the user is part of privileged user for relevant project.
		if(isPrivilegedUsers(selection))
		{
			new UnileverAddNewPAMDialog( shell, SWT.APPLICATION_MODAL | SWT.CLOSE, selection, session, UL4Common.PnP_TO_PAM_CASEUNIT_RELATION).open();			
		}		
		
		return null;
	}

	/**
     * 
     * Function to check logged user is privileged team member in assigned TC Project
     * 
     * @return true / false
     */
    
    private boolean isPrivilegedUsers(TCComponentItemRevision selection) 
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
				MessageBox.post(AIFUtility.getActiveDesktop().getShell(), e.getMessage() ,reg.getString("error"), MessageBox.ERROR);
			}
		}
		
		if(!validUser)
		{
			if(project==null)
				MessageBox.post(AIFUtility.getActiveDesktop().getShell(), reg.getString("tcprojectnotfound") ,reg.getString("error"), MessageBox.ERROR);
			else
				MessageBox.post(AIFUtility.getActiveDesktop().getShell(), "TC Project < " + pid.trim() + " > " + reg.getString("invaliduser") ,reg.getString("error"), MessageBox.ERROR);
		}
		
		
		return validUser ;
	}

}
