package sem.api;

import java.io.Serializable;

public enum PartOfSpeech implements Serializable {
	/** Noun. */
	NOUN, /** Proper noun (names, locations, organizations) */
	PROPER_NOUN, /** First/given name (e.g. Nadine). */
	FIRST_NAME, /** Last/family name (e.g. Miller). */
	LAST_NAME, /** Toponym (i.e., a place name). */
	TOPONYM,

	/** Only takes the singular form. */
	SINGULARE_TANTUM, /** Only takes the plural form. */
	PLURALE_TANTUM,

	/** Measure words (e.g., litre). */
	MEASURE_WORD,

	/** Verb. */
	VERB, /** Auxiliary verb (can, might, must, etc.). */
	AUXILIARY_VERB,

	/** Adjective. */
	ADJECTIVE, /** Adverb. */
	ADVERB,

	/** Interjection. */
	INTERJECTION, /** Salutation (e.g., good afternoon). */
	SALUTATION, /** Onomatopoeia (e.g., peng, tic-tac). */
	ONOMATOPOEIA,

	/** Phrase. */
	PHRASE, /** Idiom (e.g., rock 'n' roll). */
	IDIOM, /** Collocation (e.g., strong tea). */
	COLLOCATION, /** Proverb (e.g., that's the way life is). */
	PROVERB, /**
				 * Mnemonic (e.g., "My Very Educated Mother Just Served Us Nachos" for planet names).
				 */
	MNEMONIC,

	/** Pronoun. */
	PRONOUN, /** (Irreflexive) personal pronoun (I, you, he, she, we, etc.). */
	PERSONAL_PRONOUN, /** Reflexive personal pronoun (myself, herself, ourselves, etc.). */
	REFLEXIVE_PRONOUN, /** Possessive pronoun (mine, your, our, etc.). */
	POSSESSIVE_PRONOUN, /** Demonstrative pronoun (_This_ is fast). */
	DEMONSTRATIVE_PRONOUN, /** Relative pronoun (She sold the car, _which_ was very old ). */
	RELATIVE_PRONOUN, /** Indefinite pronoun (_Nobody_ bought the car ). */
	INDEFINITE_PRONOUN,

	/** Interrogative pronoun (who, what, etc.). */
	INTERROGATIVE_PRONOUN, /** Interrogative adverb (how, when, etc.). */
	INTERROGATIVE_ADVERB,

	/** Particle. */
	PARTICLE, /** Answer particle (yes, no, etc.). */
	ANSWERING_PARTICLE, /** Negative particle (neither...nor, etc.). */
	NEGATIVE_PARTICLE, /** Comparative particle (She is taller _than_ me). */
	COMPARATIVE_PARTICLE, /** Focus particle (also, only, even, etc.). */
	FOCUS_PARTICLE, /** Intensifying particle (very, low, etc.). */
	INTENSIFYING_PARTICLE, /** Modal particle (express attitude, e.g., German: Sprich _doch mal_ mit ihr ). */
	MODAL_PARTICLE,

	/** Article (a, the, etc.). */
	ARTICLE, /** Determiner (few, most, etc.). */
	DETERMINER,

	/** Abbreviation. */
	ABBREVIATION, /** Acronym (pronounced as a word, e.g., "ROM", "NATO", "sonar") */
	ACRONYM, /** Initialism (pronounced as letter by letter, e.g., "CD", "URL") */
	INITIALISM, /** Contraction (e.g., it's). */
	CONTRACTION,

	/** Conjunction (and, or, etc.). */
	CONJUNCTION, /** Subordinating conjunction (as soon as, after, etc.). */
	SUBORDINATOR,

	/** Preposition (e.g., underneath). */
	PREPOSITION, /** Postposition (e.g., ago). */
	POSTPOSITION,

	/** Affix. */
	AFFIX, /** Prefix. */
	PREFIX, /** Suffix. */
	SUFFIX, /** Place name suffix (e.g., -burg). */
	PLACE_NAME_ENDING, /** Bound lexeme. */
	LEXEME,

	/** Character. */
	CHARACTER, /** Letter of the alphabet (A, B, C, etc.). */
	LETTER, /** Number and numeral (e.g., two, fifteen, etc.). */
	NUMBER, /** Number and numeral (e.g., two, fifteen, etc.). */
	NUMERAL, /** Punctuation mark (., ?, ;, etc.). */
	PUNCTUATION_MARK, /** Symbol (+, Â§, $, etc.). */
	SYMBOL, /** Chinese Hanzi character. */
	HANZI, /** Japanese Kanji character. */
	KANJI, /** Japanese Katakana character. */
	KATAKANA, /** Japanese Hiragana character. */
	HIRAGANA,

	/** Gismu (a root word in Lojban). */
	GISMU,

	/** Inflected word form. */
	WORD_FORM, /** Participle. */
	PARTICIPLE, /** Transliterated word form. */
	TRANSLITERATION, UNKNOWN;

}