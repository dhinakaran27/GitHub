package com.unilever.rac.ui.environmentalreport;

public class EnvLayerData {
	private String item_name = null;
	private String item_id = null;
	private String key_country = null;
	private String material_type = null;
	private double weight = 0.0;
	private double weight_percentage = 0.0;
	private double ghg_pack_matl = 0.0;
	private double ghg_disposal = 0.0;
	private double recycled_percentage = 0.0;
	private double epr_tax = 0.0;
	private boolean wt_zero = false;
	private boolean isSummaryData = false;  
	private boolean weightMin = false;
	private boolean ghgMin = false;
	private boolean ghg_disposalMin = false;
	private boolean eprMin = false;
	private double real_ghg = 0.0; 
	private double real_ghg_disposal = 0.0;
	private double real_waste = 0.0;
	private double real_epr_tax = 0.0;
	public String getItem_name() {
		return item_name;
	}
	public void setItem_name(String item_name) {
		this.item_name = item_name;
	}
	public String getItem_id() {
		return item_id;
	}
	public void setItem_id(String item_id) {
		this.item_id = item_id;
	}
	public String getKey_country() {
		return key_country;
	}
	public void setKey_country(String key_country) {
		this.key_country = key_country;
	}
	public double getWeight() {
		return weight;
	}
	public void setWeight(double weight) {
		this.weight = weight;
	}
	public double getWeight_percentage() {
		return weight_percentage;
	}
	public void setWeight_percentage(double weight_percentage) {
		this.weight_percentage = weight_percentage;
	}
	public double getGhg_pack_matl() {
		return ghg_pack_matl;
	}
	public void setGhg_pack_matl(double ghg_pack_matl) {
		this.ghg_pack_matl = ghg_pack_matl;
	}
	public double getGhg_disposal() {
		return ghg_disposal;
	}
	public void setGhg_disposal(double ghg_disposal) {
		this.ghg_disposal = ghg_disposal;
	}
	public double getRecycled_percentage() {
		return recycled_percentage;
	}
	public void setRecycled_percentage(double recycled_percentage) {
		this.recycled_percentage = recycled_percentage;
	}
	public double getEpr_tax() {
		return epr_tax;
	}
	public void setEpr_tax(double epr_tax) {
		this.epr_tax = epr_tax;
	}
	public void setWt_zero(boolean wt_zero) {
		this.wt_zero = wt_zero;
	}
	public boolean isWt_zero() {
		return wt_zero;
	}
	public boolean isWeightMin() {
		return weightMin;
	}
	public void setWeightMin(boolean weightMin) {
		this.weightMin = weightMin;
	}
	public boolean isGhgMin() {
		return ghgMin;
	}
	public void setGhgMin(boolean ghgMin) {
		this.ghgMin = ghgMin;
	}
	public boolean isGhg_disposalMin() {
		return ghg_disposalMin;
	}
	public void setGhg_disposalMin(boolean ghg_disposalMin) {
		this.ghg_disposalMin = ghg_disposalMin;
	}
	public boolean isEprMin() {
		return eprMin;
	}
	public void setEprMin(boolean eprMin) {
		this.eprMin = eprMin;
	}
	public String getMaterial_type() {
		return material_type;
	}
	public void setMaterial_type(String material_type) {
		this.material_type = material_type;
	}
	public boolean isSummaryData() {
		return isSummaryData;
	}
	public void setSummaryData(boolean isSummaryData) {
		this.isSummaryData = isSummaryData;
	}
	public double getReal_ghg() {
		return real_ghg;
	}

	public void setReal_ghg(double real_ghg) {
		this.real_ghg = real_ghg;
	}

	public double getReal_ghg_disposal() {
		return real_ghg_disposal;
	}

	public void setReal_ghg_disposal(double real_ghg_disposal) {
		this.real_ghg_disposal = real_ghg_disposal;
	}

	public double getReal_waste() {
		return real_waste;
	}

	public void setReal_waste(double real_waste) {
		this.real_waste = real_waste;
	}

	public double getReal_epr_tax() {
		return real_epr_tax;
	}

	public void setReal_epr_tax(double real_epr_tax) {
		this.real_epr_tax = real_epr_tax;
	}
}
