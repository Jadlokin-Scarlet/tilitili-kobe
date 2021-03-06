package com.tilitili.admin.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.tilitili.admin.entity.DispatchRecommendResourcesView;
import com.tilitili.admin.entity.DispatchResourcesView;
import com.tilitili.admin.entity.RecommendFileItem;
import com.tilitili.admin.entity.VideoDataFileItem;
import com.tilitili.admin.service.RecommendService;
import com.tilitili.admin.service.VideoDataFileService;
import com.tilitili.common.entity.Recommend;
import com.tilitili.common.entity.RecommendTalk;
import com.tilitili.common.entity.RecommendVideo;
import com.tilitili.common.entity.query.RecommendQuery;
import com.tilitili.common.entity.query.RecommendTalkQuery;
import com.tilitili.common.entity.resource.Resource;
import com.tilitili.common.entity.query.VideoDataQuery;
import com.tilitili.common.entity.view.BaseModel;
import com.tilitili.admin.service.ResourceService;
import com.tilitili.common.entity.view.PageModel;
import com.tilitili.common.manager.RecommendManager;
import com.tilitili.common.mapper.RecommendMapper;
import com.tilitili.common.mapper.RecommendTalkMapper;
import com.tilitili.common.mapper.RecommendVideoMapper;
import com.tilitili.common.utils.Asserts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

@Controller
@RequestMapping("api/resources")
@Validated
@Slf4j
public class ResourceController extends BaseController {
    private final RecommendManager recommendManager;
    private final ResourceService resourceService;
    private final VideoDataFileService videoDataFileService;
    private final RecommendService recommendService;
    private final RecommendMapper recommendMapper;
    private final RecommendVideoMapper recommendVideoMapper;
    private final RecommendTalkMapper recommendTalkMapper;

    @Autowired
    public ResourceController(RecommendManager recommendManager, ResourceService resourceService, VideoDataFileService videoDataFileService, RecommendService recommendService, RecommendMapper recommendMapper, RecommendVideoMapper recommendVideoMapper, RecommendTalkMapper recommendTalkMapper) {
        this.recommendManager = recommendManager;
        this.resourceService = resourceService;
        this.videoDataFileService = videoDataFileService;
        this.recommendService = recommendService;
        this.recommendMapper = recommendMapper;
        this.recommendVideoMapper = recommendVideoMapper;
        this.recommendTalkMapper = recommendTalkMapper;
    }

    @GetMapping("")
    @ResponseBody
    public BaseModel getResources(@RequestParam List<String> needResourcesList) {
        HashMap<String, List<Resource>> resourceMap = new HashMap<>();
        needResourcesList.forEach(
                resourceName -> resourceMap.put(resourceName, resourceService.getResource(resourceName))
        );
        return new BaseModel("查询成功", true, resourceMap);
    }

    @GetMapping("/flag")
    @ResponseBody
    public BaseModel getFlag() {
        DispatchResourcesView flagResource = resourceService.getFlagResources();
        flagResource.setMusicName("曲名: " + flagResource.getMusicName());
        flagResource.setMusicOwner("社团: " + flagResource.getMusicOwner());
        flagResource.setMusicCard("专辑: " + flagResource.getMusicCard());
        flagResource.setMusicSource("原曲: " + flagResource.getMusicSource());
        return new BaseModel("success", true, flagResource);
    }

    @GetMapping("/recommendFlag")
    @ResponseBody
    public BaseModel getRecommendFlag() {
        DispatchRecommendResourcesView flagResource = resourceService.getRecommendFlagResources();
        return new BaseModel("success", true, flagResource);
    }

    @GetMapping("/adminFlag")
    @ResponseBody
    public BaseModel getAdminFlag() {
        DispatchResourcesView flagResource = resourceService.getFlagResources();
        return new BaseModel("success", true, flagResource);
    }

    @GetMapping("/videoDataFile")
    @ResponseBody
    @JsonView(VideoDataFileItem.VideoView.class)
    public BaseModel getVideoDataList(VideoDataQuery videoDataQuery) {
        return videoDataFileService.listForDataFile(videoDataQuery);
    }

    @GetMapping("/recommend")
    @ResponseBody
    public BaseModel getRecommend(RecommendQuery query) {
        if (query.getIssueId() == null) {
            RecommendVideo recommendVideo = recommendVideoMapper.getNew();
            query.setIssueId(recommendVideo.getId());
        }
        query.setSorter("sort_num", "desc");
        int total = recommendManager.countUseRecommend(query);
        List<RecommendFileItem> recommendList = recommendService.getRecommendFile(query);
        return PageModel.of(total, query.getPageSize(), query.getCurrent(), recommendList);
    }

    @GetMapping("/selfRecommend")
    @ResponseBody
    public BaseModel getSelfRecommend(RecommendQuery query) {
        if (query.getIssueId() == null) {
            RecommendVideo recommendVideo = recommendVideoMapper.getNew();
            query.setIssueId(recommendVideo.getId());
        }
        query.setSorter("sort_num", "desc");
        int total = recommendManager.countSelfRecommend(query);
        List<RecommendFileItem> recommendList = recommendService.getSelfRecommendFile(query);
        return PageModel.of(total, query.getPageSize(), query.getCurrent(), recommendList);
    }

    @GetMapping("/recommend/text")
    @ResponseBody
    public BaseModel getRecommendText(Long av) {
        Recommend recommend = recommendMapper.getByAv(av);
        return BaseModel.success(recommend.getText());
    }

    @GetMapping("/recommendTalk")
    @ResponseBody
    public BaseModel getRecommendTalk(RecommendTalkQuery query) {
        if (query.getIssueId() == null) {
            RecommendVideo recommendVideo = recommendVideoMapper.getNew();
            query.setIssueId(recommendVideo.getId());
        }
        query.setStatus(0).setSorter("id", "asc");
        int total = recommendTalkMapper.count(query);
        List<RecommendTalk> recommendTalkList = recommendTalkMapper.list(query);
        return PageModel.of(total, query.getPageSize(), query.getCurrent(), recommendTalkList);
    }

    @PatchMapping("/flag")
    @ResponseBody
    public BaseModel updateFlag(@RequestBody DispatchResourcesView resourcesView) {
        Asserts.notNull(resourcesView.getIsStaffShow1(), "参数异常");
        Asserts.notNull(resourcesView.getIsStaffShow2(), "参数异常");
        Asserts.notNull(resourcesView.getMusicName(), "参数异常");
        Asserts.notNull(resourcesView.getMusicOwner(), "参数异常");
        Asserts.notNull(resourcesView.getMusicCard(), "参数异常");
        Asserts.notNull(resourcesView.getMusicImage(), "参数异常");
        Asserts.notNull(resourcesView.getMusicSource(), "参数异常");
        Asserts.notNull(resourcesView.getTips(), "参数异常");
        resourceService.updateFlagResources(resourcesView);
        DispatchResourcesView flagResource = resourceService.getFlagResources();
        return new BaseModel("保存成功", true, flagResource);
    }
}
