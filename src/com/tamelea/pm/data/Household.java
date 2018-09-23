package com.tamelea.pm.data;

import java.lang.reflect.Field;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;

/**
 * A household or family, of one or more members.
 *
 */
@SuppressWarnings("unused")
final class Household {
	private HouseholdIndex			index;
	private PMString				name;
	private MemberIndex				head;
	private MemberIndex				spouse;
	private HashSet<MemberIndex>	others;	// besides head & spouse
	private AddressIndex			address;
	
	Household(HouseholdIndex index) {
		this.index = index;
		this.others = new HashSet<MemberIndex>();
	}
	
	Household(Element element) 
	throws PMParseException
	{
		this.others = new HashSet<MemberIndex>();
		for (HouseholdField field : HouseholdField.values()) {
			String attributeString = element.attributeValue(field.toString());
			if (attributeString == null) throw new PMParseException("Attribute " + field + " missing");
			try {
				Object fieldValue = parseAttribute(field, attributeString);
				setValue(field, fieldValue);
			} catch (ValueOfException e) {
				String reportingName = null;
				try {
					reportingName = element.attributeValue(HouseholdField.NAME.toString());
				} catch (Exception ignored) { }
				String message = "Household";
				if (reportingName != null && reportingName.length() > 0) 
					message += " " + reportingName;
				message += " attribute parse failed, attrib: " + field + " data: '" 
					+ attributeString + "'";
				if (e.getCause() != null && e.getCause().getMessage() != null)
					message += " cause: " + e.getCause().getMessage();
				throw new PMParseException(message);
			}
		}
		List<?> otherElements = element.elements("other");
		for (Iterator<?> i = otherElements.iterator(); i.hasNext(); ) {
			Element otherElement = (Element)i.next();
			String string = otherElement.attributeValue("index");
			if (string == null) throw new PMParseException("no index on other element");
			others.add(new MemberIndex(Integer.parseInt(string)));
		}
	}
	
	HouseholdIndex getIndex() {
		return index;
	}
	
	Object getValue(HouseholdField field) {
		Object value = null;
		try {
			value = field.field.get(this);
		} catch (IllegalArgumentException e) {
			throw new IllegalStateException("Household.getValue", e);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException("Household.getValue", e);
		}
		return value;
	}
	
	void setValue(HouseholdField field, Object value){
		if (value != null && !field.fieldClass.isAssignableFrom(value.getClass())) 
			throw new IllegalArgumentException("Household.setValue(): arg of class " + value.getClass().getName() 
					+ " not assignable to field " + field);
		try {
			field.field.set(this, value);
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"Household.setValue() failed for "+ field.name() + ": " + e.getMessage(), e);
		}
	}
	
	int size() {
		return HouseholdField.values().length;
	}
	
	boolean containsAsOther(MemberIndex member) {
		return others.contains(member);
	}
	
	void setOtherMembership(MemberIndex member, boolean belongs) {
		if (belongs) others.add(member);
		else others.remove(member);
	}
	
	void removeMember(MemberIndex index) {
		if (head.equals(index)) head = null;
		else if (spouse.equals(index)) spouse = null;
		else others.remove(index);
	}
	
	Set<MemberIndex> getOthers() {
		return others;
	}
	
	private static Object parseAttribute(HouseholdField field, String attributeString) 
	throws ValueOfException{
		return Data.fieldValueOf(field.fieldClass, attributeString);
	}
	
	void save(Element parent) {
		DefaultElement element = new DefaultElement("household");
		for (HouseholdField field : HouseholdField.values()) {
			Object value = getValue(field);
			String valueString = (value == null) ? "" : value.toString();
			element.addAttribute(field.toString(), valueString);
		}
		for (MemberIndex adult : others) {
			DefaultElement adultElement = new DefaultElement("other");
			adultElement.addAttribute("index", Integer.toString(adult.value));
			element.add(adultElement);
		}
		parent.add(element);
	}
}
