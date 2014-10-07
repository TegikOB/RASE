package com.tegik.addenda.soriana.piedecamion;


import org.apache.log4j.Logger;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.common.invoice.InvoiceLine;
import org.openbravo.model.common.invoice.InvoiceTax;

import javax.xml.datatype.XMLGregorianCalendar;

import java.util.GregorianCalendar;
import java.util.Date;
import java.math.BigDecimal;
import java.util.HashMap;

import javax.xml.datatype.DatatypeFactory;

import com.tegik.addenda.module.proc.manejadorAddenda;
import com.tegik.addenda.module.proc.utilidadesAddenda;
import com.tegik.addenda.soriana.piedecamion.sources.DSCargaRemisionProv;
import com.tegik.addenda.soriana.piedecamion.sources.DSCargaRemisionProv.Articulos;
import com.tegik.addenda.soriana.piedecamion.sources.DSCargaRemisionProv.Pedidos;
import com.tegik.addenda.soriana.piedecamion.sources.DSCargaRemisionProv.Remision;
import com.tegik.addenda.soriana.sources.Convertidor;
import com.tegik.addenda.soriana.sources.Finder;
import org.openbravo.model.common.plm.ApprovedVendor;

public class GenerarAddenda extends manejadorAddenda {
  ;
  private HashMap map;
  public static final Logger log = Logger.getLogger(GenerarAddenda.class);
  
  public GenerarAddenda(String invoiceId) throws Exception {
    
    super();
    
    Invoice invoice=OBDal.getInstance().get(Invoice.class,invoiceId);  
    ObjectFactory factory = new ObjectFactory();
    addVarsGlobal(invoice);

    
    try{

      this.objetoAddenda.add(createCargaRemisionProv(factory, invoice));
      this.paqueteJavaAddenda = "com.tegik.addenda.soriana.piedecamion";
      
      this.mensajeError = null;
      this.hayError = false;      

      
    } 
    catch (Exception e) { 
      log.info(e.getMessage());
      this.hayError=true;
      this.mensajeError=e.getMessage();      
    }
    
    
  }
  
 public DSCargaRemisionProv createCargaRemisionProv(ObjectFactory factory, Invoice invoice) throws Exception {
    
    DSCargaRemisionProv cargaRemisionProv = factory.createDSCargaRemisionProv();
    
    cargaRemisionProv.addRemisionOrPedimentoOrPedidos(createRemision(factory, invoice));
    cargaRemisionProv.addRemisionOrPedimentoOrPedidos(createPedido(factory));
    for (InvoiceLine linea : invoice.getInvoiceLineList()){
      //if(linea.getTax().isContmxIsiva()){

        cargaRemisionProv.addRemisionOrPedimentoOrPedidos(createArticulo(factory, linea));
      //}
    }
    
    return cargaRemisionProv;
    
  }
 
  
  
  public void addVarsGlobal(Invoice  invoice) throws Exception{
    
    utilidadesAddenda ua = new utilidadesAddenda(invoice.getId());
    map = new HashMap();
    
    
    map.put("Tienda.numero", Convertidor.toShort(ua.obtenerDatoAddenda("Tienda", "numero")));
    map.put("Proveedor.numero", Convertidor.toInteger(invoice.getBusinessPartner().getReferenceNo()));
    
    map.put("Remision.remision", ua.obtenerDatoAddenda("Remision", "remision"));
    map.put("Remision.consecutivo", Convertidor.toShort(ua.obtenerDatoAddenda("Remision", "consecutivo")));
    map.put("Remision.fechaRemision", Convertidor.toXMLGregorianCalendar(ua.obtenerDatoAddenda("Remision", "fechaRemision"), "dd-MM-yyyy"));
    map.put("Remision.tipoMoneda", Convertidor.toShort(ua.obtenerDatoAddenda("Remision", "tipoMoneda")));
    map.put("Remision.tipoBulto", Convertidor.toShort(ua.obtenerDatoAddenda("Remision", "tipoBulto")));
    map.put("Remision.entregaMercancia", Convertidor.toShort(ua.obtenerDatoAddenda("Remision", "entregaMercancia")));
    map.put("Remision.cumpleReqFiscales",Convertidor.toBoolean(ua.obtenerDatoAddenda("Remision", "cumpleReqFiscales")));
    map.put("Remision.cantidadBultos", Convertidor.toBigDecimal(ua.obtenerDatoAddenda("Remision", "cantidadBultos")));
    map.put("Remision.descuentos", Convertidor.toBigDecimal(ua.obtenerDatoAddenda("Remision", "descuentos")));
    map.put("Remision.ieps", Convertidor.toBigDecimal(ua.obtenerDatoAddenda("Remision", "ieps")));
    map.put("Remision.otrosImpuestos", Convertidor.toBigDecimal(ua.obtenerDatoAddenda("Remision", "otrosImpuestos")));
    map.put("Remision.cantidadPedidos", Convertidor.toInteger(ua.obtenerDatoAddenda("Remision", "cantidadPedidos")));
    map.put("Remision.fechaEntregaMercancia", Convertidor.toXMLGregorianCalendar(ua.obtenerDatoAddenda("Remision", "fechaEntregaMercancia"), "dd-MM-yyyy"));
    map.put("Remision.cita", Convertidor.toInteger(ua.obtenerDatoAddenda("Remision", "cita")));
    
    map.put("Pedido.folioPedido", Convertidor.toInteger(ua.obtenerDatoAddenda("Pedido", "folioPedido")));
    map.put("Pedido.cantidadArticulos", Convertidor.toInteger(ua.obtenerDatoAddenda("Pedido", "cantidadArticulos")));
    
    
    
    
  }
  
  
  
  
  public Remision createRemision(ObjectFactory factory, Invoice invoice) throws Exception{
    
    Remision remision= factory.createDSCargaRemisionProvRemision();
    
    remision.setProveedor((Integer) map.get("Proveedor.numero"));
    remision.setRemision((String) map.get("Remision.remision") == null ?  invoice.getDocumentNo() : (String) map.get("Remision.remision"));
    remision.setConsecutivo((Short) map.get("Remision.consecutivo") == null ? new Short("0") : (Short) map.get("Remision.consecutivo"));
    remision.setFechaRemision((XMLGregorianCalendar) map.get("Remision.fechaRemision") ==  null ?  Convertidor.toXMLGregorianCalendar(new Date()) : (XMLGregorianCalendar) map.get("Remision.fechaRemision"));
    remision.setTienda((Short) map.get("Tienda.numero") );
    remision.setTipoMoneda((Short) map.get("Remision.tipoMoneda") == null ? new Short("1") : (Short) map.get("Remision.tipoMoneda"));
    remision.setTipoBulto((Short) map.get("Remision.tipoBulto") == null ? new Short("1") :  (Short) map.get("Remision.tipoBulto"));
    remision.setEntregaMercancia((Short) map.get("Remision.entregaMercancia"));
    remision.setCumpleReqFiscales((Boolean) map.get("Remision.cumpleReqFiscales") == null ? true :(Boolean) map.get("Remision.cumpleReqFiscales") );
    remision.setIEPS((BigDecimal) map.get("Remision.ieps") == null ? new BigDecimal("0") :  (BigDecimal) map.get("Remision.ieps"));
    remision.setOtrosImpuestos((BigDecimal) map.get("Remision.otrosImpuestos") == null  ?  new BigDecimal(0) :(BigDecimal) map.get("Remision.otrosImpuestos") );
    remision.setCantidadPedidos((Integer) map.get("Remision.cantidadPedidos"));
    remision.setFechaEntregaMercancia((XMLGregorianCalendar) map.get("Remision.fechaEntregaMercancia"));
    remision.setCita((Integer) map.get("Remision.cita")); 
    remision.setDescuentos((BigDecimal) map.get("Remision.descuentos") == null ? new BigDecimal("0") : (BigDecimal) map.get("Remision.descuentos")); 
    
    
    BigDecimal cantidadBultos = new BigDecimal("0.0");
    for(InvoiceLine line : invoice.getInvoiceLineList()){
      cantidadBultos = cantidadBultos.add(line.getInvoicedQuantity());      
    }
    remision.setCantidadBultos((BigDecimal) map.get("Remision.cantidadBultos") == null ? cantidadBultos : (BigDecimal) map.get("Remision.cantidadBultos"));
        
    BigDecimal subtotal = invoice.getSummedLineAmount();
    remision.setSubtotal(invoice.getSummedLineAmount());
    
    BigDecimal totalIVA = new BigDecimal(0);
    
    for(InvoiceTax invoiceTax : invoice.getInvoiceTaxList()){
      if(invoiceTax.getTax().isContmxIsiva()){
        totalIVA= totalIVA.add(invoiceTax.getTaxAmount());
      }
    }
    remision.setIVA(totalIVA);
    
    BigDecimal conIva = subtotal.add(totalIVA);
    remision.setTotal(conIva);     
    
    return remision;  
    
  }
  
  
  
  public Pedidos createPedido(ObjectFactory factory) throws Exception {
    Pedidos pedido = factory.createDSCargaRemisionProvPedidos();
    
    pedido.setProveedor((Integer) map.get("Proveedor.numero"));
    pedido.setRemision((String) map.get("Remision.remision"));
    pedido.setFolioPedido((Integer) map.get("Pedido.folioPedido"));
    pedido.setTienda((Short) map.get("Tienda.numero"));
    
    pedido.setCantidadArticulos((Integer) map.get("Pedido.cantidadArticulos"));
      
    return pedido;
  }
  
  
  
  public Articulos createArticulo(ObjectFactory factory, InvoiceLine linea) throws Exception {
    
  Articulos articulo = factory.createDSCargaRemisionProvArticulos();
  
  
  articulo.setProveedor((Integer) map.get("Proveedor.numero"));
  articulo.setRemision((String) map.get("Remision.remision"));
  articulo.setFolioPedido((Integer) map.get("Pedido.folioPedido"));
  articulo.setTienda((Short) map.get("Tienda.numero"));
  
  ApprovedVendor app= Finder.findApprovedVendorByIPartner(linea.getProduct(), linea.getInvoice().getBusinessPartner());
  
  if(app.getUPCEAN() == null)
   throw  new Exception("El codigo del  producto " + linea.getProduct().getName() +" esta en vacio. Revisar la ventana de compras en producto");
  else  if(app.getUPCEAN().equals(""))
    throw  new Exception("El codigo del  producto " + linea.getProduct().getName() +" esta en vacio. Revisar la ventana de compras en producto");
  
  
  articulo.setCodigo(Convertidor.toBigDecimal(app.getUPCEAN()));
  articulo.setCantidadUnidadCompra(linea.getInvoicedQuantity());
  articulo.setCostoNetoUnidadCompra(linea.getUnitPrice());
  articulo.setPorcentajeIEPS(new BigDecimal("0.00"));
  articulo.setPorcentajeIVA(linea.getTax().getRate());
  
  return articulo;
  
  }


}
