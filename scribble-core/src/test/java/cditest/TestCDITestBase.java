package cditest;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import graph.orientdb.OrientDbGraphStore;

public class TestCDITestBase extends CDITestBase {
	private final OrientDbGraphStore db = CDITestBase.inject(OrientDbGraphStore.class);


	@Test
	public void testInject() throws Exception {
		Assertions.assertThat(db).isNotNull();
	}
}
