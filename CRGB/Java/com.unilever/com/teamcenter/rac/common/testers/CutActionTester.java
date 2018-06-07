package com.teamcenter.rac.common.testers;

import java.util.ArrayList;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.swt.widgets.Shell;

import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;

public class CutActionTester extends PropertyTester
{
    private TCSession session                 = null ;
    private Shell shell                       = null ;
    private TCComponent selection             = null ; 

	@Override
	public boolean test(Object paramObject1, String paramString, Object[] paramArrayOfObject, Object paramObject2)
    {
		boolean hide = false ;
		TCComponent selection = null ;
	    ArrayList<String> hideObjType	=  new ArrayList<String>(); 
	    ArrayList<TCComponent> pamlist	=  new ArrayList<TCComponent>();
	    
		try 
    	{						
       		session = (TCSession) AIFUtility.getSessionManager().getSession( "com.teamcenter.rac.kernel.TCSession" );
  			if(session.getGroup().toString().equalsIgnoreCase("dba"))
				return  false ;  			
            AbstractAIFUIApplication application    = AIFUtility.getActiveDesktop().getCurrentApplication(); 
            final InterfaceAIFComponent targets[]   = application.getTargetComponents();  			
			@SuppressWarnings("deprecation")
			String[] prefValue = session.getPreferenceService().getStringArray(TCPreferenceService.TC_preference_site, "U4_CutDisallowedObjectTypes");
			for ( int inx=0 ; inx<prefValue.length;inx++)
				hideObjType.add(prefValue[inx]);
			for (int inx=0 ; inx < targets.length ; inx++ )
			{
				if( targets[inx] instanceof TCComponentItemRevision  ||  targets[inx] instanceof TCComponentDataset || targets[inx] instanceof TCComponentItem )
				{
					selection = (TCComponent) targets[inx] ;
					String []types = selection.getTypeNameHierarchy();
					if(types.length >= 2)
					{
						if(hideObjType.contains(types[0]) || hideObjType.contains(types[1]))						
							hide = true ;
						
						if(types[0].equalsIgnoreCase("U4_PAMRevision") || types[1].equalsIgnoreCase("U4_PAMRevision"))
							pamlist.add(selection);					
					}
				}				
			}
			
			if(pamlist.size()>0)
			{	
				int asspamcount = 0 ;
				ArrayList<String> alAssociated = new ArrayList<String>();
				prefValue = session.getPreferenceService().getStringArray(TCPreferenceService.TC_preference_site, "U4_AssociatedSpecRelation");
				for ( int inx=0 ; inx<prefValue.length;inx++)
					alAssociated.add(prefValue[inx]);				
	            AIFComponentContext[] target2 = application.getTargetContexts();
	            for ( int inx=0 ; inx < target2.length ; inx++)
	            	if( pamlist.contains(target2[inx].getComponent()))
	            		if( alAssociated.contains(target2[inx].getContext()))
	            				asspamcount++;
				if(pamlist.size() == asspamcount)hide = false ;
			}
    	}
    	catch (Exception e)
    	{
    		MessageBox.post(shell ,e.getMessage(),"Error",MessageBox.ERROR);	
		}
			
		return hide ;	
		
	}
}
