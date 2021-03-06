/******************************************************************************* 
 * Copyright (c) 2016 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 

package org.fusesource.ide.projecttemplates.adopters.configurators;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IProjectConfigurationManager;
import org.eclipse.m2e.core.project.ResolverConfiguration;
import org.fusesource.ide.projecttemplates.internal.ProjectTemplatesActivator;
import org.fusesource.ide.projecttemplates.util.NewProjectMetaData;

/**
 * this configurator provides helper methods for maven configuration
 * 
 * @author lhein
 */
public class MavenTemplateConfigurator extends DefaultTemplateConfigurator {
	
	/*
	 * (non-Javadoc)
	 * @see org.fusesource.ide.projecttemplates.adopters.configurators.DefaultTemplateConfigurator#configure(org.eclipse.core.resources.IProject, org.fusesource.ide.projecttemplates.util.NewProjectMetaData, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public boolean configure(IProject project, NewProjectMetaData metadata, IProgressMonitor monitor) {
		boolean ok = super.configure(project, metadata, monitor);

		if (ok) {
			// by default add the maven nature
			ok = configureMavenNature(project, monitor);
		}
		
		if (ok) {
			// by default configure the version of camel used in the pom.xml
			ok = configurePomCamelVersion(project, metadata, monitor);
		}
		
		return ok;
	}
	
	/**
	 * configures the maven nature for the given project
	 * 
	 * @param project	the project to enable maven nature
	 * @param monitor	the progress monitor
	 * @return	true on success
	 */
	protected boolean configureMavenNature(IProject project, IProgressMonitor monitor) {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		try {
			monitor.beginTask("Configuring Maven Nature...", IProgressMonitor.UNKNOWN);
			ResolverConfiguration configuration = new ResolverConfiguration();
			configuration.setResolveWorkspaceProjects(true);
			configuration.setSelectedProfiles(""); //$NON-NLS-1$
			IProjectConfigurationManager configurationManager = MavenPlugin.getProjectConfigurationManager();
			configurationManager.enableMavenNature(project, configuration, monitor);
			configurationManager.updateProjectConfiguration(project, monitor);
        } catch(CoreException ex) {
        	ProjectTemplatesActivator.pluginLog().logError(ex.getMessage(), ex);
        	return false;
        } finally {
        	monitor.done();
        }
		return true;
	}
	
	/**
	 * changes all occurances of Camel version in the pom.xml file with the
	 * version defined in the wizard
	 * 
	 * @param project			the project
	 * @param projectMetaData	the metadata containing the new version
	 * @param monitor			the progress monitor
	 * @return	true on success, otherwise false
	 */
	protected boolean configurePomCamelVersion(IProject project, NewProjectMetaData projectMetaData, IProgressMonitor monitor) {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		try {
			monitor.beginTask("Adapting the project to the new Camel version...", IProgressMonitor.UNKNOWN);
			File pomFile = new File(project.getFile("pom.xml").getLocation().toOSString());
			Model m2m = MavenPlugin.getMaven().readModel(pomFile);

			final String camelVersion = projectMetaData.getCamelVersion();
			if (m2m.getDependencyManagement() != null) {
				updateCamelVersionDependencies(m2m.getDependencyManagement().getDependencies(), camelVersion);
			}
			updateCamelVersionDependencies(m2m.getDependencies(), camelVersion);
			if (m2m.getBuild().getPluginManagement() != null) {
				updateCamelVersionPlugins(m2m.getBuild().getPluginManagement().getPlugins(), camelVersion);
			}
			updateCamelVersionPlugins(m2m.getBuild().getPlugins(), camelVersion);

			OutputStream os = new BufferedOutputStream(new FileOutputStream(pomFile));
		    MavenPlugin.getMaven().writeModel(m2m, os);
			IFile pomIFile2 = project.getProject().getFile("pom.xml");
			if (pomIFile2 != null) {
				pomIFile2.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
		    }
			os.close();
		} catch (Exception ex) {
			ProjectTemplatesActivator.pluginLog().logError(ex);
			return false;
		} finally {
			monitor.done();
		}
		return true;
	}

	/**
	 * @param plugins
	 * @param camelVersion
	 */
	private void updateCamelVersionPlugins(List<Plugin> plugins, String camelVersion) {
		for (Plugin p : plugins) {
			if ("org.apache.camel".equalsIgnoreCase(p.getGroupId()) && p.getArtifactId().startsWith("camel-")) {
				p.setVersion(camelVersion);
			}
		}
	}

	/**
	 * @param dependencies
	 * @param camelVersion
	 */
	private void updateCamelVersionDependencies(List<Dependency> dependencies, String camelVersion) {
		for (Dependency dep : dependencies) {
			if ("org.apache.camel".equalsIgnoreCase(dep.getGroupId()) && dep.getArtifactId().startsWith("camel-")) {
				dep.setVersion(camelVersion);
			}
		}

	}
}
