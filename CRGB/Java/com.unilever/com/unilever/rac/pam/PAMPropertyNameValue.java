package com.unilever.rac.pam;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCProperty;

public class PAMPropertyNameValue {
	
	public TCComponent selectedComponent;
	
	public String propName;
	
	public String propDisplayName;
	
	public String propValue;
	
	public boolean isUpdated;
	
	public TCProperty tcProperty;
	
	public int columnSize;
	
	public boolean isEnabled;
	
	public boolean isStructured;
	
	public int PropertyType;
	
	
	public PAMPropertyNameValue(){
		propName = new String();
		propDisplayName = new String();
		propValue = new String();
	}
	
	public String getPropValue(String propName)
	{
		if(propName.equalsIgnoreCase(this.propName))
			return propValue;
		else 
			return null;
	}

}
