package com.example.offerteamazon.telegram;

import com.example.offerteamazon.server.HttpServerManager;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.BotSession;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Properties;
import java.util.stream.Collectors;

@Component
public class OfferteBOT extends TelegramLongPollingBot {

    private static final String SMTP_LIBERO_IT = "smtp.libero.it";
    private static final String SMTP_PORT = "587";
    private final String REMOTE_FTP_PATH = "/home/daniele/libri";
    private final String PATH_TMP = "tmp/";
    private final String SERVER_FTP = "85.235.148.177";
    private final int PORT_FTP = 22;
    private final String MAIL_INVIO = "dan.car@libero.it";
    private final String MAIL_KINDLE_D="dancarlu@kindle.com";
    private final String MAIL_KINDLE_F="frankcarlu@kindle.com";
    private final String MAIL_KINDLE_R="rdacqua@kindle.com";
    private final String MAIL_PROVA = "carlucci.daniele@gmail.com";
    enum INVII {FTP, RD, FRANK, DANK, MAIL}

    @Value("${myChatId}")
    Long MY_CHAT_ID;

    @Value("${userFtp}")
    String userFtp;

    @Value("${passwordFtp}")
    String passFtp;

    @Value("${tokenBot}")
    String tokenBot;

    @Value("${passwordMail}")
    String passwordMail;


    private BotSession registerBot;
    private OfferteBOT offerteBOT;
    private INVII invio = INVII.MAIL;
    private boolean serverStart=false;

    @Autowired
    HttpServerManager manager;

    // Aggiungi questo metodo per configurare il TrustManager
    private void disableCertificateValidation() {
        TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                }
        };

        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage()) {
                Long chatId = update.getMessage().getChatId();
                if (update.getMessage().hasText()) {
                    String text = update.getMessage().getText();
                    if (text.equals("HELP")) {
                        execute(creaSendMessage(chatId, "START STOP " + Arrays.stream(INVII.values()).collect(Collectors.toList()) + "->" + invio + " / " + serverStart, false));
                    } else if (text.equals("START")) {
                        if (!serverStart) {
                            manager.startServer();
                            serverStart=true;
                        }
                    } else if (text.equals("STOP")) {
                        if (serverStart) {
                            manager.stopServer();
                            serverStart=false;
                        }
                    } else if (text.equals("killMe")) {
                        offerteBOT.stopBot();
                    } else if (text.equals(INVII.DANK.name())) {
                        invio = INVII.DANK;
                    } else if (text.equals(INVII.FRANK.name())) {
                        invio = INVII.FRANK;
                    } else if (text.equals(INVII.RD.name())) {
                        invio = INVII.RD;
                    } else if (text.equals(INVII.MAIL.name())) {
                        invio = INVII.MAIL;
                    } else if (text.equals(INVII.FTP.name())) {
                        invio = INVII.FTP;
                    } else {
                        execute(creaSendMessage(chatId, text, true));
                    }
                } else if (update.getMessage().hasDocument()) {
                    downloadAndSend(update.getMessage().getDocument());
                }
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

    private void downloadAndSend(Document updateDocument) {
        Document document = new Document();
        document.setMimeType(updateDocument.getMimeType());
        document.setFileName(updateDocument.getFileName());
        document.setFileSize(updateDocument.getFileSize());
        document.setFileId(updateDocument.getFileId());
        GetFile getFile = new GetFile();
        getFile.setFileId(document.getFileId());
        try {
            org.telegram.telegrambots.meta.api.objects.File file = execute(getFile);
            String filePath = PATH_TMP + document.getFileName();
            downloadFile(file, new File(filePath));
            if (invio == INVII.MAIL) {
                inviaEmail(MAIL_PROVA, "doc P", "corpo", filePath);
            } else if (invio == INVII.DANK) {
                inviaEmail(MAIL_KINDLE_D, "doc D", "corpo", filePath);
            } else if (invio == INVII.FRANK) {
                inviaEmail(MAIL_KINDLE_F, "doc F", "corpo", filePath);
            } else if (invio == INVII.RD) {
                inviaEmail(MAIL_KINDLE_R, "doc R", "corpo", filePath);
            } else if (invio == INVII.FTP) {
                inviaFtp(filePath);
            }
            Files.delete(Paths.get(filePath));
        } catch (TelegramApiException | IOException e) {
            e.printStackTrace();
        }
    }

    private void inviaEmail(String destinatario, String oggetto, String corpo, String filePath) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_LIBERO_IT);
        props.put("mail.smtp.port", SMTP_PORT);
        // Trust all certificates (for testing only)
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                }
        };

        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(MAIL_INVIO, passwordMail);
            }
        });
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(MAIL_INVIO));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
            message.setSubject(oggetto);
            MimeMultipart multipart = new MimeMultipart();
            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText(corpo);
            multipart.addBodyPart(textPart);
            if (filePath != null && !filePath.isEmpty()) {
                MimeBodyPart attachmentPart = new MimeBodyPart();
                attachmentPart.attachFile(new File(filePath));
                multipart.addBodyPart(attachmentPart);
            }
            message.setContent(multipart);
            Transport.send(message);
            System.out.println("Mail inviata a: " + destinatario + " con file: " + filePath);
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getBotUsername() {
        return "FantaCronacaLiveBot";
    }

    @Override
    public String getBotToken() {
        return tokenBot;
    }

    @PostConstruct
    public OfferteBOT inizializza() throws Exception {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        System.out.println(tokenBot);
        disableCertificateValidation(); // Disabilita la validazione del certificato
        offerteBOT = this;
        registerBot = telegramBotsApi.registerBot(offerteBOT);
        offerteBOT.inviaMessaggio(MY_CHAT_ID, "AVVIATO SENZA CERTIFICATO");
        return offerteBOT;
    }

    public void inviaMessaggio(long chatId, String msg) throws TelegramApiException {
        try {
            while (msg.length() > 4000) {
                execute(creaSendMessage(chatId, msg.substring(0, 4000)));
                msg = msg.substring(4000);
            }
            execute(creaSendMessage(chatId, msg));

        } catch (TelegramApiException e) {
            throw e;
        }
    }

    private SendMessage creaSendMessage(long chatId, String msg) {
        return creaSendMessage(chatId, msg, false);
    }

    private SendMessage creaSendMessage(long chatId, String msg, boolean bReply) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableHtml(true);
        sendMessage.setParseMode("html");
        sendMessage.setChatId(Long.toString(chatId));
        String messaggio = "";
        String rep = " ";
        if (bReply) {
            for (int i = 0; i < msg.length(); i++) {
                rep = rep + "\\u" + Integer.toHexString(msg.charAt(i)).toUpperCase();
            }
            rep = rep + " ";

            rep = rep + " --> ";
            byte[] bytes = msg.getBytes();
            for (int i = 0; i < bytes.length; i++) {
                rep = rep + bytes[i] + ",";
            }
            messaggio = "<b>sono il bot reply</b> per  " + chatId;
        }
        messaggio = messaggio + "\n" + msg;
        if (bReply) {
            messaggio = messaggio + "\n" + rep;
        }
        sendMessage.setText(messaggio);
        return sendMessage;
    }

    public void startBot() {
        registerBot.start();
    }

    public void stopBot() {
        registerBot.stop();
    }

    public boolean isRunning() {
        return registerBot.isRunning();
    }

    private SendPhoto sendImageToChat(String imageUrl, String msg, String chatId) {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(chatId);
        sendPhoto.setPhoto(new InputFile(imageUrl));
        sendPhoto.setCaption(msg);
        return sendPhoto;
    }

    private void inviaFtp(String localFilePath) {
        com.jcraft.jsch.Session session = null;
        ChannelSftp channelSftp = null;
        try {
            JSch jsch = new JSch();
            session = jsch.getSession(userFtp, SERVER_FTP, PORT_FTP);
            session.setPassword(passFtp);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();

            channelSftp.put(localFilePath, REMOTE_FTP_PATH);
            System.out.println("File caricato con successo: " + localFilePath




            );

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (channelSftp != null) {
                channelSftp.disconnect();
            }
            if (session != null) {
                session.disconnect();
            }
        }
    }
}
