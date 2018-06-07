package com.unilever.rac.pam;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
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

import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.ListOfValuesInfo;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentForm;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentListOfValues;
import com.teamcenter.rac.kernel.TCComponentListOfValuesType;
import com.teamcenter.rac.kernel.TCComponentType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.kernel.TCPropertyDescriptor;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.viewer.stylesheet.beans.AbstractPropertyBean;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core.LOVService;
import com.teamcenter.services.rac.core._2013_05.LOV.InitialLovData;
import com.teamcenter.services.rac.core._2013_05.LOV.LOVSearchResults;
import com.teamcenter.services.rac.core._2013_05.LOV.LOVValueRow;
import com.unilever.rac.ui.common.UL4Common;
import com.unilever.rac.util.UnileverPreferenceUtil;
import com.unilever.rac.util.UnileverQueryUtil;
import com.unilever.rac.util.UnileverUtility;

/**
 * @author swetha.manchiraju
 *
 */
public class EPAMTableComposite extends AbstractPropertyBean{

	private static final String PALLETATTR = null;

	/**
	 * 
	 */
	private Composite tableComposite = null;

	/**
	 * 
	 */
	private Composite parentComposite = null;

	/**
	 * 
	 */
	private TCComponentItemRevision pamSpecRevision = null;

	private TCComponentItemRevision dderevision = null;

	/**
	 * 
	 */
	private String pamSecType = null;

	/**
	 * 
	 */
	private String packcomponentType = null;

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
	private Vector<Vector <PAMSecondaryPropValue>> PAMPropertyNameValue_v;

	private int ePAMTableHeight= 100;

	private int ePAmTableWidth = 700;

	private FormToolkit parentFormToolKit;
	private boolean isCheckOut = false ;
	private boolean enable_editor = false;

	public static StringBuffer sbErrorMessage = null ;
	public static String section = "" ; 
	/**
	 * 
	 */

	public static String PMLMATDTLREL          = "U4_PMLMatlDetlsRelation";
	public static String PMLMATDTLFORM         = "U4_PMLMatlDetlsForm";
	public static String  PALLET               = "U4_PalletRelation";
	public static String  PALLETPROP           = "Max Pallet Stacking";
	public static String  PALLETAAT            = "Truck";
	public static String  PALLETFORM           ="U4_StrgDistConditionsForm";

	/**
	 * @param toolkit
	 * @param composite
	 * @param paramBoolean
	 * @param PropName
	 * @throws TCException 
	 */
	public EPAMTableComposite(FormToolkit toolkit, Composite composite,boolean paramBoolean,Map<String, String> PropName) throws TCException
	{
		super(composite);

		AbstractAIFUIApplication application = AIFUtility.getCurrentApplication();

		InterfaceAIFComponent[] targets = null;

		targets = application.getTargetComponents();

		if (targets[0] instanceof TCComponentItemRevision)
			pamSpecRevision = (TCComponentItemRevision) targets[0];

		//AIFComponentContext[] aifcomponent = pamSpecRevision.whereReferenced() ;
		
		AIFComponentContext[] aifcomponent = pamSpecRevision.getPrimary() ;

		for( int inx=0 ; inx < aifcomponent.length ; inx++)
		{
			TCComponent component = (TCComponent) aifcomponent[inx].getComponent() ;

			if( component instanceof TCComponentItemRevision && ((TCComponentItemRevision)component).getTypeComponent().getType().equals(UL4Common.DDEREVISION) )
			{
				dderevision = (TCComponentItemRevision) component ;
				break;
			}
		}

		this.parentComposite = composite;
		parentComposite.setLayout(new GridLayout(1,true));
		parentComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		this.renderedPropName = (String) PropName.get("name");
		this.modifiedPropVals_v = new Vector<Map<String,Object>>();
		this.PAMPropertyNameValue_v = new Vector<Vector<PAMSecondaryPropValue>>();
		this.parentFormToolKit = toolkit;

		session = pamSpecRevision.getSession();
		pamSecType = pamSpecRevision.getType();

		if(session != null)
			UnileverUtility.getPerformanceMonitorPrefValue( session);

		AIFComponentContext[] comp;
		try
		{
			TCComponentType tccompttype = session.getTypeService().getTypeComponent (renderedPropName);
			section = tccompttype.getDisplayTypeName().replace("Relation", "");

			comp = pamSpecRevision.getPrimary();

			if  ( comp.length== 1)
			{
				if( comp[0].getComponent() instanceof TCComponentItemRevision)
					packcomponentType = comp[0].getComponent().getType();
			}
		}
		catch (TCException e) 
		{
			section  = renderedPropName;
		}
		
		isCheckOut = pamSpecRevision.isCheckedOut(); 

		if(isCheckOut)
		{
			TCComponent checkedOutUser = null;

			try {
				checkedOutUser = pamSpecRevision.getReferenceProperty("checked_out_user");
			} catch (TCException e) {
				e.printStackTrace();
			}

			if(session.getUser().equals(checkedOutUser))
				enable_editor=true;
			else
				enable_editor=false;
		}
		else 
		{
			enable_editor=false;
		}

	}	

	/**
	 * 
	 */
	public void loadConfigurations()
	{
		try {
			LoadPAMConfigurations loadConfig = new LoadPAMConfigurations();

			Vector<PAMTableConfiguration> tableConfigs = new Vector<PAMTableConfiguration>();

			tableConfigs = loadConfig.readPreferenceValues1(renderedPropName+PAMConstant.CONFIGURATIONS, session);

			PAMPropertyNameValue_v.clear();

			boolean bTableconfig = false;

			TCComponent[] renderedComponents = pamSpecRevision.getRelatedComponents(renderedPropName);

			TCComponent[] structuredComponents = pamSpecRevision.getRelatedComponents("U4_StructuredPropRelation");

			List<TCComponent> allSecondaryComponents = new ArrayList<TCComponent>(renderedComponents.length + structuredComponents.length);
			Collections.addAll(allSecondaryComponents, renderedComponents);
			Collections.addAll(allSecondaryComponents, structuredComponents);

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

			if(isCheckOut == true )
			{
				ArrayList<TCComponent> formarray = new ArrayList<TCComponent>();

				// Iterating to collect the forms to perform a single refreshObjects call
				for(int inx=0;inx<tableConfigs.size();inx++)
				{	
					if (packcomponentType == null || pamSecType == null || tableConfigs.get(inx).primaryType ==null)
						continue;

					if (tableConfigs.get(inx).packcomponentType != null)
					{
						if (((tableConfigs.get(inx).primaryType).contains(pamSecType) == true ) && ((tableConfigs.get(inx).packcomponentType).equals(packcomponentType) ==true ))
						{
							List<TCComponent> lst = (List<TCComponent>)tname_tccomponent.get(tableConfigs.get(inx).secondaryType);
							if(lst!=null)
							formarray.addAll(lst);
							continue;
						}
					}

					if	(  	((tableConfigs.get(inx).primaryType).contains(pamSecType) && (tableConfigs.get(inx).packcomponentType == null )) ||
							((tableConfigs.get(inx).primaryType).contains("All"))
							)
					{
						if (tname_tccomponent.containsKey(tableConfigs.get(inx).secondaryType))
						{
							List<TCComponent> lst = (List<TCComponent>)tname_tccomponent.get(tableConfigs.get(inx).secondaryType);
							if(lst!=null)
							formarray.addAll(lst);
						}
					}
				}

				Date logindate =  new Date(session.getStartTime());

				ArrayList<TCComponent> refresh_array = new ArrayList<TCComponent>(formarray);

				//load the form only if it is modified after the current TC session was started.
				Iterator<TCComponent> it = refresh_array.iterator();

				while (it.hasNext()) {
					Date form_lmd = it.next().getDateProperty("last_mod_date");

					if (form_lmd.after(logindate) ==false)
						it.remove();
				}

				if (refresh_array.size()>0)
				{
					long sstartTime = System.currentTimeMillis();

					DataManagementService dmService = DataManagementService.getService(session);
					dmService.refreshObjects(refresh_array.toArray(new TCComponent[refresh_array.size()]));

					if(UnileverUtility.isPerfMonitorTriggered == true)
						UnileverUtility.getPerformanceTime(sstartTime, "dmService.refreshObjects in EPAMTableComposite " + formarray.size() );
				}
			}

			for(int inx=0;inx<tableConfigs.size();inx++)
			{	
				Vector<PAMSecondaryPropValue> secondaryPropValue =new Vector<PAMSecondaryPropValue>();

				if (packcomponentType == null || pamSecType == null || tableConfigs.get(inx).packcomponentType == null || tableConfigs.get(inx).primaryType ==null)
					continue;

				if(((tableConfigs.get(inx).primaryType).contains(pamSecType) == true ) && ((tableConfigs.get(inx).packcomponentType).equals(packcomponentType) ==true ))
				{
					bTableconfig = true;
					//this gives all the related forms to the PAM SPec with the relation mentioned in the renderingHint and the type mentioned in the preference

					Vector<TCComponent >relatedComponents =getRelatedComponentOfASecondayType(tname_tccomponent, renderedPropName,tableConfigs.get(inx).secondaryType);

					if (relatedComponents.size() ==0)
						continue;

					//this then sorts the obtained forms based on the row configuration mentioned in the preference
					if(!((tableConfigs.get(inx)).rowConfigurations.propertyName.equalsIgnoreCase("")))
						relatedComponents = sortForms(relatedComponents,(tableConfigs.get(inx)).rowConfigurations.propertyName,((tableConfigs.get(inx)).rowConfigurations.propertyValues).toArray(new String[(tableConfigs.get(inx)).rowConfigurations.propertyValues.size()]));

					int [] proptype = null;

					//looping the related object and adding the column and row configurations using the custom class
					for(int jnx=0; jnx<relatedComponents.size();jnx++)
					{
						TCComponentForm tempForm = (TCComponentForm)relatedComponents.get(jnx);

						//getting the values for the required Columns						
						PAMPropertyNameValue[] propValueArray = new PAMPropertyNameValue[(tableConfigs.get(inx)).ColumnConfigurations.length];

						String [] formproperties = new String [(tableConfigs.get(inx)).ColumnConfigurations.length];

						for(int knx=0;knx<(tableConfigs.get(inx)).ColumnConfigurations.length;knx++)
							formproperties[knx]=(tableConfigs.get(inx)).ColumnConfigurations[knx].columnName;

						TCProperty []currProperty = tempForm.getTCProperties(formproperties);

						if (jnx == 0)
						{
							proptype =  new int [(tableConfigs.get(inx)).ColumnConfigurations.length];

							for(int knx=0;knx<(tableConfigs.get(inx)).ColumnConfigurations.length;knx++)
								proptype[knx]=currProperty[knx].getPropertyType();
						}

						for(int knx=0;knx<(tableConfigs.get(inx)).ColumnConfigurations.length;knx++)
						{
							propValueArray[knx] = new PAMPropertyNameValue();
							propValueArray[knx].propName= (tableConfigs.get(inx)).ColumnConfigurations[knx].columnName;
							propValueArray[knx].tcProperty = currProperty[knx];
							propValueArray[knx].propDisplayName = currProperty[knx].getPropertyDisplayName();
							propValueArray[knx].propValue= currProperty[knx].getDisplayValue();
							propValueArray[knx].columnSize = (tableConfigs.get(inx).ColumnConfigurations)[knx].columnSize;
							propValueArray[knx].isEnabled = (tableConfigs.get(inx).ColumnConfigurations)[knx].isEnabled;
							propValueArray[knx].isStructured = (tableConfigs.get(inx).ColumnConfigurations)[knx].isStructured;
							propValueArray[knx].PropertyType=proptype[knx];
						}
						//getting Values required for each table
						PAMSecondaryPropValue tempSecondaryPropValue = new PAMSecondaryPropValue();
						tempSecondaryPropValue.propNameValuepair = propValueArray;
						tempSecondaryPropValue.secondaryName = tempForm.getType();
						tempSecondaryPropValue.selectedComponent = tempForm;
						secondaryPropValue.add(tempSecondaryPropValue);
					}	
					if(secondaryPropValue.size()>0)
						PAMPropertyNameValue_v.add(secondaryPropValue);
					//break;
				}
			}

			if (bTableconfig == false)
			{
				for(int inx=0;inx<tableConfigs.size();inx++)
				{	
					Vector<PAMSecondaryPropValue> secondaryPropValue =new Vector<PAMSecondaryPropValue>();

					if((tableConfigs.get(inx).primaryType).contains(pamSecType) && (tableConfigs.get(inx).packcomponentType == null ))
					{
						bTableconfig = true;

						//this gives all the related forms to the PAM SPec with the relation mentioned in the renderingHint and the type mentioned in the preference
						Vector<TCComponent >relatedComponents =getRelatedComponentOfASecondayType(tname_tccomponent, renderedPropName,tableConfigs.get(inx).secondaryType);

						if (relatedComponents.size() ==0)
							continue;

						//this then sorts the obtained forms based on the row configuration mentioned in the preference
						if(!((tableConfigs.get(inx)).rowConfigurations.propertyName.equalsIgnoreCase("")))
							relatedComponents = sortForms(relatedComponents,(tableConfigs.get(inx)).rowConfigurations.propertyName,((tableConfigs.get(inx)).rowConfigurations.propertyValues).toArray(new String[(tableConfigs.get(inx)).rowConfigurations.propertyValues.size()]));

						int [] proptype = null;

						//looping the related object and adding the column and row configurations using the custom class
						for(int jnx=0; jnx<relatedComponents.size();jnx++)
						{
							TCComponentForm tempForm = (TCComponentForm)relatedComponents.get(jnx);

							//getting the values for the required Columns						
							PAMPropertyNameValue[] propValueArray = new PAMPropertyNameValue[(tableConfigs.get(inx)).ColumnConfigurations.length];

							String [] formproperties = new String [(tableConfigs.get(inx)).ColumnConfigurations.length];

							for(int knx=0;knx<(tableConfigs.get(inx)).ColumnConfigurations.length;knx++)
								formproperties[knx]=(tableConfigs.get(inx)).ColumnConfigurations[knx].columnName;

							TCProperty []currProperty = tempForm.getTCProperties(formproperties);

							if (jnx == 0)
							{
								proptype =  new int [(tableConfigs.get(inx)).ColumnConfigurations.length];

								for(int knx=0;knx<(tableConfigs.get(inx)).ColumnConfigurations.length;knx++)
									proptype[knx]=currProperty[knx].getPropertyType();
							}

							for(int knx=0;knx<(tableConfigs.get(inx)).ColumnConfigurations.length;knx++)
							{
								propValueArray[knx] = new PAMPropertyNameValue();
								propValueArray[knx].propName= (tableConfigs.get(inx)).ColumnConfigurations[knx].columnName;
								propValueArray[knx].tcProperty = currProperty[knx];
								propValueArray[knx].propDisplayName = currProperty[knx].getPropertyDisplayName();
								propValueArray[knx].propValue= currProperty[knx].getDisplayValue();	
								propValueArray[knx].columnSize = (tableConfigs.get(inx).ColumnConfigurations)[knx].columnSize;
								propValueArray[knx].isEnabled = (tableConfigs.get(inx).ColumnConfigurations)[knx].isEnabled;
								propValueArray[knx].isStructured = (tableConfigs.get(inx).ColumnConfigurations)[knx].isStructured;
								propValueArray[knx].PropertyType=proptype[knx];
							}
							//getting Values required for each table
							PAMSecondaryPropValue tempSecondaryPropValue = new PAMSecondaryPropValue();
							tempSecondaryPropValue.propNameValuepair = propValueArray;
							tempSecondaryPropValue.secondaryName = tempForm.getType();	
							tempSecondaryPropValue.selectedComponent = tempForm;					
							secondaryPropValue.add(tempSecondaryPropValue);						
						}	
						if(secondaryPropValue.size()>0)
							PAMPropertyNameValue_v.add(secondaryPropValue);	
						//break;
					}
				}
			}

			if (bTableconfig == false)
			{
				for(int inx=0;inx<tableConfigs.size();inx++)
				{	
					Vector<PAMSecondaryPropValue> secondaryPropValue =new Vector<PAMSecondaryPropValue>();

					if((tableConfigs.get(inx).primaryType).contains("All"))
					{
						bTableconfig = true;
						//this gives all the related forms to the PAM SPec with the relation mentioned in the renderingHint and the type mentioned in the preference
						Vector<TCComponent >relatedComponents =getRelatedComponentOfASecondayType(tname_tccomponent, renderedPropName,tableConfigs.get(inx).secondaryType);

						if (relatedComponents.size() ==0)
							continue;

						//this then sorts the obtained forms based on the row configuration mentioned in the preference
						if(!((tableConfigs.get(inx)).rowConfigurations.propertyName.equalsIgnoreCase("")))
							relatedComponents = sortForms(relatedComponents,(tableConfigs.get(inx)).rowConfigurations.propertyName,((tableConfigs.get(inx)).rowConfigurations.propertyValues).toArray(new String[(tableConfigs.get(inx)).rowConfigurations.propertyValues.size()]));

						int [] proptype = null;

						//looping the related object and adding the column and row configurations using the custom class
						for(int jnx=0; jnx<relatedComponents.size();jnx++)
						{
							TCComponentForm tempForm = (TCComponentForm)relatedComponents.get(jnx);

							//getting the values for the required Columns
							PAMPropertyNameValue[] propValueArray = new PAMPropertyNameValue[(tableConfigs.get(inx)).ColumnConfigurations.length];

							String [] formproperties = new String [(tableConfigs.get(inx)).ColumnConfigurations.length];

							for(int knx=0;knx<(tableConfigs.get(inx)).ColumnConfigurations.length;knx++)
								formproperties[knx]=(tableConfigs.get(inx)).ColumnConfigurations[knx].columnName;

							TCProperty []currProperty = tempForm.getTCProperties(formproperties);

							if (jnx == 0)
							{
								proptype =  new int [(tableConfigs.get(inx)).ColumnConfigurations.length];

								for(int knx=0;knx<(tableConfigs.get(inx)).ColumnConfigurations.length;knx++)
									proptype[knx]=currProperty[knx].getPropertyType();
							}

							for(int knx=0;knx<(tableConfigs.get(inx)).ColumnConfigurations.length;knx++)
							{
								propValueArray[knx] = new PAMPropertyNameValue();
								propValueArray[knx].propName= (tableConfigs.get(inx)).ColumnConfigurations[knx].columnName;
								propValueArray[knx].tcProperty = currProperty[knx];
								propValueArray[knx].propDisplayName = currProperty[knx].getPropertyDisplayName();
								propValueArray[knx].propValue= currProperty[knx].getDisplayValue();
								propValueArray[knx].columnSize = (tableConfigs.get(inx).ColumnConfigurations)[knx].columnSize;
								propValueArray[knx].isEnabled = (tableConfigs.get(inx).ColumnConfigurations)[knx].isEnabled;
								propValueArray[knx].isStructured = (tableConfigs.get(inx).ColumnConfigurations)[knx].isStructured;
								propValueArray[knx].PropertyType=proptype[knx];
							}
							//getting Values required for each table
							PAMSecondaryPropValue tempSecondaryPropValue = new PAMSecondaryPropValue();
							tempSecondaryPropValue.propNameValuepair = propValueArray;
							tempSecondaryPropValue.secondaryName = tempForm.getType();
							tempSecondaryPropValue.selectedComponent = tempForm;
							secondaryPropValue.add(tempSecondaryPropValue);
						}	
						if(secondaryPropValue.size()>0)
							PAMPropertyNameValue_v.add(secondaryPropValue);
						//break;
					}

				}
			}

			tableComposite = new Composite(parentComposite, SWT.NONE);
			GridData localGridData = new GridData(1808);
			localGridData.heightHint = (PAMPropertyNameValue_v.size())*ePAMTableHeight+100;
			localGridData.widthHint = ePAmTableWidth;
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
		long startTime = System.currentTimeMillis();

		loadConfigurations();
		
		if(UnileverUtility.isPerfMonitorTriggered == true)
			UnileverUtility.getPerformanceTime(startTime, "loadConfigurations in EPAMTableComposite");

		Table[] propertyTables = new Table[PAMPropertyNameValue_v.size()];
		for(int inx=0;inx<PAMPropertyNameValue_v.size();inx++)
		{
			startTime = System.currentTimeMillis();
			
			if (PAMPropertyNameValue_v.get(inx).get(0).secondaryName.compareToIgnoreCase(UL4Common.COMPLIANCERULE_FORM) == 0)
			{
				if (check_mir_phase_roll_out () == false)
					continue;
			}

			int style = SWT.VIRTUAL | SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.HIDE_SELECTION;
			Label propertyLabel = new Label(tableComposite,SWT.NONE);
			String tableType = (PAMPropertyNameValue_v.get(inx)).get(0).selectedComponent.getDisplayType();

			// defect#773 fix
			if(renderedPropName.equalsIgnoreCase(PAMConstant.PNP_CUC_DEFECTS_RELATION_NAME))
				tableType= PAMConstant.PNP_CUC_DEFECTS_TABLE_NAME ;
			else if(renderedPropName.equalsIgnoreCase(PAMConstant.PNP_CU_DEFECTS_RELATION_NAME))
				tableType= PAMConstant.PNP_CU_DEFECTS_TABLE_NAME ;
			else if(renderedPropName.equalsIgnoreCase(PAMConstant.PNP_CASEBAGETC_DEFECTS_RELATION_NAME) )
				tableType= PAMConstant.PNP_CASEBAGETC_DEFECTS_TABLE_NAME ;
			else if(renderedPropName.equalsIgnoreCase(PAMConstant.PNP_PALLET_DEFECTS_RELATION_NAME) )
				tableType= PAMConstant.PNP_PALLET_DEFECTS_TABLE_NAME ;

			section = tableType;
			propertyLabel.setText(tableType+":");
			propertyLabel.pack();
			propertyTables[inx] = new Table(tableComposite,style );
			GridData localGridData1 = new GridData(1808);
			propertyTables[inx].setRedraw(false);
			localGridData1.heightHint = ePAMTableHeight;
			localGridData1.widthHint = ePAmTableWidth;

			propertyTables[inx].setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

			propertyTables[inx].setHeaderVisible(true);
			propertyTables[inx].setLinesVisible(true);

			Display display =  propertyTables[inx].getDisplay();
			if(isCheckOut)
			{
				if(enable_editor)
				{
					Color white = display.getSystemColor(SWT.COLOR_WHITE);
					propertyTables[inx].setBackground(white);
				}
				else
				{
					Color lightGray = new Color (display, 240, 240, 240);
					propertyTables[inx].setBackground(lightGray);
				}
			}
			else
			{
				Color lightGray = new Color (display, 240, 240, 240);
				propertyTables[inx].setBackground(lightGray);
			}

			setTableConfiguration(propertyTables[inx],PAMPropertyNameValue_v.get(inx).toArray(new PAMSecondaryPropValue[PAMPropertyNameValue_v.get(inx).size()]));

			if(UnileverUtility.isPerfMonitorTriggered == true)
				UnileverUtility.getPerformanceTime(startTime, "setTableConfiguration " + tableType );
			
			propertyTables[inx].setRedraw(true);
		}

	}

	/**
	 * @param tcTable
	 * @param tableConfigValues
	 */
	public void setTableConfiguration(final Table tcTable, final PAMSecondaryPropValue[] tableConfigValues)
	{
		final LOVService mLOVService = LOVService.getService( session );

		LOVSearchResults pkgdetail_InitDataResponse = null;

		String formType = tableConfigValues[0].secondaryName;

		if ( ( formType.equals("U4_PackageDetailsForm") ) && (isCheckOut == true) )
		{
			TCComponentListOfValues PkgDtls_lov = TCComponentListOfValuesType.findLOVByName("U4_PL_PkgDetailsLOV");

			if (PkgDtls_lov != null) 
			{
				try {
					InitialLovData mInitData = new InitialLovData( );
					mInitData.lov = PkgDtls_lov;
					mInitData.propertyName=tableConfigValues[0].propNameValuepair[0].propName;
					mInitData.lovInput.owningObject=tableConfigValues[0].selectedComponent;
					pkgdetail_InitDataResponse = mLOVService.getInitialLOVValues(mInitData);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}	
		}

		final Map<String,Integer > textLimit = new HashMap<String,Integer>(PAMConstant.getTextLimit (tableConfigValues[0]));

		final int nColumn  = tableConfigValues[0].propNameValuepair.length;

		for(  int ColumnNumber =0;ColumnNumber< tableConfigValues[0].propNameValuepair.length;ColumnNumber++ )
		{
			final TableColumn propertyColumn =  new TableColumn(tcTable, SWT.BORDER_SOLID|SWT.COLOR_WIDGET_NORMAL_SHADOW);
			propertyColumn.setWidth(tableConfigValues[0].propNameValuepair[ColumnNumber].columnSize);

			String nametext = tableConfigValues[0].propNameValuepair[ColumnNumber].propDisplayName;
			propertyColumn.setText(nametext);
			propertyColumn.setResizable(true);
		}

		tcTable.setItemCount(tableConfigValues.length);

		tcTable.setData("pkg_details_lov", pkgdetail_InitDataResponse);

		tcTable.addListener(SWT.SetData, new Listener() {
			public void handleEvent(Event event) {

				final TableItem item = (TableItem) event.item;
				int row = tcTable.indexOf(item);
				boolean legend = PAMConstant.ruleValidate((TCComponentForm) tableConfigValues[row].selectedComponent);
				TCComponentForm currForm = (TCComponentForm) tableConfigValues[row].selectedComponent;
				final String formType = tableConfigValues[row].secondaryName;
				final Map<String,Object> compPropVal_m = new HashMap<String,Object>();

				try
				{
					if(formType.equalsIgnoreCase(PALLETFORM) )
					{
						String property  =  currForm.getStringProperty("u4_property");
						String attribute  =  currForm.getStringProperty("u4_attribute");

						if( property.equalsIgnoreCase(PALLETPROP) && attribute.equalsIgnoreCase(PALLETAAT))
							if(dderevision.getLogicalProperty("u4_palletized"))
								legend = true ;
					}
				}
				catch (TCException e2)
				{
					e2.printStackTrace();
				}


				for(  int ColumnNumber =0;ColumnNumber< nColumn;ColumnNumber++ )
				{
					String PropValue = tableConfigValues[row].propNameValuepair[ColumnNumber].propValue;
					int propType =  tableConfigValues[row].propNameValuepair[ColumnNumber].PropertyType;
					TCProperty currTCProp =  tableConfigValues[row].propNameValuepair[ColumnNumber].tcProperty;
					String propname = tableConfigValues[row].propNameValuepair[ColumnNumber].propName;
				
					TableEditor editor = new TableEditor(tcTable);

					if(ColumnNumber == 0 && legend )
					{
						TableEditor mandatroyEditor = new TableEditor(tcTable);
						PAMConstant.setMandatory(PropValue,mandatroyEditor,item);
					}

					if(!(tableConfigValues[row].propNameValuepair[ColumnNumber].isEnabled))
					{
						item.setText(ColumnNumber,PropValue);
						continue;
					}

					if( isStructuredComponent(currForm) && tableConfigValues[row].propNameValuepair[ColumnNumber].isStructured)
					{
						item.setText(ColumnNumber,PropValue);
						continue;
					}

					switch (propType)
					{
					case 1:
						Text text = new Text(tcTable, SWT.NONE);
						if(PropValue!=null )
							text.setText(PropValue);
						editor.grabHorizontal = true;
						editor.setEditor(text,item, ColumnNumber);
						text.setData("colId", ColumnNumber);
						editor.getEditor().setEnabled(enable_editor);
						text.addModifyListener(new ModifyListener() {
							public void modifyText(ModifyEvent event) {
								PAMSecondaryPropValue currentSecPropValues = tableConfigValues[tcTable.indexOf(item)];
								compPropVal_m.put("puid",currentSecPropValues.selectedComponent);
								compPropVal_m.put("section",section);
								int columnNum = (Integer) ((Text)(event.getSource())).getData("colId");
								compPropVal_m.put(currentSecPropValues.propNameValuepair[columnNum].propName,((Text) (event.getSource())).getText());
							}
						});
						break;
					case 2:// Date Property Type
						
						text = new Text(tcTable, SWT.NONE);
						if(PropValue!=null )
							text.setText(PropValue);
						editor.grabHorizontal = true;
						editor.setEditor(text,item, ColumnNumber);
						text.setData("colId", ColumnNumber);
						editor.getEditor().setEnabled(enable_editor);
						text.setData("section", section);
						text.addModifyListener(new ModifyListener() {
							public void modifyText(ModifyEvent event) {
								//getting the row index 
								/**
								 * Replaced the call with - tableConfigValues[tcTable.indexOf(item)] instead of tempRowConfigArray[tcTable.indexOf(item)] ;
								 * Which was causing 'NullPointerException'
								 */
								PAMSecondaryPropValue currentSecPropValues = tableConfigValues[tcTable.indexOf(item)]; //tempRowConfigArray[tcTable.indexOf(item)] ;		
								compPropVal_m.put("puid",currentSecPropValues.selectedComponent);
								String section = (String) ((Text)(event.getSource())).getData("section");
								if (section!=null)
									compPropVal_m.put("section",section);
								int columnNum = (Integer) ((Text)(event.getSource())).getData("colId");
								compPropVal_m.put(currentSecPropValues.propNameValuepair[columnNum].propName,((Text) (event.getSource())).getText());
							}
						});
						break;
					case 3:

						text = new Text(tcTable, SWT.NONE);
						if(PropValue!=null )
							text.setText(PropValue);
						editor.grabHorizontal = true;
						editor.setEditor(text,item, ColumnNumber);
						editor.getEditor().setEnabled(enable_editor);

						if (isCheckOut==true)
						{
							text.setData("colId", ColumnNumber);
							text.setData("section", section);

							text.addModifyListener(new ModifyListener() {
								public void modifyText(ModifyEvent event) {

									/**
									 * Replaced the call with - tableConfigValues[tcTable.indexOf(item)] instead of tempRowConfigArray[tcTable.indexOf(item)] ;
									 * Which was causing 'NullPointerException'
									 */
									PAMSecondaryPropValue currentSecPropValues = tableConfigValues[tcTable.indexOf(item)]; //tempRowConfigArray[tcTable.indexOf(item)] ;
									if(currentSecPropValues != null)
									{
										compPropVal_m.put("puid",currentSecPropValues.selectedComponent);
										String section = (String) ((Text)(event.getSource())).getData("section");
										if (section!=null)
											compPropVal_m.put("section",section);
										int columnNum = (Integer) ((Text)(event.getSource())).getData("colId");
										compPropVal_m.put(currentSecPropValues.propNameValuepair[columnNum].propName,((Text) (event.getSource())).getText());
									}
								}
							});
						}
						break;
					case 4:
						text = new Text(tcTable, SWT.NONE);
						if(PropValue!=null )
							text.setText(PropValue);
						editor.grabHorizontal = true;
						editor.setEditor(text,item, ColumnNumber);
						text.setData("colId", ColumnNumber);
						editor.getEditor().setEnabled(enable_editor);
						text.addModifyListener(new ModifyListener() {
							public void modifyText(ModifyEvent event) {
								//getting the row index 
								/**
								 * Replaced the call with - tableConfigValues[tcTable.indexOf(item)] instead of tempRowConfigArray[tcTable.indexOf(item)] ;
								 * Which was causing 'NullPointerException'
								 */
								PAMSecondaryPropValue currentSecPropValues = tableConfigValues[tcTable.indexOf(item)]; //tempRowConfigArray[tcTable.indexOf(item)] ;		
								compPropVal_m.put("puid",currentSecPropValues.selectedComponent);
								compPropVal_m.put("section",section);
								int columnNum = (Integer) ((Text)(event.getSource())).getData("colId");
								compPropVal_m.put(currentSecPropValues.propNameValuepair[columnNum].propName,((Text) (event.getSource())).getText());
							}
						});
						break;
					case 5: //int
						text = new Text(tcTable, SWT.NONE);
						if(PropValue!=null )
							text.setText(PropValue);
						editor.grabHorizontal = true;
						editor.setEditor(text,item, ColumnNumber);
						text.setData("colId", ColumnNumber);
						editor.getEditor().setEnabled(enable_editor);
						text.setData("section", section);
						if (isCheckOut == true)
						{
							text.addModifyListener(new ModifyListener() {
								public void modifyText(ModifyEvent event) {
									//getting the row index 
									/**
									 * Replaced the call with - tableConfigValues[tcTable.indexOf(item)] instead of tempRowConfigArray[tcTable.indexOf(item)] ;
									 * Which was causing 'NullPointerException'
									 */
									PAMSecondaryPropValue currentSecPropValues = tableConfigValues[tcTable.indexOf(item)]; //tempRowConfigArray[tcTable.indexOf(item)] ;		
									compPropVal_m.put("puid",currentSecPropValues.selectedComponent);
									String section = (String) ((Text)(event.getSource())).getData("section");

									if (section!=null)
										compPropVal_m.put("section",section);
									int columnNum = (Integer) ((Text)(event.getSource())).getData("colId");
									compPropVal_m.put(currentSecPropValues.propNameValuepair[columnNum].propName,((Text) (event.getSource())).getText());
								}
							});
						}
						break;

					case 6:

						Button checkButton = new Button(tcTable, SWT.CHECK);

						if(PropValue.equalsIgnoreCase("True"))
							checkButton.setSelection(true);
						editor.grabHorizontal = true;
						editor.setEditor(checkButton,item,ColumnNumber);
						editor.getEditor().setEnabled(enable_editor);
						CheckboxCellEditor checkBoxEditor = new CheckboxCellEditor(tcTable);
						if(PropValue.equalsIgnoreCase("True"))
							checkBoxEditor.setValue(true);

						if (isCheckOut==true)
						{
							checkButton.setData("colId", ColumnNumber);

							checkButton.addSelectionListener(new SelectionAdapter() {
								public void widgetSelected(SelectionEvent event) { 

									//getting the row index 
									/**
									 * Replaced the call with - tableConfigValues[tcTable.indexOf(item)] instead of tempRowConfigArray[tcTable.indexOf(item)] ;
									 * Which was causing 'NullPointerException'
									 */
									PAMSecondaryPropValue currentSecPropValues = tableConfigValues[tcTable.indexOf(item)]; //tempRowConfigArray[tcTable.indexOf(item)] ;		
									compPropVal_m.put("puid",currentSecPropValues.selectedComponent);
									compPropVal_m.put("section",section);
									int columnNum = (Integer) ((Button)(event.getSource())).getData("colId");
									String isTrue= (((Button) (event.getSource())).getSelection()==true)?"True":"False";
									compPropVal_m.put(currentSecPropValues.propNameValuepair[columnNum].propName,isTrue);
								}
							});
						}
						break;
					case 7:
						break;
					case 8:

						// This is a special logic to render boolean property has Yes/No radio button for compliance rule form
						if ((tableConfigValues[row].secondaryName.compareToIgnoreCase(UL4Common.COMPLIANCERULE_FORM) == 0) &&
								(propname.compareTo("u4_confirm") ==0 ) )
						{					
							TableEditor editor1 = new TableEditor(item.getParent());
							item.setData("EDITOR", editor1);
							Composite comp = new Composite(item.getParent(), SWT.NONE);
							comp.setBackground(item.getParent().getBackground());
							comp.setBackgroundMode(SWT.INHERIT_DEFAULT);
							RowLayout l = new RowLayout();
							l.marginHeight=l.marginWidth=l.marginTop=l.marginBottom= 0;
							comp.setLayout(l);
							editor1.setEditor(comp,item,ColumnNumber);

							Control ctrl = ((Control) editor1.getEditor());

							if (ctrl!=null)
								ctrl.setEnabled(enable_editor);

							Button yes_button = new Button(comp, SWT.RADIO);
							yes_button.setText("Yes");
							yes_button.setData("colId", ColumnNumber);

							Button no_button = new Button(comp, SWT.RADIO);
							no_button.setText("No");
							no_button.setData("colId", ColumnNumber);

							editor1.grabHorizontal = true;
							editor1.setEditor(comp, item, 1);

							if (PropValue!=null)
							{
								if (PropValue.equalsIgnoreCase("No"))
								{
									yes_button.setSelection(false);
									no_button.setSelection(true);
								}
								else if (PropValue.equalsIgnoreCase(""))
								{
									yes_button.setSelection(false);
									no_button.setSelection(false);
								}
								else if (PropValue.equalsIgnoreCase("Yes"))
								{
									yes_button.setSelection(true);
									no_button.setSelection(false);
								}
							}

							if (isCheckOut==true)
							{
								yes_button.addSelectionListener(new SelectionAdapter() {
									public void widgetSelected(SelectionEvent event) {
										PAMSecondaryPropValue currentSecPropValues = tableConfigValues[tcTable.indexOf(item)] ;
										//PAMSecondaryPropValue currentSecPropValues = tableConfigValues[0] ;//hard coding to get the first configuration
										compPropVal_m.put("puid",currentSecPropValues.selectedComponent);
										int columnNum = (Integer) ((Button)(event.getSource())).getData("colId"); //getting the column number
										compPropVal_m.put(currentSecPropValues.propNameValuepair[columnNum].propName,"Yes");//adding to the Map
									}
								});

								no_button.addSelectionListener(new SelectionAdapter() {
									public void widgetSelected(SelectionEvent event) {
										PAMSecondaryPropValue currentSecPropValues = tableConfigValues[tcTable.indexOf(item)] ;
										//PAMSecondaryPropValue currentSecPropValues = tableConfigValues[0] ;//hard coding to get the first configuration
										compPropVal_m.put("puid",currentSecPropValues.selectedComponent);
										int columnNum = (Integer) ((Button)(event.getSource())).getData("colId"); //getting the column number
										compPropVal_m.put(currentSecPropValues.propNameValuepair[columnNum].propName,"No");//adding to the Map
									}
								});
							}

							continue;
						}

						//defect#762 fix - start
						int textLength = 0;
						if (textLimit!=null)
							textLength = textLimit.get(propname).intValue();
						
						TCComponentListOfValues currLOVValues = null;
						ListOfValuesInfo tempLOVInfo;
						Object[] lovValues = null;

						try {

							// check for COMPLIANCERULE_FORM type, since getStringProperty was crashing otherwise after adding the disposal synccountries soa call
							if(ColumnNumber==2 && (tableConfigValues[row].secondaryName.compareToIgnoreCase(UL4Common.COMPLIANCERULE_FORM) != 0))
							{
								String currFormname = currForm.getStringProperty("object_name");

								if(currFormname.equalsIgnoreCase("Approved Food Grade"))
									currLOVValues = TCComponentListOfValuesType.findLOVByName("U4_PL_Yes-NoLOV");
								else if(currFormname.equalsIgnoreCase("Treatment") || 
										currFormname.equalsIgnoreCase("Treatment_Facestock"))
									currLOVValues = TCComponentListOfValuesType.findLOVByName("U4_TreatmentLOV");
								else if(currFormname.equalsIgnoreCase("Type_Liner"))
									currLOVValues = TCComponentListOfValuesType.findLOVByName("U4_LinerLayerLOV");
								else if(currFormname.equalsIgnoreCase("Colour_Liner")|| 
										currFormname.equalsIgnoreCase("Colour_Facestock"))
									currLOVValues = TCComponentListOfValuesType.findLOVByName("U4_ColourLOV");
								else if(currFormname.equalsIgnoreCase("Type_Facestock"))
									currLOVValues = TCComponentListOfValuesType.findLOVByName("U4_FacestockLOV");
								else if(currFormname.equalsIgnoreCase("Surface Finish_Facestock"))
									currLOVValues = TCComponentListOfValuesType.findLOVByName("U4_SurfaceFinishLOV");
								else if(currFormname.equalsIgnoreCase("Type_Adhesive"))
									currLOVValues = TCComponentListOfValuesType.findLOVByName("U4_AdhesiveLOV ");
								else if(currFormname.equalsIgnoreCase("Type_Shrink Sleeve"))
									currLOVValues = TCComponentListOfValuesType.findLOVByName("U4_ShrinkSleeveLOV");
								else if(currFormname.equalsIgnoreCase("Application_Shrink Sleeve"))
									currLOVValues = TCComponentListOfValuesType.findLOVByName("U4_ShrinkSleeveApplLOV");
								else if(currFormname.startsWith("Flexible/Rigid"))
									currLOVValues = TCComponentListOfValuesType.findLOVByName("U4_PnP_DSD_LOV");
								if(currLOVValues!=null)
								{
									try {
										tempLOVInfo = currLOVValues.getListOfValues();
										lovValues = tempLOVInfo.getListOfValues();
									} catch (TCException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}

								if(lovValues !=null)
								{	
									CCombo  combo = new CCombo(tcTable, SWT.DROP_DOWN | SWT.READ_ONLY);
									combo.setData("colId", ColumnNumber);
									String[] lovStrings = Arrays.copyOf(lovValues, lovValues.length, String[].class);
									
									if(lovStrings!=null)
										if (isCheckOut == true)//setItems was taking 40-90 milli seconds, hence we will set the items only when in check out state
											combo.setItems( lovStrings);
									
									combo.add("", 0);

									//combo.setItem( lovStrings.length,"");
									if(PropValue!=null )
										combo.setText(PropValue);

									editor.grabHorizontal = true;
									editor.setEditor(combo, item, ColumnNumber);
									editor.getEditor().setEnabled(enable_editor);
									if (isCheckOut==true)
									{
										combo.addSelectionListener(new SelectionAdapter() {
											public void widgetSelected(SelectionEvent event) {
												PAMSecondaryPropValue currentSecPropValues = tableConfigValues[tcTable.indexOf(item)] ;
												compPropVal_m.put("puid",currentSecPropValues.selectedComponent);
												compPropVal_m.put("section",section);
												int columnNum = (Integer) ((CCombo)(event.getSource())).getData("colId");
												compPropVal_m.put(currentSecPropValues.propNameValuepair[columnNum].propName,((CCombo) (event.getSource())).getText());
											}
										});
									}
								}
							}
						}
						catch (TCException e1) {
							e1.printStackTrace();
						}//defect#762 fix- end

						boolean has_lov = false;

						if(lovValues==null && ( currLOVValues = currTCProp.getLOV() )!= null)
						{	
							if(isInterdependantLov(formType, propname))
							{
								has_lov =true;
								LOVSearchResults mInitDataResponse = null ;

								if (isCheckOut == true) 
								{
									if (formType.compareTo("U4_PackageDetailsForm") == 0)
										mInitDataResponse = (LOVSearchResults) tcTable.getData("pkg_details_lov");
									else
									{
										InitialLovData mInitData = new InitialLovData();
										mInitData.propertyName=currTCProp.getPropertyName();
										mInitData.lovInput.owningObject=currTCProp.getTCComponent();
										mInitData.lov = currLOVValues;
										mInitDataResponse = mLOVService.getInitialLOVValues( mInitData );
									}

									for( LOVValueRow LOVValue : mInitDataResponse.lovValues )
									{
										String mValue = ((String[])LOVValue.propDisplayValues.get( "lov_values" ))[0];

										has_lov =false;

										if( mValue.equalsIgnoreCase( item.getText( )))
										{
											lovValues = new String[LOVValue.childRows.length];
											for( int index = 0; index < LOVValue.childRows.length; index++ )
												lovValues[index] = ((String[])LOVValue.childRows[index].propDisplayValues.get( "lov_values" ))[0];
											has_lov =true;
											break;
										}
									}
								}
							}
							else
							{
								try {

									tempLOVInfo = currLOVValues.getListOfValues();
									lovValues =  tempLOVInfo.getListOfValues();
									has_lov =true;
								} catch (TCException e) {
									e.printStackTrace();
								}
							}

							if (has_lov==true)
							{
								CCombo  combo = new CCombo(tcTable, SWT.DROP_DOWN | SWT.READ_ONLY);
								combo.setData("colId", ColumnNumber);

								if(PropValue!=null )
									combo.setText(PropValue);

								editor.grabHorizontal = true;
								editor.setEditor(combo, item, ColumnNumber);
								editor.getEditor().setEnabled(enable_editor);
								if (isCheckOut==true)
								{
									combo.addSelectionListener(new SelectionAdapter() {
										public void widgetSelected(SelectionEvent event) {
											PAMSecondaryPropValue currentSecPropValues = tableConfigValues[tcTable.indexOf(item)] ;//getting the row index 
											compPropVal_m.put("puid",currentSecPropValues.selectedComponent);
											compPropVal_m.put("section",section);
											int columnNum = (Integer) ((CCombo)(event.getSource())).getData("colId");//getting the column number
											compPropVal_m.put(currentSecPropValues.propNameValuepair[columnNum].propName,((CCombo) (event.getSource())).getText());//adding to the Map
										}
									});
								}

								if(lovValues!=null)
								{
									String[] lovStrings = Arrays.copyOf(lovValues, lovValues.length, String[].class);								
									if(lovStrings!=null)
										if (isCheckOut == true)//setItems was taking 40-90 milliseconds, hence we will set the items only when in check out state
											combo.setItems( lovStrings);
									
									combo.add("", 0);

									if(PropValue!=null )
										combo.setText(PropValue);
								}
							}
							else if( ( formType.equals("U4_CanMEMDetailsForm") ||  formType.equals("U4_BodyMatDetailsForm") || formType.equals("U4_PackageDetailsForm") || (formType.equals("U4_MaterialDetailsForm"))) && propname.equals("u4_type") ) //TODO:: ADDED for CR# 131
							{
								CCombo  combo = new CCombo(tcTable,SWT.DROP_DOWN | SWT.READ_ONLY);
								combo.setData("colId", ColumnNumber);
								// TODO :: Defect#1290
								combo.setItems(new String[]{"","-"});
								// TODO :: Defect#1300
								if(PropValue != null && PropValue.length() > 0)
									combo.setText(PropValue);
								else 
									combo.setText("");

								editor.grabHorizontal = true;
								editor.setEditor(combo, item, ColumnNumber);
								editor.getEditor().setEnabled(enable_editor);
								if (isCheckOut==true)
								{
									combo.addSelectionListener(new SelectionAdapter() {
										public void widgetSelected(SelectionEvent event) {
											PAMSecondaryPropValue currentSecPropValues = tableConfigValues[tcTable.indexOf(item)] ;//getting the row index 
											compPropVal_m.put("puid",currentSecPropValues.selectedComponent);
											compPropVal_m.put("section",section);
											int columnNum = (Integer) ((CCombo)(event.getSource())).getData("colId");//getting the column number
											compPropVal_m.put(currentSecPropValues.propNameValuepair[columnNum].propName,((CCombo) (event.getSource())).getText());//adding to the Map
										}
									});
								}
							}	
							else
							{			
								text = new Text(tcTable, SWT.NONE);
								text.setTextLimit(textLength);
								if(PropValue!=null)
									text.setText(PropValue);
								editor.grabHorizontal = true;
								editor.setEditor(text,item, ColumnNumber);
								text.setData("colId", ColumnNumber);
								editor.getEditor().setEnabled(enable_editor);
								text.addModifyListener(new ModifyListener() {
									public void modifyText(ModifyEvent event) {
										PAMSecondaryPropValue currentSecPropValues = tableConfigValues[tcTable.indexOf(item)] ;
										compPropVal_m.put("puid",currentSecPropValues.selectedComponent);
										compPropVal_m.put("section",section);
										int columnNum = (Integer) ((Text)(event.getSource())).getData("colId");
										compPropVal_m.put(currentSecPropValues.propNameValuepair[columnNum].propName,((Text) (event.getSource())).getText());
									}
								});

							}
						}
						//TODO:: ADDED for CR# 131
						else if( ( formType.equals("U4_CanMEMDetailsForm") ||  formType.equals("U4_BodyMatDetailsForm") || formType.equals("U4_PackageDetailsForm") || (formType.equals("U4_MaterialDetailsForm"))) && propname.equals("u4_type") )
						{
							CCombo  combo2 = new CCombo(tcTable,SWT.DROP_DOWN | SWT.READ_ONLY);
							combo2.setData("colId", ColumnNumber);
							// TODO :: Defect#1290
							combo2.setItems(new String[]{"","-"});
							if(PropValue != null && PropValue.length() > 0)
								combo2.setText("-");
							else 
								combo2.setText("");
							editor.grabHorizontal = true;
							editor.setEditor(combo2,item,ColumnNumber);
							editor.getEditor().setEnabled(enable_editor);
							if (isCheckOut==true)
							{
								combo2.addSelectionListener(new SelectionAdapter() {
									public void widgetSelected(SelectionEvent event) {
										PAMSecondaryPropValue currentSecPropValues = tableConfigValues[tcTable.indexOf(item)] ;
										compPropVal_m.put("puid",currentSecPropValues.selectedComponent);
										compPropVal_m.put("section",section);
										int columnNum = (Integer) ((CCombo)(event.getSource())).getData("colId");
										compPropVal_m.put(currentSecPropValues.propNameValuepair[columnNum].propName,((CCombo) (event.getSource())).getText());

									}
								});
							}
						}	
						else
						{
							text = new Text(tcTable, SWT.NONE);
							text.setTextLimit(textLength);
							if(PropValue!=null)
								text.setText(PropValue);
							editor.grabHorizontal = true;
							editor.setEditor(text,item, ColumnNumber);
							text.setData("colId", ColumnNumber);
							editor.getEditor().setEnabled(enable_editor);
							text.addModifyListener(new ModifyListener() {
								public void modifyText(ModifyEvent event) { 
									PAMSecondaryPropValue currentSecPropValues = tableConfigValues[tcTable.indexOf(item)] ;
									compPropVal_m.put("puid",currentSecPropValues.selectedComponent);
									compPropVal_m.put("section",section);
									int columnNum = (Integer) ((Text)(event.getSource())).getData("colId");
									compPropVal_m.put(currentSecPropValues.propNameValuepair[columnNum].propName,((Text) (event.getSource())).getText());
								}
							});

						}
						break;

					default:
						text = new Text(tcTable, SWT.NONE);
						text.setText(PropValue);
						editor.grabHorizontal = true;
						editor.setEditor(text,item, ColumnNumber);
						text.setData("colId", ColumnNumber);
						editor.getEditor().setEnabled(enable_editor);
						text.addModifyListener(new ModifyListener() {
							public void modifyText(ModifyEvent event) {
								PAMSecondaryPropValue currentSecPropValues = tableConfigValues[tcTable.indexOf(item)] ;
								compPropVal_m.put("puid",currentSecPropValues.selectedComponent);
								compPropVal_m.put("section",section);
								int columnNum = (Integer) ((Text)(event.getSource())).getData("colId");
								compPropVal_m.put(currentSecPropValues.propNameValuepair[columnNum].propName,((Text) (event.getSource())).getText());
							}
						});
						break;
					}
				}

				modifiedPropVals_v.add(compPropVal_m);
			}
		});
	}

	@Override
	public Object getEditableValue() {
		// TODO Auto-generated method stub
		return tableComposite;
	}



	@Override
	public String getProperty() {
		// TODO Auto-generated method stub
		return "object_name";
	}


	@Override
	public boolean isPropertyModified(TCComponent arg0) throws Exception {
		// TODO Auto-generated method stub
		return true;
	}



	@Override
	public boolean isPropertyModified(TCProperty arg0) throws Exception {
		// TODO Auto-generated method stub
		return true;
	}




	@Override
	public void load(TCProperty arg0) throws Exception {
		// TODO Auto-generated method stub

		long startTime = System.currentTimeMillis();

		createTables();

		if(UnileverUtility.isPerfMonitorTriggered == true)
		{
			UnileverUtility.getPerformanceTime(startTime, "EPAMTableComposite");
		}		
	}



	@Override
	public void load(TCComponentType arg0) throws Exception {
		// TODO Auto-generated method stub

	}



	@Override
	public void load(TCPropertyDescriptor arg0) throws Exception {
		// TODO Auto-generated method stub

	}


	@Override
	public void setMandatory(boolean arg0) {
		// TODO Auto-generated method stub

	}



	@Override
	public void setModifiable(boolean arg0) {
		// TODO Auto-generated method stub

	}



	@Override
	public void setProperty(String arg0) {
	}



	@Override
	public void setUIFValue(Object arg0) {
		// TODO Auto-generated method stub

	}
	@Override
	public void setVisible(boolean arg0) {
		// TODO Auto-generated method stub
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
		for(int inx= 0;inx<modifiedPropVals_v.size();inx++)
		{
			Map<String, Object> propValueMap = modifiedPropVals_v.get(inx);
			TCComponent currComp = (TCComponent) ( propValueMap).get("puid");
			section = (String) propValueMap.get("section");

			if(currComp != null)
				currComp.lock();

			boolean tosave = true ;

			for(Map.Entry<String, Object> propName : propValueMap.entrySet()) 
			{
				try {
					if(!(propName.getKey().equalsIgnoreCase("puid")) && !(propName.getKey().equalsIgnoreCase("section")))
					{
						currComp.setProperty(propName.getKey(), (String) (propName.getValue()));
					}
				} catch (TCException e) {
					// TODO Auto-generated catch block

					tosave = false ;
					
					if (propName.getKey().compareTo("u4_build_date") == 0)
					{
						sbErrorMessage.append(section).append(" - ").append(currComp.toString()).append(" - ");
						sbErrorMessage.append(propName.getKey()).append(" : ");
						sbErrorMessage.append(e.getMessage().replace("double", "number")).append("\n");
						sbErrorMessage.append("Please enter Build Date in the format DD-Mon-YYYY HH:MM eg: 17-Jan-2018 15:05\n");
					}
					else
					{
						sbErrorMessage.append(section).append(" - ").append(currComp.toString()).append(" - ");
						sbErrorMessage.append(propName.getKey()).append(" : ");
						sbErrorMessage.append(e.getMessage().replace("double", "number")).append("\n");
					}
				}
			}

			if(tosave && currComp != null)
			{
				try {
					currComp.save();
					currComp.unlock();
					currComp.refresh();
				}
				catch (TCException e) {
					// TODO Auto-generated catch block

				}
			}
		}
		// modifiedPropVals_v.clear();
		return null;
			}
	@Override
	public void save(TCProperty paramTCProperty)
			throws Exception
			{

		try
		{
			TCProperty localTCProperty = getPropertyToSave(paramTCProperty);
			if ((this.savable) && (localTCProperty != null)) {
				localTCProperty.getTCComponent().setTCProperty(paramTCProperty);
			}
			setDirty(false);
		} catch (TCException e) {
			// TODO Auto-generated catch block
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
		// TODO Auto-generated method stub
		for(int inx= 0;inx<modifiedPropVals_v.size();inx++)
		{
			Map<String, Object> propValueMap = modifiedPropVals_v.get(inx);
			TCComponent currComp = (TCComponent) ( propValueMap).get("puid");
			section = (String) propValueMap.get("section");

			for(Map.Entry<String, Object> propName : propValueMap.entrySet()) 
			{
				try {
					if(!(propName.getKey().equalsIgnoreCase("puid")) && !(propName.getKey().equalsIgnoreCase("section")))
						currComp.setProperty(propName.getKey(), (String) (propName.getValue()));
				} catch (TCException e) {
					// TODO Auto-generated catch block

					sbErrorMessage.append(section).append(" - ").append(currComp.toString()).append(" - ");
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
			TCComponent[] relatedStructuredComponents = pamSpecRevision.getRelatedComponents(PAMConstant.STRUCTURED_RELATION);
			List<TCComponent> relatedStructuredComponentsList = Arrays.asList(relatedStructuredComponents); 
			if(relatedStructuredComponentsList.contains(inputComp))
			{
				return true;
			}
		} catch (TCException e) {
			// TODO Auto-generated catch block
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
						//break;
					}
				} catch (TCException e) {
					// TODO Auto-generated catch block
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


	/** 30 sep fix for the interdependent LOVs*/
	public boolean isInterdependantLov(String object_type , String prop_name)
	{
		//Included U4_SubstrateMatForm - <Jayateertha:30-Dec-2014>
		//Included U4_PalletForm - <Jayateertha:03-Mar-2015 - Fix for Defect# 668>

		if	( prop_name.equalsIgnoreCase( "u4_type" ) && 
				!(
						PAMConstant.APPROVED_ADD_DETAILS_FORM_TYPE_NAME.equals( object_type )||
						"U4_SubstrateMatForm".equals(object_type)||
						"U4_PalletForm".equals(object_type)||
						"U4_PMLTypePreseForm".equals(object_type)||
						PAMConstant.APPROVED_POLY_DETAILS_LOCAL_FORM_TYPE_NAME.equals(object_type)||
						PAMConstant.APPROVED_POLY_DETAILS_FORM_TYPE_NAME.equals(object_type) ||
						PAMConstant.MATERIAL_DETAILS_FORM_TYPE_NAME.equals(object_type)
						)
				)
			return true;
		else if(prop_name.equalsIgnoreCase( "u4_where_add" ))
			return true;
		else if ((object_type.compareToIgnoreCase(UL4Common.COMPLIANCERULE_FORM)==0)  &&  (prop_name.equalsIgnoreCase( "u4_justification" )==true) )
			return true;
		else
			return false;
	}


	Listener ctrlAListener = new Listener() {		 
		@Override
		public void handleEvent(Event event) {
			// TODO Auto-generated method stub
			if ( event.stateMask == SWT.CTRL && event.keyCode == 'a' ) {
				((Text)event.widget).selectAll();
			}

		}
	};

	Listener ctrlCListener = new Listener() {		 
		@Override
		public void handleEvent(Event event) {
			// TODO Auto-generated method stub
			if ( event.stateMask == SWT.CTRL && event.keyCode == 'c' ) {
				((Text)event.widget).copy();
			}

		}
	};

	Listener ctrlVListener = new Listener() {		 
		@Override
		public void handleEvent(Event event) {
			// TODO Auto-generated method stub
			if ( event.stateMask == SWT.CTRL && event.keyCode == 'v' ) {
				((Text)event.widget).paste();
			}

		}
	};

	boolean check_mir_phase_roll_out ()
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

				project_ids = pamSpecRevision.getStringProperty(UL4Common.TCPROJECTIDS);

				if( project_ids == null || (  (project_ids.length() <= 1)  && project_ids.equalsIgnoreCase(" ")))
				{
					pamSpecRevision.refresh();
					project_ids   = pamSpecRevision.getStringProperty(UL4Common.TCPROJECTIDS);
				}

				if(project_ids != null &&   (  project_ids.length() >= 1  &&   ! project_ids.equalsIgnoreCase(" ")  ))
					component_projects =  project_ids.split(",");

				if ( (component_projects != null) && (phaserollout_projects != null))
				{
					for (int index = 0 ; index < phaserollout_projects.length ; index++)
					{
						for (int cinx = 0 ; cinx < component_projects.length ; cinx++)
						{
							if (component_projects[cinx].compareTo(phaserollout_projects[index]) == 0)
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

				project_ids = pamSpecRevision.getStringProperty(UL4Common.TCPROJECTIDS);

				if( project_ids == null || (  (project_ids.length() <= 1)  && project_ids.equalsIgnoreCase(" ")))
				{
					pamSpecRevision.refresh();
					project_ids   = pamSpecRevision.getStringProperty(UL4Common.TCPROJECTIDS);
				}

				if(project_ids != null &&   (  project_ids.length() >= 1  &&   ! project_ids.equalsIgnoreCase(" ")))
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

		return eligible_component;
	}

}



