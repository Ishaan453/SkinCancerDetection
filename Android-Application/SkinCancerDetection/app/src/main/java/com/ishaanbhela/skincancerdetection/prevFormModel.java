package com.ishaanbhela.skincancerdetection;

public class prevFormModel {
    private String Text;
    private String FormID;
    public prevFormModel(String formName, String formID) {
        this.Text = formName;
        this.FormID = formID;

    }
    public String getText() {
        return Text;
    }

    public void setText(String text) {
        Text = text;
    }

    public String getFormID() {
        return FormID;
    }

    public void setFormID(String formID) {
        FormID = formID;
    }




}
