/* Source File Name:   UL4PnPListOfPAMReportOperation
 *
 * Description:  This file contains code to generate List of PAM Specification PDF Report
 * 
 * Super Class : AbstractAIFOperation
 * 
 * Revision History:
 * -------------------------------------------------------------------------
 * Revision    Author                Date Created           Reason
 * -------------------------------------------------------------------------
 *  1.0.0       Silpa Devi Hande        14/10/2015            Initial Creation
 *
 */

package com.unilever.rac.ui.pnpreport;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.teamcenter.fms.clientcache.proxy.FileCacheProxy;
import com.teamcenter.fms.util.FMSException;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.commands.cut.CutOperation;
import com.teamcenter.rac.commands.delete.DeleteOperation;
import com.teamcenter.rac.commands.newdataset.NewDatasetOperation;
import com.teamcenter.rac.kernel.TCAccessControlService;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentDatasetType;
import com.teamcenter.rac.kernel.TCComponentRevisionRule;
import com.teamcenter.rac.kernel.TCComponentTcFile;
import com.teamcenter.rac.kernel.TCComponentTransferMode;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.plmxmlexportimportadministration.PLMXMLAdminService;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;
import com.teamcenter.schemas.soa._2006_03.exceptions.ServiceException;
import com.teamcenter.services.rac.globalmultisite.ImportExportService;
import com.teamcenter.soa.client.model.ErrorStack;
import com.teamcenter.soa.client.model.ErrorValue;
import com.unilever.rac.ui.common.UL4Common;
import com.unilever.rac.ui.common.UL4URLDecoder;

/**
 * The Class UL4PnPListOfPAMReportOperation.
 */

public class UL4PnPListOfPAMReportOperation extends AbstractAIFOperation
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
	
	private String xsltFileName         = null ;
	
	private StringBuilder sbRelease  = null ;
	
	//private String SPECIFICATIONS       = "IMAN_specification";
			
	
/**
 * Constructor
 * 
 * @param packrevision
 * @param pamrevision
 * @param tcsession
 */
			
	public UL4PnPListOfPAMReportOperation(TCComponent dderevision , TCComponent pnprevision , TCSession tcsession , boolean imageRequired )
	{
		packRevision = dderevision ;
		pamRevision = pnprevision ;
		session = tcsession;
		reg = Registry.getRegistry( this ); 
		sbRelease = new StringBuilder(); ; ;		
		imgRequired = imageRequired ;

	}	
	
	@Override
	public void executeOperation() throws Exception 
	{
		String pdfFileNeme        = null ; 
		String xmlFileName  	  = null ;	
		String tempDir            = null ;
		String dsName      		  = null ;
		
		try
		{
			String name     	 	  = pamRevision.getStringProperty(UL4Common.OBJECT_NAME).replace(" ","_");
			name                      = formatFileName(name);
			String revid    	 	  = pamRevision.getStringProperty(UL4Common.REVID);
			String timestamp 		  = getCurrentTimeStampForFIleName();
			tempDir   		  		  = System.getProperty("java.io.tmpdir");			
		    dsName       			  = "LOP_" + name + "_" +revid + "_" + getCurrentTimeStamp()  ;
			pdfFileNeme               = tempDir + dsName + "." + "pdf" ;     
			xmlFileName  	          = tempDir + File.separator + timestamp + "." + "xml" ; 	
			String fileName 		  = timestamp + "." + "xml" ;
			
			System.out.println("Temp Location : "  + tempDir);
			
			if(ExportObjectsToPLMXML(tempDir,fileName))
	        {    	   
	    	   int readBytes;
	    	   byte[] buffer = new byte [4096];
	    	   
	    	   session.setStatus("Downloading XSLT File .......");
	    	   System.out.println("Downloading XSLT File .......");
	    	   String tempXSLFileName  = getXSLFilename();   
	    	   
	    	   if( tempXSLFileName != null )
	    	   {
	    		   if(!fileVerify(tempDir + "logo.png"))
	    		   {	    		   
		        	   String fromFile = "/data/logo.png";
		        	   InputStream istream = this.getClass().getResourceAsStream(fromFile);
		
		        	   try
		        	   {   OutputStream ostream = new FileOutputStream( new File ( tempDir + "logo.png"));
		        	        
		        	       while ((readBytes = istream.read(buffer)) != -1)
		        	        	ostream.write (buffer, 0, readBytes); 
		        	   } 
		        	   catch (IOException e1)
		        	   {
		        		   	// ignore logo 
		        	   }
	    		   }
	    		   
	    		   // Build Release Status Name
	    		   
	    		   String statusName = "";
	    		   
	    		   TCProperty relStatProp = pamRevision.getTCProperty("release_status_list");
                   TCComponent[] relStats = relStatProp.getReferenceValueArray();
                   
                   if(relStats.length > 0)
                	   statusName= relStats [relStats.length-1].toDisplayString();
                   
                   
                   sbRelease.append("<xsl:template name=\"ReleaseStatusName\">");
                   sbRelease.append(insertBlocvalue1(statusName));	    		   
                   sbRelease.append("</xsl:template>\n");
	    		   
	    		   session.setStatus("Updating XSLT File for Images and Associated Specification Details.......");
	    		   System.out.println("Updating XSLT File for Images and Associated Specification Details.......");
	    			   
	    		   if(updateXSLTFIle(tempDir,tempXSLFileName))
	    				tempXSLFileName = xsltFileName;
	    		   
			       xmlfile = new File(xmlFileName);
			       xsltfile = new File(tempXSLFileName);
			       pdffile = new File(pdfFileNeme);
			       
			       if(fileVerify(xmlFileName))
			       {			
				       final FopFactory fopFactory = FopFactory.newInstance();		 
				       String url = "file:////"+ System.getProperty("java.io.tmpdir");    
				       FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
				       foUserAgent.setBaseURL(url); 
		
				       OutputStream out = new java.io.FileOutputStream(pdffile);		        
				       out = new java.io.BufferedOutputStream(out);
				       
				       session.setStatus("Generating PnP Specification PDF Report.......");
				       System.out.println("Generating PnP Specification PDF Report.......");
				       try
				       {          
				           Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, out);
				           TransformerFactory factory = TransformerFactory.newInstance();
				           Transformer transformer = factory.newTransformer(new StreamSource(xsltfile));
				           Source src = new StreamSource(xmlfile);
				           Result res = new SAXResult(fop.getDefaultHandler());
				           transformer.transform(src, res);
				           out.close();	  
				           
				           session.setStatus("Uploading PnP Specification PDF Report as Dataset.......");
				           System.out.println("Uploading PnP Specification PDF Report as Dataset.......");
				           uploadDataset(dsName, pdfFileNeme);				           
				          
				       } 
				       catch (Exception e)
					   {
					      	MessageBox.post(AIFUtility.getActiveDesktop().getShell() , "XSLT Transformation Failed \n " + e.getMessage(),reg.getString("XSLT Error"),MessageBox.ERROR);
					   }  
			       }
			       else
			       {
				      	MessageBox.post(AIFUtility.getActiveDesktop().getShell() ,reg.getString("xmlmissing") + "( "+ xmlFileName +  " ) ",reg.getString("error"),MessageBox.ERROR);
			       }
	    	   }
	       }

		}
		catch (TCException e)
		{
			MessageBox.post(AIFUtility.getActiveDesktop().getShell() ,  e.getMessage(),"Error",MessageBox.ERROR);		
		}
		finally
		{
			session.setStatus("Deleting temp files.......");
			System.out.println("Deleting temp files.......");
			delete(xmlfile);
			delete(xsltfile);
			delete(pdffile);
		}			
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
	
	private boolean updateXSLTFIle(String tempDir ,String tempXSLFileName)
	{

		xsltFileName = tempDir.concat("temp.xsl") ;
		
		FileWriter outFile  = null ;
		BufferedWriter out  = null ;		
		FileReader inFile   = null ;	 
	    BufferedReader br   = null ;	
	    String str = "</xsl:stylesheet>";
	    
		try
		{
			inFile = new FileReader(tempXSLFileName);
		    br = new BufferedReader(inFile);
		    outFile = new FileWriter(xsltFileName);
			out = new BufferedWriter(outFile);
			
            String s;
		
            while((s = br.readLine()) != null)
            	if(!(s.equalsIgnoreCase(str)))    
            	{
            		out.write(s);
            		out.write("\n");
            	}

            out.write(sbRelease.toString());
             
            out.write(str);
            br.close();
            out.close();			
		} 
		catch (IOException e) 
		{
			xsltFileName = null ;
			return false ;
		}
		finally
		{
			try
			{
				if(br!=null)
					br.close();
				if(out != null)
					out.close();
			} 
			catch (IOException e) 
			{
				xsltFileName = null ;
				return false ;
			}
		}
		
		return true ;
	}
	
    public Boolean  fileVerify(String str)
    {
    	File file = new File(str);
    	 
    	 if (file.exists()) 
 		 {  	
 			return true;
 		 }
 		 return false;     	
    }
	
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
			AIFComponentContext[] component = pamRevision.getRelated("U4_ListOfPAMRelation");			
				
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
			AIFComponentContext[] component = pamRevision.getRelated("U4_ListOfPAMRelation");			
				
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
			relation = "U4_ListOfPAMRelation";

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
	
	private String  getXSLFilename() throws TCException
	{
		String fileName = null ;		
		TCComponentDataset dataset = null;
		
  		try 
		{
			String dsName = UL4Common.PNPLISTOFPAMREPORTXSL; //U4_PnPListOfPAMReport
			
			TCComponentDatasetType datasetType = (TCComponentDatasetType)session.getTypeComponent(UL4Common.DATASET);
			dataset = datasetType.find(dsName);
            
			if ( dataset != null )
			{			
				TCComponentTcFile relatedTcFiles[] = dataset.getTcFiles();	                	
		    	
		    	if(relatedTcFiles != null && relatedTcFiles.length != 0)
		    	{	
		    		File file = ((TCComponentTcFile)relatedTcFiles[0]).getFile( null );
		    		fileName = file.getAbsolutePath();
		    	}
			}
			else
			{
		      	MessageBox.post(AIFUtility.getActiveDesktop().getShell() ," XSL < " + dsName + " > " + reg.getString("datasetmissing"),reg.getString("error"),MessageBox.ERROR);
			}
    	} 
		catch (TCException e) 
		{
	      	MessageBox.post(AIFUtility.getActiveDesktop().getShell() ,e.getMessage(),reg.getString("error"),MessageBox.ERROR);
 		}
			
  	
		return fileName ;
		
	}
 
    /**
     * 
     * Export PLMXML
     * 
     * @param directory - Export Location
     * @param fileName - Export PLMXML File Name
     * @return true / false
     */
    public boolean ExportObjectsToPLMXML(String directory , String fileName)
    {
    	boolean valid = true;
   	
    	try
    	{
	    	StringBuilder sError =new StringBuilder();
	    	String transferMode =  "U4_PnPListOfPAMDataExport" ;
	        TCComponentRevisionRule tccomponentrevisionrule = null;
	        String[] lang = new String[]{""};
	    	TCComponent[] target = new TCComponent[] {packRevision};
	    	String file = fileName;
	    	String dir = directory;
	        TCComponentTransferMode tccomponenttransfermode = PLMXMLAdminService.getInstance(session).getTransferModeByName(transferMode);
	    	ImportExportService importexportservice = ImportExportService.getService(session);
	    	com.teamcenter.services.rac.globalmultisite._2010_04.ImportExport.ExportObjectsToPLMXMLResponse exportobjectstoplmxmlresponse = null;
	        com.teamcenter.services.rac.globalmultisite._2007_12.ImportExport.NamesAndValues anamesandvalues[] = new com.teamcenter.services.rac.globalmultisite._2007_12.ImportExport.NamesAndValues[0];
	         
	        try
	        {
	            exportobjectstoplmxmlresponse = importexportservice.exportObjectsToPLMXML(target, tccomponenttransfermode, tccomponentrevisionrule, lang, file, anamesandvalues);
	        }
	        catch(ServiceException serviceexception)
	        {
	            com.teamcenter.schemas.soa._2006_03.exceptions.Error[] aerror = serviceexception.getErrors();
	            
	            for(int l = 0; l < aerror.length; l++)
	            {
	                sError.append(aerror[l].message).append("\n");
	                System.out.println(aerror[l].message);
	            }
	
	        	MessageBox.post(AIFUtility.getActiveDesktop().getShell() ,sError.toString(),reg.getString("error"),MessageBox.ERROR);
	            valid =  false ;
	
	        }
	        
	        int j = 0;
	        if(exportobjectstoplmxmlresponse != null)
	            j = exportobjectstoplmxmlresponse.serviceData.sizeOfPartialErrors();
	        if(j > 0)
	        {
	            for(int k = 0; k < j; k++)
	            {
	                ErrorStack errorstack = exportobjectstoplmxmlresponse.serviceData.getPartialError(k);
	                ErrorValue[] aerrorvalue = errorstack.getErrorValues();
	                if(aerrorvalue != null)
	                {
	                    for(int i1 = 0; i1 < aerrorvalue.length; i1++)
	                    	sError.append((" Error code: ")).append(aerrorvalue[i1].getCode()).append("\n").append(" Error level: ").append(aerrorvalue[i1].getLevel()).append("\n").append(" Error message: ").append(aerrorvalue[i1].getMessage()).toString();
	
	                    MessageBox.post(AIFUtility.getActiveDesktop().getShell() ,sError.toString(),reg.getString("error"),MessageBox.ERROR);
	                    valid =  false ;
	
	                }
	            }
	
	        }
	 
	        Object obj = null;
	        
	        try
	        {	        	
	            FileCacheProxy filecacheproxy = new FileCacheProxy();
	            filecacheproxy.Init();
	            if(exportobjectstoplmxmlresponse != null)
	            {
	                String as[] = new String[2];
	                as[0] = exportobjectstoplmxmlresponse.xmlFileTicket.ticket;
	                as[1] = exportobjectstoplmxmlresponse.logFileTicket.ticket;
	                String as1[] = new String[2];
	                as1[0] = exportobjectstoplmxmlresponse.xmlFileTicket.fileName;
	                as1[1] = exportobjectstoplmxmlresponse.logFileTicket.fileName;
	                String as2[] = filecacheproxy.RegisterTickets(as);
	                
	            	session.setStatus("Exporting PLMXML .....");
	            	System.out.println("Exporting PLMXML .....");

	                filecacheproxy.DownloadFilesToLocation("IMD", null, null, as2, dir, as1);	                
		                
	            }
	        }
	        catch(FMSException fmsexception)
	        {
	        	MessageBox.post(AIFUtility.getActiveDesktop().getShell() ,fmsexception.getMessage(),reg.getString("error"),MessageBox.ERROR);
	            valid =  false ;
	        }
    	}
        catch(Exception exception)
        {
        	MessageBox.post(AIFUtility.getActiveDesktop().getShell() ,exception.getMessage(),reg.getString("error"),MessageBox.ERROR);
            valid =  false ;
        }
    	
    	return valid;
    }
    
    /**
     * Build External Image FC Block insert info into XSLT Templates
     * 
     * @param image
     * @return StringBuilder
     */
    
    private String buildURL(String image)
    {    	
    	StringBuilder block = new StringBuilder() ;
    	block.append("\n");
    	block.append("<fo:block><fo:external-graphic src=\"");
    	block.append(StringEscapeUtils.escapeXml(image));
    	block.append("\" content-height=\"scale-to-fit\" height=\"100%\" width=\"100%\" content-width=\"scale-to-fit\" scaling=\"uniform\"/></fo:block>");
    	block.append("\n");
    	block.append("<xsl:text>&#xA;</xsl:text>");
    	block.append("\n");

    	return  block.toString();
    	
    }  
 
    
    private String insertBlocvalue(String val)
    {    	
    	StringBuilder block = new StringBuilder() ;
    	block.append("<fo:table-cell border-top-width=\"0.5pt\" border-bottom-width=\"0.5pt\" border-left-width=\"0.5pt\" border-right-width=\"0.5pt\" border-top-style=\"solid\" border-bottom-style=\"solid\" border-left-style=\"solid\" border-right-style=\"solid\">");
    	block.append("<fo:block font-size=\"8pt\" font-family=\"Times New Roman\"><xsl:text>" + val + "</xsl:text></fo:block>");
    	block.append("</fo:table-cell>");
    	return  block.toString();
    	
    }
    
    private String insertBlocvalue1(String val)
    {    	
    	StringBuilder block = new StringBuilder() ;

    	block.append("<fo:block font-size=\"8pt\" font-family=\"Times New Roman\"><xsl:text>" + val + "</xsl:text></fo:block>");
    	
    	return  block.toString();
    	
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
