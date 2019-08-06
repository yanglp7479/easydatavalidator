package com.scrapexpress.easydatavalidator;


public class SoldDetailsValidator extends AbstractDataObjectValidator<SoldDetails>{
	
	public SoldDetailsValidator(){
		
	}
	
	public void validate() throws InvalidDataException{
		
		if(getData() != null){
			
			registerRule("*.priceOrSoldPrice", IS_NOT_EMPTY_COLLECTION);
			registerRule("*.dateOrSoldDate", IS_NOT_EMPTY_COLLECTION);
			registerRule("SoldDate.value", IS_NOT_EMPTY_STRING );
			registerRule("SoldPrice.value", IS_GREATER_THAN_ZERO_DECIMAL_STRING );
			
		}

		super.validate();
	}
	
}