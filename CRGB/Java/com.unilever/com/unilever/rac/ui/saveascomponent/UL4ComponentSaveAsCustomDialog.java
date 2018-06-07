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

package com.unilever.rac.ui.saveascomponent;

/**
 * @author j.madhusudan.inamdar
 *
 */
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentListOfValues;
import com.teamcenter.rac.kernel.TCComponentProject;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCPropertyDescriptor;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.Registry;
import com.teamcenter.services.rac.core._2006_03.DataManagement.ItemProperties;
import com.unilever.rac.ui.common.UL4Common;
import com.unilever.rac.util.UnileverQueryUtil;
import com.teamcenter.rac.kernel.TCComponentType;

//com.teamcenter.services.internal.rac.core._2012_02.DataManagement;

public class UL4ComponentSaveAsCustomDialog extends TitleAreaDialog 
{

  private Combo 	projectCombo;
  private Combo 	uomCombo;
  private Text 		descriptionText;
  private String 	strDescription;
  private String 	strProject;
  private String    strUom;
  private ArrayList<String> strChildUom;
  private TCComponentProject[] projects;
  private TCSession session;
  private String[]  prefTokens;
  private String[] projectTokens;
  private String[] uomTokens;
  private Label [] comps;
  private Combo [] childUOMCombo1;
  private Label [] comps2 ;
  private Combo [] combo2 ;
  
  
  private TCComponentItemRevision selectedItemRevision;
  private TCComponentItemRevision latestPAMReleasedRevision;
  private TCComponentItemRevision newComponentItemRevision;
  private TCComponentItem newComponentItem;
  private TCComponentType tccomponenttype                      = null;
  
  private Shell currShell;
  private static final String OBJECT_SAVE_AS_INFORMATION_LABEL_CONSTANT		 			= "Object Save As information";
  private static final String DESCRIPTION_CONSTANT 										= "Description";
  private static final String PROJECT_NAME_LABEL_CONSTANT		 						= "Project Name";
  private static final String BASE_UOM_LABEL_CONSTANT		 							= "Base UOM";
  private String[] idArray									 							= new String[2];
  private boolean isCopyToClipBoardClicked												= false;
  private boolean isOpenOnCreateClicked													= false;
  private Button btnCopyToClipBoard;
  private Button btnOpenOnCreate;
  private org.eclipse.swt.graphics.Color color 				 = null ;  
  private boolean isSelectedFromDevTab = false;
  private boolean isNoPAMSpecCopy = true;
  private boolean isCloneChildComp = false;
  private Button yesButton;
  private Button noButton;
  private Button yesButton1;
  private Button noButton1;
  
  /** Registry **/
  private Registry reg = null;
  private HashMap<String, String> baseUOMMap = null;
  
  public UL4ComponentSaveAsCustomDialog(Shell parentShell, TCSession session, TCComponentItemRevision selectedItemRevision, TCComponentProject[] projects,  
		  														boolean isSelectedFromDevTab) 
  {
    super(parentShell);
    this.currShell = parentShell;
    this.session = session;
    this.selectedItemRevision = selectedItemRevision;
    this.projects = projects;
    this.isSelectedFromDevTab = isSelectedFromDevTab;
    reg = Registry.getRegistry( this );  
    try
    {
		tccomponenttype = session.getTypeComponent("U4_ComponentRevision");
	}
    catch (TCException e) 
    {
		e.printStackTrace();
	} 
    
  }

  public boolean isOKPressed()
	{
		return true;
		
	}

  @Override
  protected void createButtonsForButtonBar(Composite parent) 
  {
	   super.createButtonsForButtonBar(parent);
	
	  
	   Button ok = getButton(IDialogConstants.OK_ID);
	   ok.setText("Finish");
	   ok.setEnabled(false);
	   setButtonLayoutData(ok);
	
	   Button cancel = getButton(IDialogConstants.CANCEL_ID);
	   cancel.setText("Cancel");
	   setButtonLayoutData(cancel);
	   dialogArea.setBackground(super.getShell().getBackground());
	   
}
  @Override
  protected void configureShell(Shell newShell) 
  {
	  try
	  {
		  super.configureShell(newShell);
		  newShell.setText("Save " + selectedItemRevision.getProperty("object_type") + " As");
	  }
	  catch(TCException tcEx)
	  {
		  System.out.println(tcEx);
	  }
    
  }
  
	public boolean isClearPressed()
	{
		return true;
	}
	
  @Override
  public void create() 
  {
    super.create();
    setTitle(OBJECT_SAVE_AS_INFORMATION_LABEL_CONSTANT);
    setMessage(OBJECT_SAVE_AS_INFORMATION_LABEL_CONSTANT, IMessageProvider.INFORMATION);
    color = super.getShell().getBackground();
  }

  @Override
  protected Control createDialogArea(Composite parent) {
    Composite area = (Composite) super.createDialogArea(parent);
    Composite container = new Composite(area, SWT.NONE);
    parent.getShell().setBackground(super.getShell().getBackground());	
    container.setBackground(super.getShell().getBackground());
    area.setBackground(super.getShell().getBackground());
    container.setLayoutData(new GridData(GridData.FILL_BOTH));
    GridLayout layout = new GridLayout(2, false);
    layout.numColumns = 2;
    layout.verticalSpacing = 8;
    layout.horizontalSpacing = 100;
    container.setLayout(layout);

    createInformationLabel(container);
    createSpacer(container);
    createDescription(container);
    
    createProject(container);
    if ( (selectedItemRevision.getType().equals("U4_DDERevision") == false) && (selectedItemRevision.getType().equals("U4_CADComponentRevision") == false))
    	createBaseUOM(container);
    
    /**
	 * CR 74 & CR 195 
	 * if the selected component is an Assembly technology, then provide 2 additional options:
	 * 1. Clone Child Components
	 * 2. Retain existing Child Components
	 */
    String str = null;
	try {
		str = selectedItemRevision.getProperty("view");
	} catch (TCException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	if(selectedItemRevision.getType().startsWith("U4_A") && str.length()>0)
	{
		createCloneChildrenComponentsRadioPanel(container);
	}	
    /**
     * If User has selected DDERevision on Final Components Tab, or has selected a Pack Component on Dev Components Tab, 
     * or has selected a CAD Component on Dev Components tab then this field will be made either “invisible” or will be “greyed out” on the UI, so end user does not make any selection there.
     */
	if( ! ("U4_DDERevision".equals(selectedItemRevision.getType())) && ! (isSelectedFromDevTab) && ! ("U4_CADComponentRevision".equals(selectedItemRevision.getType())) )	
	{
    	createCopyPAMSpecLabelAndYesNoRadioPanel(container);		
	}
	
	else
	{
	    Label clabel = new Label(container, SWT.NONE);
		clabel.setText(" ");
		clabel.setLocation(20, 125);
	}
	
    createSpacer(container);
    createGroupButtonsForOpenAndCopyToClipBoard(container);
    createSpacer(container);
    
    area.setBackground(this.currShell.getDisplay().getSystemColor(SWT.COLOR_WHITE));
    return area;
  }
  
  private void createCloneChildrenComponentsRadioPanel(final Composite container) {
	// TODO Auto-generated method stub
	  Label lblClone = new Label(container, SWT.NONE);
	    lblClone.setText("Assembly Save As Option");
	    final GridData gd1 = new GridData(GridData.BEGINNING);
		gd1.horizontalIndent = 25;
		gd1.verticalIndent = 10;
		lblClone.setLayoutData(gd1);	
			   
		Group group2 = new Group(container, SWT.NO_RADIO_GROUP);
	   
	    group2.setLayout(new RowLayout(SWT.VERTICAL));	   
	    
	    yesButton1 = new Button(group2, SWT.RADIO);
	  
	    yesButton1.setText("Clone Children Components");
	    
	    noButton1 = new Button(group2, SWT.RADIO);
	    noButton1.setSelection(true);
	    noButton1.setText("Retain existing Children Components");
	    
	    final GridData gd2 = new GridData(GridData.BEGINNING);
		gd2.horizontalIndent = 25;
		///gd2.verticalIndent = 10;
		gd2.widthHint = 190;
		gd2.heightHint = 25;
			
		final Label lblClone1 = new Label(container, SWT.NONE);
	    lblClone1.setText("Child Base UOM");
		lblClone1.setLayoutData(gd2);
		
		final Label lblClone2 = new Label(container, SWT.NONE);
		lblClone2.setText(" ");
		lblClone2.setLayoutData(gd2);		
		
		final GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.horizontalSpan = 2;
		
		final Composite composite = new Composite(container,SWT.None);
		composite.setLayoutData(gridData);
		composite.setSize(620, 170);				
		composite.setVisible(false);
		final Table table = new Table(composite,SWT.VIRTUAL |SWT.V_SCROLL);		
		table.setLinesVisible(false);
		table.setVisible(false);
		table.setBounds(20, 10, 550, 160);
		for (int i = 0; i < 2; i++) {
		      TableColumn column = new TableColumn(table, SWT.NONE);
		      column.setWidth(250);
		    }
		table.setBackground(super.getShell().getBackground());
		ArrayList <String> child_base_uom;		
		ArrayList <String> prePDLChildComps = null;
			
		String s_childComps;
		try {
			s_childComps = selectedItemRevision.getProperty("view");
			if(s_childComps.length()>0){
			System.out.println(s_childComps);
			String [] child_comps_ids = s_childComps.split(",");
			child_base_uom = new ArrayList<String>();
			prePDLChildComps = new ArrayList<String>();
			
			for(int i=0;i<child_comps_ids.length;i++)
			{
				String item_id = child_comps_ids[i].trim();
				int pos = item_id.indexOf("/");			
				String child_item_id = item_id.substring(0, pos);			
				String queryName1 = "__UL_LatestReleasedItemRevision";
				String entries1[] = { "ID" };
				String values[] ={child_item_id};
				TCComponent[] components = UnileverQueryUtil.executeQuery(queryName1, entries1, values);
				if (components.length!=0)
				{
					TCComponentItemRevision item = (TCComponentItemRevision)components[0];
					if(item.getRelated("U4_PAMSpecification").length  == 0 ){
						String child_uom = item.getItem().getLatestItemRevision().getProperty(UL4Common.BASE_UOM);
						child_base_uom.add(child_uom);
						prePDLChildComps.add(item_id);
					}
				}	
			}
			
			comps =new Label[prePDLChildComps.size()];
			childUOMCombo1 = new Combo [prePDLChildComps.size()]; 
			for (int j=0; j<prePDLChildComps.size(); j++) {
				comps[j] = new Label(table, SWT.NONE);
				comps[j].setText(prePDLChildComps.get(j)) ;	
				
				//comps[j].setVisible(false);
				childUOMCombo1[j] = new Combo(table, SWT.READ_ONLY | SWT.BORDER_SOLID);
				childUOMCombo1[j].setItems(uomTokens);
				childUOMCombo1[j].setText(child_base_uom.get(j));
				
				//childUOMCombo1[j].setVisible(false);
				childUOMCombo1[j].addSelectionListener(new SelectionAdapter() 
			    {
			        public void widgetSelected(SelectionEvent e)
			        {
			        	Combo c = (Combo)e.getSource(); 		        	
			          // When the selection changes, we re-validate the list
			        	validateUom();
			        	if(c.getText()==null || "".equals(c.getText())){
							setErrorMessage("Base Uom for child components can not be empty!");
							return;
						}
			        	else{
			        		Button ok = getButton(IDialogConstants.OK_ID);
			    		    ok.setEnabled(true);
			        	}
			        }
			    });				
			}
				
			}
			if(comps!=null){
			for(int i = 0;i<(comps.length*2);i++)
			{
				 new TableItem(table, SWT.NONE);
			}
			
			TableItem[] items = table.getItems();
			
			Label [] comps1 = new Label[comps.length];
			Combo [] combo1 = new Combo[childUOMCombo1.length];
			for(int i=0;i<comps.length;i++)
			{
				comps1[i] = new Label(table,SWT.NONE);
				combo1[i] = new Combo(table,SWT.NONE);
				comps1[i].setText("");	
				combo1[i].setText("");
			}
			comps2 = new Label[comps.length*2];
			combo2 = new Combo[childUOMCombo1.length*2];
			int index=0;
			int index1=0;
			for(int i=0;i<items.length;i++)
			{
				comps2[i] = new Label(table,SWT.NONE);
				combo2[i] = new Combo(table,SWT.NONE);
			}
			for(int i=0;i<comps.length;i++)
			{
				comps2[index++]=comps1[i];
				comps2[index++]=comps[i];
				combo2[index1++]=combo1[i];
				combo2[index1++]=childUOMCombo1[i];
			}
		    for(int i = 0;i<comps2.length;i++)
	    	{	
		    	if(i%2==0)
		    	{
		    		TableEditor editor = new TableEditor(table);
			    	 editor.grabHorizontal = true;
			    	 editor.setEditor(comps2[i], items[i], 0);
			    	 editor.setEditor(comps2[i], items[i], 1);
		    	}
		    	else
		    	{
		    		TableEditor editor = new TableEditor(table);
			    	 editor.grabHorizontal = true;
			    	 editor.setEditor(comps2[i], items[i], 0);
			    	 editor.horizontalAlignment = SWT.CENTER;
					 editor.verticalAlignment = SWT.CENTER;	
			    	 editor = new TableEditor(table);
			    	 editor.grabHorizontal = true;
			    	 editor.setEditor(combo2[i], items[i], 1);	
			    	 editor.horizontalAlignment = SWT.CENTER;
					 editor.verticalAlignment = SWT.CENTER;
		    	}
		    	 		    	
	    	}
			}
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	    gd2.exclude = true;
	    gridData.exclude=true; 
	    SelectionListener selectionListener = new SelectionAdapter () {
	         public void widgetSelected(SelectionEvent event) {
	            Button button = ((Button) event.widget);
	            System.out.print(button.getText());
	            System.out.println(" selected = " + button.getSelection());
	            if("Clone Children Components".equals(button.getText()))
	            {	  
	            	yesButton1.setSelection(true);
	            	noButton1.setSelection(false);
	            	isCloneChildComp = true;
	            	if(isSelectedFromDevTab)
	            	{
	            		String s_childComps;
	            		try {
	            			s_childComps = selectedItemRevision.getProperty("view");
	            			if(s_childComps.length()>0){
	            				composite.setVisible(true);
	            				lblClone1.setVisible(true);
	            				table.setVisible(true);
	        	            	for(int i = 0;i<comps.length;i++)
	        			    	{
	        				    	comps[i].setVisible(true);
	        			    		childUOMCombo1[i].setVisible(true);
	        			    	}
	        	            	
	        	            	gridData.exclude=false;
	            				gd2.exclude = false;	            				
	            				
	    		            	container.getParent().getShell().pack();
	    		            	//container.getParent().pack();
	            			}
	            		} catch (TCException e) {
	            			// TODO Auto-generated catch block
	            			e.printStackTrace();
	            		}
		            	
	            	}
	            }
	            else if("Retain existing Children Components".equals(button.getText()))
	            {
	            	noButton1.setSelection(true);
	            	yesButton1.setSelection(false);
	            	
	            	isCloneChildComp = false;
	            	composite.setVisible(false);
	            	table.setVisible(false);
	            	lblClone2.setVisible(false);
	            	lblClone1.setVisible(false);   
	            	
	            	for(int i = 0;i<comps.length;i++)
			    	{
				    	comps[i].setVisible(false);
			    		childUOMCombo1[i].setVisible(false);
			    	}
	            	gridData.exclude=true;
	            	gd2.exclude = true;	            	
	            	//container.getParent().layout();
	            	container.getParent().getShell().pack();
	            }
	         };
	      };
	    
	    yesButton1.addSelectionListener(selectionListener);
	    
	    
	    noButton1.addSelectionListener(selectionListener);
}


private void createCopyPAMSpecLabelAndYesNoRadioPanel(Composite container) 
  {
	    Label lblCopyPAMInfo = new Label(container, SWT.NONE);
	    lblCopyPAMInfo.setText("Copy corresponding PAM Specification?");
	    GridData gd1 = new GridData(GridData.BEGINNING);
		gd1.horizontalIndent = 25;
		gd1.verticalIndent = 10;
		lblCopyPAMInfo.setLayoutData(gd1);
	    
		/*GridData data = new GridData();
        data.exclude = false;
        data.horizontalAlignment = SWT.FILL;
        bHidden.setLayoutData(data);*/
        
		Group group2 = new Group(container, SWT.NO_RADIO_GROUP);
	    //group2.setText("");
	    group2.setLayout(new RowLayout(SWT.HORIZONTAL));
	    /*new Button(group2, SWT.RADIO).setText("Yes");
	    new Button(group2, SWT.RADIO).setText("No");*/
	    
	    yesButton = new Button(group2, SWT.RADIO);
	   // yesButton.setSelection(true);
	    yesButton.setText("Yes");
	    
	    noButton = new Button(group2, SWT.RADIO);
	    noButton.setSelection(true);
	    noButton.setText("No");
	    
	    SelectionListener selectionListener = new SelectionAdapter () {
	         public void widgetSelected(SelectionEvent event) {
	            Button button = ((Button) event.widget);
	            System.out.print(button.getText());
	            System.out.println(" selected = " + button.getSelection());
	            if("Yes".equals(button.getText()))
	            {	            	
	            	yesButton.setSelection(true);
	            	noButton.setSelection(false);
	            	isNoPAMSpecCopy = false;
	            	
	            	//MR2016.02 CR258
	            	/*
	            	
	            	try
	            	{
						AIFComponentContext[] aifcomps  =  selectedItemRevision.getRelated(UL4Common.PAM_SPECIFICATION_RELATION);
					    for ( AIFComponentContext aifcomp : aifcomps ) 
					    {				                 
						  InterfaceAIFComponent aifComponent = aifcomp.getComponent();
						  
						  if(aifComponent instanceof TCComponentItemRevision)
						  {
							  TCComponentItemRevision pamspec = (TCComponentItemRevision) aifComponent;
							  
							  String masterItemId = (String) pamspec.getTCProperty(UL4Common.TEMPLATE_ID).toString();									
							  String lastSyncRevID = (String) pamspec.getTCProperty(UL4Common.TEMPLATE_LAST_SYNC_REV_ID).toString();
							  
							  if(masterItemId.length() > 0 )
							  {
    							   String entries[] = { UL4Common.ITEM_ID_ATTR_DISP_NAME };
    							   String values[] = { masterItemId };
								   TCComponent[]  components = UnileverQueryUtil.executeQuery(UL4Common.ITEM_ID_ATTR_DISP_NAME, entries, values);
    								
								   if(components != null && components.length == 1)
								   {
									   TCComponentItem pamItem = (TCComponentItem)components[0]; 
									   TCComponentItemRevision tempLatestRevision =  pamItem.getLatestItemRevision() ;										   
								       String  masterItemLAtestRevId = (String) tempLatestRevision.getTCProperty(UL4Common.REVID).toString();									
									
								       if( !  masterItemLAtestRevId.equalsIgnoreCase(lastSyncRevID))
								       {
								    	   
								    	   StringBuilder sInfo =new StringBuilder();
								    	   sInfo.append(reg.getString("WARNING_MSG_SAVE_AS_INVALID_TEMPLATE_STMT_3"));
								    	   sInfo.append("\n\n");
								    	   sInfo.append(reg.getString("WARNING_MSG_SAVE_AS_INVALID_TEMPLATE_STMT_4"));							               
							               MessageDialog.openInformation(currShell, "Information", sInfo.toString());

								       }										   
								   } 
							  }
						  }	
					  }						
					}
	            	catch (TCException e)
	            	{
						// TODO Auto-generated catch block
						e.printStackTrace();
					} 
					
					*/

	            }
	            else if("No".equals(button.getText()))
	            {
	            	noButton.setSelection(true);
	            	yesButton.setSelection(false);
	            	isNoPAMSpecCopy = true;
	            }
	         };
	      };
	      
	   
	    yesButton.addSelectionListener(selectionListener);
	    
	    
	    noButton.addSelectionListener(selectionListener);
	    
	    Label lbtMethod = new Label(container,SWT.WRAP | SWT.BORDER_SOLID);
		  lbtMethod.setText("Note: Copying corresponding PAM Specification will create a new Pack Component on the Final Components Tab.");
		  Font font = new Font(currShell.getDisplay(), new FontData("Helvetica", 8, SWT.TRANSPARENT));
		  lbtMethod.setFont(font);
		  lbtMethod.setSize(64, 32);		  
		  GridData gridData = new GridData();
		  gridData.horizontalSpan = 2;
		  gridData.horizontalIndent = 25;
		  lbtMethod.setLayoutData(gridData);
	}
 
  
  private void createSpacer(Composite container) 
  {
	  try
	  {
		  Label localLabel = new Label(container, 258);
		  GridData localGridData3 = new GridData();
		  localGridData3.horizontalAlignment = 4;
		  localGridData3.horizontalSpan = 2;
		  localGridData3.grabExcessHorizontalSpace = true;
		  localLabel.setLayoutData(localGridData3);
		    
  	  }
	  catch(Exception tcEx)
	  {
		  System.out.println(tcEx);
	  }    

  }
  
  private void createGroupButtonsForOpenAndCopyToClipBoard(Composite container) 
  {
	  try
	  {
		  	btnOpenOnCreate = new Button(container, SWT.CHECK | SWT.TRANSPARENT);
		  	btnOpenOnCreate.setText("Open On Create");
		  	btnOpenOnCreate.setLocation(20, 125);
		  	btnOpenOnCreate.pack();
		  	GridData gd = new GridData(GridData.BEGINNING);
		  	gd.horizontalIndent = 15;
		  	//gd.verticalIndent = 10;
		  	btnOpenOnCreate.setLayoutData(gd);
			  
		  	btnOpenOnCreate.addSelectionListener(new SelectionListener() {

		        public void widgetSelected(SelectionEvent event) {
		        	
		        	if(btnOpenOnCreate.getSelection())
		        		isOpenOnCreateClicked = true;
		        	else
		        		isOpenOnCreateClicked = false;
		        }

				@Override
				public void widgetDefaultSelected(SelectionEvent arg0) {
					// TODO Auto-generated method stub
					
				}
		      });

			  
		    btnCopyToClipBoard = new Button(container, SWT.CHECK | SWT.TRANSPARENCY_MASK);
		    btnCopyToClipBoard.setText("Copy To Clipboard");
		    btnCopyToClipBoard.setLocation(20, 300);
		    btnCopyToClipBoard.addSelectionListener(new SelectionListener() {

		        public void widgetSelected(SelectionEvent event) {
		        	if(btnCopyToClipBoard.getSelection())
		        		isCopyToClipBoardClicked = true;
		        	else
		        		isCopyToClipBoardClicked = false;
		        }

				@Override
				public void widgetDefaultSelected(SelectionEvent arg0) {
					// TODO Auto-generated method stub
					
				}
		      });
  	  }
	  catch(Exception tcEx)
	  {
		  System.out.println(tcEx);
	  }    

  }
  private void createInformationLabel(Composite container) 
  {
	  try
	  {
		  Label lbtMethod = new Label(container, SWT.BORDER_SOLID);
		  lbtMethod.setText(selectedItemRevision.getProperty("object_string"));
		  Font font = new Font(currShell.getDisplay(), new FontData("Helvetica", 12, SWT.TRANSPARENT));
		  lbtMethod.setFont(font);
		  lbtMethod.setSize(64, 32);		  
		  GridData gridData = new GridData();
		  gridData.horizontalSpan = 2;
		  lbtMethod.setLayoutData(gridData);
		  	
  	  }
	  catch(TCException tcEx)
	  {
		  System.out.println(tcEx);
	  }    

  }
  
  private void createDescription(Composite container) 
  {
	  Label lbtDescription = new Label(container, SWT.BORDER_SOLID);
	  lbtDescription.setText(DESCRIPTION_CONSTANT);
	
	  GridData gd = new GridData(GridData.BEGINNING);
	  gd.horizontalIndent = 25;
	  gd.verticalIndent = 10;
	  lbtDescription.setLayoutData(gd);
	  
	  descriptionText = new Text(container, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
	  GridData gridData = new GridData(GridData.BEGINNING);
	  gridData.horizontalSpan = 0;
	  
	  gridData.widthHint = 250;
	  gridData.heightHint = 40;
	  try 
	  {
		  descriptionText.setText(selectedItemRevision.getProperty(UL4Common.OBJECT_DESC));
	  } 
	  catch (TCException e) 
	  {
		// TODO Auto-generated catch block
		e.printStackTrace();
	  }
	  
	  descriptionText.setLayoutData(gridData);

  }
  
  private void validate() throws TCException 
  {
	    // We select the number of selected list entries
	    ///boolean selected = (projectCombo.getSelectionIndex() > 0);
	    if(!"".equals(projectCombo.getText()) )
	    {
	    	// We enable/disable the Finish button
		    Button ok = getButton(IDialogConstants.OK_ID);
		  String entries1[] = { UL4Common.OBJ_NAME_ATTR_DISP_NAME, UL4Common.QRY_TYPE_PARAM };
		  String values1[] = { projectCombo.getText().trim(), UL4Common.QRY_PROJECT_PARAM };
		  TCComponent[]  components1 = UnileverQueryUtil.executeQuery(UL4Common.GENERAL_QRY_NAME, entries1, values1);
		  if(components1 != null && components1.length == 1)
		  {
				TCComponentItem designProjectItem = (TCComponentItem) components1[0];
				TCComponentItemRevision designProjectLatestItemRevision = designProjectItem.getLatestItemRevision();
				if(designProjectLatestItemRevision.isCheckedOut())
				{
					String user = designProjectLatestItemRevision.getTCProperty("checked_out_user").toString();
					MessageDialog.openInformation(currShell, "Information", "Project " + projectCombo.getText().trim() + " is checked out by " + user );
					  ok.setEnabled(false);
				}
				else
				{
		    ok.setEnabled(true);
	           }
		  }

	    }
	    else
	    	// If nothing was selected, we set an error message
		      setErrorMessage("Project can not be empty!");
  }
  
  private void validateUom() 
  {
	    // We select the number of selected list entries
	    ///boolean selected = (projectCombo.getSelectionIndex() > 0);
	  if(!("".equals(projectCombo.getText())) && !("".equals(uomCombo.getText())))
	    {
	    	// We enable/disable the Finish button
		    Button ok = getButton(IDialogConstants.OK_ID);
		    ok.setEnabled(true);
	    }
	  if("".equals(uomCombo.getText()))
	    	// If nothing was selected, we set an error message
		      setErrorMessage("Uom can not be empty!");
  }
  
  private void createProject(Composite container) 
  {
	    Label lblProjectName = new Label(container, SWT.NONE);
	    lblProjectName.setText(PROJECT_NAME_LABEL_CONSTANT);
	    GridData gd1 = new GridData(GridData.BEGINNING);
		gd1.horizontalIndent = 25;
		gd1.verticalIndent = 10;
		lblProjectName.setLayoutData(gd1);
	    
	    GridData dataConditionSet = new GridData();
	    dataConditionSet.grabExcessHorizontalSpace = false;
	    projectCombo = new Combo(container, SWT.READ_ONLY | SWT.BORDER_SOLID);
	    projectCombo.addSelectionListener(new SelectionAdapter() {
	        public void widgetSelected(SelectionEvent e) {
	          // When the selection changes, we re-validate the list
	          try {
	          validate();
			} catch (TCException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	        }
	      });
	    GridData gd = new GridData();
	    //gd.grabExcessHorizontalSpace = false;
	    gd.widthHint = 180;
	    gd.heightHint = 35;
	    projectCombo.setLayoutData(gd);
	    
	    String arr = Arrays.toString(projects);
	   
	    String regx = "[]";
	    char[] ca = regx.toCharArray();
	    for (char c : ca) {
	    	arr = arr.replace(""+c, "");
	    }
	    
	    projectTokens = arr.split(",");
	    
	    Arrays.sort(projectTokens);
	
	    projectCombo.setItems(projectTokens);
    
  }
  
  private void createBaseUOM(Composite container) 
  {
	    Label lblbaseUom = new Label(container, SWT.NONE);
	    lblbaseUom.setText(BASE_UOM_LABEL_CONSTANT);
	    GridData gd1 = new GridData(GridData.BEGINNING);
		gd1.horizontalIndent = 25;
		gd1.verticalIndent = 10;
		lblbaseUom.setLayoutData(gd1);
	    
	    GridData dataConditionSet = new GridData();
	    dataConditionSet.grabExcessHorizontalSpace = false;
	    uomCombo = new Combo(container, SWT.READ_ONLY | SWT.BORDER_SOLID);
	    uomCombo.addSelectionListener(new SelectionAdapter() 
	    {
	        public void widgetSelected(SelectionEvent e)
	        {
	          // When the selection changes, we re-validate the list
	        	validateUom();
	        }
	    });
	    GridData gd = new GridData();
	    //gd.grabExcessHorizontalSpace = false;
	    gd.widthHint = 180;
	    gd.heightHint = 35;
	    uomCombo.setLayoutData(gd);
	    
	    String uomVal = null;
		try 
		{
			uomVal = selectedItemRevision.getProperty(UL4Common.BASE_UOM);
		} 
		catch (TCException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		if(uomVal.isEmpty())
		{
			try 
			{
				AIFComponentContext[] aifcomps  =  selectedItemRevision.getRelated(UL4Common.PAM_SPECIFICATION_RELATION);
				for ( AIFComponentContext aifcomp : aifcomps ) 
			    {				                 
				  InterfaceAIFComponent aifComponent = aifcomp.getComponent();
				  
				  if(aifComponent instanceof TCComponentItemRevision)
				  {
					  TCComponentItemRevision pamspec = (TCComponentItemRevision) aifComponent;
					  uomVal = (String) pamspec.getTCProperty(UL4Common.BASE_UOM).toString();									
				  } 
			    }
			}
			catch (TCException e1) 
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		   
	    String regx = "[]";
	    char[] ca = regx.toCharArray();
	    for (char c : ca) {
	    	uomVal = uomVal.replace(""+c, "");
	     }
	    
	    try 
	    {
			TCComponentListOfValues lov  = getTCPropertyDescriptor(UL4Common.BASE_UOM).getLOV();
			uomTokens = lov.getListOfValues().getStringListOfValues();
		} 
	    catch (TCException e1)
	    {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	    
	    //uomTokens = arr.split(",");
	    
	    Arrays.sort(uomTokens);
	
	    uomCombo.setItems(uomTokens);
	    if(!(uomVal == null) && !("".equals(uomVal)))
	    {
	    	for( int i =0;i<uomCombo.getItemCount();i++)
	    	{
	    		if(uomVal.equalsIgnoreCase(uomCombo.getItem(i)))
	    		{
	    		uomCombo.select(i);
	    		//uomCombo.setEnabled(false);
	    		if(!isSelectedFromDevTab){
	    			uomCombo.setEnabled(false);
	    		}
	    		break;
	    	}
	    }
  }
  	}
	
	@Override
	protected void okPressed() 
	{  
		this.strProject = projectCombo.getText();
		if(strProject == null || "".equals(strProject))
		{
			setErrorMessage("Project can not be empty!");
			return;
		}
		
		if ( (selectedItemRevision.getType().equals("U4_DDERevision") == false) && (selectedItemRevision.getType().equals("U4_CADComponentRevision") == false))
		{
			this.strUom = uomCombo.getText();
			if(strUom == null || "".equals(strUom))
			{
				setErrorMessage("Base Uom can not be empty!");
				return;
			}
		}
		
		if(combo2!=null)
		{
			strChildUom = new ArrayList<String>();
			for(int i = 0; i<combo2.length; i++)
			{
				if(i%2==0)
				{
					
				}
				else
				strChildUom.add(combo2[i].getText());
			}
			baseUOMMap = new HashMap<String,String>();
			for(int i = 0; i<comps.length;i++){
				baseUOMMap.put(comps[i].getText(), strChildUom.get(i));
			}
		}
		if ( (selectedItemRevision.getType().equals("U4_DDERevision") == false) && (selectedItemRevision.getType().equals("U4_CADComponentRevision") == false))
		{
			setBaseUom(uomCombo.getText());
		}
		setProject(projectCombo.getText());
		strProject = projectCombo.getText();
		strDescription = descriptionText.getText();
		setDescription(descriptionText.getText());
		super.okPressed();
		
		//Now based on the Flag - if user selected a component from DEV Tab, invoke UL4ComponentSaveAsDevelopmentOperation, else invoke UL4ComponentSaveAsOperation
		if(isSelectedFromDevTab)			
		{
				UL4ComponentSaveAsDevelopmentOperation saveAsOperation = new UL4ComponentSaveAsDevelopmentOperation(selectedItemRevision,session, isCopyToClipBoardClicked, isOpenOnCreateClicked, isCloneChildComp, strProject, strDescription,strUom,baseUOMMap);
				session.queueOperation(saveAsOperation);			
		}
		else
		{
			/**
			 * Check if User has selected "Copy corresponding PAM Specification?"
			 * if selected No - then A copy of the pack component will be created in the “Dev Components” tab with “Packaging Concept Creation” as a stage and copy of PAM will not be created.
			 */
			 if(isNoPAMSpecCopy)
			 {
				 if(!selectedItemRevision.getType().equals("U4_DDERevision")){
					 UL4ComponentSaveAsDevelopmentOperation saveAsOperation = new UL4ComponentSaveAsDevelopmentOperation(selectedItemRevision,session, isCopyToClipBoardClicked, isOpenOnCreateClicked, isCloneChildComp, strProject, strDescription,strUom,baseUOMMap);
					 session.queueOperation(saveAsOperation);				
				 }
				 else
				 {
					 UL4ComponentSaveAsOperation saveAsOperation = new UL4ComponentSaveAsOperation(selectedItemRevision,session, isCopyToClipBoardClicked, isOpenOnCreateClicked,isCloneChildComp, strProject, strDescription,strUom);
					 session.queueOperation(saveAsOperation);
				 }
			 }
			 else
			 {
					 UL4ComponentSaveAsOperation saveAsOperation = new UL4ComponentSaveAsOperation(selectedItemRevision,session, isCopyToClipBoardClicked, isOpenOnCreateClicked,isCloneChildComp, strProject, strDescription,strUom);
					 session.queueOperation(saveAsOperation);		
			 }
		}		
		
  }

	
	/**
	 * Empty customization hook for document creation
	 * @param inputs Additional ItemProperties to be set
	 */
	protected void extendComponentAttributes(ItemProperties inputs) {
		// customization hook for documents in Component
	}
	

  
  public void setDescription(String currDescription){
		strDescription = currDescription;
	}
  
  public void setProject(String currProject){
	  	strProject = currProject;
	}
  public void setBaseUom(String currUom)
  {
	  	strUom = currUom;
	}
  

  private TCPropertyDescriptor getTCPropertyDescriptor(String prop) throws TCException
	{
		return tccomponenttype.getPropertyDescriptor(prop);
	}

	protected boolean isResizable()
	{
	    return true;
	}
	
} 
