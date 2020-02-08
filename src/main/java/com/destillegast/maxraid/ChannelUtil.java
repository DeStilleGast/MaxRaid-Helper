package com.destillegast.maxraid;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.function.Consumer;

/**
 * Created by DeStilleGast 2-2-2020
 */
public class ChannelUtil {

    public static void sendMessage(MessageChannel channel, String message){
//        if(channel.canTalk()){?
            channel.sendMessage(message).queue();
//        }
    }

    public static void sendMessage(MessageChannel channel, MessageEmbed message){
        sendMessage(channel, message, _ignored -> {});
    }
    public static void sendMessage(MessageChannel channel, MessageEmbed message, Consumer<Message> success){
//        if(channel.canTalk()){
            channel.sendMessage(message).queue(success);
//        }
    }

}
