package com.syylacodes.gardener.bot;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@Slf4j
public class Party {
    private Long hostId;
    private List<Long> guestsId = new ArrayList<>();
    private String message;
    private Long botMessage;
    private Bot bot;
    private Room room;


    public Party(Bot bot, Long hostId, String message, Room room) {
        this.bot = bot;
        this.hostId = hostId;
        this.message = message;
        this.room = room;
        addUserToChannel(hostId);

    }

    public void addGuest(Long id) {
        guestsId.add(id);
        addUserToChannel(id);
    }

    public void removeGuest(Long id) {
        guestsId.remove(id);
    }

    public void manageParty() {
    }

    public void addUserToChannel(Long userId) {
        Guild guild = bot.getJda().getGuilds().get(0);
        TextChannel channel = guild.getTextChannelById(room.getId());
        Member member = guild.getMemberById(userId);
        try {
            channel.createPermissionOverride(member).setAllow(Permission.VIEW_CHANNEL).queue();
        } catch (Exception ignored) {

        }
    }
}
