package com.destillegast.maxraid;

import com.destillegast.maxraid.dex.*;
import com.destillegast.maxraid.queue.ChannelQueue;
import com.destillegast.maxraid.queue.QueueManager;
import com.destillegast.maxraid.queue.RaidCreator;
import lombok.Setter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import javax.swing.text.html.Option;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by DeStilleGast 2-2-2020
 */
public class CommandListener extends ListenerAdapter {

    @Setter
    private String prefix;
    private QueueManager queueManager;

    private CacheObject cache;
    private DexCache dc;

    public CommandListener(String prefix, CacheObject cache, DexCache dc, QueueManager queueManager) {
        this.prefix = prefix;
        this.cache = cache;
        this.dc = dc;
        this.queueManager = queueManager;
    }

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        if (event.getChannelType() == ChannelType.TEXT || event.getChannelType() == ChannelType.PRIVATE) {
            if (event.getMessage().getContentRaw().startsWith(prefix)) {
                String rawMessage = event.getMessage().getContentRaw();
                String command = rawMessage.substring(prefix.length()).split(" ")[0];

                List<String> args = extractArguments(rawMessage.substring(prefix.length() + command.length()));

                User user = event.getAuthor();
                MessageChannel channel = event.getChannel();

                ChannelQueue cq = queueManager.getQueue(channel);


                switch (command.toLowerCase()) {
                    case "test":
                        event.getMessage().addReaction("join:673949833538568221").queue();
                        event.getMessage().addReaction("Gigantamax:673957290990239747").queue();

                        break;
                    case "dex":
                    case "pokedex":
                        if(args.size() > 0 && !args.get(0).isEmpty()) {
                            PokeData pd = dc.getPokeDate(args.get(0));
                            if(pd == null){
                                ChannelUtil.sendMessage(event.getTextChannel(), "Could not find pokemon, try it between quotes");
                            }else{
                                EmbedBuilder eb = new EmbedBuilder();
                                eb.addField("Pokemon:", pd.getName(), false);
                                eb.addField("Type:", pd.getTypes(), false);
                                eb.setImage(pd.getDexImage());
                                eb.addField("Weakness:", pd.getEffectiveScheme().stream().filter(e -> e.getPower() > 1).map(Effectiveness::getName).collect(Collectors.joining(", ")), false);

                                event.getTextChannel().sendMessage(eb.build()).queue();
                            }
                        }else {
                            ChannelUtil.sendMessage(event.getTextChannel(), "Please give me a name of a pokemon (galar dex)");
                        }

                        break;
                    case "den":
                        if(args.size() > 0 && !args.get(0).isEmpty()) {
                            try{
                                int denNumber = Integer.parseInt(args.get(0));

                                Optional<Den> denOptional = cache.denData.stream().filter(den -> den.getNumber() == denNumber).findFirst();

                                if(denOptional.isPresent()){
                                    ChannelUtil.sendMessage(event.getTextChannel(), "Pokemons in this den: " + denOptional.get().getPokemons().stream().map(SerebiiEntry::getName).collect(Collectors.joining(", ")));
                                }

                            }catch (NumberFormatException e){
                                ChannelUtil.sendMessage(event.getTextChannel(), "Invalid number, try again");
                            }

                        }else{
                            ChannelUtil.sendMessage(event.getTextChannel(), "Please give me a den number");
                        }
                            break;
                    case "create":
                        if (cq != null) {
                            ChannelUtil.sendMessage(channel, "There is already a queue here active !");
                            return;
                        }

                        new RaidCreator(event.getJDA(), event.getMessage(), cache);

                        break;
                    case "display":
                        if(args.size() == 0){
                            ChannelUtil.sendMessage(channel, "Please declare a ID");
                        }else{
                            Optional<RaidCreator> optionalRaid = cache.activeRaid.stream().filter(r -> r.getAnnounceId().toString().equals(args.get(0))).findFirst();
                            if(optionalRaid.isPresent()){
                                ChannelUtil.sendMessage(channel, optionalRaid.get().getAnnounceEmbed());
                            }else{
                                ChannelUtil.sendMessage(channel, "No raid found");
                            }
                        }
                        break;
                    case "join":
                        if (cq != null) {
                            cq.queueUser(event.getMessage());
                        } else {
                            ChannelUtil.sendMessage(channel, "There is no queue active here !");
                            return;
                        }
                        break;
                    case "leave":
                        if (cq != null) {
                            if (cq.leaveQueue(user)) {
                                UserUtil.sendDM(user, "You have left the queue", success -> {
//                                    event.getMessage().addReaction()
                                });
                            } else {
                                UserUtil.sendDM(user, "You are not in this queue !");
                            }
                        }
                }
            }
        }
    }

    public static List<String> extractArguments(String str){
        List<String> args = new ArrayList<>();
        String regex = "\"([^\"]*)\"|(\\S+)";

        Matcher m = Pattern.compile(regex).matcher(str);
        while (m.find()) {
            if (m.group(1) != null) {
                args.add(m.group(1));
//                System.out.println("Quoted [" + m.group(1) + "]");
            } else {
                args.add(m.group(2));
//                System.out.println("Plain [" + m.group(2) + "]");
            }
        }
        return args;
    }
}
