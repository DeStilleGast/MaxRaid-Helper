package com.destillegast.maxraid.queue;

import com.destillegast.maxraid.ChannelUtil;
import com.destillegast.maxraid.UserUtil;
import com.destillegast.maxraid.dex.CacheObject;
import com.destillegast.maxraid.dex.Effectiveness;
import com.destillegast.maxraid.dex.PokeData;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.internal.utils.PermissionUtil;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created by DeStilleGast 2-2-2020
 */
public class RaidCreator extends ListenerAdapter {

    private User host;
    private boolean shiny, gmax, hiddenAbility, queue, isDone;
    private PokeData pokemon;
    private int den;

//    private TextChannel channel;
    private String friendCode, reddit, stats;

    @Getter
    private UUID announceId = UUID.randomUUID();

    private JDA bot;
    private Message message;

    private Activity.Emoji shinyEmote = new Activity.Emoji("🌟"),
            gmaxEmote = new Activity.Emoji("Gigantamax", 673957290990239747L, false);
//            hiddenAbilityEmote = "\uD83C\uDDED", // square H
//            startRaidEmote = "\u25b6"; // Play button ▶

    private CacheObject cacheObject;

    public RaidCreator(JDA jda, Message startMessage, CacheObject cacheObject) {
        this.bot = jda;
        this.cacheObject = cacheObject;

        host = startMessage.getAuthor();
        startCreator(startMessage);
    }

    private void startCreator(Message beginMessage){
        UserUtil.sendDM(beginMessage.getAuthor(), "Hello, so you want to start a raid, I'm now im now in interactive mode (I will listen to your chats here without command prefix)\n\n" +
                "```\n" +
                "What should you do:\n" +
                "providing me information, like the name, shiny, ability or den number\n" +
                "\n" +
                "some examples to tell me:\n" +
                "'name: cinderace'\n" +
                "'den 10'\n" +
                "'ability: steelworker'\n" +
                "\n" +
                "argument that I currently recognize are [name or pokemon, gmax, shiny, stats, den, fc, queue, reddit, done]" +
                "```", success -> { beginMessage.getJDA().addEventListener(RaidCreator.this); }, error -> {
            ChannelUtil.sendMessage(beginMessage.getTextChannel(), "Please open up your DM's to setup a raid");
        });

        if(beginMessage.getChannelType() != ChannelType.PRIVATE) {
            if (PermissionUtil.checkPermission(beginMessage.getTextChannel(), beginMessage.getGuild().getSelfMember(), Permission.MESSAGE_MANAGE)) {
                beginMessage.delete().queue();
            }
        }
    }


    @Override
    public void onPrivateMessageReceived(@Nonnull PrivateMessageReceivedEvent event) {
        if(event.getAuthor() != host || isDone) return;


        HashMap<String, Consumer<String>> readers = new HashMap<>();
        readers.put("name", (msg) -> { pokemon = cacheObject.dexCache.getPokeDate(msg); });
        readers.put("pokemon", (msg) -> { pokemon = cacheObject.dexCache.getPokeDate(msg); });
        readers.put("shiny", (msg) -> shiny =! shiny);
        readers.put("gmax", (msg) -> gmax =! gmax);
        readers.put("den", (msg) -> { try{ den = Integer.parseInt(msg); }catch (Exception ignored){}});
        readers.put("fc", (msg) -> friendCode = msg);
        readers.put("queue", (msg) -> queue =! queue);
        readers.put("reddit", (msg) -> {
            if (msg.startsWith("http")) {
                reddit = msg.substring(msg.indexOf("comments/")).split("/")[0];
            }else{
                reddit = msg;
            } });
        readers.put("stats", (msg) -> stats = msg);
        readers.put("done", (msg) -> isDone = true);



        String message = event.getMessage().getContentRaw();
        String cmd = message.split(" ")[0].replace(":", "");

        readers.forEach((cmdName, cmdConsume) -> {
            if(cmd.equalsIgnoreCase(cmdName)) cmdConsume.accept(message.substring(cmd.length()).replace(":", "").trim());
        });

        sendCurrentStats(event.getChannel());
    }

    private void sendCurrentStats(PrivateChannel channel){
        EmbedBuilder embedBuilder = populateEmbedBuilder(false);

        ChannelUtil.sendMessage(channel, embedBuilder.build());

        if(isDone){
            bot.removeEventListener(this);
            ChannelUtil.sendMessage(channel, String.format("You are done creating a raid, your ID is `%s`", announceId));
            cacheObject.activeRaid.add(this);
        }
    }

    private EmbedBuilder populateEmbedBuilder(boolean extraDetails){
        EmbedBuilder embedBuilder = new EmbedBuilder();

        if(pokemon != null){
            embedBuilder.addField("Pokemon:", (gmax ? new Activity.Emoji("Gigantamax", 673957290990239747L, false).getAsMention() : "") + pokemon.getName(), false);
            embedBuilder.setThumbnail(shiny ? pokemon.getShinyImage() : pokemon.getDexImage());
            if(extraDetails){
                embedBuilder.addField("Weakness:", pokemon.getEffectiveScheme().stream().filter(e -> e.getPower() > 1).map(Effectiveness::getName).collect(Collectors.joining(", ")), true);
            }
        }

        if(shiny){
            embedBuilder.addField("Shiny:", "yes", false);
        }

        if(hiddenAbility){
            embedBuilder.addField("Ability:", "Hidden ability", false);
        }

        if(stats != null && !stats.isEmpty()){
            embedBuilder.addField("Stats:", stats, false);
        }

        if(den != 0){
            embedBuilder.addField("Den: [0 to remove]", den + "", false);
        }

        if(friendCode != null && !friendCode.isEmpty()){
            embedBuilder.addField("Friendcode:", friendCode, false);
        }

        if(queue) {
            embedBuilder.addField("Queue:", "enabled", false);
        }

        if(reddit != null && !reddit.isEmpty()){
            if(extraDetails){
                embedBuilder.setDescription(String.format("Please comment 'Joined' if you managed to join the raid it would be also awesome if you mentioned if you catch the pokemon, [Link to thread here](https://www.reddit.com/r/pokemonmaxraids/comments/%s)", reddit));
            }else {
                embedBuilder.addField("Reddit: ", String.format("[link](https://www.reddit.com/r/pokemonmaxraids/comments/%s)", reddit), false);
            }
        }

        return embedBuilder;
    }


    public MessageEmbed getAnnounceEmbed(){
        return populateEmbedBuilder(true).build();
    }

//    @Override
//    public void onPrivateMessageReactionRemove(@Nonnull PrivateMessageReactionRemoveEvent event) {
//        if(event.getUser() != event.getJDA().getSelfUser()){
//            if(event.getMessageIdLong() == message.getIdLong()){
//                doToggleStuff(event.getReactionEmote().getName());
//            }
//        }
//    }
//
//    @Override
//    public void onPrivateMessageReactionAdd(@Nonnull PrivateMessageReactionAddEvent event) {
//        if(event.getUser() != event.getJDA().getSelfUser()){
//            if(event.getMessageIdLong() == message.getIdLong()){
//                doToggleStuff(event.getReactionEmote().getName());
//            }
//        }
//    }
//
//    private void doToggleStuff(String emoteName){
////        String emoteName = event.getReactionEmote().getName();
//        if(emoteName.equals(shinyEmote)){
//            shiny = !shiny;
//            message.editMessage(makeEmbed().build()).queue();
//        }else if(emoteName.equals(gmaxEmote.split(":")[0])){
//            gmax = !gmax;
//            message.editMessage(makeEmbed().build()).queue();
//        }else if(emoteName.equals(hiddenAbilityEmote)){
//            hiddenAbility = !hiddenAbility;
//            message.editMessage(makeEmbed().build()).queue();
//        }
//    }


    private EmbedBuilder makeEmbed(){
        EmbedBuilder eb = new EmbedBuilder();
        eb.addField("Shiny", formatYesNo(shiny), false);
        eb.addField("G-Max", formatYesNo(gmax), false);
        eb.addField("Hidden ability", formatYesNo(hiddenAbility), false);

        return eb;
    }

    public String formatYesNo(boolean b){
        return b ? "Yes" : "No";
    }
}
