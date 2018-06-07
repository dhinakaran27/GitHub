package com.unilever.rac.handlers.UnileverAddNewPAM;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.commands.paste.PasteOperation;
import com.teamcenter.rac.common.TCTypeRenderer;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.unilever.rac.ui.common.UL4Common;
import com.unilever.rac.ui.saveascomponent.UL4ComponentSaveAsDevelopmentOperation;
import com.unilever.rac.util.UnileverQueryUtil;

public class UnileverAddNewPAMDialog extends TitleAreaDialog {

	
	private TCSession session 										= null;
	private TCComponentItemRevision selectedItemRevision;	  
	private org.eclipse.swt.graphics.Color color 				 	= null ;  
	private Table table 											= null;
	private ArrayList selectedPAMSpecsList 							= new ArrayList();
	private ArrayList retrivePAMSpecsList 							= new ArrayList();
	private String pamSpecIDAndRevID 								= "";
	private Set<String> setOfPAMIds 								= new HashSet<String>();
	private Set<TCComponent> setOfAlreadyExistingPAMSpec 			= new HashSet<TCComponent>();
	private Map pamSpecMap 											= new Hashtable();
	private boolean isItemChecked 									= false;
	//private Map mParam = null;
	private String relation 										= null;
	protected Shell shell;
	
	
	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 * @param mParamMap 
	 * @param selection 
	 */
	public UnileverAddNewPAMDialog(Shell parent, int style, TCComponentItemRevision selection, TCSession session, String relation ) {
		super(parent);
		
		this.session = session;
		//this.mParam = mParamMap;
		this.relation = relation;
		selectedItemRevision = selection;
		selectedPAMSpecsList.clear();
		pamSpecIDAndRevID = "";
		setOfPAMIds.clear();
		pamSpecMap.clear();
		isItemChecked = false;
	}

	@Override
	  protected void createButtonsForButtonBar(Composite parent) 
	  {
		   super.createButtonsForButtonBar(parent);
		
		  
		   Button ok = getButton(IDialogConstants.OK_ID);
		   ok.setText("OK");
		   ok.setEnabled(true);
		   setButtonLayoutData(ok);
		
		   Button cancel = getButton(IDialogConstants.CANCEL_ID);
		   cancel.setText("Cancel");
		   setButtonLayoutData(cancel);
		  //dialogArea.setBackground(super.getShell().getBackground());
		   
	}
	
	@Override
	  protected void configureShell(Shell newShell) 
	  {
		  try
		  {
			  super.configureShell(newShell);
			  newShell.setText("PAM Specification");
			  newShell.setSize(500, 400);
			  Point size = newShell.computeSize(-1, -1); 
			  Rectangle screen = newShell.getDisplay().getMonitors()[0].getBounds(); 
			  newShell.setBounds( (screen.width-size.x-500)/2, (screen.height-size.y-300)/2, 500, 400 ); 
		  }
		  catch(Exception tcEx)
		  {
			  System.out.println(tcEx);
		  }
	    
	  }

	@Override
	protected boolean isResizable() {
	    return false;
	}
	
	@Override
	  protected Control createDialogArea(Composite parent) 
	  {

		  Composite mainComposite = ( Composite ) super.createDialogArea( parent );
		  
		  GridLayout layout = new GridLayout();
		  layout.marginLeft = 20;
		  layout.marginTop = 5;
		  
		  mainComposite.setLayout(layout);
		  final ScrolledComposite composite = new ScrolledComposite(mainComposite, SWT.V_SCROLL | SWT.H_SCROLL);
		  composite.setLayout(new GridLayout());
		  composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));		  

		  table = new Table(composite, SWT.CHECK | SWT.VIRTUAL | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		    
		  TCComponentItemRevision[] finalComponentArr = getRelatedPAMSpecs();
		  
		  if(finalComponentArr == null || finalComponentArr.length == 0)
		  {
		  		MessageBox.post(shell, "No PAM specifications are available for association.", "Error", MessageBox.INFORMATION);
				super.close();
		  		return null;
		  }
		  
		  session.setStatus( "Getting Associated PAM Specs..." );
		  
		  	//TODO:: Now filter them using stages - 
		  	ArrayList<TCComponentItemRevision> filteredPAMSpecList = new ArrayList<TCComponentItemRevision>();
		  	try
		  	{
			  	if(finalComponentArr != null && finalComponentArr.length != 0)
			  	{
			  		for (TCComponentItemRevision eachCompIR : finalComponentArr) 
			  		{
			  			//TODO:: Get PAMSpecifications from PACK Components					  	
			  			TCComponent[] pamSpecItems = (TCComponent[]) eachCompIR.getRelatedComponents(UL4Common.PAM_SPECIFICATION_RELATION);
				  		if(pamSpecItems != null && pamSpecItems.length != 0)
				  		{
				  			for(TCComponent pamSpecItem : pamSpecItems)
				  			{
				  				TCComponentItemRevision pamSpecItemRevision = (TCComponentItemRevision) pamSpecItem;
				  				
				  				//TODO:: Check if this/current PAMSpecRevision already linked with P&PSpecificationRevision
				  				if(isPAMSpecAlreadyLinkedToPnP(pamSpecItemRevision))
				  					continue;
				  				//Filter each PAMSpec Revision based on the stage. It has to conform only 2 stages "PILOT LOCK" OR "CAPABILITY BUILD"
				  				
				  				String pamStageInfo = pamSpecItemRevision.getProperty(UL4Common.STAGE_ATTR);
				  				
				  				if(	UL4Common.PAM_STAGE_PILOT_LOCK.equals(pamStageInfo) ||
				  					UL4Common.PAM_STAGE_CAPABILITY_BUILD.equals(pamStageInfo) )
				  				{
				  					//Always get the latest revision.
				  					TCComponentItemRevision latestPAMSpecItemRevision = pamSpecItemRevision.getItem().getLatestItemRevision();
				  					
				  					//Check if the latest Revision is RELEASED OR WORKING - then only show in the dialog.
									String releaseStatus = latestPAMSpecItemRevision.getProperty(UL4Common.RELEASE_STATUS_LIST_ATTR);
									if(UL4Common.RELEASED_STATUS.equals(releaseStatus) || "".equals(releaseStatus))
									{
										pamSpecIDAndRevID = "";
										pamSpecIDAndRevID = latestPAMSpecItemRevision.getTCProperty(UL4Common.ITEMID).toString();
										pamSpecIDAndRevID = pamSpecIDAndRevID + "/" + latestPAMSpecItemRevision.getTCProperty(UL4Common.REVID).toString();
										setOfPAMIds.add(pamSpecIDAndRevID);
										filteredPAMSpecList.add(latestPAMSpecItemRevision);
										pamSpecMap.put(pamSpecIDAndRevID, latestPAMSpecItemRevision);
									}				  					
				  				}
				  			}// for(TCComponentItemRevision pamSpecIR : pamSpecRevisions) ends			  			
				  		}// if(pamSpecRevisions != null) ends
			  		}// for (TCComponentItemRevision eachCompIR : finalComponentArr)  ends
			  	}// if(finalComponentArr != null && finalComponentArr.length != 0) ends
			  	
			  	if(filteredPAMSpecList.size() == 0)
			  	{
			  		MessageBox.post(shell, "No PAM specifications are available for association.", "Error", MessageBox.INFORMATION);
					super.close();
			  		return null;
			  	}
			  	
			  	for(int iCounter = 0; iCounter < filteredPAMSpecList.size(); iCounter++)
			  	{
			  		TableItem item = new TableItem(table, SWT.NONE);
				      
			  		item.setImage(TCTypeRenderer.getImage(filteredPAMSpecList.get(iCounter)));
			  		item.setText(" " + filteredPAMSpecList.get(iCounter) + " ");
			  	}
			  	
			  	session.setStatus( "Loading Complete." );
		  	}
		  	catch(TCException ex)
		  	{
		  		ex.printStackTrace();
		  	}
		  
		  	
		  	table.addListener(SWT.Selection, new Listener() 
		  	{
		        
		        public void handleEvent(Event e) 
		        {
		        	if(e.detail == SWT.CHECK)
		        	{
				        TableItem item = (TableItem) e.item;				        
				        if( item.getChecked())
				        {
				        	selectedPAMSpecsList.add(item);
				        }
		        	}		        	
		        }
		  	});
		    
		    table.setSize(400, 220);
		   
		    composite.setExpandHorizontal(true);
		    composite.setExpandVertical(true);
		    
		    //dialogArea.setBackground(super.getShell().getBackground());
		    
		   return mainComposite;
	  }
	
	protected boolean isPAMSpecAlreadyLinkedToPnP(TCComponentItemRevision pamSpecItemRevision)
	{
		try
		{
			TCComponent[] consumerUnitRelPAMSpecRevisions = selectedItemRevision.getRelatedComponents(relation);
			if(consumerUnitRelPAMSpecRevisions != null && consumerUnitRelPAMSpecRevisions.length != 0)
			{
				for(TCComponent eachComp : consumerUnitRelPAMSpecRevisions)
				{
					TCComponentItemRevision currentPAMSpecRevision = (TCComponentItemRevision) eachComp;
					if(currentPAMSpecRevision == pamSpecItemRevision)
						return true;
				}
			}			
		}
		catch(TCException ex)
		{
			ex.printStackTrace();
		}
		return false;
	}
	
	boolean contains(ArrayList<String> list, String name) {
	    for (String item : list) {
	        if (item.equals(name)) {
	            return true;
	        }
	    }
	    return false;
	}
	  
	@Override
	protected void okPressed() 
	{  
		selectedPAMSpecsList.clear();
		
		boolean isChecked = false;
		TableItem items[] = table.getItems();
		for(int l = 0; l < items.length; l++)
		{
			if(items[l].getChecked())
			{
				isChecked = true;
				selectedPAMSpecsList.add(items[l]);
			}
		}

		if(selectedPAMSpecsList.size() <= 0 || isChecked == false)
		{
			setErrorMessage("Please select atleast one of the PAM Specification to associate with P&P.");
			super.close();
			return;
		}
		
		//TODO:: Remove duplicates from selectedPAMSpecsList
		Object[] st = selectedPAMSpecsList.toArray();
	      for (Object s : st) {
	        if (selectedPAMSpecsList.indexOf(s) != selectedPAMSpecsList.lastIndexOf(s)) {
	        	selectedPAMSpecsList.remove(selectedPAMSpecsList.lastIndexOf(s));
	         }
	      }
	      
		retrivePAMSpecsList.clear();

		//Try to link the object between PAMSpecRevision and selected P&PSpecRevision
		try
		{
			for(int i = 0; i < selectedPAMSpecsList.size(); i++)
			{
				
				Iterator it = pamSpecMap.entrySet().iterator();
			    while (it.hasNext()) 
			    {
			        Map.Entry pair = (Map.Entry)it.next();
			        if(findMe(pair.getKey().toString(), selectedPAMSpecsList.get(i).toString()))
			        	retrivePAMSpecsList.add(pair.getValue());
			    }
			}

			if(retrivePAMSpecsList.size() != 0)
			{
				//Get the relation name from command-parameter 
				//TODO:: Command-paramter passing not working for TC10.1.XX?? Need to verify.
				//String relation = (String) mParam.get( "relation" );
				UnileverAddNewPAMOperation performConsumerUnitOperation = new UnileverAddNewPAMOperation(selectedItemRevision,session, retrivePAMSpecsList, relation);
																												
				session.queueOperation(performConsumerUnitOperation);
				
			}
			
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		
		super.okPressed();
		
	}
	
	public static boolean findMe(String subString, String mainString) 
	{
        boolean foundme = false;
        int max = mainString.length() - subString.length();
 
        // Java's Default "contains()" Method
        System.out.println(mainString.contains(subString) ? "mainString.contains(subString) Check Passed.."
                : "mainString.contains(subString) Check Failed..");
 
        // Implement your own Contains Method with Recursion
        checkrecusion: for (int i = 0; i <= max; i++) {
            int n = subString.length();
 
            int j = i;
            int k = 0;
 
            while (n-- != 0) {
                if (mainString.charAt(j++) != subString.charAt(k++)) {
                    continue checkrecusion;
                }
            }
            foundme = true;
            break checkrecusion;
        }
        System.out
        .println(foundme ? "\nResult: Yes, Match Found.."
                : "\nResult:  Nope - No Match Found..");
        return foundme;
    }
	
	protected TCComponentItemRevision[] getRelatedPAMSpecs()
	{
		try
		{
			session.setStatus( "Getting Project..." );
			  
			String tcProjectName = selectedItemRevision.getProperty(UL4Common.TCPROJECTIDS);
			String[] strProjectTokens =  tcProjectName.split(",");
			
			if(strProjectTokens.length != 1)
			{
				MessageBox.post(shell, "Multiple TC Projects assigned to selected P&P Specification object.", "Error", MessageBox.ERROR);
				return null;
			}	
			
			//Search Design Project based on TC Project Name. Design Project Name is actually concatenation of 'Project' string with TC Project Name.
			
			String entries1[] = { UL4Common.OBJ_NAME_ATTR_DISP_NAME, UL4Common.QRY_TYPE_PARAM};
			String values1[] = { strProjectTokens[0].trim(), UL4Common.QRY_PROJECT_PARAM };
			
			TCComponent[]  qryResults = UnileverQueryUtil.executeQuery(UL4Common.GENERAL_QRY_NAME, entries1, values1);
			if(qryResults != null && qryResults.length == 1)
			{
				TCComponentItem designProjectItem = (TCComponentItem) qryResults[0];
				TCComponentItemRevision designProjectLatestItemRevision = designProjectItem.getLatestItemRevision();

				session.setStatus( "Getting Relevant PACK Components..." );
				
				//Now get the PAMSpec Revisions attached to Design Project using relations U4_FinalRelation & U4_DevelopmentRelation
				/** NOT NEEDED
				 * TCComponent[] developmentRelComponents = designProjectLatestItemRevision.getRelatedComponents(UL4Common.DEVRELATION);
				 */
				TCComponent[] finalRelComponents = designProjectLatestItemRevision.getRelatedComponents(UL4Common.FINAL_RELATION);
				
				ArrayList pamSpecList = new ArrayList();
				/**
				 * NOT NEEDED
				if(developmentRelComponents != null && developmentRelComponents.length != 0)
				{
					//Get each form and do saveAs for each forms.
					for(TCComponent eachComp : developmentRelComponents)
					{
						if(eachComp instanceof TCComponentItemRevision)
						{
							TCComponentItemRevision pamSpecRevision = (TCComponentItemRevision) eachComp;
							if(pamSpecRevision != null)
								pamSpecList.add(pamSpecRevision);		
						}
					}					
					
				}
				*/
				
				if(finalRelComponents != null && finalRelComponents.length != 0)
				{
					//Get each form and do saveAs for each forms.
					for(TCComponent eachComp : finalRelComponents)
					{
						if(eachComp instanceof TCComponentItemRevision)
						{
							TCComponentItemRevision pamSpecRevision = (TCComponentItemRevision) eachComp;
							if(pamSpecRevision != null)
								pamSpecList.add(pamSpecRevision);		
						}
					}
					
				}
				
				//Convert all the list into PAMSpecIR
				
				return (TCComponentItemRevision[])pamSpecList.toArray(new TCComponentItemRevision[pamSpecList.size()]);
				
			}
			
		}
		catch(TCException ex)
		{
			ex.printStackTrace();
		}
				
		return null;
	}

	@Override
	  public void create() 
	  {
	    super.create();
	    setTitle(SELECT_PAM_SPECIFICATION_HEADER_INFORMATION_CONSTANT);
	    
	    if(UL4Common.PnP_TO_PAM_CU_RELATION.equals(this.relation))
	    {
	    	setMessage(SELECT_PAM_SPECIFICATION_HEADER_INFORMATION_CONSTANT + TO_LINK_UNDER_INFORMATION_CONSTANT + CONSUMER_UNIT_SECTION_INFORMATION_CONSTANT, 
	    				IMessageProvider.INFORMATION);
	    }
	    else if(UL4Common.PnP_TO_PAM_DU_RELATION.equals(this.relation))
	    {
	    	setMessage(SELECT_PAM_SPECIFICATION_HEADER_INFORMATION_CONSTANT + TO_LINK_UNDER_INFORMATION_CONSTANT + DISTRIBUTION_UNIT_SECTION_INFORMATION_CONSTANT, 
	    				IMessageProvider.INFORMATION);
	    }
	    else if(UL4Common.PnP_TO_PAM_CASEUNIT_RELATION.equals(this.relation))
	    {
	    	setMessage(SELECT_PAM_SPECIFICATION_HEADER_INFORMATION_CONSTANT + TO_LINK_UNDER_INFORMATION_CONSTANT + CASE_UNIT_SECTION_INFORMATION_CONSTANT, 
	    				IMessageProvider.INFORMATION);
	    }
	    color = super.getShell().getBackground();
	  }
	
	
		private static final String SELECT_PAM_SPECIFICATION_HEADER_INFORMATION_CONSTANT		= "Select PAM Specification(s) ";
		private static final String TO_LINK_UNDER_INFORMATION_CONSTANT 							= "to be associated to the ";
		private static final String CONSUMER_UNIT_SECTION_INFORMATION_CONSTANT					= "Consumer Unit section.";
		private static final String CASE_UNIT_SECTION_INFORMATION_CONSTANT						= "Case/Bag etc Unit section.";
		private static final String DISTRIBUTION_UNIT_SECTION_INFORMATION_CONSTANT				= "Distribution Unit section.";
		
		

}
