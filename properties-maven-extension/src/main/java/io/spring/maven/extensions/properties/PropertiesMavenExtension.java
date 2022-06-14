package io.spring.maven.extensions.properties;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Model;
import org.apache.maven.project.MavenProject;

import org.codehaus.plexus.component.annotations.Component;

/**
 * Maven Extension to process externalized configuration declared in Java {@link Properties}
 * applied to a {@link MavenProject}.
 *
 * @author John Blum
 * @see java.util.Properties
 * @see org.apache.maven.AbstractMavenLifecycleParticipant
 * @see org.apache.maven.execution.MavenSession
 * @see org.apache.maven.project.MavenProject
 * @see org.codehaus.plexus.component.annotations.Component
 * @since 1.0.0
 */
@SuppressWarnings("unused")
@Component(role = AbstractMavenLifecycleParticipant.class, hint = "external-properties-configuration")
public class PropertiesMavenExtension extends AbstractMavenLifecycleParticipant {

	protected static final String SPRING_DATA_VERSION_PROPERTIES_FILENAME = "spring-data-version.properties";

	protected String getPropertiesFilename() {
		return SPRING_DATA_VERSION_PROPERTIES_FILENAME;
	}

	@Override
	public void afterProjectsRead(MavenSession session) throws MavenExecutionException {

		super.afterProjectsRead(session);

		MavenProject mavenProject = session.getCurrentProject();
		Model model = mavenProject.getModel();

		File propertiesFile = new File(mavenProject.getBasedir(), getPropertiesFilename());

		if (isFile(propertiesFile)) {

			Properties projectProperties = model.getProperties();
			Properties externalProperties = load(session, propertiesFile);

			projectProperties.putAll(externalProperties);
		}
		else {
			log(session, "Properties file [%s] could not be found", propertiesFile);
		}
	}

	private boolean isFile(File path) {
		return path != null && path.isFile();
	}

	private Properties load(MavenSession session, File propertiesFile) {

		Properties externalProperties = new Properties();

		try (FileInputStream inputStream = new FileInputStream(propertiesFile)){
			externalProperties.load(inputStream);
		}
		catch (IOException ignore) {
			log(session, "Failed to load properties from [%s]", propertiesFile);
		}

		return externalProperties;
	}

	private void log(MavenSession session, String message, Object... args) {
		logToSystemErr(message, args);
	}

	private void logToSystemErr(String message, Object... args) {

		System.err.printf(message, args);
		System.err.flush();
	}
}
