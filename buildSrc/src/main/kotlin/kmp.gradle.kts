plugins {
    kotlin("multiplatform")
    id("tests")
}

kotlin {
    jvmToolchain(17)
    jvm()

    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
    }
}
