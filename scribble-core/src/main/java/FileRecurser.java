import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;

public class FileRecurser {
	static class TestKlasseUmbenenner extends SimpleFileVisitor<Path> {
		@Override
		public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
			info("Bearbeite Datei: " + file);
			Integer uc;
			Integer r;
			String name;
			boolean isRequirement = true;
			final String filename = file.getFileName().toString();

			try {
				if (filename.startsWith("UC")) {
					uc = Integer.parseInt(filename.substring(2, 4));

					if (filename.charAt(5) == 'R') {
						info("\tist ein Requirement-Test");
						r = Integer.parseInt(filename.substring(9, 11));
						name = filename.substring(11);
					} else if (filename.charAt(5) == 'A') {
						info("\tist ein Acceptance-Test");
						r = Integer.parseInt(filename.substring(6, 8));
						name = filename.substring(8);
						isRequirement = false;
					} else {
						fehler("\t\t => Konnte R nicht finden: " + filename);
						return super.visitFile(file, attrs);
					}
				} else if (filename.startsWith("R")) {
					info("\tist ein Requirement-Test");
					uc = Integer.parseInt(filename.substring(1, 3));
					r = Integer.parseInt(filename.substring(4, 6));
					name = filename.substring(6);
				} else {
					fehler("\t\t => Entspricht keinem bekannten Pattern: " + filename);
					return super.visitFile(file, attrs);
				}

				String newFilename;

				if (isRequirement) {
					newFilename = "R" + (uc < 10 ? "0" + uc : uc) + "_" + (r < 10 ? "0" + r : r) + name;
				} else {
					newFilename = "A" + (r < 10 ? "0" + r : r) + name;
				}

				final Path target = file.resolveSibling(Paths.get(newFilename));
				info("\t\t=> " + target);

				Files.move(file, target, StandardCopyOption.ATOMIC_MOVE);
				info("Ok.");
			} catch (final NumberFormatException e) {
				fehler("\t\t => Entspricht keinem bekannten Pattern: " + filename);
			}
			return super.visitFile(file, attrs);
		}
	}

	static class VerzeichnisUmschreiber extends SimpleFileVisitor<Path> {
		private final String rootDir;
		private Path newDirName;


		public VerzeichnisUmschreiber(final String rootDir) {
			this.rootDir = rootDir;
		}


		@Override
		public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
			info("Bearbeite Verzeichnis: " + dir.toString());

			if (Files.isDirectory(dir, LinkOption.NOFOLLOW_LINKS)) {
				final String dirname = dir.getFileName().toString();
				final StringBuilder newName = new StringBuilder();
				int lastChanged = -2;

				for (int i = 0; i < dirname.length(); i++) {
					final char chr = dirname.charAt(i);

					if (Character.isUpperCase(chr)) {
						if (i > 0 && dirname.charAt(i - 1) != '_' && lastChanged != i - 1) {
							newName.append("_");
						}

						newName.append(Character.toLowerCase(chr));
						lastChanged = i;
					} else {
						newName.append(chr);
					}
				}

				newDirName = dir.resolveSibling(newName.toString());

				info("\t=> " + newDirName.toAbsolutePath());
			}
			return super.preVisitDirectory(dir, attrs);
		}


		@Override
		public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException {
			Files.move(dir, newDirName, StandardCopyOption.ATOMIC_MOVE);

			return super.postVisitDirectory(dir, exc);
		}


		@Override
		public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
			if (file.getFileName().toString().endsWith(".java")) {
				BufferedReader reader = null;
				BufferedWriter writer = null;

				try {
					final Path tmpFile = file.getParent().resolve(file.getFileName().toString() + "_tmp");

					reader = new BufferedReader(new FileReader(file.toAbsolutePath().toFile()));
					writer = new BufferedWriter(new FileWriter(tmpFile.toAbsolutePath().toFile()));
					String line = reader.readLine();

					while (line != null) {

						if (line.startsWith("package ")) {
							line = "package " + newDirName.toString().substring(rootDir.length() + 1).replace(File.separatorChar, '.') + ";";
						}

						writer.write(line);
						writer.write("\n");

						line = reader.readLine();
					}

					reader.close();
					writer.close();

					Files.delete(file);
					Files.move(tmpFile, file, StandardCopyOption.ATOMIC_MOVE);
				} catch (final Throwable e) {
					e.printStackTrace();

					if (reader != null) {
						reader.close();
					}

					if (writer != null) {
						writer.close();
					}
				}
			}

			return super.visitFile(file, attrs);
		}
	}


	private static void close(final String msg) {
		System.err.println(msg);
		System.exit(-1);
	}


	private static void info(final String msg) {
		System.out.println(msg);
	}


	private static void fehler(final String msg) {
		System.err.println(msg);
	}


	public static void main(final String[] args) {
		if (args.length != 1) {
			close("Es wurde kein Verzeichnis als Parameter übergeben!");
		}

		final Path dir = Paths.get(args[0]);

		if (!Files.exists(dir, LinkOption.NOFOLLOW_LINKS)) {
			close("Der angegebene Pfad entspricht keinem Verzeichnis!\n" + dir.toAbsolutePath().toString());
		}

		// final FileVisitor<Path> fileVisitor = new TestKlasseUmbenenner();
		final FileVisitor<Path> fileVisitor = new VerzeichnisUmschreiber(args[0]);

		try {
			Files.walkFileTree(dir, fileVisitor);
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}
}
