package com.sporekart.modules.grower.web.dto;

import java.util.List;

/**
 * Request body for replacing a product's full ordered image set.
 * An empty list clears all images.
 */
public record UpdateProductImagesRequestDto(
        List<String> imageUrls
) {
}