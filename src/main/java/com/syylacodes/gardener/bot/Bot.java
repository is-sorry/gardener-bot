package com.syylacodes.gardener.bot;

import lombok.Data;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.springframework.stereotype.Service;

import javax.security.auth.login.LoginException;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

@Service
@Data
public class Bot {

    private final Config config;
    private final HashMap<Long, Party> parties = new HashMap<>();
    private final List<Room> rooms = new ArrayList<>();
    private JDA jda;

    public Bot(Config config) throws LoginException {
        jda = JDABuilder
                .createDefault(config.getToken())
                .addEventListeners(new Listener(config, this))
                .setActivity(Activity.watching("people water their gardens."))
                .enableIntents(GatewayIntent.GUILD_MEMBERS)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .build();
        this.config = config;
    }

    public void createParty(Long userId, String message, MessageReceivedEvent event) {
        Room partyRoom = null;
        for (Room room : rooms) {
            if (!room.isOccupied()) {
                room.setOccupied(true);
                partyRoom = room;
                break;
            }
        }
        if (partyRoom == null) {
            event.getChannel()
                    .sendMessage(new EmbedBuilder().setDescription("Watering rooms at capacity. Please try again after  a room has been freed").build())
                    .queue();
        }
        Party party = new Party(this, userId, message, partyRoom);
        parties.put(userId, party);
        try {
            sendPartyMessage(message, event, party);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void sendPartyMessage(String message, MessageReceivedEvent event, Party party) throws ExecutionException, InterruptedException {
        Guild guild = event.getGuild();
        MessageChannel channel = event.getChannel();
        EmbedBuilder eb = new EmbedBuilder();
        message = message.trim();
        eb.setTitle(message.split(" ")[0]);
        eb.setDescription(message.substring(message.indexOf(" ")));
        eb.setColor(Color.GREEN);

        channel.sendMessage(eb.build()).queue(message1 -> {
            party.setBotMessage(message1.getIdLong());
            message1.addReaction(Objects.requireNonNull(message1.getGuild().getEmoteById(config.getEmoji()))).queue();

        });


    }

    public void removeParty(Long userId) {

    }

    public void updateParty(Long userId) {

    }


}
