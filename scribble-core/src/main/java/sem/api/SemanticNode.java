package sem.api;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import graph.api.annotation.GraphNode;
import graph.api.annotation.Id;
import graph.api.annotation.Index;

/**
 * Implements a semantic node representing a word or expression linked to others by relations.
 *
 * @author Torsten Oltmanns<br>
 *         (c) Copyright 2015 ai-republic GmbH, Germany
 *
 */
@GraphNode
public class SemanticNode implements Identifiable, Serializable {
	private static final long serialVersionUID = -5329472913968267944L;
	@Id
	private Object id;
	@Index
	private String word;
	private Locale language;
	private String baseForm;
	private List<WordTense> tense = new ArrayList<>();
	private List<WordGender> gender = new ArrayList<>();;
	private List<PartOfSpeech> partOfSpeech = new ArrayList<>();
	private boolean location;
	private List<WordMultiplicity> multiplicities = new ArrayList<>();
	private List<WordDegree> degrees = new ArrayList<>();
	private List<String> context;
	private List<WordForm> wordForms = new ArrayList<>();
	private boolean reference;


	/**
	 * Constructor.
	 */
	public SemanticNode() {
	}


	/**
	 * Constructor.
	 *
	 * @param word
	 */
	public SemanticNode(final String word) {
		this.word = word;
	}


	/**
	 * @return the id
	 */
	@Override
	public Object getId() {
		return id;
	}


	/**
	 * @param id the id to set
	 */
	public void setId(final Object id) {
		this.id = id;
	}


	/**
	 * @return the word
	 */
	public String getWord() {
		return word;
	}


	/**
	 * @param word the word to set
	 */
	public void setWord(final String word) {
		this.word = word;
	}


	/**
	 * @return the partOfSpeech
	 */
	public List<PartOfSpeech> getPartOfSpeech() {
		return partOfSpeech;
	}


	/**
	 * @param partOfSpeech the partOfSpeech to set
	 */
	public void setPartOfSpeech(final List<PartOfSpeech> partOfSpeech) {
		this.partOfSpeech = partOfSpeech;
	}


	/**
	 * @return the location
	 */
	public boolean isLocation() {
		return location;
	}


	/**
	 * @param location the location to set
	 */
	public void setLocation(final boolean location) {
		this.location = location;
	}


	/**
	 * Gets the list of multiplicities for a noun, e.g. singular or/and plural.
	 *
	 * @return the wordMultiplicity
	 */
	public List<WordMultiplicity> getMultiplicities() {
		return multiplicities;
	}


	/**
	 * Sets the list of multiplicities for a noun, e.g. singular or/and plural.
	 *
	 * @param wordMultiplicity the wordMultiplicity to set
	 */
	public void setMultiplicities(final List<WordMultiplicity> wordMultiplicities) {
		multiplicities = wordMultiplicities;
	}


	/**
	 * @return the gender
	 */
	public List<WordGender> getGender() {
		return gender;
	}


	/**
	 * @param gender the gender to set
	 */
	public void setGender(final List<WordGender> gender) {
		this.gender = gender;
	}


	/**
	 * @return the baseForm
	 */
	public String getBaseForm() {
		return baseForm;
	}


	/**
	 * @param baseForm the baseForm to set
	 */
	public void setBaseForm(final String baseForm) {
		this.baseForm = baseForm;
	}


	public List<WordForm> getWordForms() {
		return wordForms;
	}


	public void setWordForms(final List<WordForm> wordForms) {
		this.wordForms = wordForms;
	}


	/**
	 * @return the tense
	 */
	public List<WordTense> getTense() {
		return tense;
	}


	/**
	 * @param tense the tense to set
	 */
	public void setTense(final List<WordTense> tense) {
		this.tense = tense;
	}


	/**
	 * Gets the word degrees for an adjective (positive, comparative, superlative).
	 *
	 * @return the list of degrees
	 */
	public List<WordDegree> getDegrees() {
		return degrees;
	}


	/**
	 * Sets the word degrees for an adjective (positive, comparative, superlative).
	 *
	 * @param degrees the list of degrees to set
	 */
	public void setDegrees(final List<WordDegree> degrees) {
		this.degrees = degrees;
	}


	/**
	 * Gets a list of words that this item is in contextual relation with.
	 *
	 * @return the contextual words
	 */
	public List<String> getContext() {
		return context;
	}


	/**
	 * Sets a list of words that this item is in contextual relation with.
	 *
	 * @param context the contextual words to set
	 */
	public void setContext(final List<String> context) {
		this.context = context;
	}


	/**
	 * Gets the flag that this node is referring to another node which is possibly already persisted. Possibly there are multiple nodes which this reference could lead to. The correct one could be
	 * determined by comparing context information.
	 *
	 * @return the flag
	 */
	public boolean isReference() {
		return reference;
	}


	/**
	 * Sets the flag that this node is referring to another node which is possibly already persisted. Possibly there are multiple nodes which this reference could lead to. The correct one could be
	 * determined by comparing context information.
	 *
	 * @param reference the flag
	 */
	public void setReference(final boolean reference) {
		this.reference = reference;
	}


	/**
	 * @return the language
	 */
	public Locale getLanguage() {
		return language;
	}


	/**
	 * @param language the language to set
	 */
	public void setLanguage(final Locale language) {
		this.language = language;
	}


	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "SemanticNode [id=" + id + ", word=" + word + ", language=" + language + ", baseForm=" + baseForm + ", tense=" + tense + ", gender=" + gender + ", partOfSpeech=" + partOfSpeech + ", location=" + location + ", multiplicities=" + multiplicities + ", degrees=" + degrees + ", context=" + context + ", wordForms=" + wordForms + ", reference=" + reference + "]";
	}


	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((baseForm == null) ? 0 : baseForm.hashCode());
		result = prime * result + ((context == null) ? 0 : context.hashCode());
		result = prime * result + ((degrees == null) ? 0 : degrees.hashCode());
		result = prime * result + ((gender == null) ? 0 : gender.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + (location ? 1231 : 1237);
		result = prime * result + ((multiplicities == null) ? 0 : multiplicities.hashCode());
		result = prime * result + ((partOfSpeech == null) ? 0 : partOfSpeech.hashCode());
		result = prime * result + (reference ? 1231 : 1237);
		result = prime * result + ((tense == null) ? 0 : tense.hashCode());
		result = prime * result + ((word == null) ? 0 : word.hashCode());
		result = prime * result + ((wordForms == null) ? 0 : wordForms.hashCode());
		return result;
	}


	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
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
		final SemanticNode other = (SemanticNode) obj;
		if (baseForm == null) {
			if (other.baseForm != null) {
				return false;
			}
		} else if (!baseForm.equals(other.baseForm)) {
			return false;
		}
		if (context == null) {
			if (other.context != null) {
				return false;
			}
		} else if (!context.equals(other.context)) {
			return false;
		}
		if (degrees == null) {
			if (other.degrees != null) {
				return false;
			}
		} else if (!degrees.equals(other.degrees)) {
			return false;
		}
		if (gender == null) {
			if (other.gender != null) {
				return false;
			}
		} else if (!gender.equals(other.gender)) {
			return false;
		}
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		if (location != other.location) {
			return false;
		}
		if (multiplicities == null) {
			if (other.multiplicities != null) {
				return false;
			}
		} else if (!multiplicities.equals(other.multiplicities)) {
			return false;
		}
		if (partOfSpeech == null) {
			if (other.partOfSpeech != null) {
				return false;
			}
		} else if (!partOfSpeech.equals(other.partOfSpeech)) {
			return false;
		}
		if (reference != other.reference) {
			return false;
		}
		if (tense == null) {
			if (other.tense != null) {
				return false;
			}
		} else if (!tense.equals(other.tense)) {
			return false;
		}
		if (word == null) {
			if (other.word != null) {
				return false;
			}
		} else if (!word.equals(other.word)) {
			return false;
		}
		if (wordForms == null) {
			if (other.wordForms != null) {
				return false;
			}
		} else if (!wordForms.equals(other.wordForms)) {
			return false;
		}
		return true;
	}

}
