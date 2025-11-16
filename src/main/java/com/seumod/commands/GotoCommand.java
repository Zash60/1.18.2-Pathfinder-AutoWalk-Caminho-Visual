package com.seumod.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.seumod.pathfinder.PathfinderManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class GotoCommand {
    public static void register() {
        net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            dispatcher.register(literal("goto")
                .then(argument("x", IntegerArgumentType.integer())
                    .then(argument("y", IntegerArgumentType.integer())
                        .then(argument("z", IntegerArgumentType.integer())
                            .executes(context -> {
                                int x = getInteger(context, "x");
                                int y = getInteger(context, "y");
                                int z = getInteger(context, "z");
                                
                                PathfinderManager.setTarget(x, y, z);
                                context.getSource().sendFeedback(
                                    new LiteralText("§aDestino: [" + x + ", " + y + ", " + z + "]"), 
                                    false
                                );
                                return 1;
                            })
                        )
                    )
                )
            );
        });
    }
}
