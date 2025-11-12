plugins {
   kotlin("jvm") version "2.2.20"
   kotlin("plugin.serialization") version "1.9.0" // Add this line
}

group = "com.basebeta"
version = "1.0-SNAPSHOT"

repositories {
   mavenCentral()
}

dependencies {
   testImplementation(kotlin("test"))

   val ktorVersion = "2.3.7"

   implementation("io.ktor:ktor-client-core:${ktorVersion}")
   implementation("io.ktor:ktor-serialization-kotlinx-json:${ktorVersion}")
   implementation("io.ktor:ktor-client-content-negotiation:${ktorVersion}")
   implementation("io.ktor:ktor-client-okhttp:${ktorVersion}")

   implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")
}

tasks.test {
   useJUnitPlatform()
}
kotlin {
   jvmToolchain(17)
}