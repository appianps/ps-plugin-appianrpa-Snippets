package com.appian.rpa.snippets.commons.queues.annotations;

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
public @interface AItemField {

	/**
	 * Field name
	 * 
	 * @return Field name
	 */
	String fieldName();
	
}