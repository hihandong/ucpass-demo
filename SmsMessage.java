import com.alibaba.fastjson.JSON;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import sun.misc.BASE64Encoder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Ucpass Message Send
 *
 * @author Sgmder 2016/11/24.
 */

public class SmsMessage {

    /**
     * Message Config
     */
    private final static String SMS_UTF8 = "utf-8";
    private final static String SMS_VERSION = "2014-06-30";
    private final static String SMS_REST_SERVER = "https://api.ucpaas.com";
    //SMS_ACCOUNTSID
    private final static String SMS_ACCOUNTSID = "xxxxxxxxxxxxxxxxxxxxx";
    //SMS_AUTHTOKEN
    private final static String SMS_AUTHTOKEN = "xxxxxxxxxxxxxxxxxxxxx";
    //SMS_APPID
    private final static String SMS_APPID = "xxxxxxxxxxxxxxxxxxxxx";
    //SMS_TEMPLATEID
    private final static String SMS_TEMPLATEID = "xxxxx";

    /**
     * @param to         phone num
     * @param param      param {"xxx"} or {"xxx,xxx"}
     * @param templateId templateId
     * @return response
     */
    public static String templateSMS(String to, String param, String templateId) {
        String result = "";
        CloseableHttpClient httpclient = null;
        try {
            httpclient = HttpClients.createDefault();
            // 构造请求URL内容
            String timestamp = dateToStr(new Date(), "yyyyMMddHHmmss");// 时间戳

            String sig = SMS_ACCOUNTSID + SMS_AUTHTOKEN + timestamp;

            String signature = md5Digest(sig);

            String url = SMS_REST_SERVER + "/" + SMS_VERSION +
                    "/Accounts/" + SMS_ACCOUNTSID +
                    "/Messages/templateSMS" + "?sig=" +
                    signature;

            Map<String, String> map = new HashMap<>();
            map.put("accountSid", SMS_ACCOUNTSID);
            map.put("authToken", SMS_AUTHTOKEN);
            map.put("appId", SMS_APPID);
            map.put("templateId", templateId);
            map.put("to", to);
            map.put("param", param);

            String body = JSON.toJSONString(map);

            body = "{\"templateSMS\":" + body + "}";
            HttpResponse response = get("application/json", SMS_ACCOUNTSID, timestamp, url, httpclient, body);

            // 获取响应实体信息
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                result = EntityUtils.toString(entity, "UTF-8");
            }
            // 确保HTTP响应内容全部被读出或者内容流被关闭
            EntityUtils.consume(entity);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关闭连接
            if (httpclient != null) {
                try {
                    httpclient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    private static HttpResponse get(String cType, String accountSid, String timestamp, String url,
                                    CloseableHttpClient httpclient, String body) throws Exception {
        HttpPost httppost = new HttpPost(url);
        httppost.setHeader("Accept", cType);//
        httppost.setHeader("Content-Type", cType);
        String src = accountSid + ":" + timestamp;
        String auth = base64Encoder(src);
        httppost.setHeader("Authorization", auth);
        BasicHttpEntity requestBody = new BasicHttpEntity();
        requestBody.setContent(new ByteArrayInputStream(body.getBytes("UTF-8")));
        requestBody.setContentLength(body.getBytes("UTF-8").length);
        httppost.setEntity(requestBody);
        return httpclient.execute(httppost);
    }

    private static String dateToStr(Date date, String pattern) {
        if (date == null)
            return null;
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        return formatter.format(date);
    }

    private static String md5Digest(String src) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] b = md.digest(src.getBytes(SMS_UTF8));
        return byte2HexStr(b);
    }

    private static String base64Encoder(String src) throws Exception {
        BASE64Encoder encoder = new BASE64Encoder();
        return encoder.encode(src.getBytes(SMS_UTF8));
    }

    private static String byte2HexStr(byte[] b) {
        StringBuilder sb = new StringBuilder();
        for (byte aB : b) {
            String s = Integer.toHexString(aB & 0xFF);
            if (s.length() == 1) {
                sb.append("0");
            }
            sb.append(s.toUpperCase());
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        SmsMessage.templateSMS("138XXXXXXXX", "param,param2", SMS_TEMPLATEID);
    }
}
