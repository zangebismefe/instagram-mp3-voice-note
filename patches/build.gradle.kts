group = "dev.zangebismefe.instagramvoice"

patches {
    about {
        name = "Instagram MP3 Voice Note"
        description = "Experimental Instagram Direct native voice note patch research"
        source = "https://github.com/zangebismefe/instagram-mp3-voice-note"
        author = "zangebismefe"
        contact = "https://github.com/zangebismefe/instagram-mp3-voice-note/issues"
        website = "https://github.com/zangebismefe/instagram-mp3-voice-note"
        license = "GPLv3"
    }
}

val patchListGeneratorClasspath = configurations.create("patchListGeneratorClasspath")

dependencies {
    compileOnly(libs.gson)
    patchListGeneratorClasspath(libs.gson)
}

tasks {
    register<JavaExec>("generatePatchesList") {
        description = "Build patch with patch list"
        dependsOn(build)
        classpath = sourceSets["main"].runtimeClasspath + patchListGeneratorClasspath
        mainClass.set("util.PatchListGeneratorKt")
    }
    publish {
        dependsOn("generatePatchesList")
    }
}
