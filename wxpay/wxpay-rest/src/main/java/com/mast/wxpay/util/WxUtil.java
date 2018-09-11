package com.mast.wxpay.util;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.*;

/**
 * @author Administrator
 * @date 2017-7-6
 */
public class WxUtil {

    private Logger logger = LoggerFactory.getLogger(WxUtil.class);

    public String wxEnStr(Map<String, String> map, String appKey) throws Exception {

        StringBuilder xml = new StringBuilder();
        xml.append("<xml>");
        List<String> keyList = new ArrayList<>(map.size());
        for (Map.Entry entry : map.entrySet()) {
            keyList.add((String) entry.getKey());
        }
        Collections.sort(keyList);
        StringBuilder signStr = new StringBuilder();
        for (String key : keyList) {

            signStr.append("&").append(key).append("=").append(map.get(key));
        }
        signStr.append("&key=" + appKey);
        String sign = DigestUtils.md5Hex(signStr.toString().substring(1).getBytes("ISO8859-1")).toUpperCase();
        map.put("sign", sign);
        for (String k : map.keySet()) {

            xml.append("<" + k + ">" + map.get(k) + "</" + k + ">");
        }
        xml.append("</xml>");
        return xml.toString();
    }

    public Map<String, String> xmlToMap(String strXML) throws Exception {

        try {
            Map<String, String> data = new HashMap<>(16);
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            InputStream stream = new ByteArrayInputStream(strXML.getBytes("UTF-8"));
            org.w3c.dom.Document doc = documentBuilder.parse(stream);
            doc.getDocumentElement().normalize();
            NodeList nodeList = doc.getDocumentElement().getChildNodes();
            for (int idx = 0; idx < nodeList.getLength(); ++idx) {
                Node node = nodeList.item(idx);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    org.w3c.dom.Element element = (org.w3c.dom.Element) node;
                    data.put(element.getNodeName(), element.getTextContent());
                }
            }
            try {
                stream.close();
            } catch (Exception ex) {
                // do nothing
            }
            return data;
        } catch (Exception ex) {
            logger.error("Invalid XML, can not convert to map. Error message: {}. XML content: {}" + ex.getMessage() + strXML);
            throw ex;
        }
    }

    public Map<String, String> requestWx(String sendXml, String url, String p12, String mchId) throws Exception {

        KeyStore keyStore = KeyStore.getInstance("PKCS12");

        keyStore.load(new FileInputStream(new File(p12)), mchId.toCharArray());

        // 实例化密钥库 & 初始化密钥工厂
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, mchId.toCharArray());

        // 创建 SSLContext
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), null, new SecureRandom());

        SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(
                sslContext,
                new String[]{"TLSv1"},
                null,
                new DefaultHostnameVerifier());

        BasicHttpClientConnectionManager connManager = new BasicHttpClientConnectionManager(
                RegistryBuilder.<ConnectionSocketFactory>create()
                        .register("http", PlainConnectionSocketFactory.getSocketFactory())
                        .register("https", sslConnectionSocketFactory)
                        .build(),
                null,
                null,
                null
        );
        HttpPost post = new HttpPost(url);
        post.setHeader("Content-Type", "text/xml");
        post.setEntity(new StringEntity(sendXml, ContentType.APPLICATION_XML));
        CloseableHttpClient httpClient = HttpClientBuilder.create().setConnectionManager(connManager).build();
        CloseableHttpResponse response = httpClient.execute(post);
        if (response.getStatusLine().getStatusCode() != 200) {

            logger.error("请求失败,状态码:" + response.getStatusLine().getStatusCode());
            logger.error("请求失败了");
            return null;
        }
        HttpEntity httpEntity = response.getEntity();
        String resultStr = EntityUtils.toString(httpEntity, "UTF-8");
        return this.xmlToMap(resultStr);
    }

    public Boolean validatorSign(Map<String, String> xml, String keyStr) throws Exception {

        String sign = xml.get("sign");
        xml.remove("sign");
        List<String> keys = new ArrayList<>(xml.keySet());
        Collections.sort(keys);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < keys.size(); i++) {

            String key = keys.get(i);
            String value = xml.get(key);
            if ("".equals(value)) {
                continue;
            }
            sb.append((i == 0 ? "" : "&") + key + "=" + value);
        }
        sb.append("&key=" + keyStr);
        return sign.equals(DigestUtils.md5Hex(sb.toString().getBytes()).toUpperCase());
    }
}
