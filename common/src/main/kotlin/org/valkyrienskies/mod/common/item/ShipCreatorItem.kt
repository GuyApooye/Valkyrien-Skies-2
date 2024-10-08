package org.valkyrienskies.mod.common.item

import net.minecraft.Util
import net.minecraft.network.chat.TextComponent
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.level.block.Rotation.NONE
import net.minecraft.world.level.block.state.BlockState
import org.joml.Vector3d
import org.valkyrienskies.core.api.ships.properties.IShipActiveChunksSet
import org.valkyrienskies.core.api.world.properties.DimensionId
import org.valkyrienskies.core.impl.game.ships.ShipDataCommon
import org.valkyrienskies.core.impl.game.ships.ShipTransformImpl
import org.valkyrienskies.mod.common.dimensionId
import org.valkyrienskies.mod.common.getLevelFromDimensionId
import org.valkyrienskies.mod.common.getShipManagingPos
import org.valkyrienskies.mod.common.shipObjectWorld
import org.valkyrienskies.mod.common.util.VSLevelChunk
import org.valkyrienskies.mod.common.util.VSServerLevel
import org.valkyrienskies.mod.common.util.toBlockPos
import org.valkyrienskies.mod.common.util.toJOML
import org.valkyrienskies.mod.common.util.toJOMLD
import org.valkyrienskies.mod.common.world.ShipDimension
import org.valkyrienskies.mod.common.yRange
import org.valkyrienskies.mod.util.relocateBlock
import java.util.function.DoubleSupplier

class ShipCreatorItem(
    properties: Properties, private val scale: DoubleSupplier, private val minScaling: DoubleSupplier
) : Item(properties) {

    override fun isFoil(stack: ItemStack): Boolean {
        return true
    }
    private fun getLevelFromDimensionId(dimensionId: String, server: MinecraftServer): ServerLevel? {
        return server.getLevelFromDimensionId(dimensionId)
    }
    fun moveTerrainAcrossDimensions(
        shipChunks: IShipActiveChunksSet,
        srcDimension: DimensionId,
        destDimension: DimensionId,
        server: MinecraftServer
    ) {
        val srcLevel: ServerLevel = getLevelFromDimensionId(srcDimension,server)!!
        val destLevel: ServerLevel = getLevelFromDimensionId(destDimension,server)!!

        // Copy ship chunks from srcLevel to destLevel
        shipChunks.forEach { x: Int, z: Int ->
            val srcChunk = srcLevel.getChunk(x, z)
            // This is a hack, but it fixes destLevel being in the wrong state
            (destLevel as VSServerLevel).removeChunk(x, z)

            val destChunk = destLevel.getChunk(x, z)
            (destChunk as VSLevelChunk).copyChunkFromOtherDimension(srcChunk as VSLevelChunk)
        }

        // Delete ship chunks from srcLevel
        shipChunks.forEach { x: Int, z: Int ->
            val srcChunk = srcLevel.getChunk(x, z)
            (srcChunk as VSLevelChunk).clearChunk()

            val chunkPos = srcChunk.pos
            srcLevel.chunkSource.updateChunkForced(chunkPos, false)
            (srcLevel as VSServerLevel).removeChunk(x, z)
        }
    }

    override fun useOn(ctx: UseOnContext): InteractionResult {
        val level = ctx.level as? ServerLevel ?: return super.useOn(ctx)
        val blockPos = ctx.clickedPos
        val blockState: BlockState = level.getBlockState(blockPos)

        if (!level.isClientSide) {
            val parentShip = ctx.level.getShipManagingPos(blockPos)
            if (!blockState.isAir) {
                // Make a ship
                val dimensionId = level.dimensionId

                val scale = scale.asDouble
                val minScaling = minScaling.asDouble

                val serverShip =
                    level.shipObjectWorld.createNewShipAtBlock(blockPos.toJOML(), false, scale, dimensionId)

                val centerPos = serverShip.chunkClaim.getCenterBlockCoordinates(level.yRange).toBlockPos()

                // Move the block from the world to a ship
                level.relocateBlock(blockPos, centerPos, true, serverShip, NONE)
                moveTerrainAcrossDimensions(serverShip.activeChunksSet,level.dimensionId,ShipDimension.shipDimensionId,level.server)

                ctx.player?.sendMessage(TextComponent("SHIPIFIED!"), Util.NIL_UUID)
                if (parentShip != null) {
                    // Compute the ship transform
                    val newShipPosInWorld =
                        parentShip.shipToWorld.transformPosition(blockPos.toJOMLD().add(0.5, 0.5, 0.5))
                    val newShipPosInShipyard = blockPos.toJOMLD().add(0.5, 0.5, 0.5)
                    val newShipRotation = parentShip.transform.shipToWorldRotation
                    var newShipScaling = parentShip.transform.shipToWorldScaling.mul(scale, Vector3d())
                    if (newShipScaling.x() < minScaling) {
                        // Do not allow scaling to go below minScaling
                        newShipScaling = Vector3d(minScaling, minScaling, minScaling)
                    }
                    val shipTransform =
                        ShipTransformImpl(newShipPosInWorld, newShipPosInShipyard, newShipRotation, newShipScaling)
                    (serverShip as ShipDataCommon).transform = shipTransform
                }
            }
        }

        return super.useOn(ctx)
    }
}
