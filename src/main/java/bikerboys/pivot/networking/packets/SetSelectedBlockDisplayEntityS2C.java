package bikerboys.pivot.networking.packets;

import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import static bikerboys.pivot.Pivot.MOD_ID;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record SetSelectedBlockDisplayEntityS2C(String uuid) implements CustomPacketPayload {
    public static final Identifier UPDATE_BLOCK_DISPLAY_ID = Identifier.fromNamespaceAndPath(MOD_ID, "set_block_display");
    public static final CustomPacketPayload.Type<SetSelectedBlockDisplayEntityS2C> ID = new CustomPacketPayload.Type<>(SetSelectedBlockDisplayEntityS2C.UPDATE_BLOCK_DISPLAY_ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, SetSelectedBlockDisplayEntityS2C> CODEC = StreamCodec.composite(ByteBufCodecs.STRING_UTF8, SetSelectedBlockDisplayEntityS2C::uuid, SetSelectedBlockDisplayEntityS2C::new);


    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
