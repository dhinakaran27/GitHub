package com.unilever.rac.pam;

import com.teamcenter.rac.kernel.TCComponent;

public class PAMSecondaryPropValue {
	
	public String secondaryName;
	
	public TCComponent selectedComponent;
	
	public PAMPropertyNameValue[] propNameValuepair;
	
	public PAMRowConfiguration propRowConfigPair;
	
	
	
	public String structuredProperties;
	
	public String getRequiredPAMPropertyValue(String propName)
	{
		for(int inx=0;inx<propNameValuepair.length;inx++)
		{
			if((propNameValuepair[inx].propName).equalsIgnoreCase(propName))
			{
				return propNameValuepair[inx].propValue;
			}
		}
		
		return null;
		
	}

}
