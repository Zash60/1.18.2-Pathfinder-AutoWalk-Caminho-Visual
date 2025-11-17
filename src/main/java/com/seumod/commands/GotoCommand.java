package com.seumod.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.seumod.pathfinder.PathfinderManager;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class GotoCommand {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            dispatcher.register(literal("goto")
                .then(argument("x", IntegerArgumentType.integer())
                    .then(argument("y", IntegerArgumentType.integer())
                        .then(argument("z", IntegerArgumentType.integer())
                            .executes(context -> {
                                int x = getInteger(context, "x");
                                int y = getInteger(context, "y");
                                int z = getInteger(context, "z");

                                // ANOTAÇÃO: Usamos o objeto Text moderno em vez de LiteralText
                                PathfinderManager.setTarget(new BlockPos(x, y, z));
                                context.getSource().sendFeedback(
                                    Text.literal("§aCalculando caminho para: [" + x + ", " + y + ", " + z + "]"),
                                    false
                                );
                                return 1;
                            })
                        )
                    )
                )
            );

            // ANOTAÇÃO: Adicionado um comando /stop para conveniência.
            dispatcher.register(literal("stop")
                .executes(context -> {
                    PathfinderManager.setEnabled(false);
                    context.getSource().sendFeedback(Text.literal("§cNavegação interrompida."), false);
                    return 1;
                })
            );
        });
    }
}
