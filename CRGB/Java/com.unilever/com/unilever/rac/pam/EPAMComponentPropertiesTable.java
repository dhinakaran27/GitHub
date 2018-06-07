package com.unilever.rac.pam;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.commands.paste.PasteOperation;
import com.teamcenter.rac.kernel.ListOfValuesInfo;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentForm;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentListOfValues;
import com.teamcenter.rac.kernel.TCComponentListOfValuesType;
import com.teamcenter.rac.kernel.TCComponentQuery;
import com.teamcenter.rac.kernel.TCComponentQueryType;
import com.teamcenter.rac.kernel.TCComponentType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCPreferenceService.TCPreferenceLocation;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.kernel.TCPropertyDescriptor;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.viewer.stylesheet.beans.AbstractPropertyBean;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core.LOVService;
import com.teamcenter.services.rac.core._2007_01.DataManagement.FormInfo;
import com.teamcenter.services.rac.core._2007_01.DataManagement.VecStruct;
import com.teamcenter.services.rac.core._2013_05.LOV.InitialLovData;
import com.teamcenter.services.rac.core._2013_05.LOV.LOVSearchResults;
import com.teamcenter.services.rac.core._2013_05.LOV.LOVValueRow;
import com.u4.services.rac.service.QueryService;
import com.u4.services.rac.service._2014_12.Query.PMLQueryResult;
import com.unilever.rac.ui.common.UL4Common;
import com.unilever.rac.ui.environmentalreport.ComponentData;
import com.unilever.rac.util.UnileverComponentFactorySingleton;
import com.unilever.rac.util.UnileverQueryUtil;
import com.unilever.rac.util.UnileverUtility;

/**
 * @author swetha.manchiraju
 *
 */
public class EPAMComponentPropertiesTable extends AbstractPropertyBean {
	
	private Composite tableComposite = null;

	/**
	 *
	 */
	private Composite parentComposite = null;

	/**
	 *
	 */
	private TCComponentItemRevision pamSpecRevision = null;

	/**
	 *
	 */
	private String packcomponentType = null;

	/**
	 *
	 */
	private String propname = null;

	/**
	 *
	 */
	private Vector<Map<String,Object>> modifiedPropVals_v = null;
	private Vector<Map<String,Object>> modifiedLayerPropVals_v = null;

	/**
	 *
	 */
	private String renderedPropName =null;

	
	private TCSession session;
	public TCPreferenceService prefServ = null;
	LOVService lovServ = null;
	private Shell shell                    = null;
	private Label conditionDescriptionText = null;
	private Vector<Vector <PAMSecondaryPropValue>> PAMPropertyNameValue_v;

	private String noteValue = "";
	private String methodValue = "";
	private String conditionValue = "";
	private String strConditionNumber = "";

	private HashMap<String, String> condtionSet;
	private int cpTableHeight= 100;
	private int cpTableWidth = 700;
	private FormToolkit parentFormToolKit;
	private String pamSecType;
	private String frame;
	private boolean labelLit = false ;
	private boolean carton = false ;
	private boolean isCheckOut = false ;
	boolean enable_editor = false;
	private boolean matlDetails2 = false ;
	private boolean matlDetails2loaded = false ;
	private boolean cartonloaded = false ;
    private Hashtable<String, Hashtable> tableObjects ;
    private Hashtable<String, ArrayList<String>> prefPolymers ;
    private String nonPMLMaterial[]  = null ;
    private ArrayList<String> PMLLIst = null ;
    private Vector<CCombo> BoardCombos = null ;
    private Map<String,Object> pamlayerprop = null ;
    private Map<String , Object> layetControls = null ;
    private Hashtable<String, Object> vecLayerControl = null;
    private Hashtable<String, Vector<String>> attrTMvalues = null;
    private Hashtable<String, TMDetails> TMData = null;
	private static StringBuffer sbErrorMessage = null ;
	private Map<String,String> PML2PAMCPMap  = null ;
	private Map<String,String>  PML2PAMCPAttrMap  = null ;
	private int textLimit = 0 ;
	private Map<String, TCComponentForm> pamforms = null ;
	private HashMap<String, String> CartonPNBTypeMap = null;
	private static String typecarbon = null ;
	private static String section              = "" ;
	private static String APFORM    	       = "U4_ApprvdPolymerDetailForm" ;
	private static String AP1FORM        	   = "U4_ApprvdPolymerDetailLForm" ;
	private static String APMBFORM			   = "U4_ApprvdMBDetailForm";	
	private static String ADTLFORM             = "U4_ApprvdAdditiveDtlForm";
	private static String PRTMTLDTLFORM        = "U4_PartsMaterialDetailForm";
	private static String PACKLAYERFORM        = "U4_PackLayerStructureForm" ;
	private static String LAYERFORM        	   = "U4_LayerStructureForm" ;
	private static String MATL2FORM      	   = "U4_MaterialDetails2Form";
	private static String MATLFORM      	   = "U4_MaterialDetailsForm";
	private static String JUSTIFICATIONFORM    = "U4_NonPMLReasonForm";
	private static String CPFORM               = "U4_ComponentPropertyForm";
	private static String MATLPROPFORM         = "U4_MaterialPropertiesForm";
	private static String PMLTYPE              = "u4_pml_type" ;
	private static String TYPE                 = "u4_type" ;
	private static String MANUF                = "u4_manufacturer" ;
	private static String GRADEREF             = "u4_grade_reference" ;
	private static String GRADE                = "u4_grade" ;
	private static String TARGET               = "u4_target" ;
	private static String MATL                 = "u4_material" ;
	private static String ATTR                 = "u4_attribute";
	private static String PMLLAYERREL          = "U4_PMLLayerStrctRelation";
	private static String CPFORMS              = "U4_CompPropertyRelation";
	private static String MATFORMS             = "U4_MaterialsRelation";
	private static String PMLLAYERFORM         = "U4_PMLLayerStrctForm";
	private static String PMLCOMPFORM          = "U4_PMLCompPropertyForm";
	private static String PNBLAYERFORM         = "U4_PNBLayerStrctForm";
	private static String PMLCOMPREL           = "U4_PMLCompPropertyRelation";
	private static String LAYER                = "u4_layer";
	private static String PROP                 = "u4_property";
	private static String MIN                  = "u4_min" ;
	private static String NUMBER               = "u4_no" ;
	private static String MAX                  = "u4_max" ;
	private static String UOM                  = "u4_uom" ;
	private static String COLOR                = "u4_colour";
	private static String PMLCOLOR             = "u4_color";
	private static String COMMENT              = "u4_comment";
	private static String SEQNO                = "u4_sequence_no";
	private static String THICKNESS            = "Thickness" ;
	private static String GRAMMAGE             = "Grammage" ;
	private static String TYPECARTON           = "Type-Carton" ;
	private static String BOARDGRADE           = "Board Grade";
	private static String BGATTR               = "Appr. Board 1" ;
	private static String TNAME                = "u4_trade_name";
	private static String TNVALUE              = "u4_value";
	private static String GVALUE               = "u4_grammage";
	private static String MANULOV              = "U4_PL_ManfPolymerMBLOV";
	private static String POLYMERTYPE_LOV	   =  "U4_PL_PolymerTypeLOV";
	private static String COATINGCARTON        =  "U4_PL_CoatingCartonLOV";
	private static String TYPEPMLTYPE          = "U4_ApprovedPolymersPMLTYPEMap";
	private static String LABEL_LIT            = "U4_GLabelRevision";
	private static String CARTON               = "U4_GCartonsRevision";
	private static String PREF_PML2PAMCPMAP    = "U4_PML2PAMComponentPropMap";
	private static String PREF_PNB2PAMCPMAP    = "U4_PNB2PAMComponentPropMap";
	private static String PREF_PML2PAMCPATTRMAP= "U4_PML2PAMComponentPropAttrMap";
	
	/**
	 * @param toolkit
	 * @param composite
	 * @param paramBoolean
	 * @param PropName
	 */
	public EPAMComponentPropertiesTable(FormToolkit toolkit, Composite composite,boolean paramBoolean,Map<String, String> PropName)
	{
		super(composite);

		AbstractAIFUIApplication application = AIFUtility.getCurrentApplication();
		InterfaceAIFComponent[] targets = null;
		targets = application.getTargetComponents();
		if (targets[0] instanceof TCComponentItemRevision)
			pamSpecRevision = (TCComponentItemRevision) targets[0];

		this.parentComposite = composite;
		this.parentFormToolKit = toolkit;
		parentComposite.setLayout(new GridLayout(1,true));
		parentComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		this.renderedPropName = (String) PropName.get("name");
		this.modifiedPropVals_v = new Vector<Map<String,Object>>();
		this.modifiedLayerPropVals_v = new Vector<Map<String,Object>>();
		this.PAMPropertyNameValue_v = new Vector<Vector<PAMSecondaryPropValue>>();
		this.modifiedPropVals_v = new Vector<Map<String,Object>>();
		this.BoardCombos = new Vector<CCombo>();
		vecLayerControl =  new Hashtable<String, Object>();
		attrTMvalues =  new Hashtable<String, Vector<String> >();
		session = pamSpecRevision.getSession();
		prefServ = session.getPreferenceService();
		lovServ = LOVService.getService( session );
		pamSecType = pamSpecRevision.getType();
		String prefCPMap = PREF_PML2PAMCPMAP;
		tableObjects = new Hashtable<String, Hashtable>();
		prefPolymers = new Hashtable<String, ArrayList<String>>() ;
		TMData = new Hashtable<String, TMDetails> ();
		typecarbon = "" ;

		isCheckOut = pamSpecRevision.isCheckedOut();

		if(isCheckOut)
		{
			TCComponent checkedOutUser = null;

			try {
				checkedOutUser = pamSpecRevision.getReferenceProperty("checked_out_user");
			}catch (TCException e) {
				e.printStackTrace();
			}

			if(session.getUser().equals(checkedOutUser))
				enable_editor=true;
			else
				enable_editor=false;
		}
		else
		{
			enable_editor=false;
		}

		if(LABEL_LIT.equalsIgnoreCase(pamSecType) || CARTON.equalsIgnoreCase(pamSecType))
		{
			labelLit = true ;

			if(CARTON.equalsIgnoreCase(pamSecType))
			{
				carton = true ;
				prefCPMap = PREF_PNB2PAMCPMAP;
				queryPML("__PML_Layer_GCarton",MATLPROPFORM);
				pamforms =  new HashMap<String, TCComponentForm>() ;
				CartonPNBTypeMap = new  HashMap <String,String>();
				String[] prefValue = prefServ.getStringArray(TCPreferenceService.TC_preference_site, "U4_GCARTONType2PNBMatlTypeMap");
				for ( int i=0 ;i<prefValue.length;i++)
				{
					String []str = prefValue[i].trim().split("#") ;
					if(str.length == 2 )
						CartonPNBTypeMap.put(str[0], str[1]);
				}

				try
				{
					AIFComponentContext[] forms = pamSpecRevision.getRelated(CPFORMS);
					for ( int ina= 0 ;ina< forms.length ;ina++)
					{
						TCComponentForm frm = (TCComponentForm)forms[ina].getComponent();
						String name = frm.getStringProperty("object_name");
						pamforms.put(name, frm);
					}
				}
				catch (TCException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			if( !  verifyMaterialDetails2Loaded() )
			{
				CTabFolder tab1 = getTab(parentComposite) ;
				CTabItem[] tabitems = tab1.getItems();
				TCComponentItemRevision activepml = null ;

				for(int tab = 0 ;tab < tabitems.length ;tab++)
					if(tabitems[tab].getText().startsWith("Material"))
						activepml = (TCComponentItemRevision) tabitems[tab].getData("activepml");

				if(activepml != null)
					matlDetails2 =  matlDetails2loaded = true ;
			}
		}

		PMLLIst = new ArrayList<String>();
		if(session != null)
			UnileverUtility.getPerformanceMonitorPrefValue( session);
		AIFComponentContext[] comp = null ;

		try
		{
			TCComponentType tccompttype = session.getTypeService().getTypeComponent (renderedPropName);
			section = tccompttype.getDisplayTypeName().replace("Relation", "");
			comp = pamSpecRevision.getPrimary();

			if  ( comp.length== 1)
			{
				if( comp[0].getComponent() instanceof TCComponentItemRevision)
					if( ! pamSecType.equals(comp[0].getComponent().getType()))
						packcomponentType = comp[0].getComponent().getType();
			}
			else
				System.out.println("***ERROR: The PAM is not having a single parent pack component");
		}
		catch (TCException e)
		{
			section  = renderedPropName;
		}

		if(  ( section.startsWith("Material") || section.startsWith("Component Prop" )) && labelLit == true)
		{
			PML2PAMCPMap = new  HashMap <String,String>();
			String[] prefValue = prefServ.getStringArray(TCPreferenceService.TC_preference_site, prefCPMap);
			for ( int i=0 ;i<prefValue.length;i++)
			{
				String []str = prefValue[i].trim().split("#") ;
				if(str.length == 2 )
					PML2PAMCPMap.put(str[0], str[1]);
			}
			PML2PAMCPAttrMap  = new  HashMap <String,String>();
			prefValue = prefServ.getStringArray(TCPreferenceService.TC_preference_site, PREF_PML2PAMCPATTRMAP);
			for ( int i=0 ;i<prefValue.length;i++)
			{
				String []str = prefValue[i].trim().split("#") ;
				if(str.length == 2 )
					PML2PAMCPAttrMap.put(str[0], str[1]);
			}
		}

		if(labelLit == true && section.startsWith("Component Prop") )
		{
			final CTabFolder tab1 = getTab(composite) ;
			tab1.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(org.eclipse.swt.events.SelectionEvent event) {
					if(tab1.getSelection().getText().startsWith("Component Prop"))
					{
						CTabItem[] tabitems = tab1.getItems();
						TCComponentItemRevision pml = null ;
						CTabItem cptab = null ;
						boolean matlSectionLoaded = false ;
						for(int tab = 0 ;tab < tabitems.length ;tab++)
						{
							if(tabitems[tab].getText().startsWith("Material"))
							{
								pml = (TCComponentItemRevision) tabitems[tab].getData("pml");
								matlSectionLoaded = true ;
							}
							else if(tabitems[tab].getText().startsWith("Component Prop"))
							{
								cptab = tabitems[tab];
							}
						}		
						if(pml == null && matlSectionLoaded == false )
						{
							String entries[] = {"ID"};
							String values[] = {"*"};
							String objName = null;
							String objNameMatl2 = "";

							try
							{
								AIFComponentContext[] forms = pamSpecRevision.getRelated(MATFORMS);
								if(LABEL_LIT.equalsIgnoreCase(pamSecType))
								{
									for ( int ina= 0 ;ina< forms.length ;ina++)
									{
										TCComponentForm frm = (TCComponentForm)forms[ina].getComponent();

										if(frm.getType().equals(MATL2FORM))
											objNameMatl2 = frm.getStringProperty("u4_type") ;
									}

									if(objNameMatl2.length() > 0)
									{
										TCComponent[]  components = UnileverComponentFactorySingleton.getInstance().executeQueryOptimised("__PML_MaterialDetail2", entries, values);
										if(components != null)
										{
											for( int inz=0 ;inz < components.length ;inz++)
											{
												objName = ((TCComponentItemRevision)components[inz]).getStringProperty("object_name");

												if(objNameMatl2.equals(objName))
													pml = (TCComponentItemRevision)components[inz] ;
											}
										}
									}
								}
								else if(CARTON.equalsIgnoreCase(pamSecType))
								{
									for ( int ina= 0 ;ina< forms.length ;ina++)
									{
										TCComponentForm frm = (TCComponentForm)forms[ina].getComponent();
										if(frm.getType().equals(MATLPROPFORM) && frm.getStringProperty(PROP).equals(BOARDGRADE) && frm.getStringProperty(ATTR).equals(BGATTR))
										{
											TCComponent[]  components = UnileverComponentFactorySingleton.getInstance().executeQueryOptimised("__PML_Layer_GCarton", entries, values);
											if(components != null)
											{
												for( int inz=0 ;inz < components.length ;inz++)
												{
													objName = ((TCComponentItemRevision)components[inz]).getStringProperty("object_name");
													if(frm.getStringProperty(TYPE).equals(objName))
														pml = (TCComponentItemRevision)components[inz] ;
												}
											}
										}
									}
								}
							}
							catch (TCException e)
							{
								e.printStackTrace();
							}
						}

						if(cptab != null)
						{
							Map<String, TCComponentForm> pamforms = getRelatedForms(pamSpecRevision,CPFORMS);

							for(Map.Entry<String, String> propName : PML2PAMCPMap.entrySet())
							{
								Map<String,Object> controls = (Map<String, Object>) cptab.getData(propName.getValue().toString());
								for(Map.Entry<String, String> attrName : PML2PAMCPAttrMap.entrySet())
								{
									Object object = controls.get(attrName.getKey());
									TCComponentForm compForm = pamforms.get(propName.getValue());
									Map<String,Object > map = (Map<String, Object>) controls.get("map");

									try
									{//u4_condition_number u4_method u4_condition_number
										if(attrName.getKey().equals("u4_details"))
										{
											((Label)object).setText(". . .");
											if(compForm.getTCProperty(attrName.getKey()).getUIFValue().length() > 0)
												((Label)object).setText(". . .");
											else
												((Label)object).setText("");

											if(pml != null)
												((Label)object).setEnabled(false);
											else
												((Label)object).setEnabled(true);

											map.remove("u4_details");

										}
										else if(attrName.getKey().equals("u4_note"))
										{
											if(compForm.getTCProperty(attrName.getKey()).getUIFValue().length() > 0)
												((Button)object).setText("...");
											else
												((Button)object).setText("");

											((Button)object).setEnabled(true);
											map.remove("u4_note");
										}
										else if(attrName.getKey().equals("u4_method"))
										{
											((Button)object).setText(compForm.getTCProperty(attrName.getKey()).getUIFValue());
											((Button)object).setEnabled(true);
											map.remove("u4_method");
											map.remove("u4_details");
										}
										else if(attrName.getKey().equals("u4_details"))
										{
											map.remove("u4_method");
											map.remove("u4_details");
										}
										else if(attrName.getKey().equals("u4_cr") || attrName.getKey().equals("u4_coa"))
										{
											if(compForm.getTCProperty(attrName.getKey()).getUIFValue().equalsIgnoreCase("True"))
												((Button)object).setSelection(true);
											else
												((Button)object).setSelection(false);

											if(pml != null)
												((Button)object).setEnabled(false);
											else
												((Button)object).setEnabled(true);

											map.remove("u4_cr");
											map.remove("u4_coa");
										}
										else if(object.toString().equals("Text {}"))
										{
											((Text)object).setText(compForm.getTCProperty(attrName.getKey()).getUIFValue());

											if(pml != null)
												((Text)object).setEnabled(false);
											else
												((Text)object).setEnabled(true);

											map.remove(attrName.getKey());
										}
									}
									catch ( TCException e )
									{
										e.printStackTrace();
									}
								}
							}
						}
					}
				}
			});
		}
	}

	private boolean  verifyMaterialDetails2Loaded()
	{

		boolean loaded = false ;
		Hashtable<String, TCComponent> pnbtypes = null ;
		
		if( LABEL_LIT.equalsIgnoreCase(pamSecType) )
			pnbtypes =  tableObjects.get(MATLFORM);
		else if (CARTON.equalsIgnoreCase(pamSecType))
			pnbtypes =  tableObjects.get(MATLPROPFORM);
						
		try
		{
			AIFComponentContext[] forms = pamSpecRevision.getRelated(MATFORMS);

			for ( int ina= 0 ;ina< forms.length ;ina++)
			{
				TCComponentForm frm = (TCComponentForm)forms[ina].getComponent();

				if(frm.getType().equals(MATL2FORM))
				{
					if(frm.getStringProperty("u4_type").length() > 0 )
					{
						loaded = matlDetails2 = matlDetails2loaded = true ;
						break ;
					}
				}
				else if(frm.getType().equals(MATLPROPFORM) && frm.getStringProperty(PROP).equals(BOARDGRADE) && frm.getStringProperty(ATTR).equals(BGATTR))
				{
					if(  pnbtypes!= null && pnbtypes.containsKey(frm.getStringProperty(TYPE).toString()))
					{
							loaded = matlDetails2 = matlDetails2loaded = true ;
							break ;
					}
				}
			}
		}
		catch(TCException e)
		{
			e.printStackTrace();
		}

		return loaded ;
	}

	private Map<String, TCComponentForm>  getRelatedForms(TCComponentItemRevision rev , String relation)
	{
		Map<String, TCComponentForm> map = new HashMap<String, TCComponentForm>();

		try
		{
			AIFComponentContext[] forms = rev.getRelated(relation);

			for ( int ina= 0 ;ina< forms.length ;ina++)
			{
				TCComponentForm frm = (TCComponentForm)forms[ina].getComponent();
				String name = frm.getStringProperty("object_name");
				map.put(name, frm);
			}
		}
		catch(TCException e)
		{
			e.printStackTrace();
		}

		return map;
	}

	private CTabFolder getTab(Composite composite)
	{
		Composite obj = composite.getParent() ;

		do
		{
			if(obj.toString().startsWith("CTabFolder"))
			{
				return (CTabFolder) obj ;
			}
			else
			{
				obj = obj.getParent();
			}

		}while (obj != null );


		return null;
	}
		
	public void loadConfigurations()
	{
		try {
			LoadPAMConfigurations loadConfig = new LoadPAMConfigurations();

			Vector<PAMTableConfiguration> tableConfigs = new Vector<PAMTableConfiguration>();

			tableConfigs = loadConfig.readPreferenceValues1(renderedPropName+PAMConstant.CONFIGURATIONS, session);

			ArrayList <String> ignorePrimaryTypeWhenAll = new ArrayList <String>();
			ArrayList <String> ignorePrimaryTypeWhenOnlyPri = new ArrayList <String>();

			for ( int inx = 0 ;inx < tableConfigs.size() ;inx ++ )
				if(tableConfigs.get(inx).primaryType.contains(pamSecType))
					ignorePrimaryTypeWhenAll.add(tableConfigs.get(inx).secondaryType );

			for ( int inx = 0 ;inx < tableConfigs.size() ;inx ++ )
				if(tableConfigs.get(inx).primaryType.contains(pamSecType) && packcomponentType != null &&  tableConfigs.get(inx).primaryType.contains(packcomponentType))
					ignorePrimaryTypeWhenOnlyPri.add(tableConfigs.get(inx).secondaryType );

			PAMPropertyNameValue_v.clear();

			TCComponent[] renderedComponents = pamSpecRevision.getRelatedComponents(renderedPropName);

			TCComponent[] structuredComponents = pamSpecRevision.getRelatedComponents("U4_StructuredPropRelation");

			List<TCComponent> allSecondaryComponents = new ArrayList<TCComponent>(renderedComponents.length + structuredComponents.length);
			Collections.addAll(allSecondaryComponents, renderedComponents);
			Collections.addAll(allSecondaryComponents, structuredComponents);

			Map<String, List<TCComponent>> tname_tccomponent = new HashMap<String, List<TCComponent>>();

			for(int inx=0;inx<allSecondaryComponents.size();inx++)
			{
				String formtype = allSecondaryComponents.get(inx).getType();

				if (tname_tccomponent.containsKey(formtype))
				{
					List<TCComponent> lst = (List<TCComponent>)tname_tccomponent.get(formtype);
					lst.add(allSecondaryComponents.get(inx));
					tname_tccomponent.put(formtype, lst);
				}
				else
				{
					List<TCComponent> tccomp_list = new ArrayList<TCComponent>();
					tccomp_list.add(allSecondaryComponents.get(inx));
					tname_tccomponent.put(formtype, tccomp_list);
				}
			}

			if(isCheckOut == true)
			{
				ArrayList<TCComponent> formarray = new ArrayList<TCComponent>();

				// Iterating to collect the forms to perform a single refreshObjects call
				for(int inx=0;inx<tableConfigs.size();inx++)
				{
					if (packcomponentType == null || pamSecType == null || tableConfigs.get(inx).primaryType ==null)
						continue;

					if (tableConfigs.get(inx).packcomponentType != null)
					{
						if (((tableConfigs.get(inx).primaryType).contains(pamSecType) == true ) && ((tableConfigs.get(inx).packcomponentType).equals(packcomponentType) ==true ))
						{
							List<TCComponent> lst = (List<TCComponent>)tname_tccomponent.get(tableConfigs.get(inx).secondaryType);
							if(lst != null)
							formarray.addAll(lst);
							continue;
						}
					}

					if	(  	((tableConfigs.get(inx).primaryType).contains(pamSecType) && (tableConfigs.get(inx).packcomponentType == null )) ||
							((tableConfigs.get(inx).primaryType).contains("All"))
							)
					{
						if (tname_tccomponent.containsKey(tableConfigs.get(inx).secondaryType))
						{
							List<TCComponent> lst = (List<TCComponent>)tname_tccomponent.get(tableConfigs.get(inx).secondaryType);
							if(lst != null)
							formarray.addAll(lst);
						}
					}
				}

				Date logindate =  new Date(session.getStartTime());

				ArrayList<TCComponent> refresh_array = new ArrayList<TCComponent>(formarray);

				//load the form only if it is modified after the current TC session was started.
				Iterator<TCComponent> it = refresh_array.iterator();

				while (it.hasNext()) {
					Date form_lmd = it.next().getDateProperty("last_mod_date");

					if (form_lmd.after(logindate) ==false)
						it.remove();
				}

				if (refresh_array.size()>0)
				{
					long sstartTime = System.currentTimeMillis();

					DataManagementService dmService = DataManagementService.getService(session);
					dmService.refreshObjects(refresh_array.toArray(new TCComponent[refresh_array.size()]));

					if(UnileverUtility.isPerfMonitorTriggered == true)
						UnileverUtility.getPerformanceTime(sstartTime, "dmService.refreshObjects in EPAMComponentPropertiesTable " + formarray.size() );
				}
			}

			for(int inx=0;inx<tableConfigs.size();inx++)
			{
				Vector<PAMSecondaryPropValue> secondaryPropValue  = new Vector<PAMSecondaryPropValue>();

				if(((tableConfigs.get(inx).primaryType).contains(pamSecType) == true ) &&  packcomponentType !=  null  &&   tableConfigs.get(inx).packcomponentType != null   && tableConfigs.get(inx).primaryType !=null && ((tableConfigs.get(inx).packcomponentType).equals(packcomponentType) ==true ))
				{
					//this gives all the related forms to the PAM SPec with the relation mentioned in the renderingHint and the type mentioned in the preference
					Vector<TCComponent >relatedComponents =getRelatedComponentOfASecondayType(tname_tccomponent, renderedPropName,tableConfigs.get(inx).secondaryType);

					if (relatedComponents.size() ==0)
						continue;

					//this then sorts the obtained forms based on the row configuration mentioned in the preference
					if(!((tableConfigs.get(inx)).rowConfigurations.propertyName.equalsIgnoreCase("")))
						relatedComponents = sortForms(relatedComponents,(tableConfigs.get(inx)).rowConfigurations.propertyName,((tableConfigs.get(inx)).rowConfigurations.propertyValues).toArray(new String[(tableConfigs.get(inx)).rowConfigurations.propertyValues.size()]));

					int [] proptype = null;

					//looping the related object and adding the column and row configurations using the custom class
					for(int jnx=0;jnx<relatedComponents.size();jnx++)
					{
						TCComponentForm tempForm = (TCComponentForm)relatedComponents.get(jnx);

						//getting the values for the required Columns						
						PAMPropertyNameValue[] propValueArray = new PAMPropertyNameValue[(tableConfigs.get(inx)).ColumnConfigurations.length];
						String [] formproperties = new String [(tableConfigs.get(inx)).ColumnConfigurations.length];

						for(int knx=0;knx<(tableConfigs.get(inx)).ColumnConfigurations.length;knx++)
							formproperties[knx]=(tableConfigs.get(inx)).ColumnConfigurations[knx].columnName;

						TCProperty []currProperty = tempForm.getTCProperties(formproperties);

						if (jnx == 0)
						{
							proptype =  new int [(tableConfigs.get(inx)).ColumnConfigurations.length];

							for(int knx=0;knx<(tableConfigs.get(inx)).ColumnConfigurations.length;knx++)
								proptype[knx]=currProperty[knx].getPropertyType();
						}

						for(int knx=0;knx<(tableConfigs.get(inx)).ColumnConfigurations.length;knx++)
						{
							propValueArray[knx] = new PAMPropertyNameValue();
							propValueArray[knx].propName= (tableConfigs.get(inx)).ColumnConfigurations[knx].columnName;
							propValueArray[knx].tcProperty = currProperty[knx];
							propValueArray[knx].propDisplayName = currProperty[knx].getPropertyDisplayName();
							propValueArray[knx].propValue= currProperty[knx].getDisplayValue();
							propValueArray[knx].columnSize = (tableConfigs.get(inx).ColumnConfigurations)[knx].columnSize;
							propValueArray[knx].isEnabled = (tableConfigs.get(inx).ColumnConfigurations)[knx].isEnabled;
							propValueArray[knx].isStructured = (tableConfigs.get(inx).ColumnConfigurations)[knx].isStructured;
							propValueArray[knx].PropertyType=proptype[knx];
						}
						//getting Values required for each table
						PAMSecondaryPropValue tempSecondaryPropValue = new PAMSecondaryPropValue();
						tempSecondaryPropValue.propNameValuepair = propValueArray;
						tempSecondaryPropValue.secondaryName = tempForm.getType();
						tempSecondaryPropValue.selectedComponent = tempForm;
						secondaryPropValue.add(tempSecondaryPropValue);
					}

					if(secondaryPropValue.size()>0)
						PAMPropertyNameValue_v.add(secondaryPropValue);

				}
				else if((tableConfigs.get(inx).primaryType).contains(pamSecType) &&
						!(ignorePrimaryTypeWhenOnlyPri.contains(tableConfigs.get(inx).secondaryType)))
				{

					//this gives all the related forms to the PAM SPec with the relation mentioned in the renderingHint and the type mentioned in the preference
					Vector<TCComponent >relatedComponents =getRelatedComponentOfASecondayType(tname_tccomponent, renderedPropName,tableConfigs.get(inx).secondaryType);

					if (relatedComponents.size() ==0)
						continue;

					//this then sorts the obtained forms based on the row configuration mentioned in the preference
					if(!((tableConfigs.get(inx)).rowConfigurations.propertyName.equalsIgnoreCase("")))

						relatedComponents = sortForms(relatedComponents,(tableConfigs.get(inx)).rowConfigurations.propertyName,((tableConfigs.get(inx)).rowConfigurations.propertyValues).toArray(new String[(tableConfigs.get(inx)).rowConfigurations.propertyValues.size()]));

					int [] proptype = null;

					//looping the related object and adding the column and row configurations using the custom class
					for(int jnx=0;jnx<relatedComponents.size();jnx++)
					{
						TCComponentForm tempForm = (TCComponentForm)relatedComponents.get(jnx);
						//getting the values for the required Columns						
						PAMPropertyNameValue[] propValueArray = new PAMPropertyNameValue[(tableConfigs.get(inx)).ColumnConfigurations.length];
						String [] formproperties = new String [(tableConfigs.get(inx)).ColumnConfigurations.length];

						for(int knx=0;knx<(tableConfigs.get(inx)).ColumnConfigurations.length;knx++)
							formproperties[knx]=(tableConfigs.get(inx)).ColumnConfigurations[knx].columnName;

						TCProperty []currProperty = tempForm.getTCProperties(formproperties);

						if (jnx == 0)
						{
							proptype =  new int [(tableConfigs.get(inx)).ColumnConfigurations.length];

							for(int knx=0;knx<(tableConfigs.get(inx)).ColumnConfigurations.length;knx++)
								proptype[knx]=currProperty[knx].getPropertyType();
						}

						for(int knx=0;knx<(tableConfigs.get(inx)).ColumnConfigurations.length;knx++)
						{
							propValueArray[knx] = new PAMPropertyNameValue();
							propValueArray[knx].propName= (tableConfigs.get(inx)).ColumnConfigurations[knx].columnName;
							propValueArray[knx].tcProperty = currProperty[knx];
							propValueArray[knx].propDisplayName = currProperty[knx].getPropertyDisplayName();
							propValueArray[knx].propValue= currProperty[knx].getDisplayValue();
							propValueArray[knx].columnSize = (tableConfigs.get(inx).ColumnConfigurations)[knx].columnSize;
							propValueArray[knx].isEnabled = (tableConfigs.get(inx).ColumnConfigurations)[knx].isEnabled;
							propValueArray[knx].isStructured = (tableConfigs.get(inx).ColumnConfigurations)[knx].isStructured;
							propValueArray[knx].PropertyType=proptype[knx];
						}
						//getting Values required for each table
						PAMSecondaryPropValue tempSecondaryPropValue = new PAMSecondaryPropValue();
						tempSecondaryPropValue.propNameValuepair = propValueArray;
						tempSecondaryPropValue.secondaryName = tempForm.getType();
						tempSecondaryPropValue.selectedComponent = tempForm;
						secondaryPropValue.add(tempSecondaryPropValue);
					}

					if(secondaryPropValue.size()>0)
						PAMPropertyNameValue_v.add(secondaryPropValue);

				}
				else if((tableConfigs.get(inx).primaryType).contains("All") &&
						!(ignorePrimaryTypeWhenAll.contains(tableConfigs.get(inx).secondaryType)) &&
						!(ignorePrimaryTypeWhenOnlyPri.contains(tableConfigs.get(inx).secondaryType)))
				{
					//this gives all the related forms to the PAM SPec with the relation mentioned in the renderingHint and the type mentioned in the preference
					Vector<TCComponent >relatedComponents =getRelatedComponentOfASecondayType(tname_tccomponent, renderedPropName,tableConfigs.get(inx).secondaryType);

					if (relatedComponents.size() ==0)
						continue;

					//this then sorts the obtained forms based on the row configuration mentioned in the preference
					if(!((tableConfigs.get(inx)).rowConfigurations.propertyName.equalsIgnoreCase("")))
						relatedComponents = sortForms(relatedComponents,(tableConfigs.get(inx)).rowConfigurations.propertyName,((tableConfigs.get(inx)).rowConfigurations.propertyValues).toArray(new String[(tableConfigs.get(inx)).rowConfigurations.propertyValues.size()]));

					int [] proptype = null;

					//looping the related object and adding the column and row configurations using the custom class
					for(int jnx=0;jnx<relatedComponents.size();jnx++)
					{
						TCComponentForm tempForm = (TCComponentForm)relatedComponents.get(jnx);
						//getting the values for the required Columns						
						PAMPropertyNameValue[] propValueArray = new PAMPropertyNameValue[(tableConfigs.get(inx)).ColumnConfigurations.length];
						String [] formproperties = new String [(tableConfigs.get(inx)).ColumnConfigurations.length];

						for(int knx=0;knx<(tableConfigs.get(inx)).ColumnConfigurations.length;knx++)
							formproperties[knx]=(tableConfigs.get(inx)).ColumnConfigurations[knx].columnName;

						TCProperty []currProperty = tempForm.getTCProperties(formproperties);

						if (jnx == 0)
						{
							proptype =  new int [(tableConfigs.get(inx)).ColumnConfigurations.length];

							for(int knx=0;knx<(tableConfigs.get(inx)).ColumnConfigurations.length;knx++)
								proptype[knx]=currProperty[knx].getPropertyType();
						}

						for(int knx=0;knx<(tableConfigs.get(inx)).ColumnConfigurations.length;knx++)
						{

							/**
							 * 28-Sept-2014: Added following check as Materials Tab was not getting populated due to one of the property
							 * "u4_approved_grades" on U4_PackLayerStructureForm is causing NULL. Hence, below check is introduced.
							 * Check why "U4_PackLayerStructureForm:u4_approved_grades" is causing NULL.
							 */
							if(currProperty[knx] != null)
							{
								propValueArray[knx] = new PAMPropertyNameValue();
								propValueArray[knx].propName= (tableConfigs.get(inx)).ColumnConfigurations[knx].columnName;
								propValueArray[knx].tcProperty = currProperty[knx];
								propValueArray[knx].propDisplayName = currProperty[knx].getPropertyDisplayName();
								propValueArray[knx].propValue= currProperty[knx].getDisplayValue();
								propValueArray[knx].columnSize = (tableConfigs.get(inx).ColumnConfigurations)[knx].columnSize;
								propValueArray[knx].isEnabled = (tableConfigs.get(inx).ColumnConfigurations)[knx].isEnabled;
								propValueArray[knx].isStructured = (tableConfigs.get(inx).ColumnConfigurations)[knx].isStructured;
								propValueArray[knx].PropertyType=proptype[knx];
							}
							if(currProperty == null)
								System.out.println("the property for which its failing-"+propValueArray[knx].propName);
						}
						//getting Values required for each table
						PAMSecondaryPropValue tempSecondaryPropValue = new PAMSecondaryPropValue();
						tempSecondaryPropValue.propNameValuepair = propValueArray;
						tempSecondaryPropValue.secondaryName = tempForm.getType();
						tempSecondaryPropValue.selectedComponent = tempForm;
						secondaryPropValue.add(tempSecondaryPropValue);
					}

					if(secondaryPropValue.size()>0)
						PAMPropertyNameValue_v.add(secondaryPropValue);

				}
			}

			if(PAMPropertyNameValue_v.size() > 0 )
			{
				tableComposite = new Composite(parentComposite, SWT.NONE);
				GridData localGridData = new GridData(1808);
				localGridData.heightHint = (PAMPropertyNameValue_v.size())*cpTableHeight+100;
				localGridData.widthHint = cpTableWidth;
				tableComposite.setLayout(new GridLayout(1,true));
				tableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
				parentFormToolKit.adapt(tableComposite, true, true);
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void createTables () throws TCException
	{
		long sstartTime = System.currentTimeMillis();

		loadConfigurations();
		String technology = PAMConstant.getTechnologyValue(pamSpecRevision);
		TCPreferenceService prefservice = session.getPreferenceService();
		String []prefs = prefservice.getStringValues("U4_AssemblyTechnology");
		if(prefs != null)
			for(int i = 0 ;i < prefs.length ;i++)
				if(prefs[i].equalsIgnoreCase(technology) )
					frame = "G-PamAssembly";
		if(frame == null)
			frame = PAMConstant.getFrameValue(pamSpecRevision);
	    if(frame != null )  getAttributeTMdetails(frame);
		if(UnileverUtility.isPerfMonitorTriggered == true)
			UnileverUtility.getPerformanceTime(sstartTime, "loadConfigurations in EPAMTableComposite");

		boolean prefAPFORM = false ;

		Table[] propertyTables = new Table[PAMPropertyNameValue_v.size()];

		for(int inx=0;inx<PAMPropertyNameValue_v.size();inx++)
		{
			sstartTime = System.currentTimeMillis();
			
			int style = SWT.VIRTUAL | SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.HIDE_SELECTION;
			Label propertyLabel = new Label(tableComposite,SWT.NONE);
			Button addnewRow = null;
			Button addnewRow1 = null;
			Button addnewRow2 = null;
			Button addnewRow3 = null;
			String tableType = (PAMPropertyNameValue_v.get(inx)).get(0).selectedComponent.getDisplayType();
			String formType = (PAMPropertyNameValue_v.get(inx)).get(0).selectedComponent.getType();
			//section = tableType;
			propertyLabel.setText(tableType+":");			
			propertyLabel.pack();			
			final int index = inx ;
			if(formType.equals(APFORM))
			{
				addnewRow = new Button (tableComposite,SWT.None);					
				addnewRow.setText("Add New Row");
				addnewRow.setVisible(true);
				addnewRow.setEnabled(false);
				if(isCheckOut == true){	
				addnewRow.setEnabled(true);
				}
			}
			else if(formType.equals(APMBFORM))
			{
				addnewRow1 = new Button (tableComposite,SWT.None);
				addnewRow1.setText("Add New Row");
				addnewRow1.setVisible(true);
				addnewRow1.setEnabled(false);				
				if(isCheckOut == true){						
				addnewRow1.setEnabled(true);
				}
			}
			else if(formType.equals(ADTLFORM))
			{
				addnewRow2 = new Button (tableComposite,SWT.None);		
				addnewRow2.setText("Add New Row");
				addnewRow2.setVisible(true);
				addnewRow2.setEnabled(false);
				if(isCheckOut == true){
				addnewRow2.setEnabled(true);
				}
			}
			else if(formType.equals(PRTMTLDTLFORM))
			{
				addnewRow3 = new Button (tableComposite,SWT.None);				
				addnewRow3.setText("Add New Row");
				addnewRow3.setVisible(true);
				addnewRow3.setEnabled(false);
				if(isCheckOut == true){
				addnewRow3.setEnabled(true);
				}
			}

			if(formType.equalsIgnoreCase(JUSTIFICATIONFORM))
			{
				String []values = prefServ.getStringValues("U4_NonPMLUISectionLegend");
				StringBuilder legend = new StringBuilder();
				//Added by Jayateertha - due to NullPointerException, one of the table was not rendering properly.
				if(values != null)
				{
					for(int i = 0 ;i < values.length ;i++)
						legend.append(values[i]).append("\n");
				}

				Display display = AIFUtility.getActiveDesktop().getShell().getDisplay() ;
				StyledText txt = new StyledText(tableComposite, SWT.NONE);
				txt.setText("*"+legend);
				Color red = display.getSystemColor(SWT.COLOR_DARK_RED);
				Font font = new Font(display, "Ariel", 9, SWT.NORMAL);
				StyleRange ss = new StyleRange();
				ss.start = 0;
				ss.length = legend.length() + 1;
				ss.foreground = red;
				ss.fontStyle = SWT.BOLD;
				ss.font = font ;
				txt.setStyleRange(ss);
				txt.setEditable(false);
				txt.setEnabled(false);
			}

			propertyTables[inx] = new Table(tableComposite,style );
			GridData localGridData = new GridData(1808);
			propertyTables[inx].setRedraw(false);
			localGridData.heightHint = cpTableHeight;
			localGridData.widthHint = cpTableWidth;
			propertyTables[inx].setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

			propertyTables[inx].setHeaderVisible(true);
			propertyTables[inx].setLinesVisible(true);
			Display display =  propertyTables[inx].getDisplay();

			if(isCheckOut)
			{
				if(enable_editor)
				{
					Color white = display.getSystemColor(SWT.COLOR_WHITE);
					propertyTables[inx].setBackground(white);
				}
				else
				{
					Color lightGray = new Color (display, 240, 240, 240);
					propertyTables[inx].setBackground(lightGray);
				}
			}

			else
			{
				Color lightGray = new Color (display, 240, 240, 240);
				propertyTables[inx].setBackground(lightGray);
			}

			if(formType.equals(APFORM) || formType.equals(AP1FORM))
			{
				if(!prefAPFORM)
				{
					String[] prefValues = prefServ.getStringValues(TYPEPMLTYPE);

					for(int inz=0;inz<prefValues.length;inz++)
					{
						String []str = prefValues[inz].split("=");

						if(str[0].length() > 0 && str[1].length() > 0)
						{
							String []val = str[1].split(";");
							List<String> newList = Arrays.asList(val);
							ArrayList<String> al = new ArrayList<String> ();
							al.addAll(newList);

							prefPolymers.put(str[0], al);
						}
					}

					prefAPFORM = true ;
				}

				if(isCheckOut == true)
				{
					Hashtable<String, TCComponent> pmlrevs =  tableObjects.get(formType);
					
					if (pmlrevs != null) {
						if (pmlrevs.size() == 0) {
							queryPML("__PML__ApprovedPolymers",formType);
						}
					}else {
						queryPML("__PML__ApprovedPolymers",formType);
					}
				}

				long startTime = System.currentTimeMillis();

				setTableConfiguration1(propertyTables[inx],PAMPropertyNameValue_v.get(inx).toArray(new PAMSecondaryPropValue[PAMPropertyNameValue_v.get(inx).size()]));

				if(UnileverUtility.isPerfMonitorTriggered == true)
				{
					UnileverUtility.getPerformanceTime(startTime, "setTableConfiguration1 " + formType );
				}
				
				addnewRow.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						try {
							try {
								saveProperty(pamSpecRevision);
							
							} catch (Exception e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							
							String templateId = pamSpecRevision.getStringProperty("u4_template_id");
							String TemplateSyncRevId = pamSpecRevision.getStringProperty("u4_last_sync_revision_id");
							
							String queryName = "Item Revision...";
							String entries[] = {"Item ID", "Revision"};
							String values[] = {templateId,TemplateSyncRevId};
							TCComponent[] qryResults = UnileverQueryUtil.executeQuery(queryName,entries,values);
							int countPolymerForms = 0;							
							if(qryResults.length==1)
							{
								//get count of max no of polymer forms in the master template
								TCComponentItemRevision templateRev = (TCComponentItemRevision)qryResults[0];
								AIFComponentContext[] polymerFormType = templateRev.getRelated(MATFORMS);
								for(AIFComponentContext tempform:polymerFormType){									
									TCComponentForm tmpfrm = (TCComponentForm) tempform.getComponent();
									String templateFormType = tmpfrm.getType();
									if(templateFormType.equals(APFORM))
									{
										countPolymerForms++;
									}
								}	
								//get count of polymer forms in pam spec
								int countPamSpecPolymerForms = PAMPropertyNameValue_v.get(index).toArray(new PAMSecondaryPropValue[PAMPropertyNameValue_v.get(index).size()]).length;
								
								//add new row if number of forms in pam spec is less than the number forms in master template
								if(countPolymerForms>countPamSpecPolymerForms)
								{	
									//get last sequence number of form attached to pam spec
									ArrayList<Integer>listOfForms = new ArrayList<Integer>();								
									AIFComponentContext[] pamSpecPolymerFormType = pamSpecRevision.getRelated(MATFORMS);
									for(AIFComponentContext tempform:pamSpecPolymerFormType){									
										TCComponentForm tmpfrm = (TCComponentForm) tempform.getComponent();
										String templateFormType = tmpfrm.getType();
										if(templateFormType.equals(APFORM))
										{
											int seq_no = tmpfrm.getTCProperty("u4_sequence_no").getIntValue();
											listOfForms.add(seq_no);	
										}								
									}
									Collections.sort(listOfForms);
									Collections.reverse(listOfForms);
									//search for form with the next sequence number to perform save as on
									for(AIFComponentContext tempform:polymerFormType){									
										TCComponentForm tmpfrm = (TCComponentForm) tempform.getComponent();
										String templateFormType = tmpfrm.getType();
										if(templateFormType.equals(APFORM))
										{
											int seqNo = tmpfrm.getTCProperty("u4_sequence_no").getIntValue();
											if(seqNo==(listOfForms.get(0)+100)){
												TCComponentForm newForm = tmpfrm.saveAs(tmpfrm.getProperty(UL4Common.OBJECT_NAME));												
												PasteOperation paste2 = new PasteOperation(AIFUtility.getCurrentApplication(),pamSpecRevision,newForm,MATFORMS);	
												paste2.executeOperation();	
												break;
											}
										}
									}			
								}
								else
								{
									MessageBox.post(AIFUtility.getActiveDesktop().getShell(),"Cannot add anymore rows as the maximum limit is reached","Add New Row",MessageBox.INFORMATION);
								}
							}
						} catch (TCException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
				});			
			}
			else if (formType.equals(PACKLAYERFORM) )
			{

				if(isCheckOut == true)
				{
					queryPML("__PML_PackLayer",formType);
				}

				long startTime = System.currentTimeMillis();

				setTableConfiguration1(propertyTables[inx],PAMPropertyNameValue_v.get(inx).toArray(new PAMSecondaryPropValue[PAMPropertyNameValue_v.get(inx).size()]));

				if(UnileverUtility.isPerfMonitorTriggered == true)
				{
					UnileverUtility.getPerformanceTime(startTime, "setTableConfiguration1 " + formType );
				}
			}
			else if(formType.equals(LAYERFORM))
			{

				if(isCheckOut == true)
				{
					queryPML("__PML_PackLayer",formType);
				}

				long startTime = System.currentTimeMillis();

				setTableConfiguration2(propertyTables[inx],PAMPropertyNameValue_v.get(inx).toArray(new PAMSecondaryPropValue[PAMPropertyNameValue_v.get(inx).size()]));

				if(UnileverUtility.isPerfMonitorTriggered == true)
				{
					UnileverUtility.getPerformanceTime(startTime, "setTableConfiguration2 " + formType );
				}
			}
			else if(MATLFORM.equals(formType) || formType.equals(MATL2FORM))
			{
				if (formType.equals(MATL2FORM))
				{
					if(isCheckOut == true)
					{
						queryPML("__PML_MaterialDetail2",formType);
					}
				}

				long startTime = System.currentTimeMillis();

				setTableConfiguration1(propertyTables[inx],PAMPropertyNameValue_v.get(inx).toArray(new PAMSecondaryPropValue[PAMPropertyNameValue_v.get(inx).size()]));

				if(UnileverUtility.isPerfMonitorTriggered == true)
				{
					UnileverUtility.getPerformanceTime(startTime, "setTableConfiguration1 " + formType );
				}
			}
			else if(formType.equals(APMBFORM))
			{
				long startTime = System.currentTimeMillis();

				setTableConfiguration(propertyTables[inx],PAMPropertyNameValue_v.get(inx).toArray(new PAMSecondaryPropValue[PAMPropertyNameValue_v.get(inx).size()]));

				if(UnileverUtility.isPerfMonitorTriggered == true)
				{
					UnileverUtility.getPerformanceTime(startTime, "setTableConfiguration " + formType );
				}	
				addnewRow1.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						try {

							try {
								saveProperty(pamSpecRevision);
																
							} catch (Exception e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}

							String templateId = pamSpecRevision.getStringProperty("u4_template_id");
							String TemplateSyncRevId = pamSpecRevision.getStringProperty("u4_last_sync_revision_id");
							
							String queryName = "Item Revision...";
							String entries[] = {"Item ID", "Revision"};
							String values[] = {templateId,TemplateSyncRevId};
							TCComponent[] qryResults = UnileverQueryUtil.executeQuery(queryName,entries,values);
							int countPolymerForms = 0;							
							if(qryResults.length==1)
							{
								//get count of max no of polymer forms in the master template
								TCComponentItemRevision templateRev = (TCComponentItemRevision)qryResults[0];
								AIFComponentContext[] polymerFormType = templateRev.getRelated(MATFORMS);
								for(AIFComponentContext tempform:polymerFormType){									
									TCComponentForm tmpfrm = (TCComponentForm) tempform.getComponent();
									String templateFormType = tmpfrm.getType();
									if(templateFormType.equals(APMBFORM))
									{
										countPolymerForms++;
									}									
								}	
								//get count of polymer forms in pam spec
								int countPamSpecPolymerForms = PAMPropertyNameValue_v.get(index).toArray(new PAMSecondaryPropValue[PAMPropertyNameValue_v.get(index).size()]).length;
								
								//add new row if number of forms in pam spec is less than the number forms in master template
								if(countPolymerForms>countPamSpecPolymerForms)
								{	
									//get last sequence number of form attached to pam spec
									ArrayList<Integer>listOfForms = new ArrayList<Integer>();								
									AIFComponentContext[] pamSpecPolymerFormType = pamSpecRevision.getRelated(MATFORMS);
									for(AIFComponentContext tempform:pamSpecPolymerFormType){									
										TCComponentForm tmpfrm = (TCComponentForm) tempform.getComponent();
										String templateFormType = tmpfrm.getType();
										if(templateFormType.equals(APMBFORM))
										{
											int seq_no = tmpfrm.getTCProperty("u4_sequence_no").getIntValue();
											listOfForms.add(seq_no);	
										}								
									}
									Collections.sort(listOfForms);
									Collections.reverse(listOfForms);
									//search for form with the next sequence number to perform save as on
									for(AIFComponentContext tempform:polymerFormType){									
										TCComponentForm tmpfrm = (TCComponentForm) tempform.getComponent();
										String templateFormType = tmpfrm.getType();
										if(templateFormType.equals(APMBFORM))
										{
											int seqNo = tmpfrm.getTCProperty("u4_sequence_no").getIntValue();
											if(seqNo==(listOfForms.get(0)+100)){
												TCComponentForm newForm = tmpfrm.saveAs(tmpfrm.getProperty(UL4Common.OBJECT_NAME));												
												PasteOperation paste2 = new PasteOperation(AIFUtility.getCurrentApplication(),pamSpecRevision,newForm,MATFORMS);	
												paste2.executeOperation();	
												break;
											}
										}										
									}			
								}
								else
								{
									MessageBox.post(AIFUtility.getActiveDesktop().getShell(),"Cannot add anymore rows as the maximum limit is reached","Add New Row",MessageBox.INFORMATION);
								}
							}
						} catch (TCException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
				});
			}
			else if(formType.equals(PRTMTLDTLFORM))
			{
				long startTime = System.currentTimeMillis();

				setTableConfiguration(propertyTables[inx],PAMPropertyNameValue_v.get(inx).toArray(new PAMSecondaryPropValue[PAMPropertyNameValue_v.get(inx).size()]));

				if(UnileverUtility.isPerfMonitorTriggered == true)
				{
					UnileverUtility.getPerformanceTime(startTime, "setTableConfiguration " + formType );
				}	
				addnewRow3.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						try {

							try {
								saveProperty(pamSpecRevision);
							} catch (Exception e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							
							String templateId = pamSpecRevision.getStringProperty("u4_template_id");
							String TemplateSyncRevId = pamSpecRevision.getStringProperty("u4_last_sync_revision_id");
							
							String queryName = "Item Revision...";
							String entries[] = {"Item ID", "Revision"};
							String values[] = {templateId,TemplateSyncRevId};
							TCComponent[] qryResults = UnileverQueryUtil.executeQuery(queryName,entries,values);
							int countPolymerForms = 0;							
							if(qryResults.length==1)
							{
								//get count of max no of polymer forms in the master template
								TCComponentItemRevision templateRev = (TCComponentItemRevision)qryResults[0];
								AIFComponentContext[] polymerFormType = templateRev.getRelated(MATFORMS);
								for(AIFComponentContext tempform:polymerFormType){									
									TCComponentForm tmpfrm = (TCComponentForm) tempform.getComponent();
									String templateFormType = tmpfrm.getType();
									if(templateFormType.equals(PRTMTLDTLFORM))
									{
										countPolymerForms++;
									}									
								}	
								//get count of polymer forms in pam spec
								int countPamSpecPolymerForms = PAMPropertyNameValue_v.get(index).toArray(new PAMSecondaryPropValue[PAMPropertyNameValue_v.get(index).size()]).length;
								
								//add new row if number of forms in pam spec is less than the number forms in master template
								if(countPolymerForms>countPamSpecPolymerForms)
								{	
									//get last sequence number of form attached to pam spec
									ArrayList<Integer>listOfForms = new ArrayList<Integer>();								
									AIFComponentContext[] pamSpecPolymerFormType = pamSpecRevision.getRelated(MATFORMS);
									for(AIFComponentContext tempform:pamSpecPolymerFormType){									
										TCComponentForm tmpfrm = (TCComponentForm) tempform.getComponent();
										String templateFormType = tmpfrm.getType();
										if(templateFormType.equals(PRTMTLDTLFORM))
										{
											int seq_no = tmpfrm.getTCProperty("u4_sequence_no").getIntValue();
											listOfForms.add(seq_no);	
										}								
									}
									Collections.sort(listOfForms);
									Collections.reverse(listOfForms);
									//search for form with the next sequence number to perform save as on
									for(AIFComponentContext tempform:polymerFormType){									
										TCComponentForm tmpfrm = (TCComponentForm) tempform.getComponent();
										String templateFormType = tmpfrm.getType();
										if(templateFormType.equals(PRTMTLDTLFORM))
										{
											int seqNo = tmpfrm.getTCProperty("u4_sequence_no").getIntValue();
											if(seqNo==(listOfForms.get(0)+100)){
												TCComponentForm newForm = tmpfrm.saveAs(tmpfrm.getProperty(UL4Common.OBJECT_NAME));												
												PasteOperation paste2 = new PasteOperation(AIFUtility.getCurrentApplication(),pamSpecRevision,newForm,MATFORMS);	
												paste2.executeOperation();	
												break;
											}
										}										
									}			
								}
								else
								{
									MessageBox.post(AIFUtility.getActiveDesktop().getShell(),"Cannot add anymore rows as the maximum limit is reached","Add New Row",MessageBox.INFORMATION);
								}
							}
						} catch (TCException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
				});
			}
			else if(formType.equals(ADTLFORM))
			{
				long startTime = System.currentTimeMillis();

				setTableConfiguration(propertyTables[inx],PAMPropertyNameValue_v.get(inx).toArray(new PAMSecondaryPropValue[PAMPropertyNameValue_v.get(inx).size()]));

				if(UnileverUtility.isPerfMonitorTriggered == true)
				{
					UnileverUtility.getPerformanceTime(startTime, "setTableConfiguration " + formType );
				}	
				addnewRow2.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						try {

							try {
								saveProperty(pamSpecRevision);
							} catch (Exception e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}

							String templateId = pamSpecRevision.getStringProperty("u4_template_id");
							String TemplateSyncRevId = pamSpecRevision.getStringProperty("u4_last_sync_revision_id");
							
							String queryName = "Item Revision...";
							String entries[] = {"Item ID", "Revision"};
							String values[] = {templateId,TemplateSyncRevId};
							TCComponent[] qryResults = UnileverQueryUtil.executeQuery(queryName,entries,values);
							int countPolymerForms = 0;							
							if(qryResults.length==1)
							{
								//get count of max no of polymer forms in the master template
								TCComponentItemRevision templateRev = (TCComponentItemRevision)qryResults[0];
								AIFComponentContext[] polymerFormType = templateRev.getRelated(MATFORMS);
								for(AIFComponentContext tempform:polymerFormType){									
									TCComponentForm tmpfrm = (TCComponentForm) tempform.getComponent();
									String templateFormType = tmpfrm.getType();
									if(templateFormType.equals(ADTLFORM))
									{
										countPolymerForms++;
									}									
								}	
								//get count of polymer forms in pam spec
								int countPamSpecPolymerForms = PAMPropertyNameValue_v.get(index).toArray(new PAMSecondaryPropValue[PAMPropertyNameValue_v.get(index).size()]).length;
								
								//add new row if number of forms in pam spec is less than the number forms in master template
								if(countPolymerForms>countPamSpecPolymerForms)
								{	
									//get last sequence number of form attached to pam spec
									ArrayList<Integer>listOfForms = new ArrayList<Integer>();								
									AIFComponentContext[] pamSpecPolymerFormType = pamSpecRevision.getRelated(MATFORMS);
									for(AIFComponentContext tempform:pamSpecPolymerFormType){									
										TCComponentForm tmpfrm = (TCComponentForm) tempform.getComponent();
										String templateFormType = tmpfrm.getType();
										if(templateFormType.equals(ADTLFORM))
										{
											int seq_no = tmpfrm.getTCProperty("u4_sequence_no").getIntValue();
											listOfForms.add(seq_no);
										}																		
									}
									Collections.sort(listOfForms);
									Collections.reverse(listOfForms);
									//search for form with the next sequence number to perform save as on
									for(AIFComponentContext tempform:polymerFormType){									
										TCComponentForm tmpfrm = (TCComponentForm) tempform.getComponent();
										String templateFormType = tmpfrm.getType();
										if(templateFormType.equals(ADTLFORM))
										{
											int seqNo = tmpfrm.getTCProperty("u4_sequence_no").getIntValue();
											if(seqNo==(listOfForms.get(0)+100)){
												TCComponentForm newForm = tmpfrm.saveAs(tmpfrm.getProperty(UL4Common.OBJECT_NAME));												
												PasteOperation paste2 = new PasteOperation(AIFUtility.getCurrentApplication(),pamSpecRevision,newForm,MATFORMS);	
												paste2.executeOperation();	
												break;
											}
										}																				
									}			
								}
								else
								{
									MessageBox.post(AIFUtility.getActiveDesktop().getShell(),"Cannot add anymore rows as the maximum limit is reached","Add New Row",MessageBox.INFORMATION);
								}
							}
						} catch (TCException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
				});
			}
			else
			{
				long startTime = System.currentTimeMillis();

				setTableConfiguration(propertyTables[inx],PAMPropertyNameValue_v.get(inx).toArray(new PAMSecondaryPropValue[PAMPropertyNameValue_v.get(inx).size()]));

				if(UnileverUtility.isPerfMonitorTriggered == true)
				{
					UnileverUtility.getPerformanceTime(startTime, "setTableConfiguration " + formType );
				}				
			}
			
			propertyTables[inx].setRedraw(true);
		}
		//tableComposite.setRedraw( true );
	}

	private void queryPML(String queryName , String formType)
	{
		
		long startTime = System.currentTimeMillis();
		String entries[] = {"ID"};
		String values[] = {"*"};
		String objName = null;

		Hashtable<String,TCComponent> objects = new Hashtable<String, TCComponent>();

		QueryService query_svr = QueryService.getService((TCSession)AIFDesktop.getActiveDesktop().getCurrentApplication().getSession());
		
		PMLQueryResult  response = query_svr.executePMLQuery(queryName);
		
		if(response.objects != null && response.objectname != null)
		{
			for( int inz=0 ;inz < response.objects.length ;inz++)
			{
				objects.put(response.objectname[inz], response.objects[inz]);
			}
		}

		tableObjects.put(formType, objects);

		if(formType.equals(APFORM) || formType.equals(AP1FORM))
		{
			tableObjects.put(APFORM, objects);
			tableObjects.put(AP1FORM, objects);
		}
		else
		{
			tableObjects.put(formType, objects);
		}

		if(UnileverUtility.isPerfMonitorTriggered == true)
			UnileverUtility.getPerformanceTime(startTime, "queryPML " + queryName + " " + formType);
	}

	private String getObjectName(TCComponentForm form)
	{
		try
		{
			return (form.getTCProperty("object_name").toString());
		}
		catch (TCException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		return null;
	}

	/**
	 * @param tcTable
	 * @param tableConfigValues
	 */

	public void setTableConfiguration(final Table tcTable, final PAMSecondaryPropValue[] tableConfigValues)
	{
		String[] coating_lovvalues = null;
		Object[] ApprvdAdditiveDtlLOVValues = null;

		final Map<String,Integer > textLimit = new HashMap<String,Integer>(PAMConstant.getTextLimit (tableConfigValues[0]));

		final int nColumn  = tableConfigValues[0].propNameValuepair.length;

		for(  int ColumnNumber =0;ColumnNumber< tableConfigValues[0].propNameValuepair.length;ColumnNumber++ )
		{
			final TableColumn propertyColumn =  new TableColumn(tcTable, SWT.BORDER_SOLID|SWT.COLOR_WIDGET_NORMAL_SHADOW);
			propertyColumn.setWidth(tableConfigValues[0].propNameValuepair[ColumnNumber].columnSize);

			String nametext = tableConfigValues[0].propNameValuepair[ColumnNumber].propDisplayName;
			propertyColumn.setText(nametext);
			propertyColumn.setResizable(true);
		}

		String formType = tableConfigValues[0].secondaryName;
		
		final HashMap<String, ArrayList<String>> apprvdadditivedtl_lov = new HashMap<String, ArrayList<String>>();

		if (formType.equals("U4_ApprvdAdditiveDtlForm")==true)
		{
			if (isCheckOut==true)
			{
				TCComponentListOfValues ApprvdAdditiveDtlLOV = TCComponentListOfValuesType.findLOVByName("U4_PL_ApprvdAdditiveDtlLOV");

				InitialLovData mInitData = new InitialLovData( );
				mInitData.lov = ApprvdAdditiveDtlLOV;
				mInitData.propertyName=tableConfigValues[0].propNameValuepair[0].propName;
				mInitData.lovInput.owningObject=tableConfigValues[0].selectedComponent;
				
				LOVSearchResults mInitDataResponse = lovServ.getInitialLOVValues( mInitData );
				for( LOVValueRow LOVValue : mInitDataResponse.lovValues )
				{
					String first_level_lov = ((String[])LOVValue.propDisplayValues.get( "lov_values" ))[0];
					ArrayList<String> second_level_lov = new ArrayList<String>(LOVValue.childRows.length);

					for( int index = 0;index < LOVValue.childRows.length;index++ )
						second_level_lov.add(((String[])LOVValue.childRows[index].propDisplayValues.get( "lov_values" ))[0]);

					second_level_lov.add("");

					apprvdadditivedtl_lov.put(first_level_lov,second_level_lov);
				}
			}
		}

		if (  formType.equals(MATLPROPFORM )==true)
		{
			String [] initialArray = prefServ.getStringValuesAtLocation(COATINGCARTON, TCPreferenceLocation.OVERLAY_LOCATION);
			
			String [] newArray = new String[1+initialArray.length];
			newArray[0]="";

			if (initialArray!=null && initialArray.length>0)
			{
				System.arraycopy(initialArray, 0, newArray, 1, initialArray.length);
				coating_lovvalues=newArray;
				
				tcTable.setData("coating_lovvalues", coating_lovvalues);
			}
		}
			

		tcTable.setItemCount(tableConfigValues.length);
		
		tcTable.addListener(SWT.SetData, new Listener() {
			public void handleEvent(Event event) {


				final TableItem item = (TableItem) event.item;
				int row = tcTable.indexOf(item);
				boolean legend = PAMConstant.ruleValidate((TCComponentForm) tableConfigValues[row].selectedComponent);
				final TCComponentForm currForm = (TCComponentForm) tableConfigValues[row].selectedComponent;
				final String formType = tableConfigValues[row].secondaryName;
				final Map<String,Object> compPropVal_m = new HashMap<String,Object>();

				boolean pml2pam = false ;

				String name = null;

				if (formType.equals(CPFORM) && labelLit == true)
				{
					name = getObjectName(currForm);

					if( PML2PAMCPMap != null && PML2PAMCPMap.containsValue(name))
						pml2pam = true ;
				}

				Map<String ,Object > pamform = new HashMap<String ,Object >();

				for(  int ColumnNumber =0;ColumnNumber< nColumn;ColumnNumber++ )
				{
					String PropValue = tableConfigValues[row].propNameValuepair[ColumnNumber].propValue;
					int propType =  tableConfigValues[row].propNameValuepair[ColumnNumber].PropertyType;
					TCProperty currTCProp =  tableConfigValues[row].propNameValuepair[ColumnNumber].tcProperty;
					String propname = tableConfigValues[row].propNameValuepair[ColumnNumber].propName;

					final TableEditor editor = new TableEditor(tcTable);

					Text text = new Text(tcTable, SWT.NONE);

					if(ColumnNumber == 0 && legend )
					{
						TableEditor mandatroyEditor = new TableEditor(tcTable);
						PAMConstant.setMandatory(PropValue,mandatroyEditor,item);

					}	
					if(!(tableConfigValues[row].propNameValuepair[ColumnNumber].isEnabled))
					{
						item.setText(ColumnNumber,PropValue);
						continue;
					}
					if(isStructuredComponent(currForm)&&tableConfigValues[row].propNameValuepair[ColumnNumber].isStructured)
					{
						item.setText(ColumnNumber,PropValue);
						continue;
					}

					switch (propType)
					{
					case 1:
						text = new Text(tcTable, SWT.NONE);
						item.setData(propname, (Text) text);
						item.setData("savemap", (HashMap) compPropVal_m);
						if(PropValue!=null )
							text.setText(PropValue);
						editor.grabHorizontal = true;
						editor.setEditor(text,item, ColumnNumber);
						// Added as part of CR#21
						editor.getEditor().setEnabled(enable_editor);
						text.setData("colId", ColumnNumber);

						text.addModifyListener(new ModifyListener() {
							public void modifyText(ModifyEvent event) {
								PAMSecondaryPropValue currentSecPropValues = tableConfigValues[tcTable.indexOf(item)] ;
								compPropVal_m.put("puid",currentSecPropValues.selectedComponent);
								compPropVal_m.put("section",section);
								int columnNum = (Integer) ((Text)(event.getSource())).getData("colId");
								compPropVal_m.put(currentSecPropValues.propNameValuepair[columnNum].propName,((Text) (event.getSource())).getText());
							}
						});

						if(pml2pam)
						{
							if(matlDetails2loaded)
								text.setEnabled(false);
							resetcartoncontrols(currForm,item);
							if(! pamform.containsKey(propname))
								pamform.put(propname,text);
						}
						else
							text.setEnabled(true);
						break;
					case 2:
						text = new Text(tcTable, SWT.NONE);
						item.setData(propname, (Text) text);
						item.setData("savemap", (HashMap) compPropVal_m);
						if(PropValue!=null )
							text.setText(PropValue);
						editor.grabHorizontal = true;
						editor.setEditor(text,item, ColumnNumber);
						// Added as part of CR#21
						editor.getEditor().setEnabled(enable_editor);
						text.setData("colId", ColumnNumber);
						text.addModifyListener(new ModifyListener() {
							public void modifyText(ModifyEvent event) {
								//getting the row index
								PAMSecondaryPropValue currentSecPropValues = tableConfigValues[tcTable.indexOf(item)] ;
								compPropVal_m.put("puid",currentSecPropValues.selectedComponent);
								compPropVal_m.put("section",section);

								//getting the column number
								int columnNum = (Integer) ((Text)(event.getSource())).getData("colId");

								//adding to the Map
								compPropVal_m.put(currentSecPropValues.propNameValuepair[columnNum].propName,((Text) (event.getSource())).getText());

							}
						});

						if(pml2pam)
						{
							if(matlDetails2loaded)
								text.setEnabled(false);

							resetcartoncontrols(currForm,item);

							if(! pamform.containsKey(propname))
								pamform.put(propname,text);
						}
						else
							text.setEnabled(true);
						break;
					case 3:

						text = new Text(tcTable, SWT.NONE);
						item.setData(propname, (Text) text);
						item.setData("savemap", (HashMap) compPropVal_m);
						if(PropValue!=null )
							text.setText(PropValue);
						editor.grabHorizontal = true;
						editor.setEditor(text,item, ColumnNumber);
						// Added as part of CR#21
						editor.getEditor().setEnabled(enable_editor);
						text.setData("colId", ColumnNumber);
						text.setData("section", section);
						text.addModifyListener(new ModifyListener() {
							public void modifyText(ModifyEvent event) {
								PAMSecondaryPropValue currentSecPropValues = tableConfigValues[tcTable.indexOf(item)] ;
								compPropVal_m.put("puid",currentSecPropValues.selectedComponent);
								String section = (String) ((Text)(event.getSource())).getData("section");

								if (section!=null)
									compPropVal_m.put("section",section);
								int columnNum = (Integer) ((Text)(event.getSource())).getData("colId");
								compPropVal_m.put(currentSecPropValues.propNameValuepair[columnNum].propName,((Text) (event.getSource())).getText());
							}
						});

						if(pml2pam)
						{
							if(matlDetails2loaded)
								text.setEnabled(false);
							resetcartoncontrols(currForm,item);
							if(! pamform.containsKey(propname))
								pamform.put(propname,text);
						}
						else
							text.setEnabled(true);

						break;
					case 4:
						text = new Text(tcTable, SWT.NONE);
						item.setData(propname, (Text) text);
						item.setData("savemap", (HashMap) compPropVal_m);
						if(PropValue!=null )
							text.setText(PropValue);
						editor.grabHorizontal = true;
						editor.setEditor(text,item, ColumnNumber);
						// Added as part of CR#21
						editor.getEditor().setEnabled(enable_editor);
						text.setData("colId", ColumnNumber);
						text.addModifyListener(new ModifyListener() {
							public void modifyText(ModifyEvent event) {
								//getting the row index
								PAMSecondaryPropValue currentSecPropValues = tableConfigValues[tcTable.indexOf(item)] ;
								compPropVal_m.put("puid",currentSecPropValues.selectedComponent);
								compPropVal_m.put("section",section);
								//getting the column number
								int columnNum = (Integer) ((Text)(event.getSource())).getData("colId");

								//adding to the Map
								compPropVal_m.put(currentSecPropValues.propNameValuepair[columnNum].propName,((Text) (event.getSource())).getText());
							}
						});
						if(pml2pam)
						{
							if(matlDetails2loaded)
								text.setEnabled(false);
							resetcartoncontrols(currForm,item);
							if(! pamform.containsKey(propname))
								pamform.put(propname,text);
						}
						else
							text.setEnabled(true);
						break;
					case 5:
						text = new Text(tcTable, SWT.NONE);
						item.setData("savemap", (HashMap) compPropVal_m);
						item.setData(propname, (Text) text);
						if(PropValue!=null )
							text.setText(PropValue);
						editor.grabHorizontal = true;
						editor.setEditor(text,item, ColumnNumber);
						// Added as part of CR#21
						editor.getEditor().setEnabled(enable_editor);
						text.setData("colId", ColumnNumber);
						text.setData("section", section);
						text.addModifyListener(new ModifyListener() {
							public void modifyText(ModifyEvent event) {
								PAMSecondaryPropValue currentSecPropValues = tableConfigValues[tcTable.indexOf(item)] ;
								compPropVal_m.put("puid",currentSecPropValues.selectedComponent);
								String section = (String) ((Text)(event.getSource())).getData("section");

								if (section!=null)
									compPropVal_m.put("section",section);
								int columnNum = (Integer) ((Text)(event.getSource())).getData("colId");
								compPropVal_m.put(currentSecPropValues.propNameValuepair[columnNum].propName,((Text) (event.getSource())).getText());
							}
						});
						if(pml2pam)
						{
							if(matlDetails2loaded)
								text.setEnabled(false);
							resetcartoncontrols(currForm,item);
							if(! pamform.containsKey(propname))
								pamform.put(propname,text);
						}
						else
							text.setEnabled(true);
						break;

					case 6:
						Button checkButton = new Button(tcTable, SWT.CHECK|SWT.CENTER);
						checkButton.setData("colId", ColumnNumber);
						if(PropValue.equalsIgnoreCase("True"))
							checkButton.setSelection(true);
						editor.grabHorizontal = true;
						editor.setEditor(checkButton,item,ColumnNumber);
						// Added as part of CR#21
						editor.getEditor().setEnabled(enable_editor);

						checkButton.addSelectionListener(new SelectionAdapter() {
							public void widgetSelected(SelectionEvent event) {
								PAMSecondaryPropValue currentSecPropValues = tableConfigValues[tcTable.indexOf(item)] ;
								compPropVal_m.put("puid",currentSecPropValues.selectedComponent);
								compPropVal_m.put("section",section);
								int columnNum = (Integer) ((Button)(event.getSource())).getData("colId");
								String isTrue= (((Button) (event.getSource())).getSelection()==true)?"True":"False";
								compPropVal_m.put(currentSecPropValues.propNameValuepair[columnNum].propName,isTrue);
							}
						});
						if(pml2pam)
						{
							if(matlDetails2loaded)
								checkButton.setEnabled(false);

							if(! pamform.containsKey(propname))
								pamform.put(propname,checkButton);
						}
						break;
					case 7:
						break;
					case 8:

						int textLength = textLimit.get(propname).intValue();

						if(propname.equalsIgnoreCase("u4_details"))
						{
							conditionDescriptionText = new Label(tcTable, SWT.WRAP | SWT.BORDER_SOLID);
							if(PropValue !=null  && !("".equals(PropValue)) )
							{
								conditionDescriptionText.setAlignment(SWT.CENTER);
								conditionDescriptionText.setFont( new Font(Display.getDefault(),"Arial", 8, SWT.BOLD ) );
								conditionDescriptionText.setText(". . .");
							}
							else
							{
								conditionDescriptionText.setText("");
							}

							editor.grabHorizontal = true;
							editor.setEditor(conditionDescriptionText, item, ColumnNumber);

							//Added as part of CR#21
							//editor.getEditor().setEnabled(enable_editor);
							conditionDescriptionText.setData("colId", ColumnNumber);
							conditionDescriptionText.setEnabled(false);

							if(pml2pam)
							{
								if(matlDetails2loaded)
									conditionDescriptionText.setEnabled(false);

								if(! pamform.containsKey(propname))
									pamform.put(propname,conditionDescriptionText);
							}

						}
						final PAMSecondaryPropValue currentSecPropValues = tableConfigValues[tcTable.indexOf(item)] ;
						if(propname.equalsIgnoreCase("u4_note"))
						{
							final Vector<String> noteValues = new Vector<String>();
							final HashMap<Integer,String> noteVals_m = new HashMap<Integer,String>();

							final Button  noteButton = new Button(tcTable,SWT.BUTTON1);

							if(pml2pam)
							{
								if(! pamform.containsKey(propname))
									pamform.put(propname,noteButton);
							}


							if(noteValues.size()>tcTable.indexOf(item))
								noteValue	= noteValues.get(tcTable.indexOf(item));

							else if(currentSecPropValues.getRequiredPAMPropertyValue("u4_note") !=null)
								noteValue = (String) currentSecPropValues.getRequiredPAMPropertyValue("u4_note");

							if(!noteValue.equalsIgnoreCase(""))
								noteButton.setText("...");

							noteButton.setData("colId", ColumnNumber);
							noteButton.pack(true);

							final TableEditor editor1 = new TableEditor(tcTable);
							editor1.grabHorizontal = true;
							editor1.setEditor(noteButton, item, ColumnNumber);
							// Added as part of CR#21
							editor1.getEditor().setEnabled(true);
							
							if ((isCheckOut == true) || ((isCheckOut == false) && (noteValue.isEmpty()==false))  )
							{
								noteButton.addSelectionListener(new SelectionAdapter() {
									public void widgetSelected(SelectionEvent event) {
										if(noteVals_m.get(tcTable.indexOf(item))!=null)
											noteValue	= noteVals_m.get(tcTable.indexOf(item));

										else if( currentSecPropValues.getRequiredPAMPropertyValue("u4_note") !=null)
											noteValue = currentSecPropValues.getRequiredPAMPropertyValue("u4_note");
										else
											noteValue = "";

										if(noteValue!=null && !(noteValue.equalsIgnoreCase("")))
											noteButton.setText("...");

										Display display = Display.getDefault();

										boolean pml2pamllbllit = false ;
										String name = getObjectName(currForm);
										boolean pmlvalue = false ;
										boolean typeselected = false ;

										CTabFolder tab1 = getTab(parentComposite) ;
										CTabItem cptab = null ;
										CTabItem[] tabitems = null ;

										if(tab1 != null)
											tabitems = tab1.getItems();

										if(tabitems != null)
											for(int tab = 0 ;tab < tabitems.length ;tab++)
											{
												if(tabitems[tab].getText().startsWith("Material"))
													cptab = tabitems[tab];
											}


										if ( cptab != null  && cptab.getData("lbltype") != null )
										{
											pmlvalue = (boolean) cptab.getData("lbltype");
											typeselected = true ;
										}
										if( PML2PAMCPMap != null && PML2PAMCPMap.containsValue(name) && formType.equals(CPFORM) && labelLit == true)
										{
											if(typeselected == true )
												pml2pamllbllit = pmlvalue ;
											else
												pml2pamllbllit = verifyMaterialDetails2Loaded();
										}


										// Added as part of CR#21
										NoteDialog noteDlg = new NoteDialog(display,noteValue,pamSpecRevision,pml2pamllbllit , (TCComponentForm) currentSecPropValues.selectedComponent);
										noteDlg.open();
										noteDlg.layout();
										while (!noteDlg.isDisposed()) {
											if (!display.readAndDispatch()) {
												display.sleep();
											}
										}

										if(noteDlg.isOKPressed())
										{
											noteVals_m.put(tcTable.indexOf(item), noteDlg.getNote());
											noteValue = noteDlg.getNote();

											if(!noteValue.equalsIgnoreCase(""))
												noteButton.setText("...");
											else
												noteButton.setText("");

											compPropVal_m.put("puid",currentSecPropValues.selectedComponent);
											compPropVal_m.put("section",section);


											//adding to the Map
											compPropVal_m.put("u4_note",noteValue);

											noteButton.setData("Note",noteDlg.getNote());
										}
										if(noteDlg.isClearPressed() && noteDlg.isOKPressed())
										{
											noteButton.setText("");
										}

									}
								});
							}
						}

						TCComponentListOfValues currLOVValues = null;
						ListOfValuesInfo tempLOVInfo;
						Object[] lovValues = null;
						currLOVValues = currTCProp.getLOV();

						if(( currLOVValues != null) || (propname.equalsIgnoreCase("u4_method")|| propname.equalsIgnoreCase("u4_details")))
						{
							String[] lovStrings = null;
							if(propname.equalsIgnoreCase("u4_type") && formType.equalsIgnoreCase(MATLPROPFORM) && CARTON.equalsIgnoreCase(pamSecType))
							{
								final CCombo  combo = new CCombo(tcTable,SWT.NONE);
								combo.setData("colId", ColumnNumber);
								combo.setData("item", item);
								combo.setData("name", propname);
								try
								{
									if(currForm.getStringProperty(PROP).equals(BOARDGRADE) && pamSpecRevision.isCheckedOut() == true)
									{
										Hashtable pnbObjects = tableObjects.get(MATLPROPFORM);
										ArrayList pnbnames = new ArrayList<String>();
										pnbnames.add("");
										if(pnbObjects != null)
										{
											Enumeration names = pnbObjects.keys();
											while(names.hasMoreElements())
											{
												TCComponent comp = (TCComponent) pnbObjects.get((String) names.nextElement());
												try
												{
													if(comp.getStringProperty("u4_material_type").equals(CartonPNBTypeMap.get(typecarbon)))
														pnbnames.add(comp.getStringProperty("object_name"));
												}
												catch (TCException e)
												{
													e.printStackTrace();
												}
											}
										}

										if(PropValue.length() > 0 )
											pml2pam = true ;
										else
											pml2pam = false ;

										combo.setItems((String[]) pnbnames.toArray(new String[pnbnames.size()]));

										if(currForm.getStringProperty(PROP).equals(BOARDGRADE) && currForm.getStringProperty(ATTR).equals(BGATTR) &&  pamSpecRevision.isCheckedOut() == true)
										{
											if(PropValue.length() > 0)
												matlDetails2 = matlDetails2loaded = true ;
											else
												matlDetails2 = matlDetails2loaded = false ;
										}
									}
									else
									{
										String [] coating_lovvalues = (String []) tcTable.getData("coating_lovvalues");
										combo.setItems(coating_lovvalues);
									}
								}catch (TCException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}

								if(PropValue!=null )
									combo.setText(PropValue);

								editor.grabHorizontal = true;
								editor.setEditor(combo, item, ColumnNumber);
								editor.getEditor().setEnabled(enable_editor);

								try
								{
									if( currForm.getStringProperty(PROP).equals(BOARDGRADE))
										BoardCombos.add(combo);
								}catch (TCException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}

								combo.addSelectionListener(new SelectionAdapter() {
									public void widgetSelected(SelectionEvent event) {
										PAMSecondaryPropValue currentSecPropValues = tableConfigValues[tcTable.indexOf(item)] ;
										compPropVal_m.put("puid",currentSecPropValues.selectedComponent);
										compPropVal_m.put("section",section);
										int columnNum = (Integer) ((CCombo)(event.getSource())).getData("colId");
										String currentValue = ((CCombo) (event.getSource())).getText() ;
										compPropVal_m.put(currentSecPropValues.propNameValuepair[columnNum].propName,currentValue);

										try
										{
											Text txtTN = null ;
											Text txtValue = null ;
											TCComponent comp  = null ;
											if (currForm.getStringProperty(PROP).equals(BOARDGRADE) )
											{
												TableItem item  = (TableItem) combo.getData("item");
												if(item != null)
												{
													txtTN = (Text) item.getData(TNAME);
													txtValue = (Text) item.getData(TNVALUE);
												}
												Hashtable pnbObjects = tableObjects.get(MATLPROPFORM);
												if(pnbObjects != null && currentValue.length() > 0)
													comp =  (TCComponent) pnbObjects.get(currentValue);
											}						
											if(currForm.getStringProperty(PROP).equals(BOARDGRADE) && currForm.getStringProperty(ATTR).equals(BGATTR))
											{
												populateTradeNameValues(comp , txtTN , txtValue);
												CTabFolder tab1 = getTab(parentComposite) ;
												CTabItem cptab = null ;
												CTabItem[] tabitems = null ;
												if(tab1 != null)
													tabitems = tab1.getItems();

												if(tabitems != null)
													for(int tab = 0 ;tab < tabitems.length ;tab++)
													{
														if(tabitems[tab].getText().startsWith("Material"))
															cptab = tabitems[tab];
													}

												if(cptab != null )
													cptab.setData("lbltype", false);

												if(	comp != null )
												{
													cartonloaded = true ;
													matlDetails2 = true ;
													matlDetails2loaded = true ;

													if(cptab != null )
														cptab.setData("lbltype", true);

													if( tab1.getSelection().getText().startsWith("Material"))
													{
														tab1.getSelection().setData("pml", comp);
														tab1.getSelection().setData("activepml", comp);
													}	
													populateLayerValues(comp,PNBLAYERFORM);
													PML2PAMCPMapAttrValues((TCComponentItemRevision)comp,pamforms,cptab);
												}
												else
												{
													if( cartonloaded || matlDetails2)
														resetLayerValues(false,true);

													cartonloaded = false ;
													matlDetails2 = false ;
													matlDetails2loaded = false ;

													if( tab1.getSelection().getText().startsWith("Material"))
													{
														tab1.getSelection().setData("pml", null);
														tab1.getSelection().setData("activepml", null);
													}

													PML2PAMCPMapAttrValuesClear(pamforms,cptab);
												}
											}
											else if (currForm.getStringProperty(PROP).equals(BOARDGRADE) )
											{
												populateTradeNameValues(comp , txtTN , txtValue);
											}
										}catch (TCException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
									}
								});
							}
							else if(propname.equalsIgnoreCase("u4_method"))
							{
								String propertyValue =  currentSecPropValues.getRequiredPAMPropertyValue("u4_property");
								final String temppropertyValue = propertyValue;
								final Vector<String> conditionValues = new Vector<String>();
								final HashMap<Integer,String> conditionVals_m = new HashMap<Integer,String>();
								final Vector<String> methodValues = new Vector<String>();
								final HashMap<Integer,String> methodVals_m = new HashMap<Integer,String>();

								final Button  methodButton = new Button(tcTable,SWT.BUTTON1);
								methodButton.setData("colId", ColumnNumber);
								methodButton.pack(true);
								methodButton.pack();

								if(methodValues.size()>tcTable.indexOf(item))
									methodValue	= methodValues.get(tcTable.indexOf(item));
								else if( currentSecPropValues.getRequiredPAMPropertyValue("u4_method") !=null)
									methodValue = currentSecPropValues.getRequiredPAMPropertyValue("u4_method");

								if(conditionValues.size()>tcTable.indexOf(item))
									conditionValue	= conditionValues.get(tcTable.indexOf(item));
								else if( currentSecPropValues.getRequiredPAMPropertyValue("u4_details") !=null)
									conditionValue = currentSecPropValues.getRequiredPAMPropertyValue("u4_details");

								if(PropValue !=null  && !("".equals(PropValue)) )
									methodButton.setText(PropValue);

								final TableEditor editor1 = new TableEditor(tcTable);
								editor1.grabHorizontal = true;
								editor1.setEditor(methodButton, item, ColumnNumber);

								if ( (isCheckOut == true) || ((isCheckOut == false) && (methodValue.isEmpty()==false)) )
								{
									methodButton.addSelectionListener(new SelectionAdapter() {
										public void widgetSelected(SelectionEvent event) {

											if(methodVals_m.get(tcTable.indexOf(item))!=null)
												methodValue	= methodVals_m.get(tcTable.indexOf(item));
											else if( currentSecPropValues.getRequiredPAMPropertyValue("u4_method") !=null)
												methodValue = currentSecPropValues.getRequiredPAMPropertyValue("u4_method");

											if(conditionVals_m.get(tcTable.indexOf(item))!=null)
												conditionValue	= conditionVals_m.get(tcTable.indexOf(item));
											else if( currentSecPropValues.getRequiredPAMPropertyValue("u4_details") !=null)
												conditionValue = currentSecPropValues.getRequiredPAMPropertyValue("u4_details");

											boolean pmlvalue = false ;
											boolean typeselected = false ;
											boolean pml2pamllbllit = false ;
											String name = getObjectName(currForm);

											CTabFolder tab1 = getTab(parentComposite) ;
											CTabItem cptab = null ;
											CTabItem[] tabitems = null ;
											if(tab1 != null)
												tabitems = tab1.getItems();

											if(tabitems != null)
												for(int tab = 0 ;tab < tabitems.length ;tab++)
												{
													if(tabitems[tab].getText().startsWith("Material"))
														cptab = tabitems[tab];
												}

											if ( cptab != null  && cptab.getData("lbltype") != null )
											{
												pmlvalue = (boolean) cptab.getData("lbltype");
												typeselected = true ;
											}

											if( PML2PAMCPMap != null && PML2PAMCPMap.containsValue(name) && formType.equals(CPFORM) && labelLit == true)
											{
												if(typeselected == true )
													pml2pamllbllit = pmlvalue ;
												else
													pml2pamllbllit = verifyMaterialDetails2Loaded();

												if(pml2pamllbllit)
													try
												{
														methodValue = currForm.getStringProperty("u4_method");
														conditionValue = currForm.getStringProperty("u4_details");
												}
												catch (TCException e1)
												{
													// TODO Auto-generated catch block
													e1.printStackTrace();
												}
											}

											PAMSecondaryPropValue currentSecPropValues = tableConfigValues[tcTable.indexOf(item)] ;

											String sectionAndPropName = currentSecPropValues.selectedComponent.getDisplayType() + " : " + currentSecPropValues.getRequiredPAMPropertyValue("u4_property");
											CustomMethodDialog methodDlg = new CustomMethodDialog(AIFDesktop.getActiveDesktop().getShell(),
													session,currentSecPropValues,pamSpecRevision,sectionAndPropName,conditionValue, methodValue  ,pml2pamllbllit,temppropertyValue , attrTMvalues , TMData);

											methodDlg.open();

											if( !pml2pamllbllit  &&  pamSpecRevision.isCheckedOut() )
											{
												methodValue = methodDlg.getMethod();
												conditionValue = methodDlg.getConditionSet();
												//strConditionNumber = methodDlg.getConditionNumber();
												//<Jayateertha: 17-Jun-2015> Fix for Defect# 106 - Test Method value should save blank values as well.
												if(	methodValue != null ) //&& !("".equals(methodValue)) )
												{
													methodVals_m.put(tcTable.indexOf(item), methodValue);

													compPropVal_m.put("puid",currentSecPropValues.selectedComponent);
													compPropVal_m.put("section",section);
													//adding to the Map
													compPropVal_m.put("u4_method",methodValue);
													methodButton.setText(methodValue);

													if(conditionValue != null ) // && !("".equals(conditionValue)))
													{
														conditionVals_m.put(tcTable.indexOf(item), conditionValue);
														compPropVal_m.put("puid",currentSecPropValues.selectedComponent);
														compPropVal_m.put("section",section);
														compPropVal_m.put("u4_details",conditionValue);
														conditionDescriptionText.setAlignment(SWT.CENTER);
													}
												}
											}
										}
									});
								}

								if(pml2pam)
								{
									if(! pamform.containsKey(propname))
										pamform.put(propname,methodButton);
								}
							}
							else if(propname.equalsIgnoreCase("u4_details"))
							{
								if(conditionValue != null && !("".equals(conditionValue)))
								{
									compPropVal_m.put("puid",currentSecPropValues.selectedComponent);
									compPropVal_m.put("section",section);
									compPropVal_m.put("u4_details",conditionValue);
									conditionDescriptionText.setAlignment(SWT.CENTER);
								}
							}
							else if( isInterdependantLov(currTCProp))
							{
								if (formType.equals("U4_ApprvdAdditiveDtlForm") && propname.equals("u4_where_add"))
								{
									if (isCheckOut == true)
									{
										ArrayList<String> second_level_lov = apprvdadditivedtl_lov.get(item.getText( ));
										
										if (second_level_lov!=null)
											lovValues=second_level_lov.toArray(new Object[second_level_lov.size()]);
									}
								}
								else
								{
									//LOVService mLOVService = LOVService.getService( session );
									InitialLovData mInitData = new InitialLovData( );
									mInitData.propertyName = currTCProp.getPropertyName();
									mInitData.lovInput.owningObject=currTCProp.getTCComponent();
									mInitData.lov = currLOVValues;
									LOVSearchResults mInitDataResponse = lovServ.getInitialLOVValues( mInitData );
									for( LOVValueRow LOVValue : mInitDataResponse.lovValues )
									{
										String mValue = ((String[])LOVValue.propDisplayValues.get( "lov_values" ))[0];

										if( mValue.equalsIgnoreCase( item.getText( )))
										{
											lovValues = new String[LOVValue.childRows.length];
											for( int index = 0;index < LOVValue.childRows.length;index++ )
												lovValues[index] = ((String[])LOVValue.childRows[index].propDisplayValues.get( "lov_values" ))[0];
										}
										if(propname.equalsIgnoreCase("u4_material")&& mValue.equalsIgnoreCase(  currentSecPropValues.getRequiredPAMPropertyValue("u4_layer")))
										{
											lovValues = new String[LOVValue.childRows.length];
											for( int index = 0;index < LOVValue.childRows.length;index++ )
												lovValues[index] = ((String[])LOVValue.childRows[index].propDisplayValues.get( "lov_values" ))[0];

										}
									}
								}
								/**
								 * 28-Sept-2014: Added following check for "lovValues" as Materials Tab was not getting populated due to one of the property
								 * "u4_type" on U4_ApprvdAdditiveDtlForm is causing NULL assuming its an LOV. Hence, below check is introduced.
								 */
								if (lovValues != null)
								{
									// Enhancement to provide empty dropdown select at the top of the list
									lovStrings = new String[lovValues.length + 1];
									lovStrings[0] = "";
									System.arraycopy(lovValues, 0, lovStrings, 1, lovValues.length);

									if(lovValues !=null)
									{
										CCombo  combo = new CCombo(tcTable, SWT.DROP_DOWN | SWT.READ_ONLY);
										combo.setData("colId", ColumnNumber);

										if (isCheckOut == true)//setItems was taking 40-90 milli seconds, hence we will set the items only when in check out state
											combo.setItems( lovStrings);
										//combo.setItem( lovStrings.length,"");
										if(PropValue!=null )
											combo.setText(PropValue);

										editor.grabHorizontal = true;
										editor.setEditor(combo, item, ColumnNumber);
										editor.getEditor().setEnabled(enable_editor);
										combo.addSelectionListener(new SelectionAdapter() {
											public void widgetSelected(SelectionEvent event) {
												PAMSecondaryPropValue currentSecPropValues = tableConfigValues[tcTable.indexOf(item)] ;
												compPropVal_m.put("puid",currentSecPropValues.selectedComponent);
												compPropVal_m.put("section",section);
												int columnNum = (Integer) ((CCombo)(event.getSource())).getData("colId");
												compPropVal_m.put(currentSecPropValues.propNameValuepair[columnNum].propName,((CCombo) (event.getSource())).getText());
											}
										});

										if(pml2pam)
										{
											if(matlDetails2loaded)
												combo.setEnabled(false);
											if(! pamform.containsKey(propname))
												pamform.put(propname,combo);
										}
									}
								}
								//TODO:: ADDED for CR# 131
								else if( ( formType.equals("U4_CanMEMDetailsForm") ||  formType.equals("U4_BodyMatDetailsForm") || formType.equals("U4_PackageDetailsForm") || (formType.equals("U4_MaterialDetailsForm"))) && propname.equals("u4_type") )
								{
									CCombo  combo2 = new CCombo(tcTable,SWT.DROP_DOWN | SWT.READ_ONLY);
									combo2.setData("colId", ColumnNumber);
									// TODO:: Defect#1290
									combo2.setItems(new String[]{"","-"});
									// TODO :: Defect#1300
									if(PropValue != null && PropValue.length() > 0)
										combo2.setText("-");
									else
										combo2.setText("");
									editor.grabHorizontal = true;
									editor.setEditor(combo2,item,ColumnNumber);
									// Added as part of CR#21
									editor.getEditor().setEnabled(enable_editor);

									combo2.addSelectionListener(new SelectionAdapter() {
										public void widgetSelected(SelectionEvent event) {
											PAMSecondaryPropValue currentSecPropValues = tableConfigValues[tcTable.indexOf(item)] ;
											compPropVal_m.put("puid",currentSecPropValues.selectedComponent);
											compPropVal_m.put("section",section);
											int columnNum = (Integer) ((CCombo)(event.getSource())).getData("colId");
											compPropVal_m.put(currentSecPropValues.propNameValuepair[columnNum].propName,((CCombo) (event.getSource())).getText());
										}
									});

									if(pml2pam)
									{
										if(matlDetails2loaded)
											text.setEnabled(false);

										if(! pamform.containsKey(propname)) pamform.put(propname,text);
									}

								}
								else if( ( formType.equals("U4_BristleDetailsForm") ||
										formType.equals("U4_ApprvdAnchWreMatDtlForm") ||
										formType.equals("U4_ApprvdHFMDetailsForm") ||
										formType.equals("U4_ApprvdBstlMatDtlForm") )
										&& propname.equals("u4_type") )
								{

									CCombo  combo2 = new CCombo(tcTable,SWT.DROP_DOWN | SWT.READ_ONLY);
									combo2.setData("colId", ColumnNumber);
									combo2.setItems(new String[]{""});
									if(PropValue != null)
										combo2.setText(PropValue);
									editor.grabHorizontal = true;
									editor.setEditor(combo2,item,ColumnNumber);
									// Added as part of CR#21
									editor.getEditor().setEnabled(enable_editor);

									combo2.addSelectionListener(new SelectionAdapter() {
										public void widgetSelected(SelectionEvent event) {
											PAMSecondaryPropValue currentSecPropValues = tableConfigValues[tcTable.indexOf(item)] ;
											compPropVal_m.put("puid",currentSecPropValues.selectedComponent);
											compPropVal_m.put("section",section);
											int columnNum = (Integer) ((CCombo)(event.getSource())).getData("colId");
											compPropVal_m.put(currentSecPropValues.propNameValuepair[columnNum].propName,((CCombo) (event.getSource())).getText());
										}
									});

									if(pml2pam)
									{
										if(matlDetails2loaded)
											text.setEnabled(false);

										if(! pamform.containsKey(propname)) pamform.put(propname,text);
									}
								}
								else
								{
									text = new Text(tcTable, SWT.NONE);
									text.setTextLimit(textLength);
									if(PropValue!=null)
										text.setText(PropValue);
									final TableEditor editorNonLOv = new TableEditor(tcTable);
									editorNonLOv.grabHorizontal = true;
									editorNonLOv.setEditor(text,item, ColumnNumber);
									// Added as part of CR#21
									editorNonLOv.getEditor().setEnabled(enable_editor);
									text.setData("colId", ColumnNumber);
									text.addModifyListener(new ModifyListener() {
										public void modifyText(ModifyEvent event) {
											PAMSecondaryPropValue currentSecPropValues = tableConfigValues[tcTable.indexOf(item)] ;
											compPropVal_m.put("puid",currentSecPropValues.selectedComponent);
											compPropVal_m.put("section",section);
											int columnNum = (Integer) ((Text)(event.getSource())).getData("colId");
											compPropVal_m.put(currentSecPropValues.propNameValuepair[columnNum].propName,((Text) (event.getSource())).getText());
										}
									});
									if(pml2pam)
									{
										if(matlDetails2loaded)
											text.setEnabled(false);

										resetcartoncontrols(currForm,item);

										if(! pamform.containsKey(propname))
											pamform.put(propname,text);
									}
									else
										text.setEnabled(true);
								}

							}
							else
							{
								try
								{
									tempLOVInfo = currLOVValues.getListOfValues();
									lovValues =  tempLOVInfo.getListOfValues();
								}
								catch (TCException e)
								{
									// TODO Auto-generated catch block
									e.printStackTrace();
								}

								// Enhancement to provide empty dropdown select at the top of the list
								lovStrings = new String[lovValues.length + 1];
								lovStrings[0] = "";
								System.arraycopy(lovValues, 0, lovStrings, 1, lovValues.length);

								if(lovValues !=null)
								{
									CCombo  combo = new CCombo(tcTable, SWT.DROP_DOWN | SWT.READ_ONLY);
									combo.setData("colId", ColumnNumber);

									//setItems was taking 40-90 milli seconds, hence we will set the items only when in check out state
									if (isCheckOut == true) 	combo.setItems( lovStrings);

									if(PropValue!=null )
										combo.setText(PropValue);

									editor.grabHorizontal = true;
									editor.setEditor(combo, item, ColumnNumber);
									editor.getEditor().setEnabled(enable_editor);

									combo.addSelectionListener(new SelectionAdapter() {
										public void widgetSelected(SelectionEvent event) {
											PAMSecondaryPropValue currentSecPropValues = tableConfigValues[tcTable.indexOf(item)] ;
											compPropVal_m.put("puid",currentSecPropValues.selectedComponent);
											compPropVal_m.put("section",section);
											//getting the column number
											int columnNum = (Integer) ((CCombo)(event.getSource())).getData("colId");
											compPropVal_m.put(currentSecPropValues.propNameValuepair[columnNum].propName,((CCombo) (event.getSource())).getText());
										}
									});

									if(pml2pam)
									{
										if(matlDetails2loaded)
											combo.setEnabled(false);
										if(! pamform.containsKey(propname))  pamform.put(propname,combo);
									}
								}
								else if( ( formType.equals("U4_CanMEMDetailsForm") ||  formType.equals("U4_BodyMatDetailsForm") || formType.equals("U4_PackageDetailsForm") || (formType.equals("U4_MaterialDetailsForm"))) && propname.equals("u4_type") )
								{
									CCombo  combo2 = new CCombo(tcTable,SWT.DROP_DOWN | SWT.READ_ONLY);
									combo2.setData("colId", ColumnNumber);
									combo2.setItems(new String[]{"-"});
									if(PropValue != null && "-".equals(PropValue))
										combo2.setText(PropValue);
									editor.grabHorizontal = true;
									editor.setEditor(combo2,item,ColumnNumber);
									// Added as part of CR#21
									editor.getEditor().setEnabled(enable_editor);

									combo2.addSelectionListener(new SelectionAdapter() {
										public void widgetSelected(SelectionEvent event) {
											PAMSecondaryPropValue currentSecPropValues = tableConfigValues[tcTable.indexOf(item)] ;
											compPropVal_m.put("puid",currentSecPropValues.selectedComponent);
											compPropVal_m.put("section",section);
											int columnNum = (Integer) ((CCombo)(event.getSource())).getData("colId");
											compPropVal_m.put(currentSecPropValues.propNameValuepair[columnNum].propName,((CCombo) (event.getSource())).getText());
										}
									});

									if(pml2pam)
									{
										if(matlDetails2loaded)
											text.setEnabled(false);

										if(! pamform.containsKey(propname)) pamform.put(propname,text);
									}

								}
							}
						}//if((( currLOVValues = currTCProp.getLOV() )!= null) || (propname.equalsIgnoreCase("u4_method")|| propname.equalsIgnoreCase("u4_details"))) ends
						else if( ( formType.equals("U4_CanMEMDetailsForm") ||  formType.equals("U4_BodyMatDetailsForm") || formType.equals("U4_PackageDetailsForm") || (formType.equals("U4_MaterialDetailsForm"))) && propname.equals("u4_type") )
						{
							CCombo  combo2 = new CCombo(tcTable,SWT.DROP_DOWN | SWT.READ_ONLY);
							combo2.setData("colId", ColumnNumber);
							combo2.setItems(new String[]{"-"});
							if(PropValue != null && "-".equals(PropValue))
								combo2.setText(PropValue);
							editor.grabHorizontal = true;
							editor.setEditor(combo2,item,ColumnNumber);
							// Added as part of CR#21
							editor.getEditor().setEnabled(enable_editor);

							combo2.addSelectionListener(new SelectionAdapter() {
								public void widgetSelected(SelectionEvent event) {
									PAMSecondaryPropValue currentSecPropValues = tableConfigValues[tcTable.indexOf(item)] ;
									compPropVal_m.put("puid",currentSecPropValues.selectedComponent);
									compPropVal_m.put("section",section);
									int columnNum = (Integer) ((CCombo)(event.getSource())).getData("colId");
									compPropVal_m.put(currentSecPropValues.propNameValuepair[columnNum].propName,((CCombo) (event.getSource())).getText());
								}
							});
							if(pml2pam)
							{

								if(matlDetails2loaded)
									text.setEnabled(false);

								if(! pamform.containsKey(propname)) pamform.put(propname,text);
							}


						}
						else if( ( formType.equals("U4_BristleDetailsForm") ||
								formType.equals("U4_ApprvdAnchWreMatDtlForm") ||
								formType.equals("U4_ApprvdHFMDetailsForm") ||
								formType.equals("U4_ApprvdBstlMatDtlForm") )
								&& propname.equals("u4_type") )
						{

							CCombo  combo2 = new CCombo(tcTable,SWT.DROP_DOWN | SWT.READ_ONLY);
							combo2.setData("colId", ColumnNumber);
							combo2.setItems(new String[]{""});
							if(PropValue != null)
								combo2.setText(PropValue);
							editor.grabHorizontal = true;
							editor.setEditor(combo2,item,ColumnNumber);
							// Added as part of CR#21
							editor.getEditor().setEnabled(enable_editor);

							combo2.addSelectionListener(new SelectionAdapter() {
								public void widgetSelected(SelectionEvent event) {
									PAMSecondaryPropValue currentSecPropValues = tableConfigValues[tcTable.indexOf(item)] ;
									compPropVal_m.put("puid",currentSecPropValues.selectedComponent);
									compPropVal_m.put("section",section);
									int columnNum = (Integer) ((CCombo)(event.getSource())).getData("colId");
									compPropVal_m.put(currentSecPropValues.propNameValuepair[columnNum].propName,((CCombo) (event.getSource())).getText());
								}
							});

							if(pml2pam)
							{
								if(matlDetails2loaded)
									text.setEnabled(false);

								if(! pamform.containsKey(propname)) pamform.put(propname,text);
							}
						}
						else
						{
							text = new Text(tcTable, SWT.NONE);
							text.setTextLimit(textLength);
							item.setData(currentSecPropValues.propNameValuepair[ColumnNumber].propName, (Text) text);
							item.setData("savemap", (HashMap) compPropVal_m);
							if(PropValue!=null)
								text.setText(PropValue);
							editor.grabHorizontal = true;
							editor.setEditor(text,item, ColumnNumber);
							// Added as part of CR#21
							editor.getEditor().setEnabled(enable_editor);
							text.setData("colId", ColumnNumber);
							text.addModifyListener(new ModifyListener() {
								public void modifyText(ModifyEvent event) {
									PAMSecondaryPropValue currentSecPropValues = tableConfigValues[tcTable.indexOf(item)] ;
									compPropVal_m.put("puid",currentSecPropValues.selectedComponent);
									compPropVal_m.put("section",section);
									int columnNum = (Integer) ((Text)(event.getSource())).getData("colId");
									compPropVal_m.put(currentSecPropValues.propNameValuepair[columnNum].propName,((Text) (event.getSource())).getText());
								}
							});

							if(pml2pam)
							{
								if(matlDetails2loaded)
									text.setEnabled(false);
								resetcartoncontrols(currForm,item);
								if(! pamform.containsKey(propname))
									pamform.put(propname,text);
							}
							else
								text.setEnabled(true);

						}

						break;

					default:
						text = new Text(tcTable, SWT.NONE);
						text.setText(PropValue);
						editor.grabHorizontal = true;
						editor.setEditor(text,item, ColumnNumber);
						// Added as part of CR#21
						editor.getEditor().setEnabled(enable_editor);
						text.setData("colId", ColumnNumber);
						text.addModifyListener(new ModifyListener() {
							public void modifyText(ModifyEvent event) {
								PAMSecondaryPropValue currentSecPropValues = tableConfigValues[tcTable.indexOf(item)] ;
								compPropVal_m.put("puid",currentSecPropValues.selectedComponent);
								compPropVal_m.put("section",section);
								int columnNum = (Integer) ((Text)(event.getSource())).getData("colId");
								compPropVal_m.put(currentSecPropValues.propNameValuepair[columnNum].propName,((Text) (event.getSource())).getText());
							}
						});
						if(pml2pam)
						{
							if(matlDetails2loaded)
								text.setEnabled(false);

							if(! pamform.containsKey(propname))  pamform.put(propname,text);
						}
						break;
					}
				}

				modifiedPropVals_v.add(compPropVal_m);


				pamform.put("item", item);
				pamform.put("map", compPropVal_m);

				if(pml2pam)
				{
					CTabFolder tab1 = getTab(parentComposite) ;	
				    CTabItem[] tabitems = null ;
				    if(tab1 != null)
				    {
				    	tabitems = tab1.getItems();

				    	if (tabitems!=null)
				    	{
				    		for(int tab = 0 ;tab < tabitems.length ;tab++)
				    		{
				    			if(tabitems[tab].getText().startsWith("Component Prop"))
				    			{
				    				if (name!=null)
				    					tabitems[tab].setData(name,pamform);
				    			}
				    		}
				    	}
				    }
				}
			}
		});
	}

	/**
	 * @param tcTable
	 * @param tableConfigValues1
	 *  PML in PAM
	 */

	public void setTableConfiguration1(final Table tcTable, final PAMSecondaryPropValue[] tableConfigValues )
	{
		long cstartTime = System.currentTimeMillis();

		final LOVService mLOVService = lovServ;

		String[] polymertype_lovs = null;
		String[] manfpolymermb_lovs = null;
		LOVSearchResults materialdetailsLOVResult = null;

		String formType = tableConfigValues[0].secondaryName;

		if (isCheckOut == true)
		{
			if ( ( formType.equals(APFORM) || formType.equals(AP1FORM) ))
			{
				polymertype_lovs = prefServ.getStringValuesAtLocation(POLYMERTYPE_LOV, TCPreferenceLocation.OVERLAY_LOCATION);
				manfpolymermb_lovs = prefServ.getStringValuesAtLocation(MANULOV, TCPreferenceLocation.OVERLAY_LOCATION);
			}
			else if ( formType.equals(MATLFORM) )
			{
				TCComponentListOfValues Materialdetails_lov = TCComponentListOfValuesType.findLOVByName("U4_PL_MaterialDetailsLOV");

				if(Materialdetails_lov != null)
				{
					try
					{
						InitialLovData mInitData = new InitialLovData( );
						mInitData.lov = Materialdetails_lov;
						//mInitData.propertyName=currTCProp.getPropertyName();
						//mInitData.lovInput.owningObject=currTCProp.getTCComponent();
						mInitData.propertyName=tableConfigValues[0].propNameValuepair[0].propName;
						mInitData.lovInput.owningObject=tableConfigValues[0].selectedComponent;
						materialdetailsLOVResult = lovServ.getInitialLOVValues( mInitData );
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			}

			tcTable.setData("polymertype_lovs", polymertype_lovs);
			tcTable.setData("manfpolymermb", manfpolymermb_lovs);
			tcTable.setData("materialdetails_lovresult", materialdetailsLOVResult);
		}

		final Map<String,Integer > textLimit = new HashMap<String,Integer>(PAMConstant.getTextLimit (tableConfigValues[0]));

		final int nColumn  = tableConfigValues[0].propNameValuepair.length;

		for(  int ColumnNumber =0;ColumnNumber< tableConfigValues[0].propNameValuepair.length;ColumnNumber++ )
		{
			final TableColumn propertyColumn =  new TableColumn(tcTable, SWT.BORDER_SOLID|SWT.COLOR_WIDGET_NORMAL_SHADOW);
			propertyColumn.setWidth(tableConfigValues[0].propNameValuepair[ColumnNumber].columnSize);

			String nametext = tableConfigValues[0].propNameValuepair[ColumnNumber].propDisplayName;
			propertyColumn.setText(nametext);
			propertyColumn.setResizable(true);
		}

		tcTable.setItemCount(tableConfigValues.length);

		tcTable.addListener(SWT.SetData, new Listener() {
			public void handleEvent(Event event) {

				final TableItem item = (TableItem) event.item;
				int row = tcTable.indexOf(item);
				boolean legend = PAMConstant.ruleValidate((TCComponentForm) tableConfigValues[row].selectedComponent);
				final TCComponentForm currForm = (TCComponentForm) tableConfigValues[row].selectedComponent;
				final String formType = tableConfigValues[row].secondaryName;
				final Map<String,Object> compPropVal_m = new HashMap<String,Object>();

				final CCombo  cbType  = new CCombo(tcTable,SWT.DROP_DOWN | SWT.READ_ONLY);
				final CCombo  cbPML = new CCombo(tcTable,SWT.DROP_DOWN | SWT.READ_ONLY);
				final CCombo  cbManu = new CCombo(tcTable,SWT.DROP_DOWN | SWT.READ_ONLY);
				final Text txtGrade = new Text(tcTable, SWT.NONE);
				final Button  noteButton = new Button(tcTable,SWT.BUTTON1);

				for(  int ColumnNumber =0;ColumnNumber< tableConfigValues[row].propNameValuepair.length;ColumnNumber++ )
				{
					String typeValue  = null;

					String propname = tableConfigValues[row].propNameValuepair[ColumnNumber].propName;

					if(propname.equals("u4_type"))
						typeValue =  tableConfigValues[row].propNameValuepair[ColumnNumber].propValue;

					Text text = new Text(tcTable, SWT.NONE);

					String PropValue = tableConfigValues[row].propNameValuepair[ColumnNumber].propValue;
					int propType =  tableConfigValues[row].propNameValuepair[ColumnNumber].PropertyType;
					TCProperty currTCProp =  tableConfigValues[row].propNameValuepair[ColumnNumber].tcProperty;

					TableEditor editor = new TableEditor(tcTable);

					if(ColumnNumber == 0 && legend )
					{   
						TableEditor mandatroyEditor = new TableEditor(tcTable);
						PAMConstant.setMandatory(PropValue,mandatroyEditor,item);

					}											
					if(!(tableConfigValues[row].propNameValuepair[ColumnNumber].isEnabled))
					{
						item.setText(ColumnNumber,PropValue);
						continue;
					}
					if(isStructuredComponent(currForm)&&tableConfigValues[row].propNameValuepair[ColumnNumber].isStructured)
					{
						item.setText(ColumnNumber,PropValue);
						continue;
					}

					switch (propType)
					{
					case 1:
						text = new Text(tcTable, SWT.NONE);
						if(PropValue!=null )
							text.setText(PropValue);
						editor.grabHorizontal = true;
						editor.setEditor(text,item, ColumnNumber);
						// Added as part of CR#21
						editor.getEditor().setEnabled(enable_editor);
						text.setData("colId", ColumnNumber);
						text.addModifyListener(new ModifyListener() {     
							public void modifyText(ModifyEvent event) {
								PAMSecondaryPropValue currentSecPropValues = tableConfigValues[tcTable.indexOf(item)] ;
								compPropVal_m.put("puid",currentSecPropValues.selectedComponent);
								compPropVal_m.put("section",section);
								int columnNum = (Integer) ((Text)(event.getSource())).getData("colId");
								compPropVal_m.put(currentSecPropValues.propNameValuepair[columnNum].propName,((Text) (event.getSource())).getText());

							}      
						});
						break;
					case 2:
						text = new Text(tcTable, SWT.NONE);
						if(PropValue!=null )
							text.setText(PropValue);
						editor.grabHorizontal = true;
						editor.setEditor(text,item, ColumnNumber);
						// Added as part of CR#21
						editor.getEditor().setEnabled(enable_editor);
						text.setData("colId", ColumnNumber);
						text.addModifyListener(new ModifyListener() {     
							public void modifyText(ModifyEvent event) {
								PAMSecondaryPropValue currentSecPropValues = tableConfigValues[tcTable.indexOf(item)] ;
								compPropVal_m.put("puid",currentSecPropValues.selectedComponent);
								compPropVal_m.put("section",section);
								int columnNum = (Integer) ((Text)(event.getSource())).getData("colId");
								compPropVal_m.put(currentSecPropValues.propNameValuepair[columnNum].propName,((Text) (event.getSource())).getText());

							}      
						});
						break;
					case 3:

						text = new Text(tcTable, SWT.NONE);
						if(PropValue!=null )
							text.setText(PropValue);
						editor.grabHorizontal = true;
						editor.setEditor(text,item, ColumnNumber);
						// Added as part of CR#21
						editor.getEditor().setEnabled(enable_editor);
						text.setData("colId", ColumnNumber);
						text.setData("section", section);
						text.addModifyListener(new ModifyListener() {     
							public void modifyText(ModifyEvent event) {
								PAMSecondaryPropValue currentSecPropValues = tableConfigValues[tcTable.indexOf(item)] ;
								compPropVal_m.put("puid",currentSecPropValues.selectedComponent);
								String section = (String) ((Text)(event.getSource())).getData("section");
								if (section!=null)
									compPropVal_m.put("section",section);
								int columnNum = (Integer) ((Text)(event.getSource())).getData("colId");
								compPropVal_m.put(currentSecPropValues.propNameValuepair[columnNum].propName,((Text) (event.getSource())).getText());

							}      
						});

						break;
					case 4:
						text = new Text(tcTable, SWT.NONE);
						if(PropValue!=null )
							text.setText(PropValue);
						editor.grabHorizontal = true;
						editor.setEditor(text,item, ColumnNumber);
						// Added as part of CR#21
						editor.getEditor().setEnabled(enable_editor);
						text.setData("colId", ColumnNumber);
						text.addModifyListener(new ModifyListener() {     
							public void modifyText(ModifyEvent event) {
								PAMSecondaryPropValue currentSecPropValues = tableConfigValues[tcTable.indexOf(item)] ;
								compPropVal_m.put("puid",currentSecPropValues.selectedComponent);
								compPropVal_m.put("section",section);
								int columnNum = (Integer) ((Text)(event.getSource())).getData("colId");
								compPropVal_m.put(currentSecPropValues.propNameValuepair[columnNum].propName,((Text) (event.getSource())).getText());
							}      
						});
						break;
					case 5:
						text = new Text(tcTable, SWT.NONE);
						if(PropValue!=null )
							text.setText(PropValue);
						editor.grabHorizontal = true;
						editor.setEditor(text,item, ColumnNumber);
						// Added as part of CR#21
						editor.getEditor().setEnabled(enable_editor);
						text.setData("colId", ColumnNumber);
						text.setData("section", section);
						text.addModifyListener(new ModifyListener() {     
							public void modifyText(ModifyEvent event) {
								PAMSecondaryPropValue currentSecPropValues = tableConfigValues[tcTable.indexOf(item)] ;
								compPropVal_m.put("puid",currentSecPropValues.selectedComponent);
								String section = (String) ((Text)(event.getSource())).getData("section");
								if (section!=null)
									compPropVal_m.put("section",section);
								int columnNum = (Integer) ((Text)(event.getSource())).getData("colId");
								compPropVal_m.put(currentSecPropValues.propNameValuepair[columnNum].propName,((Text) (event.getSource())).getText());
							}      
						});
						break;

					case 6:
						Button checkButton = new Button(tcTable, SWT.CHECK|SWT.CENTER);
						checkButton.setData("colId", ColumnNumber);
						if(PropValue.equalsIgnoreCase("True"))
							checkButton.setSelection(true);
						editor.grabHorizontal = true;
						editor.setEditor(checkButton,item,ColumnNumber);
						// Added as part of CR#21
						editor.getEditor().setEnabled(enable_editor);


						checkButton.addSelectionListener(new SelectionAdapter() {     
							public void widgetSelected(SelectionEvent event) {
								PAMSecondaryPropValue currentSecPropValues = tableConfigValues[tcTable.indexOf(item)] ;
								compPropVal_m.put("puid",currentSecPropValues.selectedComponent);
								compPropVal_m.put("section",section);
								int columnNum = (Integer) ((Button)(event.getSource())).getData("colId");
								String isTrue= (((Button) (event.getSource())).getSelection()==true)?"True":"False";
								compPropVal_m.put(currentSecPropValues.propNameValuepair[columnNum].propName,isTrue);

							}      
						});
						break;
					case 7:
						break;
					case 8:

						int textLength = textLimit.get(propname).intValue();

						if(propname.equalsIgnoreCase("u4_details"))
						{
							conditionDescriptionText = new Label(tcTable, SWT.WRAP | SWT.BORDER_SOLID);
							if(PropValue !=null  && !("".equals(PropValue)) )
							{
								conditionDescriptionText.setAlignment(SWT.CENTER);
								conditionDescriptionText.setFont( new Font(Display.getDefault(),"Arial", 8, SWT.BOLD ) );
								conditionDescriptionText.setText(". . .");
							}
							else
							{
								conditionDescriptionText.setText("");
							}

							editor.grabHorizontal = true;
							editor.setEditor(conditionDescriptionText, item, ColumnNumber);

							//Added as part of CR#21
							editor.getEditor().setEnabled(enable_editor);
							conditionDescriptionText.setData("colId", ColumnNumber);
							conditionDescriptionText.setEnabled(false);

							//getting the row index 
							PAMSecondaryPropValue currentSecPropValues = tableConfigValues[tcTable.indexOf(item)] ;
							//compPropVal_m.put("puid",currentSecPropValues.selectedComponent);
							//compPropVal_m.put("section",section);
							//adding to the Map
							//compPropVal_m.put("u4_details", PropValue);
						}


						final PAMSecondaryPropValue currentSecPropValues = tableConfigValues[tcTable.indexOf(item)] ;
						if(propname.equalsIgnoreCase("u4_note"))
						{
							final Vector<String> noteValues = new Vector<String>();
							final HashMap<Integer,String> noteVals_m = new HashMap<Integer,String>();


							if(noteValues.size()>tcTable.indexOf(item))
								noteValue	= noteValues.get(tcTable.indexOf(item));

							else if(currentSecPropValues.getRequiredPAMPropertyValue("u4_note") !=null)
								noteValue = (String) currentSecPropValues.getRequiredPAMPropertyValue("u4_note");

							if(!noteValue.equalsIgnoreCase(""))
								noteButton.setText("...");

							noteButton.setData("colId", ColumnNumber);
							noteButton.pack(true);
							//noteButton.pack();

							final TableEditor editor1 = new TableEditor(tcTable);
							editor1.grabHorizontal = true;
							editor1.setEditor(noteButton, item, ColumnNumber);
							// Added as part of CR#21
							editor1.getEditor().setEnabled(true);
							if ((isCheckOut == true) || ((isCheckOut == false) && (noteValue.isEmpty()==false))  )
							{
								noteButton.addSelectionListener(new SelectionAdapter() {     
									public void widgetSelected(SelectionEvent event) { 
										if(noteVals_m.get(tcTable.indexOf(item))!=null)
											noteValue	= noteVals_m.get(tcTable.indexOf(item));

										else if( currentSecPropValues.getRequiredPAMPropertyValue("u4_note") !=null)
											noteValue = currentSecPropValues.getRequiredPAMPropertyValue("u4_note");
										else 
											noteValue = "";

										if(noteValue!=null && !(noteValue.equalsIgnoreCase("")))
											noteButton.setText("...");

										Display display = Display.getDefault();
										// Added as part of CR#21
										NoteDialog noteDlg = new NoteDialog(display,noteValue,pamSpecRevision,matlDetails2loaded,(TCComponentForm) currentSecPropValues.selectedComponent);
										noteDlg.open();
										noteDlg.layout();
										while (!noteDlg.isDisposed()) {
											if (!display.readAndDispatch()) {
												display.sleep();
											}
										}

										if(noteDlg.isOKPressed())
										{
											noteVals_m.put(tcTable.indexOf(item), noteDlg.getNote());
											noteValue = noteDlg.getNote();

											if(!noteValue.equalsIgnoreCase(""))
												noteButton.setText("...");

											compPropVal_m.put("puid",currentSecPropValues.selectedComponent);
											compPropVal_m.put("section",section);
											compPropVal_m.put("u4_note",noteValue);
											noteButton.setData("Note",noteDlg.getNote());
										}
										if(noteDlg.isClearPressed() && noteDlg.isOKPressed())
										{
											noteButton.setText("");
										}

									}      
								});
							}
						}//u4_note


						TCComponentListOfValues currLOVValues = null;
						ListOfValuesInfo tempLOVInfo;
						Object[] lovValues = null;
						final InitialLovData mInitData = new InitialLovData( );
						String[] lovStrings = null;

						if((( currLOVValues = currTCProp.getLOV() )!= null) || (propname.equalsIgnoreCase("u4_method")|| propname.equalsIgnoreCase("u4_details")))
						{
							if(propname.equalsIgnoreCase("u4_method"))
							{
								String propertyValue =  currentSecPropValues.getRequiredPAMPropertyValue("u4_property");
								final String temppropertyValue = propertyValue;
								//lovStrings = getTestMethodValues(propertyValue);
								final Vector<String> conditionValues = new Vector<String>();
								final HashMap<Integer,String> conditionVals_m = new HashMap<Integer,String>();
								final Vector<String> methodValues = new Vector<String>();
								final HashMap<Integer,String> methodVals_m = new HashMap<Integer,String>();

								final Button  methodButton = new Button(tcTable,SWT.BUTTON1);
								methodButton.setData("colId", ColumnNumber);
								methodButton.pack(true);
								methodButton.pack();

								if(methodValues.size()>tcTable.indexOf(item))
									methodValue	= methodValues.get(tcTable.indexOf(item));
								else if( currentSecPropValues.getRequiredPAMPropertyValue("u4_method") !=null)
									methodValue = currentSecPropValues.getRequiredPAMPropertyValue("u4_method");


								if(conditionValues.size()>tcTable.indexOf(item))
									conditionValue	= conditionValues.get(tcTable.indexOf(item));
								else if( currentSecPropValues.getRequiredPAMPropertyValue("u4_details") !=null)
									conditionValue = currentSecPropValues.getRequiredPAMPropertyValue("u4_details");


								if(PropValue !=null  && !("".equals(PropValue)) )
									methodButton.setText(PropValue);

								final TableEditor editor1 = new TableEditor(tcTable);
								editor1.grabHorizontal = true;
								editor1.setEditor(methodButton, item, ColumnNumber);
								// Added as part of CR#21
								//editor1.getEditor().setEnabled(true);

								//final String[] txtMethodArrVals = lovStrings;
								if ( (isCheckOut == true) || ((isCheckOut == false) && (methodValue.isEmpty()==false)) )
								{
									methodButton.addSelectionListener(new SelectionAdapter() {     
										public void widgetSelected(SelectionEvent event) {

											if(methodVals_m.get(tcTable.indexOf(item))!=null)
												methodValue	= methodVals_m.get(tcTable.indexOf(item));
											else if( currentSecPropValues.getRequiredPAMPropertyValue("u4_method") !=null)
												methodValue = currentSecPropValues.getRequiredPAMPropertyValue("u4_method");


											if(conditionVals_m.get(tcTable.indexOf(item))!=null)
												conditionValue	= conditionVals_m.get(tcTable.indexOf(item));
											else if( currentSecPropValues.getRequiredPAMPropertyValue("u4_details") !=null)
												conditionValue = currentSecPropValues.getRequiredPAMPropertyValue("u4_details");

											PAMSecondaryPropValue currentSecPropValues = tableConfigValues[tcTable.indexOf(item)] ;

											String sectionAndPropName = currentSecPropValues.selectedComponent.getDisplayType() + " : " + currentSecPropValues.getRequiredPAMPropertyValue("u4_property");

											CustomMethodDialog methodDlg = new CustomMethodDialog(AIFDesktop.getActiveDesktop().getShell(), 
													session,currentSecPropValues,pamSpecRevision,sectionAndPropName,conditionValue, methodValue ,false,temppropertyValue ,attrTMvalues , TMData);

											methodDlg.open();

											methodValue = methodDlg.getMethod();
											conditionValue = methodDlg.getConditionSet();
											//strConditionNumber = methodDlg.getConditionNumber();

											if(	methodValue != null && !("".equals(methodValue)) )
											{
												methodVals_m.put(tcTable.indexOf(item), methodValue);

												compPropVal_m.put("puid",currentSecPropValues.selectedComponent);
												compPropVal_m.put("section",section);
												//adding to the Map
												compPropVal_m.put("u4_method",methodValue);
												methodButton.setText(methodValue);

												if(conditionValue != null && !("".equals(conditionValue)))
												{
													conditionVals_m.put(tcTable.indexOf(item), conditionValue);

													compPropVal_m.put("puid",currentSecPropValues.selectedComponent);
													compPropVal_m.put("section",section);
													//adding to the Map
													compPropVal_m.put("u4_details",conditionValue);
													conditionDescriptionText.setAlignment(SWT.CENTER);
												}
											}
										}      
									});
								}
							}
							else if(propname.equalsIgnoreCase("u4_details"))
							{
								if(conditionValue != null && !("".equals(conditionValue)))
								{
									compPropVal_m.put("puid",currentSecPropValues.selectedComponent);
									compPropVal_m.put("section",section);
									//adding to the Map
									compPropVal_m.put("u4_details",conditionValue);
									conditionDescriptionText.setAlignment(SWT.CENTER);
								}
							}							
							else if(  propname.equalsIgnoreCase(TYPE) &&  
									( formType.equals(APFORM) || formType.equals(AP1FORM)))
							{

								if (currLOVValues.toString().compareToIgnoreCase(POLYMERTYPE_LOV) == 0)
								{
									String[] polymertype_lovs = (String[]) tcTable.getData("polymertype_lovs");

									if(polymertype_lovs!=null)
										if (isCheckOut == true)//setItems was taking 40-90 milli seconds, hence we will set the items only when in check out state
											cbType.setItems( polymertype_lovs);
								}
								
								cbType.add("", 0);

								if(PropValue!=null && PropValue.length() > 0)
								{
									cbType.setText(PropValue);

									cbPML.removeAll();
									//Added below line for defect#950
									//cbPML.add("");

									if(prefPolymers.containsKey(PropValue))
									{
										ArrayList < String > resins = prefPolymers.get(PropValue);

										Hashtable<String, TCComponent> pmltypes =  tableObjects.get(APFORM);

										if(pmltypes != null)
										{
											Enumeration names = pmltypes.keys();

											while(names.hasMoreElements())
											{ 
												String name = (String) names.nextElement();

												TCComponent comp = pmltypes.get(name);
												try 
												{
													String resinType = comp.getStringProperty("u4_resin_type");

													if(resins.contains(resinType))
														cbPML.add(name);
												}
												catch (TCException e)
												{
													e.printStackTrace();
												}
											} 
											
											cbPML.add("",0);

											try
											{
												String val = currForm.getStringProperty(PMLTYPE);

												if(val != null)
													cbPML.setText(val);

												val = currForm.getStringProperty(GRADEREF);

												if(val == null)
													txtGrade.setText("");
												else
													txtGrade.setText(val);
											}
											catch (TCException e)
											{
												e.printStackTrace();
											}
										}
									}

								}

								editor.grabHorizontal = true;
								editor.setEditor(cbType, item, ColumnNumber);
								// Added as part of CR#21
								editor.getEditor().setEnabled(enable_editor);
								cbType.setData("colId", ColumnNumber);
								cbType.setData("section", section);
								cbType.addSelectionListener(new SelectionAdapter() {     
									public void widgetSelected(SelectionEvent event) {        
										PAMSecondaryPropValue currentSecPropValues = tableConfigValues[tcTable.indexOf(item)] ;
										compPropVal_m.put("puid",currentSecPropValues.selectedComponent);
										String section = (String) ((CCombo)(event.getSource())).getData("section");
										if (section!=null)
											compPropVal_m.put("section",section);
										int columnNum = (Integer) ((CCombo)(event.getSource())).getData("colId");
										compPropVal_m.put(currentSecPropValues.propNameValuepair[columnNum].propName,((CCombo) (event.getSource())).getText());
										cbPML.removeAll();
										cbManu.removeAll();
										//Added below line for defect#950
										cbPML.add("");
										String typevalue = ((CCombo) (event.getSource())).getText() ;
										compPropVal_m.put(PMLTYPE,"");
										txtGrade.setText("");
										compPropVal_m.put(GRADEREF,"");
										cbManu.removeAll();
										cbManu.add("");
										cbManu.setText("");
										compPropVal_m.put(MANUF,"");

										//reset pml type lovs

										if(prefPolymers.containsKey(typevalue))
										{
											ArrayList < String > resins = prefPolymers.get(typevalue);

											Hashtable<String, TCComponent> pmltypes =  tableObjects.get(APFORM);

											if(pmltypes != null)
											{
												Enumeration names = pmltypes.keys();

												while(names.hasMoreElements())
												{ 
													String name = (String) names.nextElement();

													TCComponent comp = pmltypes.get(name);

													try 
													{
														String resinType = comp.getStringProperty("u4_resin_type");

														if(resins.contains(resinType))
															cbPML.add(name);
													}
													catch (TCException e)
													{
														e.printStackTrace();
													}
												}
											}
										}

										String[] manfpolymermb_lovs = null;
										manfpolymermb_lovs = (String[]) tcTable.getData("manfpolymermb");

										//reset manu combo
										if (manfpolymermb_lovs.length > 0)
											if (pamSpecRevision.isCheckedOut() == true)//setItems was taking 40-90 milli seconds, hence we will set the items only when in check out state
												cbManu.setItems( manfpolymermb_lovs);
										
										cbManu.add("", 0);
										//TCComponentListOfValues tccomponentlistofvalues = TCComponentListOfValuesType.findLOVByName(MANULOV);
									}		

								});
							}
							else if( isInterdependantLov(currTCProp))
							{
								mInitData.lov = currLOVValues;
								mInitData.propertyName=currTCProp.getPropertyName();
								mInitData.lovInput.owningObject=currTCProp.getTCComponent();
								LOVSearchResults mInitDataResponse = mLOVService.getInitialLOVValues( mInitData );
								for( LOVValueRow LOVValue : mInitDataResponse.lovValues )
								{
									String mValue = ((String[])LOVValue.propDisplayValues.get( "lov_values" ))[0];

									if( mValue.equalsIgnoreCase( item.getText( )))
									{
										lovValues = new String[LOVValue.childRows.length];
										for( int index = 0;index < LOVValue.childRows.length;index++ )
											lovValues[index] = ((String[])LOVValue.childRows[index].propDisplayValues.get( "lov_values" ))[0];
									}
									if(propname.equalsIgnoreCase("u4_material")&& mValue.equalsIgnoreCase(  currentSecPropValues.getRequiredPAMPropertyValue("u4_layer")))
									{
										lovValues = new String[LOVValue.childRows.length];
										for( int index = 0;index < LOVValue.childRows.length;index++ )
											lovValues[index] = ((String[])LOVValue.childRows[index].propDisplayValues.get( "lov_values" ))[0];

									}
								}
								/**
								 * 28-Sept-2014: Added following check for "lovValues" as Materials Tab was not getting populated due to one of the property
								 * "u4_type" on U4_ApprvdAdditiveDtlForm is causing NULL assuming its an LOV. Hence, below check is introduced.
								 */
								if (lovValues != null)
								{
									// Enhancement to provide empty dropdown select at the top of the list
									lovStrings = new String[lovValues.length + 1];
									lovStrings[0] = "";
									System.arraycopy(lovValues, 0, lovStrings, 1, lovValues.length);

									//lovStrings = Arrays.copyOf(lovValues, lovValues.length+1, String[].class);
									//lovStrings[lovValues.length]="";
								}
								else
								{
									text = new Text(tcTable, SWT.NONE);
									text.setTextLimit(textLength);

									if(PropValue!=null)
										text.setText(PropValue);
									final TableEditor editorNonLOv = new TableEditor(tcTable);
									editorNonLOv.grabHorizontal = true;
									editorNonLOv.setEditor(text,item, ColumnNumber);
									// Added as part of CR#21
									editorNonLOv.getEditor().setEnabled(enable_editor);
									text.setData("colId", ColumnNumber);
									text.addModifyListener(new ModifyListener() {     
										public void modifyText(ModifyEvent event) {
											PAMSecondaryPropValue currentSecPropValues = tableConfigValues[tcTable.indexOf(item)] ;
											compPropVal_m.put("puid",currentSecPropValues.selectedComponent);
											compPropVal_m.put("section",section);
											int columnNum = (Integer) ((Text)(event.getSource())).getData("colId");
											compPropVal_m.put(currentSecPropValues.propNameValuepair[columnNum].propName,((Text) (event.getSource())).getText());
										}      
									});

								}	

							}	//isInterdependantLov					
							else
							{
								try {
									tempLOVInfo = currLOVValues.getListOfValues();
									lovValues =  tempLOVInfo.getListOfValues();
								} catch (TCException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								// Enhancement to provide empty dropdown select at the top of the list
								lovStrings = new String[lovValues.length + 1];
								lovStrings[0] = "";
								System.arraycopy(lovValues, 0, lovStrings, 1, lovValues.length);
								//lovStrings = Arrays.copyOf(lovValues, lovValues.length+1, String[].class);
								///** fix for the Defect166 to clear the LOV Values if required*/
								//lovStrings[lovValues.length]="";
								CCombo  combo = new CCombo(tcTable,SWT.DROP_DOWN | SWT.READ_ONLY);
								combo.setData("colId", ColumnNumber);
								//combo.setData("pml", cbPML);

								if(lovStrings!=null)
									if (isCheckOut == true)//setItems was taking 40-90 milli seconds, hence we will set the items only when in check out state
										combo.setItems( lovStrings);

								if(PropValue!=null && PropValue.length() > 0)
									combo.setText(PropValue);

								editor.grabHorizontal = true;
								editor.setEditor(combo, item, ColumnNumber);
								// Added as part of CR#21
								editor.getEditor().setEnabled(enable_editor);

								combo.addSelectionListener(new SelectionAdapter() {     
									public void widgetSelected(SelectionEvent event) {        
										PAMSecondaryPropValue currentSecPropValues = tableConfigValues[tcTable.indexOf(item)] ;
										compPropVal_m.put("puid",currentSecPropValues.selectedComponent);
										compPropVal_m.put("section",section);
										int columnNum = (Integer) ((CCombo)(event.getSource())).getData("colId");
										compPropVal_m.put(currentSecPropValues.propNameValuepair[columnNum].propName,((CCombo) (event.getSource())).getText());
									}      
								});
							}	
						}//currLOVValues
						else if( ( formType.equals(APFORM)   || formType.equals(AP1FORM) ) && propname.equals(MANUF) )
						{
							cbManu.setData("colId", ColumnNumber);
							editor.grabHorizontal = true;
							editor.setEditor(cbManu, item, ColumnNumber);
							// Added as part of CR#21
							editor.getEditor().setEnabled(enable_editor);
							cbManu.removeAll();
							cbManu.add("");
							if(PropValue.length() > 0)
								cbManu.add(PropValue);

							if (isCheckOut == true)//setItems was taking 40-90 milli seconds, hence we will set the items only when in check out state
							{
								String[] manfpolymermb_lovs = null;
								manfpolymermb_lovs = (String[]) tcTable.getData("manfpolymermb");

								if (manfpolymermb_lovs.length > 0)
									cbManu.setItems(manfpolymermb_lovs);
							}
							cbManu.add("", 0);							
							cbManu.setText(PropValue);

							cbManu.addSelectionListener(new SelectionAdapter() {     
								public void widgetSelected(SelectionEvent event) {
									PAMSecondaryPropValue currentSecPropValues = tableConfigValues[tcTable.indexOf(item)] ;
									compPropVal_m.put("puid",currentSecPropValues.selectedComponent);
									compPropVal_m.put("section",section);
									String typevalue = ((CCombo) (event.getSource())).getText() ;
									int columnNum = (Integer) ((CCombo)(event.getSource())).getData("colId");
									compPropVal_m.put(currentSecPropValues.propNameValuepair[columnNum].propName,typevalue);
								}      
							});
						}	
						else if( formType.equals(MATLFORM) && propname.equals(TYPE) )
						{
							//123
							String val = null ;
							String prop = null ;

							cbPML.removeAll();

							Hashtable<String, TCComponent> pmltypes = null ;

							try
							{
								prop = currForm.getStringProperty(PROP);
								val = currForm.getStringProperty(TYPE);
							}
							catch (TCException e)
							{
								e.printStackTrace();
							}							

							ArrayList  nonPMLLIst = new ArrayList<String>();
							nonPMLLIst.add("");

							// populate  Non PML Material from LOV U4_PL_PolymerTypeLOV
							try
							{
								LOVSearchResults materialdetailsLOVResult = null;

								materialdetailsLOVResult = (LOVSearchResults) tcTable.getData("materialdetails_lovresult");
								
								if (materialdetailsLOVResult==null)
								{
									if (isCheckOut == false)
									{
										if (typeValue!=null && typeValue.length()>0)
										{
											String dropdown_value []={typeValue};
											cbPML.setItems(dropdown_value);
										}
									}
								}

								if (materialdetailsLOVResult!=null)
								{
									for( LOVValueRow LOVValue : materialdetailsLOVResult.lovValues )
									{
										String mValue = ((String[])LOVValue.propDisplayValues.get( "lov_values" ))[0];

										if( mValue.equalsIgnoreCase( prop))
										{
											lovValues = new String[LOVValue.childRows.length];
											for( int index = 0;index < LOVValue.childRows.length;index++ )
												lovValues[index] = ((String[])LOVValue.childRows[index].propDisplayValues.get( "lov_values" ))[0];

											nonPMLMaterial = Arrays.copyOf(lovValues, lovValues.length, String[].class);
											//xxx
											//if (this.isCheckOut == true)//setItems was taking 40-90 milli seconds, hence we will set the items only when in check out state
											cbPML.setItems(nonPMLMaterial);

											Collections.addAll(nonPMLLIst, nonPMLMaterial);
										}
									}
								}
							}
							catch (Exception e)
							{
								e.printStackTrace();
							}

							/**
							 *  Fix for Defect# 654 - We are keeping the plain text field, if the Type does not have LOV contents, 
							 *  though IF has Field_Type as Association, but no Association Value.
							 */
							/**
							 *  Implementation of CR#131 - Add Combo-box for Type field with only 1 value as "-"
							 */
							if(cbPML.getItemCount() == 0)
							{
								CCombo  combo2 = new CCombo(tcTable,SWT.DROP_DOWN | SWT.READ_ONLY);
								combo2.setData("colId", ColumnNumber);
								// TODO:: Defect#1290
								combo2.setItems(new String[]{"","-"});
								/*if(PropValue != null && "-".equals(PropValue))
											combo2.setText(PropValue);*/	
								if(PropValue != null && PropValue.length() > 0)
									combo2.setText("-");
								else 
									combo2.setText("");
								editor.grabHorizontal = true;
								editor.setEditor(combo2,item,ColumnNumber);
								// Added as part of CR#21
								editor.getEditor().setEnabled(enable_editor);

								combo2.addSelectionListener(new SelectionAdapter() {     
									public void widgetSelected(SelectionEvent event) {        
										PAMSecondaryPropValue currentSecPropValues = tableConfigValues[tcTable.indexOf(item)] ;
										compPropVal_m.put("puid",currentSecPropValues.selectedComponent);
										compPropVal_m.put("section",section);
										int columnNum = (Integer) ((CCombo)(event.getSource())).getData("colId");
										compPropVal_m.put(currentSecPropValues.propNameValuepair[columnNum].propName,((CCombo) (event.getSource())).getText());

									}      
								});
							}
							else
							{
								//456
								cbPML.setData("colId", ColumnNumber);
								editor.grabHorizontal = true;
								editor.setEditor(cbPML, item, ColumnNumber);
								// Added as part of CR#21
								editor.getEditor().setEnabled(enable_editor);

								Collections.sort(nonPMLLIst);
								cbPML.setItems((String[]) nonPMLLIst.toArray(new String[nonPMLLIst.size()]));

								if(val != null)
									cbPML.setText(val);

								try 
								{
									if(labelLit && currForm.getStringProperty(PROP).equals(TYPECARTON))
										typecarbon = val ;
								} catch (TCException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}

								cbPML.addSelectionListener(new SelectionAdapter() {     
									public void widgetSelected(SelectionEvent event) {
										PAMSecondaryPropValue currentSecPropValues = tableConfigValues[tcTable.indexOf(item)] ;
										compPropVal_m.put("puid",currentSecPropValues.selectedComponent);
										compPropVal_m.put("section",section);
										String typevalue = ((CCombo) (event.getSource())).getText() ;
										int columnNum = (Integer) ((CCombo)(event.getSource())).getData("colId");
										compPropVal_m.put(currentSecPropValues.propNameValuepair[columnNum].propName,typevalue);
										String propName = null ;
										try
										{
											propName = currentSecPropValues.selectedComponent.getStringProperty(PROP).toString();
										} catch (TCException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}

										if(labelLit && propName.equals(TYPECARTON))
										{
											Hashtable pnbObjects = tableObjects.get(MATLPROPFORM);
											ArrayList pnbnames = new ArrayList<String>();
											pnbnames.add("");
											if(pnbObjects != null)
											{
												Enumeration names = pnbObjects.keys();
												while(names.hasMoreElements())
												{ 															
													TCComponent comp = (TCComponent) pnbObjects.get((String) names.nextElement());
													try 
													{
														if(comp.getStringProperty("u4_material_type").equals(CartonPNBTypeMap.get(typevalue)))
															pnbnames.add(comp.getStringProperty("object_name"));
													}
													catch (TCException e)
													{
														e.printStackTrace();
													}
												}  
											}

											for ( int inb =0 ;inb < BoardCombos.size() ;inb++)
											{
												CCombo combo = BoardCombos.get(inb);
												Collections.sort(pnbnames);
												combo.setItems((String[]) pnbnames.toArray(new String[pnbnames.size()]));
												combo.setText("");
												TableItem item  = (TableItem) combo.getData("item");
												if(item != null)
												{
													Text txtTN = (Text) item.getData(TNAME);
													if (txtTN != null ) 
													{
														txtTN.setText("");
														txtTN.setEnabled(true);
													}													 
													Text txtValue = (Text) item.getData(TNVALUE);
													if (txtValue != null )
													{ 
														txtValue.setText("");
														txtValue.setEnabled(true);
													}
													HashMap hmap = (HashMap) item.getData("savemap");
													if(hmap != null )
													{
														hmap.put(TNAME, "");
														hmap.put(TNVALUE, "");
														hmap.put(combo.getData("name"), "");
													}

													if(inb == 0)
													{
														CTabFolder tab1 = getTab(parentComposite) ;
														CTabItem cptab = null ;
														CTabItem[] tabitems = null ;
														if(tab1 != null)
															tabitems = tab1.getItems();

														if(tabitems != null)
															for(int tab = 0 ;tab < tabitems.length ;tab++)
															{
																if(tabitems[tab].getText().startsWith("Material"))
																	cptab = tabitems[tab];
															}

														if(cptab != null )
															cptab.setData("lbltype", false);

														if( cartonloaded || matlDetails2)
															resetLayerValues(false,false);

														cartonloaded = false ;
														matlDetails2 = false ;
														matlDetails2loaded = false ;

														if( tab1.getSelection().getText().startsWith("Material"))
														{
															tab1.getSelection().setData("pml", null);
															tab1.getSelection().setData("activepml", null);
														}

														PML2PAMCPMapAttrValuesClear(pamforms,cptab);
													}
												}
											}


										}
									}      
								});
							}
						}

						else if( formType.equals(MATL2FORM) && propname.equals(TYPE) )
						{
							String val = null ;
							String prop = null ;

							cbPML.setData("colId", ColumnNumber);
							editor.grabHorizontal = true;
							editor.setEditor(cbPML, item, ColumnNumber);
							// Added as part of CR#21
							editor.getEditor().setEnabled(enable_editor);
							cbPML.removeAll();
							Hashtable<String, TCComponent> pmltypes = null ;

							try
							{
								pmltypes =  tableObjects.get(MATL2FORM);
								prop = currForm.getStringProperty(PROP);
								val = currForm.getStringProperty(TYPE);
							}
							catch (TCException e)
							{
								e.printStackTrace();
							}							

							ArrayList  nonPMLLIst = new ArrayList<String>();
							nonPMLLIst.add("");

							if(pmltypes != null)
							{
								Enumeration names = pmltypes.keys();

								while(names.hasMoreElements())
								{ 
									String name = (String) names.nextElement();

									if (! (nonPMLLIst.contains(name)) )
										nonPMLLIst.add(name);
								} 
							}

							Collections.sort(nonPMLLIst);
							cbPML.setItems((String[]) nonPMLLIst.toArray(new String[nonPMLLIst.size()]));
							CTabFolder tab1 = getTab(parentComposite) ;
							if(val != null && val.length() > 0)
							{
								cbPML.setText(val);
								matlDetails2 = true ;

								if( tab1.getSelection().getText().startsWith("Material"))
									if(pmltypes != null)
										tab1.getSelection().setData("pml", pmltypes.get(val));
							}

							final Map<String, TCComponentForm> pamforms = new HashMap<String, TCComponentForm>();

							if(labelLit == true)
							{
								try
								{
									AIFComponentContext[] forms = pamSpecRevision.getRelated(CPFORMS);

									for ( int ina= 0 ;ina< forms.length ;ina++)
									{
										TCComponentForm frm = (TCComponentForm)forms[ina].getComponent();
										String name = frm.getStringProperty("object_name");
										pamforms.put(name, frm);
									}
								}
								catch (TCException e)
								{
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}

							cbPML.addSelectionListener(new SelectionAdapter() {     
								public void widgetSelected(SelectionEvent event) {
									PAMSecondaryPropValue currentSecPropValues = tableConfigValues[tcTable.indexOf(item)] ;
									compPropVal_m.put("puid",currentSecPropValues.selectedComponent);
									compPropVal_m.put("section",section);
									String typevalue = ((CCombo) (event.getSource())).getText() ;
									int columnNum = (Integer) ((CCombo)(event.getSource())).getData("colId");
									compPropVal_m.put(currentSecPropValues.propNameValuepair[columnNum].propName,typevalue);

									Hashtable<String, TCComponent> pmltypes = null ;
									pmltypes =  tableObjects.get(MATL2FORM);

									CTabFolder tab1 = getTab(parentComposite) ;
									CTabItem cptab = null ;
									CTabItem[] tabitems = null ;
									if(tab1 != null)
										tabitems = tab1.getItems();

									if(tabitems != null)
										for(int tab = 0 ;tab < tabitems.length ;tab++)
										{
											if(tabitems[tab].getText().startsWith("Material"))
												cptab = tabitems[tab];
										}

									if(cptab != null )
										cptab.setData("lbltype", false);

									if(labelLit & typevalue.length() > 0)  
									{
										matlDetails2 = true ;

										if(cptab != null )
											cptab.setData("lbltype", true);

										for ( int v=0 ;v < modifiedLayerPropVals_v.size() ;v++)
										{
											Map<String, Object> propValueMap = modifiedLayerPropVals_v.get(v);
											propValueMap.remove("u4_type");
										}

										if(pmltypes != null)
										{
											TCComponent deco = pmltypes.get(typevalue);

											if(deco != null)
											{   
												if( tab1.getSelection().getText().startsWith("Material"))
												{
													tab1.getSelection().setData("pml", deco);
													tab1.getSelection().setData("activepml", deco);
												}											   
												populateLayerValues(deco,PMLLAYERFORM);

											}	

											PML2PAMCPMapAttrValues((TCComponentItemRevision)deco,pamforms,cptab);
										}
									}
									else if(labelLit & typevalue.length() == 0)
									{
										matlDetails2 = false ;

										if( tab1.getSelection().getText().startsWith("Material"))
										{
											tab1.getSelection().setData("pml", null);
											tab1.getSelection().setData("activepml", null);
										}

										for ( int v=0 ;v < modifiedLayerPropVals_v.size() ;v++)
										{
											Map<String, Object> propValueMap = modifiedLayerPropVals_v.get(v);
											propValueMap.remove("u4_type");
										}

										resetLayerValues(false,true);
										PML2PAMCPMapAttrValuesClear(pamforms,cptab);

									}
								}      
							});
						}						

						else if( formType.equals(PACKLAYERFORM) && propname.equals(MATL) )
						{
							String val = null ;
							String layer = null ;

							cbPML.setData("colId", ColumnNumber);
							editor.grabHorizontal = true;
							editor.setEditor(cbPML, item, ColumnNumber);
							// Added as part of CR#21
							editor.getEditor().setEnabled(enable_editor);
							cbPML.removeAll();
							Hashtable<String, TCComponent> pmltypes = null ;

							try
							{
								pmltypes =  tableObjects.get(PACKLAYERFORM);
								val = currForm.getStringProperty(MATL);
								layer = currForm.getStringProperty(LAYER);
							}
							catch (TCException e)
							{
								// TODO Auto-generated catch block										
							}							

							ArrayList  nonPMLLIst = new ArrayList<String>();

							// populate  Non PML Material from LOV U4_PL_PolymerTypeLOV

							TCComponentListOfValues tccomponentlistofvalues = TCComponentListOfValuesType.findLOVByName("U4_PL_PackLayer_LOV");

							if(tccomponentlistofvalues != null)
							{
								ListOfValuesInfo lovInfo;

								try
								{ 
									mInitData.lov = tccomponentlistofvalues;
									mInitData.propertyName=currTCProp.getPropertyName();
									mInitData.lovInput.owningObject=currTCProp.getTCComponent();
									LOVSearchResults mInitDataResponse = mLOVService.getInitialLOVValues( mInitData );

									for( LOVValueRow LOVValue : mInitDataResponse.lovValues )
									{
										String mValue = ((String[])LOVValue.propDisplayValues.get( "lov_values" ))[0];

										if( mValue.equalsIgnoreCase( layer))
										{
											lovValues = new String[LOVValue.childRows.length];
											for( int index = 0;index < LOVValue.childRows.length;index++ )
												lovValues[index] = ((String[])LOVValue.childRows[index].propDisplayValues.get( "lov_values" ))[0];

											nonPMLMaterial = Arrays.copyOf(lovValues, lovValues.length, String[].class);
											cbPML.setItems(nonPMLMaterial);

											Collections.addAll(nonPMLLIst, nonPMLMaterial);
										}

									}

								}
								catch (Exception e)
								{
									e.printStackTrace();
								}
							}

							if(pmltypes != null)
							{
								Enumeration names = pmltypes.keys();

								while(names.hasMoreElements())
								{ 
									String name = (String) names.nextElement();
									nonPMLLIst.add(name);
								} 
							}

							Collections.sort(nonPMLLIst);

							cbPML.setItems((String[]) nonPMLLIst.toArray(new String[nonPMLLIst.size()]));

							cbPML.add("", 0);
							if(val != null)
								cbPML.setText(val);


							cbPML.addSelectionListener(new SelectionAdapter() {     
								public void widgetSelected(SelectionEvent event) {
									PAMSecondaryPropValue currentSecPropValues = tableConfigValues[tcTable.indexOf(item)] ;
									compPropVal_m.put("puid",currentSecPropValues.selectedComponent);
									compPropVal_m.put("section",section);
									String typevalue = ((CCombo) (event.getSource())).getText() ;
									int columnNum = (Integer) ((CCombo)(event.getSource())).getData("colId");
									compPropVal_m.put(currentSecPropValues.propNameValuepair[columnNum].propName,typevalue);

								}      
							});
						}						
						else if( ( formType.equals(APFORM)   || formType.equals(AP1FORM) ) && propname.equals(PMLTYPE) )
						{

							cbPML.setData("colId", ColumnNumber);

							editor.grabHorizontal = true;
							editor.setEditor(cbPML, item, ColumnNumber);
							// Added as part of CR#21
							editor.getEditor().setEnabled(enable_editor);

							cbPML.setText(PropValue);

							cbPML.addSelectionListener(new SelectionAdapter() {     
								public void widgetSelected(SelectionEvent event) {
									PAMSecondaryPropValue currentSecPropValues = tableConfigValues[tcTable.indexOf(item)] ;
									compPropVal_m.put("puid",currentSecPropValues.selectedComponent);
									compPropVal_m.put("section",section);

									String typevalue = ((CCombo) (event.getSource())).getText() ;
									int columnNum = (Integer) ((CCombo)(event.getSource())).getData("colId");
									compPropVal_m.put(currentSecPropValues.propNameValuepair[columnNum].propName,typevalue);
									Hashtable<String, TCComponent> pmltypes =  tableObjects.get(formType);

									if(typevalue.length() > 0)
									{
										txtGrade.setText("");
										compPropVal_m.put(GRADEREF,"");
										cbManu.removeAll();
										cbManu.add("");
										cbManu.setText("");
										compPropVal_m.put(MANUF,"");

										Enumeration names = pmltypes.keys();

										while(names.hasMoreElements())
										{ 
											String name = (String) names.nextElement();

											if(name.equals(typevalue))
											{
												TCComponent comp = pmltypes.get(name);

												try
												{
													String val = ((TCComponentItemRevision)comp).getStringProperty(MANUF);

													String[] manfpolymermb_lovs = null;

													manfpolymermb_lovs = (String[]) tcTable.getData("manfpolymermb");

													if (manfpolymermb_lovs.length > 0)
														cbManu.setItems(manfpolymermb_lovs);

													if(val.length() > 0)
														cbManu.add(val,0);

													cbManu.setText(val);
													compPropVal_m.put(MANUF,val);
													cbManu.add("", 0);

													val = ((TCComponentItemRevision)comp).getStringProperty(GRADE);

													if (val== null)
													{
														txtGrade.setText("");
													}
													else
													{
														txtGrade.setText(val);
														compPropVal_m.put(GRADEREF,val);
													}

													break ;
												}
												catch (TCException e)
												{
													// TODO Auto-generated catch block
													e.printStackTrace();
												}
											}
										} //while
									}
								}      
							});
						}
						else if( ( formType.equals(APFORM)   || formType.equals(AP1FORM) ) && propname.equals(GRADEREF) )
						{
							editor.grabHorizontal = true;
							editor.setEditor(txtGrade,item, ColumnNumber);
							// Added as part of CR#21
							editor.getEditor().setEnabled(enable_editor);
							txtGrade.setData("colId", ColumnNumber);
							editor.getEditor().setEnabled(true);

							txtGrade.setText(PropValue);
							txtGrade.setTextLimit(textLength);

							txtGrade.addModifyListener(new ModifyListener() {     
								public void modifyText(ModifyEvent event) {
									//getting the row index 
									PAMSecondaryPropValue currentSecPropValues = tableConfigValues[tcTable.indexOf(item)] ;
									compPropVal_m.put("puid",currentSecPropValues.selectedComponent);
									compPropVal_m.put("section",section);
									compPropVal_m.put(GRADEREF,txtGrade.getText());
								}      
							});
						}
						else
						{
							text = new Text(tcTable, SWT.NONE);
							text.setTextLimit(textLength);
							if(PropValue!=null)
								text.setText(PropValue);
							editor.grabHorizontal = true;
							editor.setEditor(text,item, ColumnNumber);
							// Added as part of CR#21
							editor.getEditor().setEnabled(enable_editor);
							text.setData("colId", ColumnNumber);
							text.addModifyListener(new ModifyListener() {     
								public void modifyText(ModifyEvent event) {
									PAMSecondaryPropValue currentSecPropValues = tableConfigValues[tcTable.indexOf(item)] ;
									compPropVal_m.put("puid",currentSecPropValues.selectedComponent);
									compPropVal_m.put("section",section);
									int columnNum = (Integer) ((Text)(event.getSource())).getData("colId");
									compPropVal_m.put(currentSecPropValues.propNameValuepair[columnNum].propName,((Text) (event.getSource())).getText());
								}      
							});

						}
						break;

					default:
						text = new Text(tcTable, SWT.NONE);
						text.setText(PropValue);
						editor.grabHorizontal = true;
						editor.setEditor(text,item, ColumnNumber);
						// Added as part of CR#21
						editor.getEditor().setEnabled(enable_editor);
						text.setData("colId", ColumnNumber);
						text.addModifyListener(new ModifyListener() {     
							public void modifyText(ModifyEvent event) {
								PAMSecondaryPropValue currentSecPropValues = tableConfigValues[tcTable.indexOf(item)] ;
								compPropVal_m.put("puid",currentSecPropValues.selectedComponent);
								compPropVal_m.put("section",section);
								int columnNum = (Integer) ((Text)(event.getSource())).getData("colId");
								compPropVal_m.put(currentSecPropValues.propNameValuepair[columnNum].propName,((Text) (event.getSource())).getText());
							}      
						});
						break;
					} //switch					
				} // for 
				
				modifiedPropVals_v.add(compPropVal_m);
			}

		});
	}


	public void setTableConfiguration2(final Table tcTable, final PAMSecondaryPropValue[] tableConfigValues )
	{
		final LOVService mLOVService = lovServ;

		TCComponentListOfValues LayerStructure_lov = TCComponentListOfValuesType.findLOVByName("U4_PL_LayerStructureLOV");
		LOVSearchResults layerstructureLOVResult = null;

		if(LayerStructure_lov != null)
		{
			try
			{
				InitialLovData mInitData = new InitialLovData( );
				mInitData.lov = LayerStructure_lov;
				mInitData.propertyName=tableConfigValues[0].propNameValuepair[0].propName;
				mInitData.lovInput.owningObject=tableConfigValues[0].selectedComponent;
				layerstructureLOVResult = lovServ.getInitialLOVValues( mInitData );

				tcTable.setData("layer_structure_lov", layerstructureLOVResult);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		final Map<String,Integer > textLimit = new HashMap<String,Integer>(PAMConstant.getTextLimit (tableConfigValues[0]));

		final int nColumn  = tableConfigValues[0].propNameValuepair.length;

		for(  int ColumnNumber =0;ColumnNumber< tableConfigValues[0].propNameValuepair.length;ColumnNumber++ )
		{
			final TableColumn propertyColumn =  new TableColumn(tcTable, SWT.BORDER_SOLID|SWT.COLOR_WIDGET_NORMAL_SHADOW);
			propertyColumn.setWidth(tableConfigValues[0].propNameValuepair[ColumnNumber].columnSize);

			String nametext = tableConfigValues[0].propNameValuepair[ColumnNumber].propDisplayName;
			propertyColumn.setText(nametext);
			propertyColumn.setResizable(true);
		}

		tcTable.setItemCount(tableConfigValues.length);

		tcTable.addListener(SWT.SetData, new Listener() {
			public void handleEvent(Event event) {
			}
		});

		tcTable.setItemCount(tableConfigValues.length);

		tcTable.addListener(SWT.SetData, new Listener() {
			public void handleEvent(Event event) {
				final TableItem item = (TableItem) event.item;
				int row = tcTable.indexOf(item);
				boolean legend = PAMConstant.ruleValidate((TCComponentForm) tableConfigValues[row].selectedComponent);
				final TCComponentForm currForm = (TCComponentForm) tableConfigValues[row].selectedComponent;
				final String formType = tableConfigValues[row].secondaryName;
				final Map<String,Object> compPropVal_m = new HashMap<String,Object>();

				layetControls =  new HashMap<String,Object>();

				int seqnumber = 0;
				String number = "";
				String uom = "";
				try
				{
					seqnumber = currForm.getIntProperty(SEQNO);
					number = currForm.getStringProperty(NUMBER);
					uom = currForm.getStringProperty(UOM);
				}
				catch (TCException e1)
				{
					e1.printStackTrace();
				}

				final CCombo  cbType = new CCombo(tcTable,SWT.DROP_DOWN | SWT.READ_ONLY);
				layetControls.put("seqno", seqnumber);
				layetControls.put("type", cbType);
				layetControls.put("uom", uom);
				layetControls.put(NUMBER, number);
				layetControls.put("uid", currForm.getUid());
				final Text txtColor = new Text(tcTable, SWT.NONE);
				layetControls.put("color", txtColor);
				final Text txtTarget = new Text(tcTable, SWT.NONE);
				layetControls.put("target", txtTarget);	
				final Text txtMin = new Text(tcTable, SWT.NONE);
				layetControls.put("min", txtMin);
				final Text txtMax = new Text(tcTable, SWT.NONE);
				layetControls.put("max", txtMax);
				final Text txtComment = new Text(tcTable, SWT.NONE);
				layetControls.put("comment", txtComment);
				final TableEditor teMin = new TableEditor(tcTable);	
				layetControls.put("tMin", teMin);
				final TableEditor teMax = new TableEditor(tcTable);	
				layetControls.put("tMax", teMax);
				final TableEditor teTarget = new TableEditor(tcTable);
				layetControls.put("tTarget", teTarget);
				final TableEditor teComment = new TableEditor(tcTable);	
				layetControls.put("tComment", teComment);
				final TableEditor teColor = new TableEditor(tcTable);
				layetControls.put("tColor", teColor);
				final TableEditor teType = new TableEditor(tcTable);
				layetControls.put("tType", teType);
				layetControls.put("puid", (TCComponentForm) tableConfigValues[row].selectedComponent);
				vecLayerControl.put(Integer.toString(seqnumber), layetControls);
				double value = 0 ;


				for(  int ColumnNumber =0;ColumnNumber< tableConfigValues[row].propNameValuepair.length;ColumnNumber++ )
				{
					String typeValue  = null;

					propname = tableConfigValues[row].propNameValuepair[ColumnNumber].propName;

					if(propname.equals("u4_type"))
						typeValue =  tableConfigValues[row].propNameValuepair[ColumnNumber].propValue;

					String PropValue = tableConfigValues[row].propNameValuepair[ColumnNumber].propValue;
					int propType =  tableConfigValues[row].propNameValuepair[ColumnNumber].PropertyType;
					TCProperty currTCProp =  tableConfigValues[row].propNameValuepair[ColumnNumber].tcProperty;

					TableEditor editor = new TableEditor(tcTable);

					Text text = new Text(tcTable, SWT.NONE);

					if(ColumnNumber == 0 && legend )
					{   
						TableEditor mandatroyEditor = new TableEditor(tcTable);
						PAMConstant.setMandatory(PropValue,mandatroyEditor,item);
					}											
					if(!(tableConfigValues[row].propNameValuepair[ColumnNumber].isEnabled))
					{
						item.setText(ColumnNumber,PropValue);
						continue;	
					}
					if(isStructuredComponent(currForm)&&tableConfigValues[row].propNameValuepair[ColumnNumber].isStructured)
					{
						item.setText(ColumnNumber,PropValue);
						continue;
					}

					switch (propType)
					{
					case 1:
						text = new Text(tcTable, SWT.NONE);
						if(PropValue!=null )
							text.setText(PropValue);
						editor.grabHorizontal = true;
						editor.setEditor(text,item, ColumnNumber);
						// Added as part of CR#21
						editor.getEditor().setEnabled(enable_editor); 
						text.setData("colId", ColumnNumber);

						if(matlDetails2 || cartonloaded)
							editor.getEditor().setEnabled(false);

						text.addModifyListener(new ModifyListener() {     
							public void modifyText(ModifyEvent event) {
								PAMSecondaryPropValue currentSecPropValues = tableConfigValues[tcTable.indexOf(item)] ;
								compPropVal_m.put("puid",currentSecPropValues.selectedComponent);
								compPropVal_m.put("section",section);
								int columnNum = (Integer) ((Text)(event.getSource())).getData("colId");
								compPropVal_m.put(currentSecPropValues.propNameValuepair[columnNum].propName,((Text) (event.getSource())).getText());
							}      
						});	
						break;
					case 2:
						text = new Text(tcTable, SWT.NONE);
						if(PropValue!=null )
							text.setText(PropValue);
						editor.grabHorizontal = true;
						editor.setEditor(text,item, ColumnNumber);
						// Added as part of CR#21
						editor.getEditor().setEnabled(enable_editor); 
						text.setData("colId", ColumnNumber);

						if(matlDetails2 || cartonloaded)
							editor.getEditor().setEnabled(false);

						text.addModifyListener(new ModifyListener() {     
							public void modifyText(ModifyEvent event) {
								PAMSecondaryPropValue currentSecPropValues = tableConfigValues[tcTable.indexOf(item)] ;
								compPropVal_m.put("puid",currentSecPropValues.selectedComponent);
								compPropVal_m.put("section",section);
								int columnNum = (Integer) ((Text)(event.getSource())).getData("colId");
								compPropVal_m.put(currentSecPropValues.propNameValuepair[columnNum].propName,((Text) (event.getSource())).getText());
							}      
						});	
						break;
					case 3:

						if(formType.equals(LAYERFORM) && propname.equals(MIN)  )
						{
							if(PropValue!=null )
								txtMin.setText(PropValue);

							teMin.grabHorizontal = true;
							teMin.setEditor(txtMin,item, ColumnNumber);	
							// Added as part of CR#21
							teMin.getEditor().setEnabled(enable_editor);
							txtMin.setData("colId", ColumnNumber);
							String type = cbType.getText();

							if(cartonloaded || matlDetails2)
							{
								teMin.getEditor().setEnabled(false);
							}
							else if(type.length() > 0)
							{	
								Hashtable<String, TCComponent> pmltypes =  tableObjects.get(formType);	

								if (pmltypes!=null)
								{
									if(pmltypes.containsKey(type))
										teMin.getEditor().setEnabled(false);
									else								
										teMin.getEditor().setEnabled(true);
								}
							}
							else
								teMin.getEditor().setEnabled(true); 

							txtMin.setText(PropValue);

							txtMin.addModifyListener(new ModifyListener() {     
								public void modifyText(ModifyEvent event) {
									//getting the row index 
									PAMSecondaryPropValue currentSecPropValues = tableConfigValues[tcTable.indexOf(item)] ;
									compPropVal_m.put("puid",currentSecPropValues.selectedComponent);
									compPropVal_m.put("section",section);
									compPropVal_m.put(MIN,txtMin.getText());

								}      
							});	

						}
						else if(formType.equals(LAYERFORM) && propname.equals(MAX)  )
						{
							if(PropValue!=null )
								txtMax.setText(PropValue);

							teMax.grabHorizontal = true;
							teMax.setEditor(txtMax,item, ColumnNumber);
							// Added as part of CR#21
							teMax.getEditor().setEnabled(enable_editor);
							txtMax.setData("colId", ColumnNumber);
							//teMax.getEditor().setEnabled(false);
							String type = cbType.getText();

							if(cartonloaded || matlDetails2)
							{
								teMax.getEditor().setEnabled(false);
							}
							else if(type.length() > 0)
							{	
								Hashtable<String, TCComponent> pmltypes =  tableObjects.get(formType);	

								if (pmltypes!=null)
								{
									if(pmltypes.containsKey(type))
										teMax.getEditor().setEnabled(false);
									else								
										teMax.getEditor().setEnabled(true);
								}
							}
							else
								teMax.getEditor().setEnabled(true); 

							txtMax.setText(PropValue);

							txtMax.addModifyListener(new ModifyListener() {     
								public void modifyText(ModifyEvent event) {
									//getting the row index 
									PAMSecondaryPropValue currentSecPropValues = tableConfigValues[tcTable.indexOf(item)] ;
									compPropVal_m.put("puid",currentSecPropValues.selectedComponent);
									compPropVal_m.put("section",section);
									compPropVal_m.put(MAX,txtMax.getText());

								}      
							});	
						}
						else if(formType.equals(LAYERFORM) && propname.equals(TARGET)  )
						{
							if(PropValue!=null )
								txtTarget.setText(PropValue);

							teTarget.grabHorizontal = true;
							teTarget.setEditor(txtTarget,item, ColumnNumber);
							// Added as part of CR#21
							teTarget.getEditor().setEnabled(enable_editor);
							txtTarget.setData("colId", ColumnNumber);		  
							String type = cbType.getText();

							if(cartonloaded || matlDetails2)
							{
								teTarget.getEditor().setEnabled(false);
							}
							else if(type.length() > 0)
							{	
								Hashtable<String, TCComponent> pmltypes =  tableObjects.get(formType);	

								if (pmltypes!=null)
								{
									if(pmltypes.containsKey(type))
										teTarget.getEditor().setEnabled(false);
									else								
										teTarget.getEditor().setEnabled(true);
								}
							}
							else
								teTarget.getEditor().setEnabled(true);

							txtTarget.setText(PropValue);

							txtTarget.addModifyListener(new ModifyListener() {     
								public void modifyText(ModifyEvent event) {
									//getting the row index 
									PAMSecondaryPropValue currentSecPropValues = tableConfigValues[tcTable.indexOf(item)] ;
									compPropVal_m.put("puid",currentSecPropValues.selectedComponent);
									compPropVal_m.put("section",section);
									compPropVal_m.put(TARGET,txtTarget.getText());

								}      
							});
						}
						else
						{
							text = new Text(tcTable, SWT.NONE);
							if(PropValue!=null )
								text.setText(PropValue);
							editor.grabHorizontal = true;
							editor.setEditor(text,item, ColumnNumber);
							// Added as part of CR#21
							editor.getEditor().setEnabled(enable_editor); 
							text.setData("colId", ColumnNumber);

							if(matlDetails2 || cartonloaded)
								editor.getEditor().setEnabled(false);

							text.addModifyListener(new ModifyListener() {     
								public void modifyText(ModifyEvent event) {
									PAMSecondaryPropValue currentSecPropValues = tableConfigValues[tcTable.indexOf(item)] ;
									compPropVal_m.put("puid",currentSecPropValues.selectedComponent);
									compPropVal_m.put("section",section);
									int columnNum = (Integer) ((Text)(event.getSource())).getData("colId");
									compPropVal_m.put(currentSecPropValues.propNameValuepair[columnNum].propName,((Text) (event.getSource())).getText());
								}      
							});	
						}

						break;
					case 4:
						text = new Text(tcTable, SWT.NONE);
						if(PropValue!=null )
							text.setText(PropValue);
						editor.grabHorizontal = true;
						editor.setEditor(text,item, ColumnNumber);
						// Added as part of CR#21
						editor.getEditor().setEnabled(enable_editor); 
						text.setData("colId", ColumnNumber);

						if(matlDetails2 || cartonloaded)
							editor.getEditor().setEnabled(false);

						text.addModifyListener(new ModifyListener() {     
							public void modifyText(ModifyEvent event) {
								PAMSecondaryPropValue currentSecPropValues = tableConfigValues[tcTable.indexOf(item)] ;
								compPropVal_m.put("puid",currentSecPropValues.selectedComponent);
								compPropVal_m.put("section",section);
								int columnNum = (Integer) ((Text)(event.getSource())).getData("colId");
								compPropVal_m.put(currentSecPropValues.propNameValuepair[columnNum].propName,((Text) (event.getSource())).getText());
							}      
						});	
						break;
					case 5:
						text = new Text(tcTable, SWT.NONE);
						if(PropValue!=null )
							text.setText(PropValue);
						editor.grabHorizontal = true;
						editor.setEditor(text,item, ColumnNumber);
						// Added as part of CR#21
						editor.getEditor().setEnabled(enable_editor); 
						text.setData("colId", ColumnNumber);
						text.setData("section", section);
						if(matlDetails2 || cartonloaded)
							editor.getEditor().setEnabled(false);

						text.addModifyListener(new ModifyListener() {     
							public void modifyText(ModifyEvent event) {
								PAMSecondaryPropValue currentSecPropValues = tableConfigValues[tcTable.indexOf(item)] ;
								compPropVal_m.put("puid",currentSecPropValues.selectedComponent);
								String section = (String) ((Text)(event.getSource())).getData("section");

								if (section!=null)
									compPropVal_m.put("section",section);
								int columnNum = (Integer) ((Text)(event.getSource())).getData("colId");
								compPropVal_m.put(currentSecPropValues.propNameValuepair[columnNum].propName,((Text) (event.getSource())).getText());
							}      
						});	
						break;

					case 6:
						Button checkButton = new Button(tcTable, SWT.CHECK|SWT.CENTER);	
						checkButton.setData("colId", ColumnNumber);
						if(PropValue.equalsIgnoreCase("True"))
							checkButton.setSelection(true);	
						editor.grabHorizontal = true;		
						editor.setEditor(checkButton,item,ColumnNumber);
						// Added as part of CR#21
						editor.getEditor().setEnabled(enable_editor); 

						if(matlDetails2 || cartonloaded)
							editor.getEditor().setEnabled(false);

						checkButton.addSelectionListener(new SelectionAdapter() {     
							public void widgetSelected(SelectionEvent event) {
								PAMSecondaryPropValue currentSecPropValues = tableConfigValues[tcTable.indexOf(item)] ;
								compPropVal_m.put("puid",currentSecPropValues.selectedComponent);
								compPropVal_m.put("section",section);
								int columnNum = (Integer) ((Button)(event.getSource())).getData("colId");
								String isTrue= (((Button) (event.getSource())).getSelection()==true)?"True":"False";
								compPropVal_m.put(currentSecPropValues.propNameValuepair[columnNum].propName,isTrue);
							}      
						});
						break;
					case 7:
						break;
					case 8:
						int textLength = textLimit.get(propname).intValue();

						final PAMSecondaryPropValue currentSecPropValues = tableConfigValues[tcTable.indexOf(item)] ;

						TCComponentListOfValues currLOVValues = null;
						ListOfValuesInfo tempLOVInfo;		
						Object[] lovValues = null;		
						//final LOVService mLOVService = LOVService.getService( session );		
						final InitialLovData mInitData = new InitialLovData( );		
						String[] lovStrings = null;

						if(( currLOVValues = currTCProp.getLOV() )!= null)
						{

							if( isInterdependantLov(currTCProp))
							{								
								mInitData.lov = currLOVValues;
								mInitData.propertyName=currTCProp.getPropertyName();
								mInitData.lovInput.owningObject=currTCProp.getTCComponent();
								LOVSearchResults mInitDataResponse = mLOVService.getInitialLOVValues( mInitData );
								for( LOVValueRow LOVValue : mInitDataResponse.lovValues )
								{
									String mValue = ((String[])LOVValue.propDisplayValues.get( "lov_values" ))[0];

									if( mValue.equalsIgnoreCase( item.getText( )))
									{
										lovValues = new String[LOVValue.childRows.length];
										for( int index = 0; index < LOVValue.childRows.length; index++ )
											lovValues[index] = ((String[])LOVValue.childRows[index].propDisplayValues.get( "lov_values" ))[0];
									}
									if(propname.equalsIgnoreCase("u4_material")&& mValue.equalsIgnoreCase(  currentSecPropValues.getRequiredPAMPropertyValue("u4_layer")))
									{
										lovValues = new String[LOVValue.childRows.length];
										for( int index = 0; index < LOVValue.childRows.length; index++ )
											lovValues[index] = ((String[])LOVValue.childRows[index].propDisplayValues.get( "lov_values" ))[0];

									}	
								}
								/**
								 * 28-Sept-2014: Added following check for "lovValues" as Materials Tab was not getting populated due to one of the property
								 * "u4_type" on U4_ApprvdAdditiveDtlForm is causing NULL assuming its an LOV. Hence, below check is introduced.
								 */
								if (lovValues != null)
								{
									//lovStrings = Arrays.copyOf(lovValues, lovValues.length+1, String[].class);			
									//lovStrings[lovValues.length]="";
									// Enhancement to provide empty dropdown select at the top of the list
									lovStrings = new String[lovValues.length + 1];
									lovStrings[0] = "";
									System.arraycopy(lovValues, 0, lovStrings, 1, lovValues.length);
								}
								else
								{			
									text = new Text(tcTable, SWT.NONE);
									text.setTextLimit(textLength);
									if(PropValue!=null)
										text.setText(PropValue);
									final TableEditor editorNonLOv = new TableEditor(tcTable);
									editorNonLOv.grabHorizontal = true;
									editorNonLOv.setEditor(text,item, ColumnNumber);
									// Added as part of CR#21
									editorNonLOv.getEditor().setEnabled(enable_editor);
									text.setData("colId", ColumnNumber);

									if(matlDetails2)
										editor.getEditor().setEnabled(false);

									text.addModifyListener(new ModifyListener() {     
										public void modifyText(ModifyEvent event) {
											//getting the row index 
											PAMSecondaryPropValue currentSecPropValues = tableConfigValues[tcTable.indexOf(item)] ;
											compPropVal_m.put("puid",currentSecPropValues.selectedComponent);
											compPropVal_m.put("section",section);
											//getting the column number
											int columnNum = (Integer) ((Text)(event.getSource())).getData("colId");

											//adding to the Map
											compPropVal_m.put(currentSecPropValues.propNameValuepair[columnNum].propName,((Text) (event.getSource())).getText());
										}      
									});

								}	

							}	//isInterdependantLov					
							else
							{
								try {
									tempLOVInfo = currLOVValues.getListOfValues();
									lovValues =  tempLOVInfo.getListOfValues();
								} catch (TCException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								// Enhancement to provide empty dropdown select at the top of the list
								lovStrings = new String[lovValues.length + 1];
								lovStrings[0] = "";
								System.arraycopy(lovValues, 0, lovStrings, 1, lovValues.length);

								//lovStrings = Arrays.copyOf(lovValues, lovValues.length+1, String[].class);
								///** fix for the Defect166 to clear the LOV Values if required*/
								//lovStrings[lovValues.length]="";

							}

							CCombo  combo = new CCombo(tcTable,SWT.DROP_DOWN | SWT.READ_ONLY);	
							combo.setData("colId", ColumnNumber);
							//combo.setData("pml", cbPML);

							if(lovStrings!=null)
								combo.setItems( lovStrings);

							if(PropValue!=null && PropValue.length() > 0)
							{
								combo.setText(PropValue);
							}

							editor.grabHorizontal = true;
							editor.setEditor(combo, item, ColumnNumber);
							// Added as part of CR#21
							editor.getEditor().setEnabled(enable_editor); 

							if(matlDetails2)
								editor.getEditor().setEnabled(false);

							combo.addSelectionListener(new SelectionAdapter() {     
								public void widgetSelected(SelectionEvent event) {        
									PAMSecondaryPropValue currentSecPropValues = tableConfigValues[tcTable.indexOf(item)] ;
									compPropVal_m.put("puid",currentSecPropValues.selectedComponent);
									compPropVal_m.put("section",section);
									int columnNum = (Integer) ((CCombo)(event.getSource())).getData("colId");
									compPropVal_m.put(currentSecPropValues.propNameValuepair[columnNum].propName,((CCombo) (event.getSource())).getText());
								}      
							});

						}//currLOVValues						
						else if( formType.equals(LAYERFORM) && propname.equals(TYPE) )
						{							
							String val = null ;
							String prop = null ;

							cbType.setData("colId", ColumnNumber);	
							cbType.setData(NUMBER, number);	
							teType.grabHorizontal = true;
							teType.setEditor(cbType, item, ColumnNumber);
							teType.getEditor().setEnabled(enable_editor);
							cbType.removeAll();	

							Hashtable<String, TCComponent> pmltypes = null ;

							try
							{							
								pmltypes =  tableObjects.get(LAYERFORM);
								val = currForm.getStringProperty(TYPE);
								prop = currForm.getStringProperty(PROP);	

								if(CARTON.equalsIgnoreCase(pamSecType))
									prop = "Layer-Carton";
								else if(labelLit )
									prop = "Layer-Label";
							}
							catch (TCException e)
							{
								e.printStackTrace();
							}	

							ArrayList  nonPMLLIst = new ArrayList<String>(); 

							try
							{ 
								LOVSearchResults layerstructureLOVResult = null ;

								layerstructureLOVResult = (LOVSearchResults) tcTable.getData("layer_structure_lov");

								for( LOVValueRow LOVValue : layerstructureLOVResult.lovValues )
								{
									String mValue = ((String[])LOVValue.propDisplayValues.get( "lov_values" ))[0];

									if( mValue.equalsIgnoreCase( prop))
									{
										lovValues = new String[LOVValue.childRows.length];
										for( int index = 0; index < LOVValue.childRows.length; index++ )
											lovValues[index] = ((String[])LOVValue.childRows[index].propDisplayValues.get( "lov_values" ))[0];

										nonPMLMaterial = Arrays.copyOf(lovValues, lovValues.length, String[].class);
										cbType.setItems(nonPMLMaterial);

										Collections.addAll(nonPMLLIst, nonPMLMaterial);
									}
								}	

							}
							catch (Exception e)
							{									
								e.printStackTrace();
							}

							if(pmltypes != null)
							{
								Enumeration names = pmltypes.keys(); 

								while(names.hasMoreElements())
								{ 
									String name = (String) names.nextElement();
									nonPMLLIst.add(name);
								} 
							}

							Collections.sort(nonPMLLIst);			
							cbType.setItems((String[]) nonPMLLIst.toArray(new String[nonPMLLIst.size()]));
							cbType.add("", 0);

							if(val != null)
								cbType.setText(val);

							if(matlDetails2 || cartonloaded )
								teType.getEditor().setEnabled(false);

							cbType.addSelectionListener(new SelectionAdapter() {     
								public void widgetSelected(SelectionEvent event) {
									String typevalue = ((CCombo) (event.getSource())).getText() ;

									PAMSecondaryPropValue currentSecPropValues = tableConfigValues[tcTable.indexOf(item)] ;
									compPropVal_m.put("puid",currentSecPropValues.selectedComponent);
									compPropVal_m.put("section",section);
									int columnNum = (Integer) ((CCombo)(event.getSource())).getData("colId");
									compPropVal_m.put(currentSecPropValues.propNameValuepair[columnNum].propName,typevalue);

									txtMin.setText("");
									txtMax.setText("");
									txtTarget.setText("");

									String currUID =currentSecPropValues.selectedComponent.getUid();
									String currNumber = "" ;

									try
									{
										currNumber =currentSecPropValues.selectedComponent.getStringProperty(NUMBER);
									}
									catch (TCException e1) 
									{
										// TODO Auto-generated catch block
										e1.printStackTrace();
									}

									if(typevalue.length() > 0)
									{	
										Hashtable<String, TCComponent> pmltypes =  tableObjects.get(formType);	

										if(pmltypes != null )
										{
											TCComponent itemRev =  pmltypes.get(typevalue);		
											TCComponent   compPropForms[] = null ;

											if(pmltypes.containsKey(typevalue))
											{	
												Map<String, Object> pairLayer = getLayerPair(currNumber ,  currUID);

												if(pairLayer != null)
												{
													((TableEditor)pairLayer.get("tMin")).getEditor().setEnabled(false);
													((TableEditor)pairLayer.get("tMax")).getEditor().setEnabled(false);
													((TableEditor)pairLayer.get("tTarget")).getEditor().setEnabled(false);	
												}

												teTarget.getEditor().setEnabled(false);
												teMin.getEditor().setEnabled(false);
												teMax.getEditor().setEnabled(false);

												try
												{
													compPropForms = ((TCComponentItemRevision)itemRev).getRelatedComponents(PMLCOMPREL);

													int count = 0 ;

													for ( int inx=0 ; inx <compPropForms.length ; inx++ )
													{
														if(compPropForms[inx] instanceof TCComponentForm )
														{													
															if(compPropForms[inx].getType().equals(PMLCOMPFORM))
															{	
																String pamUOM = currForm.getStringProperty(UOM);
																String pairpamUOM = ((String)pairLayer.get("uom"));
																String currProp = currForm.getStringProperty(PROP);
																String property = compPropForms[inx].getStringProperty(PROP);
																String pmlUOM = compPropForms[inx].getStringProperty(UOM);

																//if( ( pamUOM.equals(pmlUOM) || pairpamUOM.equals(pmlUOM) )  && ( property.equals(THICKNESS) || property.equals(GRAMMAGE))) // required

																if( property.equals(THICKNESS) || property.equals(GRAMMAGE)) // handle uom issue
																{	
																	double min = compPropForms[inx].getDoubleProperty(MIN);
																	double max = compPropForms[inx].getDoubleProperty(MAX);
																	double target = compPropForms[inx].getDoubleProperty(TARGET);

																	String targetVal = (target > 0) ? Double.toString(target) : "";
																	String minVal = (min > 0) ? Double.toString(min) : "";
																	String maxVal = (max > 0) ? Double.toString(max) : "";

																	//if(pamUOM.equals(pmlUOM)) // required

																	if( property.equals(THICKNESS) && pamUOM.equals("֭")) // handle uom issue
																	{
																		txtMin.setText(minVal);
																		txtMax.setText(maxVal);
																		txtTarget.setText(targetVal);
																		count++;
																	}
																	else if( property.equals(GRAMMAGE) && (!pamUOM.equals("֭"))) //handle uom issue
																	{
																		txtMin.setText(minVal);
																		txtMax.setText(maxVal);
																		txtTarget.setText(targetVal);
																		count++;
																	}
																	else
																	{
																		if(pairLayer != null)
																		{
																			((Text)pairLayer.get("target")).setText(targetVal);
																			((Text)pairLayer.get("min")).setText(minVal);	
																			((Text)pairLayer.get("max")).setText(maxVal);	
																			((CCombo)pairLayer.get("type")).setText(typevalue);
																			count++;

																			resetType(pairLayer);	
																			setTypeValue(pairLayer,typevalue);

																		}
																	}

																	if(count >= 2)
																		break ; 

																}
															}
														}
													}

												} 
												catch (TCException e)
												{
													// TODO Auto-generated catch block
													e.printStackTrace();
												}
											} 
											else
											{	
												teTarget.getEditor().setEnabled(true);
												teMin.getEditor().setEnabled(true);
												teMax.getEditor().setEnabled(true);

												Map<String, Object> pairLayer = getLayerPair(currNumber ,  currUID);

												if(pairLayer != null )
												{
													String pairTypeValue = ((CCombo)pairLayer.get("type")).getText();

													if( pmltypes != null && pmltypes.containsKey(pairTypeValue))
														resetPairLayer(pairLayer);
												}
											}
										}
										else 
										{
											teTarget.getEditor().setEnabled(true);
											teMin.getEditor().setEnabled(true);
											teMax.getEditor().setEnabled(true);
										}
									}
									else
									{
										teTarget.getEditor().setEnabled(true);
										teMin.getEditor().setEnabled(true);
										teMax.getEditor().setEnabled(true);

										Map<String, Object> pairLayer = getLayerPair(currNumber ,  currUID);

										if(pairLayer != null )
										{
											String pairTypeValue = ((CCombo)pairLayer.get("type")).getText();

											Hashtable<String, TCComponent> pmltypes =  tableObjects.get(formType);

											if( pmltypes != null && pmltypes.containsKey(pairTypeValue))
												resetPairLayer(pairLayer);

										}
									}
								}      
							});	
						}
						else if(formType.equals(LAYERFORM) && propname.equals(COLOR))
						{
							teColor.grabHorizontal = true;
							teColor.setEditor(txtColor,item, ColumnNumber);	
							// Added as part of CR#21
							teColor.getEditor().setEnabled(enable_editor);
							txtColor.setData("colId", ColumnNumber);
							teColor.getEditor().setEnabled(true);

							txtColor.setText(PropValue);
							txtColor.setTextLimit(textLength);

							if(matlDetails2 || cartonloaded )
								teColor.getEditor().setEnabled(false);

							txtColor.addModifyListener(new ModifyListener() {     
								public void modifyText(ModifyEvent event) {
									//getting the row index 
									PAMSecondaryPropValue currentSecPropValues = tableConfigValues[tcTable.indexOf(item)] ;
									compPropVal_m.put("puid",currentSecPropValues.selectedComponent);
									compPropVal_m.put("section",section);
									compPropVal_m.put(COLOR,txtColor.getText());

								}      
							});
						}
						else if(formType.equals(LAYERFORM) && propname.equals(COMMENT))
						{
							teComment.grabHorizontal = true;
							teComment.setEditor(txtComment,item, ColumnNumber);	
							// Added as part of CR#21
							teComment.getEditor().setEnabled(enable_editor);
							txtComment.setData("colId", ColumnNumber);
							teComment.getEditor().setEnabled(true);
							txtComment.setText(PropValue);
							txtComment.setTextLimit(textLength);
							if(matlDetails2 || cartonloaded)
								teComment.getEditor().setEnabled(false);

							txtComment.addModifyListener(new ModifyListener() {
								public void modifyText(ModifyEvent event) {
									//getting the row index 
									PAMSecondaryPropValue currentSecPropValues = tableConfigValues[tcTable.indexOf(item)] ;
									compPropVal_m.put("puid",currentSecPropValues.selectedComponent);
									compPropVal_m.put("section",section);
									compPropVal_m.put(COMMENT,txtComment.getText());

								}      
							});

						}
						else
						{			
							text = new Text(tcTable, SWT.NONE);
							text.setTextLimit(textLength);
							if(PropValue!=null)
								text.setText(PropValue);
							editor.grabHorizontal = true;
							editor.setEditor(text,item, ColumnNumber);	
							// Added as part of CR#21
							editor.getEditor().setEnabled(enable_editor); 
							text.setData("colId", ColumnNumber);

							if(matlDetails2 || cartonloaded)
								editor.getEditor().setEnabled(false);

							text.addModifyListener(new ModifyListener() {     
								public void modifyText(ModifyEvent event) {
									PAMSecondaryPropValue currentSecPropValues = tableConfigValues[tcTable.indexOf(item)] ;
									compPropVal_m.put("puid",currentSecPropValues.selectedComponent);
									compPropVal_m.put("section",section);
									int columnNum = (Integer) ((Text)(event.getSource())).getData("colId");
									compPropVal_m.put(currentSecPropValues.propNameValuepair[columnNum].propName,((Text) (event.getSource())).getText());
								}      
							});

						}
						break;

					default:
						text = new Text(tcTable, SWT.NONE);
						text.setText(PropValue);
						editor.grabHorizontal = true;
						editor.setEditor(text,item, ColumnNumber);
						// Added as part of CR#21
						editor.getEditor().setEnabled(enable_editor); 
						text.setData("colId", ColumnNumber);

						if(matlDetails2 || cartonloaded)
							editor.getEditor().setEnabled(false);

						text.addModifyListener(new ModifyListener() {     
							public void modifyText(ModifyEvent event) {
								PAMSecondaryPropValue currentSecPropValues = tableConfigValues[tcTable.indexOf(item)] ;
								compPropVal_m.put("puid",currentSecPropValues.selectedComponent);
								compPropVal_m.put("section",section);
								int columnNum = (Integer) ((Text)(event.getSource())).getData("colId");
								compPropVal_m.put(currentSecPropValues.propNameValuepair[columnNum].propName,((Text) (event.getSource())).getText());
							}      
						});	
						break;			


					} //switch					
				} // for
				
				modifiedLayerPropVals_v.add(compPropVal_m);
			}
		});
	}



	public void PML2PAMCPMapAttrValues(TCComponentItemRevision pml , Map<String, TCComponentForm> pamforms , CTabItem tab)
	{
		Map<String, TCComponentForm> pmlforms = getRelatedForms(pml,PMLCOMPREL);

		Map<String,Object > map = null ;

		try
		{
			for(Map.Entry<String, String> propName : PML2PAMCPMap.entrySet())
			{
				TCComponentForm formsource = pmlforms.get(propName.getKey().toString()) ;
				TCComponentForm formtarget = pamforms.get(propName.getValue().toString()) ;


				if(formsource != null && formtarget != null)
				{
					formtarget.lock();

					Map<String,Object> controls = (Map<String, Object>) tab.getData(propName.getValue().toString());

					if(controls != null)
						map = (Map<String, Object>) controls.get("map");


					for(Map.Entry<String, String> attrName : PML2PAMCPAttrMap.entrySet())
					{
						Object value = formsource.getTCProperty(attrName.getKey()).getUIFValue();

						if(value != null)
							formtarget.setProperty(attrName.getValue(), (String) (value));
						else
							formtarget.setProperty(attrName.getValue(), "");

						if(map != null)
							map.remove(attrName.getValue());
					}

					formtarget.save();
					formtarget.unlock();
					formtarget.refresh();

				}
			}
		}
		catch (TCException e)
		{
			//
			e.printStackTrace();
		}

	}

	public void PML2PAMCPMapAttrValuesClear(Map<String, TCComponentForm> pamforms, CTabItem tab)
	{
		try
		{
			Map<String,Object > map = null ;

			for(Map.Entry<String, String> propName : PML2PAMCPMap.entrySet())
			{
				TCComponentForm formtarget = pamforms.get(propName.getValue().toString()) ;

				if(formtarget != null)
				{
					formtarget.lock();

					Map<String,Object> controls = (Map<String, Object>) tab.getData(propName.getValue().toString());

					if(controls != null)
						map = (Map<String, Object>) controls.get("map");

					for(Map.Entry<String, String> attrName : PML2PAMCPAttrMap.entrySet())
					{
						Object value = "";
						formtarget.setProperty(attrName.getValue(), (String) (value));

						if(map != null)
							map.remove(attrName.getValue());
					}

					formtarget.save();
					formtarget.unlock();
					formtarget.refresh();
				}
			}
		}
		catch (TCException e)
		{
			e.printStackTrace();
		}

	}

	public boolean checkDuplicate(String val , Map<String, Object> map)
	{
		int count = 0 ;
		boolean info = false ;

		Enumeration names = vecLayerControl.keys();

		while(names.hasMoreElements())
		{
			String key = (String) names.nextElement();
			Map<String , Object >  map1 = (Map<String, Object>) vecLayerControl.get(key);

			String  typeValue  = ((CCombo)map1.get("type")).getText();

			if(val.equals(typeValue))
				count++;
		}

		if(count>1)
		{
			info =  true ;

			Hashtable<String, TCComponent> pmltypes = tableObjects.get(LAYERFORM);

			if(pmltypes.containsKey(val))
				if(count<=2)
					info =  false ;

		}

		if(info)
			MessageBox.post(shell , "Selected Type Already Exits","Information",MessageBox.INFORMATION);

		return info ;

	}

	public void  resetPairLayer(Map<String, Object> pairLayer)
	{
		((TableEditor)pairLayer.get("tMin")).getEditor().setEnabled(true);
		((TableEditor)pairLayer.get("tMax")).getEditor().setEnabled(true);
		((TableEditor)pairLayer.get("tTarget")).getEditor().setEnabled(true);
		((Text)pairLayer.get("target")).setText("");
		((Text)pairLayer.get("min")).setText("");
		((Text)pairLayer.get("max")).setText("");
		((CCombo)pairLayer.get("type")).setText("");

		resetType(pairLayer);
		setTypeValue(pairLayer,"");

	}

	public void setTypeValue(Map<String, Object> layer,String value)
	{
		try
		{
			((TCComponentForm)layer.get("puid")).setStringProperty(TYPE, value);
		}
		catch (TCException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void resetType(Map<String, Object> layer)
	{
		for ( int v=0 ;v < modifiedLayerPropVals_v.size() ;v++)
		{
			Map<String, Object> propValueMap = modifiedLayerPropVals_v.get(v);

			TCComponentForm pairForm =  (TCComponentForm) propValueMap.get("puid");
			TCComponentForm form = (TCComponentForm) layer.get("puid");

			if(pairForm == form)
				propValueMap.remove("u4_type");
		}

	}

	@SuppressWarnings("unchecked")
	public  Map<String, Object> getLayerPair(String currNumber , String currUID)
	{
		Map<String, Object> layer = null ;

		Enumeration<String> seqnos = vecLayerControl.keys();

		while(seqnos.hasMoreElements())
		{
			String name = (String) seqnos.nextElement();
			layer = (Map<String, Object>) vecLayerControl.get(name);
			String  newNumber = (String) ( layer).get(NUMBER);
			String  newUID = (String) ( layer).get("uid");

			if(currNumber.equals(newNumber) && (! currUID.equals(newUID)))
				break ;
		}

		return layer ;
	}

	@Override
	public Object getEditableValue() {
		// TODO Auto-generated method stub
		return tableComposite;
	}

	@Override
	public String getProperty() {
		// TODO Auto-generated method stub
		return "object_name";
	}

	@Override
	public boolean isPropertyModified(TCComponent arg0) throws Exception {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isPropertyModified(TCProperty arg0) throws Exception {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void load(TCProperty arg0) throws Exception {
		// TODO Auto-generated method stub

		long startTime = System.currentTimeMillis();

		createTables();

		if(UnileverUtility.isPerfMonitorTriggered == true)
			UnileverUtility.getPerformanceTime(startTime, "EPAMComponentPropertiesTable");
	}

	@Override
	public void load(TCComponentType arg0) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void load(TCPropertyDescriptor arg0) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void setMandatory(boolean arg0) {


	}

	@Override

	public void setModifiable(boolean arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setProperty(String arg0) {

	}

	@Override
	public void setUIFValue(Object arg0) {
		// TODO Auto-generated method stub

	}
	@Override
	public void setVisible(boolean arg0)
	{

	}
	@Override
	
	public TCProperty getPropertyToSave(TCComponent paramTCComponent)
	{
		TCProperty localTCProperty = null;

		if (this.property != null)
		{

			try
			{
				localTCProperty = paramTCComponent.getTCProperty(this.property);
			}
			catch (TCException e)
			{
				e.printStackTrace();
			}
			return getPropertyToSave(localTCProperty);
		}
		this.savable = true;

		for(int inx= 0;inx<modifiedLayerPropVals_v.size();inx++)
		{
			Map<String, Object> propValueMap = modifiedLayerPropVals_v.get(inx);
			TCComponent currComp = (TCComponent) ( propValueMap).get("puid");
			section = (String) propValueMap.get("section");

			if(currComp != null)
				try
			{
					currComp.lock();
			}catch (TCException e1)
			{

				e1.printStackTrace();
			}

			boolean tosave = true ;

			for(Map.Entry<String, Object> propName : propValueMap.entrySet())
			{
				try {
					if (!(propName.getKey().equalsIgnoreCase("puid")) 	&& !(propName.getKey().equalsIgnoreCase("section")))
					{
						if( ! propName.getKey().equals(TYPE) || ( propName.getKey().equals(TYPE) && matlDetails2 == false ) )
						{
							currComp.setProperty(propName.getKey(), (String) (propName.getValue()));
						}
					}
				}catch (TCException e)
				{

					tosave = false ;

					sbErrorMessage.append(section).append(" - ").append(currComp.toString()).append(" - ");
					sbErrorMessage.append(propName.getKey()).append(" : ");
					sbErrorMessage.append(e.getMessage().replace("double", "number")).append("\n");
				}
			}

			if(tosave && currComp != null)
			{
				try
				{
					currComp.save();
					currComp.unlock();
					currComp.refresh();
				}
				catch (TCException e)
				{

				}
			}
		}

		CTabFolder tab1 = getTab(parentComposite) ;
		CTabItem[] tabitems = null ;

		if(tab1 != null)
			tabitems = tab1.getItems();

		TCComponentItemRevision activepml = null ;

		if(tabitems != null)
			for(int tab = 0 ;tab < tabitems.length ;tab++)
				if(tabitems[tab].getText().startsWith("Material"))
					activepml = (TCComponentItemRevision) tabitems[tab].getData("activepml");

		ArrayList<String> compName = new ArrayList<String>();

		if(PML2PAMCPMap != null)
			for(Map.Entry<String, String> propName : PML2PAMCPMap.entrySet())
				compName.add(propName.getKey().toString()) ;

		for(int inx= 0;inx<modifiedPropVals_v.size();inx++)
		{
			Map<String, Object> propValueMap = modifiedPropVals_v.get(inx);
			TCComponent currComp = (TCComponent) ( propValueMap).get("puid");
			section = (String) propValueMap.get("section");

			String oName = "" ;
			if(currComp != null)
				try {
					oName = currComp.getStringProperty("object_name");
				}catch (TCException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}

			boolean save = false ;
			boolean tosave = true ;

			if( activepml == null)
			{
				save = true ;
			}
			else if( section != null &&  section.startsWith("Component") &&  (! compName.contains(oName)))
			{
				save = true ;
			}
			else if(  section == null || (  ! section.startsWith("Component")))
			{
				save = true ;
			}

			if(save)
			{
				if(currComp != null)
					try {
						currComp.lock();
					}catch (TCException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

				for(Map.Entry<String, Object> propName : propValueMap.entrySet())
				{
					try {
						if (!(propName.getKey().equalsIgnoreCase("puid"))
								&& !(propName.getKey().equalsIgnoreCase("section")))
						{

							currComp.setProperty(propName.getKey(), (String) (propName.getValue()));

						}
					}catch (TCException e) {
						// TODO Auto-generated catch block
						tosave = false ;

						sbErrorMessage.append(section).append(" - ").append(currComp.toString()).append(" - ");
						sbErrorMessage.append(propName.getKey()).append(" : ");
						sbErrorMessage.append(e.getMessage().replace("double", "number")).append("\n");
					}
				}

				if(tosave &&  currComp != null)
				{
					try {
						currComp.save();
						currComp.unlock();
						currComp.refresh();
					}catch (TCException e) {
						// TODO Auto-generated catch block
					}

				}

			}

		}

		//modifiedPropVals_v.clear();

		return null;
	}
	
	protected boolean isCurrentVecHasNullItems(VecStruct vecStruct)
	{
		for(int i = 0;i < vecStruct.stringVec.length;i++)
		{
			if(vecStruct.stringVec[i] == null)
				return true;
		}
		
		return false;
	}
	
	@Override
	public void save(TCProperty paramTCProperty)
	{
		try
		{
			TCProperty localTCProperty = getPropertyToSave(paramTCProperty);
			if ((this.savable) && (localTCProperty != null)) {
				localTCProperty.getTCComponent().setTCProperty(paramTCProperty);
			}
			setDirty(false);

		}catch (TCException e) {
			// TODO Auto-generated catch block
			sbErrorMessage.append(e.getMessage()).append("\n");
		}
	}

	@Override
	public TCProperty saveProperty(TCComponent paramTCComponent)
			throws Exception
	{
		long startTime = System.currentTimeMillis();

		sbErrorMessage = new StringBuffer();

		//sbErrorMessage.append(section).append("\n");
		TCProperty localTCProperty = getPropertyToSave(paramTCComponent);
		if (this.savable)
		{
			if(sbErrorMessage.length() > 0)
				MessageBox.post(AIFUtility.getActiveDesktop().getShell(), sbErrorMessage.toString() ,"Error", MessageBox.ERROR);

			return localTCProperty;
		}
		setDirty(false);

		if(sbErrorMessage.length() > 0)
			MessageBox.post(AIFUtility.getActiveDesktop().getShell(), sbErrorMessage.toString() ,"Error", MessageBox.ERROR);

		sbErrorMessage = null ;

		if(UnileverUtility.isPerfMonitorTriggered == true)
		{
			UnileverUtility.getPerformanceTime(startTime, "saveProperty in EPAMComponentPropertiesTable");
		}

		return null;
	}

	@Override
	public TCProperty getPropertyToSave(TCProperty arg0) {
		// TODO Auto-generated method stub
		long startTime = System.currentTimeMillis();

		for(int inx= 0;inx<modifiedPropVals_v.size();inx++)
		{
			Map<String, Object> propValueMap = modifiedPropVals_v.get(inx);
			TCComponent currComp = (TCComponent) ( propValueMap).get("puid");
			section = (String) propValueMap.get("section");

			for(Map.Entry<String, Object> propName : propValueMap.entrySet())
			{
				try
				{
					if(!(propName.getKey().equalsIgnoreCase("puid")) && !(propName.getKey().equalsIgnoreCase("section")))
						currComp.setProperty(propName.getKey(), (String) (propName.getValue()));
				}catch (TCException e) {
					// TODO Auto-generated catch block
					sbErrorMessage.append(section).append(" - ").append(currComp.toString()).append(" - ");
					sbErrorMessage.append(propName.getKey()).append(" : ");
					sbErrorMessage.append(e.getMessage().replace("double", "number")).append("\n");

				}
			}
		}
		if(UnileverUtility.isPerfMonitorTriggered == true)
		{
			UnileverUtility.getPerformanceTime(startTime, "getPropertyToSave in EPAMComponentPropertiesTable");
		}
		return null;
	}
		
	public void getAttributeTMdetails(String name)
	{
		TCComponentQueryType queryType;
    	TCComponent[] frameForm = null;
    	String tmvalues = null ;
    	
    	if(name.equalsIgnoreCase("U4_PaperNBoardRevision"))
    		name = "Paper N Board";
    	else if (name.equalsIgnoreCase("U4_PNGResinRevision"))
    		name = "PNG Resin";
    	else if (name.equalsIgnoreCase("U4_SubstrateRevision"))
    		name= "PNG Substrate";
    	else if (name.equalsIgnoreCase("U4_DecorationRevision"))
    		name = "Decoration Chassis";

    	try
		{
			queryType = ( TCComponentQueryType )session.getTypeComponent( "ImanQuery" );
			TCComponentQuery query = ( TCComponentQuery )queryType.find( "__U4_TestMethod..." );
			
			String[] names = {"Type","Name"};
			String[] values ={"Test Method",name};
			frameForm =  query.execute(names,values );
			
			if( frameForm != null && frameForm.length == 1 )
		    {
				TCProperty prop = frameForm[0].getTCProperty("u4_applicable_properties");
				TCProperty prop1 = frameForm[0].getTCProperty("u4_methods");
				String[] attrarray  = prop.getStringValueArray();
				String[] tmarray  = prop1.getStringValueArray();
				
			
				for(int inx=0;inx<attrarray.length;inx++)
				{
					if(attrTMvalues.containsKey(attrarray[inx]))
						(attrTMvalues.get(attrarray[inx])).add(tmarray[inx]);
					else
					{
						Vector<String> vec = new Vector<String>();
						vec.add(tmarray[inx]);
						attrTMvalues.put(attrarray[inx], vec);
					}
				}
				
				if(tmarray.length > 0) tmvalues = tmarray[0];
				for(int inx=1;inx<tmarray.length;inx++)
					tmvalues = tmvalues + ";" + tmarray[inx];
				String[] values1 ={"Test Method",tmvalues};
				frameForm =  query.execute(names,values1 );
				
				for(int inx=0 ;inx < frameForm.length ;inx++)
				{
					TMDetails tmdetails = new TMDetails();
					tmdetails.name = frameForm[inx].getStringProperty("object_name");
					tmdetails.desc = frameForm[inx].getStringProperty("object_desc");
					tmdetails.url  = frameForm[inx].getStringProperty("u4_link");
					TCProperty propcs = frameForm[inx].getTCProperty("u4_conditions");
					if(propcs.getStringValueArray().length > 0)
						tmdetails.csets = new Vector(Arrays.asList(propcs.getStringValueArray()));
					else
						tmdetails.csets = new Vector<String>();
					
					TMData.put(frameForm[inx].toString(), tmdetails);
				}
		    }

		}	
    	
		catch (Exception e) {
			e.printStackTrace();
		}
	
	}

	public Map<String,String> getConditionValues2(String methodName,String propertyValues)
	{
		TCComponentQueryType queryType;
    	TCComponent[] resultTestMethodForms = null;

    	try
		{
			queryType = ( TCComponentQueryType )session.getTypeComponent( "ImanQuery" );
			TCComponentQuery query = ( TCComponentQuery )queryType.find( "__U4_TestMethod..." );

			String[] names = {"Type","u4_method_name","u4_applicable_properties"};
			String[] values ={"Test Method",methodName,propertyValues};
			resultTestMethodForms =  query.execute(names,values );
			if( resultTestMethodForms != null && resultTestMethodForms.length > 0 )
			{
				String[] conditionStrings = new String[resultTestMethodForms.length];

				Map<String,String> conditionSets = new HashMap<String,String>();
				for(int inx=0;inx<resultTestMethodForms.length;inx++)
				{
					conditionStrings[inx] = resultTestMethodForms[inx].getPropertyDisplayableValue("u4_conditions");
					String[] tempconditionsArray = SplitUsingTokenizer(conditionStrings[inx], "-");

					if(conditionSets.containsKey(tempconditionsArray[0]))
					{
						String prevConditionVal = conditionSets.get(tempconditionsArray[0]);
						String currentConditionVal = prevConditionVal+ ","+tempconditionsArray[1];
						conditionSets.remove(tempconditionsArray[0]);
						conditionSets.put(tempconditionsArray[0],currentConditionVal);
					}

					else
						conditionSets.put(tempconditionsArray[0], tempconditionsArray[1]);

				}
				return conditionSets;
			}
		}


		catch (Exception e) {
			e.printStackTrace();
		}
		return null;


	}

	public Map<String,String> getConditionValues(String methodName,String propertyValues)
	{
		TCComponentQueryType queryType;
		TCComponent[] resultTestMethodForms = null;

		try
		{
			queryType = ( TCComponentQueryType )session.getTypeComponent( "ImanQuery" );
			TCComponentQuery query = ( TCComponentQuery )queryType.find( "__U4_TestMethod..." );

			String[] names = {"Type","u4_method_name","u4_applicable_properties"};
			String[] values ={"Test Method",methodName,propertyValues};
			resultTestMethodForms =  query.execute(names,values );
			if( resultTestMethodForms != null && resultTestMethodForms.length > 0 )
			{
				Map<String,String> conditionSets = new HashMap<String,String>();

				String conditionStrings = resultTestMethodForms[0].getPropertyDisplayableValue("u4_conditions");
				String[] tempSpiltString = SplitUsingTokenizer(conditionStrings, ",");

				for(int inx=0;inx<tempSpiltString.length;inx++)
				{
					String[] tempconditionsArray = SplitUsingTokenizer(tempSpiltString[inx], "-");
					if(conditionSets.containsKey(tempconditionsArray[0]))
					{
						String prevConditionVal = conditionSets.get(tempconditionsArray[0]);
						String currentConditionVal = prevConditionVal+ ","+tempconditionsArray[1];
						conditionSets.remove(tempconditionsArray[0]);
						conditionSets.put(tempconditionsArray[0],currentConditionVal);
					}

					else
						conditionSets.put(tempconditionsArray[0], tempconditionsArray[1]);
				}

				return conditionSets;
			}
		}

		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	public static String[] SplitUsingTokenizer(String subject, String delimiters) {
		StringTokenizer strTkn = new StringTokenizer(subject, delimiters);
		ArrayList<String> arrLis = new ArrayList<String>(subject.length());

		while(strTkn.hasMoreTokens())
			arrLis.add(strTkn.nextToken());

		return arrLis.toArray(new String[0]);
	}

	public boolean isStructuredComponent(TCComponent inputComp)
	{
		try {
			TCComponent[] relatedStructuredComponents = pamSpecRevision.getRelatedComponents(PAMConstant.STRUCTURED_RELATION);
			List<TCComponent> relatedStructuredComponentsList = Arrays.asList(relatedStructuredComponents);
			if(relatedStructuredComponentsList.contains(inputComp))
			{
				return true;
			}
		}catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	public Vector<TCComponent> sortForms(Vector<TCComponent> unSortedForms,String sortProperty,String[] SortPropertyValues)
	{
		Vector<TCComponent> sortedForms = new Vector<TCComponent>();

		for(int inx=0;inx<SortPropertyValues.length;inx++)
		{
			for(int jnx=0;jnx<unSortedForms.size();jnx++)
			{
				try {
					if(SortPropertyValues[inx].equalsIgnoreCase(unSortedForms.get(jnx).getProperty(sortProperty)))
					{
						sortedForms.add(unSortedForms.get(jnx));
						//break;
					}
				}catch (TCException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		return sortedForms;
	}

	public Vector<TCComponent> getRelatedComponentOfASecondayType(Map<String, List<TCComponent>> secondaryComponents, String relation, String secondaryType)
	{
		Vector<TCComponent> relatedSecondaryComponent= new Vector<TCComponent>();

		if (secondaryComponents.containsKey(secondaryType))
		{
			List<TCComponent> lst = (List<TCComponent>)secondaryComponents.get(secondaryType);
			relatedSecondaryComponent.addAll(lst);
		}

		return relatedSecondaryComponent;

	}

	public boolean isInterdependantLov(TCProperty currTCProp)
	{
		if(currTCProp.getPropertyName().equalsIgnoreCase( "u4_type" )&&
				!(PAMConstant.APPROVED_ADD_DETAILS_FORM_TYPE_NAME.equals(currTCProp.getTCComponent().getType())||
					PAMConstant.APPROVED_POLY_DETAILS_LOCAL_FORM_TYPE_NAME.equals(currTCProp.getTCComponent().getType())||
						PAMConstant.APPROVED_POLY_DETAILS_FORM_TYPE_NAME.equals(currTCProp.getTCComponent().getType()) ||
							PAMConstant.MATERIAL_DETAILS_FORM_TYPE_NAME.equals(currTCProp.getTCComponent().getType())   ||
							   PAMConstant.ENV_PACK_MATL_FORM_TYPE.equals(currTCProp.getTCComponent().getType())   ||
								PAMConstant.PML_MATL_PROP_FORM.equals(currTCProp.getTCComponent().getType())))
			return true;
		else if(currTCProp.getPropertyName().equalsIgnoreCase( "u4_where_add" ))
			return true;
		else if(currTCProp.getPropertyName().equalsIgnoreCase( "u4_material" )&& PAMConstant.PACK_LAYER_STRUCTURE_FORM_TYPE_NAME.equals(currTCProp.getTCComponent().getType()))
			return true;
		else
			return false;
	}
	
	public void populateTradeNameValues(TCComponent comp , Text txtTN , Text txtValue)
	{
		
		if( comp != null && txtTN != null && txtValue != null)
		{
			try
			{
				txtTN.setText(comp.getTCProperty(GRADE).toString());
				txtValue.setText(comp.getTCProperty(GVALUE).toString());
				txtTN.setEnabled(false);
				txtValue.setEnabled(false);
				
			}catch (TCException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else
		{
			if(txtTN != null) txtTN.setEnabled(true);
			if(txtValue != null) txtValue.setEnabled(true);
			if(txtTN != null) txtTN.setText("");
			if(txtValue != null) txtValue.setText("");
		}
	}

	
    private void resetLayerValues(boolean enable , boolean clear)
    {
		Enumeration names = vecLayerControl.keys();
		
		while(names.hasMoreElements())
		{
			String key = (String) names.nextElement();
			Map<String , Object > map = (Map<String, Object>) vecLayerControl.get(key);
			
			if(true)
			{
				((Text)map.get("target")).setText("");
				((Text)map.get("max")).setText("");
				((Text)map.get("min")).setText("");
				((Text)map.get("color")).setText("");
				((Text)map.get("comment")).setText("");
				((CCombo)map.get("type")).setText("");
			}
			
			((TableEditor)map.get("tMin")).getEditor().setEnabled(true);
			((TableEditor)map.get("tMax")).getEditor().setEnabled(true);
			((TableEditor)map.get("tTarget")).getEditor().setEnabled(true);
			((TableEditor)map.get("tColor")).getEditor().setEnabled(true);
			((TableEditor)map.get("tComment")).getEditor().setEnabled(true);
			((CCombo)map.get("type")).setEnabled(true);
			
			try
			{
				((TCComponentForm)map.get("puid")).setStringProperty(TYPE, "");
			}
			catch (TCException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
    }

    private void populateLayerValues(TCComponent pmlobject , String formtype)
    {
		TCComponent[] layers = null;
		try
		{
			layers = pmlobject.getRelatedComponents(PMLLAYERREL);
			for(int inx=0;inx<layers.length;inx++)
			{
				if((layers[inx].getType()).equalsIgnoreCase(formtype))
				{
					int pmlLayerSeq = layers[inx].getIntProperty(SEQNO);
					String color  = layers[inx].getStringProperty(PMLCOLOR);
					color =  (color == null) ? "" : color ;
					String type  = layers[inx].getStringProperty(TYPE);
					type =  (type == null) ? "" : type ;
					String comment  = layers[inx].getStringProperty(COMMENT);
					comment =  (comment == null) ? "" : comment ;
					double min = layers[inx].getDoubleProperty(MIN);
					double max = layers[inx].getDoubleProperty(MAX);
					double target = layers[inx].getDoubleProperty(TARGET);
					String key = Integer.toString(pmlLayerSeq) ;
					
					if(pmlLayerSeq > 0)
					{
						if(vecLayerControl.containsKey(key))
						{
							Map<String , Object > map = (Map<String, Object>) vecLayerControl.get(key);

							if(target > 0)
								((Text)map.get("target")).setText(Double.toString(target));
							else
								((Text)map.get("target")).setText("");
							
							if(max > 0)
								((Text)map.get("max")).setText(Double.toString(max));
							else
								((Text)map.get("max")).setText("");
							
							if(min > 0)
								((Text)map.get("min")).setText(Double.toString(min));
							else
								((Text)map.get("min")).setText("");
							
							((Text)map.get("color")).setText(color);
							((Text)map.get("comment")).setText(comment);
							((CCombo)map.get("type")).setText(type);
							((CCombo)map.get("type")).setEnabled(false);
							((TableEditor)map.get("tMin")).getEditor().setEnabled(false);
							((TableEditor)map.get("tMax")).getEditor().setEnabled(false);
							((TableEditor)map.get("tTarget")).getEditor().setEnabled(false);
							((TableEditor)map.get("tColor")).getEditor().setEnabled(false);
							((TableEditor)map.get("tComment")).getEditor().setEnabled(false);

							try
							{
								((TCComponentForm)map.get("puid")).setStringProperty(TYPE, type);
							}
							catch (TCException e)
							{
								// TODO Auto-generated catch block
								e.printStackTrace();
							}					 	
						}
					}
				}
			}
			
		}catch (TCException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}	
	
    }

    public void resetcartoncontrols(TCComponentForm form , TableItem item)
    {
    	 try
  	   {
  		   if(form.getStringProperty(PROP).equals(BOARDGRADE) && pamSpecRevision.isCheckedOut() == true)
				{
  			   Text txtTN = (Text) item.getData(TNAME);
  			   if ( txtTN != null ) txtTN.setEnabled(false);
  			   Text txtValue = (Text) item.getData(TNVALUE);
  			   if ( txtValue != null ) txtValue.setEnabled(false);
  			   Text txtComments = (Text) item.getData(COMMENT);
  			   if ( txtComments != null ) txtComments.setEnabled(true);
				}
  	   }catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		   }
    }

}
