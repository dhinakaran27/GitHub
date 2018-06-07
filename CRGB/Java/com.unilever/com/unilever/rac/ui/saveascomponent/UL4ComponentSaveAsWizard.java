package com.unilever.rac.ui.saveascomponent;

import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aif.kernel.IOperationService;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.commands.genericsaveas.ISaveAsService;
import com.teamcenter.rac.common.DownloadDatasetDialog;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.ui.commands.RACUICommandsActivator;
import com.teamcenter.rac.util.AdapterUtil;
import com.teamcenter.rac.util.Cookie;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.OSGIUtil;
import com.teamcenter.rac.util.SWTUIUtilities;
import com.teamcenter.rac.util.event.ClientEventDispatcher;
import com.teamcenter.rac.util.wizard.extension.BaseExternalWizard;
import com.teamcenter.rac.util.wizard.extension.BaseExternalWizardPage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.swing.SwingUtilities;
import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Shell;
import com.teamcenter.rac.ui.commands.saveas.*;

/**
 * @author j.madhusudan.inamdar
 *
 */
public class UL4ComponentSaveAsWizard 
{
	
}

/**
extends SaveAsWizard 
{
	  private static final Logger logger = Logger.getLogger(SaveAsWizard.class);
	  private boolean m_performingOperation = false;
	  private final List<BaseExternalWizardPage> m_saveAsPages = new ArrayList();
	  public static final String DEFAULT_WIZARD_ID = "com.teamcenter.rac.ui.commands.saveas.SaveAsWizard";

	  public SaveAsWizard(String paramString)
	  {
	    super(paramString);
	  }

	  public void addPage(IWizardPage paramIWizardPage)
	  {
	    if ((paramIWizardPage instanceof BaseExternalWizardPage))
	    {
	      this.m_saveAsPages.add((BaseExternalWizardPage)paramIWizardPage);
	      Collections.sort(this.m_saveAsPages);
	      super.addPage(paramIWizardPage);
	    }
	  }

	  public void addPages()
	  {
	    addCustomPages(new ArrayList());
	  }

	  public IWizardPage getPreviousPage(IWizardPage paramIWizardPage)
	  {
	    if (this.m_saveAsPages != null)
	    {
	      int i = this.m_saveAsPages.indexOf(paramIWizardPage);
	      if ((i == 0) || (i == -1))
	        return null;
	      return (IWizardPage)this.m_saveAsPages.get(i - 1);
	    }
	    return super.getPreviousPage(paramIWizardPage);
	  }

	  public IWizardPage getNextPage(IWizardPage paramIWizardPage)
	  {
	    if (this.m_saveAsPages != null)
	    {
	      int i = this.m_saveAsPages.indexOf(paramIWizardPage);
	      if ((i == this.m_saveAsPages.size() - 1) || (i == -1))
	        return null;
	      return (IWizardPage)this.m_saveAsPages.get(i + 1);
	    }
	    return super.getNextPage(paramIWizardPage);
	  }

	  public boolean performFinish()
	  {
	    this.m_performingOperation = true;
	    getContainer().updateButtons();
	    ISaveAsService localISaveAsService = (ISaveAsService)OSGIUtil.getService(RACUICommandsActivator.getDefault(), ISaveAsService.class);
	    if (localISaveAsService != null)
	      try
	      {
	        PerformFinish localPerformFinish = new PerformFinish(null);
	        IOperationService localIOperationService = (IOperationService)OSGIUtil.getService(RACUICommandsActivator.getDefault(), IOperationService.class);
	        localIOperationService.queueOperation(localPerformFinish);
	      }
	      catch (Exception localException)
	      {
	        Logger.getLogger(SaveAsWizard.class).error(localException.getLocalizedMessage(), localException);
	      }
	    return false;
	  }

	  public boolean performCancel()
	  {
	    if ((getContainer() != null) && (getContainer().getShell() != null))
	    {
	      Rectangle localRectangle = getContainer().getShell().getBounds();
	      saveDisplayParameters(localRectangle);
	    }
	    return super.performCancel();
	  }

	  void removeWizardPages(List<IWizardPage> paramList)
	  {
	    if (!paramList.isEmpty())
	    {
	      IWizardContainer localIWizardContainer = getContainer();
	      IWizardPage localIWizardPage = null;
	      if (((localIWizardContainer instanceof WizardDialog)) && (this.m_saveAsPages != null))
	      {
	        localIWizardPage = localIWizardContainer.getCurrentPage();
	        if (localIWizardPage != null)
	        {
	          while (!isDisplayablePage(localIWizardPage, paramList))
	            localIWizardPage = getPreviousPage(localIWizardPage);
	          this.m_saveAsPages.removeAll(paramList);
	          if ((localIWizardPage == null) && (!this.m_saveAsPages.isEmpty()))
	            localIWizardContainer.showPage((IWizardPage)this.m_saveAsPages.get(0));
	          else if ((localIWizardPage != null) && (!localIWizardPage.equals(localIWizardContainer.getCurrentPage())))
	            localIWizardContainer.showPage(localIWizardPage);
	        }
	      }
	    }
	  }

	  private boolean isDisplayablePage(IWizardPage paramIWizardPage, List<IWizardPage> paramList)
	  {
	    boolean bool = false;
	    if ((paramList != null) && (!paramList.isEmpty()) && (!paramList.contains(paramIWizardPage)))
	      bool = true;
	    return bool;
	  }

	  protected void saveDisplayParameters(Rectangle paramRectangle)
	  {
	    String str1 = "DialogParameters";
	    String str2 = getClass().getName();
	    try
	    {
	      Cookie localCookie = Cookie.getCookie(str1, true);
	      localCookie.setString(str2 + ".x", paramRectangle.x);
	      localCookie.setString(str2 + ".y", paramRectangle.y);
	      localCookie.setString(str2 + ".w", paramRectangle.width);
	      localCookie.setString(str2 + ".h", paramRectangle.height);
	      localCookie.close();
	    }
	    catch (Exception localException)
	    {
	      logger.error(localException.getClass().getName(), localException);
	    }
	  }

	  public boolean canFinish()
	  {
	    return this.m_performingOperation ? false : checkForFinish();
	  }

	  private boolean checkForFinish()
	  {
	    boolean bool = true;
	    Iterator localIterator = this.m_saveAsPages.iterator();
	    while (localIterator.hasNext())
	    {
	      BaseExternalWizardPage localBaseExternalWizardPage = (BaseExternalWizardPage)localIterator.next();
	      if (!localBaseExternalWizardPage.isPageComplete())
	        bool = false;
	    }
	    return bool;
	  }

	  private class PerformFinish extends AbstractAIFOperation
	  {
	    private PerformFinish()
	    {
	    }

	    public void executeOperation()
	      throws Exception
	    {
	      ISaveAsService localISaveAsService = (ISaveAsService)OSGIUtil.getService(RACUICommandsActivator.getDefault(), ISaveAsService.class);
	      StructuredSelection localStructuredSelection = SaveAsWizard.this.getContext();
	      InterfaceAIFComponent localInterfaceAIFComponent = (InterfaceAIFComponent)AdapterUtil.getAdapter(localStructuredSelection, InterfaceAIFComponent.class);
	      try
	      {
	        localISaveAsService.configureRelator(true, null, null);
	        final TCComponent localTCComponent = localISaveAsService.performSaveAsOperation();
	        if (localTCComponent != null)
	        {
	          ClientEventDispatcher.fireEventNow(SaveAsWizard.this.getWizardId(), "com/teamcenter/rac/newObjectCreated", new Object[] { "com/teamcenter/rac/wizard/resultOfObjectCreation", localTCComponent, "com/teamcenter/rac/wizard/sourceobject", localInterfaceAIFComponent });
	          SWTUIUtilities.asyncExec(new Runnable()
	          {
	            public void run()
	            {
	              IWizardContainer localIWizardContainer = SaveAsWizard.this.getContainer();
	              if ((localIWizardContainer instanceof WizardDialog))
	                ((WizardDialog)localIWizardContainer).close();
	              SaveAsWizard.PerformFinish.this.postSaveAsOperation(localTCComponent);
	            }
	          });
	        }
	      }
	      catch (TCException localTCException)
	      {
	        SWTUIUtilities.asyncExec(new Runnable()
	        {
	          public void run()
	          {
	            int i = 0;
	            switch (localTCException.errorSeverities[0])
	            {
	            case 3:
	              i = 3;
	              break;
	            case 1:
	            case 4:
	              i = 1;
	              break;
	            case 2:
	              i = 2;
	              break;
	            default:
	              i = 0;
	            }
	            if ((SaveAsWizard.this.getContainer().getCurrentPage() instanceof WizardPage))
	              ((WizardPage)SaveAsWizard.this.getContainer().getCurrentPage()).setMessage(localTCException.getDetailsMessage(), i);
	            SaveAsWizard.this.m_performingOperation = false;
	            SaveAsWizard.this.getContainer().updateButtons();
	          }
	        });
	      }
	    }

	    private void postSaveAsOperation(InterfaceAIFComponent paramInterfaceAIFComponent)
	    {
	      TCComponentItemRevision localTCComponentItemRevision1 = null;
	      if ((paramInterfaceAIFComponent instanceof TCComponentItem))
	        try
	        {
	          localTCComponentItemRevision1 = ((TCComponentItem)paramInterfaceAIFComponent).getLatestItemRevision();
	        }
	        catch (TCException localTCException)
	        {
	          SaveAsWizard.logger.error(localTCException.getLocalizedMessage(), localTCException);
	        }
	      else if ((paramInterfaceAIFComponent instanceof TCComponentItemRevision))
	        localTCComponentItemRevision1 = (TCComponentItemRevision)paramInterfaceAIFComponent;
	      if (localTCComponentItemRevision1 != null)
	      {
	        final TCComponentItemRevision localTCComponentItemRevision2 = localTCComponentItemRevision1;
	        try
	        {
	          boolean bool = localTCComponentItemRevision1.getTCProperty("is_IRDC").getLogicalValue();
	          if ((bool) && (localTCComponentItemRevision2.isCheckedOut()))
	          {
	            Runnable local3 = new Runnable()
	            {
	              public void run()
	              {
	                DownloadDatasetDialog localDownloadDatasetDialog = new DownloadDatasetDialog(localTCComponentItemRevision2, localTCComponentItemRevision2.isCheckedOut());
	                localDownloadDatasetDialog.setVisible(true);
	              }
	            };
	            SwingUtilities.invokeLater(local3);
	          }
	        }
	        catch (Exception localException)
	        {
	          MessageBox.post(localException);
	        }
	      }
	    }
	  }
	}{

}
**/