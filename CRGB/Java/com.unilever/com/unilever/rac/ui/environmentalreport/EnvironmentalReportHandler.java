package com.unilever.rac.ui.environmentalreport;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Shell;

import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCSession;
import com.unilever.rac.ui.common.UL4Common;
import com.unilever.rac.ui.environmentalreport.EnvironmentalReportDialog;
import com.unilever.rac.util.UnileverPreferenceUtil;
import com.teamcenter.rac.util.MessageBox;

public class EnvironmentalReportHandler extends AbstractHandler {
	/**
	 * The constructor.
	 */
	public EnvironmentalReportHandler() {
	}

	private Shell shell = null;
	private InterfaceAIFComponent selectedComp = null;
	private TCComponentItemRevision itemRevision = null;
	/**
	 * the command has been executed, so extract extract the needed information
	 * from the application context.
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		//IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);

		AIFDesktop desktop 					= AIFUtility.getActiveDesktop();
		shell                         	    = desktop.getShell();
		selectedComp = AIFUtility.getActiveDesktop().getCurrentApplication().getTargetComponent();
		TCComponent comp= (TCComponent) selectedComp;
		
		if (comp.getTypeComponent().toString().compareTo("U4_ProjectRevision") != 0)
		{
			MessageBox.post( "Current selection is not a Project", "Information", MessageBox.INFORMATION);
			return null;
		}
		
		if(comp!=null){		
			try {
				final TCSession session = (TCSession)AIFDesktop.getActiveDesktop().getCurrentApplication().getSession();

				String[] phaserollout_groups = UnileverPreferenceUtil.getStringPreferenceValues(TCPreferenceService.TC_preference_site, 
						"UL_MIR_PhaseRollOut_OrganizationGroups");
				
				String[] phaserollout_projects = UnileverPreferenceUtil.getStringPreferenceValues(TCPreferenceService.TC_preference_site, 
						"UL_MIR_PhaseRollOut_Projects");

				String currentGroup  = session.getCurrentGroup().getStringProperty(UL4Common.NAME);

				boolean eligible = false;

				if ((phaserollout_groups!=null) && (currentGroup!=null))
				{
					for (int index = 0 ; index < phaserollout_groups.length ; index++)
					{
						if (currentGroup.compareTo(phaserollout_groups[index]) == 0)
							eligible=true;
					}
				}

				if (eligible == false)
				{
					String projectname = ((TCComponentItemRevision) comp).getStringProperty(UL4Common.OBJECT_NAME);

					if ((projectname!=null) && (phaserollout_projects!=null))
					{
						for (int index = 0 ; index < phaserollout_projects.length ; index++)
						{
							if (projectname.compareTo(phaserollout_projects[index]) == 0)
							{
								eligible=true;
								break;
							}
						}
					}
				}

				if (eligible == true)
				{
					itemRevision = (TCComponentItemRevision) comp;
					EnvironmentalReportDialog sr = new EnvironmentalReportDialog(shell,itemRevision);
					sr.open();
				}
				else
				{
					MessageBox.post( "Environmental Report Generation is not enabled for you current login Group or the Project is not part Phase rollout", "Information", MessageBox.INFORMATION);
				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				//MessageBox.post(shell,"Please select a pack component","Error", MessageBox.ERROR);
			}		
		}	
		return null;
	}
}