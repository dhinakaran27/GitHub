package com.unilever.rac.dialogs;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.ResourceManager;
import org.eclipse.wb.swt.SWTResourceManager;

import com.teamcenter.rac.common.DataExportCommon;
import com.teamcenter.rac.common.TCTypeRenderer;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentDatasetDefinition;
import com.teamcenter.rac.kernel.TCComponentForm;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentListOfValues;
import com.teamcenter.rac.kernel.TCComponentListOfValuesType;
import com.teamcenter.rac.kernel.TCException;

import org.eclipse.jface.viewers.TableViewer;

public class ExportDataDialog extends Dialog 
{
	private Text txtComments;
	private Text txtPath;
	private Text txtSupplier;
	
	private Shell shell;
	private TCComponentItemRevision itemRevision;
	private Vector<TCComponent> vChildrenDatasets = new Vector<TCComponent>();
	private Vector<TCComponent> vParentDatasets = new Vector<TCComponent>();
	
	DataExportCommon common = new DataExportCommon();
	private String sParentDatasetName = null;
	private String sChildrenDatasetName = null;
	
	private ArrayList<String> salParentDatasetNames = new ArrayList <String> ();
	private ArrayList<String> salChildrenDatasetNames = new ArrayList <String> ();
	private ArrayList<String> salParentDatasetWithType = new ArrayList <String> ();
	private ArrayList<String> salChildrenDatasetWithType = new ArrayList <String> ();

	private ArrayList<TCComponent> alObjectsToRelease = new ArrayList<TCComponent>();

	private Text txtSupplierInfo;
	private Text txtRefDoc;
	private Table table_1;
	private CheckboxTableViewer checkboxTableViewer;
	private Combo comboExpReason;
	private String sLocValue;
	private String sLocValueZIP;
	private String sCommentsValue;
	private String sSupplierValue;
	private String sSupplierInfoValue;
	private String sExportReason;
	private String sRefDoc;
	private String sRefDocFileName;
	private Object[] selectedDatasets = null;
	TCComponentForm form = null;
	private String sExportPath = null;
	private boolean isExportFailed = false;
	private Table table;

	/**
	 * Create the dialog.
	 * @param parentShell
	 * @param comp
	 * @param vChildrenAttachments
	 * @param vChildrenAttachments2 
	 * @param tcsession
	 */
	public ExportDataDialog(Shell parentShell, TCComponent comp, Vector<TCComponent> vParentAttachments, Vector<TCComponent> vChildrenAttachments) 
	{
		super(parentShell);
		setShellStyle(SWT.RESIZE|SWT.DIALOG_TRIM);
		shell = parentShell;
		itemRevision = (TCComponentItemRevision) comp;
		vParentDatasets = vParentAttachments;
		vChildrenDatasets = vChildrenAttachments;
		//System.out.println("attachments length " + vChildrenAttachments.size());
		//System.out.println("vChildrenAttachments***************" + vChildrenAttachments.get(0).toString());
		//System.out.println("vChildrenDatasets***************" + vChildrenDatasets.get(0).toString());
	}

	/**
	 * Create contents of the dialog.
	 * @param parent
	 */
	protected Control createDialogArea(Composite parent)
	{
		parent.setToolTipText("");
		Composite container = (Composite) super.createDialogArea(parent);
		container.setFont(SWTResourceManager.getFont(common.FONT_TYPE, 9, SWT.NORMAL));
		container.setLayout(new FormLayout());
		
		Label lblAttachList = new Label(container, SWT.NONE);
		lblAttachList.setFont(SWTResourceManager.getFont(common.FONT_TYPE, 9, SWT.NORMAL));
		FormData fd_lblAttachList = new FormData();
		fd_lblAttachList.right = new FormAttachment(100, -169);
		fd_lblAttachList.left = new FormAttachment(0, 23);
		fd_lblAttachList.top = new FormAttachment(0, 10);
		lblAttachList.setLayoutData(fd_lblAttachList);
		lblAttachList.setText(common.LBL_ATTACHLIST);
	    		
		Label lblComments = new Label(container, SWT.NONE);
		FormData fd_lblComments = new FormData();
		fd_lblComments.left = new FormAttachment(lblAttachList, 0, SWT.LEFT);
		lblComments.setLayoutData(fd_lblComments);
		lblComments.setText(common.LBL_COMMENTS);
		
		txtComments = new Text(container, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL);
		fd_lblComments.bottom = new FormAttachment(txtComments, -6);
		FormData fd_txtComments = new FormData();
		fd_txtComments.left = new FormAttachment(lblAttachList, 0, SWT.LEFT);
		fd_txtComments.top = new FormAttachment(0, 412);
		txtComments.setLayoutData(fd_txtComments);
	    
		Label lblReason = new Label(container, SWT.NONE);
		FormData fd_lblReason = new FormData();
		fd_lblReason.left = new FormAttachment(0, 26);
		fd_lblReason.right = new FormAttachment(100, -295);
		lblReason.setLayoutData(fd_lblReason);
		lblReason.setText(common.LBL_EXPORTREASON);
		
		txtPath = new Text(container, SWT.BORDER);
		FormData fd_txtPath = new FormData();
		fd_txtPath.left = new FormAttachment(0, 23);
		fd_txtPath.bottom = new FormAttachment(100, -10);
		txtPath.setLayoutData(fd_txtPath);

		Button btnBrowse = new Button(container, SWT.NONE);
		fd_txtComments.right = new FormAttachment(btnBrowse, 0, SWT.RIGHT);
		fd_txtPath.right = new FormAttachment(btnBrowse, -19);
		FormData fd_btnBrowse = new FormData();
		fd_btnBrowse.right = new FormAttachment(100, -21);
		fd_btnBrowse.left = new FormAttachment(0, 322);
		fd_btnBrowse.top = new FormAttachment(txtPath, -2, SWT.TOP);
		btnBrowse.setLayoutData(fd_btnBrowse);
		btnBrowse.setText(common.BTN_BROWSE);
		
		//Browse button action selects a path to export 
		btnBrowse.addSelectionListener(new SelectionAdapter() 
	    	{
	    	    private String result;
				public void widgetSelected(SelectionEvent e) {
	    	        DirectoryDialog dialog = new DirectoryDialog(shell, SWT.OPEN);
	    	        result = dialog.open();
	    	        if (result != null)
	    	        {
	 	                txtPath.setText(result);
	    	        }
	    	      }
	    	    });
		
		Label lblSupplierName = new Label(container, SWT.NONE);
		FormData fd_lblSupplierName = new FormData();
		fd_lblSupplierName.left = new FormAttachment(lblAttachList, 0, SWT.LEFT);
		lblSupplierName.setLayoutData(fd_lblSupplierName);
		lblSupplierName.setText(common.LBL_SUPNAME);
		
		txtSupplier = new Text(container, SWT.BORDER);
		fd_lblSupplierName.bottom = new FormAttachment(txtSupplier, -6);
		
		ControlDecoration controlDecoration_1 = new ControlDecoration(lblSupplierName, SWT.RIGHT | SWT.TOP);
		controlDecoration_1.setImage(ResourceManager.getPluginImage(common.PLUGIN_NAME, common.PATH_IMAGE_MANDATORY));
		
		txtSupplier.setToolTipText(common.TOOLTIP_SUPPLIER);
		FormData fd_txtSupplier = new FormData();
		fd_txtSupplier.left = new FormAttachment(lblAttachList, 0, SWT.LEFT);
		fd_txtSupplier.right = new FormAttachment(100, -21);
		txtSupplier.setLayoutData(fd_txtSupplier);
	    
	    Label lblSupplierInfo = new Label(container, SWT.NONE);
	    fd_txtSupplier.bottom = new FormAttachment(lblSupplierInfo, -6);
	    FormData fd_lblSupplierInfo = new FormData();
	    fd_lblSupplierInfo.left = new FormAttachment(lblAttachList, 0, SWT.LEFT);
	    lblSupplierInfo.setLayoutData(fd_lblSupplierInfo);
	    lblSupplierInfo.setText(common.LBL_SUPINFO);
	    
	    txtSupplierInfo = new Text(container, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL);
	    fd_lblSupplierInfo.bottom = new FormAttachment(txtSupplierInfo, -6);
	    FormData fd_txtSupplierInfo = new FormData();
	    fd_txtSupplierInfo.bottom = new FormAttachment(lblComments, -6);
	    fd_txtSupplierInfo.top = new FormAttachment(0, 332);
	    fd_txtSupplierInfo.left = new FormAttachment(lblAttachList, 0, SWT.LEFT);
	    fd_txtSupplierInfo.right = new FormAttachment(100, -21);
	    
	    ControlDecoration controlDecoration_4 = new ControlDecoration(lblComments, SWT.RIGHT | SWT.TOP);
	    controlDecoration_4.setImage(ResourceManager.getPluginImage(common.PLUGIN_NAME, common.PATH_IMAGE_MANDATORY));
	    txtSupplierInfo.setLayoutData(fd_txtSupplierInfo);
	    
	    Label lblRefDoc = new Label(container, SWT.NONE);
	    fd_txtComments.bottom = new FormAttachment(lblRefDoc, -6);
	    FormData fd_lblRefDoc = new FormData();
	    fd_lblRefDoc.left = new FormAttachment(0, 23);
	    lblRefDoc.setLayoutData(fd_lblRefDoc);
	    lblRefDoc.setText(common.LBL_REFDOC);

	    txtRefDoc = new Text(container, SWT.BORDER);
	    fd_lblRefDoc.bottom = new FormAttachment(txtRefDoc, -6);
	    FormData fd_txtRefDoc = new FormData();
	    fd_txtRefDoc.left = new FormAttachment(0, 23);
	    txtRefDoc.setLayoutData(fd_txtRefDoc);
	    
	    Button btnBrowse2 = new Button(container, SWT.NONE);
	    fd_txtRefDoc.right = new FormAttachment(btnBrowse2, -19);
	    fd_txtRefDoc.top = new FormAttachment(btnBrowse2, 2, SWT.TOP);
	    FormData fd_btnBrowse2 = new FormData();
	    fd_btnBrowse2.bottom = new FormAttachment(btnBrowse, -23);
	    fd_btnBrowse2.right = new FormAttachment(100, -21);
	    fd_btnBrowse2.left = new FormAttachment(0, 322);
	    btnBrowse2.setLayoutData(fd_btnBrowse2);
	    btnBrowse2.setText(common.BTN_BROWSE);
	    
	    //Browse button action for file selection 
	    btnBrowse2.addSelectionListener(new SelectionAdapter() 
			    {
					public void widgetSelected(SelectionEvent event)
			    	{
		    			org.eclipse.swt.widgets.FileDialog fd = 
		    				new org.eclipse.swt.widgets.FileDialog(shell, SWT.OPEN);
		    		 	fd.setText(common.TITLE_OPENDIALOG);
		    	        fd.setFilterPath(common.DIR_FILTER);
		    	        String[] filterExt = { common.EXTN_ALLFILES};
		    	        fd.setFilterExtensions(filterExt);
		    	        
		    	        String selected = fd.open();
		    	        sRefDocFileName = fd.getFileName();
		    	       //System.out.println("sRefDocFileName" + sRefDocFileName);
		    	        
		    	        if (selected != null)
		    	        {
		 	                txtRefDoc.setText(selected);
		    	        }
			    	}
			    });
     
	    Label lblExportPath = new Label(container, SWT.NONE);
	    FormData fd_lblExportPath = new FormData();
	    fd_lblExportPath.left = new FormAttachment(0, 23);
	    fd_lblExportPath.bottom = new FormAttachment(txtPath, -6);
	    lblExportPath.setLayoutData(fd_lblExportPath);
	    lblExportPath.setText(common.LBL_EXPORTPATH);
	    
	    ControlDecoration controlDecoration_3 = new ControlDecoration(lblExportPath, SWT.RIGHT | SWT.TOP);
	    controlDecoration_3.setImage(ResourceManager.getPluginImage(common.PLUGIN_NAME, common.PATH_IMAGE_MANDATORY));

	    comboExpReason = new Combo(container, SWT.READ_ONLY);
	    fd_lblReason.bottom = new FormAttachment(comboExpReason, -6);
	    FormData fd_comboExpReason = new FormData();
	    fd_comboExpReason.bottom = new FormAttachment(lblSupplierName, -6);
	    fd_comboExpReason.left = new FormAttachment(lblAttachList, 0, SWT.LEFT);
	    fd_comboExpReason.right = new FormAttachment(lblAttachList, 0, SWT.RIGHT);
	    comboExpReason.setLayoutData(fd_comboExpReason);
	    
	    //Populate comboExpReason
	    String[] saLOVVALUES = null;
	    TCComponentListOfValues LOV= TCComponentListOfValuesType.findLOVByName(common.LOV_EXPORTREASON);
	    try 
	    {
			saLOVVALUES = LOV.getListOfValues().getStringListOfValues();
		}
	    catch (TCException e) 
	    {
	    	System.out.println("***ExportData:Error in getting LOV U4_ExportReasonLOV " 
				+ e.toString());
	    }
	    //Set LOV values to combo box
	    comboExpReason.setItems(saLOVVALUES);

	    checkboxTableViewer = CheckboxTableViewer.newCheckList(container, SWT.BORDER | SWT.FULL_SELECTION);
	    table_1 = checkboxTableViewer.getTable();
	    FormData fd_table_1 = new FormData();
	    fd_table_1.right = new FormAttachment(100, -43);
	    fd_table_1.left = new FormAttachment(0, 23);
	    fd_table_1.top = new FormAttachment(lblAttachList, 6);

	    
	    checkboxTableViewer.setContentProvider(new ContentProvider());
	    checkboxTableViewer.setLabelProvider(new TableLabelProvider());
	    
	    ControlDecoration controlDecoration_2 = new ControlDecoration(lblReason, SWT.RIGHT | SWT.TOP);
	    controlDecoration_2.setImage(ResourceManager.getPluginImage(common.PLUGIN_NAME, common.PATH_IMAGE_MANDATORY));
	    
	    ControlDecoration controlDecoration_5 = new ControlDecoration(lblAttachList, SWT.RIGHT | SWT.TOP);
	    controlDecoration_5.setImage(ResourceManager.getPluginImage(common.PLUGIN_NAME, common.PATH_IMAGE_MANDATORY));
	    table_1.setLayoutData(fd_table_1);
	    
	    Label lblChildrenDatasets = new Label(container, SWT.NONE);
	    fd_table_1.bottom = new FormAttachment(lblChildrenDatasets, -6);
	    FormData fd_lblChildrenDatasets = new FormData();
	    fd_lblChildrenDatasets.left = new FormAttachment(lblAttachList, 0, SWT.LEFT);
	    lblChildrenDatasets.setLayoutData(fd_lblChildrenDatasets);
	    lblChildrenDatasets.setText(common.LBL_CHILD_DS);
	    
	    final TableViewer tableViewer = new TableViewer(container, SWT.BORDER | SWT.FULL_SELECTION);
	    table = tableViewer.getTable();
	    fd_lblChildrenDatasets.bottom = new FormAttachment(table, -6);
	    FormData fd_table = new FormData();
	    fd_table.right = new FormAttachment(100, -43);
	    fd_table.left = new FormAttachment(0, 23);
	    fd_table.bottom = new FormAttachment(lblReason, -6);
	    fd_table.top = new FormAttachment(0, 144);
	    table.setLayoutData(fd_table);
	    
	    tableViewer.setContentProvider(new ContentProvider());
	    tableViewer.setLabelProvider(new TableLabelProviderChildren());
	    
	   /* table.addListener(SWT.EraseItem, new Listener() {
										@Override
										public void handleEvent(Event event)
										{
											// TODO Auto-generated method stub
								            if((event.detail & SWT.SELECTED) != 0 )
								            {
								                event.detail &= ~SWT.SELECTED;
								            }
										}
								    });
	    */
	    
	    tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
							        @Override
							        public void selectionChanged(final SelectionChangedEvent event) {
							            if (!event.getSelection().isEmpty()) {
							            	tableViewer.setSelection(StructuredSelection.EMPTY);
							            }
							        }

	    							});
	    
		try 
		{
			//System.out.println("attchs length ***** "+ vChildrenDatasets.size());
			for (int ds = 0; ds < vParentDatasets.size(); ds++)
			{
				sParentDatasetName = vParentDatasets.get(ds).getProperty(common.PROP_OBJ_NAME);
				//salDatasetNames.add(sDatasetName);
				
				String dsType = vParentDatasets.get(ds).getType();
				String sDatasetWithType = sParentDatasetName + common.SPLIT_PREF + dsType;
				salParentDatasetWithType.add(sDatasetWithType);
				
				String sDatasetName2 = sParentDatasetName + "   (" + dsType + ")" ;
				salParentDatasetNames.add(sDatasetName2);
			}
		} catch (TCException e1) {e1.printStackTrace();	}
		
		try 
		{
			//System.out.println("attchs length ***** "+ vChildrenDatasets.size());
			for (int ds = 0; ds < vChildrenDatasets.size(); ds++)
			{
				sChildrenDatasetName = vChildrenDatasets.get(ds).getProperty(common.PROP_OBJ_NAME);
				//salDatasetNames.add(sDatasetName);
				
				String dsType = vChildrenDatasets.get(ds).getType();
				String sDatasetWithType = sChildrenDatasetName + common.SPLIT_PREF + dsType;
				salChildrenDatasetWithType.add(sDatasetWithType);
				
				String sDatasetName2 = sChildrenDatasetName + "   (" + dsType + ")" ;
				salChildrenDatasetNames.add(sDatasetName2);
			}
		} catch (TCException e1) {e1.printStackTrace();	}
		
		
		Object[] osParentsStrings = salParentDatasetNames.toArray();	
		checkboxTableViewer.setInput(osParentsStrings);

	    Object[] oaChildrenStrings = salChildrenDatasetNames.toArray();	
	    tableViewer.setInput(oaChildrenStrings);

		return container;
	}
	
	@Override
	protected void configureShell(Shell shell)
	{
	   super.configureShell(shell);
	   shell.setText("Export Data");
	}
	
	/**
	 * Provides content(dataset nodes) for table 
	 */
	private static class ContentProvider implements IStructuredContentProvider
	{
		public Object[] getElements(Object inputElement) 
		{
			return (Object[]) inputElement;
		}
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	
	/**
	 * Provides content(parent dataset names) for table 
	 */
	private class TableLabelProvider extends LabelProvider implements ITableLabelProvider
	{
		public Image getColumnImage(Object element, int columnIndex) 
		{
			String sType = null;

			//find dataset type
			for (int i = 0; i < salParentDatasetWithType.size(); i++ )
			{
				Object[] salDatasetWithTypeArray = salParentDatasetWithType.toArray();
				String[] sTokens = salDatasetWithTypeArray[i].toString().split(common.SPLIT_PREF);
				
				if (	(element.toString().contains(sTokens[0])) 	&&
						(element.toString().contains(sTokens[1]))	)
				{
					sType = sTokens[1];
				}
			}	
			Image image = TCTypeRenderer.getTypeImage(sType, null);
			
			//Image image = Registry.getRegistry(AbstractTCApplication.class ).getImage(sType + common.EXTN_ICON );
			return image;

		}
		public String getColumnText(Object element, int columnIndex)
		{
			return element.toString();
		}
		@Override
		public void addListener(ILabelProviderListener paramILabelProviderListener) 
		{
		}
		@Override
		public void dispose() 
		{
		}
		@Override
		public boolean isLabelProperty(Object paramObject, String paramString)
		{
			return false;
		}
		@Override
		public void removeListener(ILabelProviderListener paramILabelProviderListener) 
		{
		}
	}
	
	/**
	 * Provides content(children dataset names) for table 
	 */
	private class TableLabelProviderChildren extends LabelProvider implements ITableLabelProvider
	{
		public Image getColumnImage(Object element, int columnIndex) 
		{
			String sType = null;

			//find dataset type
			for (int i = 0; i < salChildrenDatasetWithType.size(); i++ )
			{
				Object[] salDatasetWithTypeArray = salChildrenDatasetWithType.toArray();
				String[] sTokens = salDatasetWithTypeArray[i].toString().split(common.SPLIT_PREF);
				
				if (	(element.toString().contains(sTokens[0])) 	&&
						(element.toString().contains(sTokens[1]))	)
				{
					sType = sTokens[1];
				}
			}	
			Image image = TCTypeRenderer.getTypeImage(sType, null);
			
			//Image image = Registry.getRegistry(AbstractTCApplication.class ).getImage(sType + common.EXTN_ICON );
			return image;

		}
		public String getColumnText(Object element, int columnIndex)
		{
			return element.toString();
		}
		@Override
		public void addListener(ILabelProviderListener paramILabelProviderListener) 
		{
		}
		@Override
		public void dispose() 
		{
		}
		@Override
		public boolean isLabelProperty(Object paramObject, String paramString)
		{
			return false;
		}
		@Override
		public void removeListener(ILabelProviderListener paramILabelProviderListener) 
		{
		}
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
		return new Point(408, 650);
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
		//Verify user inputs
		sRefDoc = txtRefDoc.getText().toString();
		sCommentsValue = txtComments.getText().toString();
		sSupplierValue = txtSupplier.getText().toString();
		sSupplierInfoValue = txtSupplierInfo.getText().toString();
		sExportReason = comboExpReason.getText().toString();
		selectedDatasets  = checkboxTableViewer.getCheckedElements();
		sExportPath  = txtPath.getText().toString();
		
		//Prepare supplier directory
		String sSupDir = sSupplierValue;
		sSupDir = sSupDir.concat(common.UNDERSCORE);
		sSupDir = sSupDir.concat(common.getTimestamp());
		//System.out.println("supDir " + sSupDir);
		
		//Prepare supplier comments file
		String sCommentsFileName = sSupDir;
		sCommentsFileName = sCommentsFileName.concat(common.EXTN_TXT);
		//System.out.println("comments file sFileName: " + sCommentsFileName);
		
		//Create directory for temporary file storage before Zip it
		sLocValue = System.getenv(common.ENV_TEMP);
		sLocValue = sLocValue.concat(common.FL_DOUBLESEPERATOR);
		sLocValue = sLocValue.concat(sSupDir);
		//System.out.println("sLocValue temp " + sLocValue);
		File dir = new File(sLocValue);
		dir.mkdir();
		
		//Create Zip file name with location(export location)
		sLocValueZIP = txtPath.getText().toString();
		sLocValueZIP = sLocValueZIP.concat(common.FL_DOUBLESEPERATOR);
		sLocValueZIP = sLocValueZIP.concat(sSupDir);
		sLocValueZIP = sLocValueZIP.concat(common.EXTN_ZIP);

		if(isExportDialogInputValid())
		{
			super.okPressed();

			//Create "U4_ExportForm" to store export info
			try 
			{
				//New form name
				String timestamp = common.getTimestamp();
				String sNewFormName = sSupplierValue+common.UNDERSCORE+timestamp;
				//System.out.println("sRefDocFileName2" + sRefDocFileName);
				form = common.createForm(sNewFormName, common.TYPE_EXPORTDATAFORM);
				populateExportForm(form, sSupplierValue, sSupplierInfoValue, 
									sExportReason, sCommentsValue,
									sRefDocFileName, sExportPath, sCommentsFileName );
				alObjectsToRelease.add(form);
			} 
			catch (TCException e) 
			{
				System.out.println("***ExportData:Error in createDataExportForm / populateExportForm function " 
									+ e.toString());
				isExportFailed  = true;
			}
			
			//Attach "U4_ExportForm" under target IR
			try 
			{
				itemRevision.add(common.RLN_EXPBKP, form);
				itemRevision.refresh();
			} 
			catch (TCException e)
			{
				System.out.println("***ExportData:Error in attaching 'U4_DataExportForm' form under IR "
									+ e.toString());
				isExportFailed  = true;
			}
			
			ArrayList<TCComponentDataset> datasetToExport = prepareDatasetsToExport(selectedDatasets);
			
			for (int i = 0; i < datasetToExport.size(); i++)
			{	
				TCComponentDatasetDefinition dsDef = new TCComponentDatasetDefinition();
				String[] sNamedRefs = null;
				try 
				{
					dsDef = datasetToExport.get(i).getDatasetDefinitionComponent();
					sNamedRefs = dsDef.getNamedReferences();

					/*
					 *	backupDatasetUnderForm function does following 
					 *	1. Export dataset files to location
					 *	2. Save-As the dataset with timestamp
					 *	3. Paste the new dataset under given form object
					*/
					/*
					 *	***Passing only the first reference type; looping all 
					 *  references takes time to finish
					*/
					backupDatasetUnderExportForm(form, 
													datasetToExport.get(i), 
													sNamedRefs[0], 
													common.RLN_EXPBKP, 
													sLocValue);		
				}
				catch (TCException e) 
				{
					System.out.println("***ExportData:Error in backupDatasetUnderExportForm function" 
							+ e.toString());
					isExportFailed  = true;
				}

			}

			if(!sRefDoc.isEmpty())
			{
				try
				{
					/*
					 *	Copy reference document to temp directory
					*/
					common.copyFile(sRefDoc, sLocValue);
				} 
				catch (IOException e) 
				{
					System.out.println("***ExportData:Error in copyFile function" 
										+ e.toString());
					isExportFailed  = true;
				}
			}
			
			try
			{
				createInfoFile(sLocValue, sCommentsFileName);
			}
			catch (IOException e) 
			{
				System.out.println("***ExportData:Error in createInfoFile function" 
									+ e.toString());
				isExportFailed  = true;
			}
			
			try 
			{
				common.createZIPFile(sLocValueZIP, sLocValue);
			}
			catch (IOException e) 
			{
				System.out.println("***ExportData:Error in createZIPFile function" 
						+ e.toString());
				isExportFailed  = true;
			}
			
			/*try 
			{
				common.releaseObjects(alObjectsToRelease, common.PROCESS_EXPORT);	
			}
			catch (TCException e) 
			{
				System.out.println("***ExportData:Error in releaseFormAndDatasets function" 
									+ e.toString());
				isExportFailed  = true;
			}	/*/
			
			if(isExportFailed)
			{
				com.teamcenter.rac.util.MessageBox mb = 
						new com.teamcenter.rac.util.MessageBox(common.ExportFailedMsg, common.ExportFailed, 1);
                mb.setModal(true);
                mb.setVisible(true);
			}
			else
			{
				com.teamcenter.rac.util.MessageBox mb = 
						new com.teamcenter.rac.util.MessageBox(common.ExportSuccessMsg, common.ExportSuccess, 2);
                mb.setModal(true);
                mb.setVisible(true);
			}
		}
	}
	
	/**
	 * Populates export form with given values
	 * @param form
	 * @param sSupplierValue
	 * @param sSupplierInfoValue
	 * @param sExportReason
	 * @param sCommentsValue
	 * @param sRefDocFileName
	 * @param sExportPath
	 * @param sCommentsFileName
	 * @throws TCException
	 */
	private void populateExportForm(TCComponentForm form,
								String sSupplierValue, String sSupplierInfoValue,
								String sExportReason, String sCommentsValue, 
								String sRefDocFileName, String sExportPath, 
								String sCommentsFileName) throws TCException 
	{
		if (sCommentsValue != null )
			form.setProperty(common.PROP_COMMENTS, sCommentsValue.trim());
		if (sSupplierValue != null )
			form.setProperty(common.PROP_SUPPLIERNAME, sSupplierValue.trim());
		if (sExportReason != null )
			form.setProperty(common.PROP_SUPPLIERINFO, sSupplierInfoValue.trim());
		if (sExportReason != null )
			form.setProperty(common.PROP_REASON, sExportReason.trim());
		if (sRefDocFileName != null ) 
			form.setProperty(common.PROP_REFDOC, sRefDocFileName.trim());
		if (sExportPath != null )
			form.setProperty(common.PROP_EXPLOC, sExportPath.trim());
		if (sCommentsFileName != null )
			form.setProperty(common.PROP_COMMENTSFILE, sCommentsFileName.trim());
		form.refresh();
	}

	/**
	 * Create supplier info file
	 * @param sLocValue
	 * @param sCommentsFileName
	 */
	private void createInfoFile(String sLocValue, String sCommentsFileName) throws IOException
	{
		final File fPath = new File(sLocValue);
		String sLocValueAP = fPath.getAbsolutePath().replaceAll("\\\\", "\\\\\\\\");
		sLocValueAP = sLocValueAP.concat("\\\\");
		sLocValueAP = sLocValueAP.concat(sCommentsFileName);
		
		File file = new File(sLocValueAP);
		BufferedWriter output = new BufferedWriter(new FileWriter(file));
		output.write("Reason for change: " + sExportReason + "\n");
		output.write("Comments: " + sCommentsValue);
		output.close();
	}
	
	/**
	 * Collect datasets to be exported
	 * @param selectedDatasets
	 * @return isValid
	 */
	private ArrayList<TCComponentDataset> prepareDatasetsToExport(Object[] selectedDatasets)
	{
		ArrayList<TCComponentDataset> datasetToExport = new ArrayList <TCComponentDataset> ();
		
		for (int i = 0; i < selectedDatasets.length; i++)
		{
			//System.out.println("selectedDatasets " + selectedDatasets[i].toString());
			String[] tokens = selectedDatasets[i].toString().split("\\(");
			String sDSName = tokens[0];
			String[] sTemp = tokens[1].split("\\)");
			String sDSType = sTemp[0];
			
			for (int ds = 0; ds < vParentDatasets.size(); ds++)
			{			
				String sDatasetName = null;
				String sDatasetType = null;
				try 
				{
					sDatasetName = vParentDatasets.get(ds).getProperty(common.PROP_OBJ_NAME);
					sDatasetType = vParentDatasets.get(ds).getType();
				
				} catch (TCException e) {e.printStackTrace();}
				
				if(		sDatasetName.equals(sDSName.trim()) &&
						sDatasetType.equals(sDSType.trim())	)
				{
					datasetToExport.add((TCComponentDataset) vParentDatasets.get(ds));
					break;
				}
			}
		}
		
		//add all children datasets to export
		for (int dsc = 0; dsc < vChildrenDatasets.size(); dsc++)
		{
			datasetToExport.add((TCComponentDataset)vChildrenDatasets.get(dsc));
		}
		
		return datasetToExport;
		
	}
	
	/**
	 * Validates user dialog inputs
	 * @return isValid
	 */
	private boolean isExportDialogInputValid() 
	{
		boolean isValid = false;
		
		if (sCommentsValue.trim().isEmpty() || 
				sLocValue.trim().isEmpty() || 
				sSupplierValue.trim().isEmpty() ||
				sExportReason.trim().isEmpty() ||
				selectedDatasets.length == 0)
		{
			MessageBox box = new MessageBox(getShell(), SWT.ERROR);
			box.setText(common.ValueMissing);
			box.setMessage(common.ValueMissingMsg);
			box.open();
		}
		else if(!common.validatePath(sExportPath))
		{
			MessageBox box = new MessageBox(getShell(), SWT.ERROR);
			box.setText(common.PathMissing);
			box.setMessage(common.PathMIssingMsg);
			box.open();
		}
		else if ((!sRefDoc.isEmpty()) && (!common.validateFile(sRefDoc)))
		{	
			MessageBox box = new MessageBox(getShell(), SWT.ERROR);
			box.setText(common.FileMissing);
			box.setMessage(common.FileMissingMsg);
			box.open();
		}
		else 
		{
			isValid = true;
		}
		return isValid;
	}

	/**
	 * 	backupDatasetUnderForm function does following 
	 *	1. Export dataset files to location
	 *	2. Save-As the dataset with timestamp
	 *	3. Paste the new dataset under given form object
	 * @param form
	 * @param dataset
	 * @param NRef
	 * @param sRelation
	 * @param sLocationValue
	 * @throws TCException 
	 */
	private void backupDatasetUnderExportForm(TCComponentForm form, TCComponentDataset dataset, String NRef, String sRelation, String sLocationValue) throws TCException 
	{
		//Build name string for dataset
		String sDatasetName = null;
		sDatasetName = dataset.getProperty(common.PROP_OBJ_NAME);
		String sTimestamp = common.getTimestamp();
		String sNewPDFName = sDatasetName + common.UNDERSCORE + sTimestamp;
		
		dataset.getFiles(NRef, sLocationValue);
		
		//Save the PDF dataset with new name under form
		TCComponentDataset newDataset = dataset.saveAs(sNewPDFName);
		//System.out.println("adding ds " + dsType+" to alObjectsToRelese");
		alObjectsToRelease.add(newDataset);
		form.add(sRelation, newDataset);
		form.refresh();
	}
}
