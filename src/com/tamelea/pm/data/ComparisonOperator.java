package com.tamelea.pm.data;

public enum ComparisonOperator {
		LESS 			("less than"),
		LESS_EQUAL 		("less than or equal to"),
		GREATER 		("greater than"),
		GREATER_EQUAL 	("greater than or equal to"),
		EQUAL 			("equal to");
		
		private String displayValue;
		
		private ComparisonOperator(String displayValue){
			this.displayValue = displayValue;
		}
		
		public String toString(){
			return this.getDisplayValue();
		}
		
		private String getDisplayValue(){
			return this.displayValue;
		}
}
