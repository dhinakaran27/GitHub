/* Source File Name:   UL4ChangeContextDialog
 *
 * Description:  This file contains code to build Change Context Creation UI Dialog 
 * Revision History:
 * -------------------------------------------------------------------------
 * Revision    Author            Date Created       Reason
 * -------------------------------------------------------------------------
 *  1.0.0      Tushar L                             Initial Creation
 *
 */

package com.unilever.rac.ui.changecontext;

import java.awt.Frame;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.internal.forms.widgets.FormHeading;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.aifrcp.AppThemeHelper;
import com.teamcenter.rac.commands.newitem.OpenOptionPanel;
import com.teamcenter.rac.commands.paste.PasteCommand;
import com.teamcenter.rac.common.Activator;
import com.teamcenter.rac.common.TCTypeRenderer;
import com.teamcenter.rac.common.create.BOCreateDefinitionFactory;
import com.teamcenter.rac.common.create.IBOCreateDefinition;
import com.teamcenter.rac.kernel.BOCreatePropertyDescriptor;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentChangeItem;
import com.teamcenter.rac.kernel.TCComponentChangeItemRevision;
import com.teamcenter.rac.kernel.TCComponentFolder;
import com.teamcenter.rac.kernel.TCComponentForm;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentItemType;
import com.teamcenter.rac.kernel.TCComponentListOfValues;
import com.teamcenter.rac.kernel.TCComponentType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCPropertyDescriptor;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.SWTUIUtilities;
import com.teamcenter.rac.util.dialog.AbstractSWTDialog;
import com.teamcenter.rac.viewer.stylesheet.viewer.Messages;
import com.teamcenter.schemas.soa._2006_03.exceptions.ServiceException;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core._2008_06.DataManagement;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateIn;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateInput;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateOut;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateResponse;
import com.teamcenter.soa.client.model.ServiceData;
import com.unilever.rac.ui.common.UL4Common;
import com.unilever.rac.util.UnileverPreferenceUtil;
import com.unilever.rac.util.UnileverUtility;

/**
 * The Class UL4ChangeContextDialog.
 */

public class UL4ChangeContextDialog extends AbstractSWTDialog {
    private TCSession session = null;
    private Shell shell = null;
    private ArrayList< TCComponent > listOfPackComponents = null;
    private ArrayList< TCComponent > listOfSelectedComponents = null;
    private Color color_white = null;
    private Color color_gray = null;
    private FormToolkit toolkit = null;
    private TCComponentType tccomponenttype = null;
    private TCComponentType tccomponentrevisiontype = null;
    private Button trueButton = null;
    private Button falseButton = null;
    Map< String, CCombo > propObject = null;
    Map< String, String > propValue = null;
    Map< String, String > realDisplayName = null;
    private ArrayList< String > alMandatory = null;
    private ArrayList< String > alDialogProp = null;
    private NodeList nodes = null;
    private boolean isCompPrimary = false;
    private String m_itemType = null;
    private String m_itemRevType = null;

    public UL4ChangeContextDialog( Shell parentShell, TCSession tcSession, boolean isPrimary, ArrayList< String > propName, ArrayList< TCComponent > packComponentList,
            ArrayList< TCComponent > selectedComponentList, NodeList node, String objectItemType, String objectItemRevType ) {
        super( parentShell );
        session = tcSession;
        shell = parentShell;
        listOfPackComponents = packComponentList;
        listOfSelectedComponents = selectedComponentList;
        color_white = shell.getDisplay().getSystemColor( SWT.COLOR_WHITE );
        color_gray = shell.getDisplay().getSystemColor( SWT.COLOR_GRAY );
        toolkit = AppThemeHelper.getFormToolkit();
        propObject = new HashMap< String, CCombo >();
        propValue = new HashMap< String, String >();
        realDisplayName = new HashMap< String, String >();
        alMandatory = new ArrayList< String >();
        alDialogProp = new ArrayList< String >();
        nodes = node;
        m_itemType = objectItemType;
        m_itemRevType = objectItemRevType;

        this.isCompPrimary = isPrimary;

        getMandatoryProp();

        try {
            tccomponenttype = session.getTypeComponent( m_itemType );
            tccomponentrevisiontype = session.getTypeComponent( m_itemRevType );
        } catch ( TCException e ) {
            e.printStackTrace();
        }
    }

    protected Control createContents( Composite parent ) {
        initializeDialogUnits( parent );
        getShell().setSize( 640, 650 );
        createDialogAndButtonArea( parent );
        return parent;
    }

    protected void createDialogAndButtonArea( Composite parent ) {
        dialogArea = createDialogArea( parent );
        dialogArea.setLayoutData( new GridData( GridData.FILL_BOTH ) );

        separator( parent );

        buttonBar = createButtonBar( parent );
        buttonBar.setBackground( color_white );
        parent.setBackground( color_white );
        applyDialogFont( parent );
    }

    protected void createButtonsForButtonBar( Composite parent ) {
        createButton( parent, IDialogConstants.OK_ID, "Create", true );
        createButton( parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, true );

        if ( alMandatory.size() > 0 )
            super.getButton( IDialogConstants.OK_ID ).setEnabled( false );
    }

    @SuppressWarnings( "restriction" )
    protected Control createDialogArea( Composite parent ) {
        Composite mainComposite = (Composite) super.createDialogArea( parent );

        try {
            mainComposite.setLayout( new FillLayout() );
            mainComposite.setBackground( color_white );
            mainComposite.getShell().setText( tccomponenttype.getDisplayType() );
            // mainComposite.setLayout(new GridLayout(1, true));
            // mainComposite.setLayoutData( new GridData(GridData.FILL_HORIZONTAL) );
            mainComposite.setLayout( SWTUIUtilities.tightGridLayout( 1 ) );
            mainComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, false ) );

            TCComponentItemType itemType = (TCComponentItemType) session.getTypeComponent( UL4Common.PROD_NOTICE_ITEM_COMP_TYPE );
            Image image = TCTypeRenderer.getImage( itemType );

            Composite headerComposite = toolkit.createComposite( mainComposite, SWT.NONE );
            headerComposite.setLayout( SWTUIUtilities.tightGridLayout( 1 ) );
            headerComposite.setLayoutData( new GridData( SWT.FILL, SWT.TOP, true, false ) );

            ScrolledForm scrolledform = toolkit.createScrolledForm( mainComposite );
            scrolledform.setLayout( new GridLayout( 1, true ) );
            scrolledform.setLayoutData( new GridData( GridData.FILL_BOTH ) );
            Form frm = scrolledform.getForm();
            frm.setImage( image );
            frm.setBackground( color_white );
            frm.setText( tccomponenttype.getDisplayType() + " Create Information" );
            frm.setLayout( new GridLayout( 1, true ) );
            // frm.setLayoutData( new GridData(GridData.FILL_BOTH) );
            frm.setLayout( SWTUIUtilities.tightGridLayout( 1 ) );

            // m_headerComposite.setLayout( SWTUIUtilities.tightGridLayout( 1 ) );
            // m_headerComposite.setLayoutData( new GridData( SWT.FILL, SWT.TOP, true, false ) );

            FormHeading formHeadingComposite = (FormHeading) frm.getHead();
            formHeadingComposite.setLayout( SWTUIUtilities.tightGridLayout( 1 ) );
            formHeadingComposite.setLayoutData( new GridData( SWT.FILL, SWT.TOP, true, false ) );
            formHeadingComposite.setBackground( color_white );
            formHeadingComposite.setImage( image );

            Composite frmBodyComposite = frm.getBody();
            frmBodyComposite.setLayout( new GridLayout( 1, true ) );
            frmBodyComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
            frmBodyComposite.setBackground( color_white );

            Composite composite = null;

            for ( int inx = 0; inx < nodes.getLength(); inx++ ) {
                int type = 0;
                Element node = (Element) nodes.item( inx );

                if ( node.getNodeName().toString().equalsIgnoreCase( "property" ) && composite != null ) {
                    String propName = node.getAttribute( "name" );                    
                  
                    
                    if(isCompPrimary)
                    {                    	
                    	if(propName.endsWith(UL4Common.RELEASE_APPROVER_ATTR_PAD))
                    		continue ;
                    }
                    else
                    {
                    	if(propName.endsWith(UL4Common.RELEASE_APPROVER_ATTR_CTM))
                    		continue ;                    	
                    }
                    

                    if ( !alDialogProp.contains( propName ) ) {
                        if ( propName.startsWith( "revision:" ) ) {
                            String tmpPropName = propName.substring( propName.indexOf( ":" ) + 1, propName.length() );
                            type = tccomponentrevisiontype.getPropertyDescriptor( tmpPropName ).getType();

                        } else {
                            // int type = tccomponenttype.getTCProperty(propName).getPropertyType();
                            type = tccomponenttype.getPropertyDescriptor( propName ).getType();
                        }

                        alDialogProp.add( propName );

                        switch ( type ) {
                            case 1: // char

                                stringCharType( composite, propName );

                                break;

                            case 3: // double

                                floatType( composite, propName );

                                break;

                            case 4: // float

                                floatType( composite, propName );

                                break;

                            case 5: // int

                                number( composite, propName );

                                break;

                            case 6: // logical

                                radio( composite, propName );

                                break;

                            case 7: // short

                                number( composite, propName );

                                break;

                            case 8: // string

                                stringType( composite, propName );

                                break;

                            case 12: // note

                                stringType( composite, propName );

                                break;

                            case 15: // long string

                                stringType( composite, propName );

                                break;

                            default: // string

                                stringType( composite, propName );
                        }

                    }
                } else if ( node.getNodeName().toString().equalsIgnoreCase( "section" ) ) {
                    setWidth( composite );
                    String sectionTitle = "Properties";

                    if ( alMandatory.size() > 0 ) {
                        sectionTitle = "Properties (required)";
                    }

                    if ( !( node.getAttribute( "title" ).length() > 0 ) ) {
                        composite = getSectionComposite( frmBodyComposite, sectionTitle );
                    } else {
                        composite = getSectionComposite( frmBodyComposite, node.getAttribute( "title" ) );
                    }

                }
            }

            setWidth( composite );

        } catch ( TCException e ) {
            e.printStackTrace();
        }

        return mainComposite;
    }

    public OpenOptionPanel createOpenOptionPanel( Frame paramFrame, TCSession paramTCSession ) {
        // return new OpenOptionPanel(paramFrame, paramTCSession, this);
        return null;
    }

    private void setWidth( Composite composite ) throws TCException {
        if ( composite != null ) {
            Label lbl = new Label( composite, 0 );
            lbl.setLayoutData( new GridData( SWT.FILL, SWT.FILL, false, false, 1, 1 ) );
            lbl.setText( "" );

            labelEmpty( composite, "                                                                               " );
        }
    }

    private Composite getSectionComposite( Composite main, String title ) {
        Section section = createSection( main, title );

        Composite composite = toolkit.createComposite( section );
        composite.setLayout( tightGridLayout( 2 ) );
        section.setClient( composite );

        return composite;
    }

    private Section createSection( Composite composite, String title ) {
        Section section = toolkit.createSection( composite, 322 );
        section.setText( title );
        GridData griddata = new GridData();
        griddata.horizontalSpan = 1;
        griddata.horizontalAlignment = 4;
        griddata.grabExcessHorizontalSpace = true;
        section.setLayoutData( griddata );
        return section;
    }

    protected void okPressed() {
        try {
            super.okPressed();
            // call SOA service operation for object create
            TCComponent item = createChangeObjectsBySoa( propValue );            
        	
            TCComponent[] compArray = new TCComponent[1];
            compArray [0] = item;
            
            TCComponentFolder newstuffFolder = session.getUser().getNewStuffFolder();
            AIFDesktop desktop = null;
            InterfaceAIFComponent[] destArray = new InterfaceAIFComponent[] { newstuffFolder };
            PasteCommand paste = new PasteCommand( compArray, destArray, desktop );
            paste.setFailBackFlag(true);
            paste.executeModal();
  
            // check preference value for open on create
            boolean prefValue = UnileverPreferenceUtil.getLogicalPreferenceValue( TCPreferenceService.TC_preference_site, UL4Common.U4_OPEN_ON_CREATE_PREF );

            // open the create change item
            if ( prefValue && item != null ) {
                InterfaceAIFComponent[] iaifCompArr = new InterfaceAIFComponent[] { ( (TCComponentItem) item ) };
                String str = "com.teamcenter.rac.ui.perspectives.navigatorPerspective";
                Activator.getDefault().openPerspective( str );
                Activator.getDefault().openComponents( str, iaifCompArr );
            }
        } catch ( TCException e ) {
            MessageBox.post( shell, e.getMessage(), "Error", MessageBox.ERROR );
        } catch ( ServiceException e ) {

            MessageBox.post( shell, e.getMessage(), "Error", MessageBox.ERROR );
        } catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    private TCComponent createChangeObjectsBySoa( Map< String, String > propValue2 ) throws ServiceException, TCException {

        TCComponentItem item = null;
        TCComponentItemRevision itemRev = null;
        TCComponentForm form = null;

        // item properties map
        Map< String, String > itemPropsMap = new HashMap< String, String >();

        // revision properties map
        Map< String, String > itemRevPropsMap = new HashMap< String, String >();

        String object_name_value = getObjNameFromReasonForChangeAttr( propValue2 );
        System.out.println( "object name value:" + object_name_value );

        Iterator< String > itr = propValue2.keySet().iterator();
        while ( itr.hasNext() ) {
            String attr = (String) itr.next();
            String attrVal = propValue2.get( attr ).toString();

            if ( attr.startsWith( "revision" ) ) {
                itemRevPropsMap.put( attr.substring( attr.indexOf( ":" ) + 1, attr.length() ), attrVal );
            } else {
                itemPropsMap.put( attr.substring( attr.indexOf( ":" ) + 1, attr.length() ), attrVal );
            }
        }

        if ( !attrExistInMap( "object_name", itemPropsMap, propValue2 ) ) {
            itemPropsMap.put( "object_name", object_name_value );
        }

        if ( !attrExistInMap( "object_desc", itemPropsMap, propValue2 ) ) {
            itemPropsMap.put( "object_desc", object_name_value );
        }

        // item revision create input
        CreateInput[] itemRevisionCreateInput = new CreateInput[ 1 ];
        itemRevisionCreateInput[ 0 ] = new CreateInput();
        itemRevisionCreateInput[ 0 ].boName = m_itemRevType;
        itemRevisionCreateInput[ 0 ].stringProps = itemRevPropsMap;

        // compound create input for item
        Map< String, CreateInput[] > compoundCreateInputMap = new HashMap< String, CreateInput[] >();
        compoundCreateInputMap.put( "revision", itemRevisionCreateInput );

        // item create input
        CreateInput itemCreateInput = new CreateInput();
        itemCreateInput.boName = m_itemType;
        itemCreateInput.stringProps = itemPropsMap;
        itemCreateInput.compoundCreateInput = compoundCreateInputMap;

        // item create definition
        CreateIn[] itemDef = new CreateIn[ 1 ];
        itemDef[ 0 ] = new CreateIn();
        itemDef[ 0 ].clientId = "Unilever";
        itemDef[ 0 ].data = itemCreateInput;

        // service object
        DataManagement dmService = DataManagementService.getService( session );

        // call createObjects service operation
        CreateResponse createObjResponse = dmService.createObjects( itemDef );

        if ( !ServiceDataError( createObjResponse.serviceData ) ) {
            for ( CreateOut out : createObjResponse.output ) {
                for ( TCComponent obj : out.objects ) {
                    if ( obj instanceof TCComponentChangeItem ) {
                        item = (TCComponentChangeItem) obj;
                    } else if ( obj instanceof TCComponentChangeItemRevision ) {
                        itemRev = (TCComponentChangeItemRevision) obj;
                        itemRev.lock();

                        // attach problem items
                        TCComponent[] problemItemArr = getProblemItemArray( listOfPackComponents );
                        if ( problemItemArr != null ) {
                            itemRev.setRelated( UL4Common.PROBLEM_ITEM_REL_NAME, problemItemArr );
                        }

                        // update the u4_IsPrimary attr value
                        if(isCompPrimary)
                        	itemRev.setLogicalProperty( UL4Common.IS_PRIMARY_ATTR, true );
                        else
                        	itemRev.setLogicalProperty( UL4Common.IS_PRIMARY_ATTR, false );

                        // save the item revision
                        itemRev.save();
                        itemRev.unlock();
                        itemRev.refresh();
                        for ( TCComponent tcComponent : listOfSelectedComponents ) {
                            tcComponent.refresh();
                        }
                        for ( TCComponent tcComponent : listOfPackComponents ) {
                            tcComponent.refresh();
                        }
                    } else if ( obj instanceof TCComponentForm ) {
                        form = (TCComponentForm) obj;
                    }
                }
            }
            return item;
        } else {
            return null;
        }
    }

    private TCComponent[] getProblemItemArray( ArrayList< TCComponent > listSelComps ) throws TCException {
        List< TCComponent > listOfProblemItems = new ArrayList< TCComponent >();
        for ( TCComponent tcComponent : listSelComps ) {
            listOfProblemItems.add( tcComponent );
        }

        if ( m_itemType.equalsIgnoreCase( UL4Common.PROD_NOTICE_ITEM_COMP_TYPE ) ) {
            for ( TCComponent tcComponent : listSelComps ) {
                TCComponent tccomp = tcComponent.getRelatedComponent( "U4_PAMSpecification" );

                if ( tccomp != null && tccomp.getTypeComponent().getParent().getType().equalsIgnoreCase( UL4Common.PAMREVISION ) ) {
                    if ( !listOfProblemItems.contains( tccomp ) ) {
                        listOfProblemItems.add( tccomp );
                    }
                }
            }
        }

        TCComponent[] tccompArr = new TCComponent[ listOfProblemItems.size() ];
        for ( int i = 0; i < listOfProblemItems.size(); i++ ) {
            tccompArr[ i ] = listOfProblemItems.get( i );
        }

        return tccompArr;
    }

    private boolean attrExistInMap( String attr, Map< String, String > propsMap, Map< String, String > propValue ) {

        if ( !propsMap.containsKey( attr ) && propValue.containsKey( attr ) ) {
            return false;
        } else if ( !propsMap.containsKey( attr ) && !propValue.containsKey( attr ) ) {
            return false;
        } else if ( !propsMap.containsKey( attr ) && !propValue.containsKey( attr ) && alMandatory.contains( attr ) ) {
            return false;
        } else if ( !propsMap.containsKey( attr ) && !propValue.containsKey( attr ) && !alMandatory.contains( attr ) ) {
            return true;
        } else {
            return true;
        }

    }

    protected boolean ServiceDataError( final ServiceData data ) {
        if ( data.sizeOfPartialErrors() > 0 ) {
            for ( int i = 0; i < data.sizeOfPartialErrors(); i++ ) {
                for ( String msg : data.getPartialError( i ).getMessages() )
                    MessageBox.post( shell, msg, "Error", MessageBox.ERROR );
            }
            return true;
        }
        return false;
    }

    /**
     * Returns the object name attr value
     * 
     * @param propValue2
     * @return substring for object name attribute
     * @throws TCException 
     */
    private String getObjNameFromReasonForChangeAttr( Map< String, String > propValue2 ) throws TCException {

        String objNameAttrVal = null;
        String reasonForChangeAttrVal = propValue2.get( UL4Common.REASON_FOR_CHANGE_ATTR ).toString();

        TCPropertyDescriptor propDescObjName = getTCPropertyDescriptor( "object_name" );
        int maxLength = propDescObjName.getMaxLength();

        if ( reasonForChangeAttrVal.length() > 128 ) {
            objNameAttrVal = reasonForChangeAttrVal.substring( 0, maxLength );
        } else {
            objNameAttrVal = reasonForChangeAttrVal;
        }

        return objNameAttrVal;
    }

    private void separator( Composite composite ) {
        Label separator1 = new Label( composite, SWT.HORIZONTAL | SWT.SEPARATOR | SWT.SHADOW_OUT );
        separator1.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, false, 1, 1 ) );
    }

    private TCPropertyDescriptor getTCPropertyDescriptor( String prop ) throws TCException {
        if ( prop.startsWith( "revision:" ) ) {
            String tmpPropName = prop.substring( prop.indexOf( ":" ) + 1, prop.length() );
            return tccomponentrevisiontype.getPropertyDescriptor( tmpPropName );
        } else {
            return tccomponenttype.getPropertyDescriptor( prop );
        }
    }

    private String getName( String prop ) throws TCException {
        if ( prop.startsWith( "revision:" ) ) {
            String tmpPropName = prop.substring( prop.indexOf( ":" ) + 1, prop.length() );
            return tccomponentrevisiontype.getPropertyDescriptor( tmpPropName ).getUiName();
        } else {
            return tccomponenttype.getPropertyDescriptor( prop ).getUiName();
        }
    }

    private void label( Composite composite, String name ) throws TCException {
        String propertyName = getName( name );
        String propName = name.substring( name.indexOf( ":" ) + 1, name.length() );

        if ( alMandatory.contains( name ) ) {
            setMandatory( composite, propertyName );
        } else {
            Label lbl = new Label( composite, 0 );
            lbl.setLayoutData( new GridData( SWT.LEFT, SWT.NORMAL, false, false, 1, 1 ) );
            lbl.setText( propertyName );
        }
    }

    private void labelEmpty( Composite composite, String name ) throws TCException {
        String propName = name.substring( name.indexOf( ":" ) + 1, name.length() );
        Label lbl = new Label( composite, 0 );
        lbl.setLayoutData( new GridData( SWT.LEFT, SWT.NORMAL, false, false, 1, 1 ) );
        lbl.setText( propName );
    }

    private void radio( Composite composite, String name ) throws TCException {
        label( composite, name );
        Composite radio = composite;
        radio = toolkit.createComposite( composite, 0 );
        GridLayout gridlayout = new GridLayout();
        gridlayout.numColumns = 2;
        radio.setLayout( gridlayout );
        GridData griddata = new GridData();
        griddata.horizontalSpan = 1;
        radio.setLayoutData( griddata );
        trueButton = toolkit.createButton( radio, Messages.getString( "StylesheetRenderingFormProvider.true" ), 16 );
        trueButton.setData( "StylesheetRenderingFormProvider.true" );
        trueButton.setData( "name", name );
        falseButton = toolkit.createButton( radio, Messages.getString( "StylesheetRenderingFormProvider.false" ), 16 );
        falseButton.setData( "StylesheetRenderingFormProvider.false" );
        falseButton.setData( "name", name );
        falseButton.setSelection( true );
        propValue.put( name, "False" );

        trueButton.addSelectionListener( new SelectionAdapter() {
            public void widgetSelected( SelectionEvent selectionevent ) {
                Button t = (Button) selectionevent.widget;
                propValue.put( (String) t.getData( "name" ), "True" );

                enableCreateButton();
            }
        } );

        falseButton.addSelectionListener( new SelectionAdapter() {
            public void widgetSelected( SelectionEvent selectionevent ) {
                Button t = (Button) selectionevent.widget;
                propValue.put( (String) t.getData( "name" ), "False" );

                enableCreateButton();
            }
        } );
    }

    private void lov( Composite composite, String name ) throws TCException {
        TCComponentType componentType = null;
        CCombo combo = null;
        String propName = name;
        if ( name.startsWith( "revision:" ) ) {
            componentType = tccomponentrevisiontype;
            propName = propName.substring( propName.indexOf( ":" ) + 1, propName.length() );
        } else {
            componentType = tccomponenttype;
        }

        label( composite, name );
        
        TCPropertyDescriptor propDesc = getTCPropertyDescriptor( name );
        String lovName = propDesc.getLovReference().getLovInfo().getName();

        combo = new CCombo( composite, SWT.READ_ONLY | SWT.BORDER );
        combo.setLayoutData( new GridData( SWT.FILL, SWT.FILL, false, true, 1, 1 ) );
        combo.setItems( UnileverUtility.getLOVValueArrayBySoa( componentType, name, lovName ) );
   

        combo.setData( "name", name );

        propObject.put( name, combo );
        propValue.put( name, "" );

        combo.addSelectionListener( new SelectionListener() {

            @Override
            public void widgetSelected( SelectionEvent event ) {

                CCombo cc = (CCombo) event.widget;
                String selectedVal = String.valueOf( cc.getItem( cc.getSelectionIndex() ) );

                if ( selectedVal.length() > 0 ) {
                    String value = cc.getText();

                    if ( propObject.get( UL4Common.TYPE_OF_CHANGE_ATTR ).getText().equalsIgnoreCase( "Developer Only Update" ) ) {
                        CCombo combo = propObject.get( UL4Common.COMPONENT_UPDATER_ATTR );
                        combo.deselectAll();
                        combo.setEnabled( false );
                        combo.setBackground( color_gray );
                    } else {
                        CCombo combo = propObject.get( UL4Common.COMPONENT_UPDATER_ATTR );
                        combo.setEnabled( true );
                        combo.setBackground( color_white );
                    }

                    propValue.put( (String) cc.getData( "name" ), value );

                    enableCreateButton();
                }
            }

            @Override
            public void widgetDefaultSelected( SelectionEvent event ) {

            }
        } );
    }

    private void enableCreateButton() {
        boolean flag = true;

        for ( Map.Entry< String, String > propName : propValue.entrySet() )
            if ( alMandatory.contains( propName.getKey() ) )
                if ( propValue.get( propName.getKey() ).length() == 0 ) {
                    flag = false;
                    break;
                }

        super.getButton( IDialogConstants.OK_ID ).setEnabled( flag );

    }

    private void text( Composite composite, String name, TCPropertyDescriptor propDesc ) throws TCException {
        label( composite, name );
        final Text text = new Text( composite, SWT.NORMAL | SWT.BORDER );
        GridData data = new GridData();
        data.widthHint = 240;
        text.setLayoutData( data );
        text.setData( "name", name );
        setTextLength( text, name, propDesc );
        propValue.put( name, "" );

        if ( name.equalsIgnoreCase( "u4_size" ) )
            text.setTextLimit( 11 );

        text.addModifyListener( new ModifyListener() {
            public void modifyText( ModifyEvent event ) {

                Text t = (Text) event.widget;
                String value = t.getText();
                propValue.put( (String) t.getData( "name" ), value );

                enableCreateButton();
            }
        } );

    }

    private void number( Composite composite, String name ) throws TCException {
        label( composite, name );
        final Text text = new Text( composite, SWT.NORMAL | SWT.BORDER );
        GridData data = new GridData();
        data.widthHint = 240;
        text.setLayoutData( data );
        text.setData( "name", name );
        propValue.put( name, "" );

        text.addListener( SWT.Verify, new Listener() {
            public void handleEvent( Event e ) {
                String string = e.text;
                Text t = (Text) e.widget;
                String value = t.getText();
                char[] chars = new char[ string.length() ];
                string.getChars( 0, chars.length, chars, 0 );
                for ( int i = 0; i < chars.length; i++ ) {
                    if ( !( '0' <= chars[ i ] && chars[ i ] <= '9' ) ) {
                        e.doit = false;
                        return;
                    } else {
                        propValue.put( (String) text.getData( "name" ), value );
                        enableCreateButton();
                    }
                }
            }
        } );
    }

    private void floatType( Composite composite, String name ) throws TCException {
        label( composite, name );
        final Text text = new Text( composite, SWT.NORMAL | SWT.BORDER );
        GridData data = new GridData();
        data.widthHint = 240;
        text.setLayoutData( data );
        text.setData( "name", name );
        propValue.put( name, "" );

        text.addListener( SWT.Verify, new Listener() {
            public void handleEvent( Event e ) {
                String string = e.text;
                Text t = (Text) e.widget;
                String value = t.getText();
                char[] chars = new char[ string.length() ];
                string.getChars( 0, chars.length, chars, 0 );
                for ( int i = 0; i < chars.length; i++ ) {
                    if ( ( chars[ i ] >= '0' && chars[ i ] <= '9' ) || ( StringUtils.countMatches( value, "." ) == 0 && chars[ i ] == '.' ) ) {
                        propValue.put( (String) text.getData( "name" ), value );
                        enableCreateButton();
                    } else {
                        e.doit = false;
                        return;
                    }
                }
            }
        } );
    }

    private void setTextLength( Text text, String prop, TCPropertyDescriptor propDesc ) throws TCException {
        text.setTextLimit( propDesc.getMaxLength() );
    }

    private void multiText( Composite composite, String name, TCPropertyDescriptor propDesc ) throws TCException {
        label( composite, name );

        final Text text = new Text( composite, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.H_SCROLL );
        GridData data = new GridData();
        data.heightHint = 75;
        data.widthHint = 240;
        text.setLayoutData( data );
        text.setData( "name", name );
        setTextLength( text, name, propDesc );
        propValue.put( name, "" );

        text.addModifyListener( new ModifyListener() {
            public void modifyText( ModifyEvent event ) {

                Text t = (Text) event.widget;
                String value = t.getText();
                propValue.put( (String) t.getData( "name" ), value );

                enableCreateButton();
            }
        } );

    }

    public void setMandatory( Composite composite, String PropValue ) throws TCException {
        Display display = AIFUtility.getActiveDesktop().getShell().getDisplay();
        StyledText txt = new StyledText( composite, 0 );
        txt.setLayoutData( new GridData( SWT.LEFT, SWT.NORMAL, false, false, 1, 1 ) );

        Color red = display.getSystemColor( SWT.COLOR_RED );
        StyleRange style = new StyleRange();
        style.start = PropValue.length();
        style.length = 1;
        style.foreground = red;

        txt.setText( PropValue + "*" );
        txt.setStyleRange( style );
        txt.setEditable( false );
        txt.setEnabled( false );
    }

    private void getMandatoryProp() {
        boolean mandatory = false;
        alMandatory.clear();

        IBOCreateDefinition createDefinition = BOCreateDefinitionFactory.getInstance().getCreateDefinition( session, UL4Common.PROD_NOTICE_ITEM_COMP_TYPE );
        List< BOCreatePropertyDescriptor > list = createDefinition.getCreatePropertyDescriptors();
        ArrayList< BOCreatePropertyDescriptor > objProps = new ArrayList< BOCreatePropertyDescriptor >();
        objProps.clear();

        IBOCreateDefinition createDefinition1 = BOCreateDefinitionFactory.getInstance().getCreateDefinition( session, UL4Common.PROD_NOTICE_ITEMREV_COMP_TYPE );
        List< BOCreatePropertyDescriptor > list1 = createDefinition1.getCreatePropertyDescriptors();
        ArrayList< BOCreatePropertyDescriptor > objProps1 = new ArrayList< BOCreatePropertyDescriptor >();
        objProps1.clear();

        if ( list != null && list.size() > 0 ) {
            Iterator< BOCreatePropertyDescriptor > iterator = list.iterator();

            do {
                if ( !iterator.hasNext() )
                    break;

                objProps.add( iterator.next() );
            } while ( true );
        }

        if ( list1 != null && list1.size() > 0 ) {
            Iterator< BOCreatePropertyDescriptor > iterator = list1.iterator();

            do {
                if ( !iterator.hasNext() )
                    break;

                objProps.add( iterator.next() );
            } while ( true );
        }

        for ( int inx = 0; inx < objProps.size(); inx++ ) {
            BOCreatePropertyDescriptor bocreatepropertydescriptor1 = objProps.get( inx );
            TCPropertyDescriptor tcpropertydescriptor1 = bocreatepropertydescriptor1.getPropertyDescriptor();
            mandatory = tcpropertydescriptor1.isRequired();

            if ( mandatory ) {
                String compType = tcpropertydescriptor1.getTypeComponent().toString();
                if ( compType.equalsIgnoreCase( "U4_ProdNoticeRevisionCreI" ) ) {
                    alMandatory.add( "revision:" + tcpropertydescriptor1.getName() );
                    realDisplayName.put( "revision:" + tcpropertydescriptor1.getName(), tcpropertydescriptor1.getDisplayName() );
                } else {
                    alMandatory.add( tcpropertydescriptor1.getName() );
                    realDisplayName.put( tcpropertydescriptor1.getName(), tcpropertydescriptor1.getDisplayName() );
                }

                /*alMandatory.add(tcpropertydescriptor1.getName());
                realDisplayName.put( tcpropertydescriptor1.getName()  , tcpropertydescriptor1.getDisplayName() );
                mandatory = false ;*/
            }
        }
    }

    /*
    private String  getChildLOVName(String lovname , String value)
    {
    	
    	String childlovname = null ;		
    
    	try 
    	{	
    		TCComponentListOfValues lov = TCComponentListOfValuesType.findLOVByName(lovname);	
    		List<String> stringList = new ArrayList<String>(Arrays.asList(lov.getListOfValues().getStringListOfValues())); 					
    		TCComponentListOfValues[] children = lov.getListOfFilters();					
            childlovname = children[stringList.indexOf(value)].toString();							
    	}
    	catch (TCException e1)
    	{
    		e1.printStackTrace();
    	}
    	
    	return childlovname;
    	
    }
    */

    protected boolean isResizable() {
        return true;
    }

    private GridLayout tightGridLayout( int column ) {
        GridLayout localGridLayout = new GridLayout();
        localGridLayout.numColumns = column;
        localGridLayout.marginBottom = 0;
        localGridLayout.marginHeight = 5;
        localGridLayout.marginLeft = 10;
        localGridLayout.marginRight = 10;
        localGridLayout.marginTop = 5;
        localGridLayout.marginWidth = 5;
        localGridLayout.horizontalSpacing = 10;
        localGridLayout.verticalSpacing = 5;

        return localGridLayout;
    }

    private void stringCharType( Composite composite, String prop ) throws TCException {
        label( composite, prop );
        final Text text = new Text( composite, SWT.NORMAL | SWT.BORDER );
        GridData data = new GridData();
        data.widthHint = 50;
        text.setLayoutData( data );
        text.setData( "name", prop );
        text.setTextLimit( 1 );
        ;
        propValue.put( prop, "" );

        text.addModifyListener( new ModifyListener() {
            public void modifyText( ModifyEvent event ) {

                Text t = (Text) event.widget;
                String value = t.getText();
                propValue.put( (String) t.getData( "name" ), value );

                enableCreateButton();
            }
        } );
    }

    private void stringType( Composite composite, String prop ) throws TCException {
        if ( prop.equalsIgnoreCase( "revision:u4_release_approver" ) || prop.equalsIgnoreCase( "revision:u4_release_approver1" )  ) {
            lov( composite, prop );
        } else {
            TCPropertyDescriptor propDesc = getTCPropertyDescriptor( prop );

            if ( propDesc.getLOV() != null && !( propDesc.isArray() ) )
                lov( composite, prop );
            else if ( propDesc.getLOV() != null && ( propDesc.isArray() ) )
                checkboxType( composite, prop );
            else {
                int length = propDesc.getMaxLength();

                if ( length <= 128 )
                    text( composite, prop, propDesc );
                else
                    multiText( composite, prop, propDesc );
            }
        }
    }

    private void checkboxType( Composite composite, final String prop ) throws TCException {

        TCComponentListOfValues lov = getTCPropertyDescriptor( prop ).getLOV();
        String values[] = lov.getListOfValues().getStringListOfValues();

        Label lbl = new Label( composite, 0 );
        lbl.setLayoutData( new GridData( SWT.LEFT, SWT.NORMAL, false, false, 1, values.length ) );
        lbl.setText( getName( prop ) );

        propValue.put( prop, "" );

        for ( int inx = 0; inx < values.length; inx++ ) {
            Button button = new Button( composite, SWT.CHECK );
            button.setText( values[ inx ] );
            button.setLayoutData( new GridData( SWT.FILL, SWT.FILL, false, false, 1, 1 ) );

            SelectionListener selectionListener = new SelectionAdapter() {
                public void widgetSelected( SelectionEvent event ) {
                    Button button = ( (Button) event.widget );
                    String val = button.getText();
                    String oldValue = propValue.get( prop );

                    if ( button.getSelection() ) {
                        if ( !oldValue.contains( val ) )
                            propValue.put( prop, oldValue + val + ";" );
                    } else {
                        if ( oldValue.contains( val ) )
                            oldValue = replace( val + ";", "", oldValue );

                        propValue.put( prop, oldValue );
                    }
                };
            };

            button.addSelectionListener( selectionListener );
        }

    }

    public static String replace( String oldStr, String newStr, String inString ) {
        int start = inString.indexOf( oldStr );
        if ( start == -1 ) {
            return inString;
        }
        StringBuffer sb = new StringBuffer();
        sb.append( inString.substring( 0, start ) );
        sb.append( newStr );
        sb.append( inString.substring( start + oldStr.length() ) );
        return sb.toString();
    }
}