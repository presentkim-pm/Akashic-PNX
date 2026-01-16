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
    maven("https://repo.maven.apache.org/maven2/")
    maven("https://jitpack.io")
    maven("https://repo.opencollab.dev/maven-releases/")
    maven("https://repo.opencollab.dev/maven-snapshots/")
    maven("https://repo.powernukkitx.org/releases")
}

dependencies {
    compileOnly("org.powernukkitx:server:2.0.0-SNAPSHOT")
    implementation(kotlin("stdlib"))
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_21
    }
}

tasks.withType<ShadowJar> {
    archiveClassifier.set("")
    minimize()

    relocate("kotlin", "$group.libs.kotlin")
    relocate("org.jetbrains", "$group.libs.jetbrains")
}

val DEFAULT_SERVER_PATH = "../../";

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