package com.tegik.addenda.soriana.sources;
import java.util.List;

import org.hibernate.criterion.Restrictions;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.plm.ApprovedVendor;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.common.businesspartner.BusinessPartner;



public class Finder {
  
  public static ApprovedVendor findApprovedVendorByIPartner(Product product, BusinessPartner bp) throws Exception {

    OBContext.setAdminMode(true);
    
    OBCriteria<ApprovedVendor> app = OBDal.getInstance().createCriteria(ApprovedVendor.class);
    app.add(Restrictions.eq(ApprovedVendor.PROPERTY_PRODUCT, product));
    app.add(Restrictions.eq(ApprovedVendor.PROPERTY_BUSINESSPARTNER, bp));
    List<ApprovedVendor> apps = app.list();

    if (apps.isEmpty()) {
      throw new Exception("No hay un codigo asignado del producto " + product.getName()+ " para la tienda " +  bp.getName());

    }
    

    OBContext.restorePreviousMode();

    return apps.get(0);

  }
  
  

}
