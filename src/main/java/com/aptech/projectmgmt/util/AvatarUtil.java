package com.aptech.projectmgmt.util;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class AvatarUtil {

    public static final String DEFAULT_AVATAR_RESOURCE = "/images/no-image.jpg";
    public static final String DEFAULT_AVATAR_NAME = "no-image.jpg";
    public static final String PROJECT_AVATAR_DIR = "uploads/avatars";

    private AvatarUtil() {
    }

    public static void applyAvatar(ImageView imageView, String photoUrl) {
        imageView.setImage(loadAvatar(photoUrl));
        imageView.setVisible(true);
        imageView.setManaged(true);
    }

    public static Image loadAvatar(String photoUrl) {
        Image image = loadImage(photoUrl);
        if (image != null) {
            return image;
        }
        Image fallback = loadClasspathImage(DEFAULT_AVATAR_RESOURCE);
        if (fallback != null) {
            return fallback;
        }
        throw new IllegalStateException("Khong tim thay avatar mac dinh: " + DEFAULT_AVATAR_RESOURCE);
    }

    private static Image loadImage(String photoUrl) {
        if (photoUrl == null || photoUrl.isBlank()) {
            return null;
        }

        String trimmed = photoUrl.trim();

        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")
                || trimmed.startsWith("file:") || trimmed.startsWith("jar:")) {
            return loadJavaFxImage(trimmed);
        }

        Image classpathImage = loadClasspathImage(trimmed);
        if (classpathImage != null) {
            return classpathImage;
        }

        Path path = resolveStoredAvatarPath(trimmed);
        if (Files.exists(path)) {
            return loadJavaFxImage(path.toUri().toString());
        }

        return null;
    }

    private static Image loadClasspathImage(String resourcePath) {
        String normalized = resourcePath.startsWith("/") ? resourcePath : "/" + resourcePath;
        URL resource = AvatarUtil.class.getResource(normalized);
        if (resource == null && !resourcePath.contains("/")) {
            resource = AvatarUtil.class.getResource("/images/" + resourcePath);
        }
        if (resource == null) {
            return null;
        }
        return loadJavaFxImage(resource.toExternalForm());
    }

    public static Path getProjectRoot() {
        return Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
    }

    public static Path getProjectAvatarDirectory() {
        return getProjectRoot().resolve(PROJECT_AVATAR_DIR).normalize();
    }

    public static Path resolveStoredAvatarPath(String photoUrl) {
        if (photoUrl == null || photoUrl.isBlank()) {
            return getProjectRoot();
        }

        Path path = Paths.get(photoUrl.trim());
        if (path.isAbsolute()) {
            return path.normalize();
        }
        return getProjectRoot().resolve(path).normalize();
    }

    public static String toProjectRelativePath(Path filePath) {
        Path normalized = filePath.toAbsolutePath().normalize();
        Path projectRoot = getProjectRoot();
        if (normalized.startsWith(projectRoot)) {
            return projectRoot.relativize(normalized).toString().replace('\\', '/');
        }
        return normalized.toString();
    }

    public static boolean isDefaultAvatar(String photoUrl) {
        if (photoUrl == null || photoUrl.isBlank()) {
            return true;
        }
        String normalized = photoUrl.trim().replace('\\', '/');
        return DEFAULT_AVATAR_NAME.equalsIgnoreCase(normalized)
                || DEFAULT_AVATAR_RESOURCE.equalsIgnoreCase(normalized)
                || normalized.endsWith("/" + DEFAULT_AVATAR_NAME);
    }

    public static boolean isManagedProjectAvatar(String photoUrl) {
        if (photoUrl == null || photoUrl.isBlank() || isDefaultAvatar(photoUrl)) {
            return false;
        }
        String normalized = photoUrl.trim();
        if (normalized.startsWith("http://") || normalized.startsWith("https://")
                || normalized.startsWith("file:") || normalized.startsWith("jar:")) {
            return false;
        }
        Path resolved = resolveStoredAvatarPath(normalized);
        return resolved.startsWith(getProjectAvatarDirectory());
    }

    private static Image loadJavaFxImage(String url) {
        try {
            Image image = new Image(url, false);
            return image.isError() ? null : image;
        } catch (Exception ex) {
            return null;
        }
    }
}
