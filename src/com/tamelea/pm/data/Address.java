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
 * An address of a family or person.
 *
 */
@SuppressWarnings("unused")
final class Address {
	private AddressIndex	index;
	private PMString		name;
	private PMString		address;
	private PMString		address2;
	private PMString		city;
	private PMString		state;
	private PMString		postalCode;
	private PMString		country;
	private PMString		eMail;
	private Phone			homePhone;
	
	Address(AddressIndex index) {
		this.index = index;
	}
	
	Address(Element element) 
	throws PMParseException
	{
		for (AddressField field : AddressField.values()) {
			String attributeString = element.attributeValue(field.toString());
			if (attributeString == null) throw new PMParseException("Attribute " + field + " missing");
			try {
				Object fieldValue = parseAttribute(field, attributeString);
				setValue(field, fieldValue);
			} catch (ValueOfException e) {
				String message = "Address";
				message += " attribute parse failed, attrib: " + field + " data: '" 
					+ attributeString + "'";
				if (e.getCause() != null && e.getCause().getMessage() != null)
					message += " cause: " + e.getCause().getMessage();
				throw new PMParseException(message);
			}
		}
	}
	
	AddressIndex getIndex() {
		return index;
	}
	
	Object getValue(AddressField field) {
		Object value = null;
		try {
			value = field.field.get(this);
		} catch (IllegalArgumentException e) {
			throw new IllegalStateException("Address.getValue", e);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException("Address.getValue", e);
		}
		return value;
	}
	
	void setValue(AddressField field, Object value){
		if (value != null && !field.fieldClass.isAssignableFrom(value.getClass())) 
			throw new IllegalArgumentException("Address.setValue(): arg of class " + value.getClass().getName() 
					+ " not assignable to field " + field);
		try {
			field.field.set(this, value);
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"Address.setValue() failed for "+ field.name() + ": " + e.getMessage(), e);
		}
	}
	
	int size() {
		return AddressField.values().length;
	}
	
	private static Object parseAttribute(AddressField field, String attributeString) 
	throws ValueOfException{
		return Data.fieldValueOf(field.fieldClass, attributeString);
	}
	
	void save(Element parent) {
		DefaultElement element = new DefaultElement("address");
		for (AddressField field : AddressField.values()) {
			Object value = getValue(field);
			String valueString = (value == null) ? "" : value.toString();
			element.addAttribute(field.toString(), valueString);
		}
		parent.add(element);
	}
}
