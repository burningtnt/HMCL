import java.util.*

plugins {
    `cpp-application`
}

afterEvaluate {
    tasks.create("compileJNI") {
        dependsOn(project(":HMCLCore").tasks.getByName("compileJava"))

        val jniResults = mutableListOf<File>()
        tasks.withType(CppCompile::class.java).stream()
            .filter { cppCompile: CppCompile -> cppCompile.name.lowercase(Locale.ROOT).contains("release") }.forEach {
                it.dependsOn(project(":HMCLCore").tasks.getByName("compileJava"))
                it.includes(File(project(":HMCLCore").ext.get("hmcl.java.home") as String, "include").absolutePath)

                dependsOn(tasks.create(
                    "linkSharedLibrary${it.targetPlatform.get().name.substring("host:".length)}",
                    LinkSharedLibrary::class.java
                ) {
                    dependsOn(it)
                    targetPlatform.set(it.targetPlatform.get())
                    toolChain.set(it.toolChain.get())

                    val hashcode = it.outputs.files.singleFile.listFiles()[0].name
                    val objFile = it.outputs.files.singleFile.resolve("${hashcode}/jni.obj")
                    val dllFile = project.buildDir.resolve(
                        "dll/release/${it.objectFileDir.get().asFile.name}/${hashcode}/jni-${
                            it.targetPlatform.get().name.substring(
                                "host:".length
                            )
                        }.dll"
                    )

                    source.from(objFile)
                    destinationDirectory.set(dllFile.parentFile)
                    linkedFile.set(dllFile)
                    jniResults.add(dllFile)
                })
            }
        project.ext.set("hmcl.native.paths", jniResults)
    }

    listOf("build", "assemble", "check").forEach { taskName: String ->
        tasks.getByName(taskName) {
            enabled = false
            dependsOn.clear()
        }
    }
}

application {
    targetMachines.set(listOf(machines.windows.x86, machines.windows.x86_64))
}
