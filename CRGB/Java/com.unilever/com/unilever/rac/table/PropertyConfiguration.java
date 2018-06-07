package com.unilever.rac.table;

import com.teamcenter.rac.kernel.TCComponent;
import com.unilever.rac.pam.PAMRowConfiguration;

public class PropertyConfiguration {
	
	public String secondaryName;
	
	public TCComponent selectedComponent;
	
	public PropertyContainer[] propNameValuepair;
	
	public PAMRowConfiguration propRowConfigPair;
	
	
	
	public String structuredProperties;
	
	public String getRequiredPropertyValue(String propName, int index)
	{
		for(int inx=0;inx<propNameValuepair.length;inx++)
		{
			if((propNameValuepair[inx].propName).equalsIgnoreCase(propName))
			{
				if (index < propNameValuepair[inx].propValue.length )
				{
					return propNameValuepair[inx].propValue[index];
				}
			}
		}
		
		return null;
		
	}

}
