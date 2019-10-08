package graph.orientdb;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tinkerpop.blueprints.TransactionalGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;

import de.tudarmstadt.ukp.jwktl.JWKTL;
import de.tudarmstadt.ukp.jwktl.api.IWiktionaryEdition;
import de.tudarmstadt.ukp.jwktl.api.IWiktionaryEntry;
import de.tudarmstadt.ukp.jwktl.api.util.ILanguage;
import sem.api.SemanticLink;
import sem.api.SemanticNode;
import sem.graph.SemanticLinkReferenceResolver;
import sem.jwktl.Converter;

public class Import {
	private static final Logger LOG = LoggerFactory.getLogger(Import.class);
	private static OrientDbGraphStore orient;


	public static void main(final String[] args) throws Exception {
		final Weld weld = new Weld();
		final WeldContainer cdiContainer = weld.initialize();
		Import.orient = cdiContainer.instance().select(OrientDbGraphStore.class).get();

		Import.test(cdiContainer);
		// createTestgraph(cdiContainer);
		weld.shutdown();
	}


	protected static void createTestgraph(@Nonnull final WeldContainer cdiContainer) throws Exception {

		Import.orient.getReferenceResolverRegistry().register(SemanticLink.class, SemanticLinkReferenceResolver.class);

		final Converter converter = cdiContainer.instance().select(Converter.class).get();
		final File outputDirectory = new File("C:/Users/Oltmannt/Development/ai-republic/Scribble/de");
		// Connect to the Wiktionary database.
		final IWiktionaryEdition wkt = JWKTL.openEdition(outputDirectory);
		final OrientGraph graph = null;

		try {
			Files.deleteIfExists(Paths.get("TempLinks.bin"));
			FileChannel tempLinksFile = FileChannel.open(Paths.get("TempLinks.bin"), StandardOpenOption.CREATE, StandardOpenOption.WRITE);

			// delete graphdb folder if it exists
			if (Files.exists(Paths.get("graphdb"))) {
				Files.walkFileTree(Paths.get("graphdb"), new SimpleFileVisitor<Path>() {
					@Override
					public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
						Files.delete(file);
						return super.visitFile(file, attrs);
					}
				});
			}

			// create a new graphdb
			final TransactionalGraph tg = new OrientGraph("plocal:graphdb");
			tg.shutdown();

			final List<IWiktionaryEntry> entries = wkt.getEntriesForWord("Affe", entry -> entry.getWordLanguage().getName().equals("German"));
			entries.addAll(wkt.getEntriesForWord("Primat", entry -> entry.getWordLanguage().getName().equals("German")));
			entries.addAll(wkt.getEntriesForWord("Gorilla", entry -> entry.getWordLanguage().getName().equals("German")));

			for (final IWiktionaryEntry wktlEntry : entries) {
				final Map<SemanticNode, Set<SemanticLink>> items = converter.convert(wktlEntry);

				// first persist all actual nodes
				for (final Map.Entry<SemanticNode, Set<SemanticLink>> entry : items.entrySet()) {
					final SemanticNode node = entry.getKey();

					// persist the node
					Import.orient.persistNode(node);
				}

				// then the links
				for (final Set<SemanticLink> links : items.values()) {
					for (final SemanticLink link : links) {
						final SemanticNode target = link.getTarget();

						if (target.getId() == null && target.isReference()) {
							// write link to the temp file
							Import.writeTempLinkInfo(tempLinksFile, link);
						} else {
							Import.LOG.info("Persisting link which node is known:" + link);
							Import.orient.persistLink(link);
						}
					}
				}
			}

			tempLinksFile.close();

			tempLinksFile = FileChannel.open(Paths.get("TempLinks.bin"), StandardOpenOption.READ);
			SemanticLink link = (SemanticLink) Import.readTempLinkInfo(tempLinksFile);

			// at last resolve all temp links
			while (link != null) {
				// try to resolve the link target
				Import.orient.resolveLink(link);
				Import.orient.persistLink(link);

				link = (SemanticLink) Import.readTempLinkInfo(tempLinksFile);
			}

		} finally {
			// Close the database connection.
			if (wkt != null) {
				wkt.close();
			}

			if (graph != null) {
				graph.shutdown();
			}
		}
	}


	public static void test(@Nonnull final WeldContainer cdiContainer) throws Exception {

		Import.orient.getReferenceResolverRegistry().register(SemanticLink.class, SemanticLinkReferenceResolver.class);

		final Converter converter = cdiContainer.instance().select(Converter.class).get();
		final File outputDirectory = new File("C:/Users/Oltmannt/Development/ai-republic/Scribble/en");
		// Connect to the Wiktionary database.
		final IWiktionaryEdition wkt = JWKTL.openEdition(outputDirectory);
		final OrientGraph graph = null;

		try {
			Files.deleteIfExists(Paths.get("TempLinks.bin"));
			FileChannel tempLinksFile = FileChannel.open(Paths.get("TempLinks.bin"), StandardOpenOption.CREATE, StandardOpenOption.WRITE);

			// delete graphdb folder if it exists
			// if (Files.exists(Paths.get("graphdb"))) {
			// Files.walkFileTree(Paths.get("graphdb"), new SimpleFileVisitor<Path>() {
			// @Override
			// public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
			// Files.delete(file);
			// return super.visitFile(file, attrs);
			// }
			// });
			// }

			// create a new graphdb
			final TransactionalGraph tg = new OrientGraph("plocal:graphdb");
			tg.shutdown();

			// final List<IWiktionaryEntry> entries =
			// wkt.getEntriesForWord("Augsburg", entry ->
			// entry.getWordLanguage().getName().equals("German"));

			for (final IWiktionaryEntry wktlEntry : wkt.getAllEntries(entry -> {
				final ILanguage lang = entry.getWordLanguage();

				return lang != null && lang.getISO639_1() != null && lang.getISO639_1().toUpperCase().equals("EN");
			})) {
				final Map<SemanticNode, Set<SemanticLink>> items = converter.convert(wktlEntry);

				// first persist all actual nodes
				for (final Map.Entry<SemanticNode, Set<SemanticLink>> entry : items.entrySet()) {
					final SemanticNode node = entry.getKey();

					// persist the node
					Import.orient.persistNode(node);
				}

				// then the links
				for (final Set<SemanticLink> links : items.values()) {
					for (final SemanticLink link : links) {
						final SemanticNode source = link.getSource();

						// if the link source is not a reference but has not been persisted then persist it now
						if (source.getId() == null && !source.isReference()) {
							Import.orient.persistNode(source);
						}

						final SemanticNode target = link.getTarget();

						if (target.getId() == null && target.isReference()) {
							// write link to the temp file
							Import.writeTempLinkInfo(tempLinksFile, link);
						} else {
							Import.LOG.info("Persisting link which node is known:" + link);
							Import.orient.persistLink(link);
						}
					}
				}
			}

			tempLinksFile.close();

			tempLinksFile = FileChannel.open(Paths.get("TempLinks.bin"), StandardOpenOption.READ);
			SemanticLink link = (SemanticLink) Import.readTempLinkInfo(tempLinksFile);

			// at last resolve all temp links
			while (link != null) {
				// try to resolve the link target
				Import.orient.resolveLink(link);
				Import.orient.persistLink(link);

				link = (SemanticLink) Import.readTempLinkInfo(tempLinksFile);
			}

		} finally {
			// Close the database connection.
			if (wkt != null) {
				wkt.close();
			}

			if (graph != null) {
				graph.shutdown();
			}
		}
	}


	private static void writeTempLinkInfo(@Nonnull final FileChannel out, @Nonnull final Object link) throws IOException {
		Import.LOG.debug("Temporarily storing link which target is unresolved:" + link);
		final ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(bos);
			oos.writeObject(link);

			final byte[] bytes = bos.toByteArray();
			final ByteBuffer sizeBuffer = ByteBuffer.allocate(4);
			sizeBuffer.putInt(bytes.length);
			sizeBuffer.position(0);
			out.write(sizeBuffer);

			final ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
			out.write(byteBuffer);
		} catch (final Exception e) {
		} finally {
			if (oos != null) {
				oos.close();
			}
		}
	}


	private static @Nullable Object readTempLinkInfo(@Nonnull final FileChannel in) throws IOException {
		final ByteBuffer sizeBuffer = ByteBuffer.allocate(4);
		in.read(sizeBuffer);
		final int size = sizeBuffer.getInt(0);

		final ByteBuffer objBuffer = ByteBuffer.allocate(size);
		in.read(objBuffer);

		final ByteArrayInputStream bis = new ByteArrayInputStream(objBuffer.array());
		ObjectInputStream ois = null;
		Object link = null;

		try {
			ois = new ObjectInputStream(bis);
			link = ois.readObject();
		} catch (final Exception e) {
		} finally {
			if (ois != null) {
				ois.close();
			}
		}

		return link;
	}
}
