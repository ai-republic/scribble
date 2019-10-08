package store;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.EnumSet;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Default;
import javax.inject.Inject;

import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import index.api.IIndexEntry;
import store.allocation.AllocationTable;
import store.allocation.Fragment;

@Default
public class Store {
  private FileChannel channel;
  @Inject
  private Path path;
  private ObjectPool<FileChannel> readerPool;
  private final AllocationTable allocationTable = new AllocationTable();
  private boolean open = false;


  public Store() {
    open = false;
  }


  @PostConstruct
  void init() {
    final FileChannelPoolFactory factory = new FileChannelPoolFactory(path);
    final GenericObjectPoolConfig config = new GenericObjectPoolConfig();
    config.setMaxIdle(2);
    config.setMaxTotal(200);
    readerPool = new GenericObjectPool<FileChannel>(factory, config);
  }


  /**
   * Opens the store for the specified name.
   *
   * @param name the store name
   * @throws IOException if an error occurs during opening
   */
  protected synchronized void open() throws IOException {
    channel = FileChannel.open(path,
        EnumSet.of(StandardOpenOption.CREATE, StandardOpenOption.APPEND, StandardOpenOption.WRITE));
    open = true;
  }


  /**
   * Writes the specified data to the store.
   *
   * @param id the id
   * @param bytes the data
   * @return the generated {@link IIndexEntry}
   * @throws IOException if an error occurs during writing
   */
  public String write(final String id, final ByteBuffer bytes) throws IOException {
    if (!isOpen()) {
      open();
    }

    Fragment old = null;

    if (id != null) {
      old = allocationTable.findById(id);

      if (old == null || old.getId() == null) {
        throw new IOException("Specified id [" + id + "] could not be found!");
      }
    }

    final Fragment fragment = allocationTable.allocate(id, bytes.limit());

    channel.write(bytes, fragment.getStart());

    return fragment.getId();
  }


  /**
   * Reads the data for the specified id from the store.
   *
   * @param id the id
   * @return the data
   * @throws IOException if an error occurs during reading
   */
  public ByteBuffer read(final String id) throws IOException {
    if (!isOpen()) {
      open();
    }

    if (id != null) {
      final Fragment fragment = allocationTable.findById(id);

      if (fragment != null) {
        FileChannel storeReader = null;
        try {
          storeReader = readerPool.borrowObject();

          final ByteBuffer bytes = ByteBuffer.allocate((int) fragment.getSize());
          storeReader.read(bytes, fragment.getStart());

          return bytes;
        } catch (final Exception e) {
          throw new IOException(e.getMessage(), e);
        } finally {
          if (storeReader != null) {
            try {
              readerPool.returnObject(storeReader);
            } catch (final Exception e) {
              e.printStackTrace();
            }
          }
        }
      } else {
        throw new IOException("Error: Item for id='" + id + "' not found!");
      }
    }

    return null;
  }


  /**
   * Closes this store.
   */
  public void close() {
    try {
      readerPool.close();

      if (open) {
        channel.close();
        open = false;
      }
    } catch (final IOException e) {}
  }


  /**
   * Gets the {@link AllocationTable}.
   *
   * @return the {@link AllocationTable}
   */
  public AllocationTable getAllocationTable() {
    return allocationTable;
  }


  /**
   * Gets the path to the store-file.
   *
   * @return the path
   */
  public Path getPath() {
    return path;
  }


  /**
   * Returns the flag if the {@link Store} is currently open.
   *
   * @return the flag if the {@link Store} is currently open
   */
  public boolean isOpen() {
    return open;
  }

}
