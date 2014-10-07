/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.config.process;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.plm.Product;
import org.openbravo.retail.config.OBRETCOProductList;
import org.openbravo.retail.config.OBRETCOProlProduct;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.scheduling.ProcessLogger;
import org.openbravo.service.db.DbUtility;

public class IncludeAllProducts implements org.openbravo.scheduling.Process {
  private ProcessLogger logger;
  private static final Logger log4j = Logger.getLogger(IncludeAllProducts.class);

  @Override
  public void execute(ProcessBundle bundle) throws Exception {
    logger = bundle.getLogger();
    OBError msg = new OBError();
    msg.setType("Success");
    msg.setTitle(OBMessageUtils.messageBD("Success"));

    try {
      OBContext.setAdminMode(true);
      final String recordID = (String) bundle.getParams().get("Obretco_Productlist_ID");
      OBRETCOProductList productList = OBDal.getInstance().get(OBRETCOProductList.class, recordID);

      final StringBuilder whereClause = new StringBuilder();
      final List<Object> parameters = new ArrayList<Object>();
      whereClause.append(" as p ");
      whereClause.append(" where not exists (select 1 from OBRETCO_Prol_Product as retpro ");
      whereClause.append("where retpro.product.id = p.id and retpro.obretcoProductlist.id = ?) ");
      whereClause.append("and p.sale = 'Y' ");
      whereClause.append("and p.organization.id in (");
      whereClause.append(Utility
          .getInStrSet(OBContext.getOBContext().getOrganizationStructureProvider()
              .getNaturalTree(productList.getOrganization().getId())));
      whereClause.append(")");
      parameters.add(productList.getId());
      final OBQuery<Product> obq = OBDal.getInstance().createQuery(Product.class,
          whereClause.toString(), parameters);
      obq.setFilterOnReadableOrganization(false);

      final ScrollableResults productScroll = obq.scroll(ScrollMode.FORWARD_ONLY);
      int i = 0;
      try {
        while (productScroll.next()) {
          i++;
          final Product product = (Product) productScroll.get()[0];

          OBRETCOProlProduct newProductLine = OBProvider.getInstance()
              .get(OBRETCOProlProduct.class);
          newProductLine.setOrganization(productList.getOrganization());
          newProductLine.setProduct(product);
          newProductLine.setObretcoProductlist(productList);
          OBDal.getInstance().save(newProductLine);

          if ((i % 100) == 0) {
            OBDal.getInstance().flush();
            OBDal.getInstance().getSession().clear();
          }
        }
      } finally {
        productScroll.close();
      }

      bundle.setResult(msg);
      OBDal.getInstance().commitAndClose();

    } catch (Exception e) {
      String message = DbUtility.getUnderlyingSQLException(e).getMessage();
      logger.log(message);
      log4j.error(message, e);
      msg.setType("Error");
      msg.setTitle(OBMessageUtils.messageBD("Error"));
      msg.setMessage(message);
      bundle.setResult(msg);
      OBDal.getInstance().rollbackAndClose();
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
