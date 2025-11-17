package com.seumod.pathfinder;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import java.util.ArrayList;
import java.util.List;

// Esta classe agora apenas EXECUTA um caminho pré-calculado.
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

        // Rotaciona o jogador para o próximo ponto
        lookAt(player, nextVecCenter);

        // Pressiona as teclas de movimento
        client.options.forwardKey.setPressed(true);
        client.options.sprintKey.setPressed(player.getHungerManager().getFoodLevel() > 6);
        handleJumping(player, nextPos);

        // Avança para o próximo ponto do caminho
        if (player.getPos().isInRange(nextVecCenter, 1.5)) {
            currentIndex++;
        }
    }
    
    private void lookAt(ClientPlayerEntity player, Vec3d target) {
        Vec3d direction = target.subtract(player.getPos()).normalize();
        double yaw = Math.toDegrees(Math.atan2(-direction.x, direction.z));
        player.setYaw((float) yaw);
    }

    private void handleJumping(ClientPlayerEntity player, BlockPos nextNode) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!player.isOnGround()) {
            client.options.jumpKey.setPressed(false);
            return;
        }
        // Pula se o próximo nó do caminho estiver acima da posição do jogador
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
