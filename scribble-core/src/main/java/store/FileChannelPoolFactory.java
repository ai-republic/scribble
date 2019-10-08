package store;

import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.EnumSet;

import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;

public class FileChannelPoolFactory implements PooledObjectFactory<FileChannel> {
	private Path path;


	public FileChannelPoolFactory() {
	}


	public FileChannelPoolFactory(final Path path) {
		this.path = path;
	}


	@Override
	public PooledObject<FileChannel> makeObject() throws Exception {
		return new DefaultPooledObject<FileChannel>(FileChannel.open(path, EnumSet.of(StandardOpenOption.READ)));
	}


	@Override
	public void destroyObject(final PooledObject<FileChannel> p) throws Exception {
		p.getObject().close();
	}


	@Override
	public boolean validateObject(final PooledObject<FileChannel> p) {
		return p.getObject().isOpen();
	}


	@Override
	public void activateObject(final PooledObject<FileChannel> p) throws Exception {
	}


	@Override
	public void passivateObject(final PooledObject<FileChannel> p) throws Exception {
	}

}
