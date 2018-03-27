package com.github.games647.lagmonitor.commands;

import com.github.games647.lagmonitor.LagMonitor;
import com.github.games647.lagmonitor.LagUtils;

import java.util.Arrays;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import oshi.SystemInfo;
import oshi.hardware.NetworkIF;
import oshi.software.os.NetworkParams;

public class NetworkCommand extends LagCommand {

    public NetworkCommand(LagMonitor plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!isAllowed(sender, command)) {
            sendError(sender, "Not whitelisted");
            return true;
        }

        SystemInfo systemInfo = plugin.getNativeData().getSystemInfo();
        NetworkParams networkParams = systemInfo.getOperatingSystem().getNetworkParams();
        sendMessage(sender, "Domain name", networkParams.getDomainName());
        sendMessage(sender, "Host name", networkParams.getHostName());
        sendMessage(sender, "Default IPv4 Gateway", networkParams.getIpv4DefaultGateway());
        sendMessage(sender, "Default IPv6 Gateway", networkParams.getIpv6DefaultGateway());
        sendMessage(sender, "DNS servers", Arrays.toString(networkParams.getDnsServers()));

        for (NetworkIF networkInterface : systemInfo.getHardware().getNetworkIFs()) {
            sendMessage(sender, "Name", networkInterface.getName());
            sendMessage(sender, "    Displayname", networkInterface.getDisplayName());
            sendMessage(sender, "    MAC", networkInterface.getMacaddr());
            sendMessage(sender, "    MTU", String.valueOf(networkInterface.getMTU()));
            sendMessage(sender, "    IPv4", Arrays.toString(networkInterface.getIPv4addr()));
            sendMessage(sender, "    IPv6", Arrays.toString(networkInterface.getIPv6addr()));
            sendMessage(sender, "    Speed", String.valueOf(networkInterface.getSpeed()));

            String receivedBytes = LagUtils.readableBytes(networkInterface.getBytesRecv());
            String sentBytes = LagUtils.readableBytes(networkInterface.getBytesSent());
            sendMessage(sender, "    Received", receivedBytes);
            sendMessage(sender, "    Sent", sentBytes);
        }

        return false;
    }
}
