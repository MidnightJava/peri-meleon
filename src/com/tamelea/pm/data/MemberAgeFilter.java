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
		Date dobBeginDate = new Date(dobValue.getTimeInMillis());
		GregorianCalendar dobEndValue = new GregorianCalendar();
		dobEndValue.setTime(dobBeginDate);
		dobEndValue.add(Calendar.YEAR, 1);
		dobEndValue.add(Calendar.DAY_OF_YEAR, -1);
		dobEndValue.set(Calendar.HOUR_OF_DAY, 23);
		dobEndValue.set(Calendar.MINUTE, 59);
		dobEndValue.set(Calendar.SECOND, 59);
		Date dobEndDate = new Date(dobEndValue.getTimeInMillis());
		GregorianCalendar baseDate = new GregorianCalendar();
		baseDate.setTime(date.getValue());
		baseDate.add(Calendar.YEAR, -age);
		baseDate.set(Calendar.HOUR_OF_DAY, 0);
		baseDate.set(Calendar.MINUTE, 0);
		Date compareDate = new Date(baseDate.getTimeInMillis());
		switch (comparisonOp){
			case LESS:
				return dobBeginDate.after(compareDate);
			case LESS_EQUAL:
				return (dobBeginDate.after(compareDate) ||  dobBeginDate.equals(compareDate));
			case GREATER:
				return dobBeginDate.before(compareDate);
			case GREATER_EQUAL:
				return (dobBeginDate.before(compareDate) ||  dobBeginDate.equals(compareDate));
			case EQUAL:
				return ((dobBeginDate.before(compareDate) || dobBeginDate.equals(compareDate)) && 
						(dobEndDate.after(compareDate) || dobEndDate.equals(compareDate)));
		}
		return false;
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
