package com.seumod;

import com.seumod.commands.GotoCommand;
import com.seumod.pathfinder.PathfinderManager;
import com.seumod.render.PathRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.LiteralText;
import org.lwjgl.glfw.GLFW;

public class PathfinderAutoWalkMod implements ClientModInitializer {
    public static KeyBinding toggleAutoWalkKey;
    public static KeyBinding togglePathfinderKey;

    @Override
    public void onInitializeClient() {
        GotoCommand.register();
        PathRenderer.register();
        PathfinderManager.initialize();

        toggleAutoWalkKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.pathfindermod.toggle_autowalk", GLFW.GLFW_KEY_O, "category.pathfindermod"
        ));
        
        togglePathfinderKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.pathfindermod.toggle_pathfinder", GLFW.GLFW_KEY_P, "category.pathfindermod"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            while (toggleAutoWalkKey.wasPressed()) {
                PathfinderManager.setAutoWalk(!PathfinderManager.isAutoWalk());
                client.player.sendMessage(new LiteralText(
                    "AutoWalk: " + (PathfinderManager.isAutoWalk() ? "§aON" : "§cOFF")
                ), true);
            }

            while (togglePathfinderKey.wasPressed()) {
                PathfinderManager.setEnabled(!PathfinderManager.isEnabled());
                client.player.sendMessage(new LiteralText(
                    "Pathfinder: " + (PathfinderManager.isEnabled() ? "§aON" : "§cOFF")
                ), true);
            }

            // <-- LÓGICA DE ATIVAÇÃO/DESATIVAÇÃO CORRIGIDA AQUI -->
            if (PathfinderManager.isEnabled()) {
                PathfinderManager.update();
            } else {
                // Garante que o movimento pare se o pathfinder for desativado por qualquer motivo
                PathfinderManager.stopMoving();
            }

            // A lógica de autowalk simples pode ser separada
            if (PathfinderManager.isAutoWalk() && !PathfinderManager.isEnabled()) {
                client.options.forwardKey.setPressed(true);
            } else if (!PathfinderManager.isEnabled()) {
                 client.options.forwardKey.setPressed(false);
            }
        });
    }
}
