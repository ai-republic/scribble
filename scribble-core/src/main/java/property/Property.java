package property;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.enterprise.util.Nonbinding;
import javax.inject.Qualifier;

/**
 * Der {@link Property}-Qualifier wird zum Qualifizieren von Properties verwendet.
 */
@Qualifier
@Target({TYPE, METHOD, PARAMETER, FIELD})
@Retention(RUNTIME)
public @interface Property {
  /**
   * Der Schlüssel (Key) dieses Property, wie es in der Properties-Datei eingetragen ist.
   * 
   * @return
   */
  @Nonbinding
  String value();

  /**
   * Legt fest, ob der Inhalt dieses Property <code>null</code> sein darf.
   * 
   * @return <code>true</code>, wenn der Inhalt <code>null</code> sein darf
   */
  boolean nullable() default true;
}
