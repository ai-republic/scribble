package cditest;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.AfterClass;


public class CDITestBase {
  private final static Weld weld = new Weld();
  private final static WeldContainer container = weld.initialize();

  public static <T> T inject(final Class<T> clazz) {
    return container.instance().select(clazz).get();
  }

  @AfterClass
  public static void shutDownCDIContainer() {
    weld.shutdown();
  }
}
