package com.unilever.rac.ui.environmentalreport;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFDataFormat;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;

import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.commands.cut.CutOperation;
import com.teamcenter.rac.commands.delete.DeleteOperation;
import com.teamcenter.rac.commands.newdataset.NewDatasetOperation;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentDatasetType;
import com.teamcenter.rac.kernel.TCComponentForm;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentTcFile;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.Registry;
import com.unilever.rac.pam.PAMConstant;
import com.unilever.rac.ui.common.UL4Common;
import com.unilever.rac.util.UnileverQueryUtil;
import com.unilever.rac.util.UnileverUtility;

public class TotalPackImpactReportOperation extends AbstractAIFOperation {

	XSSFSheet sheet = null;
	ArrayList<String> attributes = null;
	XSSFWorkbook workbook = null;
	String descText = null;
	TCSession session = null;
	TCComponentItemRevision projRev = null;
	Registry reg = null;
	int noOfPriPacks = 0;
	ArrayList<TCComponentForm> EnvMatlForms = null;
	ArrayList<ComponentData> compObjs = null;
	AIFComponentContext[] gabiForms = null;
	AIFComponentContext[] gabiscforms = null;
	AIFComponentContext [] RRIMapForms = null;
	AIFComponentContext [] RRISCForms = null;
	AIFComponentContext [] EPRMapping = null;
	AIFComponentContext [] EPRSCForms = null;
	AIFComponentContext [] RRIProxyForms = null;
	String primary_pack_list[]=null;
	String [] keycountries = null;
	boolean WtZero = false;
	boolean WtLess1 = false;
	
	TCComponentItemRevision itemRevision = null;
	Map<TCComponentItemRevision, Double> refCompRevs;
	Map<TCComponentItemRevision, Double> pack1CompRevs;
	Map<TCComponentItemRevision, Double> pack2CompRevs;
	Map<TCComponentItemRevision, Double> pack3CompRevs;
	Map<TCComponentItemRevision, Double> pack4CompRevs;
	String[] pack_id_labels = null;
	String description = null;
	int no_of_pri_packs=0;
	
	boolean savereport = false;

	public void totalRep(){

	}

	public void genEcelrep(TCComponentItemRevision itemRevision, Map<TCComponentItemRevision, Double> refCompRevs, 
			Map<TCComponentItemRevision, Double> pack1CompRevs, 
			Map<TCComponentItemRevision, Double> pack2CompRevs, 
			Map<TCComponentItemRevision, Double> pack3CompRevs, 
			Map<TCComponentItemRevision, Double> pack4CompRevs, 
			String[] pack_id_labels, String description, int no_of_pri_packs,boolean savereport) throws IOException 
			{
		this.itemRevision = itemRevision;
		this.refCompRevs=refCompRevs;
		this.pack1CompRevs=pack1CompRevs;
		this.pack2CompRevs=pack2CompRevs;
		this.pack3CompRevs=pack3CompRevs;
		this.pack4CompRevs=pack4CompRevs;
		this.pack_id_labels = pack_id_labels;
		this.description = description;
		this.no_of_pri_packs=no_of_pri_packs;
		this.savereport = savereport;
			}

	private ComponentData [] processItemData(TCComponentItemRevision refItemrev, boolean is_refItem, double quantity){
		double waste_to_landfill = 0.0;
		double ghg = 0.0;
		double ghg_disposal = 0;
		double epr_tax = 0.0;
		ComponentData [] cmpRefData = null;
		TCComponentItemRevision pamrev = null;
		try{
			//get envirnomental packaging material forms
			EnvMatlForms = new ArrayList<TCComponentForm>(); 
			AIFComponentContext[] envforms1 = null;
			envforms1 = refItemrev.getRelated(UL4Common.ENVIRNOMENTAL_RELATION);
			if(envforms1.length>0){
				for(int j=0;j<envforms1.length;j++){
					TCComponentForm frm = (TCComponentForm)envforms1[j].getComponent();
					if (frm!=null){
						if (frm.getType().toString().compareTo("U4_EnvPackagingMatlForm") ==0)
							EnvMatlForms.add(frm);
					}
				}		
			}
			else{
				//get envirnomental layer forms from pam revision
				AIFComponentContext[] pam = refItemrev.getRelated(UL4Common.PAM_SPECIFICATION_RELATION);
				if(pam.length>0){
				pamrev = (TCComponentItemRevision)pam[0].getComponent();
				envforms1 = pamrev.getRelated(UL4Common.ENVIRNOMENTAL_RELATION);
				if(envforms1.length>0){
					for(int j=0;j<envforms1.length;j++){
						TCComponentForm frm = (TCComponentForm)envforms1[j].getComponent();
						if (frm!=null){
							if (frm.getType().toString().compareTo("U4_EnvPackagingMatlForm") ==0)
								EnvMatlForms.add(frm);
						}
					}
				}
				}
			}
			System.out.println("Processing item:"+refItemrev.getProperty("object_name"));
			String [] obj_str = refItemrev.getProperty("object_string").split("-");
			AIFComponentContext[] refweightforms = refItemrev.getRelated(UL4Common.WEIGHTRELATION);
			//String [] keycountries = projRev.getProperty("u4_key_countries").split(",");
			double dbl_wt = 0;
			boolean is_primary = is_comp_primary(refItemrev);
			
			TCComponentForm Weightform = null;
			
			cmpRefData = new ComponentData[keycountries.length];
			
			if(refweightforms.length==1)
			{
				Weightform = (TCComponentForm) refweightforms[0].getComponent();
				dbl_wt = Weightform.getDoubleProperty("u4_target");
			}
			else 
			{
				if (pamrev==null)
				{
					AIFComponentContext[] pam = refItemrev.getRelated(UL4Common.PAM_SPECIFICATION_RELATION);
					if(pam.length>0){
						pamrev = (TCComponentItemRevision)pam[0].getComponent();
					}
				}

				if (pamrev !=null)
				{
					AIFComponentContext[] compRelForms = pamrev.getRelated("U4_CompPropertyRelation");

					if (compRelForms!=null)
					{
						if (Weightform == null )
						{
							for(AIFComponentContext frm:compRelForms){
								TCComponentForm formtype = (TCComponentForm) frm.getComponent();
								if(formtype.getProperty(UL4Common.OBJECT_NAME).equals("Weight/Unit")){
									Weightform = formtype;
									String weight_value = Weightform.getProperty("u4_target");
									if (weight_value!=null && (weight_value.equals("")==false))
										dbl_wt = Double.parseDouble(weight_value);
									break;
								}
							}
						}
						
						if (Weightform == null )
						{
							for(AIFComponentContext frm:compRelForms){
								TCComponentForm formtype = (TCComponentForm) frm.getComponent();
								if(formtype.getProperty(UL4Common.OBJECT_NAME).equals("Weight")){
									Weightform = formtype;
									String weight_value = Weightform.getProperty("u4_target");
									if (weight_value!=null && (weight_value.equals("")==false))
										dbl_wt = Double.parseDouble(weight_value);
									break;
								}
							}
						}

						if (Weightform == null)
						{
							for(AIFComponentContext frm:compRelForms){
								TCComponentForm formtype = (TCComponentForm) frm.getComponent();
								if(formtype.getProperty(UL4Common.OBJECT_NAME).equals("Weight_Label")){
									Weightform = formtype;
									String weight_value = Weightform.getProperty("u4_target");
									if (weight_value!=null && (weight_value.equals("")==false))
										dbl_wt = Double.parseDouble(weight_value);
									break;
								}
							}
						}
					}
				}
			}
			if(!is_primary){
				quantity = quantity/noOfPriPacks;				
				dbl_wt=dbl_wt/noOfPriPacks;
			}	else{ 				
				dbl_wt=dbl_wt*quantity;
			}
			boolean invalid_Data = false;
			if(dbl_wt>0 && dbl_wt<1){invalid_Data = false; WtLess1 = false;}
			else if(0 == Math.floor(dbl_wt)){ invalid_Data = true; WtZero = true;}
						
			quantity = (round(quantity,2));
			
			ghg = calculate_ghg_value(refItemrev,dbl_wt);
						
			for(int i =0;i<keycountries.length;i++){	
				
				ghg_disposal = calculate_ghg_disposal(keycountries[i],dbl_wt,refItemrev);
				waste_to_landfill = calculate_waste_to_landfill(keycountries[i],dbl_wt,refItemrev);
				epr_tax = calculate_epr_tax(keycountries[i],dbl_wt,refItemrev);
				dbl_wt = (round(dbl_wt,2));
				cmpRefData[i] =new ComponentData();
				cmpRefData[i].setGhg_disposal(round(ghg_disposal,2));
				cmpRefData[i].setReal_ghg_disposal(ghg_disposal);
				cmpRefData[i].setGhg_pack_matl(round(ghg,2));
				cmpRefData[i].setReal_ghg(ghg);
				cmpRefData[i].setWaste_to_landfill(round(waste_to_landfill,2));
				cmpRefData[i].setReal_waste(waste_to_landfill);
				cmpRefData[i].setIs_refItem(is_refItem);
				cmpRefData[i].setKey_country(keycountries[i]);
				cmpRefData[i].setEpr_tax(round(epr_tax,2));
				cmpRefData[i].setReal_epr_tax(epr_tax);
				cmpRefData[i].setItem_id(obj_str[0]);
				cmpRefData[i].setItem_name(obj_str[1]);
				cmpRefData[i].set_primary_pack(is_primary);		
				cmpRefData[i].setQuantity(quantity);
				cmpRefData[i].setWeight(dbl_wt);
				cmpRefData[i].setWt_zero(invalid_Data);
			}			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return cmpRefData;
	}

	private boolean is_comp_primary(TCComponentItemRevision itemRev) {
		// TODO Auto-generated method stub
		boolean is_primary= true;
		
		try {
			
			TCProperty prop = itemRev.getTCProperty(PAMConstant.CONSUMER_UNIT);
			
			if (prop!=null)
			{
				String consumer_unit = prop.getUIFValue();
				
				if ((consumer_unit.compareToIgnoreCase("true") == 0) || (consumer_unit.compareToIgnoreCase("true") == 0))
				{
					if (consumer_unit.compareToIgnoreCase("true") == 0)
					{
						return true;
					}
					else if (consumer_unit.compareToIgnoreCase("false") == 0)
					{
						return false;
					}
				}
			}

			TCComponent item =itemRev.getItem();
			String obj_type=item.getType();
			is_primary=Arrays.asList(primary_pack_list).contains(obj_type);
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		return is_primary;
	}
	
	private void getMappingValues() {
		
		session.setStatus("Reading the Sustainability Mapping/Values template. Please wait…");
		
		long  startTime = System.currentTimeMillis();

		String gabi_scorecard_item_id = null;
		String gabi_classification_item_id = null;
		String rri_mapping_item_id = null;
		String epr_mapping_item_id = null;;
		String rri_scorecard_item_id = null;
		String epr_scorecard_item_id = null;
		String rri_proxy_template_id = null;
		
		String env_template_list[] = session.getPreferenceService().getStringValues("UL_SUSTAINABILITY_TEMPLATE");
		String separator = session.getPreferenceService().getStringValue("WSOM_find_list_separator");

		String str1[] = env_template_list[0].split(":");
		gabi_classification_item_id = str1[1];
		str1 = env_template_list[1].split(":");
		gabi_scorecard_item_id = str1[1];
		str1 = env_template_list[2].split(":");
		rri_mapping_item_id = str1[1];
		str1 = env_template_list[3].split(":");
		rri_scorecard_item_id = str1[1];
		str1 = env_template_list[4].split(":");
		rri_proxy_template_id = str1[1];
		str1 = env_template_list[5].split(":");
		epr_mapping_item_id = str1[1];
		str1 = env_template_list[6].split(":");
		epr_scorecard_item_id = str1[1];
		
		String queryName1 = "__UL_LatestReleasedItemRevision";
		String entries1[] = { "ID" };
		String values[] = { gabi_classification_item_id + separator +
				 gabi_scorecard_item_id + separator +
				 rri_mapping_item_id + separator +
				 epr_mapping_item_id+ separator +
				 rri_scorecard_item_id + separator +
				 epr_scorecard_item_id + separator +
				 rri_proxy_template_id };

		try {
			TCComponent[] components = UnileverQueryUtil.executeQuery(queryName1, entries1, values);
			if (components!=null)
			{
				for (int index = 0 ; index < components.length ; index++)
				{
					TCComponentItemRevision itemrev = (TCComponentItemRevision) components[index];
					
					if (itemrev.toString().startsWith(gabi_classification_item_id) == true)
						gabiForms = itemrev.getRelated(UL4Common.IMANSPECIFICATION);
					else if (itemrev.toString().startsWith(gabi_scorecard_item_id) == true)
						gabiscforms = itemrev.getRelated(UL4Common.IMANSPECIFICATION);
					else if (itemrev.toString().startsWith(rri_mapping_item_id) == true)
						RRIMapForms = itemrev.getRelated(UL4Common.IMANSPECIFICATION);
					else if (itemrev.toString().startsWith(epr_mapping_item_id) == true)
						EPRMapping = itemrev.getRelated(UL4Common.IMANSPECIFICATION);
					else if (itemrev.toString().startsWith(rri_scorecard_item_id) == true)
						RRISCForms = itemrev.getRelated(UL4Common.IMANSPECIFICATION);	
					else if (itemrev.toString().startsWith(epr_scorecard_item_id) == true)
						EPRSCForms = itemrev.getRelated(UL4Common.IMANSPECIFICATION);		
					else if (itemrev.toString().startsWith(rri_proxy_template_id) == true)
						RRIProxyForms = itemrev.getRelated(UL4Common.IMANSPECIFICATION);
				}
			}

			if(UnileverUtility.isPerfMonitorTriggered == true)
				UnileverUtility.getPerformanceTime(startTime, "getMappingValues" );
			
		}catch (TCException e2) {
			e2.printStackTrace();
		}
	}

	private ArrayList<ComponentData> getSummaryData(ArrayList<ComponentData> formarray, String Item_id, boolean is_ref, String pack_id_labels) {
		// TODO Auto-generated method stub

		ComponentData form = null;
		
		ArrayList<ComponentData> summaryformarray = new ArrayList<ComponentData>();

		double total_weight = 0;
		double total_ghg = 0;
		double total_ghg_disposal = 0;
		double total_epr =0;
		double total_waste = 0;
		
		for(int j = 0;j<keycountries.length;j++)
		{
			if (keycountries[j] ==null)
				continue;
			
			total_weight = 0;
			total_ghg = 0;
			total_ghg_disposal = 0;
			total_epr =0;
			total_waste = 0;
			for(int i = 0; i<formarray.size();i++)
			{
				if (formarray.get(i).getKey_country()==null)
					continue;
				
				if((formarray.get(i).getKey_country()).equals(keycountries[j])){
					total_weight +=  formarray.get(i).getWeight();
					total_ghg += formarray.get(i).getReal_ghg();
					total_ghg_disposal += formarray.get(i).getReal_ghg_disposal();
					
					double epr = formarray.get(i).getReal_epr_tax();
					
					if (epr == -1 || epr == -2)// If No: No EPR in Place or Yes: EPR in Place, Data TBC then consider as zero
						epr = 0.0;
					
					total_epr += epr;
					total_waste+=formarray.get(i).getReal_waste();
				}
			}
			form = new ComponentData();
			form.setKey_country(keycountries[j]);
			form.setWeight(total_weight);
			form.setGhg_disposal(round(total_ghg_disposal,2));
			form.setGhg_pack_matl(round(total_ghg,2));
			form.setEpr_tax(round(total_epr,2));
			form.setWaste_to_landfill(round(total_waste,2));
			form.setItem_id(Item_id);
			form.setItem_name(pack_id_labels);
			form.setIs_refItem(is_ref);
			form.setQuantity(1);
			summaryformarray.add(form);
		}
		return summaryformarray;
	}

	public void printTabledata(ArrayList<ComponentData> sortedRef, int rownum) {
		
		session.setStatus("Writing the Report Document. Please wait…");

		XSSFDataFormat format = workbook.createDataFormat();	
		
		CellStyle style2 = workbook.createCellStyle();
		style2.setBorderBottom(CellStyle.BORDER_THIN);
		style2.setBorderTop(CellStyle.BORDER_THIN);
		style2.setBorderRight(CellStyle.BORDER_THIN);
		style2.setBorderLeft(CellStyle.BORDER_THIN);
		style2.setAlignment(CellStyle.ALIGN_LEFT);
		style2.setDataFormat(format.getFormat("0.00"));
		style2.setLocked(true);

		CellStyle style2a = workbook.createCellStyle();
		style2a.setBorderBottom(CellStyle.BORDER_THIN);
		style2a.setBorderTop(CellStyle.BORDER_THIN);
		style2a.setBorderRight(CellStyle.BORDER_THIN);
		style2a.setBorderLeft(CellStyle.BORDER_THIN);
		style2a.setAlignment(CellStyle.ALIGN_RIGHT);
		style2a.setDataFormat(format.getFormat("0.00"));
		style2a.setLocked(true);
		
		CellStyle style2b = workbook.createCellStyle();
		style2b.setBorderBottom(CellStyle.BORDER_THIN);
		style2b.setBorderTop(CellStyle.BORDER_THIN);
		style2b.setBorderRight(CellStyle.BORDER_THIN);
		style2b.setBorderLeft(CellStyle.BORDER_THIN);
		style2b.setAlignment(CellStyle.ALIGN_CENTER);
		style2b.setDataFormat(format.getFormat("0.00"));
		style2b.setLocked(true);

		XSSFFont whiteFont = workbook.createFont();
		whiteFont.setColor(IndexedColors.WHITE.getIndex());
		XSSFFont blackFont = workbook.createFont();
		blackFont.setColor(IndexedColors.BLACK.getIndex());
		XSSFFont blackBoldFont = workbook.createFont();
		blackBoldFont.setColor(IndexedColors.BLACK.getIndex());
		blackBoldFont.setBold(true);
		blackBoldFont.setFontHeightInPoints((short) 12);

		CellStyle style3 = workbook.createCellStyle();
		style3.setBorderBottom(CellStyle.BORDER_THIN);
		style3.setBorderTop(CellStyle.BORDER_THIN);
		style3.setBorderRight(CellStyle.BORDER_THICK);
		style3.setBorderLeft(CellStyle.BORDER_THIN);
		style3.setAlignment(CellStyle.ALIGN_CENTER);
		style3.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
		style3.setFillPattern(CellStyle.SOLID_FOREGROUND);
		style3.setFont(whiteFont);
		style3.setDataFormat(format.getFormat("0.00"));
		style3.setLocked(true);
		
		XSSFCellStyle style4c = workbook.createCellStyle();
		style4c.setBorderBottom(CellStyle.BORDER_THIN);
		style4c.setBorderTop(CellStyle.BORDER_THIN);
		style4c.setBorderRight(CellStyle.BORDER_THICK);
		style4c.setBorderLeft(CellStyle.BORDER_THIN);
		style4c.setFillForegroundColor(new XSSFColor(new java.awt.Color(217,217,217)));
		//style4.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
		style4c.setFillPattern(CellStyle.SOLID_FOREGROUND);
		style4c.setAlignment(CellStyle.ALIGN_CENTER);
		style4c.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		style4c.setFont(blackBoldFont);
		style4c.setLocked(true);

		XSSFCellStyle style4 = workbook.createCellStyle();
		style4.setBorderBottom(CellStyle.BORDER_THIN);
		style4.setBorderTop(CellStyle.BORDER_THIN);
		style4.setBorderRight(CellStyle.BORDER_THIN);
		style4.setBorderLeft(CellStyle.BORDER_THIN);
		style4.setFillForegroundColor(new XSSFColor(new java.awt.Color(217,217,217)));
		//style4.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
		style4.setFillPattern(CellStyle.SOLID_FOREGROUND);
		style4.setAlignment(CellStyle.ALIGN_CENTER);
		style4.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		style4.setFont(blackBoldFont);
		style4.setLocked(true);
		
		XSSFCellStyle style5 = workbook.createCellStyle();
		style5.setBorderBottom(CellStyle.BORDER_THIN);
		style5.setBorderTop(CellStyle.BORDER_THIN);
		style5.setBorderRight(CellStyle.BORDER_THIN);
		style5.setBorderLeft(CellStyle.BORDER_THIN);
		style5.setAlignment(CellStyle.ALIGN_RIGHT);
		style5.setFillForegroundColor(new XSSFColor(new java.awt.Color(153,204,255)));
		//style5.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
		style5.setFont(blackBoldFont);
		style5.setFillPattern(CellStyle.SOLID_FOREGROUND);
		style5.setDataFormat(format.getFormat("0.00"));
		style5.setLocked(true);
		
		CellStyle style6 = workbook.createCellStyle();
		style6.setBorderBottom(CellStyle.BORDER_THIN);
		style6.setBorderTop(CellStyle.BORDER_THIN);
		style6.setBorderRight(CellStyle.BORDER_THIN);
		style6.setBorderLeft(CellStyle.BORDER_THIN);
		style6.setAlignment(CellStyle.ALIGN_LEFT);
		style6.setFont(blackBoldFont);
		style6.setDataFormat(format.getFormat("0"));
		style6.setLocked(true);
		
		CellStyle style6a = workbook.createCellStyle();
		style6a.setBorderBottom(CellStyle.BORDER_THIN);
		style6a.setBorderTop(CellStyle.BORDER_THIN);
		style6a.setBorderRight(CellStyle.BORDER_THIN);
		style6a.setBorderLeft(CellStyle.BORDER_THIN);
		style6a.setAlignment(CellStyle.ALIGN_CENTER);
		style6a.setFont(blackBoldFont);
		style6a.setDataFormat(format.getFormat("0"));
		style6a.setLocked(true);
		
		XSSFCellStyle style7 = workbook.createCellStyle();
		style7.setBorderBottom(CellStyle.BORDER_THIN);
		style7.setBorderTop(CellStyle.BORDER_THIN);
		style7.setBorderRight(CellStyle.BORDER_THIN);
		style7.setBorderLeft(CellStyle.BORDER_THIN);
		style7.setAlignment(CellStyle.ALIGN_CENTER);		
		style7.setDataFormat(format.getFormat("0"));
		style7.setLocked(true);
		
		XSSFCellStyle style7a = workbook.createCellStyle();
		style7a.setBorderBottom(CellStyle.BORDER_THIN);
		style7a.setBorderTop(CellStyle.BORDER_THIN);
		style7a.setBorderRight(CellStyle.BORDER_THIN);
		style7a.setBorderLeft(CellStyle.BORDER_THIN);
		style7a.setAlignment(CellStyle.ALIGN_RIGHT);		
		style7a.setFont(blackBoldFont);
		style7a.setDataFormat(format.getFormat("0.00"));
		style7a.setLocked(true);

		CellStyle cellstyle_67 = workbook.createCellStyle();
		cellstyle_67.setFillForegroundColor(IndexedColors.RED.getIndex());
		cellstyle_67.setFillPattern(CellStyle.SOLID_FOREGROUND);
		cellstyle_67.setAlignment(CellStyle.ALIGN_RIGHT);
		cellstyle_67.setFont(whiteFont);
		cellstyle_67.setDataFormat(format.getFormat("0.00"));

		// Iterate over data and write to sheet
		ListIterator<ComponentData> iter = sortedRef.listIterator();
		while (iter.hasNext()) {
			ComponentData cmpData = iter.next();
			String obj_id = cmpData.getItem_id();
			String obj_name = cmpData.getItem_name();
			String country = cmpData.getKey_country();
			double weight = cmpData.getWeight();
			double ghg = cmpData.getGhg_pack_matl();
			double ghg_disposal = cmpData.getGhg_disposal();
			double waste_to_landfill = cmpData.getWaste_to_landfill();
			double epr_tax = cmpData.getEpr_tax();
			boolean isRefItem = cmpData.isIs_refItem();
			double quantity = cmpData.getQuantity();
			boolean invalid_data = cmpData.isWt_zero();

			Row row = sheet.createRow(rownum++);
			int cellnum = 1;
			for(String str : attributes){
				Cell cell = row.createCell(cellnum);
				if (str.equals("item_id")){
					if(obj_name.length()==0)
						cell.setCellValue(obj_id);
					else if(obj_id.length()==0)
						cell.setCellValue(obj_name);
					else
						cell.setCellValue(obj_id+"-"+obj_name);
					if(isRefItem){
						row.setHeightInPoints((2*sheet.getDefaultRowHeightInPoints()));
						cell.setCellStyle(style4);
					}
					else if (obj_id.equals("Pack1")||
							obj_id.equals("Pack2")||
							obj_id.equals("Pack3")||
							obj_id.equals("Pack4")||
							obj_id.equals("Pack5")){
						cell.setCellStyle(style6);
					}
					else 
						cell.setCellStyle(style2);
				}
				else if (str.equals("u4_key_country")){
					if(isRefItem){
						cell.setCellValue("");
						cell.setCellStyle(style4c);
					}
					else{
						cell.setCellValue(country);
						cell.setCellStyle(style3);
					}
				}
				else if (str.equals("u4_quantity")){
					cell.setCellValue(quantity);
					if(isRefItem){
						cell.setCellValue("");
						cell.setCellStyle(style4);
					}
					else if (obj_id.equals("Pack1")||
							obj_id.equals("Pack2")||
							obj_id.equals("Pack3")||
							obj_id.equals("Pack4")||
							obj_id.equals("Pack5")){
						//cell.setCellStyle(style6a);
						cell.setCellStyle(style7a);
					}
					//else if(quantity == Math.floor(quantity)){
						//cell.setCellStyle(style7);
					//}
					else
						//cell.setCellStyle(style2b);
						cell.setCellStyle(style2a);
				}
				else if (str.equals("u4_est_weight")){
					cell.setCellValue(weight);
					if(isRefItem){
						cell.setCellValue("");
						cell.setCellStyle(style4);						
					}
					else if(invalid_data){
						cell.setCellStyle(cellstyle_67);
					}
					else if (obj_id.equals("Pack1")||
							obj_id.equals("Pack2")||
							obj_id.equals("Pack3")||
							obj_id.equals("Pack4")||
							obj_id.equals("Pack5")){
						//cell.setCellStyle(style7);
						if(cmpData.isWeightMin())
							cell.setCellStyle(style5);
						else
							cell.setCellStyle(style7a);
					}		
					else
						cell.setCellStyle(style2a);
				}
				else if (str.equals("u4_waste_to_landfill")){
					cell.setCellValue(waste_to_landfill);
					if(isRefItem){
						cell.setCellValue("");
						cell.setCellStyle(style4);
					}
					else if (obj_id.equals("Pack1")||
							obj_id.equals("Pack2")||
							obj_id.equals("Pack3")||
							obj_id.equals("Pack4")||
							obj_id.equals("Pack5")){
						//cell.setCellStyle(style7);
						if(cmpData.isWasteMin())
							cell.setCellStyle(style5);
						else
							cell.setCellStyle(style7a);	
					}
					else
						cell.setCellStyle(style2a);				
				}
				else if (str.equals("u4_ghg")){
					cell.setCellValue(ghg);
					if(isRefItem){
						cell.setCellValue("");
						cell.setCellStyle(style4);
					}
					else if (obj_id.equals("Pack1")||
							obj_id.equals("Pack2")||
							obj_id.equals("Pack3")||
							obj_id.equals("Pack4")||
							obj_id.equals("Pack5")){
						//cell.setCellStyle(style7);
						if(cmpData.isGhgMin())
							cell.setCellStyle(style5);
						else
							cell.setCellStyle(style7a);	
					}
					else
						cell.setCellStyle(style2a);				
				}
				else if (str.equals("u4_ghg_from_disposal")){
					cell.setCellValue(ghg_disposal);
					if(isRefItem){
						cell.setCellValue("");
						cell.setCellStyle(style4);
					}
					else if (obj_id.equals("Pack1")||
							obj_id.equals("Pack2")||
							obj_id.equals("Pack3")||
							obj_id.equals("Pack4")||
							obj_id.equals("Pack5")){
						//cell.setCellStyle(style7);
						if(cmpData.isGhg_disposalMin())
							cell.setCellStyle(style5);
						else
							cell.setCellStyle(style7a);	
					}
					else
						cell.setCellStyle(style2a);				
				}

				else if (str.equals("u4_tax")){
					//if(epr_tax == 0.0)
					//	cell.setCellValue("No EPR Data");
					if(epr_tax==-1.0)
						cell.setCellValue("No: No EPR in Place");
					else if(epr_tax==-2.0)
						cell.setCellValue("Yes: EPR in Place, Data TBC");
					else
						cell.setCellValue(epr_tax);
					if(isRefItem){
						cell.setCellValue("");
						cell.setCellStyle(style4);
					}
					else if (obj_id.equals("Pack1")||
							obj_id.equals("Pack2")||
							obj_id.equals("Pack3")||
							obj_id.equals("Pack4")||
							obj_id.equals("Pack5")){
						//cell.setCellStyle(style7);
						if(cmpData.isEprMin())
							cell.setCellStyle(style5);
						else
							cell.setCellStyle(style7a);	
					}
					else
						cell.setCellStyle(style2a);		
				}
				cellnum++;
			}
		}		
	}

	private void setMinValues(ArrayList<ComponentData> sorted){
		//String keycountries = null;
		HashMap<String, ArrayList<ComponentData>> hashMap = new HashMap<String, ArrayList<ComponentData>>();
		
		for(int kc=0;kc<keycountries.length;kc++)
		{
			ArrayList<ComponentData> list = new ArrayList<ComponentData>();
			for(int i = 0;i<sorted.size();i++){		
				if(sorted.get(i).getKey_country().equals(keycountries[kc]))
					list.add(sorted.get(i));
			}
			if (!hashMap.containsKey(keycountries[kc])) {
				hashMap.put(keycountries[kc], list);
			} 
		}
		Set set = hashMap.entrySet();
		Iterator iterator = set.iterator();
		while(iterator.hasNext()) {
			Map.Entry mentry = (Map.Entry)iterator.next();
			ArrayList<ComponentData> list1 = (ArrayList) mentry.getValue();

			setWeightMinValue(list1);
			setGHGMinValue(list1);
			setGHGDisposalMinValue(list1);
			setWasteMinValue(list1);
			setEpRMinValue(list1);
		}
	}

	private void setWeightMinValue(ArrayList<ComponentData> sorted){		
		ArrayList<ComponentData> sortedList = new ArrayList<ComponentData>(sorted);
		Collections.sort(sortedList, new Comparator<ComponentData>() {
			public int compare(ComponentData o1, ComponentData o2) {
				return Double.compare(o1.getWeight(), o2.getWeight());
			}	
		});
		if(sortedList.size()>0)	{
			double min = sortedList.get(0).getWeight();
			for(ComponentData c1 : sortedList){
				if(c1.getWeight()==min && (c1.getItem_id().equals("Pack1")||
											c1.getItem_id().equals("Pack2")||
											c1.getItem_id().equals("Pack3")||
											c1.getItem_id().equals("Pack4")||
											c1.getItem_id().equals("Pack5"))){
					c1.setWeightMin(true);
					System.out.println("min weight value:"+c1.getWeight()+" for item:" +c1.getItem_id()+" with key country:" + c1.getKey_country());
				}
			}
		}
	}
	private void setGHGMinValue(ArrayList<ComponentData> sorted){		
		
		ArrayList<ComponentData> sortedlist = new ArrayList<ComponentData>(sorted);
		Collections.sort(sortedlist, new Comparator<ComponentData>() {
			public int compare(ComponentData o1, ComponentData o2) {
				return Double.compare(o1.getGhg_pack_matl(), o2.getGhg_pack_matl());
			}
		});

		if(sortedlist.size()>0){
			double min =sortedlist.get(0).getGhg_pack_matl();
			for(ComponentData c1 : sortedlist){
				if(c1.getGhg_pack_matl()==min && (c1.getItem_id().equals("Pack1")||
											c1.getItem_id().equals("Pack2")||
											c1.getItem_id().equals("Pack3")||
											c1.getItem_id().equals("Pack4")||
											c1.getItem_id().equals("Pack5"))){
					c1.setGhgMin(true);
					System.out.println("min GHG value:"+c1.getGhg_pack_matl()+" for item:" +c1.getItem_id()+" with key country:" + c1.getKey_country());
				}
			}
		}
	}
	private void setGHGDisposalMinValue(ArrayList<ComponentData> sorted){	
		
		ArrayList<ComponentData> sortedlist = new ArrayList<ComponentData>(sorted);
		Collections.sort(sortedlist, new Comparator<ComponentData>() {
			public int compare(ComponentData o1, ComponentData o2) {
				return Double.compare(o1.getGhg_disposal(), o2.getGhg_disposal());
			}
		});
		if(sortedlist.size()>0){
			double min = sortedlist.get(0).getGhg_disposal();
			for(ComponentData c1 : sortedlist){
				if(c1.getGhg_disposal()==min && (c1.getItem_id().equals("Pack1")||
											c1.getItem_id().equals("Pack2")||
											c1.getItem_id().equals("Pack3")||
											c1.getItem_id().equals("Pack4")||
											c1.getItem_id().equals("Pack5"))){
					c1.setGhg_disposalMin(true);
					System.out.println("min GHG disposal value:"+c1.getGhg_disposal()+" for item:" +c1.getItem_id()+" with key country:" + c1.getKey_country());
				}
			}
		}
	}
	private void setWasteMinValue(ArrayList<ComponentData> sorted){
		
		ArrayList<ComponentData> sortedlist = new ArrayList<ComponentData>(sorted);
		Collections.sort(sortedlist, new Comparator<ComponentData>() {
			public int compare(ComponentData o1, ComponentData o2) {
				return Double.compare(o1.getWaste_to_landfill(), o2.getWaste_to_landfill());
			}
		});
		if(sortedlist.size()>0){
			double min = sortedlist.get(0).getWaste_to_landfill();
			for(ComponentData c1 : sortedlist){
				if(c1.getWaste_to_landfill()==min && (c1.getItem_id().equals("Pack1")||
											c1.getItem_id().equals("Pack2")||
											c1.getItem_id().equals("Pack3")||
											c1.getItem_id().equals("Pack4")||
											c1.getItem_id().equals("Pack5"))){
					c1.setWasteMin(true);
					System.out.println("min waste value:"+c1.getWaste_to_landfill()+" for item:" +c1.getItem_id()+" with key country:" + c1.getKey_country());
				}
			}
		}
	}
	private void setEpRMinValue(ArrayList<ComponentData> sorted){
		
	
		ArrayList<ComponentData> sortedlist = new ArrayList<ComponentData>(sorted);
		Collections.sort(sortedlist, new Comparator<ComponentData>() {
			public int compare(ComponentData o1, ComponentData o2) {
				return Double.compare(o1.getEpr_tax(), o2.getEpr_tax());
			}
		});
		if(sortedlist.size()>0){
		double min = sortedlist.get(0).getEpr_tax();
		for(ComponentData c1 : sortedlist){
			if(c1.getEpr_tax()==min && (c1.getItem_id().equals("Pack1")||
										c1.getItem_id().equals("Pack2")||
										c1.getItem_id().equals("Pack3")||
										c1.getItem_id().equals("Pack4")||
										c1.getItem_id().equals("Pack5"))){
				c1.setEprMin(true);
				System.out.println("min epr value:"+c1.getEpr_tax()+" for item:" +c1.getItem_id()+" with key country:" + c1.getKey_country());
			}
		}
		}
	}
	

	private String getGabiClassification(TCComponentItemRevision specComp, String material_type){
		//get gabi classification from gabi mapping item revision using material type and  material commodity
		String [] gabi_mtl_type = null;
		String [] gabi_mtl_comm = null;
		String [] gabi_mtl_class = null;
		String gabi_classification = null;
		String [] formproperties = new String [4];
		formproperties[0] = "object_name";
		formproperties[1] = "u4_environmental_type";
		formproperties[2] = "u4_material_commodity";
		formproperties[3] = "u4_gabi_classification";
		try{
			if(material_type!=null){
				AIFComponentContext[] MatlClassForm = specComp.getRelated(UL4Common.GMCFORMRELATION);
				TCComponentForm matl_class_form = (TCComponentForm )MatlClassForm[0].getComponent();
				String matl_comm = matl_class_form.getProperty(UL4Common.MAT_COMMODITY);
				String matl_class = matl_class_form.getProperty(UL4Common.MAT_CLASS);
				for(int i= 0 ; i<gabiForms.length;i++){
					TCComponentForm gabiclassificationForm = (TCComponentForm) gabiForms[i].getComponent();
					if(gabiclassificationForm.getProperty("object_name").equals(matl_class)){
						TCProperty []currProperty = gabiclassificationForm.getTCProperties(formproperties);
						gabi_mtl_type = currProperty[1].getStringValueArray();
						gabi_mtl_comm = currProperty[2].getStringValueArray();
						gabi_mtl_class = currProperty[3].getStringValueArray();
						ArrayList<Integer>inx1 =  getPropertyValueIndex(gabi_mtl_type, material_type);
						ArrayList<Integer>inx2 =  getPropertyValueIndex(gabi_mtl_comm, matl_comm);
						for(int j :inx1){						
							if (inx2.contains(j)) {								
								gabi_classification = gabi_mtl_class[j];
								break;
							}
						}	
					}				
				}			
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return gabi_classification;
	}

	private double getGabiScoreCardValue(String gabi_classification, String gabiAttribute){		
		double gabi_factor = 0;
		String [] formproperties = new String [2];
		formproperties[0] = "u4_gabi_classification";
		formproperties[1] = gabiAttribute;
		try{			
			if(gabi_classification!=null){
				TCComponentForm gabiscorecardForm = (TCComponentForm) gabiscforms[0].getComponent();
				TCProperty []currProperty = gabiscorecardForm.getTCProperties(formproperties);
				String [] gabiClassification = currProperty[0].getStringArrayValue();
				int inx = getPropertyValueIndex1(gabiClassification, gabi_classification);
				double[] default_percentage = currProperty[1].getDoubleArrayValue();
				gabi_factor = default_percentage[inx];
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return gabi_factor;
	}

	private double calculate_ghg_value(TCComponentItemRevision specComp, double weight){	
		double ghg_value = 0.0;
		ListIterator <TCComponentForm> iter = EnvMatlForms.listIterator();
		String [] formproperties = new String[4];
		formproperties[0] = "u4_type";
		formproperties[1] = "u4_weight_percentage";
		formproperties[2] = "u4_recycled_percentage";
		formproperties[3] = "u4_gabi_classification";
		while(iter.hasNext()){
			/*This is the calculations per layer:
			  ( (Component Weight * Weight % of the layer * Recycle % of the layer) * GHG Recycled Factor)
			  +
			  ( (Component Weight * Weight % of the layer  * (1 - Recycle % of the layer)) *  GHG Default Factor )*/
			try{	
				TCComponentForm frm = iter.next();
				TCProperty[] currProperty = frm.getTCProperties(formproperties);
				String material_type = currProperty[0].getStringValue();
				if(material_type.length()>0){
					double weight_percentage = currProperty[1].getDoubleValue();
					double recycle_percentage = currProperty[2].getDoubleValue();
					String gabi_classification = currProperty[3].getStringValue();
					double ghg_recycled_factor = 0;
					double ghg_default_factor = 0;

					if(gabi_classification.length()>0)	{
						//get gabi classification from gabi mapping item revision using material type and  material commodity
						gabi_classification = getGabiClassification(specComp,material_type);
					}			
					//get ghg values from gabi scorecard item revision
					ghg_recycled_factor = getGabiScoreCardValue(gabi_classification,"u4_pack_material_recycled");
					ghg_default_factor = getGabiScoreCardValue(gabi_classification,"u4_pack_material_default");

					ghg_value += ((weight*(weight_percentage/100)*(recycle_percentage/100))*ghg_recycled_factor) + ((weight*(weight_percentage/100)*(1 -(recycle_percentage/100)))*ghg_default_factor);
				}	
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
		//ghg_value =round(ghg_value,2);
		return ghg_value;
	}

	private double calculate_epr_tax(String key_country, Double dbl_wt,TCComponentItemRevision refItemrev) {

		ArrayList<Boolean> all_no_erp = new ArrayList<Boolean>();//No: No EPR in Place
		ArrayList<Boolean> all_yes_epr = new ArrayList<Boolean>();
		ArrayList<Boolean> all_mix_of_yes_no = new ArrayList<Boolean>();//"Yes: EPR in Place, Data TBC" and "No: No EPR in Place"
		ArrayList<Boolean> all_valid_values = new ArrayList<Boolean>();

		double epr_tax = 0.0;

		String [] formproperties = new String[2];
		formproperties[0] = "u4_type";
		formproperties[1] = "u4_weight_percentage";
		ListIterator <TCComponentForm> iter = EnvMatlForms.listIterator();
		while(iter.hasNext()){
			try{
				TCComponentForm frm = iter.next();
				TCProperty[] currProperty = frm.getTCProperties(formproperties);
				String material_type = currProperty[0].getStringValue();
				if(material_type.length()>0){
					double weight_percentage = currProperty[1].getDoubleValue();
					double epr_rate = 0.0;
					String epr_classification = getEPRMapping(key_country, refItemrev, material_type);
					if(epr_classification!=null){
						if(epr_classification.equals("No: No EPR in Place")){
							all_no_erp.add(true);
							all_mix_of_yes_no.add(true);
						}
						else if (epr_classification.equals("Yes: EPR in Place, Data TBC")){
							all_yes_epr.add(true);
							all_mix_of_yes_no.add(true);
						}
						else{
							epr_rate= getEPRValue(key_country,epr_classification);
							all_valid_values.add(true);
							epr_tax= epr_tax + ( (dbl_wt*(weight_percentage/100)) * epr_rate );
						}
					}
				}
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}

		boolean no_epr_place = true;
		for (int i = 0; i < all_no_erp.size(); i++) {
			if (all_no_erp.get(i) == false  )
				no_epr_place=false;
		}
		if ((no_epr_place == true) && (all_no_erp.size()>0) && (all_valid_values.size() ==0) )
			return -1;//No: No EPR in Place


		boolean yes_epr_place = true;
		for (int i = 0; i < all_yes_epr.size(); i++) {
			if (all_yes_epr.get(i) == false  )
				yes_epr_place=false;
		}
		for (int i = 0; i < all_mix_of_yes_no.size(); i++) {
			if (all_mix_of_yes_no.get(i) == false  )
				yes_epr_place=false;
		}

		if ( (yes_epr_place == true) && (all_mix_of_yes_no.size()>0) && (all_yes_epr.size()>0) && (all_valid_values.size() ==0) )
			return -2;//Yes: EPR in Place, Data TBC

		
		//epr_tax = round(epr_tax,2);
		return epr_tax;
	}
	
	public double calculate_ghg_disposal(String key_country, double weight,TCComponentItemRevision itemRev){
		double ghg_disposal = 0.0;
		String [] formproperties = new String[3];
		formproperties[0] = "u4_type";
		formproperties[1] = "u4_weight_percentage";
		formproperties[2] = "u4_gabi_classification";
		ListIterator <TCComponentForm> iter = EnvMatlForms.listIterator();
		String rri_classification = getRRIClassification(itemRev);
		while(iter.hasNext()){
			/*This is the calculations per layer:
			  ( (Component Weight * Weight % of the layer * Recovery %) * GHG Incineration Factor)
			  +
			  ( (Component Weight * Weight % of the layer  * RRI Index) *  GHG Landfill Factor )*/
			try{	
				TCComponentForm frm = iter.next();
				TCProperty[] currProperty = frm.getTCProperties(formproperties);
				String material_type = currProperty[0].getStringValue();
				if(material_type.length()>0){
					double weight_percentage = currProperty[1].getDoubleValue();
					String gabi_classification = currProperty[2].getStringValue();

					double ghg_incineration_factor = 0;
					double ghg_landfill_factor = 0;
					double rri_index = 0;
					double recovery_rate = 0;
					double recycle_rate =0;
					if(gabi_classification.length()==0){
						//get gabi classification from gabi mapping item revision using material type and  material commodity
						gabi_classification = getGabiClassification(itemRev,material_type);
					}
					//get ghg values from gabi scorecard item revision
					ghg_incineration_factor = getGabiScoreCardValue(gabi_classification,"u4_incineration_default");
					ghg_landfill_factor = getGabiScoreCardValue(gabi_classification,"u4_landfill_default");
					recovery_rate = getRRIValues(key_country,rri_classification,"u4_recovery_rate");
					rri_index = getRRIValues(key_country,rri_classification,"u4_rri_rate");
					recycle_rate = getRRIValues(key_country,rri_classification,"u4_recycle_rate");

					ghg_disposal =ghg_disposal + ( ( (weight*(weight_percentage/100))*(1-(recycle_rate/100))*(recovery_rate/100)	*	ghg_incineration_factor ) + 
												 (   (weight*(weight_percentage/100))*(1-(rri_index/100))		*ghg_landfill_factor)	);

				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
		//ghg_disposal = round(ghg_disposal,2);
		return ghg_disposal;
	}
	
	private String getRRIClassification(TCComponentItemRevision itemRev){
		String RRI_classification = null;
		String [] formproperties = new String[5];
		formproperties[0] = UL4Common.OBJECT_NAME;
		formproperties[1] = UL4Common.COMP_COMMODITY;
		formproperties[2] = UL4Common.MAT_CLASS;
		formproperties[3] = UL4Common.MAT_COMMODITY;
		formproperties[4] = "u4_rri_classification";
		try {
			//get RRI classification
			AIFComponentContext [] form = itemRev.getRelated(UL4Common.GMCFORMRELATION);
			TCComponentForm matlClassificationForm = (TCComponentForm) form[0].getComponent(); 
			String compClass = matlClassificationForm.getProperty(UL4Common.COMP_CLASS);
			String compCommodity =  matlClassificationForm.getProperty(UL4Common.COMP_COMMODITY);
			String matlClass =  matlClassificationForm.getProperty(UL4Common.MAT_CLASS);
			String matlCommodity =  matlClassificationForm.getProperty(UL4Common.MAT_COMMODITY);
			ArrayList <Integer> inx1_and_inx2 = new ArrayList<>();
			for(AIFComponentContext tempform:RRIMapForms){
				TCComponentForm tmpfrm = (TCComponentForm) tempform.getComponent();
				String frmName = tmpfrm.getProperty(UL4Common.OBJECT_NAME);
				TCProperty[] currProperties = tmpfrm.getTCProperties(formproperties);
				if(frmName.equals(compClass)){
					String [] allCompComm = currProperties[1].getStringArrayValue();
					ArrayList <Integer> inx1 = getPropertyValueIndex(allCompComm, compCommodity);
					if(inx1.size()>0){
						String [] allMatlClass = currProperties[2].getStringArrayValue();
						ArrayList <Integer> inx2 = getPropertyValueIndex(allMatlClass, matlClass);
						for(int j:inx1){							
							if (inx2.contains(j)){								
								inx1_and_inx2.add(j);
							}
						}
						if(inx1_and_inx2.size()>0){
							String [] allMatlComm = currProperties[3].getStringArrayValue();
							ArrayList <Integer> inx3 = getPropertyValueIndex(allMatlComm, matlCommodity);
							ArrayList <Integer> inx1_and_inx2_and_inx3 = new ArrayList<>();
							for(int j:inx1_and_inx2){								
								if(inx3.contains(j)){									
									inx1_and_inx2_and_inx3.add(j);
								}
							}							
							if(inx1_and_inx2_and_inx3.size()==1){								
								String[] allRRI_classification = currProperties[4].getStringArrayValue();
								RRI_classification = allRRI_classification[inx1_and_inx2_and_inx3.get(0)];
								break;
							}							
						}						
					}
				}
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return RRI_classification;
	}
	private double getRRIValues(String key_country,String RRI_classification,String rriAttribute){
		double RRIRate = 0.0;
		String RRIProxyCountry = null;
		String [] formproperties = new String [2];
		formproperties[0] = "u4_rri_classification";
		formproperties[1] = rriAttribute;
		String [] formproperties1 = new String [2];
		formproperties1[0] = "u4_country";
		formproperties1[1] = "u4_proxy_country";
		ArrayList <String> listOfCountries = new ArrayList<String>();
	try{
		if(RRI_classification!=null){
			//get RRI Rate	
			for(AIFComponentContext tempform:RRISCForms){
				TCComponentForm tmpfrm = (TCComponentForm) tempform.getComponent();
				String frmName = tmpfrm.getProperty(UL4Common.OBJECT_NAME);
				listOfCountries.add(frmName);
			}
			
			for(AIFComponentContext tempform:RRISCForms){
				if(listOfCountries.contains(key_country)){
					TCComponentForm tmpfrm = (TCComponentForm) tempform.getComponent();
					String frmName = tmpfrm.getProperty(UL4Common.OBJECT_NAME);
					TCProperty[] currProperties = tmpfrm.getTCProperties(formproperties);
					if(frmName.equalsIgnoreCase(key_country)){
						String [] allrri = currProperties[0].getStringArrayValue();
						int inx = getPropertyValueIndex1(allrri,RRI_classification);
						double [] allrrirate =  currProperties[1].getDoubleArrayValue();
						RRIRate = allrrirate[inx];
						break;
					}
				}
				else{
					for(AIFComponentContext tempform1:RRIProxyForms){
						TCComponentForm tmpfrm1 = (TCComponentForm) tempform1.getComponent();
						TCProperty[] currProperties1 = tmpfrm1.getTCProperties(formproperties1);
						String [] allcountries = currProperties1[0].getStringArrayValue();
						int inx = getPropertyValueIndex1(allcountries,key_country);
						String [] proxyCountries = currProperties1[1].getStringArrayValue();
						RRIProxyCountry = proxyCountries[inx];
						return getRRIValues(RRIProxyCountry,RRI_classification,rriAttribute);
					}					
				}
			}
		}
	}
		catch(Exception e){
			e.printStackTrace();
		}
		return RRIRate;
	}

	public double calculate_waste_to_landfill(String key_country, double weight, TCComponentItemRevision itemRev){
		double waste_to_landfill = 0.0;
		String rri_classification = getRRIClassification(itemRev);
		double RRIRate = getRRIValues(key_country, rri_classification,"u4_rri_rate");
		//calculate waste to landfill
		waste_to_landfill = weight * (1-(RRIRate/100));
		//DecimalFormat df = new DecimalFormat ("#.##");
		//waste_to_landfill = Double.parseDouble(df.format(waste_to_landfill));
		return waste_to_landfill;
	}
	private String getEPRMapping(String key_country,TCComponentItemRevision itemRev,String material_type){
		String EPR_Mapping = null;
		String [] formproperties = new String [7];
		formproperties[0] = UL4Common.OBJECT_NAME;
		formproperties[1] = "u4_environmental_type";
		formproperties[2] = UL4Common.COMP_CLASS;
		formproperties[3] = UL4Common.COMP_COMMODITY;
		formproperties[4] = UL4Common.MAT_CLASS;
		formproperties[5] = UL4Common.MAT_COMMODITY;
		formproperties[6] = "u4_epr_classification";
		try{
			AIFComponentContext [] form = itemRev.getRelated(UL4Common.GMCFORMRELATION);
			TCComponentForm matlClassificationForm = (TCComponentForm) form[0].getComponent(); 
			String compClass = matlClassificationForm.getProperty(UL4Common.COMP_CLASS);
			String compCommodity =  matlClassificationForm.getProperty(UL4Common.COMP_COMMODITY);
			String matlClass =  matlClassificationForm.getProperty(UL4Common.MAT_CLASS);
			String matlCommodity =  matlClassificationForm.getProperty(UL4Common.MAT_COMMODITY);
			
			for(AIFComponentContext tempform:EPRMapping)
			{
				TCComponentForm tmpfrm = (TCComponentForm) tempform.getComponent();
				TCProperty[] currProperties = tmpfrm.getTCProperties(formproperties);
				String frmName = currProperties[0].getStringValue();
				
				ArrayList<EPRMappingData> entire_data = new ArrayList<EPRMappingData>();

				if(frmName.equals(key_country))
				{
					String [] environmental_type = currProperties[1].getStringArrayValue();
					String [] component_class = currProperties[2].getStringArrayValue();
					String [] component_commodity = currProperties[3].getStringArrayValue();
					String [] material_class = currProperties[4].getStringArrayValue();
					String [] material_commodity = currProperties[5].getStringArrayValue();
					String [] epr_classification = currProperties[6].getStringArrayValue();
				
					for ( int index = 0 ; index < environmental_type.length ; index++)
					{
						EPRMappingData data = new EPRMappingData(environmental_type[index],component_class[index],component_commodity[index],material_class[index],material_commodity[index],epr_classification[index]);
						entire_data.add(data);
					}
					
					ArrayList<EPRMappingData> environmental_type_filter  = EPRMappingData.filter_epr_mapping_data(entire_data,"environmental_type",material_type);
					if (environmental_type_filter.size() == 0)
						environmental_type_filter  = EPRMappingData.filter_epr_mapping_data(entire_data,"environmental_type","BLANK_VALUE");

					ArrayList<EPRMappingData> component_class_filter  = EPRMappingData.filter_epr_mapping_data(environmental_type_filter,"component_class",compClass);
					if (component_class_filter.size() == 0)
						component_class_filter  = EPRMappingData.filter_epr_mapping_data(environmental_type_filter,"component_class","BLANK_VALUE");
							
					ArrayList<EPRMappingData> component_commodity_filter  = EPRMappingData.filter_epr_mapping_data(component_class_filter,"component_commodity",compCommodity);
					if (component_commodity_filter.size() == 0)
						component_commodity_filter  = EPRMappingData.filter_epr_mapping_data(component_class_filter,"component_commodity","BLANK_VALUE");
					
					ArrayList<EPRMappingData> material_class_filter  = EPRMappingData.filter_epr_mapping_data(component_commodity_filter,"material_class",matlClass);
					if (material_class_filter.size() == 0)
						material_class_filter  = EPRMappingData.filter_epr_mapping_data(component_commodity_filter,"material_class","BLANK_VALUE");
					
					ArrayList<EPRMappingData> material_commodity_filter  = EPRMappingData.filter_epr_mapping_data(material_class_filter,"material_commodity",matlCommodity);
					if (material_commodity_filter.size() == 0)
						material_commodity_filter  = EPRMappingData.filter_epr_mapping_data(material_class_filter,"material_commodity","BLANK_VALUE");
					
					for  (int index = 0 ; index < material_commodity_filter.size() ; )
					{
						EPR_Mapping =  material_commodity_filter.get(index).getEpr_classification() ;
						break;
					}

					break;
				}
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}

		return EPR_Mapping;
	}
	private double getEPRValue(String key_country, String epr_classification) {
		// TODO Auto-generated method stub
		double EPRRate = 0.0;
		String [] formproperties = new String [3];
		formproperties[0]= "u4_country";
		formproperties[1] = "u4_epr_classification";
		formproperties[2] = "u4_rate";
		try{
			if(epr_classification!=null){
				//get RRI Rate	
				for(AIFComponentContext tempform:EPRSCForms){
					TCComponentForm tmpfrm = (TCComponentForm) tempform.getComponent();
					TCProperty[] currProperties = tmpfrm.getTCProperties(formproperties);
					String [] countries = currProperties[0].getStringArrayValue();
					String [] alleprclass = currProperties[1].getStringArrayValue();

					for (int index = 0 ; index < countries.length ; index++)
					{
						if(countries[index].equalsIgnoreCase(key_country))
						{
							if(alleprclass[index].equalsIgnoreCase(epr_classification))
							{
								double [] alleprRate =  currProperties[2].getDoubleArrayValue();
								EPRRate = alleprRate[index];
								break;
							}		
						}				
					}
				}
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return EPRRate;
	}
	private ArrayList<Integer> getPropertyValueIndex(String[] allvalues, String value) {
		// TODO Auto-generated method stub
		//String [] splitStr = gabi_classification_array.split(",");
		ArrayList<Integer> inx = new ArrayList<>();
		for(int i= 0;i<allvalues.length;i++){
			if((allvalues[i].trim()).equals(value.trim())){
				inx.add(i);
			}
		}
		return inx;
	}
	private int getPropertyValueIndex1(String[] allValues, String value) {
		// TODO Auto-generated method stub		
		int inx =0;
		for(int i= 0;i<allValues.length;i++){
			if((allValues[i].trim()).equals(value.trim())){
				inx = i;
				break;
			}
		}
		return inx;
	}
	
	public double round(double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();

	    BigDecimal bd = new BigDecimal(value);
	    bd = bd.setScale(places, RoundingMode.HALF_UP);
	    return bd.doubleValue();
	}
	
	private String  getExcelFilename() throws TCException
	{
		String fileName = null ;
		TCComponentDataset dataset = null;
		
  		try 
		{
			String dsName = UL4Common.TOTALIMPREPORTXLSX;
			session = (TCSession) AIFDesktop.getActiveDesktop().getCurrentApplication().getSession();
			TCComponentDatasetType datasetType = (TCComponentDatasetType)session.getTypeComponent(UL4Common.DATASET);
			dataset = datasetType.find(dsName);
            
			if ( dataset != null )
			{			
				TCComponentTcFile relatedTcFiles[] = dataset.getTcFiles();                	
		    	
		    	if(relatedTcFiles != null && relatedTcFiles.length != 0)
		    	{	
		    		File file = ((TCComponentTcFile)relatedTcFiles[0]).getFile( null );
		    		fileName = file.getAbsolutePath();
		    		System.out.println("excel template path:" +fileName);
		    	}
			}
			else
			{
		      	//MessageBox.post(AIFUtility.getActiveDesktop().getShell() ," XLSX < " + dsName + " > " + reg.getString("dataset missing"),reg.getString("error"),MessageBox.ERROR);
			}
    	} 
		catch (TCException e) 
		{
	      	//MessageBox.post(AIFUtility.getActiveDesktop().getShell() ,e.getMessage(),reg.getString("error"),MessageBox.ERROR);
 		}
			
  	
		return fileName ;
		
	}

	public void uploadDataset(String dsName, String pdfFileNeme) throws Exception
	{		

		session.setStatus("Creating/Uploading Report to Teamcenter. Please wait…");
		
		String relation =  null ;
		InterfaceAIFComponent[] target = null ;
		String msg = "";
		try
		{				
			target = new InterfaceAIFComponent[]{projRev};
			relation = UL4Common.ENVIRONMENTALREPORT;

			NewDatasetOperation dsOperation = new NewDatasetOperation (session,AIFUtility.getActiveDesktop(),dsName,dsName,dsName,"","U4_EnvironmentalReport","MSExcel",false,pdfFileNeme , "" , "U4_excel" , true ,target ,relation);

			dsOperation.executeOperation();
		}
		catch (TCException e)
		{	
			e.printStackTrace();
		}
	}	
	private String getCurrentTimeStamp() 
	{
		SimpleDateFormat sdfDate = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss");
		return sdfDate.format(new Date());
	}

	@Override
	public void executeOperation() throws Exception {

		// TODO Auto-generated method stub
		session = (TCSession) AIFDesktop.getActiveDesktop().getCurrentApplication().getSession();
		if (session!=null)
			session.setStatus("Generating Total Pack Impact Report. Please wait…");
		UnileverUtility.getPerformanceMonitorPrefValue( session);
		long  startTime = System.currentTimeMillis();
		descText = description;
		noOfPriPacks = no_of_pri_packs;
		
		//reg = Registry.getRegistry(this);
		// Disposal table to be written
		attributes = new ArrayList<String>();
		attributes.add("item_id");
		attributes.add("u4_key_country");
		attributes.add("u4_quantity");
		attributes.add("u4_est_weight");
		attributes.add("u4_waste_to_landfill");
		attributes.add("u4_ghg");
		attributes.add("u4_ghg_from_disposal");
		attributes.add("u4_tax");
		
		// get the project revision
		AIFComponentContext[] projrevs;
		TCComponentItemRevision comp = null;
		
		int rownum = 12;
		try {
			session = (TCSession) AIFDesktop.getActiveDesktop().getCurrentApplication().getSession();
			//get all mapping items for calculation
			getMappingValues();
			projRev = itemRevision;
			keycountries = projRev.getProperty("u4_key_countries").split(",");
			primary_pack_list= session.getPreferenceService().getStringValues(UL4Common.PREF_PRIMARY_PACK_TYPE);
			InputStream inp = new FileInputStream(getExcelFilename());
			workbook = new XSSFWorkbook(inp);
		    sheet = workbook.getSheetAt(0);
		    sheet.setDisplayGridlines(false);
		} catch (TCException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		//calculate the env values
		ArrayList<ComponentData> packRefList = new ArrayList<ComponentData> ();
		ComponentData[] cmpRefData = null;
		Set set = refCompRevs.entrySet();
		Iterator iterator = set.iterator();
		while(iterator.hasNext()) {
			Map.Entry mentry = (Map.Entry)iterator.next();
			comp= (TCComponentItemRevision) mentry.getKey();
			double quantity = (double)mentry.getValue();
			cmpRefData = processItemData(comp,false,quantity);
			if(cmpRefData.length>0)
			for(int j=0;j<cmpRefData.length;j++){
				packRefList.add(cmpRefData[j]);
			}
		}
		ArrayList<ComponentData> pack1List = new ArrayList<ComponentData> ();
		ComponentData[] pack1Data = null;
		set = pack1CompRevs.entrySet();
		iterator = set.iterator();
		while(iterator.hasNext()) {
			Map.Entry mentry = (Map.Entry)iterator.next();
			comp= (TCComponentItemRevision) mentry.getKey();
			double quantity = (double)mentry.getValue();
			pack1Data = processItemData(comp,false,quantity);
			if(pack1Data.length>0)
			for(int j=0;j<pack1Data.length;j++){
				pack1List.add(pack1Data[j]);
			}
		}
		ArrayList<ComponentData> pack2List = new ArrayList<ComponentData> ();
		ComponentData[] pack2Data = null;
		set = pack2CompRevs.entrySet();
		iterator = set.iterator();
		while(iterator.hasNext()) {
			Map.Entry mentry = (Map.Entry)iterator.next();
			comp= (TCComponentItemRevision) mentry.getKey();
			double quantity = (double)mentry.getValue();
			pack2Data = processItemData(comp,false,quantity);
			if(pack2Data.length>0)
			for(int j=0;j<pack2Data.length;j++){
				pack2List.add(pack2Data[j]);
			}
		}
		ArrayList<ComponentData> pack3List = new ArrayList<ComponentData> ();
		ComponentData[] pack3Data = null;
		set = pack3CompRevs.entrySet();
		iterator = set.iterator();
		while(iterator.hasNext()) {
			Map.Entry mentry = (Map.Entry)iterator.next();
			comp= (TCComponentItemRevision) mentry.getKey();
			double quantity = (double)mentry.getValue();
			pack3Data = processItemData(comp,false,quantity);
			if(pack3Data.length>0)
			for(int j=0;j<pack3Data.length;j++){
				pack3List.add(pack3Data[j]);
			}
		}
		ArrayList<ComponentData> pack4List = new ArrayList<ComponentData> ();
		ComponentData[] pack4Data = null;
		set = pack4CompRevs.entrySet();
		iterator = set.iterator();
		while(iterator.hasNext()) {
			Map.Entry mentry = (Map.Entry)iterator.next();
			comp= (TCComponentItemRevision) mentry.getKey();
			double quantity = (double)mentry.getValue();
			pack4Data = processItemData(comp,false,quantity);
			if(pack4Data.length>0)
			for(int j=0;j<pack4Data.length;j++){
				pack4List.add(pack4Data[j]);
			}
		}

		//sort the lists according to the country
		ArrayList<ComponentData> sortedRef = new ArrayList<ComponentData>(packRefList);
		Collections.sort(sortedRef, new Comparator<ComponentData>() {
			public int compare(ComponentData o1, ComponentData o2) {
				if (o1 == null || o2==null)
					return 1;
				String ctry1 = null;
				String ctry2 = null;
				ctry1 = o1.getKey_country();
				ctry2 = o2.getKey_country();
				return ctry1.compareTo(ctry2);
			}
		});		

		ArrayList<ComponentData> sorted1 = new ArrayList<ComponentData>(pack1List);
		Collections.sort(sorted1, new Comparator<ComponentData>() {
			public int compare(ComponentData o1, ComponentData o2) {
				if (o1 == null || o2==null)
					return 1;
				
				String ctry1 = null;
				String ctry2 = null;
				ctry1 = o1.getKey_country();
				ctry2 = o2.getKey_country();
				return ctry1.compareTo(ctry2);
			}
		});

		ArrayList<ComponentData> sorted2 = new ArrayList<ComponentData>(pack2List);
		Collections.sort(sorted2, new Comparator<ComponentData>() {
			public int compare(ComponentData o1, ComponentData o2) {
				if (o1 == null || o2==null)
					return 1;
				
				String ctry1 = null;
				String ctry2 = null;
				ctry1 = o1.getKey_country();
				ctry2 = o2.getKey_country();
				return ctry1.compareTo(ctry2);
			}
		});

		ArrayList<ComponentData> sorted3 = new ArrayList<ComponentData>(pack3List);
		Collections.sort(sorted3, new Comparator<ComponentData>() {
			public int compare(ComponentData o1, ComponentData o2) {
				if (o1 == null || o2==null)
					return 1;
				String ctry1 = null;
				String ctry2 = null;
				ctry1 = o1.getKey_country();
				ctry2 = o2.getKey_country();
				return ctry1.compareTo(ctry2);
			}
		});

		ArrayList<ComponentData> sorted4 = new ArrayList<ComponentData>(pack4List);
		Collections.sort(sorted4, new Comparator<ComponentData>() {
			public int compare(ComponentData o1, ComponentData o2) {
				if (o1 == null || o2==null)
					return 1;
				String ctry1 = null;
				String ctry2 = null;
				ctry1 = o1.getKey_country();
				ctry2 = o2.getKey_country();
				return ctry1.compareTo(ctry2);
			}
		});

		//calculate the summary values
		ArrayList<ComponentData> summaryRefPack = new ArrayList<ComponentData>();
		if (sortedRef.size()>0)
			summaryRefPack = getSummaryData(sortedRef,"Pack1",false,pack_id_labels[0]);

		ArrayList<ComponentData> summaryPack1 = new ArrayList<ComponentData>();
		if (sorted1.size()>0)
			summaryPack1 = getSummaryData(sorted1,"Pack2",false,pack_id_labels[1]);

		ArrayList<ComponentData> summaryPack2 = new ArrayList<ComponentData>();
		if (sorted2.size()>0)
			summaryPack2 = getSummaryData(sorted2,"Pack3",false,pack_id_labels[2]);

		ArrayList<ComponentData> summaryPack3 = new ArrayList<ComponentData>();
		if (sorted3.size()>0)
			summaryPack3 = getSummaryData(sorted3,"Pack4",false,pack_id_labels[3]);

		ArrayList<ComponentData> summaryPack4 = new ArrayList<ComponentData>();
		if (sorted4.size()>0)
			summaryPack4 = getSummaryData(sorted4,"Pack5",false,pack_id_labels[4]);

		//insert the summary values into each list
		for (ComponentData tempform1 : summaryRefPack) {
			for (ComponentData tempform2 : sortedRef) {
				if (tempform1.getKey_country().equals(tempform2.getKey_country())) {
					int inx = sortedRef.indexOf(tempform2);
					sortedRef.add(inx, tempform1);
					break;
				}
			}
		}

		for (ComponentData tempform1 : summaryPack1) {
			for (ComponentData tempform2 : sorted1) {
				if (tempform1.getKey_country().equals(tempform2.getKey_country())) {
					int inx = sorted1.indexOf(tempform2);
					sorted1.add(inx, tempform1);
					break;
				}
			}
		}

		for (ComponentData tempform1 : summaryPack2) {
			for (ComponentData tempform2 : sorted2) {
				if (tempform1.getKey_country().equals(tempform2.getKey_country())) {
					int inx = sorted2.indexOf(tempform2);
					sorted2.add(inx, tempform1);
					break;
				}
			}
		}

		for (ComponentData tempform1 : summaryPack3) {
			for (ComponentData tempform2 : sorted3) {
				if (tempform1.getKey_country().equals(tempform2.getKey_country())) {
					int inx = sorted3.indexOf(tempform2);
					sorted3.add(inx, tempform1);
					break;
				}
			}
		}

		for (ComponentData tempform1 : summaryPack4) {
			for (ComponentData tempform2 : sorted4) {
				if (tempform1.getKey_country().equals(tempform2.getKey_country())) {
					int inx = sorted4.indexOf(tempform2);
					sorted4.add(inx, tempform1);
					break;
				}
			}
		}
	
		ArrayList<ComponentData> mergedSummaryPack = new ArrayList<ComponentData>(summaryRefPack);
		mergedSummaryPack.addAll(summaryPack1);
		mergedSummaryPack.addAll(summaryPack2);
		mergedSummaryPack.addAll(summaryPack3);
		mergedSummaryPack.addAll(summaryPack4);
		
		

		ArrayList<ComponentData> mergedSorted = new ArrayList<ComponentData>(sortedRef);
		//merge all the lists into one
		mergedSorted.addAll(sorted1);
		mergedSorted.addAll(sorted2);
		mergedSorted.addAll(sorted3);
		mergedSorted.addAll(sorted4);
		
		setMinValues(mergedSummaryPack);	

		ArrayList<ComponentData> meregedSorted1 = new ArrayList<ComponentData>(mergedSorted);
		Collections.sort(meregedSorted1, new Comparator<ComponentData>() {
			public int compare(ComponentData o1, ComponentData o2) {
				String ctry1 = null;
				String ctry2 = null;
				ctry1 = o1.getKey_country();
				ctry2 = o2.getKey_country();
				return ctry1.compareTo(ctry2);
			}
		});

		ArrayList<ComponentData> ctryFormPack = new ArrayList<ComponentData>();		
		for(int i=0;i<keycountries.length;i++){
			ComponentData comp1 = new ComponentData();
			comp1.setItem_name(keycountries[i]);
			comp1.setItem_id("");
			comp1.setKey_country(keycountries[i]);
			comp1.setIs_refItem(true);
			ctryFormPack.add(comp1);
		}
		int no_of_packs = 1;
		int ref = refCompRevs.size();
		int pack1 = pack1CompRevs.size();
		int pack2 = pack2CompRevs.size();
		int pack3 = pack3CompRevs.size();
		int pack4 = pack4CompRevs.size();		
		if(ref>0){
			no_of_packs = 1;
			if(pack1>0){
				no_of_packs = no_of_packs+1;
			}			
			if(pack2>0){
				no_of_packs = no_of_packs+1;
			}
			if(pack3>0){
				no_of_packs = no_of_packs+1;
			}			
			if(pack4>0){
				no_of_packs = no_of_packs+1;
			}			
		}
		else {
			no_of_packs =0;
			if(pack1>0){
				no_of_packs = no_of_packs+1;
			}			
			if(pack2>0){
				no_of_packs = no_of_packs+1;
			}
			if(pack3>0){
				no_of_packs = no_of_packs+1;
			}			
			if(pack4>0){
				no_of_packs = no_of_packs+1;
			}	
		}
				
		if(ref>0 && pack1>0 && pack2>0 && pack3>0 && pack4>0)
			no_of_packs = 5;
		if(ref==0 && pack1==0 && pack2==0 && pack3==0 && pack4==0)
			no_of_packs = 0;
		
		int no_of_lines_in_a_pack = no_of_packs +refCompRevs.size()+pack1CompRevs.size()+pack2CompRevs.size()+pack3CompRevs.size()+pack4CompRevs.size();
		
		//inserting the country line before each pack for each country
		for(int i = 0;i<ctryFormPack.size();i++){
			if(i==0)
				meregedSorted1.add(0, ctryFormPack.get(0));
			else
			{
				meregedSorted1.add((i*no_of_lines_in_a_pack)+i, ctryFormPack.get(i));
			}
		}
		//print the report to excel file
		printTabledata(meregedSorted1, rownum);
		// add legend info
		rownum += mergedSorted.size() + 1;
		
		// grouping the rows
		
		int groupRowNum = 13;
		//String [] keycountries = null;
		
		try {
		//	keycountries = projRev.getProperty("u4_key_countries").split(",");
		
		for(int i=0;i<keycountries.length;i++){
			groupRowNum = groupRowNum+1;
			if(ref!=0){
				sheet.groupRow(groupRowNum, groupRowNum+ref-1);
				sheet.setRowGroupCollapsed(groupRowNum, true);
				groupRowNum+=ref+1;
			}
			if(pack1!=0){
				sheet.groupRow(groupRowNum, groupRowNum+pack1-1);
				sheet.setRowGroupCollapsed(groupRowNum, true);
				groupRowNum+=pack1+1;
			}
			if(pack2!=0){
				sheet.groupRow(groupRowNum, groupRowNum+pack2-1);
				sheet.setRowGroupCollapsed(groupRowNum, true);
				groupRowNum+=pack2+1;
			}
			if(pack3!=0){
				sheet.groupRow(groupRowNum, groupRowNum+pack3-1);
				sheet.setRowGroupCollapsed(groupRowNum, true);
				groupRowNum+=pack3+1;
			}
			if(pack4!=0){
				sheet.groupRow(groupRowNum, groupRowNum+pack4-1);
				sheet.setRowGroupCollapsed(groupRowNum, true);
				groupRowNum+=pack4+1;
			}
		}
		}
		catch(Exception e){
			e.printStackTrace();
		}

		// add the project name, created date and the user name to the header table
		// doing it here because if i try to add this info before resizing
		// the column, i run into an exception regarding the rendering of the formatted rich text
		// this is a work-around, no idea why i'm getting this exception
		String proj_name = null;
		String created_by = null;
		String creation_date = null;
		String Desc_text = null;
		String no_primary_packs = null;
		try {
			session = (TCSession) AIFDesktop.getActiveDesktop().getCurrentApplication().getSession();
			created_by = session.getUserName();
			created_by = "Created by: " + created_by;
			java.util.Date date = new java.util.Date();
			java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			creation_date = sdf.format(date);
			creation_date = "Created on: " + creation_date;
			proj_name = projRev.getStringProperty(UL4Common.OBJECT_NAME);
			proj_name = "Project: " + proj_name;
			Desc_text = "Description: " + descText;
			String priPack = new Integer(no_of_pri_packs).toString() + "  ";
			no_primary_packs = "# Primary Components in Secondary Pack: " + priPack;
			//String padded = String.format("%1$-" + 3 + "s", no_primary_packs);  
			//no_primary_packs = padded + priPack;
			System.out.println(no_primary_packs);
		} catch (TCException e1) {
			e1.printStackTrace();
		}	
		CellStyle cellstyle_35 = workbook.createCellStyle();
		cellstyle_35.setAlignment(CellStyle.ALIGN_LEFT);
		
		XSSFRichTextString s1 = new XSSFRichTextString(proj_name);
		XSSFFont font2 = workbook.createFont();
		font2.setBold(true);
		s1.applyFont(0, 7, font2);
		
		XSSFRichTextString s2 = new XSSFRichTextString(created_by);
		s2.applyFont(0, 10, font2);

		XSSFRichTextString s3 = new XSSFRichTextString(creation_date);
		s3.applyFont(0, 10, font2);
		
		XSSFRichTextString s4 = new XSSFRichTextString(no_primary_packs);
		s4.applyFont(0, 38, font2);
		
		CellStyle cellstyle_65 = workbook.createCellStyle();
		cellstyle_65.setAlignment(CellStyle.ALIGN_LEFT);
		cellstyle_65.setVerticalAlignment(CellStyle.VERTICAL_TOP);
		cellstyle_65.setLocked(false);
		cellstyle_65.setWrapText(true);
		XSSFRichTextString s5 = new XSSFRichTextString(Desc_text);
		s5.applyFont(0, 11, font2);
		
		Row row_3 = null;
		Row row_4 = null;
		Row row_5 = null;
		Row row_6 = null;
		Row row_7 = null;
		for(Row row :sheet){
			int rownum1 = row.getRowNum();
			if(rownum1 == 3){
				row_3 = row;
				Cell cell_35 = row_3.getCell(2);
				if(cell_35!= null){
					cell_35.setCellValue(s1);
					cell_35.setCellStyle(cellstyle_35);
				}
				else {
					cell_35 = row_3.createCell(2);
					cell_35.setCellValue(s1);
					cell_35.setCellStyle(cellstyle_35);
				}
			}
			if(rownum1==4){
				row_4 = row;
				Cell cell_45 = row_4.getCell(2);
				if(cell_45!= null){
					cell_45.setCellValue(s2);
					cell_45.setCellStyle(cellstyle_35);
				}
				else {
					cell_45 = row_4.createCell(2);
					cell_45.setCellValue(s2);
					cell_45.setCellStyle(cellstyle_35);
				}
			}
			if(rownum1==5){
				row_5 = row;
				Cell cell_55 = row_5.getCell(2);
				if(cell_55!= null){
					cell_55.setCellValue(s3);
					cell_55.setCellStyle(cellstyle_35);
				}
				else {
					cell_55 = row_5.createCell(2);
					cell_55.setCellValue(s3);
					cell_55.setCellStyle(cellstyle_35);
				}
			}
			if(rownum1==6){
				row_6 = row;
				Cell cell_65 = row_6.getCell(2);
				if(cell_65!= null){
					cell_65.setCellValue(s4);
					cell_65.setCellStyle(cellstyle_35);
				}
				else {
					cell_65 = row_6.createCell(2);
					cell_65.setCellValue(s4);
					cell_65.setCellStyle(cellstyle_35);
				}
			}
			if(rownum1==7){
				row_7 = row;
				Cell cell_75 = row_7.getCell(2);
				if(cell_75!= null){
					cell_75.setCellValue(s5);
					cell_75.setCellStyle(cellstyle_65);
				}
				else {
					cell_75 = row_7.createCell(2);
					cell_75.setCellValue(s5);
					cell_75.setCellStyle(cellstyle_65);
				}
			}
			if(rownum1 >7) break;
		}
		
		if(WtZero){
			CellRangeAddress cra1 = new CellRangeAddress(8, 8, 6, 8);
			sheet.addMergedRegion(cra1);
			Row row8 = sheet.getRow(8);
			Cell cell_86 = row8.createCell(6);
		
			XSSFFont whiteFont = workbook.createFont();
			whiteFont.setColor(IndexedColors.WHITE.getIndex());
			
			CellStyle cellstyle_67 = workbook.createCellStyle();
			cellstyle_67.setFillForegroundColor(IndexedColors.RED.getIndex());
			cellstyle_67.setFillPattern(CellStyle.SOLID_FOREGROUND);
			cellstyle_67.setAlignment(CellStyle.ALIGN_CENTER);
			cellstyle_67.setFont(whiteFont);
			
			cell_86.setCellValue("At least one weight value is zero, resulting in invalid results");
			cell_86.setCellStyle(cellstyle_67);
		}
		
		/*sheet.lockDeleteColumns();
		sheet.lockDeleteRows();
		sheet.lockFormatCells();
		sheet.lockFormatColumns();
		sheet.lockFormatRows();
		sheet.lockInsertColumns();
		sheet.lockInsertRows();*/
		sheet.enableLocking();
		//workbook.lockStructure();

		String tempDir = System.getProperty("java.io.tmpdir");
		
		// Write the workbook in file system
		try {
			String dsname = null;
			String pdfFileNeme = null;
			
			String name = projRev.getStringProperty(UL4Common.OBJECT_NAME).replace(" ","_");
			String revid = projRev.getStringProperty(UL4Common.REVID);
			dsname = "TPIR_Project_"+name + "_" +revid + "_" +  getCurrentTimeStamp();
			pdfFileNeme = tempDir + dsname + "." + "xlsm" ; 
			FileOutputStream out = new FileOutputStream(new File(pdfFileNeme));
			workbook.write(out);
			out.close();
			// open excel file after report generation
			Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + pdfFileNeme);
			
			if (savereport==true)
				uploadDataset(dsname,pdfFileNeme);
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		session.setStatus("Environmental Report Generation Completed");
		//System.out.println("disposal_table.xlsx written successfully on disk.");
		if(UnileverUtility.isPerfMonitorTriggered == true)
			UnileverUtility.getPerformanceTime(startTime, "Generating Total Pack Impact Report" );

		
	}
}


