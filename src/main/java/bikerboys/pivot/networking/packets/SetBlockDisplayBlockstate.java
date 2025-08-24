package bikerboys.pivot.networking.packets;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import static bikerboys.pivot.Pivot.MOD_ID;

public record SetBlockDisplayBlockstate(
        String uuid,
        String propertyName,
        String value
) implements CustomPayload {
    public static final Identifier UPDATE_BLOCK_DISPLAY_ID = Identifier.of(MOD_ID, "set_block_display_blockstate");
    public static final Id<SetBlockDisplayBlockstate> ID = new Id<>(UPDATE_BLOCK_DISPLAY_ID);

    public static final PacketCodec<RegistryByteBuf, SetBlockDisplayBlockstate> CODEC =
            PacketCodec.tuple(
                    PacketCodecs.STRING, SetBlockDisplayBlockstate::uuid,
                    PacketCodecs.STRING, SetBlockDisplayBlockstate::propertyName,
                    PacketCodecs.STRING, SetBlockDisplayBlockstate::value,
                    SetBlockDisplayBlockstate::new
            );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
