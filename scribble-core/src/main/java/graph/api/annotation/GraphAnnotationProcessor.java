package graph.api.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Processor to process the graph annotations to create {@link GraphNode}s or re-create domain-objects from annotated objects.
 *
 * @author Torsten Oltmanns<br>
 *         (c) Copyright 2015, ai-republic GmbH, Germany
 */
@ApplicationScoped
public class GraphAnnotationProcessor implements IGraphAnnotationProcessor {
	private static final Logger LOG = LoggerFactory.getLogger(GraphAnnotationProcessor.class);


	@Override
	public Object getAnnotatedValue(final Object obj, final Class<? extends Annotation> annotationClass) {
		try {
			for (final Field f : getFieldsOfAllSuperclasses(obj.getClass())) {
				if (f.isAnnotationPresent(annotationClass)) {
					f.setAccessible(true);
					return f.get(obj);
				}
			}
		} catch (final Exception e) {
			e.printStackTrace();
			GraphAnnotationProcessor.LOG.error("Error getting @" + annotationClass.getSimpleName() + " annotation value on class: " + obj.getClass(), e);
		}

		throw new IllegalArgumentException("No @" + annotationClass.getSimpleName() + " annotation found on class: " + obj.getClass());
	}


	@Override
	public Set<Field> getAnnotatedFields(final Object obj, final Class<? extends Annotation> annotationClass) {
		final Set<Field> fields = new HashSet<>();
		try {
			for (final Field f : getFieldsOfAllSuperclasses(obj.getClass())) {
				if (f.isAnnotationPresent(annotationClass)) {
					f.setAccessible(true);
					fields.add(f);
				}
			}
		} catch (final Exception e) {
			e.printStackTrace();
			GraphAnnotationProcessor.LOG.error("Error getting @" + annotationClass.getSimpleName() + " annotation value on class: " + obj.getClass(), e);
		}

		return fields;
	}


	/**
	 * Gets a collection of all the fields of the specified class and its superclasses.
	 * 
	 * @param clazz the class
	 * @return all the fields of the specified class and its superclasses
	 */
	@Override
	public Set<Field> getFieldsOfAllSuperclasses(final Class<?> clazz) {
		return getFieldsOfAllSuperclasses(clazz, new LinkedHashSet<Field>());
	}


	/**
	 * Gets a collection of all the fields of the specified class and its superclasses.
	 * 
	 * @param clazz the class
	 * @param classes the set of fields to keep during recursion
	 * @return all the fields of the specified class and its superclasses
	 */
	protected Set<Field> getFieldsOfAllSuperclasses(final Class<?> clazz, Set<Field> classes) {
		for (final Field field : clazz.getDeclaredFields()) {
			if ((field.getModifiers() & 0x18) != (Modifier.FINAL | Modifier.STATIC)) {
				classes.add(field);
			}
		}

		if (clazz.getSuperclass() != null) {
			classes = getFieldsOfAllSuperclasses(clazz.getSuperclass(), classes);
		}

		return classes;
	}


	@Override
	public void setAnnotatedValue(final Object obj, final Object value, final Class<? extends Annotation> annotationClass) {
		try {
			for (final Field f : getFieldsOfAllSuperclasses(obj.getClass())) {
				if (f.isAnnotationPresent(annotationClass)) {
					f.setAccessible(true);
					f.set(obj, value);
					return;
				}
			}
		} catch (final Exception e) {
			e.printStackTrace();
			GraphAnnotationProcessor.LOG.error("Error setting @" + annotationClass.getSimpleName() + " annotation value on class: " + obj.getClass(), e);
		}

		throw new IllegalArgumentException("No @" + annotationClass.getSimpleName() + " annotation found on class: " + obj.getClass());
	}


	/**
	 * Converts the specified value to the correct numeric type by checking the fields type. In case the value resembles a {@link Number} it will convert it to the fields numeric type, e.g. if the
	 * value is Integer and the field-type is Long, then the Integer will be converted to Long.
	 *
	 * @param field the field
	 * @param value the value
	 * @return the converted value or the value itself if no conversion was done
	 */
	protected Object convertToCorrectType(final Field field, final Object value) {
		if (value instanceof Number) {
			if (field.getType() == Integer.class) {
				return ((Number) value).intValue();
			} else if (field.getType() == Long.class) {
				return ((Number) value).longValue();
			} else if (field.getType() == Float.class) {
				return ((Number) value).floatValue();
			} else if (field.getType() == Double.class) {
				return ((Number) value).doubleValue();
			} else if (field.getType() == Short.class) {
				return ((Number) value).shortValue();
			} else if (field.getType() == Byte.class) {
				return ((Number) value).byteValue();
			}
		} else if (field.getType().isEnum()) {
			if (value instanceof String) {
			}
		}

		return value;
	}


	/**
	 * Gets the values of all fields of the specified object and all its superclasses.
	 * 
	 * @param object the object
	 * @return the map of fieldnames and their values
	 * @throws Exception if an exception occurs during reading
	 */
	@Override
	public Map<String, Object> getFieldValues(final Object object) throws Exception {
		final Map<String, Object> fields = new LinkedHashMap<>();
		// parse all fields of the object
		for (final Field f : getFieldsOfAllSuperclasses(object.getClass())) {
			// exclude all final static fields
			if ((f.getModifiers() & 0x18) != (Modifier.FINAL | Modifier.STATIC)) {
				f.setAccessible(true);

				fields.put(f.getName(), f.get(object));
			}
		}

		return fields;
	}


	/**
	 * Sets the values of all fields of the specified object and all its superclasses.
	 * 
	 * @param object the object
	 * @return the map of fieldnames and their values
	 * @throws Exception if an exception occurs during writing
	 */
	@Override
	public void setFieldValues(final Object object, final Map<String, Object> fields) throws Exception {
		// parse all fields of the object
		for (final Field f : getFieldsOfAllSuperclasses(object.getClass())) {
			// exclude all final static fields
			if ((f.getModifiers() & 0x18) != (Modifier.FINAL | Modifier.STATIC)) {
				f.setAccessible(true);

				f.set(object, fields.get(f.getName()));
			}
		}
	}
}
