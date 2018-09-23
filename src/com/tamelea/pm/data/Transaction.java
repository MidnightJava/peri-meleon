/**
 * Change in membership status.
 */
package com.tamelea.pm.data;

import java.util.Date;

import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;

@SuppressWarnings("unused")
public final class Transaction implements Comparable<Transaction> {
	private MemberIndex 	index;
	private PMDate 	date;
	private TransactionType	type;
	private PMString 	authority; //BCO ref--better name?
	private PMString 	church;
	private PMString 	comment;
	
	Transaction(MemberIndex index) {
		this.index = index;
	}
	
	Transaction(Element element) 
	throws PMParseException
	{
		for (TransactionField field : TransactionField.values()) {
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
					reportingName = element.attributeValue(TransactionField.DATE.toString());
				} catch (Exception ignored) { }
				String message = "Transaction";
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
	
	/**
	 * Editor needs access.
	 * @param field
	 * @return
	 */
	public Object getValue(TransactionField field) {
		Object value = null;
		try {
			value = field.field.get(this);
		} catch (IllegalArgumentException e) {
			throw new IllegalStateException("Transaction.getValue", e);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException("Transaction.getValue", e);
		}
		return value;
	}
	
	void setValue(TransactionField field, Object value){
		if (value != null && !field.fieldClass.isAssignableFrom(value.getClass())) 
			throw new IllegalArgumentException("Transaction.setValue(): arg of class " + value.getClass().getName() 
					+ " not assignable to field " + field);
		try {
			field.field.set(this, value);
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"Transaction.setValue() failed for "+ field.name() + ": " + e.getMessage(), e);
		}
	}
	
	public int size() {
		return TransactionField.values().length;
	}
	
	private static Object parseAttribute(TransactionField field, String attributeString) 
	throws ValueOfException{
		return Data.fieldValueOf(field.fieldClass, attributeString);
	}
	
	void save(Element parent) {
		DefaultElement element = new DefaultElement("transaction");
		for (TransactionField field : TransactionField.values()) {
			Object value = getValue(field);
			String valueString = (value == null) ? "" : value.toString();
			element.addAttribute(field.toString(), valueString);
		}
		parent.add(element);
	}

	/**
	 * Support sorting a List o' Transactions.
	 * We'd need a proper hash and equals to support Transactions as Map keys.
	 */
	public int compareTo(Transaction t) {
		if (t.date == null) return -1;
		return this.date.compareTo(t.date);
	}
}
