package graph.api.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

public interface IGraphAnnotationProcessor {

	/**
	 * Gets the field value with the annotation from the specified object. There must only exist one annotated field with the specified annotation.
	 *
	 * @param obj the object
	 * @param annotationClass the annotation class
	 * @return the value
	 */
	Object getAnnotatedValue(final Object obj, final Class<? extends Annotation> annotationClass);


	/**
	 * Sets the field value with the annotation on the specified object. There must only exist one annotated field with the specified annotation.
	 *
	 * @param obj the object
	 * @param value the value
	 * @param annotationClass the annotation class
	 * @return the value
	 */
	void setAnnotatedValue(final Object obj, final Object value, final Class<? extends Annotation> annotationClass);


	/**
	 * Gets all fields in the specified object which are annotated with the specified annotation.
	 * 
	 * @param obj the object
	 * @param annotationClass the annotation class
	 * @return the collection of annotated {@link Field}s
	 */
	Set<Field> getAnnotatedFields(final Object obj, final Class<? extends Annotation> annotationClass);


	/**
	 * Gets a collection of all the fields of the specified class and its superclasses.
	 * 
	 * @param clazz the class
	 * @return all the fields of the specified class and its superclasses
	 */
	Set<Field> getFieldsOfAllSuperclasses(final Class<?> clazz);


	/**
	 * Gets the values of all fields of the specified object and all its superclasses.
	 * 
	 * @param object the object
	 * @return the map of fieldnames and their values
	 * @throws Exception if an exception occurs during reading
	 */
	Map<String, Object> getFieldValues(final Object object) throws Exception;


	/**
	 * Sets the values of all fields of the specified object and all its superclasses.
	 * 
	 * @param object the object
	 * @return the map of fieldnames and their values
	 * @throws Exception if an exception occurs during writing
	 */
	void setFieldValues(final Object object, final Map<String, Object> fields) throws Exception;
}
