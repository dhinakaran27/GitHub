package com.unilever.rac.pam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentForm;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.kernel.TCPropertyDescriptor;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.viewer.stylesheet.beans.AbstractPropertyBean;
import com.teamcenter.services.rac.core.DataManagementService;

public class ETextComposite extends AbstractPropertyBean{

	private TCComponentItemRevision pamSpecRevision;
	
	private Composite parentComposite;

	private String renderedPropName;
	
	private Vector<PAMFreeTextConfiguration> labelText_v;
	
	private HashMap<TCComponentForm,String> modtextValues_m;
	
	private Composite mainComposite;
	
	private FormToolkit parentFormToolKit;

	private TCSession session;

	private boolean isCheckOut = false ;
	private boolean enableControl = false;

	/**
	 * @param toolkit
	 * @param composite
	 * @param paramBoolean
	 * @param PropName
	 */
	public ETextComposite(FormToolkit toolkit, Composite composite,boolean paramBoolean,Map<String, String> PropName)
	{
	super(composite);	
		
		AbstractAIFUIApplication application = AIFUtility.getCurrentApplication();

		InterfaceAIFComponent[] targets = null;

		targets = application.getTargetComponents();

		if (targets[0] instanceof TCComponentItemRevision)

		{
			pamSpecRevision = (TCComponentItemRevision) targets[0];
		}		

		this.parentComposite = composite;		
		this.renderedPropName = (String) PropName.get("name");
		this.modtextValues_m = new HashMap<TCComponentForm,String>();
		this.labelText_v = new Vector<PAMFreeTextConfiguration>();
		this.parentFormToolKit = toolkit;
		
		session = pamSpecRevision.getSession();	
		parentComposite.setLayout(new GridLayout(1,true));
		mainComposite = new Composite(parentComposite, SWT.NONE);	
		//mainComposite.setBounds( 0, 0, 500, 500);
		GridData localGridData1 = new GridData(1808);		
		localGridData1.heightHint = 100;
	    localGridData1.widthHint = 700;
	    mainComposite.setLayout(new GridLayout(2,false));
	    mainComposite.setLayoutData(localGridData1);
		parentFormToolKit.adapt(mainComposite.getParent(),true,true);
	
		isCheckOut = pamSpecRevision.isCheckedOut(); 
		
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
	
	}
	
	/**
	 * The method to load the UI for the renderingHint
	 */
	public void loadConfigurations()
	{
		try {
			AIFComponentContext[] relatedComponents = pamSpecRevision.getRelated(renderedPropName);
			
			ArrayList<TCComponent> formarray = new ArrayList<TCComponent>();
			
			// Iterating to collect the forms to perform a single refreshObjects call
			for(int inx=0;inx<relatedComponents.length;inx++)
			{
				if((relatedComponents[inx].getComponent() instanceof TCComponentForm))
				{
					TCComponentForm tempForm = (TCComponentForm)(relatedComponents[inx].getComponent());
					if((tempForm.getType()).equalsIgnoreCase(PAMConstant.FREETEXTFORM))
					{	
						formarray.add((TCComponent)tempForm);		
					}
				}
			}
			
			
			if (isCheckOut == true)
			{
				DataManagementService dmService = DataManagementService.getService(session);
				dmService.refreshObjects(formarray.toArray(new TCComponent[formarray.size()]));
			}
			
			for(int inx=0;inx<relatedComponents.length;inx++)
			{
				if((relatedComponents[inx].getComponent() instanceof TCComponentForm))
				{
				TCComponentForm tempForm = (TCComponentForm)(relatedComponents[inx].getComponent());
				if((tempForm.getType()).equalsIgnoreCase(PAMConstant.FREETEXTFORM))
				{	
					PAMFreeTextConfiguration tempConfig = new PAMFreeTextConfiguration();
						
						String [] formproperties = {"u4_property","u4_text"};
						String [] properties = tempForm.getProperties(formproperties);
						
						if ((properties[0]!=null) && (properties[1]!=null))
						{
							tempConfig.propertyName = properties[0];
							tempConfig.propertyValue = properties[1];
						}

					tempConfig.currentForm = tempForm;
					labelText_v.add(tempConfig);
				}
			}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	public void createFreeTextUI() throws TCException
	{
		loadConfigurations();
		
		GridData gridData = (GridData)mainComposite.getLayoutData();
		
		gridData.heightHint = 70*labelText_v.size();
		
		mainComposite.setLayoutData( gridData);
		mainComposite.redraw();

		for(int inx =0;inx<labelText_v.size();inx++)
		{
			GridData localGridData = new GridData(1808);		
			localGridData.heightHint = 50;
		    localGridData.widthHint = 250;
			
			Label freeTextLabel = new Label(mainComposite,SWT.LEFT);
			freeTextLabel.setText(labelText_v.get(inx).propertyName+":");
			freeTextLabel.pack();
			//freeTextLabel.setLayoutData(localGridData);
			
			parentFormToolKit.adapt(freeTextLabel,true,true);
			
			GridData localGridData1 = new GridData(1808);		
			localGridData1.heightHint = 100;
		    localGridData1.widthHint = 500;
			
			Text freeText = new Text(mainComposite, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL|SWT.H_SCROLL);
			freeText.setData("component",labelText_v.get(inx).currentForm);
			freeText.setText(labelText_v.get(inx).propertyValue);
			
			GridData data = new GridData();		
			data.heightHint = 75;
			data.widthHint = 240 ;		
			freeText.setLayoutData(data);
				
			//Display display =  freeText.getDisplay();
			//Defect #959 to enable the scroll bar for PAM freetext field
			//freeText.setEnabled(false);
			freeText.setEditable(enableControl);

			freeText.setLayoutData(localGridData1);
			parentFormToolKit.adapt(freeText,true,true);
			
			freeText.addModifyListener(new ModifyListener() {     
				public void modifyText(ModifyEvent event) {         
					//adding to the Map
					TCComponentForm tempForm = (TCComponentForm)((Text)(event.getSource())).getData("component");
					modtextValues_m.put(tempForm, ((Text)(event.getSource())).getText());
					}      
				});
		}
		
		mainComposite.setSize(1000, 1000);
	}
	
	
	@Override
	public Object getEditableValue() {
		// TODO Auto-generated method stub
		return mainComposite;
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
		createFreeTextUI();
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
	 public TCProperty getPropertyToSave(TCComponent paramTCComponent) throws Exception
			  {
			    if (this.property != null)
			    {
			      TCProperty localTCProperty = paramTCComponent.getTCProperty(this.property);
			      return getPropertyToSave(localTCProperty);
			    }
			    this.savable = true;
				for(Entry<TCComponentForm, String> entry : modtextValues_m.entrySet()) 
				{
					((TCComponentForm)(entry.getKey())).setProperty("u4_text", entry.getValue());
				}
			   
				modtextValues_m.clear();
			    return null;
			  }
			  
	@Override
	public void save(TCProperty paramTCProperty) throws Exception
		  {
		    TCProperty localTCProperty = getPropertyToSave(paramTCProperty);
		    if ((this.savable) && (localTCProperty != null)) {
		      localTCProperty.getTCComponent().setTCProperty(paramTCProperty);
		    }
		    setDirty(false);
		  }
	
	@Override
	 public TCProperty saveProperty(TCComponent paramTCComponent)throws Exception
			  {
			    TCProperty localTCProperty = getPropertyToSave(paramTCComponent);
			    if (this.savable) {
			      return localTCProperty;
			    }
			    setDirty(false);
			    return null;
			  }

	@Override
	public TCProperty getPropertyToSave(TCProperty arg0) throws Exception {
		// TODO Auto-generated method stub
		System.out.println(" coming to get Property to Save");
		for(Entry<TCComponentForm, String> entry : modtextValues_m.entrySet()) 
		{
			((TCComponentForm)(entry.getKey())).setProperty("u4_text", entry.getValue());
		}
		modtextValues_m.clear();
		return null;
	}


	@Override
	public void load(TCPropertyDescriptor arg0) throws Exception {
		// TODO Auto-generated method stub
		//createFreeTextUI();
	}



}
