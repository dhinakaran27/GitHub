/* Source File Name:   UL4ComponentDialog
 *
 * Description:  This file contains code to build GMC Pack Component Creation UI Dialog 
 * Revision History:
 * -------------------------------------------------------------------------
 * Revision    Author                Date Created           Reason
 * -------------------------------------------------------------------------
 *  1.0.0       Dhinakaran.V          30/09/2014            Initial Creation
 *  2.0.0       Dhinakaran.V          30/07/2015            Updated to SWT Framework 
 *
 */


package com.unilever.rac.ui.newcomponent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.aifrcp.AppThemeHelper;
import com.teamcenter.rac.commands.paste.PasteOperation;
import com.teamcenter.rac.common.TCTypeRenderer;
import com.teamcenter.rac.common.create.BOCreateDefinitionFactory;
import com.teamcenter.rac.common.create.IBOCreateDefinition;
import com.teamcenter.rac.common.lov.view.controls.LOVDisplayer;
import com.teamcenter.rac.kernel.BOCreatePropertyDescriptor;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentForm;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentItemType;
import com.teamcenter.rac.kernel.TCComponentListOfValues;
import com.teamcenter.rac.kernel.TCComponentListOfValuesType;
import com.teamcenter.rac.kernel.TCComponentType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCPropertyDescriptor;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.dialog.AbstractSWTDialog;
import com.teamcenter.rac.viewer.stylesheet.viewer.Messages;
import com.unilever.rac.ui.common.UL4Common;
/**
 * The Class UL4ComponentDialog.
 */

public class UL4ComponentDialog extends AbstractSWTDialog 
{
    private TCSession session              						 = null ;    
    private Shell shell					   						 = null ;
    private TCComponentForm form 								 = null ;
    private ArrayList<String>  prop				    			 = null ;
    private TCComponent selection								 = null ;
    private org.eclipse.swt.graphics.Color color 				 = null ;
    private String[][] hiddenAttributes                          = null ;
    private FormToolkit toolkit                                  = null ;    
    private TCPropertyDescriptor tcpropertydescriptor            = null;
    private TCComponentType tccomponenttype                      = null;    
    private Button trueButton                                    = null ;
    private Button falseButton                                   = null ;
    Map <String,LOVDisplayer > propObject                        = null ;
    Map <String,String > propValue                               = null ;
    Map <String,String > realDisplayName                         = null ;
    private ArrayList<String> alMandatory		                 = null ;
    private ArrayList<String> alDialogProp		                 = null ;
    private NodeList nodes  									 = null ;
    private Map<String,String> technology_description			 = null ;
    
    private String 	COMP_CLASS                                   = "u4_component_class";
    private String 	COMP_COMM                                    = "u4_component_commodity";
    private String 	MATL_CLASS                                   = "u4_material_class"; 
    private String 	MATL_COMM                                    = "u4_material_commodity";
    private String 	LEAD_SIZE                         	         = "u4_lead_size";
    private String 	DESC                         	             = "object_desc";
    private String 	CP_WEIGHT_VOL                                = "u4_cu_weight_volume";   
    private String 	MFG                                          = "u4_manufacturing_processes"; 
    private String 	SHAPE                                        = "u4_shape"; 
    private String 	DELFORM                                      = "u4_delivered_form"; 
    private String 	FRAME                                        = "u4_pam_frame_type"; 
    private String 	TECH                                         = "u4_technology"; 
    private String 	COMP_DESC                                    = "u4_component_description"; 
    private String 	CONSUMER_UNIT                                = "u4_consumer_unit"; 
    private String 	MATL_DESC                                    = "u4_material_description"; 
    private String 	SIZE                                         = "u4_size"; 
    private String 	UDESC                                        = "u4_unique_descriptor"; 
    private String  GMCFORM                                      = "U4_MaterialClassificationForm";
    
    public UL4ComponentDialog( Shell parentShell, TCSession tcSession  , ArrayList<String> propName , String[][] hiddenAttr,  TCComponent sel, TCComponentForm frm , NodeList node)
    {
        super( parentShell );
        session = tcSession;
        shell = parentShell;
        selection = sel ;
	    form = frm ;
		color = shell.getDisplay().getSystemColor(SWT.COLOR_WHITE);
		hiddenAttributes = hiddenAttr ;		
		toolkit = AppThemeHelper.getFormToolkit();		
		propObject =  new HashMap<String, LOVDisplayer>();
		propValue =  new HashMap<String, String>();
		realDisplayName  =  new HashMap<String, String>();
		alMandatory = new ArrayList<String>(); 
		alDialogProp =new ArrayList<String>(); 
		nodes = node ;
		
		getMandatoryProp();
		
        try
        {
			tccomponenttype = session.getTypeComponent(GMCFORM);
		}
        catch (TCException e) 
        {
			e.printStackTrace();
		} 
        
        for( int inx=0 ; inx < hiddenAttributes.length ; inx++)
				alDialogProp.add(hiddenAttributes[inx][1]);	
        
        String[] technology_description_values = session.getPreferenceService().getStringValues("U4_TechnologyDescription");
        
        technology_description = new  HashMap <String,String>();
		
		if (technology_description_values !=null)
		{
			for (int inx = 0 ; inx < technology_description_values.length ; inx++)
			{
				String []str = technology_description_values[inx].trim().split("#") ;
				if(str.length == 2 )
					technology_description.put(str[0], str[1]);
			}
		}
    }

	protected Control createContents( Composite parent )
	{			
		initializeDialogUnits( parent );
        getShell().setSize( 640	, 670 );
        createDialogAndButtonArea( parent ); 
		return parent;
	}
	
	protected void createDialogAndButtonArea( Composite parent )
	{
		dialogArea = createDialogArea( parent);
        dialogArea.setLayoutData( new GridData( GridData.FILL_BOTH ) );
		buttonBar = createButtonBar( parent);
		buttonBar.setBackground(color);
		parent.setBackground(color);		
		applyDialogFont( parent );	
	}
	
	protected void createButtonsForButtonBar( Composite parent )
	{
	    createButton( parent, IDialogConstants.OK_ID, "Create", true );
	    createButton( parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, true );	
	    
	    if(alMandatory.size() > 0)
	    	super.getButton(IDialogConstants.OK_ID).setEnabled(false);
	}
	
	protected Control createDialogArea( Composite parent )
	{			
    	Composite mainComposite = ( Composite ) super.createDialogArea( parent );	
			
		try
		{
			mainComposite.setLayout(new FillLayout());		
		    ScrolledForm scrolledform = toolkit.createScrolledForm(mainComposite);
	        Form frm = scrolledform.getForm();	        
	        TCComponentItemType itemType = (TCComponentItemType)session.getTypeComponent("U4_Component");
	        Image image = TCTypeRenderer.getImage(itemType);
	        frm.setImage(image);
	        frm.setBackground( mainComposite.getBackground());
	        frm.setText("Pack Component");
	        mainComposite.getShell().setText("New Pack Component");
	        Composite main = frm.getBody();
	        main.setLayout(new GridLayout(1, true));  
        
	        Composite composite = null ;
	        
	        for(int inx=0;inx<nodes.getLength();inx++)
			{					
				Element  node = (Element) nodes.item(inx);
				
				if(node.getNodeName().toString().equalsIgnoreCase("property") && composite != null )
				{
					String propName = node.getAttribute("name");
					
					if( ! alDialogProp.contains(propName))
					{ 						
						int type = form.getTCProperty(propName).getPropertyType();
						
						alDialogProp.add(propName);
						
						switch ( type)
						{						
							case 1: // char
								
								stringCharType(composite , propName);
							
								break;
								
							case 3:  //double
								
								floatType(composite , propName);
								
								break;
								
							case 4: // float
								
								floatType(composite , propName);
								
								break;
								
							case 5:  // int
								
								number(composite , propName);
								
								break;
								
							case 6: // logical
								
								radio(composite,propName);
								
								break;
								
							case 7:  //short
								
								number(composite , propName);
								
								break;
								
							case 8: // string
								
								stringType(composite , propName);
								
								break;
								
							case 12: // note
								
								stringType(composite , propName);
								
								break;
								
							case 15: // long string
								
								stringType(composite , propName);
								
								break;
								
							default: // string
								
								stringType(composite , propName);
						}
						
					}
				}
				else if(node.getNodeName().toString().equalsIgnoreCase("section"))
				{	
					setWidth(composite);
					composite = getSectionComposite(main,  node.getAttribute("title"));
				}				
			}
	        
	        setWidth(composite);
	        
	        Composite hidden = new Composite(main, 0);
	        GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
	        hidden.setLayoutData(data);        
	        data.exclude = true ;	        
	       	        
	        for( int inx=0 ; inx < hiddenAttributes.length ; inx++)
    		{    			
    			String attribute  = hiddenAttributes[inx][1];  
    			LOVDisplayer lov = new  LOVDisplayer(hidden,0); 		
    			lov.initialize(form, getTCPropertyDescriptor(attribute), null);
    			propObject.put(attribute,lov);
    		}	        
	        
	        hidden.pack();	        
	        
	        
		}
        catch (TCException e) 
        {
			e.printStackTrace();
		}	
		
		
		return mainComposite;
	}
	
	private void setWidth(  Composite composite) throws TCException
	{
		if (composite != null)
		{
	        Label lbl = new Label(composite, 0);
	        lbl.setLayoutData(new GridData( SWT.FILL, SWT.FILL, false, false, 1, 1 ) );
	        lbl.setText("");
	        
	        labelEmpty(composite,"                                                                               ");
		}		
	}

	private Composite getSectionComposite(Composite main , String title)
	{
        Section section = createSection(main, title);
        Composite composite = toolkit.createComposite(section);
        composite.setLayout(tightGridLayout(2));
        section.setClient(composite);   
       
        return composite ;		
	}
	
	private Section createSection(Composite composite, String title)
    {
        Section section = toolkit.createSection(composite, 322);
        section.setText(title);
        GridData griddata = new GridData();
        griddata.horizontalSpan = 1;
        griddata.horizontalAlignment = 4;
        griddata.grabExcessHorizontalSpace = true;
        section.setLayoutData(griddata);
        return section;
    }
	
	
    protected void okPressed()
    {       	    	
    	String frame = propValue.get(FRAME).toString();    	
    	String tech = propValue.get(TECH).toString();
    	String cDesc = updateFormPropertyValue(COMP_COMM,"U4_GMC_Component_Abbreviation");
    	String mDesc = updateFormPropertyValue(MATL_COMM,"U4_GMC_Material_Abbreviation");
      	
    	try
    	{        	
        	if(cDesc.length() != 0 && mDesc.length() != 0 )
        	{    		
				String compType = getDesignComponentType(frame , tech);
				
	  			if(compType != null && compType.length() >= 1)
				{
					TCComponentItemType type = (TCComponentItemType) session.getTypeComponent( compType );
					
					if(type != null)
					{    				
	                    String newId = ( ( TCComponentItemType ) type ).getNewID();
	                    
	                    propValue.put(MATL_DESC,mDesc);
	                    propValue.put(COMP_DESC,cDesc);
	                    
	                    for(Map.Entry<String, String> propName : propValue.entrySet())       			
	        				form.setProperty (propName.getKey() , (String)  propValue.get(propName.getKey()));
	                    
						form.setStringProperty(UL4Common.OBJECT_NAME, newId);	
						
						form.save();	
						form.refresh();
	
						super.okPressed();
						
						TCComponentItem item  = type.create(newId, "", compType, "", propValue.get(DESC), null);						
						TCComponentItemRevision revision  = item.getLatestItemRevision() ;	
	
						//Attach Material Classification Form to Pack component Revision
						PasteOperation paste1 = new PasteOperation(AIFUtility.getCurrentApplication(),revision,form,UL4Common.GMCFORMRELATION);	
						paste1.executeOperation();						
						
						//Attach Pack Component to Selected Project Revision
						PasteOperation paste2 = new PasteOperation(AIFUtility.getCurrentApplication(),selection,revision,UL4Common.DEVRELATION);	
						paste2.executeOperation();	
						
						revision.lock();
						revision.setStringProperty("u4_cp_component_description",propValue.get(COMP_DESC));
						revision.save();
						revision.unlock();
					}
					else
					{
						MessageBox.post(shell, "Unknown Pack Component Type Not Found." , "Error", MessageBox.ERROR);
					}
				}
				else
				{
					String str = "\nFor selected Pam Frame Type ( " +frame+ ") & Technology ( "+tech+ " )";
					MessageBox.post(shell,"Pack Component Type Missing in Preference." + str ,"Error", MessageBox.ERROR);
				}	
        	}
			else
			{
				if(mDesc.length() == 0 )
					MessageBox.post(shell,"Material Description Value is Missing in Preference <U4_GMC_Material_Abbreviation>\n." ,"Error", MessageBox.ERROR);
				else
					MessageBox.post(shell,"Component Description Value is Missing in Preference <U4_GMC_Component_Abbreviation>\n."  ,"Error", MessageBox.ERROR);
			}
			
		} 
    	catch (TCException e)
    	{			
    		MessageBox.post(shell,e.getMessage() ,"Error", MessageBox.ERROR);
		} 
    }
    
    
    private String updateFormPropertyValue(String str ,String preference)
	{	
    	String commodity = propValue.get(str).toString();
    	
    	@SuppressWarnings("deprecation")
		String[] prefValue = session.getPreferenceService().getStringArray(TCPreferenceService.TC_preference_site, preference);
		
		for ( int i=0 ; i<prefValue.length;i++)
		{	
			String []val = prefValue[i].trim().split("#") ;
	
			if(val.length ==2 )
				if(commodity.equalsIgnoreCase(val[0]) )
					return val[1];
		}
		
		return "";
	}
    
    private String getDesignComponentType(String frame , String tech) throws TCException
	{	
		
		@SuppressWarnings("deprecation")
		String[] prefValue = session.getPreferenceService().getStringArray(TCPreferenceService.TC_preference_site, UL4Common.PREF_GMCPACKCOMPONENT);
		
		for ( int i=0 ; i<prefValue.length;i++)
		{	
			String []str = prefValue[i].trim().split("#") ;
			
			if(str.length == 3 &&  str[0].equalsIgnoreCase(frame) && str[1].equalsIgnoreCase(tech))
				return str[2] ;					
		}
		
		return null ;
	}
	
	private void separator(Composite composite) 
	{
	    Label separator1 = new Label(composite, SWT.HORIZONTAL | SWT.SEPARATOR | SWT.SHADOW_IN);
	    separator1.setLayoutData(new GridData( SWT.FILL, SWT.NORMAL, true, false,1, 1 ) );
	}
	
	private TCPropertyDescriptor getTCPropertyDescriptor(String prop) throws TCException
	{
		return tccomponenttype.getPropertyDescriptor(prop);
	}
	
	private String getName(String prop) throws TCException
	{
		return tccomponenttype.getPropertyDescriptor(prop).getUiName();
	}
	
	private void label(Composite composite , String name)  throws TCException
	{		
		if(alMandatory.contains(name))
		{
			setMandatory(composite,getName(name));			
		}
		else
		{		
	        Label lbl = new Label(composite, 0);
	        lbl.setLayoutData(new GridData( SWT.LEFT, SWT.NORMAL, false, false, 1, 1 ) );
	        lbl.setText(getName(name));	
		}
	}
	
	private void labelEmpty(Composite composite , String name)  throws TCException
	{
        Label lbl = new Label(composite, 0);
        lbl.setLayoutData(new GridData( SWT.LEFT, SWT.NORMAL, false, false, 1, 1 ) );
        lbl.setText(name);		
	}
	
	private void radio(Composite composite , String name)  throws TCException
	{
		label(composite,name);
		Composite radio = composite ;
		radio = toolkit.createComposite(composite, 0);
        GridLayout gridlayout = new GridLayout();
        gridlayout.numColumns = 2;
        radio.setLayout(gridlayout);
        GridData griddata = new GridData();
        griddata.horizontalSpan =1;
        radio.setLayoutData(griddata);
        trueButton = toolkit.createButton(radio, Messages.getString("StylesheetRenderingFormProvider.true"), 16);
        trueButton.setData("StylesheetRenderingFormProvider.true");
        trueButton.setData("name", name);
        falseButton = toolkit.createButton(radio, Messages.getString("StylesheetRenderingFormProvider.false"), 16);
        falseButton.setData("StylesheetRenderingFormProvider.false");  
        falseButton.setData("name", name);
        
        propValue.put(name, "False");
        
        if (name.compareToIgnoreCase("u4_consumer_unit")!=0)
        {
        	falseButton.setSelection(true);
        }
        
        trueButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent selectionevent)
            {            	
            	 Button t = (Button)selectionevent.widget;
             	 propValue.put((String) t.getData("name"),"True");
             	 
             	 enableCreateButton() ;
            }
        });
        
        falseButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent selectionevent)
            {
            	Button t = (Button)selectionevent.widget;
         	    propValue.put((String) t.getData("name"),"False");
         	    
         	    enableCreateButton() ;
            }
        });	
	}
	
	private void lov(Composite composite , String name)  throws TCException
	{		
		label(composite,name);
        final LOVDisplayer lov = new  LOVDisplayer(composite,0); 		
        lov.initialize(form, getTCPropertyDescriptor(name), null);        
        lov.setLayoutData( new GridData( SWT.FILL, SWT.FILL, false, true, 1, 1 ) );  
        lov.setData("name", name);
        propObject.put(name,lov);
        propValue.put(name, "");
        
        lov.addPropertyChangeListener(new IPropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent event)
            {
        		//String value  =  event.getNewValue().toString();        		
            	String value  =  lov.getSelectedDisplayValue();
        		String attribute = event.getProperty().toString();
        		propValue.put(attribute,value);

        		if(attribute.equalsIgnoreCase(TECH) && value.length() > 0)
        		{
        			try {
        				String frame = propValue.get(FRAME).toString();    	
						String compType = getDesignComponentType(frame , value);
						
						@SuppressWarnings("deprecation")
			    		String[] prefValue = session.getPreferenceService().getStringArray(TCPreferenceService.TC_preference_site, UL4Common.PREF_PRIMARY_PACK_TYPE);
						
						boolean found = false;
						
						for ( int i=0 ; i<prefValue.length;i++)
						{	
							if( prefValue[i].equalsIgnoreCase(compType))
							{
								trueButton.setSelection(true);
								falseButton.setSelection(false);
								propValue.put(CONSUMER_UNIT,"True");
								found=true;
								break;
							}
						}
						
						if (found == false)
						{
							trueButton.setSelection(false);
							falseButton.setSelection(true);
							propValue.put(CONSUMER_UNIT,"False");
						}
					} catch (TCException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
        		}
        		
        		if(attribute.equalsIgnoreCase(TECH) && value.length() > 0 )
        		{
        			if(technology_description.containsKey(value) == true)
        			{
        				String desc_value = technology_description.get(value);
        				MessageBox.post(shell, desc_value , "Information", MessageBox.INFORMATION);
        			}
        		}
        		
        		
        		String frame = "G-PAM-COPACK";
        		
        		if(attribute.equalsIgnoreCase(FRAME) && value.length() > 0 && value.equalsIgnoreCase(frame) )
        		{				
        			TCComponentListOfValues topLevelLOV = null ;
        			String []values = new String[5] ;
        			
        			for(Map.Entry<String, LOVDisplayer> propName : propObject.entrySet()) 
        			{										
    					if(propName.getKey().toString().equalsIgnoreCase(COMP_CLASS))
      						values[0] = propValue.get(propName.getKey()).toString();
    					else if(propName.getKey().toString().equalsIgnoreCase(COMP_COMM)  )    					
    						values[1] = propValue.get(propName.getKey()).toString();    				
    					else if(propName.getKey().toString().equalsIgnoreCase(MATL_CLASS) )    				
    						values[2] = propValue.get(propName.getKey()).toString();    					
    					else if(propName.getKey().toString().equalsIgnoreCase(MATL_COMM)  )    					
    						values[3] = propValue.get(propName.getKey()).toString();    					
    					else if(propName.getKey().toString().equalsIgnoreCase(DELFORM) )    					
    						values[4] = propValue.get(propName.getKey()).toString();  
    					
    					try
    					{
    						if(propName.getKey().toString().equalsIgnoreCase(FRAME))
    							topLevelLOV = form.getTCProperty(COMP_CLASS).getLOV();
    					}
    					catch (TCException e1)
    					{        						
    						e1.printStackTrace();
    					}    					
        			}
        			
        			if(topLevelLOV  != null)
        			{	
        				String childlovname =topLevelLOV.toString() ;
        				
        				for( int ina=0 ; ina < 5 ; ina++)
        				{					
        					if(childlovname != null )
        						childlovname = getChildLOVName(childlovname , values[ina]);
        				}
        				
        				if(childlovname != null )
        				{
        					TCComponentListOfValues lov = TCComponentListOfValuesType.findLOVByName(childlovname);	
        					
        					try
        					{
        						List<String> stringList = new ArrayList<String>(Arrays.asList(lov.getListOfValues().getStringListOfValues()));
        						
        						if(stringList.contains(frame))
        							stringList.remove(frame);
        						
        						if(stringList.size() > 0)
        							value = stringList.get(0);					
        						
        					} 
        					catch (TCException e1)
        					{        						
        						e1.printStackTrace();
        					}
        				}
        			}	
        		}
        		
        		for( int inx=0 ; inx < hiddenAttributes.length ; inx++)
        		{
        			String source  = hiddenAttributes[inx][0];
        			String parent  = hiddenAttributes[inx][1];
        			String target  = hiddenAttributes[inx][2];

        			if(source.equalsIgnoreCase(attribute))
        			{	
        				for(Map.Entry<String, LOVDisplayer> propName : propObject.entrySet()) 
							if (propName.getKey().equalsIgnoreCase(target))		
							{
								propObject.get(parent).setSelectedValue(value);
								propObject.get(target).setSelectedValue("");								
								propValue.put(parent, value);
								propValue.put(target, "");							
								
							}
        			}
        		}
        		
        		enableCreateButton() ;
            }
        });     
	}
	
	private void enableCreateButton()
	{
		boolean flag = true ;
		
		for(Map.Entry<String, String> propName : propValue.entrySet()) 
			if(alMandatory.contains(propName.getKey()))
				if(propValue.get(propName.getKey()).length() == 0)
				{
					flag = false ;	
					break ;
				}
		
		super.getButton(IDialogConstants.OK_ID).setEnabled(flag);
		
	}
	
	private void text(Composite composite , String name)  throws TCException
	{	
		label(composite,name);
		final Text text = new Text(composite, SWT.NORMAL | SWT.BORDER);
		GridData data = new GridData();		
		data.widthHint = 240 ;		
		text.setLayoutData(data);
		text.setData("name", name);
		setTextLength(text,name);
		propValue.put(name,"");
		        
        // BAU AD CR 87 : Max length of Size is 11
		
		if(name.equalsIgnoreCase("u4_size"))
			text.setTextLimit(11);
		
	    text.addModifyListener(new ModifyListener() {     
			public void modifyText(ModifyEvent event) { 
				
		    	  Text t = (Text)event.widget;
		    	  String value  =  t.getText();		    	  
		    	  propValue.put((String) t.getData("name"), value);
		    	  
	         	  enableCreateButton() ;
			}      
		});	
		
	}
	
	private void number(Composite composite , String name)  throws TCException
	{	
		label(composite,name);
		final Text text = new Text(composite, SWT.NORMAL | SWT.BORDER);
		GridData data = new GridData();		
		data.widthHint = 240 ;		
		text.setLayoutData(data);
		text.setData("name", name);
		propValue.put(name,"");
		
		text.addListener(SWT.Verify, new Listener() {
		      public void handleEvent(Event e) {
		        String string = e.text;
		        Text t = (Text)e.widget;
		    	String value  =  t.getText();	
		        char[] chars = new char[string.length()];
		        string.getChars(0, chars.length, chars, 0);
		        for (int i = 0; i < chars.length; i++) {
		          if (!('0' <= chars[i] && chars[i] <= '9'))
		          {
		            e.doit = false;
		            return;
		          }
		          else
		          {
			    	  propValue.put((String) text.getData("name"), value);			    	  
		         	  enableCreateButton() ;
		          }
		        }
		      }
		    });	  
	}
	
	private void floatType(Composite composite , String name)  throws TCException
	{	
		label(composite,name);
		final Text text = new Text(composite, SWT.NORMAL | SWT.BORDER);
		GridData data = new GridData();		
		data.widthHint = 240 ;		
		text.setLayoutData(data);	
		text.setData("name", name);
		propValue.put(name,"");
		
		text.addListener(SWT.Verify, new Listener() {
		      public void handleEvent(Event e) {
		        String string = e.text;
		        Text t = (Text)e.widget;
		    	String value  =  t.getText();		    	  
		        char[] chars = new char[string.length()];
		        string.getChars(0, chars.length, chars, 0);
		        for (int i = 0; i < chars.length; i++) {
		          if (  ( chars[i]   >= '0' && chars[i] <= '9' )  ||  (StringUtils.countMatches(value,".") == 0 && chars[i] == '.'  ))
		          {
		        	  propValue.put((String) text.getData("name"), value);			    	  
		         	  enableCreateButton() ;
		          }
		          else
		          {
			            e.doit = false;
			            return;
		          }
		        }
		      }
		    });	  
	}
	
	private void setTextLength(Text text , String prop) throws TCException
	{
		text.setTextLimit(tccomponenttype.getPropertyDescriptor(prop).getMaxLength());
	}
	private void multiText(Composite composite , String name)  throws TCException
	{

		label(composite,name);        
		final Text text = new Text(composite, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL|SWT.H_SCROLL);
		GridData data = new GridData();		
		data.heightHint = 75;
		data.widthHint = 240 ;		
		text.setLayoutData(data);	
		text.setData("name", name);
		setTextLength(text,name);
		propValue.put(name,"");
		
	    text.addModifyListener(new ModifyListener() {     
			public void modifyText(ModifyEvent event) { 
				
		    	  Text t = (Text)event.widget;
		    	  String value  =  t.getText();
		    	  propValue.put((String) t.getData("name"), value);
		    	  
	         	  enableCreateButton() ;
			}      
		});

	}

    public static void setMandatory(Composite composite , String PropValue)
    {    
		Display display = AIFUtility.getActiveDesktop().getShell().getDisplay() ;
		StyledText txt = new StyledText(composite, 0);
		txt.setLayoutData(new GridData( SWT.LEFT, SWT.NORMAL, false, false, 1, 1 ) );
	    txt.setText(PropValue+"*");		
	    Color red = display.getSystemColor(SWT.COLOR_RED);	
	    StyleRange style = new StyleRange();
	    style.start = PropValue.length();
	    style.length = 1 ;
	    style.foreground = red;
	    txt.setStyleRange(style);	
	    txt.setEditable(false);
	    txt.setEnabled(false);
    }
        
    private void getMandatoryProp()
    {
        boolean mandatory = false ;
        alMandatory.clear();
    	
    	IBOCreateDefinition createDefinition = BOCreateDefinitionFactory.getInstance().getCreateDefinition(session, GMCFORM);
        List<BOCreatePropertyDescriptor> list = createDefinition.getCreatePropertyDescriptors();
        ArrayList<BOCreatePropertyDescriptor> formProps =  new ArrayList<BOCreatePropertyDescriptor>();
        formProps.clear();
	                
    	if(list != null && list.size() > 0)
        {			                
            Iterator<BOCreatePropertyDescriptor> iterator = list.iterator();
            
            do
            {
                if(!iterator.hasNext())
                    break;
                
                formProps.add(iterator.next());                
            }
            while(true) ; 
        }
    	
    	for ( int inx= 0 ; inx <formProps.size() ; inx++ )
    	{	 
    		 BOCreatePropertyDescriptor bocreatepropertydescriptor1 = formProps.get(inx);
             TCPropertyDescriptor tcpropertydescriptor1 = bocreatepropertydescriptor1.getPropertyDescriptor();
             mandatory = tcpropertydescriptor1.isRequired();   
	           
             if(mandatory)
             {
            	 alMandatory.add(tcpropertydescriptor1.getName());
            	 realDisplayName.put( tcpropertydescriptor1.getName()  , tcpropertydescriptor1.getDisplayName() );
             	 mandatory = false ;
             }
         }    	
    } 
    
    private String  getChildLOVName(String lovname , String value)
	{
		
		String childlovname = null ;		

		try 
		{	
			TCComponentListOfValues lov = TCComponentListOfValuesType.findLOVByName(lovname);	
			List<String> stringList = new ArrayList<String>(Arrays.asList(lov.getListOfValues().getStringListOfValues())); 					
			TCComponentListOfValues[] children = lov.getListOfFilters();					
	        childlovname = children[stringList.indexOf(value)].toString();							
		}
		catch (TCException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		return childlovname;
		
	}
    
	protected boolean isResizable()
	{
	    return true;
	}
	
	private  GridLayout tightGridLayout(int column) 
	{
		GridLayout localGridLayout = new GridLayout();
		localGridLayout.numColumns = column;
		localGridLayout.marginBottom = 0;
		localGridLayout.marginHeight = 5;
		localGridLayout.marginLeft = 10;
		localGridLayout.marginRight = 10;
		localGridLayout.marginTop = 5;
		localGridLayout.marginWidth = 5;
		localGridLayout.horizontalSpacing = 10;
		localGridLayout.verticalSpacing = 5;

		
		return localGridLayout;
	}
	
	private void stringCharType(Composite composite , String prop) throws TCException
	{
		label(composite,prop);
		final Text text = new Text(composite, SWT.NORMAL | SWT.BORDER);
		GridData data = new GridData();		
		data.widthHint = 50 ;		
		text.setLayoutData(data);
		text.setData("name", prop);
		text.setTextLimit(1);;
		propValue.put(prop,"");
		
	    text.addModifyListener(new ModifyListener() {     
			public void modifyText(ModifyEvent event) { 
				
		    	  Text t = (Text)event.widget;
		    	  String value  =  t.getText();		    	  
		    	  propValue.put((String) t.getData("name"), value);
		    	  
	         	  enableCreateButton() ;
			}      
		});
	}
	
	private void stringType(Composite composite , String prop) throws TCException
	{
		TCPropertyDescriptor str = getTCPropertyDescriptor(prop);
		
		if(str.getLOV() != null  && ! (str.isArray()))
		{
			lov(composite,prop);
		}
		else if(str.getLOV() != null  && (str.isArray() ))
		{
			checkboxType(composite,prop);
		}
		else
		{
			int length = tccomponenttype.getPropertyDescriptor(prop).getMaxLength();
			
			if(length <= 128)
				text(composite,prop);
			else
				multiText(composite,prop);
		}
	}
	
	private void checkboxType(Composite composite , final String prop) throws TCException
	{		
		
		TCComponentListOfValues lov  = getTCPropertyDescriptor(prop).getLOV();
		String values[] = lov.getListOfValues().getStringListOfValues();
	
    	Label lbl = new Label(composite, 0);
        lbl.setLayoutData(new GridData( SWT.LEFT, SWT.NORMAL, false, false, 1, values.length ) );
        lbl.setText(getName(prop));
		
		propValue.put(prop,"");
	    
	    for ( int inx = 0 ; inx < values.length ; inx++ )
	    {
	    	Button button = new Button(composite, SWT.CHECK);
	    	button.setText(values[inx]);	
	    	button.setLayoutData(new GridData( SWT.FILL, SWT.FILL, false, false,1, 1));
			
			SelectionListener selectionListener = new SelectionAdapter ()
			    {        
			    	public void widgetSelected(SelectionEvent event)
			        {   
			    		Button button = ((Button) event.widget);      
			    		String val = button.getText();			    		
			    		String oldValue = propValue.get(prop);
			    		
			    		if(button.getSelection())
			    		{	
				    		if( ! oldValue.contains(val))
				    			propValue.put(prop, oldValue + val + ";");			    		
			    		}
			    		else
			    		{
			    			if( oldValue.contains(val))
			    				oldValue = replace(val+";", "" , oldValue);
			    			
			    			propValue.put(prop, oldValue);		
			    		}			    		
			    	};    
			    };
			    
			 button.addSelectionListener(selectionListener);
	    }    
	    
	}
	
	public static String replace(String oldStr, String newStr, String inString) {
	    int start = inString.indexOf(oldStr);
	    if (start == -1) {
	      return inString;
	    }
	    StringBuffer sb = new StringBuffer();
	    sb.append(inString.substring(0, start));
	    sb.append(newStr);
	    sb.append(inString.substring(start+oldStr.length()));
	    return sb.toString();
	  }
}