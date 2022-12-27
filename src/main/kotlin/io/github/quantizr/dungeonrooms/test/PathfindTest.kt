package io.github.quantizr.dungeonrooms.test

import io.github.quantizr.dungeonrooms.DRMConfig
import io.github.quantizr.dungeonrooms.DungeonRooms
import io.github.quantizr.dungeonrooms.handlers.TextRenderer
import io.github.quantizr.dungeonrooms.pathfinding.CachedPathFinder
import io.github.quantizr.dungeonrooms.utils.WaypointUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.joml.Vector3d
import org.joml.Vector3i
import java.awt.Color
import java.util.concurrent.Future


class PathfindTest {
    companion object {
        var textToDisplay: List<String> = emptyList()
    }

    private val pathfindFutures: MutableMap<Vector3i, Future<List<Vector3d>>> = HashMap()
    private val donePathfindFutures: MutableMap<Vector3i, List<Vector3d>> = HashMap()

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START || Minecraft.getMinecraft().thePlayer == null) return
        if (!Minecraft.getMinecraft().isSingleplayer || !DungeonRooms.debug) return
        updateFinishedFutures()

        val scrtLoc = Vector3i(10, 6, 10)
        val oldFuture = pathfindFutures[scrtLoc]
        if (oldFuture == null || oldFuture.isDone) {
            pathfindFutures[scrtLoc] = CachedPathFinder.CreatePath(
                Minecraft.getMinecraft().thePlayer,
                scrtLoc
            )
        }
    }

    private fun updateFinishedFutures(){
        with(pathfindFutures.iterator()) {
            forEach {(loc, ftr) ->
                if(ftr.isDone){
                    donePathfindFutures[loc] = ftr.get()
                    remove()
                }
            }
        }
    }

    @SubscribeEvent
    fun onWorldRender(event: RenderWorldLastEvent) {
        if (!Minecraft.getMinecraft().isSingleplayer || !DungeonRooms.debug) return
        donePathfindFutures.forEach { (_, points) ->
            WaypointUtils.drawLinesVec3(points, Color(255, 0, 0, 255), 2.0f, event.partialTicks, true)
        }
    }


    @SubscribeEvent
    fun renderPlayerInfo(event: RenderGameOverlayEvent.Post) {
        if (event.type != RenderGameOverlayEvent.ElementType.ALL) return
        if (!Minecraft.getMinecraft().isSingleplayer || !DungeonRooms.debug) return
        val mc = Minecraft.getMinecraft()
        if (textToDisplay.isNotEmpty()) {
            val scaledResolution = ScaledResolution(mc)
            var y = 0
            for (line in textToDisplay) {
                val roomStringWidth = mc.fontRendererObj.getStringWidth(line)
                TextRenderer.drawText(
                    mc, line, scaledResolution.scaledWidth * DRMConfig.textLocX / 100 - roomStringWidth / 2,
                    scaledResolution.scaledHeight * DRMConfig.textLocY / 100 + y, 1.0, true
                )
                y += mc.fontRendererObj.FONT_HEIGHT
            }
        }
    }


}