package com.unilever.rac.ui.packreport;

import java.util.Set;

public class UL4PcrPackTableConfiguration {
	private String primaryObjectType;
	private Set<String> primaryObjectTypes;
	private String secondaryObjectType;
	private UL4PcrColumnConfiguration[] columnConfiguration;
	private UL4PcrRowConfiguration rowConfiguration;
	public String getPrimaryObjectType() {
		return primaryObjectType;
	}
	public void setPrimaryObjectType(String primaryObjectType) {
		this.primaryObjectType = primaryObjectType;
	}
	public Set<String> getPrimaryObjectTypes() {
		return primaryObjectTypes;
	}
	public void setPrimaryObjectTypes(Set<String> primaryObjectTypes) {
		this.primaryObjectTypes = primaryObjectTypes;
	}
	public String getSecondaryObjectType() {
		return secondaryObjectType;
	}
	public void setSecondaryObjectType(String secondaryObjectType) {
		this.secondaryObjectType = secondaryObjectType;
	}
	public UL4PcrColumnConfiguration[] getColumnConfiguration() {
		return columnConfiguration;
	}
	public void setColumnConfiguration(
			UL4PcrColumnConfiguration[] columnConfiguration) {
		this.columnConfiguration = columnConfiguration;
	}
	public UL4PcrRowConfiguration getRowConfiguration() {
		return rowConfiguration;
	}
	public void setRowConfiguration(UL4PcrRowConfiguration rowConfiguration) {
		this.rowConfiguration = rowConfiguration;
	}
}
