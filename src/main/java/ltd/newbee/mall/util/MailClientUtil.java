package ltd.newbee.mall.util;


import jakarta.annotation.Resource;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;


@Component
public class MailClientUtil {


    @Resource
    private JavaMailSender mailSender;// 引入mail依赖后即可注入该类，通过该类实现邮件发送的最终方法。

    @Value("${spring.mail.username}")
    private String from;//定义发件人 ，从配置文件中读取

    /**
     * 发送邮件功能
     *
     * @param to              收件人邮箱，随意，可以是@163.com，也可以是@qq.com
     * @param theme，主题，当前邮件主题
     * @param content，邮件内容    发送邮件失败会保存日志
     */
    public void sendMail(String to, String theme, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(theme);

            // 设置邮件内容为HTML格式
//            helper.setText("<html><head><style type=\"text/css\">" +
//                    ".mail-title {background-color: #0096d0; padding: 20px; color: #fff; font-size: 24px;}" +
//                    ".mail-content {background-color: #f1f1f1; padding: 20px;}" +
//                    ".mail-link {color: #fff; text-decoration: none;}" +
//                    "</style></head><body>" +
//                    "<div class=\"mail-title\">" + content + "</div>" +
//                    "<div class=\"mail-content\">" +
//                    "欢迎体验cross-end blog" +
//                    "</div>" +
//                    "<div class=\"mail-title\">" +
//                    "</div>" +
//                    "</body></html>", true);
            helper.setText("<html><head><style type=\"text/css\">" +
                    ".mail-container {font-family: Arial, Helvetica, sans-serif; max-width: 600px; margin: 0 auto;}" +
                    ".mail-header {background-color: #0096d0; padding: 20px 30px;}" +
                    ".mail-title {color: #fff; font-size: 28px; margin: 0;}" +
                    ".mail-content {background-color: #fff; padding: 20px 30px;}" +
                    ".mail-text {font-size: 16px; line-height: 1.5;}" +
                    ".mail-link {background-color: #0096d0; color: #fff; text-decoration: none; padding: 10px 20px; border-radius: 5px;}" +
                    ".mail-link:hover {background-color: #0075a3;}" +
                    "</style></head><body>" +
                    "<div class=\"mail-container\">" +
                    "<div class=\"mail-header\">" +
                    "<h1 class=\"mail-title\">" + content + "</h1>" +
                    "</div>" +
                    "<div class=\"mail-content\">" +
                    "<p class=\"mail-text\">欢迎您来到 star-mall。</p>" +
                    "<p class=\"mail-text\">希望您有良好的购物体验</p>" +
                    "</div>" +
                    "</div>" +
                    "</body></html>", true);


            mailSender.send(helper.getMimeMessage());

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }
}

