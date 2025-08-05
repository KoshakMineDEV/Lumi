<h1 align="center">
    ✨ Lumi
</h1>
<p align="center">A sunshine between dark Minecraft Bedrock clouds. Drop-in replacement for Nukkit</p>

## Introduction
Lumi is a server software based on [Nukkit-MOT](https://github.com/MemoriesOfTime/Nukkit-MOT/) which was made to introduce many new features and improvements.

### What's new in Lumi?
1. Support for 1.20.0 – 1.21.93 version (you can set the minimum protocol in the config)
2. More vanilla features
3. Custom Blocks support
4. Custom Enchantments support
5. Better AntiXray system
6. Better API (effects, food, projectiles and etc.)

## How to install?
1. Install java 21 or higher
2. Download the .jar file from the links below
3. Write a command to run: `java -jar Lumi-1.1.0-SNAPSHOT.jar`

## Maven
Adding repo:
```xml
<repositories>
    <repository>
        <id>luminiadev-repository-snapshots</id>
        <url>https://repo.luminiadev.com/snapshots</url>
    </repository>
</repositories>
```

Adding dependency:
```xml
<dependency>
    <groupId>com.koshakmine</groupId>
    <artifactId>Lumi</artifactId>
    <version>1.1.0-SNAPSHOT</version>
</dependency>
```

## Gradle
Adding repo:
```kts
maven {
    name = "luminiadevRepositorySnapshots"
    url = uri("https://repo.luminiadev.com/snapshots")
}
```

Adding dependency:
```kts
compileOnly("com.koshakmine:Lumi:1.1.0-SNAPSHOT")
```

## Credits
[<img src="https://raw.githubusercontent.com/CloudburstMC/Nukkit/master/.github/images/logo.png" width="18"/>]() [Nukkit](https://github.com/CloudburstMC/Nukkit)  
[<img src="https://avatars.githubusercontent.com/u/62042238?s=200&v=4" width="18"/>]() [Nukkit-MOT](https://github.com/MemoriesOfTime/Nukkit-MOT)  
[<img src="https://avatars.githubusercontent.com/u/99014792?s=200&v=4" width="18"/>]() [PowerNukkitX](https://github.com/PowerNukkitX/PowerNukkitX)  
[<img src="https://avatars.githubusercontent.com/u/20168691?s=200&v=4" width="18"/>]() [EaseCation Nukkit](https://github.com/EaseCation/Nukkit)  
[<img src="https://avatars.githubusercontent.com/u/26197131?v=4" width="18"/>]() [NukkitPetteriM1Edition](https://github.com/PetteriM1/NukkitPetteriM1Edition)
