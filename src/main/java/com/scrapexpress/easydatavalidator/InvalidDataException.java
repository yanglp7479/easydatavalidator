package com.scrapexpress.easydatavalidator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

public class InvalidDataException extends Exception {

	Map<String,List<String>> errors = new LinkedHashMap<>();

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InvalidDataException() {
		super();
	}
	public InvalidDataException(Map<String,List<String>> errors) {
		this();
		setErrors(errors);
	}

	public String getMessage(){
		
		
		return StringUtils.join(getErrorMessages(), "<br>\n");
	}
	
	public List<String> getErrorMessages(){
		List<String> errorMessages = new ArrayList<String>();
		if(errors != null){
			for(String f : errors.keySet()){
				errorMessages.add(f + ": " + StringUtils.join(errors.get(f),", "));
			}	
		}
		return errorMessages;
	}

	public Map<String,List<String>> getErrors() {
		return errors;
	}

	public final void setErrors(Map<String,List<String>> errors) {
		this.errors = errors;
	}
	
	

}
