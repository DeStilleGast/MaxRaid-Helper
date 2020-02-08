package com.destillegast.maxraid.queue;

import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

import java.util.HashMap;

/**
 * Created by DeStilleGast 2-2-2020
 */
public class QueueManager {

    private HashMap<MessageChannel, ChannelQueue> channelQueueList = new HashMap<>();

    public ChannelQueue getQueue(MessageChannel channel){
        return channelQueueList.get(channel);
    }

    public void removeQueue(TextChannel channel){
        getQueue(channel).close();
        channelQueueList.remove(channel);
    }

    public int getRunningQueues(){
        return channelQueueList.size();
    }

}
