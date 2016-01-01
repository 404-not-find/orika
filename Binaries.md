# Downloads #
The easiest way to include Orika in your project (if using Maven or a build tool compatible with Maven dependency management) is to include a dependency as shown below.

Not using Maven? Follow these links to download jars for **[releases](http://search.maven.org/#search|ga|1|orika)** or **[snapshots](https://repository-orika.forge.cloudbees.com/snapshot/ma/glasnost/orika/)**

## Releases ##
_Available from **Maven central**_
```
<dependency>
   <groupId>ma.glasnost.orika</groupId>
   <artifactId>orika-core</artifactId>
   <version>1.4.0</version><!-- or latest version -->
</dependency> 
```


---

## Snapshots ##
Include our Cloudbees snapshot repository
```
<repositories>
   ...
   <repository>
      <id>orika-snapshots</id>
      <name>Orika Snapshots hosted by Cloudbees</name>
      <url>https://repository-orika.forge.cloudbees.com/snapshot</url>
   </repository>
   ...	
</repositories>

<dependencies>
   ...
   <dependency>
      <groupId>ma.glasnost.orika</groupId>
      <artifactId>orika-core</artifactId>
      <version>1.4.1-SNAPSHOT</version><!-- or latest version -->
   </dependency> 
   ...
</dependencies>
```