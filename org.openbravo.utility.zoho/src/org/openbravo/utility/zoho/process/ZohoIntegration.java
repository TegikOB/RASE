/*
 ************************************************************************************
 * Copyright (C) 2011-2013 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 ************************************************************************************
 */

package org.openbravo.utility.zoho.process;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.message.BasicStatusLine;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.dal.core.OBContext;
import org.openbravo.scheduling.ProcessLogger;
import org.openbravo.utility.zoho.ZohoConfig;
import org.openbravo.utility.zoho.ZohoImport;
import org.openbravo.utils.CryptoUtility;

public class ZohoIntegration {

  private ZohoImport zohoImport;
  private ProcessLogger logger;
  private static final Logger log4j = Logger.getLogger(ZohoIntegration.class);

  private static final String authenticationToken = "AUTHTOKEN=";
  private static final String resultToken = "RESULT=";
  private static final String successResult = "TRUE";
  private static final String errorCauseToken = "CAUSE=";

  public ZohoIntegration(ZohoImport zohoImport) {
    this.zohoImport = zohoImport;
  }

  public void uploadCSV(File csvFile) throws Exception {
    String authToken;
    if (zohoImport.getObuzohoConfig().getAuthtoken() == null) {
      authToken = getAuthToken(zohoImport.getObuzohoConfig());
      zohoImport.getObuzohoConfig().setAuthtoken(authToken);
    } else
      authToken = zohoImport.getObuzohoConfig().getAuthtoken();

    HttpClient httpclient = new DefaultHttpClient();
    HttpPost httpPost = createHttpPost(authToken, csvFile);
    ResponseHandler<String> responseHandler = new BasicResponseHandler();
    try {
      OBContext.setAdminMode(true);
      // String resp = httpclient.execute(httpPost, responseHandler);
      HttpResponse r = httpclient.execute(httpPost);
      StatusLine st = r.getStatusLine();

      if (st.getStatusCode() != 200) {
        // Set status OK to be able to handle response
        BasicStatusLine sl = new BasicStatusLine(st.getProtocolVersion(), 200, "OK");
        r.setStatusLine(sl);

        log("Error uploading CSV");

        String responseStr = responseHandler.handleResponse(r);
        if (responseStr.contains("\"code\":8518")) {
          // The token is invalid. A new one must be requested
          authToken = getAuthToken(zohoImport.getObuzohoConfig());
          responseHandler = new BasicResponseHandler();
          httpPost = createHttpPost(authToken, csvFile);
          r = httpclient.execute(httpPost);
          st = r.getStatusLine();
          if (st.getStatusCode() != 200) {
            // Set status OK to be able to handle response
            sl = new BasicStatusLine(st.getProtocolVersion(), 200, "OK");
            r.setStatusLine(sl);

            log("Error uploading CSV");
            throw new Exception("Error importing to zoho " + responseHandler.handleResponse(r));
          } else {
            zohoImport.getObuzohoConfig().setAuthtoken(authToken);
            logResult(new JSONObject(responseHandler.handleResponse(r)));
          }
        } else {
          throw new Exception("Error importing to zoho " + responseStr);
        }
      } else {
        logResult(new JSONObject(responseHandler.handleResponse(r)));
      }
    } finally {
      httpclient.getConnectionManager().shutdown();
      OBContext.restorePreviousMode();
    }
  }

  private String getAuthToken(ZohoConfig zohoConfig) throws ServletException,
      ClientProtocolException, IOException {
    log("Getting zoho auth token");
    String url = "https://accounts.zoho.com/apiauthtoken/nb/create?SCOPE=ZohoReports/reportsapi";

    HttpClient httpclient = new DefaultHttpClient();
    HttpPost httpPost = new HttpPost(url);

    List<NameValuePair> params = new ArrayList<NameValuePair>(2);
    params.add(new BasicNameValuePair("EMAIL_ID", zohoConfig.getUsername()));
    params.add(new BasicNameValuePair("PASSWORD", CryptoUtility.decrypt(zohoConfig.getPassword())));
    httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

    ResponseHandler<String> responseHandler = new BasicResponseHandler();
    String r = httpclient.execute(httpPost, responseHandler);

    log4j.debug("Auth Token Response:\n" + r);

    String result = r.substring(r.indexOf(resultToken) + resultToken.length());
    result = result.substring(0, result.indexOf('\n'));
    log4j.debug("Response result:" + result);

    if (successResult.equals(result)) {
      String token = r.substring(r.indexOf(authenticationToken) + authenticationToken.length());
      token = token.substring(0, token.indexOf('\n'));
      log("Zoho token: " + token);
      return token;
    } else {
      String cause = r.substring(r.indexOf(errorCauseToken) + errorCauseToken.length());
      cause = cause.substring(0, cause.indexOf('\n'));
      throw new OBException(cause);
    }
  }

  private HttpPost createHttpPost(String authToken, File csvFile)
      throws UnsupportedEncodingException {
    String url = "https://reportsapi.zoho.com/api/";
    url += zohoImport.getObuzohoConfig().getUsername() + "/";
    url += zohoImport.getObuzohoConfig().getDatabase() + "/";
    url += zohoImport.getZohoTable();
    url += "?ZOHO_ACTION=IMPORT&ZOHO_API_VERSION=1.0&ZOHO_OUTPUT_FORMAT=JSON";
    url += "&authtoken=" + URLEncoder.encode(authToken, "UTF-8");

    HttpPost httpPost = new HttpPost(url);

    FileBody uploadFilePart = new FileBody(csvFile);
    MultipartEntity reqEntity = new MultipartEntity();
    reqEntity.addPart("ZOHO_FILE", uploadFilePart);
    reqEntity.addPart("ZOHO_IMPORT_TYPE", new StringBody(zohoImport.getImportType()));
    reqEntity.addPart("ZOHO_AUTO_IDENTIFY", new StringBody("true"));
    reqEntity.addPart("ZOHO_CREATE_TABLE", new StringBody("true"));
    reqEntity.addPart("ZOHO_ON_IMPORT_ERROR", new StringBody("ABORT"));
    if (zohoImport.isDatetimeFormat())
      reqEntity.addPart("ZOHO_DATE_FORMAT", new StringBody("MM-dd-yyyy HH:mm:ss"));
    else
      reqEntity.addPart("ZOHO_DATE_FORMAT", new StringBody("MM-dd-yyyy"));

    if ("UPDATEADD".equals(zohoImport.getImportType())
        && !zohoImport.getMatchingColumns().isEmpty()) {
      reqEntity.addPart("ZOHO_MATCHING_COLUMNS", new StringBody(zohoImport.getMatchingColumns()));
    }
    httpPost.setEntity(reqEntity);
    return httpPost;
  }

  private void logResult(JSONObject resp) throws Exception {
    boolean logged = false;

    if (resp.has("response")) {
      JSONObject result = resp.getJSONObject("response");
      if (result.has("result")) {
        if (result.getJSONObject("result").has("importSummary")) {
          JSONObject summary = result.getJSONObject("result").getJSONObject("importSummary");
          if (summary.has("totalRowCount")) {
            log(summary.getString("totalRowCount") + " rows imported");
            logged = true;
            log4j.debug(resp.toString(1));
          }
        }
      }
    }
    if (!logged) {
      log("Unexpected Zoho response");
      throw new Exception("Unexpected Zoho response:\n" + resp.toString(1));
    }
  }

  private void log(String msg) {
    log4j.debug(msg);
    if (logger != null) {
      logger.logln(msg);
    }
  }

  public void setAdditionalLogger(ProcessLogger logger) {
    this.logger = logger;
  }

}
