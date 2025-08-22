package bikerboys.pivot.mixin.client;


import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.network.packet.s2c.common.DisconnectS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.client.network.ClientCommonNetworkHandler.class)
public class ClientCommonNetworkHandler {

    @Inject(method = "onDisconnect", at = @At("HEAD"))
    private void onDisconnect(DisconnectS2CPacket packet, CallbackInfo ci) {
        MinecraftClient instance = MinecraftClient.getInstance();

        if (instance != null) {
            if (instance.player != null) {
                ClientWorld clientWorld = instance.player.clientWorld;
                if (clientWorld != null) {
                    clientWorld.getEntities().forEach((entity -> {
                        if (entity instanceof DisplayEntity displayEntity) {
                            displayEntity.setGlowing(false);
                        }

                    }));
                }
            }
        }

    }

}
