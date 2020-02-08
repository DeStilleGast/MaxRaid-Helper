package com.destillegast.maxraid.dex;

import com.destillegast.maxraid.queue.RaidCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by DeStilleGast 4-2-2020
 */
public class CacheObject {

    @JsonIgnore
    public DexCache dexCache;

    public boolean reloadDex;

    public List<SerebiiEntry> serebiiDex = new ArrayList<>();
    public List<Ability> abilityDex = new ArrayList<>();
    public List<PokeData> cachedData = new ArrayList<>();
    public List<Den> denData = new ArrayList<>();

    @JsonIgnore
    public List<RaidCreator> activeRaid = new ArrayList<>();

    public void afterInit() {
        denData.forEach(den -> den.afterInit(this));
    }
}
