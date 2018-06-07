package com.unilever.rac.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Button;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentForm;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentListOfValues;
import com.teamcenter.rac.kernel.TCComponentListOfValuesType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.common.DataExportCommon;

import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.wb.swt.ResourceManager;

public class ImportDataDialog extends Dialog 
{
	private Shell shell;
	private TCComponentItemRevision itemRevision;
	
	DataExportCommon common = new DataExportCommon();
	ExportDataDialog expData;
	
	private Text txtImportFile;
	private Text txtSupplier;
	private Text txtSupplierInfo;
	private Text txtComments;
	private List listImported;
	private Combo comboImportReason;
	private String sImportFileValue;
	private String sSupplierValue;
	private String sImportReasonValue;
	TCComponentForm form = null;
	private String sSupplierInfoValue;
	private String sCommentsValue;
	//private ArrayList<TCComponent> alObjectsToRelease = new ArrayList<TCComponent>();
	private boolean isImportFailed = false;
	
	/**
	 * Create the dialog.
	 * @param parentShell
	 * @param comp
	 * @param attachments
	 * @param tcsession
	 */
	public ImportDataDialog(Shell parentShell, TCComponent comp) 
	{
		super(parentShell);
		setShellStyle(SWT.RESIZE|SWT.DIALOG_TRIM);
		shell = parentShell;
		itemRevision = (TCComponentItemRevision) comp;
	}

	/**
	 * Create contents of the dialog.
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent)
	{
		parent.setToolTipText("");
		//shell.setText("Import Data");
		Composite container = (Composite) super.createDialogArea(parent);
		container.setFont(SWTResourceManager.getFont(common.FONT_TYPE, 9, SWT.NORMAL));
		container.setLayout(new FormLayout());
		
		Label lblAttachList = new Label(container, SWT.NONE);
		lblAttachList.setFont(SWTResourceManager.getFont("Segoe UI", 9, SWT.NORMAL));
		FormData fd_lblAttachList = new FormData();
		fd_lblAttachList.right = new FormAttachment(100, -319);
		fd_lblAttachList.left = new FormAttachment(0, 23);
		fd_lblAttachList.top = new FormAttachment(0, 10);
		lblAttachList.setLayoutData(fd_lblAttachList);
		lblAttachList.setText(common.LBL_IMPORTZIP);
	    
	    Label lblNewLabel = new Label(container, SWT.NONE);
	    FormData fd_lblNewLabel = new FormData();
	    fd_lblNewLabel.left = new FormAttachment(0, 23);
	    lblNewLabel.setLayoutData(fd_lblNewLabel);
	    lblNewLabel.setText(common.LBL_FILESTOIMPORT);
	    
	    txtImportFile = new Text(container, SWT.BORDER);
	    fd_lblNewLabel.top = new FormAttachment(txtImportFile, 6);
	    FormData fd_txtImportFile = new FormData();
	    fd_txtImportFile.left = new FormAttachment(0, 23);
	    fd_txtImportFile.top = new FormAttachment(lblAttachList, 6);
	    fd_txtImportFile.bottom = new FormAttachment(100, -447);
	    txtImportFile.setLayoutData(fd_txtImportFile);
	    
	    Button btnBrowse_1 = new Button(container, SWT.NONE);
	    fd_txtImportFile.right = new FormAttachment(100, -106);
	    FormData fd_btnBrowse_1 = new FormData();
	    fd_btnBrowse_1.left = new FormAttachment(txtImportFile, 17);
	    fd_btnBrowse_1.right = new FormAttachment(100, -27);
	    fd_btnBrowse_1.top = new FormAttachment(0, 30);
	    btnBrowse_1.setLayoutData(fd_btnBrowse_1);
	    btnBrowse_1.setText(common.BTN_BROWSE);
	    
	    listImported = new List(container, SWT.BORDER);
	    FormData fd_listImported = new FormData();
	    fd_listImported.top = new FormAttachment(lblNewLabel, 6);
	    fd_listImported.left = new FormAttachment(0, 23);
	    fd_listImported.right = new FormAttachment(100, -27);
	    listImported.setLayoutData(fd_listImported);
	    
	    //Browse button action for file selection 
	    btnBrowse_1.addSelectionListener(new SelectionAdapter() 
			    {
					public void widgetSelected(SelectionEvent event)
			    	{
		    			org.eclipse.swt.widgets.FileDialog fd = 
		    				new org.eclipse.swt.widgets.FileDialog(shell, SWT.OPEN);
		    		 	fd.setText(common.TITLE_OPENDIALOG);
		    	        fd.setFilterPath(common.DIR_FILTER);
		    	        String[] filterExt = { common.EXTN_ZIPFILES};
		    	        fd.setFilterExtensions(filterExt);
		    	        String selected = fd.open();
		    	        
		    	        if (selected != null)
		    	        {
		    	        	txtImportFile.setText(selected);
		    	        	listImported.removeAll();
		    	        	listImported.add(fd.getFileName());
		    	        }
			    	}
			    });

	    comboImportReason = new Combo(container, SWT.READ_ONLY);
	    FormData fd_comboImportReason = new FormData();
	    fd_comboImportReason.right = new FormAttachment(100, -172);
	    fd_comboImportReason.left = new FormAttachment(0, 24);
	    comboImportReason.setLayoutData(fd_comboImportReason);
	    
	    //Populate comboExpReason
	    String[] saLOVVALUES = null;
	    TCComponentListOfValues LOV= TCComponentListOfValuesType.findLOVByName(common.LOV_IMPORTREASON);
	    try 
	    {
			saLOVVALUES = LOV.getListOfValues().getStringListOfValues();
		} catch (TCException e2) {e2.printStackTrace();	}
	    comboImportReason.setItems(saLOVVALUES);
	    
	    Label lblNewLabel_1 = new Label(container, SWT.NONE);
	    fd_listImported.bottom = new FormAttachment(lblNewLabel_1, -6);
	    FormData fd_lblNewLabel_1 = new FormData();
	    fd_lblNewLabel_1.left = new FormAttachment(0, 23);
	    fd_lblNewLabel_1.bottom = new FormAttachment(comboImportReason, -6);
	    lblNewLabel_1.setLayoutData(fd_lblNewLabel_1);
	    lblNewLabel_1.setText(common.LBL_IMPORTREASON);
	    
	    ControlDecoration controlDecoration_1 = new ControlDecoration(lblNewLabel_1, SWT.RIGHT | SWT.TOP);
	    controlDecoration_1.setImage(ResourceManager.getPluginImage(common.PLUGIN_NAME, common.PATH_IMAGE_MANDATORY));
	    
	    Label lblNewLabel_2 = new Label(container, SWT.NONE);
	    fd_comboImportReason.bottom = new FormAttachment(lblNewLabel_2, -6);
	    FormData fd_lblNewLabel_2 = new FormData();
	    fd_lblNewLabel_2.left = new FormAttachment(0, 23);
	    lblNewLabel_2.setLayoutData(fd_lblNewLabel_2);
	    lblNewLabel_2.setText(common.LBL_SUPNAME);
	    
	    txtSupplier = new Text(container, SWT.BORDER);
	    fd_lblNewLabel_2.bottom = new FormAttachment(txtSupplier, -6);
	    
	    ControlDecoration controlDecoration_2 = new ControlDecoration(lblNewLabel_2, SWT.RIGHT | SWT.TOP);
	    controlDecoration_2.setImage(ResourceManager.getPluginImage(common.PLUGIN_NAME, common.PATH_IMAGE_MANDATORY));
	    FormData fd_txtSupplier = new FormData();
	    fd_txtSupplier.right = new FormAttachment(100, -27);
	    fd_txtSupplier.left = new FormAttachment(0, 23);
	    txtSupplier.setLayoutData(fd_txtSupplier);
	    
	    Label lblNewLabel_3 = new Label(container, SWT.NONE);
	    fd_txtSupplier.bottom = new FormAttachment(lblNewLabel_3, -6);
	    FormData fd_lblNewLabel_3 = new FormData();
	    fd_lblNewLabel_3.left = new FormAttachment(0, 23);
	    lblNewLabel_3.setLayoutData(fd_lblNewLabel_3);
	    lblNewLabel_3.setText(common.LBL_SUPINFO);
	    
	    txtSupplierInfo = new Text(container, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL);
	    fd_lblNewLabel_3.bottom = new FormAttachment(txtSupplierInfo, -6);
	    FormData fd_txtSupplierInfo = new FormData();
	    fd_txtSupplierInfo.right = new FormAttachment(100, -27);
	    fd_txtSupplierInfo.left = new FormAttachment(0, 23);
	    fd_txtSupplierInfo.top = new FormAttachment(0, 323);
	    txtSupplierInfo.setLayoutData(fd_txtSupplierInfo);
	    
	    Label lblNewLabel_4 = new Label(container, SWT.NONE);
	    fd_txtSupplierInfo.bottom = new FormAttachment(lblNewLabel_4, -6);
	    FormData fd_lblNewLabel_4 = new FormData();
	    fd_lblNewLabel_4.left = new FormAttachment(0, 23);
	    lblNewLabel_4.setLayoutData(fd_lblNewLabel_4);
	    lblNewLabel_4.setText(common.LBL_COMMENTS);
	    
	    txtComments = new Text(container, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL);
	    fd_lblNewLabel_4.bottom = new FormAttachment(txtComments, -6);
	    FormData fd_txtComments = new FormData();
	    fd_txtComments.right = new FormAttachment(100, -27);
	    fd_txtComments.left = new FormAttachment(0, 23);
	    fd_txtComments.bottom = new FormAttachment(100, -32);
	    fd_txtComments.top = new FormAttachment(0, 397);
	    
	    ControlDecoration controlDecoration = new ControlDecoration(lblAttachList, SWT.RIGHT | SWT.TOP);
	    controlDecoration.setImage(ResourceManager.getPluginImage(common.PLUGIN_NAME, common.PATH_IMAGE_MANDATORY));
	    txtComments.setLayoutData(fd_txtComments);

		return container;
	}
	
	 @Override
	 protected void configureShell(Shell shell)
	 {
	    super.configureShell(shell);
	    shell.setText("Import Data");
	 }

	 
	/**
	 * Create contents of the button bar.
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent)
	{
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,	true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(407, 578);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 * Function: Verify the details from user given through the dialog
	 * create the form with unique name and paste the dataset under that
	 * to ensure backup
	 */
	protected void okPressed()
	{
		sImportFileValue = txtImportFile.getText().toString();
		sSupplierValue = txtSupplier.getText().toString();
		sSupplierInfoValue = txtSupplierInfo.getText().toString();
		sImportReasonValue = comboImportReason.getText().toString();
		sCommentsValue = txtComments.getText().toString();
		
		if(isImportDialogInputValid())
		{
			String sZipFileName = listImported.getItem(0).toString();
	
			super.okPressed();
			
			TCComponentDataset zipDataset = null;
			try 
			{
				zipDataset = common.createDataset(sZipFileName, sImportFileValue, common.TYPE_ZIP, common.NREF_ZIP);
			} 
			catch (TCException e) 
			{
				System.out.println("***ImportData:Error in createDataset function " 
						+ e.toString());
				isImportFailed  = true;
			}
			
			//New form name
			String timestamp = common.getTimestamp();
			String sNewFormName = sSupplierValue+common.UNDERSCORE+timestamp;
			
			//Create and populate form
			try 
			{
				form = common.createForm(sNewFormName, common.TYPE_IMPORTDATAFORM);
				populateImportForm(form, sSupplierValue, sSupplierInfoValue, sImportReasonValue, sCommentsValue );
			}
			catch (TCException e) 
			{
				System.out.println("***ImportData:Error in create DataExportForm / populate DataExportForm function " 
						+ e.toString());
				isImportFailed = true;
			}
			
			//Attach dataset under import form
			try 
			{
				form.add(common.RLN_EXPBKP, zipDataset);
			}
			catch (TCException e) 
			{
				System.out.println("***ImportData:Error adding zipdataset under form " 
						+ e.toString());
				isImportFailed = true;
			}
			
			//Attach "U4_ImportForm" under target IR
			try 
			{
				itemRevision.add(common.RLN_EXPBKP, form);
				itemRevision.refresh();
			} 
			catch (TCException e)
			{
				System.out.println("***ImportData:Error in attaching 'U4_DataImportForm' form under IR "
									+ e.toString());
				isImportFailed = true;
			}
			
			//Release "U4_ImportForm" and zip dataset
			/*try 
			{
				alObjectsToRelease.add(form);
				alObjectsToRelease.add(zipDataset);				
				common.releaseObjects(alObjectsToRelease, common.PROCESS_EXPORT);
			}
			catch (TCException e) 
			{
				System.out.println("***ImportData:Error releasing objects " 
						+ e.toString());
				isImportFailed = true;
			}*/

			if(isImportFailed)
			{
				com.teamcenter.rac.util.MessageBox mb = 
						new com.teamcenter.rac.util.MessageBox(common.ImportFailedMsg, common.ImportFailed, 1);
                mb.setModal(true);
                mb.setVisible(true);
			}
			else
			{
				com.teamcenter.rac.util.MessageBox mb = 
						new com.teamcenter.rac.util.MessageBox(common.ImportSuccessMsg, common.ImportSuccess, 2);
                mb.setModal(true);
                mb.setVisible(true);
			}
		}
	}
	
	/**
	 * Populates import form with given values
	 * @param form
	 * @param sSupplierValue
	 * @param sSupplierInfoValue
	 * @param sExportReason
	 * @param sCommentsValue
	 * @throws TCException
	 */
	private void populateImportForm(TCComponentForm form,
			String sSupplierValue, String sSupplierInfoValue,
			String sExportReason, String sCommentsValue) throws TCException 
	{
		if(sCommentsValue != null)
			form.setProperty(common.PROP_COMMENTS, sCommentsValue.trim());
		if(sSupplierValue != null)
			form.setProperty(common.PROP_SUPPLIERNAME, sSupplierValue.trim());
		if(sSupplierInfoValue != null)
			form.setProperty(common.PROP_SUPPLIERINFO, sSupplierInfoValue.trim());
		if(sExportReason != null)
			form.setProperty(common.PROP_REASON, sExportReason.trim());
		form.refresh();
	}
	
	/**
	 * Validates user dialog inputs
	 * @return isValid
	 */
	private boolean isImportDialogInputValid() 
	{
		boolean isValid = false;
		
		if (sImportFileValue.trim().isEmpty() || 
				sSupplierValue.trim().isEmpty() || 
				sImportReasonValue.trim().isEmpty() )
		{
			MessageBox box = new MessageBox(getShell(), SWT.ERROR);
			box.setText(common.ValueMissing);
			box.setMessage(common.ValueMissingMsg);
			box.open();
		}
		else if (!common.validateFile(sImportFileValue))
		{	
			MessageBox box = new MessageBox(getShell(), SWT.ERROR);
			box.setText(common.FileMissing);
			box.setMessage(common.FileMissingMsg);
			box.open();
		}
		else if(!common.isZipFileValid(sImportFileValue))
		{
			MessageBox box = new MessageBox(getShell(), SWT.ERROR);
			box.setText(common.ZipNotValid);
			box.setMessage(common.ZipNotValidMsg);
			box.open();
		}
		else 
		{
			isValid = true;
		}
		return isValid;
	}
}
