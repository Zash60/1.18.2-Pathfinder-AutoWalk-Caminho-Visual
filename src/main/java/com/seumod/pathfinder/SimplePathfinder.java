package com.seumod.pathfinder;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.block.BlockState;
import java.util.ArrayList;
import java.util.List;

public class SimplePathfinder {
    private List<BlockPos> path = new ArrayList<>();
    private int currentIndex = 0;
    private BlockPos target;

    public void setTarget(BlockPos target) {
        this.target = target;
        this.currentIndex = 0;
        this.path = calculatePath(target);
    }

    private List<BlockPos> calculatePath(BlockPos target) {
        List<BlockPos> waypoints = new ArrayList<>();
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client.player;
        if (player == null) return waypoints;

        BlockPos start = player.getBlockPos();
        double distance = Math.sqrt(start.getSquaredDistance(target));
        int steps = Math.max(1, (int)Math.ceil(distance / 3.0)); // Pontos mais próximos
        
        for (int i = 0; i <= steps; i++) {
            double t = (double)i / steps;
            int x = (int)(start.getX() + (target.getX() - start.getX()) * t);
            int y = (int)(start.getY() + (target.getY() - start.getY()) * t);
            int z = (int)(start.getZ() + (target.getZ() - start.getZ()) * t);
            y = getSafeY(client.world, x, y, z);
            waypoints.add(new BlockPos(x, y, z));
        }
        return waypoints;
    }

    private int getSafeY(World world, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        while (pos.getY() > 0 && world.getBlockState(pos).isAir()) {
            pos = pos.down();
        }
        if (!world.getBlockState(pos).isAir()) {
            pos = pos.up();
        }
        return Math.min(pos.getY() + 1, world.getTopY());
    }

    public void update() {
        if (path.isEmpty() || currentIndex >= path.size()) return;
        
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client.player;
        if (player == null || client.world == null) return;

        BlockPos nextPos = path.get(currentIndex);
        Vec3d nextVec = new Vec3d(nextPos.getX() + 0.5, nextPos.getY(), nextPos.getZ() + 0.5);
        Vec3d direction = nextVec.subtract(player.getPos()).normalize();
        
        // Definir direção
        player.setYaw((float)Math.toDegrees(Math.atan2(-direction.x, direction.z)));
        
        // Movimento contínuo
        client.options.forwardKey.setPressed(true);
        
        // Verificar e pular obstáculos
        handleJumping(client, player, nextPos);
        
        // Verificar se chegou
        if (player.getPos().distanceTo(nextVec) < 1.2) {
            currentIndex++;
        }
    }
    
    private void handleJumping(MinecraftClient client, PlayerEntity player, BlockPos targetPos) {
        BlockPos playerPos = player.getBlockPos();
        BlockPos frontPos = playerPos.offset(player.getHorizontalFacing());
        BlockPos frontUpper = frontPos.up();
        
        BlockState frontBlock = client.world.getBlockState(frontPos);
        BlockState frontUpBlock = client.world.getBlockState(frontUpper);
        
        // Detectar obstáculo ou necessidade de subir
        boolean needsToJump = !frontBlock.isAir() || 
                             (targetPos.getY() > playerPos.getY() + 1) ||
                             (!client.world.getBlockState(playerPos.up()).isAir());
        
        if (needsToJump && player.isOnGround()) {
            client.options.jumpKey.setPressed(true);
        } else {
            client.options.jumpKey.setPressed(false);
        }
    }

    public List<BlockPos> getPath() { return path; }
}
