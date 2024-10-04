package org.georchestra.docsmanager.model;

public class FileResponse {

    private String id;
    private String name;
    private Long size;
    private String url;
    private String contentType;
    private String label;
    private String plugin;
    private String dateDoc;
    private String status;
    private String comment;
    private String entity;
    private Boolean opened;

    public String getId() {
        return id;
    }

    public String getDateDoc() {
        return dateDoc;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setDateDoc(String dateDoc) {
        this.dateDoc = dateDoc;
    }

    public void setPlugin(String plugin) {
        this.plugin = plugin;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getPlugin() {
        return plugin;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getComment() {
        return comment;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public void setOpened(Boolean opened) {
        this.opened = opened;
    }

    public String getEntity() {
        return entity;
    }

    public Boolean getOpened() {
        return opened;
    }
}
