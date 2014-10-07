//Sqlc generated V1.O00-1
package org.openbravo.idl.modulescript;

import java.sql.*;

import org.apache.log4j.Logger;

import javax.servlet.ServletException;

import org.openbravo.data.FieldProvider;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.data.UtilSql;
import java.util.*;

class RemoveOldIDLEntitiesData implements FieldProvider {
static Logger log4j = Logger.getLogger(RemoveOldIDLEntitiesData.class);
  private String InitRecordNumber="0";
  public String adClientId;
  public String specificId;

  public String getInitRecordNumber() {
    return InitRecordNumber;
  }

  public String getField(String fieldName) {
    if (fieldName.equalsIgnoreCase("ad_client_id") || fieldName.equals("adClientId"))
      return adClientId;
    else if (fieldName.equalsIgnoreCase("specific_id") || fieldName.equals("specificId"))
      return specificId;
   else {
     log4j.debug("Field does not exist: " + fieldName);
     return null;
   }
 }

  public static RemoveOldIDLEntitiesData[] select(ConnectionProvider connectionProvider)    throws ServletException {
    return select(connectionProvider, 0, 0);
  }

  public static RemoveOldIDLEntitiesData[] select(ConnectionProvider connectionProvider, int firstRegister, int numberRegisters)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "        SELECT AD_CLIENT_ID, '' as SPECIFIC_ID" +
      "        FROM AD_CLIENT" +
      "        WHERE AD_CLIENT_ID <> '0'";

    ResultSet result;
    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
    PreparedStatement st = null;

    try {
    st = connectionProvider.getPreparedStatement(strSql);

      result = st.executeQuery();
      long countRecord = 0;
      long countRecordSkip = 1;
      boolean continueResult = true;
      while(countRecordSkip < firstRegister && continueResult) {
        continueResult = result.next();
        countRecordSkip++;
      }
      while(continueResult && result.next()) {
        countRecord++;
        RemoveOldIDLEntitiesData objectRemoveOldIDLEntitiesData = new RemoveOldIDLEntitiesData();
        objectRemoveOldIDLEntitiesData.adClientId = UtilSql.getValue(result, "ad_client_id");
        objectRemoveOldIDLEntitiesData.specificId = UtilSql.getValue(result, "specific_id");
        objectRemoveOldIDLEntitiesData.InitRecordNumber = Integer.toString(firstRegister);
        vector.addElement(objectRemoveOldIDLEntitiesData);
        if (countRecord >= numberRegisters && numberRegisters != 0) {
          continueResult = false;
        }
      }
      result.close();
    } catch(SQLException e){
      log4j.error("SQL error in query: " + strSql + "Exception:"+ e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
    } catch(Exception ex){
      log4j.error("Exception in query: " + strSql + "Exception:"+ ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releasePreparedStatement(st);
      } catch(Exception ignore){
        ignore.printStackTrace();
      }
    }
    RemoveOldIDLEntitiesData objectRemoveOldIDLEntitiesData[] = new RemoveOldIDLEntitiesData[vector.size()];
    vector.copyInto(objectRemoveOldIDLEntitiesData);
    return(objectRemoveOldIDLEntitiesData);
  }

/**
FF80818124241FE0012424493CA5003A -Bank Account
 *FF80818124241FE0012424493CA70044 - Open Payable
 *FF80818124241FE0012424493CA7005A - Open Receivable
 */
  public static int removeEntities(ConnectionProvider connectionProvider, String adClientId)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "       DELETE FROM IDL_ENTITIES" +
      "       WHERE IDL_ENTITIES_ID IN (" +
      "                                  SELECT SPECIFIC_ID" +
      "                                  FROM AD_REF_DATA_LOADED" +
      "                                  WHERE AD_MODULE_ID='509767E831EA4B39B4839A4EC4A28628'" +
      "                                  AND AD_TABLE_ID='237AE8134C5D4150ACCF039616BEF91F'" +
      "                                  AND AD_CLIENT_ID = ?" +
      "                                  AND GENERIC_ID IN ('FF80818124241FE0012424493CA5003A', 'FF80818124241FE0012424493CA70044'," +
      "                                                     'FF80818124241FE0012424493CA7005A')" +
      "                                )";

    int updateCount = 0;
    PreparedStatement st = null;

    int iParameter = 0;
    try {
    st = connectionProvider.getPreparedStatement(strSql);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, adClientId);

      updateCount = st.executeUpdate();
    } catch(SQLException e){
      log4j.error("SQL error in query: " + strSql + "Exception:"+ e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
    } catch(Exception ex){
      log4j.error("Exception in query: " + strSql + "Exception:"+ ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releasePreparedStatement(st);
      } catch(Exception ignore){
        ignore.printStackTrace();
      }
    }
    return(updateCount);
  }

  public static int deleteRefDataLoaded(ConnectionProvider connectionProvider, String adClientId)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "       DELETE FROM AD_REF_DATA_LOADED" +
      "       WHERE AD_MODULE_ID='509767E831EA4B39B4839A4EC4A28628'" +
      "             AND AD_TABLE_ID='237AE8134C5D4150ACCF039616BEF91F'" +
      "             AND AD_CLIENT_ID = ?" +
      "             AND GENERIC_ID IN ('FF80818124241FE0012424493CA5003A', 'FF80818124241FE0012424493CA70044'," +
      "                                'FF80818124241FE0012424493CA7005A')";

    int updateCount = 0;
    PreparedStatement st = null;

    int iParameter = 0;
    try {
    st = connectionProvider.getPreparedStatement(strSql);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, adClientId);

      updateCount = st.executeUpdate();
    } catch(SQLException e){
      log4j.error("SQL error in query: " + strSql + "Exception:"+ e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
    } catch(Exception ex){
      log4j.error("Exception in query: " + strSql + "Exception:"+ ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releasePreparedStatement(st);
      } catch(Exception ignore){
        ignore.printStackTrace();
      }
    }
    return(updateCount);
  }

  public static RemoveOldIDLEntitiesData[] totalImported(ConnectionProvider connectionProvider, String adClientId)    throws ServletException {
    return totalImported(connectionProvider, adClientId, 0, 0);
  }

  public static RemoveOldIDLEntitiesData[] totalImported(ConnectionProvider connectionProvider, String adClientId, int firstRegister, int numberRegisters)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "       SELECT SPECIFIC_ID" +
      "       FROM AD_REF_DATA_LOADED" +
      "       WHERE AD_MODULE_ID='509767E831EA4B39B4839A4EC4A28628'" +
      "             AND AD_TABLE_ID='237AE8134C5D4150ACCF039616BEF91F'" +
      "             AND AD_CLIENT_ID = ?" +
      "             AND GENERIC_ID IN ('FF80818124241FE0012424493CA5003A', 'FF80818124241FE0012424493CA70044'," +
      "                                'FF80818124241FE0012424493CA7005A')";

    ResultSet result;
    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
    PreparedStatement st = null;

    int iParameter = 0;
    try {
    st = connectionProvider.getPreparedStatement(strSql);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, adClientId);

      result = st.executeQuery();
      long countRecord = 0;
      long countRecordSkip = 1;
      boolean continueResult = true;
      while(countRecordSkip < firstRegister && continueResult) {
        continueResult = result.next();
        countRecordSkip++;
      }
      while(continueResult && result.next()) {
        countRecord++;
        RemoveOldIDLEntitiesData objectRemoveOldIDLEntitiesData = new RemoveOldIDLEntitiesData();
        objectRemoveOldIDLEntitiesData.specificId = UtilSql.getValue(result, "specific_id");
        objectRemoveOldIDLEntitiesData.InitRecordNumber = Integer.toString(firstRegister);
        vector.addElement(objectRemoveOldIDLEntitiesData);
        if (countRecord >= numberRegisters && numberRegisters != 0) {
          continueResult = false;
        }
      }
      result.close();
    } catch(SQLException e){
      log4j.error("SQL error in query: " + strSql + "Exception:"+ e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
    } catch(Exception ex){
      log4j.error("Exception in query: " + strSql + "Exception:"+ ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releasePreparedStatement(st);
      } catch(Exception ignore){
        ignore.printStackTrace();
      }
    }
    RemoveOldIDLEntitiesData objectRemoveOldIDLEntitiesData[] = new RemoveOldIDLEntitiesData[vector.size()];
    vector.copyInto(objectRemoveOldIDLEntitiesData);
    return(objectRemoveOldIDLEntitiesData);
  }
}
