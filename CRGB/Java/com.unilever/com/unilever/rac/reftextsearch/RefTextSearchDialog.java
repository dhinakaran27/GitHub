package com.unilever.rac.reftextsearch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.common.TCUtilities;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentQuery;
import com.teamcenter.rac.kernel.TCComponentQueryType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;

public class RefTextSearchDialog extends Dialog {

	protected Object result;
	protected Shell shell;
	
	private TCSession mSession = null;
	private TCComponent mComponent = null;
	private Map mParam = null;
	
	private Vector<Text> vText = null;
	
	private Button btnAdd = null;
	private Button btnExecute = null;
	
	private TableViewer tableViewer = null;
	
	private ColumnData[] mColumnData = null;
	

	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 * @param mParamMap 
	 * @param selection 
	 */
	public RefTextSearchDialog(Shell parent, int style, TCComponent selection, TCSession session, Map mParamMap ) {
		super(parent, style);
		
		mSession = session;
		mComponent = selection;
		mParam = mParamMap;
		
		vText = new Vector<Text>( );
		
		setText(( String )mParam.get( "title" ));
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open() {
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
	private void createContents() {
		shell = new Shell( getParent(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL );
		shell.setText(getText( ));
		
		initializeColumnData( (String) mParam.get( "type" ), (String) mParam.get( "columns" ));
		shell.setLayout(new FormLayout());
		
		Composite mDescComposite = new Composite( shell, SWT.NONE );
		FormData fd_mDescComposite = new FormData();
		fd_mDescComposite.bottom = new FormAttachment(0, 32);
		fd_mDescComposite.right = new FormAttachment(0, 594);
		fd_mDescComposite.top = new FormAttachment(0);
		fd_mDescComposite.left = new FormAttachment(0);
		mDescComposite.setLayoutData(fd_mDescComposite);
		
		setDescription( mDescComposite, (String) mParam.get( "description" ));
		
		Composite mQueryComposite = new Composite( shell, SWT.BORDER );
		FormData fd_mQueryComposite = new FormData();
		fd_mQueryComposite.top = new FormAttachment(mDescComposite, 6);
		fd_mQueryComposite.bottom = new FormAttachment(0, 136);
		fd_mQueryComposite.right = new FormAttachment(0, 594);
		fd_mQueryComposite.left = new FormAttachment(0);
		mQueryComposite.setLayoutData(fd_mQueryComposite);
		mQueryComposite.setLayout( new GridLayout( 3, false ));
		buildQueryComposite( mQueryComposite, (String) mParam.get( "queryCriteria" ));
		new Label(mQueryComposite, SWT.NONE);
		new Label(mQueryComposite, SWT.NONE);
		
		Composite mOutputComposite = new Composite( shell, SWT.BORDER );
		FormData fd_mOutputComposite = new FormData();
		fd_mOutputComposite.top = new FormAttachment(mQueryComposite, 6);
		fd_mOutputComposite.bottom = new FormAttachment(0, 272);
		fd_mOutputComposite.right = new FormAttachment(0, 594);
		fd_mOutputComposite.left = new FormAttachment(0);
		mOutputComposite.setLayoutData(fd_mOutputComposite);
		mOutputComposite.setLayout(new FormLayout());
		buildTableComposite( mOutputComposite );
		
		shell.pack( );
		
		shell.setSize( 600, 300 );
		
		
	}

	private void initializeColumnData( String objecttype, String columns ) {
		
		StringTokenizer mToken = new StringTokenizer( columns, "~" );
		
		mColumnData = new ColumnData[mToken.countTokens() + 1];
		mColumnData[0] = new ColumnData( "#", "", 20 );
		
		for( int inx = 0; mToken.hasMoreTokens( ); inx++ )
		{
			String attrname;
			String displayname;
			int width = 0;
			
			attrname = mToken.nextToken( );
			displayname = TCUtilities.getLocalizedValues( new String[]{ objecttype + "." + attrname })[0];
			
			mColumnData[inx + 1] = new ColumnData( displayname, attrname, width );
		}
		
	}

	private void buildTableComposite(Composite mOutputComposite) {
		
		ScrolledComposite tableComposite = new ScrolledComposite( mOutputComposite, SWT.NONE );
		FormData fd_tableComposite = new FormData();
		fd_tableComposite.bottom = new FormAttachment(100, -5);
		fd_tableComposite.right = new FormAttachment(100, -92);
		fd_tableComposite.top = new FormAttachment(0, 5);
		fd_tableComposite.left = new FormAttachment(0, 5);
		tableComposite.setLayoutData(fd_tableComposite);
		tableComposite.setSize(493, 520);
		tableComposite.setExpandHorizontal(true);
		tableComposite.setExpandVertical(true);
		
		tableViewer = new TableViewer( tableComposite, SWT.FULL_SELECTION | SWT.MULTI|SWT.BORDER );
		tableComposite.setContent( tableViewer.getTable( ));
		tableViewer.getTable( ).setLayoutData( new RowData( -1, 20 ));
		tableViewer.getTable( ).setSize( 85, 45 );
		tableViewer.setContentProvider( new ArrayContentProvider( ));
		
		tableViewer.getTable( ).addListener( SWT.Selection, new Listener() {
			
			@Override
			public void handleEvent( Event event ) {
				
				if( tableViewer.getTable( ).getSelectionCount() > 0 )
					btnAdd.setEnabled( true );
				else
					btnAdd.setEnabled( false );
				
			}
		});
		
		initializeTableColumns( );
		
		btnAdd = new Button(mOutputComposite, SWT.BUTTON1 );
		FormData fd_btnAdd = new FormData();
		fd_btnAdd.top = new FormAttachment(tableComposite, 0, SWT.TOP);
		fd_btnAdd.left = new FormAttachment(tableComposite, 18);
		btnAdd.setLayoutData(fd_btnAdd);
		btnAdd.setText( "Add" );
		btnAdd.setEnabled( false );
		
		btnAdd.addSelectionListener( new SelectionAdapter() {
			
			@Override
			public void widgetSelected( SelectionEvent event ) {
				TableItem[] mSelected = tableViewer.getTable().getSelection();
				
				TCComponent[] mSelectedComponent = new TCComponent[mSelected.length];
				
				for( int inx = 0; inx < mSelected.length; inx++ )
					mSelectedComponent[inx] = (TCComponent) mSelected[inx].getData( );
				
				try {
					//AIFComponentContext[] relatedForms = mComponent.getRelated();	
					
					TCComponent[] finalComponent = mComponent.getRelatedComponents((String)mParam.get( "relation" ));

					finalComponent = Arrays.copyOf( finalComponent, finalComponent.length + mSelectedComponent.length );
					
					for(int inx=0;inx<mSelectedComponent.length;inx++)
					{
						finalComponent[(finalComponent.length - 1)+inx] = mSelectedComponent[inx];
					}					

					mComponent.setRelated( (String)mParam.get( "relation" ), finalComponent ); 

					
					//mComponent.setRelated( (String)mParam.get( "relation" ), mSelectedComponent );
					
					MessageBox message = new MessageBox( shell);
					message.setMessage( "Successfully Added!" );
					message.open( );
				} catch (TCException e) {
					e.printStackTrace();
				}
				
			}
		
		});
		
		mOutputComposite.pack( );		
	}

	private void initializeTableColumns( ) {
		
		for( final ColumnData title : mColumnData )
		{
			TableColumn mTabColumn = new TableColumn( tableViewer.getTable( ), SWT.LEFT | SWT.BORDER );
			mTabColumn.setText( title.displayname );
			if( title.width != 0 ) mTabColumn.setWidth( title.width );
			
			TableViewerColumn mTabViewerColumn = new TableViewerColumn( tableViewer, mTabColumn );
			mTabViewerColumn.setLabelProvider(new ColumnLabelProvider()
			{
/*				
				@Override
				public String getText(Object element) {
					TCComponent comp = (TCComponent) element;
					try {
						if( title.attr_name.length() > 0 )
							return comp.getProperty( title.attr_name );
					} catch (TCException e) {
						e.printStackTrace();
					}
					
					return "";
				}
*/				
				public void update(ViewerCell cell) { 
						if( cell.getColumnIndex() == 0 )
							cell.setText(tableViewer.getTable( ).indexOf((TableItem)cell.getItem()) + 1 +"") ;
						else
						{
							TCComponent comp = (TCComponent)cell.getElement();
							try {
								if( title.attr_name.length() > 0 )
									cell.setText( comp.getProperty( title.attr_name ));
							} catch (TCException e) {
								e.printStackTrace();
							}
						}
					} 
			});
		}
		
		for( TableColumn each : tableViewer.getTable( ).getColumns( ))
			each.pack( );
		
		tableViewer.getTable( ).setHeaderVisible( true );
		tableViewer.getTable( ).pack( true );
	}

	private void buildQueryComposite(Composite mQueryComposite, String attributes ) {
		
		StringTokenizer mAttribToken = new StringTokenizer( attributes, "~" );
		
		while( mAttribToken.hasMoreTokens( ))
		{
			String attribute = mAttribToken.nextToken( );
			
			Label lblField = new Label( mQueryComposite, SWT.NONE );
			lblField.setText( attribute );
			lblField.pack( );
			
			Text mText = new Text( mQueryComposite, SWT.NONE );
			mText.setData( attribute );
			mText.setSize( 1000, 21 );
			
			vText.add( mText );
			
			if( mAttribToken.hasMoreTokens( ))
				new Label( mQueryComposite, SWT.NONE );
		}
		
		btnExecute = new Button( mQueryComposite, SWT.BUTTON1 );
		btnExecute.setText( "Execute" );
		
		btnExecute.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected( SelectionEvent event ) {
				
				TCComponentQueryType mQueryType = null;
				TCComponentQuery mRefTextQuery = null;
				TCComponent[] mResult = null;
				
				ArrayList<String> entries = new ArrayList<String>( );
				ArrayList<String> values = new ArrayList<String>( );
				
				getQueryAttributes( vText, entries, values );
				
				try {
					mQueryType = (TCComponentQueryType) mSession.getTypeComponent( "ImanQuery" );
					mRefTextQuery = (TCComponentQuery) mQueryType.find( (String) mParam.get( "queryName" ));
					mResult = mRefTextQuery.execute( entries.toArray( new String[entries.size()]), 
																values.toArray( new String[values.size()]));
					
					tableViewer.getTable().removeAll( );
					
					if( mResult != null && mResult.length != 0 )
					{
						tableViewer.setInput( new ArrayList<TCComponent>( Arrays.asList( mResult )));
						tableViewer.getTable().pack( );

					}
					else
					{
						MessageBox message = new MessageBox( shell );
						message.setMessage( "No objects Found for given search request." );
						message.open( );
					}
					
					
				} catch (TCException e) {
					e.printStackTrace();
				}
			}

			private void getQueryAttributes(Vector<Text> vText,
					ArrayList<String> entries, ArrayList<String> values) {
				
				for( Text mText : vText )
				{
					String value = mText.getText( );
					entries.add( (String)mText.getData( ));
					values.add( ( value.length() > 0)? "*" + value + "*" : "*" );
				}
			}
		});

		mQueryComposite.pack( );
	}

	private void setDescription( Composite mDescComposite, String description ) {
		Label lblDescription = new Label( mDescComposite, SWT.NONE );
		lblDescription.setText( description );
		
		lblDescription.pack( );
		
		mDescComposite.pack( );
	}
	

	
	class ColumnData
	{
		String attr_name;
		String displayname;
		int width;
		
		public ColumnData( String displayname, String attr_name, int width ) {
			this.attr_name = attr_name;
			this.displayname = displayname;
			this.width = width;
		}
	}
}
