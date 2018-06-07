/**===================================================================================
 *             
 *                     Unpublished - All rights reserved
 * ===================================================================================
 * File Description: UL4CADComponentHandler.java
 * This is a class used to create handler for the New CAD Component feature.
 * 
 * ===================================================================================
 * Revision History
 * ===================================================================================
 * Date         	Name       			  TCEng-Release  	Description of Change
 * ------------   --------------------	  -------------    ---------------------------
 * 22-Aug-2014	  Jayateertha M Inamdar   TC10.1.1.1        Initial Version
 * 
 *  $HISTORY$
 * ===================================================================================*/

package com.unilever.rac.ui.newcadcomponent;

import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;
import org.xml.sax.SAXException;

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

public class UL4CADComponentHandler extends AbstractHandler implements IHandler
{
	private ArrayList<String> alPropName      = null ;	
    private TCSession session                 = null;    
   	private Shell shell                       = null ;   	
    private ArrayList<String> alFormProp	  = null ;
    private TCComponent selection             = null ;
    
    public UL4CADComponentHandler()
    {
    	alPropName = new ArrayList<String>();
    	alFormProp = new ArrayList<String>();
    	alPropName.clear();
    	alFormProp.clear();
    	//Registry.getRegistry("com.unilever.rac.ui.newcadcomponent.newcadcomponent");		
    }

	public Object execute(ExecutionEvent event) throws ExecutionException
    {        
    	AIFDesktop desktop 					= AIFUtility.getActiveDesktop();
    	shell                         	    = desktop.getShell();
        
		try 
		{
			 ILocalSelectionService localILocalSelectionService = (ILocalSelectionService)OSGIUtil.getService(AifrcpPlugin.getDefault(), ILocalSelectionService.class);
			 ISelection localISelection = localILocalSelectionService.getSelection( "com.teamcenter.rac.leftHandNavigator.selection" );
			 session = (TCSession) AIFUtility.getSessionManager().getSession( "com.teamcenter.rac.kernel.TCSession" );
  	      	 
		     if ((localISelection == null) || (localISelection.isEmpty())) 		   
		        localISelection = HandlerUtil.getCurrentSelection(event);		    		     
        
		    InterfaceAIFComponent[] arrayOfInterfaceAIFComponent = SelectionHelper.getTargetComponents(localISelection);
			selection = (TCComponent) arrayOfInterfaceAIFComponent[0];
			
			UL4CADComponentDialog dailog= new UL4CADComponentDialog(shell,session,selection);
			dailog.open();
		
		}
		catch (ParserConfigurationException e) 
		{
			MessageBox.post(shell, e.getMessage() ,"Error", MessageBox.ERROR);
		}
		catch (SAXException e) 
		{
			MessageBox.post(shell, e.getMessage() ,"Error", MessageBox.ERROR);
		} 
		catch (IOException e)
		{
			MessageBox.post(shell, e.getMessage() ,"Error", MessageBox.ERROR);
		}
		catch (Exception e) 
		{
			MessageBox.post(shell, e.getMessage() ,"Error", MessageBox.ERROR);
		}
		
        return null;
    }
}
