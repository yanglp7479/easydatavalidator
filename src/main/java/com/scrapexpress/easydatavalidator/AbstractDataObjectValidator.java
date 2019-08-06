package com.scrapexpress.easydatavalidator;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

public abstract class AbstractDataObjectValidator<E> implements DataValidator<E>{
	private static Logger logger = Logger.getLogger(AbstractDataObjectValidator.class);
	
	private static List<Class<?>> toValidateObjectlasses = new ArrayList<Class<?>>();
	
	
	
	
	
	protected boolean isRecurring = true;
	private  Map<String, Operator> fieldValueValidationRules = new HashMap<String, Operator>();
	
	protected List<String> errors  = new ArrayList<String>();
	
	protected E reaObject;
	
	protected Map<Class<?>,DataValidator<?>> subDataValidators = new LinkedHashMap<>();
	
	private Class<?> entityClass  = (Class<?>) ((ParameterizedType) (getClass().getGenericSuperclass())).getActualTypeArguments()[0];
	
	private int index  = -1;
	
	public AbstractDataObjectValidator(){
		
	}
	
	
	public AbstractDataObjectValidator(boolean recurring){
		this.isRecurring = recurring;
	}
	
	{
		
		//Common rules for all types of properties including residential, rental, commercial ...
		fieldValueValidationRules.put("*.commercialOrLandOrRentalOrHolidayRentalOrResidentialOrRural", IS_NOT_EMPTY_COLLECTION);
		
		fieldValueValidationRules.put("*.agentID", IS_NOT_SPLITTED_BY_SPACE);
		fieldValueValidationRules.put("*.carSpaces", Logic.or(IS_NULL, IS_GREATER_THAN_ZERO_INTEGER_STRING));
		fieldValueValidationRules.put("ListingAgent.id", Logic.and(IS_NOT_NULL, IS_VALID_LISTING_AGENT_ID));
		fieldValueValidationRules.put("Img.id", Logic.and(IS_NOT_NULL, IS_VALID_IMAGE_ID));
		//reaxmlFieldValueValidationRules.put("Img.format", Logic.and(IS_NOT_NULL, IS_VALID_IMAGE_FORMAT));
		fieldValueValidationRules.put("Floorplan.id", Logic.and(IS_NOT_NULL, IS_VALID_FLOORPLAN_ID));
		
		
		fieldValueValidationRules.put("Price.value",  IS_GREATER_THAN_ZERO_DECIMAL_STRING);
		fieldValueValidationRules.put("Features.bathrooms",  Logic.or(IS_NULL, IS_ZERO_INTEGER_STRING,IS_GREATER_THAN_ZERO_INTEGER_STRING));
		fieldValueValidationRules.put("Features.carports",  Logic.or(IS_NULL, IS_GREATER_THAN_ZERO_INTEGER_STRING, IS_BOOLEAN_STRING));
		
		
	}
	
	final public DataValidator<E> registerDataObjectClassesOfXmlRegistry(Object xmlRegistryObjectFactory){
		if(xmlRegistryObjectFactory != null){
			for(Method m : xmlRegistryObjectFactory.getClass().getDeclaredMethods()){
				
				toValidateObjectlasses.add(m.getReturnType());
			}	
		}
			
		return this;		
		
	}
	
	final public DataValidator<E> registerDataClassesWithValidator(Class<?> clazz,DataValidator<?> validator){
		toValidateObjectlasses.add(clazz);
		if( validator != null){
			subDataValidators.put(clazz, validator);
		}
		
		return this;
	}
	
	
	final public AbstractDataObjectValidator<E> registerRule(String objectFieldname,Operator rule){
		fieldValueValidationRules.put(objectFieldname, rule);
		return this;
	}
	
	public AbstractDataObjectValidator<E> setIndex(int index){
		this.index = index;
		return this;
	}
	
	@SuppressWarnings("unchecked")
	public AbstractDataObjectValidator<E> setData(Object reaObject){
		if(reaObject != null && entityClass.isInstance(reaObject)){
			this.reaObject =(E)reaObject;
		}else{
			logger.error("Can not apply type of "+ reaObject.getClass().getName() + " to "+ entityClass.getName());
		}
		
		return this;
	}
	
	public E getData(){
		return this.reaObject;
	}
	
	public List<String> getErrors() {
		return errors;
	}
	
	/**
	 * validate object with default rules
	 */
	public void validate() throws InvalidDataException{
		errors = new ArrayList<String>();
		Map<String,List<String>> fieldErrors = execValidateRules() ;
		if( fieldErrors != null && fieldErrors.size() > 0){
			addErrors(fieldErrors);
			throw new InvalidDataException(fieldErrors);
		}
	}
	
	/**
	 * validate object with specified rules
	 * @param rules
	 * @throws InvalidDataException
	 */
	public void validate(Map<String,Operator> rules) throws InvalidDataException{
		Map<String,List<String>> fieldErrors = execValidateRules(rules) ;
		if( fieldErrors!=null && fieldErrors.size() > 0){
			addErrors(fieldErrors);
			throw new InvalidDataException(fieldErrors);
		}
		
	}
	
	/**
	 * execute the default rules
	 * @return
	 */
	final protected Map<String,List<String>> execValidateRules(){
		
		return execValidateRules( fieldValueValidationRules );
	}
	
	/**
	 * execute the specified rules
	 * @param rules
	 * @return
	 */
	final protected Map<String,List<String>> execValidateRules(Map<String,Operator> rules){
		Map<String,List<String>> objectFielderrors = new LinkedHashMap<String, List<String>>();
		List<String> errors = DataValidator.IS_NOT_NULL.eval(reaObject);
		if(errors!= null && errors.size() > 0 ){
			objectFielderrors.put(entityClass.getSimpleName(), errors);
		}else{
			objectFielderrors = execValidateRules(reaObject, entityClass.getSimpleName(), rules);
		}
		
		return objectFielderrors;
	}
	
	
	
	final protected String addError(String error){
		if(!StringUtils.isEmpty( error )){
			this.errors.add(error);
		}
		return error;
	}
	final protected List<String> addErrors(List<String> errors){
		if(errors != null){
			this.errors.addAll(errors);
		}
		return errors;
	}
	
	final protected List<String> addErrors(Map<String,List<String>> fielderrors){
		List<String> errors = new ArrayList<String>();
		if(fielderrors != null){
			for(String field : fielderrors.keySet()){
				errors.add(field +" "+ StringUtils.join(fielderrors.get(field)," and "));
			}
			
		}
		addErrors(errors);
		return errors;
	}
	
	
	
	//rule
	public static final ValidateRule HAS_REQUIRED_FIELDS_HAVE_VALUES = new ValidateRule() {

		@Override
		public List<String> eval(Object value) {
			List<String> errors = new ArrayList<String>();
			if(value != null){
				
				List<String> fields = lookupEmptyRequiredFields(value,value.getClass().getSimpleName()) ;
				if(fields != null && fields.size() > 0 ){
					errors.add("required details are missed !");
				}
			}
			
			return errors;
		}
		
	};

	
	
	
	
	
	
	//rule
	public static final ValidateRule IS_VALID_LISTING_AGENT_ID = new ValidateRule() {

		@Override
		public List<String> eval(Object value) {
			List<String> errors = new ArrayList<String>();
			if(value != null){
				String id = (String) value;
				
				if(! Arrays.asList(new String[]{"1","2","3","4"}).contains(id)){
					errors.add("must be one of 1,2,3 and 4 !" );
				}
				
			}
			return errors;		
		}		
	};
	
	//rule
	public static final ValidateRule IS_VALID_IMAGE_ID = new ValidateRule() {

		@Override
		public List<String> eval(Object value) {
			List<String> errors = new ArrayList<String>();
			if(value != null){
				String id = (String) value;
				
				if(StringUtils.isEmpty(id ) 
						|| id.length()!=1 
						||(id.charAt(0) < 'a' || id.charAt(0)>'z')){
					errors.add("must be in the range a through z !" );
				}
				
			}
			return errors;		
		}		
	};
	
	//rule
	public static final ValidateRule IS_VALID_IMAGE_FORMAT = new ValidateRule() {

		@Override
		public List<String> eval(Object value) {
			List<String> errors = new ArrayList<String>();
			if(value != null){
				String format = (String) value;
				
				if(!"jpg".equalsIgnoreCase(format)){
					errors.add("image must be jpg format!" );
				}
				
			}
			return errors;		
		}		
	};
	
	//rule
	public static final ValidateRule IS_VALID_FLOORPLAN_ID = new ValidateRule() {

		@Override
		public List<String> eval(Object value) {
			List<String> errors = new ArrayList<String>();
			if(value != null){
				String id = (String) value;
				
				if(StringUtils.isEmpty(id ) 
						|| id.length()!=1 
						||(id.charAt(0) < '1' || id.charAt(0)>'2')){
					errors.add("must be either 1 or 2 !" );
				}
				
			}
			return errors;		
		}		
	};
	
	
	
	//rule : 
	public static final ValidateRule IS_BOOLEAN_STRING = new ValidateRule() {

		
		@Override
		public List<String> eval(Object value) {
			
			return  buildErrors(Arrays.asList(new String[]{
					"yes","no","true","false","0","1"
					}).contains(String.valueOf(value).toLowerCase()) ? null : "is not valid boolean value !");
		}
	};
	
	
	
	private  Map<String,List<String>> execValidateRules(Object obj, String objName,Map<String,Operator> rules){
		
		
		return execValidateRules(obj, objName, this.index, rules);
	}
	
	/**
	 * CORE FUNCTION !!!!
	 * 
	 * Iterate all of fields of specified object, choose and execute rules for them  
	 * 
	 * @param obj
	 * @param objName
	 * @param index
	 * @param rules
	 * @return
	 */
	private Map<String,List<String>> execValidateRules(Object obj, String objName, int index,Map<String,Operator> rules){
		
		

		
		Map<String,List<String>> objectFielderrors = new LinkedHashMap<String, List<String>>();
		
		if(obj != null ){
			//

			/**
			 * check if there is a subvalidtor on current object 
			 * yes, then do sub validator
			 */
			DataValidator<?> subValidator = subDataValidators.get(obj.getClass());
			if(subValidator != null ){
				subValidator.setData(obj);
				try {
					subValidator.setIndex(index).validate();
					
				} catch (InvalidDataException e) {
					return e.getErrors();
				}
			}
			

			/**
			 * iterate all of fields of current object and do validation against each field if there is a rule on this field
			 */
			if(rules == null || rules.size() == 0 ){
				return objectFielderrors;
			}

			Field[] fields = obj.getClass().getDeclaredFields();
			for(Field f : fields){
				
				String fname = f.getName();
		
				//field data type
				Type ftype = f.getType();
				Type fgenerictype = f.getGenericType();
				if(fgenerictype != null && fgenerictype instanceof ParameterizedType){
					ftype =((ParameterizedType) fgenerictype).getActualTypeArguments()[0];
				}
	
				//get value
				f.setAccessible(true);
				Object fvalue = null;
				try {
					fvalue = f.get(obj);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					
				}
				
				String objectFieldName = objName+"." + fname;
				
				Operator op = null;
				
				//choose ValidateRule for required field declared in XML
				if(isRequiredField(f) && ! ((Class<?>) ftype).isPrimitive()){
					if(ftype.equals(String.class)){
						op = IS_NOT_EMPTY_STRING;
					}else{
						op = IS_NOT_NULL;
					}
				}

				//choose ValidateRule from reaxmlFieldValueValidationRules by object.fieldname
				Operator registeredop = rules.get(objectFieldName);
				if(registeredop == null){
					registeredop = rules.get( "*."+fname);
				}
				
				if(op == null){
					op = registeredop;
				}else if(op != null && registeredop != null){
					op = Logic.and(op, registeredop);
				}

				List<String> errors = null;
				//if the ValidateRule of current object field exists, then do validation against the field
				if( op != null){
					errors = ( op.eval(fvalue));
				}
				
				
				//keep errors of validation
				if(errors != null && errors.size() > 0){
					if(index < 0){
						objectFielderrors.put(objectFieldName, errors);
					}else{
						objectFielderrors.put(objName+"["+index+"]"+"."+fname, errors);
					}
					
					if(! isRecurring ){
						return objectFielderrors;
					}
					
				}	
				
				//if current value is not null and current field is the object or collection's element which belongs to reaClasses list
				//then execute validate rule for this object.
				if( fvalue != null && (toValidateObjectlasses.contains(ftype) || ftype.equals(Object.class))){
					
					
					//is collection
					if(  fvalue instanceof Collection<?>){
						int childindex = 0;
						for(Object e : (Collection<?>)fvalue){
							if(e == null ){
								continue;
							}
							Map<String,List<String>> childObjectFieldErrors = execValidateRules(e, e.getClass().getSimpleName(), childindex ++,rules );
							objectFielderrors.putAll(childObjectFieldErrors);
						}
					}
					//is array
					else if(fvalue.getClass().isArray()){
						int childindex = 0;
						for(Object e : (Object[]) fvalue){
							if(e == null ){
								continue;
							}
							Map<String,List<String>> childObjectFieldErrors = execValidateRules(e, e.getClass().getSimpleName(), childindex ++,rules);
							objectFielderrors.putAll(childObjectFieldErrors);
						}	
					}
					//is one of reaClasses
					else{
						Map<String,List<String>> childObjectFieldErrors = execValidateRules(fvalue, fvalue.getClass().getSimpleName(), -1,rules );
						objectFielderrors.putAll(childObjectFieldErrors);
					}
					
					if(! isRecurring && objectFielderrors.size() > 0){
						return objectFielderrors;
					}	
				}		
			}		
		}
		
		return objectFielderrors;
		
	}

	
	private boolean isRequiredField(Field f){
//		boolean required = false;
//		XmlElement fXmlElement = f.getAnnotation(XmlElement.class);
//		if(fXmlElement != null){
//			required = fXmlElement.required();
//		}else {
//			XmlAttribute fXmlAttribute = f.getAnnotation(XmlAttribute.class);
//			if(fXmlAttribute != null){
//				required = fXmlAttribute.required();
//			}
//		}
//		return required;
		
		return false;
	}
	
	@Deprecated
	protected List<String> validateValueFieldIsNotNull(){
		List<String> errors = new ArrayList<String>();
		List<String> names = lookupNullValueFields(reaObject, reaObject.getClass().getSimpleName());
		if(names != null){
			for(String name : names){
				errors.add(name +"'s value can not be empty!");
			}
		}
		this.errors.addAll( errors);
		return errors;
	}
	
	@Deprecated
	protected List<String> validateRequiredFields(){
		List<String> errors = new ArrayList<String>();
		List<String> names = lookupEmptyRequiredFields(reaObject, reaObject.getClass().getSimpleName());
		if(names != null){
			for(String name : names){
				errors.add(name +"'s value is required!");
			}
		}
		this.errors.addAll( errors);
		return errors;
	}
	
	
	
	
	public static List<String> lookupNullValueFields(Object obj, String objName){
		
		List<String> nameswithnullvalue = new ArrayList<String>();
		
		if(obj != null && ! obj.getClass().isPrimitive()){
			//
			
			Field[] fields = obj.getClass().getDeclaredFields();
			for(Field f : fields){
				Type ftype = f.getType();
				Type fgenerictype = f.getGenericType();
				if(fgenerictype != null && fgenerictype instanceof ParameterizedType){
					ftype =((ParameterizedType) fgenerictype).getActualTypeArguments()[0];
				}
				//only process the fields named value and typed with one of reaclasses
				if((f.getName().equals("value") && f.getType().equals(String.class))
						|| toValidateObjectlasses.contains(ftype)){
					
					
					//get value
					f.setAccessible(true);
					
					Object fvalue = null;
					try {
						fvalue = f.get(obj);
					} catch (IllegalArgumentException | IllegalAccessException e) {
						logger.error(e);
					}
						
					//is value field
					if(f.getName().equals("value") && f.getType().equals(String.class)){
						
						if(StringUtils.isEmpty((String) fvalue )){
							nameswithnullvalue.add(objName);
						}
					}else if(fvalue != null){
						
						if( fvalue instanceof Collection<?>){
							int index = 0;
							for(Object e : (Collection<?>)fvalue){
								nameswithnullvalue.addAll(lookupNullValueFields(e,f.getName() +"-"+ (index++)));
							}	
						}
						//is array
						else if( fvalue.getClass().isArray()){
							int index = 0;
							for(Object e : (Object[]) fvalue){
								nameswithnullvalue.addAll(lookupNullValueFields(e,f.getName() + (index++)));
							}
							
							
						}else{
							nameswithnullvalue.addAll(lookupNullValueFields(fvalue,f.getName()));
						}
					}
				}
				
			}
			
		}
		
		return nameswithnullvalue;
		
	}
	
	public static List<String> validateRequiredFields(Object obj, String objName){
		List<String> errors = new ArrayList<String>();
		List<String> names = lookupEmptyRequiredFields(obj, objName);
		if(names != null){
			for(String name : names){
				errors.add(name +"'s value is required!");
			}
		}
		
		return errors;
		
	}
	
	
	public static List<String> lookupEmptyRequiredFields(Object obj, String objName){
		
		List<String> nameswithnullvalue = new ArrayList<String>();
		
		if(obj != null ){
			//
			
			Field[] fields = obj.getClass().getDeclaredFields();
			for(Field f : fields){
				
				//field is required or not
				boolean required = false;
				XmlElement fXmlElement = f.getAnnotation(XmlElement.class);
				if(fXmlElement != null){
					required = fXmlElement.required();
				}else {
					XmlAttribute fXmlAttribute = f.getAnnotation(XmlAttribute.class);
					if(fXmlAttribute != null){
						required = fXmlAttribute.required();
					}
				}
				
				//field data type
				Type ftype = f.getType();
				Type fgenerictype = f.getGenericType();
				if(fgenerictype != null && fgenerictype instanceof ParameterizedType){
					ftype =((ParameterizedType) fgenerictype).getActualTypeArguments()[0];
				}
				
	
				//get value
				f.setAccessible(true);
				
				Object fvalue = null;
				try {
					fvalue = f.get(obj);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
				//required and empty value
				if(required 
						&& 
						( fvalue==null 
							|| ( f.getType().equals(String.class) && StringUtils.isEmpty((String)fvalue))
						)
					){
					
						nameswithnullvalue.add(objName + ":" + f.getName());
					
				}else if(fvalue != null 
						&& !fvalue.getClass().isPrimitive()
						&& toValidateObjectlasses.contains(ftype)){
					
					if( fvalue instanceof Collection<?>){
						int index = 0;
						for(Object e : (Collection<?>)fvalue){
							nameswithnullvalue.addAll(lookupEmptyRequiredFields(e,f.getName() + "-" + (index++)));
						}
						
						
					}
					//is array
					else if( fvalue.getClass().isArray()){
						int index = 0;
						for(Object e : (Object[]) fvalue){
							nameswithnullvalue.addAll(lookupEmptyRequiredFields(e,f.getName() + "-" + (index++)));
						}
						
						
					}else{
						nameswithnullvalue.addAll(lookupEmptyRequiredFields(fvalue,f.getName()));
					}
				}	
				
			}
			
		}
		
		return nameswithnullvalue;
		
	}
	
	
	

	public static final ValidateRule isZeroNumber(){
		return IS_ZERO_INTEGER_STRING;
	}
	
	public static final ValidateRule isNotEmptyColletion(){
		return IS_NOT_EMPTY_COLLECTION;
	}
	
	
	
	

	
}
