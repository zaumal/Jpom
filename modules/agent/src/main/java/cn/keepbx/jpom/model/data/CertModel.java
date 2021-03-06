package cn.keepbx.jpom.model.data;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.BCUtil;
import cn.hutool.crypto.KeyUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import cn.jiangzeyin.common.DefaultSystemLog;
import cn.jiangzeyin.common.spring.SpringUtil;
import cn.keepbx.jpom.model.BaseModel;
import cn.keepbx.jpom.service.system.CertService;
import cn.keepbx.jpom.system.JpomRuntimeException;
import com.alibaba.fastjson.JSONObject;

import java.io.InputStream;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Date;

/**
 * 证书实体
 *
 * @author Arno
 */
public class CertModel extends BaseModel {

    private static final String KEY = "Jpom 管理系统";
    /**
     * 证书文件
     */
    private String cert;
    /**
     * 私钥
     */
    private String key;
    /**
     * 证书到期时间
     */
    private long expirationTime;
    /**
     * 证书生效日期
     */
    private long effectiveTime;
    /**
     * 绑定域名
     */
    private String domain;
    /**
     * 白名单路径
     */
    private String whitePath;


    public String getWhitePath() {
        return whitePath;
    }

    public void setWhitePath(String whitePath) {
        this.whitePath = whitePath;
    }

    public String getCert() {
        return cert;
    }

    public void setCert(String cert) {
        this.cert = cert;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public long getExpirationTime() {
        this.convertInfo();
        return expirationTime;
    }

    public void setExpirationTime(long expirationTime) {
        this.expirationTime = expirationTime;
    }

    public String getDomain() {
        this.convertInfo();
        return domain;
    }

    /**
     * 兼容手动添加的证书文件
     */
    private void convertInfo() {
        if (!StrUtil.isEmpty(domain)) {
            return;
        }
        JSONObject jsonObject = decodeCert(getCert(), getKey());
        if (jsonObject != null) {
            // 获取信息
            this.setDomain(jsonObject.getString("domain"));
            this.setExpirationTime(jsonObject.getLongValue("expirationTime"));
            this.setEffectiveTime(jsonObject.getLongValue("effectiveTime"));

            // 数据持久化到文件中
            CertService certService = SpringUtil.getBean(CertService.class);
            certService.updateItem(this);
        }
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public long getEffectiveTime() {
        this.convertInfo();
        return effectiveTime;
    }

    public void setEffectiveTime(long effectiveTime) {
        this.effectiveTime = effectiveTime;
    }

    /**
     * 解析证书
     *
     * @param file 证书文件
     */
    public static JSONObject decodeCert(String file, String key) {
        if (file == null) {
            return null;
        }
        if (!FileUtil.exist(file)) {
            return null;
        }
        InputStream inputStream = null;
        try {
            inputStream = ResourceUtil.getStream(key);
            PrivateKey privateKey = BCUtil.readPrivateKey(inputStream);
            IoUtil.close(inputStream);
            inputStream = ResourceUtil.getStream(file);
            PublicKey publicKey = BCUtil.readPublicKey(inputStream);
            IoUtil.close(inputStream);
            RSA rsa = new RSA(privateKey, publicKey);
            String encryptStr = rsa.encryptBase64(KEY, KeyType.PublicKey);
            String decryptStr = rsa.decryptStr(encryptStr, KeyType.PrivateKey);
            if (!KEY.equals(decryptStr)) {
                throw new JpomRuntimeException("证书和私钥证书不匹配");
            }
        } finally {
            IoUtil.close(inputStream);
        }
        try {
            inputStream = ResourceUtil.getStream(file);
            // 创建证书对象
            X509Certificate oCert = (X509Certificate) KeyUtil.readX509Certificate(inputStream);
            //到期时间
            Date expirationTime = oCert.getNotAfter();
            //生效日期
            Date effectiveTime = oCert.getNotBefore();
            //域名
            String name = oCert.getSubjectDN().getName();
            int i = name.indexOf("=");
            String domain = name.substring(i + 1);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("expirationTime", expirationTime.getTime());
            jsonObject.put("effectiveTime", effectiveTime.getTime());
            jsonObject.put("domain", domain);
            return jsonObject;
        } catch (Exception e) {
            DefaultSystemLog.ERROR().error(e.getMessage(), e);
        } finally {
            IoUtil.close(inputStream);
        }
        return null;
    }
}
