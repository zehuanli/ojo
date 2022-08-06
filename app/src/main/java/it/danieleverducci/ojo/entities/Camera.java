package it.danieleverducci.ojo.entities;

import java.io.Serializable;

public class Camera implements Serializable {
    private static final long serialVersionUID = -3837361587400158910L;
    private String name;
    private String rtspUrl;
    private String rtspHDUrl;
    private int enable = 1; //启用: 1 ; 关闭: 0

    public Camera(String name, String rtspUrl, String rtspHDUrl) {
        this.name = name;
        this.rtspUrl = rtspUrl;
        this.rtspHDUrl = rtspHDUrl;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRtspUrl(String rtspUrl) {
        this.rtspUrl = rtspUrl;
    }

    public void setRtspHDUrl(String rtspHDUrl) {
        this.rtspHDUrl = rtspHDUrl;
    }

    public String getName() {
        return name;
    }

    public String getRtspUrl() {
        return rtspUrl;
    }

    public String getRtspHDUrl() {
        return rtspHDUrl;
    }

    public int getEnable() {
        return enable;
    }

    /**
     *
     * @param enable 1:enable; 0:disable
     */
    public void setEnable(int enable) {
        this.enable = enable;
    }
}
