/* Source File Name:   UL4ChangeContextHandler
 *
 * Description:  This file contains code to build Change context Creation UI Dialog 
 * Revision History:
 * -------------------------------------------------------------------------
 * Revision    Author                Date Created           Reason
 * -------------------------------------------------------------------------
 *  1.0.0      Tushar L                                     Initial Creation
 *
 */

package com.unilever.rac.ui.changecontext;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.aifrcp.AifrcpPlugin;
import com.teamcenter.rac.aifrcp.SelectionHelper;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentDatasetType;
import com.teamcenter.rac.kernel.TCComponentForm;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentTcFile;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.services.ILocalSelectionService;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.OSGIUtil;
import com.teamcenter.rac.util.Registry;
import com.unilever.rac.ui.common.UL4Common;
import com.unilever.rac.util.UnileverPreferenceUtil;

/**
 * The Class UL4ChangeContextHandler.
 */

public class UL4ChangeContextHandler extends AbstractHandler implements IHandler {
    private static final String TCComponentItemRevision = null;
	private ArrayList< String > alPropName = null;
    private TCSession session = null;
    private Shell shell = null;
    private Registry reg = null;
    private ArrayList< String > alFormProp = null;
    private NodeList nodes = null;
    private boolean isPrimary = false;
    private String m_commandId = null;
    private String m_objectItemType = null;
    private String m_objectItemRevType = null;
    private String m_createStylesheetName = null;

    public UL4ChangeContextHandler() {
        alPropName = new ArrayList< String >();
        alFormProp = new ArrayList< String >();
        alPropName.clear();
        alFormProp.clear();
        reg = Registry.getRegistry( this );
    }

    public Object execute( ExecutionEvent event ) throws ExecutionException {
        AIFDesktop desktop = AIFUtility.getActiveDesktop();
        shell = desktop.getShell();
        ArrayList< TCComponent > packComponentList = new ArrayList< TCComponent >();
        ArrayList< TCComponent > selectedComponentList = new ArrayList< TCComponent >();

        try {
            ILocalSelectionService localILocalSelectionService = (ILocalSelectionService) OSGIUtil.getService( AifrcpPlugin.getDefault(), ILocalSelectionService.class );
            ISelection localISelection = localILocalSelectionService.getSelection( "com.teamcenter.rac.leftHandNavigator.selection" );
            session = (TCSession) AIFUtility.getSessionManager().getSession( "com.teamcenter.rac.kernel.TCSession" );

            if ( ( localISelection == null ) || ( localISelection.isEmpty() ) )
                localISelection = HandlerUtil.getCurrentSelection( event );

            InterfaceAIFComponent[] arrayOfInterfaceAIFComponent = SelectionHelper.getTargetComponents( localISelection );

            packComponentList = getPackComponentsFromSelectionArray( arrayOfInterfaceAIFComponent );
            selectedComponentList = getTCComponentsFromSelectionArray( arrayOfInterfaceAIFComponent );
            
            if( ! validateComponentState(packComponentList))
            	 return null;
            	

            // Get the command id.
            m_commandId = event.getCommand().getId();
            if ( ( "com.unilever.rac.ui.changecontext.newCCProductionUpdate" ).equals( m_commandId ) ) {
                m_objectItemType = UL4Common.PROD_NOTICE_ITEM_COMP_TYPE;
                m_objectItemRevType = UL4Common.PROD_NOTICE_ITEMREV_COMP_TYPE;
                m_createStylesheetName = UL4Common.PROD_NOTICE_CREATE_STYLESHEET_NAME;
            } else if ( ( "com.unilever.rac.ui.changecontext.newCCPilotUpdate" ).equals( m_commandId ) ) {
                m_objectItemType = UL4Common.PILOT_NOTICE_ITEM_COMP_TYPE;
                m_objectItemRevType = UL4Common.PILOT_NOTICE_ITEMREV_COMP_TYPE;
                m_createStylesheetName = UL4Common.PILOT_NOTICE_CREATE_STYLESHEET_NAME;
            }

            isPrimary = isPackPrimary( packComponentList );
            

            if ( getFormDetails( m_createStylesheetName ) ) {
                UL4ChangeContextDialog dailog = new UL4ChangeContextDialog( shell, session, isPrimary, alPropName, packComponentList, selectedComponentList, nodes, m_objectItemType,
                        m_objectItemRevType );
                dailog.open();
            }
        } catch ( ParserConfigurationException e ) {
            MessageBox.post( shell, e.getMessage(), "Error", MessageBox.ERROR );
        } catch ( SAXException e ) {
            MessageBox.post( shell, e.getMessage(), "Error", MessageBox.ERROR );
        } catch ( IOException e ) {
            MessageBox.post( shell, e.getMessage(), "Error", MessageBox.ERROR );
        } catch ( Exception e ) {
            MessageBox.post( shell, e.getMessage(), "Error", MessageBox.ERROR );
        }

        return null;
    }

    private ArrayList< TCComponent > getTCComponentsFromSelectionArray( InterfaceAIFComponent[] arrayOfInterfaceAIFComponent ) {
        ArrayList< TCComponent > list = new ArrayList< TCComponent >();
        for ( int i = 0; i < arrayOfInterfaceAIFComponent.length; i++ ) {
            InterfaceAIFComponent interfaceAIFComponent = arrayOfInterfaceAIFComponent[ i ];
            if ( interfaceAIFComponent instanceof TCComponent ) {
                TCComponent comp = (TCComponent) interfaceAIFComponent;
                if ( !list.contains( comp ) ) {
                    list.add( comp );
                }
            }
        }
        return list;
    }

    private ArrayList< TCComponent > getPackComponentsFromSelectionArray( InterfaceAIFComponent[] arrayOfInterfaceAIFComponent ) throws TCException {
        ArrayList< TCComponent > list = new ArrayList< TCComponent >();
        for ( int i = 0; i < arrayOfInterfaceAIFComponent.length; i++ ) {
            InterfaceAIFComponent interfaceAIFComponent = arrayOfInterfaceAIFComponent[ i ];
            if ( interfaceAIFComponent instanceof TCComponent ) {
                TCComponent comp = (TCComponent) interfaceAIFComponent;
                if ( comp.getTypeComponent().getParent().getType().equalsIgnoreCase( UL4Common.PACKREVISION ) ) {
                    if ( !list.contains( comp ) ) {
                        list.add( comp );
                    }
                } else if ( comp.getTypeComponent().getParent().getType().equalsIgnoreCase( UL4Common.PAMREVISION ) ) {
                    AIFComponentContext[] compContextArr = comp.getPrimary();
                    for ( AIFComponentContext aifComponentContext : compContextArr ) {
                        InterfaceAIFComponent aifComponent = aifComponentContext.getComponent();
                        if ( aifComponent instanceof TCComponent ) {
                            TCComponent primaryPackComponent = (TCComponent) aifComponent;
                            if ( primaryPackComponent.getTypeComponent().getParent().getType().equalsIgnoreCase( UL4Common.PACKREVISION ) ) {
                                if ( !list.contains( primaryPackComponent ) ) {
                                    list.add( primaryPackComponent );
                                }
                            }
                        }
                    }
                }
            }
        }
        return list;
    }

    /**
     * Determines based on the component class and component commodity of the selected object, CTM approval is required or not.
     * Reads preference 'U4_CTM_Approval_Reqd_By_Commodity'.
     * @param selTCComponentList 
     * @return boolean true or false
     * @throws TCException 
     */
    private boolean isPackPrimary( ArrayList< TCComponent > selTCComponentList ) throws TCException {
        boolean result = false;

        for ( TCComponent tcComponent : selTCComponentList ) {
            TCComponentForm materialClassificationForm = (TCComponentForm) tcComponent.getRelatedComponent( UL4Common.GMCFORMRELATION );
            if ( materialClassificationForm != null ) {
                String compClassVal = (String) materialClassificationForm.getTCProperty( UL4Common.COMP_CLASS ).toString();
                String compCommodityVal = (String) materialClassificationForm.getTCProperty( UL4Common.COMP_COMMODITY ).toString();
                if ( compClassVal != null && compCommodityVal != null ) {
                    String[] prefValues = UnileverPreferenceUtil.getStringPreferenceValues( TCPreferenceService.TC_preference_site, UL4Common.U4_CTM_APPROVAL_REQD_BY_COMMODITY_PREF );

                    if ( prefValues.length != 0 ) {
                        for ( String singlePrefValue : prefValues ) {
                            String[] tokens = singlePrefValue.split( "::" );

                            if ( tokens.length != 0 ) {
                                String compClassPrefVal = tokens[ 0 ];
                                String compCommodityPrefVal = tokens[ 1 ];
                                if ( compClassVal.equalsIgnoreCase( compClassPrefVal ) && compCommodityVal.equalsIgnoreCase( compCommodityPrefVal ) ) {
                                    result = new Boolean( tokens[ 2 ] ).booleanValue();
                                    return result;
                                }
                            }
                        }
                    } else {
                        MessageBox.post( shell, "The Preference (" + UL4Common.U4_CTM_APPROVAL_REQD_BY_COMMODITY_PREF + ") does not have values", "Error", MessageBox.ERROR );
                    }
                }
            } else {
                MessageBox.post( shell, "Material Classification Form not found", "Error", MessageBox.ERROR );
            }
        }

        return result;
    }

    /**
     * 
     * Download Stylesheet which contain Required UI Attribute Details
     * @param stylesheetName 
     * 
     * @return fileName
     */

    private String getDataset( String stylesheetName ) {
        String fileName = null;
        TCComponentDataset dataset = null;

        try {
            TCComponentDatasetType datasetType = (TCComponentDatasetType) session.getTypeComponent( UL4Common.XML_RENDERING_STYLESHEET );
            dataset = datasetType.find( stylesheetName );

            if ( dataset != null ) {
                TCComponentTcFile relatedTcFiles[] = dataset.getTcFiles();

                if ( relatedTcFiles != null && relatedTcFiles.length != 0 ) {
                    File file = ( (TCComponentTcFile) relatedTcFiles[ 0 ] ).getFile( null );
                    fileName = file.getAbsolutePath();
                }
            }
        } catch ( TCException e ) {
            MessageBox.post( shell, reg.getString( "datasetmissing" ), reg.getString( "error" ), MessageBox.ERROR );
        }

        return fileName;

    }

    /**
     *  Validate  UI Attribute availability
     * @param stylesheetName 
     * 
     * @return
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */

    private boolean getFormDetails( String stylesheetName ) throws ParserConfigurationException, SAXException, IOException {
        boolean flag = false;
        String file = null;
        alPropName.clear();

        if ( ( file = getDataset( stylesheetName ) ) != null ) {
            try {
                DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document doc = builder.parse( file );
                Element element = doc.getDocumentElement();
                nodes = element.getElementsByTagName( "*" );

                NodeList names = element.getElementsByTagName( "property" );

                for ( int inx = 0; inx < names.getLength(); inx++ ) {
                    String val = ( (Element) names.item( inx ) ).getAttribute( "name" );

                    if ( !alPropName.contains( val ) )
                        alPropName.add( val );
                }

                if ( alPropName.size() == 0 ) {
                    MessageBox.post( shell, reg.getString( "stylesheetpropmissing" ), "Information", MessageBox.INFORMATION );
                } else {
                    flag = true;
                }
            } catch ( ParserConfigurationException pce ) {
                MessageBox.post( shell, pce.getMessage(), reg.getString( "error" ), MessageBox.ERROR );
            } catch ( SAXException se ) {
                MessageBox.post( shell, se.getMessage(), reg.getString( "error" ), MessageBox.ERROR );
            } catch ( IOException ioe ) {
                MessageBox.post( shell, ioe.getMessage(), reg.getString( "error" ), MessageBox.ERROR );
            }
        } else {
            MessageBox.post( shell, " < " + UL4Common.STYLESHEETNAME + " > " + reg.getString( "datasetmissing" ), "Information", MessageBox.INFORMATION );
        }

        return flag;
    }
    
    private boolean validateComponentState(ArrayList< TCComponent > componentList ) throws TCException
    {
    	 boolean flag = true;    	 
    	 
    	 for ( int inx=0 ; inx < componentList.size() ; inx++ )
    	 {
    		 String revision = componentList.get(inx).toString() ; 
    		 TCComponentItem item =  (TCComponentItem) componentList.get(inx).getRelatedComponent("items_tag");
    		 if( item.getLatestItemRevision()  != componentList.get(inx))
    		 {
 	               MessageBox.post( shell, reg.getString( "invalidpackrevision" ) + "\n\n" + revision  , "Error", MessageBox.ERROR );
 	               return false ;	               
    		 }
     		 TCComponentItemRevision pamrevision = (TCComponentItemRevision) componentList.get(inx).getRelatedComponent( "U4_PAMSpecification" );
     		 if( pamrevision.getItem().getLatestItemRevision() != pamrevision )
    		 {  
    	          MessageBox.post( shell, reg.getString( "invalidpamrevision" ) + "\n\n" + revision  , "Error", MessageBox.ERROR );
    	          return false ;
    		 } 
    	 }    	 
    	 
    	 return flag ;
   
    }

}
