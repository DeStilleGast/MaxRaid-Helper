package com.destillegast.maxraid.parsers;

import com.destillegast.maxraid.dex.CacheObject;
import com.destillegast.maxraid.dex.SerebiiEntry;
import com.destillegast.maxraid.parsers.reddit.ChildData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by DeStilleGast 5-2-2020
 */
public class AnnouncementParser extends ListenerAdapter {

    private CacheObject cacheObject;

    private Pattern ivPattern = Pattern.compile("((\\d|x){1,2})\\D((\\d|x){1,2})\\D((\\d|x){1,2})\\D((\\d|x){1,2})\\D((\\d|x){1,2})\\D((\\d|x){1,2})");
    private Pattern fcPattern = Pattern.compile("\\d{4}.\\d{4}.\\d{4}");
    private Pattern denPattern = Pattern.compile("den.{1,2}\\d{1,2}");
    private Pattern numberExtractorPattern = Pattern.compile("-?\\d+");
    private Pattern redditPattern = Pattern.compile("https:\\/\\/www\\.reddit.com\\/r\\/pokemonmaxraids\\/comments\\/(.{6})\\/");

    public AnnouncementParser(CacheObject cacheObject) {
        this.cacheObject = cacheObject;
    }

    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {
        if (event.getMessage().getContentRaw().startsWith("!!") && event.getAuthor() == event.getJDA().retrieveApplicationInfo().complete().getOwner()) {
            handleMessage(event.getChannel(), event.getMessage());
        }
    }

    public void handleMessage(TextChannel channel, Message message) {
        channel.sendMessage("Reading message...").queue(placeholder -> {

            String rawContent = message.getContentStripped().toLowerCase();

            List<SerebiiEntry> pokemons = findPokemons(rawContent);
            String IVs = findIVs(rawContent);
            boolean shiny = rawContent.contains("shiny");
            boolean gmax = rawContent.contains("gmax") || rawContent.contains("g-max");
            boolean den = rawContent.contains("den");
            TextChannel raidChannel = getTextChannel(message);

            String friendCode = findFC(rawContent);
            if (friendCode == null || friendCode.isEmpty()) {
                Member messageGuildMember = channel.getGuild().getMember(message.getAuthor());
                if (messageGuildMember != null)
                    friendCode = findFC(messageGuildMember.getEffectiveName());
            }


            boolean reddit = rawContent.contains("reddit");
            boolean noDoubleDip = rawContent.contains("double dip") || rawContent.contains("double-dip");
            boolean queue = rawContent.contains("queue");

            // todo: reddit link, friendly reminders

            EmbedBuilder embedBuilder = new EmbedBuilder();

            if (pokemons.size() == 1) {
                SerebiiEntry p = pokemons.get(0);
                p.applyToEmbed(embedBuilder);
                embedBuilder.setThumbnail(shiny ? p.getData().getShinyImage() : p.getData().getDexImage());
            }

            if (shiny) {
                boolean square = rawContent.contains("square shiny") || rawContent.contains("squareshiny");

                embedBuilder.addField("Shiny", square ? "Square" : "Normal", true);
            }

            if (gmax) {
                embedBuilder.addField("Gmax", "yes", true);
            }

            if (IVs != null && !IVs.isEmpty()) {
                embedBuilder.addField("IVs", IVs.replace('.', '/'), true);
            }

            if (den) {
                String denCode = findDen(rawContent);
                if (denCode != null && !denCode.isEmpty()) {
                    Matcher m = numberExtractorPattern.matcher(denCode);
                    if (m.find()) {
                        int denNumber = Integer.parseInt(m.group());

                        cacheObject.denData.stream().filter(denObject -> denObject.getNumber() == denNumber).findFirst().ifPresent(d ->
                                embedBuilder.addField("Pokemons from den " + denNumber, d.getPokemons().stream().map(SerebiiEntry::getName).collect(Collectors.joining(", ")), false)
                        );
                    }
                }
            }

            if (friendCode != null && !friendCode.isEmpty()) {
                embedBuilder.addField("Friendcode:", friendCode, false);
            }

            if (raidChannel != null) {
                embedBuilder.addField("Channel:", raidChannel.getAsMention(), true);
            }

            String redditUrl = "";
            if (reddit) {
                // https://www.reddit.com/r/pokemonmaxraids/comments/eziykj/the_darkest_night_shiny_gmax_charizard_campfire/
                Matcher m = redditPattern.matcher(rawContent);
                if (m.find()) {
                    ChildData postData = RedditParser.getThreadContent(m.group(1));
                    if (postData != null && postData.getSubreddit().equals("pokemonmaxraids")) {
                        embedBuilder.addField("Reddit thread:", "[thread](" + m.group() + ")", false);
                        redditUrl = m.group();

                        String r_iv = findIVs(postData.getSelftext());


                        if (r_iv != null && !r_iv.isEmpty()) {
                            embedBuilder.addField("IVs from thread:", r_iv, true);
                        }


                    }
                } else {
                    reddit = false;
                }
            }

            if (queue) {
                embedBuilder.addField("Queue", ".join", false);
            }

            if (noDoubleDip) {
                embedBuilder.appendDescription("Please don't double dip, if you have catched the pokemon give other people also a change to get it\n");
            }

            embedBuilder.appendDescription("After catching the pokemon, please unfriend the host, this will save the hoster time!");

            if (reddit) {
                embedBuilder.appendDescription("\n\nPlease comment 'Joined' on the specified [reddit thread](" + redditUrl + ") to help the hoster !");
            } else {
                embedBuilder.appendDescription("\n\nPlease click on the join reaction to show the host that you have joined !!");
            }

            embedBuilder.setFooter("Bot created by DeStilleGast");

            boolean finalReddit = reddit;
//            ChannelUtil.sendMessage(channel, embedBuilder.build(), success -> {
//                if (!finalReddit)
//                    success.addReaction("join:673949833538568221").queue();
//            });

            placeholder.editMessage(embedBuilder.build()).queue(success -> {
                if (!finalReddit) {
                    success.addReaction("join:673949833538568221").queue();
                }
                success.editMessage("Information from above").queue();
            });
        });
    }

    public List<SerebiiEntry> findPokemons(String content) {
        List<SerebiiEntry> entries = new ArrayList<>();
        content = content.toLowerCase();

        for (SerebiiEntry entry : cacheObject.serebiiDex) {
            String pokemonName = entry.getName().toLowerCase();
            if (content.contains(pokemonName) || content.contains(pokemonName.replace(" ", ""))) {
                entries.add(entry);
            }
        }

        return entries;
    }

    public String findIVs(String content) {
        Matcher m = ivPattern.matcher(content);

        return m.find() ? m.group() : null;
    }

    public TextChannel getTextChannel(Message message) {
        List<IMentionable> mentions = message.getMentions(Message.MentionType.CHANNEL);
        if (mentions.isEmpty()) return null;
        return (TextChannel) mentions.get(0);
    }

    public String findFC(String content) {
        Matcher m = fcPattern.matcher(content);

        return m.find() ? m.group() : null;
    }

    public String findDen(String content) {
        Matcher m = denPattern.matcher(content);

        return m.find() ? m.group() : null;
    }
}
