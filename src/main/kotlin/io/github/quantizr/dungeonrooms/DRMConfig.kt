package io.github.quantizr.dungeonrooms

import cc.polyfrost.oneconfig.config.Config
import cc.polyfrost.oneconfig.config.annotations.*
import cc.polyfrost.oneconfig.config.core.OneKeyBind
import cc.polyfrost.oneconfig.config.data.InfoType
import cc.polyfrost.oneconfig.config.data.Mod
import cc.polyfrost.oneconfig.config.data.ModType
import cc.polyfrost.oneconfig.libs.universal.UKeyboard.KEY_I
import cc.polyfrost.oneconfig.libs.universal.UKeyboard.KEY_NONE
import cc.polyfrost.oneconfig.libs.universal.UKeyboard.KEY_O
import cc.polyfrost.oneconfig.libs.universal.UKeyboard.KEY_P
import io.github.quantizr.dungeonrooms.ChatTransmitter.Companion.addToQueue

object DRMConfig :
    Config(Mod("DungeonsRoomsMod", ModType.SKYBLOCK, "/drm_logo_x128.png"), DungeonRooms.MODID + ".json") {

    fun init(){
        initialize()
        addListener("practiceModeOn") {
            val text = """
                §eDungeon Rooms: Practice Mode has been enabled.
                §e Waypoints will ONLY show up while you are pressing ${practiceModeKeyBind.display}
                §r (Hotkey is configurable in Minecraft Controls menu)
                """.trimIndent()
            addToQueue(text)
        }
    }

    @Switch(name = "Display Room Names In Gui", subcategory = "General")
    var guiToggled = true

    @Switch(name = "Display Welcome/MOTD", subcategory = "General")
    var motdToggled = true

    @KeyBind(name = "Quick Access Gui Key", subcategory = "General")
    var waypointGuiKey = OneKeyBind(KEY_P)

    @KeyBind(name = "Force Refresh RoomDetection (Map Data)", subcategory = "General")
    var refreshMapKeybind = OneKeyBind(KEY_NONE)

    @Dropdown(name = "Secret Image Provider", options = ["gui", "dsg", "sbp"], subcategory = "Secret Images")
    var imageHotkeyOpen = 0

    @KeyBind(name = "Keybinding For Opening Room Images From DSG/SBP", size = 2, subcategory = "Secret Images")
    var openSecretImages = OneKeyBind(KEY_O)

    @Switch(name = "Waypoints Enabled", category = "Waypoints", subcategory = "Preferences", size = 2)
    var waypointsEnabled = true

    @Switch(name = "Double-Tap Sneak To Hide Nearby", category = "Waypoints", subcategory = "Preferences")
    var sneakToDisable = true

    @Switch(name = "Disable When All Secrets Found", category = "Waypoints", subcategory = "Preferences")
    var disableWhenAllFound = true

    @Switch(name = "Enable Pathfinding To Waypoints", subcategory = "Pathfinding", category = "Waypoints")
    var pathfindingEnabled = true

    @Slider(name = "Pathfinding Refresh Rate (In Ticks)", min = 1f, max = 40f, category = "Waypoints", subcategory = "Pathfinding")
    var pathfindingRefreshRate: Int = 20

    @Dropdown(
        description = "Select pathfinding algorithm",
        name = "Pathfinding Algorithm",
        options = ["THETA* (recommended)", "A* Diagonal", "A* Fine-Grid", "Jump Point Search"],
        subcategory = "Pathfinding",
        category = "Waypoints"
    )
    var secretPathfindStrategy = 0

    @Info(
        text = "See waypoints when the key is pressed, useful for practice.",
        category = "Waypoints",
        subcategory = "Practice Mode",
        size = 2,
        type = InfoType.INFO
    )
    var dummy = false

    @KeyBind(name = "Keybinding For Practice Mode", category = "Waypoints", subcategory = "Practice Mode")
    var practiceModeKeyBind = OneKeyBind(KEY_I)

    @Switch(name = "Practice Mode", category = "Waypoints", subcategory = "Practice Mode")
    var practiceModeOn = false

    @Checkbox(name = "Show Entrance Waypoints", category = "Waypoints", subcategory = "Visibility")
    var showEntrance = true

    @Checkbox(name = "Show Superboom Waypoints", category = "Waypoints", subcategory = "Visibility")
    var showSuperboom = true

    @Checkbox(name = "Show Secret Waypoints", category = "Waypoints", subcategory = "Visibility")
    var showSecrets = true

    @Checkbox(name = "Show Fairy Soul Waypoints", category = "Waypoints", subcategory = "Visibility")
    var showFairySouls = true

    @Checkbox(name = "Show Stonk Waypoints", category = "Waypoints", subcategory = "Visibility")
    var showStonk = true

    @Switch(name = "Show Waypoint Text", category = "Waypoints", subcategory = "Appearance")
    var showWaypointText = true

    @Switch(name = "Show Bounding Box", category = "Waypoints", subcategory = "Appearance")
    var showBoundingBox = true

    @Switch(name = "Show Beacon", category = "Waypoints", subcategory = "Appearance")
    var showBeacon = true

    @Slider(name = "Offset x", min = 0F, max = 100F, subcategory = "Hud")
    var textLocX = 50

    @Slider(name = "Offset y", min = 0F, max = 100F, subcategory = "Hud")
    var textLocY = 5

}