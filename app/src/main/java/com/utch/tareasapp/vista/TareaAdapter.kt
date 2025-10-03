package com.utch.tareasapp.vista

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.utch.tareasapp.R
import com.utch.tareasapp.modelo.Tarea

/**
 * CLASE VISTA: TareaAdapter
 * Adapter moderno que usa RecyclerView y DiffUtil para mejor performance.
 * Utiliza características de Kotlin como funciones lambda y propiedades.
 * En el patrón MVC, esta es parte de la VISTA.
 */
class TareaAdapter : ListAdapter<Tarea, TareaAdapter.TareaViewHolder>(TareaDiffCallback()) {

    // ===== INTERFACES PARA COMUNICACIÓN CON ACTIVITY =====

    /**
     * Interface funcional para manejar clicks en las tareas
     */
    fun interface OnTareaClickListener {
        fun onTareaClick(tarea: Tarea)
    }

    /**
     * Interface funcional para manejar cambios en el checkbox
     */
    fun interface OnCompletadaChangeListener {
        fun onCompletadaChange(tareaId: Int)
    }

    /**
     * Interface funcional para manejar eliminación de tareas
     */
    fun interface OnEliminarTareaListener {
        fun onEliminarTarea(tareaId: Int)
    }

    // ===== PROPIEDADES DE LISTENERS =====
    var onTareaClickListener: OnTareaClickListener? = null
    var onCompletadaChangeListener: OnCompletadaChangeListener? = null
    var onEliminarTareaListener: OnEliminarTareaListener? = null

    // ===== MÉTODOS DEL ListAdapter =====

    /**
     * Crea un nuevo ViewHolder cuando RecyclerView lo necesita
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TareaViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tarea, parent, false)
        return TareaViewHolder(itemView)
    }

    /**
     * Vincula los datos de una tarea con un ViewHolder
     */
    override fun onBindViewHolder(holder: TareaViewHolder, position: Int) {
        val tarea = getItem(position)
        holder.bind(tarea)
    }

    // ===== CLASE ViewHolder =====

    /**
     * ViewHolder que contiene las vistas de cada elemento de la lista
     * Usa ViewBinding manual para mejor performance
     */
    inner class TareaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        // ===== VISTAS DEL ITEM =====
        private val cardView: MaterialCardView = itemView.findViewById(R.id.cardViewTarea)
        private val checkBox: CheckBox = itemView.findViewById(R.id.checkBoxCompletada)
        private val textTitulo: TextView = itemView.findViewById(R.id.textViewTitulo)
        private val textDescripcion: TextView = itemView.findViewById(R.id.textViewDescripcion)
        private val textId: TextView = itemView.findViewById(R.id.textViewId)
        private val textEstado: TextView = itemView.findViewById(R.id.textViewEstado)
        private val buttonEliminar: MaterialButton = itemView.findViewById(R.id.buttonEliminar)

        /**
         * Vincula una tarea con las vistas del ViewHolder
         */
        fun bind(tarea: Tarea) {
            // ===== CONFIGURAR DATOS BÁSICOS =====
            textTitulo.text = tarea.titulo
            textDescripcion.text = if (tarea.descripcion.isNotBlank()) {
                tarea.descripcion
            } else {
                "Sin descripción"
            }
            textId.text = "ID: ${tarea.id}"
            checkBox.isChecked = tarea.completada

            // ===== CONFIGURAR ESTADO VISUAL =====
            configurarEstadoVisual(tarea)

            // ===== CONFIGURAR EVENTOS =====
            configurarEventos(tarea)
        }

        /**
         * Configura el aspecto visual según el estado de la tarea
         */
        private fun configurarEstadoVisual(tarea: Tarea) {
            if (tarea.completada) {
                // ===== TAREA COMPLETADA =====
                // Texto tachado
                textTitulo.paintFlags = textTitulo.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                textDescripcion.paintFlags = textDescripcion.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

                // Colores para completada
                textTitulo.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.darker_gray))
                textDescripcion.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.darker_gray))

                // Estado visual
                textEstado.text = "✅ Completada"
                textEstado.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.holo_green_dark))

                // Card con menor elevación
                cardView.cardElevation = 1f
                cardView.alpha = 0.7f

            } else {
                // ===== TAREA PENDIENTE =====
                // Texto normal (sin tachado)
                textTitulo.paintFlags = textTitulo.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                textDescripcion.paintFlags = textDescripcion.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()

                // Colores normales
                textTitulo.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.black))
                textDescripcion.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.darker_gray))

                // Estado visual
                textEstado.text = "⏳ Pendiente"
                textEstado.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.holo_orange_dark))

                // Card con elevación normal
                cardView.cardElevation = 2f
                cardView.alpha = 1f
            }
        }

        /**
         * Configura todos los eventos de click e interacción
         */
        private fun configurarEventos(tarea: Tarea) {
            // ===== CLICK EN EL CARD COMPLETO =====
            cardView.setOnClickListener {
                onTareaClickListener?.onTareaClick(tarea)
            }

            // ===== CLICK EN EL CHECKBOX =====
            // Remover listener anterior para evitar loops
            checkBox.setOnCheckedChangeListener(null)
            // Configurar nuevo listener
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                // Solo procesar si el estado cambió realmente
                if (isChecked != tarea.completada) {
                    onCompletadaChangeListener?.onCompletadaChange(tarea.id)
                }
            }

            // ===== CLICK EN BOTÓN ELIMINAR =====
            buttonEliminar.setOnClickListener {
                onEliminarTareaListener?.onEliminarTarea(tarea.id)
            }

            // ===== LONG CLICK PARA ACCIONES ADICIONALES =====
            cardView.setOnLongClickListener {
                // Aquí podrías agregar un menú contextual o acción adicional
                true // Retorna true para indicar que el evento fue manejado
            }
        }
    }

    // ===== MÉTODOS DE UTILIDAD =====

    /**
     * Obtiene una tarea por su posición
     */
    fun getTareaAt(position: Int): Tarea? {
        return if (position in 0 until itemCount) {
            getItem(position)
        } else {
            null
        }
    }
}

// ===== CLASE PARA COMPARAR TAREAS (DiffUtil) =====

/**
 * DiffCallback para optimizar las actualizaciones del RecyclerView
 * Solo actualiza los elementos que realmente cambiaron
 */
class TareaDiffCallback : DiffUtil.ItemCallback<Tarea>() {

    /**
     * Verifica si son el mismo elemento (mismo ID)
     */
    override fun areItemsTheSame(oldItem: Tarea, newItem: Tarea): Boolean {
        return oldItem.id == newItem.id
    }

    /**
     * Verifica si el contenido es el mismo
     */
    override fun areContentsTheSame(oldItem: Tarea, newItem: Tarea): Boolean {
        return oldItem == newItem
    }

    /**
     * Obtiene el payload para actualización parcial (opcional)
     */
    override fun getChangePayload(oldItem: Tarea, newItem: Tarea): Any? {
        return when {
            oldItem.completada != newItem.completada -> "COMPLETADA_CHANGED"
            oldItem.titulo != newItem.titulo -> "TITULO_CHANGED"
            oldItem.descripcion != newItem.descripcion -> "DESCRIPCION_CHANGED"
            else -> null
        }
    }
}