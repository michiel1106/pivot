package bikerboys.pivot.networking.packets;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import static bikerboys.pivot.Pivot.MOD_ID;

public record AlignXYZ(String uuid, String coord) implements CustomPayload {
    public static final Identifier UPDATE_BLOCK_DISPLAY_ID = Identifier.of(MOD_ID, "align_x_y_z");
    public static final Id<AlignXYZ> ID = new Id<>(AlignXYZ.UPDATE_BLOCK_DISPLAY_ID);
    public static final PacketCodec<RegistryByteBuf, AlignXYZ> CODEC = PacketCodec.tuple(PacketCodecs.STRING, AlignXYZ::uuid, PacketCodecs.STRING, AlignXYZ::coord, AlignXYZ::new);



    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
