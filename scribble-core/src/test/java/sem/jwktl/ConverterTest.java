package sem.jwktl;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.assertj.core.api.Assertions;
import org.assertj.core.data.Index;
import org.junit.Test;

import sem.api.PartOfSpeech;
import sem.api.SemanticLink;
import sem.api.SemanticNode;
import sem.api.WordDegree;
import sem.api.WordForm;
import sem.api.WordGender;
import sem.api.WordMultiplicity;
import sem.api.WordRelationType;
import de.tudarmstadt.ukp.jwktl.api.IWiktionaryRelation;
import de.tudarmstadt.ukp.jwktl.api.RelationType;
import de.tudarmstadt.ukp.jwktl.api.entry.WikiString;
import de.tudarmstadt.ukp.jwktl.api.entry.WiktionaryRelation;
import de.tudarmstadt.ukp.jwktl.api.entry.WiktionarySense;
import de.tudarmstadt.ukp.jwktl.api.entry.WiktionaryWordForm;
import de.tudarmstadt.ukp.jwktl.api.util.GrammaticalCase;
import de.tudarmstadt.ukp.jwktl.api.util.GrammaticalDegree;
import de.tudarmstadt.ukp.jwktl.api.util.GrammaticalGender;
import de.tudarmstadt.ukp.jwktl.api.util.GrammaticalNumber;

public class ConverterTest {
  private final Converter converter = new Converter();
  private final Locale language = new Locale("deu");


  @Test
  public void stripNonLetters() {
    final String result = converter.stripNonLetters("(das) Augsburg");
    Assertions.assertThat(result).isEqualTo("das Augsburg");
  }


  @Test
  public void stripPrepositions() {
    final WordForm result = converter.findPrepositions("das Augsburg");
    Assertions.assertThat(result.getPreposition()).isEqualTo("das");
    Assertions.assertThat(result.getWord()).isEqualTo("Augsburg");
  }


  @Test
  public void tokenize() {
    final List<String> result = converter.tokenize("das Augsburg");
    Assertions.assertThat(result).isNotNull().isNotEmpty().hasSize(2).asList().contains("das", Index.atIndex(0))
        .contains("Augsburg", Index.atIndex(1));
  }


  @Test
  public void convertWordForm_Noun() {
    final WiktionaryWordForm form = new WiktionaryWordForm();
    form.setWordForm("(das) Augsburg");
    form.setCase(GrammaticalCase.NOMINATIVE);
    final WordForm result = converter.convertWordForm(form);
    Assertions.assertThat(result.getForm()).isEqualTo("NOMINATIVE");
    Assertions.assertThat(result.getPreposition()).isEqualTo("das");
    Assertions.assertThat(result.getWord()).isEqualTo("Augsburg");
  }


  @Test
  public void convertWordForm_Adjective() {
    final WiktionaryWordForm form = new WiktionaryWordForm();
    form.setWordForm("am grünsten");
    form.setDegree(GrammaticalDegree.SUPERLATIVE);
    final WordForm result = converter.convertWordForm(form);
    Assertions.assertThat(result.getForm()).isEqualTo("SUPERLATIVE");
    Assertions.assertThat(result.getPreposition()).isNullOrEmpty();
    Assertions.assertThat(result.getWord()).isEqualTo("am grünsten");
  }


  @Test
  public void convertMultiplicity() {
    // test assigning singular
    SemanticNode item = new SemanticNode("Augsburg");
    GrammaticalNumber number = GrammaticalNumber.SINGULAR;
    converter.convertMultiplicity(item, number);
    Assertions.assertThat(item.getMultiplicities()).asList().hasSize(1).contains(WordMultiplicity.SINGULAR);

    // adding singular again
    number = GrammaticalNumber.SINGULAR;
    converter.convertMultiplicity(item, number);
    Assertions.assertThat(item.getMultiplicities()).asList().hasSize(1).contains(WordMultiplicity.SINGULAR);

    // and in addition plural
    number = GrammaticalNumber.PLURAL;
    converter.convertMultiplicity(item, number);
    Assertions.assertThat(item.getMultiplicities()).asList().hasSize(2).hasSize(2)
        .contains(WordMultiplicity.SINGULAR, WordMultiplicity.PLURAL);

    // adding singular again
    number = GrammaticalNumber.SINGULAR;
    converter.convertMultiplicity(item, number);
    Assertions.assertThat(item.getMultiplicities()).asList().hasSize(2)
        .contains(WordMultiplicity.SINGULAR, WordMultiplicity.PLURAL);

    // adding plural again
    number = GrammaticalNumber.PLURAL;
    converter.convertMultiplicity(item, number);
    Assertions.assertThat(item.getMultiplicities()).asList().hasSize(2)
        .contains(WordMultiplicity.SINGULAR, WordMultiplicity.PLURAL);

    // test assigning plural
    item = new SemanticNode("Augsburg");
    number = GrammaticalNumber.PLURAL;
    converter.convertMultiplicity(item, number);
    Assertions.assertThat(item.getMultiplicities()).asList().hasSize(1).contains(WordMultiplicity.PLURAL);

    // adding plural again
    number = GrammaticalNumber.PLURAL;
    converter.convertMultiplicity(item, number);
    Assertions.assertThat(item.getMultiplicities()).asList().hasSize(1).contains(WordMultiplicity.PLURAL);

    // and in addition singular
    number = GrammaticalNumber.SINGULAR;
    converter.convertMultiplicity(item, number);
    Assertions.assertThat(item.getMultiplicities()).asList().hasSize(2)
        .contains(WordMultiplicity.SINGULAR, WordMultiplicity.PLURAL);

    // adding plural again
    number = GrammaticalNumber.PLURAL;
    converter.convertMultiplicity(item, number);
    Assertions.assertThat(item.getMultiplicities()).asList().hasSize(2)
        .contains(WordMultiplicity.SINGULAR, WordMultiplicity.PLURAL);

    // adding singular again
    number = GrammaticalNumber.SINGULAR;
    converter.convertMultiplicity(item, number);
    Assertions.assertThat(item.getMultiplicities()).asList().hasSize(2)
        .contains(WordMultiplicity.SINGULAR, WordMultiplicity.PLURAL);
  }


  @Test
  public void convertGender() {
    final SemanticNode item = new SemanticNode("Augsburg");
    converter.convertGender(item, GrammaticalGender.MASCULINE);
    Assertions.assertThat(item.getGender()).containsExactly(WordGender.MASCULINE);

    item.getGender().clear();
    converter.convertGender(item, GrammaticalGender.FEMININE);
    Assertions.assertThat(item.getGender()).containsExactly(WordGender.FEMININE);

    item.getGender().clear();
    converter.convertGender(item, GrammaticalGender.NEUTER);
    Assertions.assertThat(item.getGender()).containsExactly(WordGender.NEUTER);
  }


  @Test
  public void convertDegree() {
    final SemanticNode item = new SemanticNode("Augsburg");
    // add positive
    converter.convertDegree(item, GrammaticalDegree.POSITIVE);
    Assertions.assertThat(item.getDegrees()).asList().hasSize(1).contains(WordDegree.POSITIVE);

    // add positive again
    converter.convertDegree(item, GrammaticalDegree.POSITIVE);
    Assertions.assertThat(item.getDegrees()).asList().hasSize(1).contains(WordDegree.POSITIVE);

    // add comparative
    converter.convertDegree(item, GrammaticalDegree.COMPARATIVE);
    Assertions.assertThat(item.getDegrees()).asList().hasSize(2).contains(WordDegree.POSITIVE, WordDegree.COMPARATIVE);

    // add comparative again
    converter.convertDegree(item, GrammaticalDegree.COMPARATIVE);
    Assertions.assertThat(item.getDegrees()).asList().hasSize(2).contains(WordDegree.POSITIVE, WordDegree.COMPARATIVE);

    // add superlative
    converter.convertDegree(item, GrammaticalDegree.SUPERLATIVE);
    Assertions.assertThat(item.getDegrees()).asList().hasSize(3)
        .contains(WordDegree.POSITIVE, WordDegree.COMPARATIVE, WordDegree.SUPERLATIVE);

    // add superlative again
    converter.convertDegree(item, GrammaticalDegree.SUPERLATIVE);
    Assertions.assertThat(item.getDegrees()).asList().hasSize(3)
        .contains(WordDegree.POSITIVE, WordDegree.COMPARATIVE, WordDegree.SUPERLATIVE);
  }


  @Test
  public void convertPartOfSpeech() {
    final SemanticNode item = new SemanticNode("Augsburg");
    final List<de.tudarmstadt.ukp.jwktl.api.PartOfSpeech> posList =
        Arrays
            .asList(de.tudarmstadt.ukp.jwktl.api.PartOfSpeech.NOUN, de.tudarmstadt.ukp.jwktl.api.PartOfSpeech.TOPONYM);
    converter.convertPartOfSpeech(item, posList);
    Assertions.assertThat(item.getPartOfSpeech()).hasSize(1).containsExactly(PartOfSpeech.NOUN);
    Assertions.assertThat(item.isLocation()).isTrue();
  }


  @Test
  public void convertRelationType() {
    Assertions.assertThat(converter.convertRelationType(RelationType.ANTONYM)).isEqualTo(WordRelationType.ANTONYM);
    Assertions.assertThat(converter.convertRelationType(RelationType.HOLONYM)).isEqualTo(WordRelationType.HOLONYM);
    Assertions.assertThat(converter.convertRelationType(RelationType.HYPERNYM)).isEqualTo(WordRelationType.HYPERNYM);
    Assertions.assertThat(converter.convertRelationType(RelationType.HYPONYM)).isEqualTo(WordRelationType.HYPONYM);
    Assertions.assertThat(converter.convertRelationType(RelationType.MERONYM)).isEqualTo(WordRelationType.MERONYM);
    Assertions.assertThat(converter.convertRelationType(RelationType.SYNONYM)).isEqualTo(WordRelationType.SYNONYM);
  }


  @Test
  public void convertRelations() {
    final WiktionaryRelation relation = new WiktionaryRelation("test", RelationType.SYNONYM);
    final List<IWiktionaryRelation> relationsToConvert = Arrays.asList(relation);
    final Map<WordRelationType, List<SemanticNode>> relations =
        converter.convertRelations(relationsToConvert, language);

    Assertions.assertThat(relations.containsKey(WordRelationType.SYNONYM)).isTrue();
    Assertions.assertThat(relations.get(WordRelationType.SYNONYM)).isNotNull().isNotEmpty();
    Assertions.assertThat(relations.get(WordRelationType.SYNONYM).get(0)).isEqualToComparingOnlyGivenFields(
        new SemanticNode("test"), "word");
  }


  @Test
  public void extractContext() {
    final WiktionarySense sense = new WiktionarySense();
    sense.setGloss(new WikiString(
        "{{Kontext|Spiel}} [[Spielzeug]] oder kleines [[Fahrzeug]] für [[Kinder]], das wie [1] aussieht"));
    final List<String> context = converter.extractContext(sense);
    Assertions.assertThat(context).hasSize(3).containsExactly(/* "Spiel", */"Spielzeug", "Fahrzeug", "Kinder");
  }


  @Test
  public void setRelations() {
    final Map<WordRelationType, List<SemanticNode>> relations = new LinkedHashMap<>();
    final SemanticNode a = new SemanticNode("a");
    final SemanticNode b = new SemanticNode("b");
    final SemanticNode c = new SemanticNode("c");
    final SemanticNode d = new SemanticNode("d");
    final SemanticNode e = new SemanticNode("e");
    final SemanticNode f = new SemanticNode("f");

    relations.put(WordRelationType.ANTONYM, Arrays.asList(a));
    relations.put(WordRelationType.HOLONYM, Arrays.asList(b));
    relations.put(WordRelationType.HYPERNYM, Arrays.asList(c));
    relations.put(WordRelationType.HYPONYM, Arrays.asList(d));
    relations.put(WordRelationType.MERONYM, Arrays.asList(e));
    relations.put(WordRelationType.SYNONYM, Arrays.asList(f));

    final SemanticNode item = new SemanticNode("test");
    final Set<SemanticLink> links = converter.setRelations(item, relations);
    Assertions.assertThat(links).isNotNull().isNotEmpty().hasSize(6);
    final Iterator<SemanticLink> linkIt = links.iterator();
    Assertions.assertThat(compareLink(new SemanticLink(item, a, WordRelationType.ANTONYM.name()), linkIt.next()))
        .isTrue();
    Assertions.assertThat(compareLink(new SemanticLink(item, b, WordRelationType.HOLONYM.name()), linkIt.next()))
        .isTrue();
    Assertions.assertThat(compareLink(new SemanticLink(item, c, WordRelationType.HYPERNYM.name()), linkIt.next()))
        .isTrue();
    Assertions.assertThat(compareLink(new SemanticLink(item, d, WordRelationType.HYPONYM.name()), linkIt.next()))
        .isTrue();
    Assertions.assertThat(compareLink(new SemanticLink(item, e, WordRelationType.MERONYM.name()), linkIt.next()))
        .isTrue();
    Assertions.assertThat(compareLink(new SemanticLink(item, f, WordRelationType.SYNONYM.name()), linkIt.next()))
        .isTrue();
  }


  boolean compareLink(final SemanticLink expected, final SemanticLink actual) {
    if (expected.getId() != actual.getId()) {
      return false;
    }

    if (expected.getSource() != actual.getSource()) {
      return false;
    }

    if (expected.getTarget() != actual.getTarget()) {
      return false;
    }

    if (expected.getType() != actual.getType()) {
      return false;
    }

    return true;
  }
}
