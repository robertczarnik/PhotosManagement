# PhotosManagement

This is a Java CLI application, it moves photos to proper directories for you and do cool stuff on them.

## Getting Started

Java 1.10 is expected to be installed on our system.\
To run some utilities you need to install ffmpeg - https://ffmpeg.org/download.html

## Installing

After cloning this repository, change into the newly created directory and run

```
gradlew build
```

## Running the Application

After running `gradlew shadowJar` the resulting JAR file is located in directory `build/libs`.
The application can now be run by executing

```
java -jar build/libs/PhotosManagement-1.0.jar
```

## Features & Usage
In default app operates on a current folder you are now, you can change working directory by `changeDir dir`.

* move - move images to proper directories, depending on their original dates
* rename - rename directories to "serialNumber# dd.mm.yy" pattern
* update - actualization of serial numbers in directories names, it sorts directories by dates
* video frameTime audioPath ffmpegDir - make video from one image from each directory\
*(parameters are optional if specified in config file)*
* config - show configuration
* changeProp propertyName newValue - set property, propertyName=audioPath|ffmpegPath|frameTime)
* changeDir dir - change working dir
* exit - exit this application

## Screenshots

*coming soon*

## Built With

* [Gradle](https://gradle.org) - Dependency Management
* [metadata-extractor](https://github.com/drewnoakes/metadata-extractor) -  reading metadata from image files.
* [javaxt](https://mvnrepository.com/artifact/javaxt/javaxt-core/1.7.8) - rotating images.
