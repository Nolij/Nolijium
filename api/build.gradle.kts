plugins {
	id("com.github.gmazzo.buildconfig")
}

operator fun String.invoke(): String = rootProject.properties[this] as? String ?: error("Property $this not found")

buildConfig {
	className("NolijiumConstants")
	packageName("dev.nolij.nolijium.impl")

	useJavaOutput()

	buildConfigField("MOD_ID", "mod_id"())
}

dependencies {
	compileOnly(project(":stubs"))
	
	compileOnly("org.apache.logging.log4j:log4j-core:${"log4j_version"()}")

	testImplementation("org.junit.jupiter:junit-jupiter:${"junit_version"()}")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
	useJUnitPlatform()
}

tasks.assemble {
	dependsOn(tasks.test)
}

tasks.clean {
	finalizedBy(tasks.generateBuildConfig)
}