package com.aptech.projectmgmt.service;

import com.aptech.projectmgmt.model.Account;
import com.aptech.projectmgmt.repository.AccountRepository;
import com.aptech.projectmgmt.util.AvatarUtil;
import com.aptech.projectmgmt.util.SessionManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public class AccountService {

    private final AccountRepository accountRepository = new AccountRepository();

    public void updateCurrentAvatar(String photoUrl) {
        String normalizedPhotoUrl = photoUrl != null ? photoUrl.trim() : "";
        if (normalizedPhotoUrl.isEmpty()) {
            throw new RuntimeException("Duong dan avatar khong hop le");
        }

        Path photoPath = Paths.get(normalizedPhotoUrl);
        if (!Files.exists(photoPath)) {
            throw new RuntimeException("Khong tim thay file avatar da chon");
        }
        if (Files.isDirectory(photoPath)) {
            throw new RuntimeException("Avatar phai la mot file anh hop le");
        }

        SessionManager sessionManager = SessionManager.getInstance();
        Account account = sessionManager.getCurrentAccount();
        if (account == null) {
            throw new RuntimeException("Khong xac dinh duoc tai khoan dang dang nhap");
        }

        Path avatarDirectory = AvatarUtil.getProjectAvatarDirectory();
        Path storedAvatarPath = copyAvatarToProject(account.getAccountId(), photoPath, avatarDirectory);
        String oldPhotoUrl = account.getPhotoUrl();
        String storedPhotoUrl = AvatarUtil.toProjectRelativePath(storedAvatarPath);

        try {
            accountRepository.updatePhoto(account.getAccountId(), storedPhotoUrl);
        } catch (RuntimeException ex) {
            deleteQuietly(storedAvatarPath);
            throw ex;
        }

        deletePreviousAvatar(oldPhotoUrl, storedAvatarPath);
        account.setPhotoUrl(storedPhotoUrl);

        if (sessionManager.getCurrentStudent() != null) {
            sessionManager.getCurrentStudent().setPhotoUrl(storedPhotoUrl);
        }
        if (sessionManager.getCurrentStaff() != null) {
            sessionManager.getCurrentStaff().setPhotoUrl(storedPhotoUrl);
        }
    }

    private Path copyAvatarToProject(int accountId, Path sourcePhotoPath, Path avatarDirectory) {
        try {
            Files.createDirectories(avatarDirectory);
            String fileExtension = extractExtension(sourcePhotoPath.getFileName().toString());
            String randomName = "avatar_acc_" + accountId + "_" + UUID.randomUUID().toString().replace("-", "") + fileExtension;
            Path targetPath = avatarDirectory.resolve(randomName);
            Files.copy(sourcePhotoPath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            return targetPath;
        } catch (IOException ex) {
            throw new RuntimeException("Khong the luu avatar vao project: " + ex.getMessage(), ex);
        }
    }

    private void deletePreviousAvatar(String oldPhotoUrl, Path newAvatarPath) {
        if (!AvatarUtil.isManagedProjectAvatar(oldPhotoUrl)) {
            return;
        }

        Path oldAvatarPath = AvatarUtil.resolveStoredAvatarPath(oldPhotoUrl);
        if (oldAvatarPath.equals(newAvatarPath)) {
            return;
        }
        deleteQuietly(oldAvatarPath);
    }

    private void deleteQuietly(Path filePath) {
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException ignored) {
        }
    }

    private String extractExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            return ".png";
        }
        return fileName.substring(dotIndex).toLowerCase();
    }
}
