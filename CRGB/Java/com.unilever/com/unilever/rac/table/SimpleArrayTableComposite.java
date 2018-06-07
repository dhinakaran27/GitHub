package com.unilever.rac.table;

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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.ListOfValuesInfo;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentForm;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentListOfValues;
import com.teamcenter.rac.kernel.TCComponentType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.kernel.TCPropertyDescriptor;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.viewer.stylesheet.beans.AbstractPropertyBean;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core._2013_05.LOV.InitialLovData;
import com.unilever.rac.pam.LoadPAMConfigurations;
import com.unilever.rac.pam.PAMConstant;
import com.unilever.rac.pam.PAMSecondaryPropValue;
import com.unilever.rac.pam.PAMTableConfiguration;
import com.unilever.rac.ui.common.UL4Common;
import com.unilever.rac.util.UnileverUtility;

/**
* @author vivek.n.gowdagere
*
*/
public class SimpleArrayTableComposite extends AbstractPropertyBean{

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

	private int tableHeight= 100;

	private int tableWidth = 700;

	private FormToolkit parentFormToolKit;
	private boolean isCheckOut = false ;

	public static StringBuffer sbErrorMessage = null ;
	/**
* 
*/
	
	/**
* @param toolkit
* @param composite
* @param paramBoolean
* @param PropName
*/
	public SimpleArrayTableComposite(FormToolkit toolkit, Composite composite,boolean paramBoolean,Map<String, String> PropName)
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
			isCheckOut = itemRevision.isCheckedOut(); 

			LoadPAMConfigurations loadConfig = new LoadPAMConfigurations();

			Vector<PAMTableConfiguration> tableConfigs = new Vector<PAMTableConfiguration>();

			tableConfigs = loadConfig.readPreferenceValues1(renderedPropName+"_ArrayTable_Configuration", session);

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

			if(isCheckOut == true)
			{
				DataManagementService dmService = DataManagementService.getService(session);
				dmService.refreshObjects(formarray.toArray(new TCComponent[formarray.size()]));
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
							
							if (knx == 0)// Attempt to get the size of the array // Also assuming all properties have same size
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
							
							if (proptype[knx] == 3)//Double Attribute
							{
								double array[] = currProperty[knx].getDoubleArrayValue();
								
								for (int i = 0 ; i < array.length ; i++ )
									propValueArray[knx].propValue[i] = Double.toString(array[i]); 	
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
			GridData localGridData = new GridData(1808);
			localGridData.heightHint = (PAMPropertyNameValue_v.size())*tableHeight+100;
			localGridData.widthHint = tableWidth;
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
		loadConfigurations();

		Table[] propertyTables = new Table[PAMPropertyNameValue_v.size()];
		for(int inx=0;inx<PAMPropertyNameValue_v.size();inx++)
		{
			int style = SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.HIDE_SELECTION;
			Label propertyLabel = new Label(tableComposite,SWT.NONE);
			String tableType = (PAMPropertyNameValue_v.get(inx)).get(0).selectedComponent.getDisplayType();
			propertyLabel.setText(tableType+":");
			propertyLabel.pack();
			propertyTables[inx] = new Table(tableComposite,style );
			GridData localGridData1 = new GridData(1808);
			localGridData1.heightHint = tableHeight;
			localGridData1.widthHint = tableWidth;

			propertyTables[inx].setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

			propertyTables[inx].setHeaderVisible(true);
			propertyTables[inx].setLinesVisible(true);    

			Display display =  propertyTables[inx].getDisplay();
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

			long startTime = System.currentTimeMillis();
			
			setTableConfiguration(propertyTables[inx],PAMPropertyNameValue_v.get(inx).toArray(new PropertyConfiguration[PAMPropertyNameValue_v.get(inx).size()]));

			if(UnileverUtility.isPerfMonitorTriggered == true)
			{
				UnileverUtility.getPerformanceTime(startTime, "setTableConfiguration " + tableType );
			}
		}

	}

	/**
* @param tcTable
* @param tableConfigValues
*/
	public void setTableConfiguration(final Table tcTable, final PropertyConfiguration[] tableConfigValues)
	{
		boolean enable_editor = false;
		
		if(itemRevision.isCheckedOut())
		{
			TCComponent checkedOutUser = null;

			try {
				checkedOutUser = itemRevision.getReferenceProperty("checked_out_user");
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

		String validation_rule[]=null;
		
		for(int inx=0;inx<(tableConfigValues).length;inx++)
		{	
			TCComponentForm currForm = (TCComponentForm) tableConfigValues[inx].selectedComponent;
			try 
			{
				TCProperty prop = currForm.getTCProperty("u4_validation_rule");
				
				if (prop!=null)
				{
					validation_rule= prop.getStringValueArray();
				}
			} catch (TCException e) {
				e.printStackTrace();
			}
			
			
			if (tableConfigValues[inx].propNameValuepair.length<=0)
			continue;
			
			final TableItem item[] = new TableItem [tableConfigValues[inx].propNameValuepair[0].length];

			for(  int ColumnNumber =0;ColumnNumber< tableConfigValues[inx].propNameValuepair.length;ColumnNumber++ )
			{
				final int column_number = ColumnNumber;
				final int propType = tableConfigValues[inx].propNameValuepair[ColumnNumber].PropertyType;
				String PropValue[] = (String [])  tableConfigValues[inx].propNameValuepair[ColumnNumber].propValue;
				
				final TCProperty tcProperty = tableConfigValues[inx].propNameValuepair[ColumnNumber].tcProperty;

				if (inx==0)//only for the first form create the columns
				{
					final TableColumn propertyColumn =  new TableColumn(tcTable, SWT.BORDER_SOLID|SWT.COLOR_WIDGET_NORMAL_SHADOW);
					propertyColumn.setWidth(tableConfigValues[inx].propNameValuepair[ColumnNumber].columnSize);
	
					String nametext = tableConfigValues[inx].propNameValuepair[ColumnNumber].propDisplayName;
					propertyColumn.setText(nametext);
					propertyColumn.setResizable(true);
				}
				
				String[] lovStrings = null;
				

				for (int row = 0 ; row < tableConfigValues[inx].propNameValuepair[ColumnNumber].length ; row++)
				{
					final Map<String,Object> compPropVal_m = new HashMap<String,Object>();
					
					final int row_index =row;
					
					if (ColumnNumber==0)
					{
						item[row] = new TableItem(tcTable, SWT.NONE);
						
						if(validation_rule!=null && validation_rule[row]!=null)
						{
							if(validation_rule[row].length() > 4 && ( validation_rule[row].contains("M:") || validation_rule[row].contains("M-TMM") || validation_rule[row].contains("M-LWHD")))
							{
								TableEditor mandatroyEditor = new TableEditor(tcTable);
								PAMConstant.setMandatory(PropValue[row],mandatroyEditor,item[row]);    
							}
						}
					}

					item[row].setText(ColumnNumber,PropValue[row]);
				}
				
			}
			
		}
		tcTable.setRedraw(true);
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
		
		//List<String> uniq_modified_prop = new ArrayList<String>();
		Set<String> uniq_modified_prop = new HashSet<String>();

		for(int inx= 0;inx<modifiedPropVals_v.size();inx++)
		{
			Map<String, Object> propValueMap = modifiedPropVals_v.get(inx);
			
			TCProperty tcProperty = (TCProperty) ( propValueMap).get("property");
			
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
		
		for (String modified_prop : uniq_modified_prop)
		{
			List<String> values = new ArrayList<String>();
			
			TCComponent currComp = null;
			TCProperty tcProperty = null;
			int proptype = 0;
			
			for(int inx= 0;inx<modifiedPropVals_v.size();inx++)
			{
				Map<String, Object> propValueMap = modifiedPropVals_v.get(inx);
				TCProperty tc_Property = (TCProperty) ( propValueMap).get("property");
				
				if (tc_Property==null)
				continue;
				
				int rowindex =  (int) ( propValueMap).get("rowindex");

				if( tc_Property.getPropertyName().compareTo(modified_prop) == 0 )
				{
					currComp = (TCComponent) ( propValueMap).get("puid");
					tcProperty = (TCProperty) ( propValueMap).get("property");
					proptype =  (int) ( propValueMap).get("proptype");

					for(Map.Entry<String, Object> propName : propValueMap.entrySet()) 
					{
						if( tcProperty.getPropertyName().compareTo(propName.getKey()) == 0 )
						{
							values =  (List<String>) ( propValueMap).get("value");
							values.set(rowindex, ((String) (propName.getValue())));// replace the value
							break;
						}
					}
				}
			}
			
			if (currComp==null ||tcProperty==null)
			continue;
			
			currComp.lock();
			
			boolean tosave = true ;
			
			try
			{
				switch (proptype)
				{
				case 6: //Boolean type of cell
					String[] string_array = values.toArray(new String[values.size()]);
					boolean[] boolean_array = new boolean[string_array.length];
					for (int i = 0; i < string_array.length; i++)
					{
						if (string_array[i]!=null)
						boolean_array[i] = Boolean.parseBoolean(string_array[i]);
						else
						boolean_array[i] = false;
					}
					tcProperty.setLogicalValueArray(boolean_array);
					break;
				case 8: //String type of cell
					for (int i = 0; i < values.size(); i++)
					{
						if (values.get(i) == null)
						values.set(i, ""); 
					}
					String[] string_array1 = values.toArray(new String[values.size()]);
					tcProperty.setStringValueArray(string_array1);
				};
			}
			catch (TCException e) 
			{
				tosave = false ;
				sbErrorMessage.append(currComp.toString()).append(" - ");
				sbErrorMessage.append(modified_prop).append(" : ");
				sbErrorMessage.append(e.getMessage()).append("\n");
			}
			
			if(tosave==true)
			{
				try {
					currComp.save();
					currComp.unlock();
					currComp.refresh();
				}
				catch (TCException e) {
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
	

}
