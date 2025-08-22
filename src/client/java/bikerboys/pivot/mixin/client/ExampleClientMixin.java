package bikerboys.pivot.mixin.client;

import bikerboys.pivot.PivotClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.DisplayEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(value = Entity.class)
public abstract class ExampleClientMixin {

	@Inject(method = "isGlowing", at = @At("HEAD"), cancellable = true)
	private void makeAlwaysGlow(CallbackInfoReturnable<Boolean> cir) {

		Entity entity = (Entity)(Object)this;

		if (entity instanceof DisplayEntity displayEntity) {
			if (EnvType.CLIENT.equals(FabricLoader.getInstance().getEnvironmentType())) {

				if (!PivotClient.BlockEntitiesInWorld.isEmpty()) {
					UUID uuid = PivotClient.BlockEntitiesInWorld.get(PivotClient.currentIndex);


					if (displayEntity.getUuid().equals(uuid)) {
						if (PivotClient.glowing) {
							cir.setReturnValue(true);
						}
					} else {
						cir.setReturnValue(false);
					}

				}




			}
		}
	}


}