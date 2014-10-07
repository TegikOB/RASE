package com.tegik.addenda.soriana.sources;


import javax.xml.bind.JAXBException;


import org.apache.log4j.Logger;





import com.tegik.addenda.soriana.sources.ackerrorapl.AckErrorApplication;

public class ExceptionServicioWeb extends Exception {
  
  private static final Logger log = Logger.getLogger(ExceptionServicioWeb.class);
;
  
  ExceptionServicioWeb(String mensaje){
    super(mensaje);
  }
  
  
    
  

}
