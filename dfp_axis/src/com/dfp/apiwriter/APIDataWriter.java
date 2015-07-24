package com.dfp.apiwriter;

import com.dfp.auth.GetRefreshToken;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * Created by keanguan on 7/21/15.
 */
public class APIDataWriter {
    static String apiPropertiesFilePath = "";
    private static Logger logger = Logger.getLogger(APIDataWriter.class);

    public static void main(String[] args) throws Exception {

        try {
            if(args.length == 0) {
                printHelp();
                return;
            }
            //Generate security token
            else if("getRefreshToken".equals(args[0])) {
                GetRefreshToken.main(args);
                return;
            }
            String file = args[0];
            String instructionPath = args[1];
            apiPropertiesFilePath = args[2];
            String keyfile = "";
            if (args[3] == null) {
                keyfile = "key.txt";
            } else {
                keyfile = args[3];
            }

            //get log config file
            PropertyConfigurator.configure("log4j.properties");


        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            e.printStackTrace();
        }

    }

    private static void printHelp() {
    }
}
