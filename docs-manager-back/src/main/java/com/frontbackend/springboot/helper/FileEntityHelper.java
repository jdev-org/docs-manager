package com.frontbackend.springboot.helper;

import com.frontbackend.springboot.model.FileEntity;


public class FileEntityHelper {
    public static FileEntity getFileExample(String status, String plugin, String entity) {
        FileEntity fileAsExample = new FileEntity();
        if (entity != null) {
            fileAsExample.setEntity(entity);
        }
        if (status != null) {
            fileAsExample.setStatus(status);
        }
        if (plugin != null) {
            fileAsExample.setPlugin(plugin);
        }
        return fileAsExample;
    }
}
