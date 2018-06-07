/**
 * 
 */
package com.unilever.rac.handlers.UnileverAddNewPAM;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.commands.paste.PasteOperation;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCSession;
import com.unilever.rac.ui.common.UL4Common;


/**
 * @author jayateertha.inamdar
 *
 */
public class UnileverAddNewPAMOperation extends AbstractAIFOperation 
{
	private String relation											= null;
	private TCSession session										= null;
	private TCComponentItemRevision selectedItemRevision 			= null;
	private Set<TCComponent> setOfAlreadyExistingPAMSpec 			= new HashSet<TCComponent>();
	private ArrayList retrivePAMSpecsList 							= new ArrayList();


	
	public UnileverAddNewPAMOperation( TCComponentItemRevision selectedItemRevision, TCSession session, ArrayList retrivePAMSpecsList, String relation) 
	{
		this.selectedItemRevision = selectedItemRevision;
		this.session = session;
		this.relation = relation;
		this.retrivePAMSpecsList = retrivePAMSpecsList;
	}

	@Override
	public void executeOperation() throws Exception 
	{
		setOfAlreadyExistingPAMSpec.clear();

		//Attach all PAMSpecs to the P&PSpecs: To do so, get first already existing PAMSpecs
		TCComponent[] alreadyExistingPAMSpecs = selectedItemRevision.getRelatedComponents(relation);
		for(TCComponent comp : alreadyExistingPAMSpecs)
			setOfAlreadyExistingPAMSpec.add(comp);
		
		//Now, add newly selected components
		TCComponentItemRevision[] currentlySelectedPAMSpecs = (TCComponentItemRevision[])retrivePAMSpecsList.toArray(new TCComponentItemRevision[retrivePAMSpecsList.size()]);
		for(TCComponent comp : currentlySelectedPAMSpecs)
			setOfAlreadyExistingPAMSpec.add(comp);
		
		selectedItemRevision.lock();
		selectedItemRevision.setRelated(relation,//UL4Common.PnP_TO_PAM_CU_RELATION, 
										(TCComponent[])setOfAlreadyExistingPAMSpec.toArray(new TCComponent[setOfAlreadyExistingPAMSpec.size()]));
		selectedItemRevision.save();				
		selectedItemRevision.unlock();
		selectedItemRevision.refresh();					
		
		PasteOperation paste2 = new PasteOperation(AIFUtility.getCurrentApplication(),selectedItemRevision,
				(TCComponent[])setOfAlreadyExistingPAMSpec.toArray(new TCComponent[setOfAlreadyExistingPAMSpec.size()]), 
				relation);	
		paste2.executeOperation();
		
	}

}
