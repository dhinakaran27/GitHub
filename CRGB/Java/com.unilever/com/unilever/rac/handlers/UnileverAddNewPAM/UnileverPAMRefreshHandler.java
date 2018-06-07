package com.unilever.rac.handlers.UnileverAddNewPAM;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.aifrcp.AifrcpPlugin;
import com.teamcenter.rac.aifrcp.SelectionHelper;
import com.teamcenter.rac.commands.cut.CutOperation;
import com.teamcenter.rac.commands.paste.PasteOperation;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.services.ILocalSelectionService;
import com.teamcenter.rac.util.OSGIUtil;
import com.teamcenter.rac.util.Registry;
import com.unilever.rac.ui.common.UL4Common;

public class UnileverPAMRefreshHandler extends AbstractHandler 
{
	private Set<TCComponent> setOfAlreadyExistingPAMSpec 			= new HashSet<TCComponent>();
	private TCSession session 										= null;
	private boolean hasInvalidStatusForCU		 					= false;
	private boolean hasInvalidStatusForDU		 					= false;
	private boolean hasInvalidStatusForCaseUnit						= false;
	private Registry reg											= null;
	private String cuPAMSpecIDAndRevID 								= "";
	private String duPAMSpecIDAndRevID 								= "";
	private String caseUnitPAMSpecIDAndRevID 						= "";
	private String strCUWarningMessage 								= "";
	private String strDUWarningMessage 								= "";
	private String strCaseUnitWarningMessage 						= "";
	
	@Override
	public Object execute( ExecutionEvent event ) throws ExecutionException {
		
		setOfAlreadyExistingPAMSpec.clear();
		reg = Registry.getRegistry( this );
		
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
		
		
		loadAllSecondaryPAMSpecs(selection);
		
		return null;
	}
	
	private void loadAllSecondaryPAMSpecs(TCComponentItemRevision selection) 
	{
		try
		{
			hasInvalidStatusForCU 		= false;
			hasInvalidStatusForDU 		= false;
			hasInvalidStatusForCaseUnit = false;
						
			cuPAMSpecIDAndRevID = "";
			duPAMSpecIDAndRevID = "";
			caseUnitPAMSpecIDAndRevID = "";
			
			strCUWarningMessage = "";
			
			//Get all the associated PAMSpecs with 3 different set of relations and load the latest revisions of it.
			
			//Step#1: For CU Relation
			session.setStatus( "Refreshing Consumer Unit PAM Specs..." );
			setOfAlreadyExistingPAMSpec.clear();
			
			TCComponent[] relatedCUPAMSpecRevisions = selection.getRelatedComponents(UL4Common.PnP_TO_PAM_CU_RELATION);
			AIFComponentContext[] aifComponentCUArr = selection.getRelated(UL4Common.PnP_TO_PAM_CU_RELATION);
			
			if(relatedCUPAMSpecRevisions != null && relatedCUPAMSpecRevisions.length != 0)
			{
				for(TCComponent eachComp : relatedCUPAMSpecRevisions)
				{
					TCComponentItemRevision currentPAMSpecRevision = (TCComponentItemRevision) eachComp;
					if( isLatestValidRevision((TCComponentItemRevision)currentPAMSpecRevision) )
					{
						//Check if the latest Revision has Obsolete or Retired status, if so make the suitable flag to TRUE
						String strRelStatus = currentPAMSpecRevision.getTCProperty( UL4Common.RELEASE_STATUS_LIST_ATTR ).toString();
						
						if( UL4Common.OBSOLETE_RELEASED_STATUS.equalsIgnoreCase(strRelStatus) || 
								UL4Common.RETIRED_RELEASED_STATUS.equalsIgnoreCase(strRelStatus) )
						{
							hasInvalidStatusForCU = true;
							
							cuPAMSpecIDAndRevID = " ";
							cuPAMSpecIDAndRevID = currentPAMSpecRevision.getTCProperty(UL4Common.ITEMID).toString();
							cuPAMSpecIDAndRevID = cuPAMSpecIDAndRevID + "/" + currentPAMSpecRevision.getTCProperty(UL4Common.REVID).toString() + " , ";
						}
						
						continue;
					}
					
					//Perform replace - Replace the older revision with the latest one.
					//Do Cut
					try
                    {
						for(AIFComponentContext aifComp : aifComponentCUArr)
						{
							if(aifComp.getComponent() instanceof TCComponentItemRevision)
							{
								if(aifComp.getComponent() == currentPAMSpecRevision)
								{
									//Perform cut
									CutOperation cutOperation = new CutOperation(aifComp, false);
									cutOperation.executeOperation();
								}
							}
						}
                    }
                    catch( TCException e )
                    {
                        // cut failed, but if the childCmp is in
                        // more than one folders, it's possible it's
                        // already cut from other folders, so need
                        // to paste it back
                    	PasteOperation paste2 = new PasteOperation(	AIFUtility.getCurrentApplication(),selection,currentPAMSpecRevision,UL4Common.PnP_TO_PAM_CU_RELATION);	
                    	paste2.executeOperation();

                        throw e;
                    }
					
					//Perform Paste
					setOfAlreadyExistingPAMSpec.add( currentPAMSpecRevision.getItem().getLatestItemRevision() ); 
				}
			}
			
			if(hasInvalidStatusForCU)
				strCUWarningMessage = "Consumer Unit - " + cuPAMSpecIDAndRevID;
			else
				strCUWarningMessage = "";
			
			
			if(setOfAlreadyExistingPAMSpec.size() != 0)
			{
				PasteOperation paste2 = new PasteOperation(	AIFUtility.getCurrentApplication(),selection,
															(TCComponent[])setOfAlreadyExistingPAMSpec.toArray(new TCComponent[setOfAlreadyExistingPAMSpec.size()]), 
																UL4Common.PnP_TO_PAM_CU_RELATION);	
				paste2.executeOperation();
				
			}
			
			//Step#2: For DU Relation
			session.setStatus( "Refreshing Delivery Unit PAM Specs..." );
			setOfAlreadyExistingPAMSpec.clear();
			TCComponent[] relatedDUPAMSpecRevisions = selection.getRelatedComponents(UL4Common.PnP_TO_PAM_DU_RELATION);
			AIFComponentContext[] aifComponentDUArr = selection.getRelated(UL4Common.PnP_TO_PAM_DU_RELATION);
			
			if(relatedDUPAMSpecRevisions != null && relatedDUPAMSpecRevisions.length != 0)
			{
				for(TCComponent eachComp : relatedDUPAMSpecRevisions)
				{
					TCComponentItemRevision currentPAMSpecRevision = (TCComponentItemRevision) eachComp;
					if(isLatestValidRevision((TCComponentItemRevision)currentPAMSpecRevision))
					{
						//Check if the latest Revision has Obsolete or Retired status, if so make the suitable flag to TRUE
						String strRelStatus = currentPAMSpecRevision.getTCProperty( UL4Common.RELEASE_STATUS_LIST_ATTR ).toString();
						
						if( UL4Common.OBSOLETE_RELEASED_STATUS.equalsIgnoreCase(strRelStatus) || 
								UL4Common.RETIRED_RELEASED_STATUS.equalsIgnoreCase(strRelStatus) )
						{
							hasInvalidStatusForDU = true; 
							
							duPAMSpecIDAndRevID = " ";
							duPAMSpecIDAndRevID = currentPAMSpecRevision.getTCProperty(UL4Common.ITEMID).toString();
							duPAMSpecIDAndRevID = duPAMSpecIDAndRevID + "/" + currentPAMSpecRevision.getTCProperty(UL4Common.REVID).toString() + " , ";
						}
						
						continue;
					}
					
					//Perform replace - Replace the older revision with the latest one.
					//Do Cut
					try
                    {
						for(AIFComponentContext aifComp : aifComponentDUArr)
						{
							if(aifComp.getComponent() instanceof TCComponentItemRevision)
							{
								if(aifComp.getComponent() == currentPAMSpecRevision)
								{
									//Perform cut
									CutOperation cutOperation = new CutOperation(aifComp, false);
									cutOperation.executeOperation();
								}
							}
						}
                    }
                    catch( TCException e )
                    {
                        // cut failed, but if the childCmp is in
                        // more than one folders, it's possible it's
                        // already cut from other folders, so need
                        // to paste it back
                    	PasteOperation paste2 = new PasteOperation(	AIFUtility.getCurrentApplication(),selection,currentPAMSpecRevision,
                    																							UL4Common.PnP_TO_PAM_DU_RELATION);	
                    	paste2.executeOperation();

                        throw e;
                    }
					
					//Perform Paste
					setOfAlreadyExistingPAMSpec.add( currentPAMSpecRevision.getItem().getLatestItemRevision() ); 
				}
			}
			
			if(hasInvalidStatusForDU)
				strDUWarningMessage = "Distribution Unit - " + duPAMSpecIDAndRevID;
			else
				strDUWarningMessage = "";
			
			if(setOfAlreadyExistingPAMSpec.size() != 0)
			{
				PasteOperation paste2 = new PasteOperation(	AIFUtility.getCurrentApplication(),selection,
															(TCComponent[])setOfAlreadyExistingPAMSpec.toArray(new TCComponent[setOfAlreadyExistingPAMSpec.size()]), 
																UL4Common.PnP_TO_PAM_DU_RELATION);	
				paste2.executeOperation();
				
			}
			
			//Step#3: For Case Unit Relation
			session.setStatus( "Refreshing Case Unit PAM Specs..." );
			setOfAlreadyExistingPAMSpec.clear();
			TCComponent[] relatedCaseUnitPAMSpecRevisions = selection.getRelatedComponents(UL4Common.PnP_TO_PAM_CASEUNIT_RELATION);
			AIFComponentContext[] aifComponentCaseUnitArr = selection.getRelated(UL4Common.PnP_TO_PAM_CASEUNIT_RELATION);
			
			if(relatedCaseUnitPAMSpecRevisions != null && relatedCaseUnitPAMSpecRevisions.length != 0)
			{
				for(TCComponent eachComp : relatedCaseUnitPAMSpecRevisions)
				{
					TCComponentItemRevision currentPAMSpecRevision = (TCComponentItemRevision) eachComp;
					if(isLatestValidRevision((TCComponentItemRevision)currentPAMSpecRevision))
					{
						//Check if the latest Revision has Obsolete or Retired status, if so make the suitable flag to TRUE
						String strRelStatus = currentPAMSpecRevision.getTCProperty( UL4Common.RELEASE_STATUS_LIST_ATTR ).toString();
						
						if( UL4Common.OBSOLETE_RELEASED_STATUS.equalsIgnoreCase(strRelStatus) || 
								UL4Common.RETIRED_RELEASED_STATUS.equalsIgnoreCase(strRelStatus) )
						{
							hasInvalidStatusForCaseUnit = true;
							
							strCaseUnitWarningMessage = " ";
							strCaseUnitWarningMessage = currentPAMSpecRevision.getTCProperty(UL4Common.ITEMID).toString();
							strCaseUnitWarningMessage = strCaseUnitWarningMessage + "/" + currentPAMSpecRevision.getTCProperty(UL4Common.REVID).toString() + " , ";
						}
						
						continue;
					}
					
					//Perform replace - Replace the older revision with the latest one.
					//Do Cut
					try
                    {
						for(AIFComponentContext aifComp : aifComponentCaseUnitArr)
						{
							if(aifComp.getComponent() instanceof TCComponentItemRevision)
							{
								if(aifComp.getComponent() == currentPAMSpecRevision)
								{
									//Perform cut
									CutOperation cutOperation = new CutOperation(aifComp, false);
									cutOperation.executeOperation();
								}
							}
						}
                    }
                    catch( TCException e )
                    {
                        // cut failed, but if the childCmp is in
                        // more than one folders, it's possible it's
                        // already cut from other folders, so need
                        // to paste it back
                    	PasteOperation paste2 = new PasteOperation(	AIFUtility.getCurrentApplication(),selection,currentPAMSpecRevision,
                    																							UL4Common.PnP_TO_PAM_CASEUNIT_RELATION);	
                    	paste2.executeOperation();

                        throw e;
                    }
					
					//Perform Paste
					setOfAlreadyExistingPAMSpec.add( currentPAMSpecRevision.getItem().getLatestItemRevision() ); 
				}
			}
			
			if(hasInvalidStatusForCaseUnit)
				strCaseUnitWarningMessage = "Case Unit - " + caseUnitPAMSpecIDAndRevID;
			else
				strCaseUnitWarningMessage = "";
		
			if(setOfAlreadyExistingPAMSpec.size() != 0)
			{
				PasteOperation paste2 = new PasteOperation(	AIFUtility.getCurrentApplication(),selection,
															(TCComponent[])setOfAlreadyExistingPAMSpec.toArray(new TCComponent[setOfAlreadyExistingPAMSpec.size()]), 
																UL4Common.PnP_TO_PAM_CASEUNIT_RELATION);	
				paste2.executeOperation();				
			}
			
			session.setStatus( "Loading Complete." );
			
			if(hasInvalidStatusForCU || hasInvalidStatusForDU || hasInvalidStatusForCaseUnit)
			{	
				String strMsg = reg.getString("WARNING_MSG_INVALID_STATUS_FOUND_UNDER_CONSUMER_UNIT") + "\n\t";
				
				if( !"".equals(strCUWarningMessage) )
				{
					strMsg = strMsg + strCUWarningMessage + "\n";	
				}
				if( !"".equals(strDUWarningMessage) )
				{
					strMsg = strMsg + strDUWarningMessage + "\n";
				}
				if( !"".equals(strCaseUnitWarningMessage) )
				{
					strMsg = strMsg + strCaseUnitWarningMessage + "\n";
				}
				
				org.eclipse.swt.widgets.MessageBox mb = new org.eclipse.swt.widgets.MessageBox(AIFUtility.getActiveDesktop().getShell(), SWT.ICON_ERROR);
				mb.setText(reg.getString("WARNING_TITLE"));
				mb.setMessage(strMsg);
				mb.open();	
				return;

			}
		}
		catch(TCException ex)
		{
			ex.printStackTrace();
		}
	}
	
	/**
	* This method verifies if the passed item revision is latest revision
	* of its corresponding item
	* @param Item revision component
	* @return boolean value indicating if this revision is latest of its parent item & has valid released status found (RELEASED OR WORKING)
	* @author Jayateertha M Inamdar
	*/
	protected boolean isLatestValidRevision(TCComponentItemRevision revComponent) throws TCException
	{
		if(revComponent==null)
		 return false;
		
		// Retrieve parent item
		TCComponentItem itemComponent = revComponent.getItem();
		
		if(itemComponent != null)
		{
			// Get latest item revision of this item
			TCComponentItemRevision latestRevComponent = itemComponent.getLatestItemRevision();
			
			// Validate if passed revision is latest revision
			if(latestRevComponent == revComponent)
			{
				//Check if the latest Revision is RELEASED OR WORKING - then only show in the dialog.
				String releaseStatus = latestRevComponent.getProperty(UL4Common.RELEASE_STATUS_LIST_ATTR);
				if(UL4Common.RELEASED_STATUS.equals(releaseStatus) || "".equals(releaseStatus))
				{
					return true;					
				}
			}
		}	
		
		return false;
	}

}
