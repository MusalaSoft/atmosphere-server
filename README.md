[![Build Status](https://travis-ci.org/MusalaSoft/atmosphere-server.svg?branch=master)](https://travis-ci.org/MusalaSoft/atmosphere-server) [ ![Download](https://api.bintray.com/packages/musala/atmosphere/atmosphere-server/images/download.svg) ](https://bintray.com/musala/atmosphere/atmosphere-server/_latestVersion)  
See our site for better context of this readme. [Click here](http://atmosphereframework.com/)

# atmosphere-server
The atmosphere-server is the interface the [Clients](https://github.com/MusalaSoft/atmosphere-client) speak to. It establishes connections to [Agents](https://github.com/MusalaSoft/atmosphere-agent) and uses their set of devices to serve client requests.

## Build the project
You can build the project using the included Gradle wrapper by running:
* `./gradlew build` on Linux/macOS
* `gradlew build` on Windows

## Run the project
Open the terminal/command prompt window in the `atmosphere-server` root directory and run the following command:
* `./gradlew run` on Linux/macOS
* `gradlew run` on Windows

You know the server is running successfully when you see something of the kind:
```
...
>> 10 II 2017 13:03:27 - Running Server...
The Server has started successfully.
```

You may now connect Agents to it.

## Exit from the Server
To exit properly from the `Server` use the `exit` command. Otherwise an instance of the `Server` may still run and the next time you try to run a `Server` an error will occur.

```
>> exit
...
May 14, 2018 11:24:53 AM org.glassfish.tyrus.server.Server stop
INFO: Websocket Server stopped.
14 May 2018 11:24:54 - ClientRequestMonitor stopped successfully.
14 May 2018 11:24:54 - AgentMonitor unsubscribes for AgentEvent.
14 May 2018 11:24:54 - ServerManager unsubscribes for AgentEvent.
14 May 2018 11:24:54 - DataSourceProvider unsubscribes for DataSourceInitializedEvent.
14 May 2018 11:24:54 - ServerManager instance closed.
```
