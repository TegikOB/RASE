/*
 ************************************************************************************
 * Copyright (C) 2009-2010 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.idl.proc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.ListIterator;

import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.idl.initial_data_load.stockjob_0_1.StockJob;
import org.openbravo.materialmgmt.InventoryCountProcess;
import org.openbravo.model.common.enterprise.Locator;
import org.openbravo.model.common.enterprise.Warehouse;
import org.openbravo.model.common.geography.Country;
import org.openbravo.model.common.geography.Location;
import org.openbravo.model.common.geography.Region;
import org.openbravo.model.common.plm.AttributeSet;
import org.openbravo.model.common.plm.AttributeSetInstance;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.materialmgmt.transaction.InventoryCount;
import org.openbravo.model.materialmgmt.transaction.InventoryCountLine;

/**
 * 
 * @author adrian
 */
public class StockProcess extends IdlServiceETL {

  final StockJob job = new StockJob();
  InventoryCount inventoryGeneral = null;
  ArrayList<InventoryCount> inventoryList = new ArrayList<InventoryCount>();

  @Override
  protected String[][] runJob(String[] args) {
    return job.runJob(args);
  }

  @Override
  protected String getStatus() {
    return job.getStatus();
  }

  @Override
  protected void clear() {
    job.globalBuffer.clear();
  }

  @Override
  protected BaseOBObject internalProcess(Object... values) throws Exception {

    return createStock((String) values[0], (String) values[1], (String) values[2],
        (String) values[3], (String) values[4], (String) values[5], (String) values[6],
        (String) values[7], (String) values[8], (String) values[9], (String) values[10],
        (String) values[11], (String) values[12], (String) values[13], (String) values[14],
        (String) values[15], (String) values[16], (String) values[17], (String) values[18],
        (String) values[19], (String) values[20], (String) values[21], (String) values[22],
        (String) values[23], (String) values[24]);

  }

  @Override
  public Parameter[] getParameters() {
    return new Parameter[] { new Parameter("Organization", Parameter.STRING),
        new Parameter("TransactionalOrg", Parameter.STRING),
        new Parameter("Name", Parameter.STRING), new Parameter("Description", Parameter.STRING),
        new Parameter("MovementDate", Parameter.STRING),
        new Parameter("Warehouse", Parameter.STRING),
        new Parameter("WarehouseName", Parameter.STRING),
        new Parameter("WarehouseDescription", Parameter.STRING),
        new Parameter("Warehouse1stLine", Parameter.STRING),
        new Parameter("Warehouse2ndLine", Parameter.STRING),
        new Parameter("WarehousePostalCode", Parameter.STRING),
        new Parameter("WarehouseCity", Parameter.STRING),
        new Parameter("WarehouseRegion", Parameter.STRING),
        new Parameter("WarehouseCountry", Parameter.STRING),
        new Parameter("WarehouseBinSeparator", Parameter.STRING),
        new Parameter("Bin", Parameter.STRING), new Parameter("BinIsDefault", Parameter.STRING),
        new Parameter("BinPriority", Parameter.STRING), new Parameter("BinRowX", Parameter.STRING),
        new Parameter("BinStackY", Parameter.STRING), new Parameter("BinLevelZ", Parameter.STRING),
        new Parameter("Product", Parameter.STRING),
        new Parameter("AttributeValue", Parameter.STRING),
        new Parameter("Quantity", Parameter.STRING), new Parameter("Cost", Parameter.STRING) };
  }

  public BaseOBObject createStock(final String Org, final String transactionalOrg,
      final String name, final String description, final String movementdate,

      final String warehouse_searchkey, final String warehouse_name,
      final String warehouse_description, final String warehouse_1stline,
      final String warehouse_2ndline, final String warehouse_postalcode,
      final String warehouse_city, final String warehouse_region, final String warehouse_country,
      final String warehouse_binseparator,

      final String bin_searchkey, final String bin_isdefault, final String bin_priority,
      final String bin_rowX, final String bin_stackY, final String bin_levelZ,

      final String product, final String attributevalue, final String quantity, final String cost)
      throws Exception {

    InventoryCount inventory = null;

    if (this.inventoryGeneral == null
        || !(this.inventoryGeneral.getOrganization().getSearchKey().equals(rowOrganization
            .getSearchKey()))) {

      inventory = OBProvider.getInstance().get(InventoryCount.class);
      // inventory.setId(SequenceIdData.getUUID());
      inventory.setActive(true);
      inventory.setOrganization(rowOrganization);
      inventory.setName(name);
      inventory.setDescription(description);
      inventory.setMovementDate(Parameter.DATE.parse(movementdate));

      this.inventoryGeneral = inventory;
      this.inventoryList.add(inventory);

    } else if (inventoryGeneral.getOrganization().getSearchKey()
        .equals(rowOrganization.getSearchKey())) {
      inventory = inventoryGeneral;
    }

    // Search Default Warehouse
    Warehouse warehouse = findDALInstance(false, Warehouse.class, new Value("searchKey",
        warehouse_searchkey));
    if (warehouse == null) {
      warehouse = OBProvider.getInstance().get(Warehouse.class);
      warehouse.setActive(true);
      warehouse.setOrganization(rowOrganization);
      warehouse.setSearchKey(warehouse_searchkey);
      warehouse.setName(warehouse_name);
      warehouse.setDescription(warehouse_description);

      Location location = OBProvider.getInstance().get(Location.class);
      location.setActive(true);
      location.setOrganization(rowOrganization);
      location.setAddressLine1(warehouse_1stline);
      location.setAddressLine2(warehouse_2ndline);
      location.setPostalCode(warehouse_postalcode);
      location.setCityName(warehouse_city);
      // Search Default Country
      Country country = findDALInstance(false, Country.class, new Value("name", warehouse_country));
      location.setCountry(country);
      location.setRegion(findDALInstance(false, Region.class, new Value("country", country),
          new Value("name", warehouse_region)));

      try {
        OBContext.setAdminMode(true);

        OBDal.getInstance().save(location);
        OBDal.getInstance().flush();
        warehouse.setLocationAddress(location);

        warehouse.setStorageBinSeparator(warehouse_binseparator);

        OBDal.getInstance().save(warehouse);
        OBDal.getInstance().flush();
      } catch (Exception e) {
        inventoryGeneral = null;
        throw e;
      } finally {
        OBContext.restorePreviousMode();
      }
    }
    inventory.setWarehouse(warehouse);
    inventory.setPosted("N");

    if (inventory.get("id") == null) {
      OBDal.getInstance().save(inventory);
      OBDal.getInstance().flush();
    }

    // Phisical inventory count line
    InventoryCountLine line = OBProvider.getInstance().get(InventoryCountLine.class);
    line.setActive(true);
    line.setOrganization(rowTransactionalOrg);
    line.setPhysInventory(inventory);
    line.setLineNo(10L);
    line.setProduct(findDALInstance(true, Product.class, new Value("searchKey", product)));

    AttributeSet attset = line.getProduct().getAttributeSet();

    if (attset != null) {
      // for some reason these entities need
      // to be processed in admin mode.
      OBContext.getOBContext().setInAdministratorMode(true);

      AttributeSetInstance attsetinst = getAttributeSetInstance(attset, attributevalue);

      line.setAttributeSetValue(attsetinst);

      OBContext.getOBContext().setInAdministratorMode(false);
    }

    line.setQuantityCount(Parameter.BIGDECIMAL.parse(quantity));
    line.setQuantityOrderBook(BigDecimal.ZERO);
    line.setBookQuantity(BigDecimal.ZERO);
    line.setUOM(line.getProduct().getUOM());

    Locator locator = findDALInstance(false, Locator.class, new Value(Locator.PROPERTY_WAREHOUSE,
        warehouse), new Value(Locator.PROPERTY_SEARCHKEY, bin_searchkey));
    if (locator == null) {
      locator = OBProvider.getInstance().get(Locator.class);
      locator.setActive(true);
      locator.setOrganization(rowOrganization);
      locator.setWarehouse(warehouse);
      locator.setSearchKey(bin_searchkey);
      locator.setDefault(Parameter.BOOLEAN.parse(bin_isdefault));
      locator.setRelativePriority(Parameter.LONG.parse(bin_priority));
      locator.setRowX(bin_rowX);
      locator.setStackY(bin_stackY);
      locator.setLevelZ(bin_levelZ);
      OBDal.getInstance().save(locator);
      OBDal.getInstance().flush();
    }
    line.setStorageBin(locator);
    if (cost != null) {
      line.setCost(Parameter.BIGDECIMAL.parse(cost));
    }

    OBDal.getInstance().save(line);
    OBDal.getInstance().flush();

    return inventory;
  }

  @Override
  protected void postProcess() throws Exception {
    ListIterator<InventoryCount> iterador = this.inventoryList.listIterator();
    // Execute
    try {
      while (iterador.hasNext()) {
        new InventoryCountProcess().processInventory(iterador.next());
      }
      // End process
      OBDal.getInstance().commitAndClose();
    } catch (Exception e) {
      OBDal.getInstance().rollbackAndClose();
      throw e;

    } finally {
      this.inventoryGeneral = null;
      this.inventoryList.clear();
    }

  }
}
