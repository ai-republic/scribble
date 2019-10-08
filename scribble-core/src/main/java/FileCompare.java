import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FileCompare {

	protected static List<String> listSourceFiles(final Path dir) throws IOException {
		final List<String> result = new ArrayList<>();
		try (final DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*")) {
			stream.forEach(path -> result.add(path.toFile().getName()));
		} catch (final DirectoryIteratorException ex) {
			// I/O error encounted during the iteration, the cause is an IOException
			throw ex.getCause();
		}
		return result;
	}


	public static void main(final String[] args) throws IOException {
		final String path1 = "C:/TEMP/Neuer Ordner/PS";
		final String path2 = "C:/TEMP/Neuer Ordner/2.1.5";

		final List<String> dir1 = listSourceFiles(Paths.get(path1));
		final List<String> dir2 = listSourceFiles(Paths.get(path2));

		Collections.sort(dir1);
		Collections.sort(dir2);

		for (final String file : dir1) {
			if (dir2.contains(file)) {
				System.out.println("XX\t" + file);
				dir2.remove(file);
			} else {
				System.out.println("X-\t" + file);
			}
		}

		for (final String file : dir2) {
			System.out.println("-X\t" + file);
		}
	}
}
