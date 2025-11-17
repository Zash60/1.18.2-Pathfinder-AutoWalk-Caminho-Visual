package com.seumod.pathfinder;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class PathfinderManager {
    private static final SimplePathfinder pathfinder = new SimplePathfinder();
    private static boolean pathfinderEnabled = false;
    private static boolean autoWalkEnabled = false;
    private static BlockPos targetPos = null;

    // ANOTAÇÃO: Adicionamos detecção de "preso" (stuck).
    private static Vec3d lastPlayerPos = Vec3d.ZERO;
    private static int ticksStuck = 0;
    private static final int MAX_TICKS_STUCK = 60; // 3 segundos

    public static void setTarget(BlockPos target) {
        targetPos = target;
        pathfinder.createPath(target);
        setEnabled(true);
    }

    public static void setEnabled(boolean value) {
        if (pathfinderEnabled == value) return; // Evita ações redundantes

        pathfinderEnabled = value;
        if (!pathfinderEnabled) {
            stop();
        } else if (autoWalkEnabled) {
            // Se o pathfinder for ativado, o autowalk simples é desativado.
            setAutoWalk(false);
        }
    }

    public static void setAutoWalk(boolean value) {
        if (autoWalkEnabled == value) return; // Evita ações redundantes

        autoWalkEnabled = value;
        if (!autoWalkEnabled) {
            stopMoving();
        } else if (pathfinderEnabled) {
            // Se o autowalk for ativado, o pathfinder é desativado.
            setEnabled(false);
        }
    }

    public static void togglePathfinder() {
        if (targetPos != null) {
            setEnabled(!pathfinderEnabled);
        }
    }

    public static void toggleAutoWalk() {
        setAutoWalk(!autoWalkEnabled);
    }

    public static void stop() {
        targetPos = null;
        pathfinder.clearPath();
        stopMoving();
        ticksStuck = 0;
    }

    public static void update() {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null) {
            stop();
            return;
        }

        // Lógica do Pathfinder
        if (pathfinderEnabled) {
            if (targetPos == null || pathfinder.isPathEmpty()) {
                setEnabled(false);
                return;
            }

            // ANOTAÇÃO: Condição de chegada melhorada usando distância.
            if (player.getPos().distanceTo(targetPos.toCenterPos()) < 1.5) {
                player.sendMessage(Text.literal("§aDestino alcançado!"), true);
                setEnabled(false);
                return;
            }

            // ANOTAÇÃO: Detecção de jogador preso
            if (player.getPos().distanceTo(lastPlayerPos) < 0.1 && player.isOnGround()) {
                ticksStuck++;
            } else {
                ticksStuck = 0;
                lastPlayerPos = player.getPos();
            }

            if (ticksStuck > MAX_TICKS_STUCK) {
                player.sendMessage(Text.literal("§cVocê está preso! Cancelando navegação."), true);
                setEnabled(false);
                return;
            }

            pathfinder.updateMovement();
        }
        // Lógica do AutoWalk simples
        else if (autoWalkEnabled) {
            client.options.forwardKey.setPressed(true);
        }
        // Garante que o jogador pare se ambos estiverem desativados
        else {
            stopMoving();
        }
    }

    public static void stopMoving() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.options != null) {
            client.options.forwardKey.setPressed(false);
            client.options.jumpKey.setPressed(false);
            client.options.sprintKey.setPressed(false);
        }
    }
    
    // Getters
    public static boolean isEnabled() { return pathfinderEnabled; }
    public static boolean isAutoWalk() { return autoWalkEnabled; }
    public static SimplePathfinder getPathfinder() { return pathfinder; }
    public static BlockPos getTarget() { return targetPos; }
}
