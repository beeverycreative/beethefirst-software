The development happens on "develop" branch here: https://github.com/beeverycreative/beethefirst-software/tree/develop

~~How to build on Linux (builds BEESOFT for Linux, MAC OS and Windows)~~
NOTE: there are tools ont the list that we use for development of even other projects.

#Install JDK 8:
sudo add-apt-repository ppa:webupd8team/java
sudo apt-get update
sudo apt-get -y install oracle-java8-installer
sudo apt-get -y install oracle-java8-set-default

sudo apt-get install libjava3d-java
sudo cp /usr/share/java/j3d*.jar  /usr/lib/jvm/java-8-oracle/jre/lib/ext/
	
sudo apt-get install libjava3d-jni
sudo cp /usr/lib/jni/libj3dcore-ogl.so /usr/lib/jvm/java-8-oracle/jre/lib/amd64/

sudo pip install pyusb
sudo pip install pyserial

#Install Nautilus:
sudo apt-get -y install nautilus-open-terminal
nautilus -q

#Install Git and Other tools:
sudo apt-get -y install git mesa-utils chromium-browser gitg vim ant

#Install NetBeans IDE and build the application
- Git clone the BEESOFT repositorie
- Change to "development" branch: git checkout development
- Install NetBeans 8, Java SE version
- Open NetBeans
- Open the project "beesoft-app"
- Do the "Clean and Build" of the project "beesfot-app"
- You can find the BEESOFT application on the folders: beethefirst-software/beesoft-app/beesoft-usb/target/linux


-------------
BEESOFT is the software that works with BEETHEFIRST. It's open source software adapted form ReplicatorG.

ReplicatorG is an open-source GCode based controller for RepRap / CNC machines.  It has 3 main goals:

1. Be as simple to use, and as easy to install as possible.
2. Be driver oriented and abstract the GCode away, allowing users to easily create drivers for their own machine.
3. Support as much of the GCode specification as possible.

For more information, see the website at: http://www.replicat.org

INSTALLATION

Windows: http://replicat.org/installation-windows
Mac OSX: http://replicat.org/installation-mac
Linux:   http://replicat.org/installation-linux

CREDITS

ReplicatorG is an open source project, owned by nobody and supported by many.

The project is descended from the wonderful Arduino host software (http://www.arduino.cc)
Arduino is descended from the also wonderful Processing environment (http://www.processing.org)

ReplicatorG was forked from Arduino in August 2008.

See changelog.txt for a list of changes in each version.

People who have worked on ReplicatorG include:

Zach 'Hoeken' Smith (http://www.zachhoeken.com)
Marius Kintel (http://reprap.soup.io)
Adam Mayer (http://makerbot.com)

A full list of contributers is in contributers.txt

