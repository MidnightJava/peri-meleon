package com.tamelea.pm.data;

import java.util.function.Predicate;
import java.util.stream.Stream;

public class MemberNameFilter implements MemberFilter {
	
	private String				nameSearch;
	private boolean				activeOnly;
	private Data				data;
	private boolean 			matchAny;
	
	public MemberNameFilter(
			Data data, 
			String nameSearch,
			boolean activeOnly,
			boolean matchAny)
	{
		this.data = data;
		this.nameSearch = nameSearch;
		this.activeOnly = activeOnly;
		this.matchAny = matchAny;
	}
	
	public boolean match(MemberIndex index) {
		if (activeOnly){
			if (!data.isActive(index)) return false;
		}
		Predicate<String> p = s -> data.makeDisplayName(index).toLowerCase().contains(s.toLowerCase());
		if (matchAny) {
			return !nameSearch.isEmpty() && Stream.of(nameSearch.split("\\s+"))
				.anyMatch(p);
		} else {
			return !nameSearch.isEmpty() && Stream.of(nameSearch.split("\\s+"))
		        .allMatch(p);
		}
    }
	
	public boolean getMatchAny() {
		return this.matchAny;
	}
	
	public String getNameSearch() {
		
		return this.nameSearch;
	
	}
	
	
}
