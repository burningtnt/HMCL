import java.util.*

plugins {
    `cpp-application`
}

val compileTargets = listOf(machines.windows.x86, machines.windows.x86_64)

afterEvaluate {
    val compileTargetMachines = mutableMapOf<String, TargetMachine>()
    val compileTargetMachinesStatus = mutableMapOf<String, Boolean>()
    val jniResults = mutableMapOf<String, File>()
    val jniFilesRoot = projectDir.resolve("outputs")
    if (!jniFilesRoot.exists()) {
        jniFilesRoot.mkdir()
    }
    for (machine in compileTargets) {
        compileTargetMachines[machine.architecture.name] = machine
        compileTargetMachinesStatus[machine.architecture.name] = false

        jniResults[machine.architecture.name] =
            jniFilesRoot.resolve("jni-${machine.architecture.name}.${if (machine.operatingSystemFamily.isWindows) "dll" else "so"}")

        compileTargetMachinesStatus[machine.architecture.name] = jniResults[machine.architecture.name]!!.exists()

    }

    tasks.create("compileJNI") {
        dependsOn(project(":HMCLCore").tasks.getByName("compileJava"))
        tasks.withType(CppCompile::class.java).stream()
            .filter { cppCompile: CppCompile -> cppCompile.name.lowercase(Locale.ROOT).contains("release") }.forEach {
                if (it.targetPlatform == null) {
                    return@forEach
                }

                if (compileTargetMachinesStatus[it.targetPlatform.get().architecture.name]!!) {
                    return@forEach
                }
                compileTargetMachinesStatus[it.targetPlatform.get().architecture.name] = true

                it.dependsOn(project(":HMCLCore").tasks.getByName("compileJava"))
                it.includes(File(project(":HMCLCore").ext.get("hmcl.java.home") as String, "include").absolutePath)

                dependsOn(tasks.create(
                    "linkSharedLibrary${it.targetPlatform.get().name.substring("host:".length)}",
                    LinkSharedLibrary::class.java
                ) {
                    dependsOn(it)
                    targetPlatform.set(it.targetPlatform.get())
                    toolChain.set(it.toolChain.get())

                    val sharedLibraryType = if (it.targetPlatform.get().operatingSystem.isWindows) "dll" else "so"
                    val hashcode = it.outputs.files.singleFile.listFiles()[0].name
                    val objFile = it.outputs.files.singleFile.resolve("${hashcode}/jni.obj")
                    val dllFile = project.buildDir.resolve(
                        "dll/release/${it.objectFileDir.get().asFile.name}/${hashcode}/jni-${
                            it.targetPlatform.get().name.substring(
                                "host:".length
                            )
                        }.${sharedLibraryType}"
                    )

                    source.from(objFile)
                    destinationDirectory.set(dllFile.parentFile)
                    linkedFile.set(dllFile)

                    outputs.files.files.add(dllFile)
                    doLast {
                        dllFile.copyTo(jniResults[it.targetPlatform.get().architecture.name]!!, true, 2048)
                    }
                })
            }

        val sb = StringBuilder()
        for (entry in compileTargetMachinesStatus) {
            if (!entry.value) {
                sb.append(entry.key)
                sb.append(", ")
            }
        }

        if (sb.isNotEmpty()) {
            throw RuntimeException("Native machine compile results ${sb.substring(0, sb.length - ", ".length)} not found")
        }

        project.ext.set("hmcl.native.paths", jniResults.values.toMutableList())
    }

    listOf("build", "assemble", "check").forEach { taskName: String ->
        tasks.getByName(taskName) {
            enabled = false
            dependsOn.clear()
        }
    }
}

application {
    targetMachines.set(compileTargets)
}
