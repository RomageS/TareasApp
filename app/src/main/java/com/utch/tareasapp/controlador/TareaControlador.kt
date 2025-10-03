package com.utch.tareasapp.controlador

import com.utch.tareasapp.modelo.Tarea
import com.utch.tareasapp.modelo.TareaManager
import com.utch.tareasapp.vista.MainActivity

/**
 * CLASE CONTROLADOR: TareaControlador
 * Esta clase es el "intermediario" entre la Vista (pantalla) y el Modelo (datos).
 * Usa características modernas de Kotlin como funciones lambda y propiedades.
 * En el patrón MVC, esta es el CONTROLADOR.
 */
class TareaControlador(private val vista: MainActivity) {
    // ===== PROPIEDADES =====
    private val modelo = TareaManager()    // Instancia del administrador de datos

    // Callback para notificar cambios a la vista
    var onDataChanged: (() -> Unit)? = null

    // ===== MÉTODOS PÚBLICOS (API del Controlador) =====

    /**
     * OBTENER la lista de todas las tareas
     * @return lista inmutable de tareas
     */
    fun obtenerListaTareas(): List<Tarea> = modelo.obtenerTareas()

    /**
     * AGREGAR una nueva tarea con validación
     * @param titulo - Título que escribió el usuario
     * @param descripcion - Descripción que escribió el usuario (opcional)
     * @return resultado de la operación
     */
    fun agregarNuevaTarea(titulo: String, descripcion: String = ""): ResultadoOperacion {
        return when {
            titulo.isBlank() -> {
                ResultadoOperacion.Error("El título no puede estar vacío")
            }
            titulo.length > 100 -> {
                ResultadoOperacion.Error("El título es demasiado largo (máximo 100 caracteres)")
            }
            else -> {
                try {
                    val nuevaTarea = modelo.agregarTarea(titulo, descripcion)
                    notificarCambio()
                    ResultadoOperacion.Exitoso("Tarea agregada: ${nuevaTarea.titulo}")
                } catch (e: Exception) {
                    ResultadoOperacion.Error("Error al agregar tarea: ${e.message}")
                }
            }
        }
    }

    /**
     * CAMBIAR el estado de una tarea (completada/pendiente)
     * @param id - ID de la tarea que se quiere cambiar
     * @return resultado de la operación
     */
    fun alternarCompletada(id: Int): ResultadoOperacion {
        return if (modelo.alternarCompletada(id)) {
            val tarea = modelo.obtenerTareaPorId(id)
            val estado = if (tarea?.completada == true) "completada" else "pendiente"
            notificarCambio()
            ResultadoOperacion.Exitoso("Tarea marcada como $estado")
        } else {
            ResultadoOperacion.Error("No se encontró la tarea")
        }
    }

    /**
     * ELIMINAR una tarea
     * @param id - ID de la tarea que se quiere eliminar
     * @return resultado de la operación
     */
    fun eliminarTarea(id: Int): ResultadoOperacion {
        val tarea = modelo.obtenerTareaPorId(id)
        return if (modelo.eliminarTarea(id)) {
            notificarCambio()
            ResultadoOperacion.Exitoso("Tarea eliminada: ${tarea?.titulo ?: "Desconocida"}")
        } else {
            ResultadoOperacion.Error("No se pudo eliminar la tarea")
        }
    }

    /**
     * OBTENER los detalles de una tarea específica
     * @param id - ID de la tarea que queremos ver
     * @return la tarea con sus detalles, o null si no existe
     */
    fun obtenerDetalleTarea(id: Int): Tarea? = modelo.obtenerTareaPorId(id)

    /**
     * OBTENER estadísticas formateadas
     * @return string con estadísticas actuales
     */
    fun obtenerEstadisticas(): String = modelo.obtenerEstadisticas()

    /**
     * BUSCAR tareas por palabra clave
     * @param busqueda - texto a buscar
     * @return lista de tareas que coinciden con la búsqueda
     */
    fun buscarTareas(busqueda: String): List<Tarea> = modelo.buscarTareas(busqueda)

    /**
     * LIMPIAR todas las tareas completadas
     * @return resultado de la operación con cantidad eliminada
     */
    fun limpiarCompletadas(): ResultadoOperacion {
        val cantidad = modelo.limpiarCompletadas()
        return if (cantidad > 0) {
            notificarCambio()
            ResultadoOperacion.Exitoso("$cantidad tareas completadas eliminadas")
        } else {
            ResultadoOperacion.Info("No hay tareas completadas para eliminar")
        }
    }

    /**
     * OBTENER solo tareas completadas
     */
    fun obtenerTareasCompletadas(): List<Tarea> = modelo.obtenerTareasCompletadas()

    /**
     * OBTENER solo tareas pendientes
     */
    fun obtenerTareasPendientes(): List<Tarea> = modelo.obtenerTareasPendientes()

    /**
     * VERIFICAR si hay tareas
     */
    fun hayTareas(): Boolean = !modelo.estaVacia()

    // ===== MÉTODOS PRIVADOS =====

    /**
     * Notifica a la vista que los datos han cambiado
     */
    private fun notificarCambio() {
        vista.actualizarLista()
        onDataChanged?.invoke()
    }

    // ===== CLASE SELLADA para resultados de operaciones =====
    /**
     * Representa el resultado de una operación del controlador
     */
    sealed class ResultadoOperacion(val mensaje: String) {
        class Exitoso(mensaje: String) : ResultadoOperacion(mensaje)
        class Error(mensaje: String) : ResultadoOperacion(mensaje)
        class Info(mensaje: String) : ResultadoOperacion(mensaje)

        val esExitoso: Boolean get() = this is Exitoso
        val esError: Boolean get() = this is Error
    }
}