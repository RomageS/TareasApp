package com.utch.tareasapp.modelo

/**
 * CLASE MODELO: Tarea
 * Esta clase representa una tarea individual usando un data class de Kotlin.
 * Contiene todos los datos que necesita una tarea.
 * En el patrón MVC, esta es parte del MODELO.
 */
data class Tarea(
    // ===== PROPIEDADES (Variables que guardan la información) =====
    val id: Int,                          // Número único para identificar la tarea
    var titulo: String,                   // El nombre/título de la tarea
    var descripcion: String,              // Descripción detallada de la tarea
    var completada: Boolean = false,      // true = completada, false = pendiente
    val fechaCreacion: Long = System.currentTimeMillis() // Cuándo se creó la tarea
) {

    // ===== MÉTODOS ADICIONALES =====

    /**
     * Cambia el estado de completada (alterna entre true/false)
     */
    fun alternarCompletada() {
        completada = !completada
    }

    /**
     * Verifica si la tarea fue creada hoy
     * @return true si la tarea se creó hoy
     */
    fun esDeHoy(): Boolean {
        val hoy = System.currentTimeMillis()
        val unDia = 24 * 60 * 60 * 1000 // milisegundos en un día
        return (hoy - fechaCreacion) < unDia
    }

    /**
     * Obtiene un resumen de la tarea para mostrar en la lista
     * @return texto con formato para mostrar
     */
    fun getResumen(): String {
        val estado = if (completada) "✅" else "⏳"
        return "$estado $titulo"
    }

    /**
     * Verifica si el título contiene una palabra específica
     * @param palabra - palabra a buscar
     * @return true si contiene la palabra (sin importar mayúsculas/minúsculas)
     */
    fun contienePalabra(palabra: String): Boolean {
        return titulo.contains(palabra, ignoreCase = true) ||
                descripcion.contains(palabra, ignoreCase = true)
    }

    /**
     * Método toString personalizado para debug y logging
     * @return representación en string de la tarea
     */
    override fun toString(): String {
        return "Tarea(id=$id, titulo='$titulo', completada=$completada)"
    }
}