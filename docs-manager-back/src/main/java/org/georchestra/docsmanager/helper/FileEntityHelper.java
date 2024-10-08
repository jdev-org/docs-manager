package org.georchestra.docsmanager.helper;

import org.georchestra.docsmanager.model.FileEntity;

public class FileEntityHelper {
    public static FileEntity getFileExample(String status, String plugin, String entity,
            String label, String id, Boolean opened) {
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
        if (id != null) {
            fileAsExample.setId(id);
        }
        if(opened != null) {
            fileAsExample.setOpened(opened);
        } else {
            fileAsExample.setOpened(null);
        }
        return fileAsExample;
    }

    public static FileEntity getOpenedFileExample(String status, String plugin, String entity,
            String label, String id) {
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
            fileAsExample.setLabel(label);
        }

        if (id != null) {
            fileAsExample.setId(id);
        }

        fileAsExample.setOpened(true);

        return fileAsExample;
    }

    public static FileEntity getFileExample(Boolean opened) {
        FileEntity fileAsExample = new FileEntity();
        if (opened != null) {
            fileAsExample.setOpened(opened);
        }
        return fileAsExample;
    }
}
