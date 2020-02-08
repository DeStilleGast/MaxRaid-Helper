package com.destillegast.maxraid.dex;

import com.destillegast.maxraid.PathUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.ToString;
import org.jsoup.nodes.Element;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by DeStilleGast 3-2-2020
 */
@ToString
public class PokeData {

    @Getter
    private String name;

    @Getter
    private String dexImage, shinyImage;

    @Getter
    private String types;

    @Getter
    private List<Effectiveness> effectiveScheme;

    public PokeData(String name, Element imageElement, Element info, Element abilities, Element weaknessElement) throws MalformedURLException {
        this.name = name;

        // images
        URL dex = new URL(imageElement.baseUri());

        dexImage = (dex.getProtocol() + "://"+ dex.getHost() + imageElement.select("img").get(0).attr("src"));
        shinyImage = (dex.getProtocol() + "://"+ dex.getHost() + imageElement.select("img").get(1).attr("src"));

        // info
        types = (info.select("tr").get(1).select("a").stream()
                .map(element -> PathUtil.getPathFileName(element.attr("href")))
                .collect(Collectors.joining(", ")));

        // abilities
        // maybe someday


        // weakness
        this.effectiveScheme = buildEffectivenessList(weaknessElement);
    }


    private List<Effectiveness> buildEffectivenessList(Element weakness){
        List<Effectiveness> weaknessList = new ArrayList<>();

        int typeSize = weakness.select("tr").get(1).select("td").size();
        for (int i = 0; i < typeSize; i++) {

            String type = weakness.select("tr").get(1).select("td").get(i).select("a").attr("href").split("/")[2];
            String power = weakness.select("tr").get(2).select("td").get(i).text();

            weaknessList.add(new Effectiveness(type, power));
        }

        return weaknessList;
    }

    @JsonIgnore
    public List<Effectiveness> getWeaknesses(){
        return effectiveScheme.stream().filter(e -> e.getPower() > 1).collect(Collectors.toList());
    }

    @JsonIgnore
    public List<Effectiveness> getImmune(){
        return effectiveScheme.stream().filter(e -> e.getPower() == 0).collect(Collectors.toList());
    }

    private String getPathBaseName(String url){
        int slashIndex = url.lastIndexOf('/');
        int dotIndex = url.lastIndexOf('.', slashIndex);
//        String filenameWithoutExtension;
        if (dotIndex == -1) {
            return url.substring(slashIndex + 1);
        } else {
            return url.substring(slashIndex + 1, dotIndex);
        }
    }
}
