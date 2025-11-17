package com.seumod.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.seumod.pathfinder.PathfinderManager;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.text.LiteralText;
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
                                
                                PathfinderManager.setTarget(new BlockPos(x, y, z));
                                // A mensagem de feedback agora é enviada pelo PathfinderManager
                                return 1;
                            })
                        )
                    )
                )
            );
            
            dispatcher.register(literal("stop")
                .executes(context -> {
                    PathfinderManager.stop();
                    context.getSource().sendFeedback(new LiteralText("§cNavegação interrompida."), false);
                    return 1;
                })
            );
        });
    }
}
