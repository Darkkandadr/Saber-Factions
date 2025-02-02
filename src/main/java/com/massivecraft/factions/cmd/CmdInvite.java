package com.massivecraft.factions.cmd;

import com.massivecraft.factions.Conf;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.zcore.fperms.PermissableAction;
import com.massivecraft.factions.zcore.util.TL;
import com.massivecraft.factions.zcore.util.TextUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;

public class CmdInvite extends FCommand {

    /**
     * @author FactionsUUID Team - Modified By CmdrKittens
     */

    public CmdInvite() {
        super();
        this.aliases.addAll(Aliases.invite);

        this.requiredArgs.add("player name");

        this.requirements = new CommandRequirements.Builder(Permission.INVITE)
                .playerOnly()
                .withAction(PermissableAction.INVITE)
                .build();
    }

    @Override
    public void perform(CommandContext context) {
        FPlayer target = context.argAsBestFPlayerMatch(0);
        if (target == null) {
            return;
        }

        if (target.getFaction() == context.faction) {
            context.msg(TL.COMMAND_INVITE_ALREADYMEMBER, target.getName(), context.faction.getTag());
            context.msg(TL.GENERIC_YOUMAYWANT + FactionsPlugin.getInstance().cmdBase.cmdKick.getUsageTemplate(context));
            return;
        }

        // if economy is enabled, they're not on the bypass list, and this command has a cost set, make 'em pay
        if (!context.payForCommand(Conf.econCostInvite, TL.COMMAND_INVITE_TOINVITE.toString(), TL.COMMAND_INVITE_FORINVITE.toString())) {
            return;
        }

        if (context.faction.isInvited(target)) {
            context.msg(TL.COMMAND_INVITE_ALREADYINVITED, target.getName());
            return;
        }

        if (context.faction.isBanned(target)) {
            context.msg(TL.COMMAND_INVITE_BANNED, target.getName());
            return;
        }

        context.faction.invite(target);
        // Send the invitation to the target player when online, otherwise just ignore
        if (target.isOnline()) {
            // Tooltips, colors, and commands only apply to the string immediately before it.
            Component message = TL.COMMAND_INVITE_INVITEDYOU.toFormattedComponent(context.fPlayer.describeTo(target, true), context.faction.getTag())
                    .hoverEvent(HoverEvent.showText(TL.COMMAND_INVITE_CLICKTOJOIN.toComponent()))
                    .clickEvent(ClickEvent.runCommand("/" + Conf.baseCommandAliases.get(0) + " join " + context.faction.getTag()));
            TextUtil.AUDIENCES.player(target.getPlayer()).sendMessage(message);
        }
        context.faction.msg(TL.COMMAND_INVITE_INVITED, context.fPlayer.describeTo(context.faction, true), target.describeTo(context.faction));
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_INVITE_DESCRIPTION;
    }
}
