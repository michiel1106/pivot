package bikerboys.pivot.networking.packets;

import io.netty.buffer.ByteBuf;
import net.fabricmc.loader.impl.lib.sat4j.core.Vec;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3d;
import org.joml.Vector3f;

import static bikerboys.pivot.Pivot.MOD_ID;

public record UpdateBlockDisplayPos(String uuid, Vec3d pos) implements CustomPayload {
    public static final Identifier UPDATE_BLOCK_DISPLAY_ID = Identifier.of(MOD_ID, "update_block_display_pos");
    public static final Id<UpdateBlockDisplayPos> ID = new Id<>(UpdateBlockDisplayPos.UPDATE_BLOCK_DISPLAY_ID);
    public static final PacketCodec<RegistryByteBuf, UpdateBlockDisplayPos> CODEC = PacketCodec.tuple(PacketCodecs.STRING, UpdateBlockDisplayPos::uuid, Vec3d.PACKET_CODEC, UpdateBlockDisplayPos::pos, UpdateBlockDisplayPos::new);




    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
