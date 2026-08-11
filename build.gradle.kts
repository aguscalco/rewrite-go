plugins {
    id("java")
    id("org.openrewrite.build.recipe-library-base") version "latest.release"
    id("com.google.protobuf") version "0.9.4"
}

group = "org.openrewrite.recipe"
version = "0.1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://artifacts.codegenomeproject.org/maven")
    }
}

dependencies {
    implementation(platform("org.openrewrite.recipe:rewrite-recipe-bom:latest.release"))
    implementation("org.openrewrite:rewrite-core")
    
    compileOnly("org.projectlombok:lombok:latest.release")
    annotationProcessor("org.projectlombok:lombok:latest.release")
    
    implementation("com.google.protobuf:protobuf-java:3.25.3")
    
    testImplementation("org.openrewrite:rewrite-test")
    testImplementation("org.junit.jupiter:junit-jupiter-api:latest.release")
    testImplementation("org.junit.jupiter:junit-jupiter-params:latest.release")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:latest.release")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.25.3"
    }
}

sourceSets {
    main {
        proto {
            srcDir("parser/proto")
        }
    }
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-parameters")
    options.release.set(17)
}
