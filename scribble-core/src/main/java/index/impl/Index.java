package index.impl;

import index.api.IIndex;
import index.api.IIndexEntry;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class Index implements IIndex {
	private Path path;
	private final Map<String, Segment> cache = new HashMap<>();


	public Index() {
	}


	public Index(final Path path) {
		this.path = path;
	}


	@Override
	public void open() throws IOException {
	}


	@Override
	public void add(final IIndexEntry indexEntry) throws IOException {
		final Segment segment = getSegmentForId(indexEntry.getId());
		segment.add(indexEntry);
	}


	@Override
	public IIndexEntry find(final Object id) throws IOException {
		final Segment segment = getSegmentForId(id);
		return segment.get(id);
	}


	@Override
	public void remove(final Object id) throws IOException {
		final Segment segment = getSegmentForId(id);
		segment.remove(id);
	}


	@Override
	public void close() throws IOException {
	}


	public Segment getSegmentForId(final Object id) throws IOException {
		final String name = path.toString() + "/" + ("0" + id.hashCode()).substring(0, 2);
		Segment segment = cache.get(name);

		if (segment == null) {
			segment = Segment.open(name);
			cache.put(name, segment);
		}

		return segment;
	}

}
