package com.maeumdeungbul.quotes.data.repository

import com.maeumdeungbul.quotes.data.local.dao.MeditationSessionDao
import com.maeumdeungbul.quotes.data.local.entity.MeditationSessionEntity
import com.maeumdeungbul.quotes.domain.model.MeditationSession
import com.maeumdeungbul.quotes.domain.repository.MeditationRepository
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class OfflineMeditationRepository(
    private val dao: MeditationSessionDao,
) : MeditationRepository {

    override fun observeSessions(): Flow<List<MeditationSession>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun saveSession(session: MeditationSession): Long = dao.insert(
        MeditationSessionEntity(
            startedAt = session.startedAt.toEpochMilli(),
            plannedSeconds = session.plannedSeconds,
            actualSeconds = session.actualSeconds,
            type = session.type,
            soundId = session.soundId,
            completed = session.completed,
        ),
    )

    override suspend fun clearSessions() = dao.deleteAll()
}

private fun MeditationSessionEntity.toDomain() = MeditationSession(
    id = id,
    startedAt = Instant.ofEpochMilli(startedAt),
    plannedSeconds = plannedSeconds,
    actualSeconds = actualSeconds,
    type = type,
    soundId = soundId,
    completed = completed,
)
