package ioc.andresgq.gamehubmobile.data.repository

import ioc.andresgq.gamehubmobile.data.local.GameDao
import ioc.andresgq.gamehubmobile.data.local.GameEntity
import ioc.andresgq.gamehubmobile.data.local.GameLocalDataSource
import ioc.andresgq.gamehubmobile.data.remote.GameApi
import ioc.andresgq.gamehubmobile.data.remote.GameRemoteDataSource
import ioc.andresgq.gamehubmobile.data.remote.dto.CategoriaDto
import ioc.andresgq.gamehubmobile.data.remote.dto.GameDto
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class GameRepositoryTest {

    @Test
    fun getGames_remoteSuccess_updatesCacheAndReturnsRemote() = runTest {
        val remoteGames = listOf(gameDto(id = 1L, nombre = "Catan"))
        val fakeApi = FakeGameApi().apply { gamesResult = remoteGames }
        val fakeDao = FakeGameDao()
        val repository = GameRepository(
            gameRemoteDataSource = GameRemoteDataSource(fakeApi),
            gameLocalDataSource = GameLocalDataSource(fakeDao)
        )

        val result = repository.getGames()

        assertTrue(result.isSuccess)
        assertEquals(remoteGames, result.getOrNull())
        assertEquals(remoteGames.map { it.id }, fakeDao.storedGames.map { it.id })
    }

    @Test
    fun getAvailableGames_whenRemoteFails_returnsCachedGames() = runTest {
        val cachedGame = gameEntity(id = 2L, nombre = "Dixit", disponible = true)
        val fakeApi = FakeGameApi().apply { availableThrowable = IOException("offline") }
        val fakeDao = FakeGameDao().apply { storedGames = mutableListOf(cachedGame) }
        val repository = GameRepository(
            gameRemoteDataSource = GameRemoteDataSource(fakeApi),
            gameLocalDataSource = GameLocalDataSource(fakeDao)
        )

        val result = repository.getAvailableGames()

        assertTrue(result.isSuccess)
        val game = result.getOrNull().orEmpty().single()
        assertEquals(cachedGame.id, game.id)
        assertEquals(cachedGame.nombre, game.nombre)
        assertEquals(cachedGame.disponible, game.disponible)
    }

    @Test
    fun getGamesByCategory_blankCategory_returnsValidationError() = runTest {
        val repository = GameRepository(
            gameRemoteDataSource = GameRemoteDataSource(FakeGameApi()),
            gameLocalDataSource = GameLocalDataSource(FakeGameDao())
        )

        val result = repository.getGamesByCategory("   ")

        assertTrue(result.isFailure)
        assertEquals(
            "El nombre de la categoría no puede estar vacío",
            result.exceptionOrNull()?.message
        )
    }

    private class FakeGameApi : GameApi {
        var gamesResult: List<GameDto> = emptyList()
        var availableResult: List<GameDto> = emptyList()
        var getByIdResult: GameDto = GameDto(
            id = 99L,
            nombre = "Test",
            numJugadores = "2-4",
            categoria = CategoriaDto(id = 1L, nombre = "Estrategia"),
            disponible = true,
            descripcion = null,
            rutaImagen = null
        )
        var gamesByCategoryResult: List<GameDto> = emptyList()

        var gamesThrowable: Throwable? = null
        var availableThrowable: Throwable? = null
        var byCategoryThrowable: Throwable? = null
        var byIdThrowable: Throwable? = null

        override suspend fun getGames(): List<GameDto> {
            gamesThrowable?.let { throw it }
            return gamesResult
        }

        override suspend fun getGamesByCategory(categoryName: String): List<GameDto> {
            byCategoryThrowable?.let { throw it }
            return gamesByCategoryResult
        }

        override suspend fun getAvailableGames(): List<GameDto> {
            availableThrowable?.let { throw it }
            return availableResult
        }

        override suspend fun getGameById(id: Long): GameDto {
            byIdThrowable?.let { throw it }
            return getByIdResult
        }
    }

    private class FakeGameDao : GameDao {
        var storedGames: MutableList<GameEntity> = mutableListOf()

        override suspend fun getAll(): List<GameEntity> = storedGames.sortedBy { it.nombre }

        override suspend fun getByCategory(categoryName: String): List<GameEntity> =
            storedGames.filter { it.categoriaNombre == categoryName }.sortedBy { it.nombre }

        override suspend fun getAvailable(): List<GameEntity> =
            storedGames.filter { it.disponible }.sortedBy { it.nombre }

        override suspend fun getById(id: Long): GameEntity? = storedGames.firstOrNull { it.id == id }

        override suspend fun upsertAll(games: List<GameEntity>) {
            val byId = storedGames.associateBy { it.id }.toMutableMap()
            games.forEach { byId[it.id] = it }
            storedGames = byId.values.toMutableList()
        }

        override suspend fun clearAll() {
            storedGames.clear()
        }
    }

    private fun gameDto(
        id: Long,
        nombre: String,
        disponible: Boolean = true,
        categoria: String = "Estrategia"
    ): GameDto = GameDto(
        id = id,
        nombre = nombre,
        numJugadores = "2-4",
        categoria = CategoriaDto(id = 1L, nombre = categoria),
        disponible = disponible,
        descripcion = null,
        rutaImagen = null
    )

    private fun gameEntity(
        id: Long,
        nombre: String,
        disponible: Boolean,
        categoria: String = "Estrategia"
    ): GameEntity = GameEntity(
        id = id,
        nombre = nombre,
        numJugadores = "2-4",
        categoriaId = 1L,
        categoriaNombre = categoria,
        disponible = disponible,
        descripcion = null,
        rutaImagen = null
    )
}

