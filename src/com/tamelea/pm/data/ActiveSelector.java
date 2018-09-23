package com.tamelea.pm.data;

public enum ActiveSelector {
	ACTIVE ("Active"),
	NON_ACTIVE ("Non Active"),
	BOTH ("Active and Non Active");
	
	private String value;

	private ActiveSelector(String value){
		this.value= value;
	}
	
	public String toString(){
		return value;
	}
}