/*
 * Dungeon Rooms Mod - Secret Waypoints for Hypixel Skyblock Dungeons
 * Copyright 2021 Quantizr(_risk)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package io.github.quantizr.dungeonrooms.handlers

import com.google.common.collect.Iterables
import com.google.common.collect.Lists
import net.minecraft.client.Minecraft
import net.minecraft.scoreboard.Score
import net.minecraft.scoreboard.ScorePlayerTeam
import net.minecraft.util.StringUtils
import java.util.stream.Collectors

object ScoreboardHandler {
    @JvmStatic
    fun cleanSB(scoreboard: String?): String {
        val nvString = StringUtils.stripControlCodes(scoreboard).toCharArray()
        val cleaned = StringBuilder()
        for (c in nvString) {
            if (c.code in 21..126) {
                cleaned.append(c)
            }
        }
        return cleaned.toString()
    }

    @JvmStatic
    val sidebarLines: List<String>
        get() {
            val lines: MutableList<String> = ArrayList()
            if (Minecraft.getMinecraft().theWorld == null) return lines
            val scoreboard = Minecraft.getMinecraft().theWorld.scoreboard ?: return lines
            val objective = scoreboard.getObjectiveInDisplaySlot(1) ?: return lines
            var scores = scoreboard.getSortedScores(objective)
            val list = scores.stream()
                .filter { input: Score? ->
                    input != null && input.playerName != null && !input.playerName
                        .startsWith("#")
                }
                .collect(Collectors.toList())
            scores = if (list.size > 15) {
                Lists.newArrayList(Iterables.skip(list, scores.size - 15))
            } else {
                list
            }
            for (score in scores) {
                val team = scoreboard.getPlayersTeam(score.playerName)
                lines.add(ScorePlayerTeam.formatPlayerName(team, score.playerName))
            }
            return lines
        }
}