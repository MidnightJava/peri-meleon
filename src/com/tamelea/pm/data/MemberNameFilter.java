package com.tamelea.pm.data;

import java.util.stream.Stream;

public class MemberNameFilter implements MemberFilter {
	
	private String				nameSearch;
	private boolean				activeOnly;
	private Data				data;
	
	public MemberNameFilter(
			Data data, 
			String nameSearch,
			boolean activeOnly)
	{
		this.data = data;
		this.nameSearch = nameSearch;
		this.activeOnly = activeOnly;
	}
	
	public boolean match(MemberIndex index) {
		if (activeOnly){
			if (!data.isActive(index)) return false;
		}
		return !nameSearch.isEmpty() && Stream.of(nameSearch.split("\\s+"))
				.anyMatch(s -> data.makeDisplayName(index).toLowerCase().contains(s.toLowerCase()));
    }
	
	public String getNameSearch() {
		
		return this.nameSearch;
	
	}
	
	
}
