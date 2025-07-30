package de.shiewk.smoderation.paper.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import de.shiewk.smoderation.paper.listener.SocialSpyListener;
import de.shiewk.smoderation.paper.util.CommandUtil;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;

import java.util.Collection;
import java.util.List;

import static io.papermc.paper.command.brigadier.Commands.literal;
import static net.kyori.adventure.text.Component.translatable;

public final class SocialSpyCommand implements CommandProvider {

    @Override
    public LiteralCommandNode<CommandSourceStack> getCommandNode() {
        return literal("socialspy")
                .requires(CommandUtil.requirePermission("smod.socialspy"))
                .executes(this::toggleSocialSpy)
                .build();
    }

    private int toggleSocialSpy(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.getSource().getSender();
        final boolean enabled = SocialSpyListener.toggle(sender);
        if (enabled){
            sender.sendMessage(translatable("smod.command.socialspy.enabled"));
        } else {
            sender.sendMessage(translatable("smod.command.socialspy.disabled"));
        }
        return Command.SINGLE_SUCCESS;
    }

    @Override
    public String getCommandDescription() {
        return "Enables SocialSpy mode (allows you to see private messages of other players)";
    }

    @Override
    public Collection<String> getAliases() {
        return List.of("smodsocialspy");
    }
}
