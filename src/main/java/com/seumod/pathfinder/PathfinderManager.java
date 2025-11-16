package com.seumod.pathfinder;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.BlockPos;

public class PathfinderManager {
    private static SimplePathfinder pathfinder = new SimplePathfinder();
    private static boolean enabled = false;
    private static boolean autoWalkActive = false;
    private static BlockPos targetPos = null;

    public static void initialize() {}
    public static void setTarget(int x, int y, int z) {
        targetPos = new BlockPos(x, y, z);
        pathfinder.setTarget(targetPos);
        enabled = true;
    }
    public static void setEnabled(boolean value) {
        enabled = value; if (!enabled) stopMoving();
    }
    public static boolean isEnabled() { return enabled; }
    public static boolean isAutoWalk() { return autoWalkActive; }
    public static void setAutoWalk(boolean value) { autoWalkActive = value; }

    public static void update() {
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client.player;
        if (player == null || targetPos == null) return;

        if (player.getBlockPos().getSquaredDistance(targetPos) < 4.0) {
            player.sendMessage(new LiteralText("§aDestino alcançado!"), true);
            setEnabled(false); return;
        }
        pathfinder.update();
    }

    private static void stopMoving() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.options != null) {
            client.options.forwardKey.setPressed(false);
            client.options.jumpKey.setPressed(false);
        }
    }

    public static SimplePathfinder getPathfinder() { return pathfinder; }
    public static BlockPos getTarget() { return targetPos; }
}
