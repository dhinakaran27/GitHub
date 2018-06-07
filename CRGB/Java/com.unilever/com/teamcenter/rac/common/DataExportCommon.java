package com.teamcenter.rac.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
//import java.util.ArrayList;
import java.util.Date;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aifrcp.AIFUtility;
//import com.teamcenter.rac.kernel.TCAttachmentType;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentDatasetType;
import com.teamcenter.rac.kernel.TCComponentForm;
import com.teamcenter.rac.kernel.TCComponentFormType;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentProject;
import com.teamcenter.rac.kernel.TCComponentProjectType;
import com.teamcenter.rac.kernel.TCComponentUser;
//import com.teamcenter.rac.kernel.TCComponentProcessType;
//import com.teamcenter.rac.kernel.TCComponentTaskTemplate;
//import com.teamcenter.rac.kernel.TCComponentTaskTemplateType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.Registry;

public class DataExportCommon 
{
	//General Constants
	public final String PLUGIN_NAME = "com.unilever";
	public final String PATH_IMAGE_MANDATORY = "icons/mandatory_asterisk.png";
	public final String ENV_TEMP = "temp";
	public final String EXTN_ZIP = ".zip";
	public final String EXTN_TXT = ".txt";
	public final String EXTN_LOG = ".log";
	public final String EXTN_PNG = ".png";
	public final String EXTN_ICON = ".ICON";
	public final String EXTN_ZIPFILES = "*.zip";
	public final String EXTN_ALLFILES = "*.*";
	public final String FL_DOUBLESEPERATOR = "\\";
	public final String SPLIT_PREF = ":";
	public final String DIR_FILTER = "C:/";
	public final String UNDERSCORE = "_";
	public final String FONT_TYPE = "Segoe UI";
	public final String FORMAT_DATE = "dd-MM-yyyy_HH-mm-ss";
	
	//Teamcenter workflow
	//public final String PROCESS_EXPORT = "Export Data Release";
	
	//Export/Import dialog
	public final String TITLE_EXP_DATA = "Export Data";
	public final String TITLE_OPENDIALOG = "Open";
	public final String BTN_BROWSE = "Browse";
	public final String LBL_ATTACHLIST = "Export Dataset (Please select to export):";
	public final String LBL_SUPNAME = "Supplier:";
	public final String LBL_SUPINFO = "Supplier Info:";
	public final String LBL_COMMENTS = "Comments:";
	public final String LBL_EXPORTREASON = "Export Reason:";
	public final String LBL_IMPORTREASON = "Import Reason:";
	public final String LBL_EXPORTPATH = "Export Folder Path:";
	public final String LBL_REFDOC = "Reference Document:";
	public final String LBL_IMPORTZIP = "Import Zip File:";
	public final String LBL_FILESTOIMPORT = "Files to be Imported:";
	public final String LBL_CHILD_DS = "Children Datasets (All included for export):";
	public final String TOOLTIP_SUPPLIER = "Use Supplier name from SupplierNet to ensure data consistency";
	
	//Teamcenter object properties
	public final String PROP_OBJ_TYPE = "object_type";
	public final String PROP_OBJ_NAME = "object_name";
	public final String PROP_OBJ_DESC = "object_desc"; 
	public final String PROP_COMMENTS = "u4_supplier_comments";
	public final String PROP_SUPPLIERNAME = "u4_supplier_name";
	public final String PROP_REASON = "u4_reason";
	public final String PROP_SUPPLIERINFO = "u4_supplier_information";
	public final String PROP_STAGE = "u4_stage";
	public final String PROP_REFDOC = "u4_reference_document";
	public final String PROP_COMMENTSFILE = "u4_comments_file";
	public final String PROP_EXPLOC = "u4_exported_location";
	public final String PROP_HAS_CHILDREN = "ps_children";
	public final String PROP_BL_HAS_CHILDREN = "bl_has_children";
	
	
	//Teamcenter relation types
	public final String RLN_SPEC= "IMAN_specification";
	public final String RLN_REF = "IMAN_reference";
	public final String RLN_EXPBKP = "U4_SupplierExchangeRelation";
	
	//Teamcenter types
	public final String TYPE_EXPORTDATAFORM = "U4_DataExportForm";
	public final String TYPE_IMPORTDATAFORM = "U4_DataImportForm";
	public final String TYPE_PDF = "PDF";
	public final String TYPE_UGMASTER = "UGMASTER";
	public final String TYPE_ZIP = "Zip";
	public final String TYPE_CADREVISION = "U4_CADComponentRevision";
	
	//Dataset named references
	public final String NREF_ZIP = "ZIPFILE";
	
	//LOVs
	public final String LOV_EXPORTREASON = "U4_DataExportReasonLOV";
	public final String LOV_IMPORTREASON = "U4_DataImportReasonLOV";
	
	//Pack components' life stages
	public final String STAGE_PR = "Packaging Refinement";
	public final String STAGE_DR = "Design Realization";
	public final String STAGE_PL = "Pilot";
	
	//Teamcenter Preferences
	//public final String PREF_NAMEDREFTYPES = "U4_NamedRefTypes";
	public final String PREF_COMPREVS = "U4_PackComponentRevisions";

	//Register strings
	private Registry reg = Registry.getRegistry(this);
	public final String ValueMissing = reg.getString("valuesmissing.TITLE");
	public final String ValueMissingMsg = reg.getString("valuesmissing.MSG");
	public final String PathMissing = reg.getString("pathmissing.TITLE");
	public final String PathMIssingMsg = reg.getString("pathmissing.MSG");
	public final String FileMissing = reg.getString("filemissing.TITLE");
	public final String FileMissingMsg = reg.getString("filemissing.MSG");
	public final String WrongCompStatus = reg.getString("WrongObjectSelection.TITLE");
	public final String WrongCompMsg = reg.getString("WrongObjectSelection.MSG");
	public final String WrongCompDefMsg = reg.getString("WrongObjectSelectionDef.MSG");
	public final String WrongCompStageMsg = reg.getString("WrongObjectSelectionStage.MSG");
	public final String PrefMissing = reg.getString("PrefMissing.TITLE");
	public final String PrefMissingMsg = reg.getString("PrefMissing.MSG");
	public final String ExportFailed = reg.getString("ExportFailed.TITLE");
	public final String ExportFailedMsg = reg.getString("ExportFailed.MSG");
	public final String ImportFailed = reg.getString("ImportFailed.TITLE");
	public final String ImportFailedMsg = reg.getString("ImportFailed.MSG");
	public final String ImportSuccess = reg.getString("ImportSuccess.TITLE");
	public final String ImportSuccessMsg = reg.getString("ImportSuccess.MSG");
	public final String ExportSuccess = reg.getString("ExportSuccess.TITLE");
	public final String ExportSuccessMsg = reg.getString("ExportSuccess.MSG");
	public final String ZipNotValid = reg.getString("ZipNotValid.TITLE");
	public final String ZipNotValidMsg = reg.getString("ZipNotValid.MSG");
	public final String UserNotPrivileged = reg.getString("UserNotPrivileged.MSG");
	public final String UserNotPrivilegedMsg = reg.getString("UserNotPrivileged.MSG");
	
	
	TCSession session = null;
	
	public DataExportCommon()
	{		
	    AbstractAIFUIApplication app = null ;
	    app = AIFUtility.getActiveDesktop().getCurrentApplication();
	    session = (TCSession) app.getSession();
	}
	
	/**
	 * Creates a form
	 * @return TCComponentForm
	 * @throws TCException 
	 */
	public TCComponentForm createForm(String sFormName, String sFormType) throws TCException
	{	
		TCComponentFormType formType = null;
		TCComponentForm form = null;
		formType = (TCComponentFormType) session.getTypeComponent(sFormType);
		form = formType.create( sFormName, "", sFormType, true);
		return form;
	}
	
	/**
	 * Validates file
	 * @param sPath
	 * @return isPathValid
	 */
	public boolean validateFile(String sPath) 
	{
		final File fFile = new File(sPath);

		boolean isFileValid = false;
		if (fFile.isFile())
		{
			isFileValid  = true;
		}
		return isFileValid;
	}
	
	/**
	 * Validates system path
	 * @param sPath
	 * @return isPathValid
	 */
	public boolean validatePath(String sPath) 
	{
		final File fPath = new File(sPath);
        
        String locationValueAP = fPath.getAbsolutePath().replaceAll("\\\\", "\\\\\\\\");
        
		Boolean dirAccess = writeAccess(locationValueAP);
		Boolean fileAccess = fPath.exists();
		boolean isPathValid = false;
		if ((fileAccess == true) && (dirAccess == true) )
		{
			isPathValid   = true;
		}
		return isPathValid;
	}
	
	/**
	 * Check the given location have write access for current user
	 * @param path
	 */
	public boolean writeAccess(String path)
	{
		  File sample = new File(path, "empty.txt");
		  
		  try 
		  {
			sample.createNewFile();
			sample.delete();
			return true;
		  } catch (IOException e) 
		  {
			return false;
		  }
	}
	
	/**
	 * Creates a form for export information
	 * @param sPath
	 * @return isPathValid
	 */
	public String getTimestamp()
	{
		DateFormat dateFormat = new SimpleDateFormat(FORMAT_DATE);
		Date date = new Date();
		String timestamp = dateFormat.format(date);

		return timestamp;
	}

	/**
	 * Copy the file to given location
	 * @param sFileToCopy
	 * @param sTargetPath
	 * @throws IOException 
	 */
	public void copyFile(String sFileToCopy, String sTargetPath) throws IOException 
	{
		File source = new File(sFileToCopy);
		String sSourceFileName = source.getName();
		
		final File fPath = new File(sTargetPath);
		String sLocValueAP = fPath.getAbsolutePath().replaceAll("\\\\", "\\\\\\\\");
		sLocValueAP = sLocValueAP.concat("\\\\");
		sLocValueAP = sLocValueAP.concat(sSourceFileName);
		final File target = new File(sLocValueAP);

		Files.copy(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
	}
	
	/**
	 * Checks the selected component has stage of 
	 * 					"Packaging Refinement"
	 * 					"Design Realization"
	 * 					"Pilot"
	 * @param itemRevision
	 * @return isReleased
	 */
	/*
	public boolean isCompStageValidForExchange(TCComponentItemRevision itemRevision) throws TCException 
	{
		boolean isValidStage = false;
		
		String sStage= itemRevision.getProperty(PROP_STAGE);
		if(	sStage.equals(STAGE_PR)	||
				sStage.equals(STAGE_DR) )
			{
				isValidStage = true;
			}
		return isValidStage;
	}
	*/
	
	/**
	 * Checks the selected component a custom Unilever component
	 * Though the option of export given to only Unilever components,
	 * there's a chance of selecting other component in same summary page
	 * and initiate the export/import
	 * @param comp
	 * @return isComponent
	 */
	public boolean isUnileverComponent(TCComponent comp)
	{
		boolean isComponent = false;
		
		String[] values = getPreferenceValues(PREF_COMPREVS);
		
		for(int i = 0; i < values.length; i++)
		{
			if(comp.getType().toString().equalsIgnoreCase(values[i]))
			{
				isComponent = true;
				comp  = (TCComponentItemRevision) comp;
			}
		}
		
		if(comp.getType().toString().equalsIgnoreCase(TYPE_CADREVISION))
		{
			isComponent = true;
			comp  = (TCComponentItemRevision) comp;
		}
		
		return isComponent;
	}
	
	/**
	 * Checks the selected component has a proper Definition
	 * @param attachments
	 * @return isDataset
	 */
	/*public boolean isProperDefinition(TCComponent comp, TCComponent[] attachments) 
	{
		boolean isProperDefinition  = false;
		boolean is3DComponent = false;
		
		TCPreferenceService pref = session.getPreferenceService();
		String [] values = pref.getStringValues(PREF_3DCOMPS);
		
		for (int i = 0; i < values.length; i++)
		{
			if(values[i].toString().equals(comp.getType().toString()))
			{
				is3DComponent = true;
				for (int ds = 0; ds < attachments.length; ds++)
				{
					String dsType = attachments[ds].getType().toString();
					if(dsType.equalsIgnoreCase(TYPE_UGMASTER))
					{
						isProperDefinition  = true;
						
					}
				}
			}
		}
		
		if(is3DComponent == false)
		{
			for (int ds = 0; ds < attachments.length; ds++)
			{
				String dsType = attachments[ds].getType().toString();
				if(dsType.equalsIgnoreCase(TYPE_PDF))
				{
					isProperDefinition  = true;
				}
			}
		}
		
		return isProperDefinition;	
	}
	*/

	/**
	 * Gets values from given preference
	 * @param sPreference
	 * @return String[]
	 */
	public String[] getPreferenceValues(String sPreference)
	{
		TCPreferenceService pref = session.getPreferenceService();
		String [] saValues = pref.getStringValues(sPreference);
		return saValues;
	}
	
	/**
	 * Checks all needed supplier exchange preferences
	 * "U4_PackComponentRevisions", "U4_3DPackComponentTypes" & "U4_NamedRefTypes"
	 * @return boolean
	 */
	public boolean isSupExPrefsAvailable() 
	{
		boolean isSupExPrefsAvailable = false;
		
		int nPrefPCRValues = 0;
		
		String[] saPrefPCRValues = getPreferenceValues(PREF_COMPREVS);
		
		if(saPrefPCRValues != null) nPrefPCRValues = saPrefPCRValues.length;
		
		if(nPrefPCRValues > 0)
		{
			isSupExPrefsAvailable = true;
		}

		return isSupExPrefsAvailable;
	}
	
	/**
	 * Creates Zip file with all files in the given directory
	 * @param sZipFileName
	 * @param sDirtoZip
	 */
	public void createZIPFile(String sZipFileName, String sDirtoZip) throws IOException
	{
		byte[] buffer = new byte[1024];
		FileOutputStream fos = new FileOutputStream(sZipFileName);
		ZipOutputStream zos = new ZipOutputStream(fos);
		File dir = new File(sDirtoZip);
		  
		File[] files = dir.listFiles();
		for (int i = 0; i < files.length; i++) 
		{
			//System.out.println("Adding file: " + files[i].getName());
			FileInputStream fis = new FileInputStream(files[i]);
			zos.putNextEntry(new ZipEntry(files[i].getName()));
			int length;
			while ((length = fis.read(buffer)) > 0) 
			{
				zos.write(buffer, 0, length ); 
			}
			zos.closeEntry();
			fis.close();
		}
		zos.close();
	}
	
	/**
	 * Release objects with given process template
	 * @param relSet
	 * @throws TCException 
	 */
	/*public void releaseObjects(ArrayList<TCComponent> relSet, String sTemplateName) throws TCException
	{
		for(int i = 0; i < relSet.size(); i++)
		{
			//System.out.println("releasing "+relSet.get(i).toString());
			TCComponent tcComponents[] = new TCComponent[1];
			
			tcComponents[0] = relSet.get(i);
			
			TCComponent targetComponents[] = new TCComponent[1];
			targetComponents[0] = (TCComponent) tcComponents[0];
			
			int attType[] = new int[targetComponents.length];
			for (int jnx = 0; jnx < targetComponents.length; jnx++)
		    {
		         attType[jnx] = TCAttachmentType.TARGET ;
		    }
			
			TCComponentTaskTemplateType taskTemplateType = ( TCComponentTaskTemplateType ) session.getTypeComponent ( "EPMTaskTemplate" );
			TCComponentTaskTemplate processTemplate = taskTemplateType.find ( sTemplateName ,TCComponentTaskTemplate.PROCESS_TEMPLATE_TYPE );
			TCComponentProcessType processType = ( TCComponentProcessType ) session.getTypeComponent ( "EPMJob" );
			processType.create( targetComponents[0].toString() , "" , processTemplate , targetComponents , attType );
		}
	}/*/
	
	/**
	 * Creates dataset and named reference
	 * @return TCComponentDataset
	 * @throws TCException 
	 */
	public TCComponentDataset createDataset(String sDatasetName, String sFileWithLocation,
											String sDatasetType, String sNamedReference) throws TCException 
	{
		TCComponentDatasetType dsType = new TCComponentDatasetType();

		dsType = (TCComponentDatasetType) session.getTypeComponent(sDatasetType);
		TCComponentDataset zipDataset = dsType.create(sDatasetName, "", sDatasetType);
		String filepath[] = {sFileWithLocation};
		String reftype[] = {sNamedReference};
		zipDataset.setFiles(filepath, reftype);	
	
		return zipDataset;
	}


	/**
	 * Checks if Zip file is corrupted
	 * @param sImportFileValue
	 * @return isZipFileCorrupt
	 * @throws TCException 
	 */
	public boolean isZipFileValid(String sImportFileValue)
	{
		boolean isZipFileValid = false;
		
		ZipFile zipfile = null;
		try 
		{
			zipfile = new ZipFile(sImportFileValue);
			isZipFileValid = true;
		}
		catch (ZipException e) {e.printStackTrace();} 
		catch (IOException e) {e.printStackTrace();}
		finally
		{
	        try 
	        {
	            if (zipfile != null)
	            {
	                zipfile.close();
	                zipfile = null;
	            }
	        }
	        catch (IOException e) {e.printStackTrace(); }
	    }
		return isZipFileValid;
	}

	/**
	 * Checks if logged in user a privileged member in projects assigned to selected component 
	 * @param itemRevision
	 * @return boolean
	 * @throws TCException 
	 */
	public boolean isUserPrevilegedOnComponent(TCComponentItemRevision itemRevision) throws TCException
	{
		TCComponentUser userLogged = session.getUser();
		System.out.println(session.getUser().toString());
		String sProjects;
		boolean isPrevileged = false;
		sProjects = itemRevision.getProperty("project_ids");
		String[] saProjects =  sProjects.split(",");
		
		TCComponentProjectType typeProject  = (TCComponentProjectType) itemRevision.getSession().getTypeComponent("TC_Project");
		
		for(int nProj = 0; nProj < saProjects.length; nProj++)
		{
			TCComponentProject project = typeProject.find(saProjects[nProj].trim());
			
			isPrevileged = typeProject.isPrivilegedMember(project, userLogged);
		}
		return isPrevileged;	
	}
		
	/**
	 * Get all attachments from component on given attachments
	 * @param itemRevision
	 * @param sRelName
	 * @return Vector<TCComponent>
	 * @throws TCException
	 */
	public Vector<TCComponent> getAttachments(TCComponentItemRevision itemRevision, String sRelName) throws TCException
	{
		Vector<TCComponent> vAttachments = new Vector<TCComponent>();
		
		TCComponent[] aAttachments = itemRevision.getRelatedComponents(sRelName);
		for(int i = 0; i < aAttachments.length; i++)
		{
			vAttachments.add(aAttachments[i]);
		}
		return vAttachments;
	}
}



