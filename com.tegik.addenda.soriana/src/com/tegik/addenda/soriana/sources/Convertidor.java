package com.tegik.addenda.soriana.sources;

import java.text.ParseException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.datatype.DatatypeConstants;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;




import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;

import java.io.InputStream;

import com.tegik.addenda.soriana.LlamarServicioWeb;
import com.tegik.addenda.soriana.sources.ackerrorapl.AckErrorApplication;
import com.tegik.addenda.soriana.sources.ackerrorapl.AckErrorApplication;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.math.BigDecimal;
import java.util.GregorianCalendar;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.datatype.DatatypeFactory;

public class Convertidor {

  
  public static Short toShort(String cadenaNumero) throws Exception {
    
    if(cadenaNumero == null)
      return null;
    
    try{
    short numero= Short.parseShort(cadenaNumero);
    return numero;
    }catch (NumberFormatException e){
      throw new Exception("La cadena " + cadenaNumero + " no se puede convertir a numerico" );
    }
  }
  
  
  public static Integer toInteger(String cadenaNumero)throws Exception{
    
    if(cadenaNumero == null)
      return null;
    
    try{
      
    int numero= Integer.parseInt(cadenaNumero);
    return numero;
    
    }catch (NumberFormatException e){
      throw new Exception("La cadena " + cadenaNumero + " no se puede convertir a numerico" );

    }
    
  }
  
  
  public static Date toDate(String cadenaFecha, String formato) throws Exception {
    
    if(cadenaFecha == null){
      return null;
    }
    
   try{

    SimpleDateFormat fecha_formato = new SimpleDateFormat(formato);
    Date date = fecha_formato.parse(cadenaFecha);
    
    return date;
    } catch(ParseException e ){
       throw new Exception("La cadena " + cadenaFecha + " no tiene el siguiente formato " +  formato);
    }
  }
  
  public static XMLGregorianCalendar toXMLGregorianCalendar(String cadenaFecha, String formato) throws Exception{
    
    Date fecha = toDate(cadenaFecha, formato);
    if(fecha == null){
      return null;
    }
    
    GregorianCalendar c = new GregorianCalendar();
    c.setTime(fecha);
    XMLGregorianCalendar xml = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
    xml.setMillisecond(DatatypeConstants.FIELD_UNDEFINED);
    xml.setTimezone(DatatypeConstants.FIELD_UNDEFINED);    
    
    return xml;
    
  }
  
 public static XMLGregorianCalendar toXMLGregorianCalendar(Date fecha) throws Exception{
    
    
    GregorianCalendar c = new GregorianCalendar();
    c.setTime(fecha);
    XMLGregorianCalendar xml = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
    xml.setMillisecond(DatatypeConstants.FIELD_UNDEFINED);
    xml.setTimezone(DatatypeConstants.FIELD_UNDEFINED);    
    
    return xml;
  }
  
  
 public static Boolean toBoolean(String cadena) {
   
   if(cadena == null) return null;
    
    cadena= cadena.toUpperCase();
    
    if(cadena.equals("SI") && cadena.equals("S") && cadena.equals("YES")
        && cadena.equals("TRUE") && cadena.equals("Y")){
      return true;
    } else if(cadena.equals("NO") && cadena.equals("N") && cadena.equals("FALSE"))  
    return false;
    
    
    return true;
    
  }
  
 
 public static BigDecimal toBigDecimal(String cadenaNumero){
   
   if(cadenaNumero == null){
     return null;
   }
   
   return new BigDecimal(cadenaNumero);
 }
 
  
 public static Document toDocument(String xml) throws ParserConfigurationException, SAXException, IOException{
 
 
 DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
 InputSource is = new InputSource();
 is.setCharacterStream(new StringReader(xml));

 Document doc = db.parse(is);
 return doc;
 }
  
 
 public static AckErrorApplication toAckErrorApplication(String xml) throws  Exception {
  
     JAXBContext jc = JAXBContext.newInstance( "com.tegik.addenda.soriana.sources.ackerrorapl" );
     Unmarshaller u = jc.createUnmarshaller();
     InputStream inputStream =  new ByteArrayInputStream(xml.getBytes());
     AckErrorApplication o = (AckErrorApplication) u.unmarshal(new StreamSource(inputStream));    
     return o;

}
 
}
