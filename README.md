# Java Docker health check app

Docker, since verions 1.12 or so, have a neat feature to run a health check on a container. 

#### Great! Let's do it with `curl` or `iwr`
Healt check is a really simple thing to implement. You call a service, and it should `0` if it works and `1` if it does
not. Simple? Sure, even Docker dockumentation will tell you how to do it with HTTP based service using `curl`:

```docker
HEALTHCHECK --interval=5m --timeout=3s \
  CMD curl -f http://localhost/ || exit 1
```

Here, every 5 minutes with a timeout of 3 seconds `localhost` will be called. Using `curl`. On Windows, using IWR, it's
a bit more complicate, but still - it works. It's simple in the end. Right? 

So why this lib?

#### The problem with `curl` and `iwr`
1. In Linux images, you need to have curl installed. You can start `FROM` alpine and have a 4MB base image. But guess 
what - it doesn't come with `curl` pre-installed. So you need to add `RUN apk --update --no-cache add curl`. Sure, 
no sweat, easy to do. That will add a new layer of 2.5MB to the image. And additional headaches of curl and libraries. 
2. In Windows images, you need to have PowerShell installed. But recent Nano images will not have it. To save space. 
Makes sense, right?
3. If you choose any of the above you loose portability. Different scripts for health-checks depending on the platform. 
Sure, no biggie... but what for? You have Java installed already. You had to install it. Or find an image that have Java
since your app will rely on it. Right? Let's use it...

### Here comes the solution
A simple library. <100kb. You run it as a Java command line app. Yes, it has to start up Java... but that is not really 
a huge deal nowadays.

To include it in your Docker image simply download the latest release from Github:

```bash
wget https://github.com/uded/docker-healthcheck-java/releases/download/healthcheck-1.0/healthcheck.jar
``` 

*OR* if you're running maven:

```xml
<plugin>
    <groupId>com.googlecode.maven-download-plugin</groupId>
    <artifactId>download-maven-plugin</artifactId>
    <version>1.4.0</version>
    <executions>
        <execution>
            <id>install-healthcheck</id>
            <phase>generate-resources</phase>
            <goals>
                <goal>wget</goal>
            </goals>
            <configuration>
                <url>https://github.com/uded/docker-healthcheck-java/releases/download/healthcheck-1.0/healthcheck.jar</url>
                <unpack>false</unpack>
                <outputFileName>healthcheck.jar</outputFileName>
                <outputDirectory>${project.build.directory}/lib</outputDirectory>
            </configuration>
        </execution>
    </executions>
</plugin>
```

Add it to your image and then add a `HEALTHCHECK` command ass well:

```docker
ADD target/lib/healthcheck.jar healthcheck.jar
HEALTHCHECK CMD java -jar {PATH_TO}/healthcheck.jar http://localhost:8080/actuator/health
```

That's it! By default every 30 seconds this simple JAR will fire up and request given Spring Boot URL and report back 
the result of such a query... 