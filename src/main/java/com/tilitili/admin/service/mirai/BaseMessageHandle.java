package com.tilitili.admin.service.mirai;

import com.tilitili.common.entity.mirai.MiraiMessage;
import com.tilitili.common.entity.mirai.MiraiMessageView;

import java.util.List;
import java.util.Map;

public interface BaseMessageHandle {
    List<String> getKeyword();
    String getDescription();
    String getSendType();
    MiraiMessage handleMessage(MiraiMessageView message, Map<String, String> map) throws Exception;
}
