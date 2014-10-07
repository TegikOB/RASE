package com.tegik.tipocambio.banxico.sources;

import java.util.HashMap;

import org.apache.log4j.Logger;

public class Convertidor {

  private static final Logger log = Logger.getLogger(Convertidor.class);

  private HashMap<String, Moneda> lista;

  public Convertidor() {
    lista = new HashMap<String, Moneda>();
  }

  public void agregarLista(String moneda, Moneda equi) {
    moneda = moneda.trim();
    this.lista.put(moneda, equi);
  }

  public Moneda find(String moneda) {
    log.info("ESTA REGRESANDO UN RETORNO");
    return lista.get(moneda);

  }

}
