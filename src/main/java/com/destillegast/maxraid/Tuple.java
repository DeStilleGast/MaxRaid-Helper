package com.destillegast.maxraid;

/**
 * Created by DeStilleGast 3-2-2020
 */
public class Tuple<A, B>  {

    private A partA;
    private B partB;

    public Tuple(A partA, B partB) {
        this.partA = partA;
        this.partB = partB;
    }

    public A getPartA() {
        return partA;
    }

    public B getPartB() {
        return partB;
    }
}
