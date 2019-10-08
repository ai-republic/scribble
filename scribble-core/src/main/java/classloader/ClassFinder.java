package classloader;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ClassFinder {

	private static final String JAR_CONTENT_URL_DELIMITER = "!";
	private static final String INTERNALclazz_NAMESPACE_DELIMITER = "$";
	private static final String CLASS_EXTENSION = ".class";
	private static final String PACKAGE_DELIMITER = ".";
	private static final String PATH_DELIMITER = "/";
	private static final String JAR_EXTENSION = "jar";
	private static final String ZIP_EXTENSION = "zip";
	private static final String ENCODING = "UTF-8";
	private static final String FILE_PROTOCOL = "file:";

	private final ClassLoader classLoader;
	private boolean ignoreNoClassDefFoundError = false;


	public boolean isIgnoreClassNotFoundException() {
		return ignoreNoClassDefFoundError;
	}


	/**
	 * If the class finder traverses a class in which not all imports can be resolved a {@link NoClassDefFoundError} is thrown. If {@code ignoreNoClassDefFoundError} is set to {@code true} those
	 * classes will be skipped.
	 * 
	 * @param ignoreNoClassDefFoundError
	 */
	public void setIgnoreNoClassDefFoundError(final boolean ignoreNoClassDefFoundError) {
		this.ignoreNoClassDefFoundError = ignoreNoClassDefFoundError;
	}


	public ClassFinder(final ClassLoader classLoader) {
		this.classLoader = classLoader;
	}


	public ClassFinder() {
		this(Thread.currentThread().getContextClassLoader());
	}


	/**
	 * Scans all classes accessible from the class loader which belong to the given package and its sub packages.
	 * 
	 * If {@code IgnoreNoClassDefFoundError} is set to {@code true} problematic classes will be skipped.
	 * 
	 * @param packageName the base package to start searching
	 * @return the found classes
	 * @throws ClassNotFoundException if the given {@code packageName} could not be loaded
	 * @throws NoClassDefError if the given {@code packageName} could not be loaded
	 * @throws IOException if an I/O error occurs during loading of any required classpath resources (e.g. jar files)
	 */
	public List<Class<?>> getClasses(final String packageName) throws ClassNotFoundException, IOException {

		final List<Class<?>> result = new ArrayList<>();

		final Enumeration<URL> resources = classLoader.getResources(packageToPath(packageName));

		while (resources.hasMoreElements()) {
			final URL resource = resources.nextElement();
			if (resource.getProtocol().equals(JAR_EXTENSION) || resource.getProtocol().equals(ZIP_EXTENSION)) {
				// loop through entries in jar file
				String jarFileName = URLDecoder.decode(resource.getFile(), ENCODING);
				if (jarFileName.startsWith(FILE_PROTOCOL)) {
					jarFileName = jarFileName.substring(FILE_PROTOCOL.length());
				}
				jarFileName = jarFileName.substring(0, jarFileName.indexOf(JAR_CONTENT_URL_DELIMITER));
				result.addAll(findClassesInJarFile(new JarFile(jarFileName), packageName));
			} else {
				// loop through files in classpath
				final String fileName = resource.getFile();
				final String fileNameDecoded = URLDecoder.decode(fileName, ENCODING);
				result.addAll(findClassesInDirectory(new File(fileNameDecoded), packageName));
			}
		}

		return result;
	}


	/**
	 * Gibt alle Klassen dieses Pakets und der Sub-Pakete zurück - nicht jedoch innere Klassen, lokale Klassen etc.
	 */
	private Collection<? extends Class<?>> findClassesInJarFile(final JarFile jarFile, final String packageName) throws ClassNotFoundException {
		final List<Class<?>> result = new ArrayList<Class<?>>();
		final String path = packageToPath(packageName);
		final Enumeration<JarEntry> jarEntries = jarFile.entries();
		while (jarEntries.hasMoreElements()) {
			final String entryName = jarEntries.nextElement().getName();
			if (entryName.startsWith(path + PATH_DELIMITER) && entryName.endsWith(CLASS_EXTENSION) && !entryName.contains(INTERNALclazz_NAMESPACE_DELIMITER)) {
				String subPath = entryName.substring(path.length(), entryName.lastIndexOf(PACKAGE_DELIMITER));
				if (subPath.startsWith(PATH_DELIMITER)) {
					subPath = subPath.substring(1);
				}

				final String fullQualifiedClassname = packageName + PACKAGE_DELIMITER + subPath.replace(PATH_DELIMITER.charAt(0), PACKAGE_DELIMITER.charAt(0));
				final Class<?> clazz = loadClass(fullQualifiedClassname);
				if (clazz != null) {
					result.add(clazz);
				}
			}
		}
		return result;
	}


	private String packageToPath(final String packageName) {
		final String path = packageName.replace('.', '/');
		return path;
	}


	/**
	 * Recursive method used to find all classes in a given directory and subdirs. Does not return inner classes, local classes etc.
	 * 
	 * @param directory The base directory
	 * @param packageName The package name for classes found inside the base directory
	 * @return The classes
	 */
	private List<Class<?>> findClassesInDirectory(final File directory, final String packageName) throws ClassNotFoundException {

		final List<Class<?>> result = new ArrayList<>();
		if (!directory.exists()) {
			return result;
		}
		final File[] files = directory.listFiles();
		for (final File file : files) {
			final String fileName = file.getName();
			if (file.isDirectory()) {
				assert!fileName.contains(PACKAGE_DELIMITER);
				result.addAll(findClassesInDirectory(file, packageName + PACKAGE_DELIMITER + fileName));
			} else if (fileName.endsWith(CLASS_EXTENSION) && !fileName.contains(INTERNALclazz_NAMESPACE_DELIMITER)) {
				final Class<?> clazz = loadClass(getFullQualifiedClassname(packageName, fileName));
				if (clazz != null) {
					result.add(clazz);
				}
			}
		}
		return result;
	}


	private Class<?> loadClass(final String fullQualifiedClassname) throws ClassNotFoundException {
		try {
			try {
				return Class.forName(fullQualifiedClassname, true, classLoader);
			} catch (final ExceptionInInitializerError e) {
				// happen, for example, in classes, which depend on
				// Spring to inject some beans, and which fail,
				// if dependency is not fulfilled
				return Class.forName(fullQualifiedClassname, false, classLoader); // Thread.currentThread().getContextClassLoader()
			}
		} catch (final NoClassDefFoundError ex) {
			if (this.ignoreNoClassDefFoundError) {
				return null;
			}
			throw ex;
		}
	}


	private String getFullQualifiedClassname(final String packageName, final String fileName) {
		return packageName + PACKAGE_DELIMITER + fileName.substring(0, fileName.length() - CLASS_EXTENSION.length());
	}

}
