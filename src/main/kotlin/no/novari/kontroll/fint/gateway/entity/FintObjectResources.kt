package no.novari.kontroll.fint.gateway.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.core.type.TypeReference
import no.fint.model.resource.AbstractCollectionResources

class FintObjectResources : AbstractCollectionResources<Any>() {
    @JsonIgnore
    @Deprecated(
        message = "Deprecated in parent API",
        level = DeprecationLevel.WARNING,
    )
    override fun getTypeReference(): TypeReference<List<Any>> = object : TypeReference<List<Any>>() {}
}
