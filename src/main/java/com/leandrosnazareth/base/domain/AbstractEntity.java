package com.leandrosnazareth.base.domain;

import jakarta.persistence.MappedSuperclass;
import org.jspecify.annotations.Nullable;
import org.springframework.data.util.ProxyUtils;

@MappedSuperclass
public abstract class AbstractEntity<ID> {

    public abstract @Nullable ID getId();

    @Override
    public String toString() {
        return "%s{id=%s}".formatted(getClass().getSimpleName(), getId());
    }

    @Override
    public int hashCode() {
        return ProxyUtils.getUserClass(getClass()).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if (obj == this) {
            return true;
        }

        var thisUserClass = ProxyUtils.getUserClass(getClass());
        var otherUserClass = ProxyUtils.getUserClass(obj);
        if (thisUserClass != otherUserClass) {
            return false;
        }

        var id = getId();
        return id != null && id.equals(((AbstractEntity<?>) obj).getId());
    }

}
