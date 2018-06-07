/**
 * 
 */
package com.unilever.rac.util;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;

/**
 * @author j.madhusudan.inamdar
 *
 */
public class UnileverMessageDialog extends MessageDialog
{

    public UnileverMessageDialog(Shell parentShell, String dialogTitle,
            Image dialogTitleImage, String dialogMessage,
            int dialogImageType, String[] dialogButtonLabels,
            int defaultIndex) 
    {
        super(parentShell, dialogTitle, dialogTitleImage, dialogMessage,
                						dialogImageType, dialogButtonLabels, defaultIndex);
        setBlockOnOpen(false);
    }

    public void reallyClose()
    {
        cancelPressed();
    }

}
