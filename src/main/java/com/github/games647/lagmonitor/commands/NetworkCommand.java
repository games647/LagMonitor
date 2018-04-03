package com.github.games647.lagmonitor.commands;

import com.github.games647.lagmonitor.LagMonitor;
import com.github.games647.lagmonitor.utils.LagUtils;

import java.util.Arrays;
import java.util.Optional;

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
        if (!canExecute(sender, command)) {
            return true;
        }

        Optional<SystemInfo> optInfo = plugin.getNativeData().getSystemInfo();
        if (optInfo.isPresent()) {
            displayNetworkInfo(sender, optInfo.get());
        } else {
            sender.sendMessage(NATIVE_NOT_FOUND);
        }

        return true;
    }

    private void displayNetworkInfo(CommandSender sender, SystemInfo systemInfo) {
        displayGlobalNetworkInfo(sender, systemInfo.getOperatingSystem().getNetworkParams());
        for (NetworkIF networkInterface : systemInfo.getHardware().getNetworkIFs()) {
            displayInterfaceInfo(sender, networkInterface);
        }
    }

    private void displayGlobalNetworkInfo(CommandSender sender, NetworkParams networkParams) {
        sendMessage(sender, "Domain name", networkParams.getDomainName());
        sendMessage(sender, "Host name", networkParams.getHostName());
        sendMessage(sender, "Default IPv4 Gateway", networkParams.getIpv4DefaultGateway());
        sendMessage(sender, "Default IPv6 Gateway", networkParams.getIpv6DefaultGateway());
        sendMessage(sender, "DNS servers", Arrays.toString(networkParams.getDnsServers()));
    }

    private void displayInterfaceInfo(CommandSender sender, NetworkIF networkInterface) {
        sendMessage(sender, "Name", networkInterface.getName());
        sendMessage(sender, "    Display", networkInterface.getDisplayName());
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
}
