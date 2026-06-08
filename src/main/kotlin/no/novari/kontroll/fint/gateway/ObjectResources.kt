package no.novari.no.novari.kontroll.fint.gateway

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.core.type.TypeReference
import no.fint.model.resource.AbstractCollectionResources

class ObjectResources : AbstractCollectionResources<Any>() {
    @JsonIgnore
    @Deprecated(
        message = "Deprecated in parent API",
        level = DeprecationLevel.WARNING,
    )
    override fun getTypeReference(): TypeReference<List<Any>> = object : TypeReference<List<Any>>() {}
}
