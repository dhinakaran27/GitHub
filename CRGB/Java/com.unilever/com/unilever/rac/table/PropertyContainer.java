package com.unilever.rac.table;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCProperty;

public class PropertyContainer {
	
	public TCComponent selectedComponent;
	
	public String propName;
	
	public String propDisplayName;
	
	public String propValue[];
	
	public boolean isUpdated;
	
	public TCProperty tcProperty;
	
	public int columnSize;
	
	public boolean isEnabled;
	
	public int PropertyType;
	
	public int length;
		
	public PropertyContainer(int length){
		propName = new String();
		propDisplayName = new String();
		propValue = new String[length];
	}
	
	public String getPropValue(String propName, int index)
	{
		if (index < propValue.length )
		{
			if(propName.equalsIgnoreCase(this.propName))
				return propValue[index];
		}
		
		return null;
	}

}

