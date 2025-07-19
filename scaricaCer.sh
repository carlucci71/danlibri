openssl s_client -connect smtp.libero.it:587 -starttls smtp < /dev/null | sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p' > libero.crt
sudo keytool -delete -alias smtp-cert -storepass changeit -cacerts
sudo keytool -import -alias smtp-cert -file /home/daniele/github/danlibri/libero.crt -storepass changeit -noprompt -cacerts
