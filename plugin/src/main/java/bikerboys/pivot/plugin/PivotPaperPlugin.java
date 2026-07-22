package bikerboys.pivot.plugin;

import bikerboys.pivot.plugin.network.PacketRegistry;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PivotPaperPlugin extends JavaPlugin implements Listener, CommandExecutor {

    private ConfigManager configManager;
    private PacketRegistry packetRegistry;
    private final Set<UUID> authorizedPlayers = new HashSet<>();

    @Override
    public void onEnable() {
        this.configManager = new ConfigManager(this);
        this.packetRegistry = new PacketRegistry(this);

        getServer().getPluginManager().registerEvents(this, this);
        getCommand("pivot").setExecutor(this);

        String[] channels = {
                "pivot:update_block_display",
                "pivot:align_x_y_z",
                "pivot:new_block_display",
                "pivot:new_item_display",
                "pivot:take_back_display_entity",
                "pivot:update_block_display_pos",
                "pivot:duplicate_block_display",
                "pivot:lock_display_entity",
                "pivot:set_block_display_blockstate",
                "pivot:change_block_display_state",
                "pivot:set_item_display_context"
        };

        for (String channel : channels) {
            getServer().getMessenger().registerIncomingPluginChannel(this, channel, packetRegistry);
        }

        getServer().getMessenger().registerOutgoingPluginChannel(this, "pivot:auth");
        getServer().getMessenger().registerOutgoingPluginChannel(this, "pivot:set_block_display");
    }

    @Override
    public void onDisable() {
        authorizedPlayers.clear();
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public boolean isAuthorized(Player player) {
        return authorizedPlayers.contains(player.getUniqueId());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTaskLater(this, () -> checkAndAuthPlayer(player), 20L);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        authorizedPlayers.remove(event.getPlayer().getUniqueId());
    }

    public void checkAndAuthPlayer(Player player) {
        if (!player.isOnline()) return;

        boolean canUse = false;
        if (configManager.isRequireOp() && player.isOp()) {
            canUse = true;
        } else if (!configManager.isRequireOp() && (configManager.getPermission() == null || configManager.getPermission().isEmpty() || player.hasPermission(configManager.getPermission()))) {
            canUse = true;
        } else if (configManager.isRequireOp() && !player.isOp() && configManager.getPermission() != null && !configManager.getPermission().isEmpty() && player.hasPermission(configManager.getPermission())) {
            canUse = true;
        }

        byte[] buf = new byte[1];
        buf[0] = (byte) (canUse ? 1 : 0);
        player.sendPluginMessage(this, "pivot:auth", buf);

        if (canUse) {
            authorizedPlayers.add(player.getUniqueId());
        } else {
            authorizedPlayers.remove(player.getUniqueId());
            if (configManager.isKickUnauthorized()) {
                player.kickPlayer(configManager.getKickMessage());
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (sender.hasPermission("pivot.admin")) {
                configManager.loadConfig();
                sender.sendMessage("§aPivotPaper configuration reloaded.");
                for (Player player : Bukkit.getOnlinePlayers()) {
                    checkAndAuthPlayer(player);
                }
                return true;
            } else {
                sender.sendMessage("§cYou do not have permission.");
                return true;
            }
        }
        sender.sendMessage("§ePivotPaper Commands:");
        sender.sendMessage("§e/pivot reload - Reloads the configuration");
        return true;
    }
}
