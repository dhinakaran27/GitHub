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

package com.unilever.rac.ui.revise;

import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.xml.sax.SAXException;

import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.aifrcp.AifrcpPlugin;
import com.teamcenter.rac.aifrcp.SelectionHelper;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentForm;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.services.ILocalSelectionService;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.OSGIUtil;
import com.teamcenter.rac.util.Registry;
import com.unilever.rac.ui.common.UL4Common;
import com.unilever.rac.ui.newcadcomponent.UL4CADComponentDialog;
import com.unilever.rac.util.UnileverPreferenceUtil;

/**
* 	UL4ValidateReviseHandler extends AbstractHandler, an IHandler base class.
*	 	@see org.eclipse.core.commands.IHandler
* 		@see org.eclipse.core.commands.AbstractHandler
*/

public class UL4ValidateReviseHandler extends AbstractHandler 
{
	private ArrayList<String> alPropName      = null ;	
    private TCSession session                 = null;    
   	private Shell shell                       = null ;   	
    private ArrayList<String> alFormProp	  = null ;
    private TCComponent selection             = null ;

	private InterfaceAIFComponent m_selectedCmp;
    /** Cache the active projects that the user is the privileged member of. */
    private TCComponent[] privilegedProjects;
    /** The has assign priv. */
    private boolean hasAssignPriv = false;
    /** Design Project Item **/
    private TCComponentItemRevision designProjectCompIR;
    /** Flag to identify the selection operation for Design Tab or Final Tab **/
    private boolean isSelectedFromDevTab = false;
    /** Registry **/
    private Registry reg = null;
    /** MessageDialog response code **/
    private int iResponseCode = 0;
    /** MessageDialog response code for CR#144 **/
    private int iResponseCodeForWarn = 0;
    /** Pack Component Preference Tokens **/
    private String[]  prefTokens;
    /** Last Sync Rev Id from Selected Pack Component Instance for CR#144 **/
	private String lastSyncRevisionIDFromSelectedInstance;
	/** Latest Released Rev Id for CR#144 **/
	private String latestReleasedRevisionID;
	/** Flag to identify whether Latest Rev of Pack Template is Released for CR#144 **/
	private boolean isLatestPackTemplateInValid = false;
	/** Pack Component Latest Rev Id for CR#144 **/
	private String itemIdOfLatestIR;
	
	/**
	 * The constructor.
	 */
	public UL4ValidateReviseHandler() 
	{
		
	}
	
	/**
	 * the command has been executed, so extract the needed information
	 * from the application context.
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		reg = Registry.getRegistry("com.unilever.rac.ui.revise.revise");
		
    	AIFDesktop desktop 					= AIFUtility.getActiveDesktop();
    	shell                         	    = desktop.getShell();
 
    	try 
		{/*
			 ILocalSelectionService localILocalSelectionService = (ILocalSelectionService)OSGIUtil.getService(AifrcpPlugin.getDefault(), ILocalSelectionService.class);
			 ISelection localISelection = localILocalSelectionService.getSelection( "com.teamcenter.rac.leftHandNavigator.selection" );
			 session = (TCSession) AIFUtility.getSessionManager().getSession( "com.teamcenter.rac.kernel.TCSession" );
  	      	 
		     if ((localISelection == null) || (localISelection.isEmpty())) 		   
		        localISelection = HandlerUtil.getCurrentSelection(event);		    		     
        
		    InterfaceAIFComponent[] arrayOfInterfaceAIFComponent = SelectionHelper.getTargetComponents(localISelection);
			selection = (TCComponent) arrayOfInterfaceAIFComponent[0];

			Shell localShell = HandlerUtil.getActiveShell(event);
			if(localShell != null)
			{
				TCComponentItemRevision selectedItemRevision = (TCComponentItemRevision) selection;
				TCSession session = selectedItemRevision.getSession();
				
				if(selectedItemRevision instanceof TCComponentItemRevision)
				{
					//Get the Parent Type as well.
					String[] types = new String[1];
					types[0] = UL4Common.PACKREVISION;
					
					String sParentType = selectedItemRevision.isSubtypeOf(types);
					String sType = selectedItemRevision.getType();
					
					*//**
					 * Included below check for CR# 144 - Error Message on Save As of PACK COMPONENTS when current Technology has been removed or replaced.
					 *//*
					if(sParentType != null && !"".equals(sParentType))
					{
						*//**
						 * 	Get the Material Classification form to read below listed properties:
						 * 	Use - U4_MatlClassRelation
						 *//*
						TCComponentForm materialClassificationForm = (TCComponentForm) selectedItemRevision.getRelatedComponent(UL4Common.GMCFORMRELATION);
						if(materialClassificationForm != null)
						{
							*//**
							 * 	1. Identify the Item Type.
							 *  2. Get the preference (U4_GMCPackComponentType) value, and compare with the last token (split on #) whether it matches the Item Type of the 
							 *     selected pack component with the 1.
							 *  3. Get the value from "UL4Common.PAM_FRAME", "UL4Common.TECHNOLOGY" from the Source - Material Classification Form.
							 *  4. And compare values from 3 with the tokens 1 and 2 from above preference values.
							 *  
							 *//*
							
							String pamFrameTypeVal = (String) materialClassificationForm.getTCProperty(UL4Common.PAM_FRAME).toString();
							String technologyVal = (String)materialClassificationForm.getTCProperty(UL4Common.TECHNOLOGY).toString();
							if(pamFrameTypeVal != null && technologyVal != null)
							{
								  String[] prefValues = UnileverPreferenceUtil.getStringPreferenceValues(TCPreferenceService.TC_preference_site, 
										  																	UL4Common.PREF_GMCPACKCOMPONENT);
								  if(prefValues.length != 0)
								  {
									  String selectedItemType = selectedItemRevision.getItem().getType();
									  //u4_pam_frame_types#u4_technology=PackComponentType
									  for(String singlePrefValue : prefValues)
									  {
										  String[] tokens = singlePrefValue.split("#");
										  
										  if(tokens.length != 0)
										  {
											  //Token[0] = u4_pam_frame_types
											  //Token[1] = u4_technology
											  //Token[2] = PackComponentType
											  if(selectedItemType.equals(tokens[2]))
											  {
												  if( ! (pamFrameTypeVal.equals(tokens[0]) ) || ! (technologyVal.equals(tokens[1])) )
												  {
													  	//Throw Error - The selected component technology “TEMPLATE NAME” is no longer valid. Save As cannot complete.
													  	org.eclipse.swt.widgets.MessageBox mb = new org.eclipse.swt.widgets.MessageBox(localShell, SWT.ICON_ERROR);
				    									mb.setText(reg.getString("ERROR_TITLE"));
				    									mb.setMessage(reg.getString("ERROR_MSG_SAVE_AS_INVALID_TEMPLATE_STMT_1") +"\""+ technologyVal +"\""+ reg.getString("ERROR_MSG_SAVE_AS_INVALID_TEMPLATE_STMT_2"));
				    									mb.open();	
				    									return null;
												  }
											  }
										  }
									  }//or(String singlePrefValue : prefValues) ends												  
								  }//if(prefValues.length != 0) ends
							}//if(pamFrameTypeVal != null && technologyVal != null) ends
						}//if(materialClassificationForm != null) ends
					}//if(sParentType != null && !"".equals(sParentType)) ends
					
				}
				
				MessageDialog.openInformation( window.getShell(), //"Revise Error", "The selected component technology \u201CTEMPLATE NAME\u201D is no longer valid. Revise cannot complete.");
						reg.getString("MessageDialogBox.TITLE"),
							reg.getString("MessageDialogBox.MESSAGE"));				
			}

		*/}
		catch (Exception e) 
		{
			MessageBox.post(shell, e.getMessage() ,"Error", MessageBox.ERROR);
		}

		return null;
	}
}