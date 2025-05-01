package de.shiewk.smoderation.paper.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import de.shiewk.smoderation.paper.listener.SocialSpyListener;
import de.shiewk.smoderation.paper.util.CommandUtil;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

import java.util.Collection;
import java.util.List;

import static de.shiewk.smoderation.paper.SModerationPaper.CHAT_PREFIX;
import static io.papermc.paper.command.brigadier.Commands.literal;
import static net.kyori.adventure.text.Component.text;

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
        sender.sendMessage(CHAT_PREFIX.append(text("SocialSpy ").append(
                enabled ?
                        text("enabled").color(NamedTextColor.GREEN) :
                        text("disabled").color(NamedTextColor.RED)
        ).append(text("."))));
        return Command.SINGLE_SUCCESS;
    }

    @Override
    public String getCommandDescription() {
        return "Enables socialspy mode (you can see private messages of other players)";
    }

    @Override
    public Collection<String> getAliases() {
        return List.of("smodsocialspy");
    }
}
