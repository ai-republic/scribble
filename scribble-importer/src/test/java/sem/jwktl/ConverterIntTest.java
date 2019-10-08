package sem.jwktl;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.assertj.core.api.Assertions;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.tudarmstadt.ukp.jwktl.JWKTL;
import de.tudarmstadt.ukp.jwktl.api.IWiktionaryEdition;
import de.tudarmstadt.ukp.jwktl.api.IWiktionaryEntry;
import de.tudarmstadt.ukp.jwktl.api.IWiktionaryRelation;
import de.tudarmstadt.ukp.jwktl.api.RelationType;
import de.tudarmstadt.ukp.jwktl.api.entry.WiktionaryRelation;
import sem.api.PartOfSpeech;
import sem.api.SemanticLink;
import sem.api.SemanticNode;
import sem.api.WordDegree;
import sem.api.WordGender;
import sem.api.WordMultiplicity;
import sem.api.WordRelationType;

public class ConverterIntTest {
	private static IWiktionaryEdition wkt;
	private final Converter converter = new Converter();


	@BeforeClass
	public static void init() {
		final File outputDirectory = new File("../de");

		// Connect to the Wiktionary database.
		ConverterIntTest.wkt = JWKTL.openEdition(outputDirectory);
	}


	@AfterClass
	public static void close() {
		// Close the database connection.
		ConverterIntTest.wkt.close();
	}


	@Test
	public void createItemsForEntry_Noun() {
		final List<IWiktionaryEntry> entries = ConverterIntTest.wkt.getEntriesForWord("Augsburg", entry -> entry.getWordLanguage().getName().equals("German"));
		final Locale language = new Locale("deu");
		Assertions.assertThat(entries).isNotNull();
		Assertions.assertThat(entries).asList().hasSize(1);

		final WiktionaryRelation relation = new WiktionaryRelation("test", RelationType.SYNONYM);
		final List<IWiktionaryRelation> relationsToConvert = Arrays.asList(relation);
		final Map<WordRelationType, List<SemanticNode>> relations = converter.convertRelations(relationsToConvert, language);
		final SemanticNode target = relations.get(WordRelationType.SYNONYM).get(0);
		final Map<SemanticNode, Set<SemanticLink>> items = converter.createItemsForEntry(entries.get(0), relations, Arrays.asList("contextTest"), new ArrayList<>());

		Assertions.assertThat(items).isNotNull();
		Assertions.assertThat(items).hasSize(2);

		final SemanticNode item = items.entrySet().iterator().next().getKey();
		Assertions.assertThat(item.getWord()).isEqualTo("Augsburg");
		Assertions.assertThat(item.getBaseForm()).isEqualTo("Augsburg");
		Assertions.assertThat(item.getPartOfSpeech()).hasSize(1).containsExactly(PartOfSpeech.NOUN);
		Assertions.assertThat(item.isLocation()).isTrue();
		Assertions.assertThat(item.getGender()).containsExactly(WordGender.NEUTER);
		Assertions.assertThat(item.getMultiplicities()).asList().containsExactly(WordMultiplicity.SINGULAR);
		Assertions.assertThat(item.getTense()).isNullOrEmpty();

		final Set<SemanticLink> links = items.entrySet().iterator().next().getValue();
		Assertions.assertThat(links).isNotNull().isNotEmpty().hasSize(1);
		Assertions.assertThat(compareLink(new SemanticLink(item, target, WordRelationType.SYNONYM.name()), links.iterator().next())).isTrue();
	}


	@Test
	public void createItemsForEntry_Verb_Baseform() {
		final List<IWiktionaryEntry> entries = ConverterIntTest.wkt.getEntriesForWord("haben", entry -> entry.getWordLanguage().getName().equals("German"));
		final Locale language = new Locale("deu");

		Assertions.assertThat(entries).isNotNull();
		Assertions.assertThat(entries).asList().hasSize(1);

		final WiktionaryRelation relation = new WiktionaryRelation("test", RelationType.SYNONYM);
		final List<IWiktionaryRelation> relationsToConvert = Arrays.asList(relation);
		final Map<WordRelationType, List<SemanticNode>> relations = converter.convertRelations(relationsToConvert, language);
		final SemanticNode target = relations.get(WordRelationType.SYNONYM).get(0);
		final Map<SemanticNode, Set<SemanticLink>> items = converter.createItemsForEntry(entries.get(0), relations, Arrays.asList("contextTest"), new ArrayList<>());

		Assertions.assertThat(items).isNotNull();
		Assertions.assertThat(items).hasSize(1);

		final SemanticNode item = items.entrySet().iterator().next().getKey();
		Assertions.assertThat(item.getWord()).isEqualTo("haben");
		Assertions.assertThat(item.getBaseForm()).isEqualTo("haben");
		Assertions.assertThat(item.getPartOfSpeech()).hasSize(2).containsExactly(PartOfSpeech.VERB, PartOfSpeech.AUXILIARY_VERB);
		Assertions.assertThat(item.getGender()).isNullOrEmpty();
		// TODO: get flexionen of verb to determine multiplicity
		// assertThat(item.getMultiplicities()).asList().containsExactly(WordMultiplicity.PLURAL);
		Assertions.assertThat(item.getTense()).isNullOrEmpty();

		final Set<SemanticLink> links = items.entrySet().iterator().next().getValue();
		Assertions.assertThat(links).isNotNull().isNotEmpty().hasSize(1);
		Assertions.assertThat(compareLink(new SemanticLink(item, target, WordRelationType.SYNONYM.name()), links.iterator().next())).isTrue();
		Assertions.assertThat(item.getContext()).isNotNull().isNotEmpty().hasSize(1).containsOnly("contextTest");
	}


	@Test
	public void createItemsForEntry_Verb_flexionform() {
		final List<IWiktionaryEntry> entries = ConverterIntTest.wkt.getEntriesForWord("hat", entry -> entry.getWordLanguage().getName().equals("German"));
		final Locale language = new Locale("deu");

		Assertions.assertThat(entries).isNotNull();
		Assertions.assertThat(entries).asList().hasSize(1);

		final WiktionaryRelation relation = new WiktionaryRelation("test", RelationType.SYNONYM);
		final List<IWiktionaryRelation> relationsToConvert = Arrays.asList(relation);
		final Map<WordRelationType, List<SemanticNode>> relations = converter.convertRelations(relationsToConvert, language);
		// SemanticItem target = relations.get(WordRelationType.SYNONYM).get(0);
		final Map<SemanticNode, Set<SemanticLink>> items = converter.createItemsForEntry(entries.get(0), relations, Arrays.asList("contextTest"), new ArrayList<>());

		Assertions.assertThat(items).isNotNull();
		Assertions.assertThat(items).hasSize(1);

		final SemanticNode item = items.entrySet().iterator().next().getKey();
		Assertions.assertThat(item.getWord()).isEqualTo("hat");
		Assertions.assertThat(item.getBaseForm()).isEqualTo("haben");
		// TODO: get flexionen of verb to determine POS an other details
		// assertThat(item.getPartOfSpeech()).hasSize(2).containsExactly(PartOfSpeech.VERB,
		// PartOfSpeech.AUXILIARY_VERB);
		Assertions.assertThat(item.getGender()).isNullOrEmpty();
		// TODO: get flexionen of verb to determine multiplicity
		// assertThat(item.getMultiplicities()).asList().containsExactly(WordMultiplicity.PLURAL);
		// assertThat(item.getTense()).isNull();
		// assertThat(item.getRelations()).isNotNull().isNotEmpty().hasSize(1);
		// assertThat(item.getRelations().get(0)).isEqualToComparingFieldByField(new
		// SemanticLink<SemanticItem,
		// SemanticItem, WordRelationType>(item, target,
		// WordRelationType.SYNONYM));
		// assertThat(item.getContext()).isNotNull().isNotEmpty().hasSize(1).containsOnly("contextTest");
	}


	@Test
	public void createItemsForEntry_Adjective() {
		final List<IWiktionaryEntry> entries = ConverterIntTest.wkt.getEntriesForWord("grün", entry -> entry.getWordLanguage().getName().equals("German"));
		final Locale language = new Locale("deu");

		Assertions.assertThat(entries).isNotNull();
		Assertions.assertThat(entries).asList().hasSize(2);

		final WiktionaryRelation relation = new WiktionaryRelation("test", RelationType.SYNONYM);
		final List<IWiktionaryRelation> relationsToConvert = Arrays.asList(relation);
		final Map<WordRelationType, List<SemanticNode>> relations = converter.convertRelations(relationsToConvert, language);
		final SemanticNode target = relations.get(WordRelationType.SYNONYM).get(0);
		final Map<SemanticNode, Set<SemanticLink>> items = converter.createItemsForEntry(entries.get(0), relations, Arrays.asList("contextTest"), new ArrayList<>());

		Assertions.assertThat(items).isNotNull();
		Assertions.assertThat(items).hasSize(3);

		final Iterator<Map.Entry<SemanticNode, Set<SemanticLink>>> it = items.entrySet().iterator();
		Map.Entry<SemanticNode, Set<SemanticLink>> entry = it.next();
		SemanticNode item = entry.getKey();
		Assertions.assertThat(item.getWord()).isEqualTo("grün");
		Assertions.assertThat(item.getBaseForm()).isEqualTo("grün");
		Assertions.assertThat(item.getPartOfSpeech()).hasSize(1).containsExactly(PartOfSpeech.ADJECTIVE);
		Assertions.assertThat(item.getGender()).isNullOrEmpty();
		Assertions.assertThat(item.getDegrees()).hasSize(1).containsExactly(WordDegree.POSITIVE);
		Assertions.assertThat(item.getMultiplicities()).asList().hasSize(0);
		Assertions.assertThat(item.getTense()).isNullOrEmpty();

		Set<SemanticLink> links = entry.getValue();
		Assertions.assertThat(links).isNotNull().isNotEmpty().hasSize(1);
		Assertions.assertThat(compareLink(new SemanticLink(item, target, WordRelationType.SYNONYM.name()), links.iterator().next())).isTrue();
		Assertions.assertThat(item.getContext()).isNotNull().isNotEmpty().hasSize(1).containsOnly("contextTest");

		entry = it.next();
		item = entry.getKey();
		Assertions.assertThat(item.getWord()).isEqualTo("grüner");
		Assertions.assertThat(item.getBaseForm()).isEqualTo("grün");
		Assertions.assertThat(item.getPartOfSpeech()).hasSize(1).containsExactly(PartOfSpeech.ADJECTIVE);
		Assertions.assertThat(item.getGender()).isNullOrEmpty();
		Assertions.assertThat(item.getDegrees()).hasSize(1).containsExactly(WordDegree.COMPARATIVE);
		Assertions.assertThat(item.getMultiplicities()).asList().hasSize(0);
		Assertions.assertThat(item.getTense()).isNullOrEmpty();

		links = entry.getValue();
		Assertions.assertThat(links).isNotNull().isNotEmpty().hasSize(1);
		Assertions.assertThat(compareLink(new SemanticLink(item, target, WordRelationType.SYNONYM.name()), links.iterator().next())).isTrue();
		Assertions.assertThat(item.getContext()).isNotNull().isNotEmpty().hasSize(1).containsOnly("contextTest");

		entry = it.next();
		item = entry.getKey();
		Assertions.assertThat(item.getWord()).isEqualTo("am grünsten");
		Assertions.assertThat(item.getBaseForm()).isEqualTo("grün");
		Assertions.assertThat(item.getPartOfSpeech()).hasSize(1).containsExactly(PartOfSpeech.ADJECTIVE);
		Assertions.assertThat(item.getGender()).isNullOrEmpty();
		Assertions.assertThat(item.getDegrees()).hasSize(1).containsExactly(WordDegree.SUPERLATIVE);
		Assertions.assertThat(item.getMultiplicities()).asList().hasSize(0);
		Assertions.assertThat(item.getTense()).isNullOrEmpty();

		links = entry.getValue();
		Assertions.assertThat(links).isNotNull().isNotEmpty().hasSize(1);
		Assertions.assertThat(compareLink(new SemanticLink(item, target, WordRelationType.SYNONYM.name()), links.iterator().next())).isTrue();
		Assertions.assertThat(item.getContext()).isNotNull().isNotEmpty().hasSize(1).containsOnly("contextTest");
	}


	@Test
	public void convert_Noun() {
		final List<IWiktionaryEntry> entries = ConverterIntTest.wkt.getEntriesForWord("Auto", entry -> entry.getWordLanguage().getName().equals("German"));

		Assertions.assertThat(entries).isNotNull();
		Assertions.assertThat(entries).asList().hasSize(1);

		final Map<SemanticNode, Set<SemanticLink>> items = converter.convert(entries.get(0));
		Assertions.assertThat(items).isNotNull();
		Assertions.assertThat(items).hasSize(4);
		System.out.println(items);
	}


	@Test
	public void convert_Adjective() {
		final List<IWiktionaryEntry> entries = ConverterIntTest.wkt.getEntriesForWord("grün", entry -> entry.getWordLanguage().getName().equals("German"));

		Assertions.assertThat(entries).isNotNull();
		Assertions.assertThat(entries).asList().hasSize(2);

		final Map<SemanticNode, Set<SemanticLink>> items = converter.convert(entries.get(0));
		Assertions.assertThat(items).isNotNull();
		Assertions.assertThat(items).hasSize(27);
	}


	boolean compareLink(final SemanticLink expected, final SemanticLink actual) {
		if (expected == actual) {
			return true;
		}

		if (expected.getId() != actual.getId()) {
			if (actual.getId() == null || expected.getId() == null || !expected.getId().equals(actual.getId())) {
				return false;
			}
		}

		if (expected.getSource() != actual.getSource()) {
			if (actual.getSource() == null || expected.getSource() == null || !expected.getSource().equals(actual.getSource())) {
				return false;
			}
		}

		if (expected.getTarget() != actual.getTarget()) {
			if (actual.getTarget() == null || expected.getTarget() == null || !expected.getTarget().equals(actual.getTarget())) {
				return false;
			}
		}

		if (expected.getType() != actual.getType()) {
			if (actual.getType() == null || expected.getType() == null || !expected.getType().equals(actual.getType())) {
				return false;
			}
		}

		return true;
	}
}
