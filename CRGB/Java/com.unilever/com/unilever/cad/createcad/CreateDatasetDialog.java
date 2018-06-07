package com.unilever.cad.createcad;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.SWTResourceManager;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentDatasetType;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.Registry;
import com.unilever.rac.ui.common.UL4Common;

public class CreateDatasetDialog extends Dialog {

	protected Object result;
	protected Shell shell;
	
	private TCSession mSession = null;
	private TCComponent mComponent = null;
	private Registry mRegistry = null;
	
	private Composite headerComposite;
	private Composite mDataComposite;
	private Label lblComponentType;
	private Label lblTechnology;
	private Label lblCompIdValue;
	private Label lblCompTypeValue;
	private Label lblTechValue;
	

	private Map<String, GMCCompType> mTechnology;
	private final String _GMC_PACK_COMP_PREF = "U4_GMCPackComponentType";
	private Composite ugpartSelectComposite;
	
	private Vector<Button> mDwgSelection;
	private Composite composite;
	
	private Vector<TCComponent> mCreated;

	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 * @param mSession 
	 * @param mSelected 
	 */
	public CreateDatasetDialog(Shell parent, int style, TCComponentItemRevision mSelected, TCSession mSession) {
		super(parent, style);
		
		this.mSession = mSession;
		mComponent = mSelected;
		mRegistry = Registry.getRegistry( "com.unilever.cad.createcad.createcad" );
		
		this.setText( "Create CAD data (NX only)" );
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open() {
		initializeTechMap();
		createContents();
		shell.open();
		shell.layout();
		Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents( ) {
		shell = new Shell(getParent(), getStyle());
		shell.setBackground(SWTResourceManager.getColor(SWT.COLOR_LIST_BACKGROUND));
		shell.setImage( mRegistry.getImage( "title.ICON" ));
		shell.setSize(450, 415);
		shell.setText(getText());
		shell.setBackgroundMode( SWT.INHERIT_DEFAULT );
		
		getHeaderComposite( );
		
		mDataComposite = new Composite(shell, SWT.BORDER);
		FormData fd_mDataComposite = new FormData();
		fd_mDataComposite.top = new FormAttachment(headerComposite, 12);
		fd_mDataComposite.left = new FormAttachment(0, 10);
		fd_mDataComposite.right = new FormAttachment(100, -10);
		mDataComposite.setLayoutData(fd_mDataComposite);
		
		lblComponentType = new Label(mDataComposite, SWT.NONE);
		lblComponentType.setBounds(10, 27, 38, 15);
		lblComponentType.setText("Name: ");
		lblComponentType.pack( );
		
		lblTechnology = new Label(mDataComposite, SWT.NONE);
		lblTechnology.setBounds(10, 58, 75, 15);
		lblTechnology.setText("Technology: ");
		lblTechnology.pack( );
		
		lblCompTypeValue = new Label(mDataComposite, SWT.NONE);
		lblCompTypeValue.setBounds(87, 27, 55, 15);
		try {
			lblCompTypeValue.setText( mComponent.getStringProperty( "object_name" ));
			lblCompTypeValue.pack();
		} catch (TCException e) {
			e.printStackTrace();
		}
		
		lblTechValue = new Label(mDataComposite, SWT.NONE);
		lblTechValue.setBounds(100, 58, 120, 15);
		
		String comptype = null;
		
		if( mTechnology != null )
			try {
				comptype = mComponent.getStringProperty( "object_type" );
				
				if(comptype.equalsIgnoreCase(UL4Common.CAD_COMPONENT_ITEM_REV) == true)////Technology not applicable for CADComponent
				{
					lblTechnology.setText("Type: ");
					lblTechValue.setText(mComponent.getDisplayType() );
				}
				else
				{
				lblTechValue.setText( mTechnology.get( comptype ).tech );
				}
				
				lblTechValue.pack();
			} catch (TCException e) {
				e.printStackTrace();
			}
		
		ugpartSelectComposite = new Composite(shell, SWT.BORDER);
		fd_mDataComposite.bottom = new FormAttachment(ugpartSelectComposite, -6);
		FormData fd_ugpartSelectComposite = new FormData();
		fd_ugpartSelectComposite.top = new FormAttachment(0, 204);
		fd_ugpartSelectComposite.left = new FormAttachment(0, 10);
		fd_ugpartSelectComposite.right = new FormAttachment(100, -10);
		ugpartSelectComposite.setLayoutData(fd_ugpartSelectComposite);
		
		Label lblSelectDrawingTemplates = new Label(ugpartSelectComposite, SWT.NONE);
		lblSelectDrawingTemplates.setFont(SWTResourceManager.getFont("Segoe UI", 9, SWT.BOLD));
		lblSelectDrawingTemplates.setForeground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_FOREGROUND));
		lblSelectDrawingTemplates.setBounds(10, 10, 424, 15);
		lblSelectDrawingTemplates.setText("Select sheet size(s) for the drawing");
		
		composite = new Composite(ugpartSelectComposite, SWT.NONE);
		composite.setBounds(10, 31, 400, 88);
			
		Vector<String> Options2D = getCADTemplateTypes( comptype );
		
		if( Options2D.size() <= 0 )
			lblSelectDrawingTemplates.setText( "No drawing templates available" );
		
		mDwgSelection = new Vector<>();
		for( int inx = 0; inx < Options2D.size( ); inx++ )
		{			
			Button mButton = new Button( composite, SWT.CHECK );
			mButton.setBounds(100 + (inx%2)*100, 10 + (inx/2)*30, 93, 16);
			mButton.setText( Options2D.get( inx ));
			
			mDwgSelection.add( mButton );
		}
		
		Composite controlComposite = new Composite(shell, SWT.NONE);
		fd_ugpartSelectComposite.bottom = new FormAttachment(controlComposite, -6);
		FormData fd_controlComposite = new FormData();
		fd_controlComposite.top = new FormAttachment(0, 336);
		fd_controlComposite.bottom = new FormAttachment(100, -10);
		fd_controlComposite.right = new FormAttachment(mDataComposite, 0, SWT.RIGHT);
		fd_controlComposite.left = new FormAttachment(mDataComposite, 0, SWT.LEFT);
		controlComposite.setLayoutData(fd_controlComposite);
		controlComposite.setBackground(SWTResourceManager.getColor(SWT.COLOR_LIST_BACKGROUND));
		
		Button btnCreate = new Button(controlComposite, SWT.NONE);
		btnCreate.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				boolean choice = false;
				boolean is_ugm_created = checkIfUgMasterCreated( mComponent );
				
				for( Button checkbutton : mDwgSelection)
				{
					choice = choice | checkbutton.getSelection();
				}
				
				if( !choice && is_ugm_created )
				{
					MessageBox msgBox = new MessageBox( shell );
					msgBox.setText( "Warning" );
					msgBox.setMessage( "No drawing template is selected" );
					msgBox.open();
					return;
					
				}
				
				String template_str = new String( );
				String mDisplayName = null;
				
				((Button)e.getSource( )).getParent().dispose();
				
				mDisplayName = getUGDatasetString( mComponent );
				
				for( Button checkbutton : mDwgSelection)
				{
					if( checkbutton.getSelection())
					{
						if( template_str.length() == 0 )
							template_str = checkbutton.getText( );
						else
							template_str += "," + checkbutton.getText( );
						
					}
				}
/*				
				try {
					mSelected.setStringProperty( "u4_dwg_templates", template_str );
				} catch (TCException ex) {
					ex.printStackTrace();
				}
*/				try {
					TCComponentDatasetType mDatasetType = (TCComponentDatasetType) mSession.getTypeComponent( "Dataset" );
					mCreated = new Vector<>();
					
					if( !is_ugm_created )
					{
						TCComponentDataset mDataset = mDatasetType.create( mDisplayName, "UGMASTER-LoadTemplate", "UGMASTER" );
						mCreated.add( mDataset );
					}
					int count = getUGPARTCount( mComponent );
					for( Button checkbutton : mDwgSelection )
					{
						if( checkbutton.getSelection( ))
						{
							TCComponentDataset ugpart = mDatasetType.create( mDisplayName+ "-dwg" + (++count) , checkbutton.getText(), "UGPART" );
							mCreated.add( ugpart );
						}
					}
				} catch (TCException ex) {
					ex.printStackTrace();
				}
				
				if( !mCreated.isEmpty( ))
				{
					try {
						List<TCComponent> compList = new ArrayList<>();
						TCComponent[] mRelated = mComponent.getRelatedComponents( "IMAN_specification" );
						compList.addAll( Arrays.asList( mRelated ));
						compList.addAll( Arrays.asList( mCreated.toArray( new TCComponent[mCreated.size( )])));
						System.out.println( mCreated.size());
						System.out.println( compList.size() + "");
						mComponent.setRelated( "IMAN_specification", compList.toArray( new TCComponent[compList.size( )]));
						
						mComponent.lock();
						mComponent.setLogicalProperty(UL4Common.PACK_COMPONENT_USES_TEMPLATE_ATTRIBUTE_NAME, true);
						mComponent.save();
						mComponent.unlock();
						mComponent.refresh();
					} catch (TCException ex ) {
						MessageBox message = new MessageBox( shell );
						message.setText( "ERROR" );
						message.setMessage( ex.getMessage( ));
						message.open( );
						ex.printStackTrace();
					}
				}
				
				shell.dispose();
			}
		});
		btnCreate.setBounds(103, 10, 75, 25);
		btnCreate.setText("Create");
		
		Button btnCancel = new Button(controlComposite, SWT.NONE);
		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				((Button)e.getSource( )).getShell().dispose( );
				
			}
		});
		btnCancel.setBounds(223, 10, 75, 25);
		btnCancel.setText("Cancel");
		
	}
	
	private Composite getHeaderComposite( )
	{
		shell.setLayout(new FormLayout());
		headerComposite = new Composite(shell, SWT.NONE);
		FormData fd_headerComposite = new FormData();
		fd_headerComposite.bottom = new FormAttachment(0, 80);
		fd_headerComposite.right = new FormAttachment(0, 445);
		fd_headerComposite.top = new FormAttachment(0);
		fd_headerComposite.left = new FormAttachment(0);
		headerComposite.setLayoutData(fd_headerComposite);
		headerComposite.setBackgroundImage( mRegistry.getImage( "DIALOGBG" ));
		
		Label lblHeader = new Label(headerComposite, SWT.NONE );
		lblHeader.setSize(136, 37);
		lblHeader.setLocation(10, 21);
		lblHeader.setText( "" );
		lblHeader.setFont(SWTResourceManager.getFont("Segoe UI", 18, SWT.NORMAL));
		try {
			lblHeader.setText( mComponent.getStringProperty( "item_id" ) + "/" + mComponent.getStringProperty( "item_revision_id" ));
		} catch (TCException e) {
			e.printStackTrace();
		}
		lblHeader.pack();
		
		return headerComposite;	
	}
	

	
	void initializeTechMap() 
	{
		String[] prefs = mSession.getPreferenceService().getStringValues( _GMC_PACK_COMP_PREF );
		
		mTechnology = new HashMap<String, GMCCompType>();
		
		for( String each : prefs )
		{
			StringTokenizer mTokenizer = new StringTokenizer( each, "#" );
			
			GMCCompType mCompType = new GMCCompType( mTokenizer.nextToken( ),
														mTokenizer.nextToken( ),
														mTokenizer.nextToken( ));
			mTechnology.put( mCompType.comptype + "Revision", mCompType );
		}
	}
	
	class GMCCompType
	{
		String pamspec;
		String comptype;
		String tech;
		
		public GMCCompType( String pamspec, String tech, String comptype ) {
			this.pamspec = pamspec;
			this.comptype = comptype;
			this.tech = tech;
		}
	}
	
	private String getUGDatasetString( TCComponent comp )
	{
		String dsname = new String( );
		try {
			dsname = comp.getStringProperty( "item_id" ) + "-" 
					+ comp.getStringProperty( "item_revision_id" );
		} catch (TCException e) {
			e.printStackTrace();
		}
		return dsname;
	}
	
	private boolean checkIfUgMasterCreated( TCComponent comp )
	{
		try {
			TCComponent[] seconds = comp.getRelatedComponents( "IMAN_specification" );
			
			for( TCComponent each : seconds )
			{
				if( each.getType().equalsIgnoreCase( "UGMASTER" ))
					return true;
			}
		} catch (TCException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	private int getUGPARTCount( TCComponent comp )
	{
		int count = 0;
		try {
			TCComponent[] seconds = comp.getRelatedComponents( "IMAN_specification" );
			
			for( TCComponent each : seconds )
			{
				if( each.getType().equalsIgnoreCase( "UGPART" ))
					count++;
			}
		} catch (TCException e) {
			e.printStackTrace();
		}
		return count;
	}
	

	
	Vector<String> getCADTemplateTypes( String comptype )
	{
		String[] mTemplateTypes = mSession.getPreferenceService().getStringValues( "U4_GMC_Component_CAD_Templates" );
		
		Vector<String> output = new Vector<String>();
		
		for( String each : mTemplateTypes )
		{
			StringTokenizer mTokenizer = new StringTokenizer( each, "," );
			
			if( (mTokenizer.nextToken( ":" )).equalsIgnoreCase( comptype ))
			{
				StringTokenizer drgTokenizer = new StringTokenizer(mTokenizer.nextToken( ), "," );
				while( drgTokenizer.hasMoreTokens( ))
					output.add( drgTokenizer.nextToken(","));
			}
		}
		return output;
	}
}
