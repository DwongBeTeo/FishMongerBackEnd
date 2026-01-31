package datn.duong.FishSeller.util;

public class EmailUtils {
    public static String getOtpEmailTemplate(String name, String otp) {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <title>Reset Password</title>\n" +
                "</head>\n" +
                "<body style=\"margin: 0; padding: 0; font-family: Arial, sans-serif; background-color: #f4f4f4;\">\n" +
                "    <table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" style=\"background-color: #f4f4f4; padding: 40px 0;\">\n" +
                "        <tr>\n" +
                "            <td align=\"center\">\n" +
                "                <table role=\"presentation\" width=\"600\" cellspacing=\"0\" cellpadding=\"0\" style=\"background-color: #ffffff; border-radius: 8px; box-shadow: 0 4px 6px rgba(0,0,0,0.1); overflow: hidden;\">\n" +
                "                    <tr>\n" +
                "                        <td align=\"center\" style=\"padding: 40px 0 20px 0; background-color: #eef2ff;\">\n" +
                "                           <h1 style=\"color: #4F46E5; margin: 0; font-size: 24px;\">FISH SELLER</h1>\n" +
                "                        </td>\n" +
                "                    </tr>\n" +
                "                    <tr>\n" +
                "                        <td style=\"padding: 40px 40px;\">\n" +
                "                            <h2 style=\"margin: 0 0 20px 0; color: #333333; font-size: 20px; text-align: center;\">Password Reset</h2>\n" +
                "                            <p style=\"margin: 0 0 20px 0; color: #666666; line-height: 1.6; text-align: center;\">\n" +
                "                                Xin chào <strong>" + name + "</strong>,<br>\n" +
                "                                Chúng tôi nhận được yêu cầu đặt lại mật khẩu. Sử dụng mã OTP bên dưới để tiếp tục:\n" +
                "                            </p>\n" +
                "                            <div style=\"background-color: #f8fafc; border: 1px solid #e2e8f0; border-radius: 8px; padding: 20px; text-align: center; margin: 30px 0;\">\n" +
                "                                <span style=\"font-size: 32px; font-weight: bold; color: #4F46E5; letter-spacing: 10px; font-family: monospace;\">" + otp + "</span>\n" +
                "                            </div>\n" +
                "                            <p style=\"margin: 0; color: #666666; font-size: 14px; text-align: center;\">\n" +
                "                                Mã này sẽ hết hạn sau <strong>10 phút</strong>.<br>\n" +
                "                                Nếu bạn không yêu cầu, vui lòng bỏ qua email này.\n" +
                "                            </p>\n" +
                "                        </td>\n" +
                "                    </tr>\n" +
                "                    <tr>\n" +
                "                        <td style=\"background-color: #f8fafc; padding: 20px; text-align: center; border-top: 1px solid #e2e8f0;\">\n" +
                "                            <p style=\"margin: 0; color: #999999; font-size: 12px;\">\n" +
                "                                © 2026 Fish Seller. All rights reserved.\n" +
                "                            </p>\n" +
                "                        </td>\n" +
                "                    </tr>\n" +
                "                </table>\n" +
                "            </td>\n" +
                "        </tr>\n" +
                "    </table>\n" +
                "</body>\n" +
                "</html>";
    }
}