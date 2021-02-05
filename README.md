# hazelcast-consul-discovery-spi

[![Build Status](https://travis-ci.org/bitsofinfo/hazelcast-consul-discovery-spi.svg?branch=master)](https://travis-ci.org/bitsofinfo/hazelcast-consul-discovery-spi)

Provides a Consul based discovery strategy for Hazlecast 3.6+ enabled applications.
This is an easy to configure plug-and-play Hazlecast DiscoveryStrategy that will optionally register each of your Hazelcast instances with Consul and enable Hazelcast nodes to dynamically discover one another via Consul. 

See "releases" below for important compatibility details with different version of Hazelcast.

* [Status](#status)
* [Releases](#releases)
* [Requirements](#requirements)
* [Maven/Gradle install](#mavengradle)
* [Features](#features)
* [Usage](#usage)
* [Build from source](#building)
* [Unit tests](#tests)
* [Related Info](#related)
* [Todo](#todo)
* [Notes](#notes)
* [Docker info](#docker)
* [Consul ACL issues?](#consul)

![Diagram of hazelcast consul discovery strategy](/docs/diag.png "Diagram2")

## <a id="status"></a>Status

This is release candidate code, tested against Hazelcast 3.6-EA+ through 3.9.x and 4.x stable releases, as well as Consul 0.7.x up to 1.0.x.

**IMPORTANT: TAG: 2.0-RC1-20210205**: All tags prior to this might not end up in maven central. However this tag is a re-publish of 2.0-RC1 to be made available in Maven Central due to the sunset of Bintray/JCenter in 2021. 

**IMPORTANT: TAG: 1.0-RC9-20210205**: All tags prior to this might not end up in maven central. However this tag is a re-publish of 1.0-RC9 to be made available in Maven Central due to the sunset of Bintray/JCenter in 2021

## <a id="releases"></a>Releases


### Hazelcast compatibility

* For Hazelcast >= 4.0 you should use [2.0-RC1+](https://github.com/bitsofinfo/hazelcast-consul-discovery-spi/releases/tag/2.0-RC1)
  
* For Hazelcast <= 3.x you should use [1.0-RC9 or below](https://github.com/bitsofinfo/hazelcast-consul-discovery-spi/releases/tag/1.0-RC9)


### Release history

* MASTER - in progress, this README refers to what is in the master tag. Switch to relevant RELEASE tag above to see that versions README

* **2.0-RC1-20210205**: Same as 2.0-RC1 but made compliant for Maven Central due to JCenter/Bintray closure. 

* [2.0-RC1](https://github.com/bitsofinfo/hazelcast-consul-discovery-spi/releases/tag/2.0-RC1): PR #33 #34 Compat updates for Hazelcast 4.x thanks @amrsamii. **REQUIRES HAZELCAST 4.x+, will not work with Hazelcast <= 3.x**

* **1.0-RC9-20210205**: Same as 1.0-RC9 but made compliant for Maven Central due to JCenter/Bintray closure. Also explicity defined hazelcast dependency 3.10.4

* [1.0-RC9](https://github.com/bitsofinfo/hazelcast-consul-discovery-spi/releases/tag/1.0-RC9): PR #28 #29 (consul health script fixes) for #27

* [1.0-RC8](https://github.com/bitsofinfo/hazelcast-consul-discovery-spi/releases/tag/1.0-RC8): Tweaks for Consul 0.8+ (health check sample script change). consul-client upgrade to 0.17.1; build.gradle HZ `3.+` dependency. Address post Consul 0.8 ACL notes (in README) for [#26]

* [1.0-RC7](https://github.com/bitsofinfo/hazelcast-consul-discovery-spi/releases/tag/1.0-RC7): consul-client 0.13.12; Fix [#17] with PR https://github.com/bitsofinfo/hazelcast-consul-discovery-spi/pull/18

* [1.0-RC6](https://github.com/bitsofinfo/hazelcast-consul-discovery-spi/releases/tag/1.0-RC6): Fix [#12], consul-client 0.13.2 upgrade, fix base64 Java 8 issue. Tested against Hazelcast 3.6-EA+ through 3.7+ Stable releases

* [1.0-RC5](https://github.com/bitsofinfo/hazelcast-consul-discovery-spi/releases/tag/1.0-RC5): Upgrade to latest consul-client [#11](https://github.com/bitsofinfo/hazelcast-consul-discovery-spi/issues/11) Tested against Hazelcast 3.6-EA+ through 3.6.4+ Stable releases

* [1.0-RC4](https://github.com/bitsofinfo/hazelcast-consul-discovery-spi/releases/tag/1.0-RC4): Adds support for TCP/HTTP checks [#3](https://github.com/bitsofinfo/hazelcast-consul-discovery-spi/issues/3) Tested against Hazelcast 3.6-EA+ through 3.6 Stable releases

* [1.0-RC3](https://github.com/bitsofinfo/hazelcast-consul-discovery-spi/releases/tag/1.0-RC3): Adds support for TLS [#2](https://github.com/bitsofinfo/hazelcast-consul-discovery-spi/issues/2) and Consul ACLs [#4](https://github.com/bitsofinfo/hazelcast-consul-discovery-spi/issues/4) Tested against Hazelcast 3.6-EA+ through 3.6 Stable releases

* [1.0-RC2](https://github.com/bitsofinfo/hazelcast-consul-discovery-spi/releases/tag/1.0-RC2): Tested against Hazelcast 3.6-EA+ through 3.6 Stable releases

* [1.0-RC1](https://github.com/bitsofinfo/hazelcast-consul-discovery-spi/releases/tag/1.0-RC1): Tested against Hazelcast 3.6-EA+ through 3.6-RC1 Stable releases

## <a id="requirements"></a>Requirements

* Java 6+
* [Hazelcast 3.6+](https://hazelcast.org/)
* [Consul](https://consul.io/)

## <a id="mavengradle"></a>Maven/Gradle

To use this discovery strategy in your Maven or Gradle project use the dependency samples below.

### Gradle:

```
repositories {
    mavenCentral()
}

dependencies {
	compile 'org.bitsofinfo:hazelcast-consul-discovery-spi:2.0-RC1-20210205'

    // include your preferred javax.ws.rs-api implementation
    // (for the OrbitzWorldwide/consul-client dependency)
    // for example below:
    compile 'org.apache.cxf:cxf-rt-rs-client:3.0.3'
    compile 'org.apache.cxf:cxf-rt-transports-http-hc:3.0.3'
}
```

### Maven:

```
<dependencies>
    <dependency>
        <groupId>org.bitsofinfo</groupId>
        <artifactId>hazelcast-consul-discovery-spi</artifactId>
        <version>2.0-RC1-20210205</version>
    </dependency>

    <!-- include your preferred javax.ws.rs-api
         (for the https://github.com/OrbitzWorldwide/consul-client dependency)
         implementation - see gradle example above
    -->
</dependencies>
```

## <a id="features"></a>Features


* Supports two modes of operation:
	* **Read-write**: peer discovery and registration of a hazelcast instance without a local Consul agent (self registration)
	* **Read-only**: peer discovery only with an existing Consul agent setup (no registration by the strategy itself)

* If you don't want to use the built in Consul registration, just specify the `DoNothingRegistrator` (see below) in your hazelcast discovery-strategy XML config. This will require you to run your own Consul agent that defines the hazelcast service.

* If using self-registration, either `LocalDiscoveryNodeRegistrator` or `ExplicitIpPortRegistrator` which additionally support:
    * Automatic registration of the hazelcast instance with Consul
    * Custom Consul health-check script/interval to validate Hazelcast instance healthly
    * Control which IP is published as the service-address with Consul
    * Configurable discovery delay
    * Automatic Consul de-registration of instance via ShutdownHook


## <a id="usage"></a>Usage

* Ensure your project has the `hazelcast-consul-discovery-spi` artifact dependency declared in your maven pom or gradle build file as described above. Or build the jar yourself and ensure the jar is in your project's classpath.

* Have Consul running and available somewhere on your network, start it such as:
```
consul agent -server -bootstrap-expect 1 -data-dir /tmp/consul -config-dir /path/to/consul.d/ -ui-dir /path/to/consul-web-ui [-enable-script-checks]
```

* Configure your hazelcast.xml configuration file to use the `ConsulDiscoveryStrategy` (similar to the below): [See hazelcast-consul-discovery-spi-example.xml](src/main/resources/hazelcast-consul-discovery-spi-example.xml) for a full example with documentation of options.

* Launch your hazelcast instances, configured with the Consul discovery-strategy similar to the below: [see ManualRunner.java](src/test/java/org/bitsofinfo/hazelcast/discovery/consul/ManualRunner.java) example.

```
<network>
  <port auto-increment="true">5701</port>

  <join>
    <multicast enabled="false"/>
    <aws enabled="false"/>
    <tcp-ip enabled="false" />

     <discovery-strategies>
       <discovery-strategy enabled="true"
           class="org.bitsofinfo.hazelcast.discovery.consul.ConsulDiscoveryStrategy">

         <properties>
              <property name="consul-host">localhost</property>
              <property name="consul-port">8500</property>
              <property name="consul-service-name">hz-discovery-test-cluster</property>
              <property name="consul-healthy-only">true</property>
              <property name="consul-service-tags">hazelcast, test1</property>
              <property name="consul-discovery-delay-ms">10000</property>

              <property name="consul-acl-token"></property>
              <property name="consul-ssl-enabled">false</property>
              <property name="consul-ssl-server-cert-file-path"></property>
              <property name="consul-ssl-server-cert-base64"></property>
              <property name="consul-ssl-server-hostname-verify">true</property>

              <property name="consul-registrator">org.bitsofinfo.hazelcast.discovery.consul.LocalDiscoveryNodeRegistrator</property>
              <property name="consul-registrator-config"><![CDATA[
    					{
    					  "preferPublicAddress":false,
    					  "healthCheckProvider":"org.bitsofinfo.hazelcast.discovery.consul.ScriptHealthCheckBuilder",
    					  "healthCheckScript":"nc -z #MYIP #MYPORT",
    					  "healthCheckScriptIntervalSeconds":30,
    					}
                  ]]></property>
        </properties>
      </discovery-strategy>
    </discovery-strategies>

  </join>
</network>
```
* The above example uses a script health check, so don't forget to enable it in consul (see [Consul checks definition](https://www.consul.io/docs/agent/checks.html)).
You can also make use of a simple network TCP check, substituting the healthChek properties about with the following:

```
                "healthCheckProvider":"org.bitsofinfo.hazelcast.discovery.consul.TcpHealthCheckBuilder",
                "healthCheckTcp":"#MYIP:#MYPORT",
                "healthCheckTcpIntervalSeconds":30
```

* An you can also use the HTTP healthcheck that Hazelcast provides, don't forget to set Hazelcast property 'hazelcast.http.healthcheck.enabled' to true. (see Hazelcast's [Simple HTTP based healthcheck implementation](https://github.com/hazelcast/hazelcast/pull/9284)), substituting the healthChek properties about with the following:

```
 <property name="hazelcast.http.healthcheck.enabled">true</property>
 ...
                "healthCheckProvider":"org.bitsofinfo.hazelcast.discovery.consul.HttpHealthCheckBuilder",
                "healthCheckHttp":"http://#MYIP:#MYPORT/hazelcast/health",
                "healthCheckHttpIntervalSeconds":30,
```

* Once nodes are joined you can query Consul to see the auto-registration of hazelcast instances works, the service-id's generated etc

`curl http://localhost:8500/v1/catalog/services`

```
{
  "consul":[],
  "hz-discovery-test-cluster":["hazelcast","test1"],
  "web":["rails"]
}
```

`curl http://localhost:8500/v1/catalog/service/hz-discovery-test-cluster`

```
[
  {
    "Node":"myhost1",
    "Address":"192.168.0.208",
    "ServiceID":"hz-discovery-test-cluster-192.168.0.208-192.168.0.208-5701",
    "ServiceName":"hz-discovery-test-cluster",
    "ServiceTags":[
      "hazelcast",
      "test1"
    ],
    "ServiceAddress":"192.168.0.208",
    "ServicePort":5701
  },
  {
    "Node":"myhost1",
    "Address":"192.168.0.208",
    "ServiceID":"hz-discovery-test-cluster-192.168.0.208-192.168.0.208-5702",
    "ServiceName":"hz-discovery-test-cluster",
    "ServiceTags":[
      "hazelcast",
      "test1"
    ],
    "ServiceAddress":"192.168.0.208",
    "ServicePort":5702
  }
]
```

## <a id="building"></a>Building from source

* From the root of this project, build a Jar : `./gradlew assemble`

* Include the built jar artifact located at `build/libs/hazelcast-consul-discovery-spi-[VERSION].jar` in your hazelcast project

* If not already present in your hazelcast application's Maven (pom.xml) or Gradle (build.gradle) dependencies section; ensure that these dependencies are present (versions may vary as appropriate):

```
compile group: 'com.orbitz.consul', name: 'consul-client', version:'1.2.3'
compile group: 'org.apache.cxf', name:'cxf-rt-rs-client', version:'3.0.3'
compile group: 'org.apache.cxf', name:'cxf-rt-transports-http-hc', version:'3.0.3'
```

## Consul UI example

Showing [LocalDiscoveryNodeRegistrator](src/main/java/org/bitsofinfo/hazelcast/discovery/consul/LocalDiscoveryNodeRegistrator.java) configured hazelcast services with health-checks

![Diagram of consul ui](/docs/consul_ui.png "Diagram1")

## <a id="tests"></a>Unit-tests

It may also help you to understand the functionality by checking out and running the unit-tests
located at [src/test/java](src/test/java). **BE SURE TO READ** the comments in the test source files
as some of the tests require you to setup your local Consul and edit certain files.

From the command line you can run `TestExplicitIpPortRegistrator` and `TestLocalDiscoveryNodeRegistrator` unit-tests by invoking the `runTests` task using `gradlew` that runs both tests and displays the result on the console.

```
$ ./gradlew runTests
```

The task above will display output indicating the test has started and whether the test has passed or failed.

###### Sample output for passing test:
```
org.bitsofinfo.hazelcast.discovery.consul.TestExplicitIpPortRegistrator > testExplicitIpPortRegistrator STARTED

org.bitsofinfo.hazelcast.discovery.consul.TestExplicitIpPortRegistrator > testExplicitIpPortRegistrator PASSED
```

###### Sample output for failing test:
```
org.bitsofinfo.hazelcast.discovery.consul.TestDoNothingRegistrator > testDoNothingRegistrator STARTED

org.bitsofinfo.hazelcast.discovery.consul.TestDoNothingRegistrator > testDoNothingRegistrator FAILED
    java.lang.AssertionError at TestDoNothingRegistrator.java:85
```

To run individual unit-test, use the `unitTest.single` argument to provide the unit-test you would like to run. The command below runs the unit test for `TestDoNothingRegistrator`

```
$ ./gradlew -DunitTest.single=TestDoNothingRegistrator unitTest
```

##### Note on running `TestDoNothingRegistrator` unit-test
The `TestDoNothingRegistrator` unit-test should be run separately using the `unitTest.single` argument as demonstrated above as it requires you to register a service with your local consul with 5 nodes/instances. Please **CAREFULLY READ** the comments in `TestDoNothingRegistrator.java` to see how this test should be run.

##### Passing optional parameters to unit-tests
The following parameters can be passed with the `-D` option when invoking the tests
```
-DconsulPort=(some port)
-DconsulHost=(some host)
-DconsulAclToken=(some ACL token if the server requires it)
-DconsulSslEnabled=(true | false)
-DconsulSslServerCertFilePath=(/path/to/ca.cert)
-DconsulSslServerCertBase64=(base64 encoded cert string)
-DconsulSslServerHostnameVerify=(false|True)
-DconsulHealthCheckProvider=(org.bitsofinfo.hazelcast.discovery.consul.ScriptHealthCheckBuilder | org.bitsofinfo.hazelcast.discovery.consul.HttpHealthCheckBuilder)

```

## <a id="related"></a>Related info

* https://www.consul.io
* http://docs.hazelcast.org/docs/3.6/manual/html-single/index.html#discovery-spi
* https://www.consul.io/docs/guides/acl.html#complete-acl-coverage-in-consul-0-8
* **Swarm** version of this: https://github.com/bitsofinfo/hazelcast-docker-swarm-discovery-spi 
* **Etcd** version of this: https://github.com/bitsofinfo/hazelcast-etcd-discovery-spi

## <a id="todo"></a>Todo

* Ensure all configuration tweakable via `-D` system properties

## <a id="notes"></a> Notes

### <a id="docker"></a>Containerization (Docker) notes

This library may also be helpful to you: [docker-discovery-registrator-consul](https://github.com/bitsofinfo/docker-discovery-registrator-consul)

One of the main drivers for coding this module was for Hazelcast applications that were deployed as Docker containers
that would need to automatically register themselves with Consul for higher level cluster orchestration of the cluster.

If you are deploying your Hazelcast application as a Docker container, one helpful tip is that you will want to avoid hardwired
configuration in the hazelcast XML config, but rather have your Docker container take startup arguments that would be translated
to `-D` system properties on startup. Convienently Hazelcast can consume these JVM system properties and replace variable placeholders in the XML config. See this documentation for examples: [http://docs.hazelcast.org/docs/3.6/manual/html-single/index.html#using-variables](http://docs.hazelcast.org/docs/3.6/manual/html-single/index.html#using-variables)

Specifically when using this discovery strategy and Docker, it may be useful for you to use the [ExplicitIpPortRegistrator](src/main/java/org/bitsofinfo/hazelcast/discovery/consul/ExplicitIpPortRegistrator.java) `ConsulRegistrator` **instead** of the *LocalDiscoveryNodeRegistrator* as the latter relies on hazelcast to determine its IP/PORT and this may end up being the local container IP, and not the Docker host IP, leading to a situation where a unreachable IP/PORT combination is published to Consul.

**Example:** excerpt from [explicitIpPortRegistrator-example.xml](src/main/resources/explicitIpPortRegistrator-example.xml)

Start your hazelcast app such as with the below, this would assume that hazelcast is actually reachable via this configuration
via your Docker host and the port mappings that were specified on `docker run`. (i.e. the IP below would be your docker host/port that is mapped to the actual hazelcast app container and port it exposes for hazelcast).

* This library may also be helpful to you: [docker-discovery-registrator-consul](https://github.com/bitsofinfo/docker-discovery-registrator-consul) 

* Also see: https://github.com/bitsofinfo/hazelcast-consul-discovery-spi/issues/20 for info on how to do this.

See this [Docker issue for related info](https://github.com/docker/docker/issues/3778) on detecting mapped ports/ip from **within** a container

`java -jar myHzApp.jar -DregisterWithIpAddress=<dockerHostIp> -DregisterWithPort=<mappedContainerPortOnDockerHost> .... `

```
<property name="consul-registrator-config"><![CDATA[
      {
        "registerWithIpAddress":"${registerWithIpAddress}",
        "registerWithPort":${registerWithPort},
        "healthCheckScript":"nc -z #MYIP #MYPORT",
        "healthCheckScriptIntervalSeconds":30
      }
  ]]></property>
```
Until hazelcast fixes the numerous issues around interfaces/binding etc, you may be better off just running your hz app in a docker swarm and use: https://github.com/bitsofinfo/hazelcast-docker-swarm-discovery-spi  for peer to peer hazelcast cluster discovery.

### Consul health-check notes

Depending on the health check script you are using: (`nc -z #MYIP #MYPORT` OR  `/bin/sh exec 6<>/dev/tcp/#MYIP/#MYPORT || (exit 3)`  should see something like in your Consul agent monitor when the health-check scripts are running:
```
> consul monitor --log-level trace

2015/11/20 11:21:39 [DEBUG] agent: check 'service:hz-discovery-test-cluster-192.168.0.208-192.168.0.208-5701' script 'exec 6<>/dev/tcp/192.168.0.208/5701 || (exit 3)' output:
2015/11/20 11:21:39 [DEBUG] agent: Check 'service:hz-discovery-test-cluster-192.168.0.208-192.168.0.208-5701' is passing
```

Depending on the version of Hazelcast you are using, you may see something like these warnings logged when the health-check script interrogates the hazelcast port and does nothing. You are free to monitor the services any way you wish, or not at all by omitting the `healthCheckScript` JSON property; see [See hazelcast-consul-discovery-spi-example.xml](src/main/resources/hazelcast-consul-discovery-spi-example.xml) for an example.

```
Dec 13, 2017 10:25:40 AM com.hazelcast.nio.tcp.TcpIpConnection
INFO: [172.20.10.2]:5702 [hazelcast-consul-discovery-spi] [3.9.1] Connection[id=3, /172.20.10.2:5702->/172.20.10.2:57436, endpoint=null, alive=false, type=NONE] closed. Reason: Connection closed by the other side

Dec 13, 2017 10:25:40 AM com.hazelcast.nio.tcp.TcpIpConnection
INFO: [172.20.10.2]:5702 [hazelcast-consul-discovery-spi] [3.9.1] Connection[id=3, /172.20.10.2:5702->/172.20.10.2:57436, endpoint=null, alive=false, type=NONE] closed. Reason: Connection closed by the other side
```

OR

```
Nov 20, 2015 6:57:50 PM com.hazelcast.nio.tcp.SocketAcceptorThread
INFO: [192.168.0.208]:5701 [hazelcast-consul-discovery] [3.6] Accepting socket connection from /192.168.0.208:53495
Nov 20, 2015 6:57:50 PM com.hazelcast.nio.tcp.TcpIpConnectionManager
INFO: [192.168.0.208]:5701 [hazelcast-consul-discovery] [3.6] Established socket connection between /192.168.0.208:5701 and /192.168.0.208:53495
Nov 20, 2015 6:57:50 PM com.hazelcast.nio.tcp.nonblocking.NonBlockingSocketWriter
WARNING: [192.168.0.208]:5701 [hazelcast-consul-discovery] [3.6] SocketWriter is not set, creating SocketWriter with CLUSTER protocol!
Nov 20, 2015 6:57:50 PM com.hazelcast.nio.tcp.TcpIpConnection
INFO: [192.168.0.208]:5701 [hazelcast-consul-discovery] [3.6] Connection [/192.168.0.208:53495] lost. Reason: java.io.EOFException[Could not read protocol type!]
```

### <a id="consul"></a>Consul ACL issues

This library was originally developed prior to Consul 0.8, as of 0.8+, changes to the ACL system were made which may require you to grant
additional access if your target consul is in default deny mode

See: 

* https://www.consul.io/docs/guides/acl.html#complete-acl-coverage-in-consul-0-8
* https://www.consul.io/docs/guides/acl.html#bootstrapping-acls

For examples: For the unit tests to work, create a new `client` token w/ the following policy, then assign the token to the `consul-acl-token` setting for this SPI's XML config:

```
service "" { policy = "write" },
node "" { policy = "write" },
agent "" { policy = "read" }
```

NOTE! The above sample is just that (a simple sample), in production you may want to re-evaluate and lock down further as needed. 


## push

```
./gradlew -PbintrayUser= -PbintrayApiKey= bintray 
```