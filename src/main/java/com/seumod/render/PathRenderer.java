package com.seumod.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.seumod.pathfinder.PathfinderManager;
import com.seumod.pathfinder.SimplePathfinder;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack; // <-- ADD THIS LINE
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import java.util.List;

public class PathRenderer {
    public static void register() {
        WorldRenderEvents.AFTER_TRANSLUCENT.register(context -> {
            if (!PathfinderManager.isEnabled()) return;
            
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null) return;
            
            SimplePathfinder pathfinder = PathfinderManager.getPathfinder();
            List<BlockPos> path = pathfinder.getPath();
            if (path.isEmpty()) return;

            MatrixStack matrices = context.matrixStack();
            Camera camera = client.gameRenderer.getCamera();
            Vec3d cameraPos = camera.getPos();
            
            // Mover para espaço do mundo
            matrices.push();
            matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
            
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            RenderSystem.disableDepthTest();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            
            // Desenhar linhas do caminho (vermelho) - NO NÍVEL DO CHÃO
            buffer.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
            for (BlockPos pos : path) {
                double x = pos.getX() + 0.5;
                double y = pos.getY() + 0.1; // Nível do pé do personagem
                double z = pos.getZ() + 0.5;
                
                buffer.vertex(matrices.peek().getPositionMatrix(), (float)x, (float)y, (float)z)
                    .color(255, 0, 0, 200).next();
            }
            tessellator.draw();
            
            // Desenhar caixa do destino (verde)
            BlockPos target = PathfinderManager.getTarget();
            if (target != null) {
                buffer.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
                drawBox(buffer, matrices, target);
                tessellator.draw();
            }
            
            matrices.pop();
            RenderSystem.enableDepthTest();
        });
    }
    
    private static void drawBox(BufferBuilder buffer, MatrixStack matrices, BlockPos pos) {
        float x = pos.getX(), y = pos.getY(), z = pos.getZ();
        
        // Base
        addLine(buffer, matrices, x, y, z, x + 1, y, z);
        addLine(buffer, matrices, x + 1, y, z, x + 1, y, z + 1);
        addLine(buffer, matrices, x + 1, y, z + 1, x, y, z + 1);
        addLine(buffer, matrices, x, y, z + 1, x, y, z);
        
        // Topo
        addLine(buffer, matrices, x, y + 1, z, x + 1, y + 1, z);
        addLine(buffer, matrices, x + 1, y + 1, z, x + 1, y + 1, z + 1);
        addLine(buffer, matrices, x + 1, y + 1, z + 1, x, y + 1, z + 1);
        addLine(buffer, matrices, x, y + 1, z + 1, x, y + 1, z);
        
        // Verticais
        addLine(buffer, matrices, x, y, z, x, y + 1, z);
        addLine(buffer, matrices, x + 1, y, z, x + 1, y + 1, z);
        addLine(buffer, matrices, x + 1, y, z + 1, x + 1, y + 1, z + 1);
        addLine(buffer, matrices, x, y, z + 1, x, y + 1, z + 1);
    }
    
    private static void addLine(BufferBuilder buffer, MatrixStack matrices, double x1, double y1, double z1, double x2, double y2, double z2) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        buffer.vertex(matrix, (float)x1, (float)y1, (float)z1).color(0, 255, 0, 255).next();
        buffer.vertex(matrix, (float)x2, (float)y2, (float)z2).color(0, 255, 0, 255).next();
    }
}
