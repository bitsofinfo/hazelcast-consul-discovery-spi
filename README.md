# hazelcast-consul-discovery-spi

Provides a Consul based discovery strategy for Hazlecast 3.6-EA+ enabled applications.
This is an easy to configure plug-and-play Hazlecast DiscoveryStrategy that will optionally register each of your Hazelcast instances with Consul and enable Hazelcast nodes to dynamically discover one another via Consul.

* [Status](#status)
* [Requirements](#requirements)
* [Features](#features)
* [Build/Usage](#usage)
* [Related Info](#related)
* [Todo](#todo)
* [Notes](#notes)


## <a id="status"></a> Status

This is beta code.

## <a id="requirements"></a> Requirements

* Java 6+
* [Hazelcast 3.6-EA+](https://hazelcast.org/)
* [Consul](https://consul.io/)

## <a id="features"></a> Features


* Supports two modes of operation:
	* **Read-write**: peer discovery and registration of a hazelcast instance without a local Consul agent (self registration)
	* **Read-only**: peer discovery only with an existing Consul agent setup (no registration by the strategy itself)

* If you don't want to use the built in Consul registration, just specify the `DoNothingRegistrator` (see below) in your hazelcast discovery-strategy XML config. This will require you to run your own Consul agent that defines the hazelcast service.

* If using self-registration, specify `LocalDiscoveryNodeRegistrator` which additionally supports:
    * Automatic registration of the hazelcast instance with Consul
    * Custom Consul health-check script/interval to validate Hazelcast instance healthly
    * Control which IP is published as the service-address with Consul
    * Configurable discovery delay
    * Automatic Consul de-registration of instance via ShutdownHook

## <a id="usage"></a> Build & Usage

* Have Consul running and available somewhere on your network, start it such as:
```
consul agent -server -bootstrap-expect 1 -data-dir /tmp/consul -config-dir /path/to/consul.d/ -ui-dir /path/to/consul-web-ui
```
* From the root of this project, build a Jar : `./gradlew assemble`

* Include the built jar artifact located at `build/libs/hazelcast-consul-discovery-spi-1.0.0.jar` in your hazelcast project

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
              <property name="consul-registrator">org.bitsofinfo.hazelcast.discovery.consul.LocalDiscoveryNodeRegistrator</property>
		      <property name="consul-registrator-config"><![CDATA[
					{
					  "preferPublicAddress":false,
					  "healthCheckScript":"exec 6<>/dev/tcp/#MYIP/#MYPORT || (exit 3)",
					  "healthCheckScriptIntervalSeconds":30
					}
              ]]></property>
        </properties>
      </discovery-strategy>
    </discovery-strategies>

  </join>
</network>
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

## Consul UI example

Showing [LocalDiscoveryNodeRegistrator](src/main/java/org/bitsofinfo/hazelcast/discovery/consul/LocalDiscoveryNodeRegistrator.java) configured hazelcast services with health-checks

![Alt text](/docs/consul_ui.png "Diagram1")

## <a id="related"></a> Related info

* https://www.consul.io
* http://docs.hazelcast.org/docs/3.6-EA/manual/html-single/index.html#discovery-spi

## <a id="todo"></a> Todo

* Unit-tests
* Ensure all configuration tweakable via `-D` system properties
* Add support to force registered IP and PORT (for certain containerized scenarios)

## <a id="notes"></a> Notes


### Prior hazelcast versions
For versions of Hazelcast **prior to 3.6** you may want to look at these projects which seem to provide older implementations of Consul based discovery:

* https://github.com/decoomanj/hazelcast-consul
* https://github.com/decoomanj/hazelcast-consul-spi

### Consul health-check notes

You should see this in your Consul agent monitor when the health-check scripts are running:
```
> consul monitor --log-level trace

2015/11/20 11:21:39 [DEBUG] agent: check 'service:hz-discovery-test-cluster-192.168.0.208-192.168.0.208-5701' script 'exec 6<>/dev/tcp/192.168.0.208/5701 || (exit 3)' output:
2015/11/20 11:21:39 [DEBUG] agent: Check 'service:hz-discovery-test-cluster-192.168.0.208-192.168.0.208-5701' is passing
```

You will see something like these warnings logged when the health-check script interrogates the hazelcast port and does nothing. You are free to monitor the services any way you wish, or not at all by omitting the `healthCheckScript` JSON property; see [See hazelcast-consul-discovery-spi-example.xml](src/main/resources/hazelcast-consul-discovery-spi-example.xml) for an example.
```
Nov 20, 2015 6:57:50 PM com.hazelcast.nio.tcp.SocketAcceptorThread
INFO: [192.168.0.208]:5701 [hazelcast-consul-discovery] [3.6-EA] Accepting socket connection from /192.168.0.208:53495
Nov 20, 2015 6:57:50 PM com.hazelcast.nio.tcp.TcpIpConnectionManager
INFO: [192.168.0.208]:5701 [hazelcast-consul-discovery] [3.6-EA] Established socket connection between /192.168.0.208:5701 and /192.168.0.208:53495
Nov 20, 2015 6:57:50 PM com.hazelcast.nio.tcp.nonblocking.NonBlockingSocketWriter
WARNING: [192.168.0.208]:5701 [hazelcast-consul-discovery] [3.6-EA] SocketWriter is not set, creating SocketWriter with CLUSTER protocol!
Nov 20, 2015 6:57:50 PM com.hazelcast.nio.tcp.TcpIpConnection
INFO: [192.168.0.208]:5701 [hazelcast-consul-discovery] [3.6-EA] Connection [/192.168.0.208:53495] lost. Reason: java.io.EOFException[Could not read protocol type!]
```
