package store.allocation;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AllocationTable {
	private final static Logger LOG = LoggerFactory.getLogger(AllocationTable.class);
	private static Comparator<Long> comparator = (o1, o2) -> {
		if (o1.equals(o2)) {
			return 0;
		}
		return o1.compareTo(o2);
	};
	private final TreeMap<Long, Fragment> fragments = new TreeMap<Long, Fragment>(AllocationTable.comparator);
	private final TreeMap<Long, Fragment> freeSpaces = new TreeMap<Long, Fragment>(AllocationTable.comparator);
	private final Map<String, Fragment> ids = new HashMap<>();
	private final Queue<String> freeIds = new LinkedBlockingQueue<>();
	private final AtomicLong id_generator = new AtomicLong(0L);


	/**
	 * Allocates a free space for the specified size. If there is no matching free space, then a space at the end will be allocated.
	 *
	 * @param size the size
	 * @return the allocated {@link Fragment}
	 */
	public Fragment allocate(final String id, final long size) {
		synchronized (fragments) {

			final Map.Entry<Long, Fragment> entry = freeSpaces.ceilingEntry(size);
			long start, end;

			// check if there is a free fragment for the requested size
			if (entry != null && entry.getKey() >= size) {
				start = entry.getValue().getStart();
			} else {
				// if not, allocate at the end of file
				final Map.Entry<Long, Fragment> lastentry = fragments.lastEntry();

				// check if its a fresh and clean new table
				if (lastentry == null) {
					start = 0;
				} else {
					start = lastentry.getValue().getEnd() + 1;
				}
			}

			end = start + size - 1;

			final Fragment fragment = new Fragment(start, end);
			fragment.setType(FragmentType.FREE);

			block(id, fragment);

			return fragment;
		}
	}


	/**
	 * Gets the {@link Fragment} at the specified position. The position does not necessarily need to be the start but must be contained by the fragment to locate.
	 *
	 * @param position the position
	 * @return the {@link Fragment} at the position or null if none was found
	 */
	public Fragment findByPosition(final long position) {
		final Fragment fragment = fragments.get(position);

		if (fragment == null) {
			final Map.Entry<Long, Fragment> previousEntry = fragments.floorEntry(position);
			return previousEntry.getValue().clone();
		}

		return fragment.clone();
	}


	/**
	 * Frees the specified fragment, possibly joining it together with previous or following free spaces.
	 *
	 * @param fragment the fragment to free
	 */
	public void free(final Fragment fragment) {
		synchronized (fragments) {
			final Map.Entry<Long, Fragment> previousEntry = fragments.floorEntry(fragment.getStart() - 1);
			final Fragment allocatedFragment = fragments.get(fragment.getStart());
			final Map.Entry<Long, Fragment> nextEntry = fragments.ceilingEntry(fragment.getStart() + 1);
			boolean extendPrevious = false;
			boolean extendNext = false;

			// check if previous entry exists
			if (previousEntry != null && previousEntry.getValue().getType() == FragmentType.FREE) {
				final Fragment previous = previousEntry.getValue();

				// check if fragment begins at the end of the previous free
				// fragment
				if (fragment.getStart() - 1 <= previous.getEnd()) {
					extendPrevious = true;
					fragment.setStart(previous.getStart());

					// check if previous fragment overlaps the fragment end -
					// should not happen
					if (fragment.getEnd() < previous.getEnd()) {
						// extend fragment end to the end of the previous
						// fragment
						fragment.setEnd(previous.getEnd());
						AllocationTable.LOG.warn("End of previous free fragment overlaps fragment");
					}
				}
			}

			// check if next entry exists
			if (nextEntry != null && nextEntry.getValue().getType() == FragmentType.FREE) {
				final Fragment next = nextEntry.getValue();

				// check if the fragment ends at the next free fragment start
				if (fragment.getEnd() + 1 >= next.getStart()) {
					fragment.setEnd(next.getEnd());

					// check if fragment end overlaps the end of the next
					// fragment - should not happen
					if (fragment.getEnd() > next.getEnd()) {
						AllocationTable.LOG.warn("Fragment overlaps end of next free fragment");
					}

					extendNext = true;
				}
			}

			// remove the original allocated fragment
			if (allocatedFragment != null) {
				removeFragment(allocatedFragment);
			}

			// check if the previous fragment was extended
			if (extendPrevious) {
				// then remove previous fragment
				removeFragment(previousEntry.getValue());
			}

			// check if the next fragment was extended
			if (extendNext) {
				// then remove next fragment
				removeFragment(nextEntry.getValue());
			}

			// add the fragment
			fragment.setType(FragmentType.FREE);
			addFragment(fragment);
		}
	}


	/**
	 * Blocks the specified {@link Fragment} for usage.
	 *
	 * @param fragment the fragment to block
	 */
	protected void block(final String id, final Fragment fragment) {
		synchronized (fragments) {

			final Fragment allocatedFragment = fragments.get(fragment.getStart());

			if (allocatedFragment != null) {
				// check if the allocated fragment is free
				if (allocatedFragment.getType() != FragmentType.FREE) {
					throw new IllegalArgumentException("The requested fragment is already in use!");
				}

				// if the allocated fragment is smaller than the fragment
				if (allocatedFragment.getEnd() < fragment.getEnd()) {
					// check if there is a free fragment next - shouldn't happen
					// if all free segments are joined properly
					final Map.Entry<Long, Fragment> nextEntry = fragments.ceilingEntry(fragment.getStart() + 1);

					if (nextEntry != null && nextEntry.getValue().getType() == FragmentType.FREE) {
						final Fragment next = nextEntry.getValue();

						// check if the next free fragment can accommodate the
						// requested allocation
						if (next.getEnd() >= fragment.getEnd()) {

							// check if the fragment will use all of the next
							// free fragment
							if (next.getEnd() == fragment.getEnd()) {
								removeFragment(next);
							} else {
								// otherwise change the start of the next free
								// fragment
								removeFragment(next);
								next.setStart(fragment.getEnd() + 1);
								addFragment(next);
							}
						}
					} else {
						throw new IllegalArgumentException("Fragment does not fit into the requested allocation!");
					}
				} else {
					// check if the fragment will use all of the allocated
					// fragment
					if (allocatedFragment.getEnd() == fragment.getEnd()) {
						// then remove the allocated fragment
						removeFragment(allocatedFragment);
					} else {
						// otherwise change to start
						removeFragment(allocatedFragment);
						allocatedFragment.setStart(fragment.getEnd() + 1);
						addFragment(allocatedFragment);
					}
				}
			} else {
				// check if a previous free fragment exists
				final Map.Entry<Long, Fragment> previousEntry = fragments.floorEntry(fragment.getStart() - 1);

				if (previousEntry != null && previousEntry.getValue().getType() == FragmentType.FREE) {
					final Fragment previous = previousEntry.getValue();

					// check if the previous free fragment can accommodate the
					// requested fragment
					if (previous.getEnd() >= fragment.getEnd()) {

						// check if there is a rest of free space after the
						// requested fragment
						if (previous.getEnd() > fragment.getEnd()) {
							// create a new free fragment after the requested
							// fragment
							final Fragment newAfter = new Fragment(fragment.getEnd() + 1, previous.getEnd());
							newAfter.setType(FragmentType.FREE);
							addFragment(newAfter);
						}

						// change the end of the previous free fragment to
						// before the start of the requested fragment
						previous.setEnd(fragment.getStart() - 1);
					} else {
						// check if at the end of the allocation table
						final Map.Entry<Long, Fragment> nextEntry = fragments.ceilingEntry(fragment.getStart());
						if (nextEntry != null) {
							// if not, then no space to accommodate the fragment
							throw new IllegalArgumentException("There is not enough space in the previous free fragment to accommodate the fragment at this position!");
						}
					}
				} else {
					// check if at the end of the allocation table
					final Map.Entry<Long, Fragment> nextEntry = fragments.ceilingEntry(fragment.getStart());

					if (nextEntry == null) {
						// at the end of the current allocation table so extend
						// is there a previous entry
						if (previousEntry != null) {
							final Fragment previous = previousEntry.getValue();
							// then allocate a free fragment from the end of the
							// previous fragment until the start of the
							// requested
							// fragment
							if (previous.getEnd() < fragment.getStart() - 1) {
								final Fragment newBefore = new Fragment(previous.getEnd() + 1, fragment.getStart() - 1);
								newBefore.setType(FragmentType.FREE);
								addFragment(newBefore);
							}
						} else {
							// must be an fresh and empty allocation table
							// check if we need to allocate a free fragment from
							// the start of the table until the start of the
							// requested
							// fragment
							if (fragment.getStart() > 0) {
								final Fragment newBefore = new Fragment(0, fragment.getStart() - 1);
								newBefore.setType(FragmentType.FREE);
								addFragment(newBefore);
							}
						}
					} else {
						throw new IllegalArgumentException("There is no free fragment to accommodate the fragment (" + fragment + ") at this position!\nFragments:\n" + fragments + "\nFreespaces:\n" + freeSpaces);
					}
				}
			}

			// add the fragment
			fragment.setId(id);
			fragment.setType(FragmentType.BLOCK);
			addFragment(fragment);
		}
	}


	/**
	 * Adds the specified {@link Fragment} to the table. Manages also the free spaces.
	 *
	 * @param fragment the {@link Fragment}
	 */
	protected void addFragment(final Fragment fragment) {
		fragments.put(fragment.getStart(), fragment);
		String id = fragment.getId();

		if (fragment.getType() == FragmentType.FREE) {
			if (fragment.getStart() > fragment.getEnd()) {
				throw new IllegalArgumentException();
			}

			if (id != null) {
				fragment.setId(null);
				ids.remove(id);
			}

			freeSpaces.put(fragment.getSize(), fragment);
		} else if (fragment.getType() == FragmentType.BLOCK) {
			if (id == null) {
				id = freeIds.poll();

				if (id == null) {
					id = generateId();
				}

				fragment.setId(id);
			} else {
				final Fragment old = findById(fragment.getId());
				old.setId(null);
				free(old);
			}

			ids.put(id, fragment);
		}
	}


	/**
	 * Gets the fragment for the specified id.
	 *
	 * @param id the id
	 * @return the fragment for the id
	 */
	public Fragment findById(final String id) {
		return ids.get(id);
	}


	/**
	 * Generate a new unique id.
	 *
	 * @return the unique id
	 */
	private String generateId() {
		// FIXME
		return "" + id_generator.incrementAndGet();
	}


	/**
	 * Removes the specified {@link Fragment} to the table. Manages also the free spaces.
	 *
	 * @param fragment the {@link Fragment}
	 */
	protected void removeFragment(final Fragment fragment) {
		synchronized (fragments) {
			fragments.remove(fragment.getStart());

			if (fragment.getType() == FragmentType.FREE) {
				freeSpaces.remove(fragment.getSize());
			} else if (fragment.getType() == FragmentType.BLOCK) {
				if (fragment.getId() != null) {
					ids.remove(fragment.getId());
					freeIds.add(fragment.getId());
					fragment.setId(null);
				}
			}
		}
	}


	/**
	 * Gets an unmodifiable collection of all fragments.
	 *
	 * @return an unmodifiable collection of all fragments
	 */
	Map<Long, Fragment> getFragments() {
		return Collections.unmodifiableMap(fragments);
	}


	/**
	 * Gets an unmodifiable collection of all fragments.
	 *
	 * @return an unmodifiable collection of all fragments
	 */
	Map<Long, Fragment> getFreeSpaces() {
		return Collections.unmodifiableMap(freeSpaces);
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "AllocationTable [\nfragments=" + fragments + ", \nfreeSpaces=" + freeSpaces + ", \nids=" + ids + ", \nfreeIds=" + freeIds + ", \nid_generator=" + id_generator + "]";
	}
}
