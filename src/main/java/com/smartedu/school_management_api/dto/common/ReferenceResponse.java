package com.smartedu.school_management_api.dto.common;

/**
 * Flattened {id, name} stand-in for an associated entity.
 *
 * <p>Nested associations are exposed through this instead of the entity itself, which
 * is what keeps responses free of lazy-loading proxies and circular references.
 */
public record ReferenceResponse(Long id, String name) {

    public static ReferenceResponse of(Long id, String name) {
        return id == null ? null : new ReferenceResponse(id, name);
    }
}
