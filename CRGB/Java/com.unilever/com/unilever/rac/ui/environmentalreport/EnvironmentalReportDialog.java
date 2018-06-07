package com.unilever.rac.ui.environmentalreport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.common.TCTypeRenderer;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.unilever.rac.pam.PAMConstant;
import com.unilever.rac.ui.common.UL4Common;
import com.unilever.rac.util.UnileverQueryUtil;

public class EnvironmentalReportDialog extends Dialog {

	private int ReportTypeSelected  = 450;
	private int ReportTypeNotSelected  = 50;

	protected Object result;
	protected Shell shlSustainabilityReport;
	private Combo reportTypeCombo;
	private Button genReportButton;
	private Button saveReportButton;
	private Label saveReportLabel;

	private Group grpSelectGroup = null;
	private Text referenceText1 = null;
	private Button asyCheckButton [] = null;

	private AIFComponentContext[] dev_component = null;
	private AIFComponentContext[] final_component = null;

	private AIFComponentContext[] components = null;
	private TCComponentItemRevision[] componentRevs =null; 

	private TCComponentItemRevision itemRevision = null;
	private Table table;
	private Text referenceText;

	String [] inputText = new String[7]; 
	private ArrayList <TableItem> selectedComps = new ArrayList<TableItem>();

	private ArrayList <TableItem> selectedPackRefList = new ArrayList<TableItem>();
	
	String primary_pack_list[] = null;

	public EnvironmentalReportDialog(Shell parent) {
		// Pass the default styles here
		this(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
	}

	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public EnvironmentalReportDialog(Shell parent, int style) {
		super(parent, SWT.DIALOG_TRIM | SWT.RESIZE);
		setText("Environmental Report");
	}

	public EnvironmentalReportDialog(Shell parent, TCComponentItemRevision itemRevision) {
		this(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		this.itemRevision=itemRevision;
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open() {
		createContents();
		shlSustainabilityReport.open();
		shlSustainabilityReport.layout();
		Display display = getParent().getDisplay();

		TCSession session = (TCSession)AIFDesktop.getActiveDesktop().getCurrentApplication().getSession();

		while (!shlSustainabilityReport.isDisposed()) {
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

		try {
			String key_countries = itemRevision.getProperty("u4_key_countries");
			if(key_countries.length()==0){
				MessageBox mb = new MessageBox("Error: At least one key country must be selected to generate an Environmental Report","Error - Generating report",1);
				mb.setModal(true);
				mb.setVisible(true);
				return;
			}
		} catch (TCException e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		}

		final TCSession session = (TCSession)AIFDesktop.getActiveDesktop().getCurrentApplication().getSession();
		String report_type_list[] = session.getPreferenceService().getStringValues(UL4Common.PREF_ENVIRONMENTAL_REPORT_TYPE);
		primary_pack_list = session.getPreferenceService().getStringValues(UL4Common.PREF_PRIMARY_PACK_TYPE);

		if (report_type_list.length <= 0)
		{
			MessageBox mb = new MessageBox("Error - Fetching Report Type", "Failed to fetch different Environmental Report Types", 1);
			mb.setModal(true);
			mb.setVisible(true);
			return;
		}

		try {
			dev_component = itemRevision.getRelated(UL4Common.DEVRELATION);
			final_component = itemRevision.getRelated(UL4Common.FINAL_RELATION);

			ArrayList<AIFComponentContext> both = new ArrayList<AIFComponentContext>(Arrays.asList(dev_component));
			both.addAll(Arrays.asList(final_component));

			components = both.toArray(new AIFComponentContext[both.size()]); 
			ArrayList<TCComponentItemRevision> allCompRevs = new ArrayList<TCComponentItemRevision>();

			for(AIFComponentContext tempComp:components)
			{
				if(tempComp.getComponent() instanceof TCComponentItemRevision)
				{
					TCComponentItemRevision temRev = (TCComponentItemRevision) tempComp.getComponent();
					String type = temRev.getType();

					if ( (type!=null) && ((type.compareToIgnoreCase("U4_DDERevision")!=0) && (type.compareToIgnoreCase("U4_CADComponentRevision")!=0)))
					{
						temRev = temRev.getItem().getLatestItemRevision();
						allCompRevs.add(temRev);
					}
				}
			}
			Collections.sort(allCompRevs, new Comparator<TCComponentItemRevision>() {
				public int compare(TCComponentItemRevision o1, TCComponentItemRevision o2) {

					if (o1!=null && o2!=null)
					{
						String ctry1 = null;
						String ctry2 = null;
						try {
							ctry1 = o1.getProperty("item_id");
							ctry2 = o2.getProperty("item_id");
						} catch (TCException e) {
							e.printStackTrace();
						}

						return ctry1.compareTo(ctry2);
					}
					return -1;
				}
			});
			Map<String,TCComponentItemRevision> compMap = new LinkedHashMap<String,TCComponentItemRevision>();

			for(TCComponentItemRevision compRev :allCompRevs){
				try{
					String rev_id = compRev.getProperty("item_id");
					compMap.put(rev_id, compRev);
				}
				catch(Exception e){
					e.printStackTrace();
				}
			}
			allCompRevs.clear();
			allCompRevs.addAll(compMap.values());
			componentRevs = allCompRevs.toArray(new TCComponentItemRevision[allCompRevs.size()]); 
			//AIFComponentContext[] final_component = itemRevision.getRelated(UL4Common.FINAL_RELATION);
		} catch (TCException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}


		shlSustainabilityReport = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		shlSustainabilityReport.setSize(350,115);
		shlSustainabilityReport.setText("Environmental Report");

		final Composite composite = new Composite(shlSustainabilityReport, SWT.BORDER);
		composite.setBounds(5, 5, 335,75);

		Label reportTypeLabel = new Label(composite, SWT.NONE);
		reportTypeLabel.setBounds(10, 10, 87, 22);
		reportTypeLabel.setText("Report Type:");

		reportTypeCombo = new Combo(composite, SWT.NONE);
		reportTypeCombo.setBounds(105, 7, 220, 25);
		reportTypeCombo.setItems(report_type_list);

		//Label label = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
		//label.setBounds(10, 31, 590, 8);

		//final Label label_1 = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
		//label_1.setBounds(10, 31, 590, 8);

		final Composite composite_1 = new Composite(composite, SWT.NONE);
		composite_1.setBounds(10, 45, 434, 401);
		composite_1.setVisible(false);

		final Composite composite_2 = new Composite(composite, SWT.None);
		//composite_2.setBounds(10, 170, 590, 340);
		composite_2.setVisible(false);
		//composite_2.setMinSize(500, 400);
		//composite_2.setExpandHorizontal(true);
		//composite_2.setExpandVertical(true);

		final Label referenceLabel = new Label(composite_1, SWT.NONE);
		referenceLabel.setBounds(0, 0, 150, 20);
		referenceLabel.setText("Reference Component");
		referenceLabel.setVisible(false);

		referenceText = new Text(composite_1, SWT.BORDER);
		referenceText.setBounds(0, 0, 150, 20);
		referenceText.setVisible(false);

		final Button referenceButton = new Button(composite_1, SWT.NONE);
		referenceButton.setBounds(0, 0, 75, 25);
		referenceButton.setText("Browse");
		referenceButton.setVisible(false);

		Display display = AIFUtility.getActiveDesktop().getShell().getDisplay() ;
		final StyledText txt = new StyledText(composite_2, 0);
		txt.setLayoutData(new GridData( SWT.LEFT, SWT.NORMAL, false, false, 1, 1 ) );
		String PropValue = "Number of Consumer Units per Secondary:";
		txt.setText(PropValue+"*");

		Color red = display.getSystemColor(SWT.COLOR_RED);
		Color grey = display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
		txt.setBackground(grey);
		StyleRange style = new StyleRange();    
		style.start = PropValue.length() ;
		style.length = 1;
		style.foreground = red;   
		txt.setStyleRange(style);   
		txt.setEditable(false);
		txt.setEnabled(false);
		txt.setVisible(false);
		txt.setBounds(0, 5, 240, 20);

		referenceText1 = new Text(composite_2, SWT.BORDER);
		referenceText1.setBounds(250, 5, 30, 23);
		referenceText1.setText("");
		referenceText1.setVisible(false);

		referenceButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				//referenceText.setText("C510000200007");
				//child shell 
				final Shell browseRefPack = new Shell(shlSustainabilityReport, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
				browseRefPack.setSize(360, 340);
				browseRefPack.setVisible(true);
				browseRefPack.setText("Browse for Reference Component");
				//composite that will contain all control for getting inputs to the search query
				Composite refPackComp = new Composite(browseRefPack, SWT.BORDER);
				refPackComp.setBounds(8, 8, 340, 300);
				refPackComp.setVisible(true);
				Label label2 = new Label(refPackComp,SWT.NONE);
				label2.setText("Enter the search criteria below:");
				label2.setBounds(10, 10, 200, 20);
				Label [] queryLbl = new Label[7];
				final Text [] queryInput = new Text[7];
				int x = 20;
				int y = 30;
				int incr = 10;
				final String entriesLabel[] = { "Project Name","Component Class","Component Commodity","Material Class","Material Commodity","Created Before","Created After" };
				for(int i = 0;i<7;i++){
					queryLbl[i] = new Label(refPackComp,SWT.NONE);
					queryLbl[i].setText(entriesLabel[i] +" :");
					queryLbl[i].setBounds(x, y+incr, 140, 20);
					queryLbl[i].setVisible(true);
					queryInput[i] = new Text(refPackComp,SWT.BORDER |SWT.SINGLE);
					queryInput[i].setText("");
					queryInput[i].setBounds(x+140,y+incr,140, 20);
					y=y+incr+20;
					queryInput[i].setVisible(true);
					queryInput[i].setEditable(true);
				}

				Button Search_button = new Button(refPackComp,SWT.NONE);
				Search_button.setText("Search");
				Search_button.setBounds(200, 260, 50, 25);
				Search_button.setVisible(true);

				Button Cancel_button = new Button(refPackComp,SWT.NONE);
				Cancel_button.setText("Cancel");
				Cancel_button.setBounds(260, 260, 50, 25);
				Cancel_button.setVisible(true);

				Cancel_button.addSelectionListener(new SelectionAdapter(){
					public void widgetSelected(SelectionEvent e) {
						browseRefPack.dispose();
					}
				});

				Search_button.addSelectionListener(new SelectionAdapter(){
					public void widgetSelected(SelectionEvent e) {

						ArrayList<String> entries = new ArrayList<String>();
						ArrayList<String> values = new ArrayList<String>();
						for(int i = 0;i<7;i++){
							if(queryInput[i].getText().length()!=0){
								if(i==0){
									entries.add("Project Name");
									values.add(queryInput[i].getText());
								}
								if(i==1){
									entries.add("Component Class");
									values.add(queryInput[i].getText());
								}
								if(i==2){
									entries.add("Component Class");
									values.add(queryInput[i].getText());
								}
								if(i==3){
									entries.add("Material Commodity");
									values.add(queryInput[i].getText());
								}
								if(i==4){
									entries.add("Material Commodity");
									values.add(queryInput[i].getText());
								}
								if(i==5){
									entries.add("Created Before");
									values.add(queryInput[i].getText());
								}
								if(i==6){
									entries.add("Created After");
									values.add(queryInput[i].getText());
								}
							}
						}
						final Shell resultsShell = new Shell(browseRefPack);
						resultsShell.setVisible(false);
						resultsShell.setSize(350,300);
						resultsShell.setText("Search Results");
						ScrolledComposite resultsComposite = new ScrolledComposite(resultsShell,SWT.V_SCROLL | SWT.H_SCROLL);
						resultsComposite.setVisible(false);
						final Button ok_button = new Button(resultsComposite,SWT.NONE);
						ok_button.setText("OK");
						ok_button.setBounds(230, 220, 50, 25);
						ok_button.setVisible(true);
						ok_button.setEnabled(false);

						resultsComposite.setBounds(10,10,330,280);
						final Table table2 = new Table(resultsComposite, SWT.CHECK | SWT.VIRTUAL | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
						table2.setBounds(10, 10, 300, 200);
						String queryName = "__UL_find_project_component";
						ArrayList<TCComponentItemRevision> filteredPAMSpecList = new ArrayList<TCComponentItemRevision>();
						ArrayList <TCComponentItemRevision> compRevs = new ArrayList<TCComponentItemRevision>();
						final ArrayList <TableItem> selectedPAMSpecsList = new ArrayList<TableItem>();
						try {
							TCComponent[] qryResults = UnileverQueryUtil.executeQuery(queryName, entries.toArray(new String[entries.size()]), values.toArray(new String[values.size()]));
							if(qryResults != null)
							{
								for(int i=0;i<qryResults.length;i++){
									if(qryResults[i] instanceof TCComponentItemRevision){
										compRevs.add((TCComponentItemRevision) qryResults[i]);
									}
								}
								for(TCComponentItemRevision itemrev : compRevs){
									TCComponentItemRevision latestCompRev = itemrev.getItem().getLatestItemRevision();
									String releaseStatus = latestCompRev.getProperty(UL4Common.RELEASE_STATUS_LIST_ATTR);
									if(UL4Common.RELEASED_STATUS.equals(releaseStatus)){
										String pamSpecIDAndRevID = "";
										pamSpecIDAndRevID = latestCompRev.getTCProperty(UL4Common.ITEMID).toString();
										pamSpecIDAndRevID = pamSpecIDAndRevID + "/" + latestCompRev.getTCProperty(UL4Common.REVID).toString();
										//setOfPAMIds.add(pamSpecIDAndRevID);
										filteredPAMSpecList.add(latestCompRev);
										//pamSpecMap.put(pamSpecIDAndRevID, latestCompRev);
									}
								}
								filteredPAMSpecList = new ArrayList<TCComponentItemRevision>(new LinkedHashSet<TCComponentItemRevision>(filteredPAMSpecList));
								if(filteredPAMSpecList.size() == 0)
								{
									resultsShell.dispose();
									MessageBox.post(browseRefPack, "No components available to browse.", "Error", MessageBox.INFORMATION);
								}
								for(int iCounter = 0; iCounter < filteredPAMSpecList.size(); iCounter++)
								{
									resultsShell.setVisible(true);
									resultsComposite.setVisible(true);
									TableItem item = new TableItem(table2, SWT.NONE);
									item.setImage(TCTypeRenderer.getImage(filteredPAMSpecList.get(iCounter)));
									item.setText(" " + filteredPAMSpecList.get(iCounter) + " ");  		
								}
							}
						} catch (TCException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}

						table2.addListener(SWT.Selection, new Listener() 
						{							
							public void handleEvent(Event e) 
							{
								if(e.detail == SWT.CHECK)
								{
									ok_button.setEnabled(true);
									TableItem item = (TableItem) e.item;        
									if( item.getChecked())
									{
										selectedPAMSpecsList.add(item);
									}
									else {
										TableItem [] itms1 = table2.getItems();
										boolean flag = false;
										for(TableItem it:itms1){
											if(it.getChecked())
												flag = true;
										}
										if(!flag)
											ok_button.setEnabled(false);
									}
								} 					        		
							}
						});
						ok_button.addSelectionListener(new SelectionAdapter(){
							public void widgetSelected(SelectionEvent e) {
								if(selectedPAMSpecsList.size() != 1 && selectedPAMSpecsList.size()>0)
								{
									MessageBox.post(browseRefPack, "Please select only one reference Component.", "Error", MessageBox.INFORMATION);
									selectedPAMSpecsList.clear();
								}
								else {
									referenceText.setText("");
									String refitem = selectedPAMSpecsList.get(0).getText();
									int inx = refitem.indexOf("/");
									refitem = refitem.substring(0,inx);
									System.out.println("refitem:"+refitem);
									referenceText.setText(refitem);
									resultsShell.dispose();
									browseRefPack.dispose();
								}
							}
						});
						resultsComposite.setExpandHorizontal(true);
						resultsComposite.setExpandVertical(true);

						session.setStatus("");
					}
				});
			}
		});

		final Label descLabel = new Label(composite, SWT.NONE);
		//descLabel.setBounds(0, 0, 150, 20);
		descLabel.setText("Description:");
		descLabel.setVisible(false);

		final Text descBox = new Text(composite, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		descBox.setVisible(false);
		descBox.setTextLimit(255);

		final Label pack_ref_label = new Label(composite_2, SWT.NONE);
		pack_ref_label.setBounds(10, 305, 140, 20);
		pack_ref_label.setText("Reference Component ID:");

		final Button pack_ref_button = new Button(composite_2, SWT.NONE);
		pack_ref_button.setText("Browse");
		pack_ref_button.setBounds(160,300,75,25);

		final Text referenceText2 ;
		referenceText2 = new Text(composite_2, SWT.BORDER);
		referenceText2.setBounds(10, 325, 140, 23);
		referenceText2.setVisible(true);

		final Button ref_add_button = new Button(composite_2, SWT.NONE);
		ref_add_button.setText("Add");
		ref_add_button.setBounds(160,325,75,25);

		final Table table1 = new Table(composite_2, SWT.BORDER | SWT.CHECK | SWT.VIRTUAL);
		table1.setBounds(0,40, 270, 250);
		table1.setHeaderVisible(true);
		table1.setLinesVisible(true);

		TableColumn packCompColumn1 = new TableColumn(table1, SWT.NONE);
		packCompColumn1.setWidth(200);
		packCompColumn1.setText("Pack Component ID");
		
		TableColumn cu_column = new TableColumn(table1, SWT.NONE);
		cu_column.setWidth(125);
		cu_column.setText("Consumer Unit");

		for (int i = 0 ; i <  componentRevs.length ; i++)
		{
			TableItem item = new TableItem(table1, SWT.NONE);
			item.setText(new String[] {componentRevs[i].toString(), (is_comp_primary(componentRevs[i])==true)?"Primary":"Secondary" });
		}	

		final Table table2Ref = new Table(composite_2, SWT.BORDER | SWT.CHECK | SWT.VIRTUAL);
		table2Ref.setBounds(0,360, 270, 200);
		table2Ref.setHeaderVisible(true);
		table2Ref.setLinesVisible(true);

		TableColumn refCompColumn1 = new TableColumn(table2Ref, SWT.NONE);
		refCompColumn1.setWidth(200);
		refCompColumn1.setText("Reference Component ID");
		
		TableColumn ref_cu_column = new TableColumn(table2Ref, SWT.NONE);
		ref_cu_column.setWidth(125);
		ref_cu_column.setText("Consumer Unit");

		table = new Table(composite_1, SWT.BORDER | SWT.FULL_SELECTION | SWT.VIRTUAL |SWT.V_SCROLL | SWT.H_SCROLL );
		table.setBounds(0, 0, 440, 391);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		TableColumn packCompColumn = new TableColumn(table, SWT.NONE);
		packCompColumn.setWidth(329);
		packCompColumn.setText("Pack Component ID");

		//final TableColumn PackColumn = new TableColumn(table, SWT.NONE);
		//PackColumn.setResizable(false);
		//PackColumn.setWidth(104);
		//PackColumn.setText("Pack");

		final TableColumn SelectColumn = new TableColumn(table, SWT.NONE);
		SelectColumn.setResizable(false);
		SelectColumn.setWidth(104);
		SelectColumn.setText("Select");

		for (int i = 0 ; i <  componentRevs.length ; i++)
		{
			TableItem item = new TableItem(table, SWT.NONE);

			TableEditor editor = new TableEditor(table);

			Text text = new Text(table, SWT.NONE);
			text.setText(componentRevs[i].toString());
			editor.grabHorizontal = true;
			editor.setEditor(text, item, 0);
			item.setData("comp",text);			

			editor = new TableEditor(table);
			Button checkButton = new Button(table, SWT.CHECK);
			checkButton.pack();
			editor.setEditor(checkButton, item, 1);
			item.setData("cb", checkButton);
			/*checkButton.addSelectionListener(new SelectionAdapter() {     
				public void widgetSelected(SelectionEvent event) {         
					if ( ((Button)(event.getSource())).getSelection() == true)
						genReportButton.setEnabled(true);
				}      
			});*/
			Point size = checkButton.computeSize(SWT.DEFAULT,SWT.DEFAULT);
			editor.minimumWidth = size.x;
			int minWidth = 0;
			minWidth = Math.max(size.x, minWidth);
			editor.minimumHeight = size.y;
			editor.horizontalAlignment = SWT.CENTER;
			editor.verticalAlignment = SWT.CENTER;
		}
		final Table packRefBox = new Table(composite_2, SWT.BORDER | SWT.V_SCROLL | SWT.CHECK | SWT.H_SCROLL);
		TableColumn pack = new TableColumn(packRefBox, SWT.NONE);
		pack.setWidth(230);
		pack.setText("Pack Reference");
		TableColumn qty = new TableColumn(packRefBox, SWT.NONE);
		qty.setWidth(30);
		qty.setText("#");

		final Table pack1Box = new Table(composite_2, SWT.BORDER | SWT.V_SCROLL | SWT.CHECK | SWT.H_SCROLL);
		TableColumn pack1 = new TableColumn(pack1Box, SWT.NONE);
		pack1.setWidth(230);
		pack1.setText("Pack 1");
		TableColumn qty1 = new TableColumn(pack1Box, SWT.NONE);
		qty1.setWidth(30);
		qty1.setText("#");

		final Table pack2Box = new Table(composite_2, SWT.BORDER | SWT.V_SCROLL | SWT.CHECK | SWT.H_SCROLL);
		TableColumn pack2 = new TableColumn(pack2Box, SWT.NONE);
		pack2.setWidth(230);
		pack2.setText("Pack 2");
		TableColumn qty2 = new TableColumn(pack2Box, SWT.NONE);
		qty2.setWidth(30);
		qty2.setText("#");

		final Table pack3Box = new Table(composite_2, SWT.BORDER | SWT.V_SCROLL | SWT.CHECK | SWT.H_SCROLL);
		TableColumn pack3 = new TableColumn(pack3Box, SWT.NONE);
		pack3.setWidth(230);
		pack3.setText("Pack 3");
		TableColumn qty3 = new TableColumn(pack3Box, SWT.NONE);
		qty3.setWidth(30);
		qty3.setText("#");

		final Table pack4Box = new Table(composite_2, SWT.BORDER | SWT.V_SCROLL | SWT.CHECK | SWT.H_SCROLL);
		TableColumn pack4 = new TableColumn(pack4Box, SWT.NONE);
		pack4.setWidth(230);
		pack4.setText("Pack 4");
		TableColumn qty4 = new TableColumn(pack4Box, SWT.NONE);
		qty4.setWidth(30);
		qty4.setText("#");

		final Text pack_1_label = new Text(composite_2, SWT.BORDER);
		pack_1_label.setBounds(370, 18, 130, 20);
		pack_1_label.setText("");
		pack_1_label.setVisible(true);

		final Text pack_2_label = new Text(composite_2, SWT.BORDER);
		pack_2_label.setBounds(370, 128, 130, 20);
		pack_2_label.setText("");
		pack_2_label.setVisible(true);

		final Text pack_3_label = new Text(composite_2, SWT.BORDER);
		pack_3_label.setBounds(370, 238, 130, 20);
		pack_3_label.setText("");
		pack_3_label.setVisible(true);

		final Text pack_4_label = new Text(composite_2, SWT.BORDER);
		pack_4_label.setBounds(370, 348, 130, 20);
		pack_4_label.setText("");
		pack_4_label.setVisible(true);

		final Text pack_5_label = new Text(composite_2, SWT.BORDER);
		pack_5_label.setBounds(370, 458, 130, 20);
		pack_5_label.setText("");
		pack_5_label.setVisible(true);

		pack_ref_button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				//referenceText1.setText("C510000200007");
				final Shell browseRefPack = new Shell(shlSustainabilityReport, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
				browseRefPack.setSize(360, 340);
				browseRefPack.setVisible(true);
				browseRefPack.setText("Browse for Reference Component");
				//composite that will contain all control for getting inputs to the search query
				Composite refPackComp = new Composite(browseRefPack, SWT.BORDER);
				refPackComp.setBounds(8, 8, 340, 300);
				refPackComp.setVisible(true);
				Label label2 = new Label(refPackComp,SWT.NONE);
				label2.setText("Enter the search criteria below:");
				label2.setBounds(10, 10, 200, 20);
				Label [] queryLbl = new Label[7];
				final Text [] queryInput = new Text[7];
				int x = 20;
				int y = 30;
				int incr = 10;
				final String entriesLabel[] = { "Project Name","Component Class","Component Commodity","Material Class","Material Commodity","Created Before","Created After" };
				for(int i = 0;i<7;i++){
					queryLbl[i] = new Label(refPackComp,SWT.NONE);
					queryLbl[i].setText(entriesLabel[i] +" :");
					queryLbl[i].setBounds(x, y+incr, 140, 20);
					queryLbl[i].setVisible(true);
					queryInput[i] = new Text(refPackComp,SWT.BORDER |SWT.SINGLE);
					queryInput[i].setText("");
					queryInput[i].setBounds(x+140,y+incr,140, 20);
					y=y+incr+20;
					queryInput[i].setVisible(true);
					queryInput[i].setEditable(true);
				}


				Button Search_button = new Button(refPackComp,SWT.NONE);
				Search_button.setText("Search");
				Search_button.setBounds(200, 260, 50, 25);
				Search_button.setVisible(true);

				Button Cancel_button = new Button(refPackComp,SWT.NONE);
				Cancel_button.setText("Cancel");
				Cancel_button.setBounds(260, 260, 50, 25);
				Cancel_button.setVisible(true);

				Cancel_button.addSelectionListener(new SelectionAdapter(){
					public void widgetSelected(SelectionEvent e) {
						browseRefPack.dispose();
					}
				});

				Search_button.addSelectionListener(new SelectionAdapter(){
					public void widgetSelected(SelectionEvent e) {
						ArrayList<String> entries = new ArrayList<String>();
						ArrayList<String> values = new ArrayList<String>();
						for(int i = 0;i<7;i++){
							if(queryInput[i].getText().length()!=0){
								if(i==0){
									entries.add("Project Name");
									values.add(queryInput[i].getText());
								}
								if(i==1){
									entries.add("Component Class");
									values.add(queryInput[i].getText());
								}
								if(i==2){
									entries.add("Component Class");
									values.add(queryInput[i].getText());
								}
								if(i==3){
									entries.add("Material Commodity");
									values.add(queryInput[i].getText());
								}
								if(i==4){
									entries.add("Material Commodity");
									values.add(queryInput[i].getText());
								}
								if(i==5){
									entries.add("Created Before");
									values.add(queryInput[i].getText());
								}
								if(i==6){
									entries.add("Created After");
									values.add(queryInput[i].getText());
								}
							}
						}
						final Shell resultsShell = new Shell(browseRefPack);
						resultsShell.setVisible(false);
						resultsShell.setSize(350,300);
						resultsShell.setText("Search Results");
						ScrolledComposite resultsComposite = new ScrolledComposite(resultsShell,SWT.V_SCROLL | SWT.H_SCROLL);
						resultsComposite.setVisible(false);
						resultsComposite.setBounds(10,10,330,280);

						final Button ok_button = new Button(resultsComposite,SWT.NONE);
						ok_button.setText("OK");
						ok_button.setBounds(230, 220, 50, 25);
						ok_button.setVisible(true);
						ok_button.setEnabled(false);
						final Table table2 = new Table(resultsComposite, SWT.CHECK | SWT.VIRTUAL | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
						table2.setBounds(10, 10, 300, 200);
						String queryName = "__UL_find_project_component";
						ArrayList<TCComponentItemRevision> filteredPAMSpecList = new ArrayList<TCComponentItemRevision>();
						ArrayList <TCComponentItemRevision> compRevs = new ArrayList<TCComponentItemRevision>();
						final ArrayList <TableItem> selectedPackRefList = new ArrayList<TableItem>();
						try {

							TCComponent[] qryResults = UnileverQueryUtil.executeQuery(queryName, entries.toArray(new String[entries.size()]), values.toArray(new String[values.size()]));
							if(qryResults != null)
							{
								for(int i=0;i<qryResults.length;i++){
									if(qryResults[i] instanceof TCComponentItemRevision){
										compRevs.add((TCComponentItemRevision) qryResults[i]);
									}
								}
								for(TCComponentItemRevision itemrev : compRevs){
									TCComponentItemRevision latestCompRev = itemrev.getItem().getLatestItemRevision();
									String releaseStatus = latestCompRev.getProperty(UL4Common.RELEASE_STATUS_LIST_ATTR);
									if(UL4Common.RELEASED_STATUS.equals(releaseStatus)){
										String pamSpecIDAndRevID = "";
										pamSpecIDAndRevID = latestCompRev.getTCProperty(UL4Common.ITEMID).toString();
										pamSpecIDAndRevID = pamSpecIDAndRevID + "/" + latestCompRev.getTCProperty(UL4Common.REVID).toString();
										//setOfPAMIds.add(pamSpecIDAndRevID);
										filteredPAMSpecList.add(latestCompRev);
										//pamSpecMap.put(pamSpecIDAndRevID, latestCompRev);
									}
								}
								filteredPAMSpecList = new ArrayList<TCComponentItemRevision>(new LinkedHashSet<TCComponentItemRevision>(filteredPAMSpecList));
								if(filteredPAMSpecList.size() == 0)
								{
									resultsShell.dispose();
									MessageBox.post(browseRefPack, "No components available to browse.", "Error", MessageBox.INFORMATION);
								}
								for(int iCounter = 0; iCounter < filteredPAMSpecList.size(); iCounter++)
								{
									resultsShell.setVisible(true);
									resultsComposite.setVisible(true);
									TableItem item = new TableItem(table2, SWT.NONE);
									item.setImage(TCTypeRenderer.getImage(filteredPAMSpecList.get(iCounter)));
									item.setText(" " + filteredPAMSpecList.get(iCounter) + " ");
									Text text = new Text(table2, SWT.NONE);
									text.setText( filteredPAMSpecList.get(iCounter).toString());
									item.setData("comp1",text);
								}
							}
						} catch (TCException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						table2.addListener(SWT.Selection, new Listener() 
						{
							public void handleEvent(Event e) 
							{
								if(e.detail == SWT.CHECK)
								{
									ok_button.setEnabled(true);
									TableItem item = (TableItem) e.item;        
									if( item.getChecked())
									{
										selectedPackRefList.add(item);
									}
									else {
										TableItem [] itms1 = table2.getItems();
										boolean flag = false;
										for(TableItem it:itms1){
											if(it.getChecked())
												flag = true;
										}
										if(!flag)
											ok_button.setEnabled(false);
									}
								}        	
							}
						});

						ok_button.addSelectionListener(new SelectionAdapter(){
							public void widgetSelected(SelectionEvent e) {
								for(int i =0;i<selectedPackRefList.size();i++){
									TableItem item = new TableItem(table2Ref,SWT.NONE);
									TableEditor editor = new TableEditor(table2Ref);
									Text text = (Text) selectedPackRefList.get(i).getData("comp1");
									item.setText(new String[] {text.getText(), (is_comp_primary(componentRevs[i])==true)?"Primary":"Secondary" });
									editor.grabHorizontal = true;
									item.setData("text", text);
									item.setChecked(false);
									editor.setEditor(text, item, 0);
								}
								resultsShell.dispose();
								browseRefPack.dispose();
							}
						});
						resultsComposite.setExpandHorizontal(true);
						resultsComposite.setExpandVertical(true);
					}
				});
			}
		});

		ref_add_button.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				String refComp = referenceText2.getText().trim();
				if(refComp.startsWith("C") || refComp.startsWith("c"))
				{
					String queryName = "Latest Item Revision...";
					String entries[] = { "Item ID" };
					String values[] = { refComp };
					TCComponentItemRevision refItemrev = null;
					try{

						if(!refComp.equals("")){
							TCComponent[] components = UnileverQueryUtil.executeQuery(queryName, entries, values);

							if (components.length!=0)
							{
								refItemrev = (TCComponentItemRevision) components[0];

								if (refItemrev!=null)
								{
									TableItem item = new TableItem(table2Ref,SWT.NONE);
									item.setText(""+refItemrev+"");
									item.setChecked(false);
								}
							}
							else
							{
								MessageBox.post(shlSustainabilityReport ,"Unable to find Pack Component with specified ID","Error",MessageBox.ERROR);
							}
						}
					}
					catch(Exception e1){

					}
				}
				else 
				{
					MessageBox.post(shlSustainabilityReport ,"Specified Pack Component ID for report generation is invalid.","Error",MessageBox.ERROR);
				}
			}
		});

		shlSustainabilityReport.setTabList(new Control[]{composite});

		saveReportLabel  = new Label(composite, SWT.CHECK);
		saveReportLabel.setText("Save the report to Teamcenter");
		saveReportLabel.setVisible(false);

		saveReportButton = new Button(composite, SWT.CHECK);
		saveReportButton.setText("");
		saveReportButton.setVisible(false);

		genReportButton = new Button(composite, SWT.NONE);
		genReportButton.setVisible(false);
		genReportButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				String reportTypeString = reportTypeCombo.getText();
				//System.out.println("report type:" + reportTypeString);
				ArrayList <TCComponentItemRevision> compRevs = new ArrayList<TCComponentItemRevision>();
				TableItem [] items = table.getItems();
				for(TableItem it : items){
					Button Checkbox = (Button) it.getData("cb");
					if(Checkbox.getSelection()){
						Text comp = (Text) it.getData("comp");
						String obj_str = comp.getText();
						for(int i=0;i<components.length;i++){
							if(components[i].toString().equals(obj_str)){
								TCComponentItemRevision specRev = (TCComponentItemRevision)components[i].getComponent();
								//System.out.println(specRev.toString());
								compRevs.add(specRev);
								break;
							}
						}
					}
				}

				TCComponentItemRevision [] itemList = new TCComponentItemRevision[compRevs.size()];
				compRevs.toArray(itemList);
				if(reportTypeString.equals("Component Impact Report")){
					ComponentImpactReportOperation rp = new ComponentImpactReportOperation();
					try {
						String ref = referenceText.getText().trim() ;
						if(ref.startsWith("C")|| ref.length()==0 || ref.startsWith("c")){
							if((itemList.length<=5 && (ref.compareTo("")==0) )||(itemList.length<=4 && (ref.compareTo("")!=0))){
								if (itemList.length>0){
									boolean savereport = saveReportButton.getSelection();
									rp.genEcelrep(itemList,referenceText.getText(),descBox.getText(),itemRevision,savereport);
									session.queueOperation(rp);
								}
								else{
									MessageBox.post( "Select aleast one pack component for generating report", "Information", MessageBox.INFORMATION);
								}
							}
							else{
								MessageBox.post( "We can select only five or less pack component(s) for generating report", "Information", MessageBox.INFORMATION);
							}
						}
						else{
							MessageBox.post(shlSustainabilityReport ,"Only Pack Component IDs are accepted for report generation.","Error",MessageBox.ERROR);
						}

					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
				else if(reportTypeString.equals("Environment Packaging Material")){
					EnvPkgMaterialReportOperation envRp = new EnvPkgMaterialReportOperation();
					try {
						if(itemList.length==1){
							boolean savereport = saveReportButton.getSelection();
							envRp.genEcelrep(itemList, descBox.getText(),itemRevision,savereport);
							session.queueOperation(envRp);
						}
						else{
							MessageBox.post(shlSustainabilityReport ,"Environment Packaging Material report will be generated only if one component is selected","Information",MessageBox.INFORMATION);
						}
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
				else if (reportTypeString.equals("Total Pack Impact Report")){
					String text_pack = referenceText1.getText();
					if(text_pack.equals("")){
						MessageBox.post(shlSustainabilityReport ,"Please fill in the number of primary component per secondary pack to enable the calculation to be performed","Information",MessageBox.INFORMATION);
					}
					else{
						TotalPackImpactReportOperation totalRep = new TotalPackImpactReportOperation();
						Map<TCComponentItemRevision,Double> refCompRevs = new HashMap<TCComponentItemRevision,Double>();
						Map <TCComponentItemRevision,Double> pack1CompRevs = new HashMap<TCComponentItemRevision,Double>();
						Map <TCComponentItemRevision,Double> pack2CompRevs = new HashMap<TCComponentItemRevision,Double>();
						Map <TCComponentItemRevision,Double> pack3CompRevs = new HashMap<TCComponentItemRevision,Double>();
						Map <TCComponentItemRevision,Double> pack4CompRevs = new HashMap<TCComponentItemRevision,Double>();
						int no_of_pri_packs = (Integer)Integer.parseInt(referenceText1.getText());
						try {
							String queryName = "Item ID";
							String entries[] = { "Item ID" };
							TableItem [] packRefItems=packRefBox.getItems();
							for(int i = 0;i<packRefItems.length;i++){
								String refItem = packRefItems[i].getText();
								Text qty = (Text) packRefItems[i].getData("refcount");
								double quantity = Double.parseDouble(qty.getText());
								int inx = refItem.indexOf("/");
								refItem = refItem.substring(0,inx);
								String values[] = { refItem };								
								try {
									TCComponent[] components = UnileverQueryUtil.executeQuery(queryName, entries, values);
									if(components.length>0){
										AIFComponentContext[] refItemRevs = components[0].getChildren();
										TCComponentItemRevision refItemrev = (TCComponentItemRevision) refItemRevs[refItemRevs.length-1].getComponent();
										System.out.println(refItemrev.toString());
										refCompRevs.put(refItemrev,quantity);
									}
								} catch (TCException e2) {
									// TODO Auto-generated catch block
									e2.printStackTrace();
								}								
							}

							TableItem [] pack1Items=pack1Box.getItems();
							for(int i = 0;i<pack1Items.length;i++){
								String pack1Text =  pack1Items[i].getText();
								Text qty = (Text) pack1Items[i].getData("refcount");
								double quantity = Double.parseDouble(qty.getText());
								int inx = pack1Text.indexOf("/");
								String refItem = pack1Text.substring(0,inx);
								String values[] = { refItem };
								boolean found = false;
								for(int j=0;j<components.length;j++){
									if(components[j].toString().equals(pack1Text)){
										TCComponentItemRevision specRev = (TCComponentItemRevision)components[j].getComponent();
										pack1CompRevs.put(specRev,quantity);
										found=true;
										break;
									}
									else{
										try {
											TCComponent[] components = UnileverQueryUtil.executeQuery(queryName, entries, values);
											if(components.length>0){
												AIFComponentContext[] refItemRevs = components[0].getChildren();
												TCComponentItemRevision refItemrev = (TCComponentItemRevision) refItemRevs[refItemRevs.length-1].getComponent();
												pack1CompRevs.put(refItemrev,quantity);
												found=true;
												break;
											}
										} catch (TCException e1) {
											// TODO Auto-generated catch block
											e1.printStackTrace();
										}
									}
								}

								if (found== false)
								{
									Set<Entry<TCComponentItemRevision, Double>> set = refCompRevs.entrySet();
									Iterator<Entry<TCComponentItemRevision, Double>> iterator = set.iterator();

									while(iterator.hasNext()) {
										Map.Entry mentry = (Map.Entry)iterator.next();
										TCComponentItemRevision comp= (TCComponentItemRevision) mentry.getKey();
										double ref_qty = (double)mentry.getValue();

										if (comp!=null)
										{
											System.out.println(comp.toString() + " " + pack1Text);

											if(comp.toString().equals(pack1Text)){
												pack1CompRevs.put(comp,ref_qty);
												break;
											}
										}
									}
								}
							}

							TableItem [] pack2Items=pack2Box.getItems();
							for(int i = 0;i<pack2Items.length;i++){
								String pack2Text = pack2Items[i].getText();
								Text qty = (Text) pack2Items[i].getData("refcount");
								double quantity = Double.parseDouble(qty.getText());
								int inx = pack2Text.indexOf("/");
								String refItem = pack2Text.substring(0,inx);
								String values[] = { refItem };
								boolean found = false;
								for(int j=0;j<components.length;j++){
									if(components[j].toString().equals(pack2Text)){
										TCComponentItemRevision specRev = (TCComponentItemRevision)components[j].getComponent();
										//System.out.println(specRev.toString());
										pack2CompRevs.put(specRev,quantity);
										found=true;
										break;
									}
									else{
										try {
											TCComponent[] components = UnileverQueryUtil.executeQuery(queryName, entries, values);
											if(components.length>0){
												AIFComponentContext[] refItemRevs = components[0].getChildren();
												TCComponentItemRevision refItemrev = (TCComponentItemRevision) refItemRevs[refItemRevs.length-1].getComponent();
												pack2CompRevs.put(refItemrev,quantity);
												found=true;
												break;
											}
										} catch (TCException e1) {
											// TODO Auto-generated catch block
											e1.printStackTrace();
										}
									}
								}

								if (found== false)
								{
									Set<Entry<TCComponentItemRevision, Double>> set = refCompRevs.entrySet();
									Iterator<Entry<TCComponentItemRevision, Double>> iterator = set.iterator();

									while(iterator.hasNext()) {
										Map.Entry mentry = (Map.Entry)iterator.next();
										TCComponentItemRevision comp= (TCComponentItemRevision) mentry.getKey();
										double ref_qty = (double)mentry.getValue();

										if (comp!=null)
										{
											if(comp.toString().equals(pack2Text)){
												pack2CompRevs.put(comp,ref_qty);
												break;
											}
										}
									}
								}
							}
							TableItem [] pack3Items=pack3Box.getItems();
							for(int i = 0;i<pack3Items.length;i++){
								String pack3Text =  pack3Items[i].getText();
								Text qty = (Text) pack3Items[i].getData("refcount");
								double quantity = Double.parseDouble(qty.getText());
								int inx = pack3Text.indexOf("/");
								String refItem = pack3Text.substring(0,inx);
								String values[] = { refItem };
								boolean found = false;
								for(int j=0;j<components.length;j++){
									if(components[j].toString().equals(pack3Text)){
										TCComponentItemRevision specRev = (TCComponentItemRevision)components[j].getComponent();
										//System.out.println(specRev.toString());
										pack3CompRevs.put(specRev,quantity);
										found=true;
										break;
									}
									else{
										try {
											TCComponent[] components = UnileverQueryUtil.executeQuery(queryName, entries, values);
											if(components.length>0){
												AIFComponentContext[] refItemRevs = components[0].getChildren();
												TCComponentItemRevision refItemrev = (TCComponentItemRevision) refItemRevs[refItemRevs.length-1].getComponent();
												pack3CompRevs.put(refItemrev,quantity);
												found=true;
												break;
											}
										} catch (TCException e1) {
											// TODO Auto-generated catch block
											e1.printStackTrace();
										}
									}
								}

								if (found== false)
								{
									Set<Entry<TCComponentItemRevision, Double>> set = refCompRevs.entrySet();
									Iterator<Entry<TCComponentItemRevision, Double>> iterator = set.iterator();

									while(iterator.hasNext()) {
										Map.Entry mentry = (Map.Entry)iterator.next();
										TCComponentItemRevision comp= (TCComponentItemRevision) mentry.getKey();
										double ref_qty = (double)mentry.getValue();

										if (comp!=null)
										{
											if(comp.toString().equals(pack3Text)){
												pack3CompRevs.put(comp,ref_qty);
												break;
											}
										}
									}
								}
							}
							TableItem [] pack4Items=pack4Box.getItems();
							for(int i = 0;i<pack4Items.length;i++){
								String pack4Text = pack4Items[i].getText();
								Text qty = (Text) pack4Items[i].getData("refcount");
								double quantity = Double.parseDouble(qty.getText());
								int inx = pack4Text.indexOf("/");
								String refItem = pack4Text.substring(0,inx);
								String values[] = { refItem };
								boolean found = false;
								for(int j=0;j<components.length;j++){
									if(components[j].toString().equals(pack4Text)){
										TCComponentItemRevision specRev = (TCComponentItemRevision)components[j].getComponent();
										//System.out.println(specRev.toString());
										pack4CompRevs.put(specRev,quantity);
										found=true;
										break;
									}
									else{
										try {
											TCComponent[] components = UnileverQueryUtil.executeQuery(queryName, entries, values);
											if(components.length>0){
												AIFComponentContext[] refItemRevs = components[0].getChildren();
												TCComponentItemRevision refItemrev = (TCComponentItemRevision) refItemRevs[refItemRevs.length-1].getComponent();
												pack4CompRevs.put(refItemrev,quantity);
												found=true;
												break;
											}
										} catch (TCException e1) {
											// TODO Auto-generated catch block
											e1.printStackTrace();
										}
									}
								}

								if (found== false)
								{
									Set<Entry<TCComponentItemRevision, Double>> set = refCompRevs.entrySet();
									Iterator<Entry<TCComponentItemRevision, Double>> iterator = set.iterator();

									while(iterator.hasNext()) {
										Map.Entry mentry = (Map.Entry)iterator.next();
										TCComponentItemRevision comp= (TCComponentItemRevision) mentry.getKey();
										double ref_qty = (double)mentry.getValue();

										if (comp!=null)
										{
											if(comp.toString().equals(pack4Text)){
												pack4CompRevs.put(comp,ref_qty);
												break;
											}
										}
									}
								}
							}

							String pack1_label = pack_1_label.getText();
							String pack2_label = pack_2_label.getText();
							String pack3_label = pack_3_label.getText();
							String pack4_label = pack_4_label.getText();
							String pack5_label = pack_5_label.getText();
							String [] pack_id_labels = new String [5];
							pack_id_labels[0] = pack1_label;
							pack_id_labels[1] = pack2_label;
							pack_id_labels[2] = pack3_label;
							pack_id_labels[3] = pack4_label;
							pack_id_labels[4] = pack5_label;

							if ((refCompRevs.size()!=0) || (pack1CompRevs.size()!=0) || (pack2CompRevs.size()!=0) || (pack3CompRevs.size()!=0) || (pack4CompRevs.size()!=0) )
							{
								boolean savereport = saveReportButton.getSelection();
								totalRep.genEcelrep(itemRevision,refCompRevs,pack1CompRevs,pack2CompRevs,pack3CompRevs,pack4CompRevs,pack_id_labels,descBox.getText(),no_of_pri_packs,savereport);
								session.queueOperation(totalRep);
							}
							else
							{
								MessageBox.post(shlSustainabilityReport ,"Please select at least one component in a pack for correct Total Pack Impact report generation","Information",MessageBox.INFORMATION);
							}
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
				}
			}
		});
		genReportButton.setBounds(110, (int)(ReportTypeNotSelected*.8), 125, 25);
		genReportButton.setText("Generate Report");

		final Button cancelButton = new Button(composite, SWT.NONE);
		cancelButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				((Button)e.getSource( )).getShell().dispose( );
			}
		});
		cancelButton.setBounds(240,(int)(ReportTypeNotSelected*.8), 80, 25);
		cancelButton.setText("Cancel");

		grpSelectGroup = new Group(composite, SWT.NONE);
		grpSelectGroup.setBounds(476, 45, 110, 175	);
		grpSelectGroup.setText("Select Group");
		grpSelectGroup.setVisible(false);

		asyCheckButton = new Button[6];

		for (int i = 0 ; i < 5 ; i++)
		{
			asyCheckButton[i] = new Button(grpSelectGroup, SWT.CHECK);
			asyCheckButton[i].setBounds(10, ((i+1)*25), 93, 16);
			asyCheckButton[i].setText("Pack "+ (i+1));
			asyCheckButton[i].setVisible(false);
			/*asyCheckButton[i].addSelectionListener(new SelectionAdapter() {     
				public void widgetSelected(SelectionEvent event) {         
					if ( ((Button)(event.getSource())).getSelection() == true)
						genReportButton.setEnabled(true);
				}      
			});*/
		}

		reportTypeCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {

				genReportButton.setVisible(true);

				String value =  ((Combo)(event.getSource())).getText();

				genReportButton.setBounds(200, (int)(ReportTypeNotSelected*8.9), 106, 25);
				cancelButton.setBounds(310,(int)(ReportTypeNotSelected*8.9), 75, 25);
				composite_1.setVisible(true);
				//label_1.setBounds(10,(int)(ReportTypeNotSelected*8.8), 590, 6);

				if (value.compareToIgnoreCase("Total Pack Impact Report") ==0)
				{
					composite_1.setVisible(false);
					composite_2.setVisible(true);
					shlSustainabilityReport.setSize(630,(int)( ReportTypeNotSelected *14.5));
					composite.setBounds(5, 5, 610,(int)( ReportTypeNotSelected *13.7));
					composite_2.setBounds(10, 80, 590, 560);

					descLabel.setVisible(true);
					descLabel.setText("Description:");
					descBox.setVisible(true);
					//label_1.setBounds(10,(int)(ReportTypeNotSelected*14.6), 600, 8);
					//label_3.setBounds(10, 25, 600, 8);
					//descLabel.setBounds(10,40,80,20);
					//descBox.setBounds(90,40,340,75);

					descLabel.setBounds(10,(int) (ReportTypeNotSelected * 0.7),80,20);
					descBox.setBounds(105,38,500,40);

					saveReportLabel.setVisible(true);
					saveReportButton.setVisible(true);
					saveReportButton.setBounds(10, (int)(ReportTypeNotSelected*13), 25, 25);
					saveReportLabel.setBounds(30, (int)(ReportTypeNotSelected*13.05), 220, 25);

					genReportButton.setBounds(325, (int)(ReportTypeNotSelected*13), 126, 25);
					cancelButton.setBounds(460,(int)(ReportTypeNotSelected*13), 75, 25);
					//label_2.setBounds(10,120, 600, 8);

					txt.setVisible(true); 
					referenceText1.setVisible(true);
					referenceText1.setEditable(true);
					referenceText2.setBounds(10, 325, 140, 23);
					referenceText2.setToolTipText("Input Pack Component ID like C510000000XXX or click on Browse to select a Pack Component");

					final Label pack_1 = new Label(composite_2,SWT.NONE);
					pack_1.setText("Pack 1");
					pack_1.setBounds(325, 20, 45, 20);

					final Label pack_2 = new Label(composite_2,SWT.NONE);
					pack_2.setText("Pack 2");
					pack_2.setBounds(325, 130, 45, 20);

					final Label pack_3 = new Label(composite_2,SWT.NONE);
					pack_3.setText("Pack 3");
					pack_3.setBounds(325, 240, 45, 20);

					final Label pack_4 = new Label(composite_2,SWT.NONE);
					pack_4.setText("Pack 4");
					pack_4.setBounds(325, 350, 45, 20);

					final Label pack_5 = new Label(composite_2,SWT.NONE);
					pack_5.setText("Pack 5");
					pack_5.setBounds(325, 460, 45, 20);

					packRefBox.setBounds(325, 40, 260, 80);
					//packRefBox.setHeaderVisible(true);
					packRefBox.setLinesVisible(true);

					pack1Box.setBounds(325, 150, 260, 80);
					//pack1Box.setHeaderVisible(true);
					pack1Box.setLinesVisible(true);

					pack2Box.setBounds(325, 260, 260, 80);
					//pack2Box.setHeaderVisible(true);
					pack2Box.setLinesVisible(true);

					pack3Box.setBounds(325, 370, 260, 80);
					//pack3Box.setHeaderVisible(true);
					pack3Box.setLinesVisible(true);

					pack4Box.setBounds(325, 480, 260, 80);
					//pack4Box.setHeaderVisible(true);
					pack4Box.setLinesVisible(true);

					final Button packRef_Add = new Button(composite_2,SWT.NONE);
					packRef_Add.setText(">");
					packRef_Add.setBounds(285, 50, 25, 25);
					packRef_Add.addSelectionListener(new SelectionAdapter(){
						public void widgetSelected(SelectionEvent e) {
							TableItem [] items = table1.getItems();
							TableItem [] items1 = table2Ref.getItems();
							TableItem [] allItems =  (TableItem[])ArrayUtils.addAll(items, items1);
							for(int i = 0 ; i<allItems.length;i++){
								if(allItems[i].getChecked()){

									TableItem [] packRefBox_items = packRefBox.getItems();

									boolean already_exists = false;

									for(int p = 0 ; p<packRefBox_items.length;p++){
										if (packRefBox_items[p].getText().compareTo(allItems[i].getText()) ==0)
											already_exists=true;
									}

									if (already_exists==true)
										continue;

									TableItem item = new TableItem(packRefBox,SWT.NONE);
									//TableEditor editor = new TableEditor(packRefBox);
									//Text text = (Text) items[i].getData("comp1");
									item.setText(allItems[i].getText());
									//editor.grabHorizontal = true;
									//item.setData("text", text);
									item.setChecked(false);
									//editor.setEditor(text, item, 0);

									TableEditor editor = new TableEditor(packRefBox);
									Text count = new Text(packRefBox, SWT.NONE);
									count.setText("1");
									//boolean is_primary_pack = is_comp_primary(item.getText(),primary_pack_list);
									//if(is_primary_pack)
									//	count.setEditable(true);
									//else
									//	count.setEditable(false);
									count.setVisible(true);
									editor.grabHorizontal = true;
									item.setData("refcount", count);
									editor.setEditor(count, item, 1);
								}
							}
						}
					});
					final Button packRef_Del = new Button(composite_2,SWT.NONE);
					packRef_Del.setText("<");
					packRef_Del.setBounds(285, 75, 25, 25);
					packRef_Del.addSelectionListener(new SelectionAdapter(){
						public void widgetSelected(SelectionEvent e) {
							TableItem [] items = packRefBox.getItems();
							for(int i = 0 ; i<items.length;i++){
								if(items[i].getChecked()){
									//Text t1 = (Text) items[i].getData("text");
									Text t2 = (Text) items[i].getData("refcount");
									//t1.dispose();
									t2.dispose();
									packRefBox.remove(packRefBox.indexOf(items[i]));
								}
							}
						}
					});

					final Button pack1_Add = new Button(composite_2,SWT.NONE);
					pack1_Add.setText(">");
					pack1_Add.setBounds(285, 160, 25, 25);
					pack1_Add.addSelectionListener(new SelectionAdapter(){
						public void widgetSelected(SelectionEvent e) {
							TableItem [] items = table1.getItems();
							TableItem [] items1 = table2Ref.getItems();
							TableItem [] allItems =  (TableItem[])ArrayUtils.addAll(items, items1);
							for(int i = 0 ; i<allItems.length;i++){
								if(allItems[i].getChecked()){

									TableItem [] pack1Box_items = pack1Box.getItems();

									boolean already_exists = false;

									for(int p = 0 ; p<pack1Box_items.length;p++){
										if (pack1Box_items[p].getText().compareTo(allItems[i].getText()) ==0)
											already_exists=true;
									}

									if (already_exists==true)
										continue;

									TableItem item = new TableItem(pack1Box,SWT.NONE);
									//TableEditor editor = new TableEditor(pack1Box);
									//Text text = (Text) items[i].getData("comp1");
									item.setText(allItems[i].getText());
									//editor.grabHorizontal = true;
									//item.setData("text", text);
									item.setChecked(false);
									//editor.setEditor(text, item, 0);

									TableEditor editor = new TableEditor(pack1Box);
									Text count = new Text(pack1Box, SWT.NONE);
									count.setText("1");
									//setEditable only if the component is primary component
									//boolean is_primary_pack = is_comp_primary(item.getText(),primary_pack_list);
									//if(is_primary_pack)
									//	count.setEditable(true);
									//else
									//	count.setEditable(false);
									count.setVisible(true);
									editor.grabHorizontal = true;
									item.setData("refcount", count);
									editor.setEditor(count, item, 1);
								}
							}
						}
					});

					final Button pack1_Del = new Button(composite_2,SWT.NONE);
					pack1_Del.setText("<");
					pack1_Del.setBounds(285, 185, 25, 25);
					pack1_Del.addSelectionListener(new SelectionAdapter(){
						public void widgetSelected(SelectionEvent e) {
							TableItem [] items = pack1Box.getItems();
							for(int i = 0 ; i<items.length;i++){
								if(items[i].getChecked()){
									//Text t1 = (Text) items[i].getData("text");
									Text t2 = (Text) items[i].getData("refcount");
									//t1.dispose();
									t2.dispose();
									pack1Box.remove(pack1Box.indexOf(items[i]));
								}
							}
						}
					});

					final Button pack2_Add = new Button(composite_2,SWT.NONE);
					pack2_Add.setText(">");
					pack2_Add.setBounds(285, 270, 25, 25);
					pack2_Add.addSelectionListener(new SelectionAdapter(){
						public void widgetSelected(SelectionEvent e) {
							TableItem [] items = table1.getItems();
							TableItem [] items1 = table2Ref.getItems();
							TableItem [] allItems =  (TableItem[])ArrayUtils.addAll(items, items1);
							for(int i = 0 ; i<allItems.length;i++){
								if(allItems[i].getChecked()){

									TableItem [] pack2Box_items = pack2Box.getItems();

									boolean already_exists = false;

									for(int p = 0 ; p<pack2Box_items.length;p++){
										if (pack2Box_items[p].getText().compareTo(allItems[i].getText()) ==0)
											already_exists=true;
									}

									if (already_exists==true)
										continue;

									TableItem item = new TableItem(pack2Box,SWT.NONE);
									//TableEditor editor = new TableEditor(pack2Box);
									//Text text = (Text) items[i].getData("comp1");
									item.setText(allItems[i].getText());
									//editor.grabHorizontal = true;
									//item.setData("text", text);
									item.setChecked(false);
									//editor.setEditor(text, item, 0);

									TableEditor editor = new TableEditor(pack2Box);
									Text count = new Text(pack2Box, SWT.NONE);
									count.setText("1");
									count.setText("1");
									//setEditable only if the component is primary component
									//boolean is_primary_pack = is_comp_primary(item.getText(),primary_pack_list);
									//if(is_primary_pack)
									//	count.setEditable(true);
									//else
									//	count.setEditable(false);
									count.setVisible(true);
									editor.grabHorizontal = true;
									item.setData("refcount", count);
									editor.setEditor(count, item, 1);
								}
							}
						}
					});

					final Button pack2_Del = new Button(composite_2,SWT.NONE);
					pack2_Del.setText("<");
					pack2_Del.setBounds(285, 295, 25, 25);
					pack2_Del.addSelectionListener(new SelectionAdapter(){
						public void widgetSelected(SelectionEvent e) {
							TableItem [] items = pack2Box.getItems();
							for(int i = 0 ; i<items.length;i++){
								if(items[i].getChecked()){
									//Text t1 = (Text) items[i].getData("text");
									Text t2 = (Text) items[i].getData("refcount");
									//t1.dispose();
									t2.dispose();
									pack2Box.remove(pack2Box.indexOf(items[i]));
								}
							}
						}
					});

					final Button pack3_Add = new Button(composite_2,SWT.NONE);
					pack3_Add.setText(">");
					pack3_Add.setBounds(285, 380, 25, 25);
					pack3_Add.addSelectionListener(new SelectionAdapter(){
						public void widgetSelected(SelectionEvent e) {
							TableItem [] items = table1.getItems();
							TableItem [] items1 = table2Ref.getItems();
							TableItem [] allItems =  (TableItem[])ArrayUtils.addAll(items, items1);
							for(int i = 0 ; i<allItems.length;i++){
								if(allItems[i].getChecked()){

									TableItem [] pack3Box_items = pack3Box.getItems();

									boolean already_exists = false;

									for(int p = 0 ; p<pack3Box_items.length;p++){
										if (pack3Box_items[p].getText().compareTo(allItems[i].getText()) ==0)
											already_exists=true;
									}

									if (already_exists==true)
										continue;

									TableItem item = new TableItem(pack3Box,SWT.NONE);
									//TableEditor editor = new TableEditor(pack3Box);
									//Text text = (Text) items[i].getData("comp1");
									item.setText(allItems[i].getText());
									//editor.grabHorizontal = true;
									//item.setData("text", text);
									item.setChecked(false);
									//editor.setEditor(text, item, 0);

									TableEditor editor = new TableEditor(pack3Box);
									Text count = new Text(pack3Box, SWT.NONE);
									count.setText("1");
									//count.setText("1");
									//setEditable only if the component is primary component
									//boolean is_primary_pack = is_comp_primary(item.getText(),primary_pack_list);
									//if(is_primary_pack)
									//	count.setEditable(true);
									//else
									//	count.setEditable(false);
									count.setVisible(true);
									editor.grabHorizontal = true;
									item.setData("refcount", count);
									editor.setEditor(count, item, 1);
								}
							}
						}
					});

					final Button pack3_Del = new Button(composite_2,SWT.NONE);
					pack3_Del.setText("<");
					pack3_Del.setBounds(285, 405, 25, 25);
					pack3_Del.addSelectionListener(new SelectionAdapter(){
						public void widgetSelected(SelectionEvent e) {
							TableItem [] items = pack3Box.getItems();
							for(int i = 0 ; i<items.length;i++){
								if(items[i].getChecked()){
									//Text t1 = (Text) items[i].getData("text");
									Text t2 = (Text) items[i].getData("refcount");
									//t1.dispose();
									t2.dispose();
									pack3Box.remove(pack3Box.indexOf(items[i]));
								}
							}
						}
					});

					final Button pack4_Add = new Button(composite_2,SWT.NONE);
					pack4_Add.setText(">");
					pack4_Add.setBounds(285, 485, 25, 25);
					pack4_Add.addSelectionListener(new SelectionAdapter(){
						public void widgetSelected(SelectionEvent e) {
							TableItem [] items = table1.getItems();
							TableItem [] items1 = table2Ref.getItems();
							TableItem [] allItems =  (TableItem[])ArrayUtils.addAll(items, items1);
							for(int i = 0 ; i<allItems.length;i++){
								if(allItems[i].getChecked()){

									TableItem [] pack4Box_items = pack4Box.getItems();

									boolean already_exists = false;

									for(int p = 0 ; p<pack4Box_items.length;p++){
										if (pack4Box_items[p].getText().compareTo(allItems[i].getText()) ==0)
											already_exists=true;
									}

									if (already_exists==true)
										continue;

									TableItem item = new TableItem(pack4Box,SWT.NONE);
									//TableEditor editor = new TableEditor(pack4Box);
									//Text text = (Text) items[i].getData("comp1");
									item.setText(allItems[i].getText());
									//editor.grabHorizontal = true;
									//item.setData("text", text);
									item.setChecked(false);
									//editor.setEditor(text, item, 0);

									TableEditor editor = new TableEditor(pack4Box);
									Text count = new Text(pack4Box, SWT.NONE);
									count.setText("1");
									//setEditable only if the component is primary component
									//boolean is_primary_pack = is_comp_primary(item.getText(),primary_pack_list);
									//if(is_primary_pack)
									//	count.setEditable(true);
									//else
									//	count.setEditable(false);
									count.setVisible(true);
									editor.grabHorizontal = true;
									item.setData("refcount", count);
									editor.setEditor(count, item, 1);
								}
							}
						}
					});

					final Button pack4_Del = new Button(composite_2,SWT.NONE);
					pack4_Del.setText("<");
					pack4_Del.setBounds(285, 510, 25, 25);
					pack4_Del.addSelectionListener(new SelectionAdapter(){
						public void widgetSelected(SelectionEvent e) {
							TableItem [] items = pack4Box.getItems();
							for(int i = 0 ; i<items.length;i++){
								if(items[i].getChecked()){
									//Text t1 = (Text) items[i].getData("text");
									Text t2 = (Text) items[i].getData("refcount");
									//t1.dispose();
									t2.dispose();
									pack4Box.remove(pack4Box.indexOf(items[i]));
								}
							}
						}
					});

					final Label hash_ref = new Label(composite_2,SWT.NONE);
					hash_ref.setText("#");
					hash_ref.setBounds(565, 25, 10, 20);

					final Label hash_1 = new Label(composite_2,SWT.NONE);
					hash_1.setText("#");
					hash_1.setBounds(565, 135, 10, 20);

					final Label hash_2 = new Label(composite_2,SWT.NONE);
					hash_2.setText("#");
					hash_2.setBounds(565, 245, 10, 20);

					final Label hash_3 = new Label(composite_2,SWT.NONE);
					hash_3.setText("#");
					hash_3.setBounds(565, 355, 10, 20);

					final Label hash_4 = new Label(composite_2,SWT.NONE);
					hash_4.setText("#");
					hash_4.setBounds(565, 465, 10, 20);

					//genReportButton.setEnabled(true);
				}
				else if  (value.compareToIgnoreCase("Component Impact Report") ==0)
				{
					composite_2.setVisible(false);
					shlSustainabilityReport.setSize(485,(int)( ReportTypeNotSelected *12.3));
					composite.setBounds(5, 5, 470,(int)( ReportTypeNotSelected *11.5));
					composite_1.setBounds(10, 100, 450, 425);

					saveReportLabel.setVisible(true);
					saveReportButton.setVisible(true);
					saveReportButton.setBounds(10,(int)(ReportTypeNotSelected*10.8), 25, 25);
					saveReportLabel.setBounds(30, (int)(ReportTypeNotSelected*10.85), 225, 25);

					genReportButton.setBounds(260, (int)(ReportTypeNotSelected*10.8), 125, 25);
					cancelButton.setBounds(390,(int)(ReportTypeNotSelected*10.8), 75, 25);

					referenceLabel.setVisible(true);
					referenceLabel.setText("Reference Component ID:");
					referenceText.setVisible(true);
					referenceText.setToolTipText("Input Pack Component ID like C510000000XXX or click on Browse to select a Pack Component");
					referenceButton.setVisible(true);

					referenceLabel.setBounds(0, (int)(ReportTypeNotSelected*8), 170, 20);
					referenceText.setBounds(180, (int)(ReportTypeNotSelected*8), 150, 23);
					referenceButton.setBounds(340, (int)(ReportTypeNotSelected*8), 70, 25);

					//label_3.setBounds(0, 30, 590, 8);
					//label_1.setBounds(10,(int)(ReportTypeNotSelected*2.6), 590, 6);

					descLabel.setVisible(true);
					descLabel.setText("Description:");
					descBox.setVisible(true);

					descLabel.setBounds(10,(int) (ReportTypeNotSelected * 0.7),80,20);
					descBox.setBounds(105,38,345,60);

					//label_2.setBounds(10,530, 590, 6);

					SelectColumn.setWidth(104);
					//PackColumn.setWidth(0);
				}
				else if (value.compareToIgnoreCase("Environment Packaging Material") ==0)
				{
					composite_2.setVisible(false);
					shlSustainabilityReport.setSize(490,(int)( ReportTypeNotSelected *11.20));
					composite.setBounds(5, 5, 470,(int)( ReportTypeNotSelected *10.45));
					composite_1.setBounds(10, 105, 434, 370);

					descLabel.setVisible(true);
					descLabel.setText("Description:");
					descBox.setVisible(true);

					//label_1.setBounds(10,(int)(ReportTypeNotSelected*2.6), 590, 6);
					descLabel.setBounds(10,(int) (ReportTypeNotSelected),80,20);
					descBox.setBounds(105,38,340,60);

					//label_2.setBounds(10,(int)(ReportTypeNotSelected*10.4), 590, 6);
					//label_3.setVisible(false);

					saveReportLabel.setVisible(true);
					saveReportButton.setVisible(true);
					saveReportButton.setBounds(10, (int)(ReportTypeNotSelected*9.7), 25, 25);
					saveReportLabel.setBounds(30, (int)(ReportTypeNotSelected*9.75), 220, 25);


					genReportButton.setBounds(250, (int)(ReportTypeNotSelected*9.7), 125, 25);
					cancelButton.setBounds(380,(int)(ReportTypeNotSelected*9.7), 75, 25);

					SelectColumn.setWidth(104);
					//PackColumn.setWidth(0);
					grpSelectGroup.setVisible(false);

					referenceLabel.setVisible(false);
					referenceText.setVisible(false);
					referenceButton.setVisible(false);
				}
			}
		});

	}

	private boolean is_comp_primary(TCComponentItemRevision itemRev) {
		// TODO Auto-generated method stub
		boolean is_primary= true;
		
		try {
			
			TCProperty prop = itemRev.getTCProperty(PAMConstant.CONSUMER_UNIT);
			
			if (prop!=null)
			{
				String consumer_unit = prop.getUIFValue();
				
				if ((consumer_unit.compareToIgnoreCase("true") == 0) || (consumer_unit.compareToIgnoreCase("true") == 0))
				{
					if (consumer_unit.compareToIgnoreCase("true") == 0)
					{
						return true;
					}
					else if (consumer_unit.compareToIgnoreCase("false") == 0)
					{
						return false;
					}
				}
			}

			TCComponent item =itemRev.getItem();
			String obj_type=item.getType();
			is_primary=Arrays.asList(primary_pack_list).contains(obj_type);
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		return is_primary;
	}

	private boolean is_comp_primary(String item, String[] primary_pack_list) {
		// TODO Auto-generated method stub
		String queryName = "Item Revision...";
		String entries[] = { "Item ID" , "Revision"};
		String item_id = item.split("/")[0];
		String revision_id = (item.split("/")[1]).split("-")[0];
		String values [] = {item_id,revision_id};
		
		boolean is_primary= false;
		try {
			TCComponent[] components = UnileverQueryUtil.executeQuery(queryName, entries, values);
			
			if (components!=null)
			{
				TCProperty prop = components[0].getTCProperty(PAMConstant.CONSUMER_UNIT);
				
				if (prop!=null)
				{
					String consumer_unit = prop.getUIFValue();
					
					if ((consumer_unit.compareToIgnoreCase("true") == 0) || (consumer_unit.compareToIgnoreCase("true") == 0))
					{
						if (consumer_unit.compareToIgnoreCase("true") == 0)
						{
							return true;
						}
						else if (consumer_unit.compareToIgnoreCase("false") == 0)
						{
							return false;
						}
					}
				}
				
			}
			
			String obj_type=components[0].getType();
			is_primary=Arrays.asList(primary_pack_list).contains(obj_type);
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return is_primary;
	}
}

