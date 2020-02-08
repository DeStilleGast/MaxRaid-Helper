package com.destillegast.maxraid;

import com.destillegast.maxraid.parsers.AnnouncementParser;
import com.destillegast.maxraid.dex.CacheObject;
import com.destillegast.maxraid.dex.DexCache;
import com.destillegast.maxraid.dex.Serebii;
import com.destillegast.maxraid.parsers.RedditParser;
import com.destillegast.maxraid.parsers.reddit.RedditThread;
import com.destillegast.maxraid.queue.QueueManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;

/**
 * Created by DeStilleGast 2-2-2020
 */
public class Launcher extends ListenerAdapter {

    // idea: watch raid announcement
    // get pokemons from den
	// https://discordapp.com/oauth2/authorize?client_id=674371453214261260&scope=bot&permissions=10256


    public static void main(String[] args) throws IOException, InterruptedException, LoginException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        File configFile = new File("config.json");
        File cacheFile = new File("cache.json");
        if(!configFile.exists()){
            mapper.writeValue(configFile, new Config());
        }

        CacheObject co = new CacheObject();
        if(cacheFile.exists()){
             co = mapper.readValue(cacheFile, CacheObject.class);
        }
        DexCache dc = new DexCache(co);

        Config config = mapper.readValue(configFile, Config.class);


        Serebii serebii = new Serebii(co);
        serebii.loadDex();
        serebii.loadAbilities();

        serebii.loadDenInfo();
        co.afterInit();

//        if(true) return;

        if(co.reloadDex){
            co.cachedData.clear();
        }
        co.reloadDex = false;
        mapper.writeValue(cacheFile, co);



//    println(config.botToken)

        JDA bot = new JDABuilder().setAutoReconnect(true).setToken(config.botToken).setStatus(OnlineStatus.DO_NOT_DISTURB).build();

        QueueManager qm = new QueueManager();

        bot.addEventListener(new CommandListener(config.prefix, co, dc, qm));
        bot.addEventListener(new Launcher());
        bot.addEventListener(new AnnouncementParser(co));

    }

    @Override
    public void onMessageReactionAdd(@Nonnull MessageReactionAddEvent event) {
//        log.info(event.getReactionEmote().toString());

    }
}
