package com.tegik.declaracion.bancos.mexico.sources;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.log4j.Logger;
import org.openbravo.model.common.businesspartner.BusinessPartner;

public class DeclaracionBancos {
  private static final Logger log = Logger.getLogger(DeclaracionBancos.class);
  BusinessPartner bp = null;
  BigDecimal cramount;
  BigDecimal dramount;
  String description;
  Date transactionDate;
  String referenceNo;
  String name;

  public DeclaracionBancos() {
    this.cramount = BigDecimal.ZERO;
    this.dramount = BigDecimal.ZERO;
    this.referenceNo = "**";
    this.name = "";
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    if (!this.equals(""))
      this.name = name;
  }

  public String getReferenceNo() {
    return referenceNo;
  }

  public void setReferenceNo(String referenceNo) {
    this.referenceNo = referenceNo;
  }

  public BusinessPartner getBp() {
    return bp;
  }

  public void setBp(BusinessPartner bp) {
    if (bp != null) {
      setName(bp.getName());
    }
    this.bp = bp;
  }

  public BigDecimal getCramount() {
    return cramount;
  }

  public void setCramount(BigDecimal cramount) {
    if (cramount == null) {
      this.cramount = BigDecimal.ZERO;

    } else {
      this.cramount = cramount;
    }
  }

  public BigDecimal getDramount() {
    return dramount;
  }

  public void setDramount(BigDecimal dramount) {
    if (cramount == null) {
      this.dramount = BigDecimal.ZERO;
    } else {
      this.dramount = dramount;
    }

  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Date getTransactionDate() {
    return transactionDate;
  }

  public void setTransactionDate(Date transactionDate) {
    this.transactionDate = transactionDate;
  }

}
