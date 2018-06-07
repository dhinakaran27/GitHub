/**
 * 
 */
package com.unilever.rac.ui.saveascomponent;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.teamcenter.rac.aif.AIFClipboard;
import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.AIFPortal;
import com.teamcenter.rac.aif.AIFTransferable;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.commands.open.OpenCommand;
import com.teamcenter.rac.commands.paste.PasteOperation;
import com.teamcenter.rac.kernel.DeepCopyInfo;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMViewRevision;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentBOMWindowType;
import com.teamcenter.rac.kernel.TCComponentContextList;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentForm;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentProject;
import com.teamcenter.rac.kernel.TCComponentProjectType;
import com.teamcenter.rac.kernel.TCComponentRevisionRule;
import com.teamcenter.rac.kernel.TCComponentRevisionRuleType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core._2006_03.DataManagement.CreateItemsResponse;
import com.teamcenter.services.rac.core._2006_03.DataManagement.GenerateItemIdsAndInitialRevisionIdsProperties;
import com.teamcenter.services.rac.core._2006_03.DataManagement.GenerateItemIdsAndInitialRevisionIdsResponse;
import com.teamcenter.services.rac.core._2006_03.DataManagement.ItemIdsAndInitialRevisionIds;
import com.teamcenter.services.rac.core._2006_03.DataManagement.ItemProperties;
import com.teamcenter.services.rac.core._2008_06.DataManagement;
import com.teamcenter.services.rac.core._2008_06.DataManagement.DeepCopyData;
import com.teamcenter.services.rac.core._2008_06.DataManagement.PropertyNameValueInfo;
import com.teamcenter.services.rac.core._2008_06.DataManagement.SaveAsNewItemInfo;
import com.teamcenter.services.rac.core._2008_06.DataManagement.SaveAsNewItemOutput2;
import com.teamcenter.services.rac.core._2008_06.DataManagement.SaveAsNewItemResponse2;
import com.u4.services.rac.service.SpecService;
import com.u4.services.rac.service._2014_12.Spec.Send2SAPResponse;
import com.unilever.rac.ui.common.UL4Common;
import com.unilever.rac.util.UnileverPreferenceUtil;
import com.unilever.rac.util.UnileverQueryUtil;
/**
 * @author j.madhusudan.inamdar
 *
 */
public class UL4ComponentSaveAsOperation extends AbstractAIFOperation
{
	  private String 	strDescription;
	  private String 	strProject;
	  private String 	strUom;
	  private TCSession session;
	  private String[]  prefTokens;
	  private TCComponentItemRevision selectedItemRevision;
	  private TCComponentItemRevision latestPAMReleasedRevision;
	  private TCComponentItemRevision newComponentItemRevision;
	  private TCComponentItemRevision newDDEItemRevision;
	  private TCComponentItemRevision newPnPItemRevision;
	  private TCComponentItemRevision newCADComponentItemRevision;
	  private TCComponentItem newAssemblyComponentItem;
	  private TCComponentItem newDDEItem;
	  private TCComponentItem newCADComponentItem;
	  private TCComponentItem newComponentItem;
	  private String[] idArray									 							= new String[2];
	  private boolean isCopyToClipBoardClicked												= false;
	  private boolean isOpenOnCreateClicked													= false;
	  private boolean isCloneChildClicked													= false;
	private TCComponentItemRevision newAssemblyComponentItemRevision;
	 HashMap<TCComponentItem, TCComponentItemRevision> structureMap = null;

	public UL4ComponentSaveAsOperation(TCComponentItemRevision selectedItemRevision, TCSession session, boolean isCopyToClipBoardClicked, boolean isOpenOnCreateClicked, boolean isCloneChildClicked, String selectedProject, String strDesc,String selectedUom) 
	{
		this.selectedItemRevision = selectedItemRevision;
		this.session = session;
		this.isCopyToClipBoardClicked = isCopyToClipBoardClicked;
		this.isOpenOnCreateClicked = isOpenOnCreateClicked;
		this.isCloneChildClicked = isCloneChildClicked;
		this.strProject = selectedProject;
		this.strDescription = strDesc;
		this.strUom     = selectedUom;
	}

	/**
	 * Gets the Master form from an Item or ItemRevision.
	 */
	public TCComponent getMasterForm(TCComponent itemOrRevision)
	{
		
		TCComponent[] masterForm = null;
		try 
		{
			if (itemOrRevision instanceof TCComponentItemRevision) 
			{
				masterForm = itemOrRevision.getRelatedComponents(UL4Common.IMAN_MASTER_FORM_REV_TYPE);
			}
			else if (itemOrRevision instanceof TCComponentItem)
				masterForm = itemOrRevision.getRelatedComponents(UL4Common.IMAN_MASTER_FORM_TYPE);
		}
		catch (TCException e) 
		{
			e.printStackTrace();
		}
		
		return masterForm[0];
	}
	
	@Override
	public void executeOperation() throws Exception 
	{
		//PART A:: If selected object is of type U4_CADComponentRevision
		if(UL4Common.CAD_COMPONENT_ITEM_REV.equals(selectedItemRevision.getType()))
		{
			
			idArray = getAssignedIds(UL4Common.DDE_ITEM_TYPE);
			
			//2. Create CAD Comp using SAVE AS
			session.setStatus( "Creating New CAD Component..." );
			
			newCADComponentItem = saveAsComponents(selectedItemRevision, idArray, false);
			if(newCADComponentItem   != null)
			{
					//Get the newly created CADComponentItemRevision
					newCADComponentItemRevision = newCADComponentItem.getLatestItemRevision();
				
					TCComponent[] comps = selectedItemRevision.getRelatedComponents(UL4Common.IMANSPECIFICATION);
					if(comps != null && comps.length != 0)
					{
						ArrayList datasetList = new ArrayList();
						//Get each form and do saveAs for each forms.
						for(TCComponent eachComp : comps)
						{
							if(eachComp instanceof TCComponentDataset)
							{
								TCComponentDataset newDatasetCmp = ((TCComponentDataset) eachComp).saveAs(eachComp.getProperty(UL4Common.OBJECT_NAME));
								if(newDatasetCmp != null)
									datasetList.add(newDatasetCmp);						 
							}
						}
						
						TCComponent[] finalComponent = (TCComponent[])datasetList.toArray(new TCComponent[datasetList.size()]);
						newCADComponentItemRevision.lock();
						newCADComponentItemRevision.setRelated(UL4Common.IMANSPECIFICATION, finalComponent);
						newCADComponentItemRevision.save();
						newCADComponentItemRevision.unlock();
						newCADComponentItemRevision.refresh();
						 
					}
				  
				   //10. Add newly created component revision to the design project using FinalRelation.
				  session.setStatus( "Adding CAD Component to the Design Project..." );
				  
				  String entries1[] = { UL4Common.OBJ_NAME_ATTR_DISP_NAME, UL4Common.QRY_TYPE_PARAM };
				  String values1[] = { strProject.trim(), UL4Common.QRY_PROJECT_PARAM };
				  TCComponent[]  components1 = UnileverQueryUtil.executeQuery(UL4Common.GENERAL_QRY_NAME, entries1, values1);
				  if(components1 != null && components1.length == 1)
				  {

						//Assign the required project to the newly created PACK Component and its secondaries.
						session.setStatus( "Assigning Projects..." );

					  	TCComponentItem designProjectItem = (TCComponentItem) components1[0];
						TCComponentItemRevision designProjectLatestItemRevision = designProjectItem.getLatestItemRevision();
						
						//Get the project assigned to the Design Project/Design Project Revision.
						
						String designProjects = designProjectLatestItemRevision.getProperty(UL4Common.TCPROJECTIDS);
						String[] strProjectTokens =  designProjects .split(",");
						
						// Getting projects
						TCComponentProjectType typeProject  = (TCComponentProjectType) designProjectLatestItemRevision.getSession().getTypeComponent(UL4Common.TCPROJECT);
						
						// Compare the TC-project-name with the selected Design Project Name, and do the project id assignment for that only, skip rest of the project-ids.
						String designProjectName = designProjectLatestItemRevision.getProperty(UL4Common.OBJECT_NAME);
						
						//for each project, assign it to the newly created component
						for(String projectName: strProjectTokens)
						{
							if(designProjectName.equalsIgnoreCase(projectName))
							{
								TCComponentProject project = typeProject.find(projectName.trim());	
								if(project != null)
									typeProject.assignToProject(project, newComponentItemRevision.getItem());
								
								break;
							}
							
						}
					
						//Attach Pack Component to Selected Project Revision
						PasteOperation paste2 = new PasteOperation(AIFUtility.getCurrentApplication(),designProjectLatestItemRevision,newCADComponentItemRevision,
																																		UL4Common.FINAL_RELATION);	
						paste2.executeOperation();	

				  }  
				  else
				  {
					  MessageBox.post("Design Project with the specified name [ Project " + strProject +" ] is not found.", "Error", MessageBox.ERROR);
				  }
				  
				  session.setStatus( "Save As Complete." );
				  
				  if(isCopyToClipBoardClicked)
				  {
					  //Send the newlycreated comp rev to clipboard.
					  AIFClipboard localAIFClipboard = AIFPortal.getClipboard();
			          localAIFClipboard.setContents(new AIFTransferable((TCComponentItem)newCADComponentItemRevision.getItem()), null);
				  }
				  
				  if(isOpenOnCreateClicked)
				  {
					  //Open object in explorer.
					  Registry reg = Registry.getRegistry("com.teamcenter.rac.commands.exportobjects.exportobjects");
					  
					  OpenCommand localOpenCommand = (OpenCommand)reg.newInstanceForEx("openCommand", new Object[] { AIFUtility.getCurrentApplication().getDesktop(),(TCComponentItem)newCADComponentItemRevision.getItem()});
				      localOpenCommand.executeModeless();
				  }
			}
			
		}
		//PART B:: If selected object is of type U4_DDERevision
		else if(UL4Common.DDEREVISION.equals(selectedItemRevision.getType()))
		{
			
			idArray = getAssignedIds(UL4Common.DDE_ITEM_TYPE);
			
			//2. Create DDE Comp using SAVE AS
			session.setStatus( "Creating New DDE..." );
			
			newDDEItem = saveAsComponents(selectedItemRevision, idArray, false);
			if(newDDEItem  != null)
			{
					//Get the newly created DDERevision
					newDDEItemRevision = newDDEItem.getLatestItemRevision();
				
					TCComponent[] comps = selectedItemRevision.getRelatedComponents(UL4Common.IMANSPECIFICATION);
					if(comps != null && comps.length != 0)
					{
						ArrayList datasetList = new ArrayList();
						//Get each form and do saveAs for each forms.
						for(TCComponent eachComp : comps)
						{
							if(eachComp instanceof TCComponentDataset)
							{
								TCComponentDataset newDatasetCmp = ((TCComponentDataset) eachComp).saveAs(eachComp.getProperty(UL4Common.OBJECT_NAME));
								if(newDatasetCmp != null)
									datasetList.add(newDatasetCmp);						 
							}
						}
						
						TCComponent[] finalComponent = (TCComponent[])datasetList.toArray(new TCComponent[datasetList.size()]);
						newDDEItemRevision.lock();
						newDDEItemRevision.setRelated(UL4Common.IMANSPECIFICATION, finalComponent);
						newDDEItemRevision.save();
						newDDEItemRevision.unlock();
						newDDEItemRevision.refresh();
						 
					}
				  
				   //10. Add newly created component revision to the design project using FinalRelation.
				  session.setStatus( "Adding DDE to the Design Project..." );
				  
				  String entries1[] = { UL4Common.OBJ_NAME_ATTR_DISP_NAME, UL4Common.QRY_TYPE_PARAM};
				  String values1[] = { strProject.trim(), UL4Common.QRY_PROJECT_PARAM };
				  TCComponent[]  components1 = UnileverQueryUtil.executeQuery(UL4Common.GENERAL_QRY_NAME, entries1, values1);
				  if(components1 != null && components1.length == 1)
				  {
					  	session.setStatus( "Assigning Projects..." );
					  
					  	TCComponentItem designProjectItem = (TCComponentItem) components1[0];
						TCComponentItemRevision designProjectLatestItemRevision = designProjectItem.getLatestItemRevision();
						
						//3. Check if DDERevision has PnPSpec attached.
						newPnPItemRevision = (TCComponentItemRevision) newDDEItemRevision.getRelatedComponent(UL4Common.PNPSPECIFICATION);
						//TCComponentForm newPnpItemMasterForm = newPnPItemRevision.getUnderlyingComponent();
						
						//Get the project assigned to the Design Project/Design Project Revision.						
						String designProjects = designProjectLatestItemRevision.getProperty(UL4Common.TCPROJECTIDS);
						String[] strProjectTokens =  designProjects .split(",");
						
						// Getting projects
						TCComponentProjectType typeProject  = (TCComponentProjectType) designProjectLatestItemRevision.getSession().getTypeComponent(UL4Common.TCPROJECT);
						
						// Compare the TC-project-name with the selected Design Project Name, and do the project id assignment for that only, skip rest of the project-ids.
						String designProjectName = designProjectLatestItemRevision.getProperty(UL4Common.OBJECT_NAME);
						
						//for each project, assign it to the newly created DDE and its subcomponents.
						for(String projectName: strProjectTokens)
						{
							if(designProjectName.equalsIgnoreCase(projectName))
							{
								TCComponentProject project = typeProject.find(projectName.trim());	
								if(project != null)
								{
									typeProject.assignToProject(project, newDDEItemRevision.getItem());
									
									if(newPnPItemRevision != null)
									{
										typeProject.assignToProject(project, newPnPItemRevision.getItem());
										//Assign project to the DDEMaster Forms
										typeProject.assignToProject(project, getMasterForm(newDDEItemRevision.getItem()));
										typeProject.assignToProject(project, getMasterForm(newDDEItemRevision));
										//Assign project to the PnPMaster Forms
										typeProject.assignToProject(project, getMasterForm(newPnPItemRevision.getItem()));
										typeProject.assignToProject(project, getMasterForm(newPnPItemRevision));
										
										break;
									}
								}								
							}							
						}
						
						//Attatch Pack Component to Selected Project Revision
						PasteOperation paste2 = new PasteOperation(AIFUtility.getCurrentApplication(),designProjectLatestItemRevision,newDDEItemRevision,UL4Common.FINAL_RELATION);	
						paste2.executeOperation();
				  }  
				  else
				  {
					  MessageBox.post("Design Project with the specified name [ Project " + strProject +" ] is not found.", "Error", MessageBox.ERROR);
				  }
				  
				  session.setStatus( "Save As Complete." );
				  
				  if(isCopyToClipBoardClicked)	
				  {
					  //Send the newlycreated comp rev to clipboard.
					  AIFClipboard localAIFClipboard = AIFPortal.getClipboard();
			          localAIFClipboard.setContents(new AIFTransferable((TCComponentItem)newDDEItemRevision.getItem()), null);
				  }
				  if(isOpenOnCreateClicked)
				  {
					  //Open object in explorer.
					  Registry reg = Registry.getRegistry("com.teamcenter.rac.commands.exportobjects.exportobjects");
					  
					  OpenCommand localOpenCommand = (OpenCommand)reg.newInstanceForEx("openCommand", new Object[] {
							  												AIFUtility.getCurrentApplication().getDesktop(),
							  												(TCComponentItem)newDDEItemRevision.getItem()});
				      localOpenCommand.executeModeless();
				  }
				  
			}
			
		}
		//PART C:: if the selected object is of the type Assembly
		else if (selectedItemRevision.getType().startsWith("U4_A"))
		{
			if(!isCloneChildClicked)
			{
				idArray = getAssignedIds(UL4Common.COMPONENT_ITEM_TYPE);

				//2. Create Assembly Comp using SAVE AS
				session.setStatus( "Creating New Assembly Component..." );

				newAssemblyComponentItem = saveAsComponents(selectedItemRevision, idArray, false);
				if(newAssemblyComponentItem != null )
				{
					//Get the newly created CADComponentItemRevision
					newAssemblyComponentItemRevision = newAssemblyComponentItem.getLatestItemRevision();

					//10. Add newly created component revision to the design project using FinalRelation.
					session.setStatus( "Adding Assembly Component to the Design Project..." );

					String entries1[] = { UL4Common.OBJ_NAME_ATTR_DISP_NAME, UL4Common.QRY_TYPE_PARAM };
					String values1[] = { strProject.trim(), UL4Common.QRY_PROJECT_PARAM };
					TCComponent[]  components1 = UnileverQueryUtil.executeQuery(UL4Common.GENERAL_QRY_NAME, entries1, values1);
					if(components1 != null && components1.length == 1)
					{

						//Assign the required project to the newly created PACK Component and its secondaries.
						session.setStatus( "Assigning Projects..." );

						TCComponentItem designProjectItem = (TCComponentItem) components1[0];
						TCComponentItemRevision designProjectLatestItemRevision = designProjectItem.getLatestItemRevision();

						//Get the project assigned to the Design Project/Design Project Revision.

						String designProjects = designProjectLatestItemRevision.getProperty(UL4Common.TCPROJECTIDS);
						String[] strProjectTokens =  designProjects .split(",");

						// Getting projects
						TCComponentProjectType typeProject  = (TCComponentProjectType) designProjectLatestItemRevision.getSession().getTypeComponent(UL4Common.TCPROJECT);

						// Compare the TC-project-name with the selected Design Project Name, and do the project id assignment for that only, skip rest of the project-ids.
						String designProjectName = designProjectLatestItemRevision.getProperty(UL4Common.OBJECT_NAME);

						//for each project, assign it to the newly created component
						for(String projectName: strProjectTokens)
						{
							if(designProjectName.equalsIgnoreCase(projectName))
							{
								TCComponentProject project = typeProject.find(projectName.trim());	
								if(project != null)
									typeProject.assignToProject(project, newAssemblyComponentItemRevision.getItem());

								break;
							}

						}

						//Attach Pack Component to Selected Project Revision
						PasteOperation paste2 = new PasteOperation(AIFUtility.getCurrentApplication(),designProjectLatestItemRevision,newAssemblyComponentItemRevision,
								UL4Common.FINAL_RELATION);	
						paste2.executeOperation();	

					}  
					else
					{
						MessageBox.post("Design Project with the specified name [ Project " + strProject +" ] is not found.", "Error", MessageBox.ERROR);
					}

					session.setStatus( "Save As Complete." );

					if(isCopyToClipBoardClicked)
					{
						//Send the newlycreated comp rev to clipboard.
						AIFClipboard localAIFClipboard = AIFPortal.getClipboard();
						localAIFClipboard.setContents(new AIFTransferable((TCComponentItem)newAssemblyComponentItemRevision.getItem()), null);
					}

					if(isOpenOnCreateClicked)
					{
						//Open object in explorer.
						Registry reg = Registry.getRegistry("com.teamcenter.rac.commands.exportobjects.exportobjects");

						OpenCommand localOpenCommand = (OpenCommand)reg.newInstanceForEx("openCommand", new Object[] { AIFUtility.getCurrentApplication().getDesktop(),(TCComponentItem)newAssemblyComponentItemRevision.getItem()});
						localOpenCommand.executeModeless();
					}
				}
			}
			else if(isCloneChildClicked)
			{
				idArray = getAssignedIds(UL4Common.COMPONENT_ITEM_TYPE);

				//2. Create Assembly Comp using SAVE AS
				session.setStatus( "Creating New Assembly Component..." );

				newAssemblyComponentItem = saveAsComponents(selectedItemRevision, idArray, false);
				if(newAssemblyComponentItem != null )
				{
					//Get the newly created AssemblyComponentItemRevision
					newAssemblyComponentItemRevision = newAssemblyComponentItem.getLatestItemRevision();
					
					Vector <TCComponent> ChildCompIds = new Vector<TCComponent>();
					Vector <TCComponent> newChildCompIds = new Vector<TCComponent>();
					//newChildCompIds = cloneChildItems(comps);
					
					//create new BOM with new child components
					/**
					 * 1.create BOM window
					 * 2.Set BomWindow top line as the new assembly component
					 * 3.get all child components
					 * 4.clone all the child components
					 * 5.replace old components with newly created child components to the bomline 
					 * 6.save bomline
					 * 7.save bom window
					 * 8.close bom window 
					 */
					
					TCComponentBOMWindow bw = null;
					TCComponentBOMLine blTop = null;
					TCComponentBOMViewRevision bvr = null;
					
					try 
					{
						//step 1
						bw  = getAssemblyWindow(newAssemblyComponentItemRevision);	
					} 
					catch (Exception e) 
					{
						System.out.println("***DataExport:Error in getAssemblyWindow function "
								+ e.toString());
					}
						
					try 
					{
						//step 2
						blTop  = getWindowTopLine(bw, newAssemblyComponentItemRevision);	
					} 
					catch (Exception e) 
					{
						System.out.println("***DataExport:Error in getWindowTopLine function "
								+ e.toString());
					}
					try 
					{
						//step 3
						ChildCompIds.addAll(getChildrenAttachments(blTop));
						bw.close();
					} 
					catch (Exception e)
					{
						System.out.println("***DataExport:Error in getChildrenAttachments function "
								+ e.toString());
					}
					//step 4
												
					session.setStatus( "Creating New Child Components..." );
					newChildCompIds = cloneChildItems(ChildCompIds);
					
					//step 5
					try 
					{
						bw  = getAssemblyWindow(newAssemblyComponentItemRevision);							
					} 
					catch (Exception e) 
					{
						System.out.println("***DataExport:Error in getAssemblyWindow function "
								+ e.toString());
					}
						
					try 
					{
						blTop  = getWindowTopLine(bw, newAssemblyComponentItemRevision);	
					} 
					catch (Exception e) 
					{
						System.out.println("***DataExport:Error in getWindowTopLine function "
								+ e.toString());
					}
					
					structureMap = new HashMap<TCComponentItem,TCComponentItemRevision>();
					structureMap = createHashMap(ChildCompIds,newChildCompIds);
					
					TCComponentBOMWindow newbw  = getAssemblyWindow(newAssemblyComponentItemRevision);
					//TCComponentBOMViewRevision bvr=bw.askBvr();
					AIFComponentContext[] newbvr1 = newAssemblyComponentItemRevision.getRelated("structure_revisions");
					TCComponentBOMViewRevision newbvr = (TCComponentBOMViewRevision) newbvr1[0].getComponent();
					TCComponent newbv =newbvr.getReferenceProperty("bom_view");
					TCComponentBOMLine newbomline = getWindowTopLine(newbw, newAssemblyComponentItemRevision);
					
					bw  = getAssemblyWindow(selectedItemRevision);
					//TCComponentBOMViewRevision bvr=bw.askBvr();
					AIFComponentContext[] bvr1 = selectedItemRevision.getRelated("structure_revisions");
					bvr = (TCComponentBOMViewRevision) bvr1[0].getComponent();
					TCComponent bv =bvr.getReferenceProperty("bom_view");
					TCComponentBOMLine bomline = getWindowTopLine(bw, selectedItemRevision);
					
					try 
					{
						
						newbomline = cutBomLines(newbomline);
						blTop = buildBOMStructure(bomline,newbomline);
						
						//steps 6,7,8
						blTop.save();
						bw.save();
						bw.close();
					} 
					catch (Exception e)
					{
						System.out.println("***DataExport:Error in getChildrenAttachments function "
								+ e.toString());
					}
					//10. Add newly created component revision to the design project using FinalRelation.
				session.setStatus( "Adding Assembly Component to the Design Project..." );

					String entries1[] = { UL4Common.OBJ_NAME_ATTR_DISP_NAME, UL4Common.QRY_TYPE_PARAM };
					String values1[] = { strProject.trim(), UL4Common.QRY_PROJECT_PARAM };
					TCComponent[]  components1 = UnileverQueryUtil.executeQuery(UL4Common.GENERAL_QRY_NAME, entries1, values1);
					if(components1 != null && components1.length == 1)
					{

						//Assign the required project to the newly created PACK Component and its secondaries.
						session.setStatus( "Assigning Projects..." );

						TCComponentItem designProjectItem = (TCComponentItem) components1[0];
						TCComponentItemRevision designProjectLatestItemRevision = designProjectItem.getLatestItemRevision();

						//Get the project assigned to the Design Project/Design Project Revision.

						String designProjects = designProjectLatestItemRevision.getProperty(UL4Common.TCPROJECTIDS);
						String[] strProjectTokens =  designProjects .split(",");

						// Getting projects
						TCComponentProjectType typeProject  = (TCComponentProjectType) designProjectLatestItemRevision.getSession().getTypeComponent(UL4Common.TCPROJECT);

						// Compare the TC-project-name with the selected Design Project Name, and do the project id assignment for that only, skip rest of the project-ids.
						String designProjectName = designProjectLatestItemRevision.getProperty(UL4Common.OBJECT_NAME);

						//for each project, assign it to the newly created component
						for(String projectName: strProjectTokens)
						{
							if(designProjectName.equalsIgnoreCase(projectName))
							{
								TCComponentProject project = typeProject.find(projectName.trim());	
								if(project != null)
									typeProject.assignToProject(project, newAssemblyComponentItemRevision.getItem());

								break;
							}

						}

						//Attach Pack Component to Selected Project Revision						
						PasteOperation paste2 = new PasteOperation(AIFUtility.getCurrentApplication(),designProjectLatestItemRevision,newAssemblyComponentItemRevision,
								UL4Common.FINAL_RELATION);	
						paste2.executeOperation();	

					}  
					else
					{
						MessageBox.post("Design Project with the specified name [ Project " + strProject +" ] is not found.", "Error", MessageBox.ERROR);
					}

					session.setStatus( "Save As Complete." );

					if(isCopyToClipBoardClicked)
					{
						//Send the newlycreated comp rev to clipboard.
						AIFClipboard localAIFClipboard = AIFPortal.getClipboard();
						localAIFClipboard.setContents(new AIFTransferable((TCComponentItem)newAssemblyComponentItemRevision.getItem()), null);
					}

					if(isOpenOnCreateClicked)
					{
						//Open object in explorer.
						Registry reg = Registry.getRegistry("com.teamcenter.rac.commands.exportobjects.exportobjects");

						OpenCommand localOpenCommand = (OpenCommand)reg.newInstanceForEx("openCommand", new Object[] { AIFUtility.getCurrentApplication().getDesktop(),(TCComponentItem)newAssemblyComponentItemRevision.getItem()});
						localOpenCommand.executeModeless();
					}
				}
			}				

		}
		//PART D:: If selected object is of type U4_ComponentRevision
		else 
		{
			//itemid,revid,itemname,itemdesc,true,null=deepcopy
			idArray = getAssignedIds(UL4Common.COMPONENT_ITEM_TYPE);

			if(idArray.length != 0 )
			{
				try 
				{
				  //2. Create PACK Comp using SAVE AS
				  session.setStatus( "Creating New Pack Component..." );
				  
				  newComponentItem = saveAsComponents(selectedItemRevision, idArray, false);

				  session.setStatus( "Creating Forms for Pack and Pam..." );

				  if(newComponentItem != null)
				  {
					  newComponentItemRevision = newComponentItem.getLatestItemRevision();
						  
					  //10. Add newly created component revision to the design project using FinalRelation.
					  session.setStatus( "Adding New Pack Component to the Design Project..." );
					  
					  String entries1[] = { UL4Common.OBJ_NAME_ATTR_DISP_NAME, UL4Common.QRY_TYPE_PARAM };
					  String values1[] = { strProject.trim(), UL4Common.QRY_PROJECT_PARAM };
					  TCComponent[]  components1 = UnileverQueryUtil.executeQuery(UL4Common.GENERAL_QRY_NAME, entries1, values1);
					  if(components1 != null && components1.length == 1)
					  {

							//Assign the required project to the newly created PACK Component and its secondaries.
							session.setStatus( "Assigning Projects..." );

						  	TCComponentItem designProjectItem = (TCComponentItem) components1[0];
							TCComponentItemRevision designProjectLatestItemRevision = designProjectItem.getLatestItemRevision();
							
							//Get the project assigned to the Design Project/Design Project Revision.
							
							String designProjects = designProjectLatestItemRevision.getProperty(UL4Common.TCPROJECTIDS);
							String[] strProjectTokens =  designProjects .split(",");
							
							// Getting projects
							TCComponentProjectType typeProject  = (TCComponentProjectType) designProjectLatestItemRevision.getSession().getTypeComponent(UL4Common.TCPROJECT);
							
							// Compare the TC-project-name with the selected Design Project Name, and do the project id assignment for that only, skip rest of the project-ids.
							String designProjectName = designProjectLatestItemRevision.getProperty(UL4Common.OBJECT_NAME);
							
							//for each project, assign it to the newly created component
							for(String projectName: strProjectTokens)
							{
								if(designProjectName.equalsIgnoreCase(projectName))
								{
									TCComponentProject project = typeProject.find(projectName.trim());	
									if(project != null)
										typeProject.assignToProject(project, newComponentItemRevision.getItem());
									
									break;
								}
								
							}
						
							//Attach Pack Component to Selected Project Revision
							PasteOperation paste2 = new PasteOperation(AIFUtility.getCurrentApplication(),designProjectLatestItemRevision,newComponentItemRevision,
																																			UL4Common.FINAL_RELATION);	
							paste2.executeOperation();	
						
						  AIFComponentContext[] pamcomponent  = newComponentItemRevision.getRelated(UL4Common.PAMSPECIFICATION);
							 
						  if ((pamcomponent!=null))
						  {
							  if (pamcomponent.length==1)
							  {
								  TCComponentItemRevision  pamrevision    = null ;
								  pamrevision = (TCComponentItemRevision) pamcomponent[0].getComponent();
							
								  if (( pamrevision != null) && (pamrevision instanceof TCComponentItemRevision))
								  {
									  try
									  {
										  pamrevision.lock();
									  }
									  catch (TCException e1) 
									  {
										  MessageBox.post("Failed to load the Specification, Please check the access on Specification","OK",MessageBox.ERROR);
										  e1.printStackTrace();
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
									  }

									  MessageBox.post("SAP Transfer Successfully Initiated","OK",MessageBox.INFORMATION);
								  }
							  }
						  }
					  }  
					  else
					  {
						  MessageBox.post("Design Project with the specified name [ Project " + strProject +" ] is not found.", "Error", MessageBox.ERROR);
					  }
					  
					  session.setStatus( "Save As Complete." );
					  
					  if(isCopyToClipBoardClicked)
					  {
						  //Send the newlycreated comp rev to clipboard.
						  AIFClipboard localAIFClipboard = AIFPortal.getClipboard();
				          localAIFClipboard.setContents(new AIFTransferable((TCComponentItem)newComponentItemRevision.getItem()), null);
					  }
					  
					  if(isOpenOnCreateClicked)
					  {
						  //Open object in explorer.
						  Registry reg = Registry.getRegistry("com.teamcenter.rac.commands.exportobjects.exportobjects");
						  
						  OpenCommand localOpenCommand = (OpenCommand)reg.newInstanceForEx("openCommand", new Object[] { AIFUtility.getCurrentApplication().getDesktop(),(TCComponentItem)newComponentItemRevision.getItem()});
					      localOpenCommand.executeModeless();
					  }
					  
				  }
			  } 
			  catch (TCException e) 
			  {
				  e.printStackTrace();
			  }		  
			}
		}
		
	}
		
		/**
		 * This method is called when a Create button is clicked. It will get next
		 * available ID and Revision.
		 * @param strType The item type for which the data should be returned.
		 * @return Array of strings of length 2 containing the ID at index 0 and the Revision at index 1.
		 */
		@SuppressWarnings("unchecked")
		protected String[] getAssignedIds(String strType) {
			String[] strReturnArray = new String[2];
			DataManagementService dmService = DataManagementService.getService(this.session);

			GenerateItemIdsAndInitialRevisionIdsProperties[] inputs = new GenerateItemIdsAndInitialRevisionIdsProperties[1];
			inputs[0] = new GenerateItemIdsAndInitialRevisionIdsProperties();
			inputs[0].item = null;
			inputs[0].itemType = strType;
			inputs[0].count = 1;
			GenerateItemIdsAndInitialRevisionIdsResponse newIdResponse = dmService.generateItemIdsAndInitialRevisionIds(inputs);
			if (newIdResponse.serviceData.sizeOfPartialErrors() > 0) return null;

			Iterator iterator=newIdResponse.outputItemIdsAndInitialRevisionIds.entrySet().iterator();
			while(iterator.hasNext()) {
				Map.Entry entry = (Map.Entry)iterator.next();
				ItemIdsAndInitialRevisionIds[] newIdRevs = (ItemIdsAndInitialRevisionIds[]) entry.getValue();
				ItemIdsAndInitialRevisionIds ids = newIdRevs[0];
				strReturnArray[0] = ids.newItemId;
				strReturnArray[1] = ids.newRevId;
				break;
			}
			return strReturnArray;
		}
		
		private TCComponentBOMLine cutBomLines(TCComponentBOMLine newbomline) {
			// TODO Auto-generated method stub
			AIFComponentContext[] children;
			try {
				children = newbomline.getChildren();
				for(int i=0;i<children.length;i++)
				{
					TCComponentBOMLine comp = (TCComponentBOMLine) children[i].getComponent();
					if(comp.hasChildren()){
						TCComponentBOMLine childBOMLine = (TCComponentBOMLine) children[i].getComponent();
						childBOMLine.cut();
						childBOMLine = cutBomLines(comp);
						newbomline = childBOMLine.parent();
						//childBOMLine.cut();
					}
					else
					{
						TCComponentBOMLine childBOMLine = (TCComponentBOMLine) children[i].getComponent();	
						childBOMLine.cut();
					}				
				}
				newbomline.save();
			} catch (TCException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
			
			return newbomline;
		}

		private TCComponentBOMLine buildBOMStructure(TCComponentBOMLine bomline, TCComponentBOMLine newbomline) {
			// TODO Auto-generated method stub
			
			try {		 
				 AIFComponentContext[] children = bomline.getChildren();
				for(int c = 0;c<children.length;c++){
					TCComponentBOMLine comp = (TCComponentBOMLine) children[c].getComponent();
					TCComponentItem item = comp.getItem();				
					TCComponentItemRevision itemrev = structureMap.get(item);				
					if(itemrev!=null)
					{
						if(comp.hasChildren())
						{		
							TCComponentBOMLine newline = newbomline.add(itemrev.getItem(),itemrev,null,false);
							TCComponentBOMLine newchildBomline = cutBomLines(newline);
							newbomline.save();
							newline = buildBOMStructure(comp,newchildBomline);						
							newbomline = newline.parent();
						}
						else
						{						
							newbomline.add(itemrev.getItem(),itemrev,null,false);
							newbomline.save();
							System.out.println("adding bomline:"+itemrev.getProperty("object_string"));
						}
					}
				}
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
			return newbomline;
		}
		
		private HashMap<TCComponentItem, TCComponentItemRevision> createHashMap(
				Vector<TCComponent> childCompIds,
				Vector<TCComponent> newChildCompIds) {
			// TODO Auto-generated method stub
			HashMap<TCComponentItem,TCComponentItemRevision> structureMap1 = new HashMap<TCComponentItem,TCComponentItemRevision>();
			for(int i=0;i<newChildCompIds.size();i++)
			{
				try {
					TCComponentItem newItem = (TCComponentItem) newChildCompIds.get(i);
					TCComponentItemRevision newItemRev=newItem.getLatestItemRevision();
					TCComponentItemRevision basedOnComp = (TCComponentItemRevision) newItemRev.getRelatedComponent("IMAN_based_on");
					if(basedOnComp!=null)
					{
						if(childCompIds.contains(basedOnComp))
						{
							structureMap1.put(basedOnComp.getItem(),newItemRev);
						}
					}
				} catch (TCException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return structureMap1;
		}
		
		private Vector<TCComponent> cloneChildItems(Vector<TCComponent> comps) {
			// TODO Auto-generated method stub
			Vector<TCComponent> newChildCompIds =new Vector<TCComponent>();
			for(int i=0;i<comps.size();i++)
			{
				try {
					String childType = comps.get(i).getType();
					String[] childIdArray = null;
					if(UL4Common.CAD_COMPONENT_ITEM_REV.equals(childType)){
						childIdArray = getAssignedIds(UL4Common.CAD_COMPONENT_ITEM_TYPE);
					}
					else {
						childIdArray = getAssignedIds(UL4Common.COMPONENT_ITEM_TYPE);
					}
					TCComponentItemRevision childItem=(TCComponentItemRevision)comps.get(i);
					TCComponentItem newChildComp = saveAsChildComponents(childItem.getItem().getLatestItemRevision(),childIdArray,false);
					TCComponentItemRevision newChildCompRevision = newChildComp.getLatestItemRevision();
					newChildCompIds.add(newChildComp);

					session.setStatus( "Adding Child Component to the Design Project..." );

					String entries1[] = { UL4Common.OBJ_NAME_ATTR_DISP_NAME, UL4Common.QRY_TYPE_PARAM };
					String values1[] = { strProject.trim(), UL4Common.QRY_PROJECT_PARAM };
					TCComponent[]  components1 = UnileverQueryUtil.executeQuery(UL4Common.GENERAL_QRY_NAME, entries1, values1);
					if(components1 != null && components1.length == 1)
					{

						//Assign the required project to the newly created PACK Component and its secondaries.
						session.setStatus( "Assigning Child Components to Projects..." );

						TCComponentItem designProjectItem = (TCComponentItem) components1[0];
						TCComponentItemRevision designProjectLatestItemRevision = designProjectItem.getLatestItemRevision();

						//Get the project assigned to the Design Project/Design Project Revision.

						String designProjects = designProjectLatestItemRevision.getProperty(UL4Common.TCPROJECTIDS);
						String[] strProjectTokens =  designProjects .split(",");

						// Getting projects
						TCComponentProjectType typeProject  = (TCComponentProjectType) designProjectLatestItemRevision.getSession().getTypeComponent(UL4Common.TCPROJECT);

						// Compare the TC-project-name with the selected Design Project Name, and do the project id assignment for that only, skip rest of the project-ids.
						String designProjectName = designProjectLatestItemRevision.getProperty(UL4Common.OBJECT_NAME);

						//for each project, assign it to the newly created component
						for(String projectName: strProjectTokens)
						{
							if(designProjectName.equals(projectName))
							{
								TCComponentProject project = typeProject.find(projectName.trim());	
								if(project != null)
									typeProject.assignToProject(project, newChildCompRevision.getItem());

								break;
							}
						}

						//Attach Pack Component to Selected Project Revision
						if((newChildCompRevision.getProperty(UL4Common.STAGE_ATTR).equals("Design Realisation")))
						{
							PasteOperation paste2 = new PasteOperation(AIFUtility.getCurrentApplication(),designProjectLatestItemRevision,newChildCompRevision,
									UL4Common.FINAL_RELATION);	
	
							paste2.executeOperation();
						}
						else
						{
							PasteOperation paste2 = new PasteOperation(AIFUtility.getCurrentApplication(),designProjectLatestItemRevision,newChildCompRevision,
									UL4Common.DEVRELATION);	
	
							paste2.executeOperation();
						}
					} 	

				}catch (TCException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			return newChildCompIds;
		}
		public TCComponentItem saveAsChildComponents(TCComponentItemRevision childComp, String[] idArray, boolean isDeepCopyAllowed) {
			try 
			{
				//Check for Deep Copy rule is allowed or not.
				if(isDeepCopyAllowed)
				{
					ArrayList<DeepCopyInfo> dc = new ArrayList<DeepCopyInfo>();
					
					TCComponentContextList related = childComp.getRelatedList();
					
					for (int j = 0; j < related.getListCount(); j++) 
					{
						dc.add(new DeepCopyInfo(related.toTCComponentArray()[j],
								DeepCopyInfo.COPY_NOT_ACTION, related.toTCComponentArray()[j]
										.getProperty(UL4Common.OBJECT_NAME), related.get(j)
										.getContext(), true, true, true ));

					}
					
					DeepCopyInfo[] dc_a = new DeepCopyInfo[dc.size()];
					
					for (int i = 0; i < dc_a.length; i++) {
						dc_a[i] = dc.get(i);
					}
					
					boolean do_dc = dc_a.length > 0;
					
					TCComponentItem newComp = childComp.saveAsItem(idArray[0], idArray[1], childComp.getProperty(UL4Common.OBJECT_NAME), childComp.getProperty(UL4Common.OBJECT_DESC), 
							do_dc, dc_a);
					TCComponentItemRevision newCompLtRev = newComp.getLatestItemRevision();
					
					if (newCompLtRev!=null)
					{
						newCompLtRev.lock();
						String stage = childComp.getProperty(UL4Common.STAGE_ATTR);
						newCompLtRev.setProperty(UL4Common.STAGE_ATTR, stage);
						//newCompLtRev.setProperty("u4_base_uom", strUom);
						newCompLtRev.save();
						newCompLtRev.unlock();
						newCompLtRev.refresh();
					}
					
					return (TCComponentItem) newComp;
					
				}
				//Else Perform for Components
				else
				{
					TCComponentItem newComp = childComp.saveAsItem(idArray[0], idArray[1], childComp.getProperty(UL4Common.OBJECT_NAME), childComp.getProperty(UL4Common.OBJECT_DESC),
							 false, null);
					TCComponentItemRevision newCompLtRev = newComp.getLatestItemRevision();
					
					if (newCompLtRev!=null)
					{
						newCompLtRev.lock();
						String stage = childComp.getProperty(UL4Common.STAGE_ATTR);
						newCompLtRev.setProperty(UL4Common.STAGE_ATTR, stage);
						//newCompLtRev.setProperty("u4_base_uom", strUom);
						newCompLtRev.save();
						newCompLtRev.unlock();
						newCompLtRev.refresh();
					}
					
						return (TCComponentItem) newComp;
				}

			} 
			catch (TCException e) 
			{
				MessageBox.post(e.getDetailsMessage(), "Error", MessageBox.ERROR);
				e.printStackTrace();
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
			
			Vector<TCComponent> vComponents = new Vector<TCComponent>();
			Vector<TCComponent> vComponents1 = new Vector<TCComponent>();
			
			AIFComponentContext[] children = bomline.getChildren();
			
			for (int c = 0; c < children.length; c++)
			{
				//System.out.println("bl_line_name2 " + children[c].getComponent().getProperty("bl_line_name").toString());
				//System.out.println("bl_has_children "+ children[c].getComponent().getProperty("bl_has_children").toString());
				
				if(children[c].getComponent().getProperty("bl_has_children").toString().equals("True"))
				{
					TCComponent childBOMLine = (TCComponent) children[c].getComponent();
					TCComponent rev = childBOMLine.getReferenceProperty("bl_line_object");
					vComponents.add(rev);
					vComponents1.addAll(getChildrenAttachments((TCComponentBOMLine) children[c].getComponent()));
					vComponents.addAll(vComponents1);
				}
				else
				{
					TCComponent childBOMLine = (TCComponent) children[c].getComponent();
					TCComponent rev = childBOMLine.getReferenceProperty("bl_line_object");
					
					vComponents.add(rev);	
				}
			}
			return vComponents;
		}
		
		public TCComponentItem saveAsComponents(TCComponentItemRevision parent, String[] idArray, boolean isDeepCopyAllowed) {
			try 
			{
				//Check for Deep Copy rule is allowed or not.
				if(isDeepCopyAllowed)
				{
					ArrayList<DeepCopyInfo> dc = new ArrayList<DeepCopyInfo>();
					
					TCComponentContextList related = parent.getRelatedList();
					
					for (int j = 0; j < related.getListCount(); j++) 
					{
						dc.add(new DeepCopyInfo(related.toTCComponentArray()[j],
								DeepCopyInfo.COPY_NOT_ACTION, related.toTCComponentArray()[j]
										.getProperty(UL4Common.OBJECT_NAME), related.get(j)
										.getContext(), true, true, true ));

					}
					
					DeepCopyInfo[] dc_a = new DeepCopyInfo[dc.size()];
					
					for (int i = 0; i < dc_a.length; i++) {
						dc_a[i] = dc.get(i);
					}
					
					boolean do_dc = dc_a.length > 0;
					
					TCComponentItem newComp = parent.saveAsItem(idArray[0], idArray[1], selectedItemRevision.getProperty(UL4Common.OBJECT_NAME), selectedItemRevision.getProperty(UL4Common.OBJECT_DESC), 
							do_dc, dc_a);
					TCComponentItemRevision newCompLtRev = newComp.getLatestItemRevision();
					
					if (newCompLtRev!=null)
					{
						newCompLtRev.lock();
						newCompLtRev.setProperty(UL4Common.STAGE_ATTR, UL4Common.DESIGN_REALISATION_VALUE);
					newCompLtRev.setProperty("u4_base_uom", strUom);
						newCompLtRev.save();
						newCompLtRev.unlock();
						newCompLtRev.refresh();
					}

						return (TCComponentItem) newComp;
					
				}
				//else perform for DDE components
				else if(selectedItemRevision.getType().equals("U4_DDERevision")){
					TCComponentItem newComp = parent.saveAsItem(idArray[0], idArray[1], selectedItemRevision.getProperty(UL4Common.OBJECT_NAME), this.strDescription, 
							 false, null);
					TCComponentItemRevision newCompLtRev = newComp.getLatestItemRevision();
					
					if (newCompLtRev!=null)
					{
						newCompLtRev.lock();						
					//newCompLtRev.setProperty("u4_base_uom", strUom);
						newCompLtRev.save();
						newCompLtRev.unlock();
						newCompLtRev.refresh();
					}

					
						return (TCComponentItem) newComp;
				}
				//Else Perform for Components
				else
				{
					TCComponentItem newComp = parent.saveAsItem(idArray[0], idArray[1], selectedItemRevision.getProperty(UL4Common.OBJECT_NAME), this.strDescription, 
							 false, null);
					TCComponentItemRevision newCompLtRev = newComp.getLatestItemRevision();
					
					if (newCompLtRev!=null)
					{
						newCompLtRev.lock();
						newCompLtRev.setProperty(UL4Common.STAGE_ATTR, UL4Common.DESIGN_REALISATION_VALUE);
					newCompLtRev.setProperty("u4_base_uom", strUom);
						newCompLtRev.save();
						newCompLtRev.unlock();
						newCompLtRev.refresh();
					}

					
						return (TCComponentItem) newComp;
				}

			} 
			catch (TCException e) 
			{
				MessageBox.post(e.getDetailsMessage(), "Error", MessageBox.ERROR);
				e.printStackTrace();
			}
			
			return null;
		}
		
		/**
		 * Creation of the component associated with the node. After successful creation, the new component
		 * is pasted to the selected Project Revision.
		 * @param type The component type to be created
		 * @return The created component
		 * @throws TCException During creation, projects assignment - need to be validated.
		 */
			/*protected void copyFormsForSpecifiedObjectAndRelation(TCComponentItemRevision sourceItemRevision, TCComponentItemRevision targetItemRevision, String strInputRelation)
			{
				try
				{
					
					 TCComponent[] comps = sourceItemRevision.getRelatedComponents(strInputRelation);
					 if(comps != null && comps.length != 0)
					 {
						 ArrayList list = new ArrayList();
						 //Get each form and do saveAs for each forms.
						 for(TCComponent eachComp : comps)
						 {
							 if(eachComp instanceof TCComponentForm)
							 {
								 TCComponentForm newFormCmp = ((TCComponentForm) eachComp).saveAs(eachComp.getProperty(UL4Common.OBJECT_NAME));
								 if(newFormCmp != null)
									 list.add(newFormCmp);						 
							 }
						 }
						 //and then set it as related component - which is newly created obj.
						 //convert the list into array and pass it below insteadof comps
						 TCComponent[] finalComponent = (TCComponent[])list.toArray(new TCComponent[list.size()]);
						 targetItemRevision.lock();
						 targetItemRevision.setRelated(strInputRelation,finalComponent);
						 targetItemRevision.save();
						 targetItemRevision.unlock();
						 targetItemRevision.refresh();
					 }
				}
				catch(TCException tcEx)
				{
					tcEx.printStackTrace();
					 return;
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
					return;
				}
			}*/
			
			/**
			 * Creation of the component associated with the node. After successful creation, the new component
			 * is pasted to the selected Project Revision.
			 * @param type The component type to be created
			 * @return The created component
			 * @throws TCException During creation, projects assignment - need to be validated.
			 */
			/*protected TCComponent createComponent(String type, String name, String desc, TCComponentItem newComponentItem) throws TCException
			{
				String              strName         = name;
				TCComponent         newComp         = null;
				CreateItemsResponse response        = null;
				String              relation        = "";
				
				/**
				 *  newComp should be declared here so than each time a new component is created and destroyed
				 *  PasteComponent will be called with the reference to
				 *  the new component created 
				
				session.setStatus("Creating New PAM " + " " + strName + " ...");
				
				//Fix for Defect# When we do Save As of PAM - the item id should match with item id of PAM - except the 'C' prefix. 
				//For eg. if newly created Component ID = C510000200038 then, newly created PAM should have 510000200038
				
				String truncatedItemID = newComponentItem.getProperty("item_id");
				String regx = "C";
			    char[] ca = regx.toCharArray();
			    for (char c : ca) {
			    	truncatedItemID = truncatedItemID.replace(""+c, "");
			    }

			    DataManagementService dmService = DataManagementService.getService(session);
				
				ItemProperties[] inputs = new ItemProperties[1];
				inputs[0] = new ItemProperties();
				inputs[0].clientId    = "1";
				inputs[0].description = desc;
				inputs[0].itemId      = truncatedItemID; //idArray[0]; 
				inputs[0].name        = strName;
				inputs[0].revId       = idArray[1];
				inputs[0].type        = type;
				inputs[0].uom         = new String();
				
				//extendComponentAttributes(inputs[0]);

				response = dmService.createItems(inputs, null, relation);

				if (response.serviceData.sizeOfPartialErrors() == 0)
					newComp = response.output[0].item;
				else {
					TCException exception = new TCException(response.serviceData
							.getPartialError(0).getLevels(), response.serviceData
							.getPartialError(0).getCodes(), response.serviceData
							.getPartialError(0).getMessages());
					throw exception;
				}
				
				session.setReadyStatus();
				return newComp;
			}*/
			

			  /*@Override
			  protected boolean isResizable() {
			    return true;
			  }*/

			  
			  /**
			   * Takes a copy of an ItemRevision component and creates a new Item.
			   *
			   * @param itemRevision the Item Revision to save as an Item
			   * @param itemID the item ID
			   * @param revisionID the revision id
			   * @param Name the Name of the Item
			   * @param Description the Description
			   * @param CopyInfo An array of Deep Copy info class.
			   * @param itemMasterForm the Item Master form
			   * @param itemRevMasterForm the Item Revision Master form
			   * @param deepCopyRequired the deep copy required
			   * @return A Tc Component representing the newly copied Item.
			   * @throws TCException the TC exception
			   * @published
			   */
			  /*public TCComponentItem customSaveAsItem( TCComponentItemRevision itemRevision,
			          String itemID, String revisionID, String Name, String Description,
			          boolean deepCopyRequired, DeepCopyInfo[] CopyInfo,
			          TCComponentForm itemMasterForm, TCComponentForm itemRevMasterForm )
			      throws TCException
			  {
			      String propertyNames[] = { "object_name", "object_desc" };
			      TCProperty[] properties = itemRevision.getTCProperties( propertyNames );


			      SaveAsNewItemInfo saveAsNewItemInfo = new DataManagement.SaveAsNewItemInfo();

			      saveAsNewItemInfo.clientId = itemRevision.getObjectString();
			      saveAsNewItemInfo.baseItemRevision = itemRevision;
			      saveAsNewItemInfo.name = Name;
			      saveAsNewItemInfo.description = Description;
			      saveAsNewItemInfo.newItemId = itemID;
			      saveAsNewItemInfo.newRevId = revisionID;

			      int nCpInfo = CopyInfo != null ? CopyInfo.length : 0;
			      saveAsNewItemInfo.deepCopyInfo = new DataManagement.DeepCopyData[nCpInfo];
			      for( int ii = 0; ii < nCpInfo; ii++ )
			      {
			          DeepCopyData dCpData = new DataManagement.DeepCopyData();
			          dCpData.action = CopyInfo[ii].getAction();
			          dCpData.isTargetPrimary = CopyInfo[ii].isTargetPrimary();
			          dCpData.isRequired = CopyInfo[ii].isRequired();
			          dCpData.copyRelations = CopyInfo[ii].isCopyRelations();
			          dCpData.otherSideObjectTag = CopyInfo[ii].getOtherSideComponent();
			          dCpData.newName = CopyInfo[ii].getName();
			          dCpData.relationTypeName = (String)CopyInfo[ii].getRelation();
			          saveAsNewItemInfo.deepCopyInfo[ii] = dCpData;

			      }
			      

			      if ( itemMasterForm != null )
			      {
			          TCProperty[] itemMasterProps = itemMasterForm.getFormTCProperties();
			          saveAsNewItemInfo.newItemMasterProperties = new DataManagement.MasterFormPropertiesInfo();
			          saveAsNewItemInfo.newItemMasterProperties.form = itemMasterForm;
			          PropertyNameValueInfo [] propNameValueInfos = new DataManagement.PropertyNameValueInfo[itemMasterProps.length];
			          for( int ii = 0; ii < propNameValueInfos.length; ii++ )
			          {
			              PropertyNameValueInfo propNameValInfo = new DataManagement.PropertyNameValueInfo();
			              propNameValInfo.propertyName = itemMasterProps[ii].getPropertyName();
			              propNameValInfo.propertyValues = new String[1];
			              propNameValInfo.propertyValues[0] =  itemMasterProps[ii].getUIFValue();
			              propNameValueInfos[ii] = propNameValInfo;
			          }
			          saveAsNewItemInfo.newItemMasterProperties.propertyValueInfo = propNameValueInfos;
			      }

			      if ( itemRevMasterForm != null )
			      {
			          TCProperty[] itemRevMasterProps = itemRevMasterForm.getFormTCProperties();
			          saveAsNewItemInfo.newItemRevisionMasterProperties = new DataManagement.MasterFormPropertiesInfo();
			          saveAsNewItemInfo.newItemRevisionMasterProperties.form = itemRevMasterForm;
			          PropertyNameValueInfo [] propNameValueInfos = new DataManagement.PropertyNameValueInfo[itemRevMasterProps.length];
			          for( int ii = 0; ii < propNameValueInfos.length; ii++ )
			          {
			              PropertyNameValueInfo propNameValInfo = new DataManagement.PropertyNameValueInfo();
			              propNameValInfo.propertyName = itemRevMasterProps[ii].getPropertyName();
			              propNameValInfo.propertyValues = new String[1];
			              propNameValInfo.propertyValues[0] =  itemRevMasterProps[ii].getUIFValue();
			              propNameValueInfos[ii] = propNameValInfo;
			          }
			          saveAsNewItemInfo.newItemRevisionMasterProperties.propertyValueInfo = propNameValueInfos;
			      }
			      

			      SaveAsNewItemInfo [] saveAsNewItemInfos = new DataManagement.SaveAsNewItemInfo[1];
			      saveAsNewItemInfos[0] = saveAsNewItemInfo;
			      com.teamcenter.services.internal.rac.core.DataManagementService dmService = com.teamcenter.services.internal.rac.core.DataManagementService.getService( itemRevision.getSession() );
			      SaveAsNewItemResponse2 saveAsNewResp2 = dmService.saveAsNewItemObject( saveAsNewItemInfos, deepCopyRequired );
			      SaveAsNewItemOutput2 saveAs2Out = (SaveAsNewItemOutput2) saveAsNewResp2.saveAsOutputMap.get( saveAsNewItemInfo.clientId );
			      
			      
			      // If the save as failed, then throw exception.
			      if ( saveAs2Out == null && saveAsNewResp2.serviceData.sizeOfPartialErrors() > 0 )
			      { 
			          throw new TCException( saveAsNewResp2.serviceData.getPartialError( 0 ).getMessages() ); 
			      } 

			      return saveAs2Out != null ? saveAs2Out.newItem : null;
			  }*/
			  
}
