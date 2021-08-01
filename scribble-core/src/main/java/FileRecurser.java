import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.function.Consumer;

import com.sun.nio.file.ExtendedCopyOption;

public class FileRecurser {
    protected static void process(final Path dir, final Consumer<Path> f) throws IOException {
        try (final DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*")) {
            stream.forEach(f::accept);
        } catch (final DirectoryIteratorException ex) {
            // I/O error encounted during the iteration, the cause is an IOException
            throw ex.getCause();
        }
    }


    public static void main(final String[] args) throws IOException {
        if (args == null || args.length != 2) {
            System.err.println("SYNTAX: <source> <target>");
            System.exit(0);
        }

        final Path source = Paths.get(args[0]);
        final Path target = Paths.get(args[1]);

        final Consumer<Path> deepCopy = (path) -> {
            // TODO implement
        };

        Files.walk(source).forEach(path -> {
            try {
                final Path relPath = target.resolve(path.toAbsolutePath().toString().substring(source.toAbsolutePath().toString().length() + 1));
                Files.copy(path, relPath, StandardCopyOption.REPLACE_EXISTING, ExtendedCopyOption.INTERRUPTIBLE);

                if (Files.isDirectory(path)) {
                    process(path, deepCopy);
                }
            } catch (final IOException e) {
                e.printStackTrace();
            }

        });

        FileRecurser.process(source, deepCopy);
    }
}
