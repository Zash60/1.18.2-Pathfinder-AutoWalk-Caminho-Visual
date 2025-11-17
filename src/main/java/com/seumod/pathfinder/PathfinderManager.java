package com.seumod.pathfinder;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.LiteralText; // CORREÇÃO: Import correto para 1.18.2
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class PathfinderManager {
    private static final SimplePathfinder pathfinder = new SimplePathfinder();
    private static boolean pathfinderEnabled = false;
    private static boolean autoWalkEnabled = false;
    private static BlockPos targetPos = null;

    private static Vec3d lastPlayerPos = Vec3d.ZERO;
    private static int ticksStuck = 0;
    private static final int MAX_TICKS_STUCK = 60; // 3 segundos

    public static void setTarget(BlockPos target) {
        targetPos = target;
        pathfinder.createPath(target);
        setEnabled(true);
    }

    public static void setEnabled(boolean value) {
        if (pathfinderEnabled == value) return;

        pathfinderEnabled = value;
        if (!pathfinderEnabled) {
            stop();
        } else if (autoWalkEnabled) {
            setAutoWalk(false);
        }
    }

    public static void setAutoWalk(boolean value) {
        if (autoWalkEnabled == value) return;

        autoWalkEnabled = value;
        if (!autoWalkEnabled) {
            stopMoving();
        } else if (pathfinderEnabled) {
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

        if (pathfinderEnabled) {
            if (targetPos == null || pathfinder.isPathEmpty()) {
                setEnabled(false);
                return;
            }
            
            // CORREÇÃO: Calculando o centro do bloco manualmente
            Vec3d targetCenter = new Vec3d(targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5);
            if (player.getPos().distanceTo(targetCenter) < 1.5) {
                // CORREÇÃO: Usando new LiteralText()
                player.sendMessage(new LiteralText("§aDestino alcançado!"), true);
                setEnabled(false);
                return;
            }
            
            if (player.getPos().distanceTo(lastPlayerPos) < 0.1 && player.isOnGround()) {
                ticksStuck++;
            } else {
                ticksStuck = 0;
                lastPlayerPos = player.getPos();
            }

            if (ticksStuck > MAX_TICKS_STUCK) {
                // CORREÇÃO: Usando new LiteralText()
                player.sendMessage(new LiteralText("§cVocê está preso! Cancelando navegação."), true);
                setEnabled(false);
                return;
            }

            pathfinder.updateMovement();
        }
        else if (autoWalkEnabled) {
            client.options.forwardKey.setPressed(true);
        }
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
    
    public static boolean isEnabled() { return pathfinderEnabled; }
    public static boolean isAutoWalk() { return autoWalkEnabled; }
    public static SimplePathfinder getPathfinder() { return pathfinder; }
    public static BlockPos getTarget() { return targetPos; }
}
