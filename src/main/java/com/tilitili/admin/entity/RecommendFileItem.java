package com.tilitili.admin.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class RecommendFileItem {
//    private Long av;
//    private String name;
//    private String operator;
//    private String text;
    private Integer startTime;
//    private String owner;
//    private String externalOwner;
//    private String type;

    private String avStr;
    private String nameStr;
    private String operatorStr;
    private String textStr;
    private String ownerStr;
    private String externalOwnerStr;
    private String typeStr;
}
