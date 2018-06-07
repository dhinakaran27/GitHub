package com.unilever.rac.pam;

public class PAMTableConfiguration {
	
	public String secondaryType;
	
	public String primaryType;
	
	public String packcomponentType;
	
	public PAMColumnConfiguration[] ColumnConfigurations;
	
	public PAMRowConfiguration rowConfigurations;
	
	public PAMTableConfiguration()
	{
		rowConfigurations = new PAMRowConfiguration();
		
	}
	
	 

}
