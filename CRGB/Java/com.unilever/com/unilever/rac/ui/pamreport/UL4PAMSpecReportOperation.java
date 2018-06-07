/* Source File Name:   UL4PAMSpecReportOperation
 *
 * Description:  This file contains code to generate PAM Specification PDF Report
 * 
 * Super Class : AbstractAIFOperation
 * 
 * Revision History:
 * -------------------------------------------------------------------------
 * Revision    Author                Date Created           Reason
 * -------------------------------------------------------------------------
 *  1.0.0       Dhinakaran.V          30/09/2014            Initial Creation
 *  2.0.0       Sushmita T            30/05/2017            iText to generate PDF
 */

package com.unilever.rac.ui.pamreport;


import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.itextpdf.forms.PdfAcroForm;
import com.itextpdf.io.font.FontConstants;
import com.itextpdf.kernel.color.Color;
import com.itextpdf.kernel.color.DeviceRgb;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfNumber;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.xobject.PdfFormXObject;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.border.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.VerticalAlignment;
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
import com.teamcenter.rac.kernel.TCComponentTcFile;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;
import com.unilever.rac.pam.LoadPAMConfigurations;
import com.unilever.rac.pam.PAMConstant;
import com.unilever.rac.pam.PAMPropertyNameValue;
import com.unilever.rac.pam.PAMSecondaryPropValue;
import com.unilever.rac.pam.PAMTableConfiguration;
import com.unilever.rac.ui.common.UL4Common;
/**
 * The Class UL4PAMSpecReportOperation.
 */

public class UL4PAMSpecReportOperation extends AbstractAIFOperation
{	

    private static final String trueChars = "Yes";

	private static final String falseChars = "NULL";

	/** Pack Component revision  */	 
	public TCComponent packRevision		= null ;
	
    /** PAM revision  */	 
	public TCComponent pamRevision      = null ;

	/** TCSession */
	public TCSession session       		= null ;
	
    /** The Registry */	 
	private Registry reg            	= null ;
	
    /** File */	 
	private  File pdffile           	= null ;

	/** File */	 
	private File xmlfile            	= null ;

    /** File */	 
	private File xsltfile               = null ;

	/** File */	 
	private File iamgeLoc               = null ;

	/** Dataset */	 
	private boolean dataset             = false ;
	
	/** PNG Images*/	 
	private boolean images              = false ;	
	
	private boolean imgRequired            = false ;	

	private StringBuilder sbPDF         = null ;
	
	private StringBuilder sbPrintPDF    = null ;
	
	private StringBuilder sbAssociated  = null ;
	
	private StringBuilder sbRelease  = null ;
		
	private String xsltFileName         = null ;
	
	private String Drawings           = "u4_drawings";
				
	private String packageDetailsRelation = "U4_PackageDetailsRelation";
	private String articleDetailsRelation = "U4_ArticleRelation";
	private String materialsRelation = "U4_MaterialsRelation";
	private String protectionRelation = "U4_ProtectionRelation";
	private String softwareRelation = "U4_SoftwareRelation";
	private String rcvUsageCondRelation = "U4_RcvgSiteUsgCondRelation";
	private String envRelation = "U4_EnvironmentalRelation";
	private String compPropRelation = "U4_CompPropertyRelation";
	private String integratedLabelRelation = "U4_IntegratedLabelRelation";
	private String ASSOCIATED           = "U4_AssociatedSpecRelation";
/**
 * Constructor 
 * 
 * @param packrevision
 * @param pamrevision
 * @param tcsession
 */
			
	public UL4PAMSpecReportOperation(TCComponent packrevision , TCComponent pamrevision , TCSession tcsession , boolean imageRequired)
	{
		packRevision = packrevision ;
		pamRevision = pamrevision ;
		session = tcsession;
		reg = Registry.getRegistry( this ); 
		sbPrintPDF = new StringBuilder();
		sbPDF = new StringBuilder();
		sbAssociated =  new StringBuilder(); 
		sbRelease = new StringBuilder();		
		imgRequired = imageRequired ;

	}	
	
	@Override
	public void executeOperation() 
	{
		String pdfFileNeme        = null ; 
		String xmlFileName  	  = null ;	
		String tempDir            = null ;
		String dsName      		  = null ;
		
		try
		{
			String pam_id 			  = pamRevision.getStringProperty(UL4Common.ITEMID);
			String name     	 	  = pamRevision.getStringProperty(UL4Common.OBJECT_NAME).replace(" ","_");
			name                      = formatFileName(name);
			String revid    	 	  = pamRevision.getStringProperty(UL4Common.REVID);
			String timestamp 		  = getCurrentTimeStampForFIleName();
			tempDir   		  		  = System.getProperty("java.io.tmpdir");			
		    dsName       			  = pam_id + "_" + name + "_" +revid + "_" +  getCurrentTimeStamp()  ;
			pdfFileNeme               = tempDir + dsName + "." + "pdf" ;     
			xmlFileName  	          = tempDir + File.separator + timestamp + "." + "xml" ; 	
			String fileName 		  = timestamp + "." + "xml" ;
			String pam_frame_type 	  = pamRevision.getStringProperty("u4_pam_frame_type");
			
			SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String timeStamp1 = sdfDate.format(new Date());
			
			
			if(pam_frame_type.equals("G-PAM-COPACK"))
			generateCopackPDF(pdfFileNeme,dsName,timeStamp1);
			else
				generatePdf(pdfFileNeme, pamRevision,dsName,timeStamp1);	
			
		}
		catch(TCException e)
		{
			e.printStackTrace();			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private void generateCopackPDF(String dest, String dsName, String timestamp) throws Exception {
		// TODO Auto-generated method stub
		
		PdfDocument pdfDoc = new PdfDocument(new PdfWriter(dest).setSmartMode(true));
        Document doc = new Document(pdfDoc, PageSize.A4);
 
        session.setStatus("Building PDF report Header.......");
		System.out.println("Building PDF report Header.......");
        
        TableHeaderEventHandler handler = new TableHeaderEventHandler(doc,pamRevision,timestamp);
        pdfDoc.addEventHandler(PdfDocumentEvent.END_PAGE, handler);
        doc.setMargins(40 + handler.getTableHeight(), 36, 36+60, 36);
        float headerHt = handler.getTableHeight();
        
        Table reasonForIssueTable = new Table(1);
        reasonForIssueTable.setWidth(523);
        
        Table finalSpec = new Table(1);
        finalSpec.setWidth(523);
        
        Table classification = new Table(1);
        classification.setWidth(523);
        
        Table generalInfo = new Table(1);
        generalInfo.setWidth(523);
        
        Table TechDrw = new Table(1);
        TechDrw.setWidth(523);
        Table TechDrw1 = new Table(1);
        TechDrw1.setWidth(523);
		
        Table compProp = new Table(1);
        compProp.setWidth(523);
        
        Table env = new Table(1);
        env.setWidth(523);
        
        PdfFont regular = null;
        PdfFont bold = null;       
        Color lavender = null;
        String sReasonforIssue = null;        
        ArrayList<DatasetInfo> datasetType = null;                      
        try {
			regular = PdfFontFactory.createFont(FontConstants.TIMES_ROMAN);
			bold = PdfFontFactory.createFont(FontConstants.TIMES_BOLD);		
			lavender = new DeviceRgb(230, 230, 255);
			sReasonforIssue = pamRevision.getStringProperty(UL4Common.REASONFORISSUE);			
			datasetType = getDatasetType();			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        session.setStatus("Generating PAM Specification PDF Report.......");
	    System.out.println("Generating PAM Specification PDF Report.......");
        
        //add the Reason for Issue section to report
        reasonForIssueTable.addCell(new Cell().setFont(bold).setFontSize(12).setPadding(0).setBackgroundColor(lavender)
        		.add("Reason for Issue")
        		.setBorder(Border.NO_BORDER)); 
        doc.add(new Paragraph("\n"));
             
        doc.add(reasonForIssueTable);
        
        Paragraph p1 = new Paragraph(sReasonforIssue);
        p1.setFontSize(9).setFont(regular);
        doc.add(p1);  
        doc.add(new Paragraph("\n"));
        
        //add the Classification section to the report
        classification.addCell(new Cell().setFont(bold).setFontSize(12).setPadding(0).setBackgroundColor(lavender)
        		.add("Classification")
        		.setBorder(Border.NO_BORDER));
                
        doc.add(classification);
        doc.add(new Paragraph("\n"));
        
        Table classInfo = new Table(new float [] {261,262});
        classInfo.addCell("Component Class").setPadding(0).setFontSize(8); 
        classInfo.addCell(getMatlClassificationValue(UL4Common.COMP_CLASS)).setPadding(0).setFontSize(8);
        classInfo.addCell("Component Commodity").setPadding(0).setFontSize(8); 
        classInfo.addCell(getMatlClassificationValue(UL4Common.COMP_COMMODITY)).setPadding(0).setFontSize(8);
        classInfo.addCell("Material Class").setPadding(0).setFontSize(8); 
        classInfo.addCell(getMatlClassificationValue(UL4Common.MAT_CLASS)).setPadding(0).setFontSize(8);
        classInfo.addCell("Material Commodity").setPadding(0).setFontSize(8);
        classInfo.addCell(getMatlClassificationValue(UL4Common.MAT_COMMODITY)).setPadding(0).setFontSize(8);
        classInfo.addCell("Delivered Form").setPadding(0).setFontSize(8); 
        classInfo.addCell(getMatlClassificationValue(UL4Common.DELIVERED_FORM)).setPadding(0).setFontSize(8);
        classInfo.addCell("PAM Frame Type").setPadding(0).setFontSize(8); 
        classInfo.addCell(getMatlClassificationValue(UL4Common.PAM_FRAME)).setPadding(0).setFontSize(8);
        classInfo.addCell("Technology").setPadding(0).setFontSize(8); 
        classInfo.addCell(getMatlClassificationValue(UL4Common.TECHNOLOGY)).setPadding(0).setFontSize(8);
        classInfo.addCell("Manufacturing Processes").setPadding(0).setFontSize(8); 
        classInfo.addCell(getMatlClassificationValue(UL4Common.MFG_PROCESS)).setPadding(0).setFontSize(8);
        classInfo.addCell("Shape / Feature").setPadding(0).setFontSize(8); 
        classInfo.addCell(getMatlClassificationValue(UL4Common.SHAPE)).setPadding(0).setFontSize(8);
        classInfo.addCell("CU Declared Weight / Volume").setPadding(0).setFontSize(8); 
        classInfo.addCell(getMatlClassificationValue(UL4Common.WEIGHT_VOLUME)).setPadding(0).setFontSize(8);
        doc.add(classInfo);
        doc.add(new Paragraph("\n"));
        
        //add General Information section to the report
        generalInfo.addCell(new Cell().setFont(bold).setFontSize(12).setPadding(0).setBackgroundColor(lavender)
        		.add("General Information")
        		.setBorder(Border.NO_BORDER)); 
        doc.add(new Paragraph("\n"));             
        doc.add(generalInfo);
        
        Paragraph p2 = new Paragraph("Description / Application");
        p2.setFontSize(10).setFont(bold);
        doc.add(p2);
        p2 = new Paragraph("Description");
        p2.setFontSize(10).setFont(bold);
        doc.add(p2);
        String desc = getFormValue(UL4Common.GENERALINFORELATION, "General Information_Description", "u4_text");        
        doc.add(new Paragraph(desc).setFont(regular).setFontSize(8));
        
        desc = getFormValue(UL4Common.GENERALINFORELATION, "General Information_Application", "u4_text");  
        if(desc!=null)
        {
        	p2 = new Paragraph("Application");
            p2.setFontSize(10).setFont(bold);
            doc.add(p2);       
            
            doc.add(new Paragraph(desc).setFont(regular).setFontSize(8));
            
        	doc.add(new Paragraph("\n"));
        }
        
        //doc.add(new Paragraph("\n"));
        p2 = new Paragraph("Additional Information");
        p2.setFontSize(10).setFont(bold);
        doc.add(p2);
       
        String patentNo = getFormValue(UL4Common.GENERALINFORELATION, "Patent No.", "u4_description");
        String patentPendingNo = getFormValue(UL4Common.GENERALINFORELATION, "Patent Pending No.", "u4_description");
        String patentNoProp = getFormValue(UL4Common.GENERALINFORELATION, "Patent No.", "u4_property");
        String patentPendingNoProp = getFormValue(UL4Common.GENERALINFORELATION, "Patent Pending No.", "u4_property");
        String CU_barcode = getFormValue(UL4Common.GENERALINFORELATION, "CU Barcode Ref.", "u4_property");
        String CU_barcode_desc = getFormValue(UL4Common.GENERALINFORELATION, "CU Barcode Ref.", "u4_description");
        Table addlInfo = new Table(new float [] {261,262});
        addlInfo.addCell(new Cell().add("Property").setFont(bold).setFontSize(8));
        addlInfo.addCell(new Cell().add("Description").setFont(bold).setFontSize(8));
        boolean isAddInfoEmpty = true;
        if(patentNo.length()!=0)
        {
	        addlInfo.addCell(new Cell().add(patentNoProp).setFont(regular).setFontSize(8));
	        addlInfo.addCell(new Cell().add(patentNo).setFont(regular).setFontSize(8));
	        isAddInfoEmpty = false;
        }
        if(patentPendingNo.length()!=0){
        	 addlInfo.addCell(new Cell().add(patentPendingNoProp).setFont(regular).setFontSize(8));
             addlInfo.addCell(new Cell().add(patentPendingNo).setFont(regular).setFontSize(8));
             isAddInfoEmpty = false;
        } 
        if(CU_barcode!=null && CU_barcode_desc.length()!=0){
       	 addlInfo.addCell(new Cell().add(CU_barcode).setFont(regular).setFontSize(8));
       	 addlInfo.addCell(new Cell().add(CU_barcode_desc).setFont(regular).setFontSize(8));
       	isAddInfoEmpty = false;
       } 
        if (!isAddInfoEmpty) {
	        doc.add(addlInfo);
	        doc.add(new Paragraph("\n"));
        }
        
        // add technical drawings to report        
        if(imgRequired)
        {
        	Table datasetNames = new Table(new float [] {175,174,175});        	

        	DatasetInfo dsinfo = datasetType.get(0);

        	TechDrw.addCell(new Cell().setFont(bold).setFontSize(12).setPadding(0).setBackgroundColor(lavender)
        			.add("Technical Drawings")
        			.setBorder(Border.NO_BORDER)); 
        	doc.add(TechDrw);
        	doc.add(new Paragraph("\n"));	
        	datasetNames.addCell(new Cell().add("Drawing No.").setFont(bold).setFontSize(8));
        	datasetNames.addCell(new Cell().add("Type").setFont(bold).setFontSize(8));        
        	datasetNames.addCell(new Cell().add("Description").setFont(bold).setFontSize(8));

        	for(int i= 0;i<datasetType.size();i++)
        	{        		
    			datasetNames.addCell(new Cell().add(datasetType.get(i).getDatsetId()).setFont(regular).setFontSize(8));
    			datasetNames.addCell(new Cell().add(datasetType.get(i).getDatasetType()).setFont(regular).setFontSize(8));
    			datasetNames.addCell(new Cell().add(datasetType.get(i).getDatasetDesc()).setFont(regular).setFontSize(8));
    		}
        	doc.add(datasetNames);
        	doc.add(new Paragraph( "\n"));
        	for(DatasetInfo dsinfo1: datasetType)
        	{      		
				if(dsinfo1.getDatasetfilepath()!=null)
				{    	        	
					PdfDocument assocSpecDataset = new PdfDocument(new PdfReader(dsinfo1.getDatasetfilepath()));  
	
					for(int inx = 1;inx<=assocSpecDataset.getNumberOfPages();inx++)
					{	
						PdfPage origPage = assocSpecDataset.getPage(inx);	        
						Rectangle rect = origPage.getPageSizeWithRotation();	     
						PdfNumber rotate = origPage.getPdfObject().getAsNumber(PdfName.Rotate);	  		        
	
						PdfReader reader = new PdfReader(dsinfo1.getDatasetfilepath());
						reader.setUnethicalReading(true);
						String tempDir = System.getProperty("java.io.tmpdir");
						tempDir = tempDir+"tempPdf.pdf";
						PdfDocument tempPdfDoc = new PdfDocument(reader, new PdfWriter(tempDir));	    		        
	
						PdfAcroForm form = PdfAcroForm.getAcroForm(tempPdfDoc, true);
						if(form.getFormFields()!=null)
						{	        	
							if(rotate!=null)
								if(rotate.getValue()!=0)
								{
									PdfPage page = tempPdfDoc.getPage(inx);	
									page.setRotation((rotate.intValue() + 90) % 360);		        		
								}
							form.flattenFields();
							tempPdfDoc.close();		        	
						}    
	
						tempPdfDoc = new PdfDocument(new PdfReader(tempDir));
						origPage = tempPdfDoc.getPage(inx);	
	
						PdfFormXObject pageCopy = origPage.copyAsFormXObject(pdfDoc);		        
						Image image = new Image(pageCopy);
						image.setBorder(Border.NO_BORDER);
	
						if(rect.getHeight()<=rect.getWidth()){
							image.setWidthPercent(88);
						}
						else
						{	    
							image.setWidthPercent(78);
							//image.setHeight(rect.getHeight()-headerHt);
						}		       
						doc.add(image);
						tempPdfDoc.close();
					}
	
					assocSpecDataset.close();  	        
				}   	
        	}
        }
        
      //add Environmental Section
        env.addCell(new Cell().setFont(bold).setFontSize(12).setPadding(0).setBackgroundColor(lavender)
        		.add("Environmental")
        		.setBorder(Border.NO_BORDER)); 
        doc.add(new Paragraph("\n"));             
        doc.add(env);
        doc.add(new Paragraph("\n"));
        
        Map<String, List<TCComponent>> tcname_tccomponent = getForms(envRelation);        
        Vector<Vector<PAMSecondaryPropValue>> PAMPropertyNameValue_v = getEPAMTableConfig(tcname_tccomponent,envRelation);
        
        for(int inx=0;inx<PAMPropertyNameValue_v.size();inx++)
		{
        	PAMSecondaryPropValue propvalue = (PAMPropertyNameValue_v.get(inx)).get(0);
        	String tableType = propvalue.selectedComponent.getDisplayType();
        	doc.add(new Paragraph().add(tableType).setFontSize(10).setFont(bold));
        	float columnWidths [] = new float[propvalue.propNameValuepair.length];
        	for(  int ColumnNumber =0;ColumnNumber< propvalue.propNameValuepair.length;ColumnNumber++ )
    		{
        		columnWidths[ColumnNumber] = propvalue.propNameValuepair[ColumnNumber].columnSize;        	
    		}
        	Table packageDetailsInfo = new Table(columnWidths);
        	for(  int ColumnNumber =0;ColumnNumber< propvalue.propNameValuepair.length;ColumnNumber++ )
    		{
        		packageDetailsInfo.addHeaderCell(new Cell().add( propvalue.propNameValuepair[ColumnNumber].propDisplayName).setFont(bold).setFontSize(8));        		       	
    		}
        	for(  int j =0;j< (PAMPropertyNameValue_v.get(inx)).size();j++ )
    		{
        		PAMSecondaryPropValue propvalue1 = (PAMPropertyNameValue_v.get(inx)).get(j);
    			boolean rowEmpty = isRowEmpty(propvalue1, envRelation,tableType);
        		for(  int ColumnNumber =0;ColumnNumber< propvalue1.propNameValuepair.length;ColumnNumber++ )
        		{       			
        			if(!rowEmpty)        			
        				packageDetailsInfo.addCell(new Cell().add( propvalue1.propNameValuepair[ColumnNumber].propValue).setFont(regular).setFontSize(8));
        			/*else
        				packageDetailsInfo.addCell(new Cell().add(" ").setFont(regular).setFontSize(8));*/
        		}        		       	
    		}
        	doc.add(packageDetailsInfo);
        	doc.add(new Paragraph("\n"));
		}
        
       	 p2 = new Paragraph("Environmental Materials Details");
         p2.setFontSize(10).setFont(bold);
         doc.add(p2);
        
        desc = getFormValue(envRelation, "Environmental_Environmental Material Details", "u4_text");   
        doc.add(new Paragraph(desc).setFont(regular).setFontSize(8));
        doc.add(new Paragraph("\n"));
        
        //add Component Properties Section
        compProp.addCell(new Cell().setFont(bold).setFontSize(12).setPadding(0).setBackgroundColor(lavender)
        		.add("Component Properties")
        		.setBorder(Border.NO_BORDER)); 
        doc.add(new Paragraph("\n"));             
        doc.add(compProp);
        doc.add(new Paragraph("\n"));
        
        tcname_tccomponent = getForms(compPropRelation);        
        PAMPropertyNameValue_v = getEPAMTableConfig(tcname_tccomponent,compPropRelation);
        for(int inx=0;inx<PAMPropertyNameValue_v.size();inx++)
		{
        	PAMSecondaryPropValue propvalue = (PAMPropertyNameValue_v.get(inx)).get(0);
        	String tableType = propvalue.selectedComponent.getDisplayType();
        	doc.add(new Paragraph().add(tableType).setFontSize(10).setFont(bold));
        	float columnWidths [] = new float[propvalue.propNameValuepair.length];
        	for(  int ColumnNumber =0;ColumnNumber< propvalue.propNameValuepair.length;ColumnNumber++ )
    		{
        		columnWidths[ColumnNumber] = propvalue.propNameValuepair[ColumnNumber].columnSize;        	
    		}
        	Table packageDetailsInfo = new Table(columnWidths);
        	for(  int ColumnNumber =0;ColumnNumber< propvalue.propNameValuepair.length;ColumnNumber++ )
    		{
        		packageDetailsInfo.addHeaderCell(new Cell().add( propvalue.propNameValuepair[ColumnNumber].propDisplayName).setFont(bold).setFontSize(8));        		       	
    		}
        	for(  int j =0;j< (PAMPropertyNameValue_v.get(inx)).size();j++ )
    		{
        		PAMSecondaryPropValue propvalue1 = (PAMPropertyNameValue_v.get(inx)).get(j);
    			boolean rowEmpty = isRowEmpty(propvalue1, compPropRelation,tableType);
        		for(  int ColumnNumber =0;ColumnNumber< propvalue1.propNameValuepair.length;ColumnNumber++ )
        		{       			
        			if(!rowEmpty)   
        			{
        				// Added below if check as part of CR467
        				if(propvalue1.propNameValuepair[ColumnNumber].propDisplayName.equals("CoA") || propvalue1.propNameValuepair[ColumnNumber].propDisplayName.equals("Cr"))
        				{
        					if(propvalue1.propNameValuepair[ColumnNumber].propValue.equals("True"))        					
        						packageDetailsInfo.addCell(new Cell().add(trueChars).setFont(regular).setFontSize(8));        					
        					else
        						packageDetailsInfo.addCell(new Cell().add(falseChars).setFont(regular).setFontSize(8));
        				}
        				else
        					packageDetailsInfo.addCell(new Cell().add( propvalue1.propNameValuepair[ColumnNumber].propValue).setFont(regular).setFontSize(8));
        			}
        			/*else
        				packageDetailsInfo.addCell(new Cell().add(" ").setFont(regular).setFontSize(8));*/
        		}        		       	
    		}
        	doc.add(packageDetailsInfo);
        	doc.add(new Paragraph("\n"));        	
		}
        desc = getFormValue(compPropRelation, "Component Properties_Properties Information", "u4_text");
        if(desc!=null){
        p2 = new Paragraph("Properties Information");
        p2.setFontSize(10).setFont(bold);
        doc.add(p2);    
           
        doc.add(new Paragraph(desc).setFont(regular).setFontSize(8));
        }
        desc = getFormValue(compPropRelation, "Component Properties_Property Name Notes", "u4_text");
        if(desc!=null){
        p2 = new Paragraph("Property Name Notes");
        p2.setFontSize(10).setFont(bold);
        doc.add(p2);    
           
        doc.add(new Paragraph(desc).setFont(regular).setFontSize(8));
        }
        
        doc.close();
        
        String name = pamRevision.getStringProperty(UL4Common.OBJECT_NAME).replace(" ","_");
        name = formatFileName(name);
        dsName = pamRevision.getStringProperty(UL4Common.ITEMID) + "_" + name + "_" +pamRevision.getStringProperty(UL4Common.REVID) + "_" +  getCurrentTimeStamp()  ;
        
        String newpdfFileNeme = System.getProperty("java.io.tmpdir") + dsName + "." + "pdf" ;
                        
        pdfDoc = new PdfDocument(new PdfReader(dest),new PdfWriter(newpdfFileNeme));
        doc = new Document (pdfDoc);
        Rectangle pageSize;        
        int n = pdfDoc.getNumberOfPages();
        for (int i = 1; i <= n; i++) {
        	PdfPage page = pdfDoc.getPage(i);
            pageSize = page.getPageSize();
            doc.showTextAligned(new Paragraph(String.format("page %s of %s", i, n)), pageSize.getWidth()-72,50, i, TextAlignment.RIGHT, VerticalAlignment.BOTTOM, 0);
        }
        pdfDoc.close();
        
        session.setStatus("Uploading PAM Specification PDF Report as Dataset.......");
        System.out.println("Uploading PAM Specification PDF Report as Dataset.......");
               
        uploadDataset(dsName, newpdfFileNeme);
    
	}

	protected void generatePdf(String dest, TCComponent pamRevision, String dsName, String timestamp) throws Exception {
		PdfDocument pdfDoc = new PdfDocument(new PdfWriter(dest).setSmartMode(true));
        Document doc = new Document(pdfDoc, PageSize.A4);
 
        session.setStatus("Building PDF report Header.......");
		System.out.println("Building PDF report Header.......");
        
        TableHeaderEventHandler handler = new TableHeaderEventHandler(doc,pamRevision,timestamp);
        pdfDoc.addEventHandler(PdfDocumentEvent.END_PAGE, handler);
        doc.setMargins(40 + handler.getTableHeight(), 36, 36+50, 36);
        float headerHt = handler.getTableHeight();
        
        Table reasonForIssueTable = new Table(1);
        reasonForIssueTable.setWidth(523);
        
        Table finalSpec = new Table(1);
        finalSpec.setWidth(523);
        
        Table classification = new Table(1);
        classification.setWidth(523);
        
        Table generalInfo = new Table(1);
        generalInfo.setWidth(523);
        
        Table packageDetails = new Table(1);
        packageDetails.setWidth(523);
        
        Table TechDrw = new Table(1);
        TechDrw.setWidth(523);
        Table TechDrw1 = new Table(1);
        TechDrw1.setWidth(523);
        
        Table materials = new Table(1);
        materials.setWidth(523);
        
        Table rcvUsageCond = new Table(1);
        rcvUsageCond.setWidth(523);
        
        Table env = new Table(1);
        env.setWidth(523);
        
        Table compProp = new Table(1);
        compProp.setWidth(523);
        
        Table intgLabel = new Table(1);
        intgLabel.setWidth(523);
        
        Table remarks = new Table(1);
        remarks.setWidth(523);
        
        PdfFont regular = null;
        PdfFont bold = null;       
        Color lavender = null;
        String sReasonforIssue = null;        
        ArrayList<DatasetInfo> datasetType = null;                      
        try {
			regular = PdfFontFactory.createFont(FontConstants.TIMES_ROMAN);
			bold = PdfFontFactory.createFont(FontConstants.TIMES_BOLD);		
			lavender = new DeviceRgb(230, 230, 255);
			sReasonforIssue = pamRevision.getStringProperty(UL4Common.REASONFORISSUE);			
			datasetType = getDatasetType();			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        session.setStatus("Generating PAM Specification PDF Report.......");
	    System.out.println("Generating PAM Specification PDF Report.......");
        
        //add the Reason for Issue section to report
        reasonForIssueTable.addCell(new Cell().setFont(bold).setFontSize(12).setPadding(0).setBackgroundColor(lavender)
        		.add("Reason for Issue")
        		.setBorder(Border.NO_BORDER)); 
        doc.add(new Paragraph("\n"));
             
        doc.add(reasonForIssueTable);
        
        Paragraph p1 = new Paragraph(sReasonforIssue);
        p1.setFontSize(9).setFont(regular);
        doc.add(p1);  
        doc.add(new Paragraph("\n"));
        
        //add the Classification section to the report
        classification.addCell(new Cell().setFont(bold).setFontSize(12).setPadding(0).setBackgroundColor(lavender)
        		.add("Classification")
        		.setBorder(Border.NO_BORDER));
                
        doc.add(classification);
        doc.add(new Paragraph("\n"));
        
        Table classInfo = new Table(new float [] {261,262});
        classInfo.addCell("Component Class").setPadding(0).setFontSize(8); 
        classInfo.addCell(getMatlClassificationValue(UL4Common.COMP_CLASS)).setPadding(0).setFontSize(8);
        classInfo.addCell("Component Commodity").setPadding(0).setFontSize(8); 
        classInfo.addCell(getMatlClassificationValue(UL4Common.COMP_COMMODITY)).setPadding(0).setFontSize(8);
        classInfo.addCell("Material Class").setPadding(0).setFontSize(8); 
        classInfo.addCell(getMatlClassificationValue(UL4Common.MAT_CLASS)).setPadding(0).setFontSize(8);
        classInfo.addCell("Material Commodity").setPadding(0).setFontSize(8);
        classInfo.addCell(getMatlClassificationValue(UL4Common.MAT_COMMODITY)).setPadding(0).setFontSize(8);
        classInfo.addCell("Delivered Form").setPadding(0).setFontSize(8); 
        classInfo.addCell(getMatlClassificationValue(UL4Common.DELIVERED_FORM)).setPadding(0).setFontSize(8);
        classInfo.addCell("PAM Frame Type").setPadding(0).setFontSize(8); 
        classInfo.addCell(getMatlClassificationValue(UL4Common.PAM_FRAME)).setPadding(0).setFontSize(8);
        classInfo.addCell("Technology").setPadding(0).setFontSize(8); 
        classInfo.addCell(getMatlClassificationValue(UL4Common.TECHNOLOGY)).setPadding(0).setFontSize(8);
        classInfo.addCell("Manufacturing Processes").setPadding(0).setFontSize(8); 
        classInfo.addCell(getMatlClassificationValue(UL4Common.MFG_PROCESS)).setPadding(0).setFontSize(8);
        classInfo.addCell("Shape / Feature").setPadding(0).setFontSize(8); 
        classInfo.addCell(getMatlClassificationValue(UL4Common.SHAPE)).setPadding(0).setFontSize(8);
        classInfo.addCell("CU Declared Weight / Volume").setPadding(0).setFontSize(8); 
        classInfo.addCell(getMatlClassificationValue(UL4Common.WEIGHT_VOLUME)).setPadding(0).setFontSize(8);
        doc.add(classInfo);
        doc.add(new Paragraph("\n"));
        
        //add General Information section to the report
        boolean isGenInfoEmpty = true;
        Paragraph p2 = null;
        
        String desc = getFormValue(UL4Common.GENERALINFORELATION, "General Information_Description", "u4_text");
        if (desc != null && !desc.isEmpty()) {
        	isGenInfoEmpty = false;
        } else {
        	desc = "";
        }
        
        String app = getFormValue(UL4Common.GENERALINFORELATION, "General Information_Application", "u4_text"); 
        if (app != null && !app.isEmpty()) {
        	isGenInfoEmpty = false;
        } else {
        	app = "";
        }
        
        if(!isGenInfoEmpty)
        {
        	generalInfo.addCell(new Cell().setFont(bold).setFontSize(12).setPadding(0).setBackgroundColor(lavender)
            		.add("General Information")
            		.setBorder(Border.NO_BORDER)); 
            doc.add(new Paragraph("\n"));             
            doc.add(generalInfo);
          
            p2 = new Paragraph("Description / Application");
            p2.setFontSize(10).setFont(bold);
            doc.add(p2);
            if (desc != null && !desc.isEmpty()) {
            p2 = new Paragraph("Description");
            p2.setFontSize(10).setFont(bold);
            doc.add(p2);
            doc.add(new Paragraph(desc).setFont(regular).setFontSize(8));
            }
            
            if (app != null && !app.isEmpty()) {
        	p2 = new Paragraph("Application");
            p2.setFontSize(10).setFont(bold);
            doc.add(p2);
            doc.add(new Paragraph(app).setFont(regular).setFontSize(8));
            
        	doc.add(new Paragraph("\n"));
        }
        }
        
        //doc.add(new Paragraph("\n"));
        p2 = new Paragraph("Additional Information");
        p2.setFontSize(10).setFont(bold);
        doc.add(p2);
       
        String patentNo = getFormValue(UL4Common.GENERALINFORELATION, "Patent No.", "u4_description");
        String patentPendingNo = getFormValue(UL4Common.GENERALINFORELATION, "Patent Pending No.", "u4_description");
        String patentNoProp = getFormValue(UL4Common.GENERALINFORELATION, "Patent No.", "u4_property");
        String patentPendingNoProp = getFormValue(UL4Common.GENERALINFORELATION, "Patent Pending No.", "u4_property");
        String CU_barcode = getFormValue(UL4Common.GENERALINFORELATION, "CU Barcode Ref.", "u4_property");
        String CU_barcode_desc = getFormValue(UL4Common.GENERALINFORELATION, "CU Barcode Ref.", "u4_description");
        Table addlInfo = new Table(new float [] {261,262});
        addlInfo.addCell(new Cell().add("Property").setFont(bold).setFontSize(8));
        addlInfo.addCell(new Cell().add("Description").setFont(bold).setFontSize(8));
        boolean isAddInfoEmpty = true;
        if(patentNo.length()!=0)
        {
	        addlInfo.addCell(new Cell().add(patentNoProp).setFont(regular).setFontSize(8));
	        addlInfo.addCell(new Cell().add(patentNo).setFont(regular).setFontSize(8));
	        isAddInfoEmpty = false;
        }
        if(patentPendingNo.length()!=0){
        	 addlInfo.addCell(new Cell().add(patentPendingNoProp).setFont(regular).setFontSize(8));
             addlInfo.addCell(new Cell().add(patentPendingNo).setFont(regular).setFontSize(8));
             isAddInfoEmpty = false;
        } 
        if(CU_barcode!=null && CU_barcode_desc.length()!=0){
       	 addlInfo.addCell(new Cell().add(CU_barcode).setFont(regular).setFontSize(8));
       	 addlInfo.addCell(new Cell().add(CU_barcode_desc).setFont(regular).setFontSize(8));
       	isAddInfoEmpty = false;
       } 
        if (!isAddInfoEmpty) {

            p2 = new Paragraph("Additional Information");
            p2.setFontSize(10).setFont(bold);
            doc.add(p2);
	        doc.add(addlInfo);
	        doc.add(new Paragraph("\n"));
        }
       
        //add the Associated Specifications section to the report
        
        finalSpec.addCell(new Cell().setFont(bold).setFontSize(12).setPadding(0).setBackgroundColor(lavender)
        		.add("Associated Specifications / References")
        		.setBorder(Border.NO_BORDER));
        doc.add(finalSpec); 
        
        AIFComponentContext[] component = null;    	
		try
		{
			//gets all the pams attached as associated specifications
			component = pamRevision.getRelated(ASSOCIATED);	
			 			
		}
		catch (TCException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		doc.add(new Paragraph("\n"));
		//Builds table and values under associated spec section in report
		if(component.length > 0)
		{				
	    	try
	    	{
	    		Table AssocSpecTable = new Table(new float [] {250,50,50,50,123});
	    		AssocSpecTable.addCell(new Cell().add("Name").setFont(bold).setFontSize(8));
	    		AssocSpecTable.addCell(new Cell().add("PAM ID").setFont(bold).setFontSize(8));
	    		AssocSpecTable.addCell(new Cell().add("Interspec ID").setFont(bold).setFontSize(8));
	    		AssocSpecTable.addCell(new Cell().add("Revision ID").setFont(bold).setFontSize(8));
	    		AssocSpecTable.addCell(new Cell().add("Date Released").setFont(bold).setFontSize(8));
	    		
	    		for( int inx=0 ; inx < component.length ; inx++)
				{	
					TCComponentItemRevision specComp = ((TCComponentItemRevision) component[inx].getComponent());
	    			String parentType                =  specComp.getTypeComponent().getParent().toString();
	    			if(parentType.equals(UL4Common.PAMREVISION))
					{
							String name         = specComp.getTCProperty(UL4Common.OBJECT_NAME).getStringValue();
							String pamId        = specComp.getTCProperty(UL4Common.ITEMID).getNonNullDisplayableValue();
							String interspecId  = specComp.getTCProperty(UL4Common.INTERSPEC_ID).getNonNullDisplayableValue();
							String revId        = specComp.getTCProperty(UL4Common.REVID).getNonNullDisplayableValue();
							String dateReleased = specComp.getTCProperty(UL4Common.RELEASED_DATE).getNonNullDisplayableValue();
							
							AssocSpecTable.addCell(new Cell().add(name).setFont(regular).setFontSize(8));
							AssocSpecTable.addCell(new Cell().add(pamId).setFont(regular).setFontSize(8));
							AssocSpecTable.addCell(new Cell().add(interspecId).setFont(regular).setFontSize(8));
							AssocSpecTable.addCell(new Cell().add(revId).setFont(regular).setFontSize(8));
							AssocSpecTable.addCell(new Cell().add(dateReleased).setFont(regular).setFontSize(8));
							
						}
					}
	    		doc.add(AssocSpecTable);
	    		
				}
	    	catch (TCException e){
	    			//
			}   	
		}
		desc = getFormValue("U4_ReferencesRelation", "References_Additions / Exemptions", "u4_text"); 
		if(desc!=null){
    		p2 = new Paragraph("Additions / Exemptions");
	        p2.setFontSize(10).setFont(bold);
	        doc.add(p2);		          
	        doc.add(new Paragraph(desc).setFont(regular).setFontSize(8));
	        doc.add(new Paragraph("\n"));
    		}
		desc = getFormValue("U4_ReferencesRelation", "References_Local Reference", "u4_text"); 
		if(desc!=null){
    		p2 = new Paragraph("Local Reference");
	        p2.setFontSize(10).setFont(bold);
	        doc.add(p2);		          
	        doc.add(new Paragraph(desc).setFont(regular).setFontSize(8));
	        doc.add(new Paragraph("\n"));
    		}
        
        //add the package Details section to the report
		Map<String, List<TCComponent>> tcname_tccomponent = null;
		Vector<Vector<PAMSecondaryPropValue>> PAMPropertyNameValue_v = null;
		
		 if(!getMatlClassificationValue(UL4Common.PAM_FRAME).equals("G-ARTICLE") && 
				 !getMatlClassificationValue(UL4Common.TECHNOLOGY).equals("Actuator/Valve Assembly") &&
				 !getMatlClassificationValue(UL4Common.TECHNOLOGY).equals("Plastic Bottle or Jar - Assembly") &&
				 !getMatlClassificationValue(UL4Common.TECHNOLOGY).equals("Plastic Closure - Assembly") &&
				 !getMatlClassificationValue(UL4Common.TECHNOLOGY).equals("Injection Moulding - Assembly") &&
				 !getMatlClassificationValue(UL4Common.TECHNOLOGY).equals("Non-aerosol Pump - Assembly") &&
				 !getMatlClassificationValue(UL4Common.TECHNOLOGY).equals("Tube - Assembly") &&
				 !getMatlClassificationValue(UL4Common.TECHNOLOGY).equals("Actuator/Valve Assembly") &&
				 !getMatlClassificationValue(UL4Common.TECHNOLOGY).equals("Glass Bottle or Jar - Assembly") &&
				 !getMatlClassificationValue(UL4Common.TECHNOLOGY).equals("Plastic Bucket - Assembly") &&
				 !getMatlClassificationValue(UL4Common.TECHNOLOGY).equals("Cases/Corrugate - Assembly") &&
				 !getMatlClassificationValue(UL4Common.TECHNOLOGY).equals("Flexible Film - Assembly") &&
				 !getMatlClassificationValue(UL4Common.TECHNOLOGY).equals("Plastic Lids and Hoods - Assembly") &&
				 !getMatlClassificationValue(UL4Common.TECHNOLOGY).equals("Thermoforms - Assembly") &&
				 !getMatlClassificationValue(UL4Common.TECHNOLOGY).equals("Non-aerosol Pump - Assembly") &&
				 !getMatlClassificationValue(UL4Common.TECHNOLOGY).equals("Plastic Tub, Cup, Tray - Assembly") &&
				 !getMatlClassificationValue(UL4Common.PAM_FRAME).equals("G-TOOTHBRUSH")
				 && !getMatlClassificationValue(UL4Common.PAM_FRAME).equals("G-ELECTRONIC")
	        		&& !getMatlClassificationValue(UL4Common.PAM_FRAME).equals("G-ELECTROMECHANICAL")
	        		&& !getMatlClassificationValue(UL4Common.PAM_FRAME).equals("G-DEVICE-ASSEMBLY")
	        		&& !getMatlClassificationValue(UL4Common.PAM_FRAME).equals("G-FILTERS")
	        		&& !getMatlClassificationValue(UL4Common.PAM_FRAME).equals("G-MECHANICAL"))
		 {
			packageDetails.addCell(new Cell().setFont(bold).setFontSize(12).setPadding(0).setBackgroundColor(lavender)
	        		.add("Package Details")
	        		.setBorder(Border.NO_BORDER)); 
	        doc.add(new Paragraph("\n"));             
	        doc.add(packageDetails);
	        doc.add(new Paragraph("\n"));
	        tcname_tccomponent = getForms(packageDetailsRelation);        
	        PAMPropertyNameValue_v = getEPAMTableConfig(tcname_tccomponent,packageDetailsRelation);	        
		 }
		 else if (getMatlClassificationValue(UL4Common.PAM_FRAME).equals("G-ARTICLE") || getMatlClassificationValue(UL4Common.PAM_FRAME).equals("G-TOOTHBRUSH")
				 || getMatlClassificationValue(UL4Common.PAM_FRAME).equals("G-ELECTRONIC")
				 || getMatlClassificationValue(UL4Common.PAM_FRAME).equals("G-ELECTROMECHANICAL")
				 || getMatlClassificationValue(UL4Common.PAM_FRAME).equals("G-DEVICE-ASSEMBLY")
				 || getMatlClassificationValue(UL4Common.PAM_FRAME).equals("G-FILTERS")
				 || getMatlClassificationValue(UL4Common.PAM_FRAME).equals("G-MECHANICAL"))
		 {
	        packageDetails.addCell(new Cell().setFont(bold).setFontSize(12).setPadding(0).setBackgroundColor(lavender)
	        		.add("Article Details")
	        		.setBorder(Border.NO_BORDER)); 
	        doc.add(new Paragraph("\n"));             
	        doc.add(packageDetails);
	        doc.add(new Paragraph("\n"));
	        tcname_tccomponent = getForms(articleDetailsRelation);        
	        PAMPropertyNameValue_v = getEPAMTableConfig(tcname_tccomponent,articleDetailsRelation);	       
		 }
		 else if(getMatlClassificationValue(UL4Common.TECHNOLOGY).equals("Actuator/Valve Assembly") ||
				 getMatlClassificationValue(UL4Common.TECHNOLOGY).equals("Plastic Bottle or Jar - Assembly") ||
				 getMatlClassificationValue(UL4Common.TECHNOLOGY).equals("Plastic Closure - Assembly") ||
				 getMatlClassificationValue(UL4Common.TECHNOLOGY).equals("Injection Moulding - Assembly") ||
				 getMatlClassificationValue(UL4Common.TECHNOLOGY).equals("Non-aerosol Pump - Assembly") ||
				 getMatlClassificationValue(UL4Common.TECHNOLOGY).equals("Tube - Assembly") ||
				 getMatlClassificationValue(UL4Common.TECHNOLOGY).equals("Actuator/Valve Assembly") ||
				 getMatlClassificationValue(UL4Common.TECHNOLOGY).equals("Glass Bottle or Jar - Assembly") ||
				 getMatlClassificationValue(UL4Common.TECHNOLOGY).equals("Plastic Bucket - Assembly") ||
				 getMatlClassificationValue(UL4Common.TECHNOLOGY).equals("Cases/Corrugate - Assembly") ||
				 getMatlClassificationValue(UL4Common.TECHNOLOGY).equals("Flexible Film - Assembly") ||
				 getMatlClassificationValue(UL4Common.TECHNOLOGY).equals("Plastic Lids and Hoods - Assembly") ||
				 getMatlClassificationValue(UL4Common.TECHNOLOGY).equals("Thermoforms - Assembly") ||
				 getMatlClassificationValue(UL4Common.TECHNOLOGY).equals("Non-aerosol Pump - Assembly") ||
				 getMatlClassificationValue(UL4Common.TECHNOLOGY).equals("Plastic Tub, Cup, Tray - Assembly"))
		 {
			 packageDetails.addCell(new Cell().setFont(bold).setFontSize(12).setPadding(0).setBackgroundColor(lavender)
		        		.add("Package Details")
		        		.setBorder(Border.NO_BORDER)); 
		        doc.add(new Paragraph("\n"));             
		        doc.add(packageDetails);
		        doc.add(new Paragraph("\n"));
		        tcname_tccomponent = getForms(packageDetailsRelation);        
		        PAMPropertyNameValue_v = getEPAMComponentTableConfig(tcname_tccomponent,packageDetailsRelation);
		 }
        
        for(int inx=0;inx<PAMPropertyNameValue_v.size();inx++)
		{
        	PAMSecondaryPropValue propvalue = (PAMPropertyNameValue_v.get(inx)).get(0);
        	String tableType = propvalue.selectedComponent.getDisplayType();
        	//doc.add(new Paragraph().add(tableType).setFontSize(10).setFont(bold));
        	float columnWidths [] = new float[propvalue.propNameValuepair.length];
        	for(  int ColumnNumber =0;ColumnNumber< propvalue.propNameValuepair.length;ColumnNumber++ )
    		{
        		columnWidths[ColumnNumber] = propvalue.propNameValuepair[ColumnNumber].columnSize;        	
    		}
        	Table packageDetailsInfo = new Table(columnWidths);
        	for(  int ColumnNumber =0;ColumnNumber< propvalue.propNameValuepair.length;ColumnNumber++ )
    		{
        		packageDetailsInfo.addHeaderCell(new Cell().add( propvalue.propNameValuepair[ColumnNumber].propDisplayName).setFont(bold).setFontSize(8));        		       	
    		}
        	for(  int j =0;j< (PAMPropertyNameValue_v.get(inx)).size();j++ )
    		{
        		PAMSecondaryPropValue propvalue1 = (PAMPropertyNameValue_v.get(inx)).get(j);
    			boolean rowEmpty = isRowEmpty(propvalue1, articleDetailsRelation,tableType);
        		for(  int ColumnNumber =0;ColumnNumber< propvalue1.propNameValuepair.length;ColumnNumber++ )
        		{       			
        			if(!rowEmpty)        			
        				packageDetailsInfo.addCell(new Cell().add( propvalue1.propNameValuepair[ColumnNumber].propValue).setFont(regular).setFontSize(8));
        			/*else
        				packageDetailsInfo.addCell(new Cell().add(" ").setFont(regular).setFontSize(8));*/
        		}       		       	
    		}
        	if(packageDetailsInfo.getNumberOfRows() > 0  && packageDetailsInfo.isComplete() && ( ! packageDetailsInfo.isEmpty()))
        	{
        		doc.add(new Paragraph().add(tableType).setFontSize(10).setFont(bold));
        		doc.add(packageDetailsInfo);        		
        	}
        	doc.add(new Paragraph("\n"));			
		}
        if(!getMatlClassificationValue(UL4Common.PAM_FRAME).equals("G-ARTICLE") && !getMatlClassificationValue(UL4Common.PAM_FRAME).equals("G-TOOTHBRUSH")
        		&& !getMatlClassificationValue(UL4Common.PAM_FRAME).equals("G-ELECTRONIC")
        		&& !getMatlClassificationValue(UL4Common.PAM_FRAME).equals("G-ELECTROMECHANICAL")
        		&& !getMatlClassificationValue(UL4Common.PAM_FRAME).equals("G-DEVICE-ASSEMBLY")
        		&& !getMatlClassificationValue(UL4Common.PAM_FRAME).equals("G-FILTERS")
        		&& !getMatlClassificationValue(UL4Common.PAM_FRAME).equals("G-MECHANICAL")
        		)
		{	                
	        p2 = new Paragraph("Package Details Information");
	        p2.setFontSize(10).setFont(bold);
	        doc.add(p2);
	        desc = getFormValue(packageDetailsRelation, "Package Details_Package Details Information", "u4_text");   
	        doc.add(new Paragraph(desc).setFont(regular).setFontSize(8));
		}
		else if (getMatlClassificationValue(UL4Common.PAM_FRAME).equals("G-ARTICLE") || getMatlClassificationValue(UL4Common.PAM_FRAME).equals("G-TOOTHBRUSH"))
		{                
	        p2 = new Paragraph("Additional Information");
	        p2.setFontSize(10).setFont(bold);
	        doc.add(p2);
	        desc = getFormValue(articleDetailsRelation, "Article Details_Additional Information", "u4_text");   
	        doc.add(new Paragraph(desc).setFont(regular).setFontSize(8));
		}
        
                
        // add technical drawings to report        
        if(imgRequired && datasetType!=null)
        {
        	Table datasetNames = new Table(new float [] {175,174,175});        	

        	DatasetInfo dsinfo = datasetType.get(0);

        	TechDrw.addCell(new Cell().setFont(bold).setFontSize(12).setPadding(0).setBackgroundColor(lavender)
        			.add("Technical Drawings")
        			.setBorder(Border.NO_BORDER)); 
        	doc.add(TechDrw);
        	doc.add(new Paragraph("\n"));	
        	datasetNames.addCell(new Cell().add("Drawing No.").setFont(bold).setFontSize(8));
        	datasetNames.addCell(new Cell().add("Type").setFont(bold).setFontSize(8));        
        	datasetNames.addCell(new Cell().add("Description").setFont(bold).setFontSize(8));

        	for(int i= 0;i<datasetType.size();i++)
        	{        		
    			datasetNames.addCell(new Cell().add(datasetType.get(i).getDatsetId()).setFont(regular).setFontSize(8));
    			datasetNames.addCell(new Cell().add(datasetType.get(i).getDatasetType()).setFont(regular).setFontSize(8));
    			datasetNames.addCell(new Cell().add(datasetType.get(i).getDatasetDesc()).setFont(regular).setFontSize(8));
    		}
        	doc.add(datasetNames);
        	doc.add(new Paragraph( "\n"));
        	for(DatasetInfo dsinfo1: datasetType)
        	{      		
				if(dsinfo1.getDatasetfilepath()!=null)
				{    	        	
					PdfDocument assocSpecDataset = new PdfDocument(new PdfReader(dsinfo1.getDatasetfilepath()));  
	
					for(int inx = 1;inx<=assocSpecDataset.getNumberOfPages();inx++)
					{	
						PdfPage origPage = assocSpecDataset.getPage(inx);	        
						Rectangle rect = origPage.getPageSizeWithRotation();	     
						PdfNumber rotate = origPage.getPdfObject().getAsNumber(PdfName.Rotate);	  		        
	
						PdfReader reader = new PdfReader(dsinfo1.getDatasetfilepath());
						reader.setUnethicalReading(true);
						String tempDir = System.getProperty("java.io.tmpdir");
						tempDir = tempDir+"tempPdf.pdf";
						PdfDocument tempPdfDoc = new PdfDocument(reader, new PdfWriter(tempDir));	    		        
	
						PdfAcroForm form = PdfAcroForm.getAcroForm(tempPdfDoc, true);
						if(form.getFormFields()!=null)
						{	        	
							if(rotate!=null)
								if(rotate.getValue()!=0)
								{
									PdfPage page = tempPdfDoc.getPage(inx);	
									page.setRotation((rotate.intValue() + 90) % 360);		        		
								}
							form.flattenFields();
							tempPdfDoc.close();		        	
						}    
	
						tempPdfDoc = new PdfDocument(new PdfReader(tempDir));
						origPage = tempPdfDoc.getPage(inx);	
	
						PdfFormXObject pageCopy = origPage.copyAsFormXObject(pdfDoc);		        
						Image image = new Image(pageCopy);
						image.setBorder(Border.NO_BORDER);
	
						if(rect.getHeight()<=rect.getWidth()){
							image.setWidthPercent(88);
						}
						else
						{	    
							image.setWidthPercent(78);
							//image.setHeight(rect.getHeight()-headerHt);
						}		       
						doc.add(image);
						tempPdfDoc.close();
					}
	
					assocSpecDataset.close();  	        
				}   	
        	}
        }
        
        //add Materials section
        materials.addCell(new Cell().setFont(bold).setFontSize(12).setPadding(0).setBackgroundColor(lavender)
        		.add("Materials")
        		.setBorder(Border.NO_BORDER)); 
        doc.add(new Paragraph("\n"));             
        doc.add(materials);
        doc.add(new Paragraph("\n"));
        
       // Vector<Vector<PAMSecondaryPropValue>> PAMPropertyNameValue_v1 = null;
        tcname_tccomponent = getForms(materialsRelation);        
        //PAMPropertyNameValue_v = getEPAMTableConfig(tcname_tccomponent,materialsRelation);
        PAMPropertyNameValue_v = getEPAMComponentTableConfig(tcname_tccomponent,materialsRelation);
        //PAMPropertyNameValue_v.addAll(PAMPropertyNameValue_v1);
        for(int inx=0;inx<PAMPropertyNameValue_v.size();inx++)
		{
        	PAMSecondaryPropValue propvalue = (PAMPropertyNameValue_v.get(inx)).get(0);
        	String tableType = propvalue.selectedComponent.getDisplayType();
        	//doc.add(new Paragraph().add(tableType).setFontSize(10).setFont(bold));
        	float columnWidths [] = new float[propvalue.propNameValuepair.length];
        	for(  int ColumnNumber =0;ColumnNumber< propvalue.propNameValuepair.length;ColumnNumber++ )
    		{
        		columnWidths[ColumnNumber] = propvalue.propNameValuepair[ColumnNumber].columnSize;        	
    		}
        	Table packageDetailsInfo = new Table(columnWidths);
        	for(  int ColumnNumber =0;ColumnNumber< propvalue.propNameValuepair.length;ColumnNumber++ )
    		{
        		packageDetailsInfo.addHeaderCell(new Cell().add( propvalue.propNameValuepair[ColumnNumber].propDisplayName).setFont(bold).setFontSize(8));        		       	
    		}
        	for(  int j =0;j< (PAMPropertyNameValue_v.get(inx)).size();j++ )
    		{
        		PAMSecondaryPropValue propvalue1 = (PAMPropertyNameValue_v.get(inx)).get(j);
    			boolean rowEmpty = isRowEmpty(propvalue1, materialsRelation,tableType);
        		for(  int ColumnNumber =0;ColumnNumber< propvalue1.propNameValuepair.length;ColumnNumber++ )
        		{       			
        			if(!rowEmpty)        			
        				packageDetailsInfo.addCell(new Cell().add( propvalue1.propNameValuepair[ColumnNumber].propValue).setFont(regular).setFontSize(8));
        			/*else
        				packageDetailsInfo.addCell(new Cell().add(" ").setFont(regular).setFontSize(8));*/
        		}        		       	
    		}
        	if(packageDetailsInfo.getNumberOfRows() > 0  && packageDetailsInfo.isComplete() && ( ! packageDetailsInfo.isEmpty()))
        	{
        		doc.add(new Paragraph().add(tableType).setFontSize(10).setFont(bold));
        		doc.add(packageDetailsInfo);        		
        	}
        	doc.add(new Paragraph("\n"));        	
		}
        
        desc = getFormValue(materialsRelation, "Materials_Local Information", "u4_text");  
        if(desc!=null)
        {
        	p2 = new Paragraph("Local Information");
            p2.setFontSize(10).setFont(bold);
            doc.add(p2);       
            
            doc.add(new Paragraph(desc).setFont(regular).setFontSize(8));
            
        	doc.add(new Paragraph("\n"));
        }  
        desc = getFormValue(materialsRelation, "Materials_Local Reference", "u4_text");  
        if(desc!=null)
        {
        	p2 = new Paragraph("Local Reference");
            p2.setFontSize(10).setFont(bold);
            doc.add(p2);       
            
            doc.add(new Paragraph(desc).setFont(regular).setFontSize(8));
            
        	doc.add(new Paragraph("\n"));
        }  
       
        desc = getFormValue(materialsRelation, "Materials_Approved Polymer Information", "u4_text"); 
        if(desc!=null)
        {
        	p2 = new Paragraph("Approved Polymer Information");
            p2.setFontSize(10).setFont(bold);
            doc.add(p2);        
              
            doc.add(new Paragraph(desc).setFont(regular).setFontSize(8));
            
            doc.add(new Paragraph("\n"));
        }
        
        desc = getFormValue(materialsRelation, "Materials_Materials Information", "u4_text");    	
        if(desc!=null)
        {
        	p2 = new Paragraph("Materials Information");
            p2.setFontSize(10).setFont(bold);
            doc.add(p2);
                       
            doc.add(new Paragraph(desc).setFont(regular).setFontSize(8));
            
            doc.add(new Paragraph("\n"));
        }
        desc = getFormValue(materialsRelation, "Materials_Approved Masterbatch [MB] Information", "u4_text");  
    	
        if(desc!=null)
        {
        	p2 = new Paragraph("Approved Masterbatch [MB] Information");
            p2.setFontSize(10).setFont(bold);
            doc.add(p2);
                        
            doc.add(new Paragraph(desc).setFont(regular).setFontSize(8));
            
            doc.add(new Paragraph("\n"));
        }
        desc = getFormValue(materialsRelation, "Materials_Materials Note", "u4_text");  
    	if(desc!=null)
        {
        	p2 = new Paragraph("Materials Note");
            p2.setFontSize(10).setFont(bold);
            doc.add(p2);
                        
            doc.add(new Paragraph(desc).setFont(regular).setFontSize(8));
            
            doc.add(new Paragraph("\n"));
        }
    	 desc = getFormValue(materialsRelation, "Materials_Body Materials Information", "u4_text");  
         if(desc!=null)
         {
         	p2 = new Paragraph("Body Materials Information");
             p2.setFontSize(10).setFont(bold);
             doc.add(p2);       
             
             doc.add(new Paragraph(desc).setFont(regular).setFontSize(8));
             
         	doc.add(new Paragraph("\n"));
         }
         desc = getFormValue(materialsRelation, "Materials_Top Materials Information", "u4_text");  
         if(desc!=null)
         {
         	p2 = new Paragraph("Top Materials Information");
             p2.setFontSize(10).setFont(bold);
             doc.add(p2);       
             
             doc.add(new Paragraph(desc).setFont(regular).setFontSize(8));
             
         	doc.add(new Paragraph("\n"));
         }
         desc = getFormValue(materialsRelation, "Materials_Bottom Materials Information", "u4_text");  
         if(desc!=null)
         {
         	p2 = new Paragraph("Bottom Materials Information");
             p2.setFontSize(10).setFont(bold);
             doc.add(p2);       
             
             doc.add(new Paragraph(desc).setFont(regular).setFontSize(8));
             
         	doc.add(new Paragraph("\n"));
         }
         //for g-toothbrush
         desc = getFormValue(materialsRelation, "Materials_Approved Bristle Information", "u4_text");  
         if(desc!=null)
         {
         	p2 = new Paragraph("Approved Bristle Information");
             p2.setFontSize(10).setFont(bold);
             doc.add(p2);       
             
             doc.add(new Paragraph(desc).setFont(regular).setFontSize(8));
             
         	doc.add(new Paragraph("\n"));
         }
         desc = getFormValue(materialsRelation, "Materials_Approved Hot Foil Information", "u4_text");  
         if(desc!=null)
         {
         	p2 = new Paragraph("Approved Hot Foil Information");
             p2.setFontSize(10).setFont(bold);
             doc.add(p2);       
             
             doc.add(new Paragraph(desc).setFont(regular).setFontSize(8));
             
         	doc.add(new Paragraph("\n"));
         }
         desc = getFormValue(materialsRelation, "Materials_Approved Anchor Wire Information", "u4_text");  
         if(desc!=null)
         {
         	p2 = new Paragraph("Approved Anchor Wire Information");
             p2.setFontSize(10).setFont(bold);
             doc.add(p2);       
             
             doc.add(new Paragraph(desc).setFont(regular).setFontSize(8));
             
         	doc.add(new Paragraph("\n"));
         }
         desc = getFormValue(materialsRelation, "Materials_Can Maker's End Material Information", "u4_text");  
         if(desc!=null)
         {
         	p2 = new Paragraph("Can Maker's End Material Information");
             p2.setFontSize(10).setFont(bold);
             doc.add(p2);       
             
             doc.add(new Paragraph(desc).setFont(regular).setFontSize(8));
             
         	doc.add(new Paragraph("\n"));
         }
        
      //add Receiving Site Usage Conditions section
        rcvUsageCond.addCell(new Cell().setFont(bold).setFontSize(12).setPadding(0).setBackgroundColor(lavender)
        		.add("Receiving Site Usage Conditions")
        		.setBorder(Border.NO_BORDER)); 
        doc.add(new Paragraph("\n"));             
        doc.add(rcvUsageCond);
        doc.add(new Paragraph("\n"));
        
        tcname_tccomponent = getForms(rcvUsageCondRelation);        
        PAMPropertyNameValue_v = getEPAMTableConfig(tcname_tccomponent,rcvUsageCondRelation);
        
        boolean noRows = true;
        for(int inx=0;inx<PAMPropertyNameValue_v.size();inx++)
		{
        	PAMSecondaryPropValue propvalue = (PAMPropertyNameValue_v.get(inx)).get(0);
        	String tableType = propvalue.selectedComponent.getDisplayType();
        	// commented as addition of table main header is done after checking whether there are rows
        	//doc.add(new Paragraph().add(tableType).setFontSize(10).setFont(bold));
        	float columnWidths [] = new float[propvalue.propNameValuepair.length];
        	noRows = true;
        	for(  int ColumnNumber =0;ColumnNumber< propvalue.propNameValuepair.length;ColumnNumber++ )
    		{
        		columnWidths[ColumnNumber] = propvalue.propNameValuepair[ColumnNumber].columnSize;        	
    		}
        	for(  int j =0;j< (PAMPropertyNameValue_v.get(inx)).size();j++ )
    		{
        		PAMSecondaryPropValue propvalue1 = (PAMPropertyNameValue_v.get(inx)).get(j);
    			if (!isRowEmpty(propvalue1, rcvUsageCondRelation,tableType)) {
    				noRows = false;
    				break;
    			}
    		}
        	if (!noRows) {
        		doc.add(new Paragraph().add(tableType).setFontSize(10).setFont(bold));
        	}
        	Table packageDetailsInfo = new Table(columnWidths);
        	if (!noRows) {
	        	for(  int ColumnNumber =0;ColumnNumber< propvalue.propNameValuepair.length;ColumnNumber++ )
	    		{
	        		packageDetailsInfo.addHeaderCell(new Cell().add( propvalue.propNameValuepair[ColumnNumber].propDisplayName).setFont(bold).setFontSize(8));        		       	
	    		}
	        	for(  int j =0;j< (PAMPropertyNameValue_v.get(inx)).size();j++ )
	    		{
	        		PAMSecondaryPropValue propvalue1 = (PAMPropertyNameValue_v.get(inx)).get(j);
	    			boolean rowEmpty = isRowEmpty(propvalue1, rcvUsageCondRelation,tableType);
	        		for(  int ColumnNumber =0;ColumnNumber< propvalue1.propNameValuepair.length;ColumnNumber++ )
	        		{       			
	        			if(!rowEmpty)        			
	        				packageDetailsInfo.addCell(new Cell().add( propvalue1.propNameValuepair[ColumnNumber].propValue).setFont(regular).setFontSize(8));
	        			/*else
	        				packageDetailsInfo.addCell(new Cell().add(" ").setFont(regular).setFontSize(8));*/
	        		}        		       	
	    		}
	        	doc.add(packageDetailsInfo);
	        	doc.add(new Paragraph("\n"));
        	}
		}
        
        // commented as this addition should be done after checking whether value is empty
        //p2 = new Paragraph("Usage Conditions Information");
        //p2.setFontSize(10).setFont(bold);
        //doc.add(p2);
        
        desc = getFormValue(rcvUsageCondRelation, "Receiving Site Usage Conditions_Usage Conditions Information", "u4_text");
        // added below check to prevent null value
        if (desc == null) desc = "";
        if (!desc.isEmpty()) {
	        p2 = new Paragraph("Usage Conditions Information");
	        p2.setFontSize(10).setFont(bold);
	        doc.add(p2);
	        doc.add(new Paragraph(desc).setFont(regular).setFontSize(8));
	        doc.add(new Paragraph("\n"));
        }
        
        // add protection and software sections if applicable
        addProtectionAndSoftware(doc, bold, regular);
       
        
        //add Environmental Section
        env.addCell(new Cell().setFont(bold).setFontSize(12).setPadding(0).setBackgroundColor(lavender)
        		.add("Environmental")
        		.setBorder(Border.NO_BORDER)); 
        doc.add(new Paragraph("\n"));             
        doc.add(env);
        doc.add(new Paragraph("\n"));
        
        tcname_tccomponent = getForms(envRelation);        
        PAMPropertyNameValue_v = getEPAMTableConfig(tcname_tccomponent,envRelation);
        
        for(int inx=0;inx<PAMPropertyNameValue_v.size();inx++)
		{
        	PAMSecondaryPropValue propvalue = (PAMPropertyNameValue_v.get(inx)).get(0);
        	String tableType = propvalue.selectedComponent.getDisplayType();
        	//doc.add(new Paragraph().add(tableType).setFontSize(10).setFont(bold));
        	float columnWidths [] = new float[propvalue.propNameValuepair.length];
        	for(  int ColumnNumber =0;ColumnNumber< propvalue.propNameValuepair.length;ColumnNumber++ )
    		{
        		columnWidths[ColumnNumber] = propvalue.propNameValuepair[ColumnNumber].columnSize;        	
    		}
        	Table packageDetailsInfo = new Table(columnWidths);
        	for(  int ColumnNumber =0;ColumnNumber< propvalue.propNameValuepair.length;ColumnNumber++ )
    		{
        		packageDetailsInfo.addHeaderCell(new Cell().add( propvalue.propNameValuepair[ColumnNumber].propDisplayName).setFont(bold).setFontSize(8));        		       	
    		}
        	for(  int j =0;j< (PAMPropertyNameValue_v.get(inx)).size();j++ )
    		{
        		PAMSecondaryPropValue propvalue1 = (PAMPropertyNameValue_v.get(inx)).get(j);
    			boolean rowEmpty = isRowEmpty(propvalue1, envRelation,tableType);
        		for(  int ColumnNumber =0;ColumnNumber< propvalue1.propNameValuepair.length;ColumnNumber++ )
        		{       			
        			if(!rowEmpty)        			
        				packageDetailsInfo.addCell(new Cell().add( propvalue1.propNameValuepair[ColumnNumber].propValue).setFont(regular).setFontSize(8));
        			/*else
        				packageDetailsInfo.addCell(new Cell().add(" ").setFont(regular).setFontSize(8));*/
        		}        		       	
    		}
        	if(packageDetailsInfo.getNumberOfRows() > 0  && packageDetailsInfo.isComplete() && ( ! packageDetailsInfo.isEmpty()))
        	{
        		doc.add(new Paragraph().add(tableType).setFontSize(10).setFont(bold));
        		doc.add(packageDetailsInfo);        		
        	}
        	doc.add(new Paragraph("\n"));
		}
        
        // Environmental Material Details is not applicable for below frame types
        if( !getMatlClassificationValue(UL4Common.PAM_FRAME).equals("G-ELECTRONIC")
    		&& !getMatlClassificationValue(UL4Common.PAM_FRAME).equals("G-ELECTROMECHANICAL")
    		&& !getMatlClassificationValue(UL4Common.PAM_FRAME).equals("G-DEVICE-ASSEMBLY")
    		&& !getMatlClassificationValue(UL4Common.PAM_FRAME).equals("G-FILTERS")
    		&& !getMatlClassificationValue(UL4Common.PAM_FRAME).equals("G-MECHANICAL")
        		)
        {
    	 
	        desc = getFormValue(envRelation, "Environmental_Environmental Material Details", "u4_text");   
	        if (desc != null && !desc.isEmpty()) {
			    p2 = new Paragraph("Environmental Material Details");
			    p2.setFontSize(10).setFont(bold);
			    doc.add(p2);
			    doc.add(new Paragraph(desc).setFont(regular).setFontSize(8));
			    doc.add(new Paragraph("\n"));
		    }
        }
        
        
      //add Component Properties Section
        compProp.addCell(new Cell().setFont(bold).setFontSize(12).setPadding(0).setBackgroundColor(lavender)
        		.add("Component Properties")
        		.setBorder(Border.NO_BORDER)); 
        doc.add(new Paragraph("\n"));             
        doc.add(compProp);
        doc.add(new Paragraph("\n"));
        
        tcname_tccomponent = getForms(compPropRelation);        
        PAMPropertyNameValue_v = getEPAMComponentTableConfig(tcname_tccomponent,compPropRelation);
        
        for(int inx=0;inx<PAMPropertyNameValue_v.size();inx++)
		{
        	noRows = true;
        	PAMSecondaryPropValue propvalue = (PAMPropertyNameValue_v.get(inx)).get(0);
        	String tableType = propvalue.selectedComponent.getDisplayType();
        	//doc.add(new Paragraph().add(tableType).setFontSize(10).setFont(bold));
        	float columnWidths [] = new float[propvalue.propNameValuepair.length];
        	for(  int ColumnNumber =0;ColumnNumber< propvalue.propNameValuepair.length;ColumnNumber++ )
    		{
        		columnWidths[ColumnNumber] = propvalue.propNameValuepair[ColumnNumber].columnSize;        	
    		}
        	Table packageDetailsInfo = new Table(columnWidths);
        	for(  int ColumnNumber =0;ColumnNumber< propvalue.propNameValuepair.length;ColumnNumber++ )
    		{
        		packageDetailsInfo.addHeaderCell(new Cell().add( propvalue.propNameValuepair[ColumnNumber].propDisplayName).setFont(bold).setFontSize(8));        		       	
    		}
        	for(  int j =0;j< (PAMPropertyNameValue_v.get(inx)).size();j++ )
    		{
        		PAMSecondaryPropValue propvalue1 = (PAMPropertyNameValue_v.get(inx)).get(j);
    			boolean rowEmpty = isRowEmpty(propvalue1, compPropRelation,tableType);
        		for(  int ColumnNumber =0;ColumnNumber< propvalue1.propNameValuepair.length;ColumnNumber++ )
        		{       			
        			if(!rowEmpty)
        			{
        				// Added below if check as part of CR467
        				if(propvalue1.propNameValuepair[ColumnNumber].propDisplayName.equals("CoA") || propvalue1.propNameValuepair[ColumnNumber].propDisplayName.equals("Cr"))
        				{
        					if(propvalue1.propNameValuepair[ColumnNumber].propValue.equals("True"))        					
        						packageDetailsInfo.addCell(new Cell().add(trueChars).setFont(regular).setFontSize(8));
        					else
        						packageDetailsInfo.addCell(new Cell().add(falseChars).setFont(regular).setFontSize(8));
        				}
        				else
        					packageDetailsInfo.addCell(new Cell().add( propvalue1.propNameValuepair[ColumnNumber].propValue).setFont(regular).setFontSize(8));
        			/*else
        				packageDetailsInfo.addCell(new Cell().add(" ").setFont(regular).setFontSize(8));*/
        				noRows = false;
        			}
        		}        		       	
    		}
        	if (!noRows) {
	        	doc.add(new Paragraph().add(tableType).setFontSize(10).setFont(bold));
	        	doc.add(packageDetailsInfo);
	        	doc.add(new Paragraph("\n"));
        	}
		}
        desc = getFormValue(compPropRelation, "Component Properties_Properties Information", "u4_text");
        String desc2 = getFormValue(compPropRelation, "Component Properties_Property Name Notes", "u4_text");
        boolean isPropInfoEmpty = true;
        if (desc != null && desc.isEmpty())
        	isPropInfoEmpty = false;
        if (desc2 != null && desc2.isEmpty())
        	isPropInfoEmpty = false;
        if (!isPropInfoEmpty)
        {
        p2 = new Paragraph("Properties Information");
        p2.setFontSize(10).setFont(bold);
        doc.add(p2);
        
        
        desc = getFormValue(compPropRelation, "Component Properties_Properties Information", "u4_text");        
        if(desc!=null && !desc.isEmpty()){
	        p2 = new Paragraph("Properties Information");
	        p2.setFontSize(10).setFont(bold);
	        doc.add(p2);
	     
	        doc.add(new Paragraph(desc).setFont(regular).setFontSize(8));
        }
        
        desc = getFormValue(compPropRelation, "Component Properties_Property Name Notes", "u4_text");
	        if(desc2!=null && !desc2.isEmpty()){
	        p2 = new Paragraph("Property Name Notes");
	        p2.setFontSize(10).setFont(bold);
	        doc.add(p2);    
	           
	        doc.add(new Paragraph(desc).setFont(regular).setFontSize(8));
        }
        }
        
        //add integrated Labels section for closure plastic frame
        if((getMatlClassificationValue(UL4Common.PAM_FRAME).equals("G-CLOSURE-PLAS")||
        		getMatlClassificationValue(UL4Common.PAM_FRAME).equals("G-INJECTION") || 
        		getMatlClassificationValue(UL4Common.PAM_FRAME).equals("G-BOTTLE-PLASTIC")|| 
        		getMatlClassificationValue(UL4Common.PAM_FRAME).equals("G-TUB-ETC-PLASTIC") ||
        		getMatlClassificationValue(UL4Common.PAM_FRAME).equals("G-THERMOFORMS") ||
        		getMatlClassificationValue(UL4Common.PAM_FRAME).equals("G-LID-HOOD-PLSTC") ||
        		getMatlClassificationValue(UL4Common.PAM_FRAME).equals("G-AEROSOLS")))
        {
        	intgLabel.addCell(new Cell().setFont(bold).setFontSize(12).setPadding(0).setBackgroundColor(lavender)
            		.add("Integrated Label")
            		.setBorder(Border.NO_BORDER)); 
            doc.add(new Paragraph("\n"));             
            doc.add(intgLabel);
            doc.add(new Paragraph("\n"));
            
            tcname_tccomponent = getForms(integratedLabelRelation);        
            PAMPropertyNameValue_v = getEPAMTableConfig(tcname_tccomponent,integratedLabelRelation);
            
            for(int inx=0;inx<PAMPropertyNameValue_v.size();inx++)
    		{
            	PAMSecondaryPropValue propvalue = (PAMPropertyNameValue_v.get(inx)).get(0);
            	String tableType = propvalue.selectedComponent.getDisplayType();
            	//doc.add(new Paragraph().add(tableType).setFontSize(10).setFont(bold));
            	float columnWidths [] = new float[propvalue.propNameValuepair.length];
            	for(  int ColumnNumber =0;ColumnNumber< propvalue.propNameValuepair.length;ColumnNumber++ )
        		{
            		columnWidths[ColumnNumber] = propvalue.propNameValuepair[ColumnNumber].columnSize;        	
        		}
            	Table packageDetailsInfo = new Table(columnWidths);
            	for(  int ColumnNumber =0;ColumnNumber< propvalue.propNameValuepair.length;ColumnNumber++ )
        		{
            		packageDetailsInfo.addHeaderCell(new Cell().add( propvalue.propNameValuepair[ColumnNumber].propDisplayName).setFont(bold).setFontSize(8));        		       	
        		}
            	for(  int j =0;j< (PAMPropertyNameValue_v.get(inx)).size();j++ )
        		{
            		PAMSecondaryPropValue propvalue1 = (PAMPropertyNameValue_v.get(inx)).get(j);
        			boolean rowEmpty = isRowEmpty(propvalue1, integratedLabelRelation,tableType);
            		for(  int ColumnNumber =0;ColumnNumber< propvalue1.propNameValuepair.length;ColumnNumber++ )
            		{       			
            			if(!rowEmpty)        			
            				packageDetailsInfo.addCell(new Cell().add( propvalue1.propNameValuepair[ColumnNumber].propValue).setFont(regular).setFontSize(8));
            			/*else
            				packageDetailsInfo.addCell(new Cell().add(" ").setFont(regular).setFontSize(8));*/
            		}        		       	
        		}
            	if(packageDetailsInfo.getNumberOfRows() > 0  && packageDetailsInfo.isComplete() && ( ! packageDetailsInfo.isEmpty()))
            	{
            		doc.add(new Paragraph().add(tableType).setFontSize(10).setFont(bold));
            		doc.add(packageDetailsInfo);        		
            	}
            	doc.add(new Paragraph("\n"));        	
    		}
            desc = getFormValue(integratedLabelRelation, "Integrated Label_Description", "u4_text");
            if(desc!=null && !desc.isEmpty())
            {
            	// to add subsection
            	p2 = new Paragraph("Additional Information");
    	        p2.setFontSize(10).setFont(bold);
    	        doc.add(p2);
                
            	p2 = new Paragraph("Description");
                p2.setFontSize(10).setFont(bold);
                doc.add(p2);                
                 
                doc.add(new Paragraph(desc).setFont(regular).setFontSize(8));
            }
            desc = getFormValue(integratedLabelRelation, "Integrated Label_Additional Information", "u4_text");  
            if(desc!=null && !desc.isEmpty())
            {
            p2 = new Paragraph("Additional Information");
            p2.setFontSize(10).setFont(bold);
            doc.add(p2);            
             
            doc.add(new Paragraph(desc).setFont(regular).setFontSize(8));
            }
            doc.add(new Paragraph("\n"));     
        }
      //add Remarks Section
        remarks.addCell(new Cell().setFont(bold).setFontSize(12).setPadding(0).setBackgroundColor(lavender)
        		.add("Remarks")
        		.setBorder(Border.NO_BORDER)); 
        doc.add(new Paragraph("\n"));             
        doc.add(remarks);
        doc.add(new Paragraph("\n"));
         
        p2 = new Paragraph("Remarks");
        p2.setFontSize(10).setFont(bold);
        doc.add(p2);
        
        desc = getFormValue("U4_RemarksRelation", "Remarks_Remarks", "u4_text");
     // added below check to prevent null value
        if (desc == null) desc = "";
        doc.add(new Paragraph(desc).setFont(regular).setFontSize(8));
               
        doc.close();
        
        String name = pamRevision.getStringProperty(UL4Common.OBJECT_NAME).replace(" ","_");
        name = formatFileName(name);
        dsName = pamRevision.getStringProperty(UL4Common.ITEMID) + "_" + name + "_" +pamRevision.getStringProperty(UL4Common.REVID) + "_" +  getCurrentTimeStamp()  ;
        
        String newpdfFileNeme = System.getProperty("java.io.tmpdir") + dsName + "." + "pdf" ;
                        
        pdfDoc = new PdfDocument(new PdfReader(dest),new PdfWriter(newpdfFileNeme));
        doc = new Document (pdfDoc);
        Rectangle pageSize;        
        int n = pdfDoc.getNumberOfPages();
        for (int i = 1; i <= n; i++) {
        	PdfPage page = pdfDoc.getPage(i);
            pageSize = page.getPageSize();
            doc.showTextAligned(new Paragraph(String.format("page %s of %s", i, n)), pageSize.getWidth()-72,50, i, TextAlignment.RIGHT, VerticalAlignment.BOTTOM, 0);
        }
        pdfDoc.close();
        
        session.setStatus("Uploading PAM Specification PDF Report as Dataset.......");
        System.out.println("Uploading PAM Specification PDF Report as Dataset.......");
               
        uploadDataset(dsName, newpdfFileNeme);
        
        //Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + dest);
	}
	
	private void addProtectionAndSoftware(Document doc, PdfFont bold, PdfFont regular) {
		String frameType = null;
		try {
			frameType = pamRevision.getStringProperty("u4_pam_frame_type");
		} catch (TCException tcExc) {
			tcExc.printStackTrace();
			frameType = null;
		}
		if (frameType != null) {
			if (frameType.equals("G-ELECTRONIC") || frameType.equals("G-ELECTROMECHANICAL")) {
				addElectronicRelatedSection("Protection", protectionRelation, doc, bold, regular);
				if (frameType.equals("G-ELECTRONIC")) {
					addElectronicRelatedSection("Software", softwareRelation, doc, bold, regular);
				}
			}
		}
	}

	private void addElectronicRelatedSection(String sectionName, String relationName, Document doc, PdfFont bold, PdfFont regular) {
		Map<String, List<TCComponent>> tcname_tccomponent = null;
		// add section header
		Table eRelatedSecTab = new Table(1);
		eRelatedSecTab.addCell(new Cell().setFont(bold).setFontSize(12).setPadding(0)
				.setBackgroundColor(new DeviceRgb(230, 230, 255))
        		.add(sectionName)
        		.setBorder(Border.NO_BORDER)); 
        doc.add(new Paragraph("\n"));             
        doc.add(eRelatedSecTab);
		
        tcname_tccomponent = getForms(relationName);
		Vector<Vector<PAMSecondaryPropValue>> PAMPropertyNameValue_v = null;
		PAMPropertyNameValue_v = getEPAMComponentTableConfig(tcname_tccomponent,relationName);
		for(int inx=0;inx<PAMPropertyNameValue_v.size();inx++)
		{
			PAMSecondaryPropValue propvalue = (PAMPropertyNameValue_v.get(inx)).get(0);
        	String tableType = propvalue.selectedComponent.getDisplayType();
        	doc.add(new Paragraph().add(tableType).setFontSize(10).setFont(regular).setFont(bold));
        	float columnWidths [] = new float[propvalue.propNameValuepair.length];
        	for(  int ColumnNumber =0;ColumnNumber< propvalue.propNameValuepair.length;ColumnNumber++ )
    		{
        		columnWidths[ColumnNumber] = propvalue.propNameValuepair[ColumnNumber].columnSize;        	
    		}
        	Table sectionTable = new Table(columnWidths);
        	sectionTable.setWidth(524);
        	for(  int ColumnNumber =0;ColumnNumber< propvalue.propNameValuepair.length;ColumnNumber++ )
    		{
        		sectionTable.addHeaderCell(new Cell().add( propvalue.propNameValuepair[ColumnNumber].propDisplayName).setFont(bold).setFontSize(8));        		       	
    		}
			for(  int j =0;j< (PAMPropertyNameValue_v.get(inx)).size();j++ )
    		{
				PAMSecondaryPropValue propvalue1 = (PAMPropertyNameValue_v.get(inx)).get(j);
				// check for empty rows
				String colValue = null;
				boolean isEmptyRow = true;
				// check from 2nd column
				for(  int ColumnNumber =1;ColumnNumber< propvalue1.propNameValuepair.length;ColumnNumber++ )
				{
					colValue = propvalue1.propNameValuepair[ColumnNumber].propValue;
					if (colValue != null && !colValue.isEmpty())
					{
						isEmptyRow = false;
						break;
					}
				}
				if (!isEmptyRow)
				{
					for(  int ColumnNumber =0;ColumnNumber< propvalue1.propNameValuepair.length;ColumnNumber++ )
	        		{
						sectionTable.addCell(new Cell().add( propvalue1.propNameValuepair[ColumnNumber].propValue).setFont(regular).setFontSize(8));
					}
				}
				else
				{
					System.out.println("Skipping the row of this form in protection/software section");
				}
			}
			doc.add(sectionTable);
        	doc.add(new Paragraph("\n"));
		}
	}

	private ArrayList<DatasetInfo> getDatasetType() {
		// TODO Auto-generated method stub
		
		TCComponent[] seconds;
		
		ArrayList <DatasetInfo> DSInfo = new ArrayList<DatasetInfo>();
		ArrayList<DatasetInfo> sortedList = null;
		try {
			seconds = pamRevision.getRelatedComponents( Drawings );
			if(seconds.length>0){		
				for(TCComponent each : seconds)
				{
					DatasetInfo dsInfo = new DatasetInfo();
					TCComponentDataset dataset = (TCComponentDataset)each;
					String id = dataset.getStringProperty("object_string");
					String desc = dataset.getStringProperty("object_desc");
					String type = dataset.getDisplayType();
					dsInfo.setDatsetId(id);
					dsInfo.setDatasetDesc(desc);
					dsInfo.setDatasetType(type);
					
					TCComponentTcFile relatedTcFiles[] = dataset.getTcFiles(); 
					if(relatedTcFiles != null && relatedTcFiles.length != 0)
			    	{	
			    		File file = ((TCComponentTcFile)relatedTcFiles[0]).getFile( null );
			    		dsInfo.setDatasetfilepath(file.getAbsolutePath());
			    	}					
					DSInfo.add(dsInfo);
				}
				sortedList = new ArrayList<DatasetInfo>(DSInfo);
				Collections.sort(sortedList, new Comparator<DatasetInfo>() {
					public int compare(DatasetInfo o1, DatasetInfo o2) {
						return o1.getDatasetType().compareTo( o2.getDatasetType());
					}
				});
			}			
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return sortedList;
	}
	
	private String [] getNamedRef()
	{
		String [] fileName =null;
		TCComponent[] seconds;
		try {
			seconds = pamRevision.getRelatedComponents( Drawings );
			fileName = new String[seconds.length];
			int i=0;
			
		for(TCComponent each : seconds)
		{
			TCComponentDataset dataset = (TCComponentDataset)each;
			String desc = dataset.getStringProperty("object_desc");
			fileName[i] = desc;
			TCComponentTcFile relatedTcFiles[] = dataset.getTcFiles();   
			
			i++;
		}	
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return fileName;
	}
	
	private String [] getDatasetFilePath()
	{
		String [] fileName =null;
		TCComponent[] seconds;
		try {
			seconds = pamRevision.getRelatedComponents( Drawings );
			fileName = new String[seconds.length];
			int i=0;
			
		for(TCComponent each : seconds)
		{
			TCComponentDataset dataset = (TCComponentDataset)each;
			TCComponentTcFile relatedTcFiles[] = dataset.getTcFiles();   
			if(relatedTcFiles != null && relatedTcFiles.length != 0)
	    	{	
	    		File file = ((TCComponentTcFile)relatedTcFiles[0]).getFile( null );
	    		fileName[i] = file.getAbsolutePath();
	    		System.out.println("pdf dataset path:" +fileName);
	    		i++;
	    	}
		}	
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return fileName;
	}

	private boolean isRowEmpty( PAMSecondaryPropValue row, String section, String subsection)
	{
		boolean rowEmpty = false;
		
		if(section.equals(materialsRelation)){
			rowEmpty = true;
			
			for(  int i =0;i< row.propNameValuepair.length;i++ )
			{
				String display_name = row.propNameValuepair[i].propDisplayName;
				String value = row.propNameValuepair[i].propValue;
				
				if(subsection.equals("Material Details") || subsection.equals("Body Material Details") || subsection.equals("Material Details 2"))
				{
					if(!display_name.equals("Property"))
					{						
						if(!value.isEmpty())
							rowEmpty = false;
					}
				}
				else if(subsection.equals("Layer Structure"))
				{
					if(!(display_name.equals("Property")||display_name.equals("No.")||display_name.equals("UOM")))
						if(!value.isEmpty()){
							rowEmpty = false;
						}
				}
				else if(subsection.equals("Pack Layer Structure"))
				{
					if(!(display_name.equals("Property")||display_name.equals("Layer")))
						if(!value.isEmpty()){
							rowEmpty = false;
						}
				}
				else if(subsection.equals("Justification"))
				{
					if(!display_name.equals("Non-PML Selection Reason"))
					{						
						if(!value.isEmpty())
							rowEmpty = false;
					}
				}
				else if(subsection.equals("Functional Resin Properties") ||subsection.equals("Body Material Properties 2")
					||subsection.equals("Body Material Properties") || subsection.equals("Material Properties") ||
					subsection.equals("Body Material Properties")|| subsection.equals("Material Properties 2") )
				{
					if(!(display_name.equals("Property")||display_name.equals("Attribute")||display_name.equals("UOM")))
						if(!value.isEmpty()){
							rowEmpty = false;
						}
				}
				else if(subsection.equals("Handle Details") || subsection.equals("Bristle Details"))
				{
					if(!display_name.equals("Property"))
					{						
						if(!value.isEmpty())
							rowEmpty = false;
					}
				}
				else if(subsection.equals("Approved Bristle Material Details") || subsection.equals("Approved Anchor Wire Material Details") 
						|| subsection.equals("Approved Hot Foil Material Details"))
				{
					if(!display_name.equals("Property"))
					{						
						if(!value.isEmpty())
							rowEmpty = false;
					}
				}
				else if(subsection.equals("Approved Polymer Details") || subsection.equals("Approved Additives Details") || subsection.equals("Approved Additives Details - Local")
						||subsection.equals("Approved Masterbatch [MB] Details") || subsection.equals("Approved Masterbatch [MB] Details - Local"))
				{
					if(!(display_name.equals("Property")||display_name.equals("No.")))
						if(!value.isEmpty()){
							rowEmpty = false;
						}
				}
				
				else if(subsection.equals("Board Materials"))
				{
					if(!display_name.equals("Layer"))
					{						
						if(!value.isEmpty())
							rowEmpty = false;
					}
				}				
				else if(subsection.equals("Parts Material Details"))
				{
					if(!(display_name.equals("Property")||display_name.equals("No.")))
						if(!value.isEmpty()){
							rowEmpty = false;
						}
				}
				else if(subsection.equals("Can Maker's End Material Details") || subsection.equals("Can Maker's End Material Properties") || subsection.equals("Can Maker's End Material Properties 2"))
				{
					if(!(display_name.equals("Property")||display_name.equals("Attribute")||display_name.equals("UOM")))
						if(!value.isEmpty()){
							rowEmpty = false;
						}
				}
			}
		}
		else if(section.equals(packageDetailsRelation) || section.equals(articleDetailsRelation))
		{
			rowEmpty = true;
			for(  int i =0;i< row.propNameValuepair.length;i++ )
			{
				String display_name = row.propNameValuepair[i].propDisplayName;
				String value = row.propNameValuepair[i].propValue;
				
				if(subsection.equals("Package Details") || subsection.equals("Article Details"))
				{
					if(!display_name.equals("Property"))
					if(!value.isEmpty()){
							rowEmpty = false;
					}
				}
				else if(subsection.equals("Package Dimensions") || subsection.equals("Article Dimensions"))
				{
					if(!(display_name.equals("Property")||display_name.equals("Attribute")||display_name.equals("UOM")))
						if(!value.isEmpty()){
							rowEmpty = false;
						}
				}
				else if(subsection.equals("Package Dimensions II"))
				{
					if(!(display_name.equals("Property")||display_name.equals("Attribute")||display_name.equals("UOM")))
						if(!value.isEmpty()){
							rowEmpty = false;	
						}
				}
			}
		}
		else if(section.equals(rcvUsageCondRelation))
		{
			for(  int i =0;i< row.propNameValuepair.length;i++ )
			{
				String display_name = row.propNameValuepair[i].propDisplayName;
				String value = row.propNameValuepair[i].propValue;
				
				if(subsection.equals("Usage Conditions"))
				{
					if(display_name.equals("Description"))
					{						
						if(value.isEmpty())
							rowEmpty = true;
					}
					else if(subsection.equals("Usage Conditions Information"))
					{
						if(display_name.equals("Usage Conditions Information"))
						{
							if(value.isEmpty())
								rowEmpty = true;					
						}
					}
				}
			}
		}
		else if(section.equals(envRelation))
		{
			rowEmpty = true;
			for(  int i =0;i< row.propNameValuepair.length;i++ )
			{
				String display_name = row.propNameValuepair[i].propDisplayName;
				String value = row.propNameValuepair[i].propValue;
				
				if(subsection.equals("Environmental Weight"))
				{
					if(!(display_name.equals("Property")||display_name.equals("Attribute")||display_name.equals("UOM")))
					{						
						if(!value.isEmpty())
							rowEmpty = false;
					}
				}
				else if(subsection.equals("Environment Packaging Material"))
				{
					if(!display_name.equals("Material"))
					{
						if(!value.isEmpty())
							rowEmpty = false;					
					}
				}
			}
		}
		else if(section.equals(compPropRelation))
		{
			rowEmpty = true;
			for(  int i =0;i< row.propNameValuepair.length;i++ )
			{
				String display_name = row.propNameValuepair[i].propDisplayName;
				String value = row.propNameValuepair[i].propValue;	
								
				if(subsection.equals("Component Properties"))
				{
					if(!(display_name.equals("Property")||display_name.equals("Attribute")||display_name.equals("UOM") ||display_name.equals("CoA")||display_name.equals("Cr")))						
						if(!value.isEmpty() || value.equals("False")){
							
							rowEmpty = false;
						}				
				}
				else if(subsection.equals("Visual/Performance/Other Characteristics") )
				{
					if(!(display_name.equals("Property")||display_name.equals("Attribute") ||display_name.equals("CoA")||display_name.equals("Cr")))
						if(!value.isEmpty() || value.equals("False")){
							rowEmpty = false;
						}
				}
				
			}
		}
		else if(section.equals(integratedLabelRelation))
		{
			rowEmpty = true;
			for(  int i =0;i< row.propNameValuepair.length;i++ )
			{
				String display_name = row.propNameValuepair[i].propDisplayName;
				String value = row.propNameValuepair[i].propValue;
				
				if(subsection.equals("Package Details Layer Form") || subsection.equals("Package Details"))
				{
					if(!(display_name.equals("Property")))
						if(!value.isEmpty()){
							rowEmpty = false;
						}
				}
				else if(subsection.equals("Label Properties"))
				{
					if(!(display_name.equals("Property")||display_name.equals("Attribute")|| display_name.equals("UOM")||display_name.equals("CoA")||display_name.equals("Cr")))
						if(!value.isEmpty() || value.equals("False")){
							rowEmpty = false;
						}
				}
			}
		}
		
		return rowEmpty;
	}
	
	
	
	private String getFormValue(String relation, String formType, String attribute)
	{
		String value = null;
		AIFComponentContext[] Form;
		try {
			Form = pamRevision.getRelated(relation);	
			for(AIFComponentContext form1 : Form){
			TCComponentForm form = (TCComponentForm)form1.getComponent();
			String form_name = form.getStringProperty("object_name");
			if(form_name.equals(formType))
			{
				value = form.getProperty(attribute);
			}
			}
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return value;
	}
	
	private String getMatlClassificationValue(String attrValue)
	{
		String value = null;
		AIFComponentContext[] MatlClassForm;
		try {
			MatlClassForm = pamRevision.getRelated(UL4Common.GMCFORMRELATION);		
			TCComponentForm matl_class_form = (TCComponentForm )MatlClassForm[0].getComponent();			
			value = matl_class_form.getProperty(attrValue);
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return value;
	}

	private Map<String, List<TCComponent>> getForms(String relation)
	{
		TCComponent[] renderedComponents = null;
		TCComponent[] structuredComponents = null;
		try {
			renderedComponents = pamRevision.getRelatedComponents(relation);
			structuredComponents = pamRevision.getRelatedComponents("U4_StructuredPropRelation");
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
		
		return tname_tccomponent;
	}
	
	public Vector<Vector<PAMSecondaryPropValue>> getEPAMComponentTableConfig(Map<String, List<TCComponent>> tcname_tccomponent,String Relation)
	{
		LoadPAMConfigurations loadConfig = new LoadPAMConfigurations();
        
		Vector<PAMTableConfiguration> tableConfigs = new Vector<PAMTableConfiguration>();
		
		Vector<Vector <PAMSecondaryPropValue>> PAMPropertyNameValue_v = new Vector<Vector <PAMSecondaryPropValue>>();
		
		boolean bTableconfig = false;

		tableConfigs = loadConfig.readPreferenceValues1(Relation+PAMConstant.CONFIGURATIONS, session);
				
		AIFComponentContext[] comp;
		String packcomponentType = null;
		String pamSecType = null;
		try{
			pamSecType = pamRevision.getType();
			comp = pamRevision.getPrimary();

				if  ( comp.length== 1)
				{
					if( comp[0].getComponent() instanceof TCComponentItemRevision)
						packcomponentType = comp[0].getComponent().getType();
				}
			}
			catch (TCException e) 
			{
				e.printStackTrace();
			}
		
		ArrayList <String> ignorePrimaryTypeWhenAll = new ArrayList <String>();
		ArrayList <String> ignorePrimaryTypeWhenOnlyPri = new ArrayList <String>();

		for ( int inx = 0 ;inx < tableConfigs.size() ;inx ++ )
			if(tableConfigs.get(inx).primaryType.contains(pamSecType))
				ignorePrimaryTypeWhenAll.add(tableConfigs.get(inx).secondaryType );

		for ( int inx = 0 ;inx < tableConfigs.size() ;inx ++ )
			if(tableConfigs.get(inx).primaryType.contains(pamSecType) && packcomponentType != null &&  tableConfigs.get(inx).primaryType.contains(packcomponentType))
				ignorePrimaryTypeWhenOnlyPri.add(tableConfigs.get(inx).secondaryType );

		for(int inx=0;inx<tableConfigs.size();inx++)
		{
			Vector<PAMSecondaryPropValue> secondaryPropValue  = new Vector<PAMSecondaryPropValue>();

			if(((tableConfigs.get(inx).primaryType).contains(pamSecType) == true ) &&  packcomponentType !=  null  &&   tableConfigs.get(inx).packcomponentType != null   && tableConfigs.get(inx).primaryType !=null && ((tableConfigs.get(inx).packcomponentType).equals(packcomponentType) ==true ))
			{
				//this gives all the related forms to the PAM SPec with the relation mentioned in the renderingHint and the type mentioned in the preference
				Vector<TCComponent >relatedComponents =getRelatedComponentOfASecondayType(tcname_tccomponent, Relation,tableConfigs.get(inx).secondaryType);

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

					TCProperty[] currProperty = null;
					try {
						currProperty = tempForm.getTCProperties(formproperties);
					} catch (TCException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					for(int knx=0;knx<(tableConfigs.get(inx)).ColumnConfigurations.length;knx++)
					{
						propValueArray[knx] = new PAMPropertyNameValue();
						propValueArray[knx].propName= (tableConfigs.get(inx)).ColumnConfigurations[knx].columnName;
						propValueArray[knx].tcProperty = currProperty[knx];
						propValueArray[knx].propDisplayName = currProperty[knx].getPropertyDisplayName();
						propValueArray[knx].propValue= currProperty[knx].getDisplayValue();
						propValueArray[knx].columnSize = (tableConfigs.get(inx).ColumnConfigurations)[knx].columnSize;
						
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
				Vector<TCComponent >relatedComponents =getRelatedComponentOfASecondayType(tcname_tccomponent, Relation,tableConfigs.get(inx).secondaryType);

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

					TCProperty[] currProperty = null;
					try {
						currProperty = tempForm.getTCProperties(formproperties);
					} catch (TCException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					for(int knx=0;knx<(tableConfigs.get(inx)).ColumnConfigurations.length;knx++)
					{
						propValueArray[knx] = new PAMPropertyNameValue();
						propValueArray[knx].propName= (tableConfigs.get(inx)).ColumnConfigurations[knx].columnName;
						propValueArray[knx].tcProperty = currProperty[knx];
						propValueArray[knx].propDisplayName = currProperty[knx].getPropertyDisplayName();
						propValueArray[knx].propValue= currProperty[knx].getDisplayValue();
						propValueArray[knx].columnSize = (tableConfigs.get(inx).ColumnConfigurations)[knx].columnSize;
						
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
				Vector<TCComponent >relatedComponents =getRelatedComponentOfASecondayType(tcname_tccomponent, Relation,tableConfigs.get(inx).secondaryType);

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

					TCProperty[] currProperty = null;
					try {
						currProperty = tempForm.getTCProperties(formproperties);
					} catch (TCException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
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
		return PAMPropertyNameValue_v;
	}
	
	
	
	public Vector<Vector<PAMSecondaryPropValue>> getEPAMTableConfig(Map<String, List<TCComponent>> tcname_tccomponent,String Relation)
	{
		LoadPAMConfigurations loadConfig = new LoadPAMConfigurations();
        
		Vector<PAMTableConfiguration> tableConfigs = new Vector<PAMTableConfiguration>();
		
		Vector<Vector <PAMSecondaryPropValue>> PAMPropertyNameValue_v = new Vector<Vector <PAMSecondaryPropValue>>();
		
		boolean bTableconfig = false;

		tableConfigs = loadConfig.readPreferenceValues1(Relation+PAMConstant.CONFIGURATIONS, session);
				
		AIFComponentContext[] comp;
		String packcomponentType = null;
		String pamSecType = null;
		try{
			pamSecType = pamRevision.getType();
			comp = pamRevision.getPrimary();

				if  ( comp.length== 1)
				{
					if( comp[0].getComponent() instanceof TCComponentItemRevision)
						packcomponentType = comp[0].getComponent().getType();
				}
			}
			catch (TCException e) 
			{
				e.printStackTrace();
			}
				
		for(int inx=0;inx<tableConfigs.size();inx++)
		{
			Vector<PAMSecondaryPropValue> secondaryPropValue =new Vector<PAMSecondaryPropValue>();
			
			if (packcomponentType == null || pamSecType == null || tableConfigs.get(inx).packcomponentType == null || tableConfigs.get(inx).primaryType ==null)
				continue;

			if(((tableConfigs.get(inx).primaryType).contains(pamSecType) == true ) && ((tableConfigs.get(inx).packcomponentType).equals(packcomponentType) ==true ))
			{
			
				Vector<TCComponent> relatedComponents = getRelatedComponentOfASecondayType(tcname_tccomponent, Relation,tableConfigs.get(inx).secondaryType);
				
				//this then sorts the obtained forms based on the row configuration mentioned in the preference
				if(!((tableConfigs.get(inx)).rowConfigurations.propertyName.equalsIgnoreCase("")))
					relatedComponents = sortForms(relatedComponents,(tableConfigs.get(inx)).rowConfigurations.propertyName,((tableConfigs.get(inx)).rowConfigurations.propertyValues).toArray(new String[(tableConfigs.get(inx)).rowConfigurations.propertyValues.size()]));

				
				for(int jnx=0; jnx<relatedComponents.size();jnx++)
				{
					TCComponentForm tempForm = (TCComponentForm)relatedComponents.get(jnx);
	
					//getting the values for the required Columns						
					PAMPropertyNameValue[] propValueArray = new PAMPropertyNameValue[(tableConfigs.get(inx)).ColumnConfigurations.length];
	
					String [] formproperties = new String [(tableConfigs.get(inx)).ColumnConfigurations.length];
	
					for(int knx=0;knx<(tableConfigs.get(inx)).ColumnConfigurations.length;knx++)
						formproperties[knx]=(tableConfigs.get(inx)).ColumnConfigurations[knx].columnName;
	
					TCProperty[] currProperty = null;
					try {
						currProperty = tempForm.getTCProperties(formproperties);
					} catch (TCException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					for(int knx=0;knx<(tableConfigs.get(inx)).ColumnConfigurations.length;knx++)
					{
						propValueArray[knx] = new PAMPropertyNameValue();
						propValueArray[knx].propName= (tableConfigs.get(inx)).ColumnConfigurations[knx].columnName;
						propValueArray[knx].tcProperty = currProperty[knx];
						propValueArray[knx].propDisplayName = currProperty[knx].getPropertyDisplayName();
						propValueArray[knx].propValue= currProperty[knx].getDisplayValue();
						propValueArray[knx].columnSize = (tableConfigs.get(inx).ColumnConfigurations)[knx].columnSize;
					}
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
		if (bTableconfig == false)
		{
			for(int inx=0;inx<tableConfigs.size();inx++)
			{	
				Vector<PAMSecondaryPropValue> secondaryPropValue =new Vector<PAMSecondaryPropValue>();

				if((tableConfigs.get(inx).primaryType).contains(pamRevision.getType()) && (tableConfigs.get(inx).packcomponentType == null ))
				{
					bTableconfig = true;

					//this gives all the related forms to the PAM SPec with the relation mentioned in the renderingHint and the type mentioned in the preference
					Vector<TCComponent >relatedComponents =getRelatedComponentOfASecondayType(tcname_tccomponent, Relation,tableConfigs.get(inx).secondaryType);

					if (relatedComponents.size() ==0)
						continue;
					
					//this then sorts the obtained forms based on the row configuration mentioned in the preference
					if(!((tableConfigs.get(inx)).rowConfigurations.propertyName.equalsIgnoreCase("")))
						relatedComponents = sortForms(relatedComponents,(tableConfigs.get(inx)).rowConfigurations.propertyName,((tableConfigs.get(inx)).rowConfigurations.propertyValues).toArray(new String[(tableConfigs.get(inx)).rowConfigurations.propertyValues.size()]));


					//looping the related object and adding the column and row configurations using the custom class
					for(int jnx=0; jnx<relatedComponents.size();jnx++)
					{
						TCComponentForm tempForm = (TCComponentForm)relatedComponents.get(jnx);

						//getting the values for the required Columns						
						PAMPropertyNameValue[] propValueArray = new PAMPropertyNameValue[(tableConfigs.get(inx)).ColumnConfigurations.length];

						String [] formproperties = new String [(tableConfigs.get(inx)).ColumnConfigurations.length];

						for(int knx=0;knx<(tableConfigs.get(inx)).ColumnConfigurations.length;knx++)
							formproperties[knx]=(tableConfigs.get(inx)).ColumnConfigurations[knx].columnName;

						TCProperty[] currProperty = null;
						try {
							currProperty = tempForm.getTCProperties(formproperties);
						} catch (TCException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
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
					//break;
				}
			}
		}
		if (bTableconfig == false)
		{
			for(int inx=0;inx<tableConfigs.size();inx++)
			{	
				Vector<PAMSecondaryPropValue> secondaryPropValue =new Vector<PAMSecondaryPropValue>();

				if((tableConfigs.get(inx).primaryType).contains("All"))
				{
					bTableconfig = true;
					//this gives all the related forms to the PAM SPec with the relation mentioned in the renderingHint and the type mentioned in the preference
					Vector<TCComponent >relatedComponents =getRelatedComponentOfASecondayType(tcname_tccomponent, Relation,tableConfigs.get(inx).secondaryType);

					if (relatedComponents.size() ==0)
						continue;
					
					//this then sorts the obtained forms based on the row configuration mentioned in the preference
					if(!((tableConfigs.get(inx)).rowConfigurations.propertyName.equalsIgnoreCase("")))
						relatedComponents = sortForms(relatedComponents,(tableConfigs.get(inx)).rowConfigurations.propertyName,((tableConfigs.get(inx)).rowConfigurations.propertyValues).toArray(new String[(tableConfigs.get(inx)).rowConfigurations.propertyValues.size()]));

					//looping the related object and adding the column and row configurations using the custom class
					for(int jnx=0; jnx<relatedComponents.size();jnx++)
					{
						TCComponentForm tempForm = (TCComponentForm)relatedComponents.get(jnx);

						//getting the values for the required Columns
						PAMPropertyNameValue[] propValueArray = new PAMPropertyNameValue[(tableConfigs.get(inx)).ColumnConfigurations.length];

						String [] formproperties = new String [(tableConfigs.get(inx)).ColumnConfigurations.length];

						for(int knx=0;knx<(tableConfigs.get(inx)).ColumnConfigurations.length;knx++)
							formproperties[knx]=(tableConfigs.get(inx)).ColumnConfigurations[knx].columnName;

						TCProperty[] currProperty = null;
						try {
							currProperty = tempForm.getTCProperties(formproperties);
						} catch (TCException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
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
					//break;
				}

			}
		}
		
		return PAMPropertyNameValue_v;
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
		
	private String formatFileName(String filename)
    {    	
    	char[] chars = filename.toCharArray();    	
    	String invalidChars = "\\/:*?\"<>|";    	
    	StringBuilder sb = new StringBuilder();
    	
    	for (int i = 0; i < chars.length; i++)
    	{
    	    if ((invalidChars.indexOf(chars[i]) >= 0)    || (chars[i] < '\u0020')  || (chars[i] > '\u007e' && chars[i] < '\u00a0') )
    	    	sb.append("_");
    	    else
    	    	sb.append(chars[i]); 	    
    	}    	
    	
    	return sb.toString() ;
    }
	
	/**
	 * 
	 * Delete OS Files 
	 * 
	 * @param f
	 * @return
	 */
	
	 public static boolean delete(File f)
	  {
		  if(f==null)
			  return true;
		  
		  if (!f.exists())
		      throw new IllegalArgumentException(
		          "Delete: no such file or directory: " + f);

		    if (!f.canWrite())
		      throw new IllegalArgumentException("Delete: write protected: " + f);

		    if (f.isDirectory()) {
		      String[] files = f.list();
		      
		      if (files.length > 0)
		      {
		    	  for (int i = 0; i < files.length; i++) 
		    	  {
		    	      File entry = new File(f, files[i]);

		    	      if (entry.isDirectory())
		    	      {
		    	        if (!delete(entry))
		    	          return false;
		    	      }
		    	      else
		    	      {
		    	        if (!entry.delete())
		    	          return false;
		    	      }
		    	  }
		      }

		    }
		    
		    boolean success = f.delete();

		    if (!success)
		      throw new IllegalArgumentException("Delete: deletion failed");
		    
		    return success ;
		  
	  }
	 
	 /**
	     * 
	     * Delete OLD PDF Report if any found with PAM Revision under IMAN_Reference Relation
	     * 
	     * @return true / false 
	     */
	    
		public boolean deleteOldReport()
		{
			boolean delete = true ;
			
			try
			{
				AIFComponentContext[] component = pamRevision.getRelated(UL4Common.IMANREFERENCE);			
					
				for( int inx=0 ; inx < component.length ; inx++)
				{
					if( component[inx].getComponent()  instanceof TCComponentDataset)
					{
						if( component[inx].getComponent().getType().equals("PDF"))
						{	
							session.setStatus("Deleting Old PDF Report......");
							System.out.println("Deleting Old PDF Report......");
							
							DeleteOperation delOperation = new DeleteOperation( component[inx] );
							delOperation.executeOperation();		
						}
					}				
				}
			}
			catch (TCException e)
			{
				delete = false ;	

				System.out.println("Deleting Old PDF Report......Failed");
			}
			
			return delete ;	
			
		}
		
		 /**
	     * 
	     * Cut OLD PDF Report if any found with PAM Revision under IMAN_Reference Relation
	     * 
	     * @return true / false 
	     */
	    
		public boolean cutOldReport()
		{
			boolean cut = true ;
			
			try
			{
				AIFComponentContext[] component = pamRevision.getRelated(UL4Common.IMANREFERENCE);			
					
				for( int inx=0 ; inx < component.length ; inx++)
				{
					if( component[inx].getComponent()  instanceof TCComponentDataset)
					{
						if( component[inx].getComponent().getType().equals("PDF"))
						{	
							session.setStatus("Cut Old PDF Report......");
							System.out.println("Cut Old PDF Report......");
							
							CutOperation cutOperation = new CutOperation( component[inx] ,false);
							cutOperation.executeOperation();
							
							pamRevision.refresh();
						}
					}				
				}
			}
			catch (TCException e)
			{
				cut = false ;	
				
				MessageBox.post(AIFUtility.getActiveDesktop().getShell() ,
						   "Unable to Delete Old PDF Report."  , "Information" , MessageBox.INFORMATION);

				System.out.println("Cut Old PDF Report......Failed");
			}
			
			return cut ;	
			
		}
		
		public boolean isItMigratedSpec()
		{
			try
			{		
				String interspecId  = pamRevision.getTCProperty(UL4Common.INTERSPEC_ID).getNonNullDisplayableValue();
				String pamrevId     = pamRevision.getTCProperty(UL4Common.REVID).getNonNullDisplayableValue();
				String packrevid    = packRevision.getTCProperty(UL4Common.REVID).getNonNullDisplayableValue();
				TCComponent  []revs = ((TCComponentItemRevision )packRevision).getItem().getReleasedItemRevisions();
				
			   if(packrevid.equalsIgnoreCase("1")  && pamrevId.equalsIgnoreCase("1") &&  interspecId.length() > 0 &&  ( revs != null && revs[0].equals(packRevision)))
				   return true ;
			}
			catch (TCException e)
			{			
		      	MessageBox.post(AIFUtility.getActiveDesktop().getShell() ,e.getMessage(),reg.getString("error"),MessageBox.ERROR);
			}
			
			return false ;
		}
		
		/** 
		 * 
		 * Attatch Dataset to PAM Revision with relation IMAN_reference
		 * 
		 * @param dsName
		 * @param pdfFileNeme
		 * @throws Exception
		 */
		
		public void uploadDataset(String dsName, String pdfFileNeme) throws Exception
		{		
				
			String relation =  null ;
			InterfaceAIFComponent[] target = null ;
			String msg = "";
			
			try
			{	
			    if( ! isItMigratedSpec())	
			    {
			     	if(!deleteOldReport())
					    cutOldReport();	
			     	
					target = new InterfaceAIFComponent[]{pamRevision};	
					relation = UL4Common.IMANREFERENCE;
			    }
			    else
			    {			
	                InterfaceAIFComponent[] destArray = new InterfaceAIFComponent[] { session.getUser().getNewStuffFolder() };
					target = destArray ;
					relation = "contents";					
					msg = " and is placed under the \"NewStuff\" folder";
			    }


				NewDatasetOperation dsOperation = new NewDatasetOperation (
													session,AIFUtility.getActiveDesktop(),
													dsName,	dsName,dsName,"","PDF","PDF_Tool",false,
													pdfFileNeme , "" , "PDF_Reference" , true ,target ,relation);
			
				dsOperation.executeOperation();	
				
				MessageBox.post(AIFUtility.getActiveDesktop().getShell() ,
					   "PDF report " +dsName+ " successfully generated" + msg , "PDF Report" , MessageBox.INFORMATION);
			}
			catch (TCException e)
			{			
		      	MessageBox.post(AIFUtility.getActiveDesktop().getShell() ,e.getMessage(),reg.getString("error"),MessageBox.ERROR);
			}				
		}
	 
	private String getCurrentTimeStamp() 
	{
	    SimpleDateFormat sdfDate = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss");
	    return sdfDate.format(new Date());
	}
	
	private String getCurrentTimeStampForFIleName() 
	{    
		Calendar calendar = Calendar.getInstance();
	    return  Long.toString(calendar.getTimeInMillis());

	}
    
}
