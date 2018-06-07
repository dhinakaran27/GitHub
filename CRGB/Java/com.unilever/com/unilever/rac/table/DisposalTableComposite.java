package com.unilever.rac.table;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;


import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentForm;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentProjectType;
import com.teamcenter.rac.kernel.TCComponentType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.kernel.TCPropertyDescriptor;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.viewer.stylesheet.beans.AbstractPropertyBean;
import com.teamcenter.services.rac.core.DataManagementService;
import com.u4.services.rac.service.DisposalService;
import com.unilever.rac.pam.LoadPAMConfigurations;
import com.unilever.rac.pam.PAMConstant;
import com.unilever.rac.pam.PAMTableConfiguration;
import com.unilever.rac.ui.common.UL4Common;
import com.unilever.rac.util.UnileverPreferenceUtil;
import com.unilever.rac.util.UnileverQueryUtil;
import com.unilever.rac.util.UnileverUtility;

/**
* @author vivek.n.gowdagere
*
*/
public class DisposalTableComposite extends AbstractPropertyBean{

	/**
* 
*/
	
	private TCComponent disposaForm = null;
	
	private Composite tableComposite = null;

	/**
* 
*/
	private Composite parentComposite = null;

	/**
* 
*/
	private TCComponentItemRevision itemRevision = null;

	/**
* 
*/
	private String itemRevType = null;
	/**
* 
*/
	private Vector<Map<String,Object>> modifiedPropVals_v = null;

	/**
* 
*/
	private String renderedPropName =null;

	/**
* 
*/
	private TCSession session;

	/**
* 
*/
	private Vector<Vector <PropertyConfiguration>> PAMPropertyNameValue_v;

	private FormToolkit parentFormToolKit;

	public static StringBuffer sbErrorMessage = null ;
	
	String currentRole = null;
	
	static private Table propertyTable = null;
	/**
* 
*/
	
	/**
* @param toolkit
* @param composite
* @param paramBoolean
* @param PropName
*/
	public DisposalTableComposite(FormToolkit toolkit, Composite composite,boolean paramBoolean,Map<String, String> PropName)
	{
		super(composite);

		AbstractAIFUIApplication application = AIFUtility.getCurrentApplication();

		InterfaceAIFComponent[] targets = null;

		targets = application.getTargetComponents();

		if (targets[0] instanceof TCComponentItemRevision)
		itemRevision = (TCComponentItemRevision) targets[0];

		this.parentComposite = composite;
		parentComposite.setLayout(new GridLayout(1,true));
		parentComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		this.renderedPropName = (String) PropName.get("name");
		this.modifiedPropVals_v = new Vector<Map<String,Object>>();
		this.PAMPropertyNameValue_v = new Vector<Vector<PropertyConfiguration>>();
		this.parentFormToolKit = toolkit;

		session = itemRevision.getSession();
		itemRevType = itemRevision.getType();

		if(session != null)
		UnileverUtility.getPerformanceMonitorPrefValue( session);
		
		
	}

	/**
	* @param String 
* 
*/
	public void loadConfigurations()
	{
		try {
			LoadPAMConfigurations loadConfig = new LoadPAMConfigurations();

			Vector<PAMTableConfiguration> tableConfigs = new Vector<PAMTableConfiguration>();

			tableConfigs = loadConfig.readPreferenceValues1(renderedPropName+"_DisposalTable_Configuration", session);

			PAMPropertyNameValue_v.clear();

			TCComponent[] renderedComponents = itemRevision.getRelatedComponents(renderedPropName);

			List<TCComponent> allSecondaryComponents = new ArrayList<TCComponent>(renderedComponents.length);
			Collections.addAll(allSecondaryComponents, renderedComponents);

			Map<String, List<TCComponent>> tname_tccomponent = new HashMap<String, List<TCComponent>>();

			for(int inx=0;inx<allSecondaryComponents.size();inx++)
			{ 
				String formtype = allSecondaryComponents.get(inx).getType();

				if (tname_tccomponent.containsKey(formtype))
				{
					List<TCComponent> lst = (List<TCComponent>)tname_tccomponent.get(formtype);
					lst.add(allSecondaryComponents.get(inx));
					tname_tccomponent.put(formtype, lst);
				}
				else
				{
					List<TCComponent> tccomp_list = new ArrayList<TCComponent>();
					tccomp_list.add(allSecondaryComponents.get(inx));
					tname_tccomponent.put(formtype, tccomp_list);
				}
			}

			ArrayList<TCComponent> formarray = new ArrayList<TCComponent>();

			// Iterating to collect the forms to perform a single refreshObjects call
			for(int inx=0;inx<tableConfigs.size();inx++)
			{
				if ( itemRevType == null || tableConfigs.get(inx).primaryType ==null)
				continue;

				if(  tableConfigs.get(inx).primaryType.contains("All") )
				{
					if (tname_tccomponent.containsKey(tableConfigs.get(inx).secondaryType))
					{
						List<TCComponent> lst = (List<TCComponent>)tname_tccomponent.get(tableConfigs.get(inx).secondaryType);
						formarray.addAll(lst);
					}
				}
			}
			

			DataManagementService dmService = DataManagementService.getService(session);
			dmService.refreshObjects(formarray.toArray(new TCComponent[formarray.size()]));

			if (formarray.size() ==1)//There will be only one disposal form attached to component
			{
				boolean value = formarray.get(0).getLogicalProperty("u4_sync_countries");
				
				disposaForm = formarray.get(0);

				if (value==true)
				{
					DisposalService disposalsvr = DisposalService.getService((TCSession)AIFDesktop.getActiveDesktop().getCurrentApplication().getSession());
					disposalsvr.syncCountries(itemRevision);
					dmService.refreshObjects(formarray.toArray(new TCComponent[formarray.size()]));
				}
			}
			
			currentRole  = session.getCurrentRole().getStringProperty(UL4Common.ROLENAME);
			
			boolean enable_edit = false;
			
			if (currentRole.compareToIgnoreCase("Environmental Admin") == 0)
			{
				TCProperty prop = itemRevision.getTCProperty(UL4Common.OWNING_GROUP);
				String owning_group = prop.toString();
				String[] phaserollout_groups = UnileverPreferenceUtil.getStringPreferenceValues(TCPreferenceService.TC_preference_site, 
						"UL_MIR_PhaseRollOut_OrganizationGroups");

				for (int index = 0 ; index < phaserollout_groups.length ; index++)
				{
					if (owning_group.compareTo(phaserollout_groups[index]) == 0)
						enable_edit=true;
				}
				
				if (enable_edit == false)
				{
					String[] phaserollout_projects = UnileverPreferenceUtil.getStringPreferenceValues(TCPreferenceService.TC_preference_site, 
							"UL_MIR_PhaseRollOut_Projects");
					
					String project_ids = null;
					
					String[] component_projects = null;
					
					project_ids = itemRevision.getStringProperty(UL4Common.TCPROJECTIDS);
					
					if( project_ids == null || (  (project_ids.length() <= 1)  && project_ids.equalsIgnoreCase(" ")))
					{
						itemRevision.refresh();
						project_ids   = itemRevision.getStringProperty(UL4Common.TCPROJECTIDS);
					}
					
					if(project_ids != null &&   (  project_ids.length() >= 1  &&   ! project_ids.equalsIgnoreCase(" ")  ))
						component_projects =  project_ids.split(",");
					
					if ( (component_projects != null) && (phaserollout_projects != null))
					{
						for (int index = 0 ; index < phaserollout_projects.length ; index++)
						{
							for (int inx = 0 ; inx < component_projects.length ; inx++)
							{
								if (component_projects[inx].compareTo(phaserollout_projects[index]) == 0)
								{
									enable_edit=true;
									break;
								}
							}
							
							if (enable_edit == true)
								break;
						}
					}
				}

				if (enable_edit == false)
				{
					String[] phaserollout_category = UnileverPreferenceUtil.getStringPreferenceValues(TCPreferenceService.TC_preference_site, 
							"UL_MIR_PhaseRollOut_ProjectCategory");

					String project_ids = null;

					String[] component_projects = null;

					project_ids = itemRevision.getStringProperty(UL4Common.TCPROJECTIDS);

					if( project_ids == null || (  (project_ids.length() <= 1)  && project_ids.equalsIgnoreCase(" ")))
					{
						itemRevision.refresh();
						project_ids   = itemRevision.getStringProperty(UL4Common.TCPROJECTIDS);
					}

					if(project_ids != null &&   (  project_ids.length() >= 1  &&   ! project_ids.equalsIgnoreCase(" ")  ))
						component_projects =  project_ids.split(",");

					if ( (component_projects != null) && (phaserollout_category != null))
					{
						for (int index = 0 ; index < phaserollout_category.length ; index++)
						{
							for (int inx = 0 ; inx < component_projects.length ; inx++)
							{
								String entries1[] = { "Design Project Name" };
								String values1[] = { component_projects[inx] };
								TCComponent[]  components1 = UnileverQueryUtil.executeQuery("Design Project Search", entries1, values1);
								
								if(components1 != null && components1.length == 1)
								{
									TCComponentItemRevision designProjectItemRevision = (TCComponentItemRevision) components1[0];

									if (designProjectItemRevision!=null)
									{
										TCProperty category_prop = designProjectItemRevision.getTCProperty("u4_category");

										if (category_prop!=null)
										{
											String category[] = category_prop.getStringArrayValue();

											for (int c = 0 ; c < category.length ; c++)
											{
												if(category[c].compareTo(phaserollout_category[index]) == 0)
												{
													enable_edit=true;
													break;
												}
											}
										}

									}
								}
							}

							if (enable_edit == true)
								break;
						}
					}

				}
			}

			for(int inx=0;inx<tableConfigs.size();inx++)
			{
				Vector<PropertyConfiguration> secondaryPropValue =new Vector<PropertyConfiguration>();

				if ( itemRevType == null || tableConfigs.get(inx).primaryType ==null)
				continue;

				if((tableConfigs.get(inx).primaryType).contains("All"))
				{
					//this gives all the related forms to the PAM SPec with the relation mentioned in the renderingHint and the type mentioned in the preference
					Vector<TCComponent >relatedComponents =getRelatedComponentOfASecondayType(tname_tccomponent, renderedPropName,tableConfigs.get(inx).secondaryType);

					if (relatedComponents.size() ==0)
					continue;

					//this then sorts the obtained forms based on the row configuration mentioned in the preference
					if(!((tableConfigs.get(inx)).rowConfigurations.propertyName.equalsIgnoreCase("")))
					relatedComponents = sortForms(relatedComponents,(tableConfigs.get(inx)).rowConfigurations.propertyName,((tableConfigs.get(inx)).rowConfigurations.propertyValues).toArray(new String[(tableConfigs.get(inx)).rowConfigurations.propertyValues.size()]));

					//looping the related object and adding the column and row configurations using the custom class
					for(int jnx=0; jnx<relatedComponents.size();jnx++)
					{
						TCComponentForm tempForm = (TCComponentForm)relatedComponents.get(jnx);

						//getting the values for the required Columns
						PropertyContainer[] propValueArray = new PropertyContainer[(tableConfigs.get(inx)).ColumnConfigurations.length];

						String [] formproperties = new String [(tableConfigs.get(inx)).ColumnConfigurations.length];

						for(int knx=0;knx<(tableConfigs.get(inx)).ColumnConfigurations.length;knx++)
						formproperties[knx]=(tableConfigs.get(inx)).ColumnConfigurations[knx].columnName;

						TCProperty []currProperty = tempForm.getTCProperties(formproperties);
						
						int array_size = 0;

						int [] proptype =  new int [(tableConfigs.get(inx)).ColumnConfigurations.length];

						for(int knx=0;knx<(tableConfigs.get(inx)).ColumnConfigurations.length;knx++)
						{
							proptype[knx]=currProperty[knx].getPropertyType();
							
							if (knx == 0)// Attempt to get the size of the array
							{
								if (proptype[knx] == 8){
									String array[] = currProperty[knx].getStringValueArray();
									array_size = array.length;
								}
								else if (proptype[knx] == 6){
									boolean array[] = currProperty[knx].getBoolArrayValue();
									array_size = array.length;
								}
								else if (proptype[knx] == 3){
									double array[] = currProperty[knx].getDoubleArrayValue();
									array_size = array.length;
								}
							}
						}
						

						for(int knx=0;knx<(tableConfigs.get(inx)).ColumnConfigurations.length;knx++)
						{
							propValueArray[knx] = new PropertyContainer(array_size);
							propValueArray[knx].propName= (tableConfigs.get(inx)).ColumnConfigurations[knx].columnName;
							propValueArray[knx].tcProperty = currProperty[knx];
							propValueArray[knx].propDisplayName = currProperty[knx].getPropertyDisplayName();
							propValueArray[knx].columnSize = (tableConfigs.get(inx).ColumnConfigurations)[knx].columnSize;
							propValueArray[knx].isEnabled = (tableConfigs.get(inx).ColumnConfigurations)[knx].isEnabled;
							propValueArray[knx].PropertyType=proptype[knx];

							if (tempForm.getType().compareToIgnoreCase("U4_DisposalForm") == 0)
							{
								if ((propValueArray[knx].propName.compareToIgnoreCase("u4_comments") == 0 ) ||
										(propValueArray[knx].propName.compareToIgnoreCase("u4_recycling_rate") == 0 ) || 
										(propValueArray[knx].propName.compareToIgnoreCase("u4_recovery_rate") == 0 )
										)
								{
									if (enable_edit ==true)
									{
										propValueArray[knx].isEnabled=true;
									}
								}
							}
							
							if (proptype[knx] == 3)//Double Attribute
							{
								double array[] = currProperty[knx].getDoubleArrayValue();
								
								for (int i = 0 ; i < array.length ; i++ )
								{
									if ((propValueArray[knx].propName.compareToIgnoreCase("u4_rri") == 0 ) ||
											(propValueArray[knx].propName.compareToIgnoreCase("u4_recycling_rate") == 0 ) || 
											(propValueArray[knx].propName.compareToIgnoreCase("u4_recovery_rate") == 0 )
											)
									{
										double new_value = set_precision(array[i],1);
										propValueArray[knx].propValue[i] = Double.toString(new_value);
										continue;
									}
									
									double new_value = set_precision(array[i],2);
									//propValueArray[knx].propValue[i] = Double.toString(new_value);
									DecimalFormat df = new DecimalFormat("0.00");
									propValueArray[knx].propValue[i] = df.format(new_value);
								}
								
								System.out.println(" ");
							}
							if (proptype[knx] == 8)//String Attribute
							{
								propValueArray[knx].propValue= currProperty[knx].getStringValueArray();
							}
							else if (proptype[knx] == 6)//Boolean Attribute
							{
								boolean array[] = currProperty[knx].getBoolArrayValue();
								
								for (int i = 0 ; i < array.length ; i++ )
								propValueArray[knx].propValue[i] = (array[i]==true)?"true":"false";
							}
							
							propValueArray[knx].length = propValueArray[knx].propValue.length;
							
						}
						//getting Values required for each table
						PropertyConfiguration tempSecondaryPropValue = new PropertyConfiguration();
						tempSecondaryPropValue.propNameValuepair = propValueArray;
						tempSecondaryPropValue.secondaryName = tempForm.getType();
						tempSecondaryPropValue.selectedComponent = tempForm;
						secondaryPropValue.add(tempSecondaryPropValue);
					}
					
					if(secondaryPropValue.size()>0)
					PAMPropertyNameValue_v.add(secondaryPropValue);
				}
			}

			tableComposite = new Composite(parentComposite, SWT.NONE);
			tableComposite.setLayout(new GridLayout(1,true));
			tableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1)); 
			parentFormToolKit.adapt(tableComposite, true, true);

		} catch (Exception e) {

			e.printStackTrace();
		}
	}


	/**
* 
*/
	public void createTables ()
	{
		boolean eligible_component = false;

		try {
			
			String[] phaserollout_groups = UnileverPreferenceUtil.getStringPreferenceValues(TCPreferenceService.TC_preference_site, 
					"UL_MIR_PhaseRollOut_OrganizationGroups");
			
			String currentGroup  = session.getCurrentGroup().getStringProperty(UL4Common.NAME);
			
			for (int index = 0 ; index < phaserollout_groups.length ; index++)
			{
				if (currentGroup.compareTo(phaserollout_groups[index]) == 0)
					eligible_component=true;
			}
			
			if (eligible_component == false)
			{
				String[] phaserollout_projects = UnileverPreferenceUtil.getStringPreferenceValues(TCPreferenceService.TC_preference_site, 
						"UL_MIR_PhaseRollOut_Projects");
				
				String project_ids = null;
				
				String[] component_projects = null;
				
				project_ids = itemRevision.getStringProperty(UL4Common.TCPROJECTIDS);
				
				if( project_ids == null || (  (project_ids.length() <= 1)  && project_ids.equalsIgnoreCase(" ")))
				{
					itemRevision.refresh();
					project_ids   = itemRevision.getStringProperty(UL4Common.TCPROJECTIDS);
				}
				
				if(project_ids != null &&   (  project_ids.length() >= 1  &&   ! project_ids.equalsIgnoreCase(" ")  ))
					component_projects =  project_ids.split(",");
				
				if ( (component_projects != null) && (phaserollout_projects != null))
				{
					for (int index = 0 ; index < phaserollout_projects.length ; index++)
					{
						for (int inx = 0 ; inx < component_projects.length ; inx++)
						{
							if (component_projects[inx].compareTo(phaserollout_projects[index]) == 0)
							{
								eligible_component=true;
								break;
							}
						}
						
						if (eligible_component == true)
							break;
					}
				}
			}
			
			if (eligible_component == false)
			{
				String[] phaserollout_category = UnileverPreferenceUtil.getStringPreferenceValues(TCPreferenceService.TC_preference_site, 
						"UL_MIR_PhaseRollOut_ProjectCategory");

				String project_ids = null;

				String[] component_projects = null;

				project_ids = itemRevision.getStringProperty(UL4Common.TCPROJECTIDS);

				if( project_ids == null || (  (project_ids.length() <= 1)  && project_ids.equalsIgnoreCase(" ")))
				{
					itemRevision.refresh();
					project_ids   = itemRevision.getStringProperty(UL4Common.TCPROJECTIDS);
				}

				if(project_ids != null &&   (  project_ids.length() >= 1  &&   ! project_ids.equalsIgnoreCase(" ")  ))
					component_projects =  project_ids.split(",");

				if ( (component_projects != null) && (phaserollout_category != null))
				{
					for (int index = 0 ; index < phaserollout_category.length ; index++)
					{
						for (int inx = 0 ; inx < component_projects.length ; inx++)
						{
							String entries1[] = { "Design Project Name" };
							String values1[] = { component_projects[inx] };
							TCComponent[]  components1 = UnileverQueryUtil.executeQuery("Design Project Search", entries1, values1);
							
							if(components1 != null && components1.length == 1)
							{
								TCComponentItemRevision designProjectItemRevision = (TCComponentItemRevision) components1[0];

								if (designProjectItemRevision!=null)
								{
									TCProperty category_prop = designProjectItemRevision.getTCProperty("u4_category");

									if (category_prop!=null)
									{
										String category[] = category_prop.getStringArrayValue();

										for (int c = 0 ; c < category.length ; c++)
										{
											if(category[c].compareTo(phaserollout_category[index]) == 0)
											{
												eligible_component=true;
												break;
											}
										}
									}

								}
							}
						}

						if (eligible_component == true)
							break;
					}
				}

			}

		} catch (TCException e1) {
			e1.printStackTrace();
		}
		
		if (eligible_component == false)
			return;

		long startTime = System.currentTimeMillis();
		
		loadConfigurations();
		
		if (PAMPropertyNameValue_v.size()!=1)
			return;
	
		Label ghg_pack_material = new Label(tableComposite,SWT.NONE);
		
		
		
		try {
			if (disposaForm!=null)
			{
				TCProperty ghg_pack_material_prop = disposaForm.getTCProperty("u4_ghg_packaging_materials");
				
				if (ghg_pack_material_prop!=null)
				{
					String name  = ghg_pack_material_prop.getDescriptor().getDisplayName();
					
					if (name!=null)
						ghg_pack_material.setText(" " + name+": ");
					
					double value  = 0.00;
					value = ghg_pack_material_prop.getDoubleValue();
					
					double new_value = set_precision(value,2);
					DecimalFormat df = new DecimalFormat("0.00");
					
					String str_value = df.format(new_value);

					if (str_value!=null)
						ghg_pack_material.setText(ghg_pack_material.getText()+str_value);
				}
			}
		} catch (TCException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		//Label propertyLabel = new Label(tableComposite,SWT.NONE);
		
		Composite selectionComposite = new Composite(tableComposite, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		selectionComposite.setLayout(gridLayout);

		Label selectProj = new Label(selectionComposite,SWT.NONE);
		selectProj.setText("Disposal :");
		final Combo filterCombo = new Combo(selectionComposite, SWT.READ_ONLY);
		
		PropertyConfiguration[] tableConfigValues = PAMPropertyNameValue_v.get(0).toArray(new PropertyConfiguration[PAMPropertyNameValue_v.get(0).size()]);
		
		final Set<String> uniqueProjects = new HashSet<String>();
	
		for(  int ColumnNumber =0;ColumnNumber< tableConfigValues[0].propNameValuepair.length;ColumnNumber++ )
		{
			if(tableConfigValues[0].propNameValuepair[ColumnNumber].propName.compareToIgnoreCase("u4_project_name")==0)
			{
				String PropValue[] = (String [])  tableConfigValues[0].propNameValuepair[ColumnNumber].propValue;
				
				if ( (PropValue!=null) && (PropValue.length>0))
					Collections.addAll(uniqueProjects, PropValue);
			}
		}
		
		if (uniqueProjects.size()>0)
		{
			String item_values [] = new String [uniqueProjects.size() +1];
			item_values[0]="List All Projects";
		
			int p = 0;
			Iterator<String> iterator = uniqueProjects.iterator(); 
				
			while (iterator.hasNext()){
				item_values[++p]=(String) iterator.next();
			}
			
			filterCombo.setItems(item_values);
			filterCombo.setText(item_values[0]);
		}
		
		final String tableType = (PAMPropertyNameValue_v.get(0)).get(0).selectedComponent.getDisplayType();
		//	propertyLabel.setText(tableType+":");
		//propertyLabel.pack();
		propertyTable = new Table(tableComposite,SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.HIDE_SELECTION);
		propertyTable.setHeaderVisible(true);
		propertyTable.setLinesVisible(true);
		Display display =  propertyTable.getDisplay();

		filterCombo.addSelectionListener(new SelectionAdapter() {
		      public void widgetSelected(SelectionEvent e) 
		      {
		    	String filter_name = filterCombo.getText();
		    	int n_values = filterCombo.getItemCount();
		    	
		    	if ( n_values > 1)
		    	{
		    		//propertyTable.removeAll();
		    		long startTime = System.currentTimeMillis();

		    		TableItem[] items = propertyTable.getItems();
		    		for (int index = 0 ; index < items.length ; index++)
		    		{
		    			TableEditor editor = (TableEditor) items[index].getData("u4_recovery_rate");
		    			if (editor!=null)
		    			{
			    			editor.getEditor().dispose();
			    			editor.dispose();
		    			}
		    			
		    			editor = (TableEditor) items[index].getData("u4_recycling_rate");
		    			if (editor!=null)
		    			{
			    			editor.getEditor().dispose();
			    			editor.dispose();
		    			}
		    			
		    			items[index].dispose();
		    		}
		    		
		    		setTableConfiguration(PAMPropertyNameValue_v.get(0).toArray(new PropertyConfiguration[PAMPropertyNameValue_v.get(0).size()]),filter_name, false);

		    		if(UnileverUtility.isPerfMonitorTriggered == true)
		    			UnileverUtility.getPerformanceTime(startTime, "setTableConfiguration " + tableType );
		    		
		    		propertyTable.redraw();
		    	}
		      }
		    });
	 
		
		if(itemRevision.isCheckedOut())
		{
			TCComponent checkedOutUser = null;

			try {
				checkedOutUser = itemRevision.getReferenceProperty("checked_out_user");
			} catch (TCException e) {
				e.printStackTrace();
			}
			
			if(session.getUser().equals(checkedOutUser))
			{
				Color white = display.getSystemColor(SWT.COLOR_WHITE);
				propertyTable.setBackground(white);
			}
			else
			{
				Color lightGray = new Color (display, 240, 240, 240);
				propertyTable.setBackground(lightGray);
			}
		}
		else
		{
			Color lightGray = new Color (display, 240, 240, 240);
			propertyTable.setBackground(lightGray);
		}
		
		setTableConfiguration(PAMPropertyNameValue_v.get(0).toArray(new PropertyConfiguration[PAMPropertyNameValue_v.get(0).size()]),"List All Projects", true);

		if(UnileverUtility.isPerfMonitorTriggered == true)
			UnileverUtility.getPerformanceTime(startTime, "createTables " + tableType );
	}

	/**
* @param tcTable
* @param tableConfigValues
*/
	public void setTableConfiguration(final PropertyConfiguration[] tableConfigValues, String filter_name, boolean create_column)
	{
		for(int inx=0;inx<(tableConfigValues).length;inx++)
		{	
			if (tableConfigValues[inx].propNameValuepair.length<=0)
				continue;
			
			final TableItem item[] = new TableItem [tableConfigValues[inx].propNameValuepair[0].length];	
			boolean show_row[] = null;

			for(  int ColumnNumber =0;ColumnNumber< tableConfigValues[inx].propNameValuepair.length;ColumnNumber++ )
			{
				if (tableConfigValues[inx].propNameValuepair[ColumnNumber].propName.compareTo("u4_project_name")==0)
				{
					String PropValue[] = (String [])  tableConfigValues[inx].propNameValuepair[ColumnNumber].propValue;
					show_row= new boolean [PropValue.length];

					for (int row = 0 ; row < PropValue.length ; row++)
					{
						if (filter_name.startsWith("List All ")==true)
						{
							show_row[row]=true;
							continue;
						}

						show_row[row] = (PropValue[row].compareTo(filter_name)==0)?true:false;
					}
					break;
				}
			}	

			for(  int ColumnNumber =0;ColumnNumber< tableConfigValues[inx].propNameValuepair.length;ColumnNumber++ )
			{
				String PropValue[] = (String [])  tableConfigValues[inx].propNameValuepair[ColumnNumber].propValue;
								
				if (inx==0 && (create_column==true))//only for the first form create the columns
				{
					final TableColumn propertyColumn =  new TableColumn(propertyTable, SWT.BORDER_SOLID|SWT.COLOR_WIDGET_NORMAL_SHADOW);
					propertyColumn.setWidth(tableConfigValues[inx].propNameValuepair[ColumnNumber].columnSize);
	
					String nametext = tableConfigValues[inx].propNameValuepair[ColumnNumber].propDisplayName;
					propertyColumn.setText(nametext);
					propertyColumn.setResizable(true);
				}
			
				for (int row = 0 ; row < tableConfigValues[inx].propNameValuepair[ColumnNumber].length ; row++)
				{
					if (show_row!=null && show_row.length > row)
						if (show_row[row]==false)
							continue;
					
					final Map<String,Object> compPropVal_m = new HashMap<String,Object>();
					
					if (ColumnNumber==0)
						item[row] = new TableItem(propertyTable, SWT.NONE);


					if(tableConfigValues[inx].propNameValuepair[ColumnNumber].isEnabled == false)
					{
						if (PropValue[row]!=null)
							item[row].setText(ColumnNumber,PropValue[row]);
						
						continue;
					}

					Text text = new Text(propertyTable, SWT.NONE);
					
					if (PropValue[row]!=null)
						text.setText(PropValue[row]);
					else
						text.setText("");
					
					text.setData("colId", ColumnNumber);
					text.setData("rowId", row);

					TableEditor editor = new TableEditor(propertyTable);
					editor.grabHorizontal = true;
					editor.setEditor(text,item[row], ColumnNumber);
					item[row].setData(tableConfigValues[inx].propNameValuepair[ColumnNumber].propName, editor);
					enableORDisableConrol(editor.getEditor());
					
					if(tableConfigValues[inx].propNameValuepair[ColumnNumber].PropertyType == 8)
						text.setTextLimit(tableConfigValues[inx].propNameValuepair[ColumnNumber].tcProperty.getPropertyDescription().getMaxLength());

					text.addModifyListener(new ModifyListener() {
					public void modifyText(ModifyEvent event) {
						PropertyConfiguration currentSecPropValues = tableConfigValues[0];
						compPropVal_m.put("puid",currentSecPropValues.selectedComponent);
						int rowNum = (Integer) ((Text)(event.getSource())).getData("rowId");
						compPropVal_m.put("rowindex",rowNum);
						int columnNum = (Integer) ((Text)(event.getSource())).getData("colId");
						compPropVal_m.put("property",currentSecPropValues.propNameValuepair[columnNum].tcProperty );
						compPropVal_m.put("proptype",currentSecPropValues.propNameValuepair[columnNum].PropertyType );
						compPropVal_m.put("country",currentSecPropValues.propNameValuepair[1].propValue[rowNum] );
						compPropVal_m.put(currentSecPropValues.propNameValuepair[columnNum].propName,((Text) (event.getSource())).getText());
					}
					});	

					modifiedPropVals_v.add(compPropVal_m);
				}
			}
		}

		propertyTable.redraw();
	}

	@Override
	public Object getEditableValue() {
		return tableComposite;
	}

	@Override
	public String getProperty() {
		return "object_name";
	}

	@Override
	public boolean isPropertyModified(TCComponent arg0) throws Exception {
		return true;
	}

	@Override
	public boolean isPropertyModified(TCProperty arg0) throws Exception {
		return true;
	}

	@Override
	public void load(TCProperty arg0) throws Exception {
		createTables();
	}

	@Override
	public void load(TCComponentType arg0) throws Exception {
	}

	@Override
	public void load(TCPropertyDescriptor arg0) throws Exception {
	}

	@Override
	public void setMandatory(boolean arg0) {
	}
	
	@Override
	public void setModifiable(boolean arg0) {
	}

	@Override
	public void setProperty(String arg0) {
	}

	@Override
	public void setUIFValue(Object arg0) {
	}
	@Override
	public void setVisible(boolean arg0) {
	}

	@Override
	public TCProperty getPropertyToSave(TCComponent paramTCComponent)
	throws Exception
	{

		if (this.property != null)
		{
			TCProperty localTCProperty = paramTCComponent.getTCProperty(this.property);
			return getPropertyToSave(localTCProperty);
		}
		
		this.savable = true;

		Set<String> uniq_modified_prop = new HashSet<String>();

		for(int inx= 0;inx<modifiedPropVals_v.size();inx++)
		{
			Map<String, Object> propValueMap = modifiedPropVals_v.get(inx);
			
			TCProperty tcProperty = (TCProperty) (propValueMap).get("property");
			
			if (tcProperty==null)
				continue;
			
			for(Map.Entry<String, Object> propName : propValueMap.entrySet()) 
			{
				if(tcProperty.getPropertyName().compareTo(propName.getKey()) == 0 )
				{
					uniq_modified_prop.add(propName.getKey());
					break;
				}
			}
		}

		String admin_comments[]=null;
		boolean rri_overridden[]=null;
		double recovery_rate[]=null;
		double recycle_rate[]=null;
		
		TCComponent disposalForm = null;

		for (String modified_prop : uniq_modified_prop)
		{
			for(int inx= 0;inx<modifiedPropVals_v.size();inx++)
			{
				Map<String, Object> propValueMap = modifiedPropVals_v.get(inx);
				TCProperty tc_Property = (TCProperty) ( propValueMap).get("property");

				if (tc_Property==null)
				continue;

				if( tc_Property.getPropertyName().compareTo(modified_prop) == 0 )
				{
					if (disposalForm == null)
					{
						disposalForm = (TCComponent) ( propValueMap).get("puid");
						
						TCProperty rri_overridden_prop = disposalForm.getTCProperty("u4_rri_overridden");

						if (rri_overridden==null)
							rri_overridden = rri_overridden_prop.getBoolArrayValue();
					}

					for(Map.Entry<String, Object> propName : propValueMap.entrySet()) 
					{
						if( tc_Property.getPropertyName().compareTo(propName.getKey()) == 0 )
						{
							int proptype =  (int) ( propValueMap).get("proptype");
							int rowindex =  (int) ( propValueMap).get("rowindex");
							
							if (proptype == 3)//double
							{
								String string_value = ((String) (propName.getValue()));
								double newvalue = Double.parseDouble(string_value);
								
								if (modified_prop.compareToIgnoreCase("u4_recovery_rate") ==0)
								{
									if  (recovery_rate==null)
										recovery_rate = tc_Property.getDoubleValueArray();

									recovery_rate[rowindex]=newvalue;
									rri_overridden[rowindex]=true;								
								}
								else if (modified_prop.compareToIgnoreCase("u4_recycling_rate") ==0)
								{
									if (recycle_rate==null)
										recycle_rate = tc_Property.getDoubleValueArray();

									recycle_rate[rowindex]=newvalue;
									rri_overridden[rowindex]=true;
								}
							}
							else if (proptype == 8)//double
							{							
								if (modified_prop.compareToIgnoreCase("u4_comments") ==0)
								{
									if  (admin_comments==null)
										admin_comments = tc_Property.getStringValueArray();

									admin_comments[rowindex]= ((String) (propName.getValue()));// replace the value
								}
							}
							
							break;
						}
					}
				}
			}
		}
		
		if (((admin_comments!=null) || (recycle_rate!=null) || (recovery_rate!=null)) && (rri_overridden!=null) )
		{
			if (disposalForm!=null)
			{
				try {
					disposalForm.lock();
					
					if (admin_comments!=null)
					{
						for (int index = 0 ; index < admin_comments.length ; index++)
						{
							if (admin_comments[index]==null)
								admin_comments[index]="";
						}
						
						TCProperty tcProperty = disposalForm.getTCProperty("u4_comments");
						if (tcProperty!=null)
							tcProperty.setStringValueArray(admin_comments);
					}
					
					if (recovery_rate!=null)
					{
						TCProperty tcProperty = disposalForm.getTCProperty("u4_recovery_rate");
						if (tcProperty!=null)
							tcProperty.setDoubleValueArray(recovery_rate);
					}
					
					if (recycle_rate!=null)
					{
						TCProperty tcProperty = disposalForm.getTCProperty("u4_recycling_rate");
						if (tcProperty!=null)
							tcProperty.setDoubleValueArray(recycle_rate);
					}
					
					disposalForm.save();
					disposalForm.lock();
					
					TCProperty tcProperty = disposalForm.getTCProperty("u4_rri_overridden");
					if (tcProperty!=null)
						tcProperty.setLogicalValueArray(rri_overridden);
					
					disposalForm.save();
					disposalForm.unlock();
					disposalForm.refresh();
				}
				catch (TCException e) 
				{
					sbErrorMessage.append("Error saving Disposal Form");
				}
			}
			
		}

		return null;
	}
	
	@Override
	public void save(TCProperty paramTCProperty)throws Exception
	{
		try
		{
			TCProperty localTCProperty = getPropertyToSave(paramTCProperty);
			if ((this.savable) && (localTCProperty != null)) {
				localTCProperty.getTCComponent().setTCProperty(paramTCProperty);
			}
			setDirty(false);
		} catch (TCException e) {
			sbErrorMessage.append(e.getMessage()).append("\n");
		}
	}

	@Override
	public TCProperty saveProperty(TCComponent paramTCComponent) throws Exception
	{
		sbErrorMessage = new StringBuffer();

		TCProperty localTCProperty = getPropertyToSave(paramTCComponent);
		if (this.savable) 
		{
			if(sbErrorMessage.length() > 0) 
			MessageBox.post(AIFUtility.getActiveDesktop().getShell(), sbErrorMessage.toString() ,"Error", MessageBox.ERROR);
			
			sbErrorMessage = null ;
			return localTCProperty;
		}
		setDirty(false);
		

		if(sbErrorMessage.length() > 0) 
		MessageBox.post(AIFUtility.getActiveDesktop().getShell(), sbErrorMessage.toString() ,"Error", MessageBox.ERROR);
		
		sbErrorMessage = null ;
		return null;
	}

	@Override
	public TCProperty getPropertyToSave(TCProperty arg0) throws Exception 
	{

		for(int inx= 0;inx<modifiedPropVals_v.size();inx++)
		{
			Map<String, Object> propValueMap = modifiedPropVals_v.get(inx);
			TCComponent currComp = (TCComponent) ( propValueMap).get("puid");

			for(Map.Entry<String, Object> propName : propValueMap.entrySet()) 
			{
				try {
					if(!(propName.getKey().equalsIgnoreCase("puid")) && !(propName.getKey().equalsIgnoreCase("section")))
					currComp.setProperty(propName.getKey(), (String) (propName.getValue()));
				} catch (TCException e) {
					sbErrorMessage.append(currComp.toString()).append(" - ");
					sbErrorMessage.append(propName.getKey()).append(" : ");
					sbErrorMessage.append(e.getMessage().replace("double", "number")).append("\n");
				}
			}

		}
		return null;
	}

	public boolean isStructuredComponent(TCComponent inputComp)
	{ 
		try {
			TCComponent[] relatedStructuredComponents = itemRevision.getRelatedComponents(PAMConstant.STRUCTURED_RELATION);
			List<TCComponent> relatedStructuredComponentsList = Arrays.asList(relatedStructuredComponents); 
			if(relatedStructuredComponentsList.contains(inputComp))
			{
				return true;
			}
		} catch (TCException e) {
			e.printStackTrace();
		}
		return false;
	}

	public Vector<TCComponent> sortForms(Vector<TCComponent> unSortedForms,String sortProperty,String[] SortPropertyValues)
	{
		Vector<TCComponent> sortedForms = new Vector<TCComponent>();

		for(int inx=0;inx<SortPropertyValues.length;inx++)
		{
			for(int jnx=0;jnx<unSortedForms.size();jnx++)
			{
				try {
					if(SortPropertyValues[inx].equalsIgnoreCase(unSortedForms.get(jnx).getProperty(sortProperty)))
					{
						sortedForms.add(unSortedForms.get(jnx));
					}
				} catch (TCException e) {
					e.printStackTrace();
				}
			}
		}

		return sortedForms;
	}

	public Vector<TCComponent> getRelatedComponentOfASecondayType(Map<String, List<TCComponent>> secondaryComponents, String relation, String secondaryType)
	{
		Vector<TCComponent> relatedSecondaryComponent= new Vector<TCComponent>();

		if (secondaryComponents.containsKey(secondaryType))
		{
			List<TCComponent> lst = (List<TCComponent>)secondaryComponents.get(secondaryType);
			relatedSecondaryComponent.addAll(lst);
		}

		return relatedSecondaryComponent;

	}

	public void enableORDisableConrol(Control control)
	{
		if(control != null)
		{
			if(itemRevision.isCheckedOut())
			{
				TCComponent checkedOutUser = null;

				try {
					checkedOutUser = itemRevision.getReferenceProperty("checked_out_user");
				} catch (TCException e) {
					e.printStackTrace();
				}
				
				if(session.getUser().equals(checkedOutUser))
				control.setEnabled(true);
				else
				control.setEnabled(false);
			}
			else 
			{
				control.setEnabled(false);
			}
		}
	}

	


	Listener ctrlAListener = new Listener() { 
		@Override
		public void handleEvent(Event event) {
			if ( event.stateMask == SWT.CTRL && event.keyCode == 'a' ) {
				((Text)event.widget).selectAll();
			}

		}
	};

	Listener ctrlCListener = new Listener() { 
		@Override
		public void handleEvent(Event event) {
			if ( event.stateMask == SWT.CTRL && event.keyCode == 'c' ) {
				((Text)event.widget).copy();
			}

		}
	};

	Listener ctrlVListener = new Listener() { 
		@Override
		public void handleEvent(Event event) {
			if ( event.stateMask == SWT.CTRL && event.keyCode == 'v' ) {
				((Text)event.widget).paste();
			}

		}
	};
	
	double set_precision(double number, int precision)
	{
		Double d = number;
		String[] splitter = d.toString().split("\\.");
				
		if (splitter[1].length() <=precision)// After  Decimal Count
			return number;
		
	    double prec = Math.pow(10, precision);
	    int integerPart = (int) number;
	    double fractionalPart = number - integerPart;
	    fractionalPart *= prec;
	    int fractPart = (int) fractionalPart;
	    fractionalPart = (double) (integerPart) + (double) (fractPart)/prec;
	    return fractionalPart;
	}
}
