package com.scrapexpress.easydatavalidator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public interface DataValidator<E> {
	
	public DataValidator<E> setIndex(int index);
	
	public DataValidator<E>  setData(Object data);
	
	public DataValidator<E> registerRule(String objectFieldname,Operator rule);
	public DataValidator<E> registerDataClassesWithValidator(Class<?> clazz,DataValidator<?> validator);
	
	public E getData();
	
	
	
	public void validate() throws InvalidDataException;
	

	public interface Operator{
		
		public List<String> eval(Object target);
	}
	
	public class Logic implements Operator{
		
		public static Logic and(Operator...ops){
			return new Logic().doAnd(ops);
		}
		
		public static Logic or(Operator...ops){
			return new Logic().doOr(ops);
		}
		
		protected String operateType="AND";
		protected List<Operator> operators = new LinkedList<>();
		
		private Logic doAnd( Operator...ops){
			operateType = "AND";
			operators.addAll(Arrays.asList( ops ) );
			return this;
		}
		
		private  Logic doOr( Operator...ops){
			operateType = "OR";
			operators.addAll(Arrays.asList( ops ) );
			return this;
		}
		
		public List<String> eval(Object target){
			List<String> errors = new ArrayList<String>();
			if(operators.size() > 0 ){
				
				
				for(Operator op :operators){
					if(op == null ){
						continue;
					}
					
					List<String> operrors = op.eval(target);
					
					
					if("AND".equals(operateType) && (operrors != null && operrors.size() > 0)){
						String operrorMessage = StringUtils.join(operrors,", ");
						errors.add(operrorMessage);
						break;
					}
					
					if("OR".equals(operateType)){
						if(operrors == null || operrors.size() == 0){
							errors.clear();
							break;
						}else{
							String operrorMessage = StringUtils.join(operrors,", ");
							errors.add(operrorMessage);
						}	
					}			
				}		
			}
			
			if(errors.size() > 1 ){
				String errorMessage = "( " + StringUtils.join( errors , " "+operateType+" ") +" )";
				errors.clear();
				errors.add(errorMessage);
				
				
			}
			
			return errors;
		}
		
	}
	

	public abstract class ValidateRule implements Operator{
		
		protected List<String> buildErrors(String error){
			List<String> errors = new ArrayList<String>();
			if(!StringUtils.isEmpty( error )){
				errors.add(error);
			}
			return errors;
		}
		
		public abstract List<String>  eval(Object value);
		
		
		//tool functions
		public static String isNotIntegerString(String value){
			boolean is = false;
			if(value != null ){
				try {
					Integer.valueOf((String)value);
					is = true;
				} catch (NumberFormatException e) {
					
				}
			}
			if(! is){
				return "is not valid integer !";
			}
			
			return null;
		}
		
		public static String isDecimalGreaterThanZeroString(String src){
			String error = null;
			boolean greaterThanZero = false;
			
				try {
					if(new BigDecimal(src).compareTo(BigDecimal.ZERO) > 0){
						greaterThanZero =  true;
					}
				} catch (Exception e) {
					
				}
			
			
			if(! greaterThanZero){
				error ="must be decimal greater than 0 !";
			}
			
			return error;
		}
		
		
		public static String isIntegerGreaterThanZeroString(String src){
			String error = isNotIntegerString( src);
			if(error == null){
				boolean greaterThanZero = false;
				try {
					if(Integer.valueOf(src) > 0){
						greaterThanZero =  true;
					}
				} catch (NumberFormatException e) {
					
				}
				
				if(! greaterThanZero){
					error ="must be greater than 0 !";
				}
			}
			return error;
		}
		
		public static String isNotNumberString(String src){
			if(StringUtils.isEmpty( src ) || !StringUtils.isNumeric(src.replace(".", ""))){
				return "is not valid number !";
			}
			return null;
		}
		
		public static String isNotEmptyString(String src){
			if(StringUtils.isEmpty( src)){
				return "can not be empty ! ";
			}
			return null;
		}
		public static String isNotNull( Object src){
			if(src == null){
				return "can not be empty ! ";
			}
			return null;
		}
		
		public static String isNotSplittedBySpace(String src){
			if(! StringUtils.isEmpty( src) && src.split("\\s").length > 1){
				return "can not be splitted by space ! ";
			}
			return null;
		}
	}
	
	//rule : is not splitted by space
	public static final ValidateRule IS_NOT_SPLITTED_BY_SPACE = new ValidateRule() {

		@Override
		public List<String>  eval(Object value) {
			
			return buildErrors(isNotSplittedBySpace((String)value));
		}		
	};
	
	//rule : is  null
	public static final ValidateRule IS_NULL = new ValidateRule() {

		@Override
		public List<String>  eval(Object value) {
			
			return buildErrors(value != null ? "must be null" : null);
		}		
	};
	
	//rule : is not null
	public static final ValidateRule IS_NOT_NULL = new ValidateRule() {

		@Override
		public List<String>  eval(Object value) {
			
			return buildErrors(isNotNull(value));
		}		
	};

	//rule : 
	public static final ValidateRule IS_NOT_EMPTY_STRING = new ValidateRule() {

		
		@Override
		public List<String> eval(Object value) {
			
			return  buildErrors(isNotEmptyString((String)value));
		}
	};
	
	public static final ValidateRule IS_INTEGER_STRING = new ValidateRule() {

		@Override
		public List<String> eval(Object value) {
			return buildErrors(isNotIntegerString((String)value));
		}
	};
	
	public static final ValidateRule IS_GREATER_THAN_ZERO_INTEGER_STRING = new ValidateRule() {

		@Override
		public List<String> eval(Object value ) {
			
			return buildErrors(isIntegerGreaterThanZeroString((String)value));
		}
	};
	
	public static final ValidateRule IS_GREATER_THAN_ZERO_DECIMAL_STRING = new ValidateRule() {

		@Override
		public List<String> eval(Object value ) {
			
			return buildErrors(isDecimalGreaterThanZeroString((String)value));
		}
	};
	
	public static final ValidateRule IS_NOT_ZERO_INTEGER_STRING = new ValidateRule() {

		@Override
		public List<String> eval(Object value ) {
			boolean is = false;
			if(value != null ){
				try {
					is = Integer.valueOf((String)value) == 0;
				} catch (NumberFormatException e) {
					
				}
			}
			return buildErrors(is ? "can not be 0!":null);
		}
	};
	
	
	public static final ValidateRule IS_ZERO_INTEGER_STRING = new ValidateRule() {

		@Override
		public List<String> eval(Object value ) {
			boolean is = false;
			if(value != null ){
				try {
					is = Integer.valueOf((String)value) == 0;
				} catch (NumberFormatException e) {
					
				}
			}
			return buildErrors(!is ? "should be 0!":null);
		}
	};
	
	public static final ValidateRule IS_NOT_EMPTY_COLLECTION = new ValidateRule() {

		@Override
		public List<String> eval(Object value ) {
			boolean isempty = true;
			if(value != null ){
				//is collection
				if(  value instanceof Collection<?>){
					isempty = ((Collection<?>)value).size() == 0;
				}
				//is array
				else if(value.getClass().isArray()){
				
					isempty = ((Object[]) value).length == 0;
				}
				
			}
			return buildErrors(isempty ? "can not be empty collection!":null);
		}
	};
}
