package cn.keepbx.jpom.system.init;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.db.Db;
import cn.hutool.db.ds.DSFactory;
import cn.hutool.setting.Setting;
import cn.jiangzeyin.common.DefaultSystemLog;
import cn.jiangzeyin.common.PreLoadClass;
import cn.jiangzeyin.common.PreLoadMethod;
import cn.keepbx.jpom.model.system.JpomManifest;
import cn.keepbx.jpom.system.db.DbConfig;

import java.io.InputStream;


/**
 * @author jiangzeyin
 * @date 2019/4/19
 */
@PreLoadClass
public class InitDb {

    @PreLoadMethod
    private static void init() {
        Setting setting = new Setting();
        setting.set("url", DbConfig.getInstance().getDbUrl());
        setting.set("user", "jpom");
        setting.set("pass", "jpom");
        //
        if (JpomManifest.getInstance().isDebug()) {
            setting.set("showSql", "true");
            setting.set("sqlLevel", "INFO");
            setting.set("showParams", "true");
        }
        try {
            DSFactory dsFactory = DSFactory.create(setting);
            DSFactory.setCurrentDSFactory(dsFactory);
            InputStream inputStream = ResourceUtil.getStream("classpath:/bin/h2-db-v1.sql");
            String sql = IoUtil.read(inputStream, CharsetUtil.CHARSET_UTF_8);
            Db.use().execute(sql);
        } catch (Exception e) {
            DefaultSystemLog.ERROR().error("初始化数据失败", e);
            System.exit(0);
        }
    }
}
