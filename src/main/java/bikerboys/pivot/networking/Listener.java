package bikerboys.pivot.networking;

import bikerboys.pivot.networking.packets.*;
import bikerboys.pivot.util.Util;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.command.argument.ItemPredicateArgumentType;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.datafixer.fix.VillagerCanPickUpLootFix;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.property.Property;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.AffineTransformation;
import net.minecraft.util.math.Vec3d;
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
                entity = context.player().getWorld().getEntity(UUID.fromString(uuid));
            }


            if (entity != null) {


                if (entity instanceof DisplayEntity blockDisplayEntity) {

                    if (Util.getLockedDisplayEntity(blockDisplayEntity)) {
                        return;
                    }
                    if (!Util.getUuidFromDisplayEntity(blockDisplayEntity).equals(context.player().getUuidAsString()) && !Util.getUuidFromDisplayEntity(blockDisplayEntity).isEmpty()) {
                        if (!context.player().hasPermissionLevel(2)) {
                            return;
                        }
                    }

                    AffineTransformation transformation = DisplayEntity.getTransformation(blockDisplayEntity.getDataTracker());

                    // Example: reading parts of the transformation
                    Vector3f translation = transformation.getTranslation();
                    Quaternionf leftRot = transformation.getLeftRotation();
                    Vector3f scale = transformation.getScale();
                    Quaternionf rightRot = transformation.getRightRotation();


                    if (right_rotation != null) {
                        rightRot = right_rotation;
                    }
                    if (scale1 != null) {
                        scale = scale1;
                    }
                    if (translation1 != null) {
                        translation = translation1;
                    }


                    AffineTransformation newtransformation = new AffineTransformation(translation, leftRot, scale, rightRot);


                    blockDisplayEntity.setTransformation(newtransformation);


                }
            }

        }));


        ServerPlayNetworking.registerGlobalReceiver(AlignXYZ.ID, ((alignXYZ, context) -> {

            String uuid = alignXYZ.uuid();
            String coord = alignXYZ.coord();


            Entity entity = context.player().getWorld().getEntity(UUID.fromString(uuid));


            if (entity instanceof DisplayEntity blockDisplayEntity) {
                if (Util.getLockedDisplayEntity(blockDisplayEntity)) {
                    return;
                }
                if (!Util.getUuidFromDisplayEntity(blockDisplayEntity).equals(context.player().getUuidAsString()) && !Util.getUuidFromDisplayEntity(blockDisplayEntity).isEmpty()) {
                    if (!context.player().hasPermissionLevel(2)) {
                        return;
                    }
                }

                Vec3d pos = blockDisplayEntity.getPos();
                double x = pos.getX();
                double y = pos.getY();
                double z = pos.getZ();
                if (coord.equalsIgnoreCase("x")) {
                    x = Math.round(x);
                }
                if (coord.equalsIgnoreCase("y")) {
                    y = Math.round(y);
                }
                if (coord.equalsIgnoreCase("z")) {
                    z = Math.round(z);
                }
                blockDisplayEntity.setPos(x, y, z);
            }
        }));


        ServerPlayNetworking.registerGlobalReceiver(NewBlockDisplayEntity.ID, ((payload, context) -> {

            ServerPlayerEntity player = context.player();

            Item item = player.getMainHandStack().getItem();

            if (item == Items.AIR) return;

            if (item instanceof BlockItem item1) {
                Block block = item1.getBlock();
                DisplayEntity.BlockDisplayEntity blockDisplayEntity = new DisplayEntity.BlockDisplayEntity(EntityType.BLOCK_DISPLAY, context.player().getWorld());

                blockDisplayEntity.setBlockState(block.getDefaultState());
                blockDisplayEntity.setPos(player.getX(), player.getY(), player.getZ());



                if (player.getGameMode().isSurvivalLike()) {
                    player.getMainHandStack().decrement(1);
                }

                context.player().getWorld().spawnEntity(blockDisplayEntity);
                Util.setUnlockedDisplayEntity(blockDisplayEntity);
                Util.setUuidDisplayEntity(blockDisplayEntity, context.player());

                SetSelectedBlockDisplayEntityS2C setSelectedBlockDisplayEntityS2C = new SetSelectedBlockDisplayEntityS2C(blockDisplayEntity.getUuidAsString());

                ServerPlayNetworking.send(player, setSelectedBlockDisplayEntityS2C);
            }

            if (item == Items.FLINT_AND_STEEL) {
                DisplayEntity.BlockDisplayEntity blockDisplayEntity = new DisplayEntity.BlockDisplayEntity(EntityType.BLOCK_DISPLAY, context.player().getWorld());

                if (player.getGameMode().isSurvivalLike()) {
                    player.getMainHandStack().damage(1, player);
                }

                blockDisplayEntity.setBlockState(Blocks.FIRE.getDefaultState());
                blockDisplayEntity.setPos(player.getX(), player.getY(), player.getZ());

                context.player().getWorld().spawnEntity(blockDisplayEntity);
                Util.setUnlockedDisplayEntity(blockDisplayEntity);
                Util.setUuidDisplayEntity(blockDisplayEntity, context.player());

                SetSelectedBlockDisplayEntityS2C setSelectedBlockDisplayEntityS2C = new SetSelectedBlockDisplayEntityS2C(blockDisplayEntity.getUuidAsString());

                ServerPlayNetworking.send(player, setSelectedBlockDisplayEntityS2C);
            }



        }));

        ServerPlayNetworking.registerGlobalReceiver(NewItemDisplayEntity.ID, ((payload, context) -> {

            ServerPlayerEntity player = context.player();


            Item item = player.getMainHandStack().getItem();

            if (item == Items.AIR) return;



            DisplayEntity.ItemDisplayEntity itemDisplayEntity = new DisplayEntity.ItemDisplayEntity(EntityType.ITEM_DISPLAY, context.player().getWorld());



            itemDisplayEntity.setItemStack(new ItemStack(item));

            itemDisplayEntity.setPos(player.getX(), player.getY(), player.getZ());

            if (player.getGameMode().isSurvivalLike()) {
                player.getMainHandStack().decrement(1);
            }

            context.player().getWorld().spawnEntity(itemDisplayEntity);
            Util.setUnlockedDisplayEntity(itemDisplayEntity);
            Util.setUuidDisplayEntity(itemDisplayEntity, context.player());

            SetSelectedBlockDisplayEntityS2C setSelectedBlockDisplayEntityS2C = new SetSelectedBlockDisplayEntityS2C(itemDisplayEntity.getUuidAsString());

            ServerPlayNetworking.send(player, setSelectedBlockDisplayEntityS2C);


        }));


        ServerPlayNetworking.registerGlobalReceiver(TakebackBlockEntity.ID, ((payload, context) -> {
            String uuid = payload.uuid();

            Entity entity = context.player().getWorld().getEntity(UUID.fromString(uuid));

            if (entity == null) {return;}
            if (entity instanceof DisplayEntity.BlockDisplayEntity blockDisplayEntity) {
                if (Util.getLockedDisplayEntity(blockDisplayEntity)) {
                    return;
                }

                if (!Util.getUuidFromDisplayEntity(blockDisplayEntity).equals(context.player().getUuidAsString()) && !Util.getUuidFromDisplayEntity(blockDisplayEntity).isEmpty()) {
                    if (!context.player().hasPermissionLevel(2)) {
                        return;
                    }
                }

                BlockState blockState;

                if (Util.getOriginalBlockDisplayEntity(blockDisplayEntity).equalsIgnoreCase("")) {
                    blockState = blockDisplayEntity.getBlockState();
                } else {
                    Block blockfromid = Registries.BLOCK.get(Identifier.of(Util.getOriginalBlockDisplayEntity(blockDisplayEntity)));

                    if (blockfromid != Blocks.AIR) {
                        blockState = blockfromid.getDefaultState();
                    } else {
                        blockState = blockDisplayEntity.getBlockState();
                    }
                }

                Block block = blockState.getBlock();

                Item item = block.asItem();

                ServerPlayerEntity player = context.player();

                if (player.getGameMode().isSurvivalLike()) {
                    player.giveItemStack(new ItemStack(item));
                }

                entity.discard();
            } else if (entity instanceof DisplayEntity.ItemDisplayEntity itemDisplayEntity) {
                if (Util.getLockedDisplayEntity(itemDisplayEntity)) {
                    return;
                }

                if (!Util.getUuidFromDisplayEntity(itemDisplayEntity).equals(context.player().getUuidAsString()) && !Util.getUuidFromDisplayEntity(itemDisplayEntity).isEmpty()) {
                    if (!context.player().hasPermissionLevel(2)) {
                        return;
                    }
                }

                ItemStack itemStack = itemDisplayEntity.getItemStack();



                ServerPlayerEntity player = context.player();

                if (player.getGameMode().isSurvivalLike()) {
                    player.giveItemStack(itemStack);
                }

                entity.discard();
            }


        }));


        ServerPlayNetworking.registerGlobalReceiver(UpdateBlockDisplayPos.ID, (payload, context) -> {
            String uuid = payload.uuid();
            Entity entity = context.player().getWorld().getEntity(UUID.fromString(uuid));
            if (entity == null) {return;}
            if (entity instanceof DisplayEntity blockDisplayEntity) {

                if (Util.getLockedDisplayEntity(blockDisplayEntity)) {
                    return;
                }
                if (!Util.getUuidFromDisplayEntity(blockDisplayEntity).equals(context.player().getUuidAsString()) && !Util.getUuidFromDisplayEntity(blockDisplayEntity).isEmpty()) {
                    if (!context.player().hasPermissionLevel(2)) {
                        return;
                    }
                }

                blockDisplayEntity.setPos(payload.pos().x, payload.pos().y, payload.pos().z);
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(DuplicateBlockDisplay.ID, (payload, context) -> {
           String uuid = payload.uuid();
            Entity entity = context.player().getWorld().getEntity(UUID.fromString(uuid));
            ServerPlayerEntity player = context.player();
            ItemStack mainHandStack = player.getMainHandStack();


            if (entity == null) return;

            if (entity instanceof DisplayEntity.BlockDisplayEntity blockDisplayEntity) {
                if (Util.getLockedDisplayEntity(blockDisplayEntity)) {
                    return;
                }
                if (!Util.getUuidFromDisplayEntity(blockDisplayEntity).equals(context.player().getUuidAsString()) && !Util.getUuidFromDisplayEntity(blockDisplayEntity).isEmpty()) {
                    if (!context.player().hasPermissionLevel(2)) {
                        return;
                    }
                }

                Item item = blockDisplayEntity.getBlockState().getBlock().asItem();




                if (item == mainHandStack.getItem() || blockDisplayEntity.getBlockState().isOf(Blocks.FIRE) || blockDisplayEntity.getBlockState().isOf(Blocks.SOUL_FIRE)) {
                    DisplayEntity.BlockDisplayEntity duplicate = new DisplayEntity.BlockDisplayEntity(EntityType.BLOCK_DISPLAY, player.getWorld());

                    AffineTransformation transformation = DisplayEntity.getTransformation(blockDisplayEntity.getDataTracker());

                    duplicate.setBlockState(blockDisplayEntity.getBlockState());
                    duplicate.setTransformation(transformation);
                    duplicate.setPosition(blockDisplayEntity.getPos());

                    if (player.getGameMode().isSurvivalLike()) {
                        mainHandStack.decrement(1);
                    }

                    player.getWorld().spawnEntity(duplicate);
                    Util.setUnlockedDisplayEntity(duplicate);
                    Util.setUuidDisplayEntity(duplicate, context.player());

                } else {
                    player.sendMessage(Text.literal("You are not holding the block that you are trying to duplicate!"), true);
                }

            } else if (entity instanceof DisplayEntity.ItemDisplayEntity itemDisplayEntity) {
                if (Util.getLockedDisplayEntity(itemDisplayEntity)) {
                    return;
                }
                if (!Util.getUuidFromDisplayEntity(itemDisplayEntity).equals(context.player().getUuidAsString()) && !Util.getUuidFromDisplayEntity(itemDisplayEntity).isEmpty()) {
                    if (!context.player().hasPermissionLevel(2)) {
                        return;
                    }
                }

                Item item = itemDisplayEntity.getItemStack().getItem();

                if (item == mainHandStack.getItem()) {
                    DisplayEntity.ItemDisplayEntity duplicate = new DisplayEntity.ItemDisplayEntity(EntityType.ITEM_DISPLAY, player.getWorld());

                    AffineTransformation transformation = DisplayEntity.getTransformation(itemDisplayEntity.getDataTracker());

                    duplicate.setItemStack(new ItemStack(itemDisplayEntity.getItemStack().getItem()));
                    duplicate.setTransformation(transformation);
                    duplicate.setItemDisplayContext(itemDisplayEntity.getItemDisplayContext());
                    duplicate.setPosition(itemDisplayEntity.getPos());

                    if (player.getGameMode().isSurvivalLike()) {
                        mainHandStack.decrement(1);
                    }

                    player.getWorld().spawnEntity(duplicate);

                    Util.setUnlockedDisplayEntity(duplicate);
                    Util.setUuidDisplayEntity(duplicate, context.player());

                } else {
                    player.sendMessage(Text.literal("You are not holding the item that you are trying to duplicate!"), true);
                }

            }


        });


        ServerPlayNetworking.registerGlobalReceiver(LockDisplayEntity.ID, (payload, context) -> {
            String uuid = payload.uuid();
            Entity entity = context.player().getWorld().getEntity(UUID.fromString(uuid));


            if (entity == null) {return;}
            if (entity instanceof DisplayEntity blockDisplayEntity) {

                if (!Util.getUuidFromDisplayEntity(blockDisplayEntity).equals(context.player().getUuidAsString()) && !Util.getUuidFromDisplayEntity(blockDisplayEntity).isEmpty()) {


                    if (!context.player().hasPermissionLevel(2)) {
                        return;
                    }

                }

                boolean lockedDisplayEntity = Util.getLockedDisplayEntity(blockDisplayEntity);

                Util.setLockedDisplayEntity(blockDisplayEntity, !lockedDisplayEntity);

            }

        });








        ServerPlayNetworking.registerGlobalReceiver(SetBlockDisplayBlockstate.ID, (payload, context) -> {


                UUID uuid = UUID.fromString(payload.uuid());
                Entity entity = context.player().getWorld().getEntity(uuid);

                if (entity == null) {
                    return;
                }




                if (entity instanceof DisplayEntity.BlockDisplayEntity blockDisplay) {

                    if (Util.getLockedDisplayEntity(blockDisplay)) {
                        return;
                    }
                    if (!Util.getUuidFromDisplayEntity(blockDisplay).equals(context.player().getUuidAsString()) && !Util.getUuidFromDisplayEntity(blockDisplay).isEmpty()) {
                        if (!context.player().hasPermissionLevel(2)) {
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

            Entity entity = context.player().getWorld().getEntity(UUID.fromString(string));

            

            if (entity != null) {
                if (entity instanceof DisplayEntity.BlockDisplayEntity displayEntity) {

                    if (Util.getLockedDisplayEntity(displayEntity)) {
                        return;
                    }
                    if (!Util.getUuidFromDisplayEntity(displayEntity).equals(context.player().getUuidAsString()) && !Util.getUuidFromDisplayEntity(displayEntity).isEmpty()) {
                        if (!context.player().hasPermissionLevel(2)) {
                            return;
                        }
                    }


                    Block block = displayEntity.getBlockState().getBlock();

                    Identifier id = Registries.BLOCK.getId(block);

                    if (Util.getOriginalBlockDisplayEntity(displayEntity).equalsIgnoreCase("")) {
                        Util.setOriginalBlockDisplayEntity(displayEntity, id.toString());
                    }
                    boolean isDirt = displayEntity.getBlockState().isOf(Blocks.DIRT);
                    boolean isGrass = displayEntity.getBlockState().isOf(Blocks.GRASS_BLOCK);
                    boolean isFarmland = displayEntity.getBlockState().isOf(Blocks.FARMLAND);

                    boolean isBambooShoot = displayEntity.getBlockState().isOf(Blocks.BAMBOO_SAPLING);
                    boolean isBamboo = displayEntity.getBlockState().isOf(Blocks.BAMBOO);
                    boolean isPiston = displayEntity.getBlockState().isOf(Blocks.PISTON);
                    boolean isStickyPiston = displayEntity.getBlockState().isOf(Blocks.STICKY_PISTON);
                    boolean isPistonHead = displayEntity.getBlockState().isOf(Blocks.PISTON_HEAD);



                    if (isStickyPiston) {
                        if (whatchange.equalsIgnoreCase("nonsticky")) {
                            displayEntity.setBlockState(Blocks.PISTON.getDefaultState());
                            context.player().giveItemStack(new ItemStack(Items.SLIME_BALL));
                        }
                    }

                    if (isPiston) {
                        if (whatchange.equalsIgnoreCase("pistonhead")) {
                            displayEntity.setBlockState(Blocks.PISTON_HEAD.getDefaultState());
                        }
                        if (whatchange.equalsIgnoreCase("makesticky")) {
                            if (context.player().getInventory().contains(new ItemStack(Items.SLIME_BALL))) {

                                for (ItemStack stack : context.player().getInventory()) {
                                    if (stack.isOf(Items.SLIME_BALL)) {
                                        stack.decrement(1);
                                        break;
                                    }

                                }


                                displayEntity.setBlockState(Blocks.STICKY_PISTON.getDefaultState());
                            } else {
                                context.player().sendMessage(Text.literal("You currently dont have a slime ball in your inventory!"), true);
                            }

                        }
                    }


                    if (isPistonHead) {
                        if (whatchange.equalsIgnoreCase("piston")) {
                            displayEntity.setBlockState(Blocks.PISTON.getDefaultState());
                        }
                    }

                    if (isDirt) {
                        if (whatchange.equalsIgnoreCase("grass")) {
                            displayEntity.setBlockState(Blocks.GRASS_BLOCK.getDefaultState());
                        }
                        if (whatchange.equalsIgnoreCase("farmland")) {
                            displayEntity.setBlockState(Blocks.FARMLAND.getDefaultState());
                        }
                    }

                    if (isGrass) {
                        if (whatchange.equalsIgnoreCase("dirt")) {
                            displayEntity.setBlockState(Blocks.DIRT.getDefaultState());
                        }
                        if (whatchange.equalsIgnoreCase("farmland")) {
                            displayEntity.setBlockState(Blocks.FARMLAND.getDefaultState());
                        }
                    }

                    if (isFarmland) {
                        if (whatchange.equalsIgnoreCase("dirt")) {
                            displayEntity.setBlockState(Blocks.DIRT.getDefaultState());
                        }
                        if (whatchange.equalsIgnoreCase("grass")) {
                            displayEntity.setBlockState(Blocks.GRASS_BLOCK.getDefaultState());
                        }
                    }
                    if (isBambooShoot) {
                        if (whatchange.equalsIgnoreCase("bamboo")) {
                            displayEntity.setBlockState(Blocks.BAMBOO.getDefaultState());
                        }
                    }

                    if (isBamboo) {
                        if (whatchange.equalsIgnoreCase("bambooshoot")) {
                            displayEntity.setBlockState(Blocks.BAMBOO_SAPLING.getDefaultState());
                        }
                    }
                }
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(SetItemDisplayContextPacket.ID, (payload, context) -> {
                    String uuidString = payload.uuid();
                    String contextName = payload.context();


                        Entity entity = context.player().getWorld().getEntity(UUID.fromString(uuidString));

                        if (entity == null) return;
                        if (entity instanceof DisplayEntity.ItemDisplayEntity itemDisplay) {


                            if (Util.getLockedDisplayEntity(itemDisplay)) {
                                return;
                            }
                            if (!Util.getUuidFromDisplayEntity(itemDisplay).equals(context.player().getUuidAsString()) && !Util.getUuidFromDisplayEntity(itemDisplay).isEmpty()) {
                                if (!context.player().hasPermissionLevel(2)) {
                                    return;
                                }
                            }

                            ItemDisplayContext itemDisplayContext = Arrays.stream(ItemDisplayContext.values())
                                    .filter(c -> c.asString().equals(contextName))
                                    .findFirst()
                                    .orElse(ItemDisplayContext.NONE);

                            itemDisplay.setItemDisplayContext(itemDisplayContext);
                        }

                }
        );



    }


    @SuppressWarnings({"unchecked", "rawtypes"})
    private static BlockState withParsedValue(BlockState state, Property property, String value) {
        Optional<?> parsed = property.parse(value);
        if (parsed.isPresent()) {
            return state.with(property, (Comparable) parsed.get());
        } else {
        }
        return state;
    }

}
