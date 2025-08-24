package bikerboys.pivot.networking.packets;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import static bikerboys.pivot.Pivot.MOD_ID;

public record ChangeBlockDisplayState(String string, String blocktochange) implements CustomPayload {
    public static final Identifier UPDATE_BLOCK_DISPLAY_ID = Identifier.of(MOD_ID, "change_block_display_state");
    public static final Id<ChangeBlockDisplayState> ID = new Id<>(ChangeBlockDisplayState.UPDATE_BLOCK_DISPLAY_ID);
    public static final PacketCodec<RegistryByteBuf, ChangeBlockDisplayState> CODEC = PacketCodec.tuple(PacketCodecs.STRING, ChangeBlockDisplayState::string, PacketCodecs.STRING, ChangeBlockDisplayState::blocktochange, ChangeBlockDisplayState::new);


    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
