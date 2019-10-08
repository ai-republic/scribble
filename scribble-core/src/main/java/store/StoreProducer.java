package store;

import java.nio.file.Path;
import java.nio.file.Paths;

import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;

@Default
public class StoreProducer {

  @Produces
  public Path produceStorePath() {
    final String path = System.getProperty("graph.store.path");

    if (System.getProperty("graph.store.path") != null) {
      return Paths.get(path);
    }

    return Paths.get("store.bin");
  }
}
