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
com.musala.atmosphere.server.Server.<init>(Server.java:104) 05 Jul 2016 10:40:19 - Server instance created succesfully.
...
com.musala.atmosphere.server.eventservice.ServerEventService.publish(ServerEventService.java:48) 05 Jul 2016 10:40:20 - Publishing event DevicePoolDaoCreatedEvent in thread main.
>> com.musala.atmosphere.server.state.RunningServer$InnerRunThread.run(RunningServer.java:47) 05 Jul 2016 10:40:20 - Running Server...
```

You may now connect Agents to it.
