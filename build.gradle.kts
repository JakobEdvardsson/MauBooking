plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.2"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
    implementation("io.github.cdimascio:dotenv-java:3.0.0")
    implementation ("org.jsoup:jsoup:1.16.1")
}

tasks.test {
    useJUnitPlatform()
}