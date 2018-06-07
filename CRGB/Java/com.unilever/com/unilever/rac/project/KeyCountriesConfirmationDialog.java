package com.unilever.rac.project;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.kernel.TCSession;

public class KeyCountriesConfirmationDialog extends Dialog {

	protected Object result;
	protected Shell shlConfirmation;

	private List<String> countries;

	private boolean isOk= false;
	private boolean isCancel = false;

	private String action = null;
	private Text text;

	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public KeyCountriesConfirmationDialog(Shell parent, List<String> countries, String action) {
		super(parent, SWT.SHELL_TRIM|SWT.APPLICATION_MODAL);
		setText("SWT Dialog");
		this.countries = countries;
		this.action = action;
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open() {
		createContents();
		shlConfirmation.open();
		shlConfirmation.layout();
		Display display = getParent().getDisplay();
		TCSession session = (TCSession)AIFDesktop.getActiveDesktop().getCurrentApplication().getSession();
		while (!shlConfirmation.isDisposed()) {
			if (!display.readAndDispatch()) 
			{
				display.sleep();

				if (session!=null)
					session.setStatus("Saving changes to Project 'Key Countries' will take time, Please wait...");
			}
		}
		return result;
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shlConfirmation = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		shlConfirmation.setSize(260, 386);
		shlConfirmation.setText("Confirmation");
		Point pt = getParent().getDisplay().getCursorLocation();

		shlConfirmation.setLocation((pt.x + 50), (pt.y-300));

		Button okButton = new Button(shlConfirmation, SWT.NONE);
		okButton.setBounds(158, 326, 75, 25);
		okButton.setText("OK");

		okButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				isOk = true;
				isCancel = false;
				((Shell)(((Button)(e.getSource())).getParent())).close();
			}
		});

		Button cancelButton = new Button(shlConfirmation, SWT.NONE);
		cancelButton.setBounds(74, 326, 75, 25);
		cancelButton.setText("Cancel");

		cancelButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				isOk = false;
				isCancel = true;
				((Shell)(((Button)(e.getSource())).getParent())).close();
			}
		});

		ScrolledComposite scrolledComposite = new ScrolledComposite(shlConfirmation, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setBounds(10, 10, 234, 274);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);

		text = new Text(scrolledComposite, SWT.BORDER | SWT.V_SCROLL);
		scrolledComposite.setContent(text);
		scrolledComposite.setMinSize(text.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		String text_content = null;

		if (this.action.compareToIgnoreCase("Add")==0)
			text_content= "Verify 'Key Countries' being added:\n";
		else if (this.action.compareToIgnoreCase("Remove")==0)
			text_content= "Verify 'Key Countries' being removed:\n";

		for (int index = 0 ; index < countries.size() ; index++)
		{
			text_content = text_content.concat("\t") ;
			text_content = text_content.concat(countries.get(index)) ;
			text_content = text_content.concat("\n") ;
		}

		text.setText(text_content);

		Label lblNewLabel = new Label(shlConfirmation, SWT.NONE);
		lblNewLabel.setAlignment(SWT.CENTER);
		lblNewLabel.setBounds(0, 290, 240, 30);
		lblNewLabel.setText("Press 'OK' to save changes to Key Countries.\nIt may take a while to save.");
	}

	public boolean isOKPressed()
	{
		return isOk;

	}
	public boolean isCancelPressed()
	{
		return isCancel;
	}
}
