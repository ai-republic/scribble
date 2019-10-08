import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JFileChooser;

/**
 * Opens a file dialog to choose a directory where all .settings, .svn, .project, .classpath and
 * target folders are deleted.
 *
 * @author Torsten Oltmanns
 *
 */
public class ProjectCleaner {
    public static void main(final String[] args) throws IOException {
        final JFileChooser fileChooser = new JFileChooser(new File("."));
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        final int state = fileChooser.showOpenDialog(null);

        if (state == JFileChooser.APPROVE_OPTION) {
            final File file = fileChooser.getSelectedFile();
            final Path projectDir = file.toPath();

            final List<Path> toDelete = Files.walk(projectDir).parallel().filter(path -> {
                if (path.getFileName().toString().equals(".git")) {
                    // switch (path.getFileName().toString()) {
                    // case ".settings":
                    // case ".svn":
                    // case ".project":
                    // case ".classpath":
                    // case "target":
                    // case "bin":
                    return true;
                }
                return false;
            }).collect(Collectors.toList());

            for (final Path fileToDelete : toDelete) {
                System.out.println(fileToDelete);
            }

            System.out.print("Really delete all these files? (y/n): ");
            final char key = (char) System.in.read();

            if (key == 'y') {
                for (final Path fileToDelete : toDelete) {
                    if (Files.isDirectory(fileToDelete)) {
                        Files.walkFileTree(fileToDelete, new SimpleFileVisitor<Path>() {
                            @Override
                            public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
                                Files.delete(file);
                                System.out.println("   deleted: " + file);
                                return FileVisitResult.CONTINUE;
                            }


                            @Override
                            public FileVisitResult postVisitDirectory(final Path dir, final IOException e) throws IOException {
                                if (e == null) {
                                    Files.delete(dir);
                                    System.out.println("   deleted: " + dir);
                                    return FileVisitResult.CONTINUE;
                                } else {
                                    // directory iteration failed
                                    throw e;
                                }
                            }

                        });
                    } else {
                        Files.delete(fileToDelete);
                        System.out.println("   deleted: " + file);
                    }
                }
            }
        }
    }
}
