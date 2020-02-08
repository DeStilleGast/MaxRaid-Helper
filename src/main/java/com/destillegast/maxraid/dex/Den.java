package com.destillegast.maxraid.dex;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by DeStilleGast 5-2-2020
 */
@Getter
@ToString
@NoArgsConstructor
public class Den {

    private int number;

    public Den(int number) {
        this.number = number;
    }

    private List<Integer> pokemonNumbers;

    @JsonIgnore
    private List<SerebiiEntry> pokemons = new ArrayList<>();

    // 64.1K
    @JsonIgnore
    public void afterInit(CacheObject cacheObject){
        if(pokemonNumbers != null)
            pokemons = cacheObject.serebiiDex.stream().filter(serebiiEntry -> pokemonNumbers.contains(serebiiEntry.getId())).collect(Collectors.toList());

        pokemonNumbers = pokemons.stream().map(SerebiiEntry::getId).collect(Collectors.toList());

        pokemons.sort(Comparator.comparingInt(SerebiiEntry::getId));

//        pokemons = pokemons.stream().sorted(Collections.)
    }

}
