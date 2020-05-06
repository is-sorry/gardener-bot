package com.syylacodes.gardener.bot;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

@Data
@NoArgsConstructor
@Slf4j
public class Party {
    private Long hostId;
    private List<Long> guestsId = new ArrayList<>();
    private String message;
    private Long botMessage;
    private Long invokeChannel;
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
        refreshPartyMessage();

    }

    public void manageParty() {
    }

    public void addUserToChannel(Long userId) {
        Guild guild = bot.getJda().getGuilds().get(0);
        TextChannel channel = guild.getTextChannelById(room.getId());
        Member member = guild.getMemberById(userId);
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(Color.GREEN).setDescription("Hey there " + member.getAsMention());
        channel.sendMessage("Hey there " + member.getAsMention()).queue();
        if (!guestsId.isEmpty())
            refreshPartyMessage();

    }

    public void sendPartyMessage(String message, MessageReceivedEvent event, Party party) throws ExecutionException, InterruptedException {
        Guild guild = event.getGuild();
        MessageChannel channel = event.getChannel();
        EmbedBuilder eb = new EmbedBuilder();
        message = message.trim();
        eb.setTitle(message.split(" ")[0]);
        eb.setDescription(message.substring(message.indexOf(" ")));
        eb.setColor(Color.GREEN);
        eb.addField("Host: ", guild.getMemberById(hostId).getAsMention(), true);
        eb.addField("Guests: ", " ", true);

        channel.sendMessage(eb.build()).queue(message1 -> {
            party.setBotMessage(message1.getIdLong());
            party.setInvokeChannel(message1.getChannel().getIdLong());
            message1.addReaction(Objects.requireNonNull(message1.getGuild().getEmoteById(bot.getConfig().getEmoji()))).queue();

        });

    }

    public void refreshPartyMessage() {
        TextChannel textChannel = bot.getJda().getTextChannelById(invokeChannel);

        EmbedBuilder eb = new EmbedBuilder();
        message = message.trim();
        eb.setTitle(message.split(" ")[0]);
        eb.setDescription(message.substring(message.indexOf(" ")));
        eb.setColor(Color.GREEN);

        eb.addField("Host: ", textChannel.getGuild().getMemberById(hostId).getAsMention(), true);
        StringBuilder guests = new StringBuilder();
        for (Long guest : guestsId) {
            guests.append(textChannel.getGuild().getMemberById(guest).getAsMention()).append(" ");
        }
        eb.addField("Guests: ", guests.toString(), true);

        textChannel.editMessageById(botMessage, eb.build()).queue();

    }

    public String toString() {
        return hostId + " " + guestsId.toString();
    }
}
