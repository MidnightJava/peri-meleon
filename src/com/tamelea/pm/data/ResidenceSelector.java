package com.tamelea.pm.data;

public enum ResidenceSelector {
	RESIDENTS ("Residents"),
	NON_RESIDENTS ("Non Residents"),
	BOTH ("Residents and Non Residents");
	
	private String value;

	private ResidenceSelector(String value){
		this.value= value;
	}
	
	public String toString(){
		return value;
	}
}


