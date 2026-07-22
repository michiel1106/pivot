package bikerboys.pivot.plugin;

import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {

    private final PivotPaperPlugin plugin;
    private boolean requireOp;
    private String permission;
    private boolean kickUnauthorized;
    private String kickMessage;
    private boolean debug;

    public ConfigManager(PivotPaperPlugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();
        
        requireOp = config.getBoolean("require-op", true);
        permission = config.getString("permission", "pivot.admin");
        kickUnauthorized = config.getBoolean("kick-unauthorized", false);
        kickMessage = config.getString("kick-message", "§cYou are not authorized to use Pivot on this server.");
        debug = config.getBoolean("debug", false);
    }

    public boolean isRequireOp() {
        return requireOp;
    }

    public String getPermission() {
        return permission;
    }

    public boolean isKickUnauthorized() {
        return kickUnauthorized;
    }

    public String getKickMessage() {
        return kickMessage;
    }

    public boolean isDebug() {
        return debug;
    }
}
