package com.raghu;


public class PollutionReportRequest {
 
	private String land_polluted = "";
	private String land_area = "";
	private String land_type = "";
	private String land_pollutants;
	private String water_polluted = "";
	private String water_area = "";
	private String water_body = "";
	private String water_pollutants;
	private String do_upstream = "";
	private String do_downstream = "";
	private String do_upstream_image = "";
	private String do_downstream_image = "";
	private String ammonia = "";
	private String fish_kill = "";
	private String indicative_cause = "";
	private String failed_asset = "";
	private String equipment_deployed = "";
	
	public String getLand_polluted() {
		return land_polluted;
	}
	public void setLand_polluted(String land_polluted) {
		this.land_polluted = land_polluted;
	}
	public String getLand_area() {
		return land_area;
	}
	public void setLand_area(String land_area) {
		this.land_area = land_area;
	}
	public String getLand_type() {
		return land_type;
	}
	public void setLand_type(String land_type) {
		this.land_type = land_type;
	}
	public String getLand_pollutants() {
		return land_pollutants;
	}
	public void setLand_pollutants(String land_pollutants) {
		this.land_pollutants = land_pollutants;
	}
	public String getWater_polluted() {
		return water_polluted;
	}
	public void setWater_polluted(String water_polluted) {
		this.water_polluted = water_polluted;
	}
	public String getWater_area() {
		return water_area;
	}
	public void setWater_area(String water_area) {
		this.water_area = water_area;
	}
	public String getWater_body() {
		return water_body;
	}
	public void setWater_body(String water_body) {
		this.water_body = water_body;
	}
	public String getWater_pollutants() {
		return water_pollutants;
	}
	public void setWater_pollutants(String water_pollutants) {
		this.water_pollutants = water_pollutants;
	}
	public String getDo_upstream() {
		return do_upstream;
	}
	public void setDo_upstream(String do_upstream) {
		this.do_upstream = do_upstream;
	}
	public String getDo_downstream() {
		return do_downstream;
	}
	public void setDo_downstream(String do_downstream) {
		this.do_downstream = do_downstream;
	}
	public String getDo_upstream_image() {
		return do_upstream_image;
	}
	public void setDo_upstream_image(String do_upstream_image) {
		this.do_upstream_image = do_upstream_image;
	}
	public String getDo_downstream_image() {
		return do_downstream_image;
	}
	public void setDo_downstream_image(String do_downstream_image) {
		this.do_downstream_image = do_downstream_image;
	}
	public String getAmmonia() {
		return ammonia;
	}
	public void setAmmonia(String ammonia) {
		this.ammonia = ammonia;
	}
	public String getFish_kill() {
		return fish_kill;
	}
	public void setFish_kill(String fish_kill) {
		this.fish_kill = fish_kill;
	}
	public String getIndicative_cause() {
		return indicative_cause;
	}
	public void setIndicative_cause(String indicative_cause) {
		this.indicative_cause = indicative_cause;
	}
	public String getFailed_asset() {
		return failed_asset;
	}
	public void setFailed_asset(String failed_asset) {
		this.failed_asset = failed_asset;
	}
	public String getEquipment_deployed() {
		return equipment_deployed;
	}
	public void setEquipment_deployed(String equipment_deployed) {
		this.equipment_deployed = equipment_deployed;
	}
}
