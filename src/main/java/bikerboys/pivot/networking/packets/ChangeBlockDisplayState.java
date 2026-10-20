package bikerboys.pivot.networking.packets;

import static bikerboys.pivot.Pivot.MOD_ID;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ChangeBlockDisplayState(String string, String blocktochange) implements CustomPacketPayload {
    public static final Identifier UPDATE_BLOCK_DISPLAY_ID = Identifier.fromNamespaceAndPath(MOD_ID, "change_block_display_state");
    public static final Type<ChangeBlockDisplayState> ID = new Type<>(ChangeBlockDisplayState.UPDATE_BLOCK_DISPLAY_ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, ChangeBlockDisplayState> CODEC = StreamCodec.composite(ByteBufCodecs.STRING_UTF8, ChangeBlockDisplayState::string, ByteBufCodecs.STRING_UTF8, ChangeBlockDisplayState::blocktochange, ChangeBlockDisplayState::new);


    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
