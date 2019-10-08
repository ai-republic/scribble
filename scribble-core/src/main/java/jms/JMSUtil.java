package jms;

import java.io.Closeable;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.annotation.PreDestroy;
import javax.inject.Singleton;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

/**
 * Utitity to manage JMS topics and queues.
 * 
 * @author Torsten Oltmanns
 *
 */
@Singleton
public class JMSUtil implements Serializable {
  private static final long serialVersionUID = -2579224325764969085L;
  private static JMSUtil instance;
  private transient InitialContext ctx;
  private final Map<CacheKey, JMSTopicConnection> topicCache = new HashMap<>();
  private final Map<CacheKey, JMSQueueConnection> queueCache = new HashMap<>();

  /**
   * Composite key for the Queue-Cache.
   * 
   * @author Torsten Oltmanns
   */
  class CacheKey implements Serializable {
    private static final long serialVersionUID = -5486816130900827616L;
    String connectionFactoryJNDIName;
    String queueJNDIName;

    /**
     * Constructor.
     */
    public CacheKey() {}

    /**
     * Constructor.
     * 
     * @param connectionFactoryJNDIName the connection-factory JNDI name
     * @param queueJNDIName the queue JNDI name
     */
    public CacheKey(final String connectionFactoryJNDIName, final String queueJNDIName) {
      this.connectionFactoryJNDIName = connectionFactoryJNDIName;
      this.queueJNDIName = queueJNDIName;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + getOuterType().hashCode();
      result = prime * result + ((connectionFactoryJNDIName == null) ? 0 : connectionFactoryJNDIName.hashCode());
      result = prime * result + ((queueJNDIName == null) ? 0 : queueJNDIName.hashCode());
      return result;
    }

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
      final CacheKey other = (CacheKey) obj;
      if (!getOuterType().equals(other.getOuterType())) {
        return false;
      }
      if (connectionFactoryJNDIName == null) {
        if (other.connectionFactoryJNDIName != null) {
          return false;
        }
      } else if (!connectionFactoryJNDIName.equals(other.connectionFactoryJNDIName)) {
        return false;
      }
      if (queueJNDIName == null) {
        if (other.queueJNDIName != null) {
          return false;
        }
      } else if (!queueJNDIName.equals(other.queueJNDIName)) {
        return false;
      }
      return true;
    }

    private JMSUtil getOuterType() {
      return JMSUtil.this;
    }
  }

  /**
   * Implementation for the JMS queue connection.
   * 
   * @author Torsten Oltmanns
   */
  public class JMSTopicConnection implements Serializable, Closeable {
    private static final long serialVersionUID = 3199322176437431102L;
    private String connectionFactoryJNDIName;
    private String queueJNDIName;
    private TopicConnectionFactory connectionFactory;
    private transient TopicConnection connection;
    private transient TopicSession session;
    private transient Topic queue;
    private transient TopicPublisher publisher;
    private transient TopicSubscriber subscriber;

    /**
     * Constructor.
     */
    JMSTopicConnection() {}

    /**
     * Constructor.
     * 
     * @param connectionFactoryJNDIName the connection-factory JNDI name
     * @param queueJNDIName the queue JNDI name
     */
    public JMSTopicConnection(final String connectionFactoryJNDIName, final String queueJNDIName) {
      this.connectionFactoryJNDIName = connectionFactoryJNDIName;
      this.queueJNDIName = queueJNDIName;
    }

    public TopicConnectionFactory getConnectionFactory() throws NamingException {
      if (connectionFactory == null) {
        connectionFactory = (TopicConnectionFactory) getInitialContext().lookup(connectionFactoryJNDIName);
      }

      return connectionFactory;
    }

    public TopicConnection getConnection() throws JMSException, NamingException {
      if (connection == null) {
        connection = getConnectionFactory().createTopicConnection();
      }

      return connection;
    }

    public TopicSession session() throws JMSException, NamingException {
      if (session == null) {
        session = getConnection().createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
      }

      return session;
    }

    public Topic getTopic() throws JMSException, NamingException {
      if (queue == null) {
        try {
          queue = (Topic) ctx.lookup(queueJNDIName);
        } catch (final NameNotFoundException ex) {
          queue = session().createTopic(queueJNDIName);
          getInitialContext().bind(queueJNDIName, queue);
        }
      }

      getConnection().start();

      return queue;
    }

    public TopicPublisher publisher() throws JMSException, NamingException {
      if (publisher == null) {
        publisher = session().createPublisher(getTopic());
      }

      return publisher;
    }

    public TopicSubscriber subscriber() throws JMSException, NamingException {
      if (subscriber == null) {
        subscriber = session().createSubscriber(getTopic());
      }

      return subscriber;
    }

    /**
     * Closes the queue's connection and session.
     * 
     */
    @Override
    public void close() throws IOException {
      topicCache.remove(new CacheKey(connectionFactoryJNDIName, queueJNDIName));

      try {
        if (publisher != null) {
          publisher.close();
        }
      } catch (final Exception ex) {}

      try {
        if (subscriber != null) {
          subscriber.close();
        }
      } catch (final Exception ex) {}

      try {
        if (null != session) {
          session.close();
        }
      } catch (final Exception ex) {}

      try {
        if (null != connection) {
          connection.close();
        }
      } catch (final Exception ex) {}
    }
  }

  /**
   * Implementation for the JMS queue connection.
   * 
   * @author Torsten Oltmanns
   */
  public class JMSQueueConnection implements Serializable, Closeable {
    private static final long serialVersionUID = 3199322176437431102L;
    private String connectionFactoryJNDIName;
    private String queueJNDIName;
    private QueueConnectionFactory connectionFactory;
    private transient QueueConnection connection;
    private transient QueueSession session;
    private transient Queue queue;
    private transient QueueSender sender;
    private transient QueueReceiver receiver;
    private transient QueueBrowser browser;

    /**
     * Constructor.
     */
    JMSQueueConnection() {}

    /**
     * Constructor.
     * 
     * @param connectionFactoryJNDIName the connection-factory JNDI name
     * @param queueJNDIName the queue JNDI name
     */
    public JMSQueueConnection(final String connectionFactoryJNDIName, final String queueJNDIName) {
      this.connectionFactoryJNDIName = connectionFactoryJNDIName;
      this.queueJNDIName = queueJNDIName;
    }

    public QueueConnectionFactory getConnectionFactory() throws NamingException {
      if (connectionFactory == null) {
        connectionFactory = (QueueConnectionFactory) getInitialContext().lookup(connectionFactoryJNDIName);
      }

      return connectionFactory;
    }

    public QueueConnection getConnection() throws JMSException, NamingException {
      if (connection == null) {
        connection = getConnectionFactory().createQueueConnection();
      }

      return connection;
    }

    public QueueSession session() throws JMSException, NamingException {
      if (session == null) {
        session = getConnection().createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
      }

      return session;
    }

    public Queue getQueue() throws JMSException, NamingException {
      if (queue == null) {
        try {
          queue = (Queue) ctx.lookup(queueJNDIName);
        } catch (final NameNotFoundException ex) {
          queue = session().createQueue(queueJNDIName);
          getInitialContext().bind(queueJNDIName, queue);
        }
      }

      getConnection().start();

      return queue;
    }

    public QueueSender sender() throws JMSException, NamingException {
      if (sender == null) {
        sender = session().createSender(getQueue());
      }

      return sender;
    }

    public QueueReceiver receiver() throws JMSException, NamingException {
      if (receiver == null) {
        receiver = session().createReceiver(getQueue());
      }

      return receiver;
    }

    public QueueBrowser browser() throws JMSException, NamingException {
      if (browser != null) {
        browser = session().createBrowser(getQueue());
      }

      return browser;
    }

    /**
     * Closes the queue's connection and session.
     * 
     */
    @Override
    public void close() throws IOException {
      queueCache.remove(new CacheKey(connectionFactoryJNDIName, queueJNDIName));

      try {
        if (sender != null) {
          sender.close();
        }
      } catch (final Exception ex) {}

      try {
        if (receiver != null) {
          receiver.close();
        }
      } catch (final Exception ex) {}

      try {
        if (browser != null) {
          browser.close();
        }
      } catch (final Exception ex) {}

      try {
        if (null != session) {
          session.close();
        }
      } catch (final Exception ex) {}

      try {
        if (null != connection) {
          connection.close();
        }
      } catch (final Exception ex) {}
    }
  }



  /**
   * Gets an instance for non-CDI environments.
   * 
   * @return the {@link JMSUtil}
   * @throws JMSException if an error occurs during JMS queue creation
   * @throws NamingException if the JNDI names couldn't be found in the context
   */
  public static JMSUtil create() throws JMSException, NamingException {
    if (instance == null) {
      instance = new JMSUtil();
    }

    return instance;
  }

  /**
   * Constructor.
   */
  JMSUtil() {}

  /**
   * Initialisiert einen {@link InitialContext} falls dieser noch nicht kreiert wurde.
   * 
   * @return einen {@link InitialContext}
   */
  protected InitialContext getInitialContext() {
    if (ctx == null) {
      try {
        final Hashtable<String, String> env = new Hashtable<>(System.getenv());
        ctx = new InitialContext(env);
      } catch (final NamingException e) {
        throw new RuntimeException(e);
      }
    }

    return ctx;
  }

  /**
   * Gets the queue for the specified connection-factory and queue names via a JNDI lookup or from the cache if
   * possible.
   * 
   * @param connectionFactoryJNDIName the connection-factory JNDI name
   * @param queueJNDIName the queue JNDI name
   * @return the {@link JMSQueueConnection}
   * @throws JMSException if an error occurs during JMS queue creation
   * @throws NamingException if the JNDI names couldn't be found in the context
   */
  public JMSTopicConnection topic(final String connectionFactoryJNDIName, final String queueJNDIName)
      throws JMSException, NamingException {
    final CacheKey key = new CacheKey(connectionFactoryJNDIName, queueJNDIName);
    JMSTopicConnection value = topicCache.get(key);

    if (value == null) {
      value = new JMSTopicConnection(connectionFactoryJNDIName, queueJNDIName);
      topicCache.put(key, value);
    } else {
      value.getConnection().start();
    }

    return value;
  }

  /**
   * Gets the queue for the specified connection-factory and queue names via a JNDI lookup or from the cache if
   * possible.
   * 
   * @param connectionFactoryJNDIName the connection-factory JNDI name
   * @param queueJNDIName the queue JNDI name
   * @return the {@link JMSQueueConnection}
   * @throws JMSException if an error occurs during JMS queue creation
   * @throws NamingException if the JNDI names couldn't be found in the context
   */
  public JMSQueueConnection queue(final String connectionFactoryJNDIName, final String queueJNDIName)
      throws JMSException, NamingException {
    final CacheKey key = new CacheKey(connectionFactoryJNDIName, queueJNDIName);
    JMSQueueConnection value = queueCache.get(key);

    if (value == null) {
      value = new JMSQueueConnection(connectionFactoryJNDIName, queueJNDIName);
      queueCache.put(key, value);
    } else {
      value.getConnection().start();
    }

    return value;
  }

  @PreDestroy
  public void cleanUpSafe() {
    while (topicCache != null && !topicCache.isEmpty()) {
      try {
        topicCache.values().iterator().next().close();
      } catch (final Exception e) {}
    }

    while (queueCache != null && !queueCache.isEmpty()) {
      try {
        queueCache.values().iterator().next().close();
      } catch (final Exception e) {}
    }

    try {
      if (ctx != null) {
        ctx.close();
      }
    } catch (final NamingException e) {}
    ctx = null;
  }
}
