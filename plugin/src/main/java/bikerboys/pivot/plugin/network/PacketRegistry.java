package bikerboys.pivot.plugin.network;

import bikerboys.pivot.plugin.PivotPaperPlugin;
import java.nio.ByteBuffer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.UUID;

public class PacketRegistry implements PluginMessageListener {

    private final PivotPaperPlugin plugin;
    private final NamespacedKey ownerKey;
    private final NamespacedKey lockedKey;
    private final NamespacedKey originalBlockKey;

    public PacketRegistry(PivotPaperPlugin plugin) {
        this.plugin = plugin;
        this.ownerKey = new NamespacedKey(plugin, "owner");
        this.lockedKey = new NamespacedKey(plugin, "locked");
        this.originalBlockKey = new NamespacedKey(plugin, "originalblock");
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!plugin.isAuthorized(player)) {
            return;
        }

        ByteBuffer buf = ByteBuffer.wrap(message);
        try {
            switch (channel) {
                case "pivot:update_block_display":
                    handleUpdateBlockDisplay(player, buf);
                    break;
                case "pivot:align_x_y_z":
                    handleAlignXYZ(player, buf);
                    break;
                case "pivot:new_block_display":
                    handleNewBlockDisplay(player, buf);
                    break;
                case "pivot:new_item_display":
                    handleNewItemDisplay(player, buf);
                    break;
                case "pivot:take_back_display_entity":
                    handleTakebackBlock(player, buf);
                    break;
                case "pivot:update_block_display_pos":
                    handleUpdatePos(player, buf);
                    break;
                case "pivot:duplicate_block_display":
                    handleDuplicate(player, buf);
                    break;
                case "pivot:lock_display_entity":
                    handleLock(player, buf);
                    break;
                case "pivot:set_block_display_blockstate":
                    handleSetBlockstate(player, buf);
                    break;
                case "pivot:change_block_display_state":
                    handleChangeBlockstate(player, buf);
                    break;
                case "pivot:set_item_display_context":
                    handleSetItemContext(player, buf);
                    break;
            }
        } catch (Exception e) {
            if (plugin.getConfigManager().isDebug()) {
                plugin.getLogger().warning("Error handling packet for channel " + channel + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void handleUpdateBlockDisplay(Player player, ByteBuffer buf) {
        String uuidStr = BufferUtil.readString(buf);
        Entity entity = Bukkit.getEntity(UUID.fromString(uuidStr));
        if (!(entity instanceof Display display)) return;
        if (!canModify(player, display)) return;

        boolean hasTranslation = buf.get() != 0;
        Vector3f translation = hasTranslation ? new Vector3f(buf.getFloat(), buf.getFloat(), buf.getFloat()) : null;

        boolean hasRotation = buf.get() != 0;
        Quaternionf rightRotation = hasRotation ? new Quaternionf(buf.getFloat(), buf.getFloat(), buf.getFloat(), buf.getFloat()) : null;

        boolean hasScale = buf.get() != 0;
        Vector3f scale = hasScale ? new Vector3f(buf.getFloat(), buf.getFloat(), buf.getFloat()) : null;

        Transformation t = display.getTransformation();
        display.setTransformation(new Transformation(
                translation != null ? translation : t.getTranslation(),
                t.getLeftRotation(),
                scale != null ? scale : t.getScale(),
                rightRotation != null ? rightRotation : t.getRightRotation()
        ));
    }

    private void handleAlignXYZ(Player player, ByteBuffer buf) {
        String uuidStr = BufferUtil.readString(buf);
        String coord = BufferUtil.readString(buf);
        Entity entity = Bukkit.getEntity(UUID.fromString(uuidStr));
        if (!(entity instanceof Display display)) return;
        if (!canModify(player, display)) return;

        Location loc = display.getLocation();
        if (coord.equalsIgnoreCase("x")) loc.setX(Math.round(loc.getX()));
        if (coord.equalsIgnoreCase("y")) loc.setY(Math.round(loc.getY()));
        if (coord.equalsIgnoreCase("z")) loc.setZ(Math.round(loc.getZ()));
        display.teleport(loc);
    }

    private void handleNewBlockDisplay(Player player, ByteBuffer buf) {
        BufferUtil.readString(buf);

        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (mainHand.getType() == Material.AIR) return;

        BlockData blockData = null;
        if (mainHand.getType().isBlock()) {
            blockData = Bukkit.createBlockData(mainHand.getType());
        } else if (mainHand.getType() == Material.FLINT_AND_STEEL) {
            blockData = Bukkit.createBlockData(Material.FIRE);
        }

        if (blockData != null) {
            BlockDisplay display = player.getWorld().spawn(player.getLocation(), BlockDisplay.class);
            display.setBlock(blockData);
            setOwner(display, player.getUniqueId());
            sendSelectionUpdate(player, display.getUniqueId().toString());

            if (player.getGameMode() == org.bukkit.GameMode.SURVIVAL) {
                if (mainHand.getType().isBlock()) {
                    mainHand.setAmount(mainHand.getAmount() - 1);
                } else if (mainHand.getType() == Material.FLINT_AND_STEEL) {
                    // Ignora danno flint_and_steel
                }
            }
        }
    }

    private void handleNewItemDisplay(Player player, ByteBuffer buf) {
        BufferUtil.readString(buf);

        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (mainHand.getType() == Material.AIR) return;

        ItemDisplay display = player.getWorld().spawn(player.getLocation(), ItemDisplay.class);
        display.setItemStack(mainHand.clone());
        setOwner(display, player.getUniqueId());
        sendSelectionUpdate(player, display.getUniqueId().toString());

        if (player.getGameMode() == org.bukkit.GameMode.SURVIVAL) {
            mainHand.setAmount(mainHand.getAmount() - 1);
        }
    }

    private void handleTakebackBlock(Player player, ByteBuffer buf) {
        String uuidStr = BufferUtil.readString(buf);
        Entity entity = Bukkit.getEntity(UUID.fromString(uuidStr));
        if (!(entity instanceof Display display)) return;
        if (!canModify(player, display)) return;

        if (display instanceof BlockDisplay bd) {
            if (player.getGameMode() == org.bukkit.GameMode.SURVIVAL) {
                Material mat = bd.getBlock().getMaterial();
                player.getInventory().addItem(new ItemStack(mat));
            }
        } else if (display instanceof ItemDisplay id) {
            if (player.getGameMode() == org.bukkit.GameMode.SURVIVAL) {
                player.getInventory().addItem(id.getItemStack());
            }
        }
        display.remove();
    }

    private void handleUpdatePos(Player player, ByteBuffer buf) {
        String uuidStr = BufferUtil.readString(buf);
        double x = buf.getDouble();
        double y = buf.getDouble();
        double z = buf.getDouble();

        Entity entity = Bukkit.getEntity(UUID.fromString(uuidStr));
        if (!(entity instanceof Display display)) return;
        if (!canModify(player, display)) return;

        Location loc = display.getLocation();
        loc.setX(x);
        loc.setY(y);
        loc.setZ(z);
        display.teleport(loc);
    }

    private void handleDuplicate(Player player, ByteBuffer buf) {
        String uuidStr = BufferUtil.readString(buf);
        Entity entity = Bukkit.getEntity(UUID.fromString(uuidStr));
        if (!(entity instanceof Display display)) return;
        if (!canModify(player, display)) return;

        ItemStack mainHand = player.getInventory().getItemInMainHand();

        if (display instanceof BlockDisplay bd) {
            Material displayMat = bd.getBlock().getMaterial();
            if (mainHand.getType() == displayMat || displayMat == Material.FIRE || displayMat == Material.SOUL_FIRE) {
                BlockDisplay dup = player.getWorld().spawn(bd.getLocation(), BlockDisplay.class);
                dup.setBlock(bd.getBlock());
                dup.setTransformation(bd.getTransformation());
                setOwner(dup, player.getUniqueId());

                if (player.getGameMode() == org.bukkit.GameMode.SURVIVAL) {
                    mainHand.setAmount(mainHand.getAmount() - 1);
                }
            } else {
                player.sendMessage("§cYou must be holding the block to duplicate it.");
            }
        } else if (display instanceof ItemDisplay id) {
            if (mainHand.getType() == id.getItemStack().getType()) {
                ItemDisplay dup = player.getWorld().spawn(id.getLocation(), ItemDisplay.class);
                dup.setItemStack(id.getItemStack().clone());
                dup.setTransformation(id.getTransformation());
                dup.setItemDisplayTransform(id.getItemDisplayTransform());
                setOwner(dup, player.getUniqueId());

                if (player.getGameMode() == org.bukkit.GameMode.SURVIVAL) {
                    mainHand.setAmount(mainHand.getAmount() - 1);
                }
            } else {
                player.sendMessage("§cYou must be holding the item to duplicate it.");
            }
        }
    }

    private void handleLock(Player player, ByteBuffer buf) {
        String uuidStr = BufferUtil.readString(buf);
        Entity entity = Bukkit.getEntity(UUID.fromString(uuidStr));
        if (!(entity instanceof Display display)) return;
        
        String ownerStr = display.getPersistentDataContainer().get(ownerKey, PersistentDataType.STRING);
        if (ownerStr != null && !ownerStr.isEmpty() && !ownerStr.equals(player.getUniqueId().toString())) {
            if (!player.hasPermission("pivot.admin")) return;
        }

        boolean isLocked = display.getPersistentDataContainer().getOrDefault(lockedKey, PersistentDataType.BYTE, (byte)0) == 1;
        display.getPersistentDataContainer().set(lockedKey, PersistentDataType.BYTE, (byte)(isLocked ? 0 : 1));
    }

    private void handleSetBlockstate(Player player, ByteBuffer buf) {
        String uuidStr = BufferUtil.readString(buf);
        String propertyName = BufferUtil.readString(buf);
        String value = BufferUtil.readString(buf);

        Entity entity = Bukkit.getEntity(UUID.fromString(uuidStr));
        if (!(entity instanceof BlockDisplay display)) return;
        if (!canModify(player, display)) return;

        BlockData data = display.getBlock();
        try {
            BlockData newData = Bukkit.createBlockData(data.getAsString(true).replaceAll("\\[(.*?)\\]", "") + "[" + propertyName + "=" + value + "]");
            display.setBlock(newData);
        } catch (IllegalArgumentException e) {
            try {
                String currentStr = data.getAsString(true);
                if (currentStr.contains("[")) {
                    String beforeArgs = currentStr.substring(0, currentStr.indexOf('['));
                    String args = currentStr.substring(currentStr.indexOf('[') + 1, currentStr.length() - 1);
                    String[] splitArgs = args.split(",");
                    StringBuilder newArgs = new StringBuilder();
                    boolean replaced = false;
                    for (String arg : splitArgs) {
                        if (arg.startsWith(propertyName + "=")) {
                            newArgs.append(propertyName).append("=").append(value).append(",");
                            replaced = true;
                        } else {
                            newArgs.append(arg).append(",");
                        }
                    }
                    if (!replaced) {
                        newArgs.append(propertyName).append("=").append(value).append(",");
                    }
                    if (newArgs.length() > 0) newArgs.setLength(newArgs.length() - 1);
                    display.setBlock(Bukkit.createBlockData(beforeArgs + "[" + newArgs + "]"));
                } else {
                    display.setBlock(Bukkit.createBlockData(currentStr + "[" + propertyName + "=" + value + "]"));
                }
            } catch (Exception ex) {
                if (plugin.getConfigManager().isDebug()) {
                    plugin.getLogger().warning("Failed to set blockstate: " + ex.getMessage());
                }
            }
        }
    }

    private void handleChangeBlockstate(Player player, ByteBuffer buf) {
        String uuidStr = BufferUtil.readString(buf);
        String whatchange = BufferUtil.readString(buf);

        Entity entity = Bukkit.getEntity(UUID.fromString(uuidStr));
        if (!(entity instanceof BlockDisplay display)) return;
        if (!canModify(player, display)) return;

        Material mat = display.getBlock().getMaterial();

        if (mat == Material.STICKY_PISTON && whatchange.equalsIgnoreCase("nonsticky")) {
            display.setBlock(Bukkit.createBlockData(Material.PISTON));
            player.getInventory().addItem(new ItemStack(Material.SLIME_BALL));
        } else if (mat == Material.PISTON) {
            if (whatchange.equalsIgnoreCase("pistonhead")) {
                display.setBlock(Bukkit.createBlockData(Material.PISTON_HEAD));
            } else if (whatchange.equalsIgnoreCase("makesticky")) {
                if (player.getInventory().contains(Material.SLIME_BALL)) {
                    player.getInventory().removeItem(new ItemStack(Material.SLIME_BALL, 1));
                    display.setBlock(Bukkit.createBlockData(Material.STICKY_PISTON));
                } else {
                    player.sendMessage("§cYou don't have a slime ball in your inventory.");
                }
            }
        } else if (mat == Material.PISTON_HEAD && whatchange.equalsIgnoreCase("piston")) {
            display.setBlock(Bukkit.createBlockData(Material.PISTON));
        } else if (mat == Material.DIRT) {
            if (whatchange.equalsIgnoreCase("grass")) display.setBlock(Bukkit.createBlockData(Material.GRASS_BLOCK));
            if (whatchange.equalsIgnoreCase("farmland")) display.setBlock(Bukkit.createBlockData(Material.FARMLAND));
        } else if (mat == Material.GRASS_BLOCK) {
            if (whatchange.equalsIgnoreCase("dirt")) display.setBlock(Bukkit.createBlockData(Material.DIRT));
            if (whatchange.equalsIgnoreCase("farmland")) display.setBlock(Bukkit.createBlockData(Material.FARMLAND));
        } else if (mat == Material.FARMLAND) {
            if (whatchange.equalsIgnoreCase("dirt")) display.setBlock(Bukkit.createBlockData(Material.DIRT));
            if (whatchange.equalsIgnoreCase("grass")) display.setBlock(Bukkit.createBlockData(Material.GRASS_BLOCK));
        } else if (mat == Material.BAMBOO_SAPLING && whatchange.equalsIgnoreCase("bamboo")) {
            display.setBlock(Bukkit.createBlockData(Material.BAMBOO));
        } else if (mat == Material.BAMBOO && whatchange.equalsIgnoreCase("bambooshoot")) {
            display.setBlock(Bukkit.createBlockData(Material.BAMBOO_SAPLING));
        }
    }

    private void handleSetItemContext(Player player, ByteBuffer buf) {
        String uuidStr = BufferUtil.readString(buf);
        String ctx = BufferUtil.readString(buf);
        Entity entity = Bukkit.getEntity(UUID.fromString(uuidStr));
        if (!(entity instanceof ItemDisplay display)) return;
        if (!canModify(player, display)) return;

        try {
            display.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.valueOf(ctx.toUpperCase()));
        } catch (IllegalArgumentException ignored) {}
    }

    private boolean canModify(Player player, Display entity) {
        if (player.hasPermission("pivot.admin")) return true;
        if (entity.getPersistentDataContainer().getOrDefault(lockedKey, PersistentDataType.BYTE, (byte)0) == 1) return false;

        String ownerStr = entity.getPersistentDataContainer().get(ownerKey, PersistentDataType.STRING);
        if (ownerStr == null || ownerStr.isEmpty()) return true;
        return ownerStr.equals(player.getUniqueId().toString());
    }

    private void setOwner(Display entity, UUID uuid) {
        entity.getPersistentDataContainer().set(ownerKey, PersistentDataType.STRING, uuid.toString());
    }

    private void sendSelectionUpdate(Player player, String uuidStr) {
        ByteBuffer buf = ByteBuffer.allocate(uuidStr.length() + 10);
        BufferUtil.writeString(buf, uuidStr);
        byte[] arr = new byte[buf.position()];
        buf.rewind();
        buf.get(arr);
        player.sendPluginMessage(plugin, "pivot:set_block_display", arr);
    }
}
