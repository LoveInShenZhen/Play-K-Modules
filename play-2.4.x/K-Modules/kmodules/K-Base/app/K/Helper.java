package K;

/**
 * Created with IntelliJ IDEA.
 * User: kk
 * Date: 13-11-21
 * Time: 上午10:02
 * To change this template use File | Settings | File Templates.
 */

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.hash.Hashing;
import jodd.exception.ExceptionUtil;
import jodd.io.FileUtil;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import play.Logger;
import play.Play;
import play.libs.Json;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class Helper {

    public static Class LoadClass(String class_name) {
        try {
            return Helper.class.getClassLoader().loadClass(class_name);
        } catch (Exception ex) {
            throw new RuntimeException(String.format("Can not load class: %s", class_name));
        }
    }

    public static String Md5OfString(String s) {
        return Hashing.md5().hashString(s, Charset.forName("UTF-8")).toString();
    }

    public static String SHA1OfString(String s) {
        return Hashing.sha1().hashString(s, Charset.forName("UTF-8")).toString();
    }

    // 将年利率格式化为百分号的形式, 保留1位小数
    public static String IRRFormat(double irr) {
        DecimalFormat fmt = new DecimalFormat("0.0%");
        fmt.setRoundingMode(RoundingMode.HALF_UP);
        return fmt.format(irr);
    }

    public static String BorrowerRate(Double amount) {
        if (amount == null) return "0.00";
        DecimalFormat fmt = new DecimalFormat("#0.00");
        fmt.setRoundingMode(RoundingMode.DOWN);
        return fmt.format(amount);
    }

    // 格式化成百分比的形式, 保留2位小数, 100% 除外
    public static String PercentageFormat(double amount) {
        if (amount == 0.0) {
            return "0%";
        } else if (amount >= 1.0) {
            return "100%";
        }
        String style = "0.00%";
        DecimalFormat fmt = new DecimalFormat(style);
        fmt.setRoundingMode(RoundingMode.DOWN);
        return fmt.format(amount);
    }


    // 对 amount 四舍五入, 保留 count 位小数
    public static double Round(double amount, int count) {
        if (count == 0) {
            return Math.round(amount);
        }
        return Math.round(amount * Math.pow(10, count)) / Math.pow(10, count);
    }

    //  向正无穷方向舍入, 保留 count 位小数
    public static double RoundCeiling(double amount, int count) {
        if (count == 0) {
            return Math.ceil(amount);
        }
        return Math.ceil(amount * Math.pow(10, count)) / Math.pow(10, count);
    }

    // 转换金额到万元为单位, 保留1位小数, 带单位: 万
    public static String Div10Thousands(Double amount) {
        if (amount == null) return "";
        /*double tmp = amount / 10000.0;
        DecimalFormat fmt = new DecimalFormat("#.00 万");
        fmt.setRoundingMode(RoundingMode.HALF_UP);
        return fmt.format(tmp);*/
        double temp = amount / 10000.00;
        if (temp < 10) {
            DecimalFormat fmt = new DecimalFormat("#.00");
            fmt.setRoundingMode(RoundingMode.HALF_UP);
            return fmt.format(amount);
        } else {
            DecimalFormat fmt = new DecimalFormat("#.00万");
            fmt.setRoundingMode(RoundingMode.HALF_UP);
            return fmt.format(temp);
        }

    }

    // 格式化成逗号分隔的形式, 保留2位小数
    public static String CommaSeparated(Double amount) {
        if (amount == null) return "";
        DecimalFormat fmt = new DecimalFormat("#,##0.00");
        fmt.setRoundingMode(RoundingMode.HALF_UP);
        return fmt.format(amount);
    }

    public static Double ParseDouble(String val) {
        if (StringUtils.isBlank(val)) {
            return 0d;
        }
        String vmt = val.replaceAll(",", "");
        return Double.parseDouble(vmt);
    }

    public static String MoneyFmt(Double amount) {
        if (amount == null) return "0.00";
        DecimalFormat fmt = new DecimalFormat("#0.00");
        fmt.setRoundingMode(RoundingMode.DOWN);
        return fmt.format(amount);
    }

    public static Double MoneyValue(Double amount) {
        return Double.parseDouble(MoneyFmt(amount));
    }

    public static String Long2String(long amount, String format) {
        DecimalFormat fmt = new DecimalFormat(format);
        return fmt.format(amount);
    }

    public static boolean ParserBoolean(Object obj) {
        if (obj instanceof Boolean) {
            return (Boolean) obj;
        }
        if (obj instanceof Integer) {
            return (Integer) obj > 0;
        }
        if (obj instanceof String) {
            String v = (String) obj;
            return v.equalsIgnoreCase("true");
        }
        return false;
    }

    public static String Days2Months(int days) {
        int mons = days / 30;
        int leftDays = days % 30;/*
        DecimalFormat fmt = new DecimalFormat("0.0 月");
        fmt.setRoundingMode(RoundingMode.HALF_UP);
        return fmt.format(mons);*/
        StringBuilder sb = new StringBuilder();
        sb.append(mons);
        sb.append("个月");
        if (leftDays > 0) {
            sb.append(leftDays);
            sb.append("天");
        }
        return sb.toString();
    }

    // eg: 2 天 8 小时 44 分
    public static String DiffOfTwoDate(Date before, Date after) {
        if (before == null || after == null) {
            return "";
        }
        if (after.before(before)) {
            return "已经逾时";
        }

        long diff_in_ms = after.getTime() - before.getTime();
        long diffMinutes = diff_in_ms / (60 * 1000) % 60;
        long diffHours = diff_in_ms / (60 * 60 * 1000) % 24;
        long diffDays = diff_in_ms / (24 * 60 * 60 * 1000);

        String fmt;
        // diffDays==0
        if (diffDays > 0) {
            fmt = "%d 天 %d 小时 %d 分";
            return String.format(fmt, diffDays, diffHours, diffMinutes);
        } else if (diffHours > 0) {
            fmt = "%d 小时 %d 分";
            return String.format(fmt, diffHours, diffMinutes);
        } else if (diffMinutes > 0) {
            // diffDays==0 and diffHours==0
            fmt = "%d 分";
            return String.format(fmt, diffMinutes);
        } else {
            // diffDays==0 and diffHours==0 and diffMinutes == 0
            return "已经到期";
        }
    }

    public static Date ZeroMillisecond(Date date) {
        Calendar ca = Calendar.getInstance();
        ca.setTime(date);

        ca.set(Calendar.MILLISECOND, 0);
        return ca.getTime();
    }

    public static int DiffOfTwoDateInSeconds(Date date1, Date date2) {
        long time1_in_ms = date1.getTime();
        long time2_in_ms = date2.getTime();
        int diff_in_s = (int) (Math.abs(time1_in_ms - time2_in_ms) / 1000);
        return diff_in_s;
    }

    public static int DiffDays(Date before, Date after) {
        Calendar small = Calendar.getInstance();
        small.setTime(before);
        Calendar big = Calendar.getInstance();
        big.setTime(after);

        small.set(Calendar.HOUR_OF_DAY, 0);
        small.set(Calendar.MINUTE, 0);
        small.set(Calendar.SECOND, 0);
        small.set(Calendar.MILLISECOND, 0);

        big.set(Calendar.HOUR_OF_DAY, 0);
        big.set(Calendar.MINUTE, 0);
        big.set(Calendar.SECOND, 0);
        big.set(Calendar.MILLISECOND, 0);

        long diff_in_ms = big.getTimeInMillis() - small.getTimeInMillis();
        return (int) (diff_in_ms / 3600000 / 24);
    }

    public static int DiffDays(Calendar before, Calendar after) {
        Calendar a = (Calendar) before.clone();
        Calendar b = (Calendar) after.clone();
        a.set(Calendar.HOUR_OF_DAY, 0);
        a.set(Calendar.MINUTE, 0);
        a.set(Calendar.SECOND, 0);
        a.set(Calendar.MILLISECOND, 0);

        b.set(Calendar.HOUR_OF_DAY, 0);
        b.set(Calendar.MINUTE, 0);
        b.set(Calendar.SECOND, 0);
        b.set(Calendar.MILLISECOND, 0);

        long diff_in_ms = b.getTimeInMillis() - a.getTimeInMillis();
        return (int) (diff_in_ms / 3600000 / 24);
    }

    public static boolean After(Date before, Date after) {
        Calendar a = Calendar.getInstance();
        a.setTime(after);
        Calendar b = Calendar.getInstance();
        b.setTime(before);

        a.set(Calendar.HOUR_OF_DAY, 0);
        a.set(Calendar.MINUTE, 0);
        a.set(Calendar.SECOND, 0);
        a.set(Calendar.MILLISECOND, 0);

        b.set(Calendar.HOUR_OF_DAY, 0);
        b.set(Calendar.MINUTE, 0);
        b.set(Calendar.SECOND, 0);
        b.set(Calendar.MILLISECOND, 0);

        return a.after(b);
    }

    public static Date getCurrentDate(String fmt) {
        SimpleDateFormat df = new SimpleDateFormat(fmt);
        return Str2Date(df.format(new Date()));
    }

    public static Date Str2Date(String d) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            return df.parse(d);
        } catch (Exception e) {
//            e.printStackTrace();
            return null;
        }
    }

    public static Date Str2Date(String d, String fmt) {
        SimpleDateFormat df = new SimpleDateFormat(fmt);
        try {
            return df.parse(d);
        } catch (Exception e) {
//            e.printStackTrace();
            return null;
        }
    }

    public static final String HF_DATE_FMT = "yyyyMMdd";
    public static final String DB_DATE_FMT = "yyyy-MM-dd HH:mm:ss";

    public static String Date2Str(Date date, String fmt) {
        SimpleDateFormat df = new SimpleDateFormat(fmt);
        return df.format(date);
    }

    public static Date IncTimeInSec(Date date, int seconds) {
        Calendar ca = Calendar.getInstance();
        ca.setTime(date);
        ca.add(Calendar.SECOND, seconds);
        return ca.getTime();
    }

    public static Date IncTimeInDay(Date date, int days) {
        if (days == 0) {
            return date;
        }
        Calendar ca = Calendar.getInstance();
        ca.setTime(date);
        ca.add(Calendar.DAY_OF_YEAR, days);
        return ca.getTime();
    }

    public static Date DateAfter(Date date, int days) {
        return IncTimeInDay(date, days);
    }

    public static Date SetTime(Date date, int hour, int min, int sec) {
        Calendar a = Calendar.getInstance();
        a.setTime(date);

        a.set(Calendar.HOUR_OF_DAY, hour);
        a.set(Calendar.MINUTE, min);
        a.set(Calendar.SECOND, sec);
        a.set(Calendar.MILLISECOND, 0);

        return a.getTime();
    }


    public static boolean SameDay(Date date1, Date date2) {
        int diffDays = DiffDays(date1, date2);
        return diffDays == 0;
    }

    public static String HideString(String str) {
        if (StringUtils.isBlank(str)) {
            return "";
        }
        if (ValidEmail(str)) {
            return HideEmail(str);
        }
        if (ValidMobile(str)) {
            return HideMobile(str);
        }
        if (ValidIdNo(str)) {
            return HideIdNo(str);
        }

        if (str.length() >= 4) {
            return HideString(str, 2, str.length() - 2);
        } else {
            return "******";
        }
    }

    public static String HideString(String str, int from, int to) {
        if (StringUtils.isBlank(str)) {
            return "";
        }
        int length = str.length();
        if (from >= to || from > length - 1) {
            return str;
        }

        if (from < 0 || to < 0) {
            throw new RuntimeException(String.format("str=%s, from=%d, to=%d",
                    str, from, to));
        }

        String header = str.substring(0, from);
        String tail = str.substring(to);
        return String.format("%s****%s", header, tail);
    }

    public static String HideMobile(String str) {
        if (StringUtils.isBlank(str)) {
            return "";
        }
        return HideString(str, 3, 8);
    }

    public static String HideEmail(String email) {
        if (StringUtils.isBlank(email)) {
            return "";
        }
        int index = email.indexOf('@');
        int startIdx = index - 1 > 3 ? 3 : index - 1;
        return Helper.HideString(email, startIdx, index);
    }

    public static String HideIdNo(String idCard) {
        if (StringUtils.isBlank(idCard)) {
            return "";
        }
        String str1 = idCard.substring(0, 4);
        String str2 = idCard.substring(idCard.length() - 4, idCard.length());
        return str1 + StringUtils.repeat("*", 10) + str2;
    }

    public static String HideBusiCode(String busi_code) {
        if (StringUtils.isBlank(busi_code)) {
            return "";
        }
        String str1 = busi_code.substring(0, 4);
        String str2 = busi_code.substring(busi_code.length() - 4, busi_code.length());
        return str1 + StringUtils.repeat("*", 7) + str2;
    }

    public static String HideRealName(String real_name) {
        if (StringUtils.isBlank(real_name)) {
            return "";
        }
        return String.format("%s**", real_name.charAt(0));
    }

    public static boolean ValidEmail(String email_str) {
        if (StringUtils.isBlank(email_str)) {
            return false;
        }
        return email_str.matches("^[_a-zA-Z0-9-]+(\\.[_a-zA-Z0-9-]+)*@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*(\\.[a-zA-Z]{2,3})$");
    }


    public static boolean ValidMobile(String mobile_str) {
        if (StringUtils.isBlank(mobile_str)) {
            return false;
        }
        return mobile_str.matches("^[1][0-9]{10}$");
    }


    public static boolean ValidUserName(String user_name) {
        if (StringUtils.isBlank(user_name)) {
            return false;
        }
        return user_name.matches("^[a-zA-Z][a-zA-Z0-9_-]{4,24}$");
    }

    // 判断是否是合法的身份证号码
    public static boolean ValidIdNo(String idNo) {
        IdcardValidator iv = new IdcardValidator();
        String idno = idNo.trim();
        if (idno.length() != 15 && idno.length() != 18) {
            return false;
        }
        return iv.isValidatedAllIdcard(idno);
    }

    public static String StackTraceOfEx(Throwable e) {
        return ExceptionUtil.exceptionChainToString(e);
    }

    public static String ToJsonString(Object obj) {
        return Json.toJson(obj).toString();
    }

    public static String ToJsonStringPretty(Object obj) {
        JsonNode jsonNode = Json.toJson(obj);
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);
        } catch (JsonProcessingException e) {
            return e.getMessage();
        }
    }

    public static String FormatJson(String jsonStr) {
        JsonNode jsonNode = Json.parse(jsonStr);
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);
        } catch (JsonProcessingException e) {
            return e.getMessage();
        }
    }

    public static <A> A FromJsonString(String JsonStr, Class<A> clazz) {
        try {
            JsonNode node = Json.parse(JsonStr);
            return Json.fromJson(node, clazz);
        } catch (Exception ex) {
            Logger.debug(Helper.StackTraceOfEx(ex));
            return null;
        }
    }

    public static void JsonToMap(JsonNode json_data, Map<String, String> map) {
        Iterator<String> itFieldName = json_data.fieldNames();
        while (itFieldName.hasNext()) {
            String name = itFieldName.next();
            map.put(name, json_data.get(name).textValue());
        }
    }


    public static void JsonStrToMap(String jsonStr, Map<String, String> map) {
        JsonNode node = Json.parse(jsonStr);
        Iterator<String> itFieldName = node.fieldNames();
        while (itFieldName.hasNext()) {
            String name = itFieldName.next();
            map.put(name, node.get(name).textValue());
        }
    }

    public static Map<String, Object> JsonStrToMap(String jsonStr) {
        JsonNode node = Json.parse(jsonStr);
        Iterator<String> itFieldName = node.fieldNames();
        Map<String, Object> map = new HashMap<String, Object>();
        while (itFieldName.hasNext()) {
            String name = itFieldName.next();
            JsonNode n = node.get(name);
            if (n.isTextual()) {
                map.put(name, n.textValue());
            } else if (n.isInt()) {
                map.put(name, n.intValue());
            } else if (n.isLong()) {
                map.put(name, n.longValue());
            } else if (n.isDouble()) {
                map.put(name, n.doubleValue());
            } else if (n.isBoolean()) {
                map.put(name, n.booleanValue());
            }
        }

        return map;
    }

    public static String InputStreamToString(InputStream ins, int length, Charset charset) throws IOException {
        byte[] bcontents = new byte[length];
        ins.read(bcontents);
        return new String(bcontents, charset);
    }

    public static String ToBase64String(Object obj) {
        try {
            String json_str = ToJsonString(obj);
            return Base64.encodeBase64URLSafeString(json_str.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            Logger.error(ex.getMessage());
            return null;
        }
    }

    public static <A> A FromBase64String(String base64_str, Class<A> clazz) {
        try {
            byte[] bindata = Base64.decodeBase64(base64_str);
            String json_str = new String(bindata, "UTF-8");
            return FromJsonString(json_str, clazz);
        } catch (UnsupportedEncodingException ex) {
            Logger.error(ex.getMessage());
            return null;
        }
    }

    public static String Base64Decode(String base64Str) {
        try {
            byte[] bindata = Base64.decodeBase64(base64Str);
            return new String(bindata, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            Logger.error(ex.getMessage());
            return null;
        }
    }

    public static String Base64Encode(String str) {
        try {
            return Base64.encodeBase64URLSafeString(str.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            Logger.error(ex.getMessage());
            return null;
        }
    }

    public static String getCapAmount(double amount) {
        int[] numbers = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        String[] numbersCap = {"零", "壹", "贰", "叁", "肆", "伍", "陆",
                "柒", "捌", "玖"};
        String[] units = {"百亿", "十亿", "亿", "仟万", "佰万", "拾万", "万", "千", "佰",
                "拾", "圆"};
        String[] jf = {"角", "分"};
        BigDecimal bd = new BigDecimal(amount);
        amount = bd.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        DecimalFormat format = new DecimalFormat("#.##");
        String amountStr = format.format(amount);
        // String amountStr = String.valueOf(amount);
        String[] spValues = amountStr.split("\\.");
        String first = spValues[0];
        int length = first.length();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            String c = first.substring(i, i + 1);
            int n = Integer.valueOf(c);
            String unit = units[units.length - (length - i)];
            String numCap = null;
            for (int j = 0; j < 10; j++) {
                int number = numbers[j];
                if (n == number) {
                    numCap = numbersCap[j];
                    break;
                }
            }
            if (unit.length() > 1) {
                unit = unit.substring(0, 1);
            }
            if (numCap.equals("零")) {
                //if (!sb.substring(sb.length() - 1).equals(units[6])&&!sb.substring(sb.length() - 1).equals(units[2])) {
                if (unit.equals(units[6]) || unit.equals(units[2])) {
                    if (sb.substring(sb.length() - 1).equals("零")) {
                        sb.deleteCharAt(sb.length() - 1);
                    }
                    sb.append(unit);
                } else if (sb.length() > 0
                        && !sb.substring(sb.length() - 1).equals("零")) {
                    // 个位是零直接加上单位圆
                    if (i == length - 1) {
                        sb.append(unit);
                    } else {
                        sb.append(numCap);
                    }
                }
                //}
            } else {
                sb.append(numCap);
                sb.append(unit);
            }
        }
        if (sb.length() > 0 && sb.substring(sb.length() - 1).equals(numbersCap[0])) {
            sb.deleteCharAt(sb.length() - 1);
            sb.append(units[10]);
        }
        if (spValues.length == 2) {
            String second = spValues[1];
            if (!second.equals("00")) {
                for (int i = 0; i < second.length(); i++) {
                    String s = second.substring(i, i + 1);
                    int c = Integer.valueOf(s);
                    if (c > 0) {
                        String numCap = null;
                        for (int j = 0; j < 10; j++) {
                            int number = numbers[j];
                            if (c == number) {
                                numCap = numbersCap[j];
                                break;
                            }
                        }
                        sb.append(numCap);
                        sb.append(jf[i]);
                    }
                }
            }
        }
        return sb.toString();
    }


    public static String randomMobile() {
        String[] nums = new String[]{"139", "138", "137", "136", "135", "134", "150",
                "158", "130", "131", "132",
                "185", "186", "133", "153", "180", "181", "189"};
        return String.format("%s%s",
                nums[RandomUtils.nextInt(0, nums.length)],
                RandomStringUtils.randomNumeric(8));
    }

    public static String randomMobileFromExists() {
        try {
            String[] lines = FileUtil.readLines(Play.application().getFile("/conf/resource/random.mobiles"));
            return lines[RandomUtils.nextInt(0, lines.length)];
        } catch (IOException e) {
            Logger.error(ExceptionUtil.exceptionToString(e));
            return randomMobile();
        }
    }

    public static double Add(double... argArry) {
        BigDecimal result = BigDecimal.ZERO;
        for (double v : argArry) {
            BigDecimal bv = BigDecimal.valueOf(v);
            result = result.add(bv);
        }
        return result.doubleValue();
    }

    public static double Add(int scale, double... argArry) {
        BigDecimal result = BigDecimal.ZERO;
        for (double v : argArry) {
            BigDecimal bv = BigDecimal.valueOf(v);
            result = result.add(bv);
        }
        return result.setScale(BigDecimal.ROUND_UP, scale).doubleValue();
    }

    public static double Sub(double big, double... argArry) {
        BigDecimal result = BigDecimal.valueOf(big);
        for (double v : argArry) {
            BigDecimal bv = BigDecimal.valueOf(v);
            result = result.subtract(bv);
        }
        return result.doubleValue();
    }

    public static double Sub(int scale, double big, double... argArry) {
        BigDecimal result = BigDecimal.valueOf(big);
        for (double v : argArry) {
            BigDecimal bv = BigDecimal.valueOf(v);
            result = result.subtract(bv);
        }
        return result.setScale(BigDecimal.ROUND_UP, scale).doubleValue();
    }

    public static String ToUnderscoreNaming(String value) {
        StringBuilder sb = new StringBuilder(value.length());
        String unCap = StringUtils.uncapitalize(value);
        for (char c : unCap.toCharArray()) {
            if (Character.isUpperCase(c)) {
                sb.append("_").append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static String XmlDocumentToString(Document doc) throws TransformerException, IOException {
        DOMSource source = new DOMSource(doc);
        StringWriter writer = new StringWriter();
        String xml;
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml"); // xml, html,
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            transformer.transform(source, new StreamResult(writer));
            xml = writer.toString();
        } finally {
            writer.close();
        }

        return xml;
    }

    @SafeVarargs
    public static <ClassT> boolean In(ClassT test_value, ClassT... allowed_values) {
        for (ClassT val : allowed_values) {
            if (test_value.equals(val)) {
                return true;
            }
        }
        return false;
    }

    @SafeVarargs
    public static <ClassT> boolean NotIn(ClassT test_value, ClassT... allowed_values) {
        return !In(test_value, allowed_values);
    }

    public static String EscapeMarkdown(String str) {
        if (StringUtils.isBlank(str)) {
            return "";
        }
        String md = StringUtils.replace(str, "_", "\\_");
        md = StringUtils.replace(md, "*", "\\*");
        return md;
    }

}
