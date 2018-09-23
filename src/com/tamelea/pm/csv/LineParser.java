package com.tamelea.pm.csv;

/**
 * Parse a line of comma-separated values.
 * Assumes no newlines in values, regrettably.
 * (This means it won't work with data exported from Outlook.)
 * Values may contain commas, in which case they are surrounded by double quotes.
 *
 */
public class LineParser {
	private String line;
	private int index;
	
	public LineParser(String line) {
		this.line = line;
		this.index = 0;
	}
	
	public String next() {
		//Assume we're at beginning of a token
		StringBuffer token = new StringBuffer();
		if (line.charAt(index) == '"') {
			++index; //step past quote
//			token.append('"');
			while (line.charAt(index) != '"') {
				token.append(line.charAt(index));
				++index;
			}
			++index; //step past quote
//			token.append('"');
			if (index < line.length() && line.charAt(index) == ',')
				++index;  //step past comma
		} else {
			while (index < line.length() && line.charAt(index) != ',') {
				token.append(line.charAt(index));
				++index;
			}
			if (index < line.length()) ++index; //step past comma
		}
		return token.toString();
	}
	
	public boolean hasNext() {
		return index < line.length();
	}
}
