package com.tilitili.admin.service;

import com.tilitili.admin.service.mirai.BaseMessageHandle;
import com.tilitili.common.entity.mirai.MessageChain;
import com.tilitili.common.entity.mirai.MiraiMessageView;
import com.tilitili.common.exception.AssertException;
import com.tilitili.common.utils.StreamUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.logging.log4j.util.Strings.isNotBlank;

@Slf4j
@Service
public class MiraiService {

    private final List<BaseMessageHandle> messageHandleList;

    @Autowired
    public MiraiService(List<BaseMessageHandle> messageHandleList) {
        this.messageHandleList = messageHandleList;
    }

    public String handleGroupMessage(MiraiMessageView message, MiraiSessionService.MiraiSession miraiSession) {
        List<MessageChain> messageChain = message.getMessageChain();
        String text = messageChain.stream().filter(StreamUtil.isEqual(MessageChain::getType, "Plain")).map(MessageChain::getText).collect(Collectors.joining("\n"));
        String url = messageChain.stream().filter(StreamUtil.isEqual(MessageChain::getType, "Image")).map(MessageChain::getUrl).findFirst().orElse("");
        String value = text+url;

        String oldValue = miraiSession.getOrDefault("value", "");
        int oldNumber = Integer.parseInt(miraiSession.getOrDefault("number", "0"));
        if (oldValue.equals(value)) {
            miraiSession.put("number", String.valueOf(oldNumber + 1));
        } else {
            miraiSession.put("value", value);
            miraiSession.put("number", "1");
        }

        String newNumber = miraiSession.get("number");
        if (Objects.equals(newNumber, "3") && value.length() < 10) {
            return text;
        }
        return "";
    }

    public String handleMessage(MiraiMessageView message, MiraiSessionService.MiraiSession miraiSession) {
        try {
            List<MessageChain> messageChain = message.getMessageChain();
            String text = messageChain.stream().filter(StreamUtil.isEqual(MessageChain::getType, "Plain")).map(MessageChain::getText).collect(Collectors.joining("\n"));
            String url = messageChain.stream().filter(StreamUtil.isEqual(MessageChain::getType, "Image")).map(MessageChain::getUrl).findFirst().orElse("");
            String[] textList = text.split("\n");

            String title;
            String body;
            if (miraiSession.containsKey("模式")) {
                title = miraiSession.get("模式");
                body = text;
                if (Objects.equals(textList[0], "退出")) {
                    miraiSession.remove("模式");
                    return "停止"+title;
                }
            } else {
                title = textList[0];
                body = Stream.of(textList).skip(1).collect(Collectors.joining("\n"));
                if (textList[0].contains("模式")) {
                    String mod = textList[0].replaceAll("模式", "");
                    miraiSession.put("模式", mod);
                    return "开始"+mod;
                }
            }

            String[] bodyList = body.split("\n");
            Map<String, String> map = new HashMap<>();
            for (String line : bodyList) {
                if (!line.contains("=")) {
                    continue;
                }
                String key = line.split("=")[0];
                String value = line.split("=")[1];
                map.put(key.trim(), value.trim());
            }
            if (isNotBlank(body)) {
                map.put("body", body);
            }
            if (isNotBlank(url)) {
                map.put("url", url);
            }

            for (BaseMessageHandle messageHandle : messageHandleList) {
                if (messageHandle.getKeyword().contains(title)) {
                    return messageHandle.handleMessage(message, map);
                }
            }

            return "?";
        } catch (AssertException e) {
            log.error(e.getMessage());
            return e.getMessage();
        } catch (Exception e) {
            log.error("处理消息回调失败",e);
            return "¿";
        }
    }
}
