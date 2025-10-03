package com.utch.tareasapp.modelo

/**
 * CLASE MODELO: TareaManager
 * Esta clase administra TODAS las tareas de la aplicación usando características modernas de Kotlin.
 * Es como un "gerente" que se encarga de guardar, buscar, agregar y eliminar tareas.
 * En el patrón MVC, esta es parte del MODELO (maneja los datos).
 */
class TareaManager {
    // ===== PROPIEDADES =====
    private val _tareas = mutableListOf<Tarea>()    // Lista mutable privada
    val tareas: List<Tarea> get() = _tareas         // Propiedad pública de solo lectura
    private var proximoId = 1                       // Contador para IDs únicos

    // ===== BLOQUE INIT (Constructor) =====
    /**
     * Se ejecuta cuando se crea una nueva instancia de TareaManager
     * Inicializa con algunas tareas de ejemplo
     */
    init {
        // Agregar tareas de ejemplo para que la app no esté vacía al inicio
        agregarTarea("Estudiar MVC", "Revisar conceptos de Modelo Vista Controlador")
        agregarTarea("Hacer ejercicio", "Rutina de 30 minutos de cardio")
        agregarTarea("Completar proyecto", "Terminar la evidencia de Android Studio")
    }

    // ===== MÉTODOS PÚBLICOS =====

    /**
     * AGREGAR una nueva tarea a la lista
     * @param titulo - Título de la nueva tarea
     * @param descripcion - Descripción de la nueva tarea
     * @return la nueva tarea creada
     */
    fun agregarTarea(titulo: String, descripcion: String = ""): Tarea {
        // Crear nueva tarea con el próximo ID disponible
        val nuevaTarea = Tarea(
            id = proximoId++,
            titulo = titulo.trim(),
            descripcion = descripcion.trim()
        )

        // Agregar a la lista
        _tareas.add(nuevaTarea)

        return nuevaTarea
    }

    /**
     * OBTENER todas las tareas (propiedad de solo lectura)
     * @return lista inmutable de todas las tareas
     */
    fun obtenerTareas(): List<Tarea> = tareas

    /**
     * ELIMINAR una tarea por su ID usando función de extensión de Kotlin
     * @param id - ID de la tarea que queremos eliminar
     * @return true si se eliminó, false si no se encontró
     */
    fun eliminarTarea(id: Int): Boolean {
        return _tareas.removeIf { it.id == id }
    }

    /**
     * CAMBIAR el estado de completada de una tarea
     * @param id - ID de la tarea que queremos cambiar
     * @return true si se encontró y cambió, false si no existe
     */
    fun alternarCompletada(id: Int): Boolean {
        // Usar find() de Kotlin para buscar la tarea
        val tarea = _tareas.find { it.id == id }
        return if (tarea != null) {
            tarea.alternarCompletada()
            true
        } else {
            false
        }
    }

    /**
     * BUSCAR una tarea específica por su ID usando función find()
     * @param id - ID de la tarea que estamos buscando
     * @return la tarea encontrada, o null si no existe
     */
    fun obtenerTareaPorId(id: Int): Tarea? {
        return _tareas.find { it.id == id }
    }

    /**
     * OBTENER el número total de tareas usando property
     */
    val totalTareas: Int
        get() = _tareas.size

    /**
     * OBTENER el número de tareas completadas usando función count()
     */
    val tareasCompletadas: Int
        get() = _tareas.count { it.completada }

    /**
     * OBTENER el número de tareas pendientes
     */
    val tareasPendientes: Int
        get() = _tareas.count { !it.completada }

    /**
     * BUSCAR tareas que contengan una palabra específica
     * @param palabra - palabra a buscar
     * @return lista de tareas que contienen la palabra
     */
    fun buscarTareas(palabra: String): List<Tarea> {
        return if (palabra.isBlank()) {
            tareas
        } else {
            _tareas.filter { it.contienePalabra(palabra) }
        }
    }

    /**
     * OBTENER solo las tareas completadas
     * @return lista de tareas completadas
     */
    fun obtenerTareasCompletadas(): List<Tarea> {
        return _tareas.filter { it.completada }
    }

    /**
     * OBTENER solo las tareas pendientes
     * @return lista de tareas pendientes
     */
    fun obtenerTareasPendientes(): List<Tarea> {
        return _tareas.filter { !it.completada }
    }

    /**
     * LIMPIAR todas las tareas completadas
     * @return número de tareas eliminadas
     */
    fun limpiarCompletadas(): Int {
        val cantidadAntes = _tareas.size
        _tareas.removeIf { it.completada }
        return cantidadAntes - _tareas.size
    }

    /**
     * OBTENER estadísticas como string formateado
     * @return texto con estadísticas
     */
    fun obtenerEstadisticas(): String {
        return "Total: $totalTareas | Completadas: $tareasCompletadas | Pendientes: $tareasPendientes"
    }

    /**
     * VERIFICAR si hay tareas
     * @return true si no hay tareas
     */
    fun estaVacia(): Boolean = _tareas.isEmpty()

    /**
     * OBTENER la tarea más reciente
     * @return la última tarea agregada o null si no hay tareas
     */
    fun obtenerTareaMasReciente(): Tarea? {
        return _tareas.maxByOrNull { it.fechaCreacion }
    }
}