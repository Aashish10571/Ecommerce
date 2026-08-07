package com.ecommerce.backend.product.service.impl;

import com.ecommerce.backend.product.dto.request.ProductSizeRequestPayload;
import com.ecommerce.backend.product.dto.response.ProductSizeResponsePayload;
import com.ecommerce.backend.product.entity.ProductSize;
import com.ecommerce.backend.product.exception.ProductSizeAlreadyExistsException;
import com.ecommerce.backend.product.exception.ProductSizeNotFoundException;
import com.ecommerce.backend.product.mapper.ProductSizeMapper;
import com.ecommerce.backend.product.repository.ProductSizeRepository;
import com.ecommerce.backend.product.service.ProductSizeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductSizeServiceImpl implements ProductSizeService {

    private final ProductSizeMapper productSizeMapper;
    private final ProductSizeRepository productSizeRepository;

    @Override
    public List<ProductSizeResponsePayload> getAllSizes() {

        List<ProductSize> sizes = productSizeRepository.findAllByOrderBySortOrderAsc();

        return productSizeMapper.toDto(sizes);
    }

    @Override
    public ProductSizeResponsePayload getSizeById(UUID id) {
        ProductSize size = productSizeRepository.findById(id).orElseThrow(() -> new ProductSizeNotFoundException("Product size not found."));

        return productSizeMapper.toDto(size);
    }

    @Override
    public ProductSizeResponsePayload createSize(
            ProductSizeRequestPayload requestPayload
    ) {

        validateLabel(
                requestPayload.label(),
                null
        );

        ProductSize size = productSizeMapper.toEntity(requestPayload);
        ProductSize savedSize = productSizeRepository.save(size);

        return productSizeMapper.toDto(savedSize);
    }

    @Override
    public ProductSizeResponsePayload updateSize(
            UUID id,
            ProductSizeRequestPayload requestPayload
    ) {

        ProductSize size = productSizeRepository.findById(id)
                .orElseThrow(() ->
                        new ProductSizeNotFoundException("Product size not found."));

        validateLabel(
                requestPayload.label(),
                id
        );

        size.setLabel(requestPayload.label());
        size.setSortOrder(requestPayload.sortOrder());

        ProductSize updatedSize = productSizeRepository.save(size);

        return productSizeMapper.toDto(updatedSize);
    }

    @Override
    public void deleteSize(UUID id) {
        ProductSize size = productSizeRepository.findById(id).orElseThrow(() -> new ProductSizeNotFoundException("Product size not found."));

        productSizeRepository.delete(size);
    }

    private void validateLabel(
            String label,
            UUID sizeId
    ) {
        ProductSize existingSize = productSizeRepository.findByLabel(label).orElse(null);

        if (existingSize != null) {
            if (sizeId == null || !existingSize.getId().equals(sizeId)) {
                throw new ProductSizeAlreadyExistsException("Product size already exists.");
            }
        }
    }
}
