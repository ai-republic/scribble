package property;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Die Klasse {@link ExtProperties} ist ein Decorator für ein {@link Properties}-Objekt.
 * <p>
 * Es bietet typensichere Methoden zum Lesen von Werten.
 */
public class ExtProperties {
  private static final Logger LOGGER = LoggerFactory.getLogger(ExtProperties.class);

  private final java.util.Properties props;

  public ExtProperties(Properties props) {
    super();
    this.props = props;
  }

  public String getProperty(String key, String defaultValue) {
    String value = internalGet(key, defaultValue);
    if (value == null) {
      return null;
    }
    return value;
  }

  public Integer getProperty(String key, Integer defaultValue) {
    String value = internalGet(key, defaultValue);
    if (value == null) {
      return null;
    }
    return Integer.parseInt(value);
  }

  public Long getProperty(String key, Long defaultValue) {
    String value = internalGet(key, defaultValue);
    if (value == null) {
      return null;
    }
    return Long.parseLong(value);
  }

  // @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "NP_BOOLEAN_RETURN_NULL", justification = "")
  public Boolean getProperty(String key, Boolean defaultValue) {
    String value = internalGet(key, defaultValue);
    if (value == null) {
      return null;
    }
    return Boolean.parseBoolean(value);
  }

  private String internalGet(String key, Object defaultValue) {
    String value;
    if (defaultValue == null) {
      value = props.getProperty(key);
    } else {
      value = props.getProperty(key, String.valueOf(defaultValue));
    }
    return value;
  }

  public static ExtProperties loadProperties(String path) throws IOException {
    LOGGER.debug(String.format("Loading properties from %s", path));
    File file = new File(path);
    InputStream in = null;
    try {
      in = new FileInputStream(file);
      java.util.Properties props = new java.util.Properties();
      props.load(in);
      LOGGER.debug(writeContentsToString(props, path));
      return new ExtProperties(props);
    } finally {
      if (in != null) {
        in.close();
      }
    }
  }

  private static String writeContentsToString(java.util.Properties props, String path)
      throws IOException, UnsupportedEncodingException {
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    props.store(bout, String.format("Contents of %s", path));
    bout.close();
    String contents = new String(bout.toByteArray(), "UTF8");
    return contents;
  }

}
