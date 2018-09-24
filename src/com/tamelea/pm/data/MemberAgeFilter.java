package com.tamelea.pm.data;

import java.util.Calendar;
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
		GregorianCalendar dobValue = new GregorianCalendar();
		dobValue.setTime(dob.getValue());
		dobValue.set(Calendar.HOUR_OF_DAY, 0);
		dobValue.set(Calendar.MINUTE, 0);
		Date dobDate = new Date(dobValue.getTimeInMillis());
		GregorianCalendar dobPlusOneYearValue = new GregorianCalendar();
		dobPlusOneYearValue.setTime(dobDate);
		dobPlusOneYearValue.add(Calendar.YEAR, 1);
		dobPlusOneYearValue.add(Calendar.DAY_OF_YEAR, -1);
		dobPlusOneYearValue.set(Calendar.HOUR_OF_DAY, 23);
		dobPlusOneYearValue.set(Calendar.MINUTE, 59);
		dobPlusOneYearValue.set(Calendar.SECOND, 59);
		Date dobPlusOneYearDate = new Date(dobPlusOneYearValue.getTimeInMillis());
		GregorianCalendar baseDate = new GregorianCalendar();
		baseDate.setTime(date.getValue());
		baseDate.add(Calendar.YEAR, -age);
		baseDate.set(Calendar.HOUR_OF_DAY, 0);
		baseDate.set(Calendar.MINUTE, 0);
		Date compareDate = new Date(baseDate.getTimeInMillis());
		switch (comparisonOp){
			case LESS:
				return dobDate.after(compareDate);
			case LESS_EQUAL:
				return (dobDate.after(compareDate) ||  dateInBirthYear(compareDate, dobDate, dobPlusOneYearDate));
			case GREATER:
				return dobDate.before(compareDate);
			case GREATER_EQUAL:
				return (dobDate.before(compareDate) ||  dateInBirthYear(compareDate, dobDate, dobPlusOneYearDate));
			case EQUAL:
				return dateInBirthYear(compareDate, dobDate, dobPlusOneYearDate);
		}
		return false;
	}
	
	public PMDate getDate(){
		return date;
	}
	
	private boolean dateInBirthYear(Date compareDate, Date dob, Date dobPlusOneYear) {
		return ((dob.before(compareDate) || dob.equals(compareDate)) && 
				(dobPlusOneYear.after(compareDate) || dobPlusOneYear.equals(compareDate)));
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
