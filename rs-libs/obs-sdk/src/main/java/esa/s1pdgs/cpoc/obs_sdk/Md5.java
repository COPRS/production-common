package esa.s1pdgs.cpoc.obs_sdk;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang.StringUtils;
import org.springframework.util.Assert;

public class Md5 {

    public static final String MD5SUM_SUFFIX = ".md5sum";


    public static String md5KeyFor(ObsObject object) {
        return md5KeyFor(object.getKey());
    }

    public static String md5KeyFor(String key) {
        return identifyMd5File(key);
    }

    public static Entry parse(String text) {
        return Entry.parse(text);
    }

    public static List<Entry> readFrom(InputStream in) throws IOException {
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            List<Entry> md5s = new ArrayList<>();
            String line;
            while((line = reader.readLine()) != null) {
                parseIfPossible(line).ifPresent(md5s::add);
            }
            return md5s;
        }
    }

    public static Optional<Entry> parseIfPossible(String text) {
        try {
            return Optional.of(parse(text));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private static String identifyMd5File(final String prefixKey) {
        int index = prefixKey.length();
        if (!(prefixKey.contains("raw") || prefixKey.contains("DSIB"))) {
            index = prefixKey.indexOf('/');
            if (index == -1) {
                index = prefixKey.length();
            }
        }
        return (prefixKey.substring(0, index) + MD5SUM_SUFFIX);
    }

    public static final class Entry {
        private final String md5Hash;
        private final String eTag;
        private final String fileName;

        public Entry(String md5Hash, String eTag, String fileName) {
            Assert.hasText(md5Hash, "md5Hash is empty");
            Assert.hasText(eTag, "eTag is empty");
            Assert.hasText(fileName, "fileName is empty");

            this.md5Hash = md5Hash.trim();
            this.eTag = eTag.trim();
            this.fileName = fileName.trim();
        }

        public static Entry parse(String text) {
            if (StringUtils.isEmpty(text)) {
                throw new IllegalArgumentException("empty string provided");
            }

            final String[] split = text.split("\\s+");

            //new format
            if (split.length == 3) {
                return new Entry(split[0], split[1], split[2]);
            }

            //old format for compatibility
            if (split.length == 2) {
                return new Entry(split[0], split[0], split[1]);
            }

            throw new IllegalArgumentException("wrong format: " + text);
        }

        public String getMd5Hash() {
            return md5Hash;
        }

        public String getETag() {
            return eTag;
        }

        public String getFileName() {
            return fileName;
        }

        @Override
        public String toString() {
            return String.format("%s %s %s", md5Hash, eTag, fileName);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Entry entry = (Entry) o;
            return Objects.equals(md5Hash, entry.md5Hash) &&
                    Objects.equals(eTag, entry.eTag) &&
                    Objects.equals(fileName, entry.fileName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(md5Hash, eTag, fileName);
        }
    }

}
