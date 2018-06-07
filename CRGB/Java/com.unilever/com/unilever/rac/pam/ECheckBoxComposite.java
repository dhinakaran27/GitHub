package com.unilever.rac.pam;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentForm;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.kernel.TCPropertyDescriptor;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.viewer.stylesheet.beans.AbstractPropertyBean;

public class ECheckBoxComposite extends AbstractPropertyBean{
	
	private Composite parentComposite;
	private Composite mainComposite;	
	private FormToolkit parentFormToolKit;
	private TCSession session;
	private boolean isCheckOut = false ;
	private boolean enableControl = false;
	private TCComponentForm form = null ;
	private ArrayList<String> alAllProjects = null ;
	private ArrayList<String> alCutProjects = null ;
	/**
	 * @param toolkit
	 * @param composite
	 * @param paramBoolean
	 * @param PropName
	 */
	public ECheckBoxComposite(FormToolkit toolkit, Composite composite,boolean paramBoolean,Map<String, String> PropName)
	{
		super(composite);
		AbstractAIFUIApplication application = AIFUtility.getCurrentApplication();
		InterfaceAIFComponent[] targets = null;
		targets = application.getTargetComponents();
		this.parentComposite = composite;	
		if (targets[0] instanceof TCComponentForm)
			form = (TCComponentForm) targets[0];
		session = form.getSession();	
		parentComposite.setLayout(new GridLayout(1,true));
		mainComposite = new Composite(parentComposite, SWT.NONE);	
		GridData localGridData1 = new GridData(1808);		
		localGridData1.heightHint = 300;
	    localGridData1.widthHint = 1000;
	    mainComposite.setLayout(new GridLayout(1,false));
	    mainComposite.setLayoutData(localGridData1);
	    alAllProjects =  new ArrayList<String>();
	    alCutProjects =  new ArrayList<String>();
	    
	    try
		{
			String allProjects[] = form.getTCProperty("u4_project_all").getStringArrayValue();
			String cutProjects[] = form.getTCProperty("u4_project_cut").getStringArrayValue();	
			if(allProjects.length > 0)		
				alAllProjects.addAll(Arrays.asList(allProjects));
			if(cutProjects.length > 0)	
				alCutProjects.addAll(Arrays.asList(cutProjects));

		} 
		catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public boolean isPropertyModified(TCProperty arg0) throws Exception {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public Object getEditableValue() {
		// TODO Auto-generated method stub
		return mainComposite;
	}

	@Override
	public TCProperty getPropertyToSave(TCProperty arg0) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void load(TCProperty arg0) throws Exception {
		// TODO Auto-generated method stub
		
		GridData gridData = (GridData)mainComposite.getLayoutData();
		mainComposite.setLayoutData( gridData);
		mainComposite.redraw();
		Label label = new Label(mainComposite, SWT.LEFT);
		label.setText("Note : Pack component will be removed from the following checked design projects\n");
		Button[] button = new  Button[alAllProjects.size()];		
		for ( int inx=0 ; inx< alAllProjects.size() ; inx++ )
		{
			 button[inx] = new Button(mainComposite, SWT.CHECK|SWT.CENTER);
			 button[inx].setText(alAllProjects.get(inx).toString());			 
			 if(alCutProjects.contains(alAllProjects.get(inx).toString()))
				 button[inx].setSelection(true);
			 else
				 button[inx].setSelection(false);
			 
			 button[inx].addSelectionListener(new SelectionAdapter()
			 {
					public void widgetSelected(SelectionEvent event)
					{
						String isTrue= (((Button) (event.getSource())).getSelection()==true)?"True":"False";
						String value = ((Button) (event.getSource())).getText().toString();
						if(isTrue.endsWith(("True")))
							if(!alCutProjects.contains(value))
								alCutProjects.add(value);
						if(isTrue.endsWith(("False")))
							if(alCutProjects.contains(value))
								alCutProjects.remove(value);

					}
			 });
		 }		
		
		mainComposite.setSize(200,100);
		
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
}
