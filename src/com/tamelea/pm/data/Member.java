/**
 * Represents one member, past or present.
 */
package com.tamelea.pm.data;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;

@SuppressWarnings("unused")
public final class Member {
	private MemberIndex						index;
	private PMString						lastName;
	private PMString						firstName;
	private PMString						middleName;
	private PMString						maidenName;
	private PMString						suffix;
	private PMString						title;
	private PMString						nickName;
	private PMDate							dateLastChange;
	private Sex								sex;
	private PMDate							dateOfBirth;
	private PMString						placeOfBirth;
	private MemberStatus					status;
	private boolean							resident;
	private boolean							exDirectory;
	private HouseholdIndex					household;
	private AddressIndex					tempAddress;
	//Transactions usually will be wanted w.r.t. a Member.
	//For statistical reports, we'll have to gather all transactions and sort them.
	//We'd keep these sorted in a TreeMap, but Transaction Dates aren't guaranteed to be unique.
	private ArrayList<Transaction>			transactions;
	private MaritalStatus					maritalStatus;
	private PMString						spouse;
	private PMDate							dateOfMarriage;
	private PMString						divorce;
	private MemberIndex						father;
	private MemberIndex						mother;
	private PMString						eMail;
	private PMString						workEMail;
	private Phone							mobilePhone;
	private Phone							workPhone;
	private PMString						education;
	private PMString						employer;
	private PMString						baptism;
	private ArrayList<Service>				services;
	
	Member(MemberIndex index) {
		this.index = index;
		this.transactions = new ArrayList<Transaction>();
		this.services = new ArrayList<Service>();
	}
	
	Member(Element element) 
	throws PMParseException
	{
		for (MemberField field : MemberField.values()) {
			String attributeString = element.attributeValue(field.toString());
			if (attributeString != null) {
				Object fieldValue;
				try {
					fieldValue = parseAttribute(field, attributeString);
					setValue(field, fieldValue);
				} catch (ValueOfException e) {
					String reportingName = null;
					try {
						reportingName = element.attributeValue(MemberField.LAST_NAME.toString());
					} catch (Exception ignored) { }
					String message = "Member";
					if (reportingName != null && reportingName.length() > 0) message += " " + reportingName;
					message += " attribute parse failed, attrib: " + field + " data: '" 
							+ attributeString + "'";
					if (e.getCause() != null && e.getCause().getMessage() != null)
						message += " cause: " + e.getCause().getMessage();
					throw new PMParseException(message);
				}
			} else {
				setValue(field, null);
			}
		}
		this.transactions = new ArrayList<Transaction>();
		List<?> transactionElements = element.elements("transaction");
		for (Iterator<?> i = transactionElements.iterator(); i.hasNext(); ) {
			Element transactionElement = (Element)i.next();
			transactions.add(new Transaction(transactionElement));
		}
		this.services = new ArrayList<Service>();
		List<?> serviceElements = element.elements("service");
		for (Iterator<?> i = serviceElements.iterator(); i.hasNext(); ) {
			Element serviceElement = (Element)i.next();
			services.add(new Service(serviceElement));
		}
	}
	
	MemberIndex getIndex() {
		return index;
	}
	
	Object getValue(MemberField field) {
		Object value = null;
		try {
			value = field.field.get(this);
		} catch (IllegalArgumentException e) {
			throw new IllegalStateException("Member.getValue", e);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException("Member.getValue", e);
		}
		return value;
	}
	
	void setValue(MemberField field, Object value){
		if (value != null && !field.fieldClass.isAssignableFrom(value.getClass())) 
			throw new IllegalArgumentException("Member.setValue(): arg of class " + value.getClass().getName() 
					+ " not assignable to field " + field);
		try {
			field.field.set(this, value);
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"Member.setValue() failed for "+ field.name() + ": " + e.getMessage(), e);
		}
	}
	
	boolean isActive() {
		MemberStatus status = (MemberStatus)getValue(MemberField.STATUS);
		return MemberStatus.isActive(status);
	}
	
	int size() {
		return MemberField.values().length;
	}
	
	Transaction addTransaction() {
		Transaction transaction = new Transaction(index);
		transactions.add(transaction);
		return transaction;
	}
	
	/**
	 * Depends on object identity, as Transactions have no distinguishing index.
	 * @param transaction
	 */
	void remove(Transaction transaction) {
		transactions.remove(transaction);
	}
	
	List<Transaction> getTransactions() {
		return transactions;
	}
	
	Service addService() {
		Service service = new Service(index);
		services.add(service);
		return service;
	}
	
	/**
	 * Depends on object identity, as Services have no distinguishing index.
	 * @param service
	 */
	void remove(Service service) {
		services.remove(service);
	}
	
	ArrayList<Service> getServices() {
		return services;
	}
	
	private static Object parseAttribute(MemberField field, String attributeString) 
	throws ValueOfException{
		return Data.fieldValueOf(field.fieldClass, attributeString);
	}
	
	void save(Element parent) {
		DefaultElement memberElement = new DefaultElement("member");
		for (MemberField field : MemberField.values()) {
			Object value = getValue(field);
			String valueString = (value == null) ? "" : value.toString();
			memberElement.addAttribute(field.toString(), valueString);
		}
		for (Transaction transaction : transactions) {
			transaction.save(memberElement);
		}
		for (Service service : services) {
			service.save(memberElement);
		}
		parent.add(memberElement);
	}
}
