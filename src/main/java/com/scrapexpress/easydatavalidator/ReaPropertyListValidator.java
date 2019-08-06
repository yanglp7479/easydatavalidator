package com.scrapexpress.easydatavalidator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ReaPropertyListValidator extends AbstractDataObjectValidator<PropertyList>{
	
	

	public ReaPropertyListValidator() {
		super(true);
		
		/**
		 * register sub validators for the types of Resiedential, Rental, Commercial ..
		 */
		//for residential sale
		registerDataClassesWithValidator(Residential.class, new ResidentialValidator());
		
		//for residential rental
		registerDataClassesWithValidator(Rental.class, new RentalValidator());
		
	}

	@Override
	public void validate() throws InvalidDataException{
		
		//general validations for PropertyList

		errors = new ArrayList<String>();
		
		Map<String,List<String>> objectFieldErrors = execValidateRules();
		

		if( objectFieldErrors!=null && objectFieldErrors.size() > 0){
			addErrors(objectFieldErrors );
			throw new InvalidDataException(objectFieldErrors);
		}
		
	}
	

}
