package com.unilever.rac.pam;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
import org.eclipse.swt.widgets.Label;
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
import com.unilever.rac.util.UnileverUtility;

/**
 * @author swetha.manchiraju
 *
 */
public class CLayerStructureTableComposite extends AbstractPropertyBean{	
	
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
	
	private int ePAMTableHeight= 100;
	
	private int ePAmTableWidth = 700;
	
	public static StringBuffer sbErrorMessage = null ;
	public static String section = "" ;	
	
	/**
	 * @param toolkit
	 * @param composite
	 * @param paramBoolean
	 * @param PropName
	 */
	public CLayerStructureTableComposite(FormToolkit toolkit, Composite composite,boolean paramBoolean,Map<String, String> PropName)
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

		this.parentComposite = composite;	
		this.parentFormToolKit = toolkit;
		this.renderedPropName = (String) PropName.get("name");
		this.modifiedPropVals_v = new Vector<Map<String,Object>>();
		this.PAMPropertyNameValue_v = new Vector<Vector<PAMSecondaryPropValue>>();
		
		parentComposite.setLayout(new GridLayout(1,true));
		parentComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		session = pamSpecRevision.getSession();
		
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
			System.out.println("The time at the start of loadConfigurations()"+System.currentTimeMillis());
			
			LoadPAMConfigurations loadConfig = new LoadPAMConfigurations();	
			
			Vector<PAMTableConfiguration> tableConfigs = new Vector<PAMTableConfiguration>();			
			
			tableConfigs = loadConfig.readPreferenceValues1("U4_PMLLayerStrctRelation"+PAMConstant.COMPONENT_CONFIGURATIONS, session);			
					
			AIFComponentContext[] relatedComponents = pamSpecRevision.getSecondary();	
							
			ArrayList<TCComponent> formarray = new ArrayList<TCComponent>();
			
			//762 - usha.
			Vector<TCComponent>relatedForms = new Vector<TCComponent>(); ;
			for(int znx=0; znx<relatedComponents.length;znx++)
				if(((TCComponent) relatedComponents[znx].getComponent()).getType().equalsIgnoreCase("U4_PMLLayerStrctForm") || ((TCComponent) relatedComponents[znx].getComponent()).getType().equalsIgnoreCase("U4_PNBLayerStrctForm"))
					relatedForms.addElement((TCComponent) relatedComponents[znx].getComponent()); 
			
			for(int inx=0;inx<tableConfigs.size();inx++)
			{	
				if((tableConfigs.get(inx).primaryType).contains(pamSpecType)||(tableConfigs.get(inx).primaryType).equalsIgnoreCase("All"))
				{	
					//this then sorts the obtained forms based on the row configuration mentioned in the preference
					if(!((tableConfigs.get(inx)).rowConfigurations.propertyName.equalsIgnoreCase("")))
						
						relatedForms = sortForms(relatedForms,(tableConfigs.get(inx)).rowConfigurations.propertyName,((tableConfigs.get(inx)).rowConfigurations.propertyValues).toArray(new String[(tableConfigs.get(inx)).rowConfigurations.propertyValues.size()]));
	
					for(int jnx=0; (jnx<relatedForms.size());jnx++)
					{
						if((relatedForms.get(jnx) instanceof TCComponentForm))
						{
							TCComponentForm tempForm = (TCComponentForm)(relatedForms.get(jnx));
							if((tempForm.getType()).equalsIgnoreCase(tableConfigs.get(inx).secondaryType))
							{
								formarray.add((TCComponent)tempForm);
							}
						}
					}
				}
			}

			DataManagementService dmService = DataManagementService.getService(session);
			dmService.refreshObjects(formarray.toArray(new TCComponent[formarray.size()]));
							
			PAMPropertyNameValue_v.clear();
			
			
			for(int inx=0;inx<tableConfigs.size();inx++)
			{	
				Vector<PAMSecondaryPropValue> secondaryPropValue =new Vector<PAMSecondaryPropValue>();
				//PAMSecondaryPropValue[] secondaryPropValue = new PAMSecondaryPropValue[relatedComponents.length];
				if((tableConfigs.get(inx).primaryType).contains(pamSpecType)||(tableConfigs.get(inx).primaryType).equalsIgnoreCase("All"))
				{
				for(int jnx=0; (jnx<relatedForms.size());jnx++)
				{
					//762 - usha.
					//this then sorts the obtained forms based on the row configuration mentioned in the preference
					if(!((tableConfigs.get(inx)).rowConfigurations.propertyName.equalsIgnoreCase("")))	
						relatedForms = sortForms(relatedForms,(tableConfigs.get(inx)).rowConfigurations.propertyName,((tableConfigs.get(inx)).rowConfigurations.propertyValues).toArray(new String[(tableConfigs.get(inx)).rowConfigurations.propertyValues.size()]));
	
					if((relatedForms.get(jnx) instanceof TCComponentForm))
					{
//						TCComponentForm tempForm = (TCComponentForm)(relatedComponents[jnx].getComponent());
//						if((tempForm.getType()).equalsIgnoreCase(tableConfigs.get(inx).secondaryType))
//						{
						TCComponentForm tempForm = (TCComponentForm)(relatedForms.get(jnx));
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
		  					    propValueArray[knx].PropertyType =  currProperty[knx].getPropertyType();
		  					    
		  					    
		  					    
							}
							//getting Values required for each row
							PAMSecondaryPropValue tempSecondaryPropValue = new PAMSecondaryPropValue();
							
							tempSecondaryPropValue.propNameValuepair = propValueArray;
							tempSecondaryPropValue.secondaryName = tempForm.getType();	
							tempSecondaryPropValue.selectedComponent = tempForm;
							secondaryPropValue.add(tempSecondaryPropValue);							
						}					
				   }
				
				}
				if(secondaryPropValue.size()>0)
					PAMPropertyNameValue_v.add(secondaryPropValue);
				}
				
			}
			tableComposite = new Composite(parentComposite, SWT.NONE);
			GridData localGridData = new GridData(1808);		
			localGridData.heightHint = (PAMPropertyNameValue_v.size())*ePAMTableHeight+100;
		    localGridData.widthHint = ePAmTableWidth;
		    tableComposite.setLayout(new GridLayout(1,true));
		    tableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		   // tableComposite.setLayoutData(parentComposite.getLayoutData());
		    parentFormToolKit.adapt(tableComposite, true, true);
						
		} 
		catch (Exception e) {
			
			e.printStackTrace();
		}		System.out.println("The time at the end of loadConfigurations()"+System.currentTimeMillis());
		
	}
	
	
	/**
	 * 
	 */
	public void createTables ()
	{
		loadConfigurations();
		//tableComposite = new Composite(parentComposite.getParent(),SWT.NONE);
		Table[] propertyTables = new Table[PAMPropertyNameValue_v.size()];
		for(int inx=0;inx<PAMPropertyNameValue_v.size();inx++)
		{
			
			int style = SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | 
					SWT.FULL_SELECTION | SWT.HIDE_SELECTION;
			Label propertyLabel = new Label(tableComposite,SWT.NONE);
			String tableType = (PAMPropertyNameValue_v.get(inx)).get(0).selectedComponent.getDisplayType();
			propertyLabel.setText(tableType+":");
			propertyLabel.pack();
			propertyTables[inx] = new Table(tableComposite,style );
			GridData localGridData = new GridData(1808);		
			localGridData.heightHint = ePAMTableHeight;
		    localGridData.widthHint = ePAmTableWidth;
		    //propertyTables[inx].setLayoutData(localGridData);			
		    		    
		    propertyTables[inx].setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
			
		    propertyTables[inx].setHeaderVisible(true);
		    propertyTables[inx].setLinesVisible(true);		    
		   
		    Display display =  propertyTables[inx].getDisplay();
		    if(pamSpecRevision.isCheckedOut())
			{
		    	TCComponent checkedOutUser = null;
				
			    try {
					checkedOutUser = pamSpecRevision.getReferenceProperty("checked_out_user");
				} catch (TCException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			    
				if(session.getUser().equals(checkedOutUser))
				{
					Color white = display.getSystemColor(SWT.COLOR_WHITE);
				   // propertyTables[inx].setEnabled(true);
					propertyTables[inx].setBackground(white);
				}
				else
				{
					//propertyTables[inx].setEnabled(false);	
					Color lightGray = new Color (display, 240, 240, 240);				
					propertyTables[inx].setBackground(lightGray);
				}
			}
			else
			{
				//propertyTables[inx].setEnabled(false);	
				Color lightGray = new Color (display, 240, 240, 240);				
				propertyTables[inx].setBackground(lightGray);
			}
						
			setTableConfiguration(propertyTables[inx],PAMPropertyNameValue_v.get(inx).toArray(new PAMSecondaryPropValue[PAMPropertyNameValue_v.get(inx).size()]));
			
			
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
		
		boolean isTableEditabe = isTableEditable();
		//final PAMSecondaryPropValue[] tempRowConfigArray = new PAMSecondaryPropValue[tableConfigValues.length];
		 
		Map<String,Integer > textLimit = new HashMap<String,Integer>();		
		    if(tableConfigValues.length > 0)	
		    	textLimit = PAMConstant.getTextLimit (tableConfigValues[0]);
		
		for(int inx=0;inx<(tableConfigValues).length;inx++)
		{
			final TableItem item = new TableItem(tcTable, SWT.NONE);		
			
			final Map<String,Object> compPropVal_m = new HashMap<String,Object>();	
			
			boolean legend = PAMConstant.ruleValidate((TCComponentForm) tableConfigValues[inx].selectedComponent);
			
			for(  int ColumnNumber =0;ColumnNumber< tableConfigValues[inx].propNameValuepair.length;ColumnNumber++ )
			{
				propname = tableConfigValues[inx].propNameValuepair[ColumnNumber].propName;
				if(inx == 0)
				{									
					final TableColumn propertyColumn =  new TableColumn(tcTable, SWT.BORDER_SOLID|SWT.COLOR_WIDGET_NORMAL_SHADOW);
					propertyColumn.setWidth(tableConfigValues[inx].propNameValuepair[ColumnNumber].columnSize);					
					
					String nametext = tableConfigValues[inx].propNameValuepair[ColumnNumber].propDisplayName;			
					propertyColumn.setText(nametext);	
					
					propertyColumn.setResizable(true);
				}
				
				final TableEditor editor = new TableEditor(tcTable);	
							
				Text text = new Text(tcTable, SWT.NONE);				
				TCProperty currTCProp =  tableConfigValues[inx].propNameValuepair[ColumnNumber].tcProperty;					
					
				String PropValue = (String)  tableConfigValues[inx].propNameValuepair[ColumnNumber].propValue;
				
				//if(propname.equalsIgnoreCase("u4_property")||propname.equalsIgnoreCase("u4_attribute"))tableConfigValues[inx].propNameValuepair[ColumnNumber].propDisplayName
				int propType = currTCProp.getPropertyType();
				
				if(ColumnNumber == 0 && legend )
				{   
				    PAMConstant.setMandatory(PropValue,editor,item);		     	  
				}
				
				if( propType!=6 && ( !isTableEditabe || !(tableConfigValues[inx].propNameValuepair[ColumnNumber].isEnabled)))
				{
					item.setText(ColumnNumber,PropValue);
					continue;				

				}	
							
				
				switch (propType)
				{
					case 1:
						 text = new Text(tcTable, SWT.NONE);
						 if(PropValue!=null )
							 text.setText(PropValue);
					      editor.grabHorizontal = true;
					      editor.setEditor(text,item, ColumnNumber);
					      text.setData("colId", ColumnNumber);
					      
					      enableORDisableConrol(editor.getEditor());
					      text.addModifyListener(new ModifyListener() {     
								public void modifyText(ModifyEvent event) {         
									//getting the row index 
									//PAMSecondaryPropValue currentSecPropValues = tempRowConfigArray[tcTable.indexOf(item)] ;	
									PAMSecondaryPropValue currentSecPropValues = tableConfigValues[tcTable.indexOf(item)];
									compPropVal_m.put("puid",currentSecPropValues.selectedComponent);
									compPropVal_m.put("section",section);	
							        //getting the column number
									int columnNum = (Integer) ((Text)(event.getSource())).getData("colId");
									
									//adding to the Map
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
					      text.setData("colId", ColumnNumber);
					      enableORDisableConrol(editor.getEditor());
					      text.addModifyListener(new ModifyListener() {     
								public void modifyText(ModifyEvent event) {         
									//getting the row index 
									PAMSecondaryPropValue currentSecPropValues = tableConfigValues[tcTable.indexOf(item)] ;									
									compPropVal_m.put("puid",currentSecPropValues.selectedComponent);
									compPropVal_m.put("section",section);
							        //getting the column number
									int columnNum = (Integer) ((Text)(event.getSource())).getData("colId");
									
									//adding to the Map
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
					      enableORDisableConrol(editor.getEditor());
					      text.addModifyListener(new ModifyListener() {     
								public void modifyText(ModifyEvent event) {         
									//getting the row index 
									PAMSecondaryPropValue currentSecPropValues = tableConfigValues[tcTable.indexOf(item)] ;									
									compPropVal_m.put("puid",currentSecPropValues.selectedComponent);
									compPropVal_m.put("section",section);		
							        //getting the column number
									int columnNum = (Integer) ((Text)(event.getSource())).getData("colId");
									
									//adding to the Map
									compPropVal_m.put(currentSecPropValues.propNameValuepair[columnNum].propName,((Text) (event.getSource())).getText());
										
									}      
								});	
					     					
						break;
					case 4:
						 text = new Text(tcTable, SWT.NONE);
						 if(PropValue!=null )
							 text.setText(PropValue);
					     editor.grabHorizontal = true;
					      editor.setEditor(text,item, ColumnNumber);
					      text.setData("colId", ColumnNumber);
					      enableORDisableConrol(editor.getEditor());
					      text.addModifyListener(new ModifyListener() {     
								public void modifyText(ModifyEvent event) {         
									//getting the row index 
									PAMSecondaryPropValue currentSecPropValues = tableConfigValues[tcTable.indexOf(item)] ;									
									compPropVal_m.put("puid",currentSecPropValues.selectedComponent);
									compPropVal_m.put("section",section);	
							        //getting the column number
									int columnNum = (Integer) ((Text)(event.getSource())).getData("colId");
									
									//adding to the Map
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
					      enableORDisableConrol(editor.getEditor());
					      text.setData("colId", ColumnNumber);
					      text.addModifyListener(new ModifyListener() {     
								public void modifyText(ModifyEvent event) {         
									//getting the row index 
									PAMSecondaryPropValue currentSecPropValues = tableConfigValues[tcTable.indexOf(item)] ;									
									compPropVal_m.put("puid",currentSecPropValues.selectedComponent);
									compPropVal_m.put("section",section);	
							        //getting the column number
									int columnNum = (Integer) ((Text)(event.getSource())).getData("colId");
									
									//adding to the Map
									compPropVal_m.put(currentSecPropValues.propNameValuepair[columnNum].propName,((Text) (event.getSource())).getText());
									}      
								});	
						break;
						
					case 6:
						Button checkButton = new Button(tcTable, SWT.CHECK);	
						
						checkButton.setData("colId", ColumnNumber);
						if(PropValue.equalsIgnoreCase("True"))
							checkButton.setSelection(true);					
						editor.grabHorizontal = true;						
						editor.setEditor(checkButton,item,ColumnNumber);
						enableORDisableConrol(editor.getEditor());
						CheckboxCellEditor checkBoxEditor = new CheckboxCellEditor(tcTable);
						if(PropValue.equalsIgnoreCase("True"))
							checkBoxEditor.setValue(true);
						if(!(tableConfigValues[inx].propNameValuepair[ColumnNumber].isEnabled)|| !isTableEditabe	)
							checkButton.setEnabled(false);						
						else
							checkButton.setEnabled(true);
						checkButton.addSelectionListener(new SelectionAdapter() {     
							public void widgetSelected(SelectionEvent event) {         
								
								//getting the row index 
								PAMSecondaryPropValue currentSecPropValues = tableConfigValues[tcTable.indexOf(item)] ;									
								compPropVal_m.put("puid",currentSecPropValues.selectedComponent);
								compPropVal_m.put("section",section);
						        //getting the column number
								int columnNum = (Integer) ((Text)(event.getSource())).getData("colId");
								
								//adding to the Map
								compPropVal_m.put(currentSecPropValues.propNameValuepair[columnNum].propName,((Text) (event.getSource())).getText());
								
								}      
							});
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
							
							combo.add("", 0);				
							editor.grabHorizontal = true;
							editor.setEditor(combo, item, ColumnNumber);
							
							enableORDisableConrol(editor.getEditor());
							
							combo.addSelectionListener(new SelectionAdapter() {     
								public void widgetSelected(SelectionEvent event) {        
									
									//getting the row index 
									PAMSecondaryPropValue currentSecPropValues = tableConfigValues[tcTable.indexOf(item)] ;	
									//PAMSecondaryPropValue currentSecPropValues = tempRowConfigArray[tcTable.indexOf(item)] ;									
									compPropVal_m.put("puid",currentSecPropValues.selectedComponent);
									compPropVal_m.put("section",section);		
							        //getting the column number
									int columnNum = (Integer) ((CCombo)(event.getSource())).getData("colId");
									
									//adding to the Map
									compPropVal_m.put(currentSecPropValues.propNameValuepair[columnNum].propName,((CCombo) (event.getSource())).getText());

										
									}      
								});
														
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
						     enableORDisableConrol(editor.getEditor());
					     text.addModifyListener(new ModifyListener() {     
									public void modifyText(ModifyEvent event) {         
										//getting the row index 
										PAMSecondaryPropValue currentSecPropValues = tableConfigValues[tcTable.indexOf(item)] ;									
										compPropVal_m.put("puid",currentSecPropValues.selectedComponent);
										compPropVal_m.put("section",section);		
								        //getting the column number
										int columnNum = (Integer) ((Text)(event.getSource())).getData("colId");
										
										//adding to the Map
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
						      enableORDisableConrol(editor.getEditor());
						      text.addModifyListener(new ModifyListener() {     
									public void modifyText(ModifyEvent event) {         
										//getting the row index 
										PAMSecondaryPropValue currentSecPropValues = tableConfigValues[tcTable.indexOf(item)] ;									
										compPropVal_m.put("puid",currentSecPropValues.selectedComponent);
										compPropVal_m.put("section",section);		
								        //getting the column number
										int columnNum = (Integer) ((Text)(event.getSource())).getData("colId");
										
										//adding to the Map
										compPropVal_m.put(currentSecPropValues.propNameValuepair[columnNum].propName,((Text) (event.getSource())).getText());
											
										}      
									});	
							break;	
					}					
		}
	    modifiedPropVals_v.add(compPropVal_m);			
		}
			
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
			UnileverUtility.getPerformanceTime(startTime, "PAM/Pack load in createTables() in CLayerStructureTableComposite");
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
	 public TCProperty saveProperty(TCComponent paramTCComponent)
			    throws Exception
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
	
	public void enableORDisableConrol(Control control)
	{
		if(control != null)
		{
		      if(pamSpecRevision.isCheckedOut())
				{
			    	TCComponent checkedOutUser = null;
					
				    try {
						checkedOutUser = pamSpecRevision.getReferenceProperty("checked_out_user");
					} catch (TCException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				    
					if(session.getUser().equals(checkedOutUser))
					{
						control.setEnabled(true);
					}
					else
					{
						control.setEnabled(false);
					}
				}
				
				else 
				{
						control.setEnabled(false);
				}
			
		}
	}

	
}
