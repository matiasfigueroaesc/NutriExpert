package cl.duoc.nutriexpert.data.remote

import cl.duoc.nutriexpert.data.local.PredioEntity
import cl.duoc.nutriexpert.data.local.SectorEntity

// Traductores entre "lo que guarda el teléfono" (Entity) y "lo que viaja por internet" (Dto).
// Es como pasar una receta de la libreta de la casa al formulario oficial: mismos datos,
// otros nombres. Ej: en Room se llama "hectareas" y en el JSON "superficieHa".

fun PredioEntity.aDto(): PredioDto = PredioDto(
    id = remoteId,
    usuarioId = usuarioId.toLong(),
    nombre = nombre,
    comuna = comuna,
    latitud = latitud,
    longitud = longitud,
    superficieHa = hectareas
)

// Un predio que llegó del servidor ya está sincronizado
fun PredioDto.aEntity(usuarioId: Int): PredioEntity = PredioEntity(
    usuarioId = usuarioId,
    nombre = nombre,
    comuna = comuna,
    hectareas = superficieHa,
    latitud = latitud,
    longitud = longitud,
    remoteId = id,
    sincronizado = true
)

// predioRemoteId: el servidor no conoce los ids de Room, solo los suyos
fun SectorEntity.aDto(predioRemoteId: Long): SectorDto = SectorDto(
    id = remoteId,
    predioId = predioRemoteId,
    nombre = nombre,
    cultivo = cultivoId,
    superficieHa = hectareas
)

fun SectorDto.aEntity(predioLocalId: Int): SectorEntity = SectorEntity(
    predioId = predioLocalId,
    nombre = nombre,
    cultivoId = cultivo,
    hectareas = superficieHa,
    remoteId = id,
    sincronizado = true
)
