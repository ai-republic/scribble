package graph.orientdb;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.orientechnologies.orient.core.metadata.schema.OClass.INDEX_TYPE;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientElement;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import com.tinkerpop.blueprints.impls.orient.OrientGraphNoTx;
import com.tinkerpop.blueprints.impls.orient.OrientVertexType;

import graph.api.IGraphStore;
import graph.api.IReferenceResolver;
import graph.api.ReferenceResolverRegistry;
import graph.api.ResolveHit;
import graph.api.annotation.GraphLink;
import graph.api.annotation.GraphNode;
import graph.api.annotation.IGraphAnnotationProcessor;
import graph.api.annotation.Id;
import graph.api.annotation.Index;
import graph.api.annotation.LinkFunction;
import graph.api.annotation.LinkType;
import graph.api.annotation.SourceNode;
import graph.api.annotation.TargetNode;
import graph.api.query.IQueryBuilder;
import graph.api.query.Query;

/**
 * Graph store implementation using OrientDB.
 *
 * @author Torsten Oltmanns<br/>
 *         (c) Copyright 2015, ai-republic GmbH, Germany
 *
 */
@ApplicationScoped
public class OrientDbGraphStore implements IGraphStore {
  private static final Logger LOG = LoggerFactory.getLogger(OrientDbGraphStore.class);
  private final OrientGraphFactory factory =
      new OrientGraphFactory("plocal:/Users/Oltmannt/Development/orientdb-community-2.1.4/databases/graphdb")
          .setupPool(1, 10);
  // private final OrientGraphFactory factory = new
  // OrientGraphFactory("plocal:D:/Development/orientdb-community-2.1.4/databases/graphdb").setupPool(1, 10);

  @Inject
  private IGraphAnnotationProcessor annotationProcessor;
  @Inject
  private IQueryBuilder<String> queryBuilder;
  @Inject
  private ReferenceResolverRegistry referenceResolverRegistry;


  @Override
  public void close() throws IOException {
    factory.close();
  }


  @Override
  @SuppressWarnings("unchecked")
  public @Nonnull <RESULT> List<RESULT> findNodes(@Nonnull final Query query, final Class<RESULT> resultClass)
      throws Exception {
    final String sql = queryBuilder.processQuery(query);
    final List<RESULT> result = new ArrayList<>();
    final OrientGraph graph = factory.getTx();

    OrientDbGraphStore.LOG.info("execute Query: " + sql + "\nwith Params: " + query.getParameters());

    for (final Vertex v : (Iterable<Vertex>) graph.command(new OSQLSynchQuery<ODocument>(sql))
        .execute(query.getParameters().toArray())) {
      String className = null;

      if (resultClass == null || resultClass.isInterface()) {
        className = v.getProperty("@class");
      } else {
        className = resultClass.getName();
      }

      final Class<RESULT> clazz = (Class<RESULT>) Class.forName(className);

      // check if the node is a graph node
      if (clazz.getAnnotation(GraphNode.class) != null) {
        result.add(convert(v));
      } else if (v.getPropertyKeys().size() > 1) {
        // set values of some other result class
        final RESULT resultObj = clazz.newInstance();

        for (final String key : v.getPropertyKeys()) {
          final Object value = v.getProperty(key);

          if (value != null) {
            final java.lang.reflect.Field field = resultObj.getClass().getDeclaredField(key);
            field.setAccessible(true);
            field.set(resultObj, value);
          }
        }

        result.add(resultObj);
      } else {
        result.add((RESULT) v.getProperty(v.getPropertyKeys().iterator().next()));
      }
    }

    return result;
  }


  @Override
  public @Nullable Object findNodeById(@Nonnull final Object id) throws Exception {
    final OrientGraph graph = factory.getTx();
    final OrientElement element = graph.getElement(id);

    if (element instanceof Vertex) {
      final Vertex v = (Vertex) element;
      return convert(v);
    } else if (element instanceof Edge) {
      return convert((Edge) element);
    } else {
      throw new IllegalArgumentException("An object for the specified ID [" + id + "] could not be found!");
    }
  }


  @Override
  @SuppressWarnings("unchecked")
  public @Nonnull <RESULT> List<RESULT> findLinks(@Nonnull final Query query, final Class<RESULT> resultClass)
      throws Exception {
    final String sql = queryBuilder.processQuery(query);
    final List<RESULT> result = new ArrayList<>();
    final OrientGraph graph = factory.getTx();

    OrientDbGraphStore.LOG.info("execute Query: " + sql + "\nwith Params: " + query.getParameters());

    for (final Edge e : (Iterable<Edge>) graph.command(new OSQLSynchQuery<ODocument>(sql))
        .execute(query.getParameters().toArray())) {
      String className = null;

      if (resultClass == null || resultClass.isInterface()) {
        className = e.getProperty("@class");
      } else {
        className = resultClass.getName();
      }

      final Class<RESULT> clazz = (Class<RESULT>) Class.forName(className);

      // check if the node is a graph node
      if (clazz.getAnnotation(GraphLink.class) != null) {
        result.add(convert(e));
      } else if (e.getPropertyKeys().size() > 1) {
        // set values of some other result class
        final RESULT resultObj = clazz.newInstance();

        for (final String key : e.getPropertyKeys()) {
          final Object value = e.getProperty(key);

          if (value != null) {
            final java.lang.reflect.Field field = resultObj.getClass().getDeclaredField(key);
            field.setAccessible(true);
            field.set(resultObj, value);
          }
        }

        result.add(resultObj);
      } else {
        result.add((RESULT) e.getProperty(e.getPropertyKeys().iterator().next()));
      }
    }

    return result;
  }


  @SuppressWarnings("unchecked")
  @Nonnull
  <RESULT> RESULT convert(@Nonnull final Vertex vertex) throws Exception {
    final RESULT graphNode = (RESULT) Class.forName(vertex.getProperty("@class")).newInstance();

    annotationProcessor.setAnnotatedValue(graphNode, vertex.getId(), Id.class);

    for (final String key : vertex.getPropertyKeys()) {
      if (!"className".equals(key) && !key.startsWith("store_")) {
        final Object value = vertex.getProperty(key);

        if (value != null) {
          final java.lang.reflect.Field field = graphNode.getClass().getDeclaredField(key);
          field.setAccessible(true);
          field.set(graphNode, value);
        }
      }
    }

    return graphNode;
  }


  @SuppressWarnings("unchecked")
  @Nonnull
  <RESULT> RESULT convert(@Nonnull final Edge edge) throws Exception {
    final RESULT graphLink = (RESULT) Class.forName(edge.getProperty("className")).newInstance();
    annotationProcessor.setAnnotatedValue(graphLink, edge.getId(), Id.class);

    final Vertex vOut = edge.getVertex(Direction.OUT);
    annotationProcessor.setAnnotatedValue(graphLink, convert(vOut), SourceNode.class);

    final Vertex vIn = edge.getVertex(Direction.IN);
    annotationProcessor.setAnnotatedValue(graphLink, convert(vIn), TargetNode.class);

    final Object function = Class.forName(edge.getProperty("functionClass")).newInstance();
    annotationProcessor.setAnnotatedValue(graphLink, function, LinkFunction.class);
    annotationProcessor.setFieldValues(function, edge.getProperty("functionFields"));

    for (final String key : edge.getPropertyKeys()) {
      if (!"className".equals(key)) {
        final Object value = edge.getProperty(key);

        if (value != null) {
          final java.lang.reflect.Field field = graphLink.getClass().getField(key);
          field.setAccessible(true);
          field.set(graphLink, value);
        }
      }
    }

    return graphLink;
  }


  @Override
  public @Nonnull String persistNode(@Nonnull final Object graphNode) throws IOException {
    OrientDbGraphStore.LOG.info(">>>  Persisting node: " + graphNode);

    Vertex vertex;

    try {
      createClassIfNeeded(graphNode);

      final OrientGraph graph = factory.getTx();
      vertex = null;

      final Object id = annotationProcessor.getAnnotatedValue(graphNode, Id.class);

      // check if its a new node
      if (id == null) {
        // then create it and set the id on the graphNode
        vertex = graph.addVertex("class:" + graphNode.getClass().getName());
        annotationProcessor.setAnnotatedValue(graphNode, vertex.getId(), Id.class);
      } else {
        // otherwise load the corresponding vertex
        vertex = graph.getVertex(id);
      }

      // update all fields on the vertex
      update(vertex, graphNode);

      graph.commit();
    } catch (final Exception e) {
      OrientDbGraphStore.LOG.error("Error persisting node: " + graphNode, e);
      throw new IOException("Error persisting node: " + graphNode, e);
    }

    OrientDbGraphStore.LOG.debug(">>>  Persisted vertex: " + vertex + " for node: " + graphNode);

    return vertex.getId().toString();
  }


  private void createClassIfNeeded(@Nonnull final Object graphNode) {
    final OrientGraphNoTx graphNoTx = factory.getNoTx();
    try {
      OrientVertexType type = graphNoTx.getVertexType(graphNode.getClass().getName());

      if (type == null) {
        // create class definition
        OrientDbGraphStore.LOG.info("Create type definition for class: " + graphNode.getClass().getName());
        type = graphNoTx.createVertexType(graphNode.getClass().getName());

        final Set<java.lang.reflect.Field> indexFields = annotationProcessor.getAnnotatedFields(graphNode, Index.class);

        for (final java.lang.reflect.Field field : annotationProcessor
            .getFieldsOfAllSuperclasses(graphNode.getClass())) {
          if (field.getName().equals("indexFields")) {
            continue;
          }

          if (!field.getName().equals("id")) {
            OrientDbGraphStore.LOG.info("\tcreate field definition: " + field.getName());
            type.createProperty(field.getName(), OType.getTypeByClass(field.getType()));
          }

          if (indexFields.contains(field)) {
            OrientDbGraphStore.LOG.info("\tcreate index for field: " + field.getName());
            type.createIndex("index_" + field.getName(), INDEX_TYPE.NOTUNIQUE_HASH_INDEX, field.getName());
          }
        }
      }
    } catch (final Exception e) {
      OrientDbGraphStore.LOG.error("Couldn't create vertex-class: " + graphNode.getClass().getName(), e);
    }
  }


  @Override
  public @Nonnull String persistLink(@Nonnull final Object graphLink) throws IOException {
    Edge edge = null;

    try {
      final OrientGraph graph = factory.getTx();
      OrientDbGraphStore.LOG.info(">>>  Persisting link: " + graphLink);

      final Object id = annotationProcessor.getAnnotatedValue(graphLink, Id.class);

      if (id == null) {
        final Object linkType = annotationProcessor.getAnnotatedValue(graphLink, LinkType.class);
        final Object sourceNode = annotationProcessor.getAnnotatedValue(graphLink, SourceNode.class);
        final Object targetNode = annotationProcessor.getAnnotatedValue(graphLink, TargetNode.class);
        final Object sourceId = annotationProcessor.getAnnotatedValue(sourceNode, Id.class);
        final Object targetId = annotationProcessor.getAnnotatedValue(targetNode, Id.class);

        edge = graph.addEdge(null, graph.getVertex(sourceId), graph.getVertex(targetId), linkType.toString());

        annotationProcessor.setAnnotatedValue(graphLink, edge.getId(), Id.class);
      } else {
        edge = graph.getEdge(id);
      }

      update(edge, graphLink);
      graph.commit();
    } catch (final Exception e) {
      throw new RuntimeException("Error creating link: " + graphLink, e);
    }

    OrientDbGraphStore.LOG.debug(">>>  Persisted edge " + edge + " for link: " + graphLink);

    return edge.getId().toString();

  }


  private void update(@Nonnull final Vertex vertex, @Nonnull final Object graphNode) throws Exception {
    final Map<String, Object> values = annotationProcessor.getFieldValues(graphNode);

    for (final Map.Entry<String, Object> entry : values.entrySet()) {

      // check for reserved 'id' field and null values. Id will be set at
      // persist or convert time if not set already.
      if (!entry.getKey().equals("id") && entry.getValue() != null) {
        if (graphNode.getClass().getDeclaredField(entry.getKey()).getAnnotation(Id.class) != null) {
          continue;
        }

        vertex.setProperty(entry.getKey(), entry.getValue());
      }
    }
  }


  private void update(@Nonnull final Edge edge, @Nonnull final Object graphLink) throws Exception {
    edge.setProperty("className", graphLink.getClass().getName());

    final Object function = annotationProcessor.getAnnotatedValue(graphLink, LinkFunction.class);
    if (function != null) {
      edge.setProperty("functionClass", function.getClass());

      final Map<String, Object> functionFields = annotationProcessor.getFieldValues(function);

      if (functionFields != null) {
        edge.setProperty("functionFields", functionFields);
      }
    }

    for (final Map.Entry<String, Object> entry : annotationProcessor.getFieldValues(graphLink).entrySet()) {

      // check for reserved 'id' field and null values. Id will be set at
      // persist or convert time if not set already.
      if (!entry.getKey().equals("id") && entry.getValue() != null) {
        final List<Annotation> annotations =
            Arrays.asList(graphLink.getClass().getDeclaredField(entry.getKey()).getAnnotations());

        if (annotations.contains(Id.class) || annotations.contains(SourceNode.class)
            || annotations.contains(TargetNode.class) || annotations.contains(LinkFunction.class)
            || annotations.contains(LinkType.class)) {
          continue;
        }

        edge.setProperty(entry.getKey(), entry.getValue());
      }
    }
  }


  /**
   * Tries to resolve the specified links target node reference to an actual existing node. This is done by processing
   * configured {@link IReferenceResolver} from the {@link ReferenceResolverRegistry} for the reference node class and
   * returning the most probable resolved node. If no actual node could be determined, it will persist the reference
   * node and return that.
   *
   * @param refNode the reference node
   * @return the actual node or - if all resolves fail - it persists the reference node and returns it
   * @throws Exception if an error occurs
   */
  @SuppressWarnings("unchecked")
  protected @Nonnull <LINK, NODE> void resolveLink(@Nonnull final LINK graphLink) throws Exception {
    // get the registered resolvers for the link class
    final IReferenceResolver<LINK> resolver = referenceResolverRegistry.getReferenceResolver(graphLink);

    // try to resolve the best match for the link target
    final List<ResolveHit<NODE>> nodes = resolver.resolve(this, graphLink);
    NODE targetNode = null;

    if (!nodes.isEmpty()) {
      // get the best match at index 0
      targetNode = nodes.get(0).getNode();

      // set the resolved target node to the link
      annotationProcessor.setAnnotatedValue(graphLink, targetNode, TargetNode.class);
    } else {
      // if no node could be found, persist the reference target node
      targetNode = (NODE) annotationProcessor.getAnnotatedValue(graphLink, TargetNode.class);
      persistNode(targetNode);
    }
  }


  @Override
  public List<String> getLinkTypes(final Object id) throws Exception {
    final Set<String> linkTypes = new HashSet<>();
    final OrientGraphNoTx graph = factory.getNoTx();
    final Vertex v = (Vertex) graph.getElement(id);
    final Iterable<Edge> edges = v.getEdges(Direction.OUT, new String[] {});

    for (final Edge edge : edges) {
      linkTypes.add(edge.getLabel());
    }

    return new ArrayList<>(linkTypes);
  }


  @Override
  public <T> List<T> getConnectedNodes(final Object id, final String edgeType) throws Exception {
    final OrientGraphNoTx graph = factory.getNoTx();
    final List<T> nodes = new ArrayList<>();
    final Vertex v = graph.getVertex(id);

    for (final Edge e : v.getEdges(Direction.BOTH, edgeType)) {
      final Vertex vIn = e.getVertex(Direction.IN);

      if (!vIn.getId().equals(v.getId())) {
        nodes.add(convert(vIn));
      } else {
        final Vertex vOut = e.getVertex(Direction.OUT);
        nodes.add(convert(vOut));
      }
    }

    return nodes;
  }


  /**
   * Gets the {@link ReferenceResolverRegistry}.
   *
   * @return the {@link ReferenceResolverRegistry}
   */
  public ReferenceResolverRegistry getReferenceResolverRegistry() {
    return referenceResolverRegistry;
  }


  /**
   * Sets the {@link ReferenceResolverRegistry}
   *
   * @param referenceResolverRegistry the {@link ReferenceResolverRegistry}
   */
  public void setReferenceResolverRegistry(final ReferenceResolverRegistry referenceResolverRegistry) {
    this.referenceResolverRegistry = referenceResolverRegistry;
  }
}
