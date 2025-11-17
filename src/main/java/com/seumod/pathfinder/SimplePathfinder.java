package com.seumod.pathfinder;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.LiteralText; // CORREÇÃO: Import correto para 1.18.2
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SimplePathfinder {
    private List<BlockPos> path = new ArrayList<>();
    private int currentIndex = 0;

    public void createPath(BlockPos target) {
        this.path.clear();
        this.currentIndex = 0;
        
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        
        BlockPos start = client.player.getBlockPos();
        this.path = calculateLinePath(start, target);
    }
    
    private List<BlockPos> calculateLinePath(BlockPos start, BlockPos target) {
        List<BlockPos> waypoints = new ArrayList<>();
        World world = MinecraftClient.getInstance().world;
        if (world == null) return Collections.emptyList();

        double distance = Math.sqrt(start.getSquaredDistance(target));
        int steps = Math.max(1, (int) Math.ceil(distance / 2.0));

        BlockPos lastPos = null;
        for (int i = 0; i <= steps; i++) {
            double t = (double) i / steps;
            int x = (int) (start.getX() + (target.getX() - start.getX()) * t);
            int y = (int) (start.getY() + (target.getY() - start.getY()) * t);
            int z = (int) (start.getZ() + (target.getZ() - start.getZ()) * t);
            
            BlockPos currentPos = new BlockPos(x, getSafeY(world, x, y, z), z);

            if (!currentPos.equals(lastPos)) {
                waypoints.add(currentPos);
                lastPos = currentPos;
            }
        }
        return waypoints;
    }

    private int getSafeY(World world, int x, int startY, int z) {
        BlockPos.Mutable currentPos = new BlockPos.Mutable(x, startY, z);
        
        for (int y = startY; y > world.getBottomY(); y--) {
            currentPos.setY(y);
            if (!world.getBlockState(currentPos).isAir()) {
                return findHeadroom(world, currentPos) + 1;
            }
        }
        
        for (int y = startY; y < world.getTopY(); y++) {
             currentPos.setY(y);
             if (!world.getBlockState(currentPos).isAir()) {
                return findHeadroom(world, currentPos) + 1;
            }
        }
        
        return startY;
    }

    private int findHeadroom(World world, BlockPos groundPos) {
        BlockPos.Mutable pos = new BlockPos.Mutable(groundPos.getX(), groundPos.getY() + 1, groundPos.getZ());
        while(pos.getY() < world.getTopY()) {
            if (world.getBlockState(pos).isAir() && world.getBlockState(pos.up()).isAir()) {
                return pos.getY() - 1;
            }
            pos.move(0, 1, 0);
        }
        return groundPos.getY();
    }


    public void updateMovement() {
        if (isPathEmpty()) return;

        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null || client.world == null) return;

        BlockPos nextPos = path.get(currentIndex);
        // CORREÇÃO: Calculando o centro do bloco manualmente
        Vec3d nextVecCenter = new Vec3d(nextPos.getX() + 0.5, nextPos.getY() + 0.5, nextPos.getZ() + 0.5);

        if (isPathObstructed(player.getEyePos(), nextVecCenter)) {
            // CORREÇÃO: Usando new LiteralText()
            player.sendMessage(new LiteralText("§cObstáculo detectado! Navegação interrompida."), true);
            PathfinderManager.setEnabled(false);
            return;
        }

        Vec3d direction = nextVecCenter.subtract(player.getPos()).normalize();
        player.setYaw((float) Math.toDegrees(Math.atan2(-direction.x, direction.z)));
        
        client.options.forwardKey.setPressed(true);
        client.options.sprintKey.setPressed(player.getHungerManager().getFoodLevel() > 6);
        
        handleJumping(player, nextPos);
        
        if (player.getPos().distanceTo(nextVecCenter) < 1.8) {
            currentIndex++;
        }
    }
    
    private void handleJumping(ClientPlayerEntity player, BlockPos nextNode) {
        if (!player.isOnGround()) return;

        MinecraftClient client = MinecraftClient.getInstance();

        boolean shouldJump = nextNode.getY() > player.getBlockPos().getY();

        if (!shouldJump) {
             Vec3d forwardVec = Vec3d.fromPolar(0, player.getYaw()).normalize().multiply(0.6);
             BlockPos blockInFront = player.getBlockPos().add((int)forwardVec.x, 0, (int)forwardVec.z);
             BlockState stateInFront = client.world.getBlockState(blockInFront);
             
             if (!stateInFront.isAir() && client.world.getBlockState(blockInFront.up()).isAir()) {
                 shouldJump = true;
             }
        }
        
        client.options.jumpKey.setPressed(shouldJump);
    }
    
    private boolean isPathObstructed(Vec3d start, Vec3d end) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return true;

        RaycastContext context = new RaycastContext(start, end, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, client.player);
        BlockHitResult result = client.world.raycast(context);

        return result.getType() == BlockHitResult.Type.BLOCK;
    }

    public List<BlockPos> getPath() { return path; }
    public boolean isPathEmpty() { return path.isEmpty() || currentIndex >= path.size(); }
    public void clearPath() {
        path.clear();
        currentIndex = 0;
    }
}
