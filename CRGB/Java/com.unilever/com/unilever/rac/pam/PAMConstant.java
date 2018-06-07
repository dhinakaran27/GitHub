package com.unilever.rac.pam;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TableItem;

import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentForm;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCProperty;

public class PAMConstant {
	
	public static final String CONFIGURATIONS="_PAM_Table_Configuration";
	
	public static final String COMPONENT_CONFIGURATIONS="_Component_Table_Configuration";
	
	public static final String COLUMNCONFIGURATION="ColumnConfiguration";
	
	public static final String ROWCONFIGURATION="RowConfiguration";
	
	public static final String ROWORDERING="RowOrdering";
	
	public static final String STRUCTUREDPROPERTIES="StructuredProperties";
	
	public static final String PRIMARYOBJECTS="PrimaryObject";
	
	public static final String SECONDARYOBJECTS="SecondaryObject";
	
	public static final String FREETEXTFORM ="U4_FreeTextForm";
	
	public static final String PIPE = "|";
	
	public static final String COLON = ":";
	
	public static final String COMA = ",";	
	
	public static final String TILDA = "~";
	
	public static final String PACKAGE_DETAILS_RELATION = "U4_PackageDetailsRelation";
	
    public static final String COMPONENTSPREF_3D = "U4_3DPackComponentTypes";
  
    public static final String STRUCTURED_RELATION = "U4_StructuredPropRelation";
    
    public static final String PCUSESTEMPLATEATTR = "u4_pack_comp_uses_template";
    
    public static final String CONSUMER_UNIT = "u4_consumer_unit";
    
    public static final String APPROVED_ADD_DETAILS_FORM_TYPE_NAME	=	"U4_ApprvdAdditiveDtlForm";
    
    public static final String APPROVED_POLY_DETAILS_FORM_TYPE_NAME	=	"U4_ApprvdPolymerDetailForm";
    
    public static final String APPROVED_POLY_DETAILS_LOCAL_FORM_TYPE_NAME	=	"U4_ApprvdPolymerDetailLForm";
    
    public static final String MATERIAL_DETAILS_FORM_TYPE_NAME	=	"U4_MaterialDetailsForm";
    
    public static final String PACK_LAYER_STRUCTURE_FORM_TYPE_NAME	=	"U4_PackLayerStructureForm";
    
    public static final String ENV_PACK_MATL_FORM_TYPE               = "U4_PMLEnvPackMatForm";
    
    public static final String PML_MATL_PROP_FORM        	="U4_PMLMatlPropForm";
    
    //defect#773 fix
    public static final String PNP_CUC_DEFECTS_RELATION_NAME		= "U4_CUCRelation";
    public static final String PNP_CU_DEFECTS_RELATION_NAME			= "U4_CURelation";
    public static final String PNP_CASEBAGETC_DEFECTS_RELATION_NAME	= "U4_CaseBagEtcRelation";
    public static final String PNP_PALLET_DEFECTS_RELATION_NAME		= "U4_PalletDefectRelation";
    
    public static final String PNP_CUC_DEFECTS_TABLE_NAME			= "Consumer Unit Content Packaging Defects";
    public static final String PNP_CU_DEFECTS_TABLE_NAME			= "Consumer Unit Packaging Defects";
    public static final String PNP_CASEBAGETC_DEFECTS_TABLE_NAME	= "Case/Bag, etc Packaging Defects";
    public static final String PNP_PALLET_DEFECTS_TABLE_NAME		= "Pallet Packaging Defects"; 
    
    public static void setMandatory(String PropValue , TableEditor editor , TableItem item)
    {    
		Display display = AIFUtility.getActiveDesktop().getShell().getDisplay() ;
		StyledText txt = new StyledText(item.getParent(), SWT.NONE);
	    txt.setText("*"+PropValue);		
	    Color red = display.getSystemColor(SWT.COLOR_DARK_RED);	
	    Font font = new Font(display, "Courier", 14, SWT.BOLD);
	    StyleRange style = new StyleRange();
	    style.start = 0;
	    style.length = 1;
	    style.foreground = red;
	    style.fontStyle = SWT.BOLD;
	    style.font = font ;
	    txt.setStyleRange(style);	  
	    editor.grabHorizontal = true;
	    editor.setEditor(txt,item, 0);
	    editor.getEditor().setEnabled(false);	
    }
    
    public static Map<String,Integer> getTextLimit( PAMSecondaryPropValue  tableConfigValues )
    {  
    	Map<String,Integer > textLimit = new HashMap<String,Integer>();	
    	TCComponentForm form = (TCComponentForm) tableConfigValues.selectedComponent;
        	
    	for(  int inc =0;inc< tableConfigValues.propNameValuepair.length;inc++ )
    	{
    		String propname = tableConfigValues.propNameValuepair[inc].propName;
    		TCProperty currTCProp =  tableConfigValues.propNameValuepair[inc].tcProperty;	    		
			int propType = tableConfigValues.propNameValuepair[inc].PropertyType;
			
			if(propType == 8)
				textLimit.put(propname, currTCProp.getPropertyDescription().getMaxLength());
    	}	
    	
    	return textLimit ;	

    }
    
    public static boolean ruleValidate(TCComponentForm form)
    {
    	
		String rule = null ;
		
		try 
		{
			TCProperty prop = form.getTCProperty("u4_validation_rule");
			
			if (prop!=null)
				rule = prop.toString();
		}
		catch (TCException e)
		{
			return false;
		}
	
		if(rule == null )
		{
			return false ;	
		}
		else 
		{
			if(rule.length() > 4 && ( rule.contains("M:") || rule.contains("M-TMM") || rule.contains("M-LWHD")))
			{
				return true ;
			}
		}
		
		return false;
    }
    
    public static String getFrameValue(TCComponentItemRevision revision) 
    {
    	
    	String frame = null ;
    	String attribute = "u4_pam_frame_type";
    	String parent = revision.getTypeComponent().getParent().toString();
    	
    	if(parent.equalsIgnoreCase("U4_ComponentRevision"))
    	{
    		try {
				TCComponent comp = revision.getRelatedComponent("U4_MatlClassRelation");
				frame = comp.getStringProperty(attribute);
			} catch (TCException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	else if(parent.equalsIgnoreCase("U4_PAMRevision"))
    	{
    		try {
				frame =  revision.getStringProperty(attribute);				
			} catch (TCException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	else if(parent.equalsIgnoreCase("U4_PMLRevision"))
    	{
    		try {
				frame =  revision.getStringProperty("object_type");
			} catch (TCException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

    	} 
    	
    	return frame ;
    	
    }
    
    public static String getTechnologyValue(TCComponentItemRevision revision) 
    {    	
    	String technology = null ;
    	String attribute = "u4_technology";
    	String parent = revision.getTypeComponent().getParent().toString();
    	
    	if(parent.equalsIgnoreCase("U4_ComponentRevision"))
    	{
    		try {
				TCComponent comp = revision.getRelatedComponent("U4_MatlClassRelation");
				technology = comp.getStringProperty(attribute);
			} catch (TCException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	else if(parent.equalsIgnoreCase("U4_PAMRevision"))
    	{
    		try {
    			technology =  revision.getStringProperty(attribute);				
			} catch (TCException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	} 
    	
    	return technology ;
    	
    }
}
