package cn.keepbx.jpom.system;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.system.SystemUtil;
import cn.jiangzeyin.common.spring.SpringUtil;
import cn.keepbx.jpom.BaseJpomApplication;
import cn.keepbx.jpom.common.Type;
import cn.keepbx.jpom.model.system.JpomManifest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.File;

/**
 * 配置项
 *
 * @author jiangzeyin
 * @date 2019/4/16
 */
@Configuration
public class ConfigBean {
    /**
     * 用户角色header
     */
    public static final String JPOM_SERVER_SYSTEM_USER_ROLE = "Jpom-Server-SystemUserRole";
    /**
     * 用户名header
     */
    public static final String JPOM_SERVER_USER_NAME = "Jpom-Server-UserName";

    public static final String JPOM_AGENT_AUTHORIZE = "Jpom-Agent-Authorize";

    private static final String DATA = "data";

    public static final int AUTHORIZE_ERROR = 900;

    /**
     * 授权信息
     */
    private static final String AUTHORIZE = "agent_authorize.json";

    /**
     * Jpom 程序运行的 application 标识
     */
    @Value("${jpom.applicationTag:}")
    public String applicationTag;
    /**
     * 程序端口
     */
    @Value("${server.port}")
    private int port;

    private static ConfigBean configBean;

    /**
     * 单利模式
     *
     * @return config
     */
    public static ConfigBean getInstance() {
        if (configBean == null) {
            configBean = SpringUtil.getBean(ConfigBean.class);
        }
        return configBean;
    }

    public int getPort() {
        return port;
    }

    /**
     * 获取项目运行数据存储文件夹路径
     *
     * @return 文件夹路径
     */
    public String getDataPath() {
        String dataPath = FileUtil.normalize(ExtConfigBean.getInstance().getPath() + "/" + DATA);
        FileUtil.mkdir(dataPath);
        return dataPath;
    }

    /**
     * 获取pid文件
     *
     * @return file
     */
    public File getPidFile() {
        return new File(getDataPath(), StrUtil.format("pid.{}.{}",
                BaseJpomApplication.getAppType().name(), JpomManifest.getInstance().getPid()));
    }

    /**
     * 获取当前项目全局 运行信息文件路径
     *
     * @param type 程序类型
     * @return file
     */
    public File getApplicationJpomInfo(Type type) {
        return FileUtil.file(SystemUtil.getUserInfo().getTempDir(), "jpom", type.name());
    }

    /**
     * 获取 agent 端自动生成的授权文件路径
     *
     * @param dataPath 指定数据路径
     * @return file
     */
    public String getAgentAutoAuthorizeFile(String dataPath) {
        return FileUtil.normalize(dataPath + "/" + ConfigBean.AUTHORIZE);
    }
}
