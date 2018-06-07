package com.unilever.rac.pam;


import java.util.Arrays;
import java.util.Vector;

import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCSession;

public class LoadPAMConfigurations {
		
	private String[] secondaryobject_a = null;
	
	private Vector<PAMTableConfiguration> tableConfigurations = null;

	
	 public Vector<PAMTableConfiguration> readPreferenceValues2(String prefName,TCSession session)
	 {	
		 tableConfigurations = new Vector<PAMTableConfiguration>();
		 TCPreferenceService prefServ = session.getPreferenceService(); 
		 String[] prefValues = prefServ.getStringArray(TCPreferenceService.TC_preference_site, prefName);
		 for(int inx=0;inx<prefValues.length;inx++)
		 {
			 String[] pamConfigElements = splitString(prefValues[inx], PAMConstant.TILDA);
			 if(prefValues[inx].contains(PAMConstant.SECONDARYOBJECTS))
			 {				
				 secondaryobject_a = splitString(pamConfigElements[1], PAMConstant.COMA);
			 }
			 PAMTableConfiguration tabelConfig = new PAMTableConfiguration();
			 if(prefValues[inx].contains(PAMConstant.COLUMNCONFIGURATION))
			 { 				 
				 String[] ColConfigRaw = splitString(pamConfigElements[2], PAMConstant.COMA);
				 PAMColumnConfiguration[] ColConfigFinal = new PAMColumnConfiguration[ColConfigRaw.length];
				 for(int jnx=0;jnx<ColConfigRaw.length;jnx++)
				 {
					 ColConfigFinal[jnx] = getColumnConfiguration(ColConfigRaw[jnx]);
				 }
				
				 tabelConfig.secondaryType = pamConfigElements[1];
				 tabelConfig.ColumnConfigurations = ColConfigFinal;
				 
			 }	
			 if(prefValues[inx].contains(PAMConstant.ROWCONFIGURATION))
			 {
				 tabelConfig.rowConfigurations = getRowConfiguration(pamConfigElements[2]);				 
			 }
			 tableConfigurations.add(tabelConfig);
		 }	
		 return tableConfigurations;
	 }
	 public Vector<PAMTableConfiguration> readPreferenceValues(String prefName,TCSession session)
	 {	
		 tableConfigurations = new Vector<PAMTableConfiguration>();
		 TCPreferenceService prefServ = session.getPreferenceService(); 
		 String[] prefValues = prefServ.getStringArray(TCPreferenceService.TC_preference_site, prefName);
		 for(int inx=0;inx<prefValues.length;inx+=3)
		 {
			 PAMTableConfiguration tabelConfig = new PAMTableConfiguration();
			 String[] primaryObjects= splitString(prefValues[inx], PAMConstant.TILDA);
			 tabelConfig.primaryType = primaryObjects[1];
			 
			 if(prefValues[inx].contains(PAMConstant.SECONDARYOBJECTS))
			 {			
				 String[] secondaryObjects=splitString(prefValues[inx], PAMConstant.TILDA);
				 tabelConfig.secondaryType = secondaryObjects[1];
				 if((inx+1<prefValues.length) && prefValues[inx+1].contains(PAMConstant.COLUMNCONFIGURATION) )
				 { 	 String[] pamConfigElements = splitString(prefValues[inx+1], PAMConstant.TILDA);			 
					 String[] ColConfigRaw = splitString(pamConfigElements[2], PAMConstant.COMA);
					 PAMColumnConfiguration[] ColConfigFinal = new PAMColumnConfiguration[ColConfigRaw.length];
					 for(int jnx=0;jnx<ColConfigRaw.length;jnx++)
					 {
						 ColConfigFinal[jnx] = getColumnConfiguration(ColConfigRaw[jnx]);
					 }			 
					 tabelConfig.ColumnConfigurations = ColConfigFinal;
					 
				 }	
				 if((inx+2<prefValues.length) && prefValues[inx+2].contains(PAMConstant.ROWCONFIGURATION) )
				 {
					 String[] pamConfigElements = splitString(prefValues[inx+2], PAMConstant.TILDA);
					 tabelConfig.rowConfigurations = getRowConfiguration(pamConfigElements[2]);				 
				 }
				 tableConfigurations.add(tabelConfig);
			 }
			 			 
		 }	
		 return tableConfigurations;
	 }
	 
	 public Vector<PAMTableConfiguration> readPreferenceValues3(String prefName,TCSession session)
	 {
		 tableConfigurations = new Vector<PAMTableConfiguration>();
		 TCPreferenceService prefServ = session.getPreferenceService(); 
		 String[] prefValues = prefServ.getStringValues(prefName);
		 int inx =0;
		 while(inx<prefValues.length)
		 {
			 PAMTableConfiguration tabelConfig = new PAMTableConfiguration();
			 String[] primaryObjects= splitString(prefValues[inx], PAMConstant.TILDA);
			 tabelConfig.primaryType = primaryObjects[1];
			 if(prefValues[inx].contains(PAMConstant.PRIMARYOBJECTS))
			 {
				 if(prefValues[inx+1].contains(PAMConstant.SECONDARYOBJECTS))
				 {			
					 String[] secondaryObjects=splitString(prefValues[inx+1], PAMConstant.TILDA);
					 tabelConfig.secondaryType = secondaryObjects[1];
					 if((inx+2<prefValues.length) && prefValues[inx+2].contains(PAMConstant.COLUMNCONFIGURATION) )
					 { 	 String[] pamConfigElements = splitString(prefValues[inx+2], PAMConstant.TILDA);			 
						 String[] ColConfigRaw = splitString(pamConfigElements[2], PAMConstant.COMA);
						 PAMColumnConfiguration[] ColConfigFinal = new PAMColumnConfiguration[ColConfigRaw.length];
						 for(int jnx=0;jnx<ColConfigRaw.length;jnx++)
						 {
							 ColConfigFinal[jnx] = getColumnConfiguration(ColConfigRaw[jnx]);
						 }			 
						 tabelConfig.ColumnConfigurations = ColConfigFinal;						 
					 }	
					 if((inx+3<prefValues.length) && prefValues[inx+3].contains(PAMConstant.ROWCONFIGURATION) )
					 {
						 String[] pamConfigElements = splitString(prefValues[inx+3], PAMConstant.TILDA);
						 tabelConfig.rowConfigurations = getRowConfiguration(pamConfigElements[2]);				 
					 }
					 tableConfigurations.add(tabelConfig);
				 }
			 }
			 inx++;
			 
		 }		 
		 return tableConfigurations;
	 }
	 
	 public Vector<PAMTableConfiguration> readPreferenceValues1(String prefName,TCSession session)
	 {	
		 tableConfigurations = new Vector<PAMTableConfiguration>();
		 TCPreferenceService prefServ = session.getPreferenceService(); 
		 String[] prefValues = prefServ.getStringValues(prefName);
		 
		 for(int inx=0;inx<prefValues.length;inx+=4)
		 {
			 if(prefValues[inx].contains(PAMConstant.PRIMARYOBJECTS))
			 {
				 PAMTableConfiguration tabelConfig = new PAMTableConfiguration();
				 String[] primaryObjects= splitString(prefValues[inx], PAMConstant.TILDA);
				 
				// If there are 3 fields, it means technology available
				// Example: PrimaryObject~U4_GClosurePlstRevision~U4_AClosurePRevision
				 if (primaryObjects.length == 3) 
					 tabelConfig.packcomponentType = primaryObjects[2];
					 
				 tabelConfig.primaryType = primaryObjects[1];
				 
				 if(prefValues[inx+1].contains(PAMConstant.SECONDARYOBJECTS))
				 {			
					 String[] secondaryObjects=splitString(prefValues[inx+1], PAMConstant.TILDA);
					 tabelConfig.secondaryType = secondaryObjects[1];
					 if((inx+2<prefValues.length) && prefValues[inx+2].contains(PAMConstant.COLUMNCONFIGURATION) )
					 { 	 String[] pamConfigElements = splitString(prefValues[inx+2], PAMConstant.TILDA);			 
						 String[] ColConfigRaw = splitString(pamConfigElements[2], PAMConstant.COMA);
						 PAMColumnConfiguration[] ColConfigFinal = new PAMColumnConfiguration[ColConfigRaw.length];
						 for(int jnx=0;jnx<ColConfigRaw.length;jnx++)
						 {
							 ColConfigFinal[jnx] = getColumnConfiguration(ColConfigRaw[jnx]);
						 }			 
						 tabelConfig.ColumnConfigurations = ColConfigFinal;
						 
					 }	
					 if((inx+3<prefValues.length) && prefValues[inx+3].contains(PAMConstant.ROWCONFIGURATION))
					 {
						 String[] pamConfigElements = splitString(prefValues[inx+3], PAMConstant.TILDA);
						 if(!(pamConfigElements[2].equalsIgnoreCase("NA")))
							 tabelConfig.rowConfigurations = getRowConfiguration(pamConfigElements[2]);				 
					 }

					 tableConfigurations.add(tabelConfig);
				 }
			 }			 
		 }	
		 return tableConfigurations;
	 }
	 
	 public String[] splitString(String inpuStr, String delimiter)
	 {
		 String[] outputstrs = inpuStr.split(delimiter);	 
				 
		 return outputstrs;		 
	 }
	 
	 public String[] getSecondaryObjects()
	 {
		 return secondaryobject_a;
	 }
	 
	  public PAMColumnConfiguration getColumnConfiguration(String rawConfig)
	  {
		  PAMColumnConfiguration colConfigInfo = new PAMColumnConfiguration();
		  
		  String PropSizeEditable[] = splitString(rawConfig,PAMConstant.COLON );
		  
		  colConfigInfo.columnName = PropSizeEditable[0];
		  
		  colConfigInfo.columnSize = Integer.parseInt(PropSizeEditable[1]);
		  
		  if(PropSizeEditable[2].equalsIgnoreCase("Disable"))
			  colConfigInfo.isEnabled = false;
		  else
			  colConfigInfo.isEnabled = true;
		  
		  if((PropSizeEditable.length >3)&&(PropSizeEditable[3].equalsIgnoreCase("Struct")))
			  colConfigInfo.isStructured = true;
		  else
			  colConfigInfo.isStructured = false;		  
		  
		  return colConfigInfo;		  
	  }	 
	  public PAMRowConfiguration getRowConfiguration(String rawConfig)
	  {
		  PAMRowConfiguration rowConfigInfo = new PAMRowConfiguration();
		  
		  String propNameValues[] = splitString(rawConfig,PAMConstant.COLON );
		  
		  String propName = propNameValues[0];
		  
		  String[] propValules = splitString(propNameValues[1],PAMConstant.COMA);
		  
		  rowConfigInfo.propertyName = propName;
		  
		  rowConfigInfo.propertyValues = new Vector<String>(Arrays.asList(propValules));
		  
		  return rowConfigInfo;
	  }
}
