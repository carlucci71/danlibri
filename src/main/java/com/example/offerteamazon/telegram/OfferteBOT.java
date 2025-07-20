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
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.BotSession;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

@Component
public class OfferteBOT extends TelegramLongPollingBot {

    public static final String SMTP_LIBERO_IT = "smtp.libero.it";
    public static final String SMTP_PORT = "587";
    public final String REMOTE_FTP_PATH = "/home/daniele/libri";
    public final String PATH_TMP = "tmp/";
    public final String SERVER_FTP = "85.235.148.177";
    public final int PORT_FTP = 22;
    public final String MAIL_INVIO = "dan.car@libero.it";

    enum INVII {
        FTP(null)
        , RD("rdacqua@kindle.com")
        , FRANK("frankcarlu@kindle.com")
        , DANK("dancarlu@kindle.com")
        , MAIL("carlucci.daniele@gmail.com")
        , GIMMI("d.carlucci@almaviva.it")
        ;
        private final String mail;

        INVII(String mail) {
            this.mail = mail;
        }

        public static INVII getInvioFromText(String text) {
            for (INVII value : INVII.values()) {
                if (value.name().equals(text)) {
                    return value;
                }
            }
            return null;
        }

    }

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
    private boolean serverStart = false;
    Session mailSession;
    Set<Integer> ids = new HashSet<>();

    @Autowired
    HttpServerManager manager;

    @Override
    public void onUpdateReceived(Update update) {
        try {
            Integer mId = update.getMessage().getMessageId();
            if (!ids.contains(mId)) {
                ids.add(mId);
                if (update.hasMessage()) {
                    Long chatId = update.getMessage().getChatId();
                    if (update.getMessage().hasText()) {
                        String text = update.getMessage().getText();
                        if (text.equals("killMe")) {
                            inviaMessaggio(chatId, "KILLATO");
                            offerteBOT.stopBot();
                        } else {
                            boolean help = true;
                            INVII invioFromText = INVII.getInvioFromText(text);
                            if (text.equals("START")) {
                                if (!serverStart) {
                                    manager.startServer();
                                    serverStart = true;
                                }
                            } else if (text.equals("STOP")) {
                                if (serverStart) {
                                    manager.stopServer();
                                    serverStart = false;
                                }
                            } else if (text.equals("KILL")) {
                                inviaMessaggio(chatId, "KILLATO");
                                offerteBOT.stopBot();
                            } else if (invioFromText != null) {
                                invio = invioFromText;
                            } else if (!text.equals("HELP")) {
                                help = false;
                                execute(creaSendMessage(chatId, text, true));
                            }
                            if (help) {
                                help(chatId);
                            }
                        }
                    } else if (update.getMessage().hasDocument()) {
                        downloadAndSend(update.getMessage().getDocument(), chatId);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

    private void help(Long chatId) throws TelegramApiException {
        execute(creaSendMessage(chatId, "FTP START STOP " + " [" + (serverStart ? "START" : "STOP") + "] \n" + Arrays.stream(INVII.values()).toList() + " [" + invio + "]", false));
    }

    private void downloadAndSend(Document updateDocument, Long chatId) throws TelegramApiException {
        try {
            Document document = new Document();
            document.setMimeType(updateDocument.getMimeType());
            document.setFileName(updateDocument.getFileName());
            document.setFileSize(updateDocument.getFileSize());
            document.setFileId(updateDocument.getFileId());
            GetFile getFile = new GetFile();
            getFile.setFileId(document.getFileId());
            org.telegram.telegrambots.meta.api.objects.File file = execute(getFile);
            String filePath = PATH_TMP + document.getFileName();
            downloadFile(file, new File(filePath));

            if (invio == INVII.FTP) {
                inviaFtp(filePath);
            } else {
                inviaEmail(invio.mail, document.getFileName(), "In allegato il documento: " + document.getFileName(), filePath);
            }
            Files.delete(Paths.get(filePath));
            inviaMessaggio(chatId, "Mail inviata a: " + invio.mail + " con file: " + document.getFileName());
        } catch (Exception e) {
            e.printStackTrace(System.out);
            inviaMessaggio(chatId, e.getMessage());
        }
    }

    private Session createMailSession() throws Exception {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_LIBERO_IT);
        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.ssl.socketFactory", createSSLContextFactory());
        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(MAIL_INVIO, passwordMail);
            }
        });
    }

    private SSLSocketFactory createSSLContextFactory() throws Exception {
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }
        };

        HostnameVerifier allHostsValid = (hostname, session) -> true;
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(null, trustAllCerts, new java.security.SecureRandom());
        return ctx.getSocketFactory();
    }

    private void inviaEmail(String destinatario, String oggetto, String corpo, String filePath) throws Exception {
        Message message = new MimeMessage(mailSession);
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
    }

    @Override
    public String getBotUsername() {
        return "DanReadBot";
    }

    @Override
    public String getBotToken() {
        return tokenBot;
    }

    @PostConstruct
    public OfferteBOT inizializza() throws Exception {
        mailSession = createMailSession();
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        offerteBOT = this;
        registerBot = telegramBotsApi.registerBot(offerteBOT);
        offerteBOT.inviaMessaggio(MY_CHAT_ID, "AVVIATO");
        return offerteBOT;
    }

    public void inviaMessaggio(long chatId, String msg) throws TelegramApiException {
        while (msg.length() > 4000) {
            execute(creaSendMessage(chatId, msg.substring(0, 4000)));
            msg = msg.substring(4000);
        }
        execute(creaSendMessage(chatId, msg));
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
        StringBuilder rep = new StringBuilder();
        if (bReply) {
            for (int i = 0; i < msg.length(); i++) {
                rep.append("\\u").append(Integer.toHexString(msg.charAt(i)).toUpperCase());
            }
            rep.append(" ");

            rep.append(" --> ");
            byte[] bytes = msg.getBytes();
            for (byte aByte : bytes) {
                rep.append(aByte).append(",");
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

    public void stopBot() {
        registerBot.stop();
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
        } catch (Exception ex) {
            ex.printStackTrace(System.out);
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
