package com.smartedu.school_management_api.service;

import com.smartedu.school_management_api.config.StorageProperties;
import com.smartedu.school_management_api.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Stores profile pictures on disk.
 *
 * <p>The file type is decided by the first few bytes of the upload, not by the declared
 * content type or the file name: both come from the client and neither is evidence of
 * anything. A file that does not begin with a JPEG, PNG or WebP signature is refused, so
 * a script renamed to {@code .jpg} never lands in a directory the server publishes.
 *
 * <p>One image per user, named after the user id. Re-uploading replaces the previous
 * file rather than accumulating, and the extension is derived from the detected type so
 * the name always matches the actual contents.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AvatarStorageService {

    /** Public URL prefix; {@code WebConfig} maps this to the storage directory. */
    public static final String PUBLIC_PREFIX = "/uploads/avatars/";

    private static final String AVATAR_SUBDIR = "avatars";

    private final StorageProperties properties;

    /**
     * Validates and writes {@code file}, replacing any existing avatar for this user.
     *
     * @return the public path to store on the user, e.g. {@code /uploads/avatars/<id>.jpg}
     */
    public String store(UUID userId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Please choose an image to upload");
        }
        if (file.getSize() > properties.getMaxAvatarBytes()) {
            throw new BadRequestException(
                    "Image must be " + (properties.getMaxAvatarBytes() / (1024 * 1024)) + " MB or smaller");
        }

        String extension = detectExtension(file);

        try {
            Path directory = avatarDirectory();
            Files.createDirectories(directory);

            // Remove the previous image first: the new one may have a different
            // extension, which would otherwise leave two files for the same user.
            deleteExisting(userId);

            Path target = directory.resolve(userId + "." + extension);
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }
            return PUBLIC_PREFIX + target.getFileName();
        } catch (IOException e) {
            log.error("Failed to store avatar for user {}", userId, e);
            throw new BadRequestException("Could not save the image. Please try again.");
        }
    }

    /** Removes a user's avatar if there is one. Silent when there is not. */
    public void delete(UUID userId) {
        try {
            deleteExisting(userId);
        } catch (IOException e) {
            // The account edit still succeeded; a leftover file is not worth failing on.
            log.warn("Could not delete avatar for user {}", userId, e);
        }
    }

    private Path avatarDirectory() {
        return Paths.get(properties.getUploadDir(), AVATAR_SUBDIR).toAbsolutePath().normalize();
    }

    /** Deletes every stored extension for this user, not just the one we expect. */
    private void deleteExisting(UUID userId) throws IOException {
        Path directory = avatarDirectory();
        if (!Files.isDirectory(directory)) {
            return;
        }
        try (DirectoryStream<Path> matches = Files.newDirectoryStream(directory, userId + ".*")) {
            for (Path existing : matches) {
                Files.deleteIfExists(existing);
            }
        }
    }

    /**
     * Identifies the image from its leading bytes.
     *
     * <p>Checked against the content rather than the supplied name or content type,
     * either of which a client can set to anything.
     */
    private String detectExtension(MultipartFile file) {
        byte[] head = new byte[12];
        try (InputStream in = file.getInputStream()) {
            if (in.readNBytes(head, 0, head.length) < head.length) {
                throw new BadRequestException("That file is not a valid image");
            }
        } catch (IOException e) {
            throw new BadRequestException("Could not read the uploaded file");
        }

        if ((head[0] & 0xFF) == 0xFF && (head[1] & 0xFF) == 0xD8) {
            return "jpg";
        }
        if ((head[0] & 0xFF) == 0x89 && head[1] == 'P' && head[2] == 'N' && head[3] == 'G') {
            return "png";
        }
        // WebP is a RIFF container: "RIFF" then four size bytes then "WEBP".
        if (head[0] == 'R' && head[1] == 'I' && head[2] == 'F' && head[3] == 'F'
                && head[8] == 'W' && head[9] == 'E' && head[10] == 'B' && head[11] == 'P') {
            return "webp";
        }
        throw new BadRequestException("Image must be a JPG, PNG or WebP file");
    }
}
