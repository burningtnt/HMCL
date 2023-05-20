plugins {
    `java-library`
}

tasks.processResources {
    dependsOn(project(":HMCLNative").tasks.getByName("compileJNI"))

    doLast {
        val nativeRoot = this@processResources.destinationDir.resolve("native")
        if (!nativeRoot.exists()) {
            nativeRoot.mkdir()
        }

        (project(":HMCLNative").ext.get("hmcl.native.paths") as MutableList<File>).forEach { file: File ->
            if (!file.exists()) {
                throw RuntimeException("JNI File \"${file.absolutePath}\" doesn't exist.")
            }

            file.copyTo(nativeRoot.resolve(file.name))
        }
    }
}

dependencies {
    api("org.glavo:simple-png-javafx:0.3.0")
    api("com.google.code.gson:gson:2.10.1")
    api("com.moandjiezana.toml:toml4j:0.7.2")
    api("org.tukaani:xz:1.9")
    api("org.hildan.fxgson:fx-gson:5.0.0")
    api("org.jenkins-ci:constant-pool-scanner:1.2")
    api("com.github.steveice10:opennbt:1.5")
    api("org.nanohttpd:nanohttpd:2.3.1")
    api("org.apache.commons:commons-compress:1.23.0")
    compileOnlyApi("org.jetbrains:annotations:24.0.1")
}

ext.set("hmcl.java.home", org.gradle.internal.jvm.Jvm.current().javaHome.absolutePath)