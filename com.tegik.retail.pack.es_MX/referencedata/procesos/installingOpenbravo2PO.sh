apt-get install wget
apt-get install unzip


if [ ! -d /home/openbravo/localizacion ]; then
	mkdir /home/openbravo/localizacion
	cd /home/openbravo/localizacion/
	wget http://sourceforge.net/projects/openbravo/files/08-openbravo-tools/openbravo2po%20v2.3/openbravo2po_v2.3.zip
	unzip openbravo2po_v2.3.zip 
	cd openbravo2po_v2.3
	ant jar
	ant test
fi



