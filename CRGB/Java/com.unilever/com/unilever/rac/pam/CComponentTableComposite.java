package com.unilever.rac.pam;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
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
import com.teamcenter.rac.kernel.TCComponentType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.kernel.TCPropertyDescriptor;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.viewer.stylesheet.beans.AbstractPropertyBean;
import com.teamcenter.services.rac.core.DataManagementService;
import com.unilever.rac.ui.common.UL4Common;
import com.unilever.rac.util.UnileverUtility;

/**
 * @author swetha.manchiraju
 *
 */
public class CComponentTableComposite extends AbstractPropertyBean{

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

	/**
	 * 
	 */
	private String propname = null;

	/**
	 * 
	 */
	private Vector<Map<String,Object>> modifiedPropVals_v = null;

	/**
	 * 
	 */
	private String renderedPropName =null;

	private String componentType = null;

	/**
	 * 
	 */
	private TCSession session;

	/**
	 * 
	 */
	private Vector<Vector <PAMSecondaryPropValue>> PAMPropertyNameValue_v;


	private String pamSpecType = null;

	private FormToolkit parentFormToolKit;

	private int cpTableHeight= 100;

	private int cpTableWidth = 700;

	public static StringBuffer sbErrorMessage = null ;
	public static String section = "" ;
	
	private boolean isCheckOut = false ;
	private boolean enableControl = false;

	/**
	 * @param toolkit
	 * @param composite
	 * @param paramBoolean
	 * @param PropName
	 */
	public CComponentTableComposite(FormToolkit toolkit, Composite composite,boolean paramBoolean,Map<String, String> PropName)
	{
		super(composite);

		AbstractAIFUIApplication application = AIFUtility.getCurrentApplication();

		InterfaceAIFComponent[] targets = null;

		targets = application.getTargetComponents();

		if (targets[0] instanceof TCComponentItemRevision)

		{
			pamSpecRevision = (TCComponentItemRevision) targets[0];
			pamSpecType = pamSpecRevision.getType();
		}
		
		isCheckOut = pamSpecRevision.isCheckedOut(); 
		session = pamSpecRevision.getSession();
		
		TCComponent checkedOutUser = null;
		if(isCheckOut)
		{
			try {
				checkedOutUser = pamSpecRevision.getReferenceProperty("checked_out_user");

				if(session.getUser().equals(checkedOutUser))
					enableControl=true;

			} catch (TCException e) {
				e.printStackTrace();
			}
		}

		this.parentComposite = composite;
		this.renderedPropName = (String) PropName.get("name");
		this.modifiedPropVals_v = new Vector<Map<String,Object>>();
		this.parentFormToolKit = toolkit;
		this.PAMPropertyNameValue_v = new Vector<Vector<PAMSecondaryPropValue>>();

		parentComposite.setLayout(new GridLayout(1,true));
		parentComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		

		if(session != null)
		    UnileverUtility.getPerformanceMonitorPrefValue( session);

		try
		{
			TCComponentType tccompttype = session.getTypeService().getTypeComponent (renderedPropName);
			section = tccompttype.getDisplayTypeName().replace("Relation", "");
		}
		catch (TCException e) 
		{
			section  = renderedPropName;
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

			tableConfigs = loadConfig.readPreferenceValues1("U4_CompPropertyRelation"+PAMConstant.COMPONENT_CONFIGURATIONS, session);

			AIFComponentContext[] relatedComponents = pamSpecRevision.getSecondary();

			PAMPropertyNameValue_v.clear();

			ArrayList<TCComponent> formarray = new ArrayList<TCComponent>();

			for(int inx=0;inx<tableConfigs.size();inx++)
			{
				if((tableConfigs.get(inx).primaryType).contains(pamSpecType)||(tableConfigs.get(inx).primaryType).equalsIgnoreCase("All"))
				{
					for(int jnx=0; (jnx<relatedComponents.length);jnx++)
					{
						if((relatedComponents[jnx].getComponent() instanceof TCComponentForm))
						{
							TCComponentForm tempForm = (TCComponentForm)(relatedComponents[jnx].getComponent());
							if((tempForm.getType()).equalsIgnoreCase(tableConfigs.get(inx).secondaryType))
							{
								formarray.add((TCComponent)tempForm);
							}
						}
					}
				}
			}

			if (isCheckOut == true)
			{
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
						UnileverUtility.getPerformanceTime(sstartTime, "dmService.refreshObjects in CComponentTableComposite " + formarray.size() );
				}
			}

			for(int inx=0;inx<tableConfigs.size();inx++)
			{
				Vector<PAMSecondaryPropValue> secondaryPropValue =new Vector<PAMSecondaryPropValue>();

				/**
				 *  Fix for CR# 76 - <Jayateertha M Inamdar - modified on 14-May-2015>
				 */
				Vector<TCComponent >sortedForms = new Vector<TCComponent>(formarray);
				Collections.copy(sortedForms, formarray);

				sortedForms = new Vector<TCComponent>(new LinkedHashSet<TCComponent>(sortedForms));

				sortedForms = sortForms(sortedForms,(tableConfigs.get(inx)).rowConfigurations.propertyName,((tableConfigs.get(inx)).rowConfigurations.propertyValues).toArray(new String[(tableConfigs.get(inx)).rowConfigurations.propertyValues.size()]));


				if((tableConfigs.get(inx).primaryType).contains(pamSpecType)||(tableConfigs.get(inx).primaryType).equalsIgnoreCase("All"))
				{
				for(int jnx=0; (jnx<sortedForms.size());jnx++)
				{
					TCComponentForm tempForm = (TCComponentForm)sortedForms.get(jnx);
					if((tempForm.getType()).equalsIgnoreCase(tableConfigs.get(inx).secondaryType))
						{
							//getting the values for the required Columns
							PAMPropertyNameValue[] propValueArray = new PAMPropertyNameValue[(tableConfigs.get(inx)).ColumnConfigurations.length];
							String [] formproperties = new String [(tableConfigs.get(inx)).ColumnConfigurations.length];

							for(int knx=0;knx<(tableConfigs.get(inx)).ColumnConfigurations.length;knx++)
								formproperties[knx]=(tableConfigs.get(inx)).ColumnConfigurations[knx].columnName;

							TCProperty []currProperty = tempForm.getTCProperties(formproperties);

							for(int knx=0;knx<(tableConfigs.get(inx)).ColumnConfigurations.length;knx++)
							{
								propValueArray[knx] = new PAMPropertyNameValue();
								propValueArray[knx].propName= (tableConfigs.get(inx)).ColumnConfigurations[knx].columnName;
								propValueArray[knx].tcProperty = currProperty[knx];
								if(currProperty == null)
									System.out.println("the property for which its failing-"+propValueArray[knx].propName);
								propValueArray[knx].propDisplayName = currProperty[knx].getPropertyDisplayName();
		  					    propValueArray[knx].propValue= currProperty[knx].getDisplayValue();
		  					    propValueArray[knx].columnSize = (tableConfigs.get(inx).ColumnConfigurations)[knx].columnSize;
		  					    propValueArray[knx].isEnabled = (tableConfigs.get(inx).ColumnConfigurations)[knx].isEnabled;
		  					    
							}
							//getting Values required for each row
							PAMSecondaryPropValue tempSecondaryPropValue = new PAMSecondaryPropValue();

							tempSecondaryPropValue.propNameValuepair = propValueArray;
							tempSecondaryPropValue.secondaryName = tempForm.getType();
							tempSecondaryPropValue.selectedComponent = tempForm;
							secondaryPropValue.add(tempSecondaryPropValue);
						}

				}
				if(secondaryPropValue.size()>0)
					PAMPropertyNameValue_v.add(secondaryPropValue);
				}

			}
			tableComposite = new Composite(parentComposite, SWT.NONE);
			GridData localGridData = new GridData(1808);
			localGridData.heightHint = (PAMPropertyNameValue_v.size())*cpTableHeight+100;
		    localGridData.widthHint = cpTableWidth;
		    tableComposite.setLayout(new GridLayout(1,true));
		    tableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		 
		    parentFormToolKit.adapt(tableComposite, true, true);

		} catch (Exception e) {

			e.printStackTrace();
		}
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
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		return sortedForms;
	}


	/**
	 * 
	 */
	public void createTables ()
	{
		long startTime = System.currentTimeMillis();
		
		loadConfigurations();
		
		if(UnileverUtility.isPerfMonitorTriggered == true)
			UnileverUtility.getPerformanceTime(startTime, "loadConfigurations in CComponentTableComposite");
		
		//tableComposite = new Composite(parentComposite.getParent(),SWT.NONE);
		Table[] propertyTables = new Table[PAMPropertyNameValue_v.size()];
		for(int inx=0;inx<PAMPropertyNameValue_v.size();inx++)
		{
			startTime = System.currentTimeMillis();
			
			int style = SWT.VIRTUAL | SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.HIDE_SELECTION;
			Label propertyLabel = new Label(tableComposite,SWT.NONE);
			String tableType = (PAMPropertyNameValue_v.get(inx)).get(0).selectedComponent.getDisplayType();
			section = tableType;
			propertyLabel.setText(tableType+":");
			propertyLabel.pack();
			propertyTables[inx] = new Table(tableComposite,style );
			propertyTables[inx].setRedraw(false);
		 
		    propertyTables[inx].setHeaderVisible(true);
		    propertyTables[inx].setLinesVisible(true);
		    GridData localGridData = new GridData(1808);
			localGridData.heightHint = cpTableHeight;
		    localGridData.widthHint = cpTableWidth;
		    propertyTables[inx].setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		    propertyTables[inx].setHeaderVisible(true);
		    propertyTables[inx].setLinesVisible(true);
		    Display display =  propertyTables[inx].getDisplay();
		    if(isCheckOut)
			{
				if(enableControl)
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
			
			propertyTables[inx].setRedraw(true);
			
			if(UnileverUtility.isPerfMonitorTriggered == true)
				UnileverUtility.getPerformanceTime(startTime, "createTables " + tableType );
		}

	}

	public boolean isTableEditable()
	{
		//Get u4_pack_comp_uses_template property value from Pack Component Rev
		boolean isPackCompUsingTemplate = false;
		try {
			isPackCompUsingTemplate = pamSpecRevision.getLogicalProperty(PAMConstant.PCUSESTEMPLATEATTR);
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//Check whether it is a stock Component
		if( isPackCompUsingTemplate == false)
		{
			return true;
		}
		else
		{
			TCPreferenceService prefServ = session.getPreferenceService();

			String[] prefValues = prefServ.getStringValues(PAMConstant.COMPONENTSPREF_3D);
			List<String> prefList = Arrays.asList(prefValues); 
			if(prefList.contains(pamSpecType))
			{
				return false;
			}
			else
			{
				return true;
			}
		}
	}


	/**
	 * @param tcTable
	 * @param tableConfigValues
	 */
	public void setTableConfiguration(final Table tcTable, final PAMSecondaryPropValue[] tableConfigValues)
	{
		//U4_PAM2DTypes	 
		boolean canedit = false;
		if (isCheckOut == true)
			canedit = isTableEditable();
		
		final boolean isTableEditabe = canedit;

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
		
		tcTable.addListener(SWT.SetData, new Listener() {
		public void handleEvent(Event event) {
			
			final TableItem item = (TableItem) event.item;
			int row = tcTable.indexOf(item);
			boolean legend = PAMConstant.ruleValidate((TCComponentForm) tableConfigValues[row].selectedComponent);
			TCComponentForm currForm = (TCComponentForm) tableConfigValues[row].selectedComponent;
			final String formType = tableConfigValues[row].secondaryName;
			final Map<String,Object> compPropVal_m = new HashMap<String,Object>();
			
			boolean isWeightProperty = false;

			if (isCheckOut == true)
			{
				if ((formType!=null) && (formType.compareToIgnoreCase(UL4Common.COMPONENTPROPERTY_FORM) == 0))
				{
					try {
						TCProperty property = currForm.getTCProperty(UL4Common.PROPERTY_ATTRIBUTE_NAME);

						if (property!=null)
						{
							String property_value = property.getStringValue();

							if (property_value!=null)
							{
								if (	(property_value.compareTo("Weight") ==0) ||
										(property_value.compareTo("Weight/Unit") ==0) ||
										(property_value.compareTo("Weight_Label") ==0)
										)
								{
									isWeightProperty=true;
								}
							}
						}
					} catch (TCException e) {
						e.printStackTrace();
					}
				}
			}
			
			for(  int ColumnNumber =0;ColumnNumber< tableConfigValues[row].propNameValuepair.length;ColumnNumber++ )
			{
				String propname = tableConfigValues[row].propNameValuepair[ColumnNumber].propName;

				boolean override_isTableEditabe =false;

				if (isCheckOut == true)
				{
					if ( (isWeightProperty==true) &&
							((propname.compareToIgnoreCase(UL4Common.TARGET_ATTRIBUTE_NAME) == 0) ||
									(propname.compareToIgnoreCase(UL4Common.MIN_ATTRIBUTE_NAME) == 0) ||
									(propname.compareToIgnoreCase(UL4Common.MAX_ATTRIBUTE_NAME) == 0) )
							)
					{
						if (isTableEditabe==false)
							override_isTableEditabe=true;
					}
				}

				final TableEditor editor = new TableEditor(tcTable);

				Text text = new Text(tcTable, SWT.NONE);
				TCProperty currTCProp =  tableConfigValues[row].propNameValuepair[ColumnNumber].tcProperty;

				String PropValue = (String)  tableConfigValues[row].propNameValuepair[ColumnNumber].propValue;
				int propType = currTCProp.getPropertyType();

				if(ColumnNumber == 0 && legend )
				{   
					PAMConstant.setMandatory(PropValue,editor,item);
				}

				if( propType!=6 && ( !isTableEditabe || !(tableConfigValues[row].propNameValuepair[ColumnNumber].isEnabled)))
				{
					if (override_isTableEditabe==false)
					{
						item.setText(ColumnNumber,PropValue);
						continue;
					}
				}
				
				switch (propType)
				{
					case 1:
						 text = new Text(tcTable, SWT.NONE);
						 if(PropValue!=null )
							 text.setText(PropValue);
					      editor.grabHorizontal = true;
					      editor.setEditor(text,item, ColumnNumber);
					      editor.getEditor().setEnabled(enableControl);
					      text.setData("colId", ColumnNumber);
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
					case 2:
						 text = new Text(tcTable, SWT.NONE);
						 if(PropValue!=null )
							 text.setText(PropValue);
					     editor.grabHorizontal = true;
					      editor.setEditor(text,item, ColumnNumber);
					      editor.getEditor().setEnabled(enableControl);
					      text.setData("colId", ColumnNumber);
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
					case 3:
						 text = new Text(tcTable, SWT.NONE);
						 if(PropValue!=null )
							 text.setText(PropValue);
					     editor.grabHorizontal = true;
					      editor.setEditor(text,item, ColumnNumber);
					      text.setData("colId", ColumnNumber);
					      text.setData("section", section);
					      editor.getEditor().setEnabled(enableControl);
					      
					      if (isCheckOut == true)
					      {
					    	  text.addModifyListener(new ModifyListener() {
					    		  public void modifyText(ModifyEvent event) {
					    			  PAMSecondaryPropValue currentSecPropValues = tableConfigValues[tcTable.indexOf(item)] ;
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
					case 4:
						 text = new Text(tcTable, SWT.NONE);
						 if(PropValue!=null )
							 text.setText(PropValue);
					     editor.grabHorizontal = true;
					      editor.setEditor(text,item, ColumnNumber);
					      editor.getEditor().setEnabled(enableControl);
					      text.setData("colId", ColumnNumber);
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
					case 5:
						 text = new Text(tcTable, SWT.NONE);
						 if(PropValue!=null )
							 text.setText(PropValue);
					     editor.grabHorizontal = true;
					      editor.setEditor(text,item, ColumnNumber);
					      editor.getEditor().setEnabled(enableControl);
					      text.setData("colId", ColumnNumber);
					      if (isCheckOut == true)
					      {
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

					case 6:
						Button checkButton = new Button(tcTable, SWT.CHECK);

						checkButton.setData("colId", ColumnNumber);
						if(PropValue.equalsIgnoreCase("True"))
							checkButton.setSelection(true);
						editor.grabHorizontal = true;
						editor.setEditor(checkButton,item,ColumnNumber);
						editor.getEditor().setEnabled(enableControl);
						CheckboxCellEditor checkBoxEditor = new CheckboxCellEditor(tcTable);
						if(PropValue.equalsIgnoreCase("True"))
							checkBoxEditor.setValue(true);
						if(!(tableConfigValues[row].propNameValuepair[ColumnNumber].isEnabled)|| !isTableEditabe)
							checkButton.setEnabled(false);
						else
							checkButton.setEnabled(true);

						if (isCheckOut ==true)
						{
							checkButton.addSelectionListener(new SelectionAdapter() {
								public void widgetSelected(SelectionEvent event) {
									PAMSecondaryPropValue currentSecPropValues = tableConfigValues[tcTable.indexOf(item)] ;
									compPropVal_m.put("puid",currentSecPropValues.selectedComponent);
									compPropVal_m.put("section",section);
									int columnNum = (Integer) ((Text)(event.getSource())).getData("colId");
									compPropVal_m.put(currentSecPropValues.propNameValuepair[columnNum].propName,((Text) (event.getSource())).getText());
								}
							});
						}
						break;
					case 7:
						break;
					case 8:

						int textLength = textLimit.get(propname).intValue();

						TCComponentListOfValues currLOVValues = null;
						if(( currLOVValues = currTCProp.getLOV() )!= null)
						{
							ListOfValuesInfo tempLOVInfo;

							Object[] lovValues = null;

							try {
								tempLOVInfo = currLOVValues.getListOfValues();
								 lovValues =  tempLOVInfo.getListOfValues();
							} catch (TCException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							CCombo  combo = new CCombo(tcTable,SWT.NONE);
							combo.setData("colId", ColumnNumber);
							String[] lovStrings = Arrays.copyOf(lovValues, lovValues.length, String[].class);
							combo.setItems( lovStrings);
							if(PropValue!=null )
								combo.setText(PropValue);

							editor.grabHorizontal = true;
							editor.setEditor(combo, item, ColumnNumber);

							editor.getEditor().setEnabled(enableControl);

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
						else
						{
							 text = new Text(tcTable, SWT.NONE);
							 text.setTextLimit(textLength);
							 if(PropValue!=null)
								 text.setText(PropValue);
						     editor.grabHorizontal = true;
						     editor.setEditor(text,item, ColumnNumber);
						     text.setData("colId", ColumnNumber);
						     editor.getEditor().setEnabled(enableControl);
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
						      editor.getEditor().setEnabled(enableControl);
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

		tcTable.setRedraw(true);
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
			UnileverUtility.getPerformanceTime(startTime, "CComponentTableComposite");
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

					for(Map.Entry<String, Object> propName : propValueMap.entrySet()) 
					{
						try {
							if(!(propName.getKey().equalsIgnoreCase("puid")) && !(propName.getKey().equalsIgnoreCase("section")))
							{
								currComp.lock();

									currComp.setProperty(propName.getKey(), (String) (propName.getValue()));
							currComp.save();
							currComp.unlock();
							currComp.refresh();
							}
						} catch (TCException e) {
							// TODO Auto-generated catch block
							sbErrorMessage.append(section).append(" - ").append(currComp.toString()).append(" - ");
							sbErrorMessage.append(propName.getKey()).append(" : ");
							sbErrorMessage.append(e.getMessage().replace("double", "number")).append("\n");
						}
					}

				}
			    //modifiedPropVals_v.clear();
			    return null;
			  }
	
	@Override
	public void save(TCProperty paramTCProperty) throws Exception
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
	public TCProperty saveProperty(TCComponent paramTCComponent)throws Exception
	{
		sbErrorMessage = new StringBuffer();

		TCProperty localTCProperty = getPropertyToSave(paramTCComponent);
		if (this.savable) {
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
	public TCProperty getPropertyToSave(TCProperty arg0) throws Exception {
		// TODO Auto-generated method stub
		System.out.println(" coming to get Property to Save");
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
}
