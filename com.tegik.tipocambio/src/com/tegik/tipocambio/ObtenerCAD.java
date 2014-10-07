package com.tegik.tipocambio;

import java.rmi.RemoteException;

import javax.xml.rpc.ServiceException;

import org.apache.log4j.Logger;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.scheduling.ProcessLogger;
import org.openbravo.service.db.DalBaseProcess;

import com.tegik.tipocambio.banxico.DgieWSLocator;
import com.tegik.tipocambio.banxico.DgieWSPort;
import com.tegik.tipocambio.banxico.LeerXML;
import com.tegik.tipocambio.banxico.sources.Convertidor;
import com.tegik.tipocambio.banxico.sources.Finder;
import com.tegik.tipocambio.banxico.sources.Moneda;
import com.tegik.tipocambio.banxico.sources.Salvar;

//the background process needs to extend DalBaseProcess since
//we will be using DAL objects to perform database operations
public class ObtenerCAD extends DalBaseProcess {

  static int counter = 0;

  private ProcessLogger logger;
  private static final Logger log = Logger.getLogger(ObtenerCAD.class);

  public void doExecute(ProcessBundle bundle) throws Exception {

    logger = bundle.getLogger(); // this logger logs into the LOG column of

    DgieWSPort stub = null;
    DgieWSLocator inicio = new DgieWSLocator();

    try {
      Currency moneda = Finder.findCurrencyByISOCode("MXN");
      Currency monedaMeta = Finder.findCurrencyByISOCode("CAD");

      stub = inicio.getDgieWSPort();
      String resultado = stub.tiposDeCambioBanxico();
      Convertidor conv = LeerXML.obtenerConvertidordeXML(resultado);

      Moneda equi = conv.find("CAD");

      if (!(Finder.existeConversion(moneda, monedaMeta, equi.getFecha()))) {
        Salvar.salvarConversion(moneda, monedaMeta, equi);
        logger.log("Se ha guardado la conversion de la fecha " + equi.getFecha().toString()
            + " de la moneda " + moneda.getISOCode() + " a la moneda " + monedaMeta.getISOCode());
      } else {
        logger.log("Ya existe la conversion de la fecha " + equi.getFecha().toString()
            + " de la moneda " + moneda.getISOCode() + " a la moneda " + monedaMeta.getISOCode());
      }

      if (!(Finder.existeConversion(monedaMeta, moneda, equi.getFecha()))) {
        Salvar.salvarConversionInversa(monedaMeta, moneda, equi);
        logger.log("Ya existe la conversion de la fecha " + equi.getFecha().toString()
            + " de la moneda " + monedaMeta.getISOCode() + " a la moneda " + moneda.getISOCode());
      } else {

        logger.log("Ya existe la conversion de la fecha " + equi.getFecha().toString()
            + " de la moneda " + monedaMeta.getISOCode() + " a la moneda " + moneda.getISOCode());
      }

    } catch (ServiceException e) {
      logger.log(e.getMessage());
      log.info(e.getMessage());
    } catch (RemoteException e) {
      logger.log(e.getMessage());
      log.info(e.getMessage());
    } catch (NullPointerException vacio) {
      logger.log("No hay conversion el dia de hoy");
      log.info("No hay conversion el dia de hoy");

    }
  }

}