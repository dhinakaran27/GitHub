/**
 * 
 */
package com.unilever.rac.ui.packreport;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.itextpdf.io.font.FontConstants;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
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
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.layout.LayoutArea;
import com.itextpdf.layout.layout.LayoutContext;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.VerticalAlignment;
import com.itextpdf.layout.renderer.TableRenderer;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCSession;

/**
 * @author s.yarrabothula
 *
 */
public class UL4PcrTableHandler implements IEventHandler {

	private Document doc;
	/** Header Table */
	private Table containerTable;
	/** Time Stamp */
	private String timeStamp;
	/** Height of Header */
	private float headerTableHeight;
	private static final String TITLE = "Pack Component Report";
	private URL Unilever_Logo_url = this.getClass().getResource("/data/logo.png");
	private PdfFont timesRegular;
	private static final String PREF_PCR_HEADER_TAB = "U4_Pack Component Report.Header Table";
	private static final String CLASSIFICATION_HEADER = "Confidential";
	
	public UL4PcrTableHandler(Document doc, TCComponent packComponentRevision,
			TCSession session, String timeStamp) {
		this.doc = doc;
		this.timeStamp = timeStamp;
		createHeader(packComponentRevision, session);
	}

	private void createHeader(TCComponent packComponentRevision, TCSession session) {
		this.containerTable = new Table(new float[]{61,115, 116, 115, 116});
		this.containerTable.setWidth(523);
		
		try {
			timesRegular = null;
			PdfFont timesBold = null;
			timesRegular = PdfFontFactory.createFont(FontConstants.TIMES_ROMAN);
			timesBold = PdfFontFactory.createFont(FontConstants.TIMES_BOLD);
			Image logo = new Image(ImageDataFactory.create(Unilever_Logo_url));
			
			// read property names required for header info from preference and get their values from TC
			Map<String, String> headerInfo = readHeaderInformation(packComponentRevision, session);
			
			// write header information that is read in previous step
			int iHeaderPropsCount = headerInfo.size();
			System.out.println("<<PCR::DBG>> # of header props is " + iHeaderPropsCount);
			int propRowsCount = (iHeaderPropsCount % 2 == 1)?(iHeaderPropsCount + 1) / 2 : iHeaderPropsCount/2;
			System.out.println("<<PCR::DBG>> # of property rows is " + propRowsCount);
			
			// add classification statement
			Table confidentialStmt = new Table(new float[]{523});
			confidentialStmt.setBorder(Border.NO_BORDER);
			confidentialStmt.addFooterCell(CLASSIFICATION_HEADER);
			
			/*this.containerTable.addCell(new Cell(1, 5)
			.setBorderRight(Border.NO_BORDER)
			.setBorderTop(Border.NO_BORDER)
			.setBorderLeft(Border.NO_BORDER)
			.add(CLASSIFICATION_HEADER));*/
			
			// add Unilever logo
			this.containerTable.addCell(new Cell(propRowsCount + 1, 1)
				.setBorderRight(Border.NO_BORDER)
				.add(logo.setHeight(60f).setWidth(60f)));
			
			// add header title for the report
			this.containerTable.addCell(new Cell(1, 2)
				.setFont(timesBold)
				.setFontSize(12) 
				.setBorderRight(Border.NO_BORDER)
				.setBorderBottom(Border.NO_BORDER).setBorderLeft(Border.NO_BORDER)
				.setTextAlignment(TextAlignment.RIGHT)
				.setPadding(0)
				.add(TITLE));
			// add time stamp
			this.containerTable.addCell(new Cell(1, 2)
				.setFont(timesRegular)
				.setFontSize(8)
				.setBorderBottom(Border.NO_BORDER).setBorderLeft(Border.NO_BORDER)
				.setTextAlignment(TextAlignment.CENTER)
				.setVerticalAlignment(VerticalAlignment.TOP)
				.setPadding(0)
				.add(this.timeStamp));
			
			for (Map.Entry<String, String> entry : headerInfo.entrySet()) {
				this.containerTable.addCell(new Cell()
     				.setFont(timesBold)
     				.setFontSize(8)         				
     				.setTextAlignment(TextAlignment.RIGHT)
     				.setPadding(0)
     				//.setVerticalAlignment(VerticalAlignment.TOP)
     				.setBorder(Border.NO_BORDER)         				
     				.add(entry.getKey() + " : "));
				
				this.containerTable.addCell(new Cell()
	 				.setFont(timesRegular)
	 				.setFontSize(8)         				
	 				.setTextAlignment(TextAlignment.LEFT)
	 				.setPadding(0)
	 				//.setVerticalAlignment(VerticalAlignment.TOP)
	 				.setBorder(Border.NO_BORDER)         				
	 				.add(entry.getValue()));
			}
			
			if (iHeaderPropsCount % 2 == 1) {
				this.containerTable.addCell(new Cell(1, 2)
				.setBorderTop(Border.NO_BORDER)
				.setBorderLeft(Border.NO_BORDER));
			}
			
			this.containerTable.setBorder(new SolidBorder(1));
			
			// calculate height of header table
			calculateHeightOfHeaderTable();
			
		} catch (IOException ioExc) {
			System.out.println("<<PCR::DBG>> Error occured...");
			ioExc.printStackTrace();
		}
		System.out.println("<<PCR::DBG>> End of createHeader()...");		
	}

	private Map<String, String> readHeaderInformation(TCComponent packComponentRevision, TCSession session) {
		Map<String, String> headerInfo = new LinkedHashMap<>();
		Map<String, String> field2PropNames = new LinkedHashMap<>();
		Map<String, String> propNameValues = new HashMap<>();
		Set<String> fieldNames = new LinkedHashSet<>();
		
		TCPreferenceService prefServ = session.getPreferenceService();
		String[] prefValues = prefServ.getStringValues(PREF_PCR_HEADER_TAB);
		
		// get properties info that are to be read(from TC) and written into report
		for (String prefValue : prefValues) {
			if (prefValue.indexOf("=") == -1) {
				System.out.println("<<PCR::DBG>> Skipping invalid preference value ...!");
				continue;
			} else {
				String[] tokens = prefValue.split("=");
				fieldNames.add(tokens[0]);
				if (tokens[1].indexOf("/") != -1) {
					String[] subFieldTokens = tokens[0].split("/");
					String[] subPropTokens = tokens[1].split("/");
					
					for (int inx = 0; inx < subPropTokens.length; inx++) {
						field2PropNames.put(subFieldTokens[inx].trim(), subPropTokens[inx].trim());
					}
				} else {
					field2PropNames.put(tokens[0], tokens[1]);
				}
			}
		}
		
		try {			
			String[] propNames = null;
			propNames = field2PropNames.values().toArray(new String[0]);
			
			// get values from TC
			String[] propValues = packComponentRevision.getProperties(propNames);
			// put property names and values in map
			for (int iPropInx = 0; iPropInx < propNames.length; iPropInx++) {
				propNameValues.put(propNames[iPropInx], propValues[iPropInx]);
			}
			
			// populate report header
			for (String aField : fieldNames) {
				if (aField.indexOf("/") == -1) {
					headerInfo.put(aField, propNameValues.get(field2PropNames.get(aField)));
				} else {
					String finalPropValue = null;
					String[] subFields = aField.split("/");
					int iTemp = 0;
					for (String subField : subFields) {
						if (iTemp == 0) {
							finalPropValue = propNameValues.get(field2PropNames.get(subField.trim()));
						} else {
							finalPropValue = finalPropValue + "/" +
								propNameValues.get(field2PropNames.get(subField.trim()));
						}
						++iTemp;
					}
					headerInfo.put(aField, finalPropValue);
				}
			}
		} catch (TCException exc) {
			System.out.println("<<PCR::DBG>> Error occured in reading properties of header table ...!");
			exc.printStackTrace();
		}
		System.out.println("<<PCR::DBG>> End of readHeaderInformation");
		return headerInfo;		
	}

	private void calculateHeightOfHeaderTable() {
		TableRenderer renderer = (TableRenderer) this.containerTable.createRendererSubTree();
        renderer.setParent(new Document(new PdfDocument(new PdfWriter(new ByteArrayOutputStream()))).getRenderer());
        this.headerTableHeight = renderer.layout(new LayoutContext(new LayoutArea(0, PageSize.A4))).getOccupiedArea().getBBox().getHeight();
	}

	@Override
	public void handleEvent(Event event) {
		PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
		PdfDocument pdfDoc = docEvent.getDocument();
		PdfPage page = docEvent.getPage();
		//PdfCanvas pdfCanvas = new PdfCanvas(page.newContentStreamBefore(), page.getResources(), pdfDoc);
		PdfCanvas pdfCanvas = new PdfCanvas(page);
		
		float rectHeight = getHeightOfHeaderTable();
		// add header
		addHeaderStatement(pdfCanvas, pdfDoc, page);
		
		Rectangle rect = new Rectangle(pdfDoc.getDefaultPageSize().getX() + doc.getLeftMargin(),
                pdfDoc.getDefaultPageSize().getTop() - doc.getTopMargin()-10,
                100,
                rectHeight+30);
		
		new Canvas(pdfCanvas, pdfDoc, rect).add(this.containerTable);
	}

	private void addHeaderStatement(PdfCanvas pdfCanvas, PdfDocument pdfDoc, PdfPage page) {
		Rectangle rect = new Rectangle(pdfDoc.getDefaultPageSize().getX() + doc.getLeftMargin(),
                pdfDoc.getDefaultPageSize().getTop() - 20,
                524,
                20);
		new Canvas(pdfCanvas, pdfDoc, rect).add(new Paragraph(CLASSIFICATION_HEADER)
				.setFontSize(8)
				.setTextAlignment(TextAlignment.CENTER));
		// add page numbers
		addPagination2(pdfCanvas, pdfDoc, page);
		System.out.println("<<PCR::DBG>> Adding header to the report as " + CLASSIFICATION_HEADER);
	}

	private void addPagination2(PdfCanvas pdfCanvas, PdfDocument pdfDoc, PdfPage page) {
		Rectangle rect = new Rectangle(pdfDoc.getDefaultPageSize().getX() + doc.getLeftMargin(),
                pdfDoc.getDefaultPageSize().getBottom(),
                524,
                20);
		
		int pageNumber = pdfDoc.getPageNumber(page);
		new Canvas(pdfCanvas, pdfDoc, rect).add(new Paragraph(String.format("Page %d", pageNumber))
		.setFontSize(8)
		.setTextAlignment(TextAlignment.RIGHT));
	}

	public float getHeightOfHeaderTable() {
		return this.headerTableHeight;
	}

}
