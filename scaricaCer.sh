openssl s_client -connect smtp.libero.it:587 -starttls smtp < /dev/null | sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p' > libero.crt
sudo keytool -delete -alias smtp-cert -keystore /usr/lib/jvm/java-17-openjdk-amd64/lib/security/cacerts -storepass changeit
sudo keytool -import -alias smtp-cert -file /home/daniele/github/danlibri/libero.crt -keystore /usr/lib/jvm/java-17-openjdk-amd64/lib/security/cacerts -storepass changeit -noprompt

