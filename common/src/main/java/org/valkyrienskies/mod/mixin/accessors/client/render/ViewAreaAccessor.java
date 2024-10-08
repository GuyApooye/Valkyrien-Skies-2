package org.valkyrienskies.mod.mixin.accessors.client.render;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.ViewArea;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ViewArea.class)
public interface ViewAreaAccessor {
    @Invoker(value = "getRenderChunkAt")
    ChunkRenderDispatcher.RenderChunk callGetRenderChunkAt(BlockPos pos);
    @Accessor("levelRenderer")
    void setLevelRenderer(LevelRenderer levelRenderer);
    @Accessor("level")
    void setLevel(Level level);
}
