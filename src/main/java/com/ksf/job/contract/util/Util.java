package com.ksf.job.contract.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.net.URLConnection;

public class Util {

    private static final Logger logger = LogManager.getLogger();

    public static String getOriginalName(String urlString, String defaultName) {
        try {
            URL url = new URL(urlString);
            URLConnection con = url.openConnection();
            String fieldValue = con.getHeaderField("Content-Type");
            String filename = fieldValue.replace("application/", "");
            return "."+ filename;
        } catch (Exception e) {
            logger.error(e);
        }
        return ".pdf";
    }
}
