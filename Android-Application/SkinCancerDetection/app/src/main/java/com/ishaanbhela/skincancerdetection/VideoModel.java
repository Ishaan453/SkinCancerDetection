package com.ishaanbhela.skincancerdetection;

public class VideoModel {
    public VideoModel(String vidURL, String imgURL) {
        VidURL = vidURL;
        ImgURL = imgURL;
    }

    public String getVidURL() {
        return VidURL;
    }

    public void setVidURL(String vidURL) {
        VidURL = vidURL;
    }

    public String getImgURL() {
        return ImgURL;
    }

    public void setImgURL(String imgURL) {
        ImgURL = imgURL;
    }

    String VidURL;
    String ImgURL;
}
