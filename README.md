## About
**Developer:** Nicholas Kowalski

**IDE:** JetBrains IntelliJ IDEA

**Java version:** 11.0.13 (Eclipse Temurin)

**Dependency management:** Apache Maven

**Overview:** This project is a test application for font rendering with OpenGL via LWJGL (Lightweight Java Game Library).

## FreeType
This project relies on an external library for SDFs (Signed Distance Fields) that is not managed by Maven.

### Installation Steps
[1] Download the jar (release 1.1) from: https://github.com/mlomb/freetype-jni

[2] Download the windows natives dll (release 1.1) from: https://github.com/mlomb/freetype-jni

[3] Create a folder named `libs` in the project root.

[4] Place the downloaded jar from step 1 in the root of `libs`.

[5] Create a folder named "natives" in the root of `libs`.

[6] Create a folder named "windows" in the root of `natives`.

[7] Place the downloaded jar from step 2 in the root of `windows`.

[8] Open a terminal in the project root.

[9] Run the following command in the terminal:
	`mvn install:install-file -Dfile=C:\Repositories\Independent\font-rendering\libs\freetype-jni.jar -DgroupId="com.mlomb" -DartifactId="mlomb" -Dversion="1.1" -Dpackaging="jar"`

[10] Open the project in IntelliJ.

[11] In the `pom.xml` file, place the following dependency:
	<dependency>
            <groupId>com.mlomb</groupId>
            <artifactId>mlomb</artifactId>
            <version>1.1</version>
        </dependency>

[12] On the top bar in IntelliJ, click Run > Edit Configurations...

[13] In VM Options, add the following (this will let IntelliJ load the natives):
	`-Djava.library.path=./libs/natives/windows`

[14] Done!

## Notes ##
Special thanks to Gabriel Ambrosio for the excellent video series on game development in Java (YouTube channel GamesWithGabe).
This project would not have been possible without it.
