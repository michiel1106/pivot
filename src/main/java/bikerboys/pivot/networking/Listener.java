package bikerboys.pivot.networking;

import bikerboys.pivot.networking.packets.*;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.AffineTransformation;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

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
                if (entity instanceof DisplayEntity.BlockDisplayEntity blockDisplayEntity) {
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


            if (entity instanceof DisplayEntity.BlockDisplayEntity blockDisplayEntity) {
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

            Vec3d pos = player.getPos();
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
            }


        }));


        ServerPlayNetworking.registerGlobalReceiver(TakebackBlockEntity.ID, ((payload, context) -> {
            String uuid = payload.uuid();

            Entity entity = context.player().getWorld().getEntity(UUID.fromString(uuid));

            if (entity == null) {return;}
            if (entity instanceof DisplayEntity.BlockDisplayEntity blockDisplayEntity) {
                BlockState blockState = blockDisplayEntity.getBlockState();

                Block block = blockState.getBlock();

                Item item = block.asItem();

                ServerPlayerEntity player = context.player();

                if (player.getGameMode().isSurvivalLike()) {
                    player.giveItemStack(new ItemStack(item));
                }

                entity.discard();

            }
        }));


        ServerPlayNetworking.registerGlobalReceiver(UpdateBlockDisplayPos.ID, (payload, context) -> {
            String uuid = payload.uuid();
            Entity entity = context.player().getWorld().getEntity(UUID.fromString(uuid));
            if (entity == null) {return;}
            if (entity instanceof DisplayEntity.BlockDisplayEntity blockDisplayEntity) {
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
                Item item = blockDisplayEntity.getBlockState().getBlock().asItem();

                if (item == mainHandStack.getItem()) {
                    DisplayEntity.BlockDisplayEntity duplicate = new DisplayEntity.BlockDisplayEntity(EntityType.BLOCK_DISPLAY, player.getWorld());

                    AffineTransformation transformation = DisplayEntity.getTransformation(blockDisplayEntity.getDataTracker());

                    duplicate.setBlockState(blockDisplayEntity.getBlockState());
                    duplicate.setTransformation(transformation);
                    duplicate.setPosition(blockDisplayEntity.getPos());

                    if (player.getGameMode().isSurvivalLike()) {
                        mainHandStack.decrement(1);
                    }

                    player.getWorld().spawnEntity(duplicate);

                } else {
                    player.sendMessage(Text.literal("You are not holding the block display that you are trying to duplicate!"), true);
                }

            }


        });


    }

}
