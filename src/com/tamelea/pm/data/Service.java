package com.tamelea.pm.data;

import java.io.PrintStream;
import java.util.Date;

import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;
import org.json.simple.JSONObject;

import com.tamelea.pm.json.JS;

@SuppressWarnings("unused")
public final class Service implements Comparable<Service> {
	private MemberIndex index;
	private PMDate date;
	private ServiceType type;
	private PMString place;
	private PMString comment;
	
	Service(MemberIndex index) {
		this.index = index;
	}
	
	Service(Element element) 
	throws PMParseException
	{
		for (ServiceField field : ServiceField.values()) {
			String attributeString = element.attributeValue(field.toString());
			Object fieldValue;
			//permissive, for attributes added after the fact
			try {
				fieldValue = parseAttribute(
						field,
						(attributeString == null) ? "" : attributeString);
				setValue(field, fieldValue);
			} catch (ValueOfException e) {
				String reportingName = null;
				try {
					reportingName = element.attributeValue(ServiceField.DATE.toString());
				} catch (Exception ignored) { }
				String message = "Service record";
				if (reportingName != null && reportingName.length() > 0) 
					message += " " + reportingName;
				message += " attribute parse failed, attrib: " + field + " data: '" 
					+ attributeString + "'";
				if (e.getCause() != null && e.getCause().getMessage() != null)
					message += " cause: " + e.getCause().getMessage();
				throw new PMParseException(message);
			}
		}
	}
	
	public Object getValue(ServiceField field) {
		Object value = null;
		try {
			value = field.field.get(this);
		} catch (IllegalArgumentException e) {
			throw new IllegalStateException("Service.getValue", e);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException("Service.getValue", e);
		}
		return value;
	}
	
	void setValue(ServiceField field, Object value){
		if (value != null && !field.fieldClass.isAssignableFrom(value.getClass())) 
			throw new IllegalArgumentException("Service.setValue(): arg of class " + value.getClass().getName() 
					+ " not assignable to field " + field);
		try {
			field.field.set(this, value);
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"Service.setValue() failed for "+ field.name() + ": " + e.getMessage(), e);
		}
	}
	
	public int size() {
		return ServiceField.values().length;
	}
	
	private static Object parseAttribute(ServiceField field, String attributeString) 
	throws ValueOfException{
		return Data.fieldValueOf(field.fieldClass, attributeString);
	}
	
	void save(Element parent) {
		DefaultElement element = new DefaultElement("service");
		for (ServiceField field : ServiceField.values()) {
			Object value = getValue(field);
			String valueString = (value == null) ? "" : value.toString();
			element.addAttribute(field.toString(), valueString);
		}
		parent.add(element);
	}

	/**
	 * Support sorting a List o' Services.
	 * We'd need a proper hash and equals to support Services as Map keys.
	 */
	public int compareTo(Service t) {
		if (t.date == null) return -1;
		return this.date.compareTo(t.date);
	}
	
//	void exportJSON(PrintStream ps) {
//		ps.println("    {");
//		JS.addIndex(ps, "index", index);
//		JS.addDate(ps, "date", date);
//		JS.addEnum(ps, "type", type);
//		JS.addString(ps, "place", place);
//		JS.addStringNoComma(ps, "comment", comment);
//		ps.println("    }");
//	}
	
	JSONObject makeJSON() {
		JSONObject obj = new JSONObject();
		JS.addString(obj, "py/object", "pm_data_types.member.Service");
		JS.addIndex(obj, "_Service__index", index);
		JS.addDate(obj, "_Service__date", date);
		JS.addEnum(obj, "_Service__type", type);
		JS.addString(obj, "_Service__place", place);
		JS.addString(obj, "_Service__comment", comment);
		return obj;
	}
}
