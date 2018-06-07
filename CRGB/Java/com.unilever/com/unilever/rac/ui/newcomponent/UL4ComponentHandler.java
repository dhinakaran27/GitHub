/* Source File Name:   UL4ComponentHandler
 *
 * Description:  This file contains code to build GMC Pack Component Creation UI Dialog 
 * Revision History:
 * -------------------------------------------------------------------------
 * Revision    Author                Date Created           Reason
 * -------------------------------------------------------------------------
 *  1.0.0       Dhinakaran.V          30/09/2014            Initial Creation
 *
 */

package com.unilever.rac.ui.newcomponent;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.aifrcp.AifrcpPlugin;
import com.teamcenter.rac.aifrcp.SelectionHelper;
import com.teamcenter.rac.kernel.BOCreatePropertyDescriptor;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentDatasetType;
import com.teamcenter.rac.kernel.TCComponentForm;
import com.teamcenter.rac.kernel.TCComponentFormType;
import com.teamcenter.rac.kernel.TCComponentGroup;
import com.teamcenter.rac.kernel.TCComponentGroupMember;
import com.teamcenter.rac.kernel.TCComponentProject;
import com.teamcenter.rac.kernel.TCComponentProjectType;
import com.teamcenter.rac.kernel.TCComponentQuery;
import com.teamcenter.rac.kernel.TCComponentQueryType;
import com.teamcenter.rac.kernel.TCComponentTcFile;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCFormProperty;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.services.ILocalSelectionService;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.OSGIUtil;
import com.teamcenter.rac.util.Registry;
import com.unilever.rac.ui.common.UL4Common;


/**
 * The Class UL4ComponentHandler.
 */

public class UL4ComponentHandler extends AbstractHandler  implements IHandler
{
	private ArrayList<String> alPropName      = null ;	
    private TCSession session                 = null ;    
   	private Shell shell                       = null ;   	
    private Registry reg                      = null ;
    private TCComponentForm form 			  = null ;
    private TCComponentFormType ftype      	  = null ;
    private ArrayList<String> alFormProp	  = null ;
    private NodeList nodes 		              = null ;
    private TCComponent selection             = null ;
    private String sessionUser 				  = null ;
    private String sessonRole				  = null ;
    private String sessionGroup 			  = null ;
    private String[][] hiddenAttributes       = null ;
    
    public UL4ComponentHandler()
    {
    	alPropName = new ArrayList<String>();
    	alFormProp = new ArrayList<String>();
    	alPropName.clear();
    	alFormProp.clear();
    	reg = Registry.getRegistry( this );
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
			
			if(isPrivilegedUsers() && getFormDetails())
			{				
				//BMIDE configured to auto populate REVISION NAME
				if(alPropName.contains(UL4Common.OBJECT_NAME))
					alPropName.remove(UL4Common.OBJECT_NAME);
				
				//Custom Hidden attribute if any added to from
				if(getHiddenAttribue())
				{
					UL4ComponentDialog dailog= new UL4ComponentDialog(shell,session,alPropName,hiddenAttributes,selection,form , nodes);
					dailog.open(); 
				}
			}
		
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
	
    /**
     * 
     * Function to check logged user is privileged team member in assigned TC Project
     * 
     * @return true / false
     */
	

	private boolean getHiddenAttribue() throws TCException
	{	
		
		@SuppressWarnings("deprecation")
		String[] prefValue = session.getPreferenceService().getStringArray(TCPreferenceService.TC_preference_site, UL4Common.PREF_HIDDENATTRIBUTE);
		
		hiddenAttributes = new String[prefValue.length][3];
		
		for ( int inx=0 ; inx<prefValue.length;inx++)
		{	
			String []str = prefValue[inx].trim().split("#") ;
			
			if(str.length == 3 )
			{						
				for( int iny = 0 ;iny < 3 ; iny++)
				{
					if(alFormProp.contains(str[iny]))
					{
						hiddenAttributes[inx][iny] = str[iny];
					}
					else
					{
						String msg =  reg.getString("prefinvalidhiddenattribute");
						MessageBox.post(shell, msg + " < " + str[iny] + " >" ,"Error", MessageBox.ERROR);
						return false;
					}							
				}
			}
			else
			{
				String msg =  reg.getString("invalidhiddenattribute");
				MessageBox.post(shell, msg + "\n" + prefValue[inx] ,"Error", MessageBox.ERROR);
				return false;
			}	
		}
				
		for( int iny = 0 ;iny < hiddenAttributes.length ; iny++)
		{
			if(!alPropName.contains(hiddenAttributes[iny][0]))
			{
				String msg =  reg.getString("stylesheetsourcepropmissing") + " < " +  hiddenAttributes[iny][0]  + " >" +  "\nDataset / Stylesheet  < " +  UL4Common.STYLESHEETNAME;
				MessageBox.post(shell, msg ,"Error", MessageBox.ERROR);
				return false;
			}
			if(!alPropName.contains(hiddenAttributes[iny][2]))
			{
				String msg =  reg.getString("stylesheetsourcepropmissing") + " < " +  hiddenAttributes[iny][2]  + " >" +  "\nDataset / Stylesheet  < " +  UL4Common.STYLESHEETNAME;
				MessageBox.post(shell, msg ,"Error", MessageBox.ERROR);
				return false;
			}
			if(alPropName.contains(hiddenAttributes[iny][1]))
			{
				String msg =  reg.getString("stylesheetinvalidprop") + " < " +  hiddenAttributes[iny][1]  + " >" +  "\nDataset / Stylesheet  < " +  UL4Common.STYLESHEETNAME;
				MessageBox.post(shell, msg ,"Information", MessageBox.INFORMATION);
				alPropName.remove(hiddenAttributes[iny][1]);
			}
		}		
		
		return true ;
	}


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

				if( pid == null || (  (pid.length() <= 1)  && pid.equalsIgnoreCase(" ")))
				{
					selection.refresh();
					pid   = selection.getStringProperty(UL4Common.TCPROJECTIDS);
				}
				
				if(pid != null &&   (  pid.length() >= 1  &&   ! pid.equalsIgnoreCase(" ")  ))
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
	
	/**
	 * 
	 * Download Stylesheet which contain Required UI Attribute Details
	 * 
	 * @return fileName
	 */
				
	private String  getDataset()
	{
		String fileName = null ;
		TCComponentDataset dataset = null;

		try 
		{
			TCComponentDatasetType datasetType = (TCComponentDatasetType)session.getTypeComponent(UL4Common.DATASET);
			dataset = datasetType.find(UL4Common.STYLESHEETNAME);
            
			if ( dataset != null )
			{			
				TCComponentTcFile relatedTcFiles[] = dataset.getTcFiles();	                	
		    	
		    	if(relatedTcFiles != null && relatedTcFiles.length != 0)
		    	{	
		    		File file = ((TCComponentTcFile)relatedTcFiles[0]).getFile( null );
		    		fileName = file.getAbsolutePath();
		    	}
			}
    	} 
		catch (TCException e) 
		{
			MessageBox.post(shell, e.getMessage() ,reg.getString("error"), MessageBox.ERROR);
		}
			
		return fileName ;
		
	}
	
	
    /**
     *  Validate  UI Attribute availability
     * 
     * @return
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
	
	private boolean getFormDetails() throws ParserConfigurationException, SAXException, IOException
	{		
		boolean flag = false ;			
		String file = null ;
		alPropName.clear();
	
		if( (file = getDataset()) != null )
		{	    	
			try
	        {	
				ftype = (TCComponentFormType) session.getTypeComponent(UL4Common.FORM);
				form = ftype.create( "", "", UL4Common.FORM,false );				
				TCFormProperty[] str = form.getAllFormProperties();
				
				for ( int inx = 0 ; inx < str.length ; inx++)					
					alFormProp.add(str[inx].getPropertyName().toString());	
				
				//WSO Required Attribute
	
				alFormProp.add(UL4Common.OBJECT_DESC);
				
				DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder(); 
				Document doc = builder.parse(file); 
				Element element = doc.getDocumentElement();
			    nodes  = element.getElementsByTagName("*");	

			    NodeList names = element.getElementsByTagName("property");
			    
			    for ( int inx=0 ; inx < names.getLength();inx++)
			    {
			    	String val = ((Element) names.item(inx)).getAttribute("name") ;
			    	
			    	if(! alPropName.contains(val))
			    		alPropName.add(val);
			    }			    
			  
				if (alPropName.size() == 0 )
				{
					MessageBox.post(shell, reg.getString("stylesheetpropmissing"),"Information", MessageBox.INFORMATION);
				}
				else if( !alPropName.contains(UL4Common.PAM_FRAME))
				{	
					MessageBox.post(shell, " < " + UL4Common.PAM_FRAME + " > " + reg.getString("propmissing"),"Error", MessageBox.ERROR);
				}
				else if( !alPropName.contains(UL4Common.TECHNOLOGY))
				{
					MessageBox.post(shell, " < " + UL4Common.TECHNOLOGY + " > "+ reg.getString("propmissing"),"Error", MessageBox.ERROR);
				}	
				else
				{
					flag = true;
				}
			}
			catch(ParserConfigurationException pce) 
			{
				MessageBox.post(shell, pce.getMessage() ,reg.getString("error"), MessageBox.ERROR);
			}
			catch(SAXException se) 
			{
				MessageBox.post(shell, se.getMessage() ,reg.getString("error"), MessageBox.ERROR);
			}
			catch(IOException ioe)
			{
				MessageBox.post(shell, ioe.getMessage() ,reg.getString("error"), MessageBox.ERROR);
			}
			catch (TCException e)
			{
				MessageBox.post(shell, e.getMessage() ,reg.getString("error"), MessageBox.ERROR);
			}
		}
		else
		{
			MessageBox.post(shell,  " < " + UL4Common.STYLESHEETNAME + " > " + reg.getString("datasetmissing"),"Information", MessageBox.INFORMATION);
		}
		
		return flag;
 	}	
	
}
