package com.unilever.rac.ui.environmentalreport;

public class ComponentData {

	private String item_name = null;
	private String item_id = null;
	private String key_country = null;
	private double quantity = 0.0;
	private double weight = 0.0;
	private double ghg_pack_matl = 0.0;
	private double ghg_disposal = 0.0;
	private double waste_to_landfill = 0.0;
	private double epr_tax = 0.0;
	private boolean is_refItem = false;
	private boolean weightMin = false;
	private boolean ghgMin = false;
	private boolean ghg_disposalMin = false;
	private boolean eprMin = false;
	private boolean wasteMin = false;
	private boolean is_primary_pack = false;
	private boolean wt_zero = false;
	private double real_ghg = 0.0; 
	private double real_ghg_disposal = 0.0;
	private double real_waste = 0.0;
	private double real_epr_tax = 0.0;
	
	ComponentData(){
		  item_name = null;
		  item_id = null;
		  key_country = null;
		  quantity = 0.0;
		  weight = 0.0;
		  ghg_pack_matl = 0.0;
		  ghg_disposal = 0.0;
		  waste_to_landfill = 0.0;
		  epr_tax = 0.0;
		  is_refItem = false;
		  weightMin = false;
		  ghgMin = false;
		  ghg_disposalMin = false;
		  eprMin = false;
		  wasteMin = false;
		  is_primary_pack = false;
		  real_ghg = 0.0;
		  real_ghg_disposal = 0.0;
		  real_waste = 0.0;
		  real_epr_tax = 0.0;
	}
	
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
	public double getWaste_to_landfill() {
		return waste_to_landfill;
	}
	public void setWaste_to_landfill(double waste_to_landfill) {
		this.waste_to_landfill = waste_to_landfill;
	}
	public double getEpr_tax() {
		return epr_tax;
	}
	public void setEpr_tax(double epr_tax) {
		this.epr_tax = epr_tax;
	}
	public boolean isIs_refItem() {
		return is_refItem;
	}
	public void setIs_refItem(boolean is_refItem) {
		this.is_refItem = is_refItem;
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
	public boolean isWasteMin() {
		return wasteMin;
	}
	public void setWasteMin(boolean wasteMin) {
		this.wasteMin = wasteMin;
	}
	public boolean is_primary_pack() {
		return is_primary_pack;
	}
	public void set_primary_pack(boolean is_primary_pack) {
		this.is_primary_pack = is_primary_pack;
	}
	public double getQuantity() {
		return quantity;
	}
	public void setQuantity(double quantity) {
		this.quantity = quantity;
	}

	public boolean isWt_zero() {
		return wt_zero;
	}

	public void setWt_zero(boolean wt_zero) {
		this.wt_zero = wt_zero;
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
