/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.vertx.gradle.jruby.plugins

import org.gradle.api.Project
import org.gradle.api.Plugin
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.SourceSet
import org.vertx.gradle.AbstractProjectPlugin
import org.vertx.gradle.jruby.tasks.YarDoc


/**
 * @author pidster
 *
 */
class JRubyPlugin extends AbstractProjectPlugin {
	
	public static final String CONFIGURATION = 'jruby'
	
	public static final String SOURCE_DIR = 'ruby'
	
	public static final String DOCUMENTATION_TASK = 'yardoc'
	
	public JRubyPlugin() {
		super(CONFIGURATION)
	}

	protected void configureCompiler(Project project, Configuration configuration) {
		project.configure(project) {
			sourceSets {
				main {
					resources {
						srcDirs "src/main/$SOURCE_DIR"
					}
				}
				test {
					resources {
						srcDirs "src/test/$SOURCE_DIR"
					}
				}
			}
		}
	}
	
	protected void configureResources(Project project, Configuration configuration) {
//		project.convention.getPlugin(JavaPluginConvention).sourceSets.all { SourceSet sourceSet ->
//			sourceSet.resources.srcDirs << project.file("src/$sourceSet.name/$SOURCE_DIR")
//			sourceSet.resources.srcDirs.add project.file("src/$sourceSet.name/$SOURCE_DIR")
//		}
	}
	
	protected void configureDocs(Project project, Configuration configuration) {
		YarDoc doc = project.tasks.add(DOCUMENTATION_TASK, YarDoc)
		Task build = project.tasks.findByName('build')
		build.dependsOn doc
	}

}