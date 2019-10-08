package index.impl;

import index.api.IIndexEntry;

import java.util.HashMap;
import java.util.Map;

public class IndexEntry implements IIndexEntry {
	private Object id;
	private long start;
	private long size;
	private final Map<String, Object> properties = new HashMap<>();


	/**
	 * @return the id
	 */
	@Override
	public Object getId() {
		return id;
	}


	/**
	 * @param id the id to set
	 */
	@Override
	public void setId(final Object id) {
		this.id = id;
	}


	/**
	 * @return the start
	 */
	@Override
	public long getStart() {
		return start;
	}


	/**
	 * @param start the start to set
	 */
	@Override
	public void setStart(final long start) {
		this.start = start;
	}


	/**
	 * @return the size
	 */
	@Override
	public long getSize() {
		return size;
	}


	/**
	 * @param size the size to set
	 */
	@Override
	public void setSize(final long size) {
		this.size = size;
	}


	@Override
	public Object getProperty(final String key) {
		return properties.get(key);
	}


	@Override
	public void setProperty(final String key, final Object value) {
		properties.put(key, value);
	}


	@Override
	public IIndexEntry clone() {
		final IndexEntry clone = new IndexEntry();
		clone.setId(getId());
		clone.setStart(getStart());
		clone.setSize(getSize());

		return clone;
	}
}
