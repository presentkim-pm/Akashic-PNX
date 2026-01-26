import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.*

plugins {
    kotlin("jvm") version "2.0.0"
    id("com.gradleup.shadow") version "8.3.0"
}

val pluginName = "Akashic"
group = "kim.present.pnx.akashic"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://repo.opencollab.dev/maven-releases/")
    maven("https://repo.opencollab.dev/maven-snapshots/")
    maven("https://repo.powernukkitx.org/releases")
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("org.powernukkitx:server:2.0.0-SNAPSHOT")
    compileOnly(kotlin("stdlib"))

    implementation("org.jetbrains.exposed:exposed-core:0.61.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.61.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.61.0")
    implementation("com.zaxxer:HikariCP:6.0.0")
    implementation("org.mariadb.jdbc:mariadb-java-client:3.4.1")
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_21
    }
}

tasks.jar {
    enabled = true
    archiveClassifier.set("dev")
}

tasks.withType<ShadowJar> {
    archiveClassifier.set("")
}

val DEFAULT_SERVER_PATH = "../../"

tasks.register<Copy>("deploy") {
    dependsOn(tasks.shadowJar)

    val localProps = Properties()
    val propsFile = file("local.properties")
    val serverPath: String = if (propsFile.exists()) {
        localProps.load(propsFile.inputStream())
        localProps.getProperty("serverPath", DEFAULT_SERVER_PATH)
    } else DEFAULT_SERVER_PATH

    from(tasks.shadowJar.get().archiveFile)
    into(file("$serverPath/plugins"))
    doLast { println("✅ [$pluginName] Deploy completed -> $serverPath/plugins") }
}