package com.unilever.rac.ui.packreport;

import java.util.Comparator;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.soa.exceptions.NotLoadedException;

public class UL4PcrTcObjectComparator implements Comparator<TCComponent> {
	private String comparatorPropertyName;
	private String sortOrder;
	
	public UL4PcrTcObjectComparator(String comparatorPropertyName) {
		super();
		this.comparatorPropertyName = comparatorPropertyName;
	}

	public UL4PcrTcObjectComparator(String comparatorPropertyName,
			String sortOrder) {
		super();
		this.comparatorPropertyName = comparatorPropertyName;
		this.sortOrder = sortOrder;
	}

	@Override
	public int compare(TCComponent obj1, TCComponent obj2) {
		int result = 0;
		String obj1Value = null;
		String obj2Value = null;
		
		try {
			obj1Value = obj1.getPropertyDisplayableValue(comparatorPropertyName);
			obj2Value = obj2.getPropertyDisplayableValue(comparatorPropertyName);
			if (obj1Value == null) obj1Value = "";
			if (obj2Value == null) obj2Value = "";
			if (this.sortOrder.equalsIgnoreCase("DESC")) {
				result = obj2Value.compareTo(obj1Value);
			} else {
				result = obj1Value.compareTo(obj2Value);
			}
		} catch (NotLoadedException exc) {
			exc.printStackTrace();
		}
		return result;
	}

}
