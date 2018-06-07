/**===================================================================================
 *             
 *                     Unpublished - All rights reserved
 * ===================================================================================
 * File Description: UL4CADComponentDialog.java
 * This is a class used to show and create New CAD Component item and its associations.
 * 
 * ===================================================================================
 * Revision History
 * ===================================================================================
 * Date         	Name       			  TCEng-Release  	Description of Change
 * ------------   --------------------	  -------------    ---------------------------
 * 22-Aug-2014	  Jayateertha M Inamdar   TC10.1.1.1        Initial Version
 * 
 *  $HISTORY$
 * ===================================================================================*/

package com.unilever.rac.ui.newcadcomponent;

import java.awt.Button;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.commands.paste.PasteOperation;
import com.teamcenter.rac.common.create.BOCreateDefinitionFactory;
import com.teamcenter.rac.common.create.IBOCreateDefinition;
import com.teamcenter.rac.kernel.BOCreatePropertyDescriptor;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCPropertyDescriptor;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.PropertyLayout;
import com.teamcenter.rac.util.Separator;
import com.teamcenter.rac.util.iTextArea;
import com.teamcenter.rac.util.iTextField;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core._2006_03.DataManagement.CreateItemsResponse;
import com.teamcenter.services.rac.core._2006_03.DataManagement.GenerateItemIdsAndInitialRevisionIdsProperties;
import com.teamcenter.services.rac.core._2006_03.DataManagement.GenerateItemIdsAndInitialRevisionIdsResponse;
import com.teamcenter.services.rac.core._2006_03.DataManagement.ItemIdsAndInitialRevisionIds;
import com.teamcenter.services.rac.core._2006_03.DataManagement.ItemProperties;
import com.teamcenter.services.rac.core._2006_03.DataManagement.ExtendedAttributes;
import com.unilever.rac.ui.common.UL4Common;

public class UL4CADComponentDialog extends Dialog
{
    private TCSession session              						 = null ;    
    private Shell shell					   						 = null ;
    private TCComponent selection								 = null ;
    private ArrayList<String> alMandatory		                 = null ;
    //private Registry currentReg                                  = null ;
    private org.eclipse.swt.graphics.Color color 				 = null ;
    private Button assignIDButton								 = null;
    private iTextArea descriptionTextArea						 = null;
	public iTextField nameTextBox 								 = null;
	public iTextField uniqueDescTextBox							 = null;
	public iTextField sizeTextBox								 = null;
	private String[] idArray									 = new String[2];
	
    public UL4CADComponentDialog( Shell parentShell, TCSession tcSession, TCComponent sel)
    {
        super( parentShell );
        session = tcSession;
        shell = parentShell;
        selection = sel ;
        //currentReg = Registry.getRegistry(this);
		new ArrayList<String>();
	    alMandatory = new ArrayList<String>(); 
	    alMandatory.clear();
	    new ArrayList<String>();
		new JTextField(20); 
		color = shell.getDisplay().getSystemColor(SWT.COLOR_WHITE);
    }

	protected Control createContents( Composite parent )
	{			
		initializeDialogUnits( parent );
        getShell().setSize( 430	, 400 );
        createDialogAndButtonArea( parent ); 
        return parent;
	}
	
	protected void createDialogAndButtonArea( Composite parent )
	{
		dialogArea = createDialogArea( parent);
		dialogArea.setBackground(color);
		buttonBar = createButtonBar( parent);
		buttonBar.setBackground(color);
		applyDialogFont( parent );		
	}
	
	protected void createButtonsForButtonBar( Composite parent )
	{
		super.createButton( parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true );
		super.createButton( parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, true );
	}
	
	protected Control createDialogArea( Composite parent )
	{	
		Composite mainComposite = ( Composite ) super.createDialogArea( parent );		
		parent.getShell().setText("CAD Component Item");		
		parent.getShell().setBackground(color);	
		mainComposite.setBackground(color);
		
		try
		{		
			mainComposite.setLayout(new FillLayout(SWT.NORMAL));
			Composite composite2 = new Composite(mainComposite, SWT.EMBEDDED | SWT.BORDER_SOLID);
			Frame frame = SWT_AWT.new_Frame(composite2);
	 		
			JPanel  mainPanel = new JPanel(new PropertyLayout(10,10, 10, 10, 10, 10));
			JPanel subPanel = null ;
			
			String nameStr = "<html>" + UL4Common.OBJ_NAME_ATTR_DISP_NAME + ":" +"<b><font color=\"#FF0000\">*</font></b></html>";
			JLabel name = new JLabel();
			name.setText(nameStr);
			
			JLabel uniqueDescriptor = new JLabel();
			uniqueDescriptor.setText(UL4Common.UNIQUE_DESC_ATTR_DISP_NAME + ":");
			
			JLabel size = new JLabel();
			size.setText("Size:");
			
			JLabel description = new JLabel();
			description.setText(UL4Common.DESCRIPTION_ATTR_DISP_NAME);

			descriptionTextArea = new iTextArea(5, 20, false);
			descriptionTextArea.setLineWrap ( true );
			descriptionTextArea.setWrapStyleWord(true);
			descriptionTextArea.setLengthLimit(128);
	        JScrollPane descScrollPane = new JScrollPane(descriptionTextArea);
	        
			nameTextBox = new iTextField(20);
			uniqueDescTextBox = new iTextField(10);
			sizeTextBox = new iTextField(10);
			
			nameTextBox.setLengthLimit(32);
			sizeTextBox.setLengthLimit(10);
			uniqueDescTextBox.setLengthLimit(10);
			
	    	subPanel = new JPanel((new PropertyLayout(10,5, 20, 1, 10, 15)));
			assignIDButton = new Button("Assign");	
			this.assignIDButton.addActionListener(new ActionListener() {
					public void actionPerformed(
							ActionEvent e) {
						idArray = getAssignedIds(UL4Common.CAD_COMPONENT_ITEM_TYPE);
				}
			});

			
			subPanel.add( 1 + ".1.left.top.preferred.preferred", name);
			subPanel.add( 1 + ".2.left.top.preferred.preferred", nameTextBox);
			
			subPanel.add( 2 + ".1.left.top.preferred.preferred", uniqueDescriptor);
			subPanel.add( 2 + ".2.left.top.preferred.preferred", uniqueDescTextBox);
			
			subPanel.add( 3 + ".1.left.top.preferred.preferred", size);
			subPanel.add( 3 + ".2.left.top.preferred.preferred", sizeTextBox);
			
			subPanel.add( 4 + ".1.left.top.preferred.preferred", description);
			subPanel.add( 4 + ".2.left.top.preferred.preferred", descScrollPane);
			
			
	        IBOCreateDefinition createDefinition = BOCreateDefinitionFactory.getInstance().
	        												getCreateDefinition(session, UL4Common.CAD_COMPONENT_ITEM_REV);
	        List<BOCreatePropertyDescriptor> list = createDefinition.getCreatePropertyDescriptors();
	        
	        boolean mandatory = false ;
	        alMandatory.clear();
	        alMandatory.add(UL4Common.OBJECT_NAME);
	        
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
	            	 alMandatory.add(formProps.get(inx).toString());
	            	 mandatory = false ;
	             }
	    	}
	    	JLabel title = new JLabel("CAD Component Item:");
	    	title.setFont(new Font("Serif", Font.CENTER_BASELINE, 16));
	    	title.setOpaque(true);
	    	
	    	mainPanel.add(1+ ".1.left.bind.left", title );
	    	mainPanel.add(2+ ".1.left.bind.left", new Separator());
	    	mainPanel.add(3+ ".1.left.bind.left", subPanel);
			
			JScrollPane jscrollpane = new JScrollPane();
			jscrollpane.getVerticalScrollBar().setUnitIncrement(15);
			jscrollpane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);		    
			jscrollpane.getViewport().add(mainPanel, null);
			frame.add(jscrollpane);
		    
		}
		catch (Exception e) 
		{			
			MessageBox.post(shell, e.getMessage() ,"error1", MessageBox.ERROR);
		} 
		
		return mainComposite;
	}

/**
	 * Empty customization hook for document creation
	 * @param inputs Additional ItemProperties to be set
	 */
	protected void extendComponentAttributes(ItemProperties inputs) {
		// customization hook for documents in CAD Component
	}
	
    	/**
	 * Creation of the component associated with the node. After successful creation, the new component
	 * is pasted to the selected Project Revision.
	 * @param type The component type to be created
	 * @return The created component
	 * @throws TCException During creation, projects assignment - need to be validated.
	 */
	protected TCComponent createComponent(String type) throws TCException
	{
		String              strName         = nameTextBox.getText();
		TCComponent         newComp         = null;
		CreateItemsResponse response        = null;
		String              relation        = "";
		
		/**
		 *  newComp should be declared here so than each time a new component is created and destroyed
		 *  PasteComponent will be called with the reference to
		 *  the new component created 
		 */
		session.setStatus("Creating New CAD Component" + " " + strName + " ...");
		
		DataManagementService dmService = DataManagementService.getService(session);
		
		ItemProperties[] inputs = new ItemProperties[1];
		inputs[0] = new ItemProperties();
		inputs[0].clientId    = "1";
		inputs[0].description = descriptionTextArea.getText();
		inputs[0].itemId      = idArray[0]; 
		inputs[0].name        = idArray[0];
		inputs[0].revId       = idArray[1];
		inputs[0].type        = type;
		inputs[0].uom         = new String();
		
		extendComponentAttributes(inputs[0]);

		response = dmService.createItems(inputs, null, relation);

		if (response.serviceData.sizeOfPartialErrors() == 0)
			newComp = response.output[0].item;
		else {
			TCException exception = new TCException(response.serviceData
					.getPartialError(0).getLevels(), response.serviceData
					.getPartialError(0).getCodes(), response.serviceData
					.getPartialError(0).getMessages());
			throw exception;
		}
		
		session.setReadyStatus();
		return newComp;
	}

	private boolean validateMandatoryField()
	{
		boolean status = true ;
		
		try
		{			
			if(nameTextBox.getText() == null || nameTextBox.getText().length() <= 0 )
			{			
				//MessageBox.post(shell, currentReg.getString("name_cannot_null"), "Error", MessageBox.ERROR);
				MessageBox.post(shell, "Name cannot be null.", "Error", MessageBox.ERROR);
				return false;
			}
			
		}
		catch (Exception e) 
		{			
			MessageBox.post(shell, e.getMessage() , "Error", MessageBox.ERROR);
		} 

		return status ;
	}
	

    
    	/**
	 * This method is called when a Create button is clicked. It will get next
	 * available ID and Revision.
	 * @param strType The item type for which the data should be returned.
	 * @return Array of strings of length 2 containing the ID at index 0 and the Revision at index 1.
	 */
	@SuppressWarnings("unchecked")
	protected String[] getAssignedIds(String strType) {
		String[] strReturnArray = new String[2];
		DataManagementService dmService = DataManagementService.getService(this.session);

		GenerateItemIdsAndInitialRevisionIdsProperties[] inputs = new GenerateItemIdsAndInitialRevisionIdsProperties[1];
		inputs[0] = new GenerateItemIdsAndInitialRevisionIdsProperties();
		inputs[0].item = null;
		inputs[0].itemType = strType;
		inputs[0].count = 1;
		GenerateItemIdsAndInitialRevisionIdsResponse newIdResponse = dmService.generateItemIdsAndInitialRevisionIds(inputs);
		if (newIdResponse.serviceData.sizeOfPartialErrors() > 0) return null;

		Iterator iterator=newIdResponse.outputItemIdsAndInitialRevisionIds.entrySet().iterator();
		while(iterator.hasNext()) {
			Map.Entry entry = (Map.Entry)iterator.next();
			ItemIdsAndInitialRevisionIds[] newIdRevs = (ItemIdsAndInitialRevisionIds[]) entry.getValue();
			ItemIdsAndInitialRevisionIds ids = newIdRevs[0];
			strReturnArray[0] = ids.newItemId;
			strReturnArray[1] = ids.newRevId;
			break;
		}
		return strReturnArray;
	}
	
	protected boolean isResizable()
	{
	    return true;
	}
	
	protected void okPressed()
    {
		if(validateMandatoryField())
    	{
			try
			{
				TCComponent newcad = createComponent(UL4Common.CAD_COMPONENT_ITEM_TYPE);
				if(newcad instanceof TCComponentItem)
				{
					TCComponentItemRevision revision  = ((TCComponentItem)newcad).getLatestItemRevision() ;	
					
					/**
					 * Save the properties from the UI:
					 * Size, Unique Descriptor
					 */
					if(validateFields(sizeTextBox) && validateFields(uniqueDescTextBox))
					{
						revision.lock();
						revision.setProperty(UL4Common.CAD_COMPONENT_UNIQUE_DESC_ATTR, uniqueDescTextBox.getText());
						revision.setProperty(UL4Common.CAD_COMPONENT_SIZE_ATTR, sizeTextBox.getText());
						revision.setProperty(UL4Common.CAD_COMPONENT_NAME, nameTextBox.getText());
						revision.save();
						revision.unlock();
						revision.refresh();
					}
					
					//Attatch Pack Component to Selected Project Revision
					PasteOperation paste2 = new PasteOperation(AIFUtility.getCurrentApplication(),selection,revision,UL4Common.DEVRELATION);	
					paste2.executeOperation();						

				}
				
			}
			catch(TCException ex)
			{
				ex.printStackTrace();
			}			
			super.cancelPressed();
    	}
		
    }	 
	
	public Boolean validateFields(iTextField property)
	{
		if(property.getText() != null || property.getText().length() >= 0)
			return true;
		else
			return false;
	}
	    
}