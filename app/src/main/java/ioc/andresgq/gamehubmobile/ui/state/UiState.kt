package ioc.andresgq.gamehubmobile.ui.state

/**
 * Representa el estado genérico de una operación de interfaz de usuario.
 *
 * Este tipo sellado se usa para modelar de forma explícita los estados más comunes
 * de una acción asíncrona o de carga de datos:
 * - [Idle]: estado inicial o en reposo.
 * - [Loading]: operación en curso.
 * - [Success]: operación completada correctamente con un resultado.
 * - [Error]: operación fallida con un mensaje descriptivo.
 *
 * El parámetro genérico [T] representa el tipo de dato que se devuelve cuando
 * la operación finaliza con éxito.
 */
sealed interface UiState<out T> {

    /**
     * Estado inicial o neutro.
     *
     * Indica que todavía no se ha iniciado ninguna acción, o que la UI ha vuelto
     * a un estado de espera sin carga ni error.
     */
    data object Idle : UiState<Nothing>

    /**
     * Estado de carga.
     *
     * Señala que la operación asociada sigue en progreso y que la interfaz puede
     * mostrar un indicador visual, como un spinner o una barra de progreso.
     */
    data object Loading : UiState<Nothing>

    /**
     * Estado de éxito.
     *
     * Contiene el resultado de la operación cuando esta termina correctamente.
     *
     * @param data valor devuelto por la operación completada con éxito.
     */
    data class Success<T>(val data: T) : UiState<T>

    /**
     * Estado de error.
     *
     * Se usa cuando la operación falla y la UI necesita mostrar información
     * legible sobre el problema ocurrido.
     *
     * @param message mensaje descriptivo del error.
     */
    data class Error(val message: String) : UiState<Nothing>
}