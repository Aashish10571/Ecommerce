package com.ecommerce.backend.common.util;

import com.github.slugify.Slugify;
import org.springframework.stereotype.Component;

@Component
public class SlugUtil {

    private final Slugify slugify;

    public SlugUtil() {
        this.slugify = Slugify.builder().build();
    }

    public String generate(String value) {
        return slugify.slugify(value);
    }
}