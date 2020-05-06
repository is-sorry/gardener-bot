package com.syylacodes.gardener.bot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.HashMap;

@Slf4j
@RequiredArgsConstructor
@Component
public class Listener extends ListenerAdapter {
    private final Config config;
    private final Bot bot;

    @Override
    public void onReady(ReadyEvent event) {
        log.info("I'm ready!");
        JDA jda = bot.getJda();
        Guild guild = jda.getGuilds().get(0);

        if (guild.getCategoriesByName("watering rooms", true).isEmpty())
            guild.createCategory("Watering rooms")
                    .queue(category -> {
                                for (int i = 1; i <= 3; i++)
                                    guild
                                            .createTextChannel("Room " + i)
                                            .setParent(category)
                                            .queue();

                            }
                    );

        guild.getCategoriesByName("watering rooms", true).get(0)
                .getChannels().forEach(guildChannel -> bot.getRooms().put(guildChannel.getIdLong(), new Room(guildChannel.getIdLong())));
    }


    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot())
            return;
        String message = event.getMessage().getContentDisplay();
        String command = message.contains(" ") ? message.split(" ")[0] : message;
        command = command.replace(config.getPrefix(), "");
        switch (command) {
            case "start": {
                bot.createParty(event.getAuthor().getIdLong(), message.substring(message.indexOf(" ")), event);
                break;
            }
            case "end": {
                bot.removeParty(event.getAuthor().getIdLong());
                break;
            }
            case "parties": {
                //TODO: make this not be dumb (dereference IDs, prettify strings, embed)
                event.getChannel().sendMessage(bot.getParties().toString()).queue();
                break;
            }
        }
    }

    @Override
    public void onMessageReactionAdd(@Nonnull MessageReactionAddEvent event) {
        HashMap<Long, Party> parties = bot.getParties();
        if (event.getUserIdLong() == bot.getJda().getSelfUser().getIdLong())
            return;

        parties.forEach((aLong, party) -> {
            if (party.getBotMessage() == event.getMessageIdLong())
                if (event.getReactionEmote().getIdLong() == config.getEmoji()) {
                    if (event.getUserIdLong() != party.getHostId()) {
                        party.addGuest(event.getUserIdLong());
                    } else {
                        //TODO:embed
                        event.getChannel().sendMessage("You can't water for yourself, silly!").queue();
                    }
                }
        });

    }
}
