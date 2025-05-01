package de.shiewk.smoderation.paper.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import de.shiewk.smoderation.paper.SModerationPaper;
import de.shiewk.smoderation.paper.inventory.SModMenu;
import de.shiewk.smoderation.paper.util.CommandUtil;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;

import static io.papermc.paper.command.brigadier.Commands.literal;

public final class SModCommand implements CommandProvider {

    @Override
    public LiteralCommandNode<CommandSourceStack> getCommandNode() {
        return literal("smod")
                .requires(CommandUtil.requirePermission("smod.menu"))
                .executes(this::openMenu)
                .build();
    }

    private int openMenu(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Player player = CommandUtil.getExecutingPlayer(context.getSource());
        new SModMenu(player, SModerationPaper.container).open();
        return Command.SINGLE_SUCCESS;
    }

    @Override
    public String getCommandDescription() {
        return "Shows the SModeration menu.";
    }

    @Override
    public Collection<String> getAliases() {
        return List.of("smodmenu", "smoderation");
    }
}
