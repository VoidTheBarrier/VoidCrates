@file:Suppress("UnstableApiUsage")

plugins {
    java
    idea
    id("net.fabricmc.fabric-loom") version "1.17.12"
    id("org.jetbrains.kotlin.jvm").version("2.4.0")
    `maven-publish`
}

val modId = project.properties["mod_id"].toString()
version = project.properties["mod_version"].toString()
group = project.properties["mod_group"].toString()

val modName = project.properties["mod_name"].toString()
base.archivesName.set(modName)

val minecraftVersion = project.properties["minecraft_version"].toString()

// Cobblemon has no Minecraft 26.x release yet, so anything that references its API
// (directly or transitively, e.g. CobbleDollars) cannot compile against real classes
// right now. Excluded here rather than deleted so they're easy to re-enable later.
sourceSets {
    main {
        kotlin {
            exclude("dev/voidcrates/data/rewards/types/PokemonReward.kt")
            exclude("dev/voidcrates/economy/services/CobbleDollarsEconomyService.kt")
            exclude("dev/voidcrates/integrations/FlanIntegration.kt")
        }
    }
}

loom {
    mixin.useLegacyMixinAp.set(false)
    interfaceInjection.enableDependencyInterfaceInjection.set(true)
    splitEnvironmentSourceSets()
    mods {
        create(modId) {
            sourceSet(sourceSets.main.get())
        }
    }
    if (file("src/main/resources/$modId.accesswidener").exists()) {
        accessWidenerPath.set(file("src/main/resources/$modId.accesswidener"))
    }
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven {
        name = "Modrinth"
        url = uri("https://api.modrinth.com/maven")
        content {
            includeGroup("maven.modrinth")
        }
    }
    maven("https://maven.tomalbrc.de")
    maven("https://maven.nucleoid.xyz/") { name = "Nucleoid" }
    maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/") {
        name = "sonatype-oss-snapshots1"
        mavenContent { snapshotsOnly() }
    }
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://maven.impactdev.net/repository/development/")
    maven("https://repo.lucko.me")
    maven(url = "https://maven.blazing-coop.net/releases") { name = "Flemmli97" }
    maven("https://maven.pokeskies.com/releases/")
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraftVersion")
    // 26.x: game ships unobfuscated — no mappings line needed.

    implementation("net.fabricmc:fabric-loader:${project.properties["loader_version"].toString()}")
    implementation("net.fabricmc.fabric-api:fabric-api:${project.properties["fabric_version"].toString()}")
    implementation("net.fabricmc:fabric-language-kotlin:${project.properties["fabric_kotlin_version"].toString()}")

    // Coroutines
    implementation(include("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")!!)
    implementation(include("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.10.2")!!)

    implementation(include("com.github.ben-manes.caffeine:caffeine:3.2.0")!!)

    // Adventure Text – 6.9.0 for 26.x
    // NOTE: modImplementation does not exist in a non-obfuscated Loom environment (confirmed
    // via Loom 1.17.12 source: LoomGradleExtensionApiImpl throws UnsupportedOperationException
    // for remap-configuration methods when notObfuscated() is true - the mod* configuration
    // family only ever existed to trigger intermediary<->named remapping, which 26.x has no
    // use for). Plain implementation() is the correct current pattern.
    //
    // adventure-platform-fabric itself is intentionally NOT a dependency here. Its published POM
    // has no meaningful <dependencies> section (the real graph lives only in Gradle Module
    // Metadata), and the specific class VoidCrates needed from it - MinecraftServerAudiences -
    // actually lives in a second module, adventure-platform-mod-shared, which was never published
    // for unobfuscated Minecraft (last release 6.8.0, built against 1.21.11's intermediary names).
    // Rather than depend on a fragile/absent artifact, VoidCratesAudiences.kt reimplements the
    // three operations that were actually used, built entirely on the modules below plus vanilla's
    // own ComponentSerialization.CODEC - see that file for the full explanation.
    sequenceOf(
        "net.kyori:adventure-key:4.26.1",
        "net.kyori:adventure-api:4.26.1",
        "net.kyori:adventure-text-minimessage:4.26.1",
        "net.kyori:adventure-text-serializer-plain:4.26.1",
        "net.kyori:adventure-text-serializer-gson:4.26.1",
        "net.kyori:examination-api:1.3.0",
        "net.kyori:examination-string:1.3.0"
    ).forEach { dep ->
        implementation(include(dep)!!)
    }

    // Permissions API
    implementation("me.lucko:fabric-permissions-api:0.7.0")?.let {
        include(it)
    }

    // GUI libraries (2.x for 26.x)
    implementation("eu.pb4:sgui:2.1.0+26.2")?.let {
        include(it)
    }

    // Events - bumped from stale 0.5.4+1.21.11 to the real 26.2-matched release.
    // implementation (not modImplementation): the mod* configuration family doesn't exist
    // in a non-obfuscated Loom environment - see the Adventure section above for the full
    // explanation, confirmed via Loom 1.17.12 source.
    implementation("xyz.nucleoid:stimuli:0.6.2+26.2")?.let {
        include(it)
    }

    implementation("com.github.eduardomcb:discord-webhook:1.0.1")?.let {
        include(it)
    }

    // Placeholder mods
    compileOnly("io.github.miniplaceholders:miniplaceholders-api:2.2.3")
    compileOnly("io.github.miniplaceholders:miniplaceholders-kotlin-ext:2.2.3")
    implementation("eu.pb4:placeholder-api:3.1.0-beta.1+26.2")

    // Impactor economy – compileOnly; no 26.x release yet
    compileOnly("net.impactdev.impactor:common:5.3.0+1.21.1-SNAPSHOT")
    compileOnly("net.impactdev.impactor.api:economy:5.3.0-SNAPSHOT")
    compileOnly("net.impactdev.impactor.api:text:5.3.0-SNAPSHOT")

    // Database Storage
    implementation(include("org.mongodb:mongodb-driver-sync:4.11.0")!!)
    implementation(include("org.mongodb:mongodb-driver-core:4.11.0")!!)
    implementation(include("org.mongodb:bson:4.11.0")!!)

    // SQL Storage
    implementation(include("org.mariadb.jdbc:mariadb-java-client:3.1.0")!!)
    implementation(include("com.zaxxer:HikariCP:5.1.0")!!)
    implementation(include("org.xerial:sqlite-jdbc:3.43.2.2")!!)
    implementation(include("com.h2database:h2:2.2.224")!!)
    implementation(include("com.mysql:mysql-connector-j:8.2.0")!!)

    // Cobblemon – compileOnly; no 26.x release yet
    compileOnly("com.cobblemon:fabric:1.7.3+1.21.1-SNAPSHOT")

    // Flan compileOnly disabled: could not confirm the exact Maven coordinate for a
    // Minecraft 26.x-compatible build. Re-enable alongside FlanIntegration.kt once verified.
    // compileOnly("io.github.flemmli97:flan:VERSION-HERE:api") {
    //     isTransitive = false
    // }

    compileOnly(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
}

tasks.processResources {
    inputs.property("mod_version", version)

    filesMatching("fabric.mod.json") {
        expand("id" to modId, "version" to version, "name" to modName)
    }

    filesMatching("**/lang/*.json") {
        expand("id" to modId, "version" to version, "name" to modName)
    }
}

publishing {
    publications.create<MavenPublication>("maven") {
        artifactId = base.archivesName.get()
        from(components["java"])
    }
    repositories {
        mavenLocal()
    }
}

// 26.x: no remapping, so use the standard jar task instead of remapJar
tasks.jar {
    archiveFileName.set("${project.name}-fabric-$minecraftVersion-${project.version}.jar")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(25)
}

java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
    withSourcesJar()
}

tasks.withType<AbstractArchiveTask> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from("LICENSE") {
        rename { "${it}_${modId}" }
    }
}
