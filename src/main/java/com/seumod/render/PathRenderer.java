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
// A linha de import incorreta foi removida. O Minecraft usará a classe Matrix4f do pacote net.minecraft.util.math implicitamente.

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

            matrices.push();
            matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

            // Configurações de renderização aprimoradas para melhor visibilidade
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            RenderSystem.disableCull();
            RenderSystem.disableDepthTest();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.lineWidth(3.0F); // Linha mais grossa

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();

            buffer.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
            for (BlockPos pos : path) {
                // Cores mais vibrantes
                buffer.vertex(matrices.peek().getPositionMatrix(), (float)(pos.getX() + 0.5), (float)(pos.getY() + 0.1), (float)(pos.getZ() + 0.5))
                    .color(1.0f, 0.2f, 0.2f, 0.8f).next();
            }
            tessellator.draw();

            BlockPos target = PathfinderManager.getTarget();
            if (target != null) {
                // A caixa do alvo agora é um cubo em vez de apenas linhas
                // O método WorldRenderer.drawBox funciona corretamente com o BufferBuilder
                WorldRenderer.drawBox(matrices, buffer, target.getX(), target.getY(), target.getZ(), target.getX() + 1, target.getY() + 1, target.getZ() + 1, 0.2f, 1.0f, 0.2f, 0.7f, 0.2f, 1.0f, 0.2f);
            }

            matrices.pop();
            RenderSystem.enableCull();
            RenderSystem.enableDepthTest();
            RenderSystem.lineWidth(1.0F);
        });
    }
}
