# LagMonitor

## Description

Gives you the possibility to monitor your server performance. This plugin is based on the powerful tools VisualVM and
Java Mission Control, both provided by Oracle. This plugin gives you the possibility to use the features provided by
these tools also in Minecraft itself. This might be useful for server owners/administrators who cannot use the tools.

Furthermore it especially made for Minecraft itself. So you can also check your TPS (Ticks per seconds), player ping,
server timings and so on.

## Features

* Player ping
* Access to Stacktraces of running threads
* Shows your ticks per second
* Shows system performance usage
* Visual graph ingame
* Ingame timings viewer
* Access to Java environment variables (mbeans)
* Free
* Open Source

## Permissions

None

## Commands

* /ping - Gets your server ping
* /ping <player> - Gets the ping of the selected player
* /stacktrace - Gets the execution stacktrace of the current thread
* /stacktrace <threadName> - Gets the execution stacktrace of selected thread
* /thread - Outputs all running threads with their current state
* /tps - Outputs the current tps
* /mbean - List all available mbeans (java environment information, JMX)
* /mbean <beanName> - List all available attributes of this mbean
* /mbean <beanName> <attribute> - Outputs the value of this attribute
* /system - Gives you some general information about your server like free ram, number of running threads or cpu usage
* /timing - Outputs your server timings ingame
* /monitor - Monitors the CPU usage of methods
* /graph - Gives you visual graph about your server (currently only the heap usage)

## Images

Live visualizer of heap usage:

![heap usage map](http://i.imgur.com/Yiz9h6G.png)