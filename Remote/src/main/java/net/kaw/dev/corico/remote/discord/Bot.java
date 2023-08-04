/*
 * MIT License
 * 
 * Copyright (c) 2023 Kawtious
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package net.kaw.dev.corico.remote.discord;

import java.io.IOException;
import java.util.EnumSet;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import me.mattco98.voicemeeter.Voicemeeter;
import me.mattco98.voicemeeter.VoicemeeterException;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import static net.dv8tion.jda.api.interactions.commands.OptionType.*;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

public class Bot extends ListenerAdapter {

    private final Configuration config;

    private JDA jda;

    public Bot(Configuration config) {
        this.config = config;
        this.runBot();
    }

    private void runBot() {
        JDABuilder builder = JDABuilder.createLight(this.config.getBotToken(), EnumSet.noneOf(GatewayIntent.class)) // slash commands don't need any intents
                .addEventListeners(this);

        configureMemoryUsage(builder);

        this.jda = builder.build();

        createCommands(jda);
    }

    private void configureMemoryUsage(JDABuilder builder) {
        // Disable cache for member activities (streaming/games/spotify)
        builder.disableCache(CacheFlag.ACTIVITY, CacheFlag.MEMBER_OVERRIDES, CacheFlag.VOICE_STATE);

        // Disable member chunking on startup
        builder.setChunkingFilter(ChunkingFilter.NONE);

        // Disable presence updates and typing events
        builder.disableIntents(GatewayIntent.GUILD_PRESENCES, GatewayIntent.GUILD_MESSAGE_TYPING);

        // Consider guilds with more than 50 members as "large". 
        // Large guilds will only provide online members in their setup and thus reduce bandwidth if chunking is disabled.
        builder.setLargeThreshold(50);
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        // Only accept commands in DMs
        if (event.getGuild() != null) {
            return;
        }

        if (!event.getUser().getId().equals(config.getOwnerId())) {
            // in case owner id is invalid display this instead
            event.reply("You are not my owner").queue();

            return;
        }

        switch (event.getName()) {
            case "say":
                say(event);
                break;
            case "taskkill":
                taskkill(event);
                break;
            case "exec":
                exec(event);
                break;
            case "voicemeeter":
                voicemeeter(event);
                break;
            default:
                event.reply("I can't handle that command right now :(").setEphemeral(true).queue();
        }
    }

    private void createCommands(JDA api) {
        // These commands might take a few minutes to be active after creation/update/delete
        CommandListUpdateAction commands = api.updateCommands();

        // Simple reply commands
        commands.addCommands(
                Commands.slash("say", "Makes the bot say what you tell it to")
                        .addOption(STRING, "content", "What the bot should say", true)
        );

        commands.addCommands(
                Commands.slash("mute", "Voicemeeter: Mute yourself")
        );

        commands.addCommands(
                Commands.slash("taskkill", "Kill task on remote machine")
                        .addOption(STRING, "name", "Name of the task to kill (including dot and extension)", true)
        );

        commands.addCommands(
                Commands.slash("exec", "Execute a predefined task on remote machine")
                        .addOption(STRING, "task", "Task to execute", true)
        );

        commands.addCommands(
                Commands.slash("voicemeeter", "Modify a parameter in Voicemeeter")
                        .addOption(STRING, "parameter", "Which parameter to modify", true)
                        .addOption(STRING, "value", "The new value of the parameter", true)
        );

        // Send the new set of commands to discord, this will override any existing global commands with the new set provided here
        commands.queue();
    }

    private void say(SlashCommandInteractionEvent event) {
        String content = event.getOption("content").getAsString();  // content is required so no null-check here
        event.reply(content).queue(); // This requires no permissions!
    }

    /**
     * Dangerous...
     *
     * @param event
     */
    private void taskkill(SlashCommandInteractionEvent event) {
        String name = event.getOption("name").getAsString();

        try {
            Runtime.getRuntime().exec("taskkill /F /IM " + name);
        } catch (IOException ex) {
            Logger.getLogger(Bot.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Even more dangerous...
     *
     * @param event
     */
    private void exec(SlashCommandInteractionEvent event) {
        String task = event.getOption("task").getAsString();

        String reply;

        try {
            switch (task) {
                case "tf2server":
                    Runtime.getRuntime().exec(Tasks.TF2_SERVER.command());

                    reply = "Running TF2 server";
                    break;
                default:
                    reply = "I don't recognize that task";
            }

            event.reply(reply).queue(); // This requires no permissions!
        } catch (IOException ex) {
            Logger.getLogger(Bot.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void voicemeeter(SlashCommandInteractionEvent event) {
        String parameter = event.getOption("parameter").getAsString();
        String value = event.getOption("value").getAsString();

        float floatValue;

        try {
            floatValue = Float.parseFloat(value);

            Voicemeeter.setParameterFloat(parameter, floatValue);

            event.reply("Set parameter " + parameter + " to " + value).queue();
        } catch (VoicemeeterException ex) {
            event.reply("Parameter (" + parameter + ") is invalid").queue();
        } catch (NumberFormatException ex) {
            event.reply("Parameter value (" + value + ") is invalid").queue();
        }

        // Voicemeeter.setParameterFloat("Bus[3].Mute", 1);
        // event.reply("Muted").queue();
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String[] id = event.getComponentId().split(":"); // this is the custom id we specified in our button
        String authorId = id[0];
        String type = id[1];
        // Check that the button is for the user that clicked it, otherwise just ignore the event (let interaction fail)
        if (!authorId.equals(event.getUser().getId())) {
            return;
        }
        event.deferEdit().queue(); // acknowledge the button was clicked, otherwise the interaction will fail

        MessageChannel channel = event.getChannel();
        switch (type) {
            case "prune":
                int amount = Integer.parseInt(id[2]);
                event.getChannel().getIterableHistory()
                        .skipTo(event.getMessageIdLong())
                        .takeAsync(amount)
                        .thenAccept(channel::purgeMessages);
            // fallthrough delete the prompt message with our buttons
            case "delete":
                event.getHook().deleteOriginal().queue();
        }
    }

    @Deprecated
    private void createExampleCommands(JDA api) {
        // These commands might take a few minutes to be active after creation/update/delete
        CommandListUpdateAction commands = api.updateCommands();

        // Moderation commands with required options
        commands.addCommands(
                Commands.slash("ban", "Ban a user from this server. Requires permission to ban users.")
                        .addOptions(new OptionData(USER, "user", "The user to ban") // USER type allows to include members of the server or other users by id
                                .setRequired(true)) // This command requires a parameter
                        .addOptions(new OptionData(INTEGER, "del_days", "Delete messages from the past days.") // This is optional
                                .setRequiredRange(0, 7)) // Only allow values between 0 and 7 (inclusive)
                        .addOptions(new OptionData(STRING, "reason", "The ban reason to use (default: Banned by <user>)")) // optional reason
                        .setGuildOnly(true) // This way the command can only be executed from a guild, and not the DMs
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.BAN_MEMBERS)) // Only members with the BAN_MEMBERS permission are going to see this command
        );

        // Commands without any inputs
        commands.addCommands(
                Commands.slash("leave", "Make the bot leave the server")
                        .setGuildOnly(true) // this doesn't make sense in DMs
                        .setDefaultPermissions(DefaultMemberPermissions.DISABLED) // only admins should be able to use this command.
        );

        commands.addCommands(
                Commands.slash("prune", "Prune messages from this channel")
                        .addOption(INTEGER, "amount", "How many messages to prune (Default 100)") // simple optional argument
                        .setGuildOnly(true)
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MESSAGE_MANAGE))
        );

        // Send the new set of commands to discord, this will override any existing global commands with the new set provided here
        commands.queue();
    }

    @Deprecated
    private void ban(SlashCommandInteractionEvent event) {
        Member member = event.getOption("user").getAsMember(); // the "user" option is required, so it doesn't need a null-check here
        User user = event.getOption("user").getAsUser();

        event.deferReply(true).queue(); // Let the user know we received the command before doing anything else
        InteractionHook hook = event.getHook(); // This is a special webhook that allows you to send messages without having permissions in the channel and also allows ephemeral messages
        hook.setEphemeral(true); // All messages here will now be ephemeral implicitly
        if (!event.getMember().hasPermission(Permission.BAN_MEMBERS)) {
            hook.sendMessage("You do not have the required permissions to ban users from this server.").queue();
            return;
        }

        Member selfMember = event.getGuild().getSelfMember();
        if (!selfMember.hasPermission(Permission.BAN_MEMBERS)) {
            hook.sendMessage("I don't have the required permissions to ban users from this server.").queue();
            return;
        }

        if (member != null && !selfMember.canInteract(member)) {
            hook.sendMessage("This user is too powerful for me to ban.").queue();
            return;
        }

        // optional command argument, fall back to 0 if not provided
        int delDays = event.getOption("del_days", 0, OptionMapping::getAsInt); // this last part is a method reference used to "resolve" the option value

        // optional ban reason with a lazy evaluated fallback (supplier)
        String reason = event.getOption("reason",
                () -> "Banned by " + event.getUser().getName(), // used if getOption("reason") is null (not provided)
                OptionMapping::getAsString); // used if getOption("reason") is not null (provided)

        // Ban the user and send a success response
        event.getGuild().ban(user, delDays, TimeUnit.DAYS)
                .reason(reason) // audit-log ban reason (sets X-AuditLog-Reason header)
                .flatMap(v -> hook.sendMessage("Banned user " + user.getName())) // chain a followup message after the ban is executed
                .queue(); // execute the entire call chain
    }

    @Deprecated
    private void leave(SlashCommandInteractionEvent event) {
        if (!event.getMember().hasPermission(Permission.KICK_MEMBERS)) {
            event.reply("You do not have permissions to kick me.").setEphemeral(true).queue();
        } else {
            event.reply("Leaving the server... :wave:") // Yep we received it
                    .flatMap(v -> event.getGuild().leave()) // Leave server after acknowledging the command
                    .queue();
        }
    }

    @Deprecated
    private void prune(SlashCommandInteractionEvent event) {
        OptionMapping amountOption = event.getOption("amount"); // This is configured to be optional so check for null
        int amount = amountOption == null
                ? 100 // default 100
                : (int) Math.min(200, Math.max(2, amountOption.getAsLong())); // enforcement: must be between 2-200
        String userId = event.getUser().getId();
        event.reply("This will delete " + amount + " messages.\nAre you sure?") // prompt the user with a button menu
                .addActionRow(// this means "<style>(<id>, <label>)", you can encode anything you want in the id (up to 100 characters)
                        Button.secondary(userId + ":delete", "Nevermind!"),
                        Button.danger(userId + ":prune:" + amount, "Yes!")) // the first parameter is the component id we use in onButtonInteraction above
                .queue();
    }
}
