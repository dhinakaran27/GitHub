package com.unilever.rac.ui.packreport;

import com.teamcenter.rac.kernel.TCProperty;

public class UL4PcrPackTableObject {
	private String sequence;
	private TCProperty[] tcProps;
	private float[] colSizes;
	
	public float[] getColSizes() {
		return colSizes;
	}
	public void setColSizes(float[] colSizes) {
		this.colSizes = colSizes;
	}
	public String getSequence() {
		return sequence;
	}
	public void setSequence(String sequence) {
		this.sequence = sequence;
	}
	public TCProperty[] getTcProps() {
		return tcProps;
	}
	public void setTcProps(TCProperty[] tcPropPairs) {
		this.tcProps = tcPropPairs;
	}
}
