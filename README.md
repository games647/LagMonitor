# LagMonitor

## Description

Gives you the possibility to monitor your server performance. This plugin is based on the powerful tools VisualVM and
Java Mission Control, both provided by Oracle. This plugin gives you the possibility to use the features provided by
these tools also in Minecraft itself. This might be useful for server owners/administrators who cannot use the tools.

Furthermore it especially made for Minecraft itself. So you can also check your TPS (Ticks per seconds), player ping,
server timings and so on.

## Features

* Player ping
* Sample CPU usage
* Analyze RAM usage
* Access to Stacktraces of running threads
* Shows your ticks per second
* Shows system performance usage
* Visual graph ingame
* Ingame timings viewer
* Access to Java environment variables (mbeans)
* Free
* Open Source

## Planning

* Traffic (Bandwith) counter - OS and minecraft (can be done by hooking into Netty) process only
* Heap usage
* Plugin specific profiling with the thread sampler (commands, events, scheduler) - > reduces memory need and can be
more detailed
* Pagination for commands bean, monitor and timing
* Caching of the timings or running it async
* Warning alert if the main thread is WAITING, BLOCKING, SLEEPING -> Example: for SQL-/HTTP-/File-Operations
which should be run async
* Average TPS and player ping
* Output the last samples of a player ping (ping is refreshed every 40 ticks)

## Permissions

lagmonitor.* - Access to all LagMonitor features

lagmonitor.commands.* - Access to all commands

#### All command permissions
* lagmonitor.command.ping
* lagmonitor.command.stacktrace
* lagmonitor.command.thread
* lagmonitor.command.tps
* lagmonitor.command.mbean
* lagmonitor.command.system
* lagmonitor.command.timing
* lagmonitor.command.monitor
* lagmonitor.command.graph
* lagmonitor.command.environment

## Commands

```
/ping - Gets your server ping
/ping <player> - Gets the ping of the selected player
/stacktrace - Gets the execution stacktrace of the current thread
/stacktrace <threadName> - Gets the execution stacktrace of selected thread
/thread - Outputs all running threads with their current state
/tps - Outputs the current tps
/mbean - List all available mbeans (java environment information, JMX)
/mbean <beanName> - List all available attributes of this mbean
/mbean <beanName> <attribute> - Outputs the value of this attribute
/system - Gives you some general information (minecraft server related)
/timing - Outputs your server timings ingame
/monitor - Monitors the CPU usage of methods
/graph [heap/cpu/threads] - Gives you visual graph about your server (currently only the heap usage)
/env - Gives you some general information (OS related)
```

## Images

Live visualizer of heap usage:

### Timing command
![timing command](https://i.imgur.com/wAxnIxt.png)

### CPU Graph (blue=process, yellow=system) - Process load
![cpu graph](https://i.imgur.com/DajnZmP.png)

### Stacktrace and Threads command
![stacktrace and threads](http://i.imgur.com/XY7r9wz.png)

### Ping Command
![ping command](http://i.imgur.com/LITJKWw.png)

### Thread Sampler (Monitor command)
![thread sample](https://i.imgur.com/OXOakN6.png)

### System command
![system command](https://i.imgur.com/DYrNtGS.png)

### Environment command
![environment command](http://i.imgur.com/gQwr126.png)

### Heap usage graph (yellow=allocated, blue=used)
![heap usage map](http://i.imgur.com/Yiz9h6G.png)
