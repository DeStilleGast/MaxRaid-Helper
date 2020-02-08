package com.destillegast.maxraid;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;

import java.util.function.Consumer;

/**
 * Created by DeStilleGast 2-2-2020
 */
public class UserUtil {

    public static void sendDM(User user, String message){
        sendDM(user, message, null);
    }

    public static void sendDM(User user, String message, Consumer<Throwable> failed){
        sendDM(user, message, null, failed);
    }

    public static void sendDM(User user, String message, Consumer<Message> success, Consumer<Throwable> failed){
        user.openPrivateChannel()
                .flatMap ( privateChannel -> privateChannel.sendMessage(message) )
                .queue(success, failed);
    }

    public static void sendDM(User user, MessageEmbed message){
        sendDM(user, message, null);
    }

    public static void sendDM(User user, MessageEmbed message, Consumer<Throwable> failed){
        sendDM(user, message, null, failed);
    }

    public static void sendDM(User user, MessageEmbed message, Consumer<Message> success, Consumer<Throwable> failed){
        user.openPrivateChannel()
                .flatMap ( privateChannel -> privateChannel.sendMessage(message) )
                .queue(success, failed);
    }

}
