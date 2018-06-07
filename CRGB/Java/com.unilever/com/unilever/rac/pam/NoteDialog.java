package com.unilever.rac.pam;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.teamcenter.rac.kernel.TCComponentForm;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;

public class NoteDialog extends Shell {
	private final FormToolkit formToolkit = new FormToolkit(Display.getDefault());
	private Text descriptionText;
	
	private String note;
	
	private String currNote;
	
	private boolean isPressed= false;
	
	private boolean isCleared = false;


	/**
	 * Create the shell.
	 * @param display 
	 */
	public NoteDialog(Display display,String currNoteVal,TCComponentItemRevision pamSpecRevision,boolean lbllit , TCComponentForm noteform) {
		super(display, SWT.SHELL_TRIM|SWT.APPLICATION_MODAL);		
		setText("Note");
		setSize(259, 204);
		
		currNote = currNoteVal;	
		// Modified as part of Defect#929
		descriptionText = new Text(this, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL );
		descriptionText.setBounds(10, 10, 221, 111);
		descriptionText.setText(currNote);
		
		try {
			int limit= noteform.getTCProperty("u4_note").getPropertyDescription().getMaxLength();			
			descriptionText.setTextLimit(limit);
			
		} catch (TCException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		formToolkit.adapt(descriptionText, true, true);
		
		
		Button okButton = new Button(this, SWT.NONE);
		okButton.addSelectionListener(new SelectionAdapter() {
	    	@Override
	    	public void widgetSelected(SelectionEvent e) {
	    		setNote(descriptionText.getText());
	    		isPressed = true;
	    		((Shell)(((Button)(e.getSource())).getParent())).close();
	    	}
	    });
		okButton.setBounds(89, 133, 68, 23);
		formToolkit.adapt(okButton, true, true);
		okButton.setText("OK");
		
		Button cancelButton = new Button(this, SWT.NONE);
		cancelButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				((Shell)(((Button)(e.getSource())).getParent())).close();
			}
		});
		cancelButton.setBounds(163, 133, 68, 23);
		formToolkit.adapt(cancelButton, true, true);
		cancelButton.setText("Cancel");
		cancelButton.addSelectionListener(new SelectionAdapter() {
		@Override
    	public void widgetSelected(SelectionEvent e) {
    		    	}
    });
		
		Button clearButton = new Button(this, SWT.NONE);
		clearButton.setBounds(10, 131, 75, 25);
		formToolkit.adapt(clearButton, true, true);
		clearButton.setText("Clear");	
		clearButton.addSelectionListener(new SelectionAdapter() {
	    	@Override
	    	public void widgetSelected(SelectionEvent e) {
	    		descriptionText.setText("");
	    		isCleared= true;	    		
	    	}
	    });
		/**  Added as part of CR#21 - Fix for enabling the note button ( and disabling note dialog ) during non-checkout  */
		if(!pamSpecRevision.isCheckedOut() || lbllit == true )
		{
			// Modified as part of defect#929
			//descriptionText.setEnabled(false);
			descriptionText.setEditable(false);
			okButton.setEnabled(false);
			clearButton.setEnabled(false);
		}
	}

	public String getNote()
	{
		return note;
	}
	
	public void setNote(String currNote){
		note = currNote;
	}
	
	public boolean isOKPressed()
	{
		return isPressed;
		
	}
	public boolean isClearPressed()
	{
		return isCleared;
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
}
