package com.unilever.rac.ui.environmentalreport;

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
import com.teamcenter.rac.kernel.TCComponentForm;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.Registry;
import com.unilever.rac.ui.common.UL4Common;
import com.unilever.rac.util.UnileverQueryUtil;
import com.unilever.rac.util.UnileverUtility;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFDataFormat;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFShape;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import java.util.Iterator;

import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;

import java.util.TreeMap;
import java.util.Set;

public class EnvPkgMaterialReportOperation extends AbstractAIFOperation {

	XSSFSheet sheet = null;
	ArrayList<String> attributes = null;
	XSSFWorkbook workbook = null;
	String descText = null;
	TCSession session = null;
	TCComponentItemRevision projRev = null;
	Registry reg = null;
	ArrayList<TCComponentForm> EnvMatlForms = null;
	ArrayList<EnvLayerData> compObjs = null;
	AIFComponentContext[] gabiForms = null;
	AIFComponentContext[] gabiscforms = null;
	AIFComponentContext [] RRIMapForms = null;
	AIFComponentContext [] RRISCForms = null;
	AIFComponentContext [] EPRMapping = null;
	AIFComponentContext [] EPRSCForms = null;
	AIFComponentContext [] RRIProxyForms = null;
	
	TCComponentItemRevision[] specComp=null;
	String description=null;
	TCComponentItemRevision itemRevision=null;
	boolean WtZero = false;
	
	boolean savereport = false;

	public EnvPkgMaterialReportOperation() {
		// TODO Auto-generated constructor stub
	}

	public void genEcelrep(TCComponentItemRevision[] specComp, String description, TCComponentItemRevision itemRevision,boolean savereport) throws IOException {
		this.specComp=specComp;
		this.description=description;
		this.itemRevision=itemRevision;
		this.savereport = savereport;
	}

	private ArrayList<EnvLayerData> getSummaryData(ArrayList<EnvLayerData> formarray, TCComponentItemRevision itemRev) {
		// TODO Auto-generated method stub
		String material_class = null;
		EnvLayerData form = null;
		String [] keycountries = null;
		String Item_id = null;
		try {
			AIFComponentContext [] forms = itemRev.getRelated(UL4Common.GMCFORMRELATION);
			TCComponentForm materialrelationform = null;
			
			if (forms!=null && forms.length==1 )
				materialrelationform = (TCComponentForm)forms[0].getComponent();
			
			if (materialrelationform!=null)
			material_class = materialrelationform.getProperty(UL4Common.MAT_COMMODITY);
			
			keycountries = projRev.getProperty("u4_key_countries").split(",");
			Item_id = itemRev.getProperty("object_string");
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ArrayList<EnvLayerData> summaryformarray = new ArrayList<EnvLayerData>();
		double total_recycled_percentage = 0;
		double total_weight = 0;
		double total_weight_percantage = 0;
		double total_ghg = 0;
		double total_ghg_disposal = 0;
		double total_epr =0;
		for(int j = 0;j<keycountries.length;j++){
			total_recycled_percentage = 0;
			total_weight = 0;
			total_weight_percantage = 0;
			total_ghg = 0;
			total_ghg_disposal = 0;
			total_epr =0;
			
			for(int i = 0; i<formarray.size();i++){			
				if((formarray.get(i).getKey_country()).equals(keycountries[j])){
					double weight = formarray.get(i).getWeight_percentage();
				total_recycled_percentage += weight*(formarray.get(i).getRecycled_percentage()/100);
				total_weight +=  formarray.get(i).getWeight();
				total_weight_percantage += formarray.get(i).getWeight_percentage();
				total_ghg += formarray.get(i).getReal_ghg();
				total_ghg_disposal += formarray.get(i).getReal_ghg_disposal();
				double epr = formarray.get(i).getReal_epr_tax();
				
				if (epr == -1 || epr == -2)// If No: No EPR in Place or Yes: EPR in Place, Data TBC then consider as zero
					epr = 0.0;
				
				total_epr += epr;
				formarray.get(i).setItem_id("");
				}
			}
			form = new EnvLayerData();
			form.setMaterial_type(material_class);
			form.setItem_id(Item_id);
			form.setKey_country(keycountries[j]);
			form.setRecycled_percentage(total_recycled_percentage);
			form.setWeight(total_weight);
			form.setWeight_percentage(total_weight_percantage);
			form.setGhg_disposal(round(total_ghg_disposal,2));
			form.setGhg_pack_matl(round(total_ghg,2));
			form.setEpr_tax(round(total_epr,2));
			form.setSummaryData(true);
			summaryformarray.add(form);
		}
		return summaryformarray;
	}

	public void printTabledata(ArrayList<EnvLayerData> sorted, int rownum) {
		
		session.setStatus("Writing the Report Document. Please wait�");

		XSSFDataFormat format = workbook.createDataFormat();

		CellStyle style1 = workbook.createCellStyle();
		style1.setBorderBottom(CellStyle.BORDER_THIN);
		style1.setBorderTop(CellStyle.BORDER_THIN);
		style1.setBorderRight(CellStyle.BORDER_THIN);
		style1.setBorderLeft(CellStyle.BORDER_THIN);
		style1.setAlignment(CellStyle.ALIGN_RIGHT);
		style1.setDataFormat(format.getFormat("0.00"));

		CellStyle style2 = workbook.createCellStyle();
		style2.setBorderBottom(CellStyle.BORDER_THIN);
		style2.setBorderTop(CellStyle.BORDER_THIN);
		style2.setBorderRight(CellStyle.BORDER_THIN);
		style2.setBorderLeft(CellStyle.BORDER_THIN);
		style2.setAlignment(CellStyle.ALIGN_CENTER);
		//style2.setDataFormat(format.getFormat("0.00"));

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
		// style3.setLocked(true);
		
		CellStyle style3c = workbook.createCellStyle();
		style3c.setBorderBottom(CellStyle.BORDER_THIN);
		style3c.setBorderTop(CellStyle.BORDER_THIN);
		style3c.setBorderRight(CellStyle.BORDER_THICK);
		style3c.setBorderLeft(CellStyle.BORDER_THIN);
		style3c.setAlignment(CellStyle.ALIGN_CENTER);
		style3c.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		style3c.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
		style3c.setFillPattern(CellStyle.SOLID_FOREGROUND);
		style3c.setFont(blackBoldFont);
		// style3.setLocked(true);

		CellStyle style4 = workbook.createCellStyle();
		style4.setBorderBottom(CellStyle.BORDER_THIN);
		style4.setBorderTop(CellStyle.BORDER_THIN);
		style4.setBorderRight(CellStyle.BORDER_THIN);
		style4.setBorderLeft(CellStyle.BORDER_THIN);
		style4.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
		style4.setFillPattern(CellStyle.SOLID_FOREGROUND);
		style4.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		style4.setAlignment(CellStyle.ALIGN_RIGHT);
		style4.setFont(blackBoldFont);
		style4.setDataFormat(format.getFormat("0.00"));
		
		
		CellStyle cellstyle_67 = workbook.createCellStyle();
		cellstyle_67.setFillForegroundColor(IndexedColors.RED.getIndex());
		cellstyle_67.setFillPattern(CellStyle.SOLID_FOREGROUND);
		cellstyle_67.setAlignment(CellStyle.ALIGN_RIGHT);
		cellstyle_67.setFont(whiteFont);
		cellstyle_67.setDataFormat(format.getFormat("0.00"));
		cellstyle_67.setBorderBottom(CellStyle.BORDER_THIN);
		cellstyle_67.setBorderTop(CellStyle.BORDER_THIN);
		cellstyle_67.setBorderRight(CellStyle.BORDER_THIN);
		cellstyle_67.setBorderLeft(CellStyle.BORDER_THIN);


		// Iterate over data and write to sheet
		ListIterator<EnvLayerData> iter = sorted.listIterator();
		while (iter.hasNext()) {
			EnvLayerData envLayerData = iter.next();
			String Obj_name = envLayerData.getItem_name();
			String obj_id = envLayerData.getItem_id();
			String country = envLayerData.getKey_country();
			String material_type = envLayerData.getMaterial_type();
			double weight = envLayerData.getWeight();
			double weight_pecentage = envLayerData.getWeight_percentage();
			double recycled = envLayerData.getRecycled_percentage();
			double ghg = envLayerData.getGhg_pack_matl();
			double ghg_disposal = envLayerData.getGhg_disposal();
			double epr_tax = envLayerData.getEpr_tax();
			boolean isRefItem = envLayerData.isSummaryData();
			boolean invalid_data = envLayerData.isWt_zero();

			Row row = sheet.createRow(rownum++);
			if(isRefItem)
				row.setHeightInPoints((2*sheet.getDefaultRowHeightInPoints()));
			int cellnum = 1;
			for(String str : attributes){
				Cell cell = row.createCell(cellnum);
				if(str.equals("item_id")){					
					cell.setCellValue(obj_id);
					if(isRefItem){
						cell.setCellStyle(style4);
					}else {
						cell.setCellStyle(style2);
					}					
				}				
				else if(str.equals("object_name")){
					cell.setCellValue(Obj_name);
					if(isRefItem){
						cell.setCellStyle(style4);
					}else {
						cell.setCellStyle(style2);
					}									
				}				
				else if (str.equals("u4_key_country")){
					cell.setCellValue(country);
					if(isRefItem){
						cell.setCellStyle(style3c);
					}else {
						cell.setCellStyle(style3);
					}								
				}
				else if (str.equals("u4_type")){
					cell.setCellValue(material_type);
					if(isRefItem){
						cell.setCellStyle(style4);
					}else {
						cell.setCellStyle(style2);
					}								
				}
				else if (str.equals("u4_weight_percentage")){
					cell.setCellValue(weight_pecentage);
					if(isRefItem){
						cell.setCellStyle(style4);
					}else {
						cell.setCellStyle(style1);
					}									
				}
				else if (str.equals("u4_recycled_percentage")){
					cell.setCellValue(recycled);
					if(isRefItem){
						cell.setCellStyle(style4);
					}else {
						cell.setCellStyle(style1);
					}									
				}
				else if (str.equals("u4_est_weight")){
					cell.setCellValue(weight);
					if(isRefItem){
						cell.setCellStyle(style4);
					}
					else if(invalid_data){
						cell.setCellStyle(cellstyle_67);
					}else {
						cell.setCellStyle(style1);
					}										
				}
				else if (str.equals("u4_ghg_from_disposal")){
					cell.setCellValue(ghg_disposal);
					if(isRefItem){
						cell.setCellStyle(style4);
					}else {
						cell.setCellStyle(style1);
					}								
				}
				else if (str.equals("u4_ghg")){
					cell.setCellValue(ghg);
					if(isRefItem){
						cell.setCellStyle(style4);
					}else {
						cell.setCellStyle(style1);
					}										
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
						cell.setCellStyle(style4);
					}else {
						cell.setCellStyle(style1);
					}									
				}
				cellnum++;
			}
		}		
	}

	private void getMappingValues() {
		
		session.setStatus("Reading the Sustainability Mapping/Values template. Please wait�");
		
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

	private ArrayList<EnvLayerData> processItemData(TCComponentItemRevision refItemrev){
		double ghg = 0.0;
		double ghg_disposal = 0;
		double epr_tax = 0.0;
		EnvLayerData envLayerData = null;
		TCComponentItemRevision pamrev = null;
		ArrayList<EnvLayerData> envlayerforms = new ArrayList<EnvLayerData>();

		 Map<Integer, TCComponentForm> treeMap = new TreeMap<Integer, TCComponentForm>();
		
		try{
			//get environmental packaging material forms
			EnvMatlForms = new ArrayList<TCComponentForm>(); 
			AIFComponentContext[] envforms1 = null;
			envforms1 = refItemrev.getRelated(UL4Common.ENVIRNOMENTAL_RELATION);
			if(envforms1.length>0){
				for(int j=0;j<envforms1.length;j++){
					TCComponentForm frm = (TCComponentForm)envforms1[j].getComponent();
					if (frm!=null){
						if (frm.getType().toString().compareTo("U4_EnvPackagingMatlForm") ==0)
						{
							int res = frm.getIntProperty("u4_sequence_no");
							treeMap.put(res, frm);
						}
					}
				}		
			}
			else{
				//get environmental layer forms from pam revision
				AIFComponentContext[] pam = refItemrev.getRelated(UL4Common.PAM_SPECIFICATION_RELATION);
				
				if (pam!=null && pam.length==1)
				pamrev = (TCComponentItemRevision)pam[0].getComponent();
				
				if (pamrev!=null)
				envforms1 = pamrev.getRelated(UL4Common.ENVIRNOMENTAL_RELATION);
				
				if(envforms1.length>0){
					for(int j=0;j<envforms1.length;j++){
						TCComponentForm frm = (TCComponentForm)envforms1[j].getComponent();
						if (frm!=null){
							if (frm.getType().toString().compareTo("U4_EnvPackagingMatlForm") ==0)
							{
								int res = frm.getIntProperty("u4_sequence_no");
								treeMap.put(res, frm);
							}
						}
					}
				}
			}

			Set<Entry<Integer, TCComponentForm>> s = treeMap.entrySet();
		    Iterator<Entry<Integer, TCComponentForm>> it = s.iterator();
		    while ( it.hasNext() ) {
		       Map.Entry entry = (Map.Entry) it.next();
		       TCComponentForm form = (TCComponentForm) entry.getValue();
		       EnvMatlForms.add(form);
		    }
		    
			if (EnvMatlForms!=null)
			{
				for(int i = 0;i<EnvMatlForms.size();i++){
					String material_type = EnvMatlForms.get(i).getProperty("u4_type");
					if(!material_type.equals("")){
						
						String weight_value = EnvMatlForms.get(i).getProperty("u4_weight_percentage");
						String recycle_value = EnvMatlForms.get(i).getProperty("u4_recycled_percentage");
						double weight_percentage =0.0; 
						double recycle_percentage =0.0; 
						
						if (weight_value!=null && weight_value.length()>0)
							weight_percentage = Double.parseDouble(weight_value);
						
						if (recycle_value!=null && recycle_value.length()>0)
							recycle_percentage= Double.parseDouble(recycle_value);

						String form_name = EnvMatlForms.get(i).getProperty("object_name");
						System.out.println("Processing item:"+refItemrev.getProperty("object_name"));
						//String [] obj_str = refItemrev.getProperty("object_string").split("-");
						String obj_str = refItemrev.getProperty("object_string");
						System.out.println("Object string: "+obj_str);
						AIFComponentContext[] refweightforms = refItemrev.getRelated(UL4Common.WEIGHTRELATION);
						String [] keycountries = projRev.getProperty("u4_key_countries").split(",");
						TCComponentForm Weightform=null;

						if (refweightforms.length>0)
						{
							Weightform = (TCComponentForm) refweightforms[0].getComponent();
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
									if (Weightform == null ){
										for(AIFComponentContext frm:compRelForms){
											TCComponentForm formtype = (TCComponentForm) frm.getComponent();
											if(formtype.getProperty(UL4Common.OBJECT_NAME).equals("Weight/Unit")){
												Weightform = formtype;
												break;
											}
										}
									}

									if (Weightform == null ){
										for(AIFComponentContext frm:compRelForms){
											TCComponentForm formtype = (TCComponentForm) frm.getComponent();
											if(formtype.getProperty(UL4Common.OBJECT_NAME).equals("Weight")){
												Weightform = formtype;
												break;
											}
										}
									}

									if (Weightform == null){
										for(AIFComponentContext frm:compRelForms){
											TCComponentForm formtype = (TCComponentForm) frm.getComponent();
											if(formtype.getProperty(UL4Common.OBJECT_NAME).equals("Weight_Label")){
												Weightform = formtype;
												break;
											}
										}
									}
								}
							}
						}

						double dbl_wt = 0.0 ;
						if (Weightform!=null)
						{
							String value = Weightform.getProperty("u4_target");
							if (value.equals("")==false)
								dbl_wt = Double.parseDouble(value);
						}

						ghg = calculate_ghg_value(refItemrev,dbl_wt, weight_percentage, recycle_percentage, material_type);
						for(int j =0;j<keycountries.length;j++){				
							ghg_disposal = calculate_ghg_disposal(keycountries[j],dbl_wt,refItemrev, weight_percentage, material_type);
							epr_tax = calculate_epr_tax(keycountries[j],dbl_wt,refItemrev,material_type,weight_percentage);
							boolean invalid_Data = false;
							if(dbl_wt == 0.00){ invalid_Data = true; WtZero = true;}
							envLayerData =new EnvLayerData();
							envLayerData.setGhg_disposal(round(ghg_disposal,2));
							envLayerData.setReal_ghg_disposal(ghg_disposal);
							envLayerData.setGhg_pack_matl(round(ghg,2));
							envLayerData.setReal_ghg(ghg);
							envLayerData.setKey_country(keycountries[j]);
							envLayerData.setWeight(dbl_wt*(weight_percentage/100));
							envLayerData.setItem_id(obj_str);
							envLayerData.setItem_name(form_name);
							envLayerData.setWeight_percentage(weight_percentage);
							envLayerData.setRecycled_percentage(recycle_percentage);
							envLayerData.setMaterial_type(material_type);
							envLayerData.setEpr_tax(round(epr_tax,2));
							envLayerData.setReal_epr_tax(epr_tax);
							envLayerData.setWt_zero(invalid_Data);
							envlayerforms.add(envLayerData);
						}
					}
				}
			}
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return envlayerforms;
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
	private double calculate_ghg_value(TCComponentItemRevision specComp, double weight,double weight_percentage,double recycle_percentage,String material_type){	
		double ghg_value = 0.0;
		String [] formproperties = new String[3];
		formproperties[0] = "u4_weight_percentage";
		formproperties[1] = "u4_recycled_percentage";
		formproperties[2] = "u4_gabi_classification";
		/*This is the calculations per layer:
			  ( (Component Weight * Weight % of the layer * Recycle % of the layer) * GHG Recycled Factor)
			  +
			  ( (Component Weight * Weight % of the layer  * (1 - Recycle % of the layer)) *  GHG Default Factor )*/
		try{		
			if(material_type!=null){	
				double ghg_recycled_factor = 0;
				double ghg_default_factor = 0;
				//get gabi classification from gabi mapping item revision using material type and  material commodity
				String gabi_classification = getGabiClassification(specComp,material_type);
				//get ghg values from gabi scorecard item revision
				ghg_recycled_factor = getGabiScoreCardValue(gabi_classification,"u4_pack_material_recycled");
				ghg_default_factor = getGabiScoreCardValue(gabi_classification,"u4_pack_material_default");

				ghg_value = ((weight*(weight_percentage/100)*(recycle_percentage/100))*ghg_recycled_factor) + ((weight*(weight_percentage/100)*(1 -(recycle_percentage/100)))*ghg_default_factor);
			}	
		}catch(Exception e){
			e.printStackTrace();
		}

		//ghg_value = round(ghg_value,2);
		return ghg_value;
	}
	
	private double calculate_epr_tax(String key_country, Double dbl_wt,TCComponentItemRevision refItemrev, String material_type,double weight_percentage) {

		ArrayList<Boolean> all_no_erp = new ArrayList<Boolean>();//No: No EPR in Place
		ArrayList<Boolean> all_yes_epr = new ArrayList<Boolean>();
		ArrayList<Boolean> all_mix_of_yes_no = new ArrayList<Boolean>();//"Yes: EPR in Place, Data TBC" and "No: No EPR in Place"
		ArrayList<Boolean> all_valid_values = new ArrayList<Boolean>();

		double epr_tax = 0.0;

			try{
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
					epr_tax=  ( (dbl_wt*(weight_percentage/100)) * epr_rate );
					}
				}
			}
			catch(Exception e){
				e.printStackTrace();
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

	public double calculate_ghg_disposal(String key_country, double weight,TCComponentItemRevision itemRev,double weight_percentage,String material_type){
		double ghg_disposal = 0.0;
		String rri_classification = getRRIClassification(itemRev);
		
			/*This is the calculations per layer:
			  ( (Component Weight * Weight % of the layer * Recovery %) * GHG Incineration Factor)
			  +
			  ( (Component Weight * Weight % of the layer  * RRI Index) *  GHG Landfill Factor )*/
			try{
				double ghg_incineration_factor = 0;
				double ghg_landfill_factor = 0;
				double rri_index = 0;
				double recovery_rate = 0;
				double recycle_rate =0;
				
				if(material_type!=null){					
					//get gabi classification from gabi mapping item revision using material type and  material commodity
					String gabi_classification = getGabiClassification(itemRev,material_type);

					//get ghg values from gabi scorecard item revision
					ghg_incineration_factor = getGabiScoreCardValue(gabi_classification,"u4_incineration_default");
					ghg_landfill_factor = getGabiScoreCardValue(gabi_classification,"u4_landfill_default");
					recovery_rate = getRRIValues(key_country,rri_classification,"u4_recovery_rate");
					rri_index = getRRIValues(key_country,rri_classification,"u4_rri_rate");
					recycle_rate = getRRIValues(key_country,rri_classification,"u4_recycle_rate");

					ghg_disposal = ( ( (weight*(weight_percentage/100))*(1-(recycle_rate/100))*(recovery_rate/100)	*	ghg_incineration_factor ) + 
								(   (weight*(weight_percentage/100))*(1-(rri_index/100))  *ghg_landfill_factor)	);
					
				}	
			}catch(Exception e){
				e.printStackTrace();
			}
		//ghg_disposal = round(ghg_disposal,2);
		return ghg_disposal;
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

	public void uploadDataset(String dsName, String pdfFileNeme) throws Exception
	{
		session.setStatus("Creating/Uploading Report to Teamcenter. Please wait�");

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

		session = (TCSession) AIFDesktop.getActiveDesktop().getCurrentApplication().getSession();
		if (session!=null)
			session.setStatus("Generating Environment Packaging Material Report. Please wait�");
		UnileverUtility.getPerformanceMonitorPrefValue( session);
		long  startTime = System.currentTimeMillis();
		descText = description;
		// Blank workbook
		workbook = new XSSFWorkbook();
		// Create a blank sheet
		sheet = workbook.createSheet("Environment Packaging Material");
		sheet.setDisplayGridlines(false);

		Row row1 = sheet.createRow(0);
		// unilever logo and report name table
		Row row_1 = sheet.createRow(1);
		// set the report name
		Cell cell_1 = row_1.createCell(1);
		CellStyle cellstyle_12 = workbook.createCellStyle();
		cellstyle_12.setBorderTop(CellStyle.BORDER_THICK);
		cellstyle_12.setBorderLeft(CellStyle.BORDER_THICK);
		cellstyle_12.setBorderBottom(CellStyle.BORDER_THICK);
		cell_1.setCellStyle(cellstyle_12);
		Cell cell_2 = row_1.createCell(2);
		cell_2.setCellValue("Environment Packaging Material Report");
		CellStyle cellstyle_13 = workbook.createCellStyle();
		XSSFFont font1 = workbook.createFont();
		font1.setBold(true);
		font1.setFontHeightInPoints((short) 28);
		cellstyle_13.setFont(font1);
		cellstyle_13.setBorderTop(CellStyle.BORDER_THICK);
		cellstyle_13.setAlignment(CellStyle.ALIGN_LEFT);
		cell_2.setCellStyle(cellstyle_13);
		Cell cell_3 = row_1.createCell(3);
		Cell cell_4 = row_1.createCell(4);
		Cell cell_5 = row_1.createCell(5);
		Cell cell_6 = row_1.createCell(6);
		Cell cell_7 = row_1.createCell(7);
		Cell cell_8 = row_1.createCell(8);
		Cell cell_9 = row_1.createCell(9);
		CellStyle cellstyle_14 = workbook.createCellStyle();
		cellstyle_14.setBorderTop(CellStyle.BORDER_THICK);
		cell_3.setCellStyle(cellstyle_14);
		cell_4.setCellStyle(cellstyle_14);
		cell_5.setCellStyle(cellstyle_14);
		cell_6.setCellStyle(cellstyle_14);
		cell_7.setCellStyle(cellstyle_14);
		cell_8.setCellStyle(cellstyle_14);
		cell_9.setCellStyle(cellstyle_14);
		Cell cell_10 = row_1.createCell(10);
		CellStyle cellstyle_17 = workbook.createCellStyle();
		cellstyle_17.setBorderTop(CellStyle.BORDER_THICK);
		cellstyle_17.setBorderRight(CellStyle.BORDER_THICK);
		//cellstyle_17.setBorderBottom(CellStyle.BORDER_THICK);
		cell_10.setCellStyle(cellstyle_17);

		// add logo pictures to this workbook.
		String fromFile1 = "/data/logo.png";
		InputStream is1 = this.getClass().getResourceAsStream(fromFile1);

		byte[] bytes1 = IOUtils.toByteArray(is1);
		
		int pictureIdx1 = workbook.addPicture(bytes1, Workbook.PICTURE_TYPE_PNG);
		
		is1.close();

		CreationHelper helper = workbook.getCreationHelper();

		// Create the drawing patriarch. This is the top level container for all
		// shapes.
		Drawing drawing = sheet.createDrawingPatriarch();

		// add a picture shape
		ClientAnchor anchor1 = helper.createClientAnchor();
		
		// setting the exact position of the logos; anchor1 -> unilever logo ;
		// anchor2 -> MIR logo
		anchor1.setCol1(1);
		anchor1.setRow1(1);
		anchor1.setCol2(1);
		anchor1.setRow2(7);
		anchor1.setDx1(50 * XSSFShape.EMU_PER_PIXEL);
		anchor1.setDx2(150 * XSSFShape.EMU_PER_PIXEL);
		anchor1.setDy1(20 * XSSFShape.EMU_PER_PIXEL);
		anchor1.setDy2(20 * XSSFShape.EMU_PER_PIXEL);
		
		Picture pict1 = drawing.createPicture(anchor1, pictureIdx1);

		// drawing the thick border around the logos
		Row row_2 = sheet.createRow(2);
		Cell cell_12 = row_2.createCell(1);
		CellStyle cellstyle_22 = workbook.createCellStyle();
		cellstyle_22.setBorderLeft(CellStyle.BORDER_THICK);
		cell_12.setCellStyle(cellstyle_22);
		Cell cell_27 = row_2.createCell(10);
		CellStyle cellstyle_27 = workbook.createCellStyle();
		cellstyle_27.setBorderRight(CellStyle.BORDER_THICK);
		cell_27.setCellStyle(cellstyle_27);
	
		Row row_3 = sheet.createRow(3);
		Cell cell_33 = row_3.createCell(1);
		cell_33.setCellStyle(cellstyle_22);
		Cell cell_37 = row_3.createCell(10);
		cell_37.setCellStyle(cellstyle_27);

		Row row_4 = sheet.createRow(4);
		Cell cell_42 = row_4.createCell(1);
		cell_42.setCellStyle(cellstyle_22);
		Cell cell_47 = row_4.createCell(10);
		cell_47.setCellStyle(cellstyle_27);

		Row row_5 = sheet.createRow(5);
		Cell cell_52 = row_5.createCell(1);
		cell_52.setCellStyle(cellstyle_22);
		Cell cell_57 = row_5.createCell(10);
		cell_57.setCellStyle(cellstyle_27);
		
		CellStyle style61 = workbook.createCellStyle();
		XSSFFont font61 = workbook.createFont();
		font61.setBold(true);
		style61.setFont(font61);
		Cell cell_label = row_5.createCell(8);
		cell_label.setCellValue("Legend:");
		cell_label.setCellStyle(style61);

		Row row_6 = sheet.createRow(6);
		Cell cell_62 = row_6.createCell(1);
		cell_62.setCellStyle(cellstyle_22);
		Cell cell_67 = row_6.createCell(10);
		cell_67.setCellStyle(cellstyle_27);
		Row row_7 = sheet.createRow(7);
		Cell cell_72 = row_7.createCell(1);
		cell_72.setCellStyle(cellstyle_22);
		Cell cell_77 = row_7.createCell(10);
		cell_77.setCellStyle(cellstyle_27);
		Row row_8 = sheet.createRow(8);
		Cell cell_82 = row_8.createCell(1);
		CellStyle cellstyle_82 = workbook.createCellStyle();
		cellstyle_82.setBorderBottom(CellStyle.BORDER_THICK);
		cellstyle_82.setBorderLeft(CellStyle.BORDER_THICK);
		cell_82.setCellStyle(cellstyle_82);
		Cell cell_87 = row_8.createCell(10);
		CellStyle cellstyle_87 = workbook.createCellStyle();
		cellstyle_87.setBorderBottom(CellStyle.BORDER_THICK);
		cellstyle_87.setBorderRight(CellStyle.BORDER_THICK);
		cell_87.setCellStyle(cellstyle_87);
		Cell cell_83 = row_8.createCell(4);
		CellStyle cellstyle_83 = workbook.createCellStyle();
		cellstyle_83.setBorderBottom(CellStyle.BORDER_THICK);
		cell_83.setCellStyle(cellstyle_83);
		Cell cell_84 = row_8.createCell(5);
		cell_84.setCellStyle(cellstyle_83);
		Cell cell_85 = row_8.createCell(6);
		cell_85.setCellStyle(cellstyle_83);
		Cell cell_86 = row_8.createCell(7);
		cell_86.setCellStyle(cellstyle_83);
		Cell cell_81 = row_8.createCell(2);
		cell_81.setCellStyle(cellstyle_83);
		Cell cell_833 = row_8.createCell(3);
		cell_833.setCellStyle(cellstyle_83);
		Cell cell_88 = row_8.createCell(8);
		cell_88.setCellStyle(cellstyle_83);
		Cell cell_89 = row_8.createCell(9);
		cell_89.setCellStyle(cellstyle_83);

		// merging cells for table name and images
		CellRangeAddress cra1 = new CellRangeAddress(1, 8, 1, 1);
		CellRangeAddress cra2 = new CellRangeAddress(1, 2, 2, 8);
		CellRangeAddress cra4 = new CellRangeAddress(6, 8, 3, 6);
		CellRangeAddress cra5 = new CellRangeAddress(3, 3, 3, 5);
		CellRangeAddress cra6 = new CellRangeAddress(4, 4, 3, 5);
		CellRangeAddress cra7 = new CellRangeAddress(5, 5, 3, 5);

		sheet.addMergedRegion(cra1);
		sheet.addMergedRegion(cra2);
		//sheet.addMergedRegion(cra3);
		sheet.addMergedRegion(cra4);
		sheet.addMergedRegion(cra5);
		sheet.addMergedRegion(cra6);
		sheet.addMergedRegion(cra7);

		// Header of the Disposal table
		Row row2 = sheet.createRow(10);

		Cell cell0 = row2.createCell(0);

		Cell cell1 = row2.createCell(1);
		cell1.setCellValue("     Component     ");

		Cell cell2 = row2.createCell(2);
		cell2.setCellValue("     Layers     ");

		Cell cell3 = row2.createCell(3);
		cell3.setCellValue("                   Country                   ");

		Cell cell4 = row2.createCell(4);
		cell4.setCellValue("    Material Type    ");

		Cell cell5 = row2.createCell(5);
		cell5.setCellValue("    Weight(%)    ");

		Cell cell6 = row2.createCell(6);
		cell6.setCellValue("    Recycled(%)    ");

		Cell cell7 = row2.createCell(7);
		cell7.setCellValue("    Weight(g)    ");

		Cell cell8 = row2.createCell(8);
		cell8.setCellValue("  GHG from Packaging  \n  Materials (g CO2e)  ");

		Cell cell9 = row2.createCell(9);
		cell9.setCellValue("GHG from Packaging  \n Disposal (g CO2e)   ");

		Cell cell10 = row2.createCell(10);
		String euro = "\u20ac";
		cell10.setCellValue("Est. EPR Tax \n (" + euro + " per 1,000 Units)     ");

		XSSFFont font = workbook.createFont();
		font.setColor(IndexedColors.WHITE.getIndex());

		CellStyle style1 = workbook.createCellStyle();
		style1.setBorderBottom(CellStyle.BORDER_THIN);
		style1.setBorderTop(CellStyle.BORDER_THIN);
		style1.setBorderRight(CellStyle.BORDER_THIN);
		style1.setBorderLeft(CellStyle.BORDER_THIN);
		style1.setAlignment(CellStyle.ALIGN_CENTER);
		style1.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
		style1.setFillPattern(CellStyle.SOLID_FOREGROUND);
		style1.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		style1.setFont(font);
		
		CellStyle style2 = workbook.createCellStyle();
		style2.setBorderBottom(CellStyle.BORDER_THIN);
		style2.setBorderTop(CellStyle.BORDER_THIN);
		style2.setBorderRight(CellStyle.BORDER_THIN);
		style2.setBorderLeft(CellStyle.BORDER_THIN);
		style2.setAlignment(CellStyle.ALIGN_CENTER);
		style2.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
		style2.setFillPattern(CellStyle.SOLID_FOREGROUND);
		style2.setFont(font);
		style2.setWrapText(true);
		
		CellStyle style21 = workbook.createCellStyle();
		style21.setBorderBottom(CellStyle.BORDER_THIN);
		style21.setBorderTop(CellStyle.BORDER_THIN);
		style21.setBorderRight(CellStyle.BORDER_THICK);
		style21.setBorderLeft(CellStyle.BORDER_THIN);
		style21.setAlignment(CellStyle.ALIGN_CENTER);
		style21.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		style21.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
		style21.setFillPattern(CellStyle.SOLID_FOREGROUND);
		style21.setFont(font);
		
		 row2.setHeightInPoints((2*sheet.getDefaultRowHeightInPoints()));

		cell1.setCellStyle(style1);
		cell2.setCellStyle(style1);
		cell3.setCellStyle(style21);
		cell4.setCellStyle(style1);
		cell5.setCellStyle(style1);
		cell6.setCellStyle(style1);
		cell7.setCellStyle(style1);
		cell8.setCellStyle(style2);
		cell9.setCellStyle(style2);
		cell10.setCellStyle(style2);

		// Disposal table to be written
		attributes = new ArrayList<String>();
		attributes.add("item_id");
		attributes.add("object_name");
		attributes.add("u4_key_country");
		attributes.add("u4_type");
		attributes.add("u4_weight_percentage");
		attributes.add("u4_recycled_percentage");
		attributes.add("u4_est_weight");
		attributes.add("u4_ghg");
		attributes.add("u4_ghg_from_disposal");
		attributes.add("u4_tax");

		// get only those forms from the components that have the same countries as on the project revision
		//get env material forms from the pam revision
		
		ArrayList<EnvLayerData> formarray = new ArrayList<EnvLayerData>();
		ArrayList<EnvLayerData> summaryFormarray = new ArrayList<EnvLayerData>();
		compObjs = new ArrayList<EnvLayerData>();
		int rownum = 11;
		session = (TCSession) AIFDesktop.getActiveDesktop().getCurrentApplication().getSession();
		//get all mapping items for calculation
		getMappingValues();
		projRev = itemRevision;
		for (int i = 0; i < specComp.length; i++) {
			formarray = processItemData(specComp[i]);
			compObjs.addAll(formarray);
		}

		// sort the forms based on the country names
		ArrayList<EnvLayerData> sorted = new ArrayList<EnvLayerData>(compObjs);
		Collections.sort(sorted, new Comparator<EnvLayerData>() {
			public int compare(EnvLayerData o1, EnvLayerData o2) {
				String ctry1 = null;
				String ctry2 = null;
				ctry1 = o1.getKey_country();
				ctry2 = o2.getKey_country();
				return ctry1.compareTo(ctry2);
			}
		});
		
		for (int i = 0; i < specComp.length; i++) {			
			summaryFormarray = getSummaryData(sorted,specComp[i]);
		}	

		for (EnvLayerData tempform1 : summaryFormarray) {
			for (EnvLayerData tempform2 : sorted) {
				if (tempform1.getKey_country().equals(tempform2.getKey_country())) {
					int inx = sorted.indexOf(tempform2);
					sorted.add(inx, tempform1);
					break;
				}
			}
		}
		
		printTabledata(sorted, rownum);

		// add legend info
		rownum += sorted.size() + 1;

		// auto-resize the column as per the text size
		for (int i = 1; i < attributes.size() + 1; i++) {
			sheet.autoSizeColumn(i);
		}
		// add the project name, created date and the user name to the header table
		// doing it here because if i try to add this info before resizing
		// the column, i run into an exception regarding the rendering of the formatted rich text
		// this is a work-around, no idea why i'm getting this exception
		Cell cell_35 = row_3.createCell(3);
		Cell cell_45 = row_4.createCell(3);
		Cell cell_55 = row_5.createCell(3);
		Cell cell_65 = row_6.createCell(3);
		String proj_name = null;
		String created_by = null;
		String creation_date = null;
		String Desc_text = null;
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
		} catch (TCException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		CellStyle cellstyle_35 = workbook.createCellStyle();
		cellstyle_35.setAlignment(CellStyle.ALIGN_LEFT);

		XSSFRichTextString s1 = new XSSFRichTextString(proj_name);
		XSSFFont font2 = workbook.createFont();
		font2.setBold(true);
		s1.applyFont(0, 7, font2);
		cell_35.setCellValue(s1);
		cell_35.setCellStyle(cellstyle_35);
		XSSFRichTextString s2 = new XSSFRichTextString(created_by);
		s2.applyFont(0, 10, font2);
		cell_45.setCellValue(s2);
		cell_45.setCellStyle(cellstyle_35);

		XSSFRichTextString s3 = new XSSFRichTextString(creation_date);
		s3.applyFont(0, 10, font2);
		cell_55.setCellValue(s3);
		cell_55.setCellStyle(cellstyle_35);

		CellStyle cellstyle_65 = workbook.createCellStyle();
		cellstyle_65.setAlignment(CellStyle.ALIGN_LEFT);
		cellstyle_65.setVerticalAlignment(CellStyle.VERTICAL_TOP);
		cellstyle_65.setLocked(false);
		cellstyle_65.setWrapText(true);
		XSSFRichTextString s4 = new XSSFRichTextString(Desc_text);
		s4.applyFont(0, 11, font2);
		cell_65.setCellValue(s4);
		cell_65.setCellStyle(cellstyle_65);
		
		CellStyle style4 = workbook.createCellStyle();
		style4.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
		style4.setFillPattern(CellStyle.SOLID_FOREGROUND);
		Cell cell_yellow = row_6.createCell(8);
		cell_yellow.setCellStyle(style4);
		Cell cell_yellow_label = row_6.createCell(9);
		cell_yellow_label.setCellValue("Component Total per Country");
		
		if(WtZero){
			CellRangeAddress cra11 = new CellRangeAddress(7, 7, 8, 10);
			sheet.addMergedRegion(cra11);
			Row row7 = sheet.getRow(7);
			Cell cell_78 = row7.createCell(8);
		
			XSSFFont whiteFont = workbook.createFont();
			whiteFont.setColor(IndexedColors.WHITE.getIndex());
			
			CellStyle cellstyle_78 = workbook.createCellStyle();
			cellstyle_78.setFillForegroundColor(IndexedColors.RED.getIndex());
			cellstyle_78.setFillPattern(CellStyle.SOLID_FOREGROUND);
			cellstyle_78.setAlignment(CellStyle.ALIGN_CENTER);
			cellstyle_78.setFont(whiteFont);
			
			cell_78.setCellValue("At least one weight value is zero, resulting in invalid results");
			cell_78.setCellStyle(cellstyle_78);
		}

		sheet.lockDeleteColumns();
		sheet.lockDeleteRows();
		sheet.lockFormatCells();
		sheet.lockFormatColumns();
		sheet.lockFormatRows();
		sheet.lockInsertColumns();
		sheet.lockInsertRows();
		sheet.enableLocking();
		workbook.lockStructure();

		String tempDir = System.getProperty("java.io.tmpdir");

		// Write the workbook in file system
		try {
			String dsname = null;
			String pdfFileNeme = null;
			
			String name = projRev.getStringProperty(UL4Common.OBJECT_NAME).replace(" ","_");
			String revid = projRev.getStringProperty(UL4Common.REVID);
			dsname = "EPM_Project_"+name + "_" +revid + "_" +  getCurrentTimeStamp();
			pdfFileNeme = tempDir + dsname + "." + "xlsx" ; 
			FileOutputStream out = new FileOutputStream(new File(pdfFileNeme));
			workbook.write(out);
			out.close();

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

		session.setStatus("Environment Packaging Material Report Generation Completed");
		if(UnileverUtility.isPerfMonitorTriggered == true)
			UnileverUtility.getPerformanceTime(startTime, "Generating Environment Packaging Material Layers Report" );
	}
}
