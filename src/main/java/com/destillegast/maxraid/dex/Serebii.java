package com.destillegast.maxraid.dex;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by DeStilleGast 4-2-2020
 */

public class Serebii {

    private CacheObject co;

    public Serebii(CacheObject co) {
        this.co = co;
    }


    public void loadDex(){
        if(co.serebiiDex.isEmpty() || co.reloadDex) {
            co.serebiiDex.clear();

            String url = "https://www.serebii.net/pokedex-swsh/";
            System.out.println("Downloading poke dex...");

            try {
                Document doc = Jsoup.connect(url).get();
                Elements selectList = doc.getElementsByAttributeValue("name", "galar").first().select("option");

                for (Element e : selectList) {
                    if (!e.attr("value").equals("#")) {
                        String entry = e.text();
                        String[] split = entry.split(" ");
                        int id = Integer.parseInt(split[0]);
                        String name = entry.substring(entry.indexOf(" ") + 1);

                        co.serebiiDex.add(new SerebiiEntry(id, name, e.attr("value"), co));
                    }
                }

//                co.serebiiDex.forEach(System.out::println);

            } catch (IOException e) {
                System.err.println("Failed to read dex: " + e.getMessage());
                e.printStackTrace();
            }
        }else{
            co.serebiiDex.forEach(serebiiEntry -> serebiiEntry.setCacheObject(co));
        }
    }

    public void loadAbilities(){
        if(co.abilityDex.isEmpty() || co.reloadDex) {
            co.abilityDex.clear();
            String url = "https://www.serebii.net/abilitydex/";
            System.out.println("Downloading ability dex...");

            try {
                Document doc = Jsoup.connect(url).get();
                extractAbilities(doc.getElementsByAttributeValue("name", "ability").first().select("option"));
                extractAbilities(doc.getElementsByAttributeValue("name", "ability2").first().select("option"));

            }catch (IOException e){
                System.err.println("Failed to read ability dex: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void extractAbilities(Elements elementList){
        for (Element e : elementList) {
            if (!e.attr("value").equals("#")) {
                String name = e.text();

                co.abilityDex.add(new Ability(name, null, e.attr("value")));
            }
        }
    }

    public void loadDenInfo(){
        if(!(co.denData.isEmpty() || co.reloadDex)) return;

        String url = "https://www.serebii.net/swordshield/maxraidbattledens.shtml";
        System.out.println("Downloading den info...");


        try {
            Document doc = Jsoup.connect(url).get();
            Elements elements = doc.getElementsByAttributeValueStarting("href", "maxraidbattles/");

//            List<Integer> denNumbers = new ArrayList<>();
            Pattern p = Pattern.compile("-?\\d+");
            for(Element e : elements){
                String data = e.select("img").attr("alt");
                if(e.select("img").hasClass("images")) continue;

                Matcher m = p.matcher(data);
//                System.out.println(data);


                if(m.find()) {
                    int number = Integer.parseInt(m.group());

                    Optional<Den> denCache = co.denData.stream().filter(den -> den.getNumber() == number).findFirst();
                    Den den = denCache.orElseGet(() -> new Den(number));

                    String name = data.substring(0, data.lastIndexOf("-"));
                    SerebiiEntry se = co.serebiiDex.stream().filter(serebiiEntry -> serebiiEntry.getName().equalsIgnoreCase(name.trim())).findFirst().orElse(null);
                    if(!den.getPokemons().contains(se))
                        den.getPokemons().add(se);

                    if(!co.denData.contains(den)) co.denData.add(den);
                }
            };

            System.out.println("Den count: " + co.denData.size());
            if(co.denData.stream().anyMatch(den -> den.getPokemons().contains(null))){
                System.err.println("NULL pokemon found !");
            }
//            co.denData.forEach(System.out::println);
//            Collections.sort(denNumbers);
//            denNumbers.forEach(System.out::println);


        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
