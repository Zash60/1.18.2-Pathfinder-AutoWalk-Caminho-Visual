package com.seumod.pathfinder;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class SimplePathfinder {
    private List<BlockPos> path = new ArrayList<>();
    private int currentIndex = 0;

    public void setPath(List<BlockPos> newPath) {
        this.path = (newPath != null) ? newPath : new ArrayList<>();
        this.currentIndex = 0;
    }

    public void updateMovement() {
        if (isPathFinished()) return;

        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null) return;

        BlockPos nextPos = path.get(currentIndex);
        Vec3d nextVecCenter = new Vec3d(nextPos.getX() + 0.5, nextPos.getY(), nextPos.getZ() + 0.5);

        // --- LÓGICA DE PARADA DE PRECISÃO ---
        boolean isFinalNode = currentIndex == path.size() - 1;

        if (isFinalNode) {
            // Calcula a distância horizontal (ignorando o eixo Y)
            double dx = player.getX() - nextVecCenter.x;
            double dz = player.getZ() - nextVecCenter.z;
            double horizontalDistanceSq = dx * dx + dz * dz;

            // Se estiver muito perto do centro (distância < 0.15 blocos), considera que chegou.
            // Usamos a distância ao quadrado para evitar o cálculo da raiz quadrada (mais rápido). 0.15*0.15 = 0.0225
            if (horizontalDistanceSq < 0.0225) {
                PathfinderManager.stopMoving(); // Para imediatamente
                currentIndex++; // Marca o caminho como finalizado
                return; // Encerra a execução deste tick
            }
        }
        
        // --- MOVIMENTO NORMAL ---
        lookAt(player, nextVecCenter);

        client.options.forwardKey.setPressed(true);
        // CORREÇÃO: Desativa o sprint na aproximação final para não passar do ponto.
        client.options.sprintKey.setPressed(!isFinalNode && player.getHungerManager().getFoodLevel() > 6);
        handleJumping(player, nextPos);

        // Avança para o próximo ponto do caminho (exceto para o último nó, que tem sua própria lógica de parada)
        if (!isFinalNode && player.getPos().isInRange(nextVecCenter, 1.5)) {
            currentIndex++;
        }
    }

    private void lookAt(ClientPlayerEntity player, Vec3d target) {
        Vec3d playerPos = player.getEyePos(); // Usa a posição dos olhos para uma mira mais estável
        Vec3d direction = target.subtract(playerPos).normalize();
        double yaw = Math.toDegrees(Math.atan2(-direction.x, direction.z));
        player.setYaw((float) yaw);
    }

    private void handleJumping(ClientPlayerEntity player, BlockPos nextNode) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!player.isOnGround()) {
            client.options.jumpKey.setPressed(false);
            return;
        }
        boolean shouldJump = nextNode.getY() > player.getBlockPos().getY();
        client.options.jumpKey.setPressed(shouldJump);
    }

    public List<BlockPos> getPath() { return path; }
    public boolean isPathFinished() { return path.isEmpty() || currentIndex >= path.size(); }
    public void clearPath() {
        path.clear();
        currentIndex = 0;
    }
}
