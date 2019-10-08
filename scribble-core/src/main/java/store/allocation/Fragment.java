package store.allocation;

public class Fragment implements Cloneable {
	private FragmentType type = FragmentType.FREE;
	private long start;
	private long end;
	private String id;


	Fragment() {
	}


	Fragment(final long start, final long end) {
		this.start = start;
		this.end = end;
	}


	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}


	/**
	 * @param id the id to set
	 */
	public void setId(final String id) {
		this.id = id;
	}


	/**
	 * @return the type
	 */
	public FragmentType getType() {
		return type;
	}


	/**
	 * @param type the type to set
	 */
	void setType(final FragmentType type) {
		this.type = type;
	}


	/**
	 * @return the start
	 */
	public long getStart() {
		return start;
	}


	/**
	 * @param start the start to set
	 */
	public void setStart(final long start) {
		this.start = start;
	}


	/**
	 * @return the end
	 */
	public long getEnd() {
		return end;
	}


	/**
	 * @param end the end to set
	 */
	public void setEnd(final long end) {
		this.end = end;
	}


	public long getSize() {
		return end - start + 1;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Fragment [id=" + id + ", type=" + type + ", start=" + start + ", end=" + end + ", size=" + getSize() + "]";
	}


	@Override
	protected Fragment clone() {
		final Fragment clone = new Fragment(start, end);
		clone.setId(getId());
		clone.setType(getType());
		return clone;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (end ^ (end >>> 32));
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + (int) (start ^ (start >>> 32));
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Fragment other = (Fragment) obj;
		if (end != other.end) {
			return false;
		}
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		if (start != other.start) {
			return false;
		}
		if (type != other.type) {
			return false;
		}
		return true;
	}
}
