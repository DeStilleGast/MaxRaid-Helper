package com.destillegast.maxraid;

/**
 * Created by DeStilleGast 3-2-2020
 */
public class PathUtil {

    public static String getPathFileName(String path) {
        String fileName = path.substring(path.lastIndexOf('/') + 1);
        return fileName.substring(0, fileName.lastIndexOf('.'));
    }
}
