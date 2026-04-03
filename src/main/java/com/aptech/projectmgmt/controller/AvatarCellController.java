package com.aptech.projectmgmt.controller;

import com.aptech.projectmgmt.util.AvatarUtil;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;

public class AvatarCellController {

    @FXML private ImageView avatarImageView;

    public void setPhoto(String photoUrl) {
        AvatarUtil.applyAvatar(avatarImageView, photoUrl);
    }
}
