package com.tegik.retail.localizacion;

import java.util.Arrays;
import java.util.List;
 
import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.mobile.core.model.HQLProperty;
import org.openbravo.mobile.core.model.ModelExtension;
import org.openbravo.retail.posterminal.master.BPLocation;
 
@Qualifier(BPLocation.bpLocationPropertyExtension)
public class BPLocProperties extends ModelExtension {
 
  @Override
  public List<HQLProperty> getHQLProperties(Object params) {
    return Arrays.asList(
            new HQLProperty("bploc.locationAddress.region.id", "region"),  
            new HQLProperty("bploc.locationAddress.addressLine2", "addressLine2"), 
            new HQLProperty("bploc.locationAddress.tdirmNumex", "tdirmNumex"), 
            new HQLProperty("bploc.locationAddress.tdirmNumin", "tdirmNumin"), 
            new HQLProperty("bploc.locationAddress.region.name", "region_name"));
  }
}