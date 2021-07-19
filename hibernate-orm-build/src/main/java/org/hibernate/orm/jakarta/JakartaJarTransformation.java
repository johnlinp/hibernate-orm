/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.jakarta;

import javax.inject.Inject;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import static org.hibernate.orm.jakarta.JakartaPlugin.JAKARTA;

/**
 * @author Steve Ebersole
 */
@CacheableTask
public abstract class JakartaJarTransformation extends DefaultTask {
	private final RegularFileProperty sourceJar;
	private final RegularFileProperty targetJar;

	@Inject
	public JakartaJarTransformation(ObjectFactory objectFactory) {
		sourceJar = objectFactory.fileProperty();
		targetJar = objectFactory.fileProperty();

		setGroup( JAKARTA );
	}

	@InputFile
	public RegularFileProperty getSourceJar() {
		return sourceJar;
	}

	@OutputFile
	public RegularFileProperty getTargetJar() {
		return targetJar;
	}

	@TaskAction
	void transform() {
		getProject().javaexec(
				(javaExecSpec) -> {
					javaExecSpec.classpath( getProject().getConfigurations().getByName( "jakartaeeTransformTool" ) );
					javaExecSpec.setMain( "org.eclipse.transformer.jakarta.JakartaTransformer" );
					javaExecSpec.args(
							sourceJar.get().getAsFile().getAbsolutePath(),
							targetJar.get().getAsFile().getAbsolutePath(),
							"-q",
							"-tr", getProject().getRootProject().file( "rules/jakarta-renames.properties" ).getAbsolutePath(),
							"-tv", getProject().getRootProject().file( "rules/jakarta-versions.properties" ).getAbsolutePath(),
							"-td", getProject().getRootProject().file( "rules/jakarta-direct.properties" ).getAbsolutePath()
					);
				}
		);
	}
}
