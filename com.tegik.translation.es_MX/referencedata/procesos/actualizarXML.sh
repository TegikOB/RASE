totalModulos=14
Modulos=/opt/OpenbravoERP-3.0/openbravo-erp/modules
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



for (( i=0; i < (totalModulos*2); i=i+2))
do
        j=i+1
	echo "Actualizando el modulo:" ${modulosTraduccion[i]}
        rm $Modulos/${modulosTraduccion[i]}/referencedata/translation/es_MX/*
        cp  $carpetaTraduccion/${modulosTraduccion[j]}/*  $Modulos/${modulosTraduccion[i]}/referencedata/translation/es_MX
	chown openbravo:openbravo  $Modulos/${modulosTraduccion[i]}/referencedata/translation/es_MX/*
done

echo "actualizando el core"
rm $Modulos/com.tegik.translate.principalcore.es_MX/referencedata/translation/es_MX/*
cp  $carpetaTraduccion/*.xml $Modulos/com.tegik.translate.principalcore.es_MX/referencedata/translation/es_MX
chown openbravo:openbravo  $Modulos/com.tegik.translate.principalcore.es_MX/referencedata/translation/es_MX

