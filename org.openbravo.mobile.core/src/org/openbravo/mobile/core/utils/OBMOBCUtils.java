package org.openbravo.mobile.core.utils;

import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;
import org.openbravo.mobile.core.process.PropertyByType;
import org.openbravo.service.json.JsonToDataConverter;

/**
 * @author iperdomo
 * 
 */
public class OBMOBCUtils {

  public static final Logger log = Logger.getLogger(OBMOBCUtils.class);

  public static Date calculateServerDate(String orgClientDate, Long dateOffset) {
    Date serverDate = (Date) JsonToDataConverter.convertJsonToPropertyValue(
        PropertyByType.DATETIME,
        ((String) orgClientDate).subSequence(0, ((String) orgClientDate).lastIndexOf(".")));
    // date is the date in the server timezone, we need to convert it to the date in the
    // original time zone
    Date dateUTC = convertToUTC(serverDate);
    Date clientDate = new Date();
    clientDate.setTime(dateUTC.getTime() - dateOffset * 60 * 1000);
    return stripTime(clientDate);
  }

  private static Date convertToUTC(Date localTime) {
    Calendar now = Calendar.getInstance();
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(localTime);

    int gmtMillisecondOffset = (now.get(Calendar.ZONE_OFFSET) + now.get(Calendar.DST_OFFSET));
    calendar.add(Calendar.MILLISECOND, -gmtMillisecondOffset);

    return calendar.getTime();
  }

  public static Date stripTime(Date dateWithTime) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(dateWithTime);
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    return calendar.getTime();
  }
}
