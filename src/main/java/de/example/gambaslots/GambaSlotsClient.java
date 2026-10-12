package de.example.gambaslots;

import de.example.gambaslots.client.GambaSlotsScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public final class GambaSlotsClient implements ClientModInitializer {
    private static final KeyBinding OPEN_MENU = KeyBindingHelper.registerKeyBinding(
            new KeyBinding(
                    "key.gambaslots.open_menu",
                    InputUtil.Type.KEYSYM,
                    GLFW.GLFW_KEY_J,
                    new KeyBinding.Category(Identifier.of("gambaslots", "controls"))
            )
    );

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (OPEN_MENU.wasPressed()) {
                if (client.player == null) {
                    continue;
                }

                if (client.currentScreen instanceof GambaSlotsScreen) {
                    client.setScreen(null);
                } else if (client.currentScreen == null) {
                    client.setScreen(new GambaSlotsScreen());
                }
            }

            if (GambaGameState.tick() && client.player != null) {
                client.player.sendMessage(Text.literal(GambaGameState.getMessage()), false);
            }
        });
    }
}
