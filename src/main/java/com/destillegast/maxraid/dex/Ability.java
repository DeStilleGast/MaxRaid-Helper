package com.destillegast.maxraid.dex;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Created by DeStilleGast 4-2-2020
 */

@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Ability {

    @Getter
    private String name, description, url;
}
