package com.frontbackend.springboot.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "FILES")
public class FileEntity {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;
    private String name;
    private String contentType;
    private String label;
    private String entity;
    private String plugin;
    private String comment;
    private String createDate;
    private String userInfos;

    private Long size;

    @Lob
    private byte[] data;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContentType() {
        return contentType;
    }

    public String getLabel() {
        return label;
    }

    public String getEntity() {
        return entity;
    }

    public String getPlugin() {
        return plugin;
    }

    public String getComment() {
        return comment;
    }

    public String getCreateDate() {
        return createDate;
    }

    public String getUserInfos() {
        return userInfos;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setCreateDate() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH_mm_ss_SSS");
        Date date = new Date();
        this.createDate = dateFormat.format(date);
    }
    public void setPlugin(String plugin) {
        this.plugin = plugin.toUpperCase();
    }
    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public void setUserInfos(String userInfos) {
        this.userInfos = userInfos;
    }
}
