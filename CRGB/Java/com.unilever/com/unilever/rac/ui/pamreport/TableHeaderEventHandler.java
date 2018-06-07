package com.unilever.rac.ui.pamreport;

import com.itextpdf.io.font.FontConstants;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.border.Border;
import com.itextpdf.layout.border.SolidBorder;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.layout.LayoutArea;
import com.itextpdf.layout.layout.LayoutContext;
import com.itextpdf.layout.property.HorizontalAlignment;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.VerticalAlignment;
import com.itextpdf.layout.renderer.TableRenderer;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentForm;
import com.teamcenter.rac.kernel.TCException;
import com.unilever.rac.ui.common.UL4Common;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;


public class TableHeaderEventHandler implements IEventHandler {

	 protected Table table;
     protected float tableHeight;
     protected Document doc;
     private URL Unilever_Logo_url = this.getClass().getResource("/data/logo.png");     
     private String modifiedDate = "last_mod_date";
     private String releaseDate = "date_released";
     private String lastModifiedUser = "last_mod_user";
     private String baseUOM = "u4_base_uom";

     public TableHeaderEventHandler(Document doc, TCComponent pamRevision, String timestamp) {
    	 String PAM_id = null;
         String revid  = null;
         String owner  = null;
         String name  = null;
         String create_date = null;
         String modified_date = null;
         String frame = null;
         
         PdfFont regular = null;
         PdfFont bold = null;
         Image logo = null; 
         this.doc = doc;
         
         table = new Table(new float[]{61,231,231});
         table.setWidth(523);   
         //table.setHeight(125f);
         
         Table table2 = new Table(new float[]{110,119});
         table2.setWidth(229);
        // table2.setHeight(110f);
         Table table3 = new Table(new float[]{100,129});
         table3.setWidth(229);   
         table3.setHeight(110f);
         
         String tech = null;
         String interspec_id = null;
         String interspec_revid = null;
         String spec_owner = null;
         String spec_code = null;
         String status_change_date = null;
         String last_modified_user = null;
         String base_uom = null;
         String status = null;
         String keywords = null;
                 
         try {
			regular = PdfFontFactory.createFont(FontConstants.TIMES_ROMAN);
			bold = PdfFontFactory.createFont(FontConstants.TIMES_BOLD);	
			logo = new Image(ImageDataFactory.create(Unilever_Logo_url));
			PAM_id = pamRevision.getStringProperty(UL4Common.ITEMID);
	        revid  = pamRevision.getStringProperty(UL4Common.REVID);
	        owner  = pamRevision.getTCProperty(UL4Common.OWNER).getNonNullDisplayableValue();
	        name   = pamRevision.getStringProperty(UL4Common.OBJECT_NAME);
	        create_date = pamRevision.getTCProperty(UL4Common.CREATIONDATE).getNonNullDisplayableValue();
	        frame = getMatlClassificationValue(UL4Common.PAM_FRAME,pamRevision);
	        tech = getMatlClassificationValue(UL4Common.TECHNOLOGY,pamRevision);
	        modified_date = pamRevision.getTCProperty(modifiedDate).getNonNullDisplayableValue();
	        interspec_id = pamRevision.getStringProperty("u4_interspec_id");
	        interspec_revid = pamRevision.getStringProperty("u4_interspec_revision_id");
	        spec_owner = pamRevision.getStringProperty("u4_spec_owner");
	        spec_code = pamRevision.getStringProperty("u4_spec_code");
	        status_change_date = pamRevision.getTCProperty(releaseDate).getNonNullDisplayableValue();
	        last_modified_user = pamRevision.getTCProperty(lastModifiedUser).getNonNullDisplayableValue();
	        base_uom = pamRevision.getStringProperty(baseUOM);
	        status = pamRevision.getTCProperty("release_status_list").getNonNullDisplayableValue();
	        keywords = pamRevision.getStringProperty("u4_keywords");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}         
       
         table.addCell(new Cell(2,1)  
         				.setBorderTop(new SolidBorder(1))
         				.setBorderLeft(new SolidBorder(1))
         				.setBorderBottom(new SolidBorder(1))
         				.setBorderRight(Border.NO_BORDER)
         				.add(logo.setHeight(60f).setWidth(60f)));         
         table.addCell(new Cell()
         				.setFont(bold)
         				.setFontSize(12)  
         				.setBorderTop(new SolidBorder(1)) 
         				.setBorderRight(Border.NO_BORDER)
         				.setBorderBottom(Border.NO_BORDER).setBorderLeft(Border.NO_BORDER)
         				.setTextAlignment(TextAlignment.RIGHT)
         				.setPadding(0)
         				.add("General Report"));
	     table.addCell(new Cell()
						.setFont(regular)
						.setFontSize(8)  
						.setBorderTop(new SolidBorder(1))
						.setBorderRight(new SolidBorder(1))
						.setBorderBottom(Border.NO_BORDER).setBorderLeft(Border.NO_BORDER)
						.setTextAlignment(TextAlignment.CENTER)
						.setVerticalAlignment(VerticalAlignment.TOP)
						.setPadding(0)
						.add(timestamp));
         table.addCell(new Cell()
         				.setPadding(0)
         				.setBorderBottom(new SolidBorder(1))
         				.setBorderTop(Border.NO_BORDER).setBorderRight(Border.NO_BORDER).setBorderLeft(Border.NO_BORDER)
        		 		.add(table2));
         table.addCell(new Cell().setPadding(0)
        		 		.setBorderTop(Border.NO_BORDER).setBorderLeft(Border.NO_BORDER)
        		 		.setBorderRight(new SolidBorder(1))
        		 		.setBorderBottom(new SolidBorder(1))
        		 		.add(table3));
         
       
         table2.addCell(new Cell()
         				.setFont(bold)
         				.setFontSize(8)         				
         				.setTextAlignment(TextAlignment.RIGHT)
         				.setPadding(0)
         				.setVerticalAlignment(VerticalAlignment.TOP)
         				.setBorder(Border.NO_BORDER)         				
         				.add("Specification Group : "));
         table2.addCell(new Cell()
        		 		.setFont(regular)
        		 		.setFontSize(8)
        		 		.setTextAlignment(TextAlignment.LEFT)
        		 		.setPadding(0) 
        		 		.setVerticalAlignment(VerticalAlignment.TOP)
        		 		.setBorder(Border.NO_BORDER)        		 		
        		 		.add("PAM"));
         table2.addCell(new Cell()
						.setFont(bold)
						.setFontSize(8)   
						.setTextAlignment(TextAlignment.RIGHT)	
						.setPadding(0)
						.setBorder(Border.NO_BORDER)																
						.add("PAM ID / Rev : "));
        table2.addCell(new Cell()
				 		.setFont(regular)
				 		.setFontSize(8)
				 		.setTextAlignment(TextAlignment.LEFT)
				 		.setBorder(Border.NO_BORDER)
				 		.setPadding(0)
				 		.add(PAM_id+" / "+ revid));	
        table2.addCell(new Cell()
						.setFont(bold)         				
						.setFontSize(8)
						.setBorder(Border.NO_BORDER)
						.setPadding(0)
						.setTextAlignment(TextAlignment.RIGHT)						
						.add("Name : ")); 
		table2.addCell(new Cell()
						.setFont(regular)
						.setFontSize(8)
						.setPadding(0)
						.setBorder(Border.NO_BORDER)
						.setTextAlignment(TextAlignment.LEFT)
						.add(name));
		table2.addCell(new Cell()
						.setFont(bold)         				
						.setFontSize(8)  
						.setBorder(Border.NO_BORDER)
						.setPadding(0)
						.setTextAlignment(TextAlignment.RIGHT)						
						.add("Frame / Technology / Rev Id:"));						
		table2.addCell(new Cell()
						.setFont(regular)
						.setFontSize(8)
						.setPadding(0)
						.setBorder(Border.NO_BORDER)
						.setTextAlignment(TextAlignment.LEFT)
						.add(frame + " / " +tech + " / " + revid));
		table2.addCell(new Cell()
						.setFont(bold)
						.setFontSize(8)	
						.setBorder(Border.NO_BORDER)
						.setPadding(0)
						.setTextAlignment(TextAlignment.RIGHT)						
						.add("IS Reference ID/Rev :"));
		table2.addCell(new Cell()
						.setFont(regular)
						.setFontSize(8)
						.setPadding(0)
						.setBorder(Border.NO_BORDER)
						.setTextAlignment(TextAlignment.LEFT)
						.add(interspec_id + " / " + interspec_revid));
		table2.addCell(new Cell()
						.setFont(bold)
						.setPadding(0)
						.setBorder(Border.NO_BORDER)
						.setTextAlignment(TextAlignment.RIGHT)
						.setFontSize(8)					
						.add("Spec Code:"));
		table2.addCell(new Cell()
						.setFont(regular)
						.setFontSize(8)
						.setPadding(0)
						.setBorder(Border.NO_BORDER)
						.setTextAlignment(TextAlignment.LEFT)
						.add(spec_code));
		table2.addCell(new Cell()
						.setFont(bold)
						.setFontSize(8)	
						.setPadding(0)
						.setBorder(Border.NO_BORDER)
						.setTextAlignment(TextAlignment.RIGHT)						
						.add("Status Change Date:"));
		table2.addCell(new Cell()
						.setFont(regular)
						.setBorder(Border.NO_BORDER)
						.setFontSize(8)
						.setPadding(0)
						.setTextAlignment(TextAlignment.LEFT)
						.add(status_change_date));
		 table2.addCell(new Cell()
						.setFont(bold)
						.setPadding(0)
						.setBorder(Border.NO_BORDER)
						.setFontSize(8)						
						.setTextAlignment(TextAlignment.RIGHT)						
						.add("Status:"));
		 table2.addCell(new Cell()
						.setFont(regular)
						.setFontSize(8)
						.setPadding(0)
						.setBorder(Border.NO_BORDER)
						.setTextAlignment(TextAlignment.LEFT)
						.add(status));
		 table2.addCell(new Cell()		 				
						.setFont(bold)
						.setFontSize(8)						
						.setPadding(0)
						.setBorder(Border.NO_BORDER)
						.setTextAlignment(TextAlignment.RIGHT)						
						.add("Keywords:"));
		 table2.addCell(new Cell()		 				
						.setFont(regular)
						.setFontSize(8)
						.setPadding(0) 
						.setBorder(Border.NO_BORDER)
						.setTextAlignment(TextAlignment.LEFT)
						.add(keywords));
		 
         table3.addCell(new Cell()
         				.setPadding(0)
         				.setBorder(Border.NO_BORDER));         
         table3.addCell(new Cell()
						.setPadding(0)
						.setBorder(Border.NO_BORDER));  
         table3.addCell(new Cell()
         				.setBorder(Border.NO_BORDER)
         				.setFontSize(8)   
         				.setPadding(0)
         				.setTextAlignment(TextAlignment.RIGHT)
         				.setFont(bold)
         				.add("Created By : "));
         table3.addCell(new Cell()
						.setFont(regular)
						.setFontSize(8)
						.setPadding(0)
						.setBorder(Border.NO_BORDER)
						.setTextAlignment(TextAlignment.LEFT)
						.add(owner));	
         table3.addCell(new Cell()
         				.setBorder(Border.NO_BORDER)
						.setFontSize(8)  	
						.setPadding(0)
						.setTextAlignment(TextAlignment.RIGHT)
						.setFont(bold)
						.add("Created On :"));
         table3.addCell(new Cell()
						.setFont(regular)
						.setFontSize(8)
						.setPadding(0)
						.setBorder(Border.NO_BORDER)
						.setTextAlignment(TextAlignment.LEFT)
						.add(create_date));
         table3.addCell(new Cell()
						.setFontSize(8)	
						.setPadding(0)
						.setBorder(Border.NO_BORDER)
						.setTextAlignment(TextAlignment.RIGHT)
						.setFont(bold)
						.add("Modified By :"));         
         table3.addCell(new Cell()
						.setFont(regular)
						.setPadding(0)
						.setBorder(Border.NO_BORDER)
						.setFontSize(8)
						.setTextAlignment(TextAlignment.LEFT)
						.add(last_modified_user));
         table3.addCell(new Cell().setPadding(0).setBorder(Border.NO_BORDER));
         table3.addCell(new Cell()
						.setFont(regular)
						.setFontSize(8)
						.setPadding(0)
						.setBorder(Border.NO_BORDER)
						.setTextAlignment(TextAlignment.LEFT));
         table3.addCell(new Cell()
         				.setBorder(Border.NO_BORDER)
						.setFontSize(8)						
						.setFont(bold)
						.setPadding(0)
						.setTextAlignment(TextAlignment.RIGHT)
						.add("Spec Owner:"));	
         table3.addCell(new Cell()
						.setFont(regular)
						.setPadding(0)
						.setFontSize(8)
						.setBorder(Border.NO_BORDER)
						.setTextAlignment(TextAlignment.LEFT)
						.add(spec_owner));
         table3.addCell(new Cell()
         				.setFontSize(8)
         				.setPadding(0)
         				.setFont(bold)         				
         				.setTextAlignment(TextAlignment.RIGHT)
         				.setBorder(Border.NO_BORDER)
						.add("Modified On:"));
         table3.addCell(new Cell()
						.setFont(regular)
						.setFontSize(8)
						.setPadding(0)
						.setBorder(Border.NO_BORDER)
						.setTextAlignment(TextAlignment.LEFT)
						.add(modified_date));
         table3.addCell(new Cell()
         				.setFontSize(8)         				
         				.setFont(bold)
         				.setPadding(0)
         				.setTextAlignment(TextAlignment.RIGHT)
						.setBorder(Border.NO_BORDER)
						.add("Base UoM:"));
         table3.addCell(new Cell()
						.setFont(regular)
						.setFontSize(8)
						.setPadding(0)
						.setBorder(Border.NO_BORDER)
						.setTextAlignment(TextAlignment.LEFT)
						.add(base_uom));
         table3.addCell(new Cell().setPadding(0).setBorder(Border.NO_BORDER));
         table3.addCell(new Cell()
						.setFont(regular)
						.setFontSize(8)
						.setPadding(0)
						.setBorder(Border.NO_BORDER)
						.setTextAlignment(TextAlignment.LEFT));
        
         TableRenderer renderer = (TableRenderer) table.createRendererSubTree();
         renderer.setParent(new Document(new PdfDocument(new PdfWriter(new ByteArrayOutputStream()))).getRenderer());
         tableHeight = renderer.layout(new LayoutContext(new LayoutArea(0, PageSize.A4))).getOccupiedArea().getBBox().getHeight();
     }

     public void handleEvent(Event event) {
         PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
         PdfDocument pdfDoc = docEvent.getDocument();
         PdfPage page = docEvent.getPage();
         PdfCanvas canvas = new PdfCanvas(page.newContentStreamBefore(), page.getResources(), pdfDoc);
         Rectangle rect = new Rectangle(pdfDoc.getDefaultPageSize().getX() + doc.getLeftMargin(),
                 pdfDoc.getDefaultPageSize().getTop() - doc.getTopMargin(), 100, getTableHeight()+30);
         PdfFont font = null;
         Text title = null;
         Paragraph p = null;
         Table tableConfidential = new Table(1);
         tableConfidential.setWidth(523);
		try {			
			font = PdfFontFactory.createFont(FontConstants.TIMES_ROMAN);
			title =new Text("Confidential").setFont(font);
			p = new Paragraph().add(title).setFontSize(10);
			tableConfidential.addCell(new Cell().add(p).setTextAlignment(TextAlignment.CENTER).setBorder(Border.NO_BORDER));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
         new Canvas(canvas, pdfDoc, rect).add(tableConfidential).add(table).add(new Paragraph("\n")); 
         new Canvas(canvas, pdfDoc, new Rectangle(36, 20, page.getPageSize().getWidth() - 72, 60)).add(new Paragraph(""));
         
     }

     public float getTableHeight() {
         return tableHeight;
     }
     private String getMatlClassificationValue(String attrValue,TCComponent pamRevision)
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
}
