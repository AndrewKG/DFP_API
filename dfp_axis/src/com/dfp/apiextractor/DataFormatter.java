package com.dfp.apiextractor;



import com.google.api.ads.dfp.axis.v201505.DateTime;
import com.google.api.ads.dfp.axis.v201505.Money;

import java.text.SimpleDateFormat;

/**
 * Created by keanguan on 7/5/15.
 */
public class DataFormatter {
    public static String format(String dataType, Object data) {
        switch (dataType.toLowerCase()) {
            case "datetime":
                DateTime dt = (DateTime)data;
                String sp = "-";
                return dt.getDate().getYear()+sp+dt.getDate().getMonth()+sp+dt.getDate().getDay()+" "+dt.getHour()+":"+dt.getMinute()+":"+dt.getSecond();
            case "money":
                Money money = (Money)data;
                return money.getCurrencyCode() +" "+ (money.getMicroAmount()/1000000);
        }

        return data.toString();
    }
}
