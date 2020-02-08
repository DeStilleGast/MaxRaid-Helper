package com.destillegast.maxraid.dex;

import com.destillegast.maxraid.PathUtil;
import lombok.Getter;
import lombok.ToString;

/**
 * Created by DeStilleGast 3-2-2020
 */
@ToString
public class Effectiveness {

    @Getter
    private String name;

    @Getter
    private double power;

    public Effectiveness(String name, String power) {
        this.name = PathUtil.getPathFileName(name);
        this.power = Double.parseDouble(power.replace("*", ""));
    }
}
