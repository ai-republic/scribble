package store;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class StoreTest {
  private final static Weld weld = new Weld();
  private final static WeldContainer cdiContainer = StoreTest.weld.initialize();
  private Store store;

  @Before
  public void setup() throws IOException {
    store = StoreTest.cdiContainer.instance().select(Store.class).get();
  }

  @After
  public void tearDown() throws IOException {
    store.close();
    Files.delete(store.getPath());
  }

  @AfterClass
  public static void destroy() {
    StoreTest.weld.shutdown();
  }

  @Test
  public void multipleWriteRead() throws IOException {
    ByteBuffer bytes = ByteBuffer.wrap("Hello World!".getBytes());
    final String id = store.write(null, bytes);
    bytes = store.read(id);
    Assert.assertEquals("Hello World!", new String(bytes.array()));

    bytes = ByteBuffer.wrap("Hello World - again!".getBytes());
    final String returnedId = store.write(id, bytes);
    bytes = store.read(id);
    Assert.assertEquals(id, returnedId);
    Assert.assertEquals("Hello World - again!", new String(bytes.array()));

    bytes = ByteBuffer.wrap("Hello World - with more bytes!".getBytes());
    store.write(id, bytes);
    bytes = store.read(id);
    Assert.assertEquals("Hello World - with more bytes!", new String(bytes.array()));

    bytes = ByteBuffer.wrap("Hello World!".getBytes());
    store.write(id, bytes);
    bytes = store.read(id);
    Assert.assertEquals("Hello World!", new String(bytes.array()));
  }

}
