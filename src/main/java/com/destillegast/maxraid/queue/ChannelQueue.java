package com.destillegast.maxraid.queue;

import com.destillegast.maxraid.UserUtil;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageType;
import net.dv8tion.jda.api.entities.User;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by DeStilleGast 2-2-2020
 */
public class ChannelQueue {

    private String raidName;
    private User host;

    private Queue<User> userQueue = new LinkedList<>();
    private boolean isOpen;

    public void queueUser(Message message){
        if(message.getType() != MessageType.DEFAULT) return;

        User user = message.getAuthor();
        if(isOpen) {
            userQueue.add(user);

            UserUtil.sendDM(user, String.format("You have joined the queue for '%s' and you are in position %s", raidName, userQueue.size()));
        }else{
            user.openPrivateChannel()
                    .flatMap ( privateChannel -> privateChannel.sendMessage("Sorry, but the queue for '$raidName' is closed"))
                    .queue();
        }
    }

    public void startNextSession(int lobbySize, int code){

        if(userQueue.isEmpty()){
            UserUtil.sendDM(host, "The queue is empty");
            return;
        }


        for (int i = 0; i < lobbySize; i++) {
            User user = userQueue.poll();

            if(user != null){
                UserUtil.sendDM(user, String.format("%s has started/is going to start a new lobby with this code: %s", host.getAsMention(), code));
            }
        }
    }

    public boolean leaveQueue(User user){
        return userQueue.remove(user);
    }

    public boolean isInQueue(User user){
        return userQueue.contains(user);
    }

    public void close() {
        isOpen = false;
    }

    public void endNow(User closer) {
        close();

        while(userQueue.peek() != null){
            UserUtil.sendDM(userQueue.poll(), String.format("%s has closed raid '%s' while you were in queue", closer.getAsMention(), raidName));
        }
    }
}
