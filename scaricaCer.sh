echo scarica da begin ad end
read -p "Press any key to continue..."
#openssl s_client -connect smtp.libero.it:587 -starttls smtp
openssl s_client -connect smtp.libero.it:587 -starttls smtp < /dev/null | sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p' > libero.cer
