package com.ecommerce.backend.product.mapper;

import com.ecommerce.backend.category.entity.Category;
import com.ecommerce.backend.product.dto.common.ProductImagePayload;
import com.ecommerce.backend.product.dto.request.ProductRequestPayload;
import com.ecommerce.backend.product.dto.request.ProductVariantRequestPayload;
import com.ecommerce.backend.product.entity.*;
import org.springframework.stereotype.Component;

@Component
public class NewProductMapper {

    public Product toEntity(
            ProductRequestPayload request,
            Category category,
            String slug
    ) {

        return Product.builder()
                .name(request.name())
                .slug(slug)
                .description(request.description())
                .basePrice(request.basePrice())
                .category(category)
                .build();

    }

    public ProductVariant toEntity(
            ProductVariantRequestPayload request,
            Product product,
            ProductSize size,
            ProductColor color
    ) {

        return ProductVariant.builder()
                .product(product)
                .sku(request.sku())
                .priceOverride(request.priceOverride())
                .stockQuantity(request.stockQuantity())
                .size(size)
                .color(color)
                .build();

    }

    public ProductImage toEntity(
            ProductImagePayload request,
            ProductVariant variant
    ) {

        return ProductImage.builder()
                .productVariant(variant)
                .imageUrl(request.imageUrl())
                .altText(request.altText())
                .displayOrder(request.displayOrder())
                .thumbnail(request.thumbnail())
                .build();

    }
}
