package de.shiewk.smoderation.paper.command;

import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;

import java.util.Collection;
import java.util.List;

public interface CommandProvider {

    LiteralCommandNode<CommandSourceStack> getCommandNode();

    String getCommandDescription();

    default Collection<String> getAliases(){
        return List.of();
    }

}
