package com.ecommerce.backend.product.service.impl;

import com.ecommerce.backend.category.entity.Category;
import com.ecommerce.backend.category.exception.CategoryNotFoundException;
import com.ecommerce.backend.category.repository.CategoryRepository;
import com.ecommerce.backend.common.util.SlugUtil;
import com.ecommerce.backend.product.dto.common.ProductImagePayload;
import com.ecommerce.backend.product.dto.request.ProductFilterRequestPayload;
import com.ecommerce.backend.product.dto.request.ProductRequestPayload;
import com.ecommerce.backend.product.dto.request.ProductVariantRequestPayload;
import com.ecommerce.backend.product.dto.response.ProductResponsePayload;
import com.ecommerce.backend.product.entity.*;
import com.ecommerce.backend.product.enums.ProductStatus;
import com.ecommerce.backend.product.exception.DuplicateProductException;
import com.ecommerce.backend.product.exception.ProductColorNotFoundException;
import com.ecommerce.backend.product.exception.ProductNotFoundException;
import com.ecommerce.backend.product.exception.ProductSizeNotFoundException;
import com.ecommerce.backend.product.mapper.NewProductMapper;
import com.ecommerce.backend.product.mapper.ProductMapper;
import com.ecommerce.backend.product.repository.ProductColorRepository;
import com.ecommerce.backend.product.repository.ProductRepository;
import com.ecommerce.backend.product.repository.ProductSizeRepository;
import com.ecommerce.backend.product.service.ProductService;
import com.ecommerce.backend.product.specification.ProductSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

    private final SlugUtil slugUtil;
    private final ProductMapper productMapper;
    private final NewProductMapper newProductMapper;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductSizeRepository productSizeRepository;
    private final ProductColorRepository productColorRepository;

    @Override
    public Page<ProductResponsePayload> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable).map(productMapper::toDto);
    }

    @Override
    public Page<ProductResponsePayload> getAllActiveProducts(Pageable pageable) {
        return productRepository.findAllByStatus(ProductStatus.ACTIVE, pageable).map(productMapper::toDto);
    }

    @Override
    public Page<ProductResponsePayload> searchProducts(ProductFilterRequestPayload filter, Pageable pageable) {
        Specification<Product> spec = ProductSpecification.build(filter);
        return productRepository.findAll(spec, pageable).map(productMapper::toDto);
    }

    @Override
    public ProductResponsePayload getProductsBySlug(String slug) {
        Product product = productRepository.findBySlug(slug).orElseThrow(() -> new ProductNotFoundException("Product not found"));

        return  productMapper.toDto(product);
    }

    @Override
    public ProductResponsePayload createProduct(ProductRequestPayload requestPayload) {
        Category category = categoryRepository.findById(requestPayload.categoryId()).orElseThrow(() -> new CategoryNotFoundException("Category not found."));

        String slug = slugUtil.generate(requestPayload.name());
        validateSlug(slug, null);

        Product product = newProductMapper.toEntity(requestPayload, category, slug);
        buildVariants(product, requestPayload);
        Product savedProduct = productRepository.save(product);

        return productMapper.toDto(savedProduct);
    }

    @Override
    public ProductResponsePayload updateProduct(UUID id, ProductRequestPayload requestPayload) {
        Product product = productRepository.findById(id).orElseThrow(() ->new ProductNotFoundException("Product not found."));

        Category category = categoryRepository.findById(requestPayload.categoryId()).orElseThrow(() -> new CategoryNotFoundException("Category not found."));

        String slug = slugUtil.generate(requestPayload.name());
        validateSlug(slug, id);

        product.setName(requestPayload.name());
        product.setSlug(slug);
        product.setDescription(requestPayload.description());
        product.setBasePrice(requestPayload.basePrice());
        product.setCategory(category);
        product.getVariants().clear();
        buildVariants(product, requestPayload);
        Product updatedProduct = productRepository.save(product);

        return productMapper.toDto(updatedProduct);
    }

    @Override
    public void deleteProduct(UUID id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException("Product not found."));

        productRepository.delete(product);
    }

    private void buildVariants(
            Product product,
            ProductRequestPayload request
    ) {

        for (ProductVariantRequestPayload variantRequest : request.variants()) {

            ProductSize size = productSizeRepository.findById(variantRequest.sizeId()).orElseThrow(() -> new ProductSizeNotFoundException("Size not found."));
            ProductColor color = productColorRepository.findById(variantRequest.colorId())
                    .orElseThrow(() -> new ProductColorNotFoundException("Color not found."));

            ProductVariant variant = newProductMapper.toEntity(
                    variantRequest,
                    product,
                    size,
                    color
            );

            buildImages(
                    variant,
                    variantRequest
            );

            product.getVariants().add(variant);
        }
    }

    private void buildImages(
            ProductVariant variant,
            ProductVariantRequestPayload request
    ) {
        for (ProductImagePayload imageRequest : request.images()) {

            ProductImage image = newProductMapper.toEntity(
                    imageRequest,
                    variant
            );

            variant.getImages().add(image);

        }
    }

    private void validateSlug(String slug, UUID productId) {
        productRepository.findBySlug(slug)
                .filter(existing -> productId == null || !existing.getId().equals(productId))
                .ifPresent(existing -> {
                    throw new DuplicateProductException(
                            "A product with this name already exists."
                    );
                });

    }
}