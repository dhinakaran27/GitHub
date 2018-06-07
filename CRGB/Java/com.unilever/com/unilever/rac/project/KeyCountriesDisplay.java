package com.unilever.rac.project;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentListOfValues;
import com.teamcenter.rac.kernel.TCComponentListOfValuesType;
import com.teamcenter.rac.kernel.TCComponentProject;
import com.teamcenter.rac.kernel.TCComponentProjectType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.kernel.TCPropertyDescriptor;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.Registry;
import com.teamcenter.rac.viewer.stylesheet.beans.AbstractPropertyBean;
import com.teamcenter.services.rac.core.DataManagementService;
import com.unilever.rac.ui.common.UL4Common;
import com.unilever.rac.util.UnileverUtility;

public class KeyCountriesDisplay extends AbstractPropertyBean
{

	private Composite kcComposite 								= null;	
	private Composite kcComposite2 								= null;
	private Composite kcComposite3 								= null;
	private Composite parentComposite 							= null;
	private TCComponentItemRevision projectRevision 			= null;	
	private String current_cluster_values []					= null;	
	private String current_kcountries_values []					= null;	
	private Map<String,List<String>> availableCountries			= null;
	private Map<String,List<String>> selectedCountries			= null;
	private Tree rightTree										= null;
	private Tree leftTree										= null;
	private TCProperty countryProperty 							= null;
	private DataManagementService dservice 						= null;
	private TCSession session									= null;
	private String sessionUser 				  					= null;
	private String sessonRole				 					= null;
	private String sessionGroup 								= null;
	private Registry reg                      					= null;

	public KeyCountriesDisplay(FormToolkit toolkit, Composite composite,boolean paramBoolean,Map<String, String> PropName)
	{
		super(composite);

		this.parentComposite = composite;	

		GridLayout layoutparent = new GridLayout();
		layoutparent.numColumns = 4;
		layoutparent.makeColumnsEqualWidth = false;
		parentComposite.setLayout(layoutparent);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = SWT.LEFT_TO_RIGHT;
		parentComposite.setLayoutData(gridData);

		AbstractAIFUIApplication application = AIFUtility.getCurrentApplication();

		InterfaceAIFComponent[] targets = null;

		targets = application.getTargetComponents();

		if (targets[0] instanceof TCComponentItemRevision)
		{
			projectRevision = (TCComponentItemRevision) targets[0];

			if (projectRevision!=null)
			{
				session = projectRevision.getSession();

				if (session!=null)
					dservice =DataManagementService.getService(session);	
			}
		}
		
		reg = Registry.getRegistry( this );
	}

	@Override
	public boolean isPropertyModified(TCProperty arg0) throws Exception 
	{
		return true;
	}


	@Override
	public Object getEditableValue() 
	{	
		return parentComposite;
	}

	@Override
	public String getProperty() 
	{
		return "object_name";
	}

	@Override
	public TCProperty getPropertyToSave(TCComponent paramTCComponent)
			throws Exception
			{
		return null;
			}

	@Override
	public TCProperty getPropertyToSave(TCProperty arg0) throws Exception {
		return null;
	}

	private void initializeUI()
	{
		// Composite1 - For adding Left List
		kcComposite = new Composite(parentComposite, SWT.LEFT);
		leftTree = new Tree(kcComposite, SWT.MULTI|SWT.BORDER | SWT.V_SCROLL| SWT.H_SCROLL);

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		kcComposite.setLayout(gridLayout);

		leftTree.setSize(170, 200);
		leftTree.setHeaderVisible(true);
		TreeColumn column1 = new TreeColumn(leftTree, SWT.LEFT);
		column1.setText("List Of Countries");
		column1.setWidth(170);

		GridData myGrid = new GridData(GridData.FILL_BOTH);
		myGrid.heightHint = 200;
		myGrid.widthHint  = 170;
		leftTree.setLayoutData(myGrid);

		availableCountries = new HashMap<String, List<String>>();
		selectedCountries = new HashMap<String, List<String>>();

		this.loadMaps();
		this.constructTree(availableCountries,leftTree);

		// Composite2 - For adding Add and Remove Buttons
		kcComposite2 = new Composite(parentComposite, SWT.CENTER|SWT.BORDER);
		RowLayout layout = new RowLayout(SWT.VERTICAL);
		layout.wrap = false;
		layout.pack = false;
		layout.fill = false;
		layout.justify = false;
		kcComposite2.setLayout(layout);

		Button addButton = new Button(kcComposite2, SWT.PUSH);
		addButton.setText(">");

		// Register Button >>  for selection events
		addButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				switch (e.type) {
				case SWT.Selection:
					TreeItem[] removalItems = leftTree.getSelection();
					List<String> countries = new ArrayList<String>();

					for (int i = 0; i < removalItems.length; i++){
						TreeItem parentTree =  removalItems[i].getParentItem();
						if (parentTree!=null)
							countries.add(removalItems[i].getText());
					}

					if (countries.size()==0)//no counties selected.. in the dropdown
						return;

					Shell shell = parentComposite.getShell();		
					KeyCountriesConfirmationDialog Dlg = new KeyCountriesConfirmationDialog(shell,countries,"Add");
					Dlg.open();

					if(Dlg.isOKPressed() == true)
					{
						if (syncTrees(leftTree,rightTree,availableCountries,selectedCountries)== true)
						{
							long  startTime = System.currentTimeMillis();
							setKeyCountry();
							if(UnileverUtility.isPerfMonitorTriggered == true)
								UnileverUtility.getPerformanceTime(startTime, "setKeyCountry" );
						}
					}

					break;
				}
			}
		});


		Button removeButton = new Button(kcComposite2, SWT.PUSH);
		removeButton.setText("<");

		// Register Button << for selection events  
		removeButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e)
			{
				switch (e.type)
				{
				case SWT.Selection:

					TreeItem[] removalItems = rightTree.getSelection();
					List<String> countries = new ArrayList<String>();

					for (int i = 0; i < removalItems.length; i++){
						TreeItem parentTree =  removalItems[i].getParentItem();
						if (parentTree!=null)
							countries.add(removalItems[i].getText());
					}

					if (countries.size()==0)//no counties selected.. in the dropdown
						return;

					if ( ((current_kcountries_values.length)-(countries.size()) )==0)
					{
						MessageBox messageBox = new MessageBox(kcComposite2.getShell(), SWT.ICON_WARNING);
						messageBox.setText("Warning");
						messageBox.setMessage("Business rules requires that you enter value for 'Key Countries'");
						messageBox.open();
						return ;
					}

					Shell shell = parentComposite.getShell();
					KeyCountriesConfirmationDialog Dlg = new KeyCountriesConfirmationDialog(shell,countries,"Remove");
					Dlg.open();

					if(Dlg.isOKPressed() == true)
					{
						if (syncTrees(rightTree,leftTree,selectedCountries,availableCountries) == true)
						{
							long  startTime = System.currentTimeMillis();
							setKeyCountry();
							if(UnileverUtility.isPerfMonitorTriggered == true)
								UnileverUtility.getPerformanceTime(startTime, "setKeyCountry" );
						}
					}

					break;
				}
			}
		});

		// Composite3 - For adding Second List
		kcComposite3 = new Composite(parentComposite, SWT.RIGHT);
		rightTree = new Tree(kcComposite3, SWT.MULTI| SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);

		rightTree.setSize(170, 200);
		rightTree.setHeaderVisible(true);
		TreeColumn column2 = new TreeColumn(rightTree, SWT.LEFT);
		column2.setText("Selected Countries");
		column2.setWidth(170);

		GridLayout gridLayout3 = new GridLayout();
		gridLayout3.numColumns = 1;
		kcComposite3.setLayout(gridLayout3);


		GridData myGrid2 = new GridData(GridData.FILL_BOTH);
		myGrid2.heightHint = 200;
		myGrid2.widthHint  = 170;
		rightTree.setLayoutData(myGrid2);

		this.constructTree(selectedCountries,rightTree);
		
		boolean bObjectCheckedout =false;
		bObjectCheckedout = isObjectCheckedout();
		
		if (bObjectCheckedout == true)
		{
			addButton.setEnabled(true);
			removeButton.setEnabled(true);
		}
		else
		{
			addButton.setEnabled(false);
			removeButton.setEnabled(false);
		}
	}
	
	public boolean isObjectCheckedout()
	{
		
	      if(projectRevision.isCheckedOut())
			{
		    	TCComponent checkedOutUser = null;
				
			    try {
					checkedOutUser = projectRevision.getReferenceProperty("checked_out_user");
				} catch (TCException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			    
				if(session.getUser().equals(checkedOutUser))
				{
					return true;
				}
				else
				{
					return false;
				}
			}
			
			else 
			{
				return false;
			}
			
	}

	@Override
	public void load(TCProperty arg0) throws Exception 
	{
		try {
			TCProperty clusterProperty  = projectRevision.getTCProperty("u4_unilever_cluster");
			if (clusterProperty!=null)
				current_cluster_values = clusterProperty.getStringValueArray();

			countryProperty  = projectRevision.getTCProperty("u4_key_countries");
			if (countryProperty!=null)
				current_kcountries_values = countryProperty.getStringValueArray();

		} catch (TCException e) {
			e.printStackTrace();
		}

		initializeUI();
	}


	@Override
	public void load(TCPropertyDescriptor arg0) throws Exception {
	}

	@Override
	public void setModifiable(boolean arg0) {
	}

	@Override
	public void setUIFValue(Object arg0) {
	}

	private String[] getLOVValues(String lovName)
	{
		String[] lovValuesArr = null;
		TCComponentListOfValues LOV= TCComponentListOfValuesType.findLOVByName(lovName);
		try 
		{
			lovValuesArr = LOV.getListOfValues().getStringListOfValues();
		}
		catch (TCException e) 
		{

		}
		return lovValuesArr;
	}

	private void constructTree(Map<String,List<String>> selectedCountries,Tree tree)
	{
		List<String> uniq_cluster = new ArrayList<String>();

		for (Map.Entry<String,List<String>> entry : selectedCountries.entrySet()) 
			uniq_cluster.add(entry.getKey());

		Collections.sort(uniq_cluster); // sort the string elements alphabetically

		for (int index = 0 ; index < uniq_cluster.size() ; index++)
		{
			TreeItem clusterTree = new TreeItem(tree, 0);
			String continent = uniq_cluster.get(index);
			clusterTree.setText(0,continent);

			List<String> countries = (List<String>)selectedCountries.get(continent);
			if (countries != null) 
			{
				for (String country : countries) 
				{
					if (country != null) 
					{
						TreeItem country_tree = new TreeItem(clusterTree, 0);
						country_tree.setText(0,country);
					}
				}
			}
		}
	}

	private void UpdateTree(Map<String,List<String>> updateTree,Tree tree)
	{
		List<String> uniq_cluster = new ArrayList<String>();

		for (Map.Entry<String,List<String>> entry : updateTree.entrySet()) 
			uniq_cluster.add(entry.getKey());

		Collections.sort(uniq_cluster); // sort the string elements alphabetically

		TreeItem[] firstLevel =  tree.getItems();

		if (firstLevel.length == 0)
		{
			for (int index = 0 ; index < uniq_cluster.size() ; index++ )
			{
				String continent  = uniq_cluster.get(index);

				List<String> addCountriesValues = (List<String>)updateTree.get(continent);

				TreeItem new_firstlevel = new TreeItem( tree, 0);
				new_firstlevel.setText(0,continent);

				for (int country_index = 0; country_index < addCountriesValues.size(); country_index++)
				{
					TreeItem item = new TreeItem( new_firstlevel, 0);
					item.setText(0, addCountriesValues.get(country_index));
				}
			}
		}

		for (int l = 0; l < firstLevel.length; l++)
		{
			String continent  = firstLevel[l].getText();

			List<String> addCountriesValues = (List<String>)updateTree.get(continent);

			if (uniq_cluster.contains(continent)==true)
			{
				if (addCountriesValues.size() > 0)
					firstLevel[l].removeAll();

				for (int country_index = 0; country_index < addCountriesValues.size(); country_index++)
				{
					TreeItem item = new TreeItem( firstLevel[l], 0);
					item.setText(0, addCountriesValues.get(country_index));
				}
			}
			else
			{
				if (addCountriesValues.size() > 0)
				{
					TreeItem new_firstlevel = new TreeItem( tree, 0);
					new_firstlevel.setText(0, continent);

					for (int country_index = 0; country_index < addCountriesValues.size(); country_index++)
					{
						TreeItem item = new TreeItem( new_firstlevel, 0);
						item.setText(0, addCountriesValues.get(country_index));
					}
				}
			}
		}
	}



	private boolean syncTrees(Tree removalTree,Tree addTree ,Map<String,List<String>> removeCountries,Map<String,List<String>> addCountries)
	{
		TreeItem[] removalItems = removalTree.getSelection();

		for (int i = 0; i < removalItems.length; i++)
		{
			TreeItem parentTree =  removalItems[i].getParentItem();

			if (parentTree==null)
				continue;

			String continent = parentTree.getText();
			String selCountry = removalItems[i].getText();

			if (removeCountries.containsKey(continent) == true)
			{
				List<String> removalCountries = (List<String>)removeCountries.get(continent);

				if (removalCountries.contains(selCountry)==true)
				{
					removalCountries.remove(selCountry);
					removeCountries.put(continent, removalCountries);
					removalItems[i].dispose();
				}

				if (addCountries.containsKey(continent) == true)
				{
					List<String> addCountriesValues = (List<String>)addCountries.get(continent);

					if (addCountriesValues.contains(selCountry)==false)
					{
						addCountriesValues.add(selCountry);
						Collections.sort(addCountriesValues); // sort the string elements alphabetically
						addCountries.put(continent, addCountriesValues);
					}
				}
				else
				{
					List<String> rhs_values1 = new ArrayList<String>();
					rhs_values1.add(selCountry);
					addCountries.put(continent, rhs_values1);
				}
			}
			else
			{	
				MessageBox messageBox = new MessageBox(kcComposite2.getShell(), SWT.ICON_WARNING);
				messageBox.setText("Warning");
				messageBox.setMessage("Can not set "+"'"+selCountry+"'"+" as its corresponding Unilever Cluster "+"'"+continent+"'"+" was not selected");
				messageBox.open();
				return false;
			}
		}

		UpdateTree(addCountries,addTree);

		return true;
	}

	private void loadMaps()
	{
		if (current_cluster_values==null)
			return;
		
		for( String cluster_name:current_cluster_values )
		{
			String[] lovs=null;

			if(cluster_name.equalsIgnoreCase("Africa"))
				lovs = getLOVValues("U4_Africa_KeyCountryLOV");
			else if(cluster_name.equalsIgnoreCase("South Asia"))
				lovs = getLOVValues("U4_SouthAsiaKeyCountryLOV");
			else if(cluster_name.equalsIgnoreCase("Europe"))
				lovs = getLOVValues("U4_Europe_KeyCountryLOV");
			else if(cluster_name.equalsIgnoreCase("Latin America"))
				lovs = getLOVValues("U4_LatinAmericaKeyCountryLOV");
			else if(cluster_name.equalsIgnoreCase("NAMET & RUB"))
				lovs = getLOVValues("U4_Namet&RubKeyCountryLOV");
			else if(cluster_name.equalsIgnoreCase("North America"))
				lovs = getLOVValues("U4_NorthAmericaKeyCountryLOV");
			else if(cluster_name.equalsIgnoreCase("North Asia"))
				lovs = getLOVValues("U4_NorthAsiaKeyCountryLOV");
			else if(cluster_name.equalsIgnoreCase("SEAA"))
				lovs = getLOVValues("U4_SEAAKeyCountryLOV");

			for( String lov_value:lovs )
			{
				if(ArrayUtils.contains( current_kcountries_values, lov_value ) == false)
				{
					if(ArrayUtils.contains( current_cluster_values, cluster_name ) == true)
					{
						if (availableCountries.containsKey(cluster_name) == true)
						{
							List<String> rhs_values = (List<String>)availableCountries.get(cluster_name);
							rhs_values.add(lov_value);
							availableCountries.put(cluster_name, rhs_values);
						}
						else
						{
							List<String> rhs_values = new ArrayList<String>();
							rhs_values.add(lov_value);
							availableCountries.put(cluster_name, rhs_values);
						}
					}
				}
				else
				{
					if (selectedCountries.containsKey(cluster_name) == true)
					{
						List<String> rhs_values = (List<String>)selectedCountries.get(cluster_name);
						rhs_values.add(lov_value);
						selectedCountries.put(cluster_name, rhs_values);
					}
					else
					{
						List<String> rhs_values = new ArrayList<String>();
						rhs_values.add(lov_value);
						selectedCountries.put(cluster_name, rhs_values);
					}

					//add only first level, incase the lov_value is present in the selected list
					if (availableCountries.containsKey(cluster_name) == false)
					{
						List<String> rhs_values = new ArrayList<String>();
						availableCountries.put(cluster_name, rhs_values);
					}
				}
			}
		}
	}


	private void setKeyCountry()
	{
		if (isPrivilegedUsers() ==false)
			return;
		
		List<String> new_value	  = new ArrayList<String>();

		for (Map.Entry<String,List<String>> entry : selectedCountries.entrySet()) 
		{
			List<String> values = entry.getValue();

			for (int index = 0; index < values.size(); index++)
				new_value.add(values.get(index));
		}

		Collections.sort(new_value); // sort the string elements alphabetically
		TCComponent[] itemRevs = new TCComponent[]{ projectRevision };
		Map<String, DataManagementService.VecStruct> properties = new HashMap<String, DataManagementService.VecStruct>();

		DataManagementService.VecStruct vector_values = new DataManagementService.VecStruct();	
		vector_values.stringVec =  new_value.toArray(new String[new_value.size()]);
		properties.put("u4_key_countries", vector_values);

		try
		{
			if (dservice!=null)
				dservice.setProperties((TCComponent[]) itemRevs, properties);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private boolean isPrivilegedUsers() 
	{	
		TCComponentProject project = null ;     
        boolean validUser = false ;
        String pid  = null ;
		
		if(projectRevision != null)
		{
			try
			{				
				sessionUser  = session.getUserName();	
				sessionGroup = session.getGroup().getStringProperty(UL4Common.NAME);
				sessonRole   = session.getRole().getStringProperty(UL4Common.ROLENAME);
				
				if(sessonRole.equals(UL4Common.DBA))
					return true ;

				pid   = projectRevision.getStringProperty(UL4Common.TCPROJECTIDS);

				if( pid == null || (  (pid.length() <= 1)  && pid.equalsIgnoreCase(" ")))
				{
					projectRevision.refresh();
					pid   = projectRevision.getStringProperty(UL4Common.TCPROJECTIDS);
				}
				
				if(pid != null &&   (  pid.length() >= 1  &&   ! pid.equalsIgnoreCase(" ")  ))
				{					
					String[] projectsId =  pid.split(",");
					
					TCComponentProjectType projectType = (TCComponentProjectType)(session.getTypeComponent(UL4Common.TCPROJECT));
					
					for(int nProj = 0; nProj < projectsId.length; nProj++)
					{
						project = projectType.find(projectsId[nProj].trim());
						
						if(projectType.isPrivilegedMember(project, session.getUser()))
							return true ;
					}					
		
				}
				else
					project = null ;
			} 
			catch (TCException e) 
			{
				MessageBox messageBox = new MessageBox(kcComposite2.getShell(), SWT.ICON_ERROR);
				messageBox.setText("Error");
				messageBox.setMessage(e.getMessage());
				messageBox.open();
			}
		}
		
		if(!validUser)
		{
			if(project==null)
			{
				MessageBox messageBox = new MessageBox(kcComposite2.getShell(), SWT.ICON_ERROR);
				messageBox.setText("Error");
				messageBox.setMessage(reg.getString("tcprojectnotfound"));
				messageBox.open();
			}
			else
			{
				MessageBox messageBox = new MessageBox(kcComposite2.getShell(), SWT.ICON_ERROR);
				messageBox.setText("Error");
				messageBox.setMessage("TC Project < " + pid.trim() + " > " + reg.getString("invaliduser"));
				messageBox.open();
			}
		}
		
		return validUser ;
	}
	

}
