package org.georchestra.docsmanager.helper;

import org.georchestra.docsmanager.model.FileEntity;

public class FileEntityHelper {
    public static FileEntity getFileExample(String status, String plugin, String entity,
            String label) {
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
        if (label != null) {
            fileAsExample.setPlugin(label);
        }
        return fileAsExample;
    }

    public static FileEntity getFileWithLabel(String label) {
        FileEntity fileAsExample = new FileEntity();
        fileAsExample.setPlugin(label);
        return fileAsExample;
    }
}
