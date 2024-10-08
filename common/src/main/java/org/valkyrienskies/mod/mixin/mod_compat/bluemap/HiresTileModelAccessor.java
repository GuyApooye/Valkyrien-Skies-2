package org.valkyrienskies.mod.mixin.mod_compat.bluemap;

import de.bluecolored.bluemap.core.map.hires.HiresTileModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = HiresTileModel.class,remap = false)
@Pseudo
public interface HiresTileModelAccessor {

    @Accessor("position")
    double[] getPositions();

}
