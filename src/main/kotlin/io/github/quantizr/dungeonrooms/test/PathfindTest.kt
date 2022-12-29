package io.github.quantizr.dungeonrooms.test

import io.github.quantizr.dungeonrooms.DRMConfig
import io.github.quantizr.dungeonrooms.handlers.TextRenderer
import io.github.quantizr.dungeonrooms.pathfinding.CachedPathFinder
import io.github.quantizr.dungeonrooms.pathfinding.PfPath
import io.github.quantizr.dungeonrooms.utils.WaypointUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.joml.Vector3i
import java.awt.Color


class PathfindTest {

    val pathFinder: CachedPathFinder = CachedPathFinder()

    companion object {
        var textToDisplay: List<String> = emptyList()
    }

    private val donePathfindFutures: MutableMap<Vector3i, PfPath> = HashMap()

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START || Minecraft.getMinecraft().thePlayer == null) return
        if (!Minecraft.getMinecraft().isSingleplayer || !DRMConfig.debug) return

        val locs = listOf(Vector3i(10, 6, 10), Vector3i(10, 6, 15))

        for (loc in locs) {
            pathFinder.createPathAsync(
                Vector3i(
                    Minecraft.getMinecraft().thePlayer.posX.toInt(),
                    Minecraft.getMinecraft().thePlayer.posY.toInt(),
                    Minecraft.getMinecraft().thePlayer.posZ.toInt()
                ),
                loc,
                lockProcessingThisTarget = true
            ) {
                donePathfindFutures[loc] = it
            }

        }
    }

    @SubscribeEvent
    fun onWorldRender(event: RenderWorldLastEvent) {
        if (!Minecraft.getMinecraft().isSingleplayer || !DRMConfig.debug) return
        donePathfindFutures.forEach { (_, points) ->
            WaypointUtils.drawLinesVec3(
                points.path,
                Color(255, 0, 0, 255),
                Color(0, 255, 0, 255),
                2.0f,
                event.partialTicks,
                true
            )
        }
    }


    @SubscribeEvent
    fun renderPlayerInfo(event: RenderGameOverlayEvent.Post) {
        if (event.type != RenderGameOverlayEvent.ElementType.ALL) return
        if (!Minecraft.getMinecraft().isSingleplayer || !DRMConfig.debug) return
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