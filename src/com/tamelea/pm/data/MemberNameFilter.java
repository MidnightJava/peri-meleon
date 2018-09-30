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
        PMString firstName = (PMString)data.getMemberValue(index, MemberField.FIRST_NAME);
        PMString middleName = (PMString)data.getMemberValue(index, MemberField.MIDDLE_NAME);
        PMString lastName = (PMString)data.getMemberValue(index, MemberField.LAST_NAME);
        PMString nickName = (PMString)data.getMemberValue(index, MemberField.NICK_NAME);
        Boolean nameMatch = !nameSearch.isEmpty() && Stream.of(nameSearch.split("\\s+"))
        	.anyMatch(s ->
        		s.toLowerCase().equals(firstName.toString().toLowerCase()) ||
        		s.toLowerCase().equals(middleName.toString().toLowerCase()) ||
        		s.toLowerCase().equals(lastName.toString().toLowerCase()) ||
        		s.toLowerCase().equals(nickName.toString().toLowerCase())
        	);
        
        return nameMatch;
    }
	
	public String getNameSearch() {
		
		return this.nameSearch;
	
	}
	
	
}
