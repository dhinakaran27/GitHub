package com.unilever.rac.pam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentListOfValues;
import com.teamcenter.rac.kernel.TCComponentListOfValuesType;
import com.teamcenter.rac.kernel.TCComponentQuery;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.kernel.TCPropertyDescriptor;
import com.teamcenter.rac.kernel.TCQueryClause;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.Registry;
import com.teamcenter.rac.viewer.stylesheet.beans.AbstractPropertyBean;
import com.unilever.rac.util.*;

public class EPMLToPAMTableComposite extends AbstractPropertyBean
{

	private Composite tableComposite 							= null;	
	private Composite parentComposite 							= null;
	private Button addButton									= null;
	private Button clearButton									= null; 
	private Button searchButton									= null;
	private SashForm sash										= null;
	private Group controlGroup									= null;
	private Group childGroup									= null;
	private TCComponentItemRevision pamSpecRevision 			= null;	
	private TCSession session									= null;
	private Combo foodGrade 									= null;
	private Combo resinType										= null;
	private Combo pmlStatus										= null;
	private Combo unileverCluster								= null;
	private Combo classification								= null;
	private Combo manufacturer									= null;
	private Text descriptionText								= null;
	private Text gradeText										= null;
	private Table queryResultsTable								= null;
	private Registry registry									= null;
	private Control[] children									= null;
	private HashMap searchTableResultsMap 						= new HashMap();
	private String[] queryEntriesArray							= new String[8];
	private String[] queryValuesArray							= null;
	private String[] pmlRevisionPropertiesArray					= null;
	private TableViewer tableViewer								= null;
	private static final String QRY_RESULT_TITLE 				= "QRY_RESULT_TITLE";
	private static final String THIS_CLASS 						= "com.unilever.rac.pam.pam";
	private ArrayList<String[]> queryTableList 					= new ArrayList<String[]>();
	private ArrayList<TCComponentItemRevision> arrList 			= new ArrayList<TCComponentItemRevision>();
	private static final String SEARCH_MATERIALS_QUERY_NAME 	= "QUERY_NAME";

	
	public EPMLToPAMTableComposite(FormToolkit toolkit, Composite composite,boolean paramBoolean,Map<String, String> PropName)
	{
		super(composite);
		
		this.parentComposite = composite;	
		this.registry = Registry.getRegistry("com.unilever.rac.pam.pam");
		
		parentComposite.setLayout(new GridLayout(2,true));
		parentComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		AbstractAIFUIApplication application = AIFUtility.getCurrentApplication();

		InterfaceAIFComponent[] targets = null;

		targets = application.getTargetComponents();
		
		if (targets[0] instanceof TCComponentItemRevision)
		{
			pamSpecRevision = (TCComponentItemRevision) targets[0];
		}		
		
		session = pamSpecRevision.getSession();	
	}
	
	@Override
	public boolean isPropertyModified(TCProperty arg0) throws Exception 
	{
		return true;
	}

	@Override
	public Object getEditableValue() 
	{	
		return tableComposite;
	}

	@Override
	public TCProperty getPropertyToSave(TCProperty arg0) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	private void initializeUI()
	{
		tableComposite = new Composite(parentComposite, SWT.NONE);
		tableComposite.setLayout(new FillLayout());
	    sash = new SashForm(tableComposite, SWT.HORIZONTAL);
	    createControlGroup();

	}
	
	/**
	   * Creates the "control" group. This is the group on the right half of each
	   * example tab. It contains controls for adding new children to the
	   * layoutComposite, and for modifying the children's layout data.
	   */
	  void createControlGroup() 
	  {
	    controlGroup = new Group(sash, SWT.NONE);
	    controlGroup.setText("Search Panel Information: ");
	    GridLayout layout = new GridLayout();
	    layout.numColumns = 2;
	    controlGroup.setLayout(layout);
	    createControlWidgets();
	  }
	  
	  /* Listeners */
	  SelectionListener selectionListener = new SelectionAdapter() {
	    public void widgetSelected(SelectionEvent e) {
	      resetEditors();
	    }
	  };
	  
	  /**
	   * Takes information from TableEditors and stores it.
	   */
	  void resetEditors() {
	    //resetEditors(false);
	  }
	  
	  TraverseListener traverseListener = new TraverseListener() {
		    public void keyTraversed(TraverseEvent e) {
		      if (e.detail == SWT.TRAVERSE_RETURN) {
		        e.doit = false;
		        resetEditors();
		      }
		    }
		  };
	  /**
	   * Creates the control widgets.
	   */
	  void createControlWidgets() 
	  {
		  buildSearchQueryConstraintsPanel(controlGroup);
		  controlGroup.pack();
		  createChildGroup();
	  }
	  	  
	  private String[] getLOVValues(String lovName)
	  {
		  	String[] lovValuesArr = null;
		    TCComponentListOfValues LOV= TCComponentListOfValuesType.findLOVByName(lovName);
		    try 
		    {
		    	lovValuesArr = LOV.getListOfValues().getStringListOfValues();
			}
		    catch (TCException e) 
		    {

		    }
		    return lovValuesArr;
	  }
	    
	  private void getQueryAttributes(String[] inputQueryValuesArray, ArrayList<String> values) {
			
			for( String qryValue : inputQueryValuesArray )
			{
				//String value = mText;
				//entries.add( (String)mText.getData( ));
				values.add( ( qryValue.length() > 0)? "*" + qryValue + "*" : "*");
			}
		}
	  void buildSearchQueryConstraintsPanel(Group inputControlGroup)
	  {
		  	//SEARCH_CRITERIA

		    Group marginGroup = new Group(inputControlGroup, SWT.NONE);
		    marginGroup.setText("Search Criteria:");
		    GridData data = new GridData(GridData.FILL_HORIZONTAL);
		    data.verticalSpan = 2;
		    marginGroup.setLayoutData(data);
		    GridLayout layout = new GridLayout();
		    layout.numColumns = 2;
		    marginGroup.setLayout(layout);
		    
		    //Search Criteria: 
		    //Label: Description
		    new Label(marginGroup, SWT.NONE).setText("Description: ");
		    descriptionText = new Text(marginGroup, SWT.BORDER);
		    data = new GridData(GridData.FILL_HORIZONTAL);
		    data.widthHint = 60;
		    descriptionText.setLayoutData(data);
		    descriptionText.addSelectionListener(selectionListener);
		    descriptionText.addTraverseListener(traverseListener);
		    
		    //Label: Classification
		    new Label(marginGroup, SWT.NONE).setText("Classification: ");
		    classification = new Combo(marginGroup, SWT.NONE);
		    classification.setItems(getLOVValues("U4_PNGClassificationLOV"));
		    classification.select(0);
		    data = new GridData(GridData.FILL_HORIZONTAL);
		    data.widthHint = 60;
		    classification.setLayoutData(data);
		    classification.addSelectionListener(selectionListener);
		    classification.addTraverseListener(traverseListener);
		    
		    //Label: Manufacturer
		    new Label(marginGroup, SWT.NONE).setText("Manufacturer: ");
		    manufacturer = new Combo(marginGroup, SWT.NONE);
		    manufacturer.setItems(getLOVValues("U4_PMLManufacturerLOV"));
		    manufacturer.select(0);
		    data = new GridData(GridData.FILL_HORIZONTAL);
		    data.widthHint = 60;
		    manufacturer.setLayoutData(data);
		    manufacturer.addSelectionListener(selectionListener);
		    manufacturer.addTraverseListener(traverseListener);

		    //Label: ResinType
		    new Label(marginGroup, SWT.NONE).setText("Resin Type: ");
		    resinType = new Combo(marginGroup, SWT.NONE);
		    resinType.setItems(getLOVValues("U4_PNGResinTypeLOV"));
		    resinType.select(0);
		    data = new GridData(GridData.FILL_HORIZONTAL);
		    data.widthHint = 60;
		    resinType.setLayoutData(data);
		    resinType.addSelectionListener(selectionListener);
		    resinType.addTraverseListener(traverseListener);
		    
		 	//Label: PML Status
		    new Label(marginGroup, SWT.NONE).setText("PML Status: ");
		    pmlStatus = new Combo(marginGroup, SWT.NONE);
		    pmlStatus.setItems(getLOVValues("U4_PMLStatusLOV"));
		    pmlStatus.select(0);
		    data = new GridData(GridData.FILL_HORIZONTAL);
		    data.widthHint = 60;
		    pmlStatus.setLayoutData(data);
		    pmlStatus.addSelectionListener(selectionListener);
		    pmlStatus.addTraverseListener(traverseListener);
		    
		    //Label: Grade
		    new Label(marginGroup, SWT.NONE).setText("Grade: ");
		    gradeText = new Text(marginGroup, SWT.BORDER);
		    data = new GridData(GridData.FILL_HORIZONTAL);
		    data.widthHint = 60;
		    gradeText.setLayoutData(data);
		    gradeText.addSelectionListener(selectionListener);
		    gradeText.addTraverseListener(traverseListener);
		    
		    //Label: Food Grade
		    new Label(marginGroup, SWT.NONE).setText("Food Grade: ");
		    foodGrade = new Combo(marginGroup, SWT.NONE);
		    foodGrade.setItems(getLOVValues("U4_YesNoLOV"));
		    foodGrade.select(0);
		    data = new GridData(GridData.FILL_HORIZONTAL);
		    data.widthHint = 60;
		    foodGrade.setLayoutData(data);
		    foodGrade.addSelectionListener(selectionListener);
		    foodGrade.addTraverseListener(traverseListener);
		    
		    //Label: Unilever Cluster
		    new Label(marginGroup, SWT.NONE).setText("Unilever Cluster: ");
		    unileverCluster = new Combo(marginGroup, SWT.NONE);
		    unileverCluster.setItems(getLOVValues("U4_PMLClusterLOV"));
		    unileverCluster.select(0);
		    data = new GridData(GridData.FILL_HORIZONTAL);
		    data.widthHint = 60;
		    unileverCluster.setLayoutData(data);
		    unileverCluster.addSelectionListener(selectionListener);
		    unileverCluster.addTraverseListener(traverseListener);

		    /** Search Button to execute the query. */
		    
		    searchButton = new Button(marginGroup, SWT.PUSH);
		    searchButton.setText("Search");
		    data = new GridData(GridData.FILL_BOTH);
		    data.widthHint = 60;
		    searchButton.setLayoutData(data);		    
		    /* Add listener to add an element to the table */
		    searchButton.addSelectionListener(new SelectionAdapter() 
		    {
		      public void widgetSelected(SelectionEvent e) 
		      {
		    	  try 
					{
		    		  	queryResultsTable.removeAll();
				        queryTableList.clear();
				        children = new Control[0];
				        
						session.setStatus(Registry.getRegistry("com.unilever.rac.pam.pam").getString("QRY_EXECUTE_STATUS"));
						queryValuesArray = new String[8];						
						//Get the query parameters:						
						queryValuesArray[0] = descriptionText.getText();
						queryValuesArray[1] = classification.getText();
						queryValuesArray[2] = manufacturer.getText();
						queryValuesArray[3] = resinType.getText();
						queryValuesArray[4] = pmlStatus.getText();
						queryValuesArray[5] = gradeText.getText();
						queryValuesArray[6] = foodGrade.getText();
						queryValuesArray[7] = unileverCluster.getText();

						ArrayList<String> values = new ArrayList<String>( );
						
						getQueryAttributes(queryValuesArray, values );

						//Build Query Constraints:
					    TCComponentQuery query = UnileverQueryUtil.getQuery(session, registry.getString(SEARCH_MATERIALS_QUERY_NAME));
					    if (query != null)
					    {
					    	TCQueryClause[] clauses = null;
					    	clauses = query.describe();
					    	
					    	for(int iClauseCounter = 0; iClauseCounter < clauses.length; iClauseCounter++)
					    	{
					    		queryEntriesArray[iClauseCounter] = clauses[iClauseCounter].getUserEntryName();
					    	}
					    	
					    	TCComponent[] qryResult = UnileverQueryUtil.executeQuery(registry.getString(SEARCH_MATERIALS_QUERY_NAME), 
					    										queryEntriesArray, values.toArray(new String[values.size()]));//queryEntriesArray, queryValuesArray);
							arrList.clear();
							
							if( qryResult != null && qryResult.length != 0 )
							{
								for(int iResultsCounter = 0; iResultsCounter < qryResult.length; iResultsCounter++)
								{
									if(qryResult[iResultsCounter] instanceof TCComponentItemRevision)
									{
										TCComponentItemRevision pmlRevision = (TCComponentItemRevision) qryResult[iResultsCounter];
										arrList.add((TCComponentItemRevision) qryResult[iResultsCounter]);
									}
								}
							    //UnileverMessageDialog dialog = new UnileverMessageDialog (shell1, "Information", null, "Found result count: " + qryResult.length, MessageDialog.INFORMATION, new String[]{"OK", "Cancel"}, 0);
							    //dialog.open();
							}
							else
							{
								/** Show error message */
								String title = Registry.getRegistry(THIS_CLASS).getString(QRY_RESULT_TITLE);
								if(title != null)
								{					                
									MessageBox mb = new MessageBox(AIFDesktop.getActiveDesktop().getShell(), SWT.ICON_WARNING | SWT.OK);
									mb.setText("Information");
									mb.setMessage(Registry.getRegistry("com.unilever.rac.pam.pam").getString("QRY_NO_RESULT_MSG"));
									return;
								}
							}
							session.setReadyStatus();
					    }
					} 
					catch (TCException ex) 
					{
						ex.printStackTrace();
					}
					
			        pmlRevisionPropertiesArray = new String[arrList.size()*5];
			        int iSerialNumber = 1, iCntr = 0;
			        TableItem tableItem;

			        for(TCComponentItemRevision pmlRevision : arrList)
			        {
			        	try
			        	{
			        		// Create a new TableItem for each entry in the result set (each row)
					    	tableItem = new TableItem(queryResultsTable, 0);
					    	
					    	// Populate the item (mind the index!!)
				        	pmlRevisionPropertiesArray[iCntr] = Integer.toString(iSerialNumber);
				        	tableItem.setText(0, pmlRevisionPropertiesArray[iCntr]);
				        	iCntr++;
				        	
				        	pmlRevisionPropertiesArray[iCntr] = pmlRevision.getProperty("object_name");
				        	tableItem.setText(1, pmlRevisionPropertiesArray[iCntr]);
				        	
				        	pmlRevisionPropertiesArray[++iCntr] = pmlRevision.getProperty("u4_manufacturer");
				        	tableItem.setText(2, pmlRevisionPropertiesArray[iCntr]);
				        	
							pmlRevisionPropertiesArray[++iCntr] = pmlRevision.getProperty("u4_resin_type");
							tableItem.setText(3, pmlRevisionPropertiesArray[iCntr]);
							
							pmlRevisionPropertiesArray[++iCntr] = pmlRevision.getProperty("u4_grade");
							tableItem.setText(4, pmlRevisionPropertiesArray[iCntr]);
							
							queryTableList.add(pmlRevisionPropertiesArray); 
							
							//Populate the HASHMAP - so as to get the contents if user selects the particular row from Table.
							searchTableResultsMap.put(iSerialNumber, pmlRevision);
							
							iSerialNumber++;
				        	iCntr++;
			        	}
			        	catch(TCException tcEx)
			        	{
			        		
			        	}
			        }
			    }
		    });
	  }
	  
	  /**
	   * Creates the "child" group. This is the group that allows you to add
	   * children to the layout. It exists within the controlGroup.
	   */
	  void createChildGroup() 
	  {
	    childGroup = new Group(controlGroup, SWT.NONE);
	    //childGroup.setText(Registry.getRegistry("com.unilever.rac.pam.pam").getString("QRY_RESULTS_TABLE_HEADER"));
	    childGroup.setText("Query Results Table:");
	    GridLayout layout = new GridLayout();
	    layout.numColumns = 3;
	    childGroup.setLayout(layout);
	    GridData data = new GridData(GridData.FILL_BOTH);
	    data.horizontalSpan = 2;
	    childGroup.setLayoutData(data);
	    createChildWidgets(childGroup);
	  }
		
	  void createChildWidgets(Group inputGroup)
	  {
		  tableViewer = new TableViewer(inputGroup, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
		  tableViewer.setContentProvider( new ArrayContentProvider( ));
		  queryResultsTable = tableViewer.getTable();
		  GridData gridData = new GridData(GridData.FILL_BOTH);
		  gridData.horizontalSpan = 3;
		  gridData.heightHint = 150;
		  queryResultsTable.setLayoutData(gridData);
		  queryResultsTable.setHeaderVisible(true);
		  queryResultsTable.setLinesVisible(true);
		  queryResultsTable.addTraverseListener(traverseListener);
		  queryResultsTable.addListener( SWT.Selection, new Listener() {
				
				@Override
				public void handleEvent( Event event ) {
					
					if( tableViewer.getTable( ).getSelectionCount() > 0 )
						addButton.setEnabled( true );
					else
						addButton.setEnabled( false );
					
				}
			});

		  	/* Add columns to the table */
		    String[] columnHeaders = getLayoutDataFieldNames();
		    for (int i = 0; i < columnHeaders.length; i++) {
		      TableColumn column = new TableColumn(queryResultsTable, SWT.NONE);
		      column.setText(columnHeaders[i]);
		      if (i == 0)
		        column.setWidth(40);
		      else if (i == 1)
		        column.setWidth(80);
		      else
		        column.pack();
		      
		      column.setResizable(true);
		    }
		    
		    /** Add buttons */
		    addButton = new Button(inputGroup, SWT.PUSH);
		    addButton.setText("Add");
		    addButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		    addButton.addSelectionListener( new SelectionAdapter() {
				
				@Override
				public void widgetSelected( SelectionEvent event ) 
				{
					if(tableViewer.getTable().getSelection() == null || tableViewer.getTable().getSelectionCount() == 0)
					{
						MessageBox mb = new MessageBox(AIFDesktop.getActiveDesktop().getShell(), SWT.ICON_WARNING | SWT.OK);
						mb.setText("Warning");
						mb.setMessage(Registry.getRegistry("com.unilever.rac.pam.pam").getString("QRY_INVALID_SELECTIONS_TO_ADD"));
						return;
					}
					TableItem[] mSelected = tableViewer.getTable().getSelection();
					TCComponent[] mSelectedComponent = new TCComponent[mSelected.length];
					for( int inx = 0; inx < mSelected.length; inx++ )
					{
						if("".equals(mSelected[inx].getText(0)) || mSelected[inx].getText(0).length() == 0)
							continue;
						
						// get value of keys from the table
						mSelectedComponent[inx] = (TCComponent) searchTableResultsMap.get(Integer.parseInt(mSelected[inx].getText(0)));
					}
					try 
					{
						//Now, check the search results and find out whether object is already existing in the relation.
						ArrayList list = new ArrayList();
						TCComponent[] getAttachedTCComponents = pamSpecRevision.getRelatedComponents(registry.getString("MATERIALS_RELATION"));
						if(getAttachedTCComponents.length != 0 || getAttachedTCComponents != null)
						{
							for(int kCheck = 0; kCheck < mSelectedComponent.length; kCheck++)
							{
								if(mSelectedComponent != null)
								{
									//Validate if object is already attached with the same relation
									if( ! isSelectedPNGResinAlreadyExists(getAttachedTCComponents,mSelectedComponent[kCheck]) )
									{
										list.add(mSelectedComponent[kCheck]);
									}
								}
							}
						}
						//Now, get the list of attched components to update the list.
						for(TCComponent comp: getAttachedTCComponents)
						{
							TCComponent compObj = (TCComponent) comp;
							if(compObj != null)
								list.add(compObj);
						}

						TCComponent[] finalComponent = (TCComponent[])list.toArray(new TCComponent[list.size()]);
						//Now add each component under PAMSpec Revision.
						pamSpecRevision.lock();
						pamSpecRevision.setRelated(registry.getString("MATERIALS_RELATION"),finalComponent);
						pamSpecRevision.save();
						pamSpecRevision.unlock();
						pamSpecRevision.refresh();
					}
					catch (TCException e) 
					{
						e.printStackTrace();
					}
				}
			});

		    clearButton = new Button(inputGroup, SWT.PUSH);
		    clearButton.setText("Clear");
		    clearButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		    clearButton.addSelectionListener(new SelectionAdapter() {
		      public void widgetSelected(SelectionEvent e) {
		        resetEditors();
		        queryResultsTable.removeAll();
		        queryTableList.clear();
		        children = new Control[0];
		      }
		    });
		  
	  }

	  /**
	   * Returns the flag/true, if component object is already attached with the PAMSpec Revision
	   */
	  private boolean isSelectedPNGResinAlreadyExists(TCComponent[] compArr, TCComponent selectedItemRev)
	  {
		  for(TCComponent component: compArr)
		  {
			  if(component.equals(selectedItemRev))
					  return true;
		  }
		  
		  return false;		  
	  }
	  /**
	   * Returns the layout data field names. COLUMN NAMES
	   */
	  String[] getLayoutDataFieldNames() {
		  return registry.getStringArray("SEARCH_COLUMN_NAMES");
	  }
	
	@Override
	public void load(TCProperty arg0) throws Exception 
	{
		initializeUI();
	}
	
	
	@Override
	public void load(TCPropertyDescriptor arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setModifiable(boolean arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setUIFValue(Object arg0) {
		// TODO Auto-generated method stub
		
	}
	
	public void enableORDisableConrol(Control control)
	{
		if(control != null)
		{
		      if(pamSpecRevision.isCheckedOut())
				{
			    	TCComponent checkedOutUser = null;
					
				    try {
						checkedOutUser = pamSpecRevision.getReferenceProperty("checked_out_user");
					} catch (TCException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				    
					if(session.getUser().equals(checkedOutUser))
					{
						control.setEnabled(true);
					}
					else
					{
						control.setEnabled(false);
					}
				}
				
				else 
				{
						control.setEnabled(false);
				}
			
		}
	}

}
