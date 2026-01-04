package dev.by1337.core.impl.util.command;

import dev.by1337.core.util.command.BukkitCommandRegister;
import org.bukkit.Bukkit;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.craftbukkit.v1_17_R1.CraftServer;

public class BukkitCommandRegisterImpl implements BukkitCommandRegister {
    @Override
    public void register(BukkitCommand bukkitCommand) {
        CraftServer server = (CraftServer) Bukkit.getServer();
        server.getCommandMap().register(bukkitCommand.getName(), bukkitCommand);
    }

    @Override
    public void unregister(BukkitCommand bukkitCommand) {
        for (var s : bukkitCommand.getAliases()) {
            (((CraftServer) Bukkit.getServer()).getCommandMap()).getKnownCommands().remove(bukkitCommand.getName() + ":" + s);
            (((CraftServer) Bukkit.getServer()).getCommandMap()).getKnownCommands().remove(s);
        }
        (((CraftServer) Bukkit.getServer()).getCommandMap()).getKnownCommands().remove(bukkitCommand.getName() + ":" + bukkitCommand.getName());
        (((CraftServer) Bukkit.getServer()).getCommandMap()).getKnownCommands().remove(bukkitCommand.getName());
        bukkitCommand.unregister(((CraftServer) Bukkit.getServer()).getCommandMap());
    }
}
