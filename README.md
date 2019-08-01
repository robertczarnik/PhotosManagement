# PhotosManagement

This is a Java CLI application, it move photos to proper directories for you and do cool stuff on them.

## Getting Started

Java 1.8 is expected to be installed on our system.
To run some utilities you need to install ffmpeg - https://ffmpeg.org/download.html

## Installing

After cloning this repository, change into the newly created directory and run

```
./gradlew build
```

## Running the Application

After running `gradlew shadowJar` the resulting JAR file is located in directory `build/libs`.
The application can now be run by executing

```
java -jar build/libs/PhotosManagement-1.0.jar
```

## Features

* move - move images to proper directories
* rename - rename directories to "serialNumber# dd.mm.yy" pattern
* update - actualization of serial numbers in directories names
* video frameTime audioPath ffmpegDir - make video from one image from each folder


*All parameters are optional*

## Built With

* [Gradle](https://gradle.org) - Dependency Management
* [metadata-extractor](https://github.com/drewnoakes/metadata-extractor) -  reading metadata from image files.
* [javaxt](https://mvnrepository.com/artifact/javaxt/javaxt-core/1.7.8) - rotating images.
