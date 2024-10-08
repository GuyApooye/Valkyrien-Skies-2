package org.valkyrienskies.mod.mixin.accessors.client.multiplayer;

import net.minecraft.client.multiplayer.ClientLevel.ClientLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientLevelData.class)
public interface ClientLevelDataAccessor {
    @Accessor("isFlat")
    boolean getIsFlat();
}
