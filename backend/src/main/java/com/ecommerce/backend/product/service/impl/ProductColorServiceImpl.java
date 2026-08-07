package com.ecommerce.backend.product.service.impl;

import com.ecommerce.backend.product.dto.request.ProductColorRequestPayload;
import com.ecommerce.backend.product.dto.response.ProductColorResponsePayload;
import com.ecommerce.backend.product.entity.ProductColor;
import com.ecommerce.backend.product.exception.ProductColorAlreadyExistsException;
import com.ecommerce.backend.product.exception.ProductColorNotFoundException;
import com.ecommerce.backend.product.mapper.ProductColorMapper;
import com.ecommerce.backend.product.repository.ProductColorRepository;
import com.ecommerce.backend.product.service.ProductColorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductColorServiceImpl implements ProductColorService {

    private final ProductColorMapper productColorMapper;
    private final ProductColorRepository productColorRepository;

    @Override
    public List<ProductColorResponsePayload> getAllColors() {

        List<ProductColor> colors = productColorRepository.findAll();

        return productColorMapper.toDto(colors);
    }

    @Override
    public ProductColorResponsePayload getColorById(UUID id) {
        ProductColor color = productColorRepository.findById(id).orElseThrow(() -> new ProductColorNotFoundException("Product color not found."));

        return productColorMapper.toDto(color);
    }


    @Override
    public ProductColorResponsePayload createColor(ProductColorRequestPayload requestPayload) {
        validateName(requestPayload.name(), null);

        ProductColor color = productColorMapper.toEntity(requestPayload);
        ProductColor savedColor = productColorRepository.save(color);

        return productColorMapper.toDto(savedColor);
    }

    @Override
    public ProductColorResponsePayload updateColor(UUID id, ProductColorRequestPayload requestPayload) {
        ProductColor color = productColorRepository.findById(id).orElseThrow(() -> new ProductColorNotFoundException("Product color not found."));

        validateName(requestPayload.name(), id);

        color.setName(requestPayload.name());
        color.setHexCode(requestPayload.hexCode());
        ProductColor updatedColor = productColorRepository.save(color);

        return productColorMapper.toDto(updatedColor);
    }

    @Override
    public void deleteColor(UUID id) {
        ProductColor color = productColorRepository.findById(id).orElseThrow(() -> new ProductColorNotFoundException("Product color not found."));

        productColorRepository.delete(color);
    }


    private void validateName(
            String name,
            UUID colorId
    ) {

        ProductColor existingColor = productColorRepository.findByName(name).orElse(null);

        if (existingColor != null) {
            if (colorId == null || !existingColor.getId().equals(colorId)) {
                throw new ProductColorAlreadyExistsException("Product color already exists.");
            }
        }
    }
}
