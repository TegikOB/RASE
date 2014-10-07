
totalModulos=14
carpetaTraduccion=/opt/OpenbravoERP-3.0/attachments/lang/es_MX/
modulosTraduccion=(
com.tegik.workspace.widget.es_MX org.openbravo.client.myob
com.tegik.user.interface.selector.es_MX org.openbravo.userinterface.selector
com.tegik.query.list.widget.es_MX org.openbravo.client.querylist
com.tegik.advanced.pay.recei.es_MX org.openbravo.advpaymentmngt
com.tegik.widget.collection.es_MX org.openbravo.client.widgets
com.tegik.smartclient.es_MX org.openbravo.userinterface.smartclient
com.tegik.user.interface.kernel.es_MX org.openbravo.client.kernel
com.tegik.order.awaiting.delivery.es_MX org.openbravo.reports.ordersawaitingdelivery
com.tegik.integration.google.api.es_MX org.openbravo.service.integration.google
com.tegik.openid.service.integration.es_MX org.openbravo.service.integration.openid
com.tegik.json.datasource.es_MX org.openbravo.service.datasource
com.tegik.user.application.es_MX org.openbravo.client.application
com.tegik.payment.repor.es_MX org.openbravo.financial.paymentreport
com.tegik.html.widget.es_MX org.openbravo.client.htmlwidget )


if [ ! -d $carpetaTraduccion/core ]; then
   mkdir -p $carpetaTraduccion/core
fi
cp  $carpetaTraduccion/*es_MX.xml  $carpetaTraduccion/core


cd /home/openbravo/localizacion/openbravo2po_v2.3/
for (( i=0; i < (totalModulos*2); i=i+2))
do

	rm -r /home/openbravo/localizacion/${modulosTraduccion[i]}/*
	ant -lib /home/openbravo/localizacion/openbravo2po_v2.3/lib/openbravo2poTest.jar runXML2PO -DinpFold=$carpetaTraduccion/${modulosTraduccion[i+1]} -DoutFold=/home/openbravo/localizacion/${modulosTraduccion[i]} -DmsgStr=yes
	chown openbravo:openbravo /home/openbravo/localizacion/${modulosTraduccion[i]}/*
	
done

echo  "Esta actualizando core"
rm -r /home/openbravo/localizacion/com.tegik.translate.principalcore.es_MX/*
 ant -lib /home/openbravo/localizacion/openbravo2po_v2.3/lib/openbravo2poTest.jar runXML2PO -DinpFold=$carpetaTraduccion/core -DoutFold=/home/openbravo/localizacion/com.tegik.translate.principalcore.es_MX -DmsgStr=yes
chown openbravo:openbravo /home/openbravo/localizacion/com.tegik.translate.principalcore.es_MX/*

