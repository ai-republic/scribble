package index.impl;

import index.api.IIndexEntry;
import index.api.IIndexSegment;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.EnumSet;
import java.util.Map;
import java.util.TreeMap;

public class Segment implements IIndexSegment {
	private FileChannel channel;
	private boolean open;
	private Map<Object, IIndexEntry> entries;


	@SuppressWarnings({ "unchecked" })
	public static Segment open(final String name) throws IOException {
		final Segment segment = new Segment();
		final Path path = Paths.get(name + ".seg");

		if (!Files.exists(path.getParent())) {
			Files.createDirectories(path.getParent());
		}
		segment.channel = FileChannel.open(path, EnumSet.of(StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE));

		if (Files.exists(path) && Files.size(path) > 0) {
			ByteBuffer bytes = ByteBuffer.allocate((int) Files.size(path));
			segment.channel.read(bytes);

			if (bytes.remaining() == 0) {
				final ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes.array()));
				try {
					segment.entries = (Map<Object, IIndexEntry>) ois.readObject();
				} catch (final ClassNotFoundException e) {
					e.printStackTrace();
					segment.entries = new TreeMap<>();
				}
				// clean up
				bytes = null;
			}
		} else {
			segment.entries = new TreeMap<>();
		}

		return segment;
	}


	@Override
	public IIndexEntry get(final Object id) throws IOException {
		return entries.get(id);
	}


	@Override
	public void add(final IIndexEntry indexEntry) throws IOException {
		entries.put(indexEntry.getId(), indexEntry);
	}


	@Override
	public void remove(final Object id) throws IOException {
		entries.remove(id);
	}


	@Override
	public void rebuild() throws IOException {
	}


	@Override
	public void close() throws IOException {
		channel.close();
	}


	/**
	 * Returns true if the segment is open.
	 * 
	 * @return the open flag
	 */
	public boolean isOpen() {
		return open;
	}
}
