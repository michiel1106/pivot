package bikerboys.pivot.networking.packets;

import bikerboys.pivot.Pivot;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaterniond;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Optional;

import static bikerboys.pivot.Pivot.MOD_ID;

public record UpdateBlockDisplayPacketC2S(String uuid, @Nullable Vector3f translation, @Nullable Quaternionf right_rotation, @Nullable Vector3f scale) implements CustomPayload {
    public static final Identifier UPDATE_BLOCK_DISPLAY_ID = Identifier.of(MOD_ID, "update_block_display");
    public static final CustomPayload.Id<UpdateBlockDisplayPacketC2S> ID = new CustomPayload.Id<>(UpdateBlockDisplayPacketC2S.UPDATE_BLOCK_DISPLAY_ID);
    public static final PacketCodec<RegistryByteBuf, UpdateBlockDisplayPacketC2S> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, UpdateBlockDisplayPacketC2S::uuid,
            PacketCodecs.optional(PacketCodecs.VECTOR_3F), pkt -> Optional.ofNullable(pkt.translation()),
            PacketCodecs.optional(PacketCodecs.QUATERNION_F), pkt -> Optional.ofNullable(pkt.right_rotation()),
            PacketCodecs.optional(PacketCodecs.VECTOR_3F), pkt -> Optional.ofNullable(pkt.scale()),
            (uuid, optPos, optRot, optScale) -> new UpdateBlockDisplayPacketC2S(
                    uuid,
                    optPos.orElse(null),
                    optRot.orElse(null),
                    optScale.orElse(null)
            )
    );


    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
