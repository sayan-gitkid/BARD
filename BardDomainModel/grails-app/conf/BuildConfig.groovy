/* Copyright (c) 2014, The Broad Institute
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of The Broad Institute nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL The Broad Institute BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import grails.util.Environment
def useBroadRepo = System.getProperty("useBroadRepo") != "false"
grails.project.work.dir = "target"
grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.target.level = 1.6

grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // uncomment to disable ehcache
        // excludes 'ehcache'
    }
    log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'

    repositories {
        inherit(false) // don't repositories from plugins
       
       mavenRepo "https://repo.grails.org/grails/plugins" // for grails 2.2.1 and older
       // grailsPlugins() //uncomment this line, then comment out the line just above, if you decide to upgrade grails
        grailsHome()

        if (useBroadRepo) {
            mavenRepo "http://bard-repo.broadinstitute.org:8081/artifactory/bard-virtual-repo"
            grailsRepo("http://bard-repo.broadinstitute.org:8081/artifactory/bard-virtual-repo", "grailsCentral")
        } else {
            grailsCentral()
            mavenLocal()
            mavenCentral()
        }
    }

    dependencies {
        build 'com.oracle:ojdbc6:11.2.0.2.0'
        test "org.spockframework:spock-grails-support:0.7-groovy-2.0"
        compile 'org.apache.commons:commons-lang3:3.1'
        compile 'org.apache.commons:commons-math3:3.1'
    }

    plugins {
        build(":tomcat:$grailsVersion") { export = false }
        build(":codenarc:0.18.1") { export = false }
        // seems like rest client builder is required by release plugin but not getting included transitively
        // so adding explicitly here
        build(":rest-client-builder:1.0.2") { export = false }
        build(":release:2.2.1") { export = false }
        build(":improx:0.2") { export = false } // Interactive Mode Proxy; useful for IDE integration
        compile(":clover:3.1.10.1") { export = false }
        compile(":console:1.2") { export = false }
        compile(":spring-security-acl:1.1.1")
        compile(":database-migration:1.3.2") { export = true }
        compile(":spock:0.7") {
            export = false
            exclude("spock-grails-support")
        }
        compile ":spring-security-acl:1.1.1"
        /**
         * including build test data for all environments except production, oracleqa, oracledev
         */
        switch (Environment.current.name) {
            case ('production'):
            case ('oracleqa'):
            case ('oracledev'):
                break

            default:
                compile(":build-test-data:2.0.4") { export = true }
                compile(":fixtures:1.2") {
                    export = true
                    exclude('svn')
                }
                break
        }
    }
}

grails.project.repos.default = "releases"

grails.project.repos.releases.url = "http://bard-repo.broadinstitute.org:8081/artifactory/plugins-release-local"

// create a ~/.grails/settings.groovy file with these
// login to bard-repo:8443, click on your profile and get the unescaped encrypted password from artifactory
// use that encrypted password in the settings.groovy file
//
// grails.project.repos.releases.username = "changeme"
// grails.project.repos.releases.password = "changeme"

grails.project.repos.snapshots.url = "http://bard-repo.broadinstitute.org:8081/artifactory/plugins-snapshot-local"
grails.project.repos.snapshots.username = "changeme"
grails.project.repos.snapshots.password = "changeme"

codenarc.ruleSetFiles = "file:grails-app/conf/BardCodeNarcRuleSet.groovy"
codenarc.reports = {
    html('html') {
        outputFile = 'target/codenarc-reports/html/BARD-CodeNarc-Report.html'
        title = 'BARD CodeNarc Report'
    }
}
codenarc {
    exclusions = ['**/grails-app/migrations/*']
}

clover {
    license.path = "${userHome}/.grails/clover.license"
    directories: ['src/java', 'src/groovy', 'grails-app']
    includes = ['**/*.groovy', '**/*.java']
    excludes = ['test/**.*', '**/*Spec*.*', '**/conf/**', '**/migrations/**']
}
