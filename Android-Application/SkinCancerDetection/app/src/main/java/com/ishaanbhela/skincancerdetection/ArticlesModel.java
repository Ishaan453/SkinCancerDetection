package com.ishaanbhela.skincancerdetection;

public class ArticlesModel {

    String URL;
    String ImgURL;

    public ArticlesModel(String URL, String imgURL) {
        this.URL = URL;
        ImgURL = imgURL;
    }


    public String getURL() {
        return URL;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }

    public String getImgURL() {
        return ImgURL;
    }

    public void setImgURL(String imgURL) {
        ImgURL = imgURL;
    }
}
