import java.io.File

// Augman Error Handler CLI
object AugmanErrorHandler {

    // List of common errors & fixes
    private val errorFixMap = mapOf(
        "Unresolved reference" to "Check if all dependencies are added correctly in build.gradle",
        "Type mismatch" to "Check your variable types and Kotlin syntax",
        "Could not find org.jetbrains.kotlin:kotlin-stdlib" to "Make sure your Kotlin version matches in build.gradle and plugins",
        "kotlin.jvm.internal" to "Try cleaning the project and rebuilding (./gradlew clean build)",
        // Add more error patterns and suggestions here
    )

    fun analyzeLog(logFilePath: String) {
        val file = File(logFilePath)
        if (!file.exists()) {
            println("Error: Log file does not exist.")
            return
        }

        val logContent = file.readText()
        var foundIssue = false

        for ((errorKey, fix) in errorFixMap) {
            if (logContent.contains(errorKey, ignoreCase = true)) {
                println("Detected issue: $errorKey")
                println("Suggested fix: $fix\n")
                foundIssue = true
            }
        }

        if (!foundIssue) {
            println("No common errors detected. Check the log manually or extend Augman’s knowledge base.")
        }
    }
}

// Usage example:
fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("Usage: run with path to your gradle build log file.")
        return
    }
    AugmanErrorHandler.analyzeLog(args[0])
}

"Execution failed for task" to "Check if there's a syntax or dependency error in the referenced task.",
"Unresolved reference: R" to "Might be a missing import or a failed build. Try cleaning and rebuilding the project.",
"Cannot access class" to "Maybe you're missing a Kotlin Android plugin or kapt setup.",
Runtime.getRuntime().exec("./gradlew clean")

if (logContent.contains("Unresolved reference", ignoreCase = true)) {
    println("Detected issue: Unresolved reference")
    println("Suggested fix: Check if all dependencies are added correctly in build.gradle\n")

    try {
        println("Auto-fix: Running './gradlew clean' ...")
        val process = ProcessBuilder("./gradlew", "clean")
            .redirectOutput(ProcessBuilder.Redirect.INHERIT)
            .redirectError(ProcessBuilder.Redirect.INHERIT)
            .start()
        process.waitFor()
    } catch (e: Exception) {
        println("Auto-fix failed: ${e.message}")
    }

    foundIssue = true
}
