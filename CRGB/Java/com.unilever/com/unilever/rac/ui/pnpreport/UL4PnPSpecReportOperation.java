/* Source File Name:   UL4PnPSpecReportOperation
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
 *  2.0.0		Sushmita T.			  02/08/2017			Change of framework to iText
 */

package com.unilever.rac.ui.pnpreport;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
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
 * The Class UL4PnPSpecReportOperation.
 */

public class UL4PnPSpecReportOperation extends AbstractAIFOperation
{	

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


	private boolean imgRequired            = false ;	
		
	private String Drawings           = "u4_dde_drawings";
	
	private String CodingNLabellingRelation = "U4_CodingLabellingRelation";
	private String CUContentRelation = "U4_ConsumerUnitContRelation";
	private String subCURelation = "U4_SubConsumerUnitRelation";
	private String CURelation = "U4_ConsumerUnitRelation";
	private String IntermediateRelation = "U4_IntermediateUnitRelation";
	private String caseBagRelation = "U4_CaseBagRelation";
	private String transportUnit = "U4_TransportUnitRelation";
	private String layerRelation = "U4_LayerRelation";	
	private String displayUnitRelation = "U4_DisplayUnitRelation";
	private String palletRelation = "U4_PalletRelation";
	private String configRelation = "U4_ConfigurationRelation";
	private String CUCDefectRelation = "U4_CUCRelation";
	private String CUDefectRelation = "U4_CURelation";
	private String caseBagDefectRelation = "U4_CaseBagEtcRelation";
	private String palletDefectRelation = "U4_PalletDefectRelation";
	private String localInfoRelation = "U4_LocalInformationRelation";
	//private String SPECIFICATIONS       = "IMAN_specification";
			
	
/**
 * Constructor
 * 
 * @param packrevision
 * @param pamrevision
 * @param tcsession
 */
			
	public UL4PnPSpecReportOperation(TCComponent dderevision , TCComponent pnprevision , TCSession tcsession , boolean imageRequired )
	{
		packRevision = dderevision ;
		pamRevision = pnprevision ;
		session = tcsession;
		reg = Registry.getRegistry( this ); 			
		imgRequired = imageRequired ;

	}	
	
	@Override
	public void executeOperation() throws Exception 
	{
		String pdfFileNeme        = null ; 			
		String tempDir            = null ;
		String dsName      		  = null ;
		
		try
		{
			String name     	 	  = pamRevision.getStringProperty(UL4Common.OBJECT_NAME).replace(" ","_");
			name                      = formatFileName(name);
			String revid    	 	  = pamRevision.getStringProperty(UL4Common.REVID);
			String timestamp 		  = getCurrentTimeStampForFIleName();
			tempDir   		  		  = System.getProperty("java.io.tmpdir");			
		    dsName       			  = name + "_" +revid + "_" + getCurrentTimeStamp()  ;
			pdfFileNeme               = tempDir + dsName + "." + "pdf" ;    
			String fileName 		  = timestamp + "." + "xml" ;
			
			SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String timeStamp1 = sdfDate.format(new Date());
			
			generatePdf(pdfFileNeme, pamRevision,dsName,timeStamp1);

		}
		catch (TCException e)
		{
			MessageBox.post(AIFUtility.getActiveDesktop().getShell() ,  e.getMessage(),"Error",MessageBox.ERROR);		
		}		
	}
	
	protected void generatePdf(String dest, TCComponent pamRevision, String dsName, String timestamp) throws Exception {
		PdfDocument pdfDoc = new PdfDocument(new PdfWriter(dest).setSmartMode(true));
        Document doc = new Document(pdfDoc, PageSize.A4);
 
        session.setStatus("Building PDF report Header.......");
		System.out.println("Building PDF report Header.......");
        
        PNPTableHeaderEventHandler handler = new PNPTableHeaderEventHandler(doc, pamRevision, timestamp);
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
        
        Table CuContent = new Table(1);
        CuContent.setWidth(523);
        
        Table TechDrw = new Table(1);
        TechDrw.setWidth(523);
        
        Table pdfDrwg = new Table(1);
        pdfDrwg.setWidth(523);
        
        Table defects = new Table(1);
        defects.setWidth(523);
        
        Table localInfo = new Table(1);
        localInfo.setWidth(523);
        
        Table assocPAM = new Table(1);
        assocPAM.setWidth(523);
        
        PdfFont regular = null;
        PdfFont bold = null;
        Color lavender = null;
        String sReasonforIssue = null;
        String fileName [] = null ;
        
        ArrayList <TCComponentDataset>  datasetDesc = new ArrayList<TCComponentDataset>(); 
                       
        try {
			regular = PdfFontFactory.createFont(FontConstants.TIMES_ROMAN);
			bold = PdfFontFactory.createFont(FontConstants.TIMES_BOLD);		
			lavender = new DeviceRgb(230, 230, 255);
			sReasonforIssue = pamRevision.getStringProperty(UL4Common.REASONFORISSUE);
			fileName = getDatasetFilePath();
			datasetDesc = getDatasets(Drawings);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        session.setStatus("Generating PnP Specification PDF Report.......");
	    System.out.println("Generating PnP Specification PDF Report.......");
        
        //add the Reason for Issue section to report
        reasonForIssueTable.addCell(new Cell().setFont(bold).setFontSize(12).setPadding(0).setBackgroundColor(lavender)
        		.add("Reason for Issue")
        		.setBorder(Border.NO_BORDER)); 
        doc.add(new Paragraph("\n"));
             
        doc.add(reasonForIssueTable);
        
        Paragraph p1 = new Paragraph(sReasonforIssue);
        p1.setFontSize(9).setFont(regular);
        doc.add(p1);  
                       
        //add General Information section to the report
        generalInfo.addCell(new Cell().setFont(bold).setFontSize(12).setPadding(0).setBackgroundColor(lavender)
        		.add("General Information")
        		.setBorder(Border.NO_BORDER)); 
        doc.add(new Paragraph("\n"));             
        doc.add(generalInfo);
      
        Paragraph p2 = new Paragraph("Description");
        p2.setFontSize(10).setFont(bold);
        doc.add(p2);
       
        String desc = getFormValue("U4_GeneralInfoRelation", "Description", "u4_text");
        if(desc!=null)
        {            
            doc.add(new Paragraph(desc).setFont(regular).setFontSize(8));
            doc.add(new Paragraph("\n"));
        }       
                 
        //add the Coding and Labelling section to the report
        
        finalSpec.addCell(new Cell().setFont(bold).setFontSize(12).setPadding(0).setBackgroundColor(lavender)
        		.add("Coding and Labelling")
        		.setBorder(Border.NO_BORDER));
        doc.add(finalSpec); 
        
        p2 = new Paragraph("Coding and Labelling Requirements:");
        p2.setFontSize(10).setFont(bold);
        doc.add(p2);
        
        desc = getFormValue(CodingNLabellingRelation, "Coding and Labelling Requirements", "u4_text");   
        doc.add(new Paragraph(desc).setFont(regular).setFontSize(8));
		        
        //add the Configuration section to the report
        
        CuContent.addCell(new Cell().setFont(bold).setFontSize(12).setPadding(0).setBackgroundColor(lavender)
        		.add("Configuration")
        		.setBorder(Border.NO_BORDER)); 
        doc.add(new Paragraph("\n"));             
        doc.add(CuContent);
                
        p2 = new Paragraph("Consumer Unit Content");
        p2.setFontSize(10).setFont(bold);
        doc.add(p2);        
        
        Map<String, List<TCComponent>> tcname_tccomponent = getForms(CUContentRelation);
        Vector<Vector<PAMSecondaryPropValue>> PAMPropertyNameValue_v = getEPAMTableConfig(tcname_tccomponent,CUContentRelation);
        for(int inx=0;inx<PAMPropertyNameValue_v.size();inx++)
		{
        	PAMSecondaryPropValue propvalue = (PAMPropertyNameValue_v.get(inx)).get(0);
        	String tableType = propvalue.selectedComponent.getDisplayType();
        	doc.add(new Paragraph().add(tableType+ ":").setFontSize(8).setFont(regular));
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
    			boolean rowEmpty = isRowEmpty(propvalue1, CUContentRelation,tableType);
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
        desc = getFormValue(CUContentRelation, "Additional Information", "u4_text");
        if(desc!=null)
        {
        	p2 = new Paragraph("Additional Information");
            p2.setFontSize(10).setFont(bold);
            doc.add(p2);                
             
            doc.add(new Paragraph(desc).setFont(regular).setFontSize(8));
            doc.add(new Paragraph("\n"));
        }
        
        //add the Sub-Consumer Unit section to the report
        
        p2 = new Paragraph("Sub-Consumer Unit");
        p2.setFontSize(10).setFont(bold);
        doc.add(p2);
        
        tcname_tccomponent = getForms(subCURelation);
        PAMPropertyNameValue_v = getEPAMTableConfig(tcname_tccomponent,subCURelation);
        for(int inx=0;inx<PAMPropertyNameValue_v.size();inx++)
		{
        	PAMSecondaryPropValue propvalue = (PAMPropertyNameValue_v.get(inx)).get(0);
        	String tableType = propvalue.selectedComponent.getDisplayType();
        	doc.add(new Paragraph().add(tableType+ ":").setFontSize(8).setFont(regular));
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
    			boolean rowEmpty = isRowEmpty(propvalue1, subCURelation,tableType);
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
        desc = getFormValue(subCURelation, "Additional Information", "u4_text");
        if(desc!=null)
        {
        	p2 = new Paragraph("Additional Information");
            p2.setFontSize(10).setFont(bold);
            doc.add(p2);                
             
            doc.add(new Paragraph(desc).setFont(regular).setFontSize(8));
            doc.add(new Paragraph("\n"));
        }
        
        //add the Consumer Unit section to the report
        
        p2 = new Paragraph("Consumer Unit");
        p2.setFontSize(10).setFont(bold);
        doc.add(p2);
        
        tcname_tccomponent = getForms(CURelation);
        PAMPropertyNameValue_v = getEPAMTableConfig(tcname_tccomponent,CURelation);
        for(int inx=0;inx<PAMPropertyNameValue_v.size();inx++)
		{
        	PAMSecondaryPropValue propvalue = (PAMPropertyNameValue_v.get(inx)).get(0);
        	String tableType = propvalue.selectedComponent.getDisplayType();
        	doc.add(new Paragraph().add(tableType+ ":").setFontSize(8).setFont(regular));
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
    			boolean rowEmpty = isRowEmpty(propvalue1, CURelation,tableType);
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
        desc = getFormValue(CURelation, "Additional Information", "u4_text");
        if(desc!=null)
        {
        	p2 = new Paragraph("Additional Information");
            p2.setFontSize(10).setFont(bold);
            doc.add(p2);                
             
            doc.add(new Paragraph(desc).setFont(regular).setFontSize(8));
            doc.add(new Paragraph("\n"));
        }
        
        //add the Intermediate Unit section to the report
        
        p2 = new Paragraph("Intermediate Unit");
        p2.setFontSize(10).setFont(bold);
        doc.add(p2);
        
        tcname_tccomponent = getForms(IntermediateRelation);
        PAMPropertyNameValue_v = getEPAMTableConfig(tcname_tccomponent,IntermediateRelation);
        for(int inx=0;inx<PAMPropertyNameValue_v.size();inx++)
		{
        	PAMSecondaryPropValue propvalue = (PAMPropertyNameValue_v.get(inx)).get(0);
        	String tableType = propvalue.selectedComponent.getDisplayType();
        	doc.add(new Paragraph().add(tableType+ ":").setFontSize(8).setFont(regular));
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
    			boolean rowEmpty = isRowEmpty(propvalue1, IntermediateRelation,tableType);
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
        desc = getFormValue(IntermediateRelation, "Additional Information", "u4_text");
        if(desc!=null)
        {
        	p2 = new Paragraph("Additional Information");
            p2.setFontSize(10).setFont(bold);
            doc.add(p2);                
             
            doc.add(new Paragraph(desc).setFont(regular).setFontSize(8));
            doc.add(new Paragraph("\n"));
        }
     
        //add the Cases/Bag etc section to the report        
        p2 = new Paragraph("Case/Bag etc");
        p2.setFontSize(10).setFont(bold);
        doc.add(p2);
        
        tcname_tccomponent = getForms(caseBagRelation);
        PAMPropertyNameValue_v = getEPAMTableConfig(tcname_tccomponent,caseBagRelation);
        for(int inx=0;inx<PAMPropertyNameValue_v.size();inx++)
		{
        	PAMSecondaryPropValue propvalue = (PAMPropertyNameValue_v.get(inx)).get(0);
        	String tableType = propvalue.selectedComponent.getDisplayType();
        	doc.add(new Paragraph().add(tableType+ ":").setFontSize(8).setFont(regular));
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
    			boolean rowEmpty = isRowEmpty(propvalue1, caseBagRelation,tableType);
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
        desc = getFormValue(caseBagRelation, "Additional Information", "u4_text");
        if(desc!=null)
        {
        	p2 = new Paragraph("Additional Information");
            p2.setFontSize(10).setFont(bold);
            doc.add(p2);                
             
            doc.add(new Paragraph(desc).setFont(regular).setFontSize(8));
            doc.add(new Paragraph("\n"));
        }
        
      //add the Transport unit section to the report        
        p2 = new Paragraph("Transport Unit");
        p2.setFontSize(10).setFont(bold);
        doc.add(p2);
        
        tcname_tccomponent = getForms(transportUnit);
        PAMPropertyNameValue_v = getEPAMTableConfig(tcname_tccomponent,transportUnit);
        for(int inx=0;inx<PAMPropertyNameValue_v.size();inx++)
		{
        	PAMSecondaryPropValue propvalue = (PAMPropertyNameValue_v.get(inx)).get(0);
        	String tableType = propvalue.selectedComponent.getDisplayType();
        	doc.add(new Paragraph().add(tableType+ ":").setFontSize(8).setFont(regular));
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
    			boolean rowEmpty = isRowEmpty(propvalue1, transportUnit,tableType);
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
        desc = getFormValue(transportUnit, "Additional Information", "u4_text");
        if(desc!=null)
        {
        	p2 = new Paragraph("Additional Information");
            p2.setFontSize(10).setFont(bold);
            doc.add(p2);                
             
            doc.add(new Paragraph(desc).setFont(regular).setFontSize(8));
            doc.add(new Paragraph("\n"));
        }
               
      //add the Layer section to the report        
        p2 = new Paragraph("Layer");
        p2.setFontSize(10).setFont(bold);
        doc.add(p2);
        
        tcname_tccomponent = getForms(layerRelation);
        PAMPropertyNameValue_v = getEPAMTableConfig(tcname_tccomponent,layerRelation);
        for(int inx=0;inx<PAMPropertyNameValue_v.size();inx++)
		{
        	PAMSecondaryPropValue propvalue = (PAMPropertyNameValue_v.get(inx)).get(0);
        	String tableType = propvalue.selectedComponent.getDisplayType();
        	doc.add(new Paragraph().add(tableType+ ":").setFontSize(8).setFont(regular));
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
    			boolean rowEmpty = isRowEmpty(propvalue1, layerRelation,tableType);
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
        desc = getFormValue(layerRelation, "Additional Information", "u4_text");
        if(desc!=null)
        {
        	p2 = new Paragraph("Additional Information");
            p2.setFontSize(10).setFont(bold);
            doc.add(p2);                
             
            doc.add(new Paragraph(desc).setFont(regular).setFontSize(8));
            doc.add(new Paragraph("\n"));
        }
        
      //add the Display unit section to the report        
        p2 = new Paragraph("Display Unit");
        p2.setFontSize(10).setFont(bold);
        doc.add(p2);
        
        tcname_tccomponent = getForms(displayUnitRelation);
        PAMPropertyNameValue_v = getEPAMTableConfig(tcname_tccomponent,displayUnitRelation);
        for(int inx=0;inx<PAMPropertyNameValue_v.size();inx++)
		{
        	PAMSecondaryPropValue propvalue = (PAMPropertyNameValue_v.get(inx)).get(0);
        	String tableType = propvalue.selectedComponent.getDisplayType();
        	doc.add(new Paragraph().add(tableType+ ":").setFontSize(8).setFont(regular));
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
    			boolean rowEmpty = isRowEmpty(propvalue1, displayUnitRelation,tableType);
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
        desc = getFormValue(displayUnitRelation, "Additional Information", "u4_text");
        if(desc!=null)
        {
        	p2 = new Paragraph("Additional Information");
            p2.setFontSize(10).setFont(bold);
            doc.add(p2);                
             
            doc.add(new Paragraph(desc).setFont(regular).setFontSize(8));
            doc.add(new Paragraph("\n"));
        }
        
      //add the Pallet  section to the report        
        p2 = new Paragraph("Pallet");
        p2.setFontSize(10).setFont(bold);
        doc.add(p2);
        
        tcname_tccomponent = getForms(palletRelation);
        PAMPropertyNameValue_v = getEPAMTableConfig(tcname_tccomponent,palletRelation);
        for(int inx=0;inx<PAMPropertyNameValue_v.size();inx++)
		{
        	PAMSecondaryPropValue propvalue = (PAMPropertyNameValue_v.get(inx)).get(0);
        	String tableType = propvalue.selectedComponent.getDisplayType();
        	doc.add(new Paragraph().add(tableType+ ":").setFontSize(8).setFont(regular));
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
    			boolean rowEmpty = isRowEmpty(propvalue1, palletRelation,tableType);
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
        desc = getFormValue(palletRelation, "Additional Information", "u4_text");
        if(desc!=null)
        {
        	p2 = new Paragraph("Additional Information");
            p2.setFontSize(10).setFont(bold);
            doc.add(p2);                
             
            doc.add(new Paragraph(desc).setFont(regular).setFontSize(8));
            doc.add(new Paragraph("\n"));
        }
                
      //add the Configuration section to the report        
        p2 = new Paragraph("Configuration");
        p2.setFontSize(10).setFont(bold);
        doc.add(p2);       
        
        tcname_tccomponent = getForms(configRelation);
        PAMPropertyNameValue_v = getEPAMTableConfig(tcname_tccomponent,configRelation);
        for(int inx=0;inx<PAMPropertyNameValue_v.size();inx++)
		{
        	PAMSecondaryPropValue propvalue = (PAMPropertyNameValue_v.get(inx)).get(0);
        	String tableType = propvalue.selectedComponent.getDisplayType();
        	doc.add(new Paragraph().add(tableType+ ":").setFontSize(8).setFont(regular));
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
    			boolean rowEmpty = isRowEmpty(propvalue1, configRelation,tableType);
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
        
        //add the DD Drawing section to the report
        
        TechDrw.addCell(new Cell().setFont(bold).setFontSize(12).setPadding(0).setBackgroundColor(lavender)
        		.add("Distribution Design Drawing")
        		.setBorder(Border.NO_BORDER)); 
        doc.add(new Paragraph("\n"));             
        doc.add(TechDrw);
       
        // add technical drawings to report        
        if(imgRequired)
        {
        	//pdfDrwg.addCell(new Cell().setFont(bold).setFontSize(12).setPadding(0).setBackgroundColor(lavender)
    	        		//.add("Distribution Design Drawing")
    	        		//.setBorder(Border.NO_BORDER)); 
        	        
	        doc.add(pdfDrwg);
	        doc.add(new Paragraph("\n"));
	        doc.add(new Paragraph("CAPE Drawings"));
	        
	        
	        String dwgNo = pamRevision.getStringProperty(UL4Common.ITEMID);
	        String revNo = pamRevision.getStringProperty(UL4Common.REVID);
	        dwgNo = dwgNo + "/" + revNo;
	        
	        Table datasetNames = new Table(new float [] {124,144,124,124});
	        datasetNames.addCell(new Cell().add("Name").setFont(bold).setFontSize(8));
	        datasetNames.addCell(new Cell().add("Description").setFont(bold).setFontSize(8));        
	        datasetNames.addCell(new Cell().add("Owner").setFont(bold).setFontSize(8));
	        datasetNames.addCell(new Cell().add("Date Created").setFont(bold).setFontSize(8));
	        
	        for(int i= 0;i<datasetDesc.size();i++)
	        {
	        	datasetNames.addCell(new Cell().add(dwgNo).setFont(regular).setFontSize(8));
	        	datasetNames.addCell(new Cell().add(datasetDesc.get(i).getStringProperty("object_desc")).setFont(regular).setFontSize(8));
	        	datasetNames.addCell(new Cell().add(datasetDesc.get(i).getTCProperty(UL4Common.OWNER).toString()).setFont(regular).setFontSize(8));
	        	datasetNames.addCell(new Cell().add(datasetDesc.get(i).getTCProperty(UL4Common.CREATIONDATE).toString()).setFont(regular).setFontSize(8));
	        }
	        doc.add(datasetNames);
	        
	        if(fileName!=null)
	        {
	        	for(int i=0;i<fileName.length;i++){
	        		PdfDocument assocSpecDataset = new PdfDocument(new PdfReader(fileName[i]));  
	    	        
	    	        for(int inx = 1;inx<=assocSpecDataset.getNumberOfPages();inx++)
	    	        {	
	    		        PdfPage origPage = assocSpecDataset.getPage(inx);	        
	    		        Rectangle rect = origPage.getPageSizeWithRotation();	     
	    		        PdfNumber rotate = origPage.getPdfObject().getAsNumber(PdfName.Rotate);	  	
	    		        String tempDir = System.getProperty("java.io.tmpdir");
	    		        tempDir = tempDir+"tempPdf.pdf";
	    		        	    		                
	    		        PdfDocument tempPdfDoc = new PdfDocument(new PdfReader(fileName[i]), new PdfWriter(tempDir));
	    		        
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
        
        //add the Defects/Foreign Material section to the report
        
        defects.addCell(new Cell().setFont(bold).setFontSize(12).setPadding(0).setBackgroundColor(lavender)
        		.add("Defects/Foreign Material")
        		.setBorder(Border.NO_BORDER)); 
        doc.add(new Paragraph("\n"));             
        doc.add(defects);
        
        p2 = new Paragraph("Consumer Unit Content");
        p2.setFontSize(10).setFont(bold);
        doc.add(p2);
        
        p2 = new Paragraph("Consumer Unit Content Packaging Defects:");
        p2.setFont(regular).setFontSize(8);
        doc.add(p2);
        
        tcname_tccomponent = getForms(CUCDefectRelation);
        PAMPropertyNameValue_v = getEPAMTableConfig(tcname_tccomponent,CUDefectRelation);
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
    			boolean rowEmpty = isRowEmpty(propvalue1, CUCDefectRelation,tableType);
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
        
        desc = getFormValue(CUCDefectRelation, "Sampling", "u4_text");
        if(desc!=null)
        {
        	p2 = new Paragraph("Sampling");
            p2.setFontSize(10).setFont(bold);
            doc.add(p2);                
             
            doc.add(new Paragraph(desc).setFont(regular).setFontSize(8));
            doc.add(new Paragraph("\n"));
        }
        desc = getFormValue(CUCDefectRelation, "CUC Defects Definitions", "u4_text");
        if(desc!=null)
        {
        	p2 = new Paragraph("CUC Defects Definitions");
            p2.setFontSize(10).setFont(bold);
            doc.add(p2);                
             
            doc.add(new Paragraph(desc).setFont(regular).setFontSize(8));
            doc.add(new Paragraph("\n"));
        }
        
        p2 = new Paragraph("Reference");
        p2.setFontSize(10).setFont(bold);
        doc.add(p2);
        
        Table CUCDataset = new Table(new float [] {124,144,124,124});
        CUCDataset.addCell(new Cell().add("Name").setFont(bold).setFontSize(8));
        CUCDataset.addCell(new Cell().add("Description").setFont(bold).setFontSize(8));        
        CUCDataset.addCell(new Cell().add("Owner").setFont(bold).setFontSize(8));
        CUCDataset.addCell(new Cell().add("Date Created").setFont(bold).setFontSize(8));
        
        ArrayList <TCComponentDataset> CUContDS = new ArrayList <TCComponentDataset>();
        CUContDS = getDatasets("U4_CUContRefRelation");
        if(CUContDS!=null)
        {
        	for(int i= 0;i<CUContDS.size();i++)
        	{
        		CUCDataset.addCell(new Cell().add(CUContDS.get(i).getStringProperty("object_string")).setFont(regular).setFontSize(8));
        		CUCDataset.addCell(new Cell().add(CUContDS.get(i).getStringProperty("object_desc")).setFont(regular).setFontSize(8));
            	CUCDataset.addCell(new Cell().add(CUContDS.get(i).getTCProperty(UL4Common.OWNER).toString()).setFont(regular).setFontSize(8));
            	CUCDataset.addCell(new Cell().add(CUContDS.get(i).getTCProperty(UL4Common.CREATIONDATE).toString()).setFont(regular).setFontSize(8));
        	}        	
        }
        doc.add(CUCDataset);
                
        p2 = new Paragraph("Consumer Unit");
        p2.setFontSize(10).setFont(bold);
        doc.add(p2);
        
        p2 = new Paragraph("Consumer Unit Packaging Defects:");
        p2.setFont(regular).setFontSize(8);
        doc.add(p2);
        
        tcname_tccomponent = getForms(CUDefectRelation);
        PAMPropertyNameValue_v = getEPAMTableConfig(tcname_tccomponent,CUDefectRelation);
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
    			boolean rowEmpty = isRowEmpty(propvalue1, CUDefectRelation,tableType);
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
        
        desc = getFormValue(CUDefectRelation, "Sampling", "u4_text");
        if(desc!=null)
        {
        	p2 = new Paragraph("Sampling");
            p2.setFontSize(10).setFont(bold);
            doc.add(p2);                
             
            doc.add(new Paragraph(desc).setFont(regular).setFontSize(8));
            doc.add(new Paragraph("\n"));
        }
        desc = getFormValue(CUDefectRelation, "CU Defects Definitions", "u4_text");
        if(desc!=null)
        {
        	p2 = new Paragraph("CU Defects Definitions");
            p2.setFontSize(10).setFont(bold);
            doc.add(p2);                
             
            doc.add(new Paragraph(desc).setFont(regular).setFontSize(8));
            doc.add(new Paragraph("\n"));
        }
        
        p2 = new Paragraph("Reference");
        p2.setFontSize(10).setFont(bold);
        doc.add(p2);
        
        Table CUDataset = new Table(new float [] {124,144,124,124});
        CUDataset.addCell(new Cell().add("Name").setFont(bold).setFontSize(8));
        CUDataset.addCell(new Cell().add("Description").setFont(bold).setFontSize(8));        
        CUDataset.addCell(new Cell().add("Owner").setFont(bold).setFontSize(8));
        CUDataset.addCell(new Cell().add("Date Created").setFont(bold).setFontSize(8));
        
        ArrayList <TCComponentDataset> CUontDS = new ArrayList <TCComponentDataset>();
        CUontDS = getDatasets("U4_CURefRelation");
        if(CUontDS!=null)
        {
        	for(int i= 0;i<CUContDS.size();i++)
        	{
        		CUDataset.addCell(new Cell().add(CUContDS.get(i).getStringProperty("object_string")).setFont(regular).setFontSize(8));
        		CUDataset.addCell(new Cell().add(CUContDS.get(i).getStringProperty("object_desc")).setFont(regular).setFontSize(8));
            	CUDataset.addCell(new Cell().add(CUContDS.get(i).getTCProperty(UL4Common.OWNER).toString()).setFont(regular).setFontSize(8));
            	CUDataset.addCell(new Cell().add(CUContDS.get(i).getTCProperty(UL4Common.CREATIONDATE).toString()).setFont(regular).setFontSize(8));
        	}        	
        }
        doc.add(CUDataset);        
        
        p2 = new Paragraph("Case/Bag, etc");
        p2.setFontSize(10).setFont(bold);
        doc.add(p2);
        
        p2 = new Paragraph("Case/Bag, etc Packaging Defects:");
        p2.setFont(regular).setFontSize(8);
        doc.add(p2);
        
        tcname_tccomponent = getForms(caseBagDefectRelation);
        PAMPropertyNameValue_v = getEPAMTableConfig(tcname_tccomponent,caseBagDefectRelation);
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
    			boolean rowEmpty = isRowEmpty(propvalue1, caseBagDefectRelation,tableType);
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
        
        desc = getFormValue(caseBagDefectRelation, "Sampling", "u4_text");
        if(desc!=null)
        {
        	p2 = new Paragraph("Sampling");
            p2.setFontSize(10).setFont(bold);
            doc.add(p2);                
             
            doc.add(new Paragraph(desc).setFont(regular).setFontSize(8));
            doc.add(new Paragraph("\n"));
        }
        desc = getFormValue(caseBagDefectRelation, "Case/Bag etc. Defects Definitions", "u4_text");
        if(desc!=null)
        {
        	p2 = new Paragraph("Case/Bag etc. Defects Definitions");
            p2.setFontSize(10).setFont(bold);
            doc.add(p2);                
             
            doc.add(new Paragraph(desc).setFont(regular).setFontSize(8));
            doc.add(new Paragraph("\n"));
        }
        
        p2 = new Paragraph("Reference");
        p2.setFontSize(10).setFont(bold);
        doc.add(p2);
        
        Table caseBagDataset = new Table(new float [] {124,144,124,124});
        caseBagDataset.addCell(new Cell().add("Name").setFont(bold).setFontSize(8));
        caseBagDataset.addCell(new Cell().add("Description").setFont(bold).setFontSize(8));        
        caseBagDataset.addCell(new Cell().add("Owner").setFont(bold).setFontSize(8));
        caseBagDataset.addCell(new Cell().add("Date Created").setFont(bold).setFontSize(8));
        
        ArrayList <TCComponentDataset> caseBagDS = new ArrayList <TCComponentDataset>();
        caseBagDS = getDatasets("U4_CURefRelation");
        if(caseBagDS!=null)
        {
        	for(int i= 0;i<caseBagDS.size();i++)
        	{
        		caseBagDataset.addCell(new Cell().add(caseBagDS.get(i).getStringProperty("object_string")).setFont(regular).setFontSize(8));
        		caseBagDataset.addCell(new Cell().add(caseBagDS.get(i).getStringProperty("object_desc")).setFont(regular).setFontSize(8));
        		caseBagDataset.addCell(new Cell().add(caseBagDS.get(i).getTCProperty(UL4Common.OWNER).toString()).setFont(regular).setFontSize(8));
        		caseBagDataset.addCell(new Cell().add(caseBagDS.get(i).getTCProperty(UL4Common.CREATIONDATE).toString()).setFont(regular).setFontSize(8));
        	}        	
        }
        doc.add(caseBagDataset);
        
        p2 = new Paragraph("Pallet");
        p2.setFontSize(10).setFont(bold);
        doc.add(p2);
        
        p2 = new Paragraph("Pallet Packaging Defects:");
        p2.setFont(regular).setFontSize(8);
        doc.add(p2);
        
        tcname_tccomponent = getForms(palletDefectRelation);
        PAMPropertyNameValue_v = getEPAMTableConfig(tcname_tccomponent,palletDefectRelation);
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
    			boolean rowEmpty = isRowEmpty(propvalue1, palletDefectRelation,tableType);
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
        
        desc = getFormValue(palletDefectRelation, "Sampling", "u4_text");
        if(desc!=null)
        {
        	p2 = new Paragraph("Sampling");
            p2.setFontSize(10).setFont(bold);
            doc.add(p2);                
             
            doc.add(new Paragraph(desc).setFont(regular).setFontSize(8));
            doc.add(new Paragraph("\n"));
        }
        desc = getFormValue(palletDefectRelation, "Pallet Defects Definitions", "u4_text");
        if(desc!=null)
        {
        	p2 = new Paragraph("Pallet Defects Definitions");
            p2.setFontSize(10).setFont(bold);
            doc.add(p2);                
             
            doc.add(new Paragraph(desc).setFont(regular).setFontSize(8));
            doc.add(new Paragraph("\n"));
        }
        
        p2 = new Paragraph("Reference");
        p2.setFontSize(10).setFont(bold);
        doc.add(p2);
        
        Table palletDataset = new Table(new float [] {124,144,124,124});
        palletDataset.addCell(new Cell().add("Name").setFont(bold).setFontSize(8));
        palletDataset.addCell(new Cell().add("Description").setFont(bold).setFontSize(8));        
        palletDataset.addCell(new Cell().add("Owner").setFont(bold).setFontSize(8));
        palletDataset.addCell(new Cell().add("Date Created").setFont(bold).setFontSize(8));
        
        ArrayList <TCComponentDataset> palletDS = new ArrayList <TCComponentDataset>();
        palletDS = getDatasets("U4_CURefRelation");
        if(palletDS!=null)
        {
        	for(int i= 0;i<palletDS.size();i++)
        	{
        		palletDataset.addCell(new Cell().add(palletDS.get(i).getStringProperty("object_string")).setFont(regular).setFontSize(8));
        		palletDataset.addCell(new Cell().add(palletDS.get(i).getStringProperty("object_desc")).setFont(regular).setFontSize(8));
        		palletDataset.addCell(new Cell().add(palletDS.get(i).getTCProperty(UL4Common.OWNER).toString()).setFont(regular).setFontSize(8));
        		palletDataset.addCell(new Cell().add(palletDS.get(i).getTCProperty(UL4Common.CREATIONDATE).toString()).setFont(regular).setFontSize(8));
        	}        	
        }
        doc.add(palletDataset);
        
      //add Local Information section to the report
        localInfo.addCell(new Cell().setFont(bold).setFontSize(12).setPadding(0).setBackgroundColor(lavender)
        		.add("Local Information")
        		.setBorder(Border.NO_BORDER)); 
        doc.add(new Paragraph("\n"));             
        doc.add(localInfo);      
        
        desc = getFormValue(localInfoRelation, "Local Information", "u4_text");        
        doc.add(new Paragraph(desc).setFont(regular).setFontSize(8));
        
        doc.add(new Paragraph("\n"));
        
      //add Associated PAM spec section to the report
        assocPAM.addCell(new Cell().setFont(bold).setFontSize(12).setPadding(0).setBackgroundColor(lavender)
        		.add("Associated PAM Specifications")
        		.setBorder(Border.NO_BORDER)); 
        doc.add(new Paragraph("\n"));             
        doc.add(assocPAM);
        
        p2 = new Paragraph("Consumer Unit");
        p2.setFontSize(10).setFont(bold);
        doc.add(p2);
        
        Table AssocPAMCU = new Table(new float [] {124,144,124,124});
        AssocPAMCU.addCell(new Cell().add("Name").setFont(bold).setFontSize(8));
        AssocPAMCU.addCell(new Cell().add("PAM ID").setFont(bold).setFontSize(8));        
        AssocPAMCU.addCell(new Cell().add("Revision").setFont(bold).setFontSize(8));
        AssocPAMCU.addCell(new Cell().add("Date Released").setFont(bold).setFontSize(8));
        
        ArrayList <TCComponentItemRevision> AssocPAMsCU = new ArrayList <TCComponentItemRevision>();
        AssocPAMsCU = getPAMs("U4_PnPToPAMCURelation");
        if(AssocPAMsCU!=null)
        {
        	for(int i= 0;i<AssocPAMsCU.size();i++)
        	{
        		AssocPAMCU.addCell(new Cell().add(AssocPAMsCU.get(i).getStringProperty(UL4Common.OBJECT_NAME)).setFont(regular).setFontSize(8));
        		AssocPAMCU.addCell(new Cell().add(AssocPAMsCU.get(i).getStringProperty(UL4Common.ITEMID)).setFont(regular).setFontSize(8));
        		AssocPAMCU.addCell(new Cell().add(AssocPAMsCU.get(i).getTCProperty(UL4Common.REVID).toString()).setFont(regular).setFontSize(8));
        		AssocPAMCU.addCell(new Cell().add(AssocPAMsCU.get(i).getTCProperty(UL4Common.RELEASED_DATE).toString()).setFont(regular).setFontSize(8));
        	}        	
        }
        doc.add(AssocPAMCU);
        doc.add(new Paragraph("\n"));  
        
        p2 = new Paragraph("Case Unit");
        p2.setFontSize(10).setFont(bold);
        doc.add(p2);
        
        Table AssocPAMCaseUnit = new Table(new float [] {124,144,124,124});
        AssocPAMCaseUnit.addCell(new Cell().add("Name").setFont(bold).setFontSize(8));
        AssocPAMCaseUnit.addCell(new Cell().add("PAM ID").setFont(bold).setFontSize(8));        
        AssocPAMCaseUnit.addCell(new Cell().add("Revision").setFont(bold).setFontSize(8));
        AssocPAMCaseUnit.addCell(new Cell().add("Date Released").setFont(bold).setFontSize(8));
        
        ArrayList <TCComponentItemRevision> AssocPAMsCaseUnit = new ArrayList <TCComponentItemRevision>();
        AssocPAMsCaseUnit = getPAMs("U4_PnPToPAMCaseUnitRelation");
        if(AssocPAMsCaseUnit!=null)
        {
        	for(int i= 0;i<AssocPAMsCaseUnit.size();i++)
        	{
        		AssocPAMCaseUnit.addCell(new Cell().add(AssocPAMsCaseUnit.get(i).getStringProperty(UL4Common.OBJECT_NAME)).setFont(regular).setFontSize(8));
        		AssocPAMCaseUnit.addCell(new Cell().add(AssocPAMsCaseUnit.get(i).getStringProperty(UL4Common.ITEMID)).setFont(regular).setFontSize(8));
        		AssocPAMCaseUnit.addCell(new Cell().add(AssocPAMsCaseUnit.get(i).getTCProperty(UL4Common.REVID).toString()).setFont(regular).setFontSize(8));
        		AssocPAMCaseUnit.addCell(new Cell().add(AssocPAMsCaseUnit.get(i).getTCProperty(UL4Common.RELEASED_DATE).toString()).setFont(regular).setFontSize(8));
        	}        	
        }
        doc.add(AssocPAMCaseUnit);
        doc.add(new Paragraph("\n"));  
        
        p2 = new Paragraph("Distribution Unit");
        p2.setFontSize(10).setFont(bold);
        doc.add(p2);
        
        Table AssocPAMDU = new Table(new float [] {124,144,124,124});
        AssocPAMDU.addCell(new Cell().add("Name").setFont(bold).setFontSize(8));
        AssocPAMDU.addCell(new Cell().add("PAM ID").setFont(bold).setFontSize(8));        
        AssocPAMDU.addCell(new Cell().add("Revision").setFont(bold).setFontSize(8));
        AssocPAMDU.addCell(new Cell().add("Date Released").setFont(bold).setFontSize(8));
        
        ArrayList <TCComponentItemRevision> AssocPAMsDU = new ArrayList <TCComponentItemRevision>();
        AssocPAMsDU = getPAMs("U4_PnPToPAMDURelation");
        if(AssocPAMsDU!=null)
        {
        	for(int i= 0;i<AssocPAMsDU.size();i++)
        	{
        		AssocPAMDU.addCell(new Cell().add(AssocPAMsDU.get(i).getStringProperty(UL4Common.OBJECT_NAME)).setFont(regular).setFontSize(8));
        		AssocPAMDU.addCell(new Cell().add(AssocPAMsDU.get(i).getStringProperty(UL4Common.ITEMID)).setFont(regular).setFontSize(8));
        		AssocPAMDU.addCell(new Cell().add(AssocPAMsDU.get(i).getTCProperty(UL4Common.REVID).toString()).setFont(regular).setFontSize(8));
        		AssocPAMDU.addCell(new Cell().add(AssocPAMsDU.get(i).getTCProperty(UL4Common.RELEASED_DATE).toString()).setFont(regular).setFontSize(8));
        	}        	
        }
        doc.add(AssocPAMDU);
               
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
			else
			{
				System.out.println("formType not found");
			}
			}
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return value;
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
			dataset.getDisplayType();
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
	
	private ArrayList <TCComponentItemRevision> getPAMs(String Relation)
	{
		TCComponent[] seconds;
		ArrayList <TCComponentItemRevision> datasets = new ArrayList <TCComponentItemRevision>();
		try {
			seconds = pamRevision.getRelatedComponents( Relation );			
			
		for(TCComponent each : seconds)
		{
			if(each instanceof TCComponentItemRevision){
				TCComponentItemRevision PAM = (TCComponentItemRevision)each;	
			datasets.add(PAM);
			}
		}	
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return datasets;
	}
	
	private ArrayList <TCComponentDataset> getDatasets(String Relation)
	{
		String [] fileName =null;
		TCComponent[] seconds;
		ArrayList <TCComponentDataset> datasets = new ArrayList <TCComponentDataset>();
		try {
			seconds = pamRevision.getRelatedComponents( Relation );
			fileName = new String[seconds.length];
			int i=0;
			
		for(TCComponent each : seconds)
		{
			if(each instanceof TCComponentDataset){
			TCComponentDataset dataset = (TCComponentDataset)each;	
			datasets.add(dataset);
			}
		}	
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return datasets;
	}
	private boolean isRowEmpty( PAMSecondaryPropValue row, String section, String subsection)
	{
		boolean rowEmpty = false;
		
		if(section.equals(CUContentRelation)){
			for(  int i =0;i< row.propNameValuepair.length;i++ )
			{
				String display_name = row.propNameValuepair[i].propDisplayName;
				String value = row.propNameValuepair[i].propValue;
				
				if(subsection.equals("Dimensions/Weight") )
				{
					if(display_name.equals("Max"))
					{						
						if(value.isEmpty())
							rowEmpty = true;
					}
				}				
			}
		}
		else if(section.equals(subCURelation))
		{
			for(  int i =0;i< row.propNameValuepair.length;i++ )
			{
				String display_name = row.propNameValuepair[i].propDisplayName;
				String value = row.propNameValuepair[i].propValue;
				
				if(subsection.equals("Dimensions/Weight"))
				{
					if(display_name.equals("Max"))
					{						
						if(value.isEmpty())
							rowEmpty = true;
					}
				}				
			}
		}
		else if(section.equals(CURelation))
		{
			for(  int i =0;i< row.propNameValuepair.length;i++ )
			{
				String display_name = row.propNameValuepair[i].propDisplayName;
				String value = row.propNameValuepair[i].propValue;
				
				if(subsection.equals("Dimensions/Weight"))
				{
					if(display_name.equals("Max"))
					{						
						if(value.isEmpty())
							rowEmpty = true;
					}
				}				
			}
		}
		else if(section.equals(IntermediateRelation))
		{
			for(  int i =0;i< row.propNameValuepair.length;i++ )
			{
				String display_name = row.propNameValuepair[i].propDisplayName;
				String value = row.propNameValuepair[i].propValue;
				
				if(subsection.equals("Dimensions/Weight") )
				{
					if(display_name.equals("Max"))
					{						
						if(value.isEmpty())
							rowEmpty = true;
					}
				}				
			}
		}
		else if(section.equals(caseBagRelation))
		{
			for(  int i =0;i< row.propNameValuepair.length;i++ )
			{
				String display_name = row.propNameValuepair[i].propDisplayName;
				String value = row.propNameValuepair[i].propValue;
				
				if(subsection.equals("Dimensions/Weight"))
				{
					if(display_name.equals("Max"))
					{						
						if(value.isEmpty())
							rowEmpty = true;
					}
				}				
			}
		}
		else if(section.equals(transportUnit))
		{
			for(  int i =0;i< row.propNameValuepair.length;i++ )
			{
				String display_name = row.propNameValuepair[i].propDisplayName;
				String value = row.propNameValuepair[i].propValue;
				
				if( subsection.equals("Dimensions/Weight"))
				{
					if(display_name.equals("Max"))
					{						
						if(value.isEmpty())
							rowEmpty = true;
					}
				}				
			}
		}
		else if(section.equals(layerRelation))
		{
			for(  int i =0;i< row.propNameValuepair.length;i++ )
			{
				String display_name = row.propNameValuepair[i].propDisplayName;
				String value = row.propNameValuepair[i].propValue;
				
				if(subsection.equals("Dimensions/Weight") )
				{
					if(display_name.equals("Max"))
					{						
						if(value.isEmpty())
							rowEmpty = true;
					}
				}				
			}
		}
		else if(section.equals(displayUnitRelation))
		{
			for(  int i =0;i< row.propNameValuepair.length;i++ )
			{
				String display_name = row.propNameValuepair[i].propDisplayName;
				String value = row.propNameValuepair[i].propValue;
				
				if(subsection.equals("Dimensions/Weight"))
				{
					if(display_name.equals("Max"))
					{						
						if(value.isEmpty())
							rowEmpty = true;
					}
				}				
			}
		}
		else if(section.equals(palletRelation))
		{
			for(  int i =0;i< row.propNameValuepair.length;i++ )
			{
				String display_name = row.propNameValuepair[i].propDisplayName;
				String value = row.propNameValuepair[i].propValue;
				
				if(subsection.equals("Dimensions/Weight"))
				{
					if(display_name.equals("Max"))
					{						
						if(value.isEmpty())
							rowEmpty = true;
					}
				}
				else if(subsection.equals("Pallet"))
				{
					if(display_name.equals("Type"))
					{						
						if(value.isEmpty())
							rowEmpty = true;
					}
				}
				else if(subsection.equals("Storage / Distribution Conditions"))
				{
					if(display_name.equals("Number"))
					{						
						if(value.isEmpty())
							rowEmpty = true;
					}
				}
				else if(subsection.equals("Carrier Load"))
				{
					if(display_name.equals("Value"))
					{						
						if(value.isEmpty())
							rowEmpty = true;
					}
				}
			}
		}
		else if(section.equals(configRelation))
		{
			for(  int i =0;i< row.propNameValuepair.length;i++ )
			{
				String display_name = row.propNameValuepair[i].propDisplayName;
				String value = row.propNameValuepair[i].propValue;
				
				if(subsection.equals("Packaging Configuration"))
				{
					if(display_name.equals("Number"))
					{						
						if(value.isEmpty())
							rowEmpty = true;
					}
				}
				else if(subsection.equals("Environmental Info"))
				{
					if(display_name.equals("type"))
					{						
						if(value.isEmpty())
							rowEmpty = true;
					}
				}
			}
		}
		else if(section.equals(CUDefectRelation))
		{
			for(  int i =0;i< row.propNameValuepair.length;i++ )
			{
				String display_name = row.propNameValuepair[i].propDisplayName;
				String value = row.propNameValuepair[i].propValue;
				
				if(subsection.equals("Dimensions/Weight"))
				{
					if(display_name.equals("Max"))
					{						
						if(value.isEmpty())
							rowEmpty = true;
					}
				}				
			}
		}
		else if(section.equals(CUCDefectRelation))
		{
			for(  int i =0;i< row.propNameValuepair.length;i++ )
			{
				String display_name = row.propNameValuepair[i].propDisplayName;
				String value = row.propNameValuepair[i].propValue;
				
				if(subsection.equals("Consumer Unit Content Packaging Defects"))
				{
					if(display_name.equals("Max"))
					{						
						if(value.isEmpty())
							rowEmpty = true;
					}
				}				
			}
		}
		else if(section.equals(caseBagDefectRelation))
		{
			for(  int i =0;i< row.propNameValuepair.length;i++ )
			{
				String display_name = row.propNameValuepair[i].propDisplayName;
				String value = row.propNameValuepair[i].propValue;
				
				if(subsection.equals("Dimensions/Weight"))
				{
					if(display_name.equals("Max"))
					{						
						if(value.isEmpty())
							rowEmpty = true;
					}
				}				
			}
		}
		else if(section.equals(palletDefectRelation))
		{
			for(  int i =0;i< row.propNameValuepair.length;i++ )
			{
				String display_name = row.propNameValuepair[i].propDisplayName;
				String value = row.propNameValuepair[i].propValue;
				
				if(subsection.equals("Dimensions/Weight"))
				{
					if(display_name.equals("Max"))
					{						
						if(value.isEmpty())
							rowEmpty = true;
					}
				}				
			}
		}
		return rowEmpty;
	}
	
	private Map<String, List<TCComponent>> getForms(String relation)
	{
		TCComponent[] renderedComponents = null;
		TCComponent[] structuredComponents = null;
		try {
			renderedComponents = pamRevision.getRelatedComponents(relation);			
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		List<TCComponent> allSecondaryComponents = new ArrayList<TCComponent>(renderedComponents.length);
		Collections.addAll(allSecondaryComponents, renderedComponents);		

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
	  * Update XSLT Template for Image Details
	  * 
	  * 
	  * @param tempDir
	  * @param tempXSLFileName
	  * @return true / false
	  */
		
    /**
     * 
     * Deleet OLD PDF Report if any found with PAM Revision under IMAN_Reference Relation
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
				TCComponentDataset dataset = (TCComponentDataset) component[inx].getComponent() ;	
				
				if( dataset instanceof TCComponentDataset)
				{
					if( dataset.getType().equals("PDF"))
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
	 * Attatch Dataset to PAM Revision with relation IMAN_reference
	 * 
	 * @param dsName
	 * @param pdfFileNeme
	 * @throws Exception
	 */
	
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
				TCComponentDataset dataset = (TCComponentDataset) component[inx].getComponent() ;	
				
				if( dataset instanceof TCComponentDataset)
				{
					if( dataset.getType().equals("PDF"))
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
	
	public void uploadDataset(String dsName, String pdfFileNeme) throws Exception
	{		
			
		String relation =  null ;
		InterfaceAIFComponent[] target = null ;
		String msg = "";
		
		try
		{	
			if(!deleteOldReport())
				cutOldReport();			
			
			target = new InterfaceAIFComponent[]{pamRevision};	
			relation = UL4Common.IMANREFERENCE;

			NewDatasetOperation dsOperation = new NewDatasetOperation (
												session,AIFUtility.getActiveDesktop(),
												dsName,	dsName,dsName,"","PDF","PDF_Tool",false,
												pdfFileNeme , "" , "PDF_Reference" , true ,target ,relation);
		
			dsOperation.executeOperation();	
			
			MessageBox.post(AIFUtility.getActiveDesktop().getShell() ,
				   "PDF Report Successfully Generated " + msg , "Information" , MessageBox.INFORMATION);
		}
		catch (TCException e)
		{			
	      	MessageBox.post(AIFUtility.getActiveDesktop().getShell() ,e.getMessage(),reg.getString("error"),MessageBox.ERROR);
		}				
	}	

	private String getCurrentTimeStamp() 
	{
	    SimpleDateFormat sdfDate = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss");
	    Date now = new Date();
	    String strDate = sdfDate.format(now);
	    return strDate;
	}
	
	private String getCurrentTimeStampForFIleName() 
	{
		Calendar calendar = Calendar.getInstance();
	    return  Long.toString(calendar.getTimeInMillis());
	}
	
	/**
	 * Downlpad XSLT Template File
	 * 
	 * @return XSLT FileName
	 * @throws TCException
	 */
	
        
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
}
