plugins {
    alias(libs.plugins.fabric.loom)
    `maven-publish`
}

val mod_version: String by project
val maven_group: String by project
val archives_base_name: String by project

version = "$mod_version+mc.${libs.versions.minecraft.get()}"
group = maven_group
base.archivesName = archives_base_name

repositories {
    maven("https://maven.terraformersmc.com")
    maven("https://maven.quiltmc.org/repository/release")
    maven("https://api.modrinth.com/maven")
    maven("https://maven.isxander.dev/releases")
    maven("https://maven.isxander.dev/snapshots")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://jitpack.io") {
        content {
            includeGroupAndSubgroups("com.github")
        }
    }
}

loom.splitEnvironmentSourceSets()

dependencies {
    minecraft(libs.minecraft)
    implementation(libs.fabric.loader)

    compileOnly(libs.controlify) {
        exclude(group = "net.fabricmc.fabric-api")
        exclude(group = "maven.modrinth", module = "sodium")
        exclude(group = "maven.modrinth", module = "iris")
    }
    implementation(libs.fabric.api)
}

loom {
    runs {
        named("server") {
            runDir("serverRun")
        }
    }
}

tasks.processResources {
    inputs.property("version", project.version)
    filteringCharset = "UTF-8"

    filesMatching("fabric.mod.json") {
        expand("version" to project.version)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

java {
    withSourcesJar()
}

tasks.jar {
    from("LICENSE") {
        rename { "${it}_${project.base.archivesName}" }
    }
}

publishing {
    publications.register<MavenPublication>("mavenJava") {
        from(components["java"])
    }
}