package io.github.quantizr.dungeonrooms


import net.minecraft.client.Minecraft
import net.minecraft.util.ChatComponentText
import net.minecraft.util.IChatComponent
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Taken from dg-LTS under the GNU General Public License v3.0
 * https://github.com/dg-continuum/dg-LTS
 * @author kingstefan26
 */
class ChatTransmitter {

    @SubscribeEvent
    fun onTick(clientTickEvent: ClientTickEvent) {
        if (clientTickEvent.phase != TickEvent.Phase.START && Minecraft.getMinecraft().thePlayer == null) return
        if (!receiveQueue.isEmpty()) {
            val event = ClientChatReceivedEvent(1.toByte(), receiveQueue.poll())
            MinecraftForge.EVENT_BUS.post(event)
            if (!event.isCanceled) {
                Minecraft.getMinecraft().thePlayer.addChatMessage(event.message)
            }
        }
    }

    companion object {
        const val PREFIX = "DungeonRooms: "
        var receiveQueue: Queue<IChatComponent> = ConcurrentLinkedQueue()
        fun addToQueue(chat: String, noDupe: Boolean) {
            addToQueue(ChatComponentText(chat.replace("@", "§")), noDupe)
        }

        @JvmOverloads
        fun addToQueue(chat: ChatComponentText, noDupe: Boolean = false) {
            if (noDupe && receiveQueue.stream().anyMatch { it == chat }) return
            receiveQueue.add(chat)
        }
        @JvmOverloads
        fun addToQueue(chat: IChatComponent) {
            receiveQueue.add(chat)
        }

        @JvmStatic
        fun addToQueue(s: String) {
            addToQueue(s, false)
        }

        @JvmStatic
        fun addToQueueWPrefix(s: String) {
            addToQueue(PREFIX + s, false)
        }

        @JvmStatic
        fun sendDebugChat(iChatComponent: IChatComponent) {
            if (DRMConfig.debug) addToQueue(iChatComponent as ChatComponentText)
        }

        @JvmStatic
        fun sendDebugChat(text: String?) {
            sendDebugChat(ChatComponentText(text))
        }
    }
}