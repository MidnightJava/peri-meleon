package com.tamelea.pm.data;

public class SexFilter implements MemberFilter{
	
	private Sex sex;
	private Data data;
	
	public SexFilter(Sex sex, Data data){
		this.sex = sex;
		this.data = data;
	}
	
	public boolean match(MemberIndex index){
		Sex memberSex = (Sex)data.getMemberValue(index, MemberField.SEX);
		if (memberSex == sex || memberSex == null ){
			return true;
		}
		return false;
	}

}
