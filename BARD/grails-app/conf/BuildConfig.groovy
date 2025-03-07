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

def useBroadRepo = System.getProperty("useBroadRepo") != "false"

grails.servlet.version = "2.5" // Change depending on target container compliance (2.5 or 3.0)
grails.project.work.dir = "target"
grails.project.target.level = 1.6
grails.project.source.level = 1.6
grails.server.port.http = 8081
//grails.project.war.file = "target/${appName}-${appVersion}.war"

def gebVersion = "0.9.0-RC-1"
def seleniumVersion = "2.31.0"

grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // uncomment to disable ehcache
        // excludes 'ehcache'
    }
    log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    checksums true // Whether to verify checksums on resolve

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
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.
        // build scope

        // compile scope

        compile "net.logstash.log4j:jsonevent-layout:1.5"
        compile "org.grails:grails-webflow:$grailsVersion"
        compile "org.apache.httpcomponents:httpclient:4.2.3"

        compile 'org.apache.commons:commons-lang3:3.1'

        ["MarvinBeans-concurrent",
                "MarvinBeans-diverse-modules",
                "MarvinBeans-formats-peptide",
                "MarvinBeans-formats-smiles",
                "MarvinBeans-formats.cml",
                "MarvinBeans-formats.image",
                "MarvinBeans-formats",
                "MarvinBeans-license",
                "MarvinBeans-plugin",
                "MarvinBeans"].each {
            compile "ChemAxon:${it}:5.10.4"
        }

        compile 'jfree:jfreechart:1.0.13'
        compile('org.apache.httpcomponents:httpclient:4.1.2') {
            excludes "commons-codec", "commons-logging"
        }

        compile 'com.thoughtworks.xstream:xstream:1.4.2'
        compile("org.codehaus.groovy.modules.remote:remote-transport-http:0.5") {
            excludes "servlet-api"
        }
        compile 'log4j:apache-log4j-extras:1.2.17'

        compile "bard:external-validation-api:20140106"
        if (useBroadRepo) {
            // this largely because this lib is only
            // used to run adhoc scripts run at the Broad.
            compile "bard:pubchem-xml:20131010"
            compile('cbip:cbip_encoding:0.1') {
                excludes "junit"
            }
        }

        compile "com.oracle:ojdbc6:11.2.0.2.0"

        compile 'org.mapdb:mapdb:0.9.11'


        // needed for SMTPAppender (included as a compile dependency because the right one is being picked up for runtime, but not for build time)
        compile "log4j:log4j:1.2.16"
        build "log4j:log4j:1.2.16"

        // runtime scope
        // runtime 'mysql:mysql-connector-java:5.1.16'
        runtime('org.codehaus.groovy.modules.http-builder:http-builder:0.5.2') {
            excludes "commons-logging", "xml-apis", "groovy", "httpclient", "httpcore", "nekohtml"
        }
        runtime 'com.github.groovy-wslite:groovy-wslite:0.7.0'
        runtime 'commons-net:commons-net:3.2'
        runtime 'net.sf.opencsv:opencsv:2.3'
        runtime 'com.fasterxml.jackson.core:jackson-annotations:2.1.2'
        runtime 'com.fasterxml.jackson.core:jackson-core:2.1.2'
        runtime 'com.fasterxml.jackson.core:jackson-databind:2.1.2'
        runtime 'com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.1.2'
        runtime 'com.fasterxml.jackson.module:jackson-module-jaxb-annotations:2.1.2'

        // this dependency seems a little ridiculous but some page renders seem to fail and adding this
        // was advised in http://stackoverflow.com/questions/12627147/grails-rendering-plugin-gives-`java-lang-classnotfoundexception-when-deployed
        runtime 'org.springframework:spring-test:3.1.2.RELEASE'

        // test scope
        test("org.spockframework:spock-grails-support:0.7-groovy-2.0")
        test("org.objenesis:objenesis:1.3") // used by spock for Mocking object that lack no args constructor
        test("org.gebish:geb-spock:$gebVersion")
        test("org.seleniumhq.selenium:selenium-htmlunit-driver:$seleniumVersion") {
            excludes "xml-apis", "commons-io"
        }
        test("org.seleniumhq.selenium:selenium-chrome-driver:$seleniumVersion") {
            exclude "xml-apis"
        }
        test("org.seleniumhq.selenium:selenium-firefox-driver:$seleniumVersion") {
            excludes "xml-apis", "commons-io"
        }
        test("org.seleniumhq.selenium:selenium-remote-driver:$seleniumVersion") {
            excludes "xml-apis"
        }

        // provided scope
        provided('net.sourceforge.nekohtml:nekohtml:1.9.15') {
            exclude "xml-api"
        }
        provided 'org.apache.httpcomponents:httpcomponents-core:4.1.3'
    }


    plugins {
        // build scope
        build(":codenarc:0.18.1") {
            excludes "groovy-all"
        }

        build ":improx:0.2" // Interactive Mode Proxy; useful for IDE integration
        build ":tomcat:$grailsVersion"

        // compile scope
        compile ":hibernate:$grailsVersion"
        compile ":jquery-ui:1.8.15"
        compile ":export:1.5"
        compile ":resources:1.2.RC2"
        compile ":twitter-bootstrap:2.3.0"

        compile ":clover:3.1.10.1"
        compile ":spring-mobile:0.5.1"
        compile ":console:1.2"
        compile ":jquery-validation-ui:1.4.2"
        compile ":twitter-bootstrap:2.2.2"
        compile(':webflow:2.0.0') {
            exclude 'grails-webflow'
        }
        compile ":spring-security-acl:1.1.1"
        compile ":remote-control:1.4"
        compile ":google-analytics:2.0"
        compile ":mail:1.0.1"
        compile ":greenmail:1.3.3"
        compile ":cache:1.0.1"
        // compile ':events-push:1.0.M7'
        compile ":famfamfam:1.0.1"
        //compile ":spring-security-ui:0.2"

        compile ":jesque:0.6.2"
        compile ":jesque-web:0.4.0"

        compile ":spring-security-core:1.2.7.3"
        // runtime scope
        runtime ":jquery:1.7.1"

        // test scope
        test(":spock:0.7") {
            exclude "spock-grails-support"
        }
        test "org.grails.plugins:geb:$gebVersion"
        test ":remote-control:1.4"
    }
}

// making the domain plugin an in-place plugin
grails.plugin.location.'bard-domain-model' = "../BardDomainModel"
grails.plugin.location.'shopping-cart:0.8.2' = "../shopping-cart-0.8.2"
grails.plugin.location.'bard-rest-api-wrapper' = "../bard-rest-api-wrapper"
grails.plugin.location.'functional-spock' = "../functional-spock"
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
    //initstring = "bardwebclover.db"
    directories: ['src/java', 'src/groovy', 'grails-app']
    includes = ['**/*.groovy', '**/*.java']
    excludes = ['**/bardwebquery/**.*', '**/*Spec*.*', '**/mockServices/**.*', '**/conf/**', '**/GridController.*', '**/mockServices/**.*']
}
// used for tomcat when running grails run-war
grails.tomcat.jvmArgs = ["-server", "-XX:MaxPermSize=256m", "-Xmx768m"]
