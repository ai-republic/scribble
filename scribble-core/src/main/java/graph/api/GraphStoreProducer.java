package graph.api;

import graph.orientdb.OrientDbGraphStore;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

public class GraphStoreProducer {
	@Inject
	private BeanManager beanManager;
	private IGraphStore graphStore;


	@PostConstruct
	@SuppressWarnings("unchecked")
	public void init() {
		final String className = System.getProperty("graph.store.class");
		Class<? extends IGraphStore> clazz = OrientDbGraphStore.class;

		if (className != null) {
			try {
				clazz = (Class<? extends IGraphStore>) Class.forName(System.getProperty(className));
			} catch (final ClassNotFoundException e) {
				e.printStackTrace();
			}
		}

		final Bean<? extends IGraphStore> bean = (Bean<? extends IGraphStore>) beanManager.resolve(beanManager.getBeans(clazz));
		graphStore = (IGraphStore) beanManager.getReference(bean, bean.getBeanClass(), beanManager.createCreationalContext(bean));

	}


	@ApplicationScoped
	// @Produces
	public IGraphStore produceGraphStore() {
		return graphStore;
	}
}
