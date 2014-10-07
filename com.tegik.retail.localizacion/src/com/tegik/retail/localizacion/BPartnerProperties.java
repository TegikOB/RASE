package com.tegik.retail.localizacion;

import java.util.Arrays;
import java.util.List;
 
import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.mobile.core.model.HQLProperty;
import org.openbravo.mobile.core.model.ModelExtension;
import org.openbravo.retail.posterminal.master.BusinessPartner;
 
@Qualifier(BusinessPartner.businessPartnerPropertyExtension)
public class BPartnerProperties extends ModelExtension {
 
  @Override
  public List<HQLProperty> getHQLProperties(Object params) {
    return Arrays.asList(
            new HQLProperty("bpl.locationAddress.region.id", "region"),  
            new HQLProperty("bpl.locationAddress.addressLine2", "addressLine2"), 
            new HQLProperty("bpl.locationAddress.tdirmNumex", "tdirmNumex"), 
            new HQLProperty("bpl.locationAddress.tdirmNumin", "tdirmNumin"), 
            new HQLProperty("bpl.locationAddress.region.name", "region_name"));
  }
}