/*
 ************************************************************************************
 * Copyright (C) 2012-2013 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.mobile.core.process;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.service.json.JsonToDataConverter;

public abstract class ProcessHQLQuery extends SecuredJSONProcess {

  protected abstract List<String> getQuery(JSONObject jsonsent) throws JSONException;

  private final static Logger log = Logger.getLogger(ProcessHQLQuery.class);

  protected boolean isAdminMode() {
    return false;
  }

  protected StrategyQuery getStrategyQuery() {
    return ProcessHQLQuery.StrategyQueryScroll;
  }

  @Override
  public final void exec(Writer w, JSONObject jsonsent) throws IOException, ServletException {

    try {
      boolean streamOpened = false;
      if (isAdminMode()) {
        OBContext.setAdminMode();
      }

      Long lastUpdated = jsonsent.has("lastUpdated")
          && !jsonsent.get("lastUpdated").equals("undefined") ? jsonsent.getLong("lastUpdated")
          : null;

      String dateFormat = jsonsent.has("_dateFormat") ? jsonsent.getString("_dateFormat") : null;

      int totalRows = 0;
      boolean firstQuery = true;
      for (String hqlQuery : getQuery(jsonsent)) {

        SimpleQueryBuilder querybuilder = new SimpleQueryBuilder(hqlQuery, OBContext.getOBContext()
            .getCurrentClient().getId(), OBContext.getOBContext().getCurrentOrganization().getId(),
            lastUpdated != null ? new Date(lastUpdated) : null);

        final Session session = OBDal.getInstance().getSession();
        final Query query = session.createQuery(querybuilder.getHQLQuery());

        if (jsonsent.has("_limit")) {
          query.setMaxResults(jsonsent.getInt("_limit"));
        }

        List<String> queryParams = new ArrayList<String>(Arrays.asList(query.getNamedParameters()));

        if (jsonsent.has("parameters")) {
          JSONObject jsonparams = jsonsent.getJSONObject("parameters");
          Iterator<?> it = jsonparams.keys();
          while (it.hasNext()) {
            String key = (String) it.next();
            if (!queryParams.contains(key)) {
              continue;
            }
            queryParams.remove(key);

            Object value = jsonparams.get(key);
            if (value instanceof JSONObject) {
              JSONObject jsonvalue = (JSONObject) value;
              query.setParameter(
                  key,
                  JsonToDataConverter.convertJsonToPropertyValue(
                      PropertyByType.get(jsonvalue.getString("type")), jsonvalue.get("value")));
            } else {
              query.setParameter(key, JsonToDataConverter.convertJsonToPropertyValue(
                  PropertyByType.infer(value), value));
            }
          }
        }

        // XXX: for standard params (client, org, pos), no need to add as extra
        if (!queryParams.isEmpty()) {
          for (String param : queryParams) {
            if (jsonsent.has(param)) {
              Object value = jsonsent.get(param);
              if (value instanceof JSONObject) {
                JSONObject jsonvalue = (JSONObject) value;
                query.setParameter(
                    param,
                    JsonToDataConverter.convertJsonToPropertyValue(
                        PropertyByType.get(jsonvalue.getString("type")), jsonvalue.get("value")));
              } else {
                query.setParameter(param, JsonToDataConverter.convertJsonToPropertyValue(
                    PropertyByType.infer(value), value));
              }
            }
          }
        }

        if (!streamOpened) {
          JSONRowConverter.startResponse(w);
          streamOpened = true;
        }
        if (dateFormat == null) {
          totalRows += getStrategyQuery().buildResponse(w, query, firstQuery);
        } else {
          totalRows += getStrategyQuery().buildResponse(w, query, firstQuery, dateFormat);
        }
        if (totalRows > 0) {
          firstQuery = false;
        }

      }
      if (!streamOpened) {
        JSONRowConverter.startResponse(w);
        streamOpened = true;
        totalRows = 0;
      }
      JSONRowConverter.endResponse(w, totalRows);

    } catch (Exception e) {
      log.error("Error when generating query", e);
      JSONRowConverter.addJSONExceptionFields(w, e);
    } finally {
      if (isAdminMode()) {
        OBContext.restorePreviousMode();
      }
    }
  }

  public interface StrategyQuery {
    public int buildResponse(Writer w, Query query, boolean firstQuery) throws JSONException,
        IOException;

    public int buildResponse(Writer w, Query query, boolean firstQuery, String dateFormat)
        throws JSONException, IOException;
  }

  public final static StrategyQuery StrategyQueryScroll = new StrategyQuery() {
    public int buildResponse(Writer w, Query query, boolean firstQuery, String dateFormat)
        throws JSONException, IOException {
      ScrollableResults listdata = query.scroll(ScrollMode.FORWARD_ONLY);
      String[] aliases = query.getReturnAliases();
      return JSONRowConverter.buildResponse(w, Scroll.create(listdata), aliases, firstQuery,
          dateFormat);
    }

    public int buildResponse(Writer w, Query query, boolean firstQuery) throws JSONException,
        IOException {
      return buildResponse(w, query, firstQuery, null);
    }
  };

  public final static StrategyQuery StrategyQueryList = new StrategyQuery() {
    public int buildResponse(Writer w, Query query, boolean firstQuery, String dateFormat)
        throws JSONException, IOException {
      return buildResponse(w, query, firstQuery);
    }

    public int buildResponse(Writer w, Query query, boolean firstQuery) throws JSONException,
        IOException {
      List<?> listdata = query.list();
      String[] aliases = query.getReturnAliases();
      return JSONRowConverter.buildResponse(w, listdata, aliases, firstQuery);
    }
  };
}
