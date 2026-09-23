package ai.interviewhq.api.hashing;

import ai.interviewhq.api.domain.QuestionType;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;

/**
 * Centralized, deterministic identity hashing. Question ids, page ids, and
 * experience ids are all SHA-256 of a normalized identity string.
 */
public final class Hashes {

    private Hashes() {
    }

    public static String questionId(String company, String questionText, QuestionType questionType) {
        String identity = normalize(company) + '\n' + normalize(questionText) + '\n'
                + (questionType == null ? "" : normalize(questionType.canonical()));
        return sha256Hex(identity);
    }

    public static String urlHash(String url) {
        return sha256Hex(canonicalizeUrl(url));
    }

    public static String experienceId(String sourceUrl) {
        return urlHash(sourceUrl);
    }

    public static String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest((value == null ? "" : value).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    public static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
    }

    public static String canonicalizeUrl(String url) {
        if (url == null || url.isBlank()) {
            return "";
        }
        String trimmed = url.trim();
        try {
            URI uri = URI.create(trimmed);
            String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
            String host = uri.getHost() == null ? "" : uri.getHost().toLowerCase(Locale.ROOT);
            int port = uri.getPort();
            String path = uri.getPath() == null ? "" : uri.getPath();
            if (path.endsWith("/") && path.length() > 1) {
                path = path.substring(0, path.length() - 1);
            }
            String query = uri.getQuery() == null ? "" : "?" + uri.getQuery();
            String portPart = port > 0 ? ":" + port : "";
            if (host.isEmpty()) {
                return trimmed.toLowerCase(Locale.ROOT);
            }
            return scheme + "://" + host + portPart + path + query;
        } catch (IllegalArgumentException ex) {
            String noFragment = trimmed.split("#", 2)[0];
            if (noFragment.endsWith("/")) {
                noFragment = noFragment.substring(0, noFragment.length() - 1);
            }
            return noFragment.toLowerCase(Locale.ROOT);
        }
    }
}
