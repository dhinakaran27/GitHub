/* Source File Name:   UL4ExportToSAPHandler
 *
 * Description:  This file contains code to generate PAM Specification PDF Report
 * 
 * Revision History:
 * -------------------------------------------------------------------------
 * Revision    Author                Date Created           Reason
 * -------------------------------------------------------------------------
 *  1.0.0       Sushma.V          15/12/2015            Initial Creation
 *
 */

package com.unilever.rac.ui.exporttosap;


import java.util.Date;

import com.u4.services.rac.service.SpecService;
import com.u4.services.rac.service._2014_12.Spec.Send2SAPResponse;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;
import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.aifrcp.AifrcpPlugin;
import com.teamcenter.rac.aifrcp.SelectionHelper;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.services.ILocalSelectionService;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.OSGIUtil;
import com.teamcenter.rac.util.Registry;
import com.unilever.rac.ui.common.UL4Common;

/**
 * The Class UL4ExportToSAPHandler
 * 
 */


public class UL4ExportToSAPHandler extends AbstractHandler  implements IHandler
{
	
	/** TCSession */
    private TCSession session                 = null ;  

    /** Shell  */
    private Shell shell                       = null ;   	

    /** PACK Revision or PAM Revision   */
    private TCComponent selection             = null ;

    /** The Registry  */
    private Registry reg                      = null ;

    /** Pack Component revision  */	 
	public TCComponent packRevision		= null ;
	
	public String modified_xmlfile      = null;
   
  
    public Object execute(ExecutionEvent event) throws ExecutionException
    {      
    	AIFDesktop desktop 					= AIFUtility.getActiveDesktop();
    	shell                         	    = desktop.getShell();
    	reg 								= Registry.getRegistry( this ); 
   
       	TCComponentItemRevision  packrevision    = null ;
    	TCComponentItemRevision  pamrevision    = null ;
		boolean is_packcomponent = false;
  
    	try 
    	{
    		SpecService specsvr = SpecService.getService((TCSession)AIFDesktop.getActiveDesktop().getCurrentApplication().getSession());
    		ILocalSelectionService localILocalSelectionService = (ILocalSelectionService)OSGIUtil.getService(AifrcpPlugin.getDefault(), ILocalSelectionService.class);
    		ISelection localISelection = localILocalSelectionService.getSelection( "com.teamcenter.rac.leftHandNavigator.selection" );
		
			try {
			session = (TCSession) AIFUtility.getSessionManager().getSession( "com.teamcenter.rac.kernel.TCSession" );
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if ((localISelection == null) || (localISelection.isEmpty())) 		   
		       localISelection = HandlerUtil.getCurrentSelection(event);		    		     
		        
		    InterfaceAIFComponent[] arrayOfInterfaceAIFComponent = SelectionHelper.getTargetComponents(localISelection);
		    System.out.println("length:"+arrayOfInterfaceAIFComponent.length);
		    if(arrayOfInterfaceAIFComponent.length>1)
		    {
		    	MessageBox.post(shell , "Multiple Components Selected ",reg.getString("error"),MessageBox.ERROR);
		    }
		    else
		    {
			selection = (TCComponent) arrayOfInterfaceAIFComponent[0];
		
			String parentType = selection.getTypeComponent().getParent().toString();
			String classname = selection.getTypeComponent().getType().toString();
			System.out.println("ClassName is :"+classname);
				
			if(parentType.equals(UL4Common.PACKREVISION))
			{
				is_packcomponent = true;
				
				AIFComponentContext[] pamcomponent  = selection.getRelated(UL4Common.PAMSPECIFICATION);
				
				if(pamcomponent.length == 0)
				{
					MessageBox.post(shell , reg.getString("pammissing"),reg.getString("error"),MessageBox.ERROR);	
				}
				else
				{
					packrevision = (TCComponentItemRevision) selection ;
					pamrevision = (TCComponentItemRevision) pamcomponent[0].getComponent();
					String value = packrevision.getProperty("release_status_list");
					System.out.println("Value"+value);
					if(value.equals("Released"))
					{
						packrevision = null;
						MessageBox.post(shell , reg.getString("PamReleased"),reg.getString("error"),MessageBox.ERROR);
					}
					else
					{
						//AIFComponentContext[] aifcomponent = selection.whereReferenced() ;

						AIFComponentContext[] aifcomponent = selection.getPrimary() ;
						
						for( int inx=0 ; inx < aifcomponent.length ; inx++)
						{
							TCComponent component = (TCComponent) aifcomponent[inx].getComponent() ;
							
							if( component instanceof TCComponentItemRevision && ((TCComponentItemRevision)component).getTypeComponent().getType().equals("U4_ProdNoticeRevision") )
							{
								MessageBox.post(shell ,"Selected specification is still in Production Change Process.","Error",MessageBox.ERROR);	
								packrevision = null;
								break;
							}
						}
					}
				}
			}
			else if(classname.equals(UL4Common.DDEREVISION))
			{
                 AIFComponentContext[] pnpcomponent  = selection.getRelated(UL4Common.PNPSPECIFICATION);
                 if(pnpcomponent.length == 0)
 				{
                	 packrevision = null;
 					MessageBox.post(shell , reg.getString("pnpmissing"),reg.getString("error"),MessageBox.ERROR);	
 				}
                 else
                 {
                	 packrevision = (TCComponentItemRevision) selection ;
						String value = packrevision.getProperty("release_status_list");
						System.out.println("Value"+value);
					if ((value.length()<=0))
					{											
						if(packrevision.getItem().getReleasedItemRevisions().length == 0 )
						{
							pamrevision = (TCComponentItemRevision) pnpcomponent[0].getComponent();
							   AIFComponentContext[] LOPlist  = pamrevision.getRelated("U4_ListOfPAMRelation");
							   int lopcount =0;
							   lopcount = LOPlist.length;
							  					        
							   if(lopcount<=0)
							   {
								   packrevision = null;
									MessageBox.post(shell , reg.getString("lopnotfound"),reg.getString("error"),MessageBox.ERROR);
							   }
						}
						else
						{
							packrevision = null;
							MessageBox.post(shell ,"Selected specification has already released revision." ,"Error",MessageBox.ERROR);
						}
	                 }
					else
					{
						packrevision = null;
						MessageBox.post(shell , reg.getString("pnpreleased"),reg.getString("error"),MessageBox.ERROR);
					}
							}
						
			}
			else
			{
				MessageBox.post(shell , reg.getString("invalidselection"),reg.getString("error"),MessageBox.ERROR);		
			}

			if(packrevision != null && pamrevision != null)
			{		
					if (packrevision instanceof TCComponentItemRevision)
					{
						if (is_packcomponent == true)
						{
					String base_uom = pamrevision.getStringProperty("u4_base_uom");
				String reason_for_issue = pamrevision.getStringProperty("u4_reason_for_issue");
				
				if ((reason_for_issue==null) || (reason_for_issue.length()==0) )
				{
					 MessageBox.post("'Reason for Issue' is empty on PAM Specification. Please set the 'Reason for Issue' value on the PAM Specification", "Invalid 'Reason for Issue'",MessageBox.ERROR);
					 return null;
				}
				
				if ((base_uom==null) || (base_uom.length()==0))
				{
						MessageBox.post("'Base UoM' is empty on PAM Specification. Please set the 'Base UoM' value on the Pack Component", "Invalid 'Base UoM'",MessageBox.ERROR);
					 return null;
				}
						}
				
						if (pamrevision.isCheckedOut() ==false)
						{
							try
					{
								pamrevision.lock();
							}
							catch (TCException e1) 
							{
								MessageBox.post("Failed to load the Specification, Please check the access on Specification","OK",MessageBox.ERROR);
								e1.printStackTrace();
								return null;
							}
							
							try
							{
								TCProperty tcProperty = pamrevision.getTCProperty("u4_sap_transfer_date");
								
								if (tcProperty!=null)
								{
									Date date = new Date();
									tcProperty.setDateValue(date);
								}
								pamrevision.save();
								pamrevision.unlock();
							}
							catch (TCException e1) 
							{
								String msg =e1.getError();
								String errormsg = "Failed to initiate the SAP Transfer:\n" + msg ;
								MessageBox.post(errormsg ,"OK",MessageBox.ERROR);
								e1.printStackTrace();

								return null;
							}
							
							MessageBox.post("SAP Transfer Successfully Initiated","OK",MessageBox.INFORMATION);
						}
						else
						{
							MessageBox.post("Please checkin the Specification to initiate SAP Transfer","OK",MessageBox.INFORMATION);
						}

						/*
				Send2SAPResponse  respone = specsvr.sendToSAP(packrevision);
				 
				if(respone.ifail == 0)
				{
					MessageBox.post("PLMXML file exported successfully","OK",MessageBox.INFORMATION);	
				}
				else
				{
					 String msg = respone.message ;						 
					 MessageBox.post(msg  , "Send2SAP ERROR",MessageBox.ERROR);
				}
						
						*/
			}  
			}
    	} 
		}
		catch (TCException e1) 
    	{
			e1.printStackTrace();
		}
        return null;
    }
}