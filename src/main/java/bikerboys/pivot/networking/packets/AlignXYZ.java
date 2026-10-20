package bikerboys.pivot.networking.packets;

import static bikerboys.pivot.Pivot.MOD_ID;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record AlignXYZ(String uuid, String coord) implements CustomPacketPayload {
    public static final Identifier UPDATE_BLOCK_DISPLAY_ID = Identifier.fromNamespaceAndPath(MOD_ID, "align_x_y_z");
    public static final Type<AlignXYZ> ID = new Type<>(AlignXYZ.UPDATE_BLOCK_DISPLAY_ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, AlignXYZ> CODEC = StreamCodec.composite(ByteBufCodecs.STRING_UTF8, AlignXYZ::uuid, ByteBufCodecs.STRING_UTF8, AlignXYZ::coord, AlignXYZ::new);



    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
