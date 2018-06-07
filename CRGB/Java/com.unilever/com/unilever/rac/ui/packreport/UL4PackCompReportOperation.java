package com.unilever.rac.ui.packreport;

import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.itextpdf.io.font.FontConstants;
import com.itextpdf.kernel.color.Color;
import com.itextpdf.kernel.color.DeviceRgb;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.border.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;
import com.teamcenter.soa.exceptions.NotLoadedException;

public class UL4PackCompReportOperation extends AbstractAIFOperation{

	private static final String PREF_PCR_BODY = "U4_Pack Component Report.Body";
	private static final String TILDA = "~";
	private static final String COLON = ":";
	private static final String COMMA = ",";
	private static final String EQUALTO = "=";
	private TCComponent packComponentRevision;
	/** The Registry */	 
	private Registry reg = null ;
	private TCSession session;
	private Document finalDoc;
	private PdfFont bold;
	private Color lavender;
	private PdfFont regular;
	private String errorMessage;
	private UL4PcrPackTableObject emptyObj;
	
	public UL4PackCompReportOperation(TCComponent selection, TCSession session) {
		this.packComponentRevision = selection;
		this.session = session;
		this.reg = Registry.getRegistry(this);
	}

	@Override
	public void executeOperation() throws Exception {
		// read properties
		String reportFileName = null;
		
		// get name of report
		reportFileName = getReportFileName();
		System.out.println("<<PCR::DBG>> Report file name is " + reportFileName);
		
		// generate report
		boolean isSuccess = generatePackComponentReport(reportFileName);
		
		if (isSuccess) {
			// display message to user
			displayUserMessage(reportFileName);
			
			// open report
			openFile(reportFileName);
		} else {
			// display error message
			displayErrorMessage();
		}
	}
	
	private void displayErrorMessage() {
		String error = null;
		if (this.errorMessage == null || this.errorMessage.isEmpty()) {
			error = reg.getString("componentsMissed");
		} else {
			error = this.errorMessage;
		}
		MessageBox.displayModalMessageBox(getCurrentDesktop(),
				error,
				reg.getString("title"),
				MessageBox.ERROR);
	}

	private void displayUserMessage(String reportFileName) {
		String message = null;
		File tempFile = new File(reportFileName);
		String path = tempFile.getParent();
		String fileName = tempFile.getName();
		message = "Successfully generated the Pack Component report (" + fileName + ") in location : " + path;
		// commented below as user needed modal dialog
		/*MessageBox.post(AIFUtility.getActiveDesktop().getShell() ,
				message,
				reg.getString("title"),
				MessageBox.INFORMATION);*/
		// to display modal dialog
		MessageBox.displayModalMessageBox(getCurrentDesktop(), message, reg.getString("title"), MessageBox.INFORMATION);
	}

	private void openFile(String fileName) throws IOException {
		Desktop desktop = Desktop.getDesktop();
		desktop.open(new File(fileName));
	}

	private String getReportFileName() {
		String name = null;
		String fileNamePropsInfo = reg.getString("fileNameProperties"); 
				//this.session.getRegistry().getString("fileNameProperties");
		String[] fileNameProps = fileNamePropsInfo.split("-");
		try {
			String[] fileNamePropsVals = this.packComponentRevision.getProperties(fileNameProps);
		
			for (String aValue : fileNamePropsVals) {
				if (name == null) {
					name = aValue;
				} else {
					name += "_" + aValue;
				}
			}
			System.out.println("<<PCR::DBG>> initial file name: \"" + name + "\"");
			name = replaceSpecialChars(name);
			System.out.println("<<PCR::DBG>> file name after special chars handling: \"" + name + "\"");
			name = System.getProperty("java.io.tmpdir") + name + "_" + getCurrentTimeStamp() + ".pdf";
		} catch (TCException tcExc) {
			System.out.println("<<PCR::DBG>> Error in getting file name properties...!\n" +
					tcExc.getMessage());
			name = System.getProperty("java.io.tmpdir") + getCurrentTimeStamp() + "_tempPCR.pdf";
		}
		return name;
	}

	private String replaceSpecialChars(String name) {
		//String specialChars = "/\\$!@#\"\'";
		String specialChars = reg.getString("fileNameSpecialCharacters");
		System.out.println("<<PCR::DBG>> special characters to be replaced in file name:");
		System.out.println("<<PCR::DBG>> " + specialChars);
		for (int inx = 0; inx < specialChars.length(); inx++) {
			if (name.indexOf(specialChars.charAt(inx)) != -1) {
				name = name.replace(specialChars.charAt(inx), '_');
			}
		}
		return name;
	}

	private String getCurrentTimeStamp() 
	{
	    SimpleDateFormat sdfDate = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss");
	    return sdfDate.format(new Date());
	}
	
	private String getCurrentTimeStamp(String format) 
	{
	    SimpleDateFormat sdfDate = new SimpleDateFormat(format);
	    return sdfDate.format(new Date());
	}

	private boolean generatePackComponentReport(String reportFileName) throws FileNotFoundException {
		System.out.println("<<PCR::DBG>> Entered generatePackComponentReport ...");
		this.session.setStatus("Building PDF report Header.......");
		System.out.println("Building PDF report Header.......");
		
		PdfDocument pdfDoc = new PdfDocument(new PdfWriter(reportFileName).setSmartMode(true));
		this.finalDoc = new Document(pdfDoc, PageSize.A4);
		
		// instantiate handler that adds header at the defined event
		String headerTimeFormat = reg.getString("headerTimeStampFormat");
		UL4PcrTableHandler handler = new UL4PcrTableHandler(finalDoc, this.packComponentRevision, this.session, getCurrentTimeStamp(headerTimeFormat));
		
		// define page end as the defined event
		pdfDoc.addEventHandler(PdfDocumentEvent.END_PAGE, handler);
		
		// write below the header area
		this.finalDoc.setMargins(40 + handler.getHeightOfHeaderTable(), 36, 36+50, 36);
		
		// add report content
		boolean result = processReportBody();
		
		this.finalDoc.close();
		this.session.setReadyStatus();
		System.out.println("<<PCR::DBG>> Exiting generatePackComponentReport ...");
		return result;		
	}

	private boolean processReportBody() {
		boolean result = true;
		this.session.setStatus("Building PDF report body.......");
		System.out.println("<<PCR::DBG>> Building PDF report body.......");
		TCPreferenceService prefServ = session.getPreferenceService();
		String[] prefValues = prefServ.getStringValues(PREF_PCR_BODY);
		try {
			this.bold = PdfFontFactory.createFont(FontConstants.TIMES_BOLD);
			this.regular = PdfFontFactory.createFont(FontConstants.TIMES_ROMAN);
			this.lavender = new DeviceRgb(230, 230, 255);
			for (String sectionPref : prefValues) {
				result = processReportSection(sectionPref);
				if (!result) {
					break;
				}
			}
			System.out.println("<<PCR::DBG>> Building PDF report body is complete ...");
			
		} catch (IOException ioExc) {
			ioExc.printStackTrace();
			System.out.println("<<PCR::DBG>> Error while processing a section");
		}
		return result;
	}

	private boolean processReportSection(String sectionPref) {
		boolean result = true;
		TCPreferenceService prefServ = session.getPreferenceService();
		String[] prefSectionValues = prefServ.getStringValues(sectionPref);
		System.out.println("<<PCR::DBG>> Processing " + sectionPref + " section ...");
		if (prefSectionValues != null && prefSectionValues.length > 0) {
			if (prefSectionValues[0].endsWith("~horizontal")) {
				processAsHorizontalTable(prefSectionValues);
			} else if (prefSectionValues[0].endsWith("~vertical")) {
				result = processAsVerticalTable(prefSectionValues);
			} else if (prefSectionValues[0].endsWith("~Preference")) {
				processAsPerConfiguration(prefSectionValues);
			}
		}
		System.out.println("<<PCR::DBG>> Processing " + sectionPref + " section is complete.");
		return result;
	}

	private void processAsPerConfiguration(String[] configurations) {
		for (String aConfiguration : configurations) {
			System.out.println("<<PCR::DBG>> processing " + aConfiguration);
			if (aConfiguration.endsWith("~Preference")) {
				// split the value by '~'. For ex, value will be as below
				// Compliance Rules~U4_ComplianceRuleRelation_PAM_Table_Configuration~Preference
				String[] configTokens = aConfiguration.split("~");
				//String section = configTokens[0];
				String section = null;
				String subsection = null;
				String[] sectionToks = configTokens[0].split(COLON);
				section = sectionToks[0];
				if (sectionToks.length > 1) {
					subsection = sectionToks[1];
				}
				String preferenceName = configTokens[1];
				System.out.println("<<PCR::DBG>> Preference name is " + preferenceName);
				boolean isArrayData = false;
				if (configTokens.length >= 4) {
					// 3rd token describes whether it is array data
					if (configTokens[2] != null && !configTokens[2].isEmpty() &&
							configTokens[2].equalsIgnoreCase("arraydata:true")) {
						isArrayData = true;
					}
				}
				// check whether it will be displayed for the component
				boolean isVisible = true;
				if (configTokens.length >= 5) {
					// 4th token describes whether it is visible on frames if applicable
					// example: visible:G-CLOSURE-PLAS,G-INJECTION,G-LID-HOOD-PLSTC,G-THERMOFORMS,G-TUB-ETC-PLASTIC
					if (configTokens[3] != null && !configTokens[3].isEmpty()) {
						isVisible = isVisibleForComponent(configTokens[3]);
					}
				}
				if (!isVisible) {
					System.out.println("<<PCR::DBG>> This configuration not applicable for this component...!");
					continue;
				}
				Set<String> excludeForms = null;
				if (configTokens.length >= 5) {
					// 5th token describes whether it is visible on frames if applicable
					// example: exclude_forms:U4_LabelPropertiesForm
					if (configTokens[4] != null && !configTokens[4].isEmpty()) {
						excludeForms = readExcludeFormTypes(configTokens[4]);
					}
				}
					String relationName = null;
					// relation name is part of preference name
					// For ex, In U4_ComplianceRuleRelation_PAM_Table_Configuration preference
					// relation name is U4_ComplianceRuleRelation
					relationName = getRelationNameInPreference(preferenceName);
					System.out.println("<<PCR::DBG>> Relation name is " + relationName);
					// read and load properties rendering details from configuration
					List<UL4PcrPackTableConfiguration> packTableConfigs = null;
					packTableConfigs = readPackTableConfigurations(preferenceName);
					
					// write section header before table
					Table sectionHeaderTable = null;
					if (!section.equalsIgnoreCase("NA")) {
						// prepare section header table
						sectionHeaderTable = new Table(1);
						sectionHeaderTable.addCell(new Cell()
							.setFont(this.bold)
							.setFontSize(12)
							.setPadding(0)
							.setBackgroundColor(this.lavender)
					        .add(section)
					        .setBorder(Border.NO_BORDER));
						this.finalDoc.add(sectionHeaderTable);
						this.finalDoc.add(new Paragraph("\n"));
					}
					
					// get objects
					List<TCComponent> relatedObjsOfAllTypes = getRelatedObjsOfAllTypes(relationName);
					
					// process objects
					Map<String, List<TCComponent>> relatedObjsOfRequiredTypes = null;
					UL4PcrPackTableConfiguration packTabConf = null;
					
					// 
					List<UL4PcrPackTableObject> packTabObjs = null;
					UL4PcrPackTableObject[] packTabObjsArraySorted = null;
					//for (TCComponent object : relatedObjsOfAllTypes) {
						//String formType = object.getType();
					relatedObjsOfRequiredTypes = filterToGetRequiredObjects(packTableConfigs, relatedObjsOfAllTypes);
					System.out.println("<<PCR::DBG>> # of types of related objs: " + relatedObjsOfRequiredTypes.size());
					if (relatedObjsOfRequiredTypes.size() > 0) {
						int iRelObjsCount = relatedObjsOfRequiredTypes.size();
						for (Map.Entry<String, List<TCComponent>> anEntry : 
							relatedObjsOfRequiredTypes.entrySet()) {
							String type = anEntry.getKey();
							System.out.println("<<PCR::DBG>> Getting applicable config for type " + type);
							if (excludeForms == null || !excludeForms.contains(type)) {
								packTabConf = getApplicablePackTableConfiguration(packTableConfigs, type);
								this.emptyObj = null;
								if (packTabConf != null) {
									System.out.println("<<PCR::DBG>> Reading properties from these type of objs ...");
									packTabObjs = readPropertiesOfSimilarObjects(anEntry.getValue(), packTabConf, isArrayData);
									if (iRelObjsCount > 1) {
										subsection = anEntry.getValue().get(0).getDisplayType();
									}
									packTabObjsArraySorted = sortPackTableObjects(packTabObjs, packTabConf);
								} else {
									System.out.println("<<PCR::DBG>> Applicable config for type " + type +
											" is not found...!");
								}
								if (packTabObjsArraySorted != null) {
									System.out.println("<<PCR::DBG>> Writing objects info to the report ...");
									writeObjectsInfoToReport(packTabObjsArraySorted, isArrayData, subsection);
								} else if (this.emptyObj != null) {
									writeEmptyObjectsInfoToReport(emptyObj, subsection);
								}
							} else {
								System.out.println("<<PCR::DBG>> Excluding forms of this type of objects...!");
							}
						}
					}
			} else if (aConfiguration.endsWith("~Table")) {
				processAsTable(aConfiguration);
			}
		}
	}
	
	private Set<String> readExcludeFormTypes(String typeInfo) {
		Set<String> types = new HashSet<>();
		String[] typeDataToks = typeInfo.split(COLON);
		String[] typeToks = typeDataToks[1].split(COMMA);
		for (String type : typeToks) {
			if (type != null && !type.isEmpty())
				types.add(type.trim());
		}
		return types;
	}

	private boolean isVisibleForComponent(String visibleInfo) {
		boolean isVisible = false;
		// get material classification form
		try {
			TCComponent mcForm = this.packComponentRevision.getRelatedComponent("U4_MatlClassRelation");
			try {
				String frameType = null;
				frameType = mcForm.getPropertyDisplayableValue("u4_pam_frame_type");
				String visibleToks[] = null;
				visibleToks = visibleInfo.split(COLON);
				String[] frames = null;
				frames = visibleToks[1].split(COMMA);
				for (int inx = 0; inx < frames.length; inx++) {
					if (frames[inx].equalsIgnoreCase(frameType)) {
						isVisible = true;
						break;
					}
				}
			} catch (NotLoadedException nle) {
				nle.printStackTrace();
			}
		} catch (TCException tcExc) {
			tcExc.printStackTrace();
		}
		return isVisible;
	}

	private void processAsTable(String tablePropInfo) {
		String[] tokens = tablePropInfo.split(TILDA);
		String section = tokens[0];
		
		String[] sourceToks = tokens[1].split(COLON);
		String srcObjPropName = sourceToks[0];
		String propRealName = sourceToks[1];
		
		if (!section.equals("NA")) {
			Table sectionHeaderTable = null;
			// prepare section header table
			sectionHeaderTable = new Table(1);
			sectionHeaderTable.addCell(new Cell()
				.setFont(this.bold)
				.setFontSize(12)
				.setPadding(0)
				.setBackgroundColor(this.lavender)
		        .add(section)
		        .setBorder(Border.NO_BORDER));
			this.finalDoc.add(sectionHeaderTable);
			this.finalDoc.add(new Paragraph("\n"));
		}
		
		TCComponent srcObj = null;
		TCProperty objProp = null;
		TCComponent[] objs = null;
		try {
			objProp = this.packComponentRevision.getTCProperty(srcObjPropName);
			if (objProp != null) {
				if (objProp.isNotArray()) {
					srcObj = objProp.getReferenceValue();
				} else {
					objs = objProp.getReferenceValueArray();
					if (objs != null && objs.length > 0) {
						srcObj = objs[0];
					}
				}
			}
			if (srcObj != null) {
				
				String propValue = null;
				objProp = srcObj.getTCProperty(propRealName);
				propValue = objProp.getDisplayableValue();
				if (propValue == null) propValue = " ";
				Table sectionPropsTable = new Table(new float[]{261,262});
				sectionPropsTable.addCell(new Cell()
				.setFontSize(8)
				.setFont(this.bold)
				.add(objProp.getPropertyDisplayName())
				.setPadding(0));
				sectionPropsTable.addCell(propValue)
				.setPadding(0)
				.setFontSize(8)
				.setFont(this.regular);
				this.finalDoc.add(sectionPropsTable);
				this.finalDoc.add(new Paragraph("\n"));
			}
		} catch (TCException tcExc) {
			tcExc.printStackTrace();
		}
		
	}

	private void writeObjectsInfoToReport(
			UL4PcrPackTableObject[] packTableObjs, boolean isArrayData,
			String subsection) {
		if (packTableObjs.length > 0) {
			if (subsection != null && !subsection.isEmpty()) {
				this.finalDoc.add(new Paragraph(subsection).setFontSize(10));
			}
			Table sectionPropsTable = new Table(packTableObjs[0].getColSizes());
			sectionPropsTable.setWidth(529);
			// write columns first as it is horizontal
			for (TCProperty aProp : packTableObjs[0].getTcProps()) {
				sectionPropsTable.addHeaderCell(new Cell().add(aProp.getPropertyDisplayName())
				.setFont(this.bold))
				.setPadding(0)
				.setFontSize(8);
			}
			
			// write values
			String displayableValue = null;
			TCProperty[] props = null;
			if (!isArrayData) {
				for (UL4PcrPackTableObject aPackRow : packTableObjs) {
					if (aPackRow != null) props = aPackRow.getTcProps();
					if (props != null && props.length > 0) {
						for (TCProperty aProp : props) {
							displayableValue = aProp.getDisplayableValue();
							if (displayableValue == null)
								displayableValue = " ";
							sectionPropsTable.addCell(displayableValue)
							.setPadding(0)
							.setFontSize(8)
							.setFont(this.regular);
							displayableValue = null;
						}
					}
				}	
			} else {
				// to handle environmental calculations table
				if (packTableObjs.length > 0) {
					UL4PcrPackTableObject aPackRow = packTableObjs[0];
					props = aPackRow.getTcProps();
					if (props != null && props.length > 0) {
						int iPropCount = props.length;
						int iArrayValuesCount = 0;
						String temp1stValue = props[0].getDisplayableValue();
						if (temp1stValue != null && !temp1stValue.isEmpty()) {
							iArrayValuesCount = temp1stValue.split(",").length;
						}
						String[] propValToks = null;
						String finalValue = " ";
						for (int iPropValInx = 0; iPropValInx < iArrayValuesCount; iPropValInx++) {
							for (int iPropInx = 0; iPropInx < iPropCount; iPropInx++) {
								displayableValue = props[iPropInx].getDisplayableValue();
								if (displayableValue == null || displayableValue.isEmpty())
									displayableValue = " ";
								propValToks = displayableValue.split(",");
								if (iPropValInx < propValToks.length && 
										propValToks[iPropValInx] != null) {
									finalValue = propValToks[iPropValInx];
								}
								sectionPropsTable.addCell(finalValue)
								.setPadding(0)
								.setFontSize(8)
								.setFont(this.regular);
								displayableValue = null;
							} // end of inner for lop
						} // end of outer for loop
					} // end of props check
				} // end of packTableObjs check
			}
			this.finalDoc.add(sectionPropsTable);
			this.finalDoc.add(new Paragraph("\n"));
		}
	}
	
	private void writeEmptyObjectsInfoToReport(
			UL4PcrPackTableObject emptyObj, String subsection) {
		if (subsection != null && !subsection.isEmpty()) {
			this.finalDoc.add(new Paragraph(subsection).setFontSize(10));
		}
		Table sectionPropsTable = new Table(emptyObj.getColSizes());
		sectionPropsTable.setWidth(529);
		// write columns first as it is horizontal
		for (TCProperty aProp : emptyObj.getTcProps()) {
			sectionPropsTable.addHeaderCell(new Cell().add(aProp.getPropertyDisplayName())
			.setFont(this.bold))
			.setPadding(0)
			.setFontSize(8);
		}
		
		this.finalDoc.add(sectionPropsTable);
		this.finalDoc.add(new Paragraph("\n"));
	}

	private UL4PcrPackTableObject[] sortPackTableObjects(List<UL4PcrPackTableObject> packTabObjsList,
			UL4PcrPackTableConfiguration tabConf) {
		boolean isApplied = false;
		UL4PcrPackTableObject[] tabObjs = null;
		int objCount = packTabObjsList.size();
		
		if (objCount > 0) {
			tabObjs = new UL4PcrPackTableObject[objCount];
			int iny = 0;
			UL4PcrRowConfiguration rowConf = null;
			rowConf = tabConf.getRowConfiguration();
			if (rowConf != null) {
				String sequencesInOrder[] = rowConf.getPropertyValues();
				if (sequencesInOrder != null && sequencesInOrder.length > 0) {
					System.out.println("<<PCR::DBG>> Sorting the objects based on sequence no ...");
					for (int inx = 0; inx < sequencesInOrder.length; inx++) {
						for (UL4PcrPackTableObject aTabObj : packTabObjsList) {
							if (sequencesInOrder[inx].equals(aTabObj.getSequence())) {
								tabObjs[iny] = aTabObj;
								++iny;
								break;
							}
						}
					} // end of outer for loop
					isApplied = true; 
				}
			} // end of row configuration check
			if (!isApplied) {
				for (UL4PcrPackTableObject aTabObj : packTabObjsList) {
					tabObjs[iny] = aTabObj;
					++iny;
				}
			}
		}
		return tabObjs;
	}
	
	private List<UL4PcrPackTableObject> readPropertiesOfSimilarObjects(
			List<TCComponent> relatedObjsOfType,
			UL4PcrPackTableConfiguration packTabConf,
			boolean isArrayData) {
		List<UL4PcrPackTableObject> tabObjs = new ArrayList<>();
		UL4PcrColumnConfiguration[] colsConf = packTabConf.getColumnConfiguration();
		int propsCount = colsConf.length;
		String propertyNames[] = new String[propsCount];
		float colSizes[] = new float[propsCount];
		// get property names and column sizes into array
		for (int inx = 0; inx < propsCount; inx++) {
			propertyNames[inx] = colsConf[inx].getPropertyRealName();
			colSizes[inx] = colsConf[inx].getColumnSize();
		}
		TCProperty[] tcProps = null;
		UL4PcrPackTableObject aTabObj = null;
		String comparatorPropName = packTabConf.getRowConfiguration().getComparatorProperty();
		String comparatorPropValue = null;
		boolean isEmpty = false;
		this.emptyObj = null;
		for (TCComponent aComponent : relatedObjsOfType) {
			// read properties
			aTabObj = new UL4PcrPackTableObject();
			try {
				tcProps = aComponent.getTCProperties(propertyNames);
				isEmpty = isRowEmpty(colsConf, tcProps);
				comparatorPropValue = aComponent.getProperty(comparatorPropName);
				aTabObj.setTcProps(tcProps);
				aTabObj.setColSizes(colSizes);
				aTabObj.setSequence(comparatorPropValue);
			} catch (TCException exc) {
				exc.printStackTrace();
				aTabObj = null;
			}
			if (aTabObj != null && (!isEmpty || isArrayData)) {
				tabObjs.add(aTabObj);
			} else if (aTabObj != null && isEmpty) {
				this.emptyObj = aTabObj;
			}
		}
		return tabObjs;
	}

	private boolean isRowEmpty(UL4PcrColumnConfiguration[] colsConf,
			TCProperty[] tcProps) {
		boolean isEmpty = true;
		String value = null;
		for (int iColInx = 0; iColInx < colsConf.length; iColInx++) {
			if (colsConf[iColInx].isEnabled()) {
				value = tcProps[iColInx].getDisplayValue();
				if (value != null && !value.isEmpty()) {
					isEmpty = false;
					break;
				}
			}
			value = null;
		}
		return isEmpty;
	}

	private UL4PcrPackTableConfiguration getApplicablePackTableConfiguration(
			List<UL4PcrPackTableConfiguration> packTableConfigs, String secType) {
		UL4PcrPackTableConfiguration packConf = null;
		UL4PcrPackTableConfiguration genericPackConf = null;
		String selectedObjType = null;
		boolean isFound = false;
		selectedObjType = this.packComponentRevision.getType();
		for (UL4PcrPackTableConfiguration aPackTabConf : packTableConfigs) {
			if (aPackTabConf.getPrimaryObjectType() != null) {
				if (selectedObjType.equals(aPackTabConf.getPrimaryObjectType()) &&
						secType.equals(aPackTabConf.getSecondaryObjectType())) {
					packConf = aPackTabConf;
					isFound = true;
					System.out.println("<<PCR::DBG>> Found the only available specific configuration...");
					// exit loop if specific configuration is found
					break;
				} else if(aPackTabConf.getPrimaryObjectType().equalsIgnoreCase("All") &&
						secType.equals(aPackTabConf.getSecondaryObjectType())) {
					genericPackConf = aPackTabConf;
					isFound = false;
					System.out.println("<<PCR::DBG>> Found generic configuration...");
				}
			} else if (aPackTabConf.getPrimaryObjectTypes() != null) {
				Set<String> prims = aPackTabConf.getPrimaryObjectTypes();
				if (prims.size() > 0 && prims.contains(selectedObjType)) {
					if (secType.equals(aPackTabConf.getSecondaryObjectType())) {
						packConf = aPackTabConf;
						isFound = true;
						System.out.println("<<PCR::DBG>> Found specific configuration...");
						// exit loop if specific configuration is found
						break;
					}
				} else if (prims.size() > 0 && prims.contains("All")) {
					if (secType.equals(aPackTabConf.getSecondaryObjectType())) {
						genericPackConf = aPackTabConf;
						isFound = false;
						System.out.println("<<PCR::DBG>> Found generic configuration...");
					}
				}
			}
		}
		if (!isFound) {
			packConf = genericPackConf;
			System.out.println("<<PCR::DBG>> Could not find specific configuration...");
		}
		return packConf;
	}

	private Map<String, List<TCComponent>> filterToGetRequiredObjects(
			List<UL4PcrPackTableConfiguration> packTableConfigs,
			List<TCComponent> relatedObjsOfAllTypes) {
		Map<String, List<TCComponent>> objs = new TreeMap<>();
		String requiredType = null;
		String objType = null;
		for (TCComponent anObj : relatedObjsOfAllTypes) {
			objType = anObj.getType();
			for (UL4PcrPackTableConfiguration eachConf : packTableConfigs) {
				requiredType = eachConf.getSecondaryObjectType();
				if (objType.equals(requiredType)) {
					if (objs.containsKey(objType)) {
						objs.get(objType).add(anObj);
					} else {
						objs.put(objType, (new ArrayList<TCComponent>()));
						objs.get(objType).add(anObj);
					}
					objType = null;
					break;
				}
			}
		}
		return objs;
	}

	private List<UL4PcrPackTableConfiguration> readPackTableConfigurations(
			String preferenceName) {
		System.out.println("<<PCR::DBG>> Reading all pack table configurations from below preference...");
		System.out.println("<<PCR::DBG>> Preference name: " + preferenceName);
		List<UL4PcrPackTableConfiguration> configurations = new ArrayList<>();
		TCPreferenceService prefServ = session.getPreferenceService();
		String[] prefValues = prefServ.getStringValues(preferenceName);
		UL4PcrPackTableConfiguration aConfig = null;
		Set<String> tempPrimObjTypes = null;
		for (int iPrefValInx = 0; iPrefValInx < prefValues.length; iPrefValInx = iPrefValInx + 4) {
			aConfig = new UL4PcrPackTableConfiguration();
			
			// update primary object types. Below is the example value
			//PrimaryObject~U4_ClsrPDTThrdRevision,U4_ClsrPDTSnapOnRevision,U4_ClosureMetalRevision
			String[] tokens = prefValues[iPrefValInx].split(TILDA);
			String objTypeInfo = tokens[1];
			if (objTypeInfo.indexOf(',') != -1) {
				String[] primObjTypes = objTypeInfo.split(",");
				tempPrimObjTypes = new HashSet<>(Arrays.asList(primObjTypes));
				aConfig.setPrimaryObjectTypes(tempPrimObjTypes);
			} else {
				aConfig.setPrimaryObjectType(objTypeInfo);
			}
			tokens = null;
			
			// update secondary object type
			tokens = prefValues[iPrefValInx + 1].split(TILDA);
			objTypeInfo = tokens[1];
			aConfig.setSecondaryObjectType(objTypeInfo);
			tokens = null;
			
			// update column configuration
			tokens = prefValues[iPrefValInx + 2].split(TILDA);
			String colsConfigInfo = tokens[2];
			UL4PcrColumnConfiguration[] colsConfig = readColumnsConfiguration(colsConfigInfo);
			aConfig.setColumnConfiguration(colsConfig);
			tokens = null;
			
			// update row configuration
			tokens = prefValues[iPrefValInx + 3].split(TILDA);
			String rowConfigInfo = tokens[2];
			UL4PcrRowConfiguration rowConfig = readRowConfiguration(rowConfigInfo);
			if (rowConfig != null) {
				aConfig.setRowConfiguration(rowConfig);
			}
			
			configurations.add(aConfig);
		}
		System.out.println("<<PCR::DBG>> # of pack table configurations loaded: " + configurations.size());
		return configurations;
	}

	private UL4PcrRowConfiguration readRowConfiguration(String rowConfigInfo) {
		UL4PcrRowConfiguration rowConf = new UL4PcrRowConfiguration();
		String[] rowConfToks = rowConfigInfo.split(COLON);
		rowConf.setComparatorProperty(rowConfToks[0]);
		String[] values = null;
		if (rowConfToks != null && rowConfToks.length > 1 && rowConfToks[1].indexOf(COMMA) != -1) {
			values = rowConfToks[1].split(COMMA);
		}
		rowConf.setPropertyValues(values);
		return rowConf;
	}

	private UL4PcrColumnConfiguration[] readColumnsConfiguration(
			String colsConfigInfo) {
		
		String[] colsInfo = colsConfigInfo.split(COMMA);
		String[] colData = null;
		UL4PcrColumnConfiguration[] configs = new UL4PcrColumnConfiguration[colsInfo.length];
		for (int inx = 0; inx < colsInfo.length; inx++) {
			configs[inx] = new UL4PcrColumnConfiguration();
			colData = colsInfo[inx].split(COLON);
			// update column details
			configs[inx].setPropertyRealName(colData[0]);
			configs[inx].setColumnSize(Integer.parseInt(colData[1]));
			boolean isEnabled = false;
			if (colData[2] != null && colData[2].equalsIgnoreCase("Enable")) {
				isEnabled = true;
			}
			configs[inx].setEnabled(isEnabled);
			boolean isStructured = false;
			if (colData.length >=4 && colData[3] != null && colData[3].equalsIgnoreCase("Struct")) {
				isStructured = true;
			}
			configs[inx].setStructured(isStructured);
		}
		return configs;
	}

	private List<TCComponent> getRelatedObjsOfAllTypes(String relationName) {
		List<TCComponent> objects = new ArrayList<>();
		try {
			//TCComponent[] components = this.packComponentRevision.getRelatedComponents(relationName);
			AIFComponentContext[] compContexts = this.packComponentRevision.getSecondary();
			TCComponent component = null;
			for (AIFComponentContext context : compContexts) {
				component = (TCComponent) context.getComponent();
				objects.add(component);
			}
		} catch (TCException tcExc) {
			tcExc.printStackTrace();
		}
		return objects;
	}

	private String getRelationNameInPreference(String preferenceName) {
		String relationName = null;
		// 1st 3 chars are part of prefix including the '_'. Here prefix is "U4_".
		// Next underscore comes at the end of relation name as per configuration
		int posOf2ndUnderScore = preferenceName.indexOf('_', 3);
		relationName = preferenceName.substring(0, posOf2ndUnderScore);
		return relationName;
	}

	private boolean processAsVerticalTable(String[] sectionPropsInfo) {
		System.out.println("<<PCR::DBG>> Processing vertical table ...");
		boolean result = true;
		String section = null;
		String sourceObjectInfo = null;
		System.out.println("<<PCR::DBG>> section info: " + sectionPropsInfo[0]);
		// get section name and source info from 1st value of preference
		if (sectionPropsInfo[0].indexOf("~") != -1) {
			String[] tokens = sectionPropsInfo[0].split("~");
			section = tokens[0];
			sourceObjectInfo = tokens[1];
		} else {
			section = sectionPropsInfo[0];
		}
		Map<String, String> field2PropNames = new LinkedHashMap<>();
		for (int iPropInx = 1; iPropInx < sectionPropsInfo.length; iPropInx++) {
			String[] propTokens = sectionPropsInfo[iPropInx].split("=");
			field2PropNames.put(propTokens[0], propTokens[1]);
		}
		// read properties values from TC
		String[] propNames = field2PropNames.values().toArray(new String[0]);
		Table sectionHeaderTable = null;
		// prepare section header table
		sectionHeaderTable = new Table(1);
		sectionHeaderTable.addCell(new Cell()
			.setFont(this.bold)
			.setFontSize(12)
			.setPadding(0)
			.setBackgroundColor(this.lavender)
	        .add(section)
	        .setBorder(Border.NO_BORDER));
		// prepare properties table as vertical
		Table sectionPropsTable = new Table(new float[]{261,262});
		Map<String, String> propNameValues = null;
		if (sourceObjectInfo == null || sourceObjectInfo.equalsIgnoreCase("NA")) {
			propNameValues = readPropertiesFromTC(propNames);
		} else {
			List<TCComponent> reqObjs = null;
			reqObjs = getRequiredObjects(sourceObjectInfo);
			TCComponent reqObj = null;
			if (reqObjs != null && reqObjs.size() > 0)
				reqObj = reqObjs.iterator().next();
			if (reqObj != null) {
				propNameValues = readPropertiesFromTC(reqObj, propNames);
			}
			else
			{
				System.out.println("<<PCR::DBG>> Source object not found ...!");
				setErrorMessage(sourceObjectInfo);
				result = false;
			}
		}
		// update properties table
		String tempValue = null;
		
		for (Map.Entry<String, String> entry : field2PropNames.entrySet()) {
			
			
			sectionPropsTable.addCell(new Cell()
			.setFontSize(8)
			.setFont(this.bold)
			.add(entry.getKey())
			.setPadding(0));
			
			if (propNameValues != null) {
				tempValue = propNameValues.get(entry.getValue());
			}
			if (tempValue == null) tempValue = "";
			sectionPropsTable.addCell(tempValue)
			.setPadding(0)
			.setFontSize(8)
			.setFont(this.regular);
		}
	
		this.finalDoc.add(sectionHeaderTable);
		this.finalDoc.add(new Paragraph("\n"));
		this.finalDoc.add(sectionPropsTable);
		this.finalDoc.add(new Paragraph("\n"));
		System.out.println("<<PCR::DBG>> Processing vertical table complete...");
		return result;
	}

	private void setErrorMessage(String sourceObjectInfo) {
		String[] tokens = sourceObjectInfo.split(COLON);
		String typesMissing = null;
		TCComponentType tcType = null;
		if (tokens.length >= 3) {
			String typeInfo = tokens[2];
			if (typeInfo.indexOf(COLON) != -1) {
				String[] types = typeInfo.split(COLON);
				for (int inx = 0; inx < types.length; inx++) {
					if (inx == 0) {
						typesMissing = types[inx];
					} else {
						typesMissing += ("," + types[inx]);
					}
				}
			} else {
				try {
					tcType = this.session.getTypeComponent(typeInfo);
					if (tcType != null) {
						typesMissing = tcType.getDisplayTypeName();
					}
				} catch (TCException tcExc) {
					tcExc.printStackTrace();
				}
				
			}
		}
		this.errorMessage = "Unable to generate Pack Component report as " + 
		typesMissing + " form(s) is/are missing under the selected component! " +
				"Please contact System Administrator.";
	}

	private void processAsHorizontalTable(String[] sectionPropsInfo) {
		String section = null;
		String sourceObjectInfo = null;
		System.out.println("<<PCR::DBG>> Processing horizontal table ...");
		// get section name and source info from 1st value of preference
		section = sectionPropsInfo[0].substring(0, sectionPropsInfo[0].indexOf("~"));
		sourceObjectInfo = sectionPropsInfo[0].substring(sectionPropsInfo[0].indexOf("~")+1,
				sectionPropsInfo[0].lastIndexOf("~"));
		
		// get properties to read
		float[] tableCols = new float[sectionPropsInfo.length-1];
		Map<String, String> field2PropNames = new LinkedHashMap<>();
		for (int iPropInx = 1; iPropInx < sectionPropsInfo.length; iPropInx++) {
			String[] propTokens = sectionPropsInfo[iPropInx].split(",");
			// store column width
			tableCols[iPropInx-1] = Integer.parseInt(propTokens[1]);
			field2PropNames.put(propTokens[0], propTokens[2]);
		}
		List<TCComponent> reqObjs = null;
		if (sourceObjectInfo != null) {
			reqObjs = getRequiredObjects(sourceObjectInfo);
		}
		
		// write table title (section name) to report
		if (!section.equals("NA")) {
			Table sectionHeaderTable = null;
		//if (reqObjs == null || (reqObjs.size() > 0)) {
			sectionHeaderTable = new Table(1);
			sectionHeaderTable.addCell(new Cell()
			.setFont(this.bold)
			.setFontSize(12)
			.setPadding(0)
			.setBackgroundColor(this.lavender)
	        .add(section)
	        .setBorder(Border.NO_BORDER));
		
			this.finalDoc.add(sectionHeaderTable);
			this.finalDoc.add(new Paragraph("\n"));
		//}
		}
		
		// write properties table to report
		Table sectionPropsTable = new Table(tableCols);
		sectionPropsTable.setWidth(524);
		sectionPropsTable.setFixedLayout();
		
		// write columns first as it is horizontal
		for (Map.Entry<String, String> entry : field2PropNames.entrySet()) {
			sectionPropsTable.addHeaderCell(new Cell().add(entry.getKey())
			.setFont(this.bold))
			.setPadding(0)
			.setFontSize(8);
		}
		// read properties values from TC
		String[] propNames = field2PropNames.values().toArray(new String[0]);
		Map<String, String> propNameValues = null;
		if (reqObjs == null) {
			propNameValues = readPropertiesFromTC(propNames);
			// write values
			for (Map.Entry<String, String> entry : field2PropNames.entrySet()) {
				sectionPropsTable.addCell(propNameValues.get(entry.getValue()))
				.setPadding(0)
				.setFontSize(8)
				.setFont(this.regular);
			}
		} else if(reqObjs.size() > 0){
			for (TCComponent eachComp : reqObjs) {
				propNameValues = readPropertiesFromTC(eachComp, propNames);
				// write values
				for (Map.Entry<String, String> entry : field2PropNames.entrySet()) {
					sectionPropsTable.addCell(propNameValues.get(entry.getValue()))
					.setPadding(0)
					.setFontSize(8)
					.setFont(this.regular);
				}
			}
		}
		this.finalDoc.add(sectionPropsTable);
		this.finalDoc.add(new Paragraph("\n"));
		System.out.println("<<PCR::DBG>> Processing horizontal table complete ...");
	}

	private Map<String, String> readPropertiesFromTC(TCComponent eachComp,
			String[] propNames) {
		Map<String, String> propNameValues = new HashMap<>();
		try {
			String[] propValues = eachComp.getProperties(propNames);
			for (int inx = 0; inx < propNames.length; inx++)
				propNameValues.put(propNames[inx], propValues[inx]);
		} catch (TCException exce) {
			System.out.println("<<PCR::DBG>> Error in getting revision properties...!");
			System.out.println(exce.getMessage());
		}
		return propNameValues;
	}

	private List<TCComponent> getRequiredObjects(String sourceObjectInfo) {
		String propNamesInfo = null;
		String[] reqTypes = null;
		String comparatorPropInfo = null;
		List<TCComponent> requiredObjects = new ArrayList<>();
		System.out.println("<<PCR::DBG>> source info: " + sourceObjectInfo);
		String[] sourceToks = sourceObjectInfo.split(COLON);
		propNamesInfo = sourceToks[0];
		
		if (sourceToks.length > 1) {
			comparatorPropInfo = sourceToks[1];
		}
		
		if (sourceToks.length > 2) {
			reqTypes = sourceToks[2].split(COMMA);
		}
		
		String[] compPropToks = null;
		compPropToks = comparatorPropInfo.split(EQUALTO);
		String comparatorPropertyName = compPropToks[0];
		// By default, ascending order is applied
		String sortOrder = "ASC";
		if (compPropToks.length > 1) {
			sortOrder = compPropToks[1];
		}
		int added = 0;
		// if no comma(,) exist, only 1 property
		if (propNamesInfo.indexOf(COMMA) == -1) {
			added = getRequiredFromProperty(propNamesInfo, reqTypes, requiredObjects);
		} else {
			String propertyNames[] = propNamesInfo.split(COMMA);
			for (String propertyName : propertyNames) {
				added += getRequiredFromProperty(propertyName, reqTypes, requiredObjects);
			}
		}
		// sort objects if applicable
		if (comparatorPropertyName != null && !comparatorPropertyName.equals("NA") &&
				requiredObjects != null && requiredObjects.size() > 1) {
			Collections.sort(requiredObjects, new UL4PcrTcObjectComparator(comparatorPropertyName, sortOrder));
		}
		
		System.out.println("<<PCR::DBG>> # of related required objs: " + added);
		
		return requiredObjects;
	}

	private int getRequiredFromProperty(String propertyName, String[] types, List<TCComponent> objects) {

		int count = 0;
		try {
			TCProperty propObj = this.packComponentRevision.getTCProperty(propertyName);
			TCComponent obj = null;
			if (propObj.isNotArray()) {
				obj = propObj.getReferenceValue();
				if (types != null) {
					if (obj != null && isRequiredType(obj, types)) {
						objects.add(obj);
						++count;
					}
				} else {
					if (obj != null) {
						objects.add(obj);
						++count;
					}
				}
			} else {
				TCComponent[] objs = null;
				objs = propObj.getReferenceValueArray();
				if (objs != null && objs.length > 0) {
					if (types != null) {
						for (TCComponent anObj : objs) {
							if (anObj != null && isRequiredType(anObj, types)) {
								objects.add(anObj);
								++count;
							}
						} // end of for loop
					} else {
						objects.addAll(Arrays.asList(objs));
						count = objs.length;
					}
				}
			}
		} catch (TCException tcExc) {
			System.out.println("<<PCR::DBG>> Error in getting objects...!");
			System.out.println(tcExc.getMessage());
		}
		return count;
	}

	private boolean isRequiredType(TCComponent obj, String[] types) {
		boolean isFound = false;
		for (String aType : types) {
			try {
				if (obj.isTypeOf(aType)) {
					isFound = true;
					break;
				}
			} catch (TCException exc) {
				isFound = false;
				exc.printStackTrace();
			}
		}
		return isFound;
	}

	private Map<String, String> readPropertiesFromTC(String[] propNames) {
		Map<String, String> propNameValues = new HashMap<>();
		try {
			String[] propValues = this.packComponentRevision.getProperties(propNames);
			for (int inx = 0; inx < propNames.length; inx++)
				propNameValues.put(propNames[inx], propValues[inx]);
		} catch (TCException exce) {
			System.out.println("<<PCR::DBG>> Error in getting revision properties...!");
			System.out.println(exce.getMessage());
		}
		return propNameValues;
	}

}
