package com.seumod.pathfinder;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PathfinderManager {
    private static final SimplePathfinder pathExecutor = new SimplePathfinder();
    private static boolean navigationActive = false; // Se há um destino definido
    private static boolean movementPaused = false;   // Se o jogador pausou com a tecla P

    private static BlockPos targetPos = null;
    private static Vec3d lastPlayerPos = Vec3d.ZERO;
    private static int ticksStuck = 0;
    private static final int MAX_TICKS_STUCK = 60; // 3 segundos

    // Inicia o processo de pathfinding
    public static void setTarget(BlockPos target) {
        stop(); // Para qualquer navegação anterior
        targetPos = target;
        navigationActive = true;
        movementPaused = false;

        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return;
        player.sendMessage(new LiteralText("§eCalculando caminho com A*..."), true);

        // Executa o A* em uma thread separada para não congelar o jogo
        CompletableFuture.supplyAsync(() -> AStarPathfinder.findPath(player.getBlockPos(), target))
            .thenAcceptAsync(path -> {
                if (path != null && !path.isEmpty()) {
                    pathExecutor.setPath(path);
                    player.sendMessage(new LiteralText("§aCaminho encontrado! Navegando..."), true);
                } else {
                    player.sendMessage(new LiteralText("§cNão foi possível encontrar um caminho."), true);
                    stop();
                }
            }, MinecraftClient.getInstance()); // Executa o thenAccept na thread do cliente
    }

    // Usado pela tecla P para pausar/retomar
    public static void toggleMovement() {
        if (navigationActive) {
            movementPaused = !movementPaused;
            if (movementPaused) {
                stopMoving();
            }
        }
    }

    // Chamado pelo comando /stop ou ao chegar
    public static void stop() {
        navigationActive = false;
        movementPaused = false;
        targetPos = null;
        pathExecutor.clearPath();
        stopMoving();
        ticksStuck = 0;
    }

    public static void update() {
        if (!navigationActive || movementPaused) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null || pathExecutor.isPathFinished()) {
            if (navigationActive && targetPos != null) {
                player.sendMessage(new LiteralText("§aDestino alcançado!"), true);
            }
            stop();
            return;
        }

        // Detecção de jogador preso
        if (player.getPos().distanceTo(lastPlayerPos) < 0.1 && player.isOnGround()) {
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
    
    // Getters para o Renderer e Keybinds
    public static boolean isNavigationActive() { return navigationActive; }
    public static boolean isMovementPaused() { return movementPaused; }
    public static SimplePathfinder getPathExecutor() { return pathExecutor; }
    public static BlockPos getTarget() { return targetPos; }
}
