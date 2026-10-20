package bikerboys.pivot.networking;

import net.minecraft.world.entity.EntityTypes;
import net.minecraft.server.permissions.Permissions;
import bikerboys.pivot.networking.packets.*;
import bikerboys.pivot.util.Util;
import com.mojang.math.Transformation;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

public class Listener {

    public static void registerListeners() {
        ServerPlayNetworking.registerGlobalReceiver(UpdateBlockDisplayPacketC2S.ID, ((payload, context) -> {

            @Nullable String uuid = payload.uuid();

            @Nullable Vector3f translation1 = payload.translation();

            @Nullable Quaternionf right_rotation = payload.right_rotation();

            @Nullable Vector3f scale1 = payload.scale();

            
            Entity entity = null;

            if (uuid != null) {
                entity = context.player().level().getEntity(UUID.fromString(uuid));
            }


            if (entity != null) {


                if (entity instanceof Display blockDisplayEntity) {

                    if (Util.getLockedDisplayEntity(blockDisplayEntity)) {
                        return;
                    }
                    if (!Util.getUuidFromDisplayEntity(blockDisplayEntity).equals(context.player().getStringUUID()) && !Util.getUuidFromDisplayEntity(blockDisplayEntity).isEmpty()) {
                        if (!context.player().permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)) {
                            return;
                        }
                    }

                    Transformation transformation = Display.createTransformation(blockDisplayEntity.getEntityData());

                    // Example: reading parts of the transformation
                    Vector3f translation = new Vector3f(transformation.translation());
                    Quaternionf leftRot = new Quaternionf(transformation.leftRotation());
                    Vector3f scale = new Vector3f(transformation.scale());
                    Quaternionf rightRot = new Quaternionf(transformation.rightRotation());


                    if (right_rotation != null) {
                        rightRot = right_rotation;
                    }
                    if (scale1 != null) {
                        scale = scale1;
                    }
                    if (translation1 != null) {
                        translation = translation1;
                    }


                    Transformation newtransformation = new Transformation(translation, leftRot, scale, rightRot);


                    blockDisplayEntity.setTransformation(newtransformation);


                }
            }

        }));


        ServerPlayNetworking.registerGlobalReceiver(AlignXYZ.ID, ((alignXYZ, context) -> {

            String uuid = alignXYZ.uuid();
            String coord = alignXYZ.coord();


            Entity entity = context.player().level().getEntity(UUID.fromString(uuid));


            if (entity instanceof Display blockDisplayEntity) {
                if (Util.getLockedDisplayEntity(blockDisplayEntity)) {
                    return;
                }
                if (!Util.getUuidFromDisplayEntity(blockDisplayEntity).equals(context.player().getStringUUID()) && !Util.getUuidFromDisplayEntity(blockDisplayEntity).isEmpty()) {
                    if (!context.player().permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)) {
                        return;
                    }
                }

                Vec3 pos = blockDisplayEntity.position();
                double x = pos.x();
                double y = pos.y();
                double z = pos.z();
                if (coord.equalsIgnoreCase("x")) {
                    x = Math.round(x);
                }
                if (coord.equalsIgnoreCase("y")) {
                    y = Math.round(y);
                }
                if (coord.equalsIgnoreCase("z")) {
                    z = Math.round(z);
                }
                blockDisplayEntity.setPosRaw(x, y, z);
            }
        }));


        ServerPlayNetworking.registerGlobalReceiver(NewBlockDisplayEntity.ID, ((payload, context) -> {

            ServerPlayer player = context.player();

            Item item = player.getMainHandItem().getItem();

            if (item == Items.AIR) return;

            if (item instanceof BlockItem item1) {
                Block block = item1.getBlock();
                Display.BlockDisplay blockDisplayEntity = new Display.BlockDisplay(EntityTypes.BLOCK_DISPLAY, context.player().level());

                blockDisplayEntity.setBlockState(block.defaultBlockState());
                blockDisplayEntity.setPosRaw(player.getX(), player.getY(), player.getZ());



                if (player.gameMode().isSurvival()) {
                    player.getMainHandItem().shrink(1);
                }

                context.player().level().addFreshEntity(blockDisplayEntity);
                Util.setUnlockedDisplayEntity(blockDisplayEntity);
                Util.setUuidDisplayEntity(blockDisplayEntity, context.player());

                SetSelectedBlockDisplayEntityS2C setSelectedBlockDisplayEntityS2C = new SetSelectedBlockDisplayEntityS2C(blockDisplayEntity.getStringUUID());

                ServerPlayNetworking.send(player, setSelectedBlockDisplayEntityS2C);
            }

            if (item == Items.FLINT_AND_STEEL) {
                Display.BlockDisplay blockDisplayEntity = new Display.BlockDisplay(EntityTypes.BLOCK_DISPLAY, context.player().level());

                if (player.gameMode().isSurvival()) {
                    player.getMainHandItem().hurtWithoutBreaking(1, player);
                }

                blockDisplayEntity.setBlockState(Blocks.FIRE.defaultBlockState());
                blockDisplayEntity.setPosRaw(player.getX(), player.getY(), player.getZ());

                context.player().level().addFreshEntity(blockDisplayEntity);
                Util.setUnlockedDisplayEntity(blockDisplayEntity);
                Util.setUuidDisplayEntity(blockDisplayEntity, context.player());

                SetSelectedBlockDisplayEntityS2C setSelectedBlockDisplayEntityS2C = new SetSelectedBlockDisplayEntityS2C(blockDisplayEntity.getStringUUID());

                ServerPlayNetworking.send(player, setSelectedBlockDisplayEntityS2C);
            }



        }));

        ServerPlayNetworking.registerGlobalReceiver(NewItemDisplayEntity.ID, ((payload, context) -> {

            ServerPlayer player = context.player();


            Item item = player.getMainHandItem().getItem();

            if (item == Items.AIR) return;



            Display.ItemDisplay itemDisplayEntity = new Display.ItemDisplay(EntityTypes.ITEM_DISPLAY, context.player().level());



            itemDisplayEntity.setItemStack(new ItemStack(item));

            itemDisplayEntity.setPosRaw(player.getX(), player.getY(), player.getZ());

            if (player.gameMode().isSurvival()) {
                player.getMainHandItem().shrink(1);
            }

            context.player().level().addFreshEntity(itemDisplayEntity);
            Util.setUnlockedDisplayEntity(itemDisplayEntity);
            Util.setUuidDisplayEntity(itemDisplayEntity, context.player());

            SetSelectedBlockDisplayEntityS2C setSelectedBlockDisplayEntityS2C = new SetSelectedBlockDisplayEntityS2C(itemDisplayEntity.getStringUUID());

            ServerPlayNetworking.send(player, setSelectedBlockDisplayEntityS2C);


        }));


        ServerPlayNetworking.registerGlobalReceiver(TakebackBlockEntity.ID, ((payload, context) -> {
            String uuid = payload.uuid();

            Entity entity = context.player().level().getEntity(UUID.fromString(uuid));

            if (entity == null) {return;}
            if (entity instanceof Display.BlockDisplay blockDisplayEntity) {
                if (Util.getLockedDisplayEntity(blockDisplayEntity)) {
                    return;
                }

                if (!Util.getUuidFromDisplayEntity(blockDisplayEntity).equals(context.player().getStringUUID()) && !Util.getUuidFromDisplayEntity(blockDisplayEntity).isEmpty()) {
                    if (!context.player().permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)) {
                        return;
                    }
                }

                BlockState blockState;

                if (Util.getOriginalBlockDisplayEntity(blockDisplayEntity).equalsIgnoreCase("")) {
                    blockState = blockDisplayEntity.getBlockState();
                } else {
                    Block blockfromid = BuiltInRegistries.BLOCK.getValue(Identifier.parse(Util.getOriginalBlockDisplayEntity(blockDisplayEntity)));

                    if (blockfromid != Blocks.AIR) {
                        blockState = blockfromid.defaultBlockState();
                    } else {
                        blockState = blockDisplayEntity.getBlockState();
                    }
                }

                Block block = blockState.getBlock();

                Item item = block.asItem();

                ServerPlayer player = context.player();

                if (player.gameMode().isSurvival()) {
                    player.addItem(new ItemStack(item));
                }

                entity.discard();
            } else if (entity instanceof Display.ItemDisplay itemDisplayEntity) {
                if (Util.getLockedDisplayEntity(itemDisplayEntity)) {
                    return;
                }

                if (!Util.getUuidFromDisplayEntity(itemDisplayEntity).equals(context.player().getStringUUID()) && !Util.getUuidFromDisplayEntity(itemDisplayEntity).isEmpty()) {
                    if (!context.player().permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)) {
                        return;
                    }
                }

                ItemStack itemStack = itemDisplayEntity.getItemStack();



                ServerPlayer player = context.player();

                if (player.gameMode().isSurvival()) {
                    player.addItem(itemStack);
                }

                entity.discard();
            }


        }));


        ServerPlayNetworking.registerGlobalReceiver(UpdateBlockDisplayPos.ID, (payload, context) -> {
            String uuid = payload.uuid();
            Entity entity = context.player().level().getEntity(UUID.fromString(uuid));
            if (entity == null) {return;}
            if (entity instanceof Display blockDisplayEntity) {

                if (Util.getLockedDisplayEntity(blockDisplayEntity)) {
                    return;
                }
                if (!Util.getUuidFromDisplayEntity(blockDisplayEntity).equals(context.player().getStringUUID()) && !Util.getUuidFromDisplayEntity(blockDisplayEntity).isEmpty()) {
                    if (!context.player().permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)) {
                        return;
                    }
                }

                blockDisplayEntity.setPosRaw(payload.pos().x, payload.pos().y, payload.pos().z);
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(DuplicateBlockDisplay.ID, (payload, context) -> {
           String uuid = payload.uuid();
            Entity entity = context.player().level().getEntity(UUID.fromString(uuid));
            ServerPlayer player = context.player();
            ItemStack mainHandStack = player.getMainHandItem();


            if (entity == null) return;

            if (entity instanceof Display.BlockDisplay blockDisplayEntity) {
                if (Util.getLockedDisplayEntity(blockDisplayEntity)) {
                    return;
                }
                if (!Util.getUuidFromDisplayEntity(blockDisplayEntity).equals(context.player().getStringUUID()) && !Util.getUuidFromDisplayEntity(blockDisplayEntity).isEmpty()) {
                    if (!context.player().permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)) {
                        return;
                    }
                }

                Item item = blockDisplayEntity.getBlockState().getBlock().asItem();




                if (item == mainHandStack.getItem() || blockDisplayEntity.getBlockState().is(Blocks.FIRE) || blockDisplayEntity.getBlockState().is(Blocks.SOUL_FIRE)) {
                    Display.BlockDisplay duplicate = new Display.BlockDisplay(EntityTypes.BLOCK_DISPLAY, player.level());

                    Transformation transformation = Display.createTransformation(blockDisplayEntity.getEntityData());

                    duplicate.setBlockState(blockDisplayEntity.getBlockState());
                    duplicate.setTransformation(transformation);
                    duplicate.setPos(blockDisplayEntity.position());

                    if (player.gameMode().isSurvival()) {
                        mainHandStack.shrink(1);
                    }

                    player.level().addFreshEntity(duplicate);
                    Util.setUnlockedDisplayEntity(duplicate);
                    Util.setUuidDisplayEntity(duplicate, context.player());

                } else {
                    player.sendOverlayMessage(Component.literal("You are not holding the block that you are trying to duplicate!"));
                }

            } else if (entity instanceof Display.ItemDisplay itemDisplayEntity) {
                if (Util.getLockedDisplayEntity(itemDisplayEntity)) {
                    return;
                }
                if (!Util.getUuidFromDisplayEntity(itemDisplayEntity).equals(context.player().getStringUUID()) && !Util.getUuidFromDisplayEntity(itemDisplayEntity).isEmpty()) {
                    if (!context.player().permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)) {
                        return;
                    }
                }

                Item item = itemDisplayEntity.getItemStack().getItem();

                if (item == mainHandStack.getItem()) {
                    Display.ItemDisplay duplicate = new Display.ItemDisplay(EntityTypes.ITEM_DISPLAY, player.level());

                    Transformation transformation = Display.createTransformation(itemDisplayEntity.getEntityData());

                    duplicate.setItemStack(new ItemStack(itemDisplayEntity.getItemStack().getItem()));
                    duplicate.setTransformation(transformation);
                    duplicate.setItemTransform(itemDisplayEntity.getItemTransform());
                    duplicate.setPos(itemDisplayEntity.position());

                    if (player.gameMode().isSurvival()) {
                        mainHandStack.shrink(1);
                    }

                    player.level().addFreshEntity(duplicate);

                    Util.setUnlockedDisplayEntity(duplicate);
                    Util.setUuidDisplayEntity(duplicate, context.player());

                } else {
                    player.sendOverlayMessage(Component.literal("You are not holding the item that you are trying to duplicate!"));
                }

            }


        });


        ServerPlayNetworking.registerGlobalReceiver(LockDisplayEntity.ID, (payload, context) -> {
            String uuid = payload.uuid();
            Entity entity = context.player().level().getEntity(UUID.fromString(uuid));


            if (entity == null) {return;}
            if (entity instanceof Display blockDisplayEntity) {

                if (!Util.getUuidFromDisplayEntity(blockDisplayEntity).equals(context.player().getStringUUID()) && !Util.getUuidFromDisplayEntity(blockDisplayEntity).isEmpty()) {


                    if (!context.player().permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)) {
                        return;
                    }

                }

                boolean lockedDisplayEntity = Util.getLockedDisplayEntity(blockDisplayEntity);

                Util.setLockedDisplayEntity(blockDisplayEntity, !lockedDisplayEntity);

            }

        });








        ServerPlayNetworking.registerGlobalReceiver(SetBlockDisplayBlockstate.ID, (payload, context) -> {


                UUID uuid = UUID.fromString(payload.uuid());
                Entity entity = context.player().level().getEntity(uuid);

                if (entity == null) {
                    return;
                }




                if (entity instanceof Display.BlockDisplay blockDisplay) {

                    if (Util.getLockedDisplayEntity(blockDisplay)) {
                        return;
                    }
                    if (!Util.getUuidFromDisplayEntity(blockDisplay).equals(context.player().getStringUUID()) && !Util.getUuidFromDisplayEntity(blockDisplay).isEmpty()) {
                        if (!context.player().permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)) {
                            return;
                        }
                    }

                    BlockState state = blockDisplay.getBlockState();

                    for (Property<?> property : state.getProperties()) {
                        if (property.getName().equals(payload.propertyName())) {
                            BlockState newState = withParsedValue(state, property, payload.value());

                            blockDisplay.setBlockState(newState);
                            break;
                        }
                    }
                }

        });


        ServerPlayNetworking.registerGlobalReceiver(ChangeBlockDisplayState.ID, (payload, context) -> {


            String string = payload.string();
            String whatchange = payload.blocktochange();

            Entity entity = context.player().level().getEntity(UUID.fromString(string));

            

            if (entity != null) {
                if (entity instanceof Display.BlockDisplay displayEntity) {

                    if (Util.getLockedDisplayEntity(displayEntity)) {
                        return;
                    }
                    if (!Util.getUuidFromDisplayEntity(displayEntity).equals(context.player().getStringUUID()) && !Util.getUuidFromDisplayEntity(displayEntity).isEmpty()) {
                        if (!context.player().permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)) {
                            return;
                        }
                    }


                    Block block = displayEntity.getBlockState().getBlock();

                    Identifier id = BuiltInRegistries.BLOCK.getKey(block);

                    if (Util.getOriginalBlockDisplayEntity(displayEntity).equalsIgnoreCase("")) {
                        Util.setOriginalBlockDisplayEntity(displayEntity, id.toString());
                    }
                    boolean isDirt = displayEntity.getBlockState().is(Blocks.DIRT);
                    boolean isGrass = displayEntity.getBlockState().is(Blocks.GRASS_BLOCK);
                    boolean isFarmland = displayEntity.getBlockState().is(Blocks.FARMLAND);

                    boolean isBambooShoot = displayEntity.getBlockState().is(Blocks.BAMBOO_SAPLING);
                    boolean isBamboo = displayEntity.getBlockState().is(Blocks.BAMBOO);
                    boolean isPiston = displayEntity.getBlockState().is(Blocks.PISTON);
                    boolean isStickyPiston = displayEntity.getBlockState().is(Blocks.STICKY_PISTON);
                    boolean isPistonHead = displayEntity.getBlockState().is(Blocks.PISTON_HEAD);



                    if (isStickyPiston) {
                        if (whatchange.equalsIgnoreCase("nonsticky")) {
                            displayEntity.setBlockState(Blocks.PISTON.defaultBlockState());
                            context.player().addItem(new ItemStack(Items.SLIME_BALL));
                        }
                    }

                    if (isPiston) {
                        if (whatchange.equalsIgnoreCase("pistonhead")) {
                            displayEntity.setBlockState(Blocks.PISTON_HEAD.defaultBlockState());
                        }
                        if (whatchange.equalsIgnoreCase("makesticky")) {
                            if (context.player().getInventory().contains(new ItemStack(Items.SLIME_BALL))) {

                                for (ItemStack stack : context.player().getInventory()) {
                                    if (stack.is(Items.SLIME_BALL)) {
                                        stack.shrink(1);
                                        break;
                                    }

                                }


                                displayEntity.setBlockState(Blocks.STICKY_PISTON.defaultBlockState());
                            } else {
                                context.player().sendOverlayMessage(Component.literal("You currently dont have a slime ball in your inventory!"));
                            }

                        }
                    }


                    if (isPistonHead) {
                        if (whatchange.equalsIgnoreCase("piston")) {
                            displayEntity.setBlockState(Blocks.PISTON.defaultBlockState());
                        }
                    }

                    if (isDirt) {
                        if (whatchange.equalsIgnoreCase("grass")) {
                            displayEntity.setBlockState(Blocks.GRASS_BLOCK.defaultBlockState());
                        }
                        if (whatchange.equalsIgnoreCase("farmland")) {
                            displayEntity.setBlockState(Blocks.FARMLAND.defaultBlockState());
                        }
                    }

                    if (isGrass) {
                        if (whatchange.equalsIgnoreCase("dirt")) {
                            displayEntity.setBlockState(Blocks.DIRT.defaultBlockState());
                        }
                        if (whatchange.equalsIgnoreCase("farmland")) {
                            displayEntity.setBlockState(Blocks.FARMLAND.defaultBlockState());
                        }
                    }

                    if (isFarmland) {
                        if (whatchange.equalsIgnoreCase("dirt")) {
                            displayEntity.setBlockState(Blocks.DIRT.defaultBlockState());
                        }
                        if (whatchange.equalsIgnoreCase("grass")) {
                            displayEntity.setBlockState(Blocks.GRASS_BLOCK.defaultBlockState());
                        }
                    }
                    if (isBambooShoot) {
                        if (whatchange.equalsIgnoreCase("bamboo")) {
                            displayEntity.setBlockState(Blocks.BAMBOO.defaultBlockState());
                        }
                    }

                    if (isBamboo) {
                        if (whatchange.equalsIgnoreCase("bambooshoot")) {
                            displayEntity.setBlockState(Blocks.BAMBOO_SAPLING.defaultBlockState());
                        }
                    }
                }
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(SetItemDisplayContextPacket.ID, (payload, context) -> {
                    String uuidString = payload.uuid();
                    String contextName = payload.context();


                        Entity entity = context.player().level().getEntity(UUID.fromString(uuidString));

                        if (entity == null) return;
                        if (entity instanceof Display.ItemDisplay itemDisplay) {


                            if (Util.getLockedDisplayEntity(itemDisplay)) {
                                return;
                            }
                            if (!Util.getUuidFromDisplayEntity(itemDisplay).equals(context.player().getStringUUID()) && !Util.getUuidFromDisplayEntity(itemDisplay).isEmpty()) {
                                if (!context.player().permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)) {
                                    return;
                                }
                            }

                            ItemDisplayContext itemDisplayContext = Arrays.stream(ItemDisplayContext.values())
                                    .filter(c -> c.getSerializedName().equals(contextName))
                                    .findFirst()
                                    .orElse(ItemDisplayContext.NONE);

                            itemDisplay.setItemTransform(itemDisplayContext);
                        }

                }
        );



    }


    @SuppressWarnings({"unchecked", "rawtypes"})
    private static BlockState withParsedValue(BlockState state, Property property, String value) {
        Optional<?> parsed = property.getValue(value);
        if (parsed.isPresent()) {
            return state.setValue(property, (Comparable) parsed.get());
        } else {
        }
        return state;
    }

}
