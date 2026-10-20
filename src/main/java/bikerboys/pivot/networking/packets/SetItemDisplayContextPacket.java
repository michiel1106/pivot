package bikerboys.pivot.networking.packets;

import static bikerboys.pivot.Pivot.MOD_ID;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record SetItemDisplayContextPacket(
        String uuid,
        String context
) implements CustomPacketPayload {
    public static final Identifier UPDATE_BLOCK_DISPLAY_ID = Identifier.fromNamespaceAndPath(MOD_ID, "set_item_display_context");
    public static final Type<SetItemDisplayContextPacket> ID = new Type<>(UPDATE_BLOCK_DISPLAY_ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, SetItemDisplayContextPacket> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, SetItemDisplayContextPacket::uuid,
                    ByteBufCodecs.STRING_UTF8, SetItemDisplayContextPacket::context,
                    SetItemDisplayContextPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
