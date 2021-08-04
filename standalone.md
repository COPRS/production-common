# RS-API Standalone
For developing and testing purposes the RS-API can be used locally/standalone.

The project can be imported in your IDE as Maven project and build/run from there.
The other possibility is to build and run a docker image. To make things easier the `standalone.sh` script can be used like described below.

## Requisites
* Docker installed and running
* Maven installed (for building the jar package from the source code)

## Compile and Package Source Code
To compile and package the source code run from the project directory:
```console
$ mvn clean package

[INFO] --- spring-boot-maven-plugin:2.3.2.RELEASE:repackage (default) @ csgrs-native-api ---
[INFO] Replacing main artifact with repackaged archive
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```
After a successful build the jar file can be found in the target directory
```console
$ ls target | grep .jar
csgrs-native-api-0.0.1-SNAPSHOT.jar
```

## Build and Run Docker Image
First build the Docker image with:
```console
$ ./standalone.sh build

Successfully tagged rs-native-api-img:latest
```
Then run the Docker image with:
```console
$ ./standalone.sh run

running docker container: rs-native-api
```
Verify the API is up and running by:
```console
$ curl "http://localhost:8888/api/v1/ping" | jq
{
  "apiVersion": "v1"
}
```

## Handle Docker Containers and Images
Once Docker knows the API container it can conveniently be stopped and started with the `standalone.sh` script by running:
```console
$ ./standalone.sh stop

stopping docker container: rs-native-api
```
```console
$ ./standalone.sh start

starting docker container: rs-native-api
```
The container and image can be deleted with:
```console
$ ./standalone.sh remove container
```
```console
$ ./standalone.sh remove image
```

## Update the Standalone API
When there is a source code update stop the running container, delete the stopped container and delete the old Docker image. This can be done all in one by running:
```console
$ ./standalone.sh remove all
```
And then compile and package the updated source code, build the new Docker image and run the container like described above. The whole update process could be executed all in once by running:
```console
$ mvn clean package && ./standalone.sh remove all && ./standalone.sh build && ./standalone.sh run
```

## Saving and Loading the Docker Image
A ready built Docker image can be saved to a file that can be loaded into Docker on another machine. So the user would not have to compile and package the software and build the Docker image himself.

The developer saves the image by running:
```console
$ ./standalone.sh save

saving docker image: rs-native-api-img -> target/rs-native-api-img.tar.gz
```
The tar.gz file can then be send to another user who can load it into his Docker installation by copying the file into the target directory and run:
```console
$ ./standalone.sh load

loading docker image: target/rs-native-api-img.tar.gz
0ed946551078: Loading layer [==================================================>]  2.048kB/2.048kB
073d2e990188: Loading layer [==================================================>]  17.73MB/17.73MB
Loaded image: rs-native-api-img:latest
```
No image can be run, started and stopped as described above.
