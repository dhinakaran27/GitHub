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

package com.unilever.rac.ui.saveascomponent;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Comparator;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.commands.genericsaveas.ISaveAsService;
import com.teamcenter.rac.common.Activator;
import com.teamcenter.rac.kernel.ListOfValuesInfo;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentForm;
import com.teamcenter.rac.kernel.TCComponentGroup;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentListOfValues;
import com.teamcenter.rac.kernel.TCComponentListOfValuesType;
import com.teamcenter.rac.kernel.TCComponentProject;
import com.teamcenter.rac.kernel.TCComponentRole;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.ui.commands.handlers.SaveASHandler;
import com.teamcenter.rac.util.AdapterUtil;
import com.teamcenter.rac.util.Cookie;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.OSGIUtil;
import com.teamcenter.rac.util.Registry;
import com.teamcenter.rac.util.UIUtilities;
import com.teamcenter.rac.util.wizard.extension.BaseExternalWizard;
import com.teamcenter.rac.util.wizard.extension.BaseExternalWizardDialog;
import com.teamcenter.rac.util.wizard.extension.WizardExtensionHelper;
import com.teamcenter.services.rac.core.LOVService;
import com.teamcenter.services.rac.core.ProjectLevelSecurityService;
import com.teamcenter.services.rac.core._2009_10.ProjectLevelSecurity.ProjectInfo;
import com.teamcenter.services.rac.core._2009_10.ProjectLevelSecurity.UserProjectsInfo;
import com.teamcenter.services.rac.core._2009_10.ProjectLevelSecurity.UserProjectsInfoInput;
import com.teamcenter.services.rac.core._2013_05.LOV.InitialLovData;
import com.teamcenter.services.rac.core._2013_05.LOV.LOVSearchResults;
import com.teamcenter.services.rac.core._2013_05.LOV.LOVValueRow;
import com.unilever.rac.ui.common.UL4Common;
import com.unilever.rac.util.UnileverPreferenceUtil;
import com.unilever.rac.util.UnileverQueryUtil;

public class UL4ComponentSaveAsHandler extends com.teamcenter.rac.ui.commands.handlers.SaveASHandler implements IHandler
{
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
	//private String lastSyncRevisionIDFromSelectedInstance;
	/** Latest Released Rev Id for CR#144 **/
	private String latestReleasedRevisionID;
	/** Flag to identify whether Latest Rev of Pack Template is Released for CR#144 **/
	private boolean isLatestPackTemplateInValid = false;
	/** Pack Component Latest Rev Id for CR#144 **/
	private String itemIdOfLatestIR;
	/** Pack Component Latest Object Type for CR#144 **/
	private String objTypeOfLatestIR;
    
	  public Object execute(ExecutionEvent paramExecutionEvent)
	    throws ExecutionException
	  {
	    ISelection localISelection = HandlerUtil.getCurrentSelection(paramExecutionEvent);
	    if ((localISelection instanceof StructuredSelection))
	    {
	      StructuredSelection localStructuredSelection = (StructuredSelection)localISelection;
	      if (localStructuredSelection.size() != 1)
	      {
	        MessageBox.post(HandlerUtil.getActiveWorkbenchWindow(paramExecutionEvent).getShell(), com.teamcenter.rac.ui.commands.Messages.getString("toomanyObjects.MSG"), com.teamcenter.rac.ui.commands.Messages.getString("saveAs.TITLE"), 4);
	      }
	      else
	      {
	        this.m_selectedCmp = ((InterfaceAIFComponent)AdapterUtil.getAdapter(localISelection, InterfaceAIFComponent.class));
	        if ((this.m_selectedCmp instanceof TCComponent))
	        {
	          Object[] arrayOfObject = { ((TCComponent)this.m_selectedCmp).toDisplayString() };
	          this.m_selectedCmp = getUnderlyingComponent((TCComponent)this.m_selectedCmp);
	          if (this.m_selectedCmp != null)
	          {
	            Shell localShell = HandlerUtil.getActiveShell(paramExecutionEvent);
	            if (localShell != null)
	            {
	            	TCComponent selection = (TCComponent) this.m_selectedCmp;
	    			if(this.m_selectedCmp instanceof TCComponentItemRevision)
	    			{
	    				try 
	    				{
	    					reg = Registry.getRegistry( this );    	
	    					
	    					TCComponentItemRevision selectedItemRevision = (TCComponentItemRevision) this.m_selectedCmp;
	    					TCSession session = selectedItemRevision.getSession();
	    					
	    					TCComponentGroup group = session.getCurrentGroup();
	    					TCComponentRole role = session.getCurrentRole();
	    					TCComponentUser user = session.getUser();
	    					
	    					TCComponentProject[] projects = getProjects(user, group, role, session);
	    					if(projects != null && projects.length > 0)
	    					{
	    						//Show custom dialog
	    	    				//Get the Parent Type as well.
	    	    				String[] types = new String[1];
	    	    				types[0] = UL4Common.PACKREVISION;
	    	    				
	    	    				isSelectedFromDevTab = false;
	    	    				isLatestPackTemplateInValid = false;
	    	    				
	    	    				String sParentType = selectedItemRevision.isSubtypeOf(types);
	    	    				if(selectedItemRevision instanceof TCComponentItemRevision)
	    						{
	    	    					iResponseCodeForWarn = 0;
	    	    					String sType = selectedItemRevision.getType();
	    							
	    							if( (sParentType != null && sType != null) || UL4Common.DDEREVISION.equals(sType) || UL4Common.CAD_COMPONENT_ITEM_REV.equals(sType) )	    								
		    						{
	    								/**	Below condition implies for Components
	    								 * 	Included below check for CR# 144 - Error Message on Save As of PACK COMPONENTS when current Technology has been removed or replaced.
	    								 */
	    								if(sParentType != null && !"".equals(sParentType))
	    								{
	    									/**
	    									 * 	Get the Material Classification form to read below listed properties:
	    									 * 	Use - U4_MatlClassRelation
	    									 */
	    									TCComponentForm materialClassificationForm = (TCComponentForm) selectedItemRevision.getRelatedComponent(UL4Common.GMCFORMRELATION);
	    									if(materialClassificationForm != null)
	    									{
	    										/**
	    										 * 	1. Identify the Item Type.
	    										 *  2. Get the preference (U4_GMCPackComponentType) value, and compare with the last token (split on #) whether it matches the Item Type of the 
	    										 *     selected pack component with the 1.
	    										 *  3. Get the value from "UL4Common.PAM_FRAME", "UL4Common.TECHNOLOGY" from the Source - Material Classification Form.
	    										 *  4. And compare values from 3 with the tokens 1 and 2 from above preference values.
	    										 *  
	    										 */
	    										
    											String pamFrameTypeVal = (String) materialClassificationForm.getTCProperty(UL4Common.PAM_FRAME).toString();
    											String technologyVal = (String)materialClassificationForm.getTCProperty(UL4Common.TECHNOLOGY).toString();
												
												// Throw an error if the selected pack revision has Cancelled Status 
    											String releaseStatus = selectedItemRevision.getProperty(UL4Common.RELEASE_STATUS_LIST_ATTR);
    											String[] statusTokens = releaseStatus.split(",");
    											
    											for( String status : statusTokens)
    											{
	    	    									if("Cancelled".equals(status))
	    	    									{
	    	    										//Throw Error - The selected component technology “TEMPLATE NAME” is no longer valid. Save As cannot complete.
	    											  	org.eclipse.swt.widgets.MessageBox mb2 = new org.eclipse.swt.widgets.MessageBox(localShell, SWT.ICON_ERROR);
	    		    									mb2.setText(reg.getString("ERROR_TITLE"));
	    		    									//mb2.setMessage(reg.getString("ERROR_MSG_SAVE_AS_INVALID_TEMPLATE_STMT_1")+" " +"\""+ technologyVal +"\""+ " " + reg.getString("ERROR_MSG_SAVE_AS_INVALID_TEMPLATE_STMT_2"));
	    		    									mb2.setMessage(reg.getString("ERROR_MSG_SAVE_AS_INVALID_TEMPLATE_STMT_1")+" " +"\""+ technologyVal +"\""+ " " + reg.getString("ERROR_MSG_SAVE_AS_INVALID_TEMPLATE_STMT_2") + "\n\n" + reg.getString("ERROR_MSG_SAVE_AS_INVALID_TEMPLATE_STMT_3"));
	    		    									mb2.open();	
	    		    									return null;
	    	    									}
    											}
												
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
    								    									//mb.setMessage(reg.getString("ERROR_MSG_SAVE_AS_INVALID_TEMPLATE_STMT_1")+" " +"\""+ technologyVal +"\""+ " " + reg.getString("ERROR_MSG_SAVE_AS_INVALID_TEMPLATE_STMT_2"));
    								    									mb.setMessage(reg.getString("ERROR_MSG_SAVE_AS_INVALID_TEMPLATE_STMT_1")+" " +"\""+ technologyVal +"\""+ " " + reg.getString("ERROR_MSG_SAVE_AS_INVALID_TEMPLATE_STMT_2") + "\n\n" + reg.getString("ERROR_MSG_SAVE_AS_INVALID_TEMPLATE_STMT_3"));
    								    									mb.open();	
    								    									return null;
        															  }
        														  }
    														  }
    													  }//or(String singlePrefValue : prefValues) ends												  
    												  }//if(prefValues.length != 0) ends
    											}//if(pamFrameTypeVal != null && technologyVal != null) ends
	    									}//if(materialClassificationForm != null) ends
	    									
	    									/**
		    								 * 	Included below check for CR# 144 - Warning Message on Save As when component is not on latest template Revision.
		    								 *  
		    								 * 		- Step#1 : Get the object-type of newly created PACK Component, e.g. U4_ClsrPOther
		    								 *  	- Step#2 : Go to the preference (U4_PackComponent_Template) - and search for that object-type, e.g. U4_ClsrPOther::C510000000000
		    								 *  	- Step#3 : Retrieve the Template Item ID against that object-type from the pref-value, e.g. C510000000000
		    								 *  	- Step#4 : Search the relevant Template Item ID (C510000000000) and get its latest revision.
		    								 *  	- Step#5 : Check, whether latest revision is released or not and verify the value of attr - "u4_last_sync_revision_id" on the selected pack component instance, and match it against the latest released revision.
		    								 *  	- Step#6 : If above comparison is not matched, throw warning message.
		    								 *  			   The selected component is not on the latest template revision. A more recent revision has been released for this component: “TEMPLATE NAME/LATEST REVISION”.
		    								 */
		    								
		    								// Step#1
		    								String selectedItemType = selectedItemRevision.getItem().getType();
		    								
		    								// Step#2
		    								String[] prefValues = UnileverPreferenceUtil.getStringPreferenceValues(TCPreferenceService.TC_preference_site, 
																															UL4Common.U4_PACK_COMPONENT_PREFERENCE);
		    								if(prefValues.length != 0)
		    								{
		    									//PackComponentType::Template Item ID
		    									for(String singlePrefValue : prefValues)
		    									{
		    										if(singlePrefValue.toLowerCase().contains(selectedItemType.toLowerCase()))
		    										{
		    											// Step#3
		    											prefTokens = singlePrefValue.split("::");	    											
		    											System.out.println(prefTokens[1]);
		    											
		    											break;
		    										}
		    									}
		    								}
		    								
		    								// Step#4 - Search the PACK Component Template ID ( e.g. C510000000000 in DB), get the latest Released revision from it. 
		    								String entries[] = { UL4Common.ITEM_ID_ATTR_DISP_NAME };
		    								String values[] = { prefTokens[1] };
		    								TCComponent[]  components = UnileverQueryUtil.executeQuery(UL4Common.ITEM_ID_ATTR_DISP_NAME, entries, values);
		    								//boolean isReleased = false;
		    								
		    								if(components != null && components.length == 1)
		    								{
		    									TCComponentItem packTemplateItem = (TCComponentItem)components[0]; 
		    									//TCComponentItemRevision packTemplateLatestReleasedItemRevision = packTemplateItem.getLatestItemRevision();
		    									TCComponent[] irs = packTemplateItem.getRelatedComponents(UL4Common.REVISION_LIST_ATTR);
		    									if(irs.length > 0)
		    									{
		    										for(TCComponent ir : irs)
		    										{
		    											if(ir instanceof TCComponentItemRevision)
		    											{
		    												// Step#5 - Get Released Status value = Released	    												  
		    												String releaseStatus = ir.getProperty(UL4Common.RELEASE_STATUS_LIST_ATTR);
		    												if("Released".equals(releaseStatus))
		    													latestReleasedRevisionID = (String) ir.getTCProperty(UL4Common.REVID).toString();
		    													objTypeOfLatestIR = (String) ir.getTCProperty(UL4Common.OBJECT_TYPE).toString();
		    											}
		    										}
		    									}
		    								}
											
											//lastSyncRevisionIDFromSelectedInstance = (String) selectedItemRevision.getTCProperty(UL4Common.TEMPLATE_LAST_SYNC_REV_ID).toString();
											
											//if(lastSyncRevisionIDFromSelectedInstance.equals(latestReleasedRevisionID) == false)
											//	isLatestPackTemplateInValid = true;
											//else
											//	isLatestPackTemplateInValid = false;

		    								
		    								// Step#6 - Throw Warning Msg
		    								/*if(isLatestPackTemplateInValid)
		    								{
			    								MessageDialog warnMsgDialog = null;

			    								warnMsgDialog = new MessageDialog(null,reg.getString("INFO_TITLE"), null,
		    																			reg.getString("WARNING_MSG_SAVE_AS_INVALID_TEMPLATE_STMT_1") + 
		    																			" \"" +objTypeOfLatestIR+"/"+latestReleasedRevisionID +"\"" + " \n\n" + 
		    																			reg.getString("WARNING_MSG_SAVE_AS_INVALID_TEMPLATE_STMT_2"), MessageDialog.CONFIRM,
		    																			new String[]{ "Continue", "Cancel"}, 0);
			    								warnMsgDialog.open();

			    								iResponseCodeForWarn = warnMsgDialog.getReturnCode();
		    									
		    								}*/


	    									/**
		    								 * Identify if the selection is based on Dev Tab or Final Tab.
		    								 */
	  								
		    								//AIFComponentContext[] aifcomponent = selection.whereReferenced() ;
											AIFComponentContext[] aifcomponent = selection.getPrimary() ;
		    								
		    								for( int inx=0 ; inx < aifcomponent.length ; inx++)
		    								{
		    									TCComponent component = (TCComponent) aifcomponent[inx].getComponent() ;
		    									
		    									if( component instanceof TCComponentItemRevision && ((TCComponentItemRevision)component).getTypeComponent().getType().equals(UL4Common.PROJECTREV) )
		    									{
		    										designProjectCompIR = (TCComponentItemRevision) component;
		    										break;
		    									}
		    								}
		    								
		    								if(designProjectCompIR != null)
		    								{
		    									if( checkIfProjectIsAssociatedWithPackComponent(designProjectCompIR, UL4Common.DEVRELATION, selectedItemRevision) )
		    									{
		    										/*org.eclipse.swt.widgets.MessageBox mb = new org.eclipse.swt.widgets.MessageBox(localShell, SWT.ICON_INFORMATION);
				    							    mb.setText("selected from dev");
				    							    mb.setMessage("selected from dev");
				    							    mb.open();*/
				    							    isSelectedFromDevTab = true;
		    									}
		    									else if( checkIfProjectIsAssociatedWithPackComponent(designProjectCompIR, UL4Common.FINAL_RELATION, selectedItemRevision) )
		    									{
		    										/*org.eclipse.swt.widgets.MessageBox mb = new org.eclipse.swt.widgets.MessageBox(localShell, SWT.ICON_INFORMATION);
				    							    mb.setText("selected from final");
				    							    mb.setMessage("selected from final");
				    							    mb.open();*/
				    							    isSelectedFromDevTab = false;
		    									}
		    									
		    								}		    								
		    								
		    								/**  
		    								 * Check 1: when someone selects a revision with say PDL status, which may have a latest revision on Final Components tab, 
		    								 * 			we should provide a warning message (user has not selected the latest rev of comp) and carry with Save As operation with old selected IR.
		    								 */
		    								
		    								TCComponentItem selectedItem = (TCComponentItem)selectedItemRevision.getItem(); 
		    								TCComponentItemRevision selectedItemLatestRevision = selectedItem.getLatestItemRevision();
		    								
		    								String itemId = selectedItemRevision.getTCProperty(UL4Common.ITEMID).toString();
		    								String revId = selectedItemRevision.getTCProperty(UL4Common.REVID).toString();
		    								MessageDialog msgDialog = null;
		    								if(selectedItemLatestRevision != selectedItemRevision)
		    								{
		    								
		    									String itemIdOfLatestIR = selectedItemLatestRevision.getTCProperty(UL4Common.ITEMID).toString();
			    								String revIdOfLatestIR = selectedItemLatestRevision.getTCProperty(UL4Common.REVID).toString();
			    								
		    									msgDialog = new MessageDialog(null,reg.getString("INFO_TITLE"), null,
		    											reg.getString("WARNING_MSG_SAVE_AS_OLDER_REV_FOUND") +" \"" +itemIdOfLatestIR+"/"+revIdOfLatestIR +"\"" + " \n\n " + reg.getString("WARNING_MSG_CONFIRMATION"), MessageDialog.CONFIRM,
			    							    	                new String[]{ "Continue", "Cancel"}, 0);
		    									msgDialog.open();

		    									iResponseCode = msgDialog.getReturnCode();
			    							    
		    								}
		    								
		    								if(iResponseCode == 0)
		    								{
		    									//Now set the boolean flag here, if user selected the pack component from Dev Tab or from Final Tab.
			    								if(UL4Common.CAD_COMPONENT_ITEM_REV.equals(sType))
			    									isSelectedFromDevTab = true;
			    								
			    								//Now, check if selected Pack Component Revision has LCS Rejected or PDL Rejected status, if so then throw error.
			    								
			    								String strRelStatus = selectedItemRevision.getTCProperty( UL4Common.RELEASE_STATUS_LIST_ATTR ).toString();
			    								
			    								if( UL4Common.LEAD_CONCEPT_REJECTED_STATUS.equalsIgnoreCase(strRelStatus) || 
			    									UL4Common.PRODUCT_DESIGN_REJECTED_STATUS.equalsIgnoreCase(strRelStatus) ||
			    									UL4Common.PILOT_LOCK_REJECTED_STATUS.equalsIgnoreCase(strRelStatus) )
			    								{
			    									org.eclipse.swt.widgets.MessageBox mb = new org.eclipse.swt.widgets.MessageBox(localShell, SWT.ICON_ERROR);
			    									mb.setText(reg.getString("ERROR_TITLE"));
			    									mb.setMessage(reg.getString("ERROR_MSG_SAVE_AS_CANNOT_CONTINUE_STMT_1") +"\""+strRelStatus  +"\""+ reg.getString("ERROR_MSG_SAVE_AS_CANNOT_CONTINUE_STMT_2"));
			    									mb.open();	
			    									return null;
			    								}
			    								
			    								UL4ComponentSaveAsCustomDialog dialog = new UL4ComponentSaveAsCustomDialog(localShell, selection.getSession(), selectedItemRevision, projects, isSelectedFromDevTab);
					    		    			dialog.open();	
		    								}
		    								else if(iResponseCode == 1)
		    									return null;

	    								}//if(sParentType != null && !"".equals(sParentType)) ends
	    								
	    								if(UL4Common.DDEREVISION.equals(sType) || UL4Common.CAD_COMPONENT_ITEM_REV.equals(sType) )
	    								{
	    									UL4ComponentSaveAsCustomDialog dialog = new UL4ComponentSaveAsCustomDialog(localShell, selection.getSession(), selectedItemRevision, projects, isSelectedFromDevTab);
				    		    			dialog.open();	
	    								}
	    								 
    									
		    						}//if( (sParentType != null && sType != null) || UL4Common.DDEREVISION.equals(sType) || UL4Common.CAD_COMPONENT_ITEM_REV.equals(sType) ) ends
	    							else
		    		    			{
		    		    				//Else show OOTB dialog
		    		  	              CreateSaveAsDialog localCreateSaveAsDialog = new CreateSaveAsDialog(localShell);
		    		  	              localCreateSaveAsDialog.run();
		    		    				
		    		    			}
	    						}
	    						
	    						else
	    		    			{
	    		    				//Else show OOTB dialog
	    		  	              CreateSaveAsDialog localCreateSaveAsDialog = new CreateSaveAsDialog(localShell);
	    		  	              localCreateSaveAsDialog.run();
	    		    				
	    		    			}
	    	    					    						
	    					}
	    					else
    		    			{
    		    				//Else show OOTB dialog
    		  	              CreateSaveAsDialog localCreateSaveAsDialog = new CreateSaveAsDialog(localShell);
    		  	              localCreateSaveAsDialog.run();
    		    				
    		    			}
	    					
						} 
	    				catch (Exception e) 
	    				{
							e.printStackTrace();
						}
		    				
	    			}
	    			else
	    			{
	    				//Else show OOTB dialog
	  	              CreateSaveAsDialog localCreateSaveAsDialog = new CreateSaveAsDialog(localShell);
	  	              localCreateSaveAsDialog.run();
	    				
	    			}
	    			
	            }
	          }
	          else
	          {
	            MessageBox.post(HandlerUtil.getActiveWorkbenchWindow(paramExecutionEvent).getShell(), MessageFormat.format(com.teamcenter.rac.ui.commands.Messages.getString("saveAsNotSupported.MESSAGE"), arrayOfObject), com.teamcenter.rac.ui.commands.Messages.getString("saveAs.TITLE"), 1);
	          }
	        }
	        else
	        {
	          MessageBox.post(HandlerUtil.getActiveWorkbenchWindow(paramExecutionEvent).getShell(), com.teamcenter.rac.ui.commands.Messages.getString("invalidSelection.MESSAGE"), com.teamcenter.rac.ui.commands.Messages.getString("saveAs.TITLE"), 1);
	        }
	      }
	    }
	    return null;
	  }

	  private String getPropertyValue(TCComponentForm form, String property)
	  {
		  try
		  {
			  form.getTCProperty(property);
			  
		  }
		  catch(TCException tcEx)
		  {
			  
		  }
		  
		  return "";
	  }
  
	  private boolean checkIfProjectIsAssociatedWithPackComponent(TCComponentItemRevision designProjectCompIR, String relationName, TCComponentItemRevision selectedItemRevision)
	  {
		  try
		  {
			  
			  AIFComponentContext[] aifCompIRs = (AIFComponentContext[]) designProjectCompIR.getRelated(relationName);
			  if(aifCompIRs.length != 0)
			  {
				  for(AIFComponentContext aifComp : aifCompIRs)
				  {
					  if( aifComp.getComponent() instanceof TCComponentItemRevision )
					  {
						  TCComponentItemRevision packComponent = (TCComponentItemRevision) aifComp.getComponent();
						  //For each related objects, check if the one we got is = to selectedItemRevision
						  if(packComponent == selectedItemRevision)
						  {
							  return true;
						  }
						  else
							  continue;  
					  }					  
				  }
			  }
			  	
			  return false;
		  }
		  catch(Exception tcEx)
		  {
			  tcEx.printStackTrace();
			  return false;
		  }

	  }
	  
	  /**
	     * This method gets the value of the site preference
	     * TC_show_all_user_projects
	     *
	     * @return true  - show all projects.
	     *         false - filter projects based on user's group and role.
	     *         unset - show all projects.
	     *
	     */
	    /*private boolean showAllUserPrivProjects()
	    {
	        if ( !UserSettingDialog.prefValueRead )
	        {
	            try
	            {
	                TCPreferenceService prefs = session.getPreferenceService();
	                UserSettingDialog.show_all_user_projects = prefs.getString(TCPreferenceService.TC_preference_site, UserSettingDialog.TC_SHOW_ALL_USER_PROJECTS);
	                UserSettingDialog.prefValueRead = true;
	            }
	            catch ( Exception ex )
	            {
	                ex.printStackTrace();
	            }
	        }
	        if ( UserSettingDialog.show_all_user_projects == null ||
	             UserSettingDialog.show_all_user_projects.equalsIgnoreCase( "" ) ||
	             UserSettingDialog.show_all_user_projects.equalsIgnoreCase( "true" ) )
	        {
	            return true;
	        }
	        return false;
	    }
	    */
	  
	  /**
	     * Get relavent user projects.
	     *
	     * @return TCComponentProject[] - project components.
	     * @published
	     */
	    protected TCComponentProject[] getProjects(TCComponentUser user, TCComponentGroup group, TCComponentRole role, TCSession session )
	    {
	        try
	        {
	            TCComponentProject[] projects = null;
	            //if (!showAllUserPrivProjects() && group != null && role != null)
	            /*if(user != null && group != null && role != null )
	            {
	                //Get the projects for the session user/group/role combination.
	                projects = getProjectsForGroupMember( user, group, role, session);
	            }
	            else
	            {
	                // return user's active projects
	                projects = getProjectsForUser( user, true, false, false, session );
	            }*/
	            projects = getProjectsForUser( user, true, false, false, session );

	            final Comparator projectNameOrder = new Comparator()
	            {
	                public int compare(Object o1, Object o2)
	                {
	                   TCComponentProject proj1 = (TCComponentProject)o1;
	                   TCComponentProject proj2 = (TCComponentProject)o2;

	                   String n1 = proj1.toString().toUpperCase();
	                   String n2 = proj2.toString().toUpperCase();

	                   return n1.compareTo( n2 );
	                }
	            };

	            Arrays.sort( projects, projectNameOrder );

	            return projects;
	        }
	        catch (Exception e )
	        {
	            MessageBox.post(e);
	        }

	        return null;
	    }
	  /**
       * Validate project privilege.
       *
       * @param proj the proj
       * @published
       */
      private void validateProjectPrivilege(TCComponentProject proj )
      {
          //check if the user is a privileged user in this project.
          //We could get team definition from the selected project and validate
          //if the user is a privileged team member. However, each time when
          //a project is selected, we have to make a server trip to get the team def.
          //The current solution is to cache the privileged projects for the user.
          //and compare the selected projects against the privileged projects.

          hasAssignPriv = false;
          for(int i=0; privilegedProjects != null && i < privilegedProjects.length; i++)
          {
              if( privilegedProjects[i].equals( proj ) )
              {
                  hasAssignPriv = true;
                  break;
              }
          }
      }

	  /**
	     * Method to get all possible projects for a specific user
	     * with a specified group-role combination
	     * @param user current User
	     * @param role role for which projects are needed
	     * @return All projects which have specified role setting OR null if nothing is found
	     */
	    public final TCComponentProject[] getProjectsForGroupMember(
	            final TCComponentUser user, final TCComponentGroup group,
	            final TCComponentRole role, TCSession session )
	    {
	        ProjectLevelSecurityService service = ProjectLevelSecurityService.getService( session );
	        com.teamcenter.services.rac.core._2009_04.ProjectLevelSecurity.LoadProjectDataForUserResponse data = service.loadProjectDataForUser(
	                user, group, role );
	        return data.applicableProjects;
	    }
	    
	    /**
	     * Method to get all possible projects for a specific user - It returns all the projects.
	     * with a specified group-role combination
	     * @param user current User
	     * @param role role for which projects are needed
	     * @return All projects which have specified role setting OR null if nothing is found
	     */
	    
	  public final TCComponentProject[] getProjectsForUser(
	            final TCComponentUser user, final boolean activeProjectsOnly,
	            final boolean privilegedProjectsOnly, final boolean programsOnly, TCSession session )
	    {
	        UserProjectsInfoInput input = new UserProjectsInfoInput();
	        input.user = user;
	        input.activeProjectsOnly = activeProjectsOnly;
	        input.privilegedProjectsOnly = privilegedProjectsOnly;
	        input.programsOnly = programsOnly;
	        input.clientId = "Call getProjectsForUser() at "+ new java.util.Date();

	        ProjectLevelSecurityService service = ProjectLevelSecurityService.getService( session );
	        com.teamcenter.services.rac.core._2009_10.ProjectLevelSecurity.UserProjectsInfoResponse resp = service.getUserProjects( new UserProjectsInfoInput[] { input } );

	        // Partial error happens, means no projects retrieved, so return null
	        if( resp.serviceData.sizeOfPartialErrors() != 0 )
	        {
	            String[] messages = resp.serviceData.getPartialError( 0 ).getMessages();
	            for( int j = 0; j < messages.length; ++j )
	            {
	                System.err.println( messages[j] );
	            }
	            //MessageBox.post( this, new TCException( messages ) );
	            return null;
	        }

	        if( resp.userProjectInfos.length > 0 )
	        {
	            UserProjectsInfo userProjectsInfo = resp.userProjectInfos[0];
	            ProjectInfo[] projectsInfo = userProjectsInfo.projectsInfo;

	            int projectsCount = projectsInfo.length;
	            TCComponentProject[] projects = new TCComponentProject[projectsCount];

	            for( int i = 0; i < projectsCount; i++ )
	            {
	                projects[i] = projectsInfo[i].project;
	            }

	            return projects;
	        }

	        return null;
	    }
	  
	  protected static void readDisplayParameters(Wizard paramWizard, WizardDialog paramWizardDialog)
	  {
	    String str1 = "DialogParameters";
	    String str2 = paramWizard.getClass().getName();
	    if (Cookie.exists(str1, true))
	      try
	      {
	        int i = 0;
	        int j = 0;
	        int k = 0;
	        int m = 0;
	        Cookie localCookie = Cookie.getCookie(str1, true);
	        i = localCookie.getNumber(str2 + ".x");
	        j = localCookie.getNumber(str2 + ".y");
	        k = localCookie.getNumber(str2 + ".w");
	        m = localCookie.getNumber(str2 + ".h");
	        if ((k > 0) && (m > 0))
	          paramWizardDialog.getShell().setBounds(i, j, k, m);
	      }
	      catch (Exception localException)
	      {
	      }
	  }

	  protected BaseExternalWizard getWizard()
	  {
	    String str = "com.teamcenter.rac.ui.commands.saveas.SaveAsWizard";
	    return WizardExtensionHelper.getWizard(str);
	  }

	  protected String getWizardTitle()
	  {
	    String str = null;
	    if ((this.m_selectedCmp instanceof TCComponent))
	    {
	      Object[] arrayOfObject = { ((TCComponent)this.m_selectedCmp).getTypeComponent().getDisplayType() };
	      str = MessageFormat.format(com.teamcenter.rac.ui.commands.Messages.getString("saveAswizard.TITLE"), arrayOfObject);
	    }
	    return str;
	  }

	  private TCComponent getUnderlyingComponent(TCComponent paramTCComponent)
	  {
	    try
	    {
	      if (paramTCComponent.isRuntimeType())
	        return paramTCComponent.getUnderlyingComponent().isRuntimeType() ? null : paramTCComponent.getUnderlyingComponent();
	    }
	    catch (TCException localTCException)
	    {
	      Logger.getLogger(SaveASHandler.class).error(localTCException.getLocalizedMessage(), localTCException);
	    }
	    return paramTCComponent;
	  }

	  private class CreateSaveAsDialog
	    implements Runnable
	  {
	    private final Shell m_shell;

	    private CreateSaveAsDialog(Shell arg2)
	    {
	      Object localObject = arg2;
	      this.m_shell = (Shell) localObject;
	    }

	    public void run()
	    {
	    	 
	      BaseExternalWizard localBaseExternalWizard = UL4ComponentSaveAsHandler.this.getWizard();
	      if (localBaseExternalWizard != null)
	      {
	        localBaseExternalWizard.setForcePreviousAndNextButtons(true);
	        ISaveAsService localISaveAsService = (ISaveAsService)OSGIUtil.getService(Activator.getDefault(), ISaveAsService.class);
	        
	        if (localISaveAsService != null)
	        {
	          localISaveAsService.setInput((TCComponent)UL4ComponentSaveAsHandler.this.m_selectedCmp);
	          localBaseExternalWizard.setWindowTitle(UL4ComponentSaveAsHandler.this.getWizardTitle());
	          localBaseExternalWizard.setContext(new StructuredSelection(UL4ComponentSaveAsHandler.this.m_selectedCmp));
	          Shell localShell1 = UIUtilities.getCurrentModalShell();
	          BaseExternalWizardDialog localBaseExternalWizardDialog = new BaseExternalWizardDialog(localShell1, localBaseExternalWizard);
	          localBaseExternalWizardDialog.create();
	          UL4ComponentSaveAsHandler.readDisplayParameters(localBaseExternalWizard, localBaseExternalWizardDialog);
	          Shell localShell2 = localBaseExternalWizardDialog.getShell();
	          UIUtilities.setCurrentModalShell(localShell2);
	          localBaseExternalWizardDialog.open();
	          UIUtilities.setCurrentModalShell(localShell1);
	        }
	        else
	        {
	          MessageBox.post(this.m_shell, com.teamcenter.rac.ui.commands.Messages.getString("saveAsServiceNotAvailble.MEG"), com.teamcenter.rac.ui.commands.Messages.getString("saveAs.TITLE"), 4);
	        }
	      } 
	    }
	  }
	}

