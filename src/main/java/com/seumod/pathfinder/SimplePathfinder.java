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

        boolean isFinalNode = currentIndex == path.size() - 1;

        if (isFinalNode) {
            double dx = player.getX() - nextVecCenter.x;
            double dz = player.getZ() - nextVecCenter.z;
            double horizontalDistanceSq = dx * dx + dz * dz;

            if (horizontalDistanceSq < 0.0225) {
                PathfinderManager.stopMoving();
                currentIndex++;
                return;
            }
        }
        
        lookAt(player, nextVecCenter);

        client.options.forwardKey.setPressed(true);
        client.options.sprintKey.setPressed(!isFinalNode && player.getHungerManager().getFoodLevel() > 6);
        
        // CORREÇÃO: A lógica de pulo foi movida para um método mais robusto.
        handleJumping(player, nextPos);

        // Avança para o próximo ponto do caminho com uma distância menor para mais precisão.
        if (!isFinalNode && player.getPos().isInRange(nextVecCenter, 1.2)) {
            currentIndex++;
        }
    }

    private void lookAt(ClientPlayerEntity player, Vec3d target) {
        Vec3d playerPos = player.getEyePos();
        Vec3d direction = target.subtract(playerPos).normalize();
        double yaw = Math.toDegrees(Math.atan2(-direction.x, direction.z));
        player.setYaw((float) yaw);
    }

    // CORREÇÃO: Lógica de pulo completamente refeita.
    private void handleJumping(ClientPlayerEntity player, BlockPos nextNode) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!player.isOnGround()) {
            client.options.jumpKey.setPressed(false);
            return;
        }

        boolean shouldJump = false;
        
        // Condição 1: Pular se o próximo nó do caminho for mais alto.
        if (nextNode.getY() > player.getBlockPos().getY()) {
            shouldJump = true;
        }

        // Condição 2: Pular se houver um obstáculo na frente (mesmo que o próximo nó não seja mais alto).
        // Isso é crucial para saltar sobre vãos.
        if (!shouldJump) {
            Vec3d forward = Vec3d.fromPolar(0, player.getYaw()).normalize().multiply(0.9);
            BlockPos blockInFront = player.getBlockPos().add((int)Math.ceil(forward.x), 0, (int)Math.ceil(forward.z));
            
            // Verifica se o bloco na frente é sólido e se há espaço para pular sobre ele.
            if (!client.world.getBlockState(blockInFront).isAir() && 
                 client.world.getBlockState(blockInFront.up()).isAir() && 
                 client.world.getBlockState(player.getBlockPos().up(2)).isAir()) {
                shouldJump = true;
            }
        }
        
        client.options.jumpKey.setPressed(shouldJump);
    }

    public List<BlockPos> getPath() { return path; }
    public boolean isPathFinished() { return path.isEmpty() || currentIndex >= path.size(); }
    public void clearPath() {
        path.clear();
        currentIndex = 0;
    }
}
