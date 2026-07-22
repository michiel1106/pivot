package bikerboys.pivot.networking.packets;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import static bikerboys.pivot.Pivot.MOD_ID;

public record PivotAuthPacketS2C(boolean authorized) implements CustomPayload {
    public static final Identifier AUTH_ID = Identifier.of(MOD_ID, "auth");
    public static final CustomPayload.Id<PivotAuthPacketS2C> ID = new CustomPayload.Id<>(AUTH_ID);
    
    // The server just sends a byte array of length 1: new byte[]{1} for true, 0 for false.
    // PacketCodecs.BOOL works similarly, but we use a custom codec since we wrote bytes manually in the spigot plugin.
    public static final PacketCodec<RegistryByteBuf, PivotAuthPacketS2C> CODEC = PacketCodec.tuple(
            PacketCodecs.BYTE.xmap(b -> b != 0, b -> (byte) (b ? 1 : 0)), PivotAuthPacketS2C::authorized,
            PivotAuthPacketS2C::new
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
