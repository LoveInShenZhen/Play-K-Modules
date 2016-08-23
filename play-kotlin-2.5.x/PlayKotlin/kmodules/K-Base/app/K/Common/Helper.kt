package K.Common

/**
 * Created with IntelliJ IDEA.
 * User: kk
 * Date: 13-11-21
 * Time: 上午10:02
 * To change this template use File | Settings | File Templates.
 */

import K.Aop.annotations.Comment
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.hash.Hashing
import com.lowagie.text.pdf.BaseFont
import jodd.datetime.JDateTime
import jodd.exception.ExceptionUtil
import jodd.io.FileUtil
import jodd.util.ReflectUtil
import jxl.Workbook
import jxl.write.*
import jxl.write.Number
import org.apache.commons.codec.binary.Base64
import org.apache.commons.lang3.CharUtils
import org.apache.commons.lang3.RandomStringUtils
import org.apache.commons.lang3.RandomUtils
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.reflect.FieldUtils
import org.apache.commons.lang3.tuple.Pair
import org.w3c.dom.Document
import org.xhtmlrenderer.pdf.ITextRenderer
import play.Logger
import play.libs.Json
import scala.Console
import java.io.*
import java.lang.reflect.Field
import java.math.BigDecimal
import java.math.RoundingMode
import java.nio.charset.Charset
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.stream.Collectors
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerException
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

object Helper {

    fun DLog(message: String, vararg args: Any) {
        val msg = String.format("%s%s%s", scala.Console.YELLOW(), message, Console.RESET())
        Logger.debug(msg, *args)
    }

    fun LoadClass(class_name: String): Class<*>? {
        try {
            return Hub.application().classloader().loadClass(class_name)
        } catch (ex: Exception) {
            throw BizLogicException("Can not load class: %s", class_name)
        }

    }

    fun getFile(path: String): File {
        return Hub.application().getFile(path)
    }

    fun Md5OfString(s: String): String {
        return Hashing.md5().hashString(s, Charset.forName("UTF-8")).toString()
    }

    fun SHA1OfString(s: String): String {
        return Hashing.sha1().hashString(s, Charset.forName("UTF-8")).toString()
    }

    // 将年利率格式化为百分号的形式, 保留1位小数
    fun IRRFormat(irr: Double): String {
        val fmt = DecimalFormat("0.0%")
        fmt.roundingMode = RoundingMode.HALF_UP
        return fmt.format(irr)
    }

    fun BorrowerRate(amount: Double?): String {
        if (amount == null) return "0.00"
        val fmt = DecimalFormat("#0.00")
        fmt.roundingMode = RoundingMode.DOWN
        return fmt.format(amount)
    }

    // 格式化成百分比的形式, 保留2位小数, 100% 除外
    fun PercentageFormat(amount: Double): String {
        if (amount == 0.0) {
            return "0%"
        } else if (amount >= 1.0) {
            return "100%"
        }
        val style = "0.00%"
        val fmt = DecimalFormat(style)
        fmt.roundingMode = RoundingMode.DOWN
        return fmt.format(amount)
    }


    // 对 amount 四舍五入, 保留 count 位小数
    fun Round(amount: Double, count: Int): Double {
        if (count == 0) {
            return Math.round(amount).toDouble()
        }
        return Math.round(amount * Math.pow(10.0, count.toDouble())) / Math.pow(10.0, count.toDouble())
    }

    //  向正无穷方向舍入, 保留 count 位小数
    fun RoundCeiling(amount: Double, count: Int): Double {
        if (count == 0) {
            return Math.ceil(amount)
        }
        return Math.ceil(amount * Math.pow(10.0, count.toDouble())) / Math.pow(10.0, count.toDouble())
    }

    // 转换金额到万元为单位, 保留1位小数, 带单位: 万
    fun Div10Thousands(amount: Double?): String {
        if (amount == null) return ""
        /*double tmp = amount / 10000.0;
        DecimalFormat fmt = new DecimalFormat("#.00 万");
        fmt.setRoundingMode(RoundingMode.HALF_UP);
        return fmt.format(tmp);*/
        val temp = amount / 10000.00
        if (temp < 10) {
            val fmt = DecimalFormat("#.00")
            fmt.roundingMode = RoundingMode.HALF_UP
            return fmt.format(amount)
        } else {
            val fmt = DecimalFormat("#.00万")
            fmt.roundingMode = RoundingMode.HALF_UP
            return fmt.format(temp)
        }

    }

    // 格式化成逗号分隔的形式, 保留2位小数
    fun CommaSeparated(amount: Double?): String {
        if (amount == null) return ""
        val fmt = DecimalFormat("#,##0.00")
        fmt.roundingMode = RoundingMode.HALF_UP
        return fmt.format(amount)
    }

    fun ParseDouble(`val`: String): Double {
        if (StringUtils.isBlank(`val`)) {
            return 0.0
        }
        val vmt = `val`.replace(",".toRegex(), "")
        return java.lang.Double.parseDouble(vmt)
    }

    fun MoneyFmt(amount: Double?): String {
        if (amount == null) return "0.00"
        val fmt = DecimalFormat("#0.00")
        fmt.roundingMode = RoundingMode.DOWN
        return fmt.format(amount)
    }

    fun MoneyColorFmt(amount: Double?): String {
        if (amount == null) return "0.00"
        val fmt = DecimalFormat("#0.00")
        fmt.roundingMode = RoundingMode.DOWN
        // todo
        return fmt.format(amount)
    }

    fun MoneyValue(amount: Double?): Double {
        return java.lang.Double.parseDouble(MoneyFmt(amount))
    }

    fun Long2String(amount: Long, format: String): String {
        val fmt = DecimalFormat(format)
        return fmt.format(amount)
    }

    fun Days2Months(days: Int): String {
        val mons = days / 30
        val leftDays = days % 30/*
        DecimalFormat fmt = new DecimalFormat("0.0 月");
        fmt.setRoundingMode(RoundingMode.HALF_UP);
        return fmt.format(mons);*/
        val sb = StringBuilder()
        sb.append(mons)
        sb.append("个月")
        if (leftDays > 0) {
            sb.append(leftDays)
            sb.append("天")
        }
        return sb.toString()
    }

    // eg: 2 天 8 小时 44 分
    fun DiffOfTwoDate(before: Date?, after: Date?): String {
        if (before == null || after == null) {
            return ""
        }
        if (after.before(before)) {
            return "已经逾时"
        }

        val diff_in_ms = after.time - before.time
        val diffMinutes = diff_in_ms / (60 * 1000) % 60
        val diffHours = diff_in_ms / (60 * 60 * 1000) % 24
        val diffDays = diff_in_ms / (24 * 60 * 60 * 1000)

        val fmt: String
        // diffDays==0
        if (diffDays > 0) {
            fmt = "%d 天 %d 小时 %d 分"
            return String.format(fmt, diffDays, diffHours, diffMinutes)
        } else if (diffHours > 0) {
            fmt = "%d 小时 %d 分"
            return String.format(fmt, diffHours, diffMinutes)
        } else if (diffMinutes > 0) {
            // diffDays==0 and diffHours==0
            fmt = "%d 分"
            return String.format(fmt, diffMinutes)
        } else {
            // diffDays==0 and diffHours==0 and diffMinutes == 0
            return "已经到期"
        }
    }

    fun ZeroMillisecond(date: Date): Date {
        val ca = Calendar.getInstance()
        ca.time = date

        ca.set(Calendar.MILLISECOND, 0)
        return ca.time
    }

    fun DiffOfTwoDateInSeconds(date1: Date, date2: Date): Int {
        val time1_in_ms = date1.time
        val time2_in_ms = date2.time
        val diff_in_s = (Math.abs(time1_in_ms - time2_in_ms) / 1000).toInt()
        return diff_in_s
    }

    fun DiffDays(before: Date, after: Date): Int {
        val small = Calendar.getInstance()
        small.time = before
        val big = Calendar.getInstance()
        big.time = after

        small.set(Calendar.HOUR_OF_DAY, 0)
        small.set(Calendar.MINUTE, 0)
        small.set(Calendar.SECOND, 0)
        small.set(Calendar.MILLISECOND, 0)

        big.set(Calendar.HOUR_OF_DAY, 0)
        big.set(Calendar.MINUTE, 0)
        big.set(Calendar.SECOND, 0)
        big.set(Calendar.MILLISECOND, 0)

        val diff_in_ms = big.timeInMillis - small.timeInMillis
        return (diff_in_ms / 3600000 / 24).toInt()
    }

    fun DiffDays(before: Calendar, after: Calendar): Int {
        val a = before.clone() as Calendar
        val b = after.clone() as Calendar
        a.set(Calendar.HOUR_OF_DAY, 0)
        a.set(Calendar.MINUTE, 0)
        a.set(Calendar.SECOND, 0)
        a.set(Calendar.MILLISECOND, 0)

        b.set(Calendar.HOUR_OF_DAY, 0)
        b.set(Calendar.MINUTE, 0)
        b.set(Calendar.SECOND, 0)
        b.set(Calendar.MILLISECOND, 0)

        val diff_in_ms = b.timeInMillis - a.timeInMillis
        return (diff_in_ms / 3600000 / 24).toInt()
    }

    fun ToGMT(date: Date): String {
        val df = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss", Locale.US)
        df.timeZone = TimeZone.getTimeZone("GMT")

        return df.format(date) + " GMT"
    }

    fun FromGMT(time_str: String): Date {
        val df = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss", Locale.US)
        df.timeZone = TimeZone.getTimeZone("GMT")

        try {
            return df.parse(time_str.replace("GMT", "").trim { it <= ' ' })
        } catch (ex: Exception) {
            throw BizLogicException("不合理的 GMT String: %s", time_str)
        }

    }


    fun Str2Date(d: String): Date? {
        val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        try {
            return df.parse(d)
        } catch (e: Exception) {
            //            e.printStackTrace();
            return null
        }

    }

    fun Str2Date(d: String, fmt: String): Date? {
        val df = SimpleDateFormat(fmt)
        try {
            return df.parse(d)
        } catch (e: Exception) {
            //            e.printStackTrace();
            return null
        }

    }

    val HF_DATE_FMT = "yyyyMMdd"
    val DB_DATE_FMT = "yyyy-MM-dd HH:mm:ss"

    fun Date2Str(date: Date, fmt: String): String {
        val df = SimpleDateFormat(fmt)
        return df.format(date)
    }

    fun IncTimeInSec(date: Date, seconds: Int): Date {
        val ca = Calendar.getInstance()
        ca.time = date
        ca.add(Calendar.SECOND, seconds)
        return ca.time
    }

    fun IncTimeInDay(date: Date, days: Int): Date {
        if (days == 0) {
            return date
        }
        val ca = Calendar.getInstance()
        ca.time = date
        ca.add(Calendar.DAY_OF_YEAR, days)
        return ca.time
    }

    fun DateAfter(date: Date, days: Int): Date {
        return IncTimeInDay(date, days)
    }

    fun SetTime(date: Date, hour: Int, min: Int, sec: Int): Date {
        val a = Calendar.getInstance()
        a.time = date

        a.set(Calendar.HOUR_OF_DAY, hour)
        a.set(Calendar.MINUTE, min)
        a.set(Calendar.SECOND, sec)
        a.set(Calendar.MILLISECOND, 0)

        return a.time
    }


    fun SameDay(date1: Date, date2: Date): Boolean {
        val diffDays = DiffDays(date1, date2)
        return diffDays == 0
    }

    fun HideString(str: String): String {
        if (StringUtils.isBlank(str)) {
            return ""
        }
        if (ValidEmail(str)) {
            return HideEmail(str)
        }
        if (ValidMobile(str)) {
            return HideMobile(str)
        }
        if (ValidIdNo(str)) {
            return HideIdNo(str)
        }

        if (str.length >= 4) {
            return HideString(str, 2, str.length - 2)
        } else {
            return "******"
        }
    }

    fun HideString(str: String, from: Int, to: Int): String {
        if (StringUtils.isBlank(str)) {
            return ""
        }
        val length = str.length
        if (from >= to || from > length - 1) {
            return str
        }

        if (from < 0 || to < 0) {
            throw RuntimeException(String.format("str=%s, from=%d, to=%d",
                    str, from, to))
        }

        val header = str.substring(0, from)
        val tail = str.substring(to)
        return String.format("%s****%s", header, tail)
    }

    fun HideMobile(str: String): String {
        if (StringUtils.isBlank(str)) {
            return ""
        }
        return HideString(str, 3, 8)
    }

    fun HideEmail(email: String): String {
        if (StringUtils.isBlank(email)) {
            return ""
        }
        val index = email.indexOf('@')
        val startIdx = if (index - 1 > 3) 3 else index - 1
        return Helper.HideString(email, startIdx, index)
    }

    fun HideIdNo(idCard: String): String {
        if (StringUtils.isBlank(idCard)) {
            return ""
        }
        val str1 = idCard.substring(0, 4)
        val str2 = idCard.substring(idCard.length - 4, idCard.length)
        return str1 + StringUtils.repeat("*", 10) + str2
    }

    fun HideBusiCode(busi_code: String): String {
        if (StringUtils.isBlank(busi_code)) {
            return ""
        }
        val str1 = busi_code.substring(0, 4)
        val str2 = busi_code.substring(busi_code.length - 4, busi_code.length)
        return str1 + StringUtils.repeat("*", 7) + str2
    }

    fun HideRealName(real_name: String): String {
        if (StringUtils.isBlank(real_name)) {
            return ""
        }
        return String.format("%s**", real_name[0])
    }

    fun ValidEmail(email_str: String): Boolean {
        if (StringUtils.isBlank(email_str)) {
            return false
        }
        return email_str.matches("^[_a-zA-Z0-9-]+(\\.[_a-zA-Z0-9-]+)*@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*(\\.[a-zA-Z]{2,3})$".toRegex())
    }


    fun ValidMobile(mobile_str: String): Boolean {
        if (StringUtils.isBlank(mobile_str)) {
            return false
        }
        return mobile_str.matches("^[1][0-9]{10}$".toRegex())
    }


    fun ValidUserName(user_name: String): Boolean {
        if (StringUtils.isBlank(user_name)) {
            return false
        }
        return user_name.matches("^[a-zA-Z][a-zA-Z0-9_-]{4,24}$".toRegex())
    }

    // 判断是否是合法的身份证号码
    fun ValidIdNo(idNo: String): Boolean {
        val iv = IdcardValidator()
        val idno = idNo.trim { it <= ' ' }
        if (idno.length != 15 && idno.length != 18) {
            return false
        }
        return iv.isValidatedAllIdcard(idno)
    }

    fun StackTraceOfEx(e: Throwable): String {
        return ExceptionUtil.exceptionChainToString(e)
    }

    fun ToJsonString(obj: Any): String {
        return Json.toJson(obj).toString()
    }

    fun ToJsonStringPretty(obj: Any?): String? {
        if (obj == null) {
            return null
        }
        val jsonNode = Json.toJson(obj)
        val mapper = ObjectMapper()
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode)
        } catch (e: JsonProcessingException) {
            return e.message
        }

    }

    fun FormatJson(jsonStr: String): String {
        val jsonNode = Json.parse(jsonStr)
        val mapper = ObjectMapper()

        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode)
    }

    fun <A> FromJsonString(JsonStr: String, clazz: Class<A>): A? {
        try {
            val node = Json.parse(JsonStr)
            return Json.fromJson(node, clazz)
        } catch (ex: Exception) {
            Logger.debug(Helper.StackTraceOfEx(ex))
            return null
        }

    }

    fun JsonToMap(json_data: JsonNode, map: MutableMap<String, String>) {
        val itFieldName = json_data.fieldNames()
        while (itFieldName.hasNext()) {
            val name = itFieldName.next()
            map.put(name, json_data.get(name).textValue())
        }
    }


    fun JsonStrToMap(jsonStr: String, map: MutableMap<String, String>) {
        val node = Json.parse(jsonStr)
        val itFieldName = node.fieldNames()
        while (itFieldName.hasNext()) {
            val name = itFieldName.next()
            map.put(name, node.get(name).textValue())
        }
    }

    fun JsonStrToMap(jsonStr: String): Map<String, Any> {
        val node = Json.parse(jsonStr)
        val itFieldName = node.fieldNames()
        val map = HashMap<String, Any>()
        while (itFieldName.hasNext()) {
            val name = itFieldName.next()
            val n = node.get(name)
            if (n.isTextual) {
                map.put(name, n.textValue())
            } else if (n.isInt) {
                map.put(name, n.intValue())
            } else if (n.isLong) {
                map.put(name, n.longValue())
            } else if (n.isDouble) {
                map.put(name, n.doubleValue())
            } else if (n.isBoolean) {
                map.put(name, n.booleanValue())
            }
        }

        return map
    }

    @Throws(IOException::class)
    fun InputStreamToString(ins: InputStream, length: Int, charset: Charset): String {
        val bcontents = ByteArray(length)
        ins.read(bcontents)
        return String(bcontents, charset)
    }

    fun ToBase64String(obj: Any): String? {
        try {
            val json_str = ToJsonString(obj)
            return Base64.encodeBase64URLSafeString(json_str.toByteArray(charset("UTF-8")))
        } catch (ex: UnsupportedEncodingException) {
            Logger.error(ex.message)
            return null
        }

    }

    fun <A> FromBase64String(base64_str: String, clazz: Class<A>): A? {
        try {
            val bindata = Base64.decodeBase64(base64_str)
            val json_str = bindata.toString(Charsets.UTF_8)
            return FromJsonString(json_str, clazz)
        } catch (ex: UnsupportedEncodingException) {
            Logger.error(ex.message)
            return null
        }

    }

    fun Base64Decode(base64Str: String): String? {
        try {
            val bindata = Base64.decodeBase64(base64Str)
            return bindata.toString(Charsets.UTF_8)
        } catch (ex: UnsupportedEncodingException) {
            Logger.error(ex.message)
            return null
        }

    }

    fun Base64Encode(str: String): String? {
        try {
            return Base64.encodeBase64URLSafeString(str.toByteArray(charset("UTF-8")))
        } catch (ex: UnsupportedEncodingException) {
            Logger.error(ex.message)
            return null
        }

    }

    fun getCapAmount(amount: Double): String {
        var amount = amount
        val numbers = intArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)
        val numbersCap = arrayOf("零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖")
        val units = arrayOf("百亿", "十亿", "亿", "仟万", "佰万", "拾万", "万", "千", "佰", "拾", "圆")
        val jf = arrayOf("角", "分")
        val bd = BigDecimal(amount)
        amount = bd.setScale(2, BigDecimal.ROUND_HALF_UP).toDouble()
        val format = DecimalFormat("#.##")
        val amountStr = format.format(amount)
        // String amountStr = String.valueOf(amount);
        val spValues = amountStr.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val first = spValues[0]
        val length = first.length
        val sb = StringBuilder()
        for (i in 0..length - 1) {
            val c = first.substring(i, i + 1)
            val n = Integer.valueOf(c)!!
            var unit = units[units.size - (length - i)]
            var numCap: String? = null
            for (j in 0..9) {
                val number = numbers[j]
                if (n == number) {
                    numCap = numbersCap[j]
                    break
                }
            }
            if (unit.length > 1) {
                unit = unit.substring(0, 1)
            }
            if (numCap == "零") {
                //if (!sb.substring(sb.length() - 1).equals(units[6])&&!sb.substring(sb.length() - 1).equals(units[2])) {
                if (unit == units[6] || unit == units[2]) {
                    if (sb.substring(sb.length - 1) == "零") {
                        sb.deleteCharAt(sb.length - 1)
                    }
                    sb.append(unit)
                } else if (sb.length > 0 && sb.substring(sb.length - 1) != "零") {
                    // 个位是零直接加上单位圆
                    if (i == length - 1) {
                        sb.append(unit)
                    } else {
                        sb.append(numCap)
                    }
                }
                //}
            } else {
                sb.append(numCap)
                sb.append(unit)
            }
        }
        if (sb.length > 0 && sb.substring(sb.length - 1) == numbersCap[0]) {
            sb.deleteCharAt(sb.length - 1)
            sb.append(units[10])
        }
        if (spValues.size == 2) {
            val second = spValues[1]
            if (second != "00") {
                for (i in 0..second.length - 1) {
                    val s = second.substring(i, i + 1)
                    val c = Integer.valueOf(s)!!
                    if (c > 0) {
                        var numCap: String? = null
                        for (j in 0..9) {
                            val number = numbers[j]
                            if (c == number) {
                                numCap = numbersCap[j]
                                break
                            }
                        }
                        sb.append(numCap)
                        sb.append(jf[i])
                    }
                }
            }
        }
        return sb.toString()
    }


    fun randomMobile(): String {
        val nums = arrayOf("139", "138", "137", "136", "135", "134", "150", "158", "130", "131", "132", "185", "186", "133", "153", "180", "181", "189")
        return String.format("%s%s",
                nums[RandomUtils.nextInt(0, nums.size)],
                RandomStringUtils.randomNumeric(8))
    }

    fun randomMobileFromExists(): String {
        try {
            val lines = FileUtil.readLines(getFile("/conf/resource/random.mobiles"))
            return lines[RandomUtils.nextInt(0, lines.size)]
        } catch (e: IOException) {
            Logger.error(ExceptionUtil.exceptionChainToString(e))
            return randomMobile()
        }

    }

    fun Add(vararg argArry: Double): Double {
        var result = BigDecimal.ZERO
        for (v in argArry) {
            val bv = BigDecimal.valueOf(v)
            result = result.add(bv)
        }
        return result.toDouble()
    }

    fun Add(scale: Int, vararg argArry: Double): Double {
        var result = BigDecimal.ZERO
        for (v in argArry) {
            val bv = BigDecimal.valueOf(v)
            result = result.add(bv)
        }
        return result.setScale(BigDecimal.ROUND_UP, scale).toDouble()
    }

    fun Sub(big: Double, vararg argArry: Double): Double {
        var result = BigDecimal.valueOf(big)
        for (v in argArry) {
            val bv = BigDecimal.valueOf(v)
            result = result.subtract(bv)
        }
        return result.toDouble()
    }

    fun Sub(scale: Int, big: Double, vararg argArry: Double): Double {
        var result = BigDecimal.valueOf(big)
        for (v in argArry) {
            val bv = BigDecimal.valueOf(v)
            result = result.subtract(bv)
        }
        return result.setScale(BigDecimal.ROUND_UP, scale).toDouble()
    }

    fun ToUnderscoreNaming(value: String): String {
        val sb = StringBuilder(value.length)
        val unCap = StringUtils.uncapitalize(value)
        for (c in unCap.toCharArray()) {
            if (Character.isUpperCase(c)) {
                sb.append("_").append(Character.toLowerCase(c))
            } else {
                sb.append(c)
            }
        }
        return sb.toString()
    }

    @Throws(TransformerException::class, IOException::class)
    fun XmlDocumentToString(doc: Document): String {
        val source = DOMSource(doc)
        val writer = StringWriter()
        val xml: String
        try {
            val transformer = TransformerFactory.newInstance().newTransformer()
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8")
            transformer.setOutputProperty(OutputKeys.INDENT, "yes")
            transformer.setOutputProperty(OutputKeys.METHOD, "xml") // xml, html,
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4")
            transformer.transform(source, StreamResult(writer))
            xml = writer.toString()
        } finally {
            writer.close()
        }

        return xml
    }

    @SafeVarargs
    fun <ClassT> In(test_value: ClassT, vararg allowed_values: ClassT): Boolean {
        for (`val` in allowed_values) {
            if (test_value == `val`) {
                return true
            }
        }
        return false
    }

    @SafeVarargs
    fun <ClassT> NotIn(test_value: ClassT, vararg allowed_values: ClassT): Boolean {
        return !In(test_value, *allowed_values)
    }

    fun EscapeMarkdown(str: String): String {
        if (StringUtils.isBlank(str)) {
            return ""
        }
        var md = StringUtils.replace(str, "_", "\\_")
        md = StringUtils.replace(md, "*", "\\*")
        return md
    }

    fun HtmlToPdf(html_content: String, output_file_path: String): Boolean {
        try {
            val os = FileOutputStream(output_file_path)
            val renderer = ITextRenderer()
            renderer.setDocumentFromString(html_content)

            // 解决中文支持问题
            val fontResolver = renderer.fontResolver
            val font_path = Hub.application().getFile("/conf/resource/simsun.ttc").absolutePath
            fontResolver.addFont(font_path, BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED)
            //解决图片的相对路径问题
            val imgdir = Hub.application().getFile("/conf/resource").toURI().toString()
            renderer.sharedContext.baseURL = imgdir
            renderer.layout()
            renderer.createPDF(os)

            os.flush()
            os.close()
            return true
        } catch (ex: Exception) {
            Logger.error(ex.message)
            return false
        }

    }

//    @Throws(IOException::class, WriteException::class, IllegalAccessException::class)
//    fun <T> writeExcel(out: OutputStream, records: List<T>) {
////        val clazz = records[0].
////        val columns = Arrays.stream(Class<T>.declaredFields).filter { field ->
////            val comment = field.getAnnotation(Comment::class.java)
////            comment != null
////        }.map<Pair<Field, String>>({ field ->
////            val comment = field.getAnnotation(Comment::class.java)
////            Pair.of<Field, String>(field, comment.value)
////        }).collect<List<Pair<Field, String>>, Any>(Collectors.toList<Pair<Field, String>>())
//
//
//        val columns = Class<T>.declaredFields.filter { field ->
//            val comment = field.getAnnotation(Comment::class.java)
//            comment != null
//        }
//
//        val workbook = Workbook.createWorkbook(out)
//        val sheet = workbook.createSheet("Default", 0)
//
//        // 第一行写 Header
//        for (i in columns.indices) {
//            val label = Label(i, 0, columns[i].right)
//            sheet.addCell(label)
//        }
//
//        for (i in records.indices) {
//            val row = i + 1
//            for (j in columns.indices) {
//                val fieldValue = FieldUtils.readField(columns[j].left, records[i])
//
//                if (fieldValue == null) {
//                    val blankCell = Blank(j, row)
//                    sheet.addCell(blankCell)
//                    continue
//                }
//
//                if (ReflectUtil.isInstanceOf(fieldValue, JDateTime::class.java)) {
//                    val dateTime = fieldValue as JDateTime
//                    val dateCell = DateTime(j, row, dateTime.convertToDate())
//                    sheet.addCell(dateCell)
//                    continue
//                }
//
//                if (ReflectUtil.isInstanceOf(fieldValue, Date::class.java)) {
//                    val dateTime = fieldValue as Date
//                    val dateCell = DateTime(j, row, dateTime)
//                    sheet.addCell(dateCell)
//                    continue
//                }
//
//                if (ReflectUtil.isInstanceOf(fieldValue, Double::class.java)) {
//                    val `val` = fieldValue as Double
//                    val numberCell = Number(j, row, `val`)
//                    sheet.addCell(numberCell)
//                    continue
//                }
//
//                if (ReflectUtil.isInstanceOf(fieldValue, Int::class.java)) {
//                    val `val` = fieldValue as Int
//                    val numberCell = Number(j, row, `val`.toDouble())
//                    sheet.addCell(numberCell)
//                    continue
//                }
//
//                if (ReflectUtil.isInstanceOf(fieldValue, Long::class.java)) {
//                    val `val` = fieldValue as Long
//                    val numberCell = Number(j, row, `val`.toDouble())
//                    sheet.addCell(numberCell)
//                    continue
//                }
//
//                // 其他类型, 都转换成 String
//                val labelCell = Label(j, row, fieldValue.toString())
//                sheet.addCell(labelCell)
//            }
//        }
//        workbook.write()
//        workbook.close()
//    }

    fun Md5ToShortCode(md5str: String): String {
        //        String part1 = md5str.substring(0, 4);
        //        String part2 = md5str.substring(4, 8);
        //        String part3 = md5str.substring(8, 12);
        //        String part4 = md5str.substring(12, 16);
        //
        //        Logger.debug("{} {} {} {}", part1, part2, part3, part4);
        //
        //        long long1 = Long.parseUnsignedLong(part1, 16);
        //        long long2 = Long.parseUnsignedLong(part2, 16);
        //        long long3 = Long.parseUnsignedLong(part3, 16);
        //        long long4 = Long.parseUnsignedLong(part4, 16);
        //
        //        String scode1 = To62Radix(long1);
        //        String scode2 = To62Radix(long2);
        //        String scode3 = To62Radix(long3);
        //        String scode4 = To62Radix(long4);
        //
        //        Logger.debug("{} {} {} {}", long1, long2, long3, long4);
        //        Logger.debug("{} {} {} {}", scode1, scode2, scode3, scode4);

        val part1 = md5str.substring(0, 8)
        val part2 = md5str.substring(8, 16)

        Logger.debug("{} {}", part1, part2)

        val long1 = java.lang.Long.parseUnsignedLong(part1, 16)
        val long2 = java.lang.Long.parseUnsignedLong(part2, 16)

        Logger.debug("{} {}", long1, long2)

        val scode1 = To62Radix(long1)
        val scode2 = To62Radix(long2)


        Logger.debug("{} {}", scode1, scode2)
        return ""
    }

    private val codes_62Radix = CharArray(62)

    fun To62Radix(value: Long): String {

        val v = value / 62
        val idx = (value % 62).toInt()

        if (v == 0L) {
            return CharUtils.toString(codes_62Radix[idx])
        } else {
            return To62Radix(v) + CharUtils.toString(codes_62Radix[idx])
        }
    }

    fun From62RadixStr(vstr: String): Long {
        if (StringUtils.isBlank(vstr) || !StringUtils.isAlphanumeric(vstr)) {
            throw BizLogicException("不合法的值,只允许字母和数字")
        }
        var result: Long = 0
        for (i in 0..vstr.length - 1) {
            val s = vstr[vstr.length - 1 - i]
            val codev = QueryCodes62Radix(s)
            result = result + codev * Math.pow(62.0, i.toDouble()).toLong()
        }
        return result
    }

    private fun QueryCodes62Radix(code: Char): Int {
        for (i in codes_62Radix.indices) {
            if (codes_62Radix[i] == code) {
                return i
            }
        }
        throw BizLogicException("必须是0-9a-zA-Z")
    }

    init {
        for (i in 0..9) {
            codes_62Radix[i] = ('0' + i).toChar()
        }
        for (i in 10..10 + 26 - 1) {
            codes_62Radix[i] = ('a' + i - 10).toChar()
        }
        for (i in 36..36 + 26 - 1) {
            codes_62Radix[i] = ('A' + i - 36).toChar()
        }

    }

}