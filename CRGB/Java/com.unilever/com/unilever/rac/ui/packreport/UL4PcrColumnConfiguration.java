package com.unilever.rac.ui.packreport;

public class UL4PcrColumnConfiguration {
	private String propertyRealName;
	private int columnSize;
	private boolean isEnabled;
	private boolean isStructured;
	
	public String getPropertyRealName() {
		return propertyRealName;
	}
	public void setPropertyRealName(String propertyRealName) {
		this.propertyRealName = propertyRealName;
	}
	public int getColumnSize() {
		return columnSize;
	}
	public void setColumnSize(int columnSize) {
		this.columnSize = columnSize;
	}
	public boolean isEnabled() {
		return isEnabled;
	}
	public void setEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}
	public boolean isStructured() {
		return isStructured;
	}
	public void setStructured(boolean isStructured) {
		this.isStructured = isStructured;
	}
}
