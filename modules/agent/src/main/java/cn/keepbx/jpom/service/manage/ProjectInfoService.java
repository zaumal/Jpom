package cn.keepbx.jpom.service.manage;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.keepbx.jpom.common.BaseOperService;
import cn.keepbx.jpom.model.data.ProjectInfoModel;
import cn.keepbx.jpom.model.data.ProjectRecoverModel;
import cn.keepbx.jpom.system.AgentConfigBean;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.util.HashSet;
import java.util.List;

/**
 * 项目管理
 *
 * @author jiangzeyin
 */
@Service
public class ProjectInfoService extends BaseOperService<ProjectInfoModel> {
    @Resource
    private ProjectRecoverService projectRecoverService;

    /**
     * 查询所有项目信息
     *
     * @return list
     */
    @Override
    public List<ProjectInfoModel> list() {
        JSONObject jsonObject = getJSONObject(AgentConfigBean.PROJECT);
        JSONArray jsonArray = formatToArray(jsonObject);
        return jsonArray.toJavaList(ProjectInfoModel.class);
    }


    public HashSet<String> getAllGroup() {
        //获取所有分组
        List<ProjectInfoModel> projectInfoModels = list();
        HashSet<String> hashSet = new HashSet<>();
        for (ProjectInfoModel projectInfoModel : projectInfoModels) {
            hashSet.add(projectInfoModel.getGroup());
        }
        return hashSet;
    }

    /**
     * 保存项目信息
     *
     * @param projectInfo 项目
     */
    @Override
    public void addItem(ProjectInfoModel projectInfo) {
        // 保存
        saveJson(AgentConfigBean.PROJECT, projectInfo.toJson());
    }

    /**
     * 删除项目
     *
     * @param projectInfo 项目
     */
    public void deleteProject(ProjectInfoModel projectInfo, String userId) throws Exception {
        deleteJson(AgentConfigBean.PROJECT, projectInfo.getId());
        // 添加回收记录
        ProjectRecoverModel projectRecoverModel = new ProjectRecoverModel(projectInfo);
        projectRecoverModel.setDelUser(userId);
        projectRecoverService.addItem(projectRecoverModel);
    }

    /**
     * 修改项目信息
     *
     * @param projectInfo 项目信息
     */
    @Override
    public boolean updateItem(ProjectInfoModel projectInfo) throws Exception {
        projectInfo.setModifyTime(DateUtil.now());
        updateJson(AgentConfigBean.PROJECT, projectInfo.toJson());
        return true;
    }


    /**
     * 根据id查询项目
     *
     * @param id 项目Id
     * @return model
     */
    @Override
    public ProjectInfoModel getItem(String id) {
        return getJsonObjectById(AgentConfigBean.PROJECT, id, ProjectInfoModel.class);
    }

    public String getLogSize(String id) {
        ProjectInfoModel pim;
        pim = getItem(id);
        if (pim == null) {
            return null;
        }
        String logSize = null;
        File file = new File(pim.getLog());
        if (file.exists()) {
            long fileSize = file.length();
            if (fileSize <= 0) {
                return null;
            }
            logSize = FileUtil.readableFileSize(fileSize);
        }
        return logSize;
    }
}
