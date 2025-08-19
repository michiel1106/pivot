package bikerboys.pivot.networking.packets;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import static bikerboys.pivot.Pivot.MOD_ID;

public record SetSelectedBlockDisplayEntityS2C(String uuid) implements CustomPayload {
    public static final Identifier UPDATE_BLOCK_DISPLAY_ID = Identifier.of(MOD_ID, "set_block_display");
    public static final CustomPayload.Id<SetSelectedBlockDisplayEntityS2C> ID = new CustomPayload.Id<>(SetSelectedBlockDisplayEntityS2C.UPDATE_BLOCK_DISPLAY_ID);
    public static final PacketCodec<RegistryByteBuf, SetSelectedBlockDisplayEntityS2C> CODEC = PacketCodec.tuple(PacketCodecs.STRING, SetSelectedBlockDisplayEntityS2C::uuid, SetSelectedBlockDisplayEntityS2C::new);


    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
