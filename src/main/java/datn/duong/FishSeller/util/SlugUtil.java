package datn.duong.FishSeller.util;

import java.text.Normalizer;
import java.util.regex.Pattern;

public class SlugUtil {

    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

    public static String makeSlug(String input) {
        if (input == null)
            throw new IllegalArgumentException();

        // 1. Chuyển sang chữ thường
        String nowhitespace = WHITESPACE.matcher(input).replaceAll("-");
        
        // 2. Chuẩn hóa tiếng Việt (bỏ dấu)
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        
        // 3. Loại bỏ các ký tự đặc biệt, chỉ giữ lại chữ cái, số và dấu gạch ngang
        String slug = NONLATIN.matcher(normalized).replaceAll("");
        
        return slug.toLowerCase(java.util.Locale.ENGLISH);
    }
}