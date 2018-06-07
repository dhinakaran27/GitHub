package com.unilever.rac.ui.environmentalreport;

import java.util.ArrayList;

public class EPRMappingData {

	private String environmental_type = null;
	private String component_class = null;
	private String component_commodity = null;
	private String material_class = null;
	private String material_commodity = null;
	private String epr_classification = null;

	EPRMappingData(String environmental_type,String component_class,String component_commodity,String material_class,String material_commodity,String epr_classification){
		this.environmental_type = environmental_type;
		this.component_class = component_class;
		this.component_commodity = component_commodity;
		this.material_class = material_class;
		this.material_commodity = material_commodity;
		this.setEpr_classification(epr_classification);
	}
	
	
	public static ArrayList<EPRMappingData> filter_epr_mapping_data(ArrayList<EPRMappingData> data, String filter_attribute, String filter_value)
	{
		ArrayList<EPRMappingData> new_data = new ArrayList<EPRMappingData>();

		for (int index = 0 ; index < data.size() ; index++)
		{
			if (filter_attribute.compareTo("environmental_type")==0)
			{
				if (data.get(index).environmental_type.compareTo(filter_value) == 0)
					new_data.add(data.get(index));
			}
			if (filter_attribute.compareTo("component_class")==0)
			{
				if (data.get(index).component_class.compareTo(filter_value) == 0)
					new_data.add(data.get(index));
			}
			if (filter_attribute.compareTo("component_commodity")==0)
			{
				if (data.get(index).component_commodity.compareTo(filter_value) == 0)
					new_data.add(data.get(index));
			}
			if (filter_attribute.compareTo("material_class")==0)
			{
				if (data.get(index).material_class.compareTo(filter_value) == 0)
					new_data.add(data.get(index));
			}
			if (filter_attribute.compareTo("material_commodity")==0)
			{
				if (data.get(index).material_commodity.compareTo(filter_value) == 0)
					new_data.add(data.get(index));
			}
		}

		return new_data;
	}


	public String getEpr_classification() {
		return epr_classification;
	}


	public void setEpr_classification(String epr_classification) {
		this.epr_classification = epr_classification;
	}
}
