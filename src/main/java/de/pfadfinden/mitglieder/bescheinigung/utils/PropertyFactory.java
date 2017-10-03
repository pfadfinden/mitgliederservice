package de.pfadfinden.mitglieder.bescheinigung.utils;

import de.pfadfinden.mitglieder.bescheinigung.AwsRequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertyFactory {

    private static final Logger logger = LoggerFactory.getLogger(PropertyFactory.class);
    private static final Properties propertiesMap = new Properties();

    static {
        final String filename = System.getenv("PROPFILE");
        if(filename == null){
            logger.error("Unable to load java env PROPFILE");
        }

        InputStream input = null;

        try {
            input = AwsRequestHandler.class.getClassLoader().getResourceAsStream(filename);
            if(input==null){
                logger.error("Unable to load property file {} ",filename);
            }
            propertiesMap.load(input);
        } catch (IOException e) {
            e.getStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static Properties getPropertiesMap() {
        return propertiesMap;
    }
}
