plugins {
    id("com.hayden.no-main-class")
    id("com.hayden.kotlin")
    id("com.hayden.observable-app")
    id("com.hayden.docker-compose")
	id("com.hayden.jdbc-persistence")
    id("com.hayden.aop")
    id("com.hayden.templating")
    id("com.hayden.spring-app")
    id("com.hayden.web-app")
    id("net.bytebuddy.byte-buddy-gradle-plugin") version "1.14.17"
}

group = "com.hayden"
version = "1.0.0"

dependencies {
    implementation(project(":jdbc-persistence"))
}