package com.appian.rpa.snippets.commons.excel.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to indicate the queue item field name that correspond
 * to the annotated field.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface AExcelField {

	/**
	 * Field name
	 * 
	 * @return Field name
	 */
	String fieldName();

	/**
	 * Create new column in the excel
	 * 
	 * @return True if you want to create a new column
	 */
	boolean createColumn() default false;

}
