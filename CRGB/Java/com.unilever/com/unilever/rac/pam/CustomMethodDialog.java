/**===================================================================================
 *             
 *                     Unpublished - All rights reserved
 * ===================================================================================
 * File Description: CustomMethodDialog.java
 * This class is used to initiate Test Method Condition dialog, where in user can 
 * select Test Methods and appropriate conditions for further use.
 * 
 * ===================================================================================
 * Revision History 
 * ===================================================================================
 * Date         	Name       			  TCEng-Release  	Description of Change
 * ------------   --------------------	  -------------    ---------------------------
 * 29-Jan-2015	  Jayateertha M Inamdar   TC10.1.3        Initial Version
 * 
 *  $HISTORY$
 * ===================================================================================*/

package com.unilever.rac.pam;

/**
 * @author j.madhusudan.inamdar
 *
 */
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.Vector;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentQuery;
import com.teamcenter.rac.kernel.TCComponentQueryType;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.kernel.TCSession;

public class CustomMethodDialog extends TitleAreaDialog 
{

  private Combo 	txtMethodCombo;
  private Combo 	conditionSetCombo;
  private Text 		descriptionText;
  
  private String 	strMethod;
  private String 	strDescription;
  private String 	strConditionSet;
  private String 	sectionAndPropName;
  private String 	strPropertyName;
  
  private String[] 	strArrMethodValues;
  private TCSession session;
  
  private HashMap<String, String> condtionSet;
  
  private PAMSecondaryPropValue currentSecPropValues;
  
  private TCComponentItemRevision pamSpecRevision;

  private static final String SECTION_AND_PROPERTY_LABEL_INFORMATION_CONSTANT 			= "Section : Property";
  private static final String TEST_METHOD_LABEL_CONSTANT 								= "Method";
  private static final String TEST_METHOD_FORM_DESCRIPTION_CONSTANT 					= "Description";
  private static final String TEST_METHOD_CONDITION_SET_CONSTANT 						= "Condition Set";
  private static final String IMAN_QUERY_NAME_CONSTANT 									= "ImanQuery";
  private static final String TEST_METHOD_QUERY_NAME_CONSTANT 							= "__U4_TestMethod...";
  private static final String QRY_CONSTRAINT_TYPE_CONSTANT 								= "Type";
  private static final String QRY_CONSTRAINT_METHOD_NAME_CONSTANT 						= "u4_method_name";
  private static final String QRY_CONSTRAINT_APP_PROPS_CONSTANT 						= "u4_applicable_properties";
  private static final String QRY_CONSTRAINT_TYPE_VALUE_CONSTANT 						= "Test Method";
  private static final String CONDITIONS_PROPERTY_CONSTANT 								= "u4_conditions";
  private static final String OBJECT_DESC_PROPERTY_CONSTANT 							= "object_desc";
  
  boolean pml2pam = false ;
  private Hashtable<String, Vector<String>> attrTMvalues = new Hashtable<String, Vector<String>>(); 
  private Hashtable<String, TMDetails> TMData = new Hashtable<String, TMDetails>();  

  public CustomMethodDialog(Shell parentShell,  TCSession session,PAMSecondaryPropValue currentSecPropValues,TCComponentItemRevision pamSpecRevision, 
		  																		String sectionAndPropName, String conditionValue, String methodValue  ,
		  																		boolean lbllit, String propertyValue  ) {
    super(parentShell);
    this.session 			= session;
    this.pamSpecRevision 	= pamSpecRevision;
    this.sectionAndPropName = sectionAndPropName;
    this.strConditionSet 	= conditionValue;
    this.strMethod 			= methodValue;
    this.currentSecPropValues = currentSecPropValues;
    this.pml2pam  =  lbllit ;
    this.strPropertyName = propertyValue;
    this.strDescription = "";
    strArrMethodValues = getTestMethodValues(propertyValue);

  }
  
  public CustomMethodDialog(Shell parentShell,  TCSession session,PAMSecondaryPropValue currentSecPropValues,TCComponentItemRevision pamSpecRevision, 
			String sectionAndPropName, String conditionValue, String methodValue  ,
			boolean lbllit, String propertyValue   , Hashtable<String, Vector<String>> attrTMvalues ,  Hashtable<String, TMDetails> TMData ) {
		super(parentShell);
		this.session 			= session;
		this.pamSpecRevision 	= pamSpecRevision;
		this.sectionAndPropName = sectionAndPropName;
		this.strConditionSet 	= conditionValue;
		this.strMethod 			= methodValue;
		this.currentSecPropValues = currentSecPropValues;
		this.pml2pam  =  lbllit ;
		this.strPropertyName = propertyValue;
		this.strDescription = "";
		this.attrTMvalues = attrTMvalues ;
		this.TMData =  TMData ;		
		Vector tm = attrTMvalues.get(propertyValue);
		if(tm!=null) 
			strArrMethodValues = (String[]) tm.toArray(new String[tm.size()]);
}

  public boolean isOKPressed()
	{
		return true;
		
	}

  @Override
  protected void configureShell(Shell newShell) 
  {
    super.configureShell(newShell);
    newShell.setText("Test Method Details");
  }
  
	public boolean isClearPressed()
	{
		return true;
	}
	
  @Override
  public void create() 
  {
    super.create();
    setTitle(SECTION_AND_PROPERTY_LABEL_INFORMATION_CONSTANT);
    setMessage("" + sectionAndPropName, IMessageProvider.INFORMATION);
  }

  @Override
  protected Control createDialogArea(Composite parent) {
    Composite area = (Composite) super.createDialogArea(parent);
    Composite container = new Composite(area, SWT.NONE);
    container.setLayoutData(new GridData(GridData.FILL_BOTH));
    GridLayout layout = new GridLayout(2, false);
    container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    container.setLayout(layout);

    createMethod(container);
    createDescription(container);
    createConditionSet(container);    
        
    txtMethodCombo.addSelectionListener(new SelectionAdapter() 
    {
	      public void widgetDefaultSelected(SelectionEvent event) 
	      {
	    	  //Enable the condition-set field and populate Description + Condition-set fields.
	    	  String testmethod = (((Combo) (event.getSource())).getText());	
	    	  TMDetails tmdata = TMData.get((((Combo) (event.getSource())).getText()));
	    	  if(tmdata != null )	    		  
	    	  {
	    		  strDescription = tmdata.desc ;
	    		  descriptionText.setText(strDescription);
	       		  conditionSetCombo.setItems((tmdata.csets).toArray(new String[(tmdata.csets).size()]));
	       		  conditionSetCombo.add("", 0);
 	    	  }
	    	  else
	    		  conditionSetCombo.setText("");
	    	 
	      }
	      
	      public void widgetSelected(SelectionEvent e) 
	      {
	    	  //Enable the condition-set field and populate Description + Condition-set fields.

	    	  String testmethod = (((Combo) (e.getSource())).getText());	
	    	  TMDetails tmdata = TMData.get((((Combo) (e.getSource())).getText()));
	    	  if(tmdata != null )	    		  
	    	  {
	    		  descriptionText.setText(tmdata.desc);
	       		  conditionSetCombo.setItems((tmdata.csets).toArray(new String[(tmdata.csets).size()]));
	       		  conditionSetCombo.add("", 0);
 	    	  }
	    	  else
	    		  conditionSetCombo.setText("");
	      }
	});
        
    return area;
  }
  
  private void createMethod(Composite container) 
  {
    Label lbtMethod = new Label(container, SWT.NONE);
    lbtMethod.setText(TEST_METHOD_LABEL_CONSTANT);
    GridData dataMethod = new GridData();
    dataMethod.grabExcessHorizontalSpace = true;
    dataMethod.horizontalAlignment = GridData.FILL;
    txtMethodCombo = new Combo(container, SWT.READ_ONLY | SWT.BORDER_SOLID);
    txtMethodCombo.setBounds(50, 50, 150, 75);
    
    if(strArrMethodValues != null )
    {
    	if(this.strMethod != null && !("".equals(this.strMethod)))
    	{
    		txtMethodCombo.setItems(strArrMethodValues);
    		txtMethodCombo.select(Arrays.asList(strArrMethodValues).indexOf(this.strMethod));    		
    		TMDetails tmdata = TMData.get(strMethod);    		
    		if(tmdata != null )	    		  
    			strDescription= tmdata.desc;
    	}
    	else
    	{
    		txtMethodCombo.setItems(strArrMethodValues);
    	}
    }
    else
    {
    	if(this.strMethod != null && !("".equals(this.strMethod)))
    	{
    		txtMethodCombo.add(this.strMethod, 0);
    		txtMethodCombo.setText(this.strMethod);
    	}
    }
    
    txtMethodCombo.add("", 0);
    
    System.out.println(txtMethodCombo.getItemCount());

    txtMethodCombo.setLayoutData(dataMethod);
    /**  Added as part of CR#21 - Fix for enabling the note button ( and disabling note dialog ) during non-checkout  */
   	if(!pamSpecRevision.isCheckedOut() )
   	{
		txtMethodCombo.removeAll();
		txtMethodCombo.add(this.strMethod);
		txtMethodCombo.setText(this.strMethod);
   		txtMethodCombo.setEnabled(false);
   	}
   	
	if(pml2pam)
		txtMethodCombo.setEnabled(false);

  }
  
  private void createDescription(Composite container) 
  {
	    Label lbtDescription = new Label(container, SWT.BORDER_SOLID);
	    lbtDescription.setText(TEST_METHOD_FORM_DESCRIPTION_CONSTANT);

	    GridData dataDescription = new GridData();
	    dataDescription.grabExcessHorizontalSpace = true;
	    dataDescription.grabExcessVerticalSpace = true;
	    dataDescription.horizontalAlignment = GridData.FILL;
	    dataDescription.verticalAlignment = GridData.FILL;	    
	    
	    descriptionText = new Text(container, SWT.BORDER | SWT.WRAP | SWT.MULTI);
	    descriptionText.setBounds(50, 75, 150, 250);
	    descriptionText.setLayoutData(dataDescription);
	    descriptionText.setEnabled(false);
	    descriptionText.setText(strDescription);
	    
	    /**  Added as part of CR#21 - Fix for enabling the note button ( and disabling note dialog ) during non-checkout  */
	    
	   	if(!pamSpecRevision.isCheckedOut())
	   	{
	   		descriptionText.setEnabled(false);
	   	}
	   	
	   	if(pml2pam)
	   		descriptionText.setEnabled(false);
	  }
  
  private void createConditionSet(Composite container) {
    Label lbtConditionSet = new Label(container, SWT.NONE);
    lbtConditionSet.setText(TEST_METHOD_CONDITION_SET_CONSTANT);
    
    GridData dataConditionSet = new GridData();
    dataConditionSet.grabExcessHorizontalSpace = true;
    dataConditionSet.horizontalAlignment = GridData.FILL;
    conditionSetCombo = new Combo(container, SWT.READ_ONLY | SWT.BORDER_SOLID);
    conditionSetCombo.setBounds(50, 100, 150, 75);
    conditionSetCombo.setLayoutData(dataConditionSet); 
    TMDetails tmdata = null  ;
    
    if(this.strMethod != null && !("".equals(this.strMethod)))
    	tmdata = TMData.get(strMethod);	
		if(tmdata != null )	    		  
		{
			if(descriptionText != null)
				descriptionText.setText(tmdata.desc);
		    conditionSetCombo.setItems((tmdata.csets).toArray(new String[(tmdata.csets).size()]));  		  
	   	    if(this.strConditionSet != null && !("".equals(this.strConditionSet)))
	    		 conditionSetCombo.select((tmdata.csets).indexOf(this.strConditionSet));

		}
	    else
	    {
	    	//conditionSetCombo.add(this.strConditionSet, 0);
	    	conditionSetCombo.setText(this.strConditionSet);
	    }
	
	conditionSetCombo.add("", 0);

    
    /**  Added as part of CR#21 - Fix for enabling the note button ( and disabling note dialog ) during non-checkout  */
   	if(!pamSpecRevision.isCheckedOut()  )
   	{
   		conditionSetCombo.removeAll();
   		conditionSetCombo.add(this.strConditionSet);
   		conditionSetCombo.setText(this.strConditionSet);
   		conditionSetCombo.setEnabled(false);
   	}
   	
   if(pml2pam)
   		conditionSetCombo.setEnabled(false);  
   	
  }

  @Override
  protected boolean isResizable() {
    return true;
  }

  public Map<String,String> getConditionValues(String methodName,String propertyValues)
	{
		TCComponentQueryType queryType;
		TCComponent[] resultTestMethodForms = null;
  
  	try
		{
			queryType = ( TCComponentQueryType )session.getTypeComponent( IMAN_QUERY_NAME_CONSTANT);
			TCComponentQuery query = ( TCComponentQuery )queryType.find( TEST_METHOD_QUERY_NAME_CONSTANT );
			
			String[] names = {QRY_CONSTRAINT_TYPE_CONSTANT,QRY_CONSTRAINT_METHOD_NAME_CONSTANT,QRY_CONSTRAINT_APP_PROPS_CONSTANT};
			String[] values ={QRY_CONSTRAINT_TYPE_VALUE_CONSTANT,methodName,propertyValues};
			resultTestMethodForms =  query.execute(names,values );
			if( resultTestMethodForms != null && resultTestMethodForms.length > 0 )
		    {		
					Map<String,String> conditionSets = new HashMap<String,String>();									
					TCProperty prop = resultTestMethodForms[0].getTCProperty(CONDITIONS_PROPERTY_CONSTANT);
					descriptionText.setText(resultTestMethodForms[0].getProperty(OBJECT_DESC_PROPERTY_CONSTANT));					
					String[] strarray  = prop.getStringValueArray();					
					
					for(int inx=0;inx<strarray.length;inx++)
					{
						String[] tempconditionsArray = SplitUsingTokenizer(strarray[inx], "-");
							
						if(conditionSets.containsKey(tempconditionsArray[0]))
						{
							String prevConditionVal = conditionSets.get(tempconditionsArray[0]);
							String currentConditionVal = prevConditionVal+ ","+tempconditionsArray[1];
							conditionSets.remove(tempconditionsArray[0]);
							conditionSets.put(tempconditionsArray[0],currentConditionVal);
						}						
							
						else
							conditionSets.put(tempconditionsArray[0], tempconditionsArray[1]);
					}					
				
					return conditionSets;
		    }
		} 
  	
  	
		catch (Exception e) {		
			e.printStackTrace();
		}
		return null;
		
	
	}

  public static String[] SplitUsingTokenizer(String subject, String delimiters) {
		   StringTokenizer strTkn = new StringTokenizer(subject, delimiters);
		   ArrayList<String> arrLis = new ArrayList<String>(subject.length());

		   while(strTkn.hasMoreTokens())
		      arrLis.add(strTkn.nextToken());

		   return arrLis.toArray(new String[0]);
		}

  @Override
  protected void okPressed() 
  {  
		if( !pml2pam  &&  pamSpecRevision.isCheckedOut() )
		{	  
			  setMethod(txtMethodCombo.getText());
			  setConditions(conditionSetCombo.getText());
			  setDescription(descriptionText.getText());
		}
		
	  super.okPressed();
  }

  public void setMethod(String currMethod){
		strMethod = currMethod;
	}
  
  public void setDescription(String currDescription){
		strDescription = currDescription;
	}
  
  public void setConditions(String currConditions){
	  strConditionSet = currConditions;
	}
  
  public String getMethod() {
    return strMethod;
  }

  public String getDescription() {
	    return strDescription;
	  }
  
  public String getConditionSet() {
    return strConditionSet;
  }
  
  public String getConditionNumber()
  {
	  String strConditionNumber = null;
	  //<Jayateertha: 17-Jun-2015> Fixed for NullPointerException.
	  if(condtionSet != null)
	  {
			for (Entry<String, String> entry : condtionSet.entrySet()) {
		        if ( getConditionSet().equals(entry.getValue())) {
		        	strConditionNumber =  entry.getKey();
		        	break;
		        }
			}
		  
	  }
		
	  return strConditionNumber;  
  }
  
  public String[] getTextMethodValues()
  {
	  return strArrMethodValues;
  }
  
  public String[] getTestMethodValues(String propertyValues)
	{	
		TCComponentQueryType queryType;
  	TCComponent[] resultTestMethodForms = null;
  
  	try
		{
			queryType = ( TCComponentQueryType )session.getTypeComponent( "ImanQuery" );
			TCComponentQuery query = ( TCComponentQuery )queryType.find( "__U4_TestMethod..." );
			
	        System.out.println(propertyValues);
			
			String[] names = {"Type","u4_applicable_properties"};
			String[] values ={"Test Method",propertyValues,};
			resultTestMethodForms =  query.execute(names,values );
			
			 System.out.println(propertyValues + "  count " + resultTestMethodForms.length);
			if( resultTestMethodForms != null && resultTestMethodForms.length > 0 )
		    {
				String[] FormNames = new String[resultTestMethodForms.length+1];
				for(int inx=0;inx<resultTestMethodForms.length;inx++)
				{					
					FormNames[inx] = resultTestMethodForms[inx].getPropertyDisplayableValue("u4_method_name");
				}
				/** fix for the defect 166 to allow empty values*/
				FormNames[resultTestMethodForms.length]="";
				
				/** CR 14 testmethod sort order */
				Arrays.sort(FormNames,String.CASE_INSENSITIVE_ORDER);
				
				return FormNames;
		    }
		} 
		catch (Exception e) {		
			e.printStackTrace();
		}
		
		return null;		
	}
} 