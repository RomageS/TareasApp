package com.utch.tareasapp.vista

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.utch.tareasapp.R
import com.utch.tareasapp.controlador.TareaControlador
import com.utch.tareasapp.modelo.Tarea

/**
 * CLASE VISTA: MainActivity
 * Activity principal que implementa una interfaz moderna usando Kotlin.
 * Utiliza RecyclerView, Material Design y gestión moderna del estado.
 * En el patrón MVC, esta es la VISTA principal.
 */
class MainActivity : AppCompatActivity() {

    // ===== PROPIEDADES =====
    private lateinit var controlador: TareaControlador
    private lateinit var adapter: TareaAdapter

    // Views (sin ViewBinding para simplicidad en el tutorial)
    private lateinit var editTextTitulo: com.google.android.material.textfield.TextInputEditText
    private lateinit var buttonAgregar: com.google.android.material.button.MaterialButton
    private lateinit var buttonLimpiar: com.google.android.material.button.MaterialButton
    private lateinit var recyclerViewTareas: RecyclerView
    private lateinit var textViewEstadisticas: android.widget.TextView
    private lateinit var layoutVacio: android.widget.LinearLayout

    // ===== CICLO DE VIDA =====

    /**
     * Se ejecuta cuando se crea la Activity
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicialización en orden
        inicializarControlador()
        inicializarVistas()
        configurarRecyclerView()
        configurarEventos()
        actualizarInterfaz()

        // Mostrar mensaje de bienvenida
        mostrarMensaje("¡Bienvenido a tu Lista de Tareas!", TipoMensaje.INFO)
    }

    /**
     * Se ejecuta cuando la Activity se reanuda
     */
    override fun onResume() {
        super.onResume()
        actualizarInterfaz()
    }

    // ===== MÉTODOS DE INICIALIZACIÓN =====

    /**
     * Inicializa el controlador MVC
     */
    private fun inicializarControlador() {
        controlador = TareaControlador(this)

        // Configurar callback para cambios de datos
        controlador.onDataChanged = {
            actualizarEstadisticas()
        }
    }

    /**
     * Inicializa todas las vistas
     */
    private fun inicializarVistas() {
        editTextTitulo = findViewById(R.id.editTextTitulo)
        buttonAgregar = findViewById(R.id.buttonAgregar)
        buttonLimpiar = findViewById(R.id.buttonLimpiar)
        recyclerViewTareas = findViewById(R.id.recyclerViewTareas)
        textViewEstadisticas = findViewById(R.id.textViewEstadisticas)
        layoutVacio = findViewById(R.id.layoutVacio)
    }

    /**
     * Configura el RecyclerView y su adapter
     */
    private fun configurarRecyclerView() {
        // Crear y configurar adapter
        adapter = TareaAdapter().apply {
            // Configurar listeners usando SAM (Single Abstract Method)
            onTareaClickListener = TareaAdapter.OnTareaClickListener { tarea ->
                mostrarDetallesTarea(tarea)
            }

            onCompletadaChangeListener = TareaAdapter.OnCompletadaChangeListener { tareaId ->
                manejarCambioCompletada(tareaId)
            }

            onEliminarTareaListener = TareaAdapter.OnEliminarTareaListener { tareaId ->
                manejarEliminarTarea(tareaId)
            }
        }

        // Configurar RecyclerView
        recyclerViewTareas.apply {
            this.adapter = this@MainActivity.adapter
            layoutManager = LinearLayoutManager(this@MainActivity)
            setHasFixedSize(true)
        }

        // Agregar ItemTouchHelper para swipe to delete
        configurarSwipeToDelete()
    }

    /**
     * Configura gestos de deslizar para eliminar
     */
    private fun configurarSwipeToDelete() {
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val tarea = adapter.getTareaAt(position)
                tarea?.let {
                    manejarEliminarTarea(it.id)
                }
            }
        })

        itemTouchHelper.attachToRecyclerView(recyclerViewTareas)
    }

    /**
     * Configura todos los eventos de la interfaz
     */
    private fun configurarEventos() {
        // ===== BOTÓN AGREGAR =====
        buttonAgregar.setOnClickListener {
            manejarAgregarTarea()
        }

        // ===== BOTÓN LIMPIAR COMPLETADAS =====
        buttonLimpiar.setOnClickListener {
            manejarLimpiarCompletadas()
        }

        // ===== ENTER EN CAMPO DE TEXTO =====
        editTextTitulo.setOnEditorActionListener { _, _, _ ->
            manejarAgregarTarea()
            true
        }
    }

    // ===== MÉTODOS DE MANEJO DE EVENTOS =====

    /**
     * Maneja la adición de una nueva tarea
     */
    private fun manejarAgregarTarea() {
        val titulo = editTextTitulo.text?.toString()?.trim() ?: ""

        when (val resultado = controlador.agregarNuevaTarea(titulo)) {
            is TareaControlador.ResultadoOperacion.Exitoso -> {
                editTextTitulo.text?.clear()
                mostrarMensaje(resultado.mensaje, TipoMensaje.EXITO)
                actualizarInterfaz()
            }
            is TareaControlador.ResultadoOperacion.Error -> {
                mostrarMensaje(resultado.mensaje, TipoMensaje.ERROR)
            }
            is TareaControlador.ResultadoOperacion.Info -> {
                mostrarMensaje(resultado.mensaje, TipoMensaje.INFO)
            }
        }
    }

    /**
     * Maneja el cambio de estado completado/pendiente
     */
    private fun manejarCambioCompletada(tareaId: Int) {
        when (val resultado = controlador.alternarCompletada(tareaId)) {
            is TareaControlador.ResultadoOperacion.Exitoso -> {
                mostrarMensaje(resultado.mensaje, TipoMensaje.EXITO)
                actualizarInterfaz()
            }
            is TareaControlador.ResultadoOperacion.Error -> {
                mostrarMensaje(resultado.mensaje, TipoMensaje.ERROR)
                // Revertir cambio en la interfaz
                actualizarLista()
            }
            else -> {
                actualizarInterfaz()
            }
        }
    }

    /**
     * Maneja la eliminación de una tarea
     */
    private fun manejarEliminarTarea(tareaId: Int) {
        // Obtener datos de la tarea antes de eliminar para el undo
        val tarea = controlador.obtenerDetalleTarea(tareaId)

        when (val resultado = controlador.eliminarTarea(tareaId)) {
            is TareaControlador.ResultadoOperacion.Exitoso -> {
                actualizarInterfaz()

                // Mostrar Snackbar con opción de deshacer
                tarea?.let { tareaEliminada ->
                    mostrarSnackbarUndo(resultado.mensaje, tareaEliminada)
                }
            }
            is TareaControlador.ResultadoOperacion.Error -> {
                mostrarMensaje(resultado.mensaje, TipoMensaje.ERROR)
            }
            else -> {
                actualizarInterfaz()
            }
        }
    }

    /**
     * Maneja la limpieza de tareas completadas
     */
    private fun manejarLimpiarCompletadas() {
        when (val resultado = controlador.limpiarCompletadas()) {
            is TareaControlador.ResultadoOperacion.Exitoso -> {
                mostrarMensaje(resultado.mensaje, TipoMensaje.EXITO)
                actualizarInterfaz()
            }
            is TareaControlador.ResultadoOperacion.Info -> {
                mostrarMensaje(resultado.mensaje, TipoMensaje.INFO)
            }
            is TareaControlador.ResultadoOperacion.Error -> {
                mostrarMensaje(resultado.mensaje, TipoMensaje.ERROR)
            }
        }
    }

    /**
     * Muestra los detalles de una tarea en un Toast
     */
    private fun mostrarDetallesTarea(tarea: Tarea) {
        val detalles = buildString {
            appendLine("📋 ${tarea.titulo}")
            if (tarea.descripcion.isNotBlank()) {
                appendLine("📝 ${tarea.descripcion}")
            }
            appendLine("🆔 ID: ${tarea.id}")
            appendLine("📅 ${if (tarea.esDeHoy()) "Creada hoy" else "Creada anteriormente"}")
            append("✅ ${if (tarea.completada) "Completada" else "Pendiente"}")
        }

        Toast.makeText(this, detalles, Toast.LENGTH_LONG).show()
    }

    // ===== MÉTODOS PÚBLICOS (LLAMADOS DESDE EL CONTROLADOR) =====

    /**
     * Actualiza la lista de tareas (llamado desde el controlador)
     */
    fun actualizarLista() {
        val tareas = controlador.obtenerListaTareas()
        adapter.submitList(tareas.toList()) // Crear nueva lista para trigger DiffUtil
    }

    /**
     * Actualiza toda la interfaz
     */
    private fun actualizarInterfaz() {
        actualizarLista()
        actualizarEstadisticas()
        actualizarVisibilidadVistas()
    }

    /**
     * Actualiza las estadísticas mostradas
     */
    private fun actualizarEstadisticas() {
        textViewEstadisticas.text = controlador.obtenerEstadisticas()
    }

    /**
     * Actualiza la visibilidad de vistas según el estado
     */
    private fun actualizarVisibilidadVistas() {
        val hayTareas = controlador.hayTareas()

        recyclerViewTareas.visibility = if (hayTareas) View.VISIBLE else View.GONE
        layoutVacio.visibility = if (hayTareas) View.GONE else View.VISIBLE
        buttonLimpiar.isEnabled = controlador.obtenerTareasCompletadas().isNotEmpty()
    }

    // ===== MÉTODOS DE UTILIDAD =====

    /**
     * Muestra un mensaje usando diferentes métodos según el tipo
     */
    private fun mostrarMensaje(mensaje: String, tipo: TipoMensaje) {
        when (tipo) {
            TipoMensaje.EXITO -> {
                Toast.makeText(this, "✅ $mensaje", Toast.LENGTH_SHORT).show()
            }
            TipoMensaje.ERROR -> {
                Toast.makeText(this, "❌ $mensaje", Toast.LENGTH_LONG).show()
            }
            TipoMensaje.INFO -> {
                Toast.makeText(this, "ℹ️ $mensaje", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Muestra Snackbar con opción de deshacer
     */
    private fun mostrarSnackbarUndo(mensaje: String, tareaEliminada: Tarea) {
        Snackbar.make(findViewById(android.R.id.content), mensaje, Snackbar.LENGTH_LONG)
            .setAction("DESHACER") {
                // Funcionalidad para restaurar tarea (implementación básica)
                controlador.agregarNuevaTarea(tareaEliminada.titulo, tareaEliminada.descripcion)
                mostrarMensaje("Tarea restaurada", TipoMensaje.INFO)
            }
            .show()
    }

    // ===== ENUM PARA TIPOS DE MENSAJE =====

    /**
     * Enum que define los tipos de mensaje para mostrar
     */
    private enum class TipoMensaje {
        EXITO, ERROR, INFO
    }
}