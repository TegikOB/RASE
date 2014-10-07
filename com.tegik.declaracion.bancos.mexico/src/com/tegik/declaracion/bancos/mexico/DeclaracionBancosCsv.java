package com.tegik.declaracion.bancos.mexico;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.openbravo.advpaymentmngt.utility.FIN_BankStatementImport;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.financialmgmt.payment.FIN_BankStatement;
import org.openbravo.model.financialmgmt.payment.FIN_BankStatementLine;

import com.tegik.declaracion.bancos.mexico.sources.DeclaracionBancos;
import com.tegik.declaracion.bancos.mexico.utilities.ExceptionConfig;
import com.tegik.declaracion.bancos.mexico.utilities.ExceptionConvertObject;
import com.tegik.declaracion.bancos.mexico.utilities.ExceptionFile;
import com.tegik.declaracion.bancos.mexico.utilities.Utilidad;

public class DeclaracionBancosCsv extends FIN_BankStatementImport {
  private static final Logger log = Logger.getLogger(DeclaracionBancosCsv.class);

  @Override
  public List<FIN_BankStatementLine> loadFile(InputStream in, FIN_BankStatement targetBankStatement) {
    OBContext.setAdminMode(true);
    List<FIN_BankStatementLine> newLines = new ArrayList<FIN_BankStatementLine>();
    OBError msg = new OBError();
    msg.setType("Error");

    try {

      BanMexConfiguracion config = targetBankStatement.getAccount().getBanmexConfiguracion();

      if (config == null) {
        throw new ExceptionConfig("BANMEX_NoConfigFinnaAccoun");
      }

      List<DeclaracionBancos> declaraciones = Utilidad.revisarArchivo(in, config);
      long line = 10;

      for (DeclaracionBancos declaracion : declaraciones) {
        newLines.add(getBankStatementLine(declaracion, targetBankStatement, line));
        line = line + 10;
      }

    } catch (ExceptionConfig excepConfig) {
      log.info(excepConfig.getMessage());
      msg.setTitle("Error en la configuración:");
      msg.setMessage(excepConfig.getMessageOpenbravo());
      setMyError(msg);

    } catch (ExceptionFile excepFile) {
      log.info(excepFile.getMessage());
      msg.setTitle("Error en el archivo: ");
      msg.setMessage(excepFile.getMessageOpenbravo());
      setMyError(msg);

    } catch (ExceptionConvertObject excepConvOb) {
      log.info(excepConvOb.getMessage());
      msg.setTitle("Error en el tipo de dato: ");
      msg.setMessage(excepConvOb.getMessageOpenbravo());
      setMyError(msg);

    }

    /*
     * DgieWSPort stub = null; DgieWSLocator inicio = new DgieWSLocator();
     * 
     * try { stub = inicio.getDgieWSPort(); } catch (ServiceException e) { // TODO Auto-generated
     * catch block e.printStackTrace(); } try { String resultado = stub.tiposDeCambioBanxico();
     * log.info(resultado); } catch (RemoteException e) { // TODO Auto-generated catch block
     * e.printStackTrace(); }
     */

    OBContext.restorePreviousMode();

    return newLines;
  }

  public FIN_BankStatementLine getBankStatementLine(DeclaracionBancos dec,
      FIN_BankStatement targetBankStatement, long linea) {

    FIN_BankStatementLine line = new FIN_BankStatementLine();
    line.setBankStatement(targetBankStatement);
    line.setClient(targetBankStatement.getClient());
    line.setOrganization(targetBankStatement.getOrganization());
    line.setCramount(dec.getCramount());
    line.setDescription(dec.getDescription());

    line.setTransactionDate(dec.getTransactionDate());
    line.setDramount(dec.getDramount());
    line.setBpartnername(dec.getName());

    if (dec.getBp() != null) {
      BusinessPartner bpartner = OBDal.getInstance()
          .get(BusinessPartner.class, dec.getBp().getId());
      line.setBusinessPartner(bpartner);
      line.setBpartnername(dec.getName());

    }

    line.setReferenceNo(dec.getReferenceNo() == null || dec.getReferenceNo().equals("") ? "**"
        : dec.getReferenceNo());
    line.setLineNo(linea);

    OBDal.getInstance().save(line);
    OBDal.getInstance().flush();

    return line;
  }

  public void updateBankStatement(FIN_BankStatement targetBankStatement) {

  }

}
