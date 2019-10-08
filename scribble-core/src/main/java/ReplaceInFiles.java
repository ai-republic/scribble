import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JFileChooser;

/**
 * Opens a file dialog to choose a directory where all matches are replaced.
 *
 * @author Torsten Oltmanns
 *
 */
public class ReplaceInFiles {
	public static void main(final String[] args) throws IOException {
		final JFileChooser fileChooser = new JFileChooser(new File("."));
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		final int state = fileChooser.showOpenDialog(null);

		if (state == JFileChooser.APPROVE_OPTION) {
			final File file = fileChooser.getSelectedFile();
			final Path projectDir = file.toPath();

			final List<Path> allFiles = Files.walk(projectDir).parallel().filter(path -> {
				try {
					return path.getFileName().toString().endsWith(".java") && new String(Files.readAllBytes(path), Charset.forName("utf-8")).contains("package com.airepublic");
				} catch (final Exception e) {
					e.printStackTrace();
					return false;
				}
			}).collect(Collectors.toList());

			for (final Path fileToDelete : allFiles) {
				System.out.println(fileToDelete);
			}

			System.out.print("Really replace in all these files? (y/n): ");
			final char key = (char) System.in.read();

			if (key == 'y') {
				for (final Path path : allFiles) {
					String content = new String(Files.readAllBytes(path), Charset.forName("utf-8"));
					final int idx = content.indexOf("package com.airepublic");

					if (idx != -1) {
						content = content.replaceAll("package com.airepublic",
								"/**\n   Copyright 2015 Torsten Oltmanns, ai-republic GmbH, Germany\n\n   Licensed under the Apache License, Version 2.0 (the \"License\");\n   you may not use this file except in compliance with the License.\n   You may obtain a copy of the License at\n\n     http://www.apache.org/licenses/LICENSE-2.0\n\n   Unless required by applicable law or agreed to in writing, software\n   distributed under the License is distributed on an \"AS IS\" BASIS,\n   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n   See the License for the specific language governing permissions and\n   limitations under the License.\n*/\npackage com.airepublic");
						Files.write(path, content.getBytes(), StandardOpenOption.WRITE);
					}
					System.out.println("   replaced: " + path);
				}
			}
		}
	}
}
