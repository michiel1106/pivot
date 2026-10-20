package bikerboys.pivot.networking.packets;

import static bikerboys.pivot.Pivot.MOD_ID;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record SetBlockDisplayBlockstate(
        String uuid,
        String propertyName,
        String value
) implements CustomPacketPayload {
    public static final Identifier UPDATE_BLOCK_DISPLAY_ID = Identifier.fromNamespaceAndPath(MOD_ID, "set_block_display_blockstate");
    public static final Type<SetBlockDisplayBlockstate> ID = new Type<>(UPDATE_BLOCK_DISPLAY_ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, SetBlockDisplayBlockstate> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, SetBlockDisplayBlockstate::uuid,
                    ByteBufCodecs.STRING_UTF8, SetBlockDisplayBlockstate::propertyName,
                    ByteBufCodecs.STRING_UTF8, SetBlockDisplayBlockstate::value,
                    SetBlockDisplayBlockstate::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
