package com.tamelea.pm.json;

import java.io.PrintStream;

import org.json.simple.JSONObject;

import com.tamelea.pm.data.IntegerIndex;
import com.tamelea.pm.data.PMDate;
import com.tamelea.pm.data.PMString;
import com.tamelea.pm.data.Phone;

public final class JS {
	
	@SuppressWarnings("unchecked")
	public static void addIndex(JSONObject obj, String fieldName, IntegerIndex fieldValue) {
		//Export index as string, rather than as number, to ease conversion from internal
		//indexes to Mongo-assigned indexes (which we treat as strings)
		obj.put(fieldName, fieldValue != null ? Integer.toString(fieldValue.value()) : null);
	}
	
	@SuppressWarnings("unchecked")
	public static void addString(JSONObject obj, String fieldName, PMString fieldValue) {
		obj.put(fieldName,  fieldValue != null ? fieldValue.toString() : null);
	}
	
	@SuppressWarnings("unchecked")
	public static void addPhone(JSONObject obj, String fieldName, Phone fieldValue) {
		obj.put(fieldName,  fieldValue != null ? fieldValue.toString() : null);
	}
	
	@SuppressWarnings("unchecked")
	public static void addDate(JSONObject obj, String fieldName, PMDate fieldValue) {
		obj.put(fieldName,  fieldValue != null ? fieldValue.toIso() : null);
	}
	
	@SuppressWarnings("rawtypes")
	public static void addEnum(PrintStream ps, String fieldName, Enum fieldValue) {
		ps.println("    \"" + fieldName + "\": " + (fieldValue != null ? "\"" + fieldValue.toString() + "\"" : "null") + ",");
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void addEnum(JSONObject obj, String fieldName, Enum fieldValue) {
		obj.put(fieldName, fieldValue != null ? fieldValue.toString() : null);
	}
	
	@SuppressWarnings("unchecked")
	public static void addBoolean(JSONObject obj, String fieldName, boolean fieldValue) {
		obj.put(fieldName, Boolean.valueOf(fieldValue));
	}

}
