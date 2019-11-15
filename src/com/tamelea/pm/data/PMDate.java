package com.tamelea.pm.data;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.tamelea.pm.PeriMeleon;

/**
 * Encapsulate a Date and provide the static valueOf().
 * Immutable, as usual.
 * PMDate can have a null Date, the consequence of parsing an empty String.
 * These compare properly, to support sorting Transactions by date.
 */
public final class PMDate implements Comparable<PMDate> {
	private final Date value;
	
	public PMDate() {
		this.value = null;
	}
	
	public PMDate(Date date) {
		this.value = date;
	}
	
	public PMDate(String parseString)
	throws java.text.ParseException
	{
		if (parseString.equals("")) this.value = null;
		else this.value = PeriMeleon.dateFormat.parse(parseString);
	}
	
	public PMDate(DateFormat format, String parseString)
	throws java.text.ParseException
	{
		if (parseString.equals("")) this.value = null;
		else this.value = format.parse(parseString);
	}
	
	public static PMDate getToday() {
		return new PMDate(new Date());
	}
	
	public String toString() {
		if (value == null) return "";
		return PeriMeleon.dateFormat.format(value);
	}
	
	public String toIso() {
		if (value == null) return null;
		return PeriMeleon.isoFormat.format(value);
	}
	
	public static PMDate valueOf(String parseString)
	throws java.text.ParseException
	{
		if (parseString.equals("")) return new PMDate();
		Date date = PeriMeleon.dateFormat.parse(parseString);
		return new PMDate(date);
	}
	
	public Date getValue(){
		return value;
	}
	
	public boolean isEmpty() {
		return value == null;
	}
	
	public boolean equals(Object o) {
		if (!(o instanceof PMDate)) return false;
		PMDate md = (PMDate)o;
		if (value == null) {
			return md.value == null;
		} else {
			if (md.value == null) return false;
			else return value.equals(md.value);
		}
	}
	
	public int hashCode() {
		if (value == null) return 0;
		else return value.hashCode();
	}

	public int compareTo(PMDate o) {
		if (value == null) {
			if (o.value == null) return 0;
			else return -1;
		} else {
			if (o.value == null) return 1;
			else return value.compareTo(o.value);
		}
	}
	
	public boolean after(Date date) {
		if (value == null) return false;
		return value.after(date);
	}
	
	public boolean before(Date date) {
		if (value == null) return false;
		return value.before(date);
	}
	
	public boolean isInMonth(Month month) {
		if (value == null) return false;
		//Maybe someday I'll learn why the Calendar API requires this
		//make-and-set circumlocution
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(value);
		return month.ordinal() == calendar.get(Calendar.MONTH);
	}
	
	public int getDayOfMonth() {
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(value);
		return calendar.get(Calendar.DAY_OF_MONTH);
	}
}
