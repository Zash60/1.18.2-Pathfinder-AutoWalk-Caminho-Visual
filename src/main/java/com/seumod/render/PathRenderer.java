package com.seumod.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.seumod.pathfinder.PathfinderManager;
import com.seumod.pathfinder.SimplePathfinder;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class PathRenderer {
    public static void register() {
        WorldRenderEvents.AFTER_TRANSLUCENT.register(context -> {
            if (!PathfinderManager.isNavigationActive()) return;

            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null) return;

            SimplePathfinder pathExecutor = PathfinderManager.getPathExecutor();
            List<BlockPos> path = pathExecutor.getPath();
            if (path == null || path.isEmpty()) return;

            MatrixStack matrices = context.matrixStack();
            Camera camera = client.gameRenderer.getCamera();
            Vec3d cameraPos = camera.getPos();

            matrices.push();
            matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            RenderSystem.disableCull();
            RenderSystem.disableDepthTest();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.lineWidth(3.0F);

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();

            // --- Operação de Desenho 1: A Linha do Caminho ---
            buffer.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
            for (BlockPos pos : path) {
                buffer.vertex(matrices.peek().getPositionMatrix(), (float)(pos.getX() + 0.5), (float)(pos.getY() + 0.1), (float)(pos.getZ() + 0.5))
                    .color(1.0f, 0.2f, 0.2f, 0.8f).next();
            }
            tessellator.draw(); // Finaliza o desenho da linha

            // --- Operação de Desenho 2: A Caixa do Destino ---
            BlockPos target = PathfinderManager.getTarget();
            if (target != null) {
                // CORREÇÃO: Inicia uma NOVA operação de desenho para a caixa.
                buffer.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
                // O método WorldRenderer.drawBox adiciona os vértices ao buffer atual.
                WorldRenderer.drawBox(matrices, buffer, target.getX(), target.getY(), target.getZ(), target.getX() + 1, target.getY() + 1, target.getZ() + 1, 0.2f, 1.0f, 0.2f, 0.7f);
                tessellator.draw(); // Finaliza o desenho da caixa.
            }

            matrices.pop();
            RenderSystem.enableCull();
            RenderSystem.enableDepthTest();
            RenderSystem.lineWidth(1.0F);
        });
    }
}
