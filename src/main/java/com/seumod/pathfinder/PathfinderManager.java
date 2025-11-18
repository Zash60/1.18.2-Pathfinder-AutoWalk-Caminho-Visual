package com.seumod.pathfinder;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.concurrent.CompletableFuture;

public class PathfinderManager {
    private static final SimplePathfinder pathExecutor = new SimplePathfinder();
    private static boolean navigationActive = false;
    private static boolean movementPaused = false;

    private static BlockPos targetPos = null;
    private static Vec3d lastPlayerPos = Vec3d.ZERO;
    private static int ticksStuck = 0;
    private static final int MAX_TICKS_STUCK = 60;

    public static void setTarget(BlockPos target) {
        stop();
        
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return;

        BlockPos safeStartPos = getSafePlayerStartPos(player);

        if (safeStartPos.equals(target)) {
            player.sendMessage(new LiteralText("§aVocê já está no destino!"), true);
            // CORREÇÃO: Garante que o alvo seja nulo para o renderer não desenhar nada.
            targetPos = null; 
            return;
        }
        
        targetPos = target;
        navigationActive = true;
        movementPaused = false;
        player.sendMessage(new LiteralText("§eCalculando caminho com A*..."), true);

        CompletableFuture.supplyAsync(() -> AStarPathfinder.findPath(safeStartPos, target))
            .thenAcceptAsync(path -> {
                if (path != null && !path.isEmpty()) {
                    pathExecutor.setPath(path);
                    player.sendMessage(new LiteralText("§aCaminho encontrado! Navegando..."), true);
                } else {
                    player.sendMessage(new LiteralText("§cNão foi possível encontrar um caminho."), true);
                    stop();
                }
            }, MinecraftClient.getInstance());
    }
    
    private static BlockPos getSafePlayerStartPos(ClientPlayerEntity player) {
        BlockPos playerPos = player.getBlockPos();
        if (player.isOnGround()) {
            return playerPos;
        }
        for (int i = 1; i <= 5; i++) {
            BlockPos checkPos = player.getBlockPos().down(i);
            if (!player.world.getBlockState(checkPos).isAir()) {
                return checkPos.up();
            }
        }
        return playerPos;
    }

    public static void toggleMovement() {
        if (navigationActive) {
            movementPaused = !movementPaused;
            if (movementPaused) stopMoving();
        }
    }

    public static void stop() {
        navigationActive = false;
        movementPaused = false;
        targetPos = null;
        pathExecutor.clearPath();
        stopMoving();
        ticksStuck = 0;
    }

    public static void update() {
        if (!navigationActive || movementPaused) return;

        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) {
            stop();
            return;
        }

        if (pathExecutor.isPathFinished()) {
            if (navigationActive) {
                player.sendMessage(new LiteralText("§aDestino alcançado!"), true);
            }
            stop();
            return;
        }

        if (player.getPos().distanceTo(lastPlayerPos) < 0.05 && player.isOnGround()) {
            ticksStuck++;
        } else {
            ticksStuck = 0;
            lastPlayerPos = player.getPos();
        }

        if (ticksStuck > MAX_TICKS_STUCK) {
            player.sendMessage(new LiteralText("§cVocê está preso! Cancelando navegação."), true);
            stop();
            return;
        }

        pathExecutor.updateMovement();
    }

    public static void stopMoving() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.options != null) {
            client.options.forwardKey.setPressed(false);
            client.options.jumpKey.setPressed(false);
            client.options.sprintKey.setPressed(false);
        }
    }
    
    public static boolean isNavigationActive() { return navigationActive; }
    public static boolean isMovementPaused() { return movementPaused; }
    public static SimplePathfinder getPathExecutor() { return pathExecutor; }
    public static BlockPos getTarget() { return targetPos; }
}
