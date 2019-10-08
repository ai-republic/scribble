package classloader;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class AllClassLoader extends URLClassLoader {
	private final ClassLoadHandler handler;

	public static interface ClassLoadHandler {
		void handleLoadedClass(Class<?> clazz);
	}


	public AllClassLoader(final ClassLoadHandler handler) {
		super(new URL[] {}, AllClassLoader.class.getClassLoader());

		ClassLoader cl = AllClassLoader.class.getClassLoader();

		while (cl != null) {
			if (cl instanceof URLClassLoader) {
				for (final URL url : ((URLClassLoader) cl).getURLs()) {
					System.out.println("adding URL: " + url);
					addURL(url);
				}
			} else {
				System.out.println("Not an URLClassLoader: " + cl.getClass().getName());
			}

			cl = cl.getParent();
		}

		this.handler = handler;

		for (final URL url : getURLs()) {
			loadClassesForURL(url);
		}
	}


	protected void loadClassesForURL(final URL url) {
		URLConnection urlc = null;

		try {
			urlc = url.openConnection();

			if (urlc instanceof JarURLConnection) {
				final JarURLConnection juc = (JarURLConnection) urlc;
				final JarFile jar = juc.getJarFile();

				loadClassesInJar(jar);

			} else {
				final File file = new File(url.getFile());

				if (file.isDirectory()) {
					loadClassesInDirectory(file.getAbsolutePath(), file);
				} else if (file.getName().endsWith(".class")) {
					loadClassFromFile("", file);
				} else if (file.getName().endsWith(".jar")) {
					loadClassesInJar(new JarFile(file));
				}
			}
		} catch (final Exception e) {
			System.err.println(e.getClass().getName() + ":" + e.getMessage());
		} finally {
			if (urlc != null) {
				try {
					urlc.getInputStream().close();
				} catch (final IOException e) {
				}
			}
		}
	}


	protected void loadClassesInJar(final JarFile jar) {
		JarEntry entry = null;
		final Enumeration<JarEntry> entries = jar.entries();
		String className = "";

		while (entries.hasMoreElements()) {
			entry = entries.nextElement();

			try {
				if (entry.isDirectory() || !entry.getName().endsWith(".class")) {
					continue;
				}
				// -6 because of .class
				className = entry.getName().substring(0, entry.getName().length() - 6);
				className = className.replace('/', '.');

				handler.handleLoadedClass(loadClass(className));
			} catch (final Throwable e) {
				// System.err.println("Could not find '" + className + "' in Jar:" + jar.getName());
				// e.printStackTrace();
				System.err.println(e.getClass().getName() + ":" + e.getMessage());
			}
		}
	}


	protected void loadClassesInDirectory(final String rootDir, final File dir) throws ClassNotFoundException {
		for (final File file : dir.listFiles()) {
			if (file.isDirectory()) {
				loadClassesInDirectory(rootDir, file);
			} else {
				if (file.getName().endsWith(".class")) {
					loadClassFromFile(rootDir, file);
				}
			}
		}
	}


	protected void loadClassFromFile(final String rootDir, final File file) throws ClassNotFoundException {
		if (file.getName().endsWith(".class")) {
			String className = file.getAbsolutePath().substring(rootDir.length());
			if (className.startsWith(File.separator)) {
				className = className.substring(File.separator.length());
			}

			className = className.substring(0, className.length() - 6);
			className = className.replace(File.separatorChar, '.');

			handler.handleLoadedClass(loadClass(className));
		}

		throw new IllegalArgumentException("Not a class file: " + file);
	}
}
