package com.seumod.pathfinder;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
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

    // ANOTAÇÃO: A geração do caminho foi movida para seu próprio método para clareza.
    private List<BlockPos> calculateLinePath(BlockPos start, BlockPos target) {
        List<BlockPos> waypoints = new ArrayList<>();
        World world = MinecraftClient.getInstance().world;
        if (world == null) return Collections.emptyList();

        double distance = Math.sqrt(start.getSquaredDistance(target));
        int steps = Math.max(1, (int) Math.ceil(distance / 2.0)); // Pontos mais próximos para melhor ajuste ao terreno

        BlockPos lastPos = null;
        for (int i = 0; i <= steps; i++) {
            double t = (double) i / steps;
            int x = (int) (start.getX() + (target.getX() - start.getX()) * t);
            int y = (int) (start.getY() + (target.getY() - start.getY()) * t);
            int z = (int) (start.getZ() + (target.getZ() - start.getZ()) * t);
            
            BlockPos currentPos = new BlockPos(x, getSafeY(world, x, y, z), z);

            // Evita adicionar pontos duplicados
            if (!currentPos.equals(lastPos)) {
                waypoints.add(currentPos);
                lastPos = currentPos;
            }
        }
        return waypoints;
    }

    private int getSafeY(World world, int x, int startY, int z) {
        BlockPos.Mutable currentPos = new BlockPos.Mutable(x, startY, z);
        
        // Procura para baixo por um chão sólido
        for (int y = startY; y > world.getBottomY(); y--) {
            currentPos.setY(y);
            if (!world.getBlockState(currentPos).isAir()) {
                 // Sobe até encontrar espaço para o jogador
                return findHeadroom(world, currentPos) + 1;
            }
        }
        
        // Se não achou chão, procura para cima
        for (int y = startY; y < world.getTopY(); y++) {
             currentPos.setY(y);
             if (!world.getBlockState(currentPos).isAir()) {
                return findHeadroom(world, currentPos) + 1;
            }
        }
        
        return startY; // Fallback
    }

    private int findHeadroom(World world, BlockPos groundPos) {
        BlockPos.Mutable pos = new BlockPos.Mutable(groundPos.getX(), groundPos.getY() + 1, groundPos.getZ());
        while(pos.getY() < world.getTopY()) {
            if (world.getBlockState(pos).isAir() && world.getBlockState(pos.up()).isAir()) {
                return pos.getY() - 1; // Retorna a coordenada do chão
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
        Vec3d nextVecCenter = nextPos.toCenterPos();

        // ANOTAÇÃO: Verificação simples de obstáculo (parede)
        if (isPathObstructed(player.getEyePos(), nextVecCenter)) {
            player.sendMessage(net.minecraft.text.Text.literal("§cObstáculo detectado! Navegação interrompida."), true);
            PathfinderManager.setEnabled(false);
            return;
        }

        // Rotaciona o jogador para o próximo ponto
        Vec3d direction = nextVecCenter.subtract(player.getPos()).normalize();
        player.setYaw((float) Math.toDegrees(Math.atan2(-direction.x, direction.z)));
        
        client.options.forwardKey.setPressed(true);
        client.options.sprintKey.setPressed(player.getHungerManager().getFoodLevel() > 6);
        
        handleJumping(player, nextPos);
        
        // Avança para o próximo ponto do caminho
        if (player.getPos().distanceTo(nextVecCenter) < 1.8) {
            currentIndex++;
        }
    }
    
    // ANOTAÇÃO: Lógica de salto melhorada e mais proativa
    private void handleJumping(ClientPlayerEntity player, BlockPos nextNode) {
        if (!player.isOnGround()) return;

        MinecraftClient client = MinecraftClient.getInstance();

        // Pula se o próximo bloco do caminho estiver acima
        boolean shouldJump = nextNode.getY() > player.getBlockPos().getY();

        // Pula se houver um bloco na frente (lógica original)
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
    
    // ANOTAÇÃO: Nova função para verificar se há uma parede entre o jogador e o próximo ponto.
    private boolean isPathObstructed(Vec3d start, Vec3d end) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return true;

        RaycastContext context = new RaycastContext(start, end, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, client.player);
        BlockHitResult result = client.world.raycast(context);

        // Se o raycast acertou um bloco, o caminho está obstruído
        return result.getType() == BlockHitResult.Type.BLOCK;
    }

    public List<BlockPos> getPath() { return path; }
    public boolean isPathEmpty() { return path.isEmpty() || currentIndex >= path.size(); }
    public void clearPath() {
        path.clear();
        currentIndex = 0;
    }
            }
