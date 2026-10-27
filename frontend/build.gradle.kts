import java.io.File

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.13.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.23" apply false
}

// Si el proyecto reside en OneDrive, reubicar la carpeta temporal build para evitar errores de ReparsePoint y bloqueos de Windows
if (projectDir.absolutePath.contains("OneDrive", ignoreCase = true)) {
    val localBuildBase = File(System.getProperty("user.home"), ".gradle-builds/CiudadActiva/root")
    layout.buildDirectory.set(localBuildBase)
}
