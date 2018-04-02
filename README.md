# LagMonitor

## Description

Gives you the possibility to monitor your server performance. This plugin is based on the powerful tools VisualVM and
Java Mission Control, both provided by Oracle. This plugin gives you the possibility to use the features provided by
these tools also in Minecraft itself. This might be useful for server owners/administrators who cannot use the tools.

Furthermore it especially made for Minecraft itself. So you can also check your TPS (Ticks per seconds), player ping,
server timings and so on.

## Features

* Player ping
* Offline Java version checker
* Thread safety checks
* Many details about your setup like Hardware (Disk, Processor, ...) and about your OS 
* Sample CPU usage
* Analyze RAM usage
* Access to Stacktraces of running threads
* Shows your ticks per second with history
* Shows system performance usage
* Visual graphs in-game
* In-game timings viewer
* Access to Java environment variables (mbeans)
* Plugin specific profiles
* Blocking operations on the main thread check
* Compatible 1.8+ servers
* Make Heap and Thread dumps
* Create Java Flight Recorder dump and analyze it later on your own computer
* Log the server performance into a MySQL/MariaDB database
* Free

## Requirements

* Java 8+
* Spigot or a fork of it (ex: Paper)

## Permissions

lagmonitor.* - Access to all LagMonitor features

lagmonitor.commands.* - Access to all commands

### All command permissions

* lagmonitor.command.ping
* lagmonitor.command.ping.other
* lagmonitor.command.stacktrace
* lagmonitor.command.thread
* lagmonitor.command.tps
* lagmonitor.command.mbean
* lagmonitor.command.system
* lagmonitor.command.environment
* lagmonitor.command.timing
* lagmonitor.command.monitor
* lagmonitor.command.graph
* lagmonitor.command.native
* lagmonitor.command.vm
* lagmonitor.command.network
* lagmonitor.command.tasks
* lagmonitor.command.heap
* lagmonitor.command.jfr

## Commands

    /ping - Gets your server ping
    /ping <player> - Gets the ping of the selected player
    /stacktrace - Gets the execution stacktrace of the current thread
    /stacktrace <threadName> - Gets the execution stacktrace of selected thread
    /thread - Outputs all running threads with their current state
    /tpshistory - Outputs the current tps
    /mbean - List all available mbeans (java environment information, JMX)
    /mbean <beanName> - List all available attributes of this mbean
    /mbean <beanName> <attribute> - Outputs the value of this attribute
    /system - Gives you some general information (Minecraft server related)
    /env - Gives you some general information (OS related)
    /timing - Outputs your server timings ingame
    /monitor [start/stop/paste] - Monitors the CPU usage of methods
    /graph [heap/cpu/threads/classes] - Gives you visual graph about your server (currently only the heap usage)
    /native - Gives you some native os information
    /vm - Outputs vm specific information like garbage collector, class loading or vm specification
    /network - Shows network interface configuration
    /tasks - Information about running and pending tasks
    /heap - Heap dump about your current memory
    /lagpage <next/prev/pageNumber/save/all> - Pagination command for the current pagination session
    /jfr <start/stop/dump> - Manages the Java Flight Recordings of the native Java VM. It gives you much more detailed
        information including network communications, file read/write times, detailed heap and thread data, ...

## Development builds

Development builds of this project can be acquired at the provided CI (continuous integration) server. It contains the
latest changes from the Source-Code in preparation for the following release. This means they could contain new
features, bug fixes and other changes since the last release.

Nevertheless builds are only tested using a small set of automated and minor manual tests. Therefore they **could**
contain new bugs and are likely to be less stable than released versions.

https://ci.codemc.org/job/Games647/job/LagMonitor/changes

## Images

### Heap command
![heap command](https://i.imgur.com/AzDwYxq.png)

### Timing command
![timing command](https://i.imgur.com/wAxnIxt.png)

### CPU Graph (blue=process, yellow=system) - Process load
![cpu graph](https://i.imgur.com/DajnZmP.png)

### Stacktrace and Threads command
![stacktrace and threads](https://i.imgur.com/XY7r9wz.png)

### Ping Command
![ping command](https://i.imgur.com/LITJKWw.png)

### Thread Sampler (Monitor command)
![thread sample](https://i.imgur.com/OXOakN6.png)

### System command
![system command](https://i.imgur.com/hrIV6bW.png)

### Environment command
![environment command](https://i.imgur.com/gQwr126.png)

### Heap usage graph (yellow=allocated, blue=used)
![heap usage map](https://i.imgur.com/Yiz9h6G.png)
