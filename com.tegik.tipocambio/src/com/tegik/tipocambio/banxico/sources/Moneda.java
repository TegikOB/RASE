package com.tegik.tipocambio.banxico.sources;

import java.math.BigDecimal;
import java.util.Date;

public class Moneda {

  BigDecimal cantidad;
  Date fecha;
  String moneda;

  public Moneda(String moneda, Date fecha, BigDecimal cantidad) {
    this.cantidad = cantidad;
    this.fecha = fecha;
    this.moneda = moneda;
  }

  public Moneda() {

  }

  public BigDecimal getCantidad() {
    return cantidad;
  }

  public void setCantidad(BigDecimal cantidad) {
    this.cantidad = cantidad;
  }

  public Date getFecha() {
    return fecha;
  }

  public void setFecha(Date fecha) {
    this.fecha = fecha;
  }

  public String getMoneda() {
    return moneda;
  }

  public void setMoneda(String moneda) {
    this.moneda = moneda;
  }

}
