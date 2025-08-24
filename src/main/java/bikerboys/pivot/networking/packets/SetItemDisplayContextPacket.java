package bikerboys.pivot.networking.packets;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import static bikerboys.pivot.Pivot.MOD_ID;

public record SetItemDisplayContextPacket(
        String uuid,
        String context
) implements CustomPayload {
    public static final Identifier UPDATE_BLOCK_DISPLAY_ID = Identifier.of(MOD_ID, "set_item_display_context");
    public static final Id<SetItemDisplayContextPacket> ID = new Id<>(UPDATE_BLOCK_DISPLAY_ID);

    public static final PacketCodec<RegistryByteBuf, SetItemDisplayContextPacket> CODEC =
            PacketCodec.tuple(
                    PacketCodecs.STRING, SetItemDisplayContextPacket::uuid,
                    PacketCodecs.STRING, SetItemDisplayContextPacket::context,
                    SetItemDisplayContextPacket::new
            );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
