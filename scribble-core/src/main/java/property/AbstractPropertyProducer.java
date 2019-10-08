package property;


import java.lang.reflect.Array;
import java.util.Arrays;

import javax.annotation.Nullable;
import javax.enterprise.inject.spi.InjectionPoint;

import org.assertj.core.util.VisibleForTesting;
import org.slf4j.Logger;

/**
 * Basisklasse für anwendungsspezifische Property-Producer.
 *
 */
public abstract class AbstractPropertyProducer {
  private final Logger logger;

  private final ExtProperties props;

  /**
   * Parameter-less public constructor for CDI.
   */
  public AbstractPropertyProducer() {
    this(null, null);
  }

  public AbstractPropertyProducer(Logger logger, ExtProperties props) {
    this.logger = logger;
    this.props = props;
  }

  @Nullable
  public Integer produceNullableIntegerProperty(InjectionPoint ip) {
    String key = getKey(ip);
    Integer result = props.getProperty(key, (Integer) null);
    log(ip, result);
    return result;
  }

  public int produceIntegerProperty(InjectionPoint ip) {
    String key = getKey(ip);
    Integer result = props.getProperty(key, (Integer) null);
    if (result == null) {
      throw new IllegalStateException(String.format("Can't produce property! No property defined for key %s.", key));
    }
    log(ip, result);
    return result;
  }

  @Nullable
  public Long produceNullableLongProperty(InjectionPoint ip) {
    String key = getKey(ip);
    Long result = props.getProperty(key, (Long) null);
    log(ip, result);
    return result;
  }

  public long produceLongProperty(InjectionPoint ip) {
    String key = getKey(ip);
    Long result = props.getProperty(key, (Long) null);
    if (result == null) {
      throw new IllegalStateException(String.format("Can't produce property! No property defined for key %s.", key));
    }
    log(ip, result);
    return result;
  }

  @Nullable
  public long[] produceNullableLongArrayProperty(InjectionPoint ip) {
    String key = getKey(ip);
    String preResult = props.getProperty(key, (String) null);
    long[] result = createLongArray(preResult);
    log(ip, result);
    return result;
  }

  public long[] produceLongArrayProperty(InjectionPoint ip) {
    String key = getKey(ip);
    String preResult = props.getProperty(key, (String) null);
    if (preResult == null) {
      throw new IllegalStateException(String.format("Can't produce property! No property defined for key %s.", key));
    }
    long[] result = createLongArray(preResult);
    log(ip, result);
    return result;
  }

  @Nullable
  public static long[] createLongArray(@Nullable String preResult) {
    if (preResult == null) {
      return null;
    }
    String[] array = preResult.split("[ ]*,[ ]*");
    long[] result = new long[array.length];
    int i = 0;
    for (String string : array) {
      result[i++] = Long.parseLong(string);
    }
    return result;
  }

  @Nullable
  public Boolean produceNullableBooleanProperty(InjectionPoint ip) {
    String key = getKey(ip);
    Boolean result = props.getProperty(key, (Boolean) null);
    log(ip, result);
    return result;
  }

  public boolean produceBooleanProperty(InjectionPoint ip) {
    String key = getKey(ip);
    Boolean result = props.getProperty(key, (Boolean) null);
    if (result == null) {
      throw new IllegalStateException(String.format("Can't produce property! No property defined for key %s.", key));
    }
    log(ip, result);
    return result;
  }

  @Nullable
  public String produceNullableStringProperty(InjectionPoint ip) {
    String key = getKey(ip);
    String result = props.getProperty(key, (String) null);
    log(ip, result);
    return result;
  }

  public String produceStringProperty(InjectionPoint ip) {
    String key = getKey(ip);
    String result = props.getProperty(key, (String) null);
    if (result == null) {
      throw new IllegalStateException(String.format("Can't produce property! No property defined for key %s.", key));
    }
    log(ip, result);
    return result;
  }

  @Nullable
  public String[] produceNullableStringArrayProperty(InjectionPoint ip) {
    String key = getKey(ip);
    String preResult = props.getProperty(key, (String) null);
    String[] result = createStringArray(preResult);
    log(ip, result);
    return result;
  }

  public String[] produceStringArrayProperty(InjectionPoint ip) {
    String key = getKey(ip);
    String preResult = props.getProperty(key, (String) null);
    if (preResult == null) {
      throw new IllegalStateException(String.format("Can't produce property! No property defined for key %s.", key));
    }
    String[] result = createStringArray(preResult);
    log(ip, result);
    return result;
  }

  @VisibleForTesting
  @Nullable
  public static String[] createStringArray(@Nullable String preResult) {
    if (preResult == null) {
      return null;
    }
    if (preResult.trim().length() == 0) {
      return new String[0];
    }
    String[] result = preResult.split("[ ]*,[ ]*");
    return result;
  }

  private String getKey(InjectionPoint ip) {
    String key = ip.getAnnotated().getAnnotation(Property.class).value();
    return key;
  }

  private void log(InjectionPoint ip, Object value) {
    if (logger.isDebugEnabled()) {
      logger.debug(String.format("producing property %s: %s", ip, toString(value)));
    }
  }

  private String toString(Object obj) {
    if (obj == null) {
      return null;
    } else if (obj.getClass().isArray()) {
      Object[] array = toObjectArray(obj);
      return Arrays.toString(array);
    } else {
      return String.valueOf(obj);
    }
  }

  private static final Class<?>[] ARRAY_PRIMITIVE_TYPES = {int[].class, float[].class, double[].class, boolean[].class,
      byte[].class, short[].class, long[].class, char[].class};

  /**
   * Converts the given inputArray into an array of type Object[]. This is intended to be uses for primitive arrays. It
   * the inputArray is already of type Object[] then no conversion is done and the inputArray is returned.
   * 
   * @param inputArray
   * @return Object[]
   */
  private static Object[] toObjectArray(Object inputArray) {
    if (inputArray == null) {
      throw new NullPointerException("inputArray must not be null!");
    }

    Class<?> inputArrayClass = inputArray.getClass();

    if (!inputArrayClass.isArray()) {
      throw new IllegalArgumentException(
          String.format("inputArray must be an array but class is %s!", inputArrayClass));
    }

    Object[] outputArray = null;

    for (Class<?> arrKlass : ARRAY_PRIMITIVE_TYPES) {
      if (inputArrayClass.isAssignableFrom(arrKlass)) {
        int arrlength = Array.getLength(inputArray);
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
