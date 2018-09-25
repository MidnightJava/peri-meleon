package com.tamelea.pm.data;

import java.time.LocalDate;
import java.time.MonthDay;
import java.util.Date;
import java.util.GregorianCalendar;

public class MemberAgeFilter implements MemberFilter {
	
	private PMDate				date;
	private ComparisonOperator	comparisonOp;
	private int					age;
	private boolean				activeOnly;
	private Data				data;
	
	public MemberAgeFilter(
			Data data, 
			PMDate date, 
			ComparisonOperator op, 
			int age, 
			boolean activeOnly)
	{
		this.data = data;
		this.date = date;
		this.comparisonOp = op;
		this.age = age;
		this.activeOnly = activeOnly;
	}
	
	public boolean match(MemberIndex index) {
		if (activeOnly){
			if (!data.isActive(index)) return false;
		}
		PMDate dob = (PMDate)data.getMemberValue(index, MemberField.DATE_OF_BIRTH);
		if (dob.getValue() == null) return false;
		int membersAge = membersConventionalAge(dob.getValue());
		switch (comparisonOp){
			case LESS:
				return membersAge < age;
			case LESS_EQUAL:
				return membersAge <= age;
			case GREATER:
				return membersAge > age;
			case GREATER_EQUAL:
				return membersAge >= age;
			case EQUAL:
				return membersAge == age;
			default:
				return false;
		}
	}
	
	/**
	 * Compute a member's "conventional age", which is incremented
	 * on their birthday.
	 * @param storedDob date of birth stored for member
	 * @return conventional age
	 */
	private int membersConventionalAge(Date storedDob) {
		GregorianCalendar storedCalendar = new GregorianCalendar();
		storedCalendar.setTime(storedDob);
		//Escape Early Java Calendar Hell
		LocalDate dob = LocalDate.of(
				storedCalendar.get(GregorianCalendar.YEAR), 
				storedCalendar.get(GregorianCalendar.MONTH) + 1, //definitions differ!
				storedCalendar.get(GregorianCalendar.DAY_OF_MONTH));
		LocalDate todaysDate = LocalDate.now();
		MonthDay dobMonthDay = MonthDay.of(dob.getMonth(), dob.getDayOfMonth());
		int age = 0;
		if (MonthDay.now().isBefore(dobMonthDay)) {
			//before birthday
			age = todaysDate.getYear() - dob.getYear() - 1;
		} else {
			//on or after birthday
			age = todaysDate.getYear() - dob.getYear();
		}
		return age;
	}
	
	public PMDate getDate(){
		return date;
	}
	
	public Integer getAge(){
		return age;
	}
	
	public ComparisonOperator getComparisonOp(){
		return comparisonOp;
	}
//	
//	public MemberField getSortField(){
//		return MemberField.getConstantValue(sortColName);
//	}
}
