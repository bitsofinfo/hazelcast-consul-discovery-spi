# hazelcast-consul-discovery-spi

Provides a Consul based discovery strategy for Hazlecast 3.6-EA+ enabled applications.
This is an easy to configure plug-and-play Hazlecast DiscoveryStrategy that will optionally register each of your Hazelcast instances with Consul and enable Hazelcast nodes to dynamically discover one another.

* [Status](#status)
* [Requirements](#requirements)
* [Features](#features)
* [Usage](#usage)
* [Related Info](#related)
* [Notes](#notes)


## <a id="status"></a> Status

This is beta code.

## <a id="requirements"></a> Requirements

* Java 6+
* [Hazelcast 3.6-EA+](https://hazelcast.org/)
* [Consul](https://consul.io/)

## <a id="features"></a> Features

* Permits discovery of peers **without** a local Consul agent (self registration)

* Permits discovery of peers **with** an existing Consul agent setup

* If you don't want to use the built in Consul registration, just specify the `DoNothingRegistrator` (see below) in your hazelcast discovery-strategy XML config. This will require you to run your own Consul agent that defines the hazelcast service.

* If using self-registration, specify `LocalDiscoveryNodeRegistrator` which additionally supports:
    * Automatic registration of the hazelcast instance with Consul
    * Custom Consul health-check script/interval to validate Hazelcast instance healthly
    * Control which IP is published as the service-address with Consul
    * Configurable discovery delay
    * Automatic Consul de-registration of instance via ShutdownHook

## <a id="usage"></a> Usage

1. Have Consul running and available somewhere on your network
2. From the root of the project run: `./gradlew assemble`
3. Include the built jar artifact located at `build/libs/hazelcast-consul-discovery-spi-1.0.0.jar` in your hazelcast project
4. Configure your hazelcast.xml configuration file to use the `ConsulDiscoveryStrategy` (similar to the below): [See hazelcast-consul-discovery-spi-example.xml](src/main/resources/hazelcast-consul-discovery-spi-example.xml) for a full example with documentation of options.

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

## Consul UI example

Showing [LocalDiscoveryNodeRegistrator](src/main/java/org/bitsofinfo/hazelcast/discovery/consul/LocalDiscoveryNodeRegistrator.java) configured hazelcast services with health-checks

![Alt text](/docs/consul_ui.png "Diagram1")

## <a id="related"></a> Related info

* https://www.consul.io
* http://docs.hazelcast.org/docs/3.6-EA/manual/html-single/index.html#discovery-spi

## <a id="notes"></a> Notes

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
