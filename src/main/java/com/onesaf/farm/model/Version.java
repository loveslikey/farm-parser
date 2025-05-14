package com.onesaf.farm.model;

import lombok.Data;

/**
 * OneSAF TDB文件格式版本信息
 */
@Data
public class Version {
    private short version;  // 版本号，目前为8
    private short format;   // 格式号，目前为0
    private short update;   // 更新号，目前为0

    // 总大小：6字节
}