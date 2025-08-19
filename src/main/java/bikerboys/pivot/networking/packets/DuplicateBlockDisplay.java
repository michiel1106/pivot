package bikerboys.pivot.networking.packets;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import static bikerboys.pivot.Pivot.MOD_ID;

public record DuplicateBlockDisplay(String uuid) implements CustomPayload {
    public static final Identifier UPDATE_BLOCK_DISPLAY_ID = Identifier.of(MOD_ID, "duplicate_block_display");
    public static final Id<DuplicateBlockDisplay> ID = new Id<>(DuplicateBlockDisplay.UPDATE_BLOCK_DISPLAY_ID);
    public static final PacketCodec<RegistryByteBuf, DuplicateBlockDisplay> CODEC = PacketCodec.tuple(PacketCodecs.STRING, DuplicateBlockDisplay::uuid, DuplicateBlockDisplay::new);


    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
