package sem.api;

import java.io.Serializable;

/**
 * The class stores a form of a word with its grammatical case, preposition and word.
 *
 * @author Torsten Oltmanns<br>
 *         (c) Copyright 2015 ai-republic GmbH, Germany
 */
public class WordForm implements Serializable {
	private static final long serialVersionUID = -8326823383602121593L;
	private String form;
	private String preposition;
	private String word;


	public String getForm() {
		return form;
	}


	public void setForm(final String form) {
		this.form = form;
	}


	public String getPreposition() {
		return preposition;
	}


	public void setPreposition(final String preposition) {
		this.preposition = preposition;
	}


	public String getWord() {
		return word;
	}


	public void setWord(final String word) {
		this.word = word;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((form == null) ? 0 : form.hashCode());
		result = prime * result + ((preposition == null) ? 0 : preposition.hashCode());
		result = prime * result + ((word == null) ? 0 : word.hashCode());
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
		final WordForm other = (WordForm) obj;
		if (form == null) {
			if (other.form != null) {
				return false;
			}
		} else if (!form.equals(other.form)) {
			return false;
		}
		if (preposition == null) {
			if (other.preposition != null) {
				return false;
			}
		} else if (!preposition.equals(other.preposition)) {
			return false;
		}
		if (word == null) {
			if (other.word != null) {
				return false;
			}
		} else if (!word.equals(other.word)) {
			return false;
		}
		return true;
	}


	@Override
	public String toString() {
		return "WordForm [form=" + form + ", preposition=" + preposition + ", word=" + word + "]";
	}

}
