package store.allocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class AllocationTableTest {
	private final AllocationTable allocationTable = new AllocationTable();

	@Before
	public void setup() {
		allocationTable.block(null, new Fragment(0, 10));
		allocationTable.block(null, new Fragment(11, 20));
		allocationTable.block(null, new Fragment(21, 40));
		allocationTable.free(new Fragment(41, 60));
		allocationTable.block(null, new Fragment(61, 100));
		allocationTable.block(null, new Fragment(101, 200));
	}

	@Test
	public void allocateBoundingFreeFragments() {
		// free fragment lower bound
		Fragment x = allocationTable.findByPosition(21);
		allocationTable.free(x);
		Assert.assertEquals(FragmentType.FREE, x.getType());

		x = allocationTable.findByPosition(21);
		Assert.assertEquals(FragmentType.FREE, x.getType());
		Assert.assertEquals(21, x.getStart());
		Assert.assertEquals(60, x.getEnd());

		// free fragment upper bound
		x = allocationTable.findByPosition(71);
		allocationTable.free(x);

		x = allocationTable.findByPosition(71);
		Assert.assertEquals(FragmentType.FREE, x.getType());
		Assert.assertEquals(21, x.getStart());
		Assert.assertEquals(100, x.getEnd());

		// free enclosed fragment in free space
		x = new Fragment(41, 70);
		allocationTable.free(x);

		allocationTable.findByPosition(51);
		Assert.assertEquals(FragmentType.FREE, x.getType());
		Assert.assertEquals(21, x.getStart());
		Assert.assertEquals(100, x.getEnd());
	}

	@Test
	public void free() {
		allocationTable.free(new Fragment(11, 20));
		allocationTable.block(null, new Fragment(15, 16));

		Fragment f = allocationTable.findByPosition(12);
		Assert.assertEquals(11, f.getStart());
		Assert.assertEquals(14, f.getEnd());
		Assert.assertEquals(FragmentType.FREE, f.getType());

		f = allocationTable.findByPosition(15);
		Assert.assertEquals(15, f.getStart());
		Assert.assertEquals(16, f.getEnd());
		Assert.assertEquals(FragmentType.BLOCK, f.getType());

		f = allocationTable.findByPosition(18);
		Assert.assertEquals(17, f.getStart());
		Assert.assertEquals(20, f.getEnd());
		Assert.assertEquals(FragmentType.FREE, f.getType());
	}

	@Test
	public void multiThreadedAccess() {
		final List<Fragment> blocked = Collections.synchronizedList(new ArrayList<Fragment>());
		new Thread() {
			@Override
			public void run() {
				for (int i = 0; i < 20; i++) {
					final long size = (long) (Math.random() * 49) + 1;
					final Fragment block = allocationTable.allocate(null, size);
					blocked.add(block);
					System.out.println("#1." + i + ": " + block);
				}
			}
		}.start();

		new Thread() {
			@Override
			public void run() {
				for (int i = 0; i < 20; i++) {
					while (blocked.isEmpty()) {
						try {
							Thread.sleep(2);
						} catch (final InterruptedException e) {
						}
					}

					final Fragment block = blocked.remove(0);
					final Fragment f = allocationTable.findByPosition(block.getStart());
					System.out.println("#2." + i + ": " + f + " - FREE");
					allocationTable.free(f);
				}
			}
		}.start();

		new Thread() {
			@Override
			public void run() {
				for (int i = 0; i < 20; i++) {
					final long size = (long) (Math.random() * 49) + 1;
					final Fragment block = allocationTable.allocate(null, size);
					blocked.add(block);
					System.out.println("#3." + i + ": " + block);
				}
			}
		}.start();

		try {
			Thread.sleep(1000);
		} catch (final InterruptedException e) {
		}

		System.out.println(allocationTable);
		Assert.assertTrue(26 <= allocationTable.getFragments().size());
	}
}
