

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * Wraps the array to use in, e.g. a map as key to implement equals() and hashCode() methods.
 * 
 * @author Torsten Oltmanns
 *
 * @param <ARRAY> the array
 */
public class ArrayWrapper<ARRAY> {
  private final ARRAY array;

  /**
   * Creates a {@link ArrayWrapper} of the specified array.
   * 
   * @param array the arry
   * @return the {@link ArrayWrapper}
   */
  public static <ARRAY> ArrayWrapper<ARRAY> wrap(final ARRAY array) {
    return new ArrayWrapper<ARRAY>(array);
  }

  /**
   * Returns <code>true</code> if the specified object is a collection of arrays of primitive types.
   * 
   * @param obj the object to check
   * @return <code>true</code> if the specified object is a collection of arrays of primitive types
   */
  public static boolean isCollectionOfPrimitiveArrays(final Object obj) {
    if (obj instanceof Collection) {
      final Collection<?> col = (Collection<?>) obj;

      if (!col.isEmpty()) {
        final Object element = col.iterator().next();
        return isPrimitiveArray(element);
      }
    }
    return false;
  }

  /**
   * Returns <code>true</code> if the specified object is an array of primitive types.
   * 
   * @param obj the object to check
   * @return <code>true</code> if the specified object is an array of primitive types
   */
  public static boolean isPrimitiveArray(final Object obj) {
    return isPrimitiveArrayClass(obj.getClass());
  }

  /**
   * Returns <code>true</code> if the specified class is an array of primitive types.
   * 
   * @param clazz the class to check
   * @return <code>true</code> if the specified class is an array of primitive types
   */
  public static boolean isPrimitiveArrayClass(final Class<?> clazz) {
    return clazz.isArray() && clazz.getComponentType().isPrimitive();
  }

  /**
   * Constructor.
   * 
   * @param array the array
   */
  public ArrayWrapper(final ARRAY array) {
    this.array = array;
  }

  @Override
  public boolean equals(final Object other) {
    if (!(other instanceof ArrayWrapper)) {
      return false;
    }

    if (array instanceof byte[]) {
      return Arrays.equals((byte[]) array, (byte[]) ((ArrayWrapper<?>) other).array);
    }

    if (array instanceof char[]) {
      return Arrays.equals((char[]) array, (char[]) ((ArrayWrapper<?>) other).array);
    }

    if (array instanceof short[]) {
      return Arrays.equals((short[]) array, (short[]) ((ArrayWrapper<?>) other).array);
    }

    if (array instanceof int[]) {
      return Arrays.equals((int[]) array, (int[]) ((ArrayWrapper<?>) other).array);
    }

    if (array instanceof long[]) {
      return Arrays.equals((long[]) array, (long[]) ((ArrayWrapper<?>) other).array);
    }

    if (array instanceof float[]) {
      return Arrays.equals((float[]) array, (float[]) ((ArrayWrapper<?>) other).array);
    }

    if (array instanceof double[]) {
      return Arrays.equals((double[]) array, (double[]) ((ArrayWrapper<?>) other).array);
    }

    return Arrays.equals((Object[]) array, (Object[]) ((ArrayWrapper<?>) other).array);
  }

  @Override
  public int hashCode() {
    if (array instanceof byte[]) {
      return Arrays.hashCode((byte[]) array);
    }

    if (array instanceof char[]) {
      return Arrays.hashCode((char[]) array);
    }

    if (array instanceof short[]) {
      return Arrays.hashCode((short[]) array);
    }

    if (array instanceof int[]) {
      return Arrays.hashCode((int[]) array);
    }

    if (array instanceof long[]) {
      return Arrays.hashCode((long[]) array);
    }

    if (array instanceof float[]) {
      return Arrays.hashCode((float[]) array);
    }

    if (array instanceof double[]) {
      return Arrays.hashCode((double[]) array);
    }

    return Arrays.hashCode((Object[]) array);
  }


  public static void main(final String[] args) {
    final ArrayList<byte[]> list = new ArrayList<byte[]>();
    list.add(new byte[] {97});
    isCollectionOfPrimitiveArrays(list);
  }
}
