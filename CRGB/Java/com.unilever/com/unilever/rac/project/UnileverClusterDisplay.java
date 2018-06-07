package com.unilever.rac.project;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.forms.widgets.FormToolkit;
import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.ListOfValuesInfo;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentListOfValues;
import com.teamcenter.rac.kernel.TCComponentListOfValuesType;
import com.teamcenter.rac.kernel.TCComponentProject;
import com.teamcenter.rac.kernel.TCComponentProjectType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.kernel.TCPropertyDescriptor;
import com.teamcenter.rac.util.Registry;
import com.teamcenter.rac.viewer.stylesheet.beans.AbstractPropertyBean;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.rac.kernel.TCSession;
import com.unilever.rac.ui.common.UL4Common;

public class UnileverClusterDisplay extends AbstractPropertyBean
{
	private Composite ucComposite					= null;
	private Composite parentComposite 				= null;
	private TCComponentItemRevision projectRevision = null;	
	private List<String> clusterVal					= null;
	private Map<String,List<String>> lov_Values		= null;
	private String [] cluster_names 				= null;
	private Button[] clusterButton 					= null;
	private TCProperty clusterProperty 				= null;
	private String current_countries[] 				= null;
	private DataManagementService dservice			= null;
	private TCSession session						= null;
	private String sessionUser 				  		= null;
	private String sessonRole				 		= null;
	private String sessionGroup 					= null;
	private Registry reg                      		= null;

	public UnileverClusterDisplay(FormToolkit toolkit, Composite composite,boolean paramBoolean,Map<String, String> PropName)
	{
		super(composite);

		this.parentComposite = composite;	

		GridLayout layoutparent = new GridLayout();
		layoutparent.numColumns = 2;
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

			try {
				clusterProperty  = projectRevision.getTCProperty("u4_unilever_cluster");

				if (clusterProperty!=null)
				{
					ListOfValuesInfo tempLOVInfo;					

					TCComponentListOfValues clusterLOVs = clusterProperty.getLOV();

					if (clusterLOVs!=null)
					{
						tempLOVInfo = clusterLOVs.getListOfValues();

						if (tempLOVInfo!=null)
						{
							Object objectArray[] = tempLOVInfo.getListOfValues();
							cluster_names = Arrays.copyOf(objectArray, objectArray.length, String[].class);
							Arrays.sort(cluster_names); // sort the string elements alphabetically
						}
					}
				}

				lov_Values = new HashMap<String, List<String>>();
				getLOVValues();

			} catch (TCException e) {
				e.printStackTrace();
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

	public void initializeUI()
	{
		// Composite for Unilever Cluster Group of Buttons
		ucComposite = new Composite(parentComposite, SWT.LEFT);

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		gridLayout.verticalSpacing = 9;
		ucComposite.setLayout(gridLayout);
		clusterVal = new ArrayList<String>();
		this.initializeList();

		// Add all the Check Buttons to the Composite
		initializeButtons();
	}

	@Override
	public void load(TCProperty arg0) throws Exception 
	{
		TCProperty countryProperty=null;
		try {
			countryProperty = projectRevision.getTCProperty("u4_key_countries");
		} catch (TCException e) {
			e.printStackTrace();
		}

		if (countryProperty!=null)
		{
			current_countries = countryProperty.getStringValueArray();
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

	//	@Override
	public TCProperty saveProperty(TCComponent paramTCComponent)
			throws Exception
			{
		return null;
			}

	//	@Override
	public void save(TCProperty paramTCProperty)
			throws Exception
			{

			}

	public boolean IsChecked(String buttonText)
	{
		if (clusterVal!=null)
			if (clusterVal.contains(buttonText)==true)
				return true;

		return false;
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

	public void initializeButtons()
	{
		clusterButton = new Button[8];

		int i = 0;
		
		boolean bObjectCheckedout =false;	
		
		bObjectCheckedout = isObjectCheckedout();

		if (cluster_names!=null)
		{
			for( String cluster_name:cluster_names )
			{
				clusterButton[i] = new Button( ucComposite, SWT.CHECK);
				if (bObjectCheckedout == true)
				{
					clusterButton[i].setEnabled(true);
				}
				else
				{
					clusterButton[i].setEnabled(false);
				}

				if (cluster_name.compareTo("NAMET & RUB")==0)
					clusterButton[i].setText("NAMET && RUB");
				else
					clusterButton[i].setText(cluster_name);

				if (cluster_name.compareTo("NAMET & RUB")==0)
				{
					if(IsChecked("NAMET & RUB"))
						clusterButton[i].setSelection(true);
				}
				else
				{
					if(IsChecked(clusterButton[i].getText()))
						clusterButton[i].setSelection(true);
				}

				clusterButton[i].addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event event)
					{
						Button button = (Button) event.widget;
						String cluster_name_value = button.getText();

						cluster_name_value = (cluster_name_value.compareTo("NAMET && RUB")==0)?"NAMET & RUB":cluster_name_value;

						if(button.getSelection())
						{
							if (current_countries.length >0)
							{
								clusterVal.add(cluster_name_value);
								setCluster();
							}
							else if (current_countries.length == 0)
							{
								clusterWarning(cluster_name_value,2);//Can not set '"+continent+"' cluster.\nPlease fill 'Key Countries' prior to setting Cluster
								button.setSelection(false);
							}
						}
						else
						{
							if (clusterVal.size() ==1)
							{
								clusterWarning(cluster_name_value,0);//"Business rules requires that you enter value for 'Unilever Cluster'"
								button.setSelection(true);
								return;
							}

							if(canUnSetCluster(cluster_name_value) == true)
							{
								clusterVal.remove(cluster_name_value);
								setCluster();
							}
							else
							{
								if (current_countries.length == 0)
								{
									clusterWarning(cluster_name_value,3);//Can not unset '"+continent+"' cluster.\nPlease fill 'Key Countries' prior to unset of Cluster
									button.setSelection(true);
								}
								else if (current_countries.length > 0)
								{
									clusterWarning(cluster_name_value,1);//Can not unset '"+continent+"' cluster.\nThe Key Country is filled for this cluster.
									button.setSelection(true);
								}

							}
						}

					}
				});

				i++;
			}
		}
	}

	private void initializeList()
	{
		String array[] =null;
		if (clusterProperty!=null)
			array = clusterProperty.getStringValueArray();

		if (array!=null)
			clusterVal = new ArrayList<String>(Arrays.asList(array));

	}
	public boolean canUnSetCluster(String continent)
	{
		if ((current_countries !=null) && (current_countries.length ==0)  )
			return false;

		if (lov_Values.containsKey(continent) == true)
		{
			List<String> CountriesValues = (List<String>)lov_Values.get(continent);

			if (current_countries !=null)
			{
				for( String retVal:current_countries )
				{
					if (CountriesValues.contains(retVal)==true)
					{
						return false;
					}
				}
			}
		}

		return true;
	}


	public void setCluster()
	{
		if (isPrivilegedUsers() ==false)
			return;
		
		if (clusterVal!=null)
		{
			Collections.sort(clusterVal); // sort the string elements alphabetically

			TCComponent[] itemRevs = new TCComponent[]{ projectRevision };
			Map<String, DataManagementService.VecStruct> properties = new HashMap<String, DataManagementService.VecStruct>();

			DataManagementService.VecStruct vector_values = new DataManagementService.VecStruct();	
			vector_values.stringVec =  clusterVal.toArray(new String[clusterVal.size()]);
			properties.put("u4_unilever_cluster", vector_values);

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
				MessageBox messageBox = new MessageBox(ucComposite.getShell(), SWT.ICON_ERROR);
				messageBox.setText("Error");
				messageBox.setMessage(e.getMessage());
				messageBox.open();
			}
		}
		
		if(!validUser)
		{
			if(project==null)
			{
				MessageBox messageBox = new MessageBox(ucComposite.getShell(), SWT.ICON_ERROR);
				messageBox.setText("Error");
				messageBox.setMessage(reg.getString("tcprojectnotfound"));
				messageBox.open();
			}
			else
			{
				MessageBox messageBox = new MessageBox(ucComposite.getShell(), SWT.ICON_ERROR);
				messageBox.setText("Error");
				messageBox.setMessage("TC Project < " + pid.trim() + " > " + reg.getString("invaliduser"));
				messageBox.open();
			}
		}
		
		return validUser ;
	}
	

	public void clusterWarning(String continent, int index)
	{
		MessageBox messageBox = new MessageBox(ucComposite.getShell(), SWT.ICON_WARNING);
		messageBox.setText("Warning");
		
		if (index==0)
			messageBox.setMessage("Business rules requires that you enter value for 'Unilever Cluster'");
		else if (index ==1)
			messageBox.setMessage("Cannot unselect '"+continent+"' cluster, while a Key Country is selected for this cluster");
		else if (index ==2)
			messageBox.setMessage("Cannot set '"+continent+"' cluster,Please fill 'Key Countries' prior to setting 'Unilever Cluster'");
		else if (index ==3)
			messageBox.setMessage("Cannot unselect '"+continent+"' cluster,Please fill 'Key Countries' prior to unselecting 'Unilever Cluster'");

		messageBox.open();
	}

	private void getLOVValues( )
	{
		if (cluster_names!=null)
		{
			for( String cluster_name:cluster_names )
			{
				String lovName = "";

				if(cluster_name.equalsIgnoreCase("Africa"))
					lovName="U4_Africa_KeyCountryLOV";
				else if(cluster_name.equalsIgnoreCase("South Asia"))
					lovName="U4_SouthAsiaKeyCountryLOV";
				else if(cluster_name.equalsIgnoreCase("Europe"))
					lovName="U4_Europe_KeyCountryLOV";
				else if(cluster_name.equalsIgnoreCase("Latin America"))
					lovName="U4_LatinAmericaKeyCountryLOV";
				else if(cluster_name.equalsIgnoreCase("NAMET & RUB"))
					lovName="U4_Namet&RubKeyCountryLOV";
				else if(cluster_name.equalsIgnoreCase("North America"))
					lovName="U4_NorthAmericaKeyCountryLOV";
				else if(cluster_name.equalsIgnoreCase("North Asia"))
					lovName="U4_NorthAsiaKeyCountryLOV";
				else if(cluster_name.equalsIgnoreCase("SEAA"))
					lovName="U4_SEAAKeyCountryLOV";

				TCComponentListOfValues LOV= TCComponentListOfValuesType.findLOVByName(lovName);
				try 
				{
					if (LOV!=null)
					{
						String[] lovValuesArr = LOV.getListOfValues().getStringListOfValues();

						if (lov_Values.containsKey(cluster_name) == false)
						{
							List<String> rhs_values = new ArrayList<String>();
							Collections.addAll(rhs_values, lovValuesArr);
							lov_Values.put(cluster_name, rhs_values);
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
}