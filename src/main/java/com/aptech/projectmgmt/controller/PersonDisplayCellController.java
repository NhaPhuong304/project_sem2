package com.aptech.projectmgmt.controller;

import com.aptech.projectmgmt.util.AvatarUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

public class PersonDisplayCellController {

    @FXML private ImageView avatarImageView;
    @FXML private Label nameLabel;

    public void setPerson(String name, String photoUrl) {
        nameLabel.setText(name != null ? name : "");
        nameLabel.setStyle("-fx-text-fill: black;");
        AvatarUtil.applyAvatar(avatarImageView, photoUrl);
    }
}
