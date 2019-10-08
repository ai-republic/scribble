package sem.jwktl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sem.api.PartOfSpeech;
import sem.api.SemanticLink;
import sem.api.SemanticNode;
import sem.api.WordDegree;
import sem.api.WordForm;
import sem.api.WordGender;
import sem.api.WordMultiplicity;
import sem.api.WordRelationType;
import sem.api.importer.ISemanticItemImportConverter;
import de.tudarmstadt.ukp.jwktl.api.IWiktionaryEntry;
import de.tudarmstadt.ukp.jwktl.api.IWiktionaryRelation;
import de.tudarmstadt.ukp.jwktl.api.IWiktionarySense;
import de.tudarmstadt.ukp.jwktl.api.IWiktionaryTranslation;
import de.tudarmstadt.ukp.jwktl.api.IWiktionaryWordForm;
import de.tudarmstadt.ukp.jwktl.api.RelationType;
import de.tudarmstadt.ukp.jwktl.api.util.GrammaticalDegree;
import de.tudarmstadt.ukp.jwktl.api.util.GrammaticalGender;
import de.tudarmstadt.ukp.jwktl.api.util.GrammaticalNumber;

/**
 * Converts the specified typed instance to a {@link SemanticNode}.
 *
 * @author Torsten Oltmanns<br>
 *         (c) Copyright 2015 ai-republic GmbH, Germany
 * @param <T> the typed instance to convert
 */
@ApplicationScoped
public class Converter implements ISemanticItemImportConverter<IWiktionaryEntry> {
	private final static Logger LOG = LoggerFactory.getLogger(Converter.class);


	@Override
	public Map<SemanticNode, Set<SemanticLink>> convert(final IWiktionaryEntry entry) {
		final Map<SemanticNode, Set<SemanticLink>> items = new LinkedHashMap<>();

		// create duplicate spelled words for each sense with their own
		// relations
		for (final IWiktionarySense sense : entry.getSenses()) {
			final Locale language = new Locale(entry.getWordLanguage().getISO639_3());
			final Map<WordRelationType, List<SemanticNode>> relations = convertRelations(sense.getRelations(), language);
			final List<String> contextWords = extractContext(sense);
			final Map<SemanticNode, Set<SemanticLink>> entryItems = createItemsForEntry(entry, relations, contextWords, sense.getTranslations());

			items.putAll(entryItems);
		}

		return items;
	}


	/**
	 * Converts the translations and returns a map of language locale to their translations.
	 *
	 * @param translations the list of JWKL translation for the sense
	 * @return the map of language locale to their translations
	 */
	protected Set<SemanticLink> convertTranslations(final SemanticNode node, final List<IWiktionaryTranslation> translations, final List<String> context) {
		final Set<SemanticLink> result = new HashSet<>();

		if (translations != null) {
			for (final IWiktionaryTranslation translation : translations) {

				Locale language = null;

				if (translation.getLanguage() != null) {
					if (translation.getLanguage().getISO639_3() != null) {
						language = new Locale(translation.getLanguage().getISO639_3());
					} else if (translation.getLanguage().getISO639_2T() != null) {
						language = new Locale(translation.getLanguage().getISO639_2T());
					} else if (translation.getLanguage().getISO639_2B() != null) {
						language = new Locale(translation.getLanguage().getISO639_2B());
					} else if (translation.getLanguage().getISO639_1() != null) {
						language = new Locale(translation.getLanguage().getISO639_1());
					}
				}

				final SemanticLink link = new SemanticLink(node, resolveItem(translation.getTranslation(), language, context), WordRelationType.TRANSLATION.name());
				link.addParameter("language", language);
				link.addParameter("transliteration", translation.getTransliteration());
				link.addParameter("gender", convertTranslationInfoToGender(translation.getAdditionalInformation()));
				result.add(link);
			}
		}

		return result;
	}


	private WordGender convertTranslationInfoToGender(final String additionalInformation) {
		WordGender gender = null;

		if (StringUtils.isNotEmpty(additionalInformation)) {
			if (additionalInformation.contains("{{m}}")) {
				gender = WordGender.MASCULINE;
			} else if (additionalInformation.contains("{{f}}")) {
				gender = WordGender.FEMININE;
			}
		}

		return gender;
	}


	/**
	 * Parses all terms in {{}} or [[]] brackets. When parsing the {{}} terms, it can optionally have some pre-leading command expressions separated with pipe (|) symbols which will be ignored.
	 *
	 * @param sense containing the contextual information about a term
	 * @return the list of contextual words in the glossary
	 */
	protected List<String> extractContext(final IWiktionarySense sense) {
		final List<String> context = new ArrayList<>();
		final String txt = sense.getGloss().getText();

		// first parse all {{xxxx|word}} terms
		// TODO: maybe ignore template {{}} due to indeterministic format, maybe
		// better to parse all Nouns in the quotation
		// in addition to [[]]?
		// int start = txt.indexOf("{{");
		//
		// while (start != -1) {
		// int end = txt.indexOf("}}", start + 2);
		// String expression = txt.substring(start + 2, end);
		//
		// // check if the expression contains commands
		// if (expression.indexOf('|') != -1) {
		// // the first part is a command - ignore
		// // the second part may be a language ref or a contextual word
		// // the third part may be a language ref or a contextual word
		// context.add(expression.substring(expression.lastIndexOf('|') + 1));
		// } else {
		// context.add(expression);
		// }
		//
		// start = txt.indexOf("{{", end);
		// }

		// then parse all [[word]] terms
		int start = txt.indexOf("[[");

		// check if there are words explicitly marked
		if (start != -1) {

			while (start != -1) {
				final int end = txt.indexOf("]]", start + 2);

				if (end > start + 2) {
					context.add(txt.substring(start + 2, end));
					start = txt.indexOf("[[", end);
				} else if (txt.length() > start + 2) {
					context.add(txt.substring(start + 2));
					start = -1;
				} else {
					start = -1;
				}
			}
		} else if (txt.length() > 0) {
			context.add(txt);
		}

		return context;
	}


	/**
	 * Creates {@link SemanticNode}s for the entry's word, possibly its base-form and (if specified) each word-form.
	 *
	 * @param entry the entry
	 * @param relations the relations
	 * @param context the contextual words
	 * @param translations the translations for the word context
	 * @return the collection of {@link SemanticNode}s
	 */
	protected Map<SemanticNode, Set<SemanticLink>> createItemsForEntry(final IWiktionaryEntry entry, final Map<WordRelationType, List<SemanticNode>> relations, final List<String> context, final List<IWiktionaryTranslation> translations) {
		final Map<String, SemanticNode> wordToItemMap = new LinkedHashMap<>();
		Map<SemanticNode, Set<SemanticLink>> result = null;
		result = new LinkedHashMap<>();
		final Map<GrammaticalNumber, List<WordForm>> wordForms = new HashMap<>();

		try {
			// if entrylink is set and the POS is WORD_FORM the entrylink
			// contains
			// the base form of a verb
			if (entry.getEntryLink() != null) {
				if (entry.getEntryLinkType().startsWith("Grundformverweis")) {
					if (!wordToItemMap.containsKey(entry.getWord())) {
						// create the word of the entry
						final SemanticNode item = new SemanticNode(entry.getWord());
						// set language
						item.setLanguage(new Locale(entry.getWordLanguage().getISO639_3()));
						// set base form
						item.setBaseForm(entry.getEntryLink());
						// TODO: Ignore and load all forms with base-form? Or load
						// base-form an create entries for this, then use
						// POS and other details for this entry

						// store.persistPojoNode(item);

						wordToItemMap.put(item.getWord(), item);

						// set relations
						final Set<SemanticLink> links = setRelations(item, relations);
						// set translations
						links.addAll(convertTranslations(item, translations, context));

						result.put(item, links);
					}
				} else {
					Converter.LOG.warn("Could not process entry-link-type: " + entry.getEntryLinkType() + " for word=" + entry.getWord());
				}
			} else {
				// create the word of the entry
				SemanticNode item = new SemanticNode(entry.getWord());
				// set language
				item.setLanguage(new Locale(entry.getWordLanguage().getISO639_3()));
				// set base form
				item.setBaseForm(entry.getWord());
				// set part-of-speech
				convertPartOfSpeech(item, entry.getPartsOfSpeech());
				// set gender
				convertGender(item, entry.getGender());
				// set context
				item.setContext(context);
				// set relations
				Set<SemanticLink> links = setRelations(item, relations);
				// set translations
				links.addAll(convertTranslations(item, translations, context));

				result.put(item, links);
				wordToItemMap.put(item.getWord(), item);

				if (entry.getWordForms() != null) {
					// create a word reference for each word form
					for (final IWiktionaryWordForm form : entry.getWordForms()) {
						if (form.getWordForm() != null) {
							final WordForm wordForm = convertWordForm(form);
							item = wordToItemMap.get(wordForm.getWord());

							if (item == null) {
								item = new SemanticNode(wordForm.getWord());
								// set language
								item.setLanguage(new Locale(entry.getWordLanguage().getISO639_3()));
								// set base form
								item.setBaseForm(entry.getWord());
								// set part-of-speech
								convertPartOfSpeech(item, entry.getPartsOfSpeech());
								// set gender
								convertGender(item, entry.getGender());
								// set context
								item.setContext(context);
								// set relations
								links = setRelations(item, relations);
								// set translations
								links.addAll(convertTranslations(item, translations, context));
								// set multiplicity (for nouns)
								convertMultiplicity(item, form.getNumber());
								// set degree (for adjectives)
								convertDegree(item, form.getDegree());

								result.put(item, links);
								wordToItemMap.put(item.getWord(), item);
							} else {
								// set multiplicity (for nouns)
								convertMultiplicity(item, form.getNumber());
								// set degree (for adjectives)
								convertDegree(item, form.getDegree());
							}

							// set wordform
							List<WordForm> list = wordForms.get(form.getNumber());

							if (list == null) {
								list = new ArrayList<>();
								wordForms.put(form.getNumber(), list);
							}

							if (!list.contains(wordForm)) {
								list.add(wordForm);
							}
						}
					}
				}
			}

			// cleanup multiplicities and set wordforms for the specific multiplicity type
			for (final SemanticNode node : wordToItemMap.values()) {

				// remove SINGULAR if word and baseform differ when PLURAL is also set, then its only PLURAL and SINGULAR
				// was set during reference creation
				if (node.getMultiplicities().contains(WordMultiplicity.PLURAL)) {
					if (node.getMultiplicities().contains(WordMultiplicity.SINGULAR) && node.getBaseForm() != null && !node.getWord().equals(node.getBaseForm())) {
						node.getMultiplicities().remove(WordMultiplicity.SINGULAR);
					}
				}

				// set the wordforms based on their multiplicity
				if (node.getMultiplicities().contains(WordMultiplicity.SINGULAR)) {
					node.setWordForms(wordForms.get(GrammaticalNumber.SINGULAR));
				}
				if (node.getMultiplicities().contains(WordMultiplicity.PLURAL)) {
					node.setWordForms(wordForms.get(GrammaticalNumber.PLURAL));
				}
			}
		} catch (final Exception e) {
			throw new RuntimeException("Error creating nodes from entry: " + entry, e);
		}

		return result;
	}


	/**
	 * Set the specified relations on the item.
	 *
	 * @param item the item
	 * @param relations the relations
	 */
	protected Set<SemanticLink> setRelations(final SemanticNode item, final Map<WordRelationType, List<SemanticNode>> relations) {
		final Set<SemanticLink> links = new LinkedHashSet<>();

		if (relations != null) {
			for (final WordRelationType relation : relations.keySet()) {
				for (final SemanticNode target : relations.get(relation)) {
					links.add(new SemanticLink(item, target, relation.name()));
				}
			}
		}

		return links;
	}


	/**
	 * Converts the specified relations to a supported mapping of relation-type and the corresponding word list and adds them to each specified item.
	 *
	 * @param relations the relations
	 * @return the supported relations
	 */
	protected Map<WordRelationType, List<SemanticNode>> convertRelations(final List<IWiktionaryRelation> relations, final Locale language) {
		if (relations != null) {
			final Map<WordRelationType, List<SemanticNode>> itemRelations = new LinkedHashMap<>();

			// convert the relation and create a map which lists all related
			// words mapped by their relation-type
			for (final IWiktionaryRelation relation : relations) {
				// convert the relation
				final WordRelationType type = convertRelationType(relation.getRelationType());

				// if we support the relation...
				if (type != null) {
					// ...check if a list of related words has already been
					// created
					List<SemanticNode> words = itemRelations.get(type);

					if (words == null) {
						words = new ArrayList<>();
						itemRelations.put(type, words);
					}

					// if the related word does not yet exist add it to the word
					// list of the relation-type
					if (!words.contains(relation.getTarget())) {
						words.add(resolveItem(relation.getTarget(), language, Arrays.asList(relation.getTargetSense())));
					}
				}
			}

			return itemRelations;
		}

		return null;
	}


	protected SemanticNode resolveItem(final String word, final Locale language, final List<String> senses) {
		SemanticNode node = null;

		try {
			// // build query to search for the word
			// final IGraphQuery query = store.createQuery();
			// query.findByProperty("word", word);
			//
			// // query store
			// final List<GraphNode> result = store.findNodes(query);
			//
			// if (!result.isEmpty()) {
			// node = store.convertToPojo(result.get(0), SemanticNode.class);
			// }

			// if no node was found in the store...
			if (node == null) {
				// ... create a new node and persist it
				node = new SemanticNode(word);
				node.setLanguage(language);
				node.setContext(senses);
				node.setReference(true);
				// store.persistPojoNode(node);
			}
		} catch (final Exception e) {
			Converter.LOG.error("Error resolving node for word: " + word);
			throw new RuntimeException("Error resolving node for word: " + word, e);
		}

		return node;
	}


	/**
	 * Converts the specified relation-type to a supported type.
	 *
	 * @param relationType the relation-type
	 * @return the converted type or null if not supported
	 */
	protected WordRelationType convertRelationType(final RelationType relationType) {
		switch (relationType) {
			case HYPERNYM:
				return WordRelationType.HYPERNYM;
			case HYPONYM:
				return WordRelationType.HYPONYM;
			case SYNONYM:
				return WordRelationType.SYNONYM;
			case ANTONYM:
				return WordRelationType.ANTONYM;
			case HOLONYM:
				return WordRelationType.HOLONYM;
			case MERONYM:
				return WordRelationType.MERONYM;
			default:
				return null;
		}
	}


	/**
	 * Converts the GrammaticalDegree to {@link WordDegree}.
	 *
	 * @param item the item
	 * @param gender the gender
	 */
	protected void convertDegree(final SemanticNode item, final GrammaticalDegree degree) {
		if (degree != null) {
			final List<WordDegree> list = item.getDegrees();

			switch (degree) {
				case POSITIVE:
					if (!list.contains(WordDegree.POSITIVE)) {
						list.add(WordDegree.POSITIVE);
					}
					break;
				case COMPARATIVE:
					if (!list.contains(WordDegree.COMPARATIVE)) {
						list.add(WordDegree.COMPARATIVE);
					}
					break;
				case SUPERLATIVE:
					if (!list.contains(WordDegree.SUPERLATIVE)) {
						list.add(WordDegree.SUPERLATIVE);
					}
					break;
			}
		}
	}


	/**
	 * Converts the list of part-of-speech tags and sets them on the item. <br>
	 * NOTE:
	 * <ul>
	 * <li>TOPONYM: sets the location flag on the item to true and is not set as a part-of-speech attribute</li>
	 * <li>WORD_FORM: is ignored</li>
	 * </ul>
	 *
	 * @param item the item
	 * @param posList the part-of-speech-tags
	 */
	protected void convertPartOfSpeech(final SemanticNode item, final List<de.tudarmstadt.ukp.jwktl.api.PartOfSpeech> posList) {
		if (posList != null) {
			for (final de.tudarmstadt.ukp.jwktl.api.PartOfSpeech pos : posList) {
				if (pos != null) {
					switch (pos) {
						case TOPONYM:
							item.setLocation(true);
							break;
						case WORD_FORM:
							break;
						default:
							final PartOfSpeech partOfSpeech = PartOfSpeech.valueOf(pos.name());
							if (!item.getPartOfSpeech().contains(partOfSpeech)) {
								item.getPartOfSpeech().add(partOfSpeech);
							}
					}
				}
			}
		}
	}


	/**
	 * Converts the GrammaticalGender to {@link WordGender}.
	 *
	 * @param item the item
	 * @param gender the gender
	 */
	protected void convertGender(final SemanticNode item, final GrammaticalGender gender) {
		if (gender != null) {
			switch (gender) {
				case NEUTER:
					item.getGender().add(WordGender.NEUTER);
					break;
				case FEMININE:
					item.getGender().add(WordGender.FEMININE);
					break;
				case MASCULINE:
					item.getGender().add(WordGender.MASCULINE);
					break;
				default:
					break;
			}
		}
	}


	/**
	 * Convert the GrammaticalNumber to {@link WordMultiplicity} on the specified item.
	 *
	 * @param item the item
	 * @param number the number
	 */
	protected void convertMultiplicity(final SemanticNode item, final GrammaticalNumber number) {
		if (number != null) {
			final List<WordMultiplicity> list = item.getMultiplicities();

			switch (number) {
				case SINGULAR:
					if (!list.contains(WordMultiplicity.SINGULAR)) {
						list.add(WordMultiplicity.SINGULAR);
					}
					break;
				case PLURAL:
					if (!list.contains(WordMultiplicity.PLURAL)) {
						list.add(WordMultiplicity.PLURAL);
					}
					break;
			}
		}
	}


	/**
	 * Cleans the specified expression from unwanted words/chars.
	 *
	 * @param expression the expression
	 * @return the cleaned expression
	 */
	protected WordForm convertWordForm(final IWiktionaryWordForm form) {
		final String expression = stripNonLetters(form.getWordForm());
		final WordForm wordForm = findPrepositions(expression);

		if (form.getCase() != null) { // for NOUNS
			wordForm.setForm(form.getCase().name());
		} else if (form.getDegree() != null) { // for ADJECTIVES
			wordForm.setForm(form.getDegree().name());
		}

		return wordForm;
	}


	/**
	 * Strips all non-letter characters from the expression but conserving the space chars.
	 *
	 * @param expression the expression
	 * @return the expression without non-letters
	 */
	protected String stripNonLetters(final String expression) {
		final StringBuilder strippedWord = new StringBuilder();
		expression.codePoints().forEachOrdered((value) -> {
			if (Character.isLetter(value) || Character.isSpaceChar(value)) {
				strippedWord.appendCodePoint(value);
			}
		});

		return strippedWord.toString();
	}


	/**
	 * Finds the configures prepositions from the specified expression and create a {@link WordForm} with the preposition and stripped word/expression.
	 *
	 * @param expression the expression
	 * @return a {@link WordForm} with the preposition and stripped word/expression
	 */
	protected WordForm findPrepositions(final String expression) {
		// TODO: externalize the list of prepositions
		final List<String> prepositions = Arrays.asList(new String[] { "der", "die", "das", "den", "dem", "des", "deren", "dessen", "a", "the" });

		final WordForm wordForm = new WordForm();
		final StringBuilder strippedWord = new StringBuilder();

		for (final String token : tokenize(expression)) {
			if (!prepositions.contains(token.toLowerCase())) {
				if (strippedWord.length() > 0) {
					strippedWord.append(" ");
				}

				strippedWord.append(token);
			} else {
				wordForm.setPreposition(token);
			}
		}

		wordForm.setWord(strippedWord.toString());

		return wordForm;
	}


	/**
	 * Tokenizes the specified expressions into a list of separate words.
	 *
	 * @param expression the expression
	 * @return the list of words
	 */
	protected List<String> tokenize(String expression) {
		// first trim any trailing spaces
		expression = expression.trim();

		int start = 0;
		int end = expression.indexOf(" ");
		final List<String> tokens = new ArrayList<>();

		while (end != -1) {
			try {
				tokens.add(expression.substring(start, end));

				start = end + 1;
				while (expression.charAt(start) == ' ') {
					start++;
				}

				end = expression.indexOf(" ", start);
			} catch (final Exception e) {
				Converter.LOG.error("Error tokenizing expression '" + expression + "']: tokens=" + tokens + ", start=" + start + ", end=" + end);
				throw e;
			}
		}

		if (start < expression.length() - 1) {
			end = expression.length();
			tokens.add(expression.substring(start, end));
		}

		return tokens;
	}

}
