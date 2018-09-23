package com.tamelea.pm.nhpc;

import java.text.SimpleDateFormat;
import java.util.regex.Pattern;

final class DateFormatRecord {
	SimpleDateFormat format;
	Pattern pattern;
	
	DateFormatRecord(String formatString, String patternString) {
		format = new SimpleDateFormat(formatString);
		pattern = Pattern.compile(patternString);
	}
}
