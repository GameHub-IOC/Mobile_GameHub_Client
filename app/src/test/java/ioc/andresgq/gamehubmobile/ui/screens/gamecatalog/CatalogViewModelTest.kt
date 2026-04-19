package ioc.andresgq.gamehubmobile.ui.screens.gamecatalog

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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CatalogViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun init_withRemoteGames_mapsAndPublishesSuccess() = runTest(mainDispatcherRule.scheduler) {
        val fakeApi = FakeGameApi().apply {
            gamesResult = listOf(
                gameDto(
                    id = 7L,
                    nombre = "Terraforming Mars",
                    categoria = "Estrategia",
                    disponible = true
                )
            )
        }
        val viewModel = createViewModel(fakeApi)

        advanceUntilIdle()

        val state = viewModel.catalogState.value
        assertTrue(state is UiState.Success)
        val game = (state as UiState.Success).data.single()
        assertEquals(7L, game.id)
        assertEquals("Terraforming Mars", game.nombre)
        assertEquals("Estrategia", game.categoria)
        assertEquals(1, fakeApi.getGamesCalls)
    }

    @Test
    fun loadCatalog_withoutForce_whenSuccess_doesNotReload() = runTest(mainDispatcherRule.scheduler) {
        val fakeApi = FakeGameApi().apply {
            gamesResult = listOf(gameDto(id = 1L, nombre = "Catan"))
        }
        val viewModel = createViewModel(fakeApi)
        advanceUntilIdle()

        viewModel.loadCatalog(force = false)
        advanceUntilIdle()

        assertEquals(1, fakeApi.getGamesCalls)
    }

    @Test
    fun loadCatalog_withForce_reloadsAndReplacesData() = runTest(mainDispatcherRule.scheduler) {
        val fakeApi = FakeGameApi().apply {
            gamesResult = listOf(gameDto(id = 1L, nombre = "Catan"))
        }
        val viewModel = createViewModel(fakeApi)
        advanceUntilIdle()

        fakeApi.gamesResult = listOf(gameDto(id = 2L, nombre = "Azul", categoria = "Familia"))
        viewModel.loadCatalog(force = true)
        advanceUntilIdle()

        assertEquals(2, fakeApi.getGamesCalls)
        val state = viewModel.catalogState.value as UiState.Success
        assertEquals(1, state.data.size)
        assertEquals("Azul", state.data.first().nombre)
        assertEquals("Familia", state.data.first().categoria)
    }

    private fun createViewModel(fakeApi: FakeGameApi): CatalogViewModel {
        val repository = GameRepository(
            gameRemoteDataSource = GameRemoteDataSource(fakeApi),
            gameLocalDataSource = GameLocalDataSource(FakeGameDao()),
            ioDispatcher = mainDispatcherRule.dispatcher
        )
        return CatalogViewModel(repository)
    }

    private class FakeGameApi : GameApi {
        var gamesResult: List<GameDto> = emptyList()
        var getGamesCalls: Int = 0

        override suspend fun getGames(): List<GameDto> {
            getGamesCalls += 1
            return gamesResult
        }

        override suspend fun getGamesByCategory(categoryName: String): List<GameDto> = emptyList()

        override suspend fun getAvailableGames(): List<GameDto> = emptyList()

        override suspend fun getGameById(id: Long): GameDto = GameDto(
            id = id,
            nombre = "Juego $id",
            numJugadores = "2-4",
            categoria = CategoriaDto(id = 1L, nombre = "Estrategia"),
            disponible = true,
            descripcion = null,
            observaciones = null,
            rutaImagen = null
        )
    }

    private class FakeGameDao : GameDao {
        override suspend fun getAll(): List<GameEntity> = emptyList()

        override suspend fun getByCategory(categoryName: String): List<GameEntity> = emptyList()

        override suspend fun getAvailable(): List<GameEntity> = emptyList()

        override suspend fun getById(id: Long): GameEntity? = null

        override suspend fun upsertAll(games: List<GameEntity>) = Unit

        override suspend fun clearAll() = Unit
    }

    private fun gameDto(
        id: Long,
        nombre: String,
        categoria: String = "Estrategia",
        disponible: Boolean = true
    ): GameDto = GameDto(
        id = id,
        nombre = nombre,
        numJugadores = "2-4",
        categoria = CategoriaDto(id = 1L, nombre = categoria),
        disponible = disponible,
        descripcion = null,
        observaciones = null,
        rutaImagen = null
    )
}

