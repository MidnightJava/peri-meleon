package com.tamelea.pm.data;

import java.lang.reflect.Array;
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
        PMString[] allNameFieldValues = new PMString[] {
        		(PMString) data.getMemberValue(index, MemberField.FIRST_NAME),
        		(PMString) data.getMemberValue(index, MemberField.MIDDLE_NAME),
        		(PMString) data.getMemberValue(index, MemberField.LAST_NAME),
        		(PMString) data.getMemberValue(index, MemberField.NICK_NAME)
        };
        Boolean nameMatch = !nameSearch.isEmpty() && Stream.of(nameSearch.split("\\s+"))
        	.anyMatch(s ->
        		Stream.of(allNameFieldValues).anyMatch(s2 -> s2.toString().toLowerCase().equals(s.toLowerCase()))
        	);
        
        return nameMatch;
    }
	
	public String getNameSearch() {
		
		return this.nameSearch;
	
	}
	
	
}
