package com.scrapexpress.easydatavalidator;


public class ResidentialValidator extends AbstractDataObjectValidator<Residential>{
	
	
	public ResidentialValidator(){
		super();
		//register sub validator for validating SoldDetails
		
	}
	
	public void validate() throws InvalidDataException{
		
		if(getData() != null){
			
			if("current".equalsIgnoreCase(getData().getStatus())){
				registerRule("*.price", IS_NOT_NULL);
				registerRule("*.cagtgory", Logic.and(IS_NOT_NULL, IS_VALID_CATEGORY ));
				registerRule("*.features", IS_NOT_NULL );
	
			}else if("sold".equalsIgnoreCase(getData().getStatus())){
				//soldDetails is required for sold property
//				registerRule("*.soldDetails", IS_NOT_NULL );
//				registerRule("*.priceOrSoldPrice", IS_NOT_EMPTY_COLLECTION);
//				registerRule("*.dateOrSoldDate", IS_NOT_EMPTY_COLLECTION);
//				registerRule("SoldDate.value", IS_NOT_EMPTY_STRING );
//				registerRule("SoldPrice.value", IS_GREATER_THAN_ZERO_DECIMAL_STRING );
				registerDataClassesWithValidator(SoldDetails.class, new  SoldDetailsValidator());
				
			}
		}
		
		
		super.validate();
	}
	
}