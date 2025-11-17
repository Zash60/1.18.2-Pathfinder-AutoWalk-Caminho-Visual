package com.seumod;

import com.seumod.commands.GotoCommand;
import com.seumod.pathfinder.PathfinderManager;
import com.seumod.render.PathRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class PathfinderAutoWalkMod implements ClientModInitializer {
    public static KeyBinding toggleAutoWalkKey;
    public static KeyBinding togglePathfinderKey;

    @Override
    public void onInitializeClient() {
        GotoCommand.register();
        PathRenderer.register();

        // ANOTAÇÃO: Centralizamos as constantes de categoria para evitar repetição.
        String category = "category.pathfindermod";

        toggleAutoWalkKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.pathfindermod.toggle_autowalk", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_O, category
        ));
        
        togglePathfinderKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.pathfindermod.toggle_pathfinder", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_P, category
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            // ANOTAÇÃO: A lógica de toggle foi simplificada e movida para o PathfinderManager.
            while (toggleAutoWalkKey.wasPressed()) {
                PathfinderManager.toggleAutoWalk();
                client.player.sendMessage(Text.literal(
                    "AutoWalk: " + (PathfinderManager.isAutoWalk() ? "§aON" : "§cOFF")
                ), true);
            }

            while (togglePathfinderKey.wasPressed()) {
                PathfinderManager.togglePathfinder();
                client.player.sendMessage(Text.literal(
                    "Pathfinder: " + (PathfinderManager.isEnabled() ? "§aON" : "§cOFF")
                ), true);
            }

            // ANOTAÇÃO: Lógica de movimento e atualização foi centralizada no PathfinderManager
            // para maior clareza e para evitar bugs de estado.
            PathfinderManager.update();
        });
    }
}
