package bikerboys.pivot.networking.packets;

import io.netty.buffer.ByteBuf;
import net.fabricmc.loader.impl.lib.sat4j.core.Vec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.joml.Vector3f;

import static bikerboys.pivot.Pivot.MOD_ID;

public record UpdateBlockDisplayPos(String uuid, Vec3 pos) implements CustomPacketPayload {
    public static final Identifier UPDATE_BLOCK_DISPLAY_ID = Identifier.fromNamespaceAndPath(MOD_ID, "update_block_display_pos");
    public static final Type<UpdateBlockDisplayPos> ID = new Type<>(UpdateBlockDisplayPos.UPDATE_BLOCK_DISPLAY_ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateBlockDisplayPos> CODEC = StreamCodec.composite(ByteBufCodecs.STRING_UTF8, UpdateBlockDisplayPos::uuid, Vec3.STREAM_CODEC, UpdateBlockDisplayPos::pos, UpdateBlockDisplayPos::new);




    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
