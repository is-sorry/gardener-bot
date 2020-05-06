package com.syylacodes.gardener.bot;

import lombok.Data;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.springframework.stereotype.Service;

import javax.security.auth.login.LoginException;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

@Service
@Data
public class Bot {

    private final Config config;
    private final HashMap<Long, Party> parties = new HashMap<>();
    private final HashMap<Long, Room> rooms = new HashMap<>();
    private final Queue queue;
    private JDA jda;

    public Bot(Config config, Queue queue) throws LoginException {
        jda = JDABuilder
                .createDefault(config.getToken())
                .addEventListeners(new Listener(config, this))
                .setActivity(Activity.watching("people water their gardens."))
                .enableIntents(GatewayIntent.GUILD_MEMBERS)
                .enableIntents(GatewayIntent.GUILD_PRESENCES)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .build();
        this.config = config;
        this.queue = queue;
    }

    public void createParty(Long userId, String message, MessageReceivedEvent event) {
        Room partyRoom = null;
        for (Room room : rooms.values()) {
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
            party.sendPartyMessage(message, event, party);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }



    public void removeParty(Long userId) {
        Party party = parties.remove(userId);
        rooms.get(party.getRoom().getId()).setOccupied(false);

    }

    public void updateParty(Long userId) {

    }


}
