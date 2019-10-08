package sem.jwktl;

import java.io.File;
import java.util.Locale;

import de.tudarmstadt.ukp.jwktl.JWKTL;
import de.tudarmstadt.ukp.jwktl.api.IWiktionaryEdition;
import de.tudarmstadt.ukp.jwktl.api.IWiktionaryEntry;
import de.tudarmstadt.ukp.jwktl.api.IWiktionaryPage;
import de.tudarmstadt.ukp.jwktl.api.IWiktionaryRelation;
import de.tudarmstadt.ukp.jwktl.api.IWiktionarySense;
import de.tudarmstadt.ukp.jwktl.api.IWiktionaryTranslation;
import de.tudarmstadt.ukp.jwktl.api.IWiktionaryWordForm;

public class WiktionaryImporter {

	public void importDictionary() {
		final File dumpFile = new File("C:/Users/Torsten/Downloads/enwiktionary-20150321-pages-articles.xml.bz2");
		final File outputDirectory = new File("D:/Development/projects/ai-republic/Scribble/en_new");
		final boolean overwriteExisting = false;

		JWKTL.parseWiktionaryDump(dumpFile, outputDirectory, overwriteExisting);

		// Connect to the Wiktionary database.
		final IWiktionaryEdition wkt = JWKTL.openEdition(outputDirectory);

		// Close the database connection.
		wkt.close();

	}


	public static void main(final String[] args) throws Exception {
		new WiktionaryImporter().importDictionary();

		final File outputDirectory = new File("de_new");

		// Connect to the Wiktionary database.
		final IWiktionaryEdition wkt = JWKTL.openEdition(outputDirectory);

		final IWiktionaryPage page = wkt.getPageForWord("Affe");

		for (final IWiktionaryEntry entry : page.getEntries()) {
			System.out.println("Word: " + entry.getWord());
			System.out.println("Language: " + entry.getWordLanguage());
			System.out.println("Entrylink: " + entry.getEntryLink());
			System.out.println("Entrylink-Type: " + entry.getEntryLinkType());
			System.out.println("\tPOS:\t\t\t" + entry.getPartsOfSpeech());
			System.out.println("\tGender: \t\t" + entry.getGender());
			System.out.println("\tEtymology: \t\t" + entry.getWordEtymology());

			if (entry.getWordForms() != null) {
				for (final IWiktionaryWordForm form : entry.getWordForms()) {
					System.out.println("\n\tWortform:\n\t\t" + form.getWordForm());
					System.out.println("\t\tCase:   " + form.getCase());
					System.out.println("\t\tAspect: " + form.getAspect());
					System.out.println("\t\tDegree: " + form.getDegree());
					System.out.println("\t\tMood:   " + form.getMood());
					System.out.println("\t\tNonFini:" + form.getNonFiniteForm());
					System.out.println("\t\tNumber: " + form.getNumber());
					System.out.println("\t\tPerson: " + form.getPerson());
					System.out.println("\t\tTense:  " + form.getTense());
				}
			}

			System.out.println("\n\tSenses:");
			for (final IWiktionarySense sense : entry.getSenses()) {
				System.out.println("\t\tMarker: " + sense.getMarker());
				System.out.println("\t\tIndex: " + sense.getIndex());
				System.out.println("\t\tKey: " + sense.getKey());
				System.out.println("\t\tExamples: \t" + sense.getExamples());
				try {
					System.out.println("\t\tQuotations: \t" + sense.getQuotations());
				} catch (final Exception e) {
				}
				System.out.println("\t\tReferences: \t" + sense.getReferences());
				System.out.println("\t\tGloss:\t\t" + sense.getGloss().getText());
				System.out.println("\t\tRelations:");

				if (sense.getRelations() != null) {
					for (final IWiktionaryRelation relation : sense.getRelations()) {
						System.out.println("\t\t\t" + relation.getRelationType() + ": [" + relation.getTargetSense() + "]" + relation.getTarget() + " (" + relation.getLinkType() + ")");
					}
				}

				System.out.println("\n\t\tTranslations:");
				if (sense.getTranslations() != null) {
					for (final IWiktionaryTranslation t : sense.getTranslations()) {
						final Locale locale = new Locale(t.getLanguage().getISO639_1());
						System.out.println("\t\t\tLanguage: " + locale.getDisplayName());
						System.out.println("\t\t\t\tTranslation: " + t.getTranslation());
						System.out.println("\t\t\t\tTransliteration: " + t.getTransliteration());
						System.out.println("\t\t\t\tAdditionalInfo: " + t.getAdditionalInformation());
					}
				} else {
					System.out.println("\t\t\tNone");
				}
			}

		}
		// Close the database connection.
		wkt.close();
	}
}
