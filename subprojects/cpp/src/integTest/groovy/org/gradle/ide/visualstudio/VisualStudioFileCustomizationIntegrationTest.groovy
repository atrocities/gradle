/*
 * Copyright 2013 the original author or authors.
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


package org.gradle.ide.visualstudio

import org.gradle.ide.visualstudio.fixtures.FiltersFile
import org.gradle.ide.visualstudio.fixtures.ProjectFile
import org.gradle.ide.visualstudio.fixtures.SolutionFile
import org.gradle.nativebinaries.language.cpp.fixtures.AbstractInstalledToolChainIntegrationSpec
import org.gradle.nativebinaries.language.cpp.fixtures.app.CppHelloWorldApp

class VisualStudioFileCustomizationIntegrationTest extends AbstractInstalledToolChainIntegrationSpec {

    def app = new CppHelloWorldApp()

    def setup() {
        app.writeSources(file("src/main"))
        buildFile << """
    apply plugin: 'cpp'
    apply plugin: 'visual-studio'

    model {
        platforms {
            create("win32") {
                architecture "i386"
            }
        }
        buildTypes {
            create("debug")
            create("release")
        }
    }
    executables {
        main {}
    }
"""
    }

    def "can specific location of generated project files"() {
        when:
        buildFile << """
    model {
        visualStudio {
            projects.all {
                projectFile.location = "other/project.vcxproj"
                filtersFile.location = "other/filters.vcxproj.filters"
            }
            solutions.all {
                solutionFile.location = "vs/main.solution"
            }
        }
    }
"""
        and:
        run "mainVisualStudio"

        then:
        executedAndNotSkipped ":mainExeVisualStudio"

        and:
        final projectFile = projectFile("other/project.vcxproj")
        filtersFile("other/filters.vcxproj.filters")

        final mainSolution = solutionFile("vs/main.solution")
        mainSolution.assertHasProjects("mainExe")
        mainSolution.assertReferencesProject("mainExe", projectFile)
    }

    private SolutionFile solutionFile(String path) {
        return new SolutionFile(file(path))
    }

    private ProjectFile projectFile(String path) {
        return new ProjectFile(file(path))
    }

    private FiltersFile filtersFile(String path) {
        return new FiltersFile(file(path))
    }
}