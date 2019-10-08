package classloader;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ReflectionUtil {
	private static final Class<?>[] ARRAY_PRIMITIVE_TYPES = { int[].class, float[].class, double[].class, boolean[].class, byte[].class, short[].class, long[].class, char[].class };


	public static <D> Collection<Class<? extends D>> getClassesInPackage(final String packageName, final Class<D> ofType, final Class<? extends Annotation> withAnnotation) {

		try {
			final List<Class<?>> classes = (new ClassFinder(ofType.getClassLoader())).getClasses(packageName);

			final List<Class<? extends D>> result = new ArrayList<>();
			for (final Class<?> cls : classes) {
				if (cls != null && ofType.isAssignableFrom(cls) && cls.getAnnotation(withAnnotation) != null) {

					@SuppressWarnings("unchecked")
					final Class<? extends D> castedCls = (Class<? extends D>) cls;
					result.add(castedCls);
				}
			}
			return result;
		} catch (final ClassNotFoundException e) {
			throw new UndeclaredThrowableException(e);
		} catch (final IOException e) {
			throw new UndeclaredThrowableException(e);
		}
	}


	public static Collection<Class<?>> getClassesInPackage(final String packageName) {
		try {
			return (new ClassFinder(ReflectionUtil.class.getClassLoader())).getClasses(packageName);
		} catch (final ClassNotFoundException e) {
			throw new UndeclaredThrowableException(e);
		} catch (final IOException e) {
			throw new UndeclaredThrowableException(e);
		}
	}


	/**
	 * Converts the given inputArray into an array of type Object[]. This is intended to be uses for primitive arrays. It the inputArray is already of type Object[] then no conversion is done and the
	 * inputArray is returned.
	 * 
	 * @param inputArray
	 * @return Object[]
	 */
	public static Object[] toObjectArray(final Object inputArray) {
		final Class<?> inputArrayClass = inputArray.getClass();
		if (!inputArrayClass.isArray()) {
			throw new IllegalArgumentException(String.format("inputArray must be an array but class is %s!", inputArrayClass));
		}
		Object[] outputArray = null;

		for (final Class<?> arrKlass : ARRAY_PRIMITIVE_TYPES) {
			if (inputArrayClass.isAssignableFrom(arrKlass)) {
				final int arrlength = Array.getLength(inputArray);
				outputArray = new Object[arrlength];
				for (int i = 0; i < arrlength; ++i) {
					outputArray[i] = Array.get(inputArray, i);
				}
				break;
			}
		}
		if (outputArray == null) {
			outputArray = (Object[]) inputArray;
		}

		return outputArray;
	}
}
