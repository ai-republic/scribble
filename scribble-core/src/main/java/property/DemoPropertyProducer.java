package property;

import java.io.IOException;

import javax.annotation.Nullable;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CDI-Producer für die Erzeugung von Property-Werten in dieser Anwendung.
 * <p>
 * Die Werte werden aus einer Properties-Datei (siehe {@link #SYSTEM_PROPERTY_WITH_PATH_TO_PROPERTIES_FILE}) gelesen.
 * <p>
 * <b>Beispiel "Den Wert eines 'nullable' Property definieren":</b>
 * 
 * <pre><code>  
  public class SampleCdiBean {
    {@literal @}Inject
    {@literal @}Property("myFirstPropertyKey", nullable=true)
    private Integer myFirstProperty;
  }
 * </code></pre> Hier wird aus der Properties-Datei der Wert für den Schlüssel 'myFirstPropertyKey' ausgelesen und in
 * das Feld 'myFirstProperty' geschrieben. Sollte der Wert nicht definiert sein, so wird das Feld mit <code>null</code>
 * belegt.
 * <p>
 * <b>Beispiel "Den Wert eines 'non-nullable' Property definieren":</b>
 * 
 * <pre><code>  
  public class SampleCdiBean {    
  
    {@literal @}Inject
    {@literal @}Property("mySecondPropertyKey", nullable=false)
    private int mySecondProperty;
  }
 * </code></pre> Hier wird aus der Properties-Datei der Wert für den Schlüssel 'mySecondPropertyKey' ausgelesen und in
 * das Feld 'mySecondProperty' geschrieben. Sollte der Wert nicht definiert sein, so wird eine
 * NullableDependencyException geworfen.
 * <p>
 * <b>Beispiel "Den Wert eines 'nullable' Property mittels Setter-Methode definieren":</b>
 * 
 * <pre><code>  
  public class SampleCdiBean {    
    private int myThirdProperty = 123; // Hier ist ein Default-Value hinterlegt!
    {@literal @}Inject
    {@literal @}Property("myThirdPropertyKey", nullable=true)
    private void setMyThirdProperty(Integer value) {
      if ( value != null) {
        myThirdProperty = value;
      }
    }
  }
 * </code></pre> Hier wird aus der Properties-Datei der Wert für den Schlüssel 'myThirdPropertyKey' ausgelesen und beim
 * Aufruf der Methode 'setMyThirdProperty' als Argument 'value' übergeben. Sollte der Wert nicht definiert sein, so wird
 * <code>null</code> übergeben. Auf diese Art und Weise kann man beispielsweise sicherstellen, dass der (im Code
 * definierte) Default-Wert nur dann überschrieben wird, wenn der Wert tatsächlich in der Properties-Datei eingetragen
 * ist.
 */
@ApplicationScoped
public class DemoPropertyProducer extends AbstractPropertyProducer {
  private static final Logger LOGGER = LoggerFactory.getLogger(DemoPropertyProducer.class);
  /**
   * Name des Sytem-Property (aka "Minus-D-Parameter"), das den Dateipfad zur Properties-Datei enthält.
   */
  private static final String SYSTEM_PROPERTY_WITH_PATH_TO_PROPERTIES_FILE = "demoapp.properties";

  public DemoPropertyProducer() throws IOException {
    super(LOGGER, ExtProperties.loadProperties(System.getProperty(SYSTEM_PROPERTY_WITH_PATH_TO_PROPERTIES_FILE)));
  }

  @Override
  @Produces
  @Property("")
  @Nullable
  public Integer produceNullableIntegerProperty(InjectionPoint ip) {
    return super.produceNullableIntegerProperty(ip);
  }

  @Override
  @Produces
  @Property(value = "", nullable = false)
  public int produceIntegerProperty(InjectionPoint ip) {
    return super.produceIntegerProperty(ip);
  }

  @Override
  @Produces
  @Property("")
  @Nullable
  public Long produceNullableLongProperty(InjectionPoint ip) {
    return super.produceNullableLongProperty(ip);
  }

  @Override
  @Produces
  @Property(value = "", nullable = false)
  public long produceLongProperty(InjectionPoint ip) {
    return super.produceNullableLongProperty(ip);
  }

  @Override
  @Produces
  @Property("")
  @Nullable
  public long[] produceNullableLongArrayProperty(InjectionPoint ip) {
    return super.produceNullableLongArrayProperty(ip);
  }

  @Override
  @Produces
  @Property(value = "", nullable = false)
  public long[] produceLongArrayProperty(InjectionPoint ip) {
    return super.produceLongArrayProperty(ip);
  }

  @Override
  @Produces
  @Property("")
  @Nullable
  public Boolean produceNullableBooleanProperty(InjectionPoint ip) {
    return super.produceNullableBooleanProperty(ip);
  }

  @Override
  @Produces
  @Property(value = "", nullable = false)
  public boolean produceBooleanProperty(InjectionPoint ip) {
    return super.produceBooleanProperty(ip);
  }

  @Override
  @Produces
  @Property("")
  @Nullable
  public String produceNullableStringProperty(InjectionPoint ip) {
    return super.produceNullableStringProperty(ip);
  }

  @Override
  @Produces
  @Property(value = "", nullable = false)
  public String produceStringProperty(InjectionPoint ip) {
    return super.produceStringProperty(ip);
  }

  @Override
  @Produces
  @Property("")
  @Nullable
  public String[] produceNullableStringArrayProperty(InjectionPoint ip) {
    return super.produceNullableStringArrayProperty(ip);
  }

  @Override
  @Produces
  @Property(value = "", nullable = false)
  public String[] produceStringArrayProperty(InjectionPoint ip) {
    return super.produceStringArrayProperty(ip);
  }

}
