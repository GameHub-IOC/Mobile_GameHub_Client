package ioc.andresgq.gamehubmobile.ui.screens.gamedetail

import ioc.andresgq.gamehubmobile.data.local.GameDao
import ioc.andresgq.gamehubmobile.data.local.GameEntity
import ioc.andresgq.gamehubmobile.data.local.GameLocalDataSource
import ioc.andresgq.gamehubmobile.data.remote.GameApi
import ioc.andresgq.gamehubmobile.data.remote.GameRemoteDataSource
import ioc.andresgq.gamehubmobile.data.remote.dto.CategoriaDto
import ioc.andresgq.gamehubmobile.data.remote.dto.GameDto
import ioc.andresgq.gamehubmobile.data.repository.GameRepository
import ioc.andresgq.gamehubmobile.testutil.MainDispatcherRule
import ioc.andresgq.gamehubmobile.ui.state.UiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class GameDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun init_withExistingGame_emitsSuccess() = runTest(mainDispatcherRule.scheduler) {
        val fakeApi = FakeGameApi().apply {
            gameById = gameDto(id = 1L, nombre = "Catan")
        }
        val viewModel = createViewModel(fakeApi, gameId = 1L)

        advanceUntilIdle()

        val state = viewModel.gameState.value
        assertTrue(state is UiState.Success)
        assertEquals("Catan", (state as UiState.Success).data.nombre)
    }

    @Test
    fun init_whenGameNotFound_emitsError() = runTest(mainDispatcherRule.scheduler) {
        val fakeApi = FakeGameApi().apply {
            throwable = httpException(404)
        }
        val viewModel = createViewModel(fakeApi, gameId = 99L)

        advanceUntilIdle()

        val state = viewModel.gameState.value
        assertTrue(state is UiState.Error)
        assertEquals("Juego no encontrado", (state as UiState.Error).message)
    }

    private fun createViewModel(fakeApi: FakeGameApi, gameId: Long): GameDetailViewModel {
        val repository = GameRepository(
            gameRemoteDataSource = GameRemoteDataSource(fakeApi),
            gameLocalDataSource = GameLocalDataSource(FakeGameDao()),
            ioDispatcher = mainDispatcherRule.dispatcher
        )
        return GameDetailViewModel(
            gameRepository = repository,
            gameId = gameId
        )
    }

    private fun gameDto(id: Long, nombre: String): GameDto = GameDto(
        id = id,
        nombre = nombre,
        numJugadores = "2-4",
        categoria = CategoriaDto(id = 1L, nombre = "Estrategia"),
        disponible = true,
        descripcion = null,
        rutaImagen = null
    )

    private fun httpException(code: Int): HttpException {
        val response = Response.error<Any>(code, "error".toResponseBody())
        return HttpException(response)
    }

    private class FakeGameApi : GameApi {
        var gameById: GameDto = GameDto(
            id = 1L,
            nombre = "Default",
            numJugadores = "2-4",
            categoria = CategoriaDto(1L, "Estrategia"),
            disponible = true,
            descripcion = null,
            rutaImagen = null
        )
        var throwable: Throwable? = null

        override suspend fun getGames(): List<GameDto> = emptyList()

        override suspend fun getGamesByCategory(categoryName: String): List<GameDto> = emptyList()

        override suspend fun getAvailableGames(): List<GameDto> = emptyList()

        override suspend fun getGameById(id: Long): GameDto {
            throwable?.let { throw it }
            return gameById
        }
    }

    private class FakeGameDao : GameDao {
        override suspend fun getAll(): List<GameEntity> = emptyList()

        override suspend fun getByCategory(categoryName: String): List<GameEntity> = emptyList()

        override suspend fun getAvailable(): List<GameEntity> = emptyList()

        override suspend fun getById(id: Long): GameEntity? = null

        override suspend fun upsertAll(games: List<GameEntity>) = Unit

        override suspend fun clearAll() = Unit
    }
}

