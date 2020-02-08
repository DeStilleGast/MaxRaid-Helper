package com.destillegast.maxraid.dex;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import net.dv8tion.jda.api.EmbedBuilder;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by DeStilleGast 4-2-2020
 */
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SerebiiEntry {

    @Getter
    private int id;

    @Getter
    private String name, url;

    @Setter
    private CacheObject cacheObject;


    @JsonIgnore
    public PokeData getData(){
        return cacheObject.dexCache.getPokeDate(name);
    }

    public void applyToEmbed(EmbedBuilder embedBuilder){
        embedBuilder.addField("Pokemon:", this.name, false);

        applyEffective(embedBuilder, "Weakness:", e -> e.getPower() > 1);
        applyEffective(embedBuilder, "Not very effective:", e -> e.getPower() == 0.5);
        applyEffective(embedBuilder, "Immune:", e -> e.getPower() == 0);


//        embedBuilder.addField("Immune: ", this.getData().getEffectiveScheme().stream().filter(e -> e.getPower() == 0).map(Effectiveness::getName).collect(Collectors.joining(", ")), false);
    }

    private void applyEffective(EmbedBuilder embedBuilder, String name, Predicate<Effectiveness> filter){
        Stream<Effectiveness> effectivenessStream = this.getData().getEffectiveScheme().stream().filter(filter);
        if(effectivenessStream.findAny().isPresent())
            embedBuilder.addField(name, this.getData().getEffectiveScheme().stream().filter(filter).map(Effectiveness::getName).collect(Collectors.joining(", ")), true);
    }
}
