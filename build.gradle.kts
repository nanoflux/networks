import net.minecrell.pluginyml.bukkit.BukkitPluginDescription
import net.minecrell.pluginyml.paper.PaperPluginDescription

plugins {
    `java-library`
    `maven-publish`
    signing
    id("xyz.jpenilla.run-paper") version "2.2.2"
    id("net.minecrell.plugin-yml.paper") version "0.6.0"
}

runPaper.folia.registerTask()

group = "dev.nanoflux"
version = "3.1.0-SNAPSHOT"
description = "A performance friendly way to sort your items"

repositories {
    mavenCentral()
    maven("https://papermc.io/repo/repository/maven-public/")
}

dependencies {
    //compileOnly("io.papermc.paper", "paper-api", "1.19.3-R0.1-SNAPSHOT")
    compileOnly("dev.folia", "folia-api", "1.20.6-R0.1-SNAPSHOT")
    paperLibrary("net.kyori", "adventure-text-minimessage", "4.13.1")
    paperLibrary("org.spongepowered", "configurate-hocon", "4.1.2")
    paperLibrary("org.spongepowered", "configurate-yaml", "4.1.2")
    paperLibrary("org.incendo", "cloud-core", "2.0.0")
    paperLibrary("org.incendo", "cloud-annotations", "2.0.0")
    paperLibrary("org.incendo", "cloud-paper", "2.0.0-beta.10")
    paperLibrary("org.incendo", "cloud-paper-signed-arguments", "2.0.0-beta.10")
    paperLibrary("org.incendo", "cloud-bukkit", "2.0.0-beta.10")
    paperLibrary("org.incendo", "cloud-minecraft-extras", "2.0.0-beta.10")
    paperLibrary("org.incendo", "cloud-brigadier", "2.0.0-beta.10")
    paperLibrary("com.google.code.gson", "gson", "2.10.1")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

paper {
    main = "dev.nanoflux.networks.Main"
    loader = "dev.nanoflux.networks.Loader"
    apiVersion = "1.20.6"
    load = BukkitPluginDescription.PluginLoadOrder.STARTUP
    website = "https://github.com/Kwantux/Networks"
    authors = listOf("Kwantux")
    prefix = "networks"

    permissions {
        register("networks.create") {
            description = "Allows you to create networks"
            default = BukkitPluginDescription.Permission.Default.TRUE // TRUE, FALSE, OP or NOT_OP
        }
        register("networks.listforeign") {
            description = "Allows you to list networks of other players"
            default = BukkitPluginDescription.Permission.Default.TRUE // TRUE, FALSE, OP or NOT_OP
        }
        register("networks.bypass_limit") {
            description = "Allows you to bypass the network owning limit"
            default = BukkitPluginDescription.Permission.Default.OP // TRUE, FALSE, OP or NOT_OP
        }
        register("networks.data") {
            description = "Allows you to save and reload config and network data"
            default = BukkitPluginDescription.Permission.Default.OP // TRUE, FALSE, OP or NOT_OP
        }
    }

    generateLibrariesJson = true

    foliaSupported = true
}

tasks {
    runServer {
        minecraftVersion("1.21.1")
    }
}