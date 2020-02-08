package com.destillegast.maxraid.dex;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by DeStilleGast 3-2-2020
 */
public class DexCache {

    private CacheObject co;

    public DexCache(CacheObject co) {
        this.co = co;
        this.co.dexCache = this;
    }

    public PokeData getPokeDate(String pokeName) {

        String finalPokeName = pokeName;
        if (co.serebiiDex.stream().map(SerebiiEntry::getName).anyMatch(s -> s.equalsIgnoreCase(finalPokeName))) {
            pokeName = pokeName.replace(" ", "");
            return getPokeDateIntern(pokeName.toLowerCase());
        }
        return null;
    }

    private PokeData getPokeDateIntern(String pokeName) {
        Optional<PokeData> cachedResult = co.cachedData.stream().filter(pd -> pd.getName().equalsIgnoreCase(pokeName)).findFirst();
        if (cachedResult.isPresent()) {
            return cachedResult.get();
        } else {
            String dex = "https://www.serebii.net/pokedex-swsh/" + pokeName;
//        String dex = "https://www.serebii.net/pokedex-swsh/cinderace/";
            try {
                Document doc = Jsoup.connect(dex).get();

                doc.outputSettings().escapeMode(Entities.EscapeMode.xhtml);

                List<Element> elementList = doc.select("table").stream().filter(table -> table.hasClass("dextable")).collect(Collectors.toList());

                String name = doc.title().split("-")[0].trim();
                Element images = elementList.get(0);
                Element info = elementList.get(1);
                Element abilities = elementList.get(2);
                Element weakness = elementList.get(3);

                PokeData newPd = new PokeData(name, images, info, abilities, weakness);
                co.cachedData.add(newPd);
                return newPd;

            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }
}
