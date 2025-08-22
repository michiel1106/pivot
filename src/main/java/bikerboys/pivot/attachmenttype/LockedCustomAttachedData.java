package bikerboys.pivot.attachmenttype;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

public record LockedCustomAttachedData(boolean locked) {

    public static Codec<LockedCustomAttachedData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("locked").forGetter(LockedCustomAttachedData::locked) // our object just has a list of floats
    ).apply(instance, LockedCustomAttachedData::new));
    public static PacketCodec<ByteBuf, LockedCustomAttachedData> PACKET_CODEC = PacketCodecs.codec(CODEC);

    // A default value we can use as an "empty" or reset data component
    // it uses List.of() which creates an empty, immutable list.
    public static LockedCustomAttachedData DEFAULT = new LockedCustomAttachedData(false);

    public LockedCustomAttachedData setLocked(boolean locked) {

        return new LockedCustomAttachedData(locked);
    }

    public LockedCustomAttachedData lock() {

        return new LockedCustomAttachedData(true);
    }


    public LockedCustomAttachedData unlock() {

        return new LockedCustomAttachedData(false);
    }


    public LockedCustomAttachedData clear() { // clear method, just returns the default empty component
        return DEFAULT;
    }
}
