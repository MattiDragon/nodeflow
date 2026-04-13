plugins {
    alias(libs.plugins.fabric.loom)
    alias(libs.plugins.publish.mod)
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
    maven("https://maven.nucleoid.xyz")
    mavenCentral()
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
        // Avoid pulling in unnecessary deps (why are these api?)
        exclude(group = "net.fabricmc.fabric-api")
        exclude(group = "dev.isxander", module = "yet-another-config-lib")
        exclude(group = "net.caffeinemc", module = "sodium-fabric")
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

publishMods {
    val mcVersion = libs.versions.minecraft.get()
    val modVersion = properties["mod_version"] as String

    file = tasks.jar.get().archiveFile
    additionalFiles.from(tasks["sourcesJar"])

    displayName = "v$modVersion [$mcVersion]"
    changelog = providers.fileContents(layout.projectDirectory.file("changelog/$modVersion+$mcVersion.md")).asText

    type.set(providers.environmentVariable("RELEASE_TYPE").map { me.modmuss50.mpp.ReleaseType.of(it) })
    modLoaders.addAll("fabric")

    dryRun = providers.gradleProperty("publish_dry_run").isPresent

    modrinth {
        projectId = "ktKs9gT1"
        accessToken.set(providers.environmentVariable("MODRINTH_TOKEN"))

        requires("fabric-api")

        minecraftVersions.add(providers.environmentVariable("MODRINTH_MC_VERSION").filter { it.isNotBlank() }.orElse(mcVersion))
    }

    github {
        repository = "MattiDragon/nodeflow"
        accessToken.set(providers.environmentVariable("GITHUB_TOKEN"))

        commitish.set(providers.environmentVariable("GITHUB_BRANCH"))
        tagName.set(version.map { it.replace('+', '-') })
    }
}

publishing {
    publications.register<MavenPublication>("mavenJava") {
        from(components["java"])
    }
}