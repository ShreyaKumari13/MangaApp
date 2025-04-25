package com.example.androiddevelopmenttask.data.api

import com.example.androiddevelopmenttask.data.api.model.ChapterDto
import com.example.androiddevelopmenttask.data.api.model.ChapterImagesResponse
import com.example.androiddevelopmenttask.data.api.model.ChapterListResponse
import com.example.androiddevelopmenttask.data.api.model.MangaDto
import com.example.androiddevelopmenttask.data.api.model.MangaListResponse
import kotlinx.coroutines.delay
import javax.inject.Inject

/**
 * Mock implementation of MangaApiService for demo purposes
 */
class MockMangaApiService @Inject constructor() : MangaApiService {

    private val mockMangaList = listOf(
        MangaDto(
            idString = "1",
            title = "One Piece",
            subTitle = "The Great Pirate Era",
            description = "Follows the adventures of Monkey D. Luffy and his pirate crew in order to find the greatest treasure ever left by the legendary Pirate, Gold Roger. The famous mystery treasure named \"One Piece\".",
            coverImage = "https://m.media-amazon.com/images/I/51FXs5gTmdL._SY445_SX342_.jpg",
            authors = listOf("Eiichiro Oda"),
            genres = listOf("Action", "Adventure", "Comedy", "Fantasy"),
            chapters = 1050,
            status = "Ongoing",
            nsfw = false,
            type = "japan",
            createdAt = System.currentTimeMillis() - 365 * 24 * 60 * 60 * 1000L, // 1 year ago
            updatedAt = System.currentTimeMillis()
        ),
        MangaDto(
            idString = "2",
            title = "Naruto",
            subTitle = "",
            description = "Naruto Uzumaki, a mischievous adolescent ninja, struggles as he searches for recognition and dreams of becoming the Hokage, the village's leader and strongest ninja.",
            coverImage = "https://m.media-amazon.com/images/I/71QYLrc-IQL._SY522_.jpg",
            authors = listOf("Masashi Kishimoto"),
            genres = listOf("Action", "Adventure", "Fantasy"),
            chapters = 700,
            status = "Completed",
            nsfw = false,
            type = "japan",
            createdAt = System.currentTimeMillis() - 730 * 24 * 60 * 60 * 1000L, // 2 years ago
            updatedAt = System.currentTimeMillis() - 180 * 24 * 60 * 60 * 1000L // 6 months ago
        ),
        MangaDto(
            idString = "3",
            title = "Attack on Titan",
            subTitle = "",
            description = "In a world where humanity lives inside cities surrounded by enormous walls due to the Titans, gigantic humanoid creatures who devour humans seemingly without reason, a young boy named Eren Yeager dreams of seeing the world beyond the walls.",
            coverImage = "https://m.media-amazon.com/images/I/91M9VaZWxOL._SY522_.jpg",
            authors = listOf("Hajime Isayama"),
            genres = listOf("Action", "Drama", "Fantasy", "Horror"),
            chapters = 139,
            status = "Completed",
            nsfw = false,
            type = "japan",
            createdAt = System.currentTimeMillis() - 500 * 24 * 60 * 60 * 1000L,
            updatedAt = System.currentTimeMillis() - 90 * 24 * 60 * 60 * 1000L
        ),
        MangaDto(
            idString = "4",
            title = "My Hero Academia",
            subTitle = "",
            description = "In a world where people with superpowers (known as \"Quirks\") are the norm, Izuku Midoriya has dreams of one day becoming a Hero, despite being bullied by his classmates for not having a Quirk.",
            coverImage = "https://m.media-amazon.com/images/I/81S6Uc9uICL._SY522_.jpg",
            authors = listOf("Kohei Horikoshi"),
            genres = listOf("Action", "Comedy", "Superhero"),
            chapters = 340,
            status = "Ongoing",
            nsfw = false,
            type = "japan",
            createdAt = System.currentTimeMillis() - 300 * 24 * 60 * 60 * 1000L,
            updatedAt = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L
        ),
        MangaDto(
            idString = "5",
            title = "Demon Slayer",
            subTitle = "",
            description = "A youth begins a quest to fight demons and save his sister after finding his family slaughtered and his sister turned into a demon.",
            coverImage = "https://m.media-amazon.com/images/I/81ZNkhqRvVL._SY522_.jpg",
            authors = listOf("Koyoharu Gotouge"),
            genres = listOf("Action", "Fantasy", "Historical"),
            chapters = 205,
            status = "Completed",
            nsfw = false,
            type = "japan",
            createdAt = System.currentTimeMillis() - 400 * 24 * 60 * 60 * 1000L,
            updatedAt = System.currentTimeMillis() - 100 * 24 * 60 * 60 * 1000L
        ),
        MangaDto(
            idString = "6",
            title = "Jujutsu Kaisen",
            subTitle = "",
            description = "A boy swallows a cursed talisman - the finger of a demon - and becomes cursed himself. He enters a shaman school to be able to locate the demon's other body parts and thus exorcise himself.",
            coverImage = "https://m.media-amazon.com/images/I/81s8xJUzWGL._SY522_.jpg",
            authors = listOf("Gege Akutami"),
            genres = listOf("Action", "Fantasy", "Supernatural"),
            chapters = 180,
            status = "Ongoing",
            nsfw = false,
            type = "japan",
            createdAt = System.currentTimeMillis() - 250 * 24 * 60 * 60 * 1000L,
            updatedAt = System.currentTimeMillis() - 3 * 24 * 60 * 60 * 1000L
        ),
        MangaDto(
            idString = "7",
            title = "Dragon Ball",
            subTitle = "",
            description = "Dragon Ball tells the tale of a young warrior by the name of Son Goku, a young peculiar boy with a tail who embarks on a quest to become stronger and learns of the Dragon Balls, when, once all 7 are gathered, grant a wish.",
            coverImage = "https://m.media-amazon.com/images/I/71NBkn6cz+L._SY522_.jpg",
            authors = listOf("Akira Toriyama"),
            genres = listOf("Action", "Adventure", "Comedy", "Martial Arts"),
            chapters = 520,
            status = "Completed",
            nsfw = false,
            type = "japan",
            createdAt = System.currentTimeMillis() - 800 * 24 * 60 * 60 * 1000L,
            updatedAt = System.currentTimeMillis() - 600 * 24 * 60 * 60 * 1000L
        ),
        MangaDto(
            idString = "8",
            title = "Tokyo Ghoul",
            subTitle = "",
            description = "A college student is attacked by a ghoul, a being that feeds on human flesh. He later receives an organ transplant from the ghoul, becoming part ghoul himself and drawn into their society.",
            coverImage = "https://m.media-amazon.com/images/I/51uWo8NkCnL._SY445_SX342_.jpg",
            authors = listOf("Sui Ishida"),
            genres = listOf("Action", "Horror", "Supernatural", "Psychological"),
            chapters = 143,
            status = "Completed",
            nsfw = false,
            type = "japan",
            createdAt = System.currentTimeMillis() - 450 * 24 * 60 * 60 * 1000L,
            updatedAt = System.currentTimeMillis() - 350 * 24 * 60 * 60 * 1000L
        ),
        MangaDto(
            idString = "9",
            title = "Bleach",
            subTitle = "",
            description = "High school student Ichigo Kurosaki, who has the ability to see ghosts, gains soul reaper powers from Rukia Kuchiki and sets out to save the world from \"Hollows\".",
            coverImage = "https://m.media-amazon.com/images/I/51FEKMNpQlL._SY445_SX342_.jpg",
            authors = listOf("Tite Kubo"),
            genres = listOf("Action", "Adventure", "Supernatural"),
            chapters = 686,
            status = "Completed",
            nsfw = false,
            type = "japan",
            createdAt = System.currentTimeMillis() - 600 * 24 * 60 * 60 * 1000L,
            updatedAt = System.currentTimeMillis() - 400 * 24 * 60 * 60 * 1000L
        ),
        MangaDto(
            idString = "10",
            title = "Death Note",
            subTitle = "",
            description = "An intelligent high school student goes on a secret crusade to eliminate criminals from the world after discovering a notebook capable of killing anyone whose name is written into it.",
            coverImage = "https://m.media-amazon.com/images/I/51SkIDTd9rL._SY445_SX342_.jpg",
            authors = listOf("Tsugumi Ohba"),
            genres = listOf("Mystery", "Psychological", "Supernatural", "Thriller"),
            chapters = 108,
            status = "Completed",
            nsfw = false,
            type = "japan",
            createdAt = System.currentTimeMillis() - 550 * 24 * 60 * 60 * 1000L,
            updatedAt = System.currentTimeMillis() - 450 * 24 * 60 * 60 * 1000L
        ),
        MangaDto(
            idString = "11",
            title = "One Punch Man",
            subTitle = "",
            description = "The story of Saitama, a hero who can defeat any opponent with a single punch but seeks to find a worthy opponent after growing bored by a lack of challenge.",
            coverImage = "https://m.media-amazon.com/images/I/81VJBkUd-5L._SY522_.jpg",
            authors = listOf("ONE"),
            genres = listOf("Action", "Comedy", "Superhero"),
            chapters = 140,
            status = "Ongoing",
            nsfw = false,
            type = "japan",
            createdAt = System.currentTimeMillis() - 200 * 24 * 60 * 60 * 1000L,
            updatedAt = System.currentTimeMillis() - 5 * 24 * 60 * 60 * 1000L
        ),
        MangaDto(
            idString = "12",
            title = "Hunter x Hunter",
            subTitle = "",
            description = "Gon Freecss aspires to become a Hunter, an exceptional being capable of greatness. With his friends and his potential, he seeks for his father who left him when he was younger.",
            coverImage = "https://m.media-amazon.com/images/I/81nC4u9eYfL._SY522_.jpg",
            authors = listOf("Yoshihiro Togashi"),
            genres = listOf("Action", "Adventure", "Fantasy"),
            chapters = 390,
            status = "Ongoing",
            nsfw = false,
            type = "japan",
            createdAt = System.currentTimeMillis() - 350 * 24 * 60 * 60 * 1000L,
            updatedAt = System.currentTimeMillis() - 10 * 24 * 60 * 60 * 1000L
        )
    )

    override suspend fun getMangaList(page: Int, limit: Int): MangaListResponse {
        // Simulate network delay
        delay(1000)

        val startIndex = (page - 1) * limit
        val endIndex = minOf(startIndex + limit, mockMangaList.size)

        val mangaList = if (startIndex < mockMangaList.size) {
            mockMangaList.subList(startIndex, endIndex)
        } else {
            emptyList()
        }

        return MangaListResponse(
            data = mangaList,
            code = 200
        )
    }

    override suspend fun getMangaById(id: Int): MangaDto {
        // Simulate network delay
        delay(500)

        // Convert the integer ID to a string for comparison
        val idString = id.toString()
        return mockMangaList.find { it.idString == idString }
            ?: throw Exception("Manga not found")
    }

    override suspend fun searchManga(query: String): MangaListResponse {
        // Simulate network delay
        delay(800)

        val filteredList = mockMangaList.filter {
            it.title.contains(query, ignoreCase = true) ||
                    it.subTitle.contains(query, ignoreCase = true) ||
                    it.authors.any { author -> author.contains(query, ignoreCase = true) } ||
                    it.genres.any { genre -> genre.contains(query, ignoreCase = true) }
        }

        return MangaListResponse(
            data = filteredList,
            code = 200
        )
    }

    override suspend fun getLatestManga(): MangaListResponse {
        // Simulate network delay
        delay(700)

        // Return the most recent 5 manga (for mock purposes, we'll just take the first 5)
        val latestManga = mockMangaList.take(5)

        return MangaListResponse(
            data = latestManga,
            code = 200
        )
    }

    override suspend fun getChapters(mangaId: Int): ChapterListResponse {
        delay(500)
        return ChapterListResponse(emptyList()) // Mock implementation
    }

    override suspend fun getChapterImages(mangaId: Int, chapterNumber: String): ChapterImagesResponse {
        delay(500)
        return ChapterImagesResponse(emptyList()) // Mock implementation
    }
}
